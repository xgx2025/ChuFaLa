package com.hope.chufala.infra;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** Keeps chat output independently of the HTTP connection and replays it by SSE event ID. */
@Slf4j
@Component
public class ChatStreamManager {
    private static final Duration RETENTION = Duration.ofHours(2);
    private static final int BATCH_SIZE = 100;
    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;
    private final Map<String, Set<Connection>> connections = new ConcurrentHashMap<>();

    public ChatStreamManager(StringRedisTemplate redis, ObjectMapper objectMapper) {
        this.redis = redis;
        this.objectMapper = objectMapper;
    }

    public void create(String messageId, Long userId, String conversationId) {
        redis.opsForHash().putAll(metaKey(messageId), Map.of(
                "userId", userId.toString(), "conversationId", conversationId));
        redis.expire(metaKey(messageId), RETENTION);
        // Both keys receive their TTL before generation starts. Publishing each delta
        // must not block on extra Redis commands on the model's signal thread.
        publish(messageId, "ready", "");
        redis.expire(eventsKey(messageId), RETENTION);
    }

    public boolean belongsTo(String messageId, Long userId) {
        return userId.toString().equals(redis.opsForHash().get(metaKey(messageId), "userId"));
    }

    public void publish(String messageId, String type, String data) {
        try {
            String event = objectMapper.writeValueAsString(Map.of("type", type, "data", data));
            redis.opsForList().rightPush(eventsKey(messageId), event);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("无法保存聊天事件", e);
        }
    }

    public SseEmitter subscribe(String messageId, String lastEventId) {
        SseEmitter emitter = new SseEmitter(300_000L);
        subscribe(messageId, lastEventId, emitter);
        return emitter;
    }

    void subscribe(String messageId, String lastEventId, SseEmitter emitter) {
        long cursor = parseEventId(lastEventId);
        Long size = redis.opsForList().size(eventsKey(messageId));
        if (size != null && cursor > size) cursor = 0;
        Connection connection = new Connection(messageId, emitter, cursor);
        emitter.onCompletion(() -> remove(connection));
        emitter.onTimeout(() -> remove(connection));
        emitter.onError(error -> remove(connection));
        connections.computeIfAbsent(messageId, ignored -> ConcurrentHashMap.newKeySet()).add(connection);
        deliver(connection);
    }

    @Scheduled(fixedDelay = 100)
    public void deliverPendingEvents() {
        for (String messageId : connections.keySet()) deliverAll(messageId);
    }

    private void deliverAll(String messageId) {
        for (Connection connection : List.copyOf(connections.getOrDefault(messageId, Set.of()))) {
            deliver(connection);
        }
    }

    private void deliver(Connection connection) {
        synchronized (connection) {
            if (connection.closed) return;
            try {
                if (!Boolean.TRUE.equals(redis.hasKey(metaKey(connection.messageId)))) {
                    sendTerminal(connection, "failed", "聊天流已过期，请重新提问");
                    return;
                }
                while (!connection.closed) {
                    List<String> events = redis.opsForList().range(eventsKey(connection.messageId),
                            connection.cursor, connection.cursor + BATCH_SIZE - 1);
                    if (events == null || events.isEmpty()) return;
                    for (String raw : events) {
                        var event = objectMapper.readTree(raw);
                        String type = event.path("type").asText();
                        String data = event.path("data").asText();
                        long id = ++connection.cursor;
                        if (!send(connection, type, objectMapper.writeValueAsString(data), id)) return;
                        if ("complete".equals(type) || "failed".equals(type)) {
                            close(connection);
                            return;
                        }
                    }
                    if (events.size() < BATCH_SIZE) return;
                }
            } catch (Exception e) {
                log.warn("投递聊天事件失败，messageId={}", connection.messageId, e);
            }
        }
    }

    private boolean send(Connection connection, String type, String data, long id) {
        try {
            connection.emitter.send(SseEmitter.event().id(Long.toString(id)).name(type).data(data));
            return true;
        } catch (IOException | IllegalStateException e) {
            remove(connection);
            return false;
        }
    }

    private void sendTerminal(Connection connection, String type, String data) throws JsonProcessingException {
        if (send(connection, type, objectMapper.writeValueAsString(data), connection.cursor + 1)) close(connection);
    }

    private void close(Connection connection) {
        remove(connection);
        connection.emitter.complete();
    }

    private void remove(Connection connection) {
        connection.closed = true;
        Set<Connection> active = connections.get(connection.messageId);
        if (active != null) {
            active.remove(connection);
            if (active.isEmpty()) connections.remove(connection.messageId, active);
        }
    }

    private static long parseEventId(String value) {
        if (value == null || !value.matches("[0-9]{1,18}")) return 0;
        try { return Long.parseLong(value); }
        catch (NumberFormatException e) { return 0; }
    }

    private static String metaKey(String messageId) { return "chat:stream:" + messageId; }
    private static String eventsKey(String messageId) { return "chat:events:" + messageId; }

    private static final class Connection {
        private final String messageId;
        private final SseEmitter emitter;
        private long cursor;
        private boolean closed;

        private Connection(String messageId, SseEmitter emitter, long cursor) {
            this.messageId = messageId;
            this.emitter = emitter;
            this.cursor = cursor;
        }
    }
}
