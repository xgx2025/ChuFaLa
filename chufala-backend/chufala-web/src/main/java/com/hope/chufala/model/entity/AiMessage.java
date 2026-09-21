package com.hope.chufala.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import com.hope.chufala.model.vo.PointVO;
import com.hope.chufala.model.dto.PayParamDTO;
import com.hope.chufala.infra.RedisData;

@Data
@NoArgsConstructor
public class AiMessage {
    @JsonSerialize(using = ToStringSerializer.class)
    @TableField(value = "id")
    private Long id;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long conversationId;
    private String role;
    private String content;
    private LocalDateTime createTime;
}
