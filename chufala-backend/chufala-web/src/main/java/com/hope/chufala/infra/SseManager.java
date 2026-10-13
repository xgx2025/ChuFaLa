package com.hope.chufala.infra;

import com.hope.chufala.agent.AgentTask;
import com.hope.chufala.agent.TaskStatus;
import com.hope.chufala.model.vo.TravelItineraryVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SSE 连接管理器。
 *
 * <p>维护「任务 ID → SseEmitter」的映射，供行程规划各节点推送进度；
 * 连接完成或超时会自动清理，避免 Map 无限增长。
 *
 * @author 谢光湘
 */
@Slf4j
@Component
public class SseManager implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * 存储SSE Emitter
     * 一个任务ID对应一个SSE Emitter
     */
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final transient TaskQueue taskQueue;

    public SseManager(TaskQueue taskQueue) {
        this.taskQueue = taskQueue;
    }

    /**
     * 注册一个任务的 SseEmitter
     *
     * @param taskId  任务 ID
     * @param emitter SSE 发射器
     */
    public void registerEmitter(String taskId, SseEmitter emitter) {
        emitters.put(taskId, emitter);
        log.info("已为任务 {} 注册 SSE 连接", taskId);
        // 设置完成和超时回调，自动清理
        emitter.onCompletion(() -> emitters.remove(taskId, emitter));
        emitter.onTimeout(() -> emitters.remove(taskId, emitter));
    }

    /**
     * 发送进度信息
     *
     * <p>推送失败（连接已断开）时移除该连接，不影响规划流程继续执行。
     *
     * @param taskId   任务 ID
     * @param node 节点名
     * @param message  进度文案
     */
    public void sendProgress(String taskId, String node, String message) {
        TaskQueue.TaskProgress progress = new TaskQueue.TaskProgress(node, message, System.currentTimeMillis());
        try {
            if (!taskQueue.updateProgress(taskId, node, message, progress.timestamp())) return;
        } catch (RuntimeException e) {
            log.warn("保存规划节点进度失败，taskId={}", taskId, e);
        }
        sendProgressEvent(taskId, progress);
    }

    public void replayProgress(String taskId) {
        TaskQueue.TaskProgress progress = taskQueue.getProgress(taskId);
        if (progress != null) sendProgressEvent(taskId, progress);
    }

    private void sendProgressEvent(String taskId, TaskQueue.TaskProgress progress) {
        SseEmitter emitter = emitters.get(taskId);
        if(emitter != null){
            try {
                emitter.send(SseEmitter.event()
                        .name("progress")
                        .data(Map.of(
                                "node", progress.node(),
                                "message", progress.message(),
                                "timestamp", progress.timestamp()
                        )));
            }catch (IOException | IllegalStateException e){
                emitters.remove(taskId, emitter);
            }
        }
    }

    /**
     * 完成任务
     *
     * <p>推送 complete 事件（携带最终行程）并关闭连接。
     *
     * @param taskId 任务 ID
     * @param result 最终行程结果
     */
    public void completeTask(String taskId, TravelItineraryVO result){
        SseEmitter emitter = emitters.remove(taskId);
        if (emitter != null){
            try{
                emitter.send(SseEmitter.event().name("complete").data(result));
                emitter.complete();
            } catch (IOException | IllegalStateException e) {
                log.debug("任务 {} 的完成事件发送失败", taskId, e);
                try {
                    emitter.completeWithError(e);
                } catch (IllegalStateException ignored) {
                    // 连接可能已经被客户端关闭；Redis 中的完成状态不受影响。
                }
            }
        }
    }

    /** 推送失败终态并关闭连接。 */
    public void failTask(String taskId, String message) {
        SseEmitter emitter = emitters.remove(taskId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event().name("failed").data(message));
                emitter.complete();
            } catch (IOException | IllegalStateException e) {
                log.debug("任务 {} 的失败事件发送失败", taskId, e);
                try {
                    emitter.completeWithError(e);
                } catch (IllegalStateException ignored) {
                    // 连接可能已经被客户端关闭。
                }
            }
        }
    }

    /** 补发其他进程写入的终态，以及执行进程退出后的失败状态。 */
    @Scheduled(fixedDelay = 15_000)
    public void deliverFinishedTasks() {
        for (String taskId : emitters.keySet()) {
            try {
                AgentTask task = taskQueue.getTask(taskId);
                if (task == null || task.getStatus() == TaskStatus.FAILED) {
                    failTask(taskId, "行程规划失败或任务已过期");
                } else if (task.getStatus() == TaskStatus.COMPLETED) {
                    completeTask(taskId, task.getResult());
                }
            } catch (RuntimeException e) {
                log.warn("检查规划任务终态失败，taskId={}", taskId, e);
            }
        }
    }
}
