package com.hope.chufala.tool;

import com.hope.chufala.domain.entity.User;
import com.hope.chufala.service.IAiConversationService;
import com.hope.chufala.service.IUserService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@SuppressWarnings("all")
@Component
public class UserInfoTools {
    @Autowired
    private IAiConversationService aiConversationService;
    @Autowired
    private IUserService userService;

    @Tool(description = "通过当前会话ID<来自系统提示词>,获取当前用户的昵称(姓名)")
    public String getCurrentUserNickname(@ToolParam(description = "当前的会话ID") Long conversationId) {
        Long currentUserId = aiConversationService.getUserIdByConversationId(conversationId);
        User user = userService.getUserById(currentUserId);
        return user.getUsername();
    }
}
