package com.hope.chufala.infra;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hope.chufala.common.model.vo.PageResult;
import com.hope.chufala.model.entity.Hotel;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/** 酒店列表热点页缓存。 */
@Component
public class HotelListCache extends CursorListCache<Hotel> {
    public HotelListCache(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        super("hotel", new TypeReference<PageResult<Hotel>>() { }, redisTemplate, objectMapper);
    }
}
