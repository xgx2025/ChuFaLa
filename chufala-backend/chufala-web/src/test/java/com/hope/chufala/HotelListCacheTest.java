package com.hope.chufala;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hope.chufala.common.model.vo.PageResult;
import com.hope.chufala.infra.HotelListCache;
import com.hope.chufala.model.entity.Hotel;
import com.hope.chufala.util.CursorPaginationUtils;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class HotelListCacheTest {
    @Test
    void cachedPageRoundTripsWithoutUserDistance() throws Exception {
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        @SuppressWarnings("unchecked") ValueOperations<String, String> values = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(values);
        when(values.get("hotel:list:cursor:generation")).thenReturn("generation-1");
        HotelListCache cache = new HotelListCache(redis, new ObjectMapper());
        String key = cache.pageKey("rating", "filters", 10, null);
        assertEquals("hotel:list:cursor:v2:generation-1:rating:10:filters:1:start", key);

        Hotel hotel = new Hotel();
        hotel.setId(42L);
        hotel.setName("测试酒店");
        PageResult<Hotel> page = new PageResult<>();
        page.setData(List.of(hotel));
        page.setSize(10);
        page.setHasMore(false);
        cache.put(key, page);
        verify(values).set(eq(key), argThat(json -> json.contains("测试酒店")),
                argThat(ttl -> ttl.compareTo(Duration.ofSeconds(180)) >= 0
                        && ttl.compareTo(Duration.ofSeconds(240)) < 0));

        when(values.get(key)).thenReturn(new ObjectMapper().writeValueAsString(page));
        PageResult<Hotel> restored = cache.get(key);
        assertEquals(42L, restored.getData().get(0).getId());
        assertNull(restored.getData().get(0).getDistance());
    }

    @Test
    void pageKeyIncludesSortValueIdSizeAndScopeAndSkipsDeepPages() {
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        @SuppressWarnings("unchecked") ValueOperations<String, String> values = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(values);
        when(values.get("hotel:list:cursor:generation")).thenReturn("generation-1");
        HotelListCache cache = new HotelListCache(redis, new ObjectMapper());

        String page2 = cache.pageKey("rating", "filters", 10,
                new CursorPaginationUtils.Cursor(4.8, 42L, 2));
        assertEquals("hotel:list:cursor:v2:generation-1:rating:10:filters:2:4.8:42", page2);
        assertNotEquals(page2, cache.pageKey("rating", "filters", 10,
                new CursorPaginationUtils.Cursor(4.7, 42L, 2)));
        assertNotEquals(page2, cache.pageKey("rating", "filters", 20,
                new CursorPaginationUtils.Cursor(4.8, 42L, 2)));
        assertNotEquals(page2, cache.pageKey("rating", "other", 10,
                new CursorPaginationUtils.Cursor(4.8, 42L, 2)));
        assertNotNull(cache.pageKey("rating", "filters", 10,
                new CursorPaginationUtils.Cursor(4.8, 42L, 3)));
        assertNull(cache.pageKey("rating", "filters", 10,
                new CursorPaginationUtils.Cursor(4.8, 42L, 4)));
        assertNull(cache.pageKey("rating", "filters", 10,
                new CursorPaginationUtils.Cursor(4.8, 42L, 0)));
    }

    @Test
    void generationChangesOnlyAfterTransactionCommits() {
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        @SuppressWarnings("unchecked") ValueOperations<String, String> values = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(values);
        HotelListCache cache = new HotelListCache(redis, new ObjectMapper());

        TransactionSynchronizationManager.initSynchronization();
        TransactionSynchronizationManager.setActualTransactionActive(true);
        try {
            cache.invalidateAfterCommit();
            verify(values, never()).set(eq("hotel:list:cursor:generation"), anyString());
            for (TransactionSynchronization synchronization : TransactionSynchronizationManager.getSynchronizations()) {
                synchronization.afterCommit();
            }
            verify(values).set(eq("hotel:list:cursor:generation"), anyString());
        } finally {
            TransactionSynchronizationManager.setActualTransactionActive(false);
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void oldPageWriteCannotPopulateNewGenerationAfterInvalidation() {
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        @SuppressWarnings("unchecked") ValueOperations<String, String> values = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(values);
        AtomicReference<String> generation = new AtomicReference<>("old");
        when(values.get("hotel:list:cursor:generation")).thenAnswer(invocation -> generation.get());
        doAnswer(invocation -> {
            generation.set(invocation.getArgument(1));
            return null;
        }).when(values).set(eq("hotel:list:cursor:generation"), anyString());
        HotelListCache cache = new HotelListCache(redis, new ObjectMapper());

        String oldKey = cache.pageKey("rating", "filters", 10, null);
        cache.invalidateAfterCommit();
        String newKey = cache.pageKey("rating", "filters", 10, null);

        assertNotEquals(oldKey, newKey);
        PageResult<Hotel> stale = new PageResult<>();
        stale.setData(List.of(new Hotel()));
        cache.put(oldKey, stale);
        verify(values).set(eq(oldKey), anyString(), any(Duration.class));
        assertNull(cache.get(newKey));
    }
}
