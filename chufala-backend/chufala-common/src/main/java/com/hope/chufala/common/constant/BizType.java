package com.hope.chufala.common.constant;

/**
 * 业务类型常量。
 *
 * <p>作为支付适配器的路由键。已实现的适配器支持 HOTEL 和 VIP；
 * TICKET 预留给门票支付业务。
 *
 * @author 谢光湘
 */
public class BizType {
    /** 酒店业务 */
    public static final String HOTEL = "HOTEL";
    /** 门票业务 */
    public static final String TICKET = "TICKET";
    /** 会员业务 */
    public static final String VIP = "VIP";
}
