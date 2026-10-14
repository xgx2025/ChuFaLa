package com.hope.chufala.agent.node;

import com.hope.chufala.agent.TravelPlanState;
import com.hope.chufala.infra.SseManager;
import com.hope.chufala.model.dto.UserPlanDTO;
import com.hope.chufala.model.vo.AttractionInfoVO;
import com.hope.chufala.model.vo.TravelItineraryVO;
import com.hope.chufala.service.IAttractionService;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PlanItineraryNodeTest {
    @Test
    void missingModelTipsDoNotAbortRoutePlanning() {
        ChatClient chatClient = mock(ChatClient.class, RETURNS_DEEP_STUBS);
        IAttractionService attractions = mock(IAttractionService.class);
        SseManager progress = mock(SseManager.class);
        PlanItineraryNode node = new PlanItineraryNode();
        ReflectionTestUtils.setField(node, "chatClient", chatClient);
        ReflectionTestUtils.setField(node, "attractionService", attractions);
        ReflectionTestUtils.setField(node, "sseManager", progress);

        var modelResult = new PlanItineraryNode.PlanVo("四日行程", List.of(
                new PlanItineraryNode.PlanVo.Schedule(List.of("1", "2", "3", "4"),
                        List.of("上午游览", "午后游览", "傍晚游览"))));
        when(chatClient.prompt().system(anyString()).user(anyString()).call()
                .entity(PlanItineraryNode.PlanVo.class)).thenReturn(modelResult);
        when(attractions.queryAttractionInfoById(anyLong())).thenAnswer(call -> {
            AttractionInfoVO attraction = new AttractionInfoVO();
            attraction.setName("景点 " + call.getArgument(0));
            return attraction;
        });
        UserPlanDTO plan = new UserPlanDTO("杭州", 2, LocalDate.of(2026, 10, 20),
                1, List.of("美食"), 2000.0);
        TravelPlanState state = new TravelPlanState(Map.of(
                TravelPlanState.TASK_ID, "task-1", TravelPlanState.USER_PLAN, plan,
                TravelPlanState.CANDIDATE_ATTRACTIONS, List.of()));

        Map<String, Object> result = node.apply(state);

        TravelItineraryVO itinerary = (TravelItineraryVO) result.get(TravelPlanState.ITINERARY_SKELETON);
        assertEquals(4, itinerary.getDailySchedules().get(0).getActivities().size());
        assertEquals("傍晚游览", itinerary.getDailySchedules().get(0).getActivities().get(2).getTip());
        assertEquals("", itinerary.getDailySchedules().get(0).getActivities().get(3).getTip());
        verify(progress).stageCompleted(eq("task-1"), eq("routes"), anyString(), anyString());
    }
}
