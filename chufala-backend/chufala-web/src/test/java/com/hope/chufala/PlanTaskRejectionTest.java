package com.hope.chufala;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hope.chufala.agent.AgentService;
import com.hope.chufala.common.constant.ResultCode;
import com.hope.chufala.common.model.vo.Result;
import com.hope.chufala.common.util.ThreadLocalUtils;
import com.hope.chufala.config.ThreadPoolConfig;
import com.hope.chufala.controller.AgentController;
import com.hope.chufala.infra.task.RedisPlanTaskStore;
import com.hope.chufala.model.dto.UserPlanDTO;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.RejectedExecutionException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;

class PlanTaskRejectionTest {
    @Test
    void saturatedPlanExecutorRejectsWithoutRunningOnSubmitter() {
        ThreadPoolTaskExecutor executor = (ThreadPoolTaskExecutor) new ThreadPoolConfig().planExecutor();
        CountDownLatch release = new CountDownLatch(1);
        try {
            for (int i = 0; i < 66; i++) {
                executor.execute(() -> {
                    try {
                        release.await();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            }
            assertThrows(RejectedExecutionException.class, () -> executor.execute(() -> {}));
        } finally {
            release.countDown();
            executor.shutdown();
        }
    }

    @Test
    void rejectedPlanDoesNotLeavePendingTask() {
        AgentController controller = new AgentController();
        AgentService agentService = mock(AgentService.class);
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        @SuppressWarnings("unchecked") ValueOperations<String, String> values = mock(ValueOperations.class);
        @SuppressWarnings("unchecked") HashOperations<String, Object, Object> hashes = mock(HashOperations.class);
        when(redis.opsForValue()).thenReturn(values);
        when(redis.opsForHash()).thenReturn(hashes);
        when(hashes.entries(anyString())).thenReturn(Map.of());
        when(redis.execute(any(RedisScript.class), anyList(),
                anyString(), anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(1L);
        RedisPlanTaskStore planTaskStore = new RedisPlanTaskStore(redis, new ObjectMapper().findAndRegisterModules());
        ReflectionTestUtils.setField(controller, "agentService", agentService);
        ReflectionTestUtils.setField(controller, "planTaskStore", planTaskStore);
        doThrow(new RejectedExecutionException("full")).when(agentService).planTravel(anyString(), eq(7L));
        Claims claims = Jwts.claims();
        claims.put("userId", 7L);
        ThreadLocalUtils.set(claims);
        try {
            Result result = controller.submitTask(new UserPlanDTO());
            assertEquals(ResultCode.SYSTEM_BUSY.getCode(), result.getCode());
            assertNull(result.getData());
            verify(agentService).planTravel(argThat(taskId -> planTaskStore.getTask(taskId) == null), eq(7L));
            verify(redis).delete(anyString());
        } finally {
            ThreadLocalUtils.remove();
        }
    }
}
