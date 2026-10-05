package com.hope.chufala.agent;

import com.hope.chufala.model.dto.UserPlanDTO;
import com.hope.chufala.model.vo.TravelItineraryVO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 行程规划任务。
 *
 * <p>提交后暂存在 TaskQueue 中，供异步执行与进度订阅时按 taskId 检索。
 *
 * @author 谢光湘
 */
@Data
@NoArgsConstructor
@AllArgsConstructor

public class AgentTask {
    /**
     * 任务ID
     */
    private String taskId;
    /** 提交任务的用户 ID，仅用于服务端归属校验。 */
    private Long userId;
    /**
     * 用户旅游的相关信息
     */
    private UserPlanDTO  userPlan;
    /**
     * 任务状态（目前没有用到该字段）
     */
    private TaskStatus status;
    /**
     * 行程规划结果
     */
    private TravelItineraryVO result;

    /**
     * 构造不含结果的初始任务（提交时使用）。
     *
     * @param taskId   任务 ID
     * @param userId   提交用户 ID
     * @param userPlan 规划入参
     * @param status   初始状态
     */
    public AgentTask(String taskId, Long userId, UserPlanDTO userPlan, TaskStatus status) {
        this.taskId = taskId;
        this.userId = userId;
        this.userPlan = userPlan;
        this.status = status;
    }
}
