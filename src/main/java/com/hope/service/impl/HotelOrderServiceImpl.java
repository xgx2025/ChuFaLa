package com.hope.service.impl;

import com.hope.domain.entity.HotelOrder;
import com.hope.mapper.HotelOrderMapper;
import com.hope.service.IHotelOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HotelOrderServiceImpl implements IHotelOrderService {
    @Autowired
    private HotelOrderMapper hotelOrderMapper;
    @Override
    public HotelOrder getById(Long orderId) {
        return hotelOrderMapper.selectById(orderId);
    }

    @Override
    public boolean updateStatus(Long orderId, String paymentStatus) {
       HotelOrder order = new HotelOrder();
       order.setId(orderId);
       order .setPaymentStatus(paymentStatus);
       return hotelOrderMapper.updateById(order)>0;
    }
}
