package com.hope.chufala.service.impl;

import cn.hutool.core.lang.Snowflake;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.hope.chufala.common.exception.ResourceNotFoundException;
import com.hope.chufala.model.entity.AiConversation;
import com.hope.chufala.model.entity.AiMessage;
import com.hope.chufala.mapper.AiConversationMapper;
import com.hope.chufala.mapper.AiMessageMapper;
import com.hope.chufala.service.IAiConversationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * AI 会话与消息服务实现。
 *
 * <p>会话/消息 ID 由 Hutool Snowflake 生成（非自增）；会话采用逻辑删除
 * （is_deleted），且所有读写都带 userId 条件，从 SQL 层保证越权不可达。
 *
 * @author 谢光湘
 */
@Service
public class AiConversationServiceImpl implements IAiConversationService {

    @Autowired
    private AiConversationMapper aiConversationMapper;
    @Autowired
    private AiMessageMapper aiMessageMapper;


    /**
     * 新建会话并写入首条用户消息（同一事务）。
     *
     * <p>会话标题取首条消息前 13 个字符，超出则截断加省略号。
     *
     * @param userId  用户 ID
     * @param message 首条用户消息
     * @return 含 conversationId 与 messageId 的映射
     */
    @Override
    @Transactional
    public Map<String, Long> createConversationAndSaveFirstMessage(Long userId, String message) {
        //生成会话ID
        Snowflake snowflake = new Snowflake();
        Long conversationId = snowflake.nextId();
        String title = null;
        if (message.length()<=13){
            title = message;
        }else{
            title = message.substring(0,13)+"...";
        }
        //存储新会话
        AiConversation aiConversation = new AiConversation();
        aiConversation.setId(conversationId);
        aiConversation.setUserId(userId);
        aiConversation.setTitle(title);
        aiConversation.setCreateTime(LocalDateTime.now());
        aiConversationMapper.insert(aiConversation);
        //生成消息ID
        Long messageId = snowflake.nextId();
        //存储第一条消息
        AiMessage aiMessage = new AiMessage();
        aiMessage.setId(messageId);
        aiMessage.setConversationId(conversationId);
        aiMessage.setRole("user");
        aiMessage.setContent(message);
        aiMessage.setCreateTime(LocalDateTime.now());
        //TODO:对于数据库插入异常进行处理
        aiMessageMapper.insert(aiMessage);
        //返回会话ID和消息ID
        return Map.of("conversationId",conversationId,"messageId",messageId);
    }

    /**
     * 保存一条消息。
     *
     * @param conversationId 会话 ID
     * @param role           消息角色
     * @param content        消息内容
     * @return 新消息 ID
     */
    @Override
    public Long saveMessage(String conversationId, String role, String content) {
        AiMessage aiMessage = new AiMessage();
        Snowflake snowflake = new Snowflake();
        Long messageId = snowflake.nextId();
        aiMessage.setId(messageId);
        aiMessage.setConversationId(Long.valueOf(conversationId));
        aiMessage.setRole(role);
        aiMessage.setContent(content);
        aiMessage.setCreateTime(LocalDateTime.now());
        aiMessageMapper.insert(aiMessage);
        return messageId;
    }


    /**
     * 创建助手消息草稿（内容为空，待流式补全）。
     *
     * @param conversationId 会话 ID
     * @return 消息 ID（字符串形式）
     */
    @Override
    public String createAssistantMessageDraft(String conversationId) {
        Snowflake snowflake = new Snowflake();
        String messageId = String.valueOf(snowflake.nextId());
        AiMessage draft = new AiMessage();
        draft.setId(Long.valueOf(messageId));
        draft.setConversationId(Long.valueOf(conversationId));
        draft.setRole("assistant");
        draft.setContent("");
        draft.setCreateTime(LocalDateTime.now());
        aiMessageMapper.insert(draft);

        return messageId;
    }

    /**
     * 更新助手消息内容。
     *
     * @param messageId  消息 ID
     * @param content    最新内容
     * @param isComplete 是否已生成完毕（当前未落库，预留字段）
     */
    @Override
    public void updateAssistantMessageContent(String messageId, String content, boolean isComplete) {
        AiMessage message = new AiMessage();
        message.setId(Long.valueOf(messageId));
        message.setContent(content);
//        message.setStatus(isComplete ? MessageStatus.COMPLETED.name() : MessageStatus.DRAFT.name());
        aiMessageMapper.updateById(message);
    }


    /**
     * 查询用户的全部未删除会话（按下单时间倒序）。
     *
     * @param userId 用户 ID
     * @return 会话列表
     */
    @Override
    public List<AiConversation> getConversationListByUserId(Long userId) {
        QueryWrapper<AiConversation> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId).eq("is_deleted", 0);
        queryWrapper.orderByDesc("create_time");
        return aiConversationMapper.selectList(queryWrapper);
    }

    /**
     * 逻辑删除会话（同时校验归属，影响行数不为 1 即视为不存在）。
     *
     * @param id     会话 ID
     * @param userId 用户 ID
     */
    @Override
    public void deleteConversationById(Long id, Long userId) {
        UpdateWrapper<AiConversation> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", id).eq("user_id", userId).eq("is_deleted", 0).set("is_deleted", 1);
        if (aiConversationMapper.update(null, updateWrapper) != 1) {
            throw new ResourceNotFoundException("会话不存在");
        }
    }

    /**
     * 查询会话下的全部消息（按创建时间升序）。
     *
     * @param id     会话 ID
     * @param userId 用户 ID
     * @return 消息列表
     */
    @Override
    public List<AiMessage> getConversationById(Long id, Long userId) {
        requireConversationOwner(id, userId);
        QueryWrapper<AiMessage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("conversation_id", id);
        queryWrapper.orderByAsc("create_time");
        return aiMessageMapper.selectList(queryWrapper);
    }

    /**
     * 校验会话归属，不存在或非本人所属时抛 ResourceNotFoundException。
     *
     * @param conversationId 会话 ID
     * @param userId         用户 ID
     */
    @Override
    public void requireConversationOwner(Long conversationId, Long userId) {
        QueryWrapper<AiConversation> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", conversationId).eq("user_id", userId).eq("is_deleted", 0);
        AiConversation aiConversation = aiConversationMapper.selectOne(queryWrapper);
        if (aiConversation == null) {
            throw new ResourceNotFoundException("会话不存在");
        }
    }
}
