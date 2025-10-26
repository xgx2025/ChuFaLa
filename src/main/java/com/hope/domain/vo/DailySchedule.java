package com.hope.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailySchedule {
    private int day;
    private String weather;
    private String temperature;
    private List<AttractionInfo> activities;
    private List<HotelInfo> recommendHotels;
}
