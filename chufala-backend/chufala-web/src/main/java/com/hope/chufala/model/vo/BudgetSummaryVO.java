package com.hope.chufala.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 行程预算汇总。
 *
 * @author 谢光湘
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BudgetSummaryVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    /** 总预算（元） */
    private Double total;
    /** 分项明细 */
    private List<BudgetItemVO> breakdown;
}
