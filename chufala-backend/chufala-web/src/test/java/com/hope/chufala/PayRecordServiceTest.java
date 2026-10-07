package com.hope.chufala;

import com.hope.chufala.mapper.PayRecordMapper;
import com.hope.chufala.model.entity.PayRecord;
import com.hope.chufala.service.impl.PayRecordServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.ArgumentCaptor;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PayRecordServiceTest {
    @Test
    void newHotelPaymentRecordMarksItsFormAsAbsolutelyExpiring() {
        PayRecordMapper mapper = mock(PayRecordMapper.class);

        service(mapper).ensurePayRecord("HOTEL", 101L, 7L, new BigDecimal("10.00"));

        ArgumentCaptor<PayRecord> inserted = ArgumentCaptor.forClass(PayRecord.class);
        verify(mapper).insert(inserted.capture());
        assertTrue(inserted.getValue().getAbsoluteExpiryEnabled());
    }

    @Test
    void concurrentInsertConflictReusesTheCommittedPaymentRecord() {
        PayRecordMapper mapper = mock(PayRecordMapper.class);
        PayRecord existing = record();
        when(mapper.selectByOrderId(101L)).thenReturn(null, existing);
        when(mapper.insert(any(PayRecord.class))).thenThrow(new DuplicateKeyException("duplicate order_id"));
        PayRecordServiceImpl service = service(mapper);

        assertDoesNotThrow(() -> service.ensurePayRecord("HOTEL", 101L, 7L, new BigDecimal("10.00")));

        verify(mapper, times(2)).selectByOrderId(101L);
        verify(mapper).insert(any(PayRecord.class));
    }

    @Test
    void existingOrderWithDifferentAmountCannotCreateAnotherPaymentRecord() {
        PayRecordMapper mapper = mock(PayRecordMapper.class);
        when(mapper.selectByOrderId(101L)).thenReturn(record());
        PayRecordServiceImpl service = service(mapper);

        assertThrows(IllegalArgumentException.class,
                () -> service.ensurePayRecord("HOTEL", 101L, 7L, new BigDecimal("11.00")));

        verify(mapper, never()).insert(any(PayRecord.class));
    }

    private PayRecordServiceImpl service(PayRecordMapper mapper) {
        PayRecordServiceImpl service = new PayRecordServiceImpl();
        ReflectionTestUtils.setField(service, "payRecordMapper", mapper);
        return service;
    }

    private PayRecord record() {
        PayRecord record = new PayRecord();
        record.setStatus("WAIT_PAY");
        record.setUserId(7L);
        record.setBizType("HOTEL");
        record.setMoney(new BigDecimal("10.00"));
        return record;
    }
}
