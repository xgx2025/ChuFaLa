package com.hope.chufala.service.impl;

import com.alipay.api.AlipayApiException;
import com.alipay.api.response.AlipayTradeCloseResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.hope.chufala.infra.AlipayTemplate;
import com.hope.chufala.mapper.PayRecordMapper;
import com.hope.chufala.model.dto.ConfirmedAlipayTrade;
import com.hope.chufala.model.entity.PayRecord;
import com.hope.chufala.service.IAlipayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 取消酒店订单前取得渠道侧的终态证明。渠道调用不在数据库事务内执行。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HotelPaymentCloseGuard {
    public enum Result { NO_PAYMENT_RECORD, CLOSED, PAID }

    private static final ZoneId ALIPAY_ZONE = ZoneId.of("Asia/Shanghai");

    private final PayRecordMapper payRecordMapper;
    private final AlipayTemplate alipayTemplate;
    // 避免支付服务 -> 酒店适配器 -> 酒店订单服务的 Bean 创建循环。
    private final ObjectProvider<IAlipayService> alipayService;

    public Result prepareCancellation(Long orderId, LocalDateTime deadline) {
        PayRecord record = payRecordMapper.selectByOrderId(orderId);
        if (record == null) {
            return Result.NO_PAYMENT_RECORD;
        }
        if (!"HOTEL".equalsIgnoreCase(record.getBizType())) {
            throw new IllegalStateException("酒店订单对应了其他业务支付记录，orderId=" + orderId);
        }
        if ("SUCCESS".equals(record.getStatus()) || "REFUND_REQUIRED".equals(record.getStatus())) {
            return Result.PAID;
        }

        boolean absoluteExpiryEnabled = Boolean.TRUE.equals(record.getAbsoluteExpiryEnabled());
        AlipayTradeQueryResponse trade = query(orderId);
        Result result = interpretQuery(orderId, deadline, absoluteExpiryEnabled, trade);
        if (result != null) {
            return result;
        }

        // 查到 WAIT_BUYER_PAY 后不能直接取消：查询和本地状态更新之间仍可能付款。
        try {
            AlipayTradeCloseResponse close = alipayTemplate.closeTrade(orderId);
            if (close != null && close.isSuccess()) {
                return Result.CLOSED;
            }
            log.warn("支付宝关单未成功，重新查单，orderId={}, subCode={}", orderId,
                    close == null ? null : close.getSubCode());
        } catch (AlipayApiException e) {
            log.warn("支付宝关单结果不确定，重新查单，orderId={}", orderId, e);
        }
        result = interpretQuery(orderId, deadline, absoluteExpiryEnabled, query(orderId));
        if (result == null) {
            throw new IllegalStateException("支付宝交易仍可付款，稍后重试关单，orderId=" + orderId);
        }
        return result;
    }

    private AlipayTradeQueryResponse query(Long orderId) {
        try {
            AlipayTradeQueryResponse response = alipayTemplate.queryTrade(orderId);
            if (response == null) {
                throw new IllegalStateException("支付宝查单无响应，orderId=" + orderId);
            }
            return response;
        } catch (AlipayApiException e) {
            throw new IllegalStateException("支付宝查单失败，暂不释放库存，orderId=" + orderId, e);
        }
    }

    /** 返回 null 表示仍待付款，需要尝试关单。 */
    private Result interpretQuery(Long orderId, LocalDateTime deadline, boolean absoluteExpiryEnabled,
                                  AlipayTradeQueryResponse response) {
        if (!response.isSuccess()) {
            if ("ACQ.TRADE_NOT_EXIST".equals(response.getSubCode())) {
                // pageExecute 只生成表单；未提交表单时支付宝可能根本没有交易。
                // 截止时间前仍可能提交旧表单，不能释放库存。
                if (absoluteExpiryEnabled && deadline != null
                        && !LocalDateTime.now(ALIPAY_ZONE).isBefore(deadline)) {
                    return Result.CLOSED;
                }
                if (!absoluteExpiryEnabled && deadline != null
                        && !LocalDateTime.now(ALIPAY_ZONE).isBefore(deadline)) {
                    throw new IllegalStateException("旧支付表单可能仍可付款，需人工核查后取消，orderId=" + orderId);
                }
                throw new IllegalArgumentException("支付页面仍可能付款，请在支付截止后重试取消");
            }
            throw new IllegalStateException("支付宝查单状态未知，暂不释放库存，orderId=" + orderId
                    + ", subCode=" + response.getSubCode());
        }
        if (!orderId.toString().equals(response.getOutTradeNo())) {
            throw new IllegalStateException("支付宝查单商户订单号不匹配，orderId=" + orderId);
        }
        String status = response.getTradeStatus();
        if ("TRADE_SUCCESS".equals(status) || "TRADE_FINISHED".equals(status)) {
            if (response.getSendPayDate() == null || response.getTotalAmount() == null) {
                throw new IllegalStateException("支付宝已付款交易缺少金额或付款时间，orderId=" + orderId);
            }
            ConfirmedAlipayTrade paid = new ConfirmedAlipayTrade(orderId, response.getTradeNo(),
                    new BigDecimal(response.getTotalAmount()),
                    LocalDateTime.ofInstant(response.getSendPayDate().toInstant(), ALIPAY_ZONE),
                    response.getSubject());
            alipayService.getObject().confirmQueriedTrade(paid);
            return Result.PAID;
        }
        if ("TRADE_CLOSED".equals(status)) {
            return Result.CLOSED;
        }
        if ("WAIT_BUYER_PAY".equals(status)) {
            return null;
        }
        throw new IllegalStateException("支付宝交易状态未知，暂不释放库存，orderId=" + orderId
                + ", status=" + status);
    }
}
