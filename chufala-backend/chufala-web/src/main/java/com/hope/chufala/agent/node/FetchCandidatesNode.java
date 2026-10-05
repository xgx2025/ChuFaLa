package com.hope.chufala.agent.node;
//获取候选景点和酒店

import com.hope.chufala.infra.SseManager;
import com.hope.chufala.model.vo.AttractionInfoVO;
import com.hope.chufala.model.vo.HotelInfoVO;
import com.hope.chufala.service.IAttractionService;
import com.hope.chufala.service.IHotelService;
import com.hope.chufala.agent.TravelPlanState;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.NodeAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 候选数据获取节点（流水线首节点）。
 *
 * <p>按目的地城市取出候选景点与候选酒店，写入状态供后续节点使用。
 *
 * @author 谢光湘
 */
@Slf4j
@Component
public class FetchCandidatesNode implements NodeAction<TravelPlanState> {
    @Autowired
    private IAttractionService attractionService;
    @Autowired
    private IHotelService hotelService;
    @Autowired
    private SseManager sseManager;

    /**
     * 查询目的地的候选景点与酒店。
     *
     * @param state 规划状态（需含 userPlan.destination）
     * @return 含候选景点与候选酒店的状态增量
     * @throws Exception 节点执行异常
     */
    @Override
    public Map<String, Object> apply(TravelPlanState state) throws Exception {
        log.info("===FetchCandidatesNode节点===");
        log.info("获取候选景点和酒店");
        sseManager.sendProgress(state.taskId(),"fetchCandidate", "正在查询景点和酒店...");
        String destination = state.userPlan().getDestination();
        List<AttractionInfoVO> attractions = attractionService.queryAttractionByCity(destination);
        List<HotelInfoVO> hotels = hotelService.findHotelSimpleByCity(destination);
        sseManager.sendProgress(state.taskId(), "fetchCandidate", "查询完成");
        return Map.of(TravelPlanState.CANDIDATE_ATTRACTIONS, attractions, TravelPlanState.CANDIDATE_HOTELS, hotels);
    }
}
