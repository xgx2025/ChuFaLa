package com.hope.chufala.constant;

/**
 * 酒店订单状态常量。
 *
 * <p>状态以中文字符串直接存于 hotel_order.order_status；normalizeFilter 用于
 * 兼容前端可能传入的「未支付」写法（历史数据里该值曾被写成两种）。
 *
 * @author 谢光湘
 */
public final class HotelOrderStatus {
    /** 待支付 */
    public static final String UNPAID = "待支付";
    /** 已支付 */
    public static final String PAID = "已支付";
    /** 已取消 */
    public static final String CANCELLED = "已取消";

    /**
     * 归一化前端传入的状态筛选值。
     *
     * @param status 原始状态
     * @return 归一化后的状态（"未支付" → "待支付"，其余原样返回）
     */
    public static String normalizeFilter(String status) {
        return "未支付".equals(status) ? UNPAID : status;
    }

    private HotelOrderStatus() {
    }
}
