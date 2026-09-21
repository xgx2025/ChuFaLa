package com.hope.chufala.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailyScheduleVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private int day;
    private String weather;
    private String temperature;
    private List<AttractionInfoVO> activities;
    private List<HotelInfoVO> recommendHotels;
}
