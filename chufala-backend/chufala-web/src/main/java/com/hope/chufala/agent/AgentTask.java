package com.hope.chufala.agent;

import com.hope.chufala.model.dto.UserPlanDTO;
import com.hope.chufala.model.vo.TravelItineraryVO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    public AgentTask(String taskId, Long userId, UserPlanDTO userPlan, TaskStatus status) {
        this.taskId = taskId;
        this.userId = userId;
        this.userPlan = userPlan;
        this.status = status;
    }
}
