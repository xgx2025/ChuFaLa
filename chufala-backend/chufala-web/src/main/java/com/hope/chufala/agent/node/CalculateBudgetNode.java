package com.hope.chufala.agent.node;


import com.hope.chufala.model.vo.*;
import com.hope.chufala.infra.SseManager;
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
    private SseManager sseManager;

    /**
     * 计算行程预算并写回行程骨架。
     *
     * <p>注意：交通/餐饮金额直接取模型返回列表的下标 0、1，隐含依赖模型
     * 按「交通在前、餐饮在后」的顺序返回。
     *
     * @param state 规划状态（需含 masterPlan 与 itinerarySkeleton）
     * @return 含更新后 itinerarySkeleton 的状态增量
     * @throws Exception 节点执行异常
     */
    @Override
    public Map<String, Object> apply(TravelPlanState state) throws Exception {
        log.info("===CalculateBudgetNode节点===");
        log.info("开始计算旅游预算");
        sseManager.sendProgress(state.taskId(), "CalculateBudgetNode", "正在计算旅游预算");
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
                attractionCost += attraction.getPrice()*people;
            }
            Double hotelPrice = 0.0;
            List<HotelInfoVO> recommendHotels = dailySchedule.getRecommendHotels();
            for (int j = 0; j < recommendHotels.size(); j++) {
                HotelInfoVO hotel = recommendHotels.get(j);
                hotelPrice += hotel.getPrice();
            }
            hotelPrice /= recommendHotels.size();
            hotelCost += hotelPrice * people;
        }

        List<BudgetItemVO> budgetList = new ArrayList<>();
        double totalCost = attractionCost + hotelCost;
        if (response != null){
            double trafficCost = response.budgetList.get(0).amount;
            double foodCost = response.budgetList.get(1).amount;
            totalCost += (trafficCost + foodCost);

            budgetList.add(new BudgetItemVO("门票费用", attractionCost, attractionCost/totalCost));
            budgetList.add(new BudgetItemVO("住宿费用", Math.round(hotelCost*100.0)/100.0, hotelCost/totalCost));

            if (response.budgetList.get(0).category.equals("交通费用")){
                budgetList.add(new BudgetItemVO("交通费用", response.budgetList.get(0).amount, trafficCost/totalCost));
            }else{
                budgetList.add(new BudgetItemVO("交通费用", response.budgetList.get(1).amount, trafficCost/totalCost));
            }
            //计算比率

            if (response.budgetList.get(0).category.equals("餐饮费用")){
                budgetList.add(new BudgetItemVO("餐饮费用", response.budgetList.get(0).amount, foodCost/totalCost));
            }else{
                budgetList.add(new BudgetItemVO("餐饮费用", response.budgetList.get(1).amount, foodCost/totalCost));
            }
            //计算比率
        }
        log.info("BudgetList{}", budgetList);
        log.info("预算计算结果: {}", budgetList);
        itinerary.setBudgetSummary(new BudgetSummaryVO(totalCost, budgetList));
        sseManager.sendProgress(state.taskId(), "CalculateBudgetNode", "预算计算完成");
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
