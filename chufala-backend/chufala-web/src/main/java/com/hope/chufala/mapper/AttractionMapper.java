package com.hope.chufala.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hope.chufala.model.entity.Attraction;
import com.hope.chufala.model.vo.PointVO;
import com.hope.chufala.model.vo.AttractionInfoVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 景点 Mapper。
 *
 * <p>自定义 SQL 由 AttractionMapper.xml 提供（按「与接口同路径」约定加载），
 * 支持分页检索与 AI 规划所需的按城市/ID 查询。
 *
 * @author 谢光湘
 */
@Mapper
public interface AttractionMapper extends BaseMapper<Attraction> {

    /**
     * 按评分降序、ID 降序游标查询景点，最多返回 size 条。
     *
     * @param size 查询条数；服务层传入请求页大小加一，用于判断是否有下一页
     * @param keyword 关键词
     * @param stars 星级
     * @param city 城市
     * @param tags 标签
     * @param lastId 上一页最后一条景点的 ID；首页为 null
     * @param lastValue 上一页最后一条景点的评分；评分为空时可为 null
     * @return 按评分和 ID 排列的景点列表
     */
    List<Attraction> selectAttractionPage(@Param("size") Integer size,
                                         @Param("keyword") String keyword, @Param("stars") Integer stars,
                                         @Param("city") String city, @Param("tags") List<String> tags,
                                         @Param("lastId") Long lastId,
                                         @Param("lastValue") Double lastValue);

    /**
     * 按城市查询景点（含图片等完整信息）。
     *
     * @param city 城市名称
     * @return 景点信息列表
     */
    List<AttractionInfoVO> selectAttractionByCity(String city);

    /**
     * 查询景点图片 URL 列表。
     *
     * @param attractionId 景点 ID
     * @return 图片 URL 列表
     */
    List<String> findAttractionImage(Long attractionId);

    /**
     * 按城市查询景点简单信息。
     *
     * @param name 城市名称
     * @return 景点信息列表
     */
    List<AttractionInfoVO> selectAttractionSimpleByCity(String name);

    /**
     * 按 ID 查询景点名称。
     *
     * @param id 景点 ID
     * @return 景点名称
     */
    String queryAttractionNameById(Long id);

    /**
     * 按 ID 查询景点坐标。
     *
     * @param id 景点 ID
     * @return 坐标
     */
    PointVO queryAttractionPositionById(Long id);

    /**
     * 按 ID 查询景点基本信息。
     *
     * @param id 景点 ID
     * @return 景点信息
     */
    AttractionInfoVO queryAttractionInfoById(Long id);
}
