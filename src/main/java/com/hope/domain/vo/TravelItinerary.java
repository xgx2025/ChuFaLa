package com.hope.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TravelItinerary {
    private List<DailySchedule> travelItinerary;
    private BudgetSummary budgetSummary;
}
