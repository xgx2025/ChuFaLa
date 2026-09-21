package com.hope.chufala.service;

import com.hope.chufala.model.dto.AttractionPageQueryDTO;
import com.hope.chufala.model.entity.Attraction;
import com.hope.chufala.model.vo.AttractionInfo;
import com.hope.chufala.common.model.vo.PageResult;

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
    /**
     * 根据城市名称查询景点简单信息
     * @param name
     * @return
     */
    List<AttractionInfo> queryAttractionSimpleByCity(String name);

    /**
     * 根据id查询景点基本信息
     * @param id
     * @return
     */
    AttractionInfo queryAttractionInfoById(Long id);

    String queryAttractionNameById(Long id);
    /**
     * 根据id查询景点详细信息
     * @param id
     * @return
     */
    Attraction getAttractionById(Long id);

    Double[] queryAttractionPositionById(Long id);

    /**
     * 根据关键词查询景点基本信息
     * @param keyword
     * @return
     */
    List<Attraction> searchAttractionsByKeyword(String keyword);
}
