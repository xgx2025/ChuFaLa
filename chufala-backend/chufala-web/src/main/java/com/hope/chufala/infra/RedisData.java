package com.hope.chufala.infra;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import com.hope.chufala.model.vo.PointVO;
import com.hope.chufala.model.dto.PayParamDTO;

@Data
@NoArgsConstructor
public class RedisData {
    private LocalDate expireTime;
    private Object data;

}
