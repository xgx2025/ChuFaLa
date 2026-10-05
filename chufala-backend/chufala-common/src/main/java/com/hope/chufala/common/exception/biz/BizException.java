package com.hope.chufala.common.exception.biz;

/**
 * 业务异常基类。
 *
 * <p>表示可预期的业务规则冲突（非系统故障），GlobalExceptionHandler 统一按
 * SYSTEM_BUSY 返回并记录请求路径与堆栈。
 *
 * @author 谢光湘
 */
public class BizException extends RuntimeException{
    public BizException(String message) {
        super(message);
    }
}
