package com.hope.chufala.infra;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hope.chufala.common.model.vo.PageResult;
import com.hope.chufala.util.CursorPaginationUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/** 列表前 3 页共用的 Redis cache-aside 实现；缓存值不含用户距离。 */
@Slf4j
public abstract class CursorListCache<T> {
    private final String generationKey;
    private final String pagePrefix;
    private final TypeReference<PageResult<T>> pageType;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    protected CursorListCache(String listName, TypeReference<PageResult<T>> pageType,
                              StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        generationKey = listName + ":list:cursor:generation";
        pagePrefix = listName + ":list:cursor:v2:";
        this.pageType = pageType;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    /** 只为首页及新版游标标明的第 2～3 页生成键；旧游标和深页直接回源。 */
    public String pageKey(String sort, String scope, int size, CursorPaginationUtils.Cursor cursor) {
        int page = cursor == null ? 1 : cursor.page();
        if (page < 1 || page > 3) return null;
        try {
            String generation = redisTemplate.opsForValue().get(generationKey);
            if (generation == null) {
                String candidate = UUID.randomUUID().toString();
                Boolean created = redisTemplate.opsForValue().setIfAbsent(generationKey, candidate);
                generation = Boolean.TRUE.equals(created) ? candidate : redisTemplate.opsForValue().get(generationKey);
            }
            if (generation == null) return null;
            String position = cursor == null ? "start" : cursor.value() + ":" + cursor.id();
            return pagePrefix + generation + ':' + sort + ':' + size + ':' + scope + ':' + page + ':' + position;
        } catch (RuntimeException e) {
            log.warn("列表缓存版本读取失败，回源数据库，list={}", pagePrefix, e);
            return null;
        }
    }

    /** 读取缓存页；无缓存或 Redis 异常时返回 null。 */
    public PageResult<T> get(String key) {
        if (key == null) return null;
        try {
            String json = redisTemplate.opsForValue().get(key);
            return json == null ? null : objectMapper.readValue(json, pageType);
        } catch (Exception e) {
            log.warn("列表缓存读取失败，回源数据库，key={}", key, e);
            return null;
        }
    }

    /** 写入不含用户距离的分页结果；后续页与空页采用更短 TTL。 */
    public void put(String key, PageResult<T> page) {
        if (key == null) return;
        try {
            long seconds = page.getData().isEmpty() ? 30
                    : key.endsWith(":1:start") ? 180 + ThreadLocalRandom.current().nextLong(60)
                    : 90 + ThreadLocalRandom.current().nextLong(30);
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(page), Duration.ofSeconds(seconds));
        } catch (Exception e) {
            log.warn("列表缓存写入失败，key={}", key, e);
        }
    }

    /** 数据提交后切换缓存版本；旧页由 TTL 自然清理。 */
    public void invalidateAfterCommit() {
        if (TransactionSynchronizationManager.isActualTransactionActive()
                && TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    rotateGeneration();
                }
            });
        } else {
            rotateGeneration();
        }
    }

    private void rotateGeneration() {
        try {
            redisTemplate.opsForValue().set(generationKey, UUID.randomUUID().toString());
        } catch (RuntimeException e) {
            log.warn("列表缓存失效失败，旧缓存最多保留约 4 分钟，list={}", pagePrefix, e);
        }
    }
}
