package com.hope.chufala.agent.node;


import com.hope.chufala.common.util.Gcj02DistanceCalculator;
import com.hope.chufala.model.vo.AttractionInfo;
import com.hope.chufala.model.vo.DailySchedule;
import com.hope.chufala.model.vo.TravelItinerary;
import com.hope.chufala.service.IAttractionService;
import com.hope.chufala.agent.TravelPlanState;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.NodeAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class DistanceCalculationNode implements NodeAction<TravelPlanState> {
    @Autowired
    private IAttractionService attractionService;

    @Override
    public Map<String, Object> apply(TravelPlanState state) throws Exception {
        log.info("===DistanceCalculateNode节点===");
        log.info("开始计算景点之间的距离和耗时");
        //获取景点列表
        TravelItinerary travelItinerary = state.itinerarySkeleton();
        List<DailySchedule> dailyScheduleList = travelItinerary.getDailySchedules();
        //填充地理信息
        for (int i = 0; i < dailyScheduleList.size(); i++) {
            List<AttractionInfo> activities = dailyScheduleList.get(i).getActivities();
            for (int j = 0; j < activities.size(); j++) {
                AttractionInfo attractionInfo = activities.get(j);
                Double[] position = attractionService.queryAttractionPositionById(Long.valueOf(attractionInfo.getId()));
                attractionInfo.setPosition(position);
                DecimalFormat df = new DecimalFormat("#.00");
                DecimalFormat df2 = new DecimalFormat("#");
                if (j!=0){
                    AttractionInfo preAttractionInfo = activities.get(j - 1);
                    Double[] position2 = attractionService.queryAttractionPositionById(Long.valueOf(preAttractionInfo.getId()));
                    Double distance = Gcj02DistanceCalculator.calculateDistance(position, position2);
                    preAttractionInfo.setDistance(df.format(distance)+"km");
                    //计算驾车时间
                    Double drivingTime = distance / 10;  //10km/h  10/60min 1/6min
                    if (drivingTime < 1 / 6.0){
                        preAttractionInfo.setDrivingTime("1分钟");
                    }else if(drivingTime < 1) {
                        preAttractionInfo.setDrivingTime(df2.format(drivingTime*60)+"分钟");
                    }else{
                        preAttractionInfo.setDrivingTime(df2.format(drivingTime)+"小时");
                    }
                }
            }
        }
        travelItinerary.setDailySchedules(dailyScheduleList);
        return TravelPlanState.updateState(state, Map.of(TravelPlanState.ITINERARY_SKELETON, travelItinerary), TravelPlanState.SCHEMA);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GeoInfo {
        private List<String> name;
        private List<Double> lng;
        private List<Double> lat;
        private List<String> distance;
        private List<String> drivingTime;
    }

}
