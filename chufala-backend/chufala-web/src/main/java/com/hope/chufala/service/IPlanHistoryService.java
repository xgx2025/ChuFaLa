package com.hope.chufala.service;

import com.hope.chufala.model.dto.UserPlanDTO;
import com.hope.chufala.model.entity.PlanHistory;
import com.hope.chufala.model.vo.TravelItineraryVO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 行程规划历史服务。
 *
 * @author 谢光湘
 */
public interface IPlanHistoryService {
    /**
     * 保存一条行程规划历史。
     *
     * @param userId      用户 ID
     * @param createTime  创建时间
     * @param userPlan    本次规划入参
     * @param planContent 规划内容摘要
     * @param planHistory 完整行程结果
     */
    void addHistory(Long userId, LocalDateTime createTime, UserPlanDTO userPlan,String planContent, TravelItineraryVO planHistory);
    /**
     * 查询用户的规划历史列表。
     *
     * @param userId 用户 ID
     * @return 历史列表
     */
    List<PlanHistory> queryHistory(Long userId);

    /**
     * 查询指定 ID 的规划结果（校验归属）。
     *
     * @param id     历史记录 ID
     * @param userId 用户 ID
     * @return 完整行程结果
     */
    TravelItineraryVO queryPlanResult(Long id, Long userId);

    /**
     * 查询指定 ID 的规划内容摘要（校验归属）。
     *
     * @param id     历史记录 ID
     * @param userId 用户 ID
     * @return 规划内容摘要
     */
    String getPlanContentById(Long id, Long userId);
}
