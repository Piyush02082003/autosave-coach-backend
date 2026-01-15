package com.autosavecoach.backend.dto;

import com.autosavecoach.backend.model.Category;
import lombok.Getter;

@Getter
public class BudgetComparisonResponse {

    private Category category;
    private double budget;
    private double spent;
    private double remaining;
    private double percentageUsed;
    private String status;

    public BudgetComparisonResponse(
            Category category,
            double budget,
            double spent,
            double remaining,
            double percentageUsed,
            String status
    ) {
        this.category = category;
        this.budget = budget;
        this.spent = spent;
        this.remaining = remaining;
        this.percentageUsed = percentageUsed;
        this.status = status;
    }

}

