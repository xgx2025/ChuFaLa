package com.hope.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hope.domain.entity.Hotel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface HotelMapper extends BaseMapper<Hotel> {

    // 普通分页：查询全部酒店（按ID排序）
    List<Hotel> selectAllByPage(
            @Param("offset") Integer offset,
            @Param("size") Integer size
    );

    // 普通分页：查询按评分排序的酒店
    List<Hotel> selectByScoreRankPage(
            @Param("offset") Integer offset,
            @Param("size") Integer size,
            @Param("stars") Integer stars,
            @Param("city") String city,
            @Param("maxPrice") Double maxPrice,
            @Param("minPrice") Double minPrice,
            @Param("facilities")List<String> facilities
    );

    // 游标分页：按评分排序（基于上一页最后一条数据）
    List<Hotel> selectByScoreRankCursor(
            @Param("lastAvgScore") Double lastAvgScore,
            @Param("lastHotelId") Long lastHotelId,
            @Param("size") Integer size
    );

    // 更新酒店评分和评论数
    void updateScoreAndCount(
            @Param("hotelId") Long hotelId,
            @Param("newAvgScore") Double newAvgScore,
            @Param("newCommentCount") Integer newCommentCount
    );

    // 查询全部酒店总数
    Long countTotal();
    // 查询符合条件的酒店总数
    Long countTotalByCondition(Integer stars, String city, Double maxPrice, Double minPrice, List<String> facilities);
}
