package com.hope.chufala.common.exception.user;

public class VerifyCodeTooFrequentException extends RuntimeException {
    public VerifyCodeTooFrequentException(String message) {
        super(message);
    }
}
