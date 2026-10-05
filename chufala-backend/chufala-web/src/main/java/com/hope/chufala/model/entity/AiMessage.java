package com.hope.chufala.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * AI 对话消息实体，对应 ai_message 表。
 *
 * <p>role 区分消息来源：user-用户提问，assistant-模型回复。
 *
 * @author 谢光湘
 */
@Data
@NoArgsConstructor
public class AiMessage {
    /** 主键；序列化为字符串避免前端精度丢失 */
    @JsonSerialize(using = ToStringSerializer.class)
    @TableField(value = "id")
    private Long id;
    /** 所属会话 ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long conversationId;
    /** 消息角色：user / assistant */
    private String role;
    /** 消息内容 */
    private String content;
    /** 创建时间 */
    private LocalDateTime createTime;
}
