package com.hope.chufala.infra;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hope.chufala.agent.AgentTask;
import com.hope.chufala.agent.TaskStatus;
import com.hope.chufala.model.dto.UserPlanDTO;
import com.hope.chufala.model.vo.TravelItineraryVO;
import jakarta.annotation.PreDestroy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Redis 中的行程规划任务。每个任务占用一个带过期时间的 Hash。 */
@Component
public class TaskQueue {

    private static final String KEY_PREFIX = "task:plan:";
    private static final String WORKER_KEY_PREFIX = "task:plan:worker:";
    // 异步执行器中的任务不会跨服务重启恢复，运行中记录也必须有过期上限。
    static final Duration ACTIVE_TTL = Duration.ofHours(24);
    static final Duration FINISHED_TTL = Duration.ofHours(2);
    private static final Duration WORKER_TTL = Duration.ofMinutes(1);

    private static final DefaultRedisScript<Long> CREATE_SCRIPT = script("""
            redis.call('HSET', KEYS[1], 'taskId', ARGV[1], 'userId', ARGV[2],
                'userPlan', ARGV[3], 'status', 'PENDING', 'createdAt', ARGV[4],
                'workerId', ARGV[5])
            redis.call('PEXPIRE', KEYS[1], ARGV[6])
            return 1
            """);
    private static final DefaultRedisScript<Long> START_SCRIPT = script("""
            if redis.call('HGET', KEYS[1], 'status') ~= 'PENDING' then return 0 end
            redis.call('HSET', KEYS[1], 'status', 'PROCESSING')
            redis.call('PEXPIRE', KEYS[1], ARGV[1])
            return 1
            """);
    private static final DefaultRedisScript<Long> COMPLETE_SCRIPT = script("""
            if redis.call('HGET', KEYS[1], 'status') ~= 'PROCESSING' then return 0 end
            redis.call('HSET', KEYS[1], 'status', 'COMPLETED', 'result', ARGV[1])
            redis.call('PEXPIRE', KEYS[1], ARGV[2])
            return 1
            """);
    private static final DefaultRedisScript<Long> FAIL_SCRIPT = script("""
            local status = redis.call('HGET', KEYS[1], 'status')
            if status ~= 'PENDING' and status ~= 'PROCESSING' then return 0 end
            redis.call('HSET', KEYS[1], 'status', 'FAILED')
            redis.call('HDEL', KEYS[1], 'result')
            redis.call('PEXPIRE', KEYS[1], ARGV[1])
            return 1
            """);
    private static final DefaultRedisScript<Long> PROGRESS_SCRIPT = script("""
            if redis.call('HGET', KEYS[1], 'status') ~= 'PROCESSING' then return 0 end
            redis.call('HSET', KEYS[1], 'progressNode', ARGV[1],
                'progressMessage', ARGV[2], 'progressAt', ARGV[3])
            return 1
            """);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final String workerId = UUID.randomUUID().toString();

    public TaskQueue(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public String submitTask(Long userId, UserPlanDTO userPlan) {
        heartbeat();
        String taskId = UUID.randomUUID().toString();
        Long created = redisTemplate.execute(CREATE_SCRIPT, List.of(key(taskId)), taskId,
                userId.toString(), toJson(userPlan), Long.toString(System.currentTimeMillis()),
                workerId, Long.toString(ACTIVE_TTL.toMillis()));
        if (!Long.valueOf(1).equals(created)) {
            throw new IllegalStateException("创建行程规划任务失败");
        }
        return taskId;
    }

    /** 不存在或已过期时返回 null。返回值是快照，修改它不会写回 Redis。 */
    public AgentTask getTask(String taskId) {
        Map<Object, Object> fields = redisTemplate.opsForHash().entries(key(taskId));
        if (fields.isEmpty()) {
            return null;
        }
        String owner = (String) fields.get("workerId");
        String status = (String) fields.get("status");
        if (owner != null && !workerId.equals(owner)
                && (TaskStatus.PENDING.name().equals(status) || TaskStatus.PROCESSING.name().equals(status))
                && Boolean.FALSE.equals(redisTemplate.hasKey(WORKER_KEY_PREFIX + owner))) {
            // 原执行进程已停止：只把状态改为失败，保留记录供重连的用户读取。
            failTask(taskId);
            fields = redisTemplate.opsForHash().entries(key(taskId));
            if (fields.isEmpty()) {
                return null;
            }
        }
        AgentTask task = new AgentTask();
        task.setTaskId((String) fields.get("taskId"));
        task.setUserId(Long.valueOf((String) fields.get("userId")));
        task.setUserPlan(fromJson((String) fields.get("userPlan"), UserPlanDTO.class));
        task.setStatus(TaskStatus.valueOf((String) fields.get("status")));
        if (fields.containsKey("result")) {
            task.setResult(fromJson((String) fields.get("result"), TravelItineraryVO.class));
        }
        return task;
    }

    public boolean startTask(String taskId) {
        return Long.valueOf(1).equals(redisTemplate.execute(START_SCRIPT, List.of(key(taskId)),
                Long.toString(ACTIVE_TTL.toMillis())));
    }

    public boolean completeTask(String taskId, TravelItineraryVO result) {
        return Long.valueOf(1).equals(redisTemplate.execute(COMPLETE_SCRIPT, List.of(key(taskId)),
                toJson(result), Long.toString(FINISHED_TTL.toMillis())));
    }

    public boolean failTask(String taskId) {
        return Long.valueOf(1).equals(redisTemplate.execute(FAIL_SCRIPT, List.of(key(taskId)),
                Long.toString(FINISHED_TTL.toMillis())));
    }

    public boolean updateProgress(String taskId, String node, String message, long timestamp) {
        return Long.valueOf(1).equals(redisTemplate.execute(PROGRESS_SCRIPT, List.of(key(taskId)),
                node, message, Long.toString(timestamp)));
    }

    /** 供新建立的 SSE 连接补收最近一次节点进度。 */
    public TaskProgress getProgress(String taskId) {
        List<Object> fields = redisTemplate.opsForHash().multiGet(key(taskId),
                List.of("progressNode", "progressMessage", "progressAt"));
        if (fields == null || fields.size() != 3 || fields.get(0) == null
                || fields.get(1) == null || fields.get(2) == null) {
            return null;
        }
        return new TaskProgress((String) fields.get(0), (String) fields.get(1),
                Long.parseLong((String) fields.get(2)));
    }

    public record TaskProgress(String node, String message, long timestamp) {}

    /** 执行器拒绝任务后立即删除 PENDING 记录。 */
    public void removeTask(String taskId) {
        redisTemplate.delete(key(taskId));
    }

    /** 运行中任务的执行进程心跳；进程退出后心跳自动过期。 */
    @Scheduled(fixedDelay = 15_000)
    public void heartbeat() {
        redisTemplate.opsForValue().set(WORKER_KEY_PREFIX + workerId, "1", WORKER_TTL);
    }

    @PreDestroy
    public void stopHeartbeat() {
        redisTemplate.delete(WORKER_KEY_PREFIX + workerId);
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("序列化行程规划任务失败", e);
        }
    }

    private <T> T fromJson(String json, Class<T> type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("反序列化行程规划任务失败", e);
        }
    }

    private static String key(String taskId) {
        return KEY_PREFIX + taskId;
    }

    private static DefaultRedisScript<Long> script(String source) {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText(source);
        script.setResultType(Long.class);
        return script;
    }
}
