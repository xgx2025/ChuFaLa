package com.hope.chufala.service.agent.node;

import com.hope.chufala.domain.agent.SseManager;
import com.hope.chufala.domain.dto.UserPlanDTO;
import com.hope.chufala.domain.vo.DailySchedule;
import com.hope.chufala.domain.vo.TravelItinerary;
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
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class WeatherInquiryNode implements NodeAction<TravelPlanState> {
    @Resource(name="deepseek")
    private ChatClient chatClient;
    @Autowired
    private SseManager sseManager;

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
            TravelItinerary itinerary = state.itinerarySkeleton();
            //获取未来的天气
            List<DailyWeather.WeatherInfo> weatherInfos = response.getWeatherInfos();
            List<DailySchedule> dailyScheduleList = itinerary.getDailySchedules();

            for (int i = 0; i < dailyScheduleList.size(); i++) {
                DailySchedule schedule = dailyScheduleList.get(i);
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


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyWeather implements Serializable {
        private List<DailyWeather.WeatherInfo> weatherInfos;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class WeatherInfo implements Serializable {
            @Serial
            private static final long serialVersionUID = 1L;
            private String date;
            private String weather;
            private String temperature;
        }
    }
}
