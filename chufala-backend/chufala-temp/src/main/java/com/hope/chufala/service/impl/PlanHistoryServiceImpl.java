package com.hope.chufala.service.impl;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hope.chufala.domain.dto.UserPlanDTO;
import com.hope.chufala.domain.entity.PlanHistory;
import com.hope.chufala.domain.vo.TravelItinerary;
import com.hope.chufala.mapper.PlanHistoryMapper;
import com.hope.chufala.service.IPlanHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class PlanHistoryServiceImpl implements IPlanHistoryService {
    @Autowired
    private PlanHistoryMapper planHistoryMapper;
    @Override
    public void addHistory(Long userId, LocalDateTime createTime, UserPlanDTO userPlan,String planContent, TravelItinerary plan) {
        //1. 获取雪花算法实例（<默认>机房ID=0，机器ID=0）
        Snowflake snowFlake = IdUtil.getSnowflake(1,1);
        PlanHistory history = new PlanHistory();
        history.setId(snowFlake.nextId());
        history.setUserId(userId);
        history.setDestination(userPlan.getDestination());
        history.setStartDate(userPlan.getStartDate());
        history.setPeople(userPlan.getPeople());
        history.setDayNum(userPlan.getDayNum());
        history.setBudget(userPlan.getBudget());
        history.setPlanContent(planContent);
        history.setPlanResult(plan);
        history.setCreateTime(createTime);
        planHistoryMapper.insert(history);
    }

    @Override
    public List<PlanHistory> queryHistory(Long userId) {
        QueryWrapper<PlanHistory> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        //设置要查询的字段
        queryWrapper.select("id","destination","people","start_date","day_num","budget","create_time").orderByDesc("create_time");
        //查询10条数据
        queryWrapper.last("limit 10");
        return planHistoryMapper.selectList(queryWrapper);
    }

    @Override
    public TravelItinerary queryPlanResult(Long id) {
        PlanHistory planHistory = planHistoryMapper.selectById(id);
        log.info("规划结果：{}", planHistory);
        return planHistory.getPlanResult();
    }

    @Override
    public String getPlanContentById(Long id) {
        QueryWrapper<PlanHistory> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", id).select("plan_content");
        PlanHistory planHistory = planHistoryMapper.selectOne(queryWrapper);
        return planHistory.getPlanContent();
    }

}
