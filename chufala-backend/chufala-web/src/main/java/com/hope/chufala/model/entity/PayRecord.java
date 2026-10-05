package com.hope.chufala.model.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付流水记录，对应 pay_record 表。
 *
 * <p>一次支付尝试对应一条记录，用于幂等地推进支付状态，
 * 并支撑支付回调的重复通知与退款补偿。
 *
 * @author 谢光湘
 */
@Data
@NoArgsConstructor
public class PayRecord {
    /** 自增主键 */
    private Long id;
    /** 支付用户 ID */
    private Long userId;
    /** 关联的业务订单 ID */
    private Long orderId;
    /** 业务类型，取值见 BizType：HOTEL / TICKET */
    private String bizType;
    /** 支付状态：WAIT_PAY-待支付，SUCCESS-支付成功，REFUND_REQUIRED-需退款，REFUNDED-已退款 */
    private String status;
    /** 支付金额（元） */
    private BigDecimal money;
    /** 第三方交易流水号 */
    private String tradeNo;
    /** 支付完成时间 */
    private LocalDateTime payTime;


}
