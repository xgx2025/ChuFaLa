package com.hope.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayParam {
    /**
     * 主键Id
     */
    private Long id;
    /**
     * 订单Id
     */
    private Long orderId;

    /**
     * 用户Id
     */
    private Long userId;

    /**
     * 接口Id
     */
    private String subject;

    /**
     * 支付金额
     */
    private Double money;

    /**
     * 商品描述
     */
    private String body;

    /**
     * 支付方式
     */
    private String paymentMethod;

}
