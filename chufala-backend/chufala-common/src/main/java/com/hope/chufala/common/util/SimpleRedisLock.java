package com.hope.chufala.common.util;

import cn.hutool.core.lang.UUID;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * 基于 Redis 的简易分布式锁
 *
 * 用法：每个需要互斥的操作 new 一个实例；tryLock 成功后必须在 finally
 * 或事务完成回调中 unlock，否则只能等过期时间自动释放。
 */
public class SimpleRedisLock {
    private final StringRedisTemplate stringRedisTemplate;
    private final String name;

    public SimpleRedisLock(String name,StringRedisTemplate stringRedisTemplate){
        this.name = name;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    private static final String KEY_PREFIX = "lock:";
    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT;
    static{
        UNLOCK_SCRIPT = new DefaultRedisScript<>();
        UNLOCK_SCRIPT.setLocation(new ClassPathResource("unlock.lua"));
        UNLOCK_SCRIPT.setResultType(Long.class);
    }

    /**
     * 本次持有锁的唯一标识。
     *
     * 这里不使用 Thread.currentThread().getId() 做归属判断：线程 ID 会在线程销毁后
     * 被 JVM 复用，若旧锁尚未过期而新线程恰好拿到同一 ID，就会出现「新线程误删他人锁」。
     * 改为每次获取锁生成一个 UUID 写入 Redis，作为锁持有者的标识。
     */
    private String lockId;

    /**
     * 尝试获取锁（单次尝试，不等待）。
     *
     * @param timeoutSeconds 锁的过期时间（秒）。注意这不是等待时长——拿不到锁会立即
     *                       返回 false，由调用方决定重试还是失败。设置过期时间是为了
     *                       在持有者宕机时能自动释放，避免死锁。
     * @return true 表示获取成功
     */
    public boolean tryLock(long timeoutSeconds){
        String id = UUID.randomUUID().toString(true);
        Boolean success = stringRedisTemplate.opsForValue()
                .setIfAbsent(KEY_PREFIX + name, id, timeoutSeconds, TimeUnit.SECONDS);
        if (Boolean.TRUE.equals(success)) {
            this.lockId = id;
            return true;
        }
        return false;
    }

    /**
     * 释放锁。
     *
     * 通过 Lua 脚本保证「比对标识 + 删除」的原子性，避免误删在此期间
     * 已被其他线程重新获取的锁。未成功获取过锁时调用是安全的空操作。
     */
    public void unlock(){
        String id = this.lockId;
        if (id == null) {
            return;
        }
        stringRedisTemplate.execute(UNLOCK_SCRIPT, Collections.singletonList(KEY_PREFIX + name), id);
        this.lockId = null;
    }
}
