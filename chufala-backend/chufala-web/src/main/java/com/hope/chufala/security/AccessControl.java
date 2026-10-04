package com.hope.chufala.security;

import com.hope.chufala.common.exception.ForbiddenException;
import com.hope.chufala.model.entity.User;
import com.hope.chufala.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccessControl {
    private static final int ADMIN_ROLE = 2;

    private final IUserService userService;

    public void requireAdmin(Long userId) {
        if (userId == null) {
            throw new ForbiddenException("需要管理员权限");
        }
        User user = userService.getUserById(userId);
        if (user == null || user.getIsDelete() != 0 || user.getRole() != ADMIN_ROLE) {
            throw new ForbiddenException("需要管理员权限");
        }
    }
}
