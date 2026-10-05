package com.hope.chufala.agent.node;


import com.hope.chufala.common.util.Gcj02DistanceCalculator;
import com.hope.chufala.model.vo.AttractionInfoVO;
import com.hope.chufala.model.vo.DailyScheduleVO;
import com.hope.chufala.model.vo.TravelItineraryVO;
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

/**
 * 景点距离与驾车耗时计算节点。
 *
 * <p>逐个补全景点坐标，并计算「相邻景点」之间的距离与驾车时间；结果写在
 * <b>前一个</b>景点上，表示「由此前往下一个景点」的行程段。
 * 驾车时间按固定 10km/h 估算，不足 1 分钟时显示为 1 分钟。
 *
 * @author 谢光湘
 */
@Slf4j
@Component
public class DistanceCalculationNode implements NodeAction<TravelPlanState> {
    @Autowired
    private IAttractionService attractionService;

    /**
     * 计算每日相邻景点间的距离与驾车耗时，并写回行程骨架。
     *
     * @param state 规划状态（需含 itinerarySkeleton）
     * @return 含更新后 itinerarySkeleton 的状态
     * @throws Exception 节点执行异常
     */
    @Override
    public Map<String, Object> apply(TravelPlanState state) throws Exception {
        log.info("===DistanceCalculateNode节点===");
        log.info("开始计算景点之间的距离和耗时");
        //获取景点列表
        TravelItineraryVO travelItinerary = state.itinerarySkeleton();
        List<DailyScheduleVO> dailyScheduleList = travelItinerary.getDailySchedules();
        //填充地理信息
        for (int i = 0; i < dailyScheduleList.size(); i++) {
            List<AttractionInfoVO> activities = dailyScheduleList.get(i).getActivities();
            for (int j = 0; j < activities.size(); j++) {
                AttractionInfoVO attractionInfo = activities.get(j);
                Double[] position = attractionService.queryAttractionPositionById(Long.valueOf(attractionInfo.getId()));
                attractionInfo.setPosition(position);
                DecimalFormat df = new DecimalFormat("#.00");
                DecimalFormat df2 = new DecimalFormat("#");
                if (j!=0){
                    AttractionInfoVO preAttractionInfo = activities.get(j - 1);
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

    /** 地理信息载体（当前未被引用，保留作为结构化扩展位） */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GeoInfo {
        /** 地点名称 */
        private List<String> name;
        /** 经度 */
        private List<Double> lng;
        /** 纬度 */
        private List<Double> lat;
        /** 距离描述 */
        private List<String> distance;
        /** 驾车耗时描述 */
        private List<String> drivingTime;
    }

}
