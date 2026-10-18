package com.hope.chufala.agent.node;

import com.hope.chufala.agent.TravelPlanState;
import com.hope.chufala.infra.stream.PlanProgressStreamManager;
import com.hope.chufala.model.dto.UserPlanDTO;
import com.hope.chufala.model.vo.AttractionInfoVO;
import com.hope.chufala.model.vo.DailyScheduleVO;
import com.hope.chufala.model.vo.HotelInfoVO;
import com.hope.chufala.model.vo.TravelItineraryVO;
import com.hope.chufala.service.IHotelService;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class IncompleteModelOutputTest {
    private final UserPlanDTO plan = new UserPlanDTO("杭州", 2,
            LocalDate.of(2026, 10, 20), 2, List.of(), 2000.0);

    @Test
    void fewerWeatherDaysLeaveMissingDaysUnspecified() throws Exception {
        ChatClient client = mock(ChatClient.class, RETURNS_DEEP_STUBS);
        PlanProgressStreamManager progress = mock(PlanProgressStreamManager.class);
        WeatherInquiryNode node = new WeatherInquiryNode();
        ReflectionTestUtils.setField(node, "chatClient", client);
        ReflectionTestUtils.setField(node, "planProgressStreamManager", progress);
        when(client.prompt().user(anyString()).call().entity(WeatherInquiryNode.DailyWeather.class))
                .thenReturn(new WeatherInquiryNode.DailyWeather(List.of(
                        new WeatherInquiryNode.DailyWeather.WeatherInfo("2026-10-20", "晴天", "20℃"))));
        TravelItineraryVO itinerary = itinerary(2);

        node.apply(state(itinerary));

        assertEquals("晴天", itinerary.getDailySchedules().get(0).getWeather());
        assertNull(itinerary.getDailySchedules().get(1).getWeather());
        verify(progress).weatherUpdated(eq("task-1"), anyString(), eq("已核对 1 天的天气"),
                eq(List.of(new com.hope.chufala.infra.task.RedisPlanTaskStore.WeatherPreview(1, "晴天", "20℃"))));
    }

    @Test
    void fewerHotelDaysDoNotInterruptBudgetStage() throws Exception {
        ChatClient client = mock(ChatClient.class, RETURNS_DEEP_STUBS);
        IHotelService hotels = mock(IHotelService.class);
        PlanProgressStreamManager progress = mock(PlanProgressStreamManager.class);
        RecommendHotelNode node = new RecommendHotelNode();
        ReflectionTestUtils.setField(node, "chatClient", client);
        ReflectionTestUtils.setField(node, "hotelService", hotels);
        ReflectionTestUtils.setField(node, "planProgressStreamManager", progress);
        when(client.prompt().system(anyString()).user(anyString()).call()
                .entity(RecommendHotelNode.HotelList.class))
                .thenReturn(new RecommendHotelNode.HotelList(List.of(), List.of(
                        new RecommendHotelNode.DailyHotel(List.of("1"), List.of("酒店")))));
        when(hotels.findHotelSimpleById(1L)).thenReturn(new HotelInfoVO());
        TravelItineraryVO itinerary = itinerary(2);

        node.apply(state(itinerary));

        assertEquals(1, itinerary.getDailySchedules().get(0).getRecommendHotels().size());
        assertNull(itinerary.getDailySchedules().get(1).getRecommendHotels());
        verify(progress).stageCompleted(eq("task-1"), eq("stays"), anyString(), eq("已为 1 天安排酒店候选"));
    }

    @Test
    void singleBudgetCategoryStillProducesFiniteTotal() throws Exception {
        ChatClient client = mock(ChatClient.class, RETURNS_DEEP_STUBS);
        PlanProgressStreamManager progress = mock(PlanProgressStreamManager.class);
        CalculateBudgetNode node = new CalculateBudgetNode();
        ReflectionTestUtils.setField(node, "chatClient", client);
        ReflectionTestUtils.setField(node, "planProgressStreamManager", progress);
        when(client.prompt().system(anyString()).user(anyString()).call()
                .entity(CalculateBudgetNode.TravelBudget.class))
                .thenReturn(new CalculateBudgetNode.TravelBudget(List.of(
                        new CalculateBudgetNode.TravelBudget.Budget("交通费用", 100.0))));
        TravelItineraryVO itinerary = itinerary(1);
        AttractionInfoVO attraction = new AttractionInfoVO();
        attraction.setPrice(50.0);
        itinerary.getDailySchedules().get(0).setActivities(List.of(attraction));

        node.apply(state(itinerary));

        assertEquals(200.0, itinerary.getBudgetSummary().getTotal());
        assertEquals(3, itinerary.getBudgetSummary().getBreakdown().size());
        assertTrue(Double.isFinite(itinerary.getBudgetSummary().getTotal()));
        verify(progress).stageCompleted(eq("task-1"), eq("budget"), anyString(), anyString());
    }

    private TravelPlanState state(TravelItineraryVO itinerary) {
        return new TravelPlanState(Map.of(TravelPlanState.TASK_ID, "task-1",
                TravelPlanState.USER_PLAN, plan, TravelPlanState.MASTER_PLAN, "测试行程",
                TravelPlanState.ITINERARY_SKELETON, itinerary));
    }

    private TravelItineraryVO itinerary(int days) {
        List<DailyScheduleVO> schedules = new ArrayList<>();
        for (int day = 1; day <= days; day++) {
            DailyScheduleVO schedule = new DailyScheduleVO();
            schedule.setDay(day);
            schedule.setActivities(List.of());
            schedules.add(schedule);
        }
        TravelItineraryVO itinerary = new TravelItineraryVO();
        itinerary.setDailySchedules(schedules);
        return itinerary;
    }
}
