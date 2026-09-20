package com.hope.chufala.service.agent.service;

import com.hope.chufala.service.IPlanHistoryService;
import com.hope.chufala.service.agent.node.*;
import com.hope.chufala.domain.agent.AgentTask;
import com.hope.chufala.domain.agent.SseManager;
import com.hope.chufala.domain.agent.TaskQueue;
import com.hope.chufala.domain.agent.TaskStatus;
import com.hope.chufala.domain.dto.UserPlanDTO;
import com.hope.chufala.service.agent.TravelPlanState;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.StateGraph;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

@Slf4j
@Service
public class AgentService {
    //使用大模型的节点由ICO容器自动管理
    @Autowired
    private FetchCandidatesNode fetchCandidatesNode;
    @Autowired
    private PlanItineraryNode planItineraryNode;
    @Autowired
    private RecommendHotelNode recommendHotelNode;
    @Autowired
    private WeatherInquiryNode weatherInquiryNode;
    @Autowired
    private DistanceCalculationNode distanceCalculationNode;
    @Autowired
    private CalculateBudgetNode calculateBudgetNode;
    @Autowired
    private TaskQueue taskQueue;
    @Autowired
    private SseManager sseManager;
    @Autowired
    private IPlanHistoryService planHistoryService;

    //异步执行任务（指定专用线程池，避免与发信等任务互相抢占线程）
    @Async("planExecutor")
    public void planTravel(String taskId,Long userId){
        //获取开始执行的时间
        LocalDateTime createTime = LocalDateTime.now();
        long startTime = System.currentTimeMillis();
        AgentTask task = taskQueue.getTask(taskId);
        if (task == null) {
            // 任务不存在或已被清理，直接失败退出，避免后续 task.getUserPlan() 抛 NPE
            log.error("任务不存在或已过期，taskId={}", taskId);
            sseManager.sendProgress(taskId, "failed", "任务不存在或已过期");
            return;
        }
        UserPlanDTO userPlan = task.getUserPlan();
        try {
           var graph = new StateGraph<TravelPlanState>(TravelPlanState.SCHEMA, TravelPlanState::new)
                   .addNode("fetchCandidate", node_async(fetchCandidatesNode))
                   .addNode("recommendAttraction", node_async(planItineraryNode))
                   .addNode("recommendHotel", node_async(recommendHotelNode))
                   .addNode("weatherInQuery", node_async(weatherInquiryNode))
                   .addNode("distanceCalculate", node_async(distanceCalculationNode))
                   .addNode("calculateBudget", node_async(calculateBudgetNode))
                   .addEdge(StateGraph.START, "fetchCandidate")
                   .addEdge("fetchCandidate", "recommendAttraction")
                   .addEdge("recommendAttraction", "weatherInQuery")
                   .addEdge("weatherInQuery", "distanceCalculate")
                   .addEdge("distanceCalculate", "recommendHotel")
                   .addEdge("recommendHotel", "calculateBudget")
                   .addEdge("calculateBudget", StateGraph.END)
                   .compile();

           Map<String,Object> initialState = Map.of(
                TravelPlanState.TASK_ID,taskId,
                TravelPlanState.USER_PLAN,userPlan,
                TravelPlanState.MASTER_PLAN,userPlan
           );
           Optional<TravelPlanState> result = graph.invoke(initialState);

           // 4. 处理最终结果
           if (result.isPresent()) {
               //获取规划完成时的时间
               long endTime = System.currentTimeMillis();
               long costMillis = endTime - startTime;
               log.info("旅游规划完成，耗时 {} 毫秒（约 {} 秒）", costMillis, String.format("%.2f", costMillis / 1000.0));
               //向用户推送旅游规划结果
               task.setResult(result.get().itinerarySkeleton());
               task.setStatus(TaskStatus.COMPLETED);
               sseManager.completeTask(taskId, task.getResult());
               log.info("旅游行程规划结果{}", task.getResult());
               //将规划结果保存到数据库
               planHistoryService.addHistory(userId,createTime,userPlan,result.get().masterPlan(),task.getResult());
           } else {
               throw new RuntimeException("任务执行未返回结果");
           }
        } catch (Exception e) {
            log.error("旅游行程规划失败！", e);
            task.setStatus(TaskStatus.FAILED);
            sseManager.sendProgress(taskId, "failed", "旅游行程规划失败！");
        }
    }
}
