package com.hope.chufala.service;

import com.hope.chufala.model.dto.UserPlanDTO;
import com.hope.chufala.model.entity.PlanHistory;
import com.hope.chufala.model.vo.TravelItinerary;

import java.time.LocalDateTime;
import java.util.List;

public interface IPlanHistoryService {
    /**
     * 添加行程规划历史
     * @param userId
     * @param createTime
     * @param planHistory
     * @return
     */
    void addHistory(Long userId, LocalDateTime createTime, UserPlanDTO userPlan,String planContent, TravelItinerary planHistory);
    /**
     * 查询用户行程规划历史
     * @param userId
     * @return
     */
    List<PlanHistory> queryHistory(Long userId);

    /**
     * 查询指定ID的规划结果
     *
     * @param id
     * @return
     */
    TravelItinerary queryPlanResult(Long id);

    /**
     * 通过ID查询规划结果内容
     * @param id
     * @return
     */
    String getPlanContentById(Long id);
}
