package com.hope.chufala.service.impl;

import cn.hutool.core.lang.Snowflake;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
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

@Service
public class AiConversationServiceImpl implements IAiConversationService {

    @Autowired
    private AiConversationMapper aiConversationMapper;
    @Autowired
    private AiMessageMapper aiMessageMapper;


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

    @Override
    public void updateAssistantMessageContent(String messageId, String content, boolean isComplete) {
        AiMessage message = new AiMessage();
        message.setId(Long.valueOf(messageId));
        message.setContent(content);
//        message.setStatus(isComplete ? MessageStatus.COMPLETED.name() : MessageStatus.DRAFT.name());
        aiMessageMapper.updateById(message);
    }


    @Override
    public List<AiConversation> getConversationListByUserId(Long userId) {
        QueryWrapper<AiConversation> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId).eq("is_deleted", 0);
        queryWrapper.orderByDesc("create_time");
        return aiConversationMapper.selectList(queryWrapper);
    }

    @Override
    public void deleteConversationById(Long id) {
        UpdateWrapper<AiConversation> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", id).set("is_deleted", 1);
        aiConversationMapper.update(null, updateWrapper);
    }

    @Override
    public List<AiMessage> getConversationById(Long id) {
        QueryWrapper<AiMessage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("conversation_id", id);
        queryWrapper.orderByAsc("create_time");
        return aiMessageMapper.selectList(queryWrapper);
    }

    @Override
    public Long getUserIdByConversationId(Long conversationId) {
        QueryWrapper<AiConversation> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", conversationId);
        AiConversation aiConversation = aiConversationMapper.selectOne(queryWrapper);
        return aiConversation.getUserId();
    }
}
