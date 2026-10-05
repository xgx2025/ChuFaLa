package com.hope.chufala.infra;

import com.hope.chufala.model.vo.TravelItineraryVO;
import lombok.extern.slf4j.Slf4j;
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
        emitter.onCompletion(() -> emitters.remove(taskId));
        emitter.onTimeout(() -> emitters.remove(taskId));
    }

    /**
     * 发送进度信息
     *
     * <p>推送失败（连接已断开）时移除该连接，不影响规划流程继续执行。
     *
     * @param taskId   任务 ID
     * @param nodeNode 节点名
     * @param message  进度文案
     */
    public void sendProgress(String taskId,String nodeNode,String message) {
        SseEmitter emitter = emitters.get(taskId);
        if(emitter != null){
            try {
                emitter.send(SseEmitter.event()
                        .name("progress")
                        .data(Map.of(
                                "node",nodeNode,
                                "message",message,
                                "timestamp",System.currentTimeMillis()
                        )));
            }catch (IOException e){
                emitters.remove(taskId);
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
        SseEmitter emitter = emitters.get(taskId);
        if (emitter != null){
            try{
                emitter.send(SseEmitter.event().name("complete").data(result));
                emitter.complete();
            } catch (IOException e) {
                emitter.completeWithError(e);
            }
        }
    }
}
