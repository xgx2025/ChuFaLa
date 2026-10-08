package com.hope.chufala.service.impl;

import com.hope.chufala.common.exception.LocationUnavailableException;
import com.hope.chufala.common.util.CoordinateTransformUtils;
import com.hope.chufala.common.util.Gcj02DistanceCalculator;
import com.hope.chufala.model.dto.HotelPageQueryDTO;
import com.hope.chufala.model.entity.Hotel;
import com.hope.chufala.model.entity.HotelReview;
import com.hope.chufala.model.entity.Room;
import com.hope.chufala.model.vo.HotelInfoVO;
import com.hope.chufala.common.model.vo.PageResult;
import com.hope.chufala.mapper.HotelMapper;
import com.hope.chufala.infra.HotelListCache;

import com.hope.chufala.mapper.HotelReviewMapper;
import com.hope.chufala.mapper.RoomMapper;
import com.hope.chufala.service.IHotelService;
import com.hope.chufala.util.CursorPaginationUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;

/**
 * 酒店服务实现。
 *
 * <p>酒店列表使用游标分页；评分和价格排序的首页优先读取 Redis，
 * 距离排序按用户坐标实时查询。
 * 距离计算统一走 {@link #calculateDistances}，注意其参数顺序为 (经度, 纬度)。
 *
 * @author 谢光湘
 */
@Service
public class HotelServiceImpl implements IHotelService {
    @Autowired
    private HotelMapper hotelMapper;
    @Autowired
    private RoomMapper roomMapper;
    @Autowired
    private HotelReviewMapper commentMapper;
    @Autowired
    private HotelListCache hotelListCache;

    private static final Logger log = LoggerFactory.getLogger(HotelServiceImpl.class);

    /**
     * 新增酒店。
     *
     * @param hotel 酒店实体
     * @return 是否成功
     */
    @Override
    @Transactional
    public boolean addHotel(Hotel hotel) {
        boolean inserted = hotelMapper.insert(hotel) > 0;
        if (inserted) hotelListCache.invalidateAfterCommit();
        return inserted;
    }

    /**
     * 按筛选条件和排序方式游标分页查询酒店，并计算本页酒店距离。
     *
     * <p>游标绑定筛选条件、排序方式及距离排序所用坐标。评分和价格排序的首页
     * 优先读取不含用户距离的缓存，命中后重算展示距离。缓存未命中时查询 size + 1 条。
     * 距离排序实时查库，游标保存 SQL 排序原值，避免展示值四舍五入后漏页。
     *
     * @param query 筛选条件、排序方式、页大小、用户坐标及可选游标
     * @return 本页酒店、hasMore 和可选 nextCursor
     */
    @Override
    public PageResult<Hotel> queryHotelsByScoreRank(HotelPageQueryDTO query) {
        int size = CursorPaginationUtils.size(query.getSize(), 10);
        if (query.getUserLng() == null || query.getUserLat() == null) {
            throw new LocationUnavailableException("无法获取用户当前位置，请重试！");
        }
        String sort = query.getSort();
        String cursorSort = "price-asc".equals(sort) || "price-desc".equals(sort) || "distance".equals(sort)
                ? sort : "rating";
        CoordinateTransformUtils.Point point = CoordinateTransformUtils.bd09ToWgs84(query.getUserLng(), query.getUserLat());
        List<String> facilities = query.getFacilities() == null ? List.of()
                : query.getFacilities().stream().sorted().toList();
        String scope = CursorPaginationUtils.scope(query.getStars(), query.getCity(),
                query.getMinPrice(), query.getMaxPrice(), facilities,
                "distance".equals(cursorSort) ? point.getLatitude() : null,
                "distance".equals(cursorSort) ? point.getLongitude() : null);
        CursorPaginationUtils.Cursor cursor = query.getCursor() == null || query.getCursor().isBlank()
                ? null : CursorPaginationUtils.decode(query.getCursor(), cursorSort, scope);
        String cacheKey = cursor == null && !"distance".equals(cursorSort)
                ? hotelListCache.firstPageKey(cursorSort, scope, size) : null;
        PageResult<Hotel> cached = cacheKey == null ? null : hotelListCache.get(cacheKey);
        if (cached != null) {
            calculateDistances(cached.getData(), point.getLatitude(), point.getLongitude());
            return cached;
        }
        List<Hotel> rows = hotelMapper.selectByScoreRankPage(size + 1, query.getStars(), query.getCity(),
                query.getMaxPrice(), query.getMinPrice(), query.getFacilities(), sort,
                point.getLatitude(), point.getLongitude(),
                cursor == null ? null : cursor.id(), cursor == null ? null : cursor.value());
        boolean hasMore = rows.size() > size;
        List<Hotel> hotels = new ArrayList<>(rows.subList(0, Math.min(size, rows.size())));
        PageResult<Hotel> result = new PageResult<>();
        result.setData(hotels);
        result.setSize(size);
        result.setHasMore(hasMore);
        if (hasMore) {
            Hotel last = hotels.get(hotels.size() - 1);
            Double value;
            if ("distance".equals(cursorSort)) {
                value = last.getCursorSortValue() == null ? 1.0E15 : last.getCursorSortValue();
            } else if ("price-asc".equals(cursorSort)) {
                value = last.getPrice();
            } else if ("price-desc".equals(cursorSort)) {
                value = last.getPrice();
            } else {
                value = last.getOverallRating();
            }
            result.setNextCursor(CursorPaginationUtils.encode(cursorSort, scope, value, last.getId()));
        }
        if (cacheKey != null) hotelListCache.put(cacheKey, result);
        calculateDistances(hotels, point.getLatitude(), point.getLongitude());
        return result;
    }

