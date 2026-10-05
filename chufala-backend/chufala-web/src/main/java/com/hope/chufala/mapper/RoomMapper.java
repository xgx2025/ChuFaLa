package com.hope.chufala.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hope.chufala.model.entity.Room;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 房型 Mapper。
 *
 * <p>自定义 SQL 由 RoomMapper.xml 提供；selectRoomPrice 是获取真实房价的入口
 * （IRoomService.selectRoomPrice 只是占位桩）。
 *
 * @author 谢光湘
 */
@Mapper
public interface RoomMapper extends BaseMapper<Room> {
    /**
     * 查询酒店下的房型列表。
     *
     * @param hotelId 酒店 ID
     * @return 房型列表
     */
    List<Room> selectRoomTypeList(Long hotelId);

    /**
     * 查询房型图片 URL 列表。
     *
     * @param roomTypeId 房型 ID
     * @return 图片 URL 列表
     */
    List<String> findRoomTypeImage(Long roomTypeId);

    /**
     * 查询房型价格。
     *
     * @param roomTypeId 房型 ID
     * @return 价格（元/晚）
     */
    Double selectRoomPrice(Long roomTypeId);

    /**
     * 按房型 ID 反查所属酒店 ID。
     *
     * @param roomTypeId 房型 ID
     * @return 酒店 ID
     */
    Long selectHotelIdByRoomTypeId(Long roomTypeId);

    /**
     * 按房型 ID 查询房型名称。
     *
     * @param roomTypeId 房型 ID
     * @return 房型名称
     */
    String selectNameById(Long roomTypeId);


}
