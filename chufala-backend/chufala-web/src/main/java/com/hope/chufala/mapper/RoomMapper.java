package com.hope.chufala.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hope.chufala.model.entity.Room;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface RoomMapper extends BaseMapper<Room> {
    List<Room> selectRoomTypeList(Long hotelId);

    List<String> findRoomTypeImage(Long roomTypeId);

    Double selectRoomPrice(Long roomTypeId);

    boolean updateStock(Long roomTypeId,int roomCount);

    Long selectHotelIdByRoomTypeId(Long roomTypeId);

    String selectNameById(Long roomTypeId);


}
