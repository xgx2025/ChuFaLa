package com.hope.chufala.common.exception.user;

/**
 * 验证码发送过于频繁异常。
 *
 * <p>命中 60 秒发送冷却时抛出，GlobalExceptionHandler 返回
 * CAPTCHA_SEND_TOO_MANY_TIMES，消息中带有剩余秒数。
 *
 * @author 谢光湘
 */
public class VerifyCodeTooFrequentException extends RuntimeException {
    public VerifyCodeTooFrequentException(String message) {
        super(message);
    }
}
