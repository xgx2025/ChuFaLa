package com.hope.chufala.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hope.chufala.model.entity.SpringAiChatMemory;
import org.apache.ibatis.annotations.Mapper;

/**
 * Spring AI 对话记忆 Mapper（spring_ai_chat_memory 表）。
 *
 * <p>由 Spring AI 的 JDBC ChatMemory 机制使用，业务代码一般不直接调用。
 *
 * @author 谢光湘
 */
@Mapper
public interface SpringAiChatMemoryMapper extends BaseMapper<SpringAiChatMemory> {
}
