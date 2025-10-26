package com.hope.service;

import com.hope.domain.dto.HotelOrderDTO;
import com.hope.domain.entity.HotelOrder;
import com.hope.domain.vo.PageResult;
import com.hope.domain.vo.Result;

public interface IHotelOrderService {
    HotelOrder getByOrderId(Long bizId);

    Result createHotelOrder(HotelOrderDTO hotelOrderDTO);

    void updateStatus(Long orderId, String paymentStatus);

   PageResult<HotelOrder> queryAllHotelOrder(Long userId, String orderStatus,Integer currentPage, Integer pageSize);

   boolean deleteOrder(Long orderId);

    boolean cancelOrder(Long orderId);
}
