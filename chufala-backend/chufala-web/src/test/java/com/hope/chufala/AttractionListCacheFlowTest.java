package com.hope.chufala;

import com.hope.chufala.common.model.vo.PageResult;
import com.hope.chufala.infra.AttractionListCache;
import com.hope.chufala.mapper.AttractionMapper;
import com.hope.chufala.model.dto.AttractionPageQueryDTO;
import com.hope.chufala.model.entity.Attraction;
import com.hope.chufala.service.impl.AttractionServiceImpl;
import com.hope.chufala.util.CursorPaginationUtils;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AttractionListCacheFlowTest {
    @Test
    void secondPageCacheHitRecalculatesDistanceWithoutSql() {
        AttractionMapper mapper = mock(AttractionMapper.class);
        AttractionListCache cache = mock(AttractionListCache.class);
        AttractionServiceImpl service = service(mapper, cache);
        AttractionPageQueryDTO query = query();
        String scope = CursorPaginationUtils.scope(null, null, null, List.of());
        query.setCursor(CursorPaginationUtils.encode("rating", scope, 4.9, 9L, 2));
        Attraction attraction = attraction(8L, 4.8);
        attraction.setDistance(999.0);
        when(cache.pageKey(eq("rating"), eq(scope), eq(1),
                eq(new CursorPaginationUtils.Cursor(4.9, 9L, 2)))).thenReturn("page-2");
        when(cache.get("page-2")).thenReturn(page(attraction));

        PageResult<Attraction> result = service.queryAttraction(query);

        assertNotEquals(999.0, result.getData().get(0).getDistance());
        verifyNoInteractions(mapper);
    }

    @Test
    void firstPageStoresRawRowsAndNextCursorPointsToSecondPage() {
        AttractionMapper mapper = mock(AttractionMapper.class);
        AttractionListCache cache = mock(AttractionListCache.class);
        AttractionServiceImpl service = service(mapper, cache);
        when(cache.pageKey(eq("rating"), anyString(), eq(1), isNull())).thenReturn("first");
        when(mapper.selectAttractionPage(eq(2), any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(attraction(9L, 4.9), attraction(8L, 4.8)));
        doAnswer(invocation -> {
            PageResult<Attraction> cached = invocation.getArgument(1);
            assertNull(cached.getData().get(0).getDistance());
            return null;
        }).when(cache).put(eq("first"), any());

        PageResult<Attraction> result = service.queryAttraction(query());

        assertTrue(result.getHasMore());
        assertNotNull(result.getData().get(0).getDistance());
        String scope = CursorPaginationUtils.scope(null, null, null, List.of());
        assertEquals(2, CursorPaginationUtils.decode(result.getNextCursor(), "rating", scope).page());
        verify(cache).put(eq("first"), any());
    }

    @Test
    void fourthPageBypassesCacheAndInsertInvalidatesList() {
        AttractionMapper mapper = mock(AttractionMapper.class);
        AttractionListCache cache = mock(AttractionListCache.class);
        AttractionServiceImpl service = service(mapper, cache);
        AttractionPageQueryDTO query = query();
        String scope = CursorPaginationUtils.scope(null, null, null, List.of());
        query.setCursor(CursorPaginationUtils.encode("rating", scope, 4.5, 20L, 4));
        when(mapper.selectAttractionPage(anyInt(), any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(attraction(19L, 4.4)));

        service.queryAttraction(query);

        verify(cache, never()).get(anyString());
        verify(cache, never()).put(anyString(), any());
        verify(mapper).selectAttractionPage(eq(2), any(), any(), any(), any(), eq(20L), eq(4.5));

        when(mapper.insert(any(Attraction.class))).thenReturn(1);
        assertTrue(service.addAttraction(attraction(21L, 5.0)));
        verify(cache).invalidateAfterCommit();
    }

    private AttractionServiceImpl service(AttractionMapper mapper, AttractionListCache cache) {
        AttractionServiceImpl service = new AttractionServiceImpl();
        ReflectionTestUtils.setField(service, "attractionMapper", mapper);
        ReflectionTestUtils.setField(service, "attractionListCache", cache);
        return service;
    }

    private AttractionPageQueryDTO query() {
        AttractionPageQueryDTO query = new AttractionPageQueryDTO();
        query.setSize(1);
        query.setUserLng(116.40);
        query.setUserLat(39.90);
        return query;
    }

    private Attraction attraction(long id, double rating) {
        Attraction attraction = new Attraction();
        attraction.setId(id);
        attraction.setRating(rating);
        attraction.setLongitude(116.41);
        attraction.setLatitude(39.91);
        return attraction;
    }

    private PageResult<Attraction> page(Attraction attraction) {
        PageResult<Attraction> page = new PageResult<>();
        page.setData(List.of(attraction));
        page.setSize(1);
        page.setHasMore(false);
        return page;
    }
}
