package com.hope.service.impl;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.json.JSONUtil;
import com.hope.constant.ResultCode;
import com.hope.domain.dto.HotelOrderDTO;
import com.hope.domain.dto.HotelPageQueryDTO;
import com.hope.domain.entity.Hotel;
import com.hope.domain.entity.HotelOrder;
import com.hope.domain.entity.HotelReview;
import com.hope.domain.entity.RoomType;
import com.hope.domain.vo.PageResult;
import com.hope.domain.vo.Result;
import com.hope.mapper.HotelMapper;
import com.hope.mapper.HotelOrderMapper;
import com.hope.mapper.HotelReviewMapper;
import com.hope.mapper.RoomTypeMapper;
import com.hope.service.IHotelService;
import com.hope.utils.CoordinateTransformUtil;
import com.hope.utils.RedisWorker;
import com.hope.utils.SignaturePriceUtil;
import com.hope.utils.ThreadLocalUtil;
import io.jsonwebtoken.Claims;
import jakarta.annotation.Resource;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.springframework.util.StringUtils;

import javax.swing.*;

@Service
public class HotelServiceImpl implements IHotelService {
    @Autowired
    private HotelMapper hotelMapper;
    @Autowired
    private RoomTypeMapper roomTypeMapper;
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

        // 转换用户坐标（仅用于距离计算，不放入缓存Key）
        CoordinateTransformUtil.Point mgPoint = CoordinateTransformUtil.bd09ToWgs84(query.getUserLng(), query.getUserLat());
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

        String cacheKey = cacheKeyBuilder.toString();

        // 2. 查缓存
        String cacheValue = redisTemplate.opsForValue().get(cacheKey);
        if (cacheValue != null) {
            // 从缓存获取基础数据后，仍需计算距离（因为缓存中不存储距离）
            PageResult<Hotel> result = JSONUtil.toBean(cacheValue,   new TypeReference<PageResult<Hotel>>() {}, false);
            // 重新计算距离
            calculateDistances(result.getData(), userLat, userLng);
            return result;
        }

        // 3. 查数据库
        int offset = (page - 1) * size;
        List<Hotel> hotels = hotelMapper.selectByScoreRankPage(
                offset, size, stars, city, maxPrice, minPrice, facilities);

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
        redisTemplate.opsForValue().set(cacheKey, JSONUtil.toJsonStr(result), CACHE_TTL, TimeUnit.SECONDS);
        return result;
    }

    // 提取距离计算为单独方法，便于复用
    private void calculateDistances(List<Hotel> hotels, Double userLat, Double userLng) {
        for (Hotel hotel : hotels) {
            double distance = DistanceUtil.calculateDistance(
                    userLat, userLng,
                    hotel.getLatitude(), hotel.getLongitude()
            );
            double formattedDistance = Math.round(distance * 10) / 10.0;
            hotel.setDistance(formattedDistance);
        }
    }


    // 距离计算工具类
    class DistanceUtil {
        private static final double EARTH_RADIUS = 6371.0; // 地球半径，单位公里

        /**
         * 使用Haversine公式计算两点间距离
         * @param lat1 纬度1
         * @param lon1 经度1
         * @param lat2 纬度2
         * @param lon2 经度2
         * @return 距离（公里）
         */
        public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
            // 将角度转为弧度
            double radLat1 = Math.toRadians(lat1);
            double radLon1 = Math.toRadians(lon1);
            double radLat2 = Math.toRadians(lat2);
            double radLon2 = Math.toRadians(lon2);

            // 差值
            double dLat = radLat2 - radLat1;
            double dLon = radLon2 - radLon1;

            // Haversine公式
            double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                    + Math.cos(radLat1) * Math.cos(radLat2)
                    * Math.sin(dLon / 2) * Math.sin(dLon / 2);
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

            // 计算距离（公里）
            return EARTH_RADIUS * c;
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
        List<RoomType> roomList =roomTypeMapper.selectRoomTypeList(id);
        roomList.forEach(roomType -> {
            List<String> images = roomTypeMapper.findRoomTypeImage(roomType.getId());
            roomType.setImageList(images);
        });
        hotel.setRoomList(roomList);
        return hotel;
    }

    @Override
    public RoomType getRoomInfo(Long id) {
        return roomTypeMapper.selectById(id);
    }



    // 删除与该酒店相关的所有缓存（简化实现：实际可按前缀批量删除）
    private void deleteRelatedCache(Long hotelId) {
        // 实际项目中可通过Redis的KEYS命令模糊匹配删除，这里简化逻辑
        redisTemplate.delete(redisTemplate.keys(CACHE_PREFIX_ALL + "*"));
        redisTemplate.delete(redisTemplate.keys(CACHE_PREFIX_RANK + "*"));
        log.info("酒店相关缓存已删除，hotelId={}", hotelId);
    }
}
