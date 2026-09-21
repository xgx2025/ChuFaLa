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

@Slf4j
@Component
public class CalculateBudgetNode implements NodeAction<TravelPlanState> {
    @Resource(name = "deepseek")
    private ChatClient chatClient;
    @Autowired
    private SseManager sseManager;

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
        TravelItinerary itinerary = state.itinerarySkeleton();
        List<DailySchedule> dailySchedules = itinerary.getDailySchedules();
        for (int i = 0; i < dailySchedules.size(); i++) {
            DailySchedule dailySchedule = dailySchedules.get(i);
            List<AttractionInfo> activities = dailySchedule.getActivities();
            for (int j = 0; j < activities.size(); j++) {
                AttractionInfo attraction = activities.get(j);
                attractionCost += attraction.getPrice()*people;
            }
            Double hotelPrice = 0.0;
            List<HotelInfo> recommendHotels = dailySchedule.getRecommendHotels();
            for (int j = 0; j < recommendHotels.size(); j++) {
                HotelInfo hotel = recommendHotels.get(j);
                hotelPrice += hotel.getPrice();
            }
            hotelPrice /= recommendHotels.size();
            hotelCost += hotelPrice * people;
        }

        List<BudgetItem> budgetList = new ArrayList<>();
        double totalCost = attractionCost + hotelCost;
        if (response != null){
            double trafficCost = response.budgetList.get(0).amount;
            double foodCost = response.budgetList.get(1).amount;
            totalCost += (trafficCost + foodCost);

            budgetList.add(new BudgetItem("门票费用", attractionCost, attractionCost/totalCost));
            budgetList.add(new BudgetItem("住宿费用", Math.round(hotelCost*100.0)/100.0, hotelCost/totalCost));

            if (response.budgetList.get(0).category.equals("交通费用")){
                budgetList.add(new BudgetItem("交通费用", response.budgetList.get(0).amount, trafficCost/totalCost));
            }else{
                budgetList.add(new BudgetItem("交通费用", response.budgetList.get(1).amount, trafficCost/totalCost));
            }
            //计算比率

            if (response.budgetList.get(0).category.equals("餐饮费用")){
                budgetList.add(new BudgetItem("餐饮费用", response.budgetList.get(0).amount, foodCost/totalCost));
            }else{
                budgetList.add(new BudgetItem("餐饮费用", response.budgetList.get(1).amount, foodCost/totalCost));
            }
            //计算比率
        }
        log.info("BudgetList{}", budgetList);
        log.info("预算计算结果: {}", budgetList);
        itinerary.setBudgetSummary(new BudgetSummary(totalCost, budgetList));
        sseManager.sendProgress(state.taskId(), "CalculateBudgetNode", "预算计算完成");
        return Map.of(TravelPlanState.ITINERARY_SKELETON, itinerary);
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class TravelBudget{
        private List<Budget> budgetList;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        static class Budget{
            private String category;
            private Double amount;
        }
    }

}
