package com.hope.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("spring-ai-chat-memory")
public class PlanHistory {
    @JsonSerialize(using = ToStringSerializer.class)
    @TableField(value = "conversation_id")
    private Long conversationId;
    private String content;
    private String type;
    @TableField(value = "timestamp")
    private LocalDateTime createdAt;

}
