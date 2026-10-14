package com.hope.chufala.agent.node;

import com.hope.chufala.model.dto.UserPlanDTO;
import com.hope.chufala.model.vo.AttractionInfoVO;
import com.hope.chufala.model.vo.DailyScheduleVO;
import com.hope.chufala.model.vo.TravelItineraryVO;
import com.hope.chufala.infra.SseManager;
import com.hope.chufala.service.IAttractionService;
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

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * 行程骨架生成节点。
 *
 * <p>由 deepseek 依据候选景点与用户偏好，为每天挑选景点并给出游玩提示，同时生成
 * 文字版总计划（masterPlan）；本地再按景点 ID 补全展示信息，组装成 itinerarySkeleton。
 *
 * @author 谢光湘
 */
@Slf4j
@Component
public class PlanItineraryNode implements NodeAction<TravelPlanState> {
    @Resource(name = "deepseek")
    private ChatClient chatClient;
    @Autowired
    private IAttractionService attractionService;
    @Autowired
    private SseManager sseManager;


    /**
     * 生成逐日行程骨架与总计划。
     *
     * @param state 规划状态（需含 userPlan 与候选景点）
     * @return 含 masterPlan 与 itinerarySkeleton 的状态增量；模型未返回日程时抛出异常
     */
    @Override
    public Map<String, Object> apply(TravelPlanState state) {
        log.info("===PlanItineraryNode节点===");
        log.info("开始规划行程");
        sseManager.stageStarted(state.taskId(), "routes", "正在编排每日路线和景点");
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
        1. 为每天选择景点，输出景点列表以及对应的 tip；每一天的 tips 数量必须与 attractionIds 数量相同，顺序一一对应。
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

        if (response != null && response.getSchedules() != null && !response.getSchedules().isEmpty()) {
            List<PlanVo.Schedule> scheduleList = response.getSchedules();
            //创建行程
            TravelItineraryVO travelItinerary = new TravelItineraryVO();
            List<DailyScheduleVO> dailyScheduleList = new ArrayList<>();
            int currentDay = 1;
            for (PlanVo.Schedule value : scheduleList) {
                if (value == null || value.getAttractionIds() == null) {
                    log.warn("第 {} 天的景点列表缺失，保留空白日程", currentDay);
                    DailyScheduleVO emptyDay = new DailyScheduleVO();
                    emptyDay.setDay(currentDay++);
                    emptyDay.setActivities(List.of());
                    dailyScheduleList.add(emptyDay);
                    continue;
                }
                //创建每天行程
                DailyScheduleVO dailySchedule = new DailyScheduleVO();
                //创建每天活动(景点)
                ArrayList<AttractionInfoVO> activities = new ArrayList<>();
                List<String> tips = value.getTips();
                if (tips == null || tips.size() != value.getAttractionIds().size()) {
                    log.warn("第 {} 天景点与游玩提示数量不一致：景点 {} 个，提示 {} 条；缺失提示留空",
                            currentDay, value.getAttractionIds().size(), tips == null ? 0 : tips.size());
                }
                for (int j = 0; j < value.getAttractionIds().size(); j++) {
                    AttractionInfoVO attraction = new AttractionInfoVO();
                    attraction.setId(value.getAttractionIds().get(j));
                    attraction.setTip(tips != null && j < tips.size() && tips.get(j) != null ? tips.get(j) : "");
                    //填充其他信息
                    AttractionInfoVO attractionInfo;
                    try {
                        attractionInfo = attractionService.queryAttractionInfoById(Long.parseLong(attraction.getId()));
                    } catch (NumberFormatException e) {
                        log.warn("忽略模型返回的无效景点 ID：{}", attraction.getId());
                        continue;
                    }
                    if (attractionInfo == null) {
                        log.warn("忽略不存在的景点 ID：{}", attraction.getId());
                        continue;
                    }
                    attraction.setOtherInfo(attractionInfo);
                    activities.add(attraction);
                }
                dailySchedule.setActivities(activities);
                dailySchedule.setDay(currentDay++);
                dailyScheduleList.add(dailySchedule);
            }
            travelItinerary.setDailySchedules(dailyScheduleList);
            sseManager.stageCompleted(state.taskId(), "routes", "每日路线已编排",
                    "已安排 " + dailyScheduleList.size() + " 天的游览路线");
            return Map.of(TravelPlanState.MASTER_PLAN,
                    response.getMasterPlan() == null ? "" : response.getMasterPlan(),
                    TravelPlanState.ITINERARY_SKELETON, travelItinerary);
        }
        throw new IllegalStateException("模型未返回可用的每日行程");
    }


    /** 大模型返回的行程规划结果 */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlanVo implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        /** 文字版总计划 */
        private String masterPlan;
        /** 逐日安排 */
        private List<PlanVo.Schedule> schedules;

        /** 单日安排：景点 ID 与对应游玩提示 */
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Schedule implements Serializable{
            @Serial
            private static final long serialVersionUID = 1L;
            /** 当日景点 ID 列表 */
            private List<String> attractionIds;
            /** 与景点一一对应的游玩提示 */
            private List<String> tips;
        }
    }

}
