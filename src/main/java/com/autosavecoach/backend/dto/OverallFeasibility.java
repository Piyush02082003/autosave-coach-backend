package com.autosavecoach.backend.dto;

import lombok.Getter;

@Getter
public class OverallFeasibility {
    private double totalBudget;
    private double spentSoFar;
    private double remainingBudget;
    private int daysLeft;
    private double allowedPerDay;
    private String status;

    public OverallFeasibility(
            double totalBudget,
            double spentSoFar,
            double remainingBudget,
            int daysLeft,
            double allowedPerDay,
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
