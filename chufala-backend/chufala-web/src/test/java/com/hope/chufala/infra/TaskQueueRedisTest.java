package com.hope.chufala.infra;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hope.chufala.agent.AgentTask;
import com.hope.chufala.agent.TaskStatus;
import com.hope.chufala.model.dto.UserPlanDTO;
import com.hope.chufala.model.vo.TravelItineraryVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TaskQueueRedisTest {
    private final StringRedisTemplate redis = mock(StringRedisTemplate.class);
    @SuppressWarnings("unchecked")
    private final HashOperations<String, Object, Object> hashes = mock(HashOperations.class);
    @SuppressWarnings("unchecked")
    private final ValueOperations<String, String> values = mock(ValueOperations.class);
    private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();

    @BeforeEach
    void setUp() {
        when(redis.opsForHash()).thenReturn(hashes);
        when(redis.opsForValue()).thenReturn(values);
    }

    @Test
    void taskCanBeReadByAnotherQueueInstance() throws Exception {
        when(redis.execute(any(RedisScript.class), anyList(),
                anyString(), anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(1L);
        UserPlanDTO plan = new UserPlanDTO("杭州", 2, LocalDate.of(2026, 10, 20),
                3, List.of("美食"), 2000.0);
        TaskQueue writer = new TaskQueue(redis, mapper);
        String taskId = writer.submitTask(7L, plan);
        String key = "task:plan:" + taskId;
        @SuppressWarnings("unchecked")
        var script = (RedisScript<Long>) mockingDetails(redis).getInvocations().stream()
                .filter(call -> call.getMethod().getName().equals("execute"))
                .findFirst().orElseThrow().getArgument(0);
        assertTrue(script.getScriptAsString().contains("PEXPIRE"));
        verify(redis).execute(eq(script), eq(List.of(key)), eq(taskId), eq("7"),
                anyString(), anyString(), anyString(), eq(Long.toString(TaskQueue.ACTIVE_TTL.toMillis())));

        Map<Object, Object> stored = new HashMap<>();
        stored.put("taskId", taskId);
        stored.put("userId", "7");
        stored.put("userPlan", mapper.writeValueAsString(plan));
        stored.put("status", "PENDING");
        stored.put("workerId", "other-live-worker");
        when(hashes.entries(key)).thenReturn(stored);
        when(redis.hasKey("task:plan:worker:other-live-worker")).thenReturn(true);
        AgentTask restored = new TaskQueue(redis, mapper).getTask(taskId);
        assertEquals(7L, restored.getUserId());
        assertEquals(plan, restored.getUserPlan());
        assertEquals(TaskStatus.PENDING, restored.getStatus());
    }

    @Test
    void terminalUpdatesUseTwoHourExpiryAndDoNotRecreateMissingTask() {
        when(redis.execute(any(RedisScript.class), anyList(), anyString(), anyString()))
                .thenReturn(1L, 0L);
        when(redis.execute(any(RedisScript.class), anyList(), anyString())).thenReturn(1L);
        TaskQueue queue = new TaskQueue(redis, mapper);
        assertTrue(queue.completeTask("done", new TravelItineraryVO()));
        assertFalse(queue.completeTask("missing", new TravelItineraryVO()));
        assertTrue(queue.failTask("failed"));
        Map<Object, Object> completed = new HashMap<>();
        completed.put("taskId", "done");
        completed.put("userId", "7");
        completed.put("userPlan", "{}");
        completed.put("status", "COMPLETED");
        completed.put("result", "{\"dailySchedules\":null,\"budgetSummary\":null}");
        when(hashes.entries("task:plan:done")).thenReturn(completed);
        assertNotNull(new TaskQueue(redis, mapper).getTask("done").getResult());
        verify(redis, times(2)).execute(any(RedisScript.class), anyList(), anyString(),
                eq(Long.toString(TaskQueue.FINISHED_TTL.toMillis())));
        verify(redis).execute(any(RedisScript.class), eq(List.of("task:plan:failed")),
                eq(Long.toString(TaskQueue.FINISHED_TTL.toMillis())));
    }

    @Test
    void abandonedRunningTaskBecomesFailedWhenItsWorkerHeartbeatExpires() throws Exception {
        TaskQueue queue = new TaskQueue(redis, mapper);
        Map<Object, Object> active = new HashMap<>();
        active.put("taskId", "old");
        active.put("userId", "7");
        active.put("userPlan", mapper.writeValueAsString(new UserPlanDTO()));
        active.put("status", "PROCESSING");
        active.put("workerId", "dead-worker");
        Map<Object, Object> failed = new HashMap<>(active);
        failed.put("status", "FAILED");
        when(hashes.entries("task:plan:old")).thenReturn(active, failed);
        when(redis.hasKey("task:plan:worker:dead-worker")).thenReturn(false);
        when(redis.execute(any(RedisScript.class), anyList(), anyString())).thenReturn(1L);

        assertEquals(TaskStatus.FAILED, queue.getTask("old").getStatus());
        verify(redis).execute(any(RedisScript.class), eq(List.of("task:plan:old")),
                eq(Long.toString(TaskQueue.FINISHED_TTL.toMillis())));
    }
}
