package com.autosavecoach.backend.dto;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class OverallFeasibility {
    private BigDecimal totalBudget;
    private BigDecimal spentSoFar;
    private BigDecimal remainingBudget;
    private int daysLeft;
    private BigDecimal allowedPerDay;
    private String status;

    public OverallFeasibility(
            BigDecimal totalBudget,
            BigDecimal spentSoFar,
            BigDecimal remainingBudget,
            int daysLeft,
            BigDecimal allowedPerDay,
            String status
    ) {
        this.totalBudget = totalBudget;
        this.spentSoFar = spentSoFar;
        this.remainingBudget = remainingBudget;
        this.daysLeft = daysLeft;
        this.allowedPerDay = allowedPerDay;
        this.status = status;
    }
}
