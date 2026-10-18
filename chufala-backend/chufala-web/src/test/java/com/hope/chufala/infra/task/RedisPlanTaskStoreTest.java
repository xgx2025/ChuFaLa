package com.hope.chufala.infra.task;

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

class RedisPlanTaskStoreTest {
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
        RedisPlanTaskStore writer = new RedisPlanTaskStore(redis, mapper);
        String taskId = writer.submitTask(7L, plan);
        String key = "task:plan:" + taskId;
        @SuppressWarnings("unchecked")
        var script = (RedisScript<Long>) mockingDetails(redis).getInvocations().stream()
                .filter(call -> call.getMethod().getName().equals("execute"))
                .findFirst().orElseThrow().getArgument(0);
        assertTrue(script.getScriptAsString().contains("PEXPIRE"));
        verify(redis).execute(eq(script), eq(List.of(key)), eq(taskId), eq("7"),
                anyString(), anyString(), anyString(), eq(Long.toString(RedisPlanTaskStore.ACTIVE_TTL.toMillis())));

        Map<Object, Object> stored = new HashMap<>();
        stored.put("taskId", taskId);
        stored.put("userId", "7");
        stored.put("userPlan", mapper.writeValueAsString(plan));
        stored.put("status", "PENDING");
        stored.put("workerId", "other-live-worker");
        when(hashes.entries(key)).thenReturn(stored);
        when(redis.hasKey("task:plan:worker:other-live-worker")).thenReturn(true);
        AgentTask restored = new RedisPlanTaskStore(redis, mapper).getTask(taskId);
        assertEquals(7L, restored.getUserId());
        assertEquals(plan, restored.getUserPlan());
        assertEquals(TaskStatus.PENDING, restored.getStatus());
    }

    @Test
    void terminalUpdatesUseTwoHourExpiryAndDoNotRecreateMissingTask() {
        when(redis.execute(any(RedisScript.class), anyList(), anyString(), anyString(), anyString()))
                .thenReturn(1L, 0L, 1L);
        RedisPlanTaskStore queue = new RedisPlanTaskStore(redis, mapper);
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
        assertNotNull(new RedisPlanTaskStore(redis, mapper).getTask("done").getResult());
        verify(redis, times(2)).execute(any(RedisScript.class), anyList(), anyString(),
                eq(Long.toString(RedisPlanTaskStore.FINISHED_TTL.toMillis())),
                eq(Integer.toString(RedisPlanTaskStore.EVENT_HISTORY_LIMIT)));
        verify(redis).execute(any(RedisScript.class), eq(List.of("task:plan:failed")),
                eq(Long.toString(RedisPlanTaskStore.FINISHED_TTL.toMillis())), anyString(),
                eq(Integer.toString(RedisPlanTaskStore.EVENT_HISTORY_LIMIT)));
    }

    @Test
    void abandonedRunningTaskBecomesFailedWhenItsWorkerHeartbeatExpires() throws Exception {
        RedisPlanTaskStore queue = new RedisPlanTaskStore(redis, mapper);
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
        when(redis.execute(any(RedisScript.class), anyList(), anyString(), anyString(), anyString()))
                .thenReturn(1L);

        assertEquals(TaskStatus.FAILED, queue.getTask("old").getStatus());
        verify(redis).execute(any(RedisScript.class), eq(List.of("task:plan:old")),
                eq(Long.toString(RedisPlanTaskStore.FINISHED_TTL.toMillis())), anyString(),
                eq(Integer.toString(RedisPlanTaskStore.EVENT_HISTORY_LIMIT)));
    }

    @Test
    void latestNodeProgressCanBeReadAfterReconnect() {
        when(redis.execute(any(RedisScript.class), anyList(),
                anyString(), anyString(), anyString(), anyString())).thenReturn(1L);
        when(hashes.multiGet("task:plan:task-1",
                List.of("progressNode", "progressMessage", "progressAt")))
                .thenReturn(List.of("PlanItineraryNode", "正在规划行程", "123"));
        RedisPlanTaskStore queue = new RedisPlanTaskStore(redis, mapper);
        assertTrue(queue.updateProgress("task-1", "PlanItineraryNode", "正在规划行程", 123L));
        assertEquals(new RedisPlanTaskStore.TaskProgress("PlanItineraryNode", "正在规划行程", 123L),
                new RedisPlanTaskStore(redis, mapper).getProgress("task-1"));
    }

    @Test
    void stageSnapshotRestoresEveryStageAndKeepsMonotonicSequence() {
        when(redis.execute(any(RedisScript.class), eq(List.of("task:plan:task-1")),
                eq("routes"), eq("COMPLETED"), eq("每日路线已编排"),
                eq("已安排 3 天路线"), anyString(), eq(""),
                eq(Integer.toString(RedisPlanTaskStore.EVENT_HISTORY_LIMIT)))).thenReturn(4L);
        Map<Object, Object> stored = new HashMap<>();
        stored.put("progressSeq", "4");
        stored.put("currentStage", "routes");
        stored.put("stage:candidates:state", "COMPLETED");
        stored.put("stage:candidates:summary", "筛选了 12 处景点");
        stored.put("stage:routes:state", "COMPLETED");
        stored.put("stage:routes:message", "每日路线已编排");
        stored.put("stage:routes:summary", "已安排 3 天路线");
        stored.put("stage:routes:updatedAt", "123");
        when(hashes.entries("task:plan:task-1")).thenReturn(stored);

        RedisPlanTaskStore queue = new RedisPlanTaskStore(redis, mapper);
        assertEquals(4L, queue.updateStage("task-1", "routes", "COMPLETED",
                "每日路线已编排", "已安排 3 天路线"));
        RedisPlanTaskStore.StageSnapshot snapshot = new RedisPlanTaskStore(redis, mapper).getStageSnapshot("task-1");
        assertEquals(4L, snapshot.seq());
        assertEquals("routes", snapshot.currentStage());
        assertEquals(5, snapshot.stages().size());
        assertEquals("筛选了 12 处景点", snapshot.stages().get(0).summary());
        assertEquals("已安排 3 天路线", snapshot.stages().get(1).summary());
        assertEquals("PENDING", snapshot.stages().get(2).state());
        assertEquals(123L, snapshot.stages().get(1).updatedAt());
    }

    @Test
    void weatherPreviewIsPersistedAndRestoredWithStageSnapshot() {
        when(redis.execute(any(RedisScript.class), eq(List.of("task:plan:task-1")),
                eq("logistics"), eq("RUNNING"), anyString(), anyString(), anyString(), anyString(),
                eq(Integer.toString(RedisPlanTaskStore.EVENT_HISTORY_LIMIT))))
                .thenReturn(5L);
        Map<Object, Object> stored = new HashMap<>();
        stored.put("progressSeq", "5");
        stored.put("currentStage", "logistics");
        stored.put("stage:logistics:state", "RUNNING");
        stored.put("stage:logistics:weatherDays", "[{\"day\":1,\"weather\":\"晴天\",\"temperature\":\"20℃\"}]");
        when(hashes.entries("task:plan:task-1")).thenReturn(stored);
        RedisPlanTaskStore queue = new RedisPlanTaskStore(redis, mapper);
        List<RedisPlanTaskStore.WeatherPreview> days = List.of(new RedisPlanTaskStore.WeatherPreview(1, "晴天", "20℃"));

        assertEquals(5L, queue.updateWeatherStage("task-1", "天气已查询", "已核对 1 天的天气", days));
        assertEquals(days, new RedisPlanTaskStore(redis, mapper).getStageSnapshot("task-1")
                .stages().get(2).weatherDays());
    }

    @Test
    void eventHistoryReturnsOnlyEventsAfterCursorInOrder() {
        String key = "task:plan:task-1";
        when(hashes.multiGet(key, List.of("eventSeq", "status")))
                .thenReturn(List.of("3", "PROCESSING"));
        when(hashes.multiGet(key, List.of("event:2:type", "event:2:data",
                "event:3:type", "event:3:data")))
                .thenReturn(List.of("progress", "{\"message\":\"第一步\"}",
                        "snapshot", "{\"seq\":2}"));

        RedisPlanTaskStore.EventBatch batch = new RedisPlanTaskStore(redis, mapper).getEventsAfter("task-1", 1, 100);

        assertFalse(batch.historyExpired());
        assertEquals(List.of(
                new RedisPlanTaskStore.TaskEvent(2, "progress", "{\"message\":\"第一步\"}"),
                new RedisPlanTaskStore.TaskEvent(3, "snapshot", "{\"seq\":2}")), batch.events());
    }

    @Test
    void eventHistoryReportsExpiredCursor() {
        when(hashes.multiGet("task:plan:task-1", List.of("eventSeq", "status")))
                .thenReturn(List.of("600", "PROCESSING"));

        RedisPlanTaskStore.EventBatch batch = new RedisPlanTaskStore(redis, mapper).getEventsAfter("task-1", 1, 100);

        assertTrue(batch.historyExpired());
        assertTrue(batch.events().isEmpty());
    }
}
