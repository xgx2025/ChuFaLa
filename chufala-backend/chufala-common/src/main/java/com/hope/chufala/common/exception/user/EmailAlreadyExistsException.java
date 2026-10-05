package com.hope.chufala.common.exception.user;

/**
 * 邮箱已被注册异常。
 *
 * <p>注册时邮箱重复抛出，GlobalExceptionHandler 返回 EMAIL_ALREADY_EXISTS。
 *
 * @author 谢光湘
 */
public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException(String message) {
        super(message);
    }
}
