package com.hope.chufala.model.entity;

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
@TableName("spring-ai-chat-memory")
public class SpringAIChatMemory {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long conversationId;
    private String content;
    private String type;
    @TableField(value = "timestamp")
    private LocalDateTime createdAt;

}
