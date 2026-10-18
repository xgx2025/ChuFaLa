package com.hope.chufala.infra.stream;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class ChatStreamManagerTest {
    @Test
    void publishDoesNotRefreshRedisTtlForEveryChunk() throws Exception {
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        @SuppressWarnings("unchecked")
        ListOperations<String, String> events = mock(ListOperations.class);
        when(redis.opsForList()).thenReturn(events);
        ChatStreamManager manager = new ChatStreamManager(redis, new ObjectMapper());

        manager.publish("42", "delta", "hello");

        var event = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(events).rightPush(eq("chat:events:42"), event.capture());
        var json = new ObjectMapper().readTree(event.getValue());
        assertTrue("delta".equals(json.path("type").asText()));
        assertTrue("hello".equals(json.path("data").asText()));
        verify(redis, never()).expire(anyString(), any(Duration.class));
    }

    @Test
    void createPersistsReadyEventAndSetsTtlBeforeGeneration() throws Exception {
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        @SuppressWarnings("unchecked")
        ListOperations<String, String> events = mock(ListOperations.class);
        @SuppressWarnings("unchecked")
        HashOperations<String, Object, Object> metadata = mock(HashOperations.class);
        when(redis.opsForList()).thenReturn(events);
        when(redis.opsForHash()).thenReturn(metadata);
        ChatStreamManager manager = new ChatStreamManager(redis, new ObjectMapper());

        manager.create("42", 7L, "11");

        verify(redis).expire("chat:stream:42", Duration.ofHours(2));
        verify(redis).expire("chat:events:42", Duration.ofHours(2));
        var event = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(events).rightPush(eq("chat:events:42"), event.capture());
        assertTrue("ready".equals(new ObjectMapper().readTree(event.getValue()).path("type").asText()));
    }

    @Test
    void reconnectReplaysOnlyMissingEventsAndClosesAfterCompletion() throws Exception {
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        @SuppressWarnings("unchecked")
        ListOperations<String, String> events = mock(ListOperations.class);
        when(redis.opsForList()).thenReturn(events);
        when(redis.hasKey("chat:stream:42")).thenReturn(true);
        when(events.size("chat:events:42")).thenReturn(3L);
        when(events.range("chat:events:42", 1, 100)).thenReturn(List.of(
                "{\"type\":\"delta\",\"data\":\"世界\"}",
                "{\"type\":\"complete\",\"data\":\"\"}"));
        SseEmitter emitter = mock(SseEmitter.class);
        ChatStreamManager manager = new ChatStreamManager(redis, new ObjectMapper());

        manager.subscribe("42", "1", emitter);
        manager.deliverPendingEvents();

        var sent = org.mockito.ArgumentCaptor.forClass(SseEmitter.SseEventBuilder.class);
        verify(emitter, times(2)).send(sent.capture());
        assertTrue(sent.getAllValues().get(0).build().stream()
                .anyMatch(part -> part.getData().toString().contains("id:2")));
        assertTrue(sent.getAllValues().get(1).build().stream()
                .anyMatch(part -> part.getData().toString().contains("id:3")));
        verify(emitter).complete();
        verify(events).range("chat:events:42", 1, 100);
    }
}
