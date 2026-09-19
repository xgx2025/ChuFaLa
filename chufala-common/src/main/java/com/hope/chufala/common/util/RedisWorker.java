package com.hope.chufala.common.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * Redis分布式自增ID生成器
 * @author xgx
 * @date 2023/9/23 16:07
 * 1. 生成的ID是64位整数，前32位是时间戳，后32位是自增序列
 * 2. 时间戳部分从2023-12-31开始，单位是秒
 * 3. 每个时间戳内最多生成2^32个ID （约42亿）
 */
@Component
public class RedisWorker {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final long BEGIN_TIMESTAMP = 1735689600; // 2023-12-31 00:00:00 UTC的秒级时间戳
    private static final int COUNT_BITS = 32;

    public long nextId(String keyPrefix){
        LocalDateTime now = LocalDateTime.now();
        long nowSecond = now.toEpochSecond(ZoneOffset.UTC);
        long timestamp = nowSecond - BEGIN_TIMESTAMP;

        String date = now.format(DateTimeFormatter.ofPattern("yyyy:MM:dd"));
        long count = stringRedisTemplate.opsForValue().increment("icr:" + keyPrefix + ":" + date);

        return timestamp << COUNT_BITS | count;
    }
}
