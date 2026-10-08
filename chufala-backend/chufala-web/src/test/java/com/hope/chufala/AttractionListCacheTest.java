package com.hope.chufala;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hope.chufala.common.model.vo.PageResult;
import com.hope.chufala.infra.AttractionListCache;
import com.hope.chufala.model.entity.Attraction;
import com.hope.chufala.util.CursorPaginationUtils;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AttractionListCacheTest {
    @Test
    void pageKeysAreIsolatedAndCacheRoundTripsWithoutDistance() throws Exception {
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        @SuppressWarnings("unchecked") ValueOperations<String, String> values = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(values);
        when(values.get("attraction:list:cursor:generation")).thenReturn("generation-1");
        AttractionListCache cache = new AttractionListCache(redis, new ObjectMapper());
        String key = cache.pageKey("rating", "filters", 12,
                new CursorPaginationUtils.Cursor(4.8, 42L, 3));
        assertEquals("attraction:list:cursor:v2:generation-1:rating:12:filters:3:4.8:42", key);

        Attraction attraction = new Attraction();
        attraction.setId(41L);
        PageResult<Attraction> page = new PageResult<>();
        page.setData(List.of(attraction));
        page.setSize(12);
        page.setHasMore(false);
        cache.put(key, page);
        verify(values).set(eq(key), anyString(), argThat(ttl ->
                ttl.compareTo(Duration.ofSeconds(90)) >= 0 && ttl.compareTo(Duration.ofSeconds(120)) < 0));

        when(values.get(key)).thenReturn(new ObjectMapper().writeValueAsString(page));
        PageResult<Attraction> restored = cache.get(key);
        assertEquals(41L, restored.getData().get(0).getId());
        assertNull(restored.getData().get(0).getDistance());

        cache.invalidateAfterCommit();
        verify(values).set(eq("attraction:list:cursor:generation"), anyString());
    }
}
