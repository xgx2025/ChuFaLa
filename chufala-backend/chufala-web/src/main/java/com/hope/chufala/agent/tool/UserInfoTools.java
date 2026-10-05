package com.hope.chufala.agent.tool;

import com.hope.chufala.model.entity.User;
import com.hope.chufala.service.IUserService;
import com.hope.chufala.security.ToolUserContext;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 用户信息工具。
 *
 * <p>用户身份通过 ToolContext 传递，仅返回当前登录用户自身的信息。
 *
 * @author 谢光湘
 */
@SuppressWarnings("all")
@Component
public class UserInfoTools {
    @Autowired
    private IUserService userService;

    /**
     * 获取当前登录用户的昵称。
     *
     * @param toolContext 工具上下文（含当前用户 ID）
     * @return 用户昵称
     */
    @Tool(description = "获取当前用户的昵称(姓名)")
    public String getCurrentUserNickname(ToolContext toolContext) {
        Long currentUserId = ToolUserContext.getUserId(toolContext);
        User user = userService.getUserById(currentUserId);
        return user.getUsername();
    }
}
