package com.autosavecoach.backend.dto;

import com.autosavecoach.backend.model.Category;
import lombok.Getter;

import java.time.YearMonth;

@Getter
public class BudgetComparisonResponse {

    private YearMonth month;
    private Category category;
    private double budget;
    private double spent;
    private double remaining;
    private double percentageUsed;
    private String status;

    public BudgetComparisonResponse(
            YearMonth month,
            Category category,
            double budget,
            double spent,
            double remaining,
            double percentageUsed,
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

