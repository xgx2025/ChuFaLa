package com.hope.domain.entity;


import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayRecord {
    private Long id;
    @TableField(value = "order_id")
    private Long orderId;
    @TableField(value = "biz_type")
    private String bizType;
    private String status;
    private Double money;
    @TableField(value = "trade_no")
    private Long tradeNo;
    @TableField(value = "pay_time")
    private LocalDate payTime;


}
