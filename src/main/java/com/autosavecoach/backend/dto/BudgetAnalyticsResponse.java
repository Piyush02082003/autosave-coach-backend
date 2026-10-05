package com.autosavecoach.backend.dto;

import com.autosavecoach.backend.model.TransactionCategory;
import lombok.Getter;

import java.time.YearMonth;

@Getter
public class BudgetAnalyticsResponse {

    private YearMonth month;
    private TransactionCategory transactionCategory;
    private double budget;
    private double spent;
    private double remaining;
    private double percentageUsed;
    private String status;

    public BudgetAnalyticsResponse(
            YearMonth month,
            TransactionCategory transactionCategory,
            double budget,
            double spent,
            double remaining,
            double percentageUsed,
            String status
    ) {
        this.month = month;
        this.transactionCategory = transactionCategory;
        this.budget = budget;
        this.spent = spent;
        this.remaining = remaining;
        this.percentageUsed = percentageUsed;
        this.status = status;
    }

}

