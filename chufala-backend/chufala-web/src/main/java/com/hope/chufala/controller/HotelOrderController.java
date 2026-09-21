package com.hope.chufala.controller;



import com.hope.chufala.common.constant.ResultCode;
import com.hope.chufala.common.exception.InvalidSignatureException;
import com.hope.chufala.common.util.ThreadLocalUtils;
import com.hope.chufala.model.dto.HotelOrderDTO;
import com.hope.chufala.model.entity.HotelOrder;
import com.hope.chufala.common.model.vo.PageResult;
import com.hope.chufala.common.model.vo.Result;

import com.hope.chufala.service.IHotelOrderService;

import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/hotelOrders")
public class HotelOrderController {
    @Autowired
    private IHotelOrderService hotelOrderService;

    @PostMapping
    public Result createHotelOrder(@RequestBody HotelOrderDTO hotelOrderDTO){
        Claims claims = ThreadLocalUtils.get();
        Long userId = claims.get("userId", Long.class);
        String orderId = hotelOrderService.createHotelOrder(hotelOrderDTO,userId);
        return Result.ok(orderId);
    }

    @GetMapping
    public Result getAllHotelOrder(@RequestParam Integer currentPage,@RequestParam Integer pageSize,@RequestParam String orderStatus) {
        Claims claims =  ThreadLocalUtils.get();
        Long userId = claims.get("userId", Long.class);
        PageResult<HotelOrder> pageResult = hotelOrderService.getHotelOrderByUserIdPage(userId, orderStatus, currentPage, pageSize);
        if (pageResult == null){
            return Result.fail(ResultCode.NOT_FOUND);
        }
        return Result.ok(pageResult);
    }

    @DeleteMapping("/{orderId}")
    public Result deleteOrder(@PathVariable Long orderId) {
        try {
            hotelOrderService.deleteOrder(orderId);
        }catch(RuntimeException e){
            log.error("删除订单失败！",e);
            return Result.fail(ResultCode.SYSTEM_BUSY);
        }
        return Result.ok(null);
    }

    @PutMapping("/cancel/{orderId}")
    public Result cancelOrder(@PathVariable Long orderId) {
        try {
            hotelOrderService.cancelOrder(orderId);
        }catch (RuntimeException e) {
            log.error("取消订单失败！",e);
            return Result.fail(ResultCode.SYSTEM_BUSY);
        }
        return Result.ok(null);
    }
}
