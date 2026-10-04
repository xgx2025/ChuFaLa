package com.hope.chufala;

import com.alipay.easysdk.factory.Factory;
import com.alipay.easysdk.payment.common.Client;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.hope.chufala.adapter.BizAdapter;
import com.hope.chufala.adapter.BizAdapterFactory;
import com.hope.chufala.exception.OrderAlreadyCancelledException;
import com.hope.chufala.infra.AlipayTemplate;
import com.hope.chufala.mapper.PayRecordMapper;
import com.hope.chufala.model.entity.PayRecord;
import com.hope.chufala.service.impl.AlipayServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PaymentNotifyRegressionTest {
    @Test
    void callbackForCancelledOrderCreatesRefundRequiredRecord() throws Exception {
        PayRecordMapper mapper = mock(PayRecordMapper.class);
        BizAdapterFactory factory = mock(BizAdapterFactory.class);
        BizAdapter adapter = mock(BizAdapter.class);
        AlipayTemplate template = mock(AlipayTemplate.class);
        Client client = mock(Client.class);
        AlipayServiceImpl service = service(mapper, factory, template);
        PayRecord record = record();
        when(template.getAppId()).thenReturn("merchant-app");
        when(mapper.selectByOrderId(101L)).thenReturn(record);
        when(factory.getAdapter("HOTEL")).thenReturn(adapter);
        when(mapper.update(isNull(), any(UpdateWrapper.class))).thenReturn(1);
        when(client.verifyNotify(any(Map.class))).thenReturn(true);
        doThrow(new OrderAlreadyCancelledException("已取消"))
                .when(adapter).handlePaySuccess(101L, record);

        try (MockedStatic<Factory.Payment> payment = mockStatic(Factory.Payment.class)) {
            payment.when(Factory.Payment::Common).thenReturn(client);
            assertEquals("success", service.handleNotify("支付宝", notification("10.00")));
        }

        verify(mapper).update(isNull(), org.mockito.ArgumentMatchers.<UpdateWrapper<PayRecord>>argThat(update ->
                update.getSqlSegment().contains("status")
                        && update.getParamNameValuePairs().containsValue("REFUND_REQUIRED")
                        && update.getParamNameValuePairs().containsValue("WAIT_PAY")));
    }

    @Test
    void amountMismatchCannotMarkOrderPaid() throws Exception {
        PayRecordMapper mapper = mock(PayRecordMapper.class);
        BizAdapterFactory factory = mock(BizAdapterFactory.class);
        AlipayTemplate template = mock(AlipayTemplate.class);
        Client client = mock(Client.class);
        AlipayServiceImpl service = service(mapper, factory, template);
        when(template.getAppId()).thenReturn("merchant-app");
        when(mapper.selectByOrderId(101L)).thenReturn(record());
        when(client.verifyNotify(any(Map.class))).thenReturn(true);

        try (MockedStatic<Factory.Payment> payment = mockStatic(Factory.Payment.class)) {
            payment.when(Factory.Payment::Common).thenReturn(client);
            assertEquals("fail", service.handleNotify("支付宝", notification("0.01")));
        }

        verify(factory, never()).getAdapter(any());
        verify(mapper, never()).update(isNull(), any(UpdateWrapper.class));
    }

    private AlipayServiceImpl service(PayRecordMapper mapper, BizAdapterFactory factory, AlipayTemplate template) {
        AlipayServiceImpl service = new AlipayServiceImpl();
        ReflectionTestUtils.setField(service, "payRecordMapper", mapper);
        ReflectionTestUtils.setField(service, "bizAdapterFactory", factory);
        ReflectionTestUtils.setField(service, "alipayTemplate", template);
        return service;
    }

    private PayRecord record() {
        PayRecord record = new PayRecord();
        record.setId(1L);
        record.setOrderId(101L);
        record.setBizType("HOTEL");
        record.setMoney(new BigDecimal("10.00"));
        record.setStatus("WAIT_PAY");
        return record;
    }

    private MockHttpServletRequest notification(String amount) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("out_trade_no", "101");
        request.addParameter("app_id", "merchant-app");
        request.addParameter("trade_status", "TRADE_SUCCESS");
        request.addParameter("total_amount", amount);
        request.addParameter("gmt_payment", "2026-10-04 12:00:00");
        request.addParameter("trade_no", "gateway-trade-1");
        return request;
    }
}
