package com.hope.chufala;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hope.chufala.common.model.vo.PageResult;
import com.hope.chufala.infra.cache.HotelListCache;
import com.hope.chufala.model.entity.Hotel;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CursorListCacheProtectionTest {
    @Test
    void cacheMissLoadsOnlyAfterAcquiringLock() {
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        @SuppressWarnings("unchecked") ValueOperations<String, String> values = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(values);
        HotelListCache cache = new HotelListCache(redis, new ObjectMapper());
        String key = "hotel:list:cursor:v2:g:rating:10:scope:1:start";
        when(values.setIfAbsent(eq("lock:cache:page:" + key), anyString(), eq(15L), eq(TimeUnit.SECONDS)))
                .thenReturn(true);
        AtomicInteger loads = new AtomicInteger();

        PageResult<Hotel> result = cache.getOrLoad(key, () -> {
            loads.incrementAndGet();
            return page();
        });

        assertEquals(1, loads.get());
        assertEquals(1, result.getData().size());
        verify(values).set(eq(key), anyString(), any(Duration.class));
    }

    @Test
    void lockContenderReadsResultWithoutLoadingDatabase() throws Exception {
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        @SuppressWarnings("unchecked") ValueOperations<String, String> values = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(values);
        HotelListCache cache = new HotelListCache(redis, new ObjectMapper());
        String key = "hotel:list:cursor:v2:g:rating:10:scope:1:start";
        String json = new ObjectMapper().writeValueAsString(page());
        AtomicInteger reads = new AtomicInteger();
        when(values.get(key)).thenAnswer(invocation -> reads.incrementAndGet() == 1 ? null : json);

        PageResult<Hotel> result = cache.getOrLoad(key, () -> fail("不应重复查库"));

        assertEquals(1, result.getData().size());
        verify(values).setIfAbsent(eq("lock:cache:page:" + key), anyString(), eq(15L), eq(TimeUnit.SECONDS));
    }

    @Test
    void redisFailureFallsBackToDatabase() {
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        @SuppressWarnings("unchecked") ValueOperations<String, String> values = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(values);
        HotelListCache cache = new HotelListCache(redis, new ObjectMapper());
        String key = "hotel:list:cursor:v2:g:rating:10:scope:1:start";
        when(values.setIfAbsent(eq("lock:cache:page:" + key), anyString(), eq(15L), eq(TimeUnit.SECONDS)))
                .thenThrow(new RedisConnectionFailureException("unavailable"));

        assertEquals(1, cache.getOrLoad(key, this::page).getData().size());
    }

    @Test
    void databaseErrorIsNotRetriedAsRedisFailure() {
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        @SuppressWarnings("unchecked") ValueOperations<String, String> values = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(values);
        HotelListCache cache = new HotelListCache(redis, new ObjectMapper());
        String key = "hotel:list:cursor:v2:g:rating:10:scope:1:start";
        when(values.setIfAbsent(eq("lock:cache:page:" + key), anyString(), eq(15L), eq(TimeUnit.SECONDS)))
                .thenReturn(true);
        AtomicInteger loads = new AtomicInteger();

        assertThrows(DataIntegrityViolationException.class, () -> cache.getOrLoad(key, () -> {
            loads.incrementAndGet();
            throw new DataIntegrityViolationException("database failed");
        }));
        assertEquals(1, loads.get());
    }

    private PageResult<Hotel> page() {
        PageResult<Hotel> page = new PageResult<>();
        page.setData(List.of(new Hotel()));
        page.setSize(10);
        page.setHasMore(false);
        return page;
    }
}
