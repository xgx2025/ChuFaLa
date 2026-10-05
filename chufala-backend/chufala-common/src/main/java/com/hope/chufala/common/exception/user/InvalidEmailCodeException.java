package com.hope.chufala.common.exception.user;

/**
 * 邮箱验证码错误异常。
 *
 * <p>注册时验证码不匹配或已过期抛出，GlobalExceptionHandler 返回 EMAIL_VERIFY_CODE_ERROR。
 *
 * @author 谢光湘
 */
public class InvalidEmailCodeException extends RuntimeException {
    public InvalidEmailCodeException(String message) {
        super(message);
    }
}
