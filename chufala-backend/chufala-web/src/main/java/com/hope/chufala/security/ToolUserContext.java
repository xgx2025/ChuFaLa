package com.hope.chufala.security;

import com.hope.chufala.common.exception.ForbiddenException;
import org.springframework.ai.chat.model.ToolContext;

/**
 * 工具调用用户上下文。
 *
 * <p>从 Spring AI 的 ToolContext 中取出 userId，供 {@code @Tool} 方法识别调用者；
 * 缺失时抛 ForbiddenException，避免工具被无身份调用。
 *
 * @author 谢光湘
 */
public final class ToolUserContext {
    private ToolUserContext() {
    }

    /**
     * 从工具上下文中取出当前用户 ID。
     *
     * @param toolContext 工具上下文
     * @return 用户 ID
     * @throws ForbiddenException 上下文中没有用户身份
     */
    public static Long getUserId(ToolContext toolContext) {
        Object userId = toolContext == null ? null : toolContext.getContext().get("userId");
        if (userId instanceof Number number) {
            return number.longValue();
        }
        throw new ForbiddenException("缺少用户身份");
    }
}
