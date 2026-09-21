package com.hope.chufala.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
