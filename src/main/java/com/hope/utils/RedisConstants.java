package com.hope.utils;

public class RedisConstants {
    public static final String LOCK_SHOP_KEY = "lock:shop:";


    private static final String CACHE_PREFIX_ALL = "hotel:list:all:page:";
    private static final String CACHE_PREFIX_RANK = "hotel:list:rank:desc:page:";
    private static final String CACHE_PREFIX_CURSOR = "hotel:list:rank:desc:cursor:";
    private static final Integer CACHE_TTL = 3600; // 缓存过期时间（秒）

}
