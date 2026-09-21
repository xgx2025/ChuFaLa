package com.hope.chufala.agent;

import com.hope.chufala.model.dto.UserPlanDTO;
import com.hope.chufala.model.vo.AttractionInfo;
import com.hope.chufala.model.vo.TravelItinerary;
import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.state.Channel;
import org.bsc.langgraph4j.state.Channels;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TravelPlanState extends AgentState {
    public static final String TASK_ID = "taskId";
    //用户输入
//    public static final String DESTINATION = "destination";
//    public static final String PEOPLE = "people";
//    public static final String START_DATE = "startDate";
//    public static final String DAY_NUM = "dayNum";
    public static final String USER_PLAN = "userPlan";

    //总计划（文字版）
    public static final String MASTER_PLAN = "masterPlan";

    //原始数据
    public static final String CANDIDATE_ATTRACTIONS = "candidateAttractions";
    public static final String CANDIDATE_HOTELS = "candidateHotels";

    //中间结果
    public static final String ITINERARY_SKELETON = "itinerarySkeleton";

    //最终结果
//    public static final String FINAL_ITINERARY = "finalItinerary";

    public static final Map<String, Channel<?>> SCHEMA = Map.of(
            TASK_ID, Channels.base(() -> ""),
            USER_PLAN,Channels.base(UserPlanDTO::new),
            MASTER_PLAN, Channels.base(() -> ""),
            CANDIDATE_ATTRACTIONS, Channels.base(ArrayList::new),
            CANDIDATE_HOTELS, Channels.base(ArrayList::new),
            ITINERARY_SKELETON, Channels.base(()->new TravelItinerary())
    );


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
    public UserPlanDTO userPlan(){
        return this.<UserPlanDTO>value(USER_PLAN).orElse(new UserPlanDTO());
    }
    public String masterPlan(){return this.<String>value(MASTER_PLAN).orElse("");}
    public List<AttractionInfo> candidateAttractions() {
        // 用 new ArrayList<>() 包装，避免返回不可修改的 List.of()
        return this.<List<AttractionInfo>>value(CANDIDATE_ATTRACTIONS)
                .map(ArrayList::new) // 转为可修改的 ArrayList（可选，根据业务需求）
                .orElse(new ArrayList<>());
    }
    public List<AttractionInfo> candidateHotels(){
        return this.<List<AttractionInfo>>value(CANDIDATE_HOTELS).orElse(List.of());
    }

    public TravelItinerary itinerarySkeleton(){
        return this.<TravelItinerary>value(ITINERARY_SKELETON).orElse(null);
    }
//    public TravelItinerary finalItinerary(){
//        return this.<TravelItinerary>value(FINAL_ITINERARY).orElse(null);
//    }


    //    public void setCandidateAttractions(List<AttractionInfo> candidateAttractions){
//        this.data().put(CANDIDATE_ATTRACTIONS, candidateAttractions);
//    }
// 关键修改：set 方法不再直接修改 data()，而是创建新的 TravelPlanState 实例
    public TravelPlanState setCandidateAttractions(List<AttractionInfo> candidateAttractions) {
        // 1. 复制原有状态数据（创建可修改的 Map）
        Map<String, Object> newData = new HashMap<>(this.data());
        // 2. 放入新的候选景点数据（确保是可修改的 List，这里用 ArrayList 包装）
        newData.put(CANDIDATE_ATTRACTIONS, new ArrayList<>(candidateAttractions));
        // 3. 返回新的状态实例（框架要求不可变状态，通过新实例传递修改后的数据）
        return new TravelPlanState(newData);
    }
}
