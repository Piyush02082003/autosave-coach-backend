package com.autosavecoach.backend.dto;

import lombok.Getter;

import java.time.YearMonth;
import java.util.List;

@Getter
public class BudgetFeasibilityResponse {
    private YearMonth month;
    private OverallFeasibility overall;
    private List<CategoryFeasibility> categories;

    public BudgetFeasibilityResponse(
            YearMonth month,
            OverallFeasibility overall,
            List<CategoryFeasibility> categories
    ) {
        this.month = month;
        this.overall = overall;
        this.categories = categories;
    }
}
