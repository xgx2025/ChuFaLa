package com.hope.chufala.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.hope.chufala.model.vo.TravelItineraryVO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * AI 旅行规划历史实体，对应 plan_history 表。
 *
 * <p>planResult 以 JSON 形式落库（JacksonTypeHandler 负责序列化 TravelItineraryVO），
 * 因此实体标注了 {@code @TableName(autoResultMap = true)} 以启用自定义 TypeHandler。
 *
 * @author 谢光湘
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(autoResultMap = true)
public class PlanHistory {
    /** 主键；序列化为字符串避免前端精度丢失 */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    /** 归属用户 ID */
    private Long userId;
    /** 目的地 */
    private String destination;
    /** 出行人数 */
    private Integer people;
    /** 行程天数 */
    private int dayNum;
    /** 出发日期 */
    private LocalDate startDate;
    /** 偏好标签列表，非持久化字段 */
    @TableField(exist = false)
    private List<String> preferences;
    /** 预算（元） */
    private Double budget;
    /** 规划内容摘要 */
    private String planContent;
    /** 完整行程结果，对应列 plan_result，以 JSON 存储 */
    @TableField(value = "plan_result",typeHandler = JacksonTypeHandler.class)
    private TravelItineraryVO planResult;
    /** 创建时间 */
    private LocalDateTime createTime;
}
