package com.hope.chufala;

import com.hope.chufala.agent.AgentTask;
import com.hope.chufala.agent.TaskStatus;
import com.hope.chufala.common.util.ThreadLocalUtils;
import com.hope.chufala.common.exception.ResourceNotFoundException;
import com.hope.chufala.controller.AgentController;
import com.hope.chufala.infra.stream.PlanProgressStreamManager;
import com.hope.chufala.infra.task.RedisPlanTaskStore;
import com.hope.chufala.model.vo.TravelItineraryVO;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class AgentProgressReplayTest {
    @Test
    void completedTaskSubscriptionPassesCursorToSseManager() {
        AgentController controller = new AgentController();
        RedisPlanTaskStore queue = mock(RedisPlanTaskStore.class);
        PlanProgressStreamManager sse = mock(PlanProgressStreamManager.class);
        TravelItineraryVO result = new TravelItineraryVO();
        AgentTask task = new AgentTask("task-1", 7L, null, TaskStatus.COMPLETED);
        task.setResult(result);
        when(queue.getTask("task-1")).thenReturn(task);
        ReflectionTestUtils.setField(controller, "planTaskStore", queue);
        ReflectionTestUtils.setField(controller, "planProgressStreamManager", sse);
        Claims claims = Jwts.claims();
        claims.put("userId", 7L);
        ThreadLocalUtils.set(claims);
        try {
            assertNotNull(controller.subscribeProgress("task-1", "12"));
            verify(sse).subscribe(eq("task-1"), any(SseEmitter.class), eq("12"));
        } finally {
            ThreadLocalUtils.remove();
        }
    }

    @Test
    void failedTaskSubscriptionIsAllowedForOwner() {
        AgentController controller = new AgentController();
        RedisPlanTaskStore queue = mock(RedisPlanTaskStore.class);
        PlanProgressStreamManager sse = mock(PlanProgressStreamManager.class);
        AgentTask task = new AgentTask("task-2", 7L, null, TaskStatus.FAILED);
        when(queue.getTask("task-2")).thenReturn(task);
        ReflectionTestUtils.setField(controller, "planTaskStore", queue);
        ReflectionTestUtils.setField(controller, "planProgressStreamManager", sse);
        Claims claims = Jwts.claims();
        claims.put("userId", 7L);
        ThreadLocalUtils.set(claims);
        try {
            assertNotNull(controller.subscribeProgress("task-2", null));
            verify(sse).subscribe(eq("task-2"), any(SseEmitter.class), isNull());
        } finally {
            ThreadLocalUtils.remove();
        }
    }

    @Test
    void activeTaskSubscriptionWithoutCursorStartsWithSnapshot() {
        AgentController controller = new AgentController();
        RedisPlanTaskStore queue = mock(RedisPlanTaskStore.class);
        PlanProgressStreamManager sse = mock(PlanProgressStreamManager.class);
        AgentTask task = new AgentTask("task-3", 7L, null, TaskStatus.PROCESSING);
        when(queue.getTask("task-3")).thenReturn(task);
        ReflectionTestUtils.setField(controller, "planTaskStore", queue);
        ReflectionTestUtils.setField(controller, "planProgressStreamManager", sse);
        Claims claims = Jwts.claims();
        claims.put("userId", 7L);
        ThreadLocalUtils.set(claims);
        try {
            assertNotNull(controller.subscribeProgress("task-3", null));
            verify(sse).subscribe(eq("task-3"), any(SseEmitter.class), isNull());
        } finally {
            ThreadLocalUtils.remove();
        }
    }

    @Test
    void anotherUsersCursorCannotReadTaskEvents() {
        AgentController controller = new AgentController();
        RedisPlanTaskStore queue = mock(RedisPlanTaskStore.class);
        PlanProgressStreamManager sse = mock(PlanProgressStreamManager.class);
        when(queue.getTask("task-4")).thenReturn(
                new AgentTask("task-4", 8L, null, TaskStatus.PROCESSING));
        ReflectionTestUtils.setField(controller, "planTaskStore", queue);
        ReflectionTestUtils.setField(controller, "planProgressStreamManager", sse);
        Claims claims = Jwts.claims();
        claims.put("userId", 7L);
        ThreadLocalUtils.set(claims);
        try {
            assertThrows(ResourceNotFoundException.class,
                    () -> controller.subscribeProgress("task-4", "3"));
            verifyNoInteractions(sse);
        } finally {
            ThreadLocalUtils.remove();
        }
    }
}
