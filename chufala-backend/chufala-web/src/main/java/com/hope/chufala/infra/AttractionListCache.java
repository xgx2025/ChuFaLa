package com.hope.chufala.infra;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hope.chufala.common.model.vo.PageResult;
import com.hope.chufala.model.entity.Attraction;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/** 景点列表热点页缓存。 */
@Component
public class AttractionListCache extends CursorListCache<Attraction> {
    public AttractionListCache(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        super("attraction", new TypeReference<PageResult<Attraction>>() { }, redisTemplate, objectMapper);
    }
}
