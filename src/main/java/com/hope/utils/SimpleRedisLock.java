package com.hope.utils;

import cn.hutool.core.lang.UUID;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class SimpleRedisLock {
    private final StringRedisTemplate stringRedisTemplate;
    private final String name;

    public SimpleRedisLock(String name,StringRedisTemplate stringRedisTemplate){
        this.name = name;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    private static final String KEY_PREFIX = "lock:";
    private static final String ID_PREFIX = UUID.randomUUID().toString(true)+"-";
    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT;
    static{
        UNLOCK_SCRIPT = new DefaultRedisScript<>();
        UNLOCK_SCRIPT.setLocation(new ClassPathResource("unlock.lua"));
        UNLOCK_SCRIPT.setResultType(Long.class);
    }


    public boolean tryLock(long timeout){
        String threadId = ID_PREFIX + Thread.currentThread().getId();
        Boolean success = stringRedisTemplate.opsForValue().setIfAbsent(KEY_PREFIX+name,threadId,timeout, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(success);
    }

    public void unlock(){
        //调用lua脚本，保证释放锁的原子性（判断标识和删除）
       stringRedisTemplate.execute(UNLOCK_SCRIPT,
               Collections.singletonList(KEY_PREFIX+name),
               ID_PREFIX+Thread.currentThread().getId());
    }

//    public void unlock(){
//        String threadId = ID_PREFIX +Thread.currentThread().getId();
//        String id = stringRedisTemplate.opsForValue().get(KEY_PREFIX+name);
//        if (threadId.equals(id)){
//            stringRedisTemplate.delete(KEY_PREFIX+name);
//        }
//    }
}
