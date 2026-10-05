package com.hope.chufala.model.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * AI 对话会话实体，对应 ai_conversation 表。
 *
 * <p>一个会话聚合多条 AiMessage，用于历史对话列表展示与消息归属。
 *
 * @author 谢光湘
 */
@Data
@NoArgsConstructor
public class AiConversation {
    /** 主键；序列化为字符串避免前端精度丢失 */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    /** 归属用户 ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;
    /** 会话标题 */
    private String title;
    /** 创建时间 */
    private LocalDateTime createTime;
    /** 逻辑删除标记 */
    private Integer isDeleted;
}
