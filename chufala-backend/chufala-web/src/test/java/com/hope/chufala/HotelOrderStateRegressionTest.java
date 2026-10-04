package com.hope.chufala;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.hope.chufala.constant.HotelOrderStatus;
import com.hope.chufala.exception.OrderAlreadyCancelledException;
import com.hope.chufala.mapper.HotelOrderMapper;
import com.hope.chufala.mapper.RoomMapper;
import com.hope.chufala.mapper.RoomDailyStockMapper;
import com.hope.chufala.model.entity.HotelOrder;
import com.hope.chufala.service.impl.HotelOrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class HotelOrderStateRegressionTest {
    private final HotelOrderMapper orderMapper = mock(HotelOrderMapper.class);
    private final RoomMapper roomMapper = mock(RoomMapper.class);
    private final RoomDailyStockMapper dailyStockMapper = mock(RoomDailyStockMapper.class);
    private final HotelOrderServiceImpl service = new HotelOrderServiceImpl();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "hotelOrderMapper", orderMapper);
        ReflectionTestUtils.setField(service, "roomMapper", roomMapper);
        ReflectionTestUtils.setField(service, "roomDailyStockMapper", dailyStockMapper);
    }

    @Test
    void unpaidStateMatchesDatabaseAndFrontend() {
        assertEquals("待支付", HotelOrderStatus.UNPAID);
        assertEquals("待支付", HotelOrderStatus.normalizeFilter("未支付"));
    }

    @Test
    void legacyUnpaidFilterQueriesCanonicalDatabaseStatus() {
        service.findHotelOrdersByUserIdWithConditions(202L, "未支付", null);

        verify(orderMapper).selectList(org.mockito.ArgumentMatchers.<QueryWrapper<HotelOrder>>argThat(query ->
                query.getSqlSegment().contains("order_status")
                        && query.getParamNameValuePairs().containsValue(HotelOrderStatus.UNPAID)));
    }

    @Test
    void repeatedCancellationDoesNotRestoreStockTwice() {
        when(orderMapper.selectOne(any(QueryWrapper.class))).thenReturn(order(HotelOrderStatus.CANCELLED));

        assertDoesNotThrow(() -> service.cancelOrder(101L, 202L));

        verify(orderMapper, never()).update(isNull(), any(UpdateWrapper.class));
        verify(dailyStockMapper, never()).increaseStock(any(), any(LocalDate.class), any(Integer.class));
    }

    @Test
    void paidOrderCannotBeCancelled() {
        when(orderMapper.selectOne(any(QueryWrapper.class))).thenReturn(order(HotelOrderStatus.PAID));

        assertThrows(IllegalArgumentException.class, () -> service.cancelOrder(101L, 202L));
        verify(dailyStockMapper, never()).increaseStock(any(), any(LocalDate.class), any(Integer.class));
    }

    @Test
    void cancellationRestoresStockOnlyAfterPendingToCancelledTransition() {
        when(orderMapper.selectOne(any(QueryWrapper.class))).thenReturn(order(HotelOrderStatus.UNPAID));
        when(orderMapper.update(isNull(), any(UpdateWrapper.class))).thenReturn(1);
        when(dailyStockMapper.increaseStock(any(), any(LocalDate.class), any(Integer.class))).thenReturn(1);

        service.cancelOrder(101L, 202L);

        verify(orderMapper).update(isNull(), org.mockito.ArgumentMatchers.<UpdateWrapper<HotelOrder>>argThat(update ->
                update.getSqlSegment().contains("order_status")
                        && update.getSqlSegment().contains("user_id")
                        && update.getParamNameValuePairs().containsValue(HotelOrderStatus.UNPAID)
                        && update.getParamNameValuePairs().containsValue(HotelOrderStatus.CANCELLED)));
        verify(dailyStockMapper).increaseStock(303L, LocalDate.of(2026, 10, 4), 2);
        verify(dailyStockMapper).increaseStock(303L, LocalDate.of(2026, 10, 5), 2);
    }

    @Test
    void concurrentPaymentWinningTransitionCannotRestoreStock() {
        when(orderMapper.selectOne(any(QueryWrapper.class))).thenReturn(order(HotelOrderStatus.UNPAID), order(HotelOrderStatus.PAID));
        when(orderMapper.update(isNull(), any(UpdateWrapper.class))).thenReturn(0);

        assertThrows(IllegalArgumentException.class, () -> service.cancelOrder(101L, 202L));
        verify(dailyStockMapper, never()).increaseStock(any(), any(LocalDate.class), any(Integer.class));
    }

    @Test
    void delayedCancellationUsesDatabaseQuantityAndChecksExpiry() {
        when(orderMapper.selectOne(any(QueryWrapper.class))).thenReturn(order(HotelOrderStatus.UNPAID));
        when(orderMapper.update(isNull(), any(UpdateWrapper.class))).thenReturn(1);
        when(dailyStockMapper.increaseStock(any(), any(LocalDate.class), any(Integer.class))).thenReturn(1);

        service.cancelDelayOrder(101L);

        verify(orderMapper).update(isNull(), org.mockito.ArgumentMatchers.<UpdateWrapper<HotelOrder>>argThat(update ->
                update.getSqlSegment().contains("book_time")
                        && update.getParamNameValuePairs().containsValue(HotelOrderStatus.UNPAID)));
        verify(dailyStockMapper).increaseStock(303L, LocalDate.of(2026, 10, 4), 2);
        verify(dailyStockMapper).increaseStock(303L, LocalDate.of(2026, 10, 5), 2);
    }

    @Test
    void stockRestoreFailureIsNotIgnored() {
        when(orderMapper.selectOne(any(QueryWrapper.class))).thenReturn(order(HotelOrderStatus.UNPAID));
        when(orderMapper.update(isNull(), any(UpdateWrapper.class))).thenReturn(1);

        assertThrows(IllegalStateException.class, () -> service.cancelOrder(101L, 202L));
    }

    @Test
    void deletingPendingOrderCancelsAndRestoresStockInSameTransaction() {
        when(orderMapper.selectOne(any(QueryWrapper.class))).thenReturn(order(HotelOrderStatus.UNPAID));
        when(orderMapper.update(isNull(), any(UpdateWrapper.class))).thenReturn(1);
        when(dailyStockMapper.increaseStock(any(), any(LocalDate.class), any(Integer.class))).thenReturn(1);

        service.deleteOrder(101L, 202L);

        verify(dailyStockMapper).increaseStock(303L, LocalDate.of(2026, 10, 4), 2);
        verify(dailyStockMapper).increaseStock(303L, LocalDate.of(2026, 10, 5), 2);
        verify(orderMapper).update(isNull(), org.mockito.ArgumentMatchers.<UpdateWrapper<HotelOrder>>argThat(update ->
                update.getSqlSegment().contains("is_deleted")
                        && update.getSqlSegment().contains("user_id")));
    }

    @Test
    void paymentCannotResurrectCancelledOrder() {
        when(orderMapper.update(isNull(), any(UpdateWrapper.class))).thenReturn(0);
        when(orderMapper.selectOne(any(QueryWrapper.class))).thenReturn(order(HotelOrderStatus.CANCELLED));

        assertThrows(OrderAlreadyCancelledException.class, () -> service.markOrderPaid(101L));
        verify(orderMapper).update(isNull(), org.mockito.ArgumentMatchers.<UpdateWrapper<HotelOrder>>argThat(update ->
                update.getSqlSegment().contains("order_status")
                        && update.getParamNameValuePairs().containsValue(HotelOrderStatus.UNPAID)));
    }

    private HotelOrder order(String status) {
        HotelOrder order = new HotelOrder();
        order.setOrderId(101L);
        order.setUserId(202L);
        order.setRoomTypeId(303L);
        order.setRoomCount(2);
        order.setCheckIn(LocalDate.of(2026, 10, 4));
        order.setCheckOut(LocalDate.of(2026, 10, 6));
        order.setOrderStatus(status);
        return order;
    }
}
