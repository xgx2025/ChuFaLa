package com.hope.chufala.agent.node;

import com.hope.chufala.infra.SseManager;
import com.hope.chufala.model.dto.UserPlanDTO;
import com.hope.chufala.model.vo.DailyScheduleVO;
import com.hope.chufala.model.vo.HotelInfoVO;
import com.hope.chufala.model.vo.TravelItineraryVO;
import com.hope.chufala.service.IHotelService;
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
 * 酒店推荐节点。
 *
 * <p>由豆包模型按行程与候选酒店为每天挑选若干酒店（每天 3 个左右、不超过 5 个），
 * 本地再按酒店 ID 补全信息，写入对应日程的 recommendHotels。
 *
 * @author 谢光湘
 */
@Slf4j
@Component
public class RecommendHotelNode implements NodeAction<TravelPlanState>{
    @Resource(name = "doubao")
    private ChatClient chatClient;
    @Autowired
    private IHotelService hotelService;
    @Autowired
    private SseManager sseManager;


    /**
     * 为每日行程推荐酒店并写回行程骨架。
     *
     * @param state 规划状态（需含 itinerarySkeleton 与候选酒店）
     * @return 含更新后 itinerarySkeleton 的状态增量
     * @throws Exception 节点执行异常
     */
    @Override
    public Map<String, Object> apply(TravelPlanState state) throws Exception {
        log.info("===RecommendHotelNode节点===");
        log.info("开始推荐酒店");
        sseManager.sendProgress(state.taskId(),"RecommendHotelNode", "正在推荐酒店...");
        UserPlanDTO userPlan = state.userPlan();
        //查询酒店列表
        String userPrompt = """
        请为%s地区从%s开始，持续%d天的行程推荐酒店(每天的酒店推荐建议3个左右，最多不要超过5个)。请快速推荐！
        这是我的行程安排%s。
        可供选择的酒店列表%s（包含酒店id和name）。
        按照日期进行选择，每日可以推荐多个酒店，填充data、id和name字段(这些信息必须来自用户提供的信息)，最后严格按照格式返回纯json格式。
        """.formatted(userPlan.getDestination(),userPlan.getStartDate(),userPlan.getDayNum(),state.masterPlan(),state.candidateHotels());
        HotelList response =chatClient.prompt()
                .system("根据用户行程和要求推荐酒店")
                .user(userPrompt)
                .call()
                .entity(HotelList.class);
        log.info("大模型推荐结果: {}", response);

        TravelItineraryVO itinerary = state.itinerarySkeleton();
        List<DailyScheduleVO> dailyScheduleList = itinerary.getDailySchedules();
        if (response != null) {
            for (int i = 0; i < dailyScheduleList.size(); i++) {
                DailyScheduleVO dailySchedule = dailyScheduleList.get(i);
                DailyHotel  dailyHotel = response.getDailyHotels().get(i);
                int num = response.dailyHotels.get(i).ids.size();
                List<HotelInfoVO> recommendHotels = new ArrayList<>();
                for (int j = 0; j < num; j++) {
                    HotelInfoVO hotelInfo = hotelService.findHotelSimpleById(Long.parseLong(dailyHotel.ids.get(j)));
                    recommendHotels.add(hotelInfo);
                }
                dailySchedule.setRecommendHotels(recommendHotels);
                dailyScheduleList.set(i, dailySchedule);
            }
            itinerary.setDailySchedules(dailyScheduleList);
        }
        sseManager.sendProgress(state.taskId(), "RecommendHotelNode", "酒店推荐完成");
        log.info("酒店推荐结果: {}", itinerary);
        return Map.of(TravelPlanState.ITINERARY_SKELETON,itinerary);
    }


    /** 大模型返回的酒店推荐结果 */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HotelList {
        /** 模型补充的说明文本 */
        private List<String> data;
        /** 逐日推荐酒店 */
        private List<DailyHotel> dailyHotels;
    }

    /** 单日推荐酒店（ID 与名称成对） */
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        static class DailyHotel{
        /** 酒店 ID 列表 */
        private List<String> ids;
        /** 酒店名称列表 */
        private List<String> names;
    }
}
