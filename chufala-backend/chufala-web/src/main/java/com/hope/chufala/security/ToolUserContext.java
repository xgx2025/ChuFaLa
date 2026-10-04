package com.hope.chufala.security;

import com.hope.chufala.common.exception.ForbiddenException;
import org.springframework.ai.chat.model.ToolContext;

public final class ToolUserContext {
    private ToolUserContext() {
    }

    public static Long getUserId(ToolContext toolContext) {
        Object userId = toolContext == null ? null : toolContext.getContext().get("userId");
        if (userId instanceof Number number) {
            return number.longValue();
        }
        throw new ForbiddenException("缺少用户身份");
    }
}
