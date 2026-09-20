package com.hope.chufala.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hope.chufala.domain.entity.HotelReview;
import org.apache.ibatis.annotations.Mapper;

import java.util.Map;

@Mapper
public interface HotelReviewMapper extends BaseMapper<HotelReview> {

    Map<String, Object> calculateScoreAndCount(Long hotelId);
}
