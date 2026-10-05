package com.hope.chufala.common.exception.user;

/**
 * 登录失败异常。
 *
 * <p>邮箱不存在或密码错误时统一抛出同一提示，避免暴露邮箱是否已注册；
 * GlobalExceptionHandler 返回 USERNAME_OR_PASSWORD_ERROR。
 *
 * @author 谢光湘
 */
public class InvalidLoginException extends RuntimeException {
    public InvalidLoginException(String message) {
        super(message);
    }
}
