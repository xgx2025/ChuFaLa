package com.hope.chufala.model.vo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 行程预算分项。
 *
 * @author 谢光湘
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BudgetItemVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    /** 费用类别 */
    private String category;
    /** 金额（元） */
    private Double amount;
    /** 占比（%） */
    private Double percentage;
}
