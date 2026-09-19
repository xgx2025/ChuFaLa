package com.hope.chufala.service;


import com.hope.chufala.domain.entity.VipPaymentRecord;

public interface IMembershipService {
    String createMembershipOrder(Long userId);

    VipPaymentRecord getRecordById(Long orderId);
}
