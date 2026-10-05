package com.autosavecoach.backend.dto;

import com.autosavecoach.backend.model.TransactionCategory;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.YearMonth;

@Getter
@AllArgsConstructor
public class BudgetDriftResponse {
    private YearMonth month;
    private TransactionCategory transactionCategory;
    private String driftLevel;
    private double recentAvgSpend;
    private double historicalAvgSpend;
    private double driftPercentage;
}
