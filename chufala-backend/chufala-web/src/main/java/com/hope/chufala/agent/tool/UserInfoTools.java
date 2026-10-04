package com.hope.chufala.agent.tool;

import com.hope.chufala.model.entity.User;
import com.hope.chufala.service.IUserService;
import com.hope.chufala.security.ToolUserContext;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@SuppressWarnings("all")
@Component
public class UserInfoTools {
    @Autowired
    private IUserService userService;

    @Tool(description = "获取当前用户的昵称(姓名)")
    public String getCurrentUserNickname(ToolContext toolContext) {
        Long currentUserId = ToolUserContext.getUserId(toolContext);
        User user = userService.getUserById(currentUserId);
        return user.getUsername();
    }
}
