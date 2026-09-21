package com.hope.chufala.agent.tool;

import com.hope.chufala.model.entity.Hotel;
import com.hope.chufala.model.entity.Room;
import com.hope.chufala.service.IHotelService;
import com.hope.chufala.service.IRoomService;
import org.checkerframework.checker.signature.qual.IdentifierOrArray;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class HotelInfoTools {
    @Autowired
    private IHotelService hotelService;

    @Tool(description = "根据酒店ID查询酒店详细信息，包括酒店名称、地址、星级、简介、酒店图片、房间ID等")
    public String getHotelRoomsByHotelId(@ToolParam(description = "酒店ID") Long hotelId) {
        Hotel hotel =hotelService.getHotelDetail(hotelId);
        return hotel.toString();
    }

    @Tool(description = "根据房间ID查询该房间信息")
    public String getRoomInfoByRoomId(@ToolParam(description = "房间ID") Long roomId) {
        Room room = hotelService.getRoomInfo(roomId);
        return room.toString();
    }
}
