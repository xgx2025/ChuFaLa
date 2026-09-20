package com.hope.chufala.service;

import com.hope.chufala.domain.entity.AiConversation;
import com.hope.chufala.domain.entity.AiMessage;

import java.util.List;
import java.util.Map;

public interface IAiConversationService {
    /**
     * 创建一个会话并保存第一条用户消息
     * @param userId 用户ID
     * @return 会话ID
     */
    Map<String,Long> createConversationAndSaveFirstMessage(Long userId, String message);

    /**
     * 保存消息
     * @param conversationId 会话ID
     * @param role 消息角色
     * @param content 消息内容
     * @return 消息ID
     */
    Long saveMessage(String conversationId,String role,String content);

    /**
     * 创建助手消息草稿
     * @param conversationId 会话ID
     * @return 消息ID
     */
    String createAssistantMessageDraft(String conversationId);

    /**
     * 更新助手消息内容
     * @param messageId 消息ID
     * @param content 消息内容
     * @param isComplete 是否完成
     */
    void updateAssistantMessageContent(String messageId, String content, boolean isComplete);

    /**
     * 通过用户ID获取历史会话
     * @param userId 用户ID
     * @return
     */
    List<AiConversation> getConversationListByUserId(Long userId);

    /**
     * 通过ID删除会话
     * @param id
     */
    void deleteConversationById(Long id);

    /**
     * 通过ID获取会话
     * @param id
     * @return
     */
    List<AiMessage> getConversationById(Long id);

    Long getUserIdByConversationId(Long conversationId);
}
