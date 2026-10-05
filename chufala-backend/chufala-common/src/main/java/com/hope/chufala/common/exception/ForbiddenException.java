package com.hope.chufala.common.exception;

/**
 * 禁止访问异常（HTTP 403）。
 *
 * <p>由 AccessControl 在权限不足或缺少身份时抛出，GlobalExceptionHandler 转为
 * USER_NOT_PERMITTED 响应。
 *
 * @author 谢光湘
 */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}
