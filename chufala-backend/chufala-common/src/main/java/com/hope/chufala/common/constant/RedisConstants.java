package com.hope.chufala.common.constant;

/**
 * Redis 键常量。
 *
 * <p>注意：本类的缓存前缀字段均为 private 且类内未使用，实际生效的酒店列表
 * 缓存 Key 由 HotelServiceImpl 自行拼接（前缀不含 {@code page:} 片段），
 * 两者并不一致，修改缓存逻辑时以 HotelServiceImpl 为准。
 *
 * @author 谢光湘
 */
public class RedisConstants {
    /** 店铺锁键前缀 */
    public static final String LOCK_SHOP_KEY = "lock:shop:";


    private static final String CACHE_PREFIX_ALL = "hotel:list:all:page:";
    private static final String CACHE_PREFIX_RANK = "hotel:list:rank:desc:page:";
    private static final String CACHE_PREFIX_CURSOR = "hotel:list:rank:desc:cursor:";
    private static final Integer CACHE_TTL = 3600; // 缓存过期时间（秒）

}
