package com.hope.chufala.service;

import com.hope.chufala.model.dto.AttractionPageQueryDTO;
import com.hope.chufala.model.entity.Attraction;
import com.hope.chufala.model.vo.AttractionInfoVO;
import com.hope.chufala.common.model.vo.PageResult;

import java.util.List;

/**
 * 景点服务。
 *
 * <p>提供景点新增、分页检索，以及面向 AI 行程规划的按城市/关键词轻量查询。
 *
 * @author 谢光湘
 */
public interface IAttractionService {

    /**
     * 新增景点。
     *
     * @param attraction 景点实体
     * @return 是否成功
     */
    boolean addAttraction(Attraction attraction);

    /**
     * 按评分和 ID 游标分页查询景点；前 3 页优先读取 Redis。
     *
     * @param query 筛选条件、页大小、用户坐标及可选游标
     * @return 分页结果；nextCursor 仅有下一页时返回
     */
    PageResult<Attraction> queryAttraction(AttractionPageQueryDTO query);

    /**
     * 按城市查询景点（含图片等完整展示信息）。
     *
     * @param name 城市名称
     * @return 景点信息列表
     */
    List<AttractionInfoVO> queryAttractionByCity(String name);
    /**
     * 按城市查询景点简单信息。
     *
     * @param name 城市名称
     * @return 景点信息列表
     */
    List<AttractionInfoVO> queryAttractionSimpleByCity(String name);

    /**
     * 按 ID 查询景点基本信息。
     *
     * @param id 景点 ID
     * @return 景点信息
     */
    AttractionInfoVO queryAttractionInfoById(Long id);

    /**
     * 按 ID 查询景点名称。
     *
     * @param id 景点 ID
     * @return 景点名称
     */
    String queryAttractionNameById(Long id);
    /**
     * 按 ID 查询景点详细信息。
     *
     * @param id 景点 ID
     * @return 景点实体
     */
    Attraction getAttractionById(Long id);

    /**
     * 按 ID 查询景点坐标。
     *
     * @param id 景点 ID
     * @return 坐标数组，格式为 [经度, 纬度]
     */
    Double[] queryAttractionPositionById(Long id);

    /**
     * 按关键词检索景点。
     *
     * @param keyword 关键词
     * @return 景点列表
     */
    List<Attraction> searchAttractionsByKeyword(String keyword);
}
