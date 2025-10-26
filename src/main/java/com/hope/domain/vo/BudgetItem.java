package com.hope.domain.vo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BudgetItem {
    private String category;
    private Double amount;
    private Double percentage;
}
