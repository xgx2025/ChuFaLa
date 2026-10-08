package com.hope.chufala;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hope.chufala.common.model.vo.PageResult;
import com.hope.chufala.mapper.HotelMapper;
import com.hope.chufala.mapper.HotelOrderMapper;
import com.hope.chufala.model.entity.Hotel;
import com.hope.chufala.model.entity.HotelOrder;
import com.hope.chufala.service.impl.HotelOrderServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class HotelOrderCursorPageTest {
    @Test
    void ordersUseStableCursorAndCountOnlyFirstPage() {
        HotelOrderMapper orderMapper = mock(HotelOrderMapper.class);
        HotelMapper hotelMapper = mock(HotelMapper.class);
        HotelOrderServiceImpl service = new HotelOrderServiceImpl();
        ReflectionTestUtils.setField(service, "hotelOrderMapper", orderMapper);
        ReflectionTestUtils.setField(service, "hotelMapper", hotelMapper);

        LocalDateTime time = LocalDateTime.of(2026, 10, 8, 12, 30);
        HotelOrder first = order(3L, time);
        HotelOrder second = order(2L, time);
        HotelOrder third = order(1L, time.minusHours(1));
        Hotel hotel = new Hotel();
        hotel.setId(9L);
        hotel.setName("测试酒店");
        when(orderMapper.selectCount(any())).thenReturn(3L);
        when(orderMapper.selectList(any())).thenReturn(List.of(first, second, third), List.of(third));
        when(hotelMapper.selectBatchIds(any())).thenReturn(List.of(hotel));

        PageResult<HotelOrder> page1 = service.getHotelOrderByUserIdPage(7L, "all", 2, null);
        assertEquals(List.of(first, second), page1.getData());
        assertEquals(3L, page1.getTotal());
        assertTrue(page1.getHasMore());
        assertNotNull(page1.getNextCursor());
        assertEquals("测试酒店", page1.getData().get(0).getHotelName());

        PageResult<HotelOrder> page2 = service.getHotelOrderByUserIdPage(7L, "all", 2, page1.getNextCursor());
        assertEquals(List.of(third), page2.getData());
        assertNull(page2.getTotal());
        assertFalse(page2.getHasMore());
        verify(orderMapper, times(1)).selectCount(any());
        ArgumentCaptor<QueryWrapper<HotelOrder>> queries = ArgumentCaptor.forClass(QueryWrapper.class);
        verify(orderMapper, times(2)).selectList(queries.capture());
        String secondSql = queries.getAllValues().get(1).getSqlSegment();
        assertTrue(secondSql.contains("book_time"));
        assertTrue(secondSql.contains("id"));
        assertTrue(secondSql.contains("LIMIT 3"));
    }

    private HotelOrder order(Long id, LocalDateTime time) {
        HotelOrder order = new HotelOrder();
        order.setId(id);
        order.setHotelId(9L);
        order.setBookTime(time);
        return order;
    }
}
