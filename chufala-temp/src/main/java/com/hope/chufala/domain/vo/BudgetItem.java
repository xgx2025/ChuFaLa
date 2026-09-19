package com.hope.chufala.domain.vo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BudgetItem implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String category;
    private Double amount;
    private Double percentage;
}
