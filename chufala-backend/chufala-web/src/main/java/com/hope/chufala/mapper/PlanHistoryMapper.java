package com.hope.chufala.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hope.chufala.model.entity.PlanHistory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 行程规划历史 Mapper。
 *
 * <p>仅使用 MyBatis-Plus 通用 CRUD；plan_result 列的 JSON 序列化由实体的
 * JacksonTypeHandler 负责。
 *
 * @author 谢光湘
 */
@Mapper
public interface PlanHistoryMapper extends BaseMapper<PlanHistory> {
}
