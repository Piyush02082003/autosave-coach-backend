package com.autosavecoach.backend.dto;

import lombok.Getter;

@Getter
public class BudgetCalibrationResponse {

    private TransactionCategory transactionCategory;
    private double currentBudget;
    private double avgHistoricalSpend;
    private double recommendedBudget;
    private String calibrationStatus;
    private double deviationPercent;

    public BudgetCalibrationResponse(
            TransactionCategory transactionCategory,
            double currentBudget,
            double avgHistoricalSpend,
            double recommendedBudget,
            String calibrationStatus,
            double deviationPercent
    ) {
        this.transactionCategory = transactionCategory;
        this.currentBudget = currentBudget;
        this.avgHistoricalSpend = avgHistoricalSpend;
        this.recommendedBudget = recommendedBudget;
        this.calibrationStatus = calibrationStatus;
        this.deviationPercent = deviationPercent;
    }
}
