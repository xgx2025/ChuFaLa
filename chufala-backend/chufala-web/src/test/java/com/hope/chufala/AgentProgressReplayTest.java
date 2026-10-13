package com.hope.chufala;

import com.hope.chufala.agent.AgentTask;
import com.hope.chufala.agent.TaskStatus;
import com.hope.chufala.common.util.ThreadLocalUtils;
import com.hope.chufala.controller.AgentController;
import com.hope.chufala.infra.SseManager;
import com.hope.chufala.infra.TaskQueue;
import com.hope.chufala.model.vo.TravelItineraryVO;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class AgentProgressReplayTest {
    @Test
    void reconnectReplaysPersistedCompletion() {
        AgentController controller = new AgentController();
        TaskQueue queue = mock(TaskQueue.class);
        SseManager sse = mock(SseManager.class);
        TravelItineraryVO result = new TravelItineraryVO();
        AgentTask task = new AgentTask("task-1", 7L, null, TaskStatus.COMPLETED);
        task.setResult(result);
        when(queue.getTask("task-1")).thenReturn(task);
        ReflectionTestUtils.setField(controller, "taskQueue", queue);
        ReflectionTestUtils.setField(controller, "sseManager", sse);
        Claims claims = Jwts.claims();
        claims.put("userId", 7L);
        ThreadLocalUtils.set(claims);
        try {
            assertNotNull(controller.subscribeProgress("task-1"));
            verify(sse).registerEmitter(eq("task-1"), any(SseEmitter.class));
            verify(sse).completeTask("task-1", result);
        } finally {
            ThreadLocalUtils.remove();
        }
    }

    @Test
    void reconnectReplaysPersistedFailure() {
        AgentController controller = new AgentController();
        TaskQueue queue = mock(TaskQueue.class);
        SseManager sse = mock(SseManager.class);
        AgentTask task = new AgentTask("task-2", 7L, null, TaskStatus.FAILED);
        when(queue.getTask("task-2")).thenReturn(task);
        ReflectionTestUtils.setField(controller, "taskQueue", queue);
        ReflectionTestUtils.setField(controller, "sseManager", sse);
        Claims claims = Jwts.claims();
        claims.put("userId", 7L);
        ThreadLocalUtils.set(claims);
        try {
            assertNotNull(controller.subscribeProgress("task-2"));
            verify(sse).registerEmitter(eq("task-2"), any(SseEmitter.class));
            verify(sse).failTask(eq("task-2"), anyString());
        } finally {
            ThreadLocalUtils.remove();
        }
    }

    @Test
    void activeTaskReplaysItsLatestNodeProgress() {
        AgentController controller = new AgentController();
        TaskQueue queue = mock(TaskQueue.class);
        SseManager sse = mock(SseManager.class);
        AgentTask task = new AgentTask("task-3", 7L, null, TaskStatus.PROCESSING);
        when(queue.getTask("task-3")).thenReturn(task);
        ReflectionTestUtils.setField(controller, "taskQueue", queue);
        ReflectionTestUtils.setField(controller, "sseManager", sse);
        Claims claims = Jwts.claims();
        claims.put("userId", 7L);
        ThreadLocalUtils.set(claims);
        try {
            assertNotNull(controller.subscribeProgress("task-3"));
            verify(sse).replayProgress("task-3");
        } finally {
            ThreadLocalUtils.remove();
        }
    }
}
