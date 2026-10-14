package com.hope.chufala.infra;

import com.hope.chufala.agent.TaskStatus;
import com.hope.chufala.model.vo.TravelItineraryVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** 将 Redis 中的规划事件按 ID 投递给当前实例的 SSE 连接。 */
@Slf4j
@Component
public class SseManager {
    private static final int REPLAY_BATCH_SIZE = 100;

    private final Map<String, Set<Connection>> connections = new ConcurrentHashMap<>();
    private final TaskQueue taskQueue;

    public SseManager(TaskQueue taskQueue) {
        this.taskQueue = taskQueue;
    }

    /** 初次订阅发送当前快照；带有效游标的订阅补发该 ID 之后的所有事件。 */
    public void subscribe(String taskId, SseEmitter emitter, String lastEventId) {
        Connection connection = new Connection(taskId, emitter);
        emitter.onCompletion(() -> remove(connection));
        emitter.onTimeout(() -> remove(connection));
        emitter.onError(error -> remove(connection));

        TaskQueue.ReplayState state = taskQueue.getReplayState(taskId);
        if (state == null) {
            sendTerminal(connection, "failed", "行程规划失败或任务已过期", 0);
            return;
        }

        Long requestedId = parseEventId(lastEventId);
        boolean canReplay = requestedId != null && requestedId >= state.firstId() - 1
                && requestedId <= state.lastId();
        if (canReplay) {
            connection.cursor = requestedId;
        } else {
            connection.cursor = state.lastId();
            if (state.status() == TaskStatus.COMPLETED) {
                sendTerminal(connection, "complete", state.result(), state.lastId());
                return;
            }
            if (state.status() == TaskStatus.FAILED) {
                sendTerminal(connection, "failed", state.failureMessage(), state.lastId());
                return;
            }
            if (!send(connection, "snapshot", state.snapshot(), state.lastId())) return;
        }

        connections.computeIfAbsent(taskId, ignored -> ConcurrentHashMap.newKeySet()).add(connection);
        deliver(connection);
        // 已收到终态 ID 的客户端通常已经结束；若它再次订阅，仍明确返回终态。
        if (!connection.closed && canReplay && state.status() != TaskStatus.PROCESSING
                && state.status() != TaskStatus.PENDING) {
            TaskQueue.ReplayState latest = taskQueue.getReplayState(taskId);
            if (latest != null && connection.cursor == latest.lastId()) {
                if (latest.status() == TaskStatus.COMPLETED) {
                    sendTerminal(connection, "complete", latest.result(), latest.lastId());
                } else if (latest.status() == TaskStatus.FAILED) {
                    sendTerminal(connection, "failed", latest.failureMessage(), latest.lastId());
                }
            }
        }
    }

    public void sendProgress(String taskId, String node, String message) {
        try {
            if (taskQueue.updateProgress(taskId, node, message, System.currentTimeMillis())) {
                deliverAll(taskId);
            }
        } catch (RuntimeException e) {
            log.warn("保存规划节点进度失败，taskId={}", taskId, e);
        }
    }

    public void stageStarted(String taskId, String stageId, String message) {
        publishStage(taskId, stageId, "RUNNING", message, "");
    }

    public void stageUpdated(String taskId, String stageId, String message, String summary) {
        publishStage(taskId, stageId, "RUNNING", message, summary);
    }

    public void weatherUpdated(String taskId, String message, String summary,
                               List<TaskQueue.WeatherPreview> days) {
        try {
            if (taskQueue.updateWeatherStage(taskId, message, summary, days) != 0) {
                deliverAll(taskId);
            }
        } catch (RuntimeException e) {
            log.warn("保存逐日天气预览失败，taskId={}", taskId, e);
        }
    }

    public void stageCompleted(String taskId, String stageId, String message, String summary) {
        publishStage(taskId, stageId, "COMPLETED", message, summary);
    }

    private void publishStage(String taskId, String stageId, String state, String message, String summary) {
        try {
            if (taskQueue.updateStage(taskId, stageId, state, message, summary) != 0) {
                deliverAll(taskId);
            }
        } catch (RuntimeException e) {
            log.warn("保存规划阶段失败，taskId={}, stageId={}", taskId, stageId, e);
        }
    }

    /** 任务终态由 TaskQueue 先写入 Redis，这里只触发立即投递。 */
    public void completeTask(String taskId, TravelItineraryVO result) {
        deliverAll(taskId);
    }

