package com.hope.chufala.exception;

/**
 * 订单已取消异常。
 *
 * <p>支付成功回调到达时订单已被取消（超时回收）会抛出；AlipayServiceImpl 捕获后
 * 把支付记录置为 REFUND_REQUIRED，进入退款处理流程。
 *
 * @author 谢光湘
 */
public class OrderAlreadyCancelledException extends RuntimeException {
    public OrderAlreadyCancelledException(String message) {
        super(message);
    }
}
