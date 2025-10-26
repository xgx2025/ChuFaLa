package com.hope.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hope.domain.entity.PlanHistory;
import com.hope.mapper.PlanHistoryMapper;
import com.hope.service.IPlanHistoryService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PlanHistoryServiceImpl implements IPlanHistoryService {
    @Resource
    private PlanHistoryMapper planHistoryMapper;
    @Override
    public List<PlanHistory> queryHistory(Long userId) {
        QueryWrapper<PlanHistory> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        //不要查询context字段
        queryWrapper.select("conversation_id",  "type", "timestamp");
        return planHistoryMapper.selectList(queryWrapper);
    }
}
