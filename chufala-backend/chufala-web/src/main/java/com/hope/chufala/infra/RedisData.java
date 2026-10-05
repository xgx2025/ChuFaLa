package com.hope.chufala.infra;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 逻辑过期缓存的包装体：把数据与「逻辑过期时间」一起存入 Redis，
 * 由 CacheClient 判断是否触发异步重建。
 *
 * @author 谢光湘
 */
@Data
@NoArgsConstructor
public class RedisData {
    /** 逻辑过期时间 */
    private LocalDate expireTime;
    /** 实际缓存数据 */
    private Object data;

}
