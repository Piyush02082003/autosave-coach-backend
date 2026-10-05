package com.autosavecoach.backend.dto;

import com.autosavecoach.backend.model.TransactionCategory;
import lombok.Getter;

@Getter
public class CategoryFeasibility {
    private TransactionCategory transactionCategory;
    private double allowedPerDay;
    private double historyPerDay;
    private String status;

    public CategoryFeasibility(
            TransactionCategory transactionCategory,
            double allowedPerDay,
            double historyPerDay,
            String status
    ) {
        this.transactionCategory = transactionCategory;
        this.allowedPerDay = allowedPerDay;
        this.historyPerDay = historyPerDay;
        this.status = status;
    }
}
