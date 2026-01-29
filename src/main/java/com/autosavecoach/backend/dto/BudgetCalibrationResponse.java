package com.autosavecoach.backend.dto;

import com.autosavecoach.backend.model.Category;
import lombok.Getter;

@Getter
public class BudgetCalibrationResponse {

    private Category category;
    private double currentBudget;
    private double avgHistoricalSpend;
    private double recommendedBudget;
    private String calibrationStatus;
    private double deviationPercent;

    public BudgetCalibrationResponse(
            Category category,
            double currentBudget,
            double avgHistoricalSpend,
            double recommendedBudget,
            String calibrationStatus,
            double deviationPercent
    ) {
        this.category = category;
        this.currentBudget = currentBudget;
        this.avgHistoricalSpend = avgHistoricalSpend;
        this.recommendedBudget = recommendedBudget;
        this.calibrationStatus = calibrationStatus;
        this.deviationPercent = deviationPercent;
    }
}
