package com.hope.chufala.agent.node;

import com.hope.chufala.infra.SseManager;
import com.hope.chufala.model.dto.UserPlanDTO;
import com.hope.chufala.model.vo.DailyScheduleVO;
import com.hope.chufala.model.vo.TravelItineraryVO;
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
import java.util.List;
import java.util.Map;

/**
 * 天气查询节点。
 *
 * <p>由 deepseek 按目的地与出发日期推断逐日天气，天气词限定为
 * 【晴天、多云、阴天、小雨、中雨、大雨】，写入对应日程的 weather 与 temperature。
 *
 * @author 谢光湘
 */
@Slf4j
@Component
public class WeatherInquiryNode implements NodeAction<TravelPlanState> {
    @Resource(name="deepseek")
    private ChatClient chatClient;
    @Autowired
    private SseManager sseManager;

    /**
     * 查询并填充逐日天气。
     *
     * @param state 规划状态（需含 itinerarySkeleton）
     * @return 含更新后 itinerarySkeleton 的状态增量；模型无返回时为空 Map
     * @throws Exception 节点执行异常
     */
    @Override
    public Map<String, Object> apply(TravelPlanState state) throws Exception {
        log.info("===WeatherInquiryNode节点===");
        log.info("===开始查询天气===");
        sseManager.sendProgress(state.taskId(), "WeatherInquiryNode", "正在查询天气...");
        UserPlanDTO userPlan = state.userPlan();
        String prompt = """
        请查询%s地区从%s开始以来连续%d天的天气（天气情况和最低温与最高温）。天气词请使用【晴天、多云、阴天、小雨、中雨、大雨】
        """.formatted(userPlan.getDestination(), userPlan.getStartDate(),userPlan.getDayNum());
        DailyWeather response =  chatClient.prompt()
                .user(prompt)
                .call()
                .entity(DailyWeather.class);
        log.info("天气查询结果：{}",response);
        if (response != null) {
            TravelItineraryVO itinerary = state.itinerarySkeleton();
            //获取未来的天气
            List<DailyWeather.WeatherInfo> weatherInfos = response.getWeatherInfos();
            List<DailyScheduleVO> dailyScheduleList = itinerary.getDailySchedules();

            for (int i = 0; i < dailyScheduleList.size(); i++) {
                DailyScheduleVO schedule = dailyScheduleList.get(i);
                DailyWeather.WeatherInfo weather = weatherInfos.get(i);
                schedule.setWeather(weather.getWeather());
                schedule.setTemperature(weather.getTemperature());
            }
            itinerary.setDailySchedules(dailyScheduleList);

            sseManager.sendProgress(state.taskId(), "WeatherInquiryNode", "天气查询完成");
            return Map.of(TravelPlanState.ITINERARY_SKELETON, itinerary);
        }
        return Map.of();
    }


    /** 大模型返回的天气结果 */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyWeather implements Serializable {
        /** 逐日天气 */
        private List<DailyWeather.WeatherInfo> weatherInfos;

        /** 单日天气 */
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class WeatherInfo implements Serializable {
            @Serial
            private static final long serialVersionUID = 1L;
            /** 日期 */
            private String date;
            /** 天气描述 */
            private String weather;
            /** 温度描述 */
            private String temperature;
        }
    }
}
