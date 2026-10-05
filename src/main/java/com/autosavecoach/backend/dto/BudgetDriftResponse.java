package com.autosavecoach.backend.dto;

import com.autosavecoach.backend.model.Category;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.YearMonth;

@Getter
@AllArgsConstructor
public class BudgetDriftResponse {
    private YearMonth month;
    private Category category;
    private String driftLevel;
    private BigDecimal recentAvgSpend;
    private BigDecimal historicalAvgSpend;
    private BigDecimal driftPercentage;
}
