package com.hope.chufala;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hope.chufala.common.model.vo.PageResult;
import com.hope.chufala.infra.HotelListCache;
import com.hope.chufala.model.entity.Hotel;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Duration;
import java.util.List;

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
        String key = cache.firstPageKey("rating", "filters", 10);
        assertEquals("hotel:list:cursor:v1:generation-1:rating:10:filters", key);

        Hotel hotel = new Hotel();
        hotel.setId(42L);
        hotel.setName("测试酒店");
        PageResult<Hotel> page = new PageResult<>();
        page.setData(List.of(hotel));
        page.setSize(10);
        page.setHasMore(false);
        cache.put(key, page);
        verify(values).set(eq(key), argThat(json -> json.contains("测试酒店")), any(Duration.class));

        when(values.get(key)).thenReturn(new ObjectMapper().writeValueAsString(page));
        PageResult<Hotel> restored = cache.get(key);
        assertEquals(42L, restored.getData().get(0).getId());
        assertNull(restored.getData().get(0).getDistance());
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
}
