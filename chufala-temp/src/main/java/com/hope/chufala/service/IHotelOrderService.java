package com.hope.chufala.service;

import com.hope.chufala.domain.dto.HotelOrderDTO;
import com.hope.chufala.domain.entity.HotelOrder;
import com.hope.chufala.domain.vo.PageResult;

import javax.annotation.Nullable;
import java.time.LocalDate;
import java.util.List;

public interface IHotelOrderService {
    HotelOrder getByOrderId(Long bizId);

    HotelOrder getByOrderIdAndUserId(Long orderId,Long userId);

    String createHotelOrder(HotelOrderDTO hotelOrderDTO,Long userId);

    void updateStatus(Long orderId, String paymentStatus);

    /**
     * 分页查询用户的酒店订单
     * @param userId 用户ID
     * @param orderStatus 订单状态
     * @param currentPage 当前页
     * @param pageSize 页面大小
     * @return 分页结果
     */
    PageResult<HotelOrder> getHotelOrderByUserIdPage(Long userId, String orderStatus,Integer currentPage, Integer pageSize);

    /**
     * 根据用户ID查询所有订单
     * @param userId 用户ID
     * @return 订单列表
     */
    List<HotelOrder> findHotelOrdersByUserIdWithConditions(Long userId, String orderStatus, @Nullable LocalDate bookTime);
    /**
     * 删除订单
     * @param orderId
     */
    void deleteOrder(Long orderId);

    void cancelOrder(Long orderId);

    void cancelDelayOrder(Long orderId,Long roomId,int roomCount);
}
