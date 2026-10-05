package com.hope.chufala.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Spring AI 对话记忆实体，对应 spring_ai_chat_memory 表。
 *
 * <p>由 Spring AI 的 JDBC ChatMemory 机制读写，为大模型提供多轮对话上下文；
 * timestamp 列映射为 createdAt。
 *
 * @author 谢光湘
 */
@Data
@NoArgsConstructor
@TableName("spring_ai_chat_memory")
public class SpringAiChatMemory {
    /** 会话 ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long conversationId;
    /** 消息内容 */
    private String content;
    /** 消息类型 */
    private String type;
    /** 写入时间，对应列 timestamp */
    @TableField(value = "timestamp")
    private LocalDateTime createdAt;

}
