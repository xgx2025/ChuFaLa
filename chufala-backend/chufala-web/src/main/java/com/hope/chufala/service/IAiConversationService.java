package com.hope.chufala.service;

import com.hope.chufala.model.entity.AiConversation;
import com.hope.chufala.model.entity.AiMessage;

import java.util.List;
import java.util.Map;

/**
 * AI 会话与消息服务。
 *
 * <p>负责会话生命周期（创建、列表、删除）与消息持久化；助手回复采用
 * 「先建草稿、再流式补全内容」的两步写入，配合 SSE 做增量推送。
 *
 * @author 谢光湘
 */
public interface IAiConversationService {
    /**
     * 创建一个会话并保存第一条用户消息。
     *
     * @param userId  用户 ID
     * @param message 首条用户消息内容
     * @return 含会话 ID 与消息 ID 的映射
     */
    Map<String,Long> createConversationAndSaveFirstMessage(Long userId, String message);

    /**
     * 保存一条消息。
     *
     * @param conversationId 会话 ID
     * @param role           消息角色（user / assistant）
     * @param content        消息内容
     * @return 新消息 ID
     */
    Long saveMessage(String conversationId,String role,String content);

    /**
     * 创建助手消息草稿（内容为空，待流式补全）。
     *
     * @param conversationId 会话 ID
     * @return 消息 ID
     */
    String createAssistantMessageDraft(String conversationId);

    /**
     * 更新助手消息内容。
     *
     * @param messageId  消息 ID
     * @param content    最新内容
     * @param isComplete 是否已生成完毕
     */
    void updateAssistantMessageContent(String messageId, String content, boolean isComplete);

    /**
     * 查询用户的全部历史会话。
     *
     * @param userId 用户 ID
     * @return 会话列表
     */
    List<AiConversation> getConversationListByUserId(Long userId);

    /**
     * 删除指定会话（校验归属）。
     *
     * @param id     会话 ID
     * @param userId 用户 ID
     */
    void deleteConversationById(Long id, Long userId);

    /**
     * 查询指定会话下的全部消息。
     *
     * @param id     会话 ID
     * @param userId 用户 ID
     * @return 消息列表
     */
    List<AiMessage> getConversationById(Long id, Long userId);

    /**
     * 校验会话归属，非本人所属时拒绝访问。
     *
     * @param conversationId 会话 ID
     * @param userId         用户 ID
     */
    void requireConversationOwner(Long conversationId, Long userId);
}