    public void failTask(String taskId, String message) {
        deliverAll(taskId);
        TaskQueue.ReplayState state;
        try {
            state = taskQueue.getReplayState(taskId);
        } catch (RuntimeException e) {
            log.warn("查询规划失败状态出错，taskId={}", taskId, e);
            state = null;
        }
        if (state == null || (state.status() != TaskStatus.FAILED
                && state.status() != TaskStatus.COMPLETED)) {
            for (Connection connection : List.copyOf(connections.getOrDefault(taskId, Set.of()))) {
                sendTerminal(connection, "failed", message, connection.cursor);
            }
        }
    }

    /** 跨实例执行的任务由定时读取 Redis 事件日志补推。 */
    @Scheduled(fixedDelay = 1_000)
    public void deliverPendingEvents() {
        for (String taskId : connections.keySet()) {
            deliverAll(taskId);
        }
    }

    /** 保留原有的执行进程失联检测，使已连接的页面也能收到失败终态。 */
    @Scheduled(fixedDelay = 5_000)
    public void detectAbandonedTasks() {
        for (String taskId : connections.keySet()) {
            try {
                taskQueue.getTask(taskId);
                deliverAll(taskId);
            } catch (RuntimeException e) {
                log.warn("检查规划任务执行状态失败，taskId={}", taskId, e);
            }
        }
    }

    private void deliverAll(String taskId) {
        for (Connection connection : List.copyOf(connections.getOrDefault(taskId, Set.of()))) {
            deliver(connection);
        }
    }

    private void deliver(Connection connection) {
        synchronized (connection) {
            if (connection.closed) return;
            try {
                while (!connection.closed) {
                    TaskQueue.EventBatch batch = taskQueue.getEventsAfter(
                            connection.taskId, connection.cursor, REPLAY_BATCH_SIZE);
                    if (batch == null) {
                        sendTerminal(connection, "failed", "行程规划失败或任务已过期", connection.cursor);
                        return;
                    }
                    if (batch.historyExpired()) {
                        TaskQueue.ReplayState state = taskQueue.getReplayState(connection.taskId);
                        if (state == null) {
                            sendTerminal(connection, "failed", "行程规划失败或任务已过期", connection.cursor);
                        } else if (state.status() == TaskStatus.COMPLETED) {
                            sendTerminal(connection, "complete", state.result(), state.lastId());
                        } else if (state.status() == TaskStatus.FAILED) {
                            sendTerminal(connection, "failed", state.failureMessage(), state.lastId());
                        } else if (send(connection, "snapshot", state.snapshot(), state.lastId())) {
                            connection.cursor = state.lastId();
                        }
                        return;
                    }
                    if (batch.events().isEmpty()) return;
                    for (TaskQueue.TaskEvent event : batch.events()) {
                        Object payload = event.data();
                        if ("complete".equals(event.type())) {
                            TaskQueue.ReplayState state = taskQueue.getReplayState(connection.taskId);
                            payload = state == null ? null : state.result();
                        }
                        if (!send(connection, event.type(), payload, event.id())) return;
                        connection.cursor = event.id();
                        if ("complete".equals(event.type()) || "failed".equals(event.type())) {
                            close(connection);
                            return;
                        }
                    }
                    if (batch.events().size() < REPLAY_BATCH_SIZE) return;
                }
            } catch (RuntimeException e) {
                log.warn("补推规划进度失败，taskId={}", connection.taskId, e);
                // Redis 暂时不可用时保留连接，下一轮再从已投递 ID 继续。
            }
        }
    }

    private boolean send(Connection connection, String type, Object payload, long id) {
        try {
            connection.emitter.send(SseEmitter.event().id(Long.toString(id)).name(type).data(payload));
            return true;
        } catch (IOException | IllegalStateException e) {
            remove(connection);
            return false;
        }
    }

    private void sendTerminal(Connection connection, String type, Object payload, long id) {
        synchronized (connection) {
            if (!connection.closed && send(connection, type, payload, id)) close(connection);
        }
    }

    private void close(Connection connection) {
        remove(connection);
        connection.emitter.complete();
    }

    private void remove(Connection connection) {
        connection.closed = true;
        Set<Connection> taskConnections = connections.get(connection.taskId);
        if (taskConnections != null) {
            taskConnections.remove(connection);
            if (taskConnections.isEmpty()) connections.remove(connection.taskId, taskConnections);
        }
    }

    private static Long parseEventId(String value) {
        if (value == null || !value.matches("[0-9]{1,18}")) return null;
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static final class Connection {
        private final String taskId;
        private final SseEmitter emitter;
        private volatile long cursor;
        private volatile boolean closed;

        private Connection(String taskId, SseEmitter emitter) {
            this.taskId = taskId;
            this.emitter = emitter;
        }
    }
}
