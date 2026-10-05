package com.hope.chufala.common.exception;

/**
 * 位置信息不可用异常。
 *
 * <p>用户坐标缺失（如未授权定位）时抛出，GlobalExceptionHandler 返回 LOCATION_UNAVAILABLE，
 * 前端据此提示重新定位。
 *
 * @author 谢光湘
 */
public class LocationUnavailableException extends RuntimeException {
    public LocationUnavailableException(String message) {
        super(message);
    }
}
