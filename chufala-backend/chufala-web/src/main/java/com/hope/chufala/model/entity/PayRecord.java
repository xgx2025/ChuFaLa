package com.hope.chufala.model.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class PayRecord {
    private Long id;
    private Long userId;
    private Long orderId;
    private String bizType;
    private String status;
    private BigDecimal money;
    private String tradeNo;
    private LocalDateTime payTime;


}
