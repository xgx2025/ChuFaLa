package com.hope.chufala.controller;


import com.hope.chufala.common.constant.ResultCode;
import com.hope.chufala.domain.entity.Room;
import com.hope.chufala.common.domain.vo.Result;
import com.hope.chufala.service.IHotelService;
import com.hope.chufala.service.IRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/rooms")
public class RoomController {
    @Autowired
    private IHotelService hotelService;
    @Autowired
    private IRoomService roomService;
    @GetMapping("/{id}")
    public Result getRoomInfo(@PathVariable Long id) {
        Room roomInfo = hotelService.getRoomInfo(id);
        if (roomInfo == null){
            return Result.fail(ResultCode.NOT_FOUND);
        }
        return Result.ok(roomInfo);
    }

    @GetMapping("/totalPrice")
    public Result getRoomTotalPrice(@RequestParam("id") Long id,
                                    @RequestParam("checkIn") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate checkIn,
                                    @RequestParam("checkOut") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate checkOut,
                                    @RequestParam("roomCount") int roomCount){
        Map<String,String> result = roomService.calculateRoomTotalPrice(id,checkIn,checkOut,roomCount);
        if (result == null){
            return Result.fail(ResultCode.NOT_FOUND);
        }
        return Result.ok(result);
    }

}
