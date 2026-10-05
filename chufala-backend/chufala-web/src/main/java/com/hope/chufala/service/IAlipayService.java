package com.hope.chufala.service;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 支付宝支付服务。
 *
 * @author 谢光湘
 */
public interface IAlipayService {
    /**
     * 创建支付，返回支付表单或跳转链接。
     *
     * @param bizType 业务类型（见 BizType：HOTEL / TICKET）
     * @param bizId   业务订单 ID
     * @return 支付表单 HTML 或支付链接
     */
    String createPay(String bizType, Long bizId);

    /**
     * 处理支付回调（校验签名并推进订单状态）。
     *
     * @param channel 支付渠道标识
     * @param request 回调请求
     * @return 返回给支付平台的应答内容
     */
    String handleNotify(String channel, HttpServletRequest request);
}
