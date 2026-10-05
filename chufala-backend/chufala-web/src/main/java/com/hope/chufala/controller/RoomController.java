package com.hope.chufala.controller;


import com.hope.chufala.common.constant.ResultCode;
import com.hope.chufala.model.entity.Room;
import com.hope.chufala.common.model.vo.Result;
import com.hope.chufala.service.IHotelService;
import com.hope.chufala.service.IRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

/**
 * 房型接口。
 *
 * <p>提供房型信息、按入住区间计算总价（含价格签名）与可售房量查询。
 *
 * @author 谢光湘
 */
@RestController
@RequestMapping("/rooms")
public class RoomController {
    @Autowired
    private IHotelService hotelService;
    @Autowired
    private IRoomService roomService;
    /**
     * 查询房型信息。
     *
     * @param id 房型 ID
     * @return 房型信息
     */
    @GetMapping("/{id}")
    public Result getRoomInfo(@PathVariable Long id) {
        Room roomInfo = hotelService.getRoomInfo(id);
        if (roomInfo == null){
            return Result.fail(ResultCode.NOT_FOUND);
        }
        return Result.ok(roomInfo);
    }

    /**
     * 计算房型总价并返回价格签名。
     *
     * @param id        房型 ID
     * @param checkIn   入住日期（yyyy-MM-dd）
     * @param checkOut  离店日期（yyyy-MM-dd）
     * @param roomCount 房间数
     * @return 含订单原始数据与签名的键值对
     */
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

    /**
     * 查询指定入住区间内的可售房量。
     *
     * @param id       房型 ID
     * @param checkIn  入住日期（yyyy-MM-dd）
     * @param checkOut 离店日期（yyyy-MM-dd）
     * @return 可售房量
     */
    @GetMapping("/{id}/availability")
    public Result getAvailableStock(@PathVariable Long id,
                                    @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate checkIn,
                                    @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate checkOut) {
        return Result.ok(roomService.getAvailableStock(id, checkIn, checkOut));
    }

}
