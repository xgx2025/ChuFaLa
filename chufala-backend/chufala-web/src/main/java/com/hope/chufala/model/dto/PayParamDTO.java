package com.hope.chufala.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import com.hope.chufala.model.vo.PointVO;

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


}
