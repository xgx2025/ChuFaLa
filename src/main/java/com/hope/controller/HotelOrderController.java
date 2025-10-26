package com.hope.controller;


import com.hope.constant.ResultCode;
import com.hope.domain.dto.HotelOrderDTO;
import com.hope.domain.entity.HotelOrder;
import com.hope.domain.vo.PageResult;
import com.hope.domain.vo.Result;
import com.hope.service.IHotelOrderService;
import com.hope.utils.ThreadLocalUtil;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/hotelOrder")
public class HotelOrderController {
    @Autowired
    private IHotelOrderService hotelOrderService;
    @PostMapping("/create")
    public Result createHotelOrder(@RequestBody HotelOrderDTO hotelOrderDTO){
        Result result = hotelOrderService.createHotelOrder(hotelOrderDTO);
        String orderId = (String) result.getData();
        System.out.println(orderId);
        return Result.ok(orderId);
    }

    @GetMapping("/all")
    public Result getAllHotelOrder(@RequestParam Integer currentPage,@RequestParam Integer pageSize,@RequestParam String orderStatus) {
        Claims claims =  ThreadLocalUtil.get();
        Long userId = claims.get("userId", Long.class);
        PageResult<HotelOrder> pageResult = hotelOrderService.queryAllHotelOrder(userId, orderStatus, currentPage, pageSize);
        if (pageResult == null){
            return Result.fail(ResultCode.NOT_FOUND);
        }
        return Result.ok(pageResult);
    }

    @DeleteMapping("/delete/{orderId}")
    public Result deleteOrder(@PathVariable Long orderId) {
        boolean delete = hotelOrderService.deleteOrder(orderId);
        if (!delete){
            return Result.fail(ResultCode.SYSTEM_BUSY);
        }
        return Result.ok(null);
    }

    @PutMapping("/cancel/{orderId}")
    public Result cancelOrder(@PathVariable Long orderId) {
        boolean cancel = hotelOrderService.cancelOrder(orderId);
        if (!cancel){
            return Result.fail(ResultCode.SYSTEM_BUSY);
        }
        return Result.ok(null);
    }
}
