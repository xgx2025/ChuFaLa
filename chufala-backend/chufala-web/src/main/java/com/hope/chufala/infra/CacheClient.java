package com.hope.chufala.infra;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.hope.chufala.common.util.SimpleRedisLock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Redis 缓存工具。
 *
 * <p>封装三种缓存策略：普通写入、逻辑过期写入、以及对应的两种读模式——
 * {@link #queryWithPassThrough} 用空值缓存防穿透，
 * {@link #queryWithLogicalExpire} 用互斥锁 + 异步重建防击穿。
 *
 * @author 谢光湘
 */
@Component
@Slf4j
public class CacheClient {
    private final StringRedisTemplate stringRedisTemplate;


    public CacheClient(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * 写入缓存（JSON 序列化，带物理过期时间）。
     *
     * @param key   缓存键
     * @param value 缓存值
     * @param time  过期时间
     * @param unit  时间单位
     */
    public void set(String key, Object value, Long time, TimeUnit unit) {
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(value), time, unit);
    }

    /**
     * 以「逻辑过期」方式写入缓存（不设物理过期，过期时间存进值里）。
     *
     * @param key   缓存键
     * @param value 缓存值
     * @param time  逻辑过期时长
     * @param unit  时间单位
     */
    public void setWithLogicalExpire(String key, Object value, Long time, TimeUnit unit) {
        RedisData redisData = new RedisData();
        redisData.setData(value);
        redisData.setExpireTime(LocalDateTime.now().plus(time, unit.toChronoUnit()));
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(redisData));
    }

    /**
     * 利用缓存中存空值解决缓存穿透问题
     * 带缓存穿透处理的查询方法
     *
     * @param keyPrefix  缓存键的前缀
     * @param id         查询参数ID
     * @param type       返回值类型Class对象
     * @param dbFallback 数据库查询回调函数
     * @param time       缓存过期时间
     * @param unit       缓存过期时间单位
     * @return 查询结果，如果缓存和数据库都不存在则返回null
     */
    public <T, R> R queryWithPassThrough(String keyPrefix, T id, Class<R> type, Function<T, R> dbFallback, Long time, TimeUnit unit) {
        String key = keyPrefix + id;
        String json = stringRedisTemplate.opsForValue().get(key);
        if (StrUtil.isNotBlank(json)) {
            return JSONUtil.toBean(json, type);
        }
        if (json != null) {
            return null;
        }
        //缓存中为null
        R r = dbFallback.apply(id);
        if (r == null) {
            // 处理缓存穿透，缓存设置空值
            stringRedisTemplate.opsForValue().set(key, "", 2L, TimeUnit.MINUTES);
            return null;
        }
        this.set(key, r, time, unit);
        return r;
    }

    private static final ThreadPoolExecutor CACHE_REBUILD_EXECUTOR = new ThreadPoolExecutor(
            2, 10, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<>(100), task -> {
                Thread thread = new Thread(task, "cache-rebuild");
                thread.setDaemon(true);
                return thread;
            });


    /**
     * 利用逻辑过期解决缓存击穿问题
     * 使用逻辑过期策略查询缓存数据（缓存中须提前加载好数据）
     * <p>该方法首先从Redis缓存中查询数据，如果缓存不存在或者为空则返回null。
     * 如果缓存存在但已过期，则触发异步重建缓存操作。</p>
     *
     * @param keyPrefix 缓存键前缀，用于构建完整的Redis键
     * @param id 查询参数ID，与键前缀组合成完整键名
     * @param type 返回值类型Class对象，用于反序列化
     * @param dbFallback 数据库回源函数，当缓存过期时用于从数据库加载数据
     * @param time 逻辑过期时间
     * @param unit 逻辑过期时间单位
     * @param <T> 查询参数类型
     * @param <R> 返回值类型
     * @return 缓存中的数据对象，如果缓存不存在或者为空则返回null
     */
    public <T,R> R queryWithLogicalExpire(String keyPrefix, T id,Class<R> type,Function<T,R> dbFallback,Long time, TimeUnit unit) {
        String key = keyPrefix + id;
        String json =stringRedisTemplate.opsForValue().get(key);
        if(StrUtil.isBlank(json)){
            return null;
        }
        RedisData redisData = JSONUtil.toBean(json, RedisData.class);
        R r = BeanUtil.toBean(redisData.getData(),type);
        LocalDateTime expireTime = redisData.getExpireTime();
        if(expireTime.isAfter(LocalDateTime.now())){
            return r;
        }
        //重建缓存
        SimpleRedisLock lock = new SimpleRedisLock("cache:logical:" + key, stringRedisTemplate);
        boolean isLock = lock.tryLock(30);
        if(isLock){
            try {
                CACHE_REBUILD_EXECUTOR.submit(()->{
                    try {
                        R r1 = dbFallback.apply(id);
                        this.setWithLogicalExpire(key, r1, time, unit);
                    } catch (Exception e) {
                        log.warn("逻辑过期缓存重建失败，key={}", key, e);
                    } finally {
                        try {
                            lock.unlock();
                        } catch (RuntimeException e) {
                            log.warn("逻辑过期缓存锁释放失败，key={}", key, e);
                        }
                    }
                });
            } catch (java.util.concurrent.RejectedExecutionException e) {
                try {
                    lock.unlock();
                } catch (RuntimeException unlockError) {
                    log.warn("逻辑过期缓存锁释放失败，key={}", key, unlockError);
                }
                log.warn("逻辑过期缓存重建队列已满，key={}", key, e);
            }
        }
        return r;
    }


}
