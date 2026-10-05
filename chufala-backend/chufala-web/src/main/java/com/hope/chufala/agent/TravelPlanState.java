package com.hope.chufala.agent;

import com.hope.chufala.model.dto.UserPlanDTO;
import com.hope.chufala.model.vo.AttractionInfoVO;
import com.hope.chufala.model.vo.TravelItineraryVO;
import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.state.Channel;
import org.bsc.langgraph4j.state.Channels;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * LangGraph4j 的行程规划状态。
 *
 * <p>承载流水线各节点之间传递的数据：{@link #SCHEMA} 声明每个键对应的通道
 * （决定默认值与合并策略），节点返回的 Map 会按此合并回状态。
 *
 * <p>框架要求状态不可变，因此 set 类方法不修改原对象，而是复制 data 后返回新实例。
 *
 * @author 谢光湘
 */
public class TravelPlanState extends AgentState {
    /** 任务 ID */
    public static final String TASK_ID = "taskId";
    //用户输入
//    public static final String DESTINATION = "destination";
//    public static final String PEOPLE = "people";
//    public static final String START_DATE = "startDate";
//    public static final String DAY_NUM = "dayNum";
    /** 用户规划入参 */
    public static final String USER_PLAN = "userPlan";

    //总计划（文字版）
    /** 大模型生成的总计划文本 */
    public static final String MASTER_PLAN = "masterPlan";

    //原始数据
    /** 候选景点列表 */
    public static final String CANDIDATE_ATTRACTIONS = "candidateAttractions";
    /** 候选酒店列表 */
    public static final String CANDIDATE_HOTELS = "candidateHotels";

    //中间结果
    /** 逐步填充的行程骨架 */
    public static final String ITINERARY_SKELETON = "itinerarySkeleton";

    //最终结果
//    public static final String FINAL_ITINERARY = "finalItinerary";

    /** 各状态键的通道定义（默认值与合并策略） */
    public static final Map<String, Channel<?>> SCHEMA = Map.of(
            TASK_ID, Channels.base(() -> ""),
            USER_PLAN,Channels.base(UserPlanDTO::new),
            MASTER_PLAN, Channels.base(() -> ""),
            CANDIDATE_ATTRACTIONS, Channels.base(ArrayList::new),
            CANDIDATE_HOTELS, Channels.base(ArrayList::new),
            ITINERARY_SKELETON, Channels.base(()->new TravelItineraryVO())
    );


    /**
     * 读取任务 ID。
     *
     * @return 任务 ID，缺失时返回空串
     */
    public String taskId(){return this.<String>value(TASK_ID).orElse("");}

    public TravelPlanState(Map<String, Object> initData) {
        super(initData);
    }

//    public String destination(){
//        return this.<String>value(DESTINATION).orElse("");
//    }
//    public void setDestination(String destination){
//        this.data().put(DESTINATION, destination);
//    }
//    public String people(){
//        return this.<String>value(PEOPLE).orElse("");
//    }
//    public void setPeople(String people){
//        this.data().put(PEOPLE, people);
//    }
//    public String startDate(){
//        return this.<String>value(START_DATE).orElse("");
//    }
//    public void setStartDate(String startDate){
//        this.data().put(START_DATE, startDate);
//    }
//    public String dayNum(){
//        return this.<String>value(DAY_NUM).orElse("");
//    }
//    public void setDayNum(String dayNum){
//        this.data().put(DAY_NUM, dayNum);
//    }
    /**
     * 读取用户规划入参。
     *
     * @return 规划入参，缺失时返回空对象
     */
    public UserPlanDTO userPlan(){
        return this.<UserPlanDTO>value(USER_PLAN).orElse(new UserPlanDTO());
    }
    /**
     * 读取大模型生成的总计划文本。
     *
     * @return 总计划文本，缺失时返回空串
     */
    public String masterPlan(){return this.<String>value(MASTER_PLAN).orElse("");}
    /**
     * 读取候选景点列表。
     *
     * @return 可修改的候选景点列表
     */
    public List<AttractionInfoVO> candidateAttractions() {
        // 用 new ArrayList<>() 包装，避免返回不可修改的 List.of()
        return this.<List<AttractionInfoVO>>value(CANDIDATE_ATTRACTIONS)
                .map(ArrayList::new) // 转为可修改的 ArrayList（可选，根据业务需求）
                .orElse(new ArrayList<>());
    }
    /**
     * 读取候选酒店列表。
     *
     * @return 候选酒店列表，缺失时返回空列表
     */
    public List<AttractionInfoVO> candidateHotels(){
        return this.<List<AttractionInfoVO>>value(CANDIDATE_HOTELS).orElse(List.of());
    }

    /**
     * 读取行程骨架。
     *
     * @return 行程骨架，缺失时返回 null
     */
    public TravelItineraryVO itinerarySkeleton(){
        return this.<TravelItineraryVO>value(ITINERARY_SKELETON).orElse(null);
    }
//    public TravelItineraryVO finalItinerary(){
//        return this.<TravelItineraryVO>value(FINAL_ITINERARY).orElse(null);
//    }


    //    public void setCandidateAttractions(List<AttractionInfoVO> candidateAttractions){
//        this.data().put(CANDIDATE_ATTRACTIONS, candidateAttractions);
//    }
    /**
     * 返回写入了新候选景点列表的状态副本（不修改原对象）。
     *
     * @param candidateAttractions 新的候选景点列表
     * @return 新的状态实例
     */
// 关键修改：set 方法不再直接修改 data()，而是创建新的 TravelPlanState 实例
    public TravelPlanState setCandidateAttractions(List<AttractionInfoVO> candidateAttractions) {
        // 1. 复制原有状态数据（创建可修改的 Map）
        Map<String, Object> newData = new HashMap<>(this.data());
        // 2. 放入新的候选景点数据（确保是可修改的 List，这里用 ArrayList 包装）
        newData.put(CANDIDATE_ATTRACTIONS, new ArrayList<>(candidateAttractions));
        // 3. 返回新的状态实例（框架要求不可变状态，通过新实例传递修改后的数据）
        return new TravelPlanState(newData);
    }
}
