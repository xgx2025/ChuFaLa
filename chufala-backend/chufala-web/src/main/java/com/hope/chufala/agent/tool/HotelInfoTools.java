package com.hope.chufala.agent.tool;

import com.hope.chufala.model.entity.Hotel;
import com.hope.chufala.model.entity.Room;
import com.hope.chufala.service.IHotelService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * 酒店与房型信息查询工具，供智能助手在对话中按需调用。
 *
 * @author 谢光湘
 */
@Component
public class HotelInfoTools {
    @Autowired
    private IHotelService hotelService;

    /**
     * 按酒店 ID 查询酒店详情（含房型列表）。
     *
     * @param hotelId 酒店 ID
     * @return 酒店信息文本
     */
    @Tool(description = "根据酒店ID查询酒店详细信息，包括酒店名称、地址、星级、简介、酒店图片、房间ID等")
    public String getHotelRoomsByHotelId(@ToolParam(description = "酒店ID") Long hotelId) {
        Hotel hotel =hotelService.getHotelDetail(hotelId);
        return hotel.toString();
    }

    /**
     * 按房型 ID 查询房型信息。
     *
     * @param roomId 房型 ID
     * @return 房型信息文本
     */
    @Tool(description = "根据房间ID查询该房间信息")
    public String getRoomInfoByRoomId(@ToolParam(description = "房间ID") Long roomId) {
        Room room = hotelService.getRoomInfo(roomId);
        return room.toString();
    }
}
