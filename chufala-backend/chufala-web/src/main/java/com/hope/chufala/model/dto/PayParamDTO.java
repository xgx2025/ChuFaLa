package com.hope.chufala.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 发起支付请求参数。
 *
 * @author 谢光湘
 */
@Data
@NoArgsConstructor
public class PayParamDTO {
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
    private BigDecimal money;

    /**
     * 商品描述
     */
    private String body;

    /**
     * 支付方式
     */
    private String paymentMethod;

    /** 酒店订单的绝对支付截止时间；其他业务保持渠道默认相对时限。 */
    private LocalDateTime expireTime;


}
