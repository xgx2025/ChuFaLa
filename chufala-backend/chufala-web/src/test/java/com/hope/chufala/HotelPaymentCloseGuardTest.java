package com.hope.chufala;

import com.alipay.api.response.AlipayTradeCloseResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.hope.chufala.infra.AlipayTemplate;
import com.hope.chufala.mapper.PayRecordMapper;
import com.hope.chufala.model.dto.ConfirmedAlipayTrade;
import com.hope.chufala.model.entity.PayRecord;
import com.hope.chufala.service.IAlipayService;
import com.hope.chufala.service.impl.HotelPaymentCloseGuard;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import java.time.LocalDateTime;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class HotelPaymentCloseGuardTest {
    private final PayRecordMapper mapper = mock(PayRecordMapper.class);
    private final AlipayTemplate template = mock(AlipayTemplate.class);
    private final IAlipayService service = mock(IAlipayService.class);
    @SuppressWarnings("unchecked")
    private final ObjectProvider<IAlipayService> provider = mock(ObjectProvider.class);
    private final HotelPaymentCloseGuard guard = new HotelPaymentCloseGuard(mapper, template, provider);

    @Test
    void anUnsubmittedPaymentFormCannotBeCancelledBeforeItsDeadline() throws Exception {
        when(mapper.selectByOrderId(101L)).thenReturn(record());
        AlipayTradeQueryResponse missing = notFound();
        when(template.queryTrade(101L)).thenReturn(missing);

        assertThrows(IllegalArgumentException.class,
                () -> guard.prepareCancellation(101L, LocalDateTime.now().plusMinutes(10)));
        verify(template, never()).closeTrade(any());
    }

    @Test
    void anUnsubmittedExpiredFormCanReleaseStockAfterItsAbsoluteDeadline() throws Exception {
        when(mapper.selectByOrderId(101L)).thenReturn(record());
        AlipayTradeQueryResponse missing = notFound();
        when(template.queryTrade(101L)).thenReturn(missing);

        assertEquals(HotelPaymentCloseGuard.Result.CLOSED,
                guard.prepareCancellation(101L, LocalDateTime.now().minusMinutes(1)));
    }

    @Test
    void legacyFormWithoutAbsoluteDeadlineCannotBeAssumedClosed() throws Exception {
        PayRecord oldRecord = record();
        oldRecord.setAbsoluteExpiryEnabled(false);
        when(mapper.selectByOrderId(101L)).thenReturn(oldRecord);
        AlipayTradeQueryResponse missing = notFound();
        when(template.queryTrade(101L)).thenReturn(missing);

        assertThrows(IllegalStateException.class,
                () -> guard.prepareCancellation(101L, LocalDateTime.now().minusMinutes(1)));
    }

    @Test
    void waitingTradeMustBeClosedBeforeStockIsReleased() throws Exception {
        when(mapper.selectByOrderId(101L)).thenReturn(record());
        AlipayTradeQueryResponse waiting = query("WAIT_BUYER_PAY");
        when(template.queryTrade(101L)).thenReturn(waiting);
        AlipayTradeCloseResponse close = mock(AlipayTradeCloseResponse.class);
        when(close.isSuccess()).thenReturn(true);
        when(template.closeTrade(101L)).thenReturn(close);

        assertEquals(HotelPaymentCloseGuard.Result.CLOSED,
                guard.prepareCancellation(101L, LocalDateTime.now().minusMinutes(1)));
        verify(template).closeTrade(101L);
    }

    @Test
    void paymentWinningTheCloseRaceIsSettledInsteadOfCancelled() throws Exception {
        when(mapper.selectByOrderId(101L)).thenReturn(record());
        AlipayTradeQueryResponse paid = query("TRADE_SUCCESS");
        when(paid.getTradeNo()).thenReturn("gateway-trade-1");
        when(paid.getTotalAmount()).thenReturn("10.00");
        when(paid.getSendPayDate()).thenReturn(new Date());
        AlipayTradeQueryResponse waiting = query("WAIT_BUYER_PAY");
        when(template.queryTrade(101L)).thenReturn(waiting, paid);
        AlipayTradeCloseResponse close = mock(AlipayTradeCloseResponse.class);
        when(close.isSuccess()).thenReturn(false);
        when(template.closeTrade(101L)).thenReturn(close);
        when(provider.getObject()).thenReturn(service);

        assertEquals(HotelPaymentCloseGuard.Result.PAID,
                guard.prepareCancellation(101L, LocalDateTime.now().minusMinutes(1)));
        verify(service).confirmQueriedTrade(any(ConfirmedAlipayTrade.class));
    }

    @Test
    void failedCloseAndStillWaitingPaymentMustNotReleaseStock() throws Exception {
        when(mapper.selectByOrderId(101L)).thenReturn(record());
        AlipayTradeQueryResponse waiting = query("WAIT_BUYER_PAY");
        when(template.queryTrade(101L)).thenReturn(waiting);
        AlipayTradeCloseResponse close = mock(AlipayTradeCloseResponse.class);
        when(close.isSuccess()).thenReturn(false);
        when(template.closeTrade(101L)).thenReturn(close);

        assertThrows(IllegalStateException.class,
                () -> guard.prepareCancellation(101L, LocalDateTime.now().minusMinutes(1)));
    }

    private PayRecord record() {
        PayRecord record = new PayRecord();
        record.setBizType("HOTEL");
        record.setStatus("WAIT_PAY");
        record.setAbsoluteExpiryEnabled(true);
        return record;
    }

    private AlipayTradeQueryResponse notFound() {
        AlipayTradeQueryResponse response = mock(AlipayTradeQueryResponse.class);
        when(response.getSubCode()).thenReturn("ACQ.TRADE_NOT_EXIST");
        return response;
    }

    private AlipayTradeQueryResponse query(String status) {
        AlipayTradeQueryResponse response = mock(AlipayTradeQueryResponse.class);
        when(response.isSuccess()).thenReturn(true);
        when(response.getOutTradeNo()).thenReturn("101");
        when(response.getTradeStatus()).thenReturn(status);
        return response;
    }
}
