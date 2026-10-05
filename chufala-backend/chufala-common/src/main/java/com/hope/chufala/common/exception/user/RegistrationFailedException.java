package com.hope.chufala.common.exception.user;

/**
 * 注册失败异常。
 *
 * <p>用户入库失败（如数据库异常）时抛出，GlobalExceptionHandler 返回 UNKNOWN_ERROR。
 *
 * @author 谢光湘
 */
public class RegistrationFailedException extends RuntimeException {
    public RegistrationFailedException(String message) {
        super(message);
    }
}
