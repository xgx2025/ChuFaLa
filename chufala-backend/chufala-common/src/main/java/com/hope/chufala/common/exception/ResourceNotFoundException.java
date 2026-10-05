package com.hope.chufala.common.exception;

/**
 * 资源不存在异常（HTTP 404）。
 *
 * <p>归属校验失败、记录缺失时抛出；调用方通常用「查不到」来掩盖「无权访问」，
 * 避免泄露资源是否存在。
 *
 * @author 谢光湘
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
