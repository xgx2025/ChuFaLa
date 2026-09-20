package com.hope.chufala.common.exception.user;

public class InvalidEmailCodeException extends RuntimeException {
    public InvalidEmailCodeException(String message) {
        super(message);
    }
}
