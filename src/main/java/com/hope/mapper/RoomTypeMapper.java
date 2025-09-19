package com.hope.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hope.domain.entity.RoomType;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface RoomTypeMapper extends BaseMapper<RoomType> {
    List<RoomType> selectRoomTypeList(Long hotelId);

    List<String> findRoomTypeImage(Long roomTypeId);

    Double selectRoomPrice(Long roomTypeId);


}
