package com.hope.chufala.infra.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hope.chufala.common.model.vo.PageResult;
import com.hope.chufala.common.exception.biz.BizException;
import com.hope.chufala.infra.cache.SimpleRedisLock;
import com.hope.chufala.util.CursorPaginationUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/** 列表前 3 页共用的 Redis cache-aside 实现；缓存值不含用户距离。 */
@Slf4j
public abstract class CursorListCache<T> {
    private static final int MAX_CONCURRENT_DATABASE_LOADS = 24;
    private static final int CACHE_WAIT_ATTEMPTS = 20;
    private static final long CACHE_WAIT_MILLIS = 50;
    private final Semaphore databaseLoadPermits = new Semaphore(MAX_CONCURRENT_DATABASE_LOADS);
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

    /** 只为首页及游标标明的第 2～3 页生成键；深页直接回源。 */
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

    /** 同一缓存页只允许一个实例回源；等待者读取重建结果，Redis 故障时限制数据库并发。 */
    public PageResult<T> getOrLoad(String key, Supplier<PageResult<T>> databaseLoader) {
        if (key == null) return loadFromDatabase(databaseLoader);
        PageResult<T> cached = get(key);
        if (cached != null) return cached;

        SimpleRedisLock lock = new SimpleRedisLock("cache:page:" + key, redisTemplate);
        boolean acquired;
        try {
            acquired = lock.tryLock(15);
        } catch (DataAccessException e) {
            log.warn("Redis 不可用，限制并发后回源数据库，key={}", key, e);
            return loadFromDatabase(databaseLoader);
        }
        if (acquired) return loadWithLock(key, databaseLoader, lock);

        try {
            for (int attempt = 0; attempt < CACHE_WAIT_ATTEMPTS; attempt++) {
                TimeUnit.MILLISECONDS.sleep(CACHE_WAIT_MILLIS);
                cached = get(key);
                if (cached != null) return cached;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BizException("列表数据正在加载，请稍后重试");
        }
        try {
            acquired = lock.tryLock(15);
        } catch (DataAccessException e) {
            log.warn("Redis 不可用，限制并发后回源数据库，key={}", key, e);
            return loadFromDatabase(databaseLoader);
        }
        if (acquired) return loadWithLock(key, databaseLoader, lock);
        throw new BizException("列表数据正在加载，请稍后重试");
    }

    private PageResult<T> loadWithLock(String key, Supplier<PageResult<T>> databaseLoader,
                                       SimpleRedisLock lock) {
        try {
            PageResult<T> cached = get(key);
            if (cached != null) return cached;
            PageResult<T> page = loadFromDatabase(databaseLoader);
            put(key, page);
            return page;
        } finally {
            try {
                lock.unlock();
            } catch (RuntimeException e) {
                log.warn("列表缓存锁释放失败，等待锁自动过期，key={}", key, e);
            }
        }
    }

    private PageResult<T> loadFromDatabase(Supplier<PageResult<T>> databaseLoader) {
        if (!databaseLoadPermits.tryAcquire()) {
            throw new BizException("列表查询繁忙，请稍后重试");
        }
        try {
            return databaseLoader.get();
        } finally {
            databaseLoadPermits.release();
        }
    }

    /** 写入不含用户距离的分页结果；后续页与空页采用更短 TTL。 */
    public void put(String key, PageResult<T> page) {
        if (key == null) return;
        try {
            long seconds = page.getData().isEmpty() ? 30 + ThreadLocalRandom.current().nextLong(15)
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

    protected boolean rotateGeneration() {
        try {
            redisTemplate.opsForValue().set(generationKey, UUID.randomUUID().toString());
            return true;
        } catch (RuntimeException e) {
            log.warn("列表缓存失效失败，旧缓存最多保留约 4 分钟，list={}", pagePrefix, e);
            return false;
        }
    }
}
