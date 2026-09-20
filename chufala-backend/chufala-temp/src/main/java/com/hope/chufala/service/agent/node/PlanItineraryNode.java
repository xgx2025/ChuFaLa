package com.hope.chufala.service.agent.node;

import com.hope.chufala.domain.dto.UserPlanDTO;
import com.hope.chufala.domain.vo.AttractionInfo;
import com.hope.chufala.domain.vo.DailySchedule;
import com.hope.chufala.domain.vo.TravelItinerary;
import com.hope.chufala.domain.agent.SseManager;
import com.hope.chufala.service.IAttractionService;
import com.hope.chufala.service.agent.TravelPlanState;
import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.NodeAction;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Slf4j
@Component
public class PlanItineraryNode implements NodeAction<TravelPlanState> {
    @Resource(name = "deepseek")
    private ChatClient chatClient;
    @Autowired
    private IAttractionService attractionService;
    @Autowired
    private SseManager sseManager;


    @Override
    public Map<String, Object> apply(TravelPlanState state) {
        log.info("===PlanItineraryNode节点===");
        log.info("开始规划行程");
        sseManager.sendProgress(state.taskId(),"PlanItineraryNode", "正在规划行程、选择景点...");
        UserPlanDTO userPlan = state.userPlan();
        List<String> preferences = userPlan.getPreferences();
        String preferenesStr =  "无";
        if (preferences != null && !preferences.isEmpty()) {
            preferenesStr = preferences.toString();
        }
        String prompt = """
        请根据以下信息规划每日行程。
        目的地: %s
        人数：%s
        出发日期：%s
        天数: %s
        候选景点: %s
        偏好：%s
        预算：%s
        要求：
        1. 为每天选择景点，输出景点列表,以及对应的tip。
        2. 将总旅游计划填充到masterPlan字段。
        """.formatted(userPlan.getDestination(), userPlan.getPeople(), userPlan.getStartDate(),userPlan.getDayNum(),state.candidateAttractions(),
                preferenesStr, userPlan.getBudget());

        PlanVo response = chatClient
                .prompt()
                .system("你是一位旅游行程规划师。")
                .user(prompt)
                .call()
                .entity(PlanVo.class);

        log.info("大模型规划结果: {}", response);

        if (response != null) {
            List<PlanVo.Schedule> scheduleList = response.getSchedules();
            //创建行程
            TravelItinerary travelItinerary = new TravelItinerary();
            List<DailySchedule> dailyScheduleList = new ArrayList<>();
            int currentDay = 1;
            for (PlanVo.Schedule value : scheduleList) {
                //创建每天行程
                DailySchedule dailySchedule = new DailySchedule();
                //创建每天活动(景点)
                ArrayList<AttractionInfo> activities = new ArrayList<>();
                for (int j = 0; j < value.getAttractionIds().size(); j++) {
                    AttractionInfo attraction = new AttractionInfo();
                    attraction.setId(value.getAttractionIds().get(j));
                    attraction.setTip(value.getTips().get(j));
                    //填充其他信息
                    AttractionInfo attractionInfo = attractionService.queryAttractionInfoById(Long.parseLong(attraction.getId()));
                    attraction.setOtherInfo(attractionInfo);
                    activities.add(attraction);
                }
                dailySchedule.setActivities(activities);
                dailySchedule.setDay(currentDay++);
                dailyScheduleList.add(dailySchedule);
            }
            travelItinerary.setDailySchedules(dailyScheduleList);
            sseManager.sendProgress(state.taskId(), "PlanItineraryNode", "路线规划完成，景点选择完毕");
            return Map.of(TravelPlanState.MASTER_PLAN,response.getMasterPlan(),TravelPlanState.ITINERARY_SKELETON, travelItinerary);
        }
        return Map.of();
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlanVo implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        private String masterPlan;
        private List<PlanVo.Schedule> schedules;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Schedule implements Serializable{
            @Serial
            private static final long serialVersionUID = 1L;
            private List<String> attractionIds;
            private List<String> tips;
        }
    }

}
