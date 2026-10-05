package com.hope.chufala.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hope.chufala.model.entity.AiConversation;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI 会话 Mapper。
 *
 * <p>仅使用 MyBatis-Plus 通用 CRUD，无自定义 SQL。
 *
 * @author 谢光湘
 */
@Mapper
public interface AiConversationMapper extends BaseMapper<AiConversation> {

}
