package com.hope.service;

import com.hope.domain.dto.AttractionPageQueryDTO;
import com.hope.domain.entity.Attraction;
import com.hope.domain.vo.AttractionInfo;
import com.hope.domain.vo.PageResult;

import java.util.List;

public interface IAttractionService {

    /**
     * 添加景点
     * @param attraction
     * @return
     */
    boolean addAttraction(Attraction attraction);

    /**
     * 分页查询景点
     * @return
     */
    PageResult<Attraction> queryAttraction(AttractionPageQueryDTO query);

    /**
     * 根据城市名称查询景点
     * @param name
     * @return
     */
    List<AttractionInfo> queryAttractionByCity(String name);

    Attraction getAttractionById(Long id);

}
