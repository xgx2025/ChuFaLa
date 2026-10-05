package com.hope.chufala.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 单日行程安排。
 *
 * @author 谢光湘
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailyScheduleVO implements Serializable {
    private static final long serialVersionUID = 1L;
    /** 第几天，从 1 开始 */
    private int day;
    /** 天气描述 */
    private String weather;
    /** 温度描述 */
    private String temperature;
    /** 当日活动（景点）列表 */
    private List<AttractionInfoVO> activities;
    /** 当日推荐酒店列表 */
    private List<HotelInfoVO> recommendHotels;
}
