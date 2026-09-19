package com.hope.chufala.domain.agent;

import com.hope.chufala.domain.dto.UserPlanDTO;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TaskQueue {
    /**
     * 任务队列
     * key:任务ID
     * value:任务对象（存储用户行程的相关信息、规划后的结果）
     */
    private final Map<String,AgentTask> taskMap = new ConcurrentHashMap<>();

    /**
     * 提交任务
     * @param userPlan 用户旅行的相关信息
     * @return
     */
    public String submitTask(UserPlanDTO userPlan){
        String taskId = UUID.randomUUID().toString();
        taskMap.put(taskId,new AgentTask(taskId,userPlan,TaskStatus.PENDING));
        return taskId;
    }
    /**
     * 获取任务
     * @param taskId
     * @return
     */
    public AgentTask getTask(String taskId){
        return taskMap.get(taskId);
    }
}
