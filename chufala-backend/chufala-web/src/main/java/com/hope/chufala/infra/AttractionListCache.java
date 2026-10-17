package com.hope.chufala.infra;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.hope.chufala.common.model.vo.PageResult;
import com.hope.chufala.model.entity.Attraction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/** 景点列表热点页缓存。 */
@Slf4j
@Component
public class AttractionListCache extends CursorListCache<Attraction> {
    public static final String INVALIDATION_CHANNEL = "attraction:list:cursor:invalidate";
    private static final TypeReference<PageResult<Attraction>> PAGE_TYPE = new TypeReference<>() { };
    private final Cache<FirstPageKey, String> firstPages = Caffeine.newBuilder()
            .maximumWeight(8 * 1024 * 1024)
            .weigher((FirstPageKey key, String value) ->
                    (int) Math.min(Integer.MAX_VALUE, 2L * value.length()))
            .expireAfterWrite(Duration.ofSeconds(5))
            .recordStats()
            .build();
    private final AtomicLong localGeneration = new AtomicLong();
    private final Object invalidationLock = new Object();
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public AttractionListCache(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        super("attraction", PAGE_TYPE, redisTemplate, objectMapper);
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    /** 首页先查本地缓存；只有未命中时才读取 Redis generation 和页面。 */
    public PageResult<Attraction> getFirstPageOrLoad(String scope, int size,
                                                      Supplier<PageResult<Attraction>> databaseLoader) {
        FirstPageKey localKey = new FirstPageKey(scope, size);
        long observedGeneration = localGeneration.get();
        String json = firstPages.getIfPresent(localKey);
        if (json != null) {
            try {
                return objectMapper.readValue(json, PAGE_TYPE);
            } catch (Exception e) {
                firstPages.invalidate(localKey);
                log.warn("景点首页本地缓存解析失败，回退 Redis，scope={}", scope, e);
            }
        }

        String redisKey = pageKey("rating", scope, size, null);
        PageResult<Attraction> page = getOrLoad(redisKey, databaseLoader);
        if (redisKey != null) {
            try {
                String snapshot = objectMapper.writeValueAsString(page);
                synchronized (invalidationLock) {
                    if (localGeneration.get() == observedGeneration) {
                        firstPages.put(localKey, snapshot);
                    }
                }
            } catch (Exception e) {
                log.warn("景点首页本地缓存写入失败，scope={}", scope, e);
            }
        }
        return page;
    }

    /** Redis 通知和本实例提交后均清理 L1。 */
    public void invalidateLocal() {
        synchronized (invalidationLock) {
            localGeneration.incrementAndGet();
            firstPages.invalidateAll();
        }
    }

    @Override
    protected boolean rotateGeneration() {
        boolean rotated = super.rotateGeneration();
        invalidateLocal();
        if (rotated) {
            try {
                redisTemplate.convertAndSend(INVALIDATION_CHANNEL, "invalidate");
            } catch (RuntimeException e) {
                log.warn("景点首页本地缓存失效通知失败，等待短 TTL 过期", e);
            }
        }
        return rotated;
    }

    private record FirstPageKey(String scope, int size) { }
}
