package com.hope.chufala.infra.stream;

import com.hope.chufala.infra.task.RedisPlanTaskStore;

import com.hope.chufala.agent.TaskStatus;
import com.hope.chufala.model.vo.TravelItineraryVO;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.ArgumentCaptor;
import static org.mockito.Mockito.*;

class PlanProgressStreamManagerTest {
    private static final RedisPlanTaskStore.StageSnapshot SNAPSHOT = new RedisPlanTaskStore.StageSnapshot(
            3, "routes", List.of(new RedisPlanTaskStore.StageState(
            "routes", "RUNNING", "正在编排", "", 2, List.of())));

    @Test
    void reconnectReplaysOnlyEventsAfterLastEventIdAndDoesNotDuplicateThem() throws Exception {
        RedisPlanTaskStore queue = mock(RedisPlanTaskStore.class);
        when(queue.getReplayState("task-1")).thenReturn(
                new RedisPlanTaskStore.ReplayState(3, 1, TaskStatus.PROCESSING, SNAPSHOT, null));
        when(queue.getEventsAfter("task-1", 1, 100)).thenReturn(new RedisPlanTaskStore.EventBatch(
                1, 3, false, List.of(
                        new RedisPlanTaskStore.TaskEvent(2, "progress", "{\"message\":\"第一步\"}"),
                        new RedisPlanTaskStore.TaskEvent(3, "snapshot", "{\"seq\":3}"))));
        when(queue.getEventsAfter("task-1", 3, 100)).thenReturn(
                new RedisPlanTaskStore.EventBatch(1, 3, false, List.of()));
        PlanProgressStreamManager manager = new PlanProgressStreamManager(queue);
        SseEmitter emitter = mock(SseEmitter.class);

        manager.subscribe("task-1", emitter, "1");
        manager.deliverPendingEvents();

        ArgumentCaptor<SseEmitter.SseEventBuilder> events =
                ArgumentCaptor.forClass(SseEmitter.SseEventBuilder.class);
        verify(emitter, times(2)).send(events.capture());
        assertTrue(events.getAllValues().get(0).build().stream()
                .anyMatch(part -> part.getData().toString().contains("id:2")));
        assertTrue(events.getAllValues().get(1).build().stream()
                .anyMatch(part -> part.getData().toString().contains("id:3")));
        verify(queue).getEventsAfter("task-1", 1, 100);
        verify(queue).getEventsAfter("task-1", 3, 100);
    }

    @Test
    void firstSubscriptionReceivesCurrentSnapshot() throws Exception {
        RedisPlanTaskStore queue = mock(RedisPlanTaskStore.class);
        when(queue.getReplayState("task-1")).thenReturn(
                new RedisPlanTaskStore.ReplayState(3, 1, TaskStatus.PROCESSING, SNAPSHOT, null));
        when(queue.getEventsAfter("task-1", 3, 100)).thenReturn(
                new RedisPlanTaskStore.EventBatch(1, 3, false, List.of()));
        SseEmitter emitter = mock(SseEmitter.class);

        new PlanProgressStreamManager(queue).subscribe("task-1", emitter, null);

        verify(emitter).send(any(SseEmitter.SseEventBuilder.class));
        verify(queue).getEventsAfter("task-1", 3, 100);
    }

    @Test
    void expiredCursorFallsBackToCurrentSnapshot() throws Exception {
        RedisPlanTaskStore queue = mock(RedisPlanTaskStore.class);
        when(queue.getReplayState("task-1")).thenReturn(
                new RedisPlanTaskStore.ReplayState(600, 89, TaskStatus.PROCESSING, SNAPSHOT, null));
        when(queue.getEventsAfter("task-1", 600, 100)).thenReturn(
                new RedisPlanTaskStore.EventBatch(89, 600, false, List.of()));
        SseEmitter emitter = mock(SseEmitter.class);

        new PlanProgressStreamManager(queue).subscribe("task-1", emitter, "1");

        verify(emitter).send(any(SseEmitter.SseEventBuilder.class));
        verify(queue).getEventsAfter("task-1", 600, 100);
    }

    @Test
    void persistedCompletionIsDeliveredAfterReconnect() throws Exception {
        RedisPlanTaskStore queue = mock(RedisPlanTaskStore.class);
        TravelItineraryVO result = new TravelItineraryVO();
        when(queue.getReplayState("task-1")).thenReturn(
                new RedisPlanTaskStore.ReplayState(2, 1, TaskStatus.COMPLETED, SNAPSHOT, result));
        when(queue.getEventsAfter("task-1", 1, 100)).thenReturn(new RedisPlanTaskStore.EventBatch(
                1, 2, false, List.of(new RedisPlanTaskStore.TaskEvent(2, "complete", ""))));
        SseEmitter emitter = mock(SseEmitter.class);

        new PlanProgressStreamManager(queue).subscribe("task-1", emitter, "1");

        verify(emitter).send(any(SseEmitter.SseEventBuilder.class));
        verify(emitter).complete();
    }
}
