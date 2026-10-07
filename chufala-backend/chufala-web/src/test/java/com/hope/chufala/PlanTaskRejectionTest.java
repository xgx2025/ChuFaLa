package com.hope.chufala;

import com.hope.chufala.agent.AgentService;
import com.hope.chufala.common.constant.ResultCode;
import com.hope.chufala.common.model.vo.Result;
import com.hope.chufala.common.util.ThreadLocalUtils;
import com.hope.chufala.config.ThreadPoolConfig;
import com.hope.chufala.controller.AgentController;
import com.hope.chufala.infra.TaskQueue;
import com.hope.chufala.model.dto.UserPlanDTO;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.RejectedExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.argThat;

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
        TaskQueue taskQueue = new TaskQueue();
        ReflectionTestUtils.setField(controller, "agentService", agentService);
        ReflectionTestUtils.setField(controller, "taskQueue", taskQueue);
        doThrow(new RejectedExecutionException("full")).when(agentService).planTravel(anyString(), eq(7L));
        Claims claims = Jwts.claims();
        claims.put("userId", 7L);
        ThreadLocalUtils.set(claims);
        try {
            Result result = controller.submitTask(new UserPlanDTO());
            assertEquals(ResultCode.SYSTEM_BUSY.getCode(), result.getCode());
            assertNull(result.getData());
            verify(agentService).planTravel(argThat(taskId -> taskQueue.getTask(taskId) == null), eq(7L));
        } finally {
            ThreadLocalUtils.remove();
        }
    }
}
