package com.hope.chufala.infra;

import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.hope.chufala.model.dto.PayParamDTO;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 支付宝支付请求组装单元测试。
 *
 * <p>校验订单号、金额、标题等字段正确映射到 SDK 模型，且商品标题中的
 * 特殊字符（引号）不会破坏请求结构。
 *
 * @author 谢光湘
 */
class AlipayTemplateTest {
    @Test
    void hotelPaymentUsesOrderDescriptionAndPreservesSpecialCharacters() {
        AlipayTemplate template = new AlipayTemplate();
        template.returnUrl = "https://example.com/return";
        template.notifyUrl = "https://example.com/notify";

        PayParamDTO param = new PayParamDTO();
        param.setOrderId(123L);
        param.setMoney(new BigDecimal("299.00"));
        param.setSubject("酒店预订：海景\"套房");
        param.setBody("房间：海景\"套房，入住时间：2026-10-05");

        AlipayTradePagePayRequest request = template.buildPayRequest(param);
        AlipayTradePagePayModel model = (AlipayTradePagePayModel) request.getBizModel();

        assertEquals("123", model.getOutTradeNo());
        assertEquals("299.00", model.getTotalAmount());
        assertEquals(param.getSubject(), model.getSubject());
        assertEquals(param.getBody(), model.getBody());
        assertEquals("FAST_INSTANT_TRADE_PAY", model.getProductCode());
        assertEquals("https://example.com/return", request.getReturnUrl());
        assertEquals("https://example.com/notify", request.getNotifyUrl());
    }
}
