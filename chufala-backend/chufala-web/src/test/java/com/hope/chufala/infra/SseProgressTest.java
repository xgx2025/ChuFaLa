package com.hope.chufala.infra;

import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class SseProgressTest {
    @Test
    void progressBeforeSubscriptionIsSavedAndReplayed() throws Exception {
        TaskQueue queue = mock(TaskQueue.class);
        when(queue.updateProgress(eq("task-1"), eq("PlanItineraryNode"), any(), anyLong()))
                .thenReturn(true);
        SseManager manager = new SseManager(queue);

        manager.sendProgress("task-1", "PlanItineraryNode", "正在规划行程");
        verify(queue).updateProgress(eq("task-1"), eq("PlanItineraryNode"),
                eq("正在规划行程"), anyLong());

        SseEmitter emitter = mock(SseEmitter.class);
        manager.registerEmitter("task-1", emitter);
        when(queue.getProgress("task-1")).thenReturn(
                new TaskQueue.TaskProgress("PlanItineraryNode", "正在规划行程", 123L));
        manager.replayProgress("task-1");

        verify(emitter).send(any(SseEmitter.SseEventBuilder.class));
    }
}
