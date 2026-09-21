package com.hope.chufala.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.hope.chufala.model.vo.PointVO;
import com.hope.chufala.model.dto.PayParamDTO;
import com.hope.chufala.infra.RedisData;

@Data
@NoArgsConstructor
public class Voucher {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
}
