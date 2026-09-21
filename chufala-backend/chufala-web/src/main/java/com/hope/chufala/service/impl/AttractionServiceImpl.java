package com.hope.chufala.service.impl;

import com.hope.chufala.common.exception.LocationUnavailableException;
import com.hope.chufala.common.util.Gcj02DistanceCalculator;
import com.hope.chufala.model.dto.AttractionPageQueryDTO;
import com.hope.chufala.model.entity.Attraction;
import com.hope.chufala.model.vo.PointVO;
import com.hope.chufala.model.vo.AttractionInfoVO;
import com.hope.chufala.common.model.vo.PageResult;
import com.hope.chufala.mapper.AttractionMapper;
import com.hope.chufala.service.IAttractionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.Random;

@Slf4j
@Service
public class AttractionServiceImpl implements IAttractionService {

    @Autowired
    private AttractionMapper attractionMapper;
    @Override
    public boolean addAttraction(Attraction attraction) {
        return attractionMapper.insert(attraction)>0;
    }

    /**
     * 查询景点列表
     * @param query 景点经纬度信息采用GCJ-02坐标(高德地图)
     * @return
     */
    @Override
    public PageResult<Attraction> queryAttraction(AttractionPageQueryDTO query) {
        log.info("开始查询景点列表----{}", query);
        //TODO 目前未实现排序功能
        List<Attraction> attractions = attractionMapper.selectAttractionPage(query.getOffset(), query.getSize(), query.getKeyword(), query.getStars(), query.getCity(), query.getTags());

        if (attractions == null){
            return null;
        }
        if(query.getUserLng() == null || query.getUserLat() == null){
            throw new LocationUnavailableException("无法获取用户当前位置，请重试！");
        }
        log.error("用户经度：{}, 用户纬度：{}", query.getUserLng(), query.getUserLat());
        //计算与用户的距离
        attractions.forEach(attraction -> {
            double distance = Gcj02DistanceCalculator.calculateDistance(
                    query.getUserLng(), query.getUserLat(),
                    attraction.getLongitude(),attraction.getLatitude()
            );
            double formattedDistance = Math.round(distance * 10) / 10.0;    //保留一位小数
            attraction.setDistance(formattedDistance);
        });

        long total = attractionMapper.selectAttractionCount(query.getKeyword(), query.getStars(), query.getCity(), query.getTags());
        boolean hasMore = (query.getOffset() + query.getSize()) < total;
        int totalPages = (int) Math.ceil((double) total / query.getSize());
        return new PageResult<>(attractions, total,totalPages,query.getOffset(), query.getSize(), null, null,hasMore);
    }

    @Override
    public List<AttractionInfoVO> queryAttractionByCity(String city) {
        return attractionMapper.selectAttractionByCity(city);

    }

    @Override
    public List<AttractionInfoVO> queryAttractionSimpleByCity(String name) {
        return attractionMapper.selectAttractionSimpleByCity(name);
    }

    @Override
    public AttractionInfoVO queryAttractionInfoById(Long id) {
        return attractionMapper.queryAttractionInfoById(id);
    }

    @Override
    public String queryAttractionNameById(Long id) {
        return attractionMapper.queryAttractionNameById(id);
    }

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

    @Override
    public Double[] queryAttractionPositionById(Long id) {
        PointVO point = attractionMapper.queryAttractionPositionById(id);
        Double[] position = new Double[2];
        position[0] = point.getLongitude();
        position[1] = point.getLatitude();
        return position;
    }

    @Override
    public List<Attraction> searchAttractionsByKeyword(String keyword) {
        return attractionMapper.selectAttractionPage(null, null, keyword,null, null, null);
    }
}
