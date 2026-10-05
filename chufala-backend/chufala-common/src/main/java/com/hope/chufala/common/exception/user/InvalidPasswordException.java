package com.hope.chufala.common.exception.user;

/**
 * 密码错误异常。
 *
 * <p>当前登录流程已统一改用 InvalidLoginException，本类保留作为更细粒度的扩展位。
 *
 * @author 谢光湘
 */
public class InvalidPasswordException extends RuntimeException {
    public InvalidPasswordException(String message) {
        super(message);
    }
}
