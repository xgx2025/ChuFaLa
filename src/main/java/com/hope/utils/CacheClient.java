package com.hope.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.hope.domain.entity.RedisData;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Component
public class CacheClient {
    private final StringRedisTemplate stringRedisTemplate;
    private static final Long CACHE_NLL_TLL = 5L;


    public CacheClient(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public void set(String key, Object value, Long time, TimeUnit unit) {
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(value), time, unit);
    }

    public void setWithLogicalExpire(String key, Object value, Long time, TimeUnit unit) {
        RedisData redisData = new RedisData();
        redisData.setData(value);
        redisData.setExpireTime(LocalDate.now().plus(time, unit.toChronoUnit()));
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

    private static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10);


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
        LocalDate expireTime = redisData.getExpireTime();
        if(expireTime.isAfter(LocalDate.now())){
            return r;
        }
        //重建缓存
        String lockKey = "lock:" + key;
        String clientId = UUID.randomUUID().toString();
        boolean isLock = tryLock(lockKey);
        if(isLock){
            CACHE_REBUILD_EXECUTOR.submit(()->{
                try {
                    R r1 = dbFallback.apply(id);
                    this.setWithLogicalExpire(key, r1, time, unit);
                }catch (Exception e){
                    throw new RuntimeException(e);
                }finally {
                    unlock(lockKey);
                }
            });
        }
        return r;
    }


    /**
     * 尝试获取分布式锁
     * @param key 锁的键名，用于标识唯一的锁
     * @return 获取锁成功返回true，否则返回false
     */
    private boolean tryLock(String key){
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1",10L,TimeUnit.SECONDS);
        return BooleanUtil.isTrue(flag);
    }

    /**
     * 解锁操作，通过删除Redis中的指定键来实现解锁
     * @param key 要删除的Redis键值，用于标识需要解锁的资源
     */
    private void unlock(String key){
        stringRedisTemplate.delete(key);
    }


}
