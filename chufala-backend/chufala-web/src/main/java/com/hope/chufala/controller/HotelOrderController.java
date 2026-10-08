package com.hope.chufala.controller;



import com.hope.chufala.common.constant.ResultCode;
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

/**
 * 酒店订单接口。
 *
 * <p>提供下单、订单分页查询、删除与取消；用户身份从 ThreadLocal 中的 JWT claims 获取。
 *
 * @author 谢光湘
 */
@Slf4j
@RestController
@RequestMapping("/hotelOrders")
public class HotelOrderController {
    @Autowired
    private IHotelOrderService hotelOrderService;

    /**
     * 创建酒店订单。
     *
     * @param hotelOrderDTO 下单参数（含被签名的原始数据）
     * @return 新订单的业务订单号
     */
    @PostMapping
    public Result createHotelOrder(@RequestBody HotelOrderDTO hotelOrderDTO){
        Claims claims = ThreadLocalUtils.get();
        Long userId = claims.get("userId", Long.class);
        String orderId = hotelOrderService.createHotelOrder(hotelOrderDTO,userId);
        return Result.ok(orderId);
    }

    /**
     * 按下单时间和 ID 游标分页查询当前用户的酒店订单。
     *
     * @param size        每页大小，默认为 10，最大为 50
     * @param cursor      上一页返回的 nextCursor；首页不传
     * @param orderStatus 订单状态筛选；后续页需与首页一致
     * @return 订单列表、是否有下一页及下一页游标；total 仅首页返回
     */
    @GetMapping
    public Result getHotelOrderPage(@RequestParam(defaultValue = "10") Integer size,
                                   @RequestParam(required = false) String cursor,
                                   @RequestParam(defaultValue = "all") String orderStatus) {
        Claims claims =  ThreadLocalUtils.get();
        Long userId = claims.get("userId", Long.class);
        PageResult<HotelOrder> pageResult = hotelOrderService.getHotelOrderByUserIdPage(userId, orderStatus, size, cursor);
        return Result.ok(pageResult);
    }

    /**
     * 按业务订单号查询当前用户未删除的订单详情。
     *
     * @param orderId 业务订单号
     * @return 含酒店名称和地址的订单；不存在或不属于当前用户时返回 NOT_FOUND
     */
    @GetMapping("/{orderId}")
    public Result getHotelOrderDetail(@PathVariable Long orderId) {
        Claims claims = ThreadLocalUtils.get();
        Long userId = claims.get("userId", Long.class);
        HotelOrder order = hotelOrderService.getHotelOrderDetail(userId, orderId);
        return order == null ? Result.fail(ResultCode.NOT_FOUND) : Result.ok(order);
    }

    /**
     * 删除订单（校验归属）。
     *
     * @param orderId 业务订单号
     * @return 操作结果
     */
    @DeleteMapping("/{orderId}")
    public Result deleteOrder(@PathVariable Long orderId) {
        Claims claims = ThreadLocalUtils.get();
        Long userId = claims.get("userId", Long.class);
        hotelOrderService.deleteOrder(orderId, userId);
        return Result.ok(null);
    }

    /**
     * 取消订单（回补库存）。
     *
     * @param orderId 业务订单号
     * @return 操作结果
     */
    @PutMapping("/cancel/{orderId}")
    public Result cancelOrder(@PathVariable Long orderId) {
        Claims claims = ThreadLocalUtils.get();
        Long userId = claims.get("userId", Long.class);
        hotelOrderService.cancelOrder(orderId, userId);
        return Result.ok(null);
    }
}
