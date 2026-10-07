package com.hope.chufala;

import com.hope.chufala.service.IHotelOrderService;
import com.hope.chufala.task.ExpiredHotelOrderTask;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ExpiredHotelOrderTaskTest {
    @Test
    void failedOldOrderDoesNotStarveLaterExpiredOrders() {
        IHotelOrderService service = mock(IHotelOrderService.class);
        when(service.getExpiredUnpaidOrderIds(100, null)).thenReturn(List.of(101L, 102L));
        when(service.getExpiredUnpaidOrderIds(100, 102L)).thenReturn(List.of(201L));
        doThrow(new IllegalStateException("gateway unavailable")).when(service).cancelDelayOrder(101L);
        ExpiredHotelOrderTask task = new ExpiredHotelOrderTask(service);

        task.cancelExpiredOrders();
        task.cancelExpiredOrders();

        verify(service).cancelDelayOrder(102L);
        verify(service).cancelDelayOrder(201L);
    }
}
