package com.hope.chufala;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hope.chufala.common.model.vo.PageResult;
import com.hope.chufala.infra.cache.AttractionListCache;
import com.hope.chufala.model.entity.Attraction;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AttractionFirstPageMultiLevelCacheTest {
    private static final String GENERATION_KEY = "attraction:list:cursor:generation";
    private static final String PAGE_KEY = "attraction:list:cursor:v2:g1:rating:12:scope:1:start";

    @Test
    void secondReadUsesLocalSnapshotWithoutRedisOrUserDistance() throws Exception {
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        @SuppressWarnings("unchecked") ValueOperations<String, String> values = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(values);
        when(values.get(GENERATION_KEY)).thenReturn("g1");
        when(values.get(PAGE_KEY)).thenReturn(jsonPage(41L));
        AttractionListCache cache = new AttractionListCache(redis, new ObjectMapper());

        PageResult<Attraction> first = cache.getFirstPageOrLoad("scope", 12, () -> fail("不应查库"));
        first.getData().get(0).setDistance(123.0);
        PageResult<Attraction> second = cache.getFirstPageOrLoad("scope", 12, () -> fail("不应查库"));

        assertNotSame(first, second);
        assertNull(second.getData().get(0).getDistance());
        verify(values, times(1)).get(GENERATION_KEY);
        verify(values, times(1)).get(PAGE_KEY);
    }

    @Test
    void localInvalidationForcesNextReadThroughRedis() throws Exception {
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        @SuppressWarnings("unchecked") ValueOperations<String, String> values = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(values);
        when(values.get(GENERATION_KEY)).thenReturn("g1");
        when(values.get(PAGE_KEY)).thenReturn(jsonPage(41L), jsonPage(42L));
        AttractionListCache cache = new AttractionListCache(redis, new ObjectMapper());

        assertEquals(41L, cache.getFirstPageOrLoad("scope", 12, () -> fail("不应查库"))
                .getData().get(0).getId());
        cache.invalidateLocal();
        assertEquals(42L, cache.getFirstPageOrLoad("scope", 12, () -> fail("不应查库"))
                .getData().get(0).getId());
        verify(values, times(2)).get(GENERATION_KEY);
        verify(values, times(2)).get(PAGE_KEY);
    }

    @Test
    void writeInvalidatesAndPublishesOnlyAfterCommit() {
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        @SuppressWarnings("unchecked") ValueOperations<String, String> values = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(values);
        AttractionListCache cache = new AttractionListCache(redis, new ObjectMapper());

        TransactionSynchronizationManager.initSynchronization();
        TransactionSynchronizationManager.setActualTransactionActive(true);
        try {
            cache.invalidateAfterCommit();
            verify(values, never()).set(eq(GENERATION_KEY), anyString());
            verify(redis, never()).convertAndSend(eq(AttractionListCache.INVALIDATION_CHANNEL), anyString());
            for (TransactionSynchronization synchronization : TransactionSynchronizationManager.getSynchronizations()) {
                synchronization.afterCommit();
            }
            verify(values).set(eq(GENERATION_KEY), anyString());
            verify(redis).convertAndSend(AttractionListCache.INVALIDATION_CHANNEL, "invalidate");
        } finally {
            TransactionSynchronizationManager.setActualTransactionActive(false);
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void invalidationDuringLoadCannotRepopulateOldLocalPage() throws Exception {
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        @SuppressWarnings("unchecked") ValueOperations<String, String> values = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(values);
        when(values.get(GENERATION_KEY)).thenReturn("g1");
        CountDownLatch reading = new CountDownLatch(1);
        CountDownLatch resume = new CountDownLatch(1);
        AtomicInteger reads = new AtomicInteger();
        when(values.get(PAGE_KEY)).thenAnswer(invocation -> {
            if (reads.incrementAndGet() == 1) {
                reading.countDown();
                assertTrue(resume.await(5, TimeUnit.SECONDS));
                return jsonPage(41L);
            }
            return jsonPage(42L);
        });
        AttractionListCache cache = new AttractionListCache(redis, new ObjectMapper());
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Future<PageResult<Attraction>> first = executor.submit(
                    () -> cache.getFirstPageOrLoad("scope", 12, () -> fail("不应查库")));
            assertTrue(reading.await(5, TimeUnit.SECONDS));
            cache.invalidateLocal();
            resume.countDown();
            assertEquals(41L, first.get(5, TimeUnit.SECONDS).getData().get(0).getId());
            assertEquals(42L, cache.getFirstPageOrLoad("scope", 12, () -> fail("不应查库"))
                    .getData().get(0).getId());
            verify(values, times(2)).get(PAGE_KEY);
        } finally {
            resume.countDown();
            executor.shutdownNow();
        }
    }

    private String jsonPage(long id) throws Exception {
        Attraction attraction = new Attraction();
        attraction.setId(id);
        PageResult<Attraction> page = new PageResult<>();
        page.setData(List.of(attraction));
        page.setSize(12);
        page.setHasMore(false);
        return new ObjectMapper().writeValueAsString(page);
    }
}
