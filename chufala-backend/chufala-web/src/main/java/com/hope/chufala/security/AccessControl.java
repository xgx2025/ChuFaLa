package com.hope.chufala.security;

import com.hope.chufala.common.exception.ForbiddenException;
import com.hope.chufala.model.entity.User;
import com.hope.chufala.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 权限校验组件。
 *
 * <p>基于 ThreadLocal 中的 JWT claims 判定管理员身份：要求角色为 2（ADMIN_ROLE）
 * 且未被逻辑删除，否则抛 ForbiddenException。用户是否存在以数据库为准，
 * 避免只凭 token 中的 role 声明放行。
 *
 * @author 谢光湘
 */
@Component
@RequiredArgsConstructor
public class AccessControl {
    /** 管理员角色值 */
    private static final int ADMIN_ROLE = 2;

    private final IUserService userService;

    /**
     * 要求调用者为管理员，否则拒绝访问。
     *
     * @param userId 当前用户 ID（取自 JWT claims）
     * @throws ForbiddenException 未登录、用户不存在、已删除或非管理员
     */
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
