package com.hope.chufala;

import com.hope.chufala.infra.cache.DetailMissCache;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DetailMissCacheTest {
    @Test
    void missingMarkerHasShortJitteredTtl() {
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        @SuppressWarnings("unchecked") ValueOperations<String, String> values = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(values);
        when(values.get("hotel:detail:missing:42")).thenReturn("1");
        DetailMissCache cache = new DetailMissCache(redis);

        assertTrue(cache.isMissing("hotel", 42L));
        cache.rememberMissing("hotel", 42L);

        verify(values).set(eq("hotel:detail:missing:42"), eq("1"), argThat(ttl ->
                ttl.compareTo(Duration.ofSeconds(60)) >= 0
                        && ttl.compareTo(Duration.ofSeconds(90)) < 0));
    }

    @Test
    void newlyCreatedIdClearsMarkerAfterCommit() {
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        DetailMissCache cache = new DetailMissCache(redis);
        TransactionSynchronizationManager.initSynchronization();
        TransactionSynchronizationManager.setActualTransactionActive(true);
        try {
            cache.forgetAfterCommit("attraction", 42L);
            verify(redis, never()).delete(anyString());
            for (TransactionSynchronization synchronization : TransactionSynchronizationManager.getSynchronizations()) {
                synchronization.afterCommit();
            }
            verify(redis).delete("attraction:detail:missing:42");
        } finally {
            TransactionSynchronizationManager.setActualTransactionActive(false);
            TransactionSynchronizationManager.clearSynchronization();
        }
    }
}
