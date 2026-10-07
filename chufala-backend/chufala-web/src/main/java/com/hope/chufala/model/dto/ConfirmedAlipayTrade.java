package com.hope.chufala.model.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 支付宝查单确认已付款后的可信交易信息。 */
public record ConfirmedAlipayTrade(Long orderId, String tradeNo, BigDecimal amount,
                                   LocalDateTime paidAt, String subject) {
}
