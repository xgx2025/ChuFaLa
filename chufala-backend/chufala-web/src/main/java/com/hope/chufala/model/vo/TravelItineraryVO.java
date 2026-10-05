package com.hope.chufala.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 完整行程规划结果。
 *
 * <p>作为 PlanHistory.planResult 的 JSON 载体持久化，同时是规划接口的最终返回结构。
 *
 * @author 谢光湘
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TravelItineraryVO implements Serializable {
    private static final long serialVersionUID = 1L;
    /** 逐日行程安排 */
    private List<DailyScheduleVO> dailySchedules;
    /** 预算汇总 */
    private BudgetSummaryVO budgetSummary;

}
