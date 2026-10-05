package com.hope.chufala.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hope.chufala.model.entity.HotelReview;
import org.apache.ibatis.annotations.Mapper;

import java.util.Map;

/**
 * 酒店评价 Mapper。
 *
 * <p>除通用 CRUD 外，提供聚合计算新评分与评论数的方法，供提交评价后更新
 * hotel 表的冗余字段。
 *
 * @author 谢光湘
 */
@Mapper
public interface HotelReviewMapper extends BaseMapper<HotelReview> {

    /**
     * 按酒店聚合计算最新平均评分与评论数。
     *
     * @param hotelId 酒店 ID
     * @return 含 new_avg / new_count 的 Map
     */
    Map<String, Object> calculateScoreAndCount(Long hotelId);
}
