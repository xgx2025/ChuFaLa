package com.hope.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.hope.domain.entity.Attraction;
import com.hope.mapper.AttractionMapper;
import com.hope.service.IAttractionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AttractionServiceImpl implements IAttractionService {

    @Autowired
    private AttractionMapper attractionMapper;
    @Override
    public boolean addAttraction(Attraction attraction) {
        return attractionMapper.insert(attraction)>0;
    }
}
