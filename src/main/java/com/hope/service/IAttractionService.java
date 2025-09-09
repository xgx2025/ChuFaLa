package com.hope.service;

import com.hope.domain.entity.Attraction;

public interface IAttractionService {

    /**
     * 添加景点
     * @param attraction
     * @return
     */
    boolean addAttraction(Attraction attraction);
}
