package com.hope.chufala.constant;

public final class HotelOrderStatus {
    public static final String UNPAID = "待支付";
    public static final String PAID = "已支付";
    public static final String CANCELLED = "已取消";

    public static String normalizeFilter(String status) {
        return "未支付".equals(status) ? UNPAID : status;
    }

    private HotelOrderStatus() {
    }
}
