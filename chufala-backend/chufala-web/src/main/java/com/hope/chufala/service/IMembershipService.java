package com.hope.chufala.service;


import com.hope.chufala.model.entity.VipPaymentRecord;

/**
 * 会员（VIP）服务。
 *
 * @author 谢光湘
 */
public interface IMembershipService {
    /**
     * 创建会员购买订单。
     *
     * @param userId 用户 ID
     * @return 支付表单或支付链接
     */
    String createMembershipOrder(Long userId);

    /**
     * 按记录 ID 查询会员支付记录。
     *
     * @param orderId 支付记录 ID
     * @return 支付记录
     */
    VipPaymentRecord getRecordById(Long orderId);
}
