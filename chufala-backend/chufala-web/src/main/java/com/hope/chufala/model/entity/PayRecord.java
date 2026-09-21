package com.hope.chufala.model.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.hope.chufala.model.vo.PointVO;
import com.hope.chufala.model.dto.PayParamDTO;
import com.hope.chufala.infra.RedisData;

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
