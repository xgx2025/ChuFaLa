package com.hope.chufala.service.impl;

import com.hope.chufala.common.exception.LocationUnavailableException;
import com.hope.chufala.common.util.Gcj02DistanceCalculator;
import com.hope.chufala.model.dto.AttractionPageQueryDTO;
import com.hope.chufala.model.entity.Attraction;
import com.hope.chufala.model.vo.PointVO;
import com.hope.chufala.model.vo.AttractionInfoVO;
import com.hope.chufala.common.model.vo.PageResult;
import com.hope.chufala.mapper.AttractionMapper;
import com.hope.chufala.infra.AttractionListCache;
import com.hope.chufala.service.IAttractionService;
import com.hope.chufala.util.CursorPaginationUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.ArrayList;
import java.util.Random;

/**
 * 景点服务实现。
 *
 * <p>列表前 3 页优先读取 Redis，并按用户坐标实时计算距离（GCJ-02）；详情接口的评论数是随机数占位
 * （景点侧暂无真实评论数据）。
 *
 * @author 谢光湘
 */
@Slf4j
@Service
public class AttractionServiceImpl implements IAttractionService {

    @Autowired
    private AttractionMapper attractionMapper;
    @Autowired
    private AttractionListCache attractionListCache;
    /**
     * 新增景点。
     *
     * @param attraction 景点实体
     * @return 是否成功
     */
    @Override
    @Transactional
    public boolean addAttraction(Attraction attraction) {
        boolean inserted = attractionMapper.insert(attraction) > 0;
        if (inserted) attractionListCache.invalidateAfterCommit();
        return inserted;
    }

    /**
     * 按评分降序、ID 降序游标分页查询景点，并计算本页景点距离。
     *
     * <p>游标绑定关键词、星级、城市和标签条件。前 3 页优先读取不含用户距离的缓存，
     * 命中后按当前用户位置重算距离；深页直接查库。
     * 缓存未命中时查询 size + 1 条判断是否还有下一页，
     * 缺少用户坐标时抛出 LocationUnavailableException。
     *
     * @param query 筛选条件、页大小、用户坐标及可选游标
     * @return 本页景点、hasMore 和可选 nextCursor
     */
    @Override
    public PageResult<Attraction> queryAttraction(AttractionPageQueryDTO query) {
        log.info("开始查询景点列表----{}", query);
        int size = CursorPaginationUtils.size(query.getSize(), 12);
        if (query.getUserLng() == null || query.getUserLat() == null) {
            throw new LocationUnavailableException("无法获取用户当前位置，请重试！");
        }
        List<String> tags = query.getTags() == null ? List.of() : query.getTags().stream().sorted().toList();
        String scope = CursorPaginationUtils.scope(query.getKeyword(), query.getStars(), query.getCity(), tags);
        CursorPaginationUtils.Cursor cursor = query.getCursor() != null && !query.getCursor().isBlank()
                ? CursorPaginationUtils.decode(query.getCursor(), "rating", scope) : null;
        String cacheKey = attractionListCache.pageKey("rating", scope, size, cursor);
        PageResult<Attraction> cached = cacheKey == null ? null : attractionListCache.get(cacheKey);
        if (cached != null) {
            calculateDistances(cached.getData(), query.getUserLng(), query.getUserLat());
            return cached;
        }
        List<Attraction> rows = attractionMapper.selectAttractionPage(size + 1,
                query.getKeyword(), query.getStars(), query.getCity(), query.getTags(),
                cursor == null ? null : cursor.id(), cursor == null ? null : cursor.value());
        boolean hasMore = rows.size() > size;
        List<Attraction> attractions = new ArrayList<>(rows.subList(0, Math.min(size, rows.size())));

        PageResult<Attraction> result = new PageResult<>();
        result.setData(attractions);
        result.setSize(size);
        result.setHasMore(hasMore);
        if (hasMore) {
            Attraction last = attractions.get(attractions.size() - 1);
            result.setNextCursor(CursorPaginationUtils.encode("rating", scope, last.getRating(), last.getId(),
                    cursor == null ? 2 : Math.min(4, cursor.page() + 1)));
        }
        if (cacheKey != null) attractionListCache.put(cacheKey, result);
        calculateDistances(attractions, query.getUserLng(), query.getUserLat());
        return result;
    }

    /** 按当前请求的用户坐标计算展示距离，避免共享缓存包含其他用户的距离。 */
    private void calculateDistances(List<Attraction> attractions, Double userLng, Double userLat) {
        attractions.forEach(attraction -> {
            double distance = Gcj02DistanceCalculator.calculateDistance(
                    userLng, userLat, attraction.getLongitude(), attraction.getLatitude());
            attraction.setDistance(Math.round(distance * 10) / 10.0);
        });
    }

    /**
     * 按城市查询景点（含完整展示信息）。
     *
     * @param city 城市名称
     * @return 景点信息列表
     */
    @Override
    public List<AttractionInfoVO> queryAttractionByCity(String city) {
        return attractionMapper.selectAttractionByCity(city);

    }

    /**
     * 按城市查询景点简单信息。
     *
     * @param name 城市名称
     * @return 景点信息列表
     */
    @Override
    public List<AttractionInfoVO> queryAttractionSimpleByCity(String name) {
        return attractionMapper.selectAttractionSimpleByCity(name);
    }

    /**
     * 按 ID 查询景点基本信息。
     *
     * @param id 景点 ID
     * @return 景点信息
     */
    @Override
    public AttractionInfoVO queryAttractionInfoById(Long id) {
        return attractionMapper.queryAttractionInfoById(id);
    }

    /**
     * 按 ID 查询景点名称。
     *
     * @param id 景点 ID
     * @return 景点名称
     */
    @Override
    public String queryAttractionNameById(Long id) {
        return attractionMapper.queryAttractionNameById(id);
    }

    /**
     * 按 ID 查询景点详情（含图片；评论数为随机占位值）。
     *
     * @param id 景点 ID
     * @return 景点详情
     */
    @Override
    public Attraction getAttractionById(Long id) {
         Attraction attraction = attractionMapper.selectById(id);
         List<String> images = attractionMapper.findAttractionImage(id);
         attraction.setMainImage(images.get(0));
         attraction.setOtherImages(images.subList(1,images.size()));
         Random random = new Random();
         attraction.setReviewCount(random.nextInt(1000));
         return attraction;
    }

    /**
     * 按 ID 查询景点坐标。
     *
     * @param id 景点 ID
     * @return 坐标数组，格式为 [经度, 纬度]
     */
    @Override
    public Double[] queryAttractionPositionById(Long id) {
        PointVO point = attractionMapper.queryAttractionPositionById(id);
        Double[] position = new Double[2];
        position[0] = point.getLongitude();
        position[1] = point.getLatitude();
        return position;
    }

    /**
     * 按关键词检索景点（复用分页查询，仅传关键词）。
     *
     * @param keyword 关键词
     * @return 景点列表
     */
    @Override
    public List<Attraction> searchAttractionsByKeyword(String keyword) {
        return attractionMapper.selectAttractionPage(50, keyword, null, null, null, null, null);
    }
}
