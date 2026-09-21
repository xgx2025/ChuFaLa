package com.hope.chufala.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BudgetSummary implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private Double total;
    private List<BudgetItem> breakdown;
}
