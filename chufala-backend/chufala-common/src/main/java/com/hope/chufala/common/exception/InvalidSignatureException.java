package com.hope.chufala.common.exception;

/**
 * 价格签名校验失败异常。
 *
 * <p>由 SignaturePriceUtils.verifyPriceSignature 失败路径抛出，
 * GlobalExceptionHandler 返回 DATA_NOT_SAFE，不向客户端暴露具体原因。
 *
 * @author 谢光湘
 */
public class InvalidSignatureException extends RuntimeException{
    public InvalidSignatureException(String message) {
        super(message);
    }
}
