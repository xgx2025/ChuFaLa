package com.hope.chufala.common.constant;

/**
 * 业务类型常量。
 *
 * <p>作为支付适配器（BizAdapterFactory）的路由键，与实现类 Bean 名
 * {@code {bizType.toLowerCase()}Adapter} 一一对应（HOTEL → hotelAdapter）。
 *
 * @author 谢光湘
 */
public class BizType {
    /** 酒店业务 */
    public static final String HOTEL = "HOTEL";
    /** 门票业务 */
    public static final String TICKET = "TICKET";
}
