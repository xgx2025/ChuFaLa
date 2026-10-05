package com.hope.chufala.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * AI 行程规划入参。
 *
 * <p>作为 LangGraph4j 规划流程的初始输入，贯穿各规划节点。
 *
 * @author 谢光湘
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPlanDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    /** 目的地 */
    private String destination;
    /** 出行人数 */
    private Integer people;
    /** 出发日期 */
    private LocalDate startDate;
    /** 行程天数 */
    private Integer dayNum;
    /** 偏好标签列表 */
    private List<String> preferences;
    /** 预算（元） */
    private Double budget;
}
