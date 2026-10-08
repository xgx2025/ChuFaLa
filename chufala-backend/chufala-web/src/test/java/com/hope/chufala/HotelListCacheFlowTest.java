package com.hope.chufala;

import com.hope.chufala.common.model.vo.PageResult;
import com.hope.chufala.infra.HotelListCache;
import com.hope.chufala.mapper.HotelMapper;
import com.hope.chufala.model.dto.HotelPageQueryDTO;
import com.hope.chufala.model.entity.Hotel;
import com.hope.chufala.service.impl.HotelServiceImpl;
import com.hope.chufala.util.CursorPaginationUtils;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class HotelListCacheFlowTest {
    @Test
    void cacheHitAvoidsSqlAndCalculatesCurrentUsersDistance() {
        HotelMapper mapper = mock(HotelMapper.class);
        HotelListCache cache = mock(HotelListCache.class);
        HotelServiceImpl service = service(mapper, cache);
        Hotel hotel = hotel();
        PageResult<Hotel> cached = page(hotel);
        when(cache.pageKey(eq("rating"), anyString(), eq(10), isNull())).thenReturn("page-key");
        when(cache.get("page-key")).thenReturn(cached);

        PageResult<Hotel> result = service.queryHotelsByScoreRank(query("rating"));

        assertSame(cached, result);
        assertNotNull(result.getData().get(0).getDistance());
        verifyNoInteractions(mapper);
        verify(cache, never()).put(anyString(), any());
    }

    @Test
    void cacheMissStoresPageBeforeAddingUserDistance() {
        HotelMapper mapper = mock(HotelMapper.class);
        HotelListCache cache = mock(HotelListCache.class);
        HotelServiceImpl service = service(mapper, cache);
        Hotel hotel = hotel();
        when(cache.pageKey(eq("rating"), anyString(), eq(10), isNull())).thenReturn("page-key");
        when(mapper.selectByScoreRankPage(anyInt(), any(), any(), any(), any(), any(), any(),
                anyDouble(), anyDouble(), any(), any())).thenReturn(List.of(hotel));
        doAnswer(invocation -> {
            PageResult<Hotel> page = invocation.getArgument(1);
            assertNull(page.getData().get(0).getDistance());
            return null;
        }).when(cache).put(eq("page-key"), any());

        PageResult<Hotel> result = service.queryHotelsByScoreRank(query("rating"));

        assertNotNull(result.getData().get(0).getDistance());
        verify(cache).put(eq("page-key"), any());
        verify(mapper).selectByScoreRankPage(eq(11), any(), any(), any(), any(), any(), any(),
                anyDouble(), anyDouble(), any(), any());
    }

    @Test
    void distanceSortBypassesSharedCache() {
        HotelMapper mapper = mock(HotelMapper.class);
        HotelListCache cache = mock(HotelListCache.class);
        HotelServiceImpl service = service(mapper, cache);
        when(mapper.selectByScoreRankPage(anyInt(), any(), any(), any(), any(), any(), any(),
                anyDouble(), anyDouble(), any(), any())).thenReturn(List.of(hotel()));

        service.queryHotelsByScoreRank(query("distance"));

        verifyNoInteractions(cache);
        verify(mapper).selectByScoreRankPage(eq(11), any(), any(), any(), any(), any(),
                eq("distance"), anyDouble(), anyDouble(), any(), any());
    }

    @Test
    void secondPageUsesCacheAndFourthPageQueriesDatabase() {
        HotelMapper mapper = mock(HotelMapper.class);
        HotelListCache cache = mock(HotelListCache.class);
        HotelServiceImpl service = service(mapper, cache);
        HotelPageQueryDTO second = query("rating");
        String scope = CursorPaginationUtils.scope(null, null, null, null, List.of(), null, null);
        second.setCursor(CursorPaginationUtils.encode("rating", scope, 4.9, 9L, 2));
        when(cache.pageKey(eq("rating"), eq(scope), eq(10),
                eq(new CursorPaginationUtils.Cursor(4.9, 9L, 2)))).thenReturn("page-2");
        when(cache.get("page-2")).thenReturn(page(hotel()));

        service.queryHotelsByScoreRank(second);
        verifyNoInteractions(mapper);

        HotelPageQueryDTO fourth = query("rating");
        fourth.setCursor(CursorPaginationUtils.encode("rating", scope, 4.5, 20L, 4));
        when(mapper.selectByScoreRankPage(anyInt(), any(), any(), any(), any(), any(), any(),
                anyDouble(), anyDouble(), any(), any())).thenReturn(List.of(hotel()));
        service.queryHotelsByScoreRank(fourth);
        verify(cache).pageKey(eq("rating"), eq(scope), eq(10),
                eq(new CursorPaginationUtils.Cursor(4.5, 20L, 4)));
        verify(cache, never()).get(null);
        verify(mapper).selectByScoreRankPage(eq(11), any(), any(), any(), any(), any(), any(),
                anyDouble(), anyDouble(), eq(20L), eq(4.5));
    }

    @Test
    void thirdPageCursorMovesToUncachedDepthAndStaysThere() {
        HotelMapper mapper = mock(HotelMapper.class);
        HotelListCache cache = mock(HotelListCache.class);
        HotelServiceImpl service = service(mapper, cache);
        String scope = CursorPaginationUtils.scope(null, null, null, null, List.of(), null, null);
        HotelPageQueryDTO query = query("rating");
        query.setSize(1);
        query.setCursor(CursorPaginationUtils.encode("rating", scope, 4.9, 9L, 3));
        Hotel first = hotel();
        first.setId(8L);
        first.setOverallRating(4.8);
        Hotel second = hotel();
        second.setId(7L);
        second.setOverallRating(4.7);
        when(mapper.selectByScoreRankPage(anyInt(), any(), any(), any(), any(), any(), any(),
                anyDouble(), anyDouble(), any(), any())).thenReturn(List.of(first, second));

        PageResult<Hotel> third = service.queryHotelsByScoreRank(query);
        assertEquals(4, CursorPaginationUtils.decode(third.getNextCursor(), "rating", scope).page());
        query.setCursor(third.getNextCursor());
        PageResult<Hotel> fourth = service.queryHotelsByScoreRank(query);
        assertEquals(4, CursorPaginationUtils.decode(fourth.getNextCursor(), "rating", scope).page());
    }

    private HotelServiceImpl service(HotelMapper mapper, HotelListCache cache) {
        HotelServiceImpl service = new HotelServiceImpl();
        ReflectionTestUtils.setField(service, "hotelMapper", mapper);
        ReflectionTestUtils.setField(service, "hotelListCache", cache);
        return service;
    }

    private HotelPageQueryDTO query(String sort) {
        HotelPageQueryDTO query = new HotelPageQueryDTO();
        query.setSize(10);
        query.setSort(sort);
        query.setUserLng(116.40);
        query.setUserLat(39.90);
        return query;
    }

    private Hotel hotel() {
        Hotel hotel = new Hotel();
        hotel.setId(42L);
        hotel.setName("测试酒店");
        hotel.setLongitude(116.41);
        hotel.setLatitude(39.91);
        hotel.setOverallRating(4.8);
        return hotel;
    }

    private PageResult<Hotel> page(Hotel hotel) {
        PageResult<Hotel> result = new PageResult<>();
        result.setData(List.of(hotel));
        result.setSize(10);
        result.setHasMore(false);
        return result;
    }
}
