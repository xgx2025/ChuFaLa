package com.hope.chufala.domain.agent;

import com.hope.chufala.domain.dto.UserPlanDTO;
import com.hope.chufala.domain.vo.TravelItinerary;
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
    private TravelItinerary result;

    public AgentTask(String taskId, UserPlanDTO userPlan, TaskStatus status) {
        this.taskId = taskId;
        this.userPlan = userPlan;
        this.status = status;
    }
}
