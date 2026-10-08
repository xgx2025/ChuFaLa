package com.hope.chufala.infra;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hope.chufala.common.model.vo.PageResult;
import com.hope.chufala.model.entity.Hotel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/** 酒店列表首页缓存；仅保存不含用户距离的分页结果。 */
@Slf4j
@Component
public class HotelListCache {
    private static final String GENERATION_KEY = "hotel:list:cursor:generation";
    private static final String PAGE_PREFIX = "hotel:list:cursor:v1:";
    private static final TypeReference<PageResult<Hotel>> PAGE_TYPE = new TypeReference<>() { };

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public HotelListCache(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * 生成首页缓存键。缓存版本由 Redis 统一维护，空版本会重新生成，避免键被淘汰后复用旧缓存。
     * Redis 不可用时返回 null，查询直接回源数据库。
     */
    public String firstPageKey(String sort, String scope, int size) {
        try {
            String generation = redisTemplate.opsForValue().get(GENERATION_KEY);
            if (generation == null) {
                String candidate = UUID.randomUUID().toString();
                Boolean created = redisTemplate.opsForValue().setIfAbsent(GENERATION_KEY, candidate);
                generation = Boolean.TRUE.equals(created) ? candidate : redisTemplate.opsForValue().get(GENERATION_KEY);
            }
            return generation == null ? null : PAGE_PREFIX + generation + ':' + sort + ':' + size + ':' + scope;
        } catch (RuntimeException e) {
            log.warn("酒店列表缓存版本读取失败，回源数据库", e);
            return null;
        }
    }

    /** 读取缓存页；无缓存或 Redis 异常时返回 null。 */
    public PageResult<Hotel> get(String key) {
        if (key == null) return null;
        try {
            String json = redisTemplate.opsForValue().get(key);
            return json == null ? null : objectMapper.readValue(json, PAGE_TYPE);
        } catch (Exception e) {
            log.warn("酒店列表缓存读取失败，回源数据库，key={}", key, e);
            return null;
        }
    }

    /** 写入不含用户距离的首页结果；空结果使用更短的过期时间。 */
    public void put(String key, PageResult<Hotel> page) {
        if (key == null) return;
        try {
            long seconds = page.getData().isEmpty() ? 60 : 300 + ThreadLocalRandom.current().nextLong(60);
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(page), Duration.ofSeconds(seconds));
        } catch (Exception e) {
            log.warn("酒店列表缓存写入失败，key={}", key, e);
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
            redisTemplate.opsForValue().set(GENERATION_KEY, UUID.randomUUID().toString());
        } catch (RuntimeException e) {
            log.warn("酒店列表缓存失效失败，旧缓存最多保留约 6 分钟", e);
        }
    }
}
