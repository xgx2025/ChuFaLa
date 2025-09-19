package com.hope.service;

import jakarta.servlet.http.HttpServletRequest;

public interface IAlipayService {
    // 创建支付（返回支付表单/链接）
    String createPay(String bizType, Long bizId);
    // 处理支付回调
    String handleNotify(String channel, HttpServletRequest request);
}
