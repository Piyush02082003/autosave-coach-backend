package com.autosavecoach.backend.dto;

import com.autosavecoach.backend.model.Category;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.YearMonth;

@Getter
public class BudgetAnalyticsResponse {

    private YearMonth month;
    private Category category;
    private BigDecimal budget;
    private BigDecimal spent;
    private BigDecimal remaining;
    private BigDecimal percentageUsed;
    private String status;

    public BudgetAnalyticsResponse(
            YearMonth month,
            Category category,
            BigDecimal budget,
            BigDecimal spent,
            BigDecimal remaining,
            BigDecimal percentageUsed,
            String status
    ) {
        this.month = month;
        this.category = category;
        this.budget = budget;
        this.spent = spent;
        this.remaining = remaining;
        this.percentageUsed = percentageUsed;
        this.status = status;
    }

}

