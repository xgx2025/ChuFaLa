package com.hope.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.hope.domain.dto.AttractionPageQueryDTO;
import com.hope.domain.entity.Attraction;
import com.hope.domain.vo.AttractionInfo;
import com.hope.domain.vo.PageResult;
import com.hope.mapper.AttractionMapper;
import com.hope.service.IAttractionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Random;


@Service
public class AttractionServiceImpl implements IAttractionService {

    @Autowired
    private AttractionMapper attractionMapper;
    @Override
    public boolean addAttraction(Attraction attraction) {
        return attractionMapper.insert(attraction)>0;
    }

    @Override
    public PageResult<Attraction> queryAttraction(AttractionPageQueryDTO query) {
        List<Attraction> attractions = attractionMapper.selectAttractionPage(query.getPage(), query.getSize(), query.getKeyword(), query.getStars(), query.getCity(), query.getTags());
        if (attractions == null){
            return null;
        }
        return new PageResult<>(attractions, (long) attractions.size(), query.getPage(), query.getSize(), null, null, null, null);
    }

    @Override
    public List<AttractionInfo> queryAttractionByCity(String city) {
        return attractionMapper.selectAttractionByCity(city);
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
}
