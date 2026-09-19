package com.hope.chufala.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hope.chufala.domain.entity.Attraction;
import com.hope.chufala.domain.entity.Point;
import com.hope.chufala.domain.vo.AttractionInfo;
import org.apache.ibatis.annotations.Mapper;

import javax.annotation.Nullable;
import java.util.List;

@Mapper
public interface AttractionMapper extends BaseMapper<Attraction> {

    /**
     * 分页查询景点
     * @param offset 偏移量
     * @param size   页大小
     * @param keyword 关键词
     * @param stars 星级
     * @param city 城市
     * @param tags 标签
     * @return 景点列表
     */
    List<Attraction> selectAttractionPage(@Nullable Integer offset, @Nullable Integer size, @Nullable String keyword, @Nullable Integer stars, @Nullable String city, @Nullable List<String> tags);

    /**
     * 查询景点总数
     * @param keyword 关键词
     * @param stars 星级
     * @param city 城市
     * @param tags 标签
     * @return 景点总数
     */
    Long selectAttractionCount(@Nullable String keyword, @Nullable Integer stars, @Nullable String city, @Nullable List<String> tags);

    List<AttractionInfo> selectAttractionByCity(String city);

    List<String> findAttractionImage(Long attractionId);

    List<AttractionInfo> selectAttractionSimpleByCity(String name);

    String queryAttractionNameById(Long id);

    Point queryAttractionPositionById(Long id);

    AttractionInfo queryAttractionInfoById(Long id);
}
