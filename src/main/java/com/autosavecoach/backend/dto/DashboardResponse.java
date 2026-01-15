package com.autosavecoach.backend.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class DashboardResponse {
    private final String month;
    private final double totalBudget;
    private final double totalSpent;
    private final double remaining;
    private final double percentageUsed;
    private final List<BudgetComparisonResponse> categories;

    public DashboardResponse(
            String month,
            double totalBudget,
            double totalSpent,
            double remaining,
            double percentageUsed,
            List<BudgetComparisonResponse> categories
    ) {
        this.month = month;
        this.totalBudget = totalBudget;
        this.totalSpent = totalSpent;
        this.remaining = remaining;
        this.percentageUsed = percentageUsed;
        this.categories = categories;
    }

}
