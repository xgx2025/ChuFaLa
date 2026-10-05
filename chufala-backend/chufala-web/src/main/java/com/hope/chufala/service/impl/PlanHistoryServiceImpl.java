package com.hope.chufala.service.impl;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hope.chufala.common.exception.ResourceNotFoundException;
import com.hope.chufala.model.dto.UserPlanDTO;
import com.hope.chufala.model.entity.PlanHistory;
import com.hope.chufala.model.vo.TravelItineraryVO;
import com.hope.chufala.mapper.PlanHistoryMapper;
import com.hope.chufala.service.IPlanHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 行程规划历史服务实现。
 *
 * <p>planResult 以 JSON 落库（见 PlanHistory 的 JacksonTypeHandler）。列表查询
 * 只 select 摘要字段并限制 10 条，避免把整份大 JSON 一并查出；详情类查询一律带
 * userId 条件做归属校验。
 *
 * @author 谢光湘
 */
@Slf4j
@Service
public class PlanHistoryServiceImpl implements IPlanHistoryService {
    @Autowired
    private PlanHistoryMapper planHistoryMapper;
    /**
     * 保存一条行程规划历史（ID 由 Snowflake 生成）。
     *
     * @param userId      用户 ID
     * @param createTime  创建时间
     * @param userPlan    本次规划入参
     * @param planContent 规划内容摘要
     * @param plan        完整行程结果
     */
    @Override
    public void addHistory(Long userId, LocalDateTime createTime, UserPlanDTO userPlan,String planContent, TravelItineraryVO plan) {
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

    /**
     * 查询用户的规划历史（仅摘要字段，最多 10 条，按创建时间倒序）。
     *
     * @param userId 用户 ID
     * @return 历史列表
     */
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

    /**
     * 查询指定 ID 的规划结果（校验归属）。
     *
     * @param id     历史记录 ID
     * @param userId 用户 ID
     * @return 完整行程结果
     */
    @Override
    public TravelItineraryVO queryPlanResult(Long id, Long userId) {
        PlanHistory planHistory = planHistoryMapper.selectOne(new QueryWrapper<PlanHistory>()
                .eq("id", id).eq("user_id", userId));
        if (planHistory == null) {
            throw new ResourceNotFoundException("行程不存在");
        }
        return planHistory.getPlanResult();
    }

    /**
     * 查询指定 ID 的规划内容摘要（校验归属）。
     *
     * @param id     历史记录 ID
     * @param userId 用户 ID
     * @return 规划内容摘要
     */
    @Override
    public String getPlanContentById(Long id, Long userId) {
        QueryWrapper<PlanHistory> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", id).eq("user_id", userId).select("plan_content");
        PlanHistory planHistory = planHistoryMapper.selectOne(queryWrapper);
        if (planHistory == null) {
            throw new ResourceNotFoundException("行程不存在");
        }
        return planHistory.getPlanContent();
    }

}
