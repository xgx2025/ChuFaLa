package com.hope.chufala.service;

import jakarta.servlet.http.HttpServletRequest;
import com.hope.chufala.model.dto.ConfirmedAlipayTrade;

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

    /** 处理主动查单确认的成功交易，与异步通知共用幂等落库流程。 */
    void confirmQueriedTrade(ConfirmedAlipayTrade trade);
}
