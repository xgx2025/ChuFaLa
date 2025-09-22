package com.hope.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hope.domain.dto.HotelOrderDTO;
import com.hope.domain.entity.HotelOrder;
import com.hope.domain.vo.Result;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface HotelOrderMapper extends BaseMapper<HotelOrder> {

}
