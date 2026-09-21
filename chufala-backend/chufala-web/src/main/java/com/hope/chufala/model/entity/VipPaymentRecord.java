package com.hope.chufala.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * VIP会员支付记录
 */
@Data
@NoArgsConstructor
@TableName("vip_payment_record")
public class VipPaymentRecord {

    @TableId(type = IdType.AUTO)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 商户订单号
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long outTradeNo;

    /**
     * 支付宝交易凭证号
     */
    private String tradeNo;

    /**
     * 支付金额
     */
    private BigDecimal totalAmount;

    /**
     * 订单标题
     */
    private String subject;

    /**
     * 购买的会员时长(天)
     */
    private Integer durationDays;

    /**
     * 支付状态: 0-待支付, 1-支付成功, 2-支付失败
     */
    private Integer payStatus;

    /**
     * 支付完成时间
     */
    private LocalDateTime payTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
