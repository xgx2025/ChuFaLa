package com.hope.chufala.config;

import com.hope.chufala.infra.cache.AttractionListCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

/** 景点首页 L1 的跨实例失效通知。 */
@Configuration
public class AttractionCacheConfig {
    @Bean
    public RedisMessageListenerContainer attractionCacheInvalidationListener(
            RedisConnectionFactory connectionFactory, AttractionListCache cache) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener((message, pattern) -> cache.invalidateLocal(),
                new ChannelTopic(AttractionListCache.INVALIDATION_CHANNEL));
        return container;
    }
}
