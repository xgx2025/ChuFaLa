package com.hope.constant;

import java.util.List;

public enum ResultCode {

    SUCCESS(0, "成功"),
    FAIL(1, "失败"),
    UNAUTHORIZED(4001, "未认证"),
    NOT_FOUND(4004, "未找到"),
    METHOD_NOT_ALLOWED(4005, "方法不允许"),
    REQUEST_TIMEOUT(4008, "请求超时"),
    INTERNAL_SERVER_ERROR(5000, "服务器错误"),
    NOT_IMPLEMENTED(5001, "未实现"),
    BAD_GATEWAY(5002, "网关错误"),
    UNKNOWN_ERROR(9999, "未知错误"),
    SYSTEM_BUSY(9998, "系统繁忙,请重试"),

    USERNAME_OR_PASSWORD_ERROR(1000, "用户名或密码错误"),
    USER_NOT_EXIST(1001, "用户不存在"),
    USER_ALREADY_EXISTS(1002, "用户已存在"),
    USER_NOT_LOGIN(1003, "用户未登录"),
    USER_NOT_AUTHORIZED(1004, "用户未授权"),
    USER_NOT_PERMITTED(1005, "用户无权限"),

    DATA_NOT_SAFE(2000, "数据不安全，请重试"),

    EMAIL_ALREADY_EXISTS(2001, "该邮箱已注册"),
    EMAIL_NOT_VERIFIED(2003, "邮箱未验证"),
    EMAIL_VERIFY_CODE_ERROR(2004, "邮箱验证码错误"),
    EMAIL_VERIFY_CODE_SEND_TOO_MANY_TIMES(2006, "邮箱验证码发送次数过多"),
    EMAIL_VERIFY_CODE_SEND_FAILED(2007, "邮箱验证码发送失败"),
    EMAIL_VERIFY_CODE_SEND_SUCCESS(2008, "邮箱验证码发送成功"),

    USER_NOT_LOGIN_OR_NOT_AUTHORIZED(3001, "用户未登录或未授权"),
    USER_NOT_LOGIN_OR_NOT_AUTHORIZED_OR_NOT_PERMITTED(3002, "用户未登录或未授权或无权限"),
    USER_NOT_LOGIN_OR_NOT_AUTHORIZED_OR_NOT_PERMITTED_OR_NOT_FOUND(3003, "用户未登录或未授权或无权限或未找到");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
