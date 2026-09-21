package com.hope.chufala.domain.agent;

import com.hope.chufala.domain.dto.UserPlanDTO;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 行程规划任务队列
 *
 * 局限：任务只存放在本进程内存中，服务重启后进行中的任务会丢失。
 * 如需跨重启保留，应改为 Redis 或数据库存储。
 */
@Component
public class TaskQueue {

    /**
     * 已结束任务的保留时长。
     * 超期后在下次提交任务时被清理；未结束（PENDING / PROCESSING）的任务不会被清理，
     * 否则规划流程取不到任务会直接 NPE。
     */
    private static final long FINISHED_TASK_TTL_MILLIS = TimeUnit.HOURS.toMillis(2);

    /**
     * 任务队列
     * key:任务ID
     * value:任务对象（存储用户行程的相关信息、规划后的结果）及创建时间
     */
    private final Map<String, TaskEntry> taskMap = new ConcurrentHashMap<>();

    /** 任务 + 创建时间 */
    private record TaskEntry(AgentTask task, long createdAt) {}

    /**
     * 提交任务
     * @param userPlan 用户旅行的相关信息
     * @return 任务ID
     */
    public String submitTask(UserPlanDTO userPlan){
        // 顺带清理已结束且超期的任务，避免任务只增不减导致内存持续增长
        evictFinishedTasks();
        String taskId = UUID.randomUUID().toString();
        taskMap.put(taskId, new TaskEntry(new AgentTask(taskId, userPlan, TaskStatus.PENDING), System.currentTimeMillis()));
        return taskId;
    }

    /**
     * 获取任务
     * @param taskId 任务ID
     * @return 任务对象；不存在（未提交或已被清理）时返回 null
     */
    public AgentTask getTask(String taskId){
        TaskEntry entry = taskMap.get(taskId);
        return entry == null ? null : entry.task();
    }

    /**
     * 清理已结束（COMPLETED / FAILED）且超过保留时长的任务。
     */
    private void evictFinishedTasks() {
        long deadline = System.currentTimeMillis() - FINISHED_TASK_TTL_MILLIS;
        taskMap.entrySet().removeIf(entry -> {
            TaskEntry value = entry.getValue();
            return value.createdAt() < deadline && isFinished(value.task().getStatus());
        });
    }

    private boolean isFinished(TaskStatus status) {
        return status == TaskStatus.COMPLETED || status == TaskStatus.FAILED;
    }
}
