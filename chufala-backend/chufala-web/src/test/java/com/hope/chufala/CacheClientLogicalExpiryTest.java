package com.hope.chufala;

import com.hope.chufala.infra.CacheClient;
import com.hope.chufala.model.entity.Hotel;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CacheClientLogicalExpiryTest {
    @Test
    void minuteBasedExpiryCanBeWrittenAndRead() {
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        @SuppressWarnings("unchecked") ValueOperations<String, String> values = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(values);
        CacheClient cache = new CacheClient(redis);
        Hotel hotel = new Hotel();
        hotel.setId(42L);

        cache.setWithLogicalExpire("hotel:42", hotel, 5L, TimeUnit.MINUTES);
        ArgumentCaptor<String> json = ArgumentCaptor.forClass(String.class);
        verify(values).set(eq("hotel:42"), json.capture());
        when(values.get("hotel:42")).thenReturn(json.getValue());

        Hotel restored = cache.queryWithLogicalExpire("hotel:", 42L, Hotel.class,
                id -> fail("未过期时不应回源"), 5L, TimeUnit.MINUTES);
        assertEquals(42L, restored.getId());
    }
}
