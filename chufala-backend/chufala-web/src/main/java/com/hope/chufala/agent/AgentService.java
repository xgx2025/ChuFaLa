package com.hope.chufala.agent;

import com.hope.chufala.service.IPlanHistoryService;
import com.hope.chufala.agent.node.*;
import com.hope.chufala.agent.AgentTask;
import com.hope.chufala.infra.SseManager;
import com.hope.chufala.infra.TaskQueue;
import com.hope.chufala.model.dto.UserPlanDTO;
import com.hope.chufala.agent.TravelPlanState;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.StateGraph;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * 行程规划编排服务。
 *
 * <p>用 LangGraph4j 把六个节点串成一条固定流水线，在专用线程池 planExecutor 上异步执行：
 * <pre>
 *   START → fetchCandidate（查候选景点/酒店）
 *         → recommendAttraction（LLM 选景点、生成总计划）
 *         → weatherInQuery（LLM 查天气）
 *         → distanceCalculate（算景点间距离与驾车耗时）
 *         → recommendHotel（LLM 推荐每日酒店）
 *         → calculateBudget（LLM 估交通餐饮 + 本地算门票住宿）
 *         → END
 * </pre>
 * 每个节点通过 SseManager 推送进度；成功后写入 PlanHistory 并推送完成事件。
 *
 * @author 谢光湘
 */
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

    /**
     * 异步执行一次行程规划任务。
     *
     * <p>节点图按固定拓扑编译后执行；任务不存在时直接推送失败并退出（避免后续 NPE），
     * 任一环节抛异常都会把任务置为 FAILED 并推送失败进度。
     *
     * @param taskId 任务 ID（需已存在于 TaskQueue）
     * @param userId 提交用户 ID，用于归属校验与历史落库
     */
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
            sseManager.failTask(taskId, "任务不存在或已过期");
            return;
        }
        if (!taskQueue.startTask(taskId)) {
            log.warn("任务状态不允许开始执行，taskId={}", taskId);
            sseManager.failTask(taskId, "任务不存在或状态不允许执行");
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
               var itinerary = result.get().itinerarySkeleton();
               // 历史落库成功后再持久化终态并通知前端，避免先发完成事件后落库失败。
               planHistoryService.addHistory(userId, createTime, userPlan, result.get().masterPlan(), itinerary);
               if (!taskQueue.completeTask(taskId, itinerary)) {
                   throw new IllegalStateException("任务不存在或状态不允许完成：" + taskId);
               }
               sseManager.completeTask(taskId, itinerary);
               log.info("旅游行程规划结果{}", itinerary);
           } else {
               throw new RuntimeException("任务执行未返回结果");
           }
        } catch (Exception e) {
            log.error("旅游行程规划失败！", e);
            try {
                taskQueue.failTask(taskId, "旅游行程规划失败！");
            } catch (Exception persistenceError) {
                log.error("持久化任务失败状态时出错，taskId={}", taskId, persistenceError);
            }
            sseManager.failTask(taskId, "旅游行程规划失败！");
        }
    }
}
