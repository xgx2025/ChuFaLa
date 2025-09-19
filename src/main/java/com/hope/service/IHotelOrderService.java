package com.hope.service;

import com.hope.domain.entity.HotelOrder;

public interface IHotelOrderService {
    HotelOrder getById(Long bizId);

    boolean updateStatus(Long orderId, String paymentStatus);
}
