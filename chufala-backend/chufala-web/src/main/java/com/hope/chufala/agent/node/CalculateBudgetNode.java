package com.hope.chufala.agent.node;


import com.hope.chufala.model.vo.*;
import com.hope.chufala.infra.stream.PlanProgressStreamManager;
import com.hope.chufala.agent.TravelPlanState;
import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.NodeAction;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 预算计算节点（流水线末节点）。
 *
 * <p>费用来自两个来源：交通与餐饮由 deepseek 估算（模型只返回 category + amount），
 * 门票与住宿在本地计算——门票按「景点票价 × 人数」逐项累加，住宿按「当日推荐酒店均价 × 人数」
 * 逐日累加，最后统一折算占比写入 itinerarySkeleton.budgetSummary。
 *
 * @author 谢光湘
 */
@Slf4j
@Component
public class CalculateBudgetNode implements NodeAction<TravelPlanState> {
    @Resource(name = "deepseek")
    private ChatClient chatClient;
    @Autowired
    private PlanProgressStreamManager planProgressStreamManager;

    /**
     * 计算行程预算并写回行程骨架。
     *
     * <p>交通和餐饮按分类名称识别；模型漏掉任一分类时保留已有费用，不因列表长度中断规划。
     *
     * @param state 规划状态（需含 masterPlan 与 itinerarySkeleton）
     * @return 含更新后 itinerarySkeleton 的状态增量
     * @throws Exception 节点执行异常
     */
    @Override
    public Map<String, Object> apply(TravelPlanState state) throws Exception {
        log.info("===CalculateBudgetNode节点===");
        log.info("开始计算旅游预算");
        planProgressStreamManager.stageStarted(state.taskId(), "budget", "正在核算门票、住宿、交通和餐饮费用");
        int people = state.userPlan().getPeople();
        //大模型预算餐饮、交通费用
        String userPrompt =
            """
            这是行程计划：%s，人数：%s,游玩天数：%s。预算一下交通费用和餐饮费用。
            请填充category字段（分类选择为“交通费用”、“餐饮费用”）和amount字段（分类的费用）。
            请严格按照纯json格式返回。
            """
            .formatted(state.masterPlan(), state.userPlan().getPeople(), state.userPlan().getDayNum());
        TravelBudget response = chatClient.prompt()
                .system("你是一位旅游预算师。")
                .user(userPrompt)
                .call()
                .entity(TravelBudget.class);
        log.info("大模型预算结果: {}", response);
        //计算门票、酒店费用
        double attractionCost = 0.0;
        double hotelCost = 0.0;
        TravelItineraryVO itinerary = state.itinerarySkeleton();
        List<DailyScheduleVO> dailySchedules = itinerary.getDailySchedules();
        for (int i = 0; i < dailySchedules.size(); i++) {
            DailyScheduleVO dailySchedule = dailySchedules.get(i);
            List<AttractionInfoVO> activities = dailySchedule.getActivities();
            for (int j = 0; j < activities.size(); j++) {
                AttractionInfoVO attraction = activities.get(j);
                if (attraction.getPrice() != null) attractionCost += attraction.getPrice()*people;
            }
            Double hotelPrice = 0.0;
            List<HotelInfoVO> recommendHotels = dailySchedule.getRecommendHotels();
            if (recommendHotels != null) {
                for (HotelInfoVO hotel : recommendHotels) {
                    if (hotel != null && hotel.getPrice() != null) hotelPrice += hotel.getPrice();
                }
                if (!recommendHotels.isEmpty()) hotelPrice /= recommendHotels.size();
            }
            hotelCost += hotelPrice * people;
        }

        double trafficCost = 0.0;
        double foodCost = 0.0;
        boolean hasTraffic = false;
        boolean hasFood = false;
        if (response != null && response.budgetList != null) {
            for (TravelBudget.Budget item : response.budgetList) {
                if (item == null || item.category == null || item.amount == null || item.amount < 0) continue;
                if (item.category.contains("交通")) {
                    trafficCost = item.amount;
                    hasTraffic = true;
                } else if (item.category.contains("餐饮")) {
                    foodCost = item.amount;
                    hasFood = true;
                }
            }
        }
        double totalCost = attractionCost + hotelCost + trafficCost + foodCost;
        List<BudgetItemVO> budgetList = new ArrayList<>();
        double denominator = totalCost > 0 ? totalCost : 1;
        budgetList.add(new BudgetItemVO("门票费用", attractionCost, attractionCost / denominator));
        budgetList.add(new BudgetItemVO("住宿费用", Math.round(hotelCost * 100.0) / 100.0, hotelCost / denominator));
        if (hasTraffic) budgetList.add(new BudgetItemVO("交通费用", trafficCost, trafficCost / denominator));
        if (hasFood) budgetList.add(new BudgetItemVO("餐饮费用", foodCost, foodCost / denominator));
        log.info("BudgetList{}", budgetList);
        log.info("预算计算结果: {}", budgetList);
        itinerary.setBudgetSummary(new BudgetSummaryVO(totalCost, budgetList));
        planProgressStreamManager.stageCompleted(state.taskId(), "budget", "行程预算已汇总",
                String.format("总预算约 ¥%.0f；%s", totalCost,
                        hasTraffic && hasFood ? "含门票、住宿、交通和餐饮"
                                : "交通或餐饮暂无完整估算"));
        return Map.of(TravelPlanState.ITINERARY_SKELETON, itinerary);
    }


    /** 大模型返回的预算结果 */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class TravelBudget{
        /** 分项预算列表（交通、餐饮） */
        private List<Budget> budgetList;

        /** 单个预算分项 */
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        static class Budget{
            /** 费用类别 */
            private String category;
            /** 金额（元） */
            private Double amount;
        }
    }

}
