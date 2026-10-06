package com.hope.chufala.service;

import com.hope.chufala.model.entity.PayRecord;

import java.math.BigDecimal;

/**
 * 支付流水服务。
 *
 * @author 谢光湘
 */
public interface IPayRecordService {
    /**
     * 幂等创建并校验商户订单的支付记录。
     */
    void ensurePayRecord(String bizType, Long orderId, Long userId, BigDecimal money);

    /**
     * 更新支付流水状态。
     *
     * @param payRecord 含目标状态与定位信息的流水记录
     * @return 是否更新成功
     */
    boolean updateStatus(PayRecord payRecord);
}
