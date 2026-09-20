//package com.hope.utils;
//
//import com.hope.domain.agent.SseManager;
//import lombok.extern.slf4j.Slf4j;
//
//@Slf4j
//public class ProgressUtil {
//
//    /**
//     * 安全地发送任务进度
//     * @param taskId 任务ID
//     * @param sseManager SSE管理器
//     * @param nodeName 节点名称
//     * @param message 进度消息
//     */
//    public static void sendProgress(String taskId, SseManager sseManager, String nodeName, String message) {
//        try {
//            if (sseManager != null && taskId != null) {
//                sseManager.sendProgress(taskId, nodeName, message);
//            }
//        } catch (Exception e) {
//            // 推送失败不应影响主流程，只记录日志
//            log.error("发送进度失败 (节点: {}, 消息: {})", nodeName, message, e);
//        }
//    }
//}