    // 提取距离计算为单独方法，便于复用
    /**
     * 计算每个酒店与用户的距离（公里，保留一位小数），写回 Hotel.distance。
     *
     * @param hotels  酒店列表
     * @param userLat 用户纬度
     * @param userLng 用户经度
     */
    private void calculateDistances(List<Hotel> hotels, Double userLat, Double userLng) {
        for (Hotel hotel : hotels) {
            // ⚠️ 参数顺序：签名是 calculateDistance(lon1, lat1, lon2, lat2)，
            // 原来传的是 (userLat, userLng, hotelLat, hotelLng)，等于把纬度塞进了 lat1 的位置、
            // 经度（如 121.47°）当成了纬度去算 —— 纬度超过 ±90° 在几何上无意义，
            // 算出来的不是真正的大圆距离。
            // 实测（用户坐标取北京天安门，酒店坐标取库中真实值）：
            //   同城 北京青石板巷  正确 5.4km → 错误 2.6km（-52%）
            //   同城 北京古北禧玥  正确 4.3km → 错误 5.0km（+16%）
            //   跨城 上海          正确 1067.9km → 错误 731.9km（-31%）
            //   跨城 杭州          正确 1123.5km → 错误 655.4km（-42%）
            // 修复后与 SQL 里 Haversine 的排序结果完全一致。
            double distance = Gcj02DistanceCalculator.calculateDistance(
                    userLng, userLat,
                    hotel.getLongitude(), hotel.getLatitude()
            );
            double formattedDistance = Math.round(distance * 10) / 10.0;    //保留一位小数
            hotel.setDistance(formattedDistance);
        }
    }


    //提交评论并更新酒店评分（带事务）
    /**
     * 提交评价，并在同一事务内更新酒店的评分与评论数冗余字段。
     *
     * @param review 评价内容
     */
    @Override
    @Transactional
    public void submitReview(HotelReview review) {
        // 1. 插入评论
        commentMapper.insert(review);
        log.info("评论插入成功，hotelId={}", review.getHotelId());

        // 2. 计算该酒店的新评分和评论数
        Map<String, Object> scoreInfo = commentMapper.calculateScoreAndCount(review.getHotelId());
        Double newAvgScore = Double.valueOf(scoreInfo.get("new_avg").toString());
        Integer newCommentCount = Integer.valueOf(scoreInfo.get("new_count").toString());

        // 3. 更新酒店表的冗余字段
        hotelMapper.updateScoreAndCount(review.getHotelId(), newAvgScore, newCommentCount);
        hotelListCache.invalidateAfterCommit();
        log.info("酒店评分更新成功，hotelId={}, 新评分={}", review.getHotelId(), newAvgScore);

    }

    /**
     * 查询酒店详情（含图片与房型列表，房型图片逐个补全）。
     *
     * @param id 酒店 ID
     * @return 酒店详情
     */
    @Override
    public Hotel getHotelDetail(Long id) {
        Hotel hotel = hotelMapper.selectById(id);
        List<String> imageList = hotelMapper.findHotelImage(id);
        hotel.setMainImage(imageList.get(0));
        hotel.setOtherImages(imageList.subList(1,imageList.size()));
        List<Room> roomList = roomMapper.selectRoomTypeList(id);
        roomList.forEach(roomType -> {
            List<String> images = roomMapper.findRoomTypeImage(roomType.getId());
            roomType.setImageList(images);
        });
        hotel.setRoomList(roomList);
        return hotel;
    }

    /**
     * 查询房型信息。
     *
     * @param id 房型 ID
     * @return 房型
     */
    @Override
    public Room getRoomInfo(Long id) {
        return roomMapper.selectById(id);
    }

    /**
     * 按城市查询酒店信息。
     *
     * @param city 城市名称
     * @return 酒店信息列表
     */
    @Override
    public List<HotelInfoVO> findHotelByCity(String city) {
        return hotelMapper.findHotelByCity(city);
    }

    /**
     * 按城市查询酒店简单信息。
     *
     * @param city 城市名称
     * @return 酒店信息列表
     */
    @Override
    public List<HotelInfoVO> findHotelSimpleByCity(String city){
        return hotelMapper.findHotelSimpleByCity(city);
    }

    /**
     * 按 ID 查询酒店简单信息。
     *
     * @param id 酒店 ID
     * @return 酒店信息
     */
    @Override
    public HotelInfoVO findHotelSimpleById(Long id) {
        return hotelMapper.findHotelSimpleById(id);
    }

//    @Override
//    public List<HotelInfoVO> findHotelByCity(String city) {
//        QueryWrapper<HotelInfoVO> queryWrapper = new QueryWrapper<>();
//        queryWrapper.like("address", city);
//        return hotelMapper.selectList(queryWrapper);
//    }


}
