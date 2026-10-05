package com.autosavecoach.backend.dto;

import com.autosavecoach.backend.model.Category;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class BudgetCalibrationResponse {

    private Category category;
    private BigDecimal currentBudget;
    private BigDecimal avgHistoricalSpend;
    private BigDecimal recommendedBudget;
    private String calibrationStatus;
    private BigDecimal deviationPercent;

    public BudgetCalibrationResponse(
            Category category,
            BigDecimal currentBudget,
            BigDecimal avgHistoricalSpend,
            BigDecimal recommendedBudget,
            String calibrationStatus,
            BigDecimal deviationPercent
    ) {
        this.category = category;
        this.currentBudget = currentBudget;
        this.avgHistoricalSpend = avgHistoricalSpend;
        this.recommendedBudget = recommendedBudget;
        this.calibrationStatus = calibrationStatus;
        this.deviationPercent = deviationPercent;
    }
}
