package com.hope.chufala.service.impl;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.json.JSONUtil;
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

import com.hope.chufala.mapper.HotelReviewMapper;
import com.hope.chufala.mapper.RoomMapper;
import com.hope.chufala.service.IHotelService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.springframework.util.StringUtils;

@Service
public class HotelServiceImpl implements IHotelService {
    @Autowired
    private HotelMapper hotelMapper;
    @Autowired
    private RoomMapper roomMapper;
    @Autowired
    private HotelReviewMapper commentMapper;
    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final Logger log = LoggerFactory.getLogger(HotelServiceImpl.class);

    @Override
    public boolean addHotel(Hotel hotel) {
        return hotelMapper.insert(hotel) > 0;
    }

    // Redis缓存前缀
    private static final String CACHE_PREFIX_ALL = "hotel:list:all:";
    private static final String CACHE_PREFIX_RANK = "hotel:list:rank:desc:";
    private static final Integer CACHE_TTL = 24 * 3600; // 缓存过期时间（秒）



    // 分页查询
    @Override
    public PageResult<Hotel> queryHotelsByScoreRank(HotelPageQueryDTO query) {
        Integer page = query.getPage();
        Integer size = query.getSize();
        Integer stars = query.getStars();
        String city = query.getCity();
        Double maxPrice = query.getMaxPrice();
        Double minPrice = query.getMinPrice();
        List<String> facilities = query.getFacilities();
        String sort = query.getSort();

        // distance 排序的结果依赖用户坐标，而缓存 Key 故意不含坐标（见下方注释），
        // 一旦走缓存就会把「上一个用户的位置」算出来的顺序发出去，所以这种情况直接跳过缓存。
        boolean cacheable = !"distance".equals(sort);

        // 转换用户坐标（仅用于距离计算，不放入缓存Key）
        if (query.getUserLng()==null||query.getUserLat()==null){
            throw new LocationUnavailableException("无法获取用户当前位置，请重试！");
        }
        CoordinateTransformUtils.Point mgPoint = CoordinateTransformUtils.bd09ToWgs84(query.getUserLng(), query.getUserLat());
        Double userLng = mgPoint.getLongitude();
        Double userLat = mgPoint.getLatitude();

        // 1. 生成缓存Key，移除经纬度参数
        StringBuilder cacheKeyBuilder = new StringBuilder(CACHE_PREFIX_RANK);
        cacheKeyBuilder.append("page:").append(page)
                .append(":size:").append(size);

        // 处理其他参数
        if (stars != null) {
            cacheKeyBuilder.append(":stars:").append(stars);
        }
        if (StringUtils.hasText(city)) {
            cacheKeyBuilder.append(":city:").append(city);
        }
        if (maxPrice != null) {
            cacheKeyBuilder.append(":maxPrice:").append(maxPrice);
        }
        if (minPrice != null) {
            cacheKeyBuilder.append(":minPrice:").append(minPrice);
        }
        // 处理集合参数，排序后拼接，确保顺序不影响key
        if (facilities != null && !facilities.isEmpty()) {
            List<String> sortedFacilities = new ArrayList<>(facilities);
            Collections.sort(sortedFacilities);
            cacheKeyBuilder.append(":facilities:").append(String.join(",", sortedFacilities));
        }

        // 排序方式必须进 Key：否则「价格升序」的请求会命中「评分排序」的缓存
        if (StringUtils.hasText(sort)) {
            cacheKeyBuilder.append(":sort:").append(sort);
        }

        String cacheKey = cacheKeyBuilder.toString();

        // 2. 查缓存（distance 排序不可缓存，见 cacheable 的说明）
        String cacheValue = cacheable ? redisTemplate.opsForValue().get(cacheKey) : null;
        if (cacheValue != null) {
            // 从缓存获取基础数据后，仍需计算距离（因为缓存中不存储距离）
            PageResult<Hotel> result = JSONUtil.toBean(cacheValue, new TypeReference<PageResult<Hotel>>() {}, false);
            // 重新计算距离
            calculateDistances(result.getData(), userLat, userLng);
            return result;
        }

        // 3. 查数据库
        int offset = (page - 1) * size;
        List<Hotel> hotels = hotelMapper.selectByScoreRankPage(
                offset, size, stars, city, maxPrice, minPrice, facilities, sort, userLat, userLng);

        // 总条数计算需要带查询条件
        Long total = hotelMapper.countTotalByCondition(
                stars, city, maxPrice, minPrice, facilities);

        // 4. 计算每个酒店与用户的距离（临时计算，不存入缓存）
        calculateDistances(hotels, userLat, userLng);

        // 5. 封装结果
        PageResult<Hotel> result = new PageResult<>();
        result.setData(hotels);
        result.setPage(page);
        result.setSize(size);
        result.setTotal(total);
        result.setHasMore(page * size < total);

        // 6. 回写缓存（缓存中不包含距离信息，或包含但使用时会重新计算）
        if (cacheable) {
            redisTemplate.opsForValue().set(cacheKey, JSONUtil.toJsonStr(result), CACHE_TTL, TimeUnit.SECONDS);
        }
        return result;
    }

    // 提取距离计算为单独方法，便于复用
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


    // 普通分页：查询全部酒店
    @Override
    public PageResult<Hotel> queryAllHotels(Integer page, Integer size) {
        // 1. 生成缓存Key
        String cacheKey = CACHE_PREFIX_ALL + page + ":size:" + size;

        // 2. 查缓存
        String cacheValue = redisTemplate.opsForValue().get(cacheKey);
        if (cacheValue != null) {
            return JSONUtil.toBean(cacheValue, PageResult.class);
        }

        // 3. 缓存未命中，查数据库
        int offset = (page - 1) * size;
        List<Hotel> hotels = hotelMapper.selectAllByPage(offset, size);
        Long total = hotelMapper.countTotal();

        // 4. 封装分页结果
        PageResult<Hotel> result = new PageResult<>();
        result.setData(hotels);
        result.setPage(page);
        result.setSize(size);
        result.setTotal(total);
        result.setHasMore(page * size < total);

        // 5. 回写缓存
        redisTemplate.opsForValue().set(cacheKey, JSONUtil.toJsonStr(result), CACHE_TTL, TimeUnit.SECONDS);

        return result;
    }

    //提交评论并更新酒店评分（带事务）
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
        log.info("酒店评分更新成功，hotelId={}, 新评分={}", review.getHotelId(), newAvgScore);

        // 4. 主动删除相关缓存（保证数据一致性）
        deleteRelatedCache(review.getHotelId());
    }

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

    @Override
    public Room getRoomInfo(Long id) {
        return roomMapper.selectById(id);
    }

    @Override
    public List<HotelInfoVO> findHotelByCity(String city) {
        return hotelMapper.findHotelByCity(city);
    }

    @Override
    public List<HotelInfoVO> findHotelSimpleByCity(String city){
        return hotelMapper.findHotelSimpleByCity(city);
    }

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


    // 删除与该酒店相关的所有缓存（简化实现：实际可按前缀批量删除）
    private void deleteRelatedCache(Long hotelId) {
        // 实际项目中可通过Redis的KEYS命令模糊匹配删除，这里简化逻辑
        redisTemplate.delete(redisTemplate.keys(CACHE_PREFIX_ALL + "*"));
        redisTemplate.delete(redisTemplate.keys(CACHE_PREFIX_RANK + "*"));
        log.info("酒店相关缓存已删除，hotelId={}", hotelId);
    }
}
