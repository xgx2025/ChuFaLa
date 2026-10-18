package com.hope.chufala.infra.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

/** 只缓存详情查询中不存在的 ID，避免反复查询数据库。 */
@Slf4j
@Component
public class DetailMissCache {
    private final StringRedisTemplate redisTemplate;

    public DetailMissCache(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean isMissing(String type, Long id) {
        try {
            return "1".equals(redisTemplate.opsForValue().get(key(type, id)));
        } catch (RuntimeException e) {
            log.warn("详情负缓存读取失败，回源数据库，type={}, id={}", type, id, e);
            return false;
        }
    }

    public void rememberMissing(String type, Long id) {
        try {
            redisTemplate.opsForValue().set(key(type, id), "1",
                    Duration.ofSeconds(60 + ThreadLocalRandom.current().nextLong(30)));
        } catch (RuntimeException e) {
            log.warn("详情负缓存写入失败，type={}, id={}", type, id, e);
        }
    }

    public void forgetAfterCommit(String type, Long id) {
        if (id == null) return;
        if (TransactionSynchronizationManager.isActualTransactionActive()
                && TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    forget(type, id);
                }
            });
        } else {
            forget(type, id);
        }
    }

    private void forget(String type, Long id) {
        try {
            redisTemplate.delete(key(type, id));
        } catch (RuntimeException e) {
            log.warn("详情负缓存清除失败，type={}, id={}", type, id, e);
        }
    }

    private String key(String type, Long id) {
        return type + ":detail:missing:" + id;
    }
}
