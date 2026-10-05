package com.hope.chufala.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * AI 对话请求体。
 *
 * <p>由 AgentController 的对话接口接收，支持携带附件与续接已有会话。
 *
 * @author 谢光湘
 */
@Data
@NoArgsConstructor
public class ChatRequest {
    /** 用户输入内容 */
    private String message;
    /** 会话 ID，为空表示新建会话 */
    private String conversationId;
    /** 指定使用的模型标识 */
    private String model;
    /** 关联的行程规划 ID */
    private String planId;
    /** 附件文件 ID 列表 */
    private List<String> fileIds;
}
