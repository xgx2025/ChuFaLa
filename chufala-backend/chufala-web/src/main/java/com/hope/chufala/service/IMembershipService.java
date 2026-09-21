package com.hope.chufala.service;


import com.hope.chufala.model.entity.VipPaymentRecord;

public interface IMembershipService {
    String createMembershipOrder(Long userId);

    VipPaymentRecord getRecordById(Long orderId);
}
