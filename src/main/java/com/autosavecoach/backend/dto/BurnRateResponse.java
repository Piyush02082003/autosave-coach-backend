package com.autosavecoach.backend.dto;

import lombok.Getter;

@Getter
public class BurnRateResponse {

    private int daysElapsed;
    private double totalSpent;
    private double dailyBurnRate;

    public BurnRateResponse(int daysElapsed, double totalSpent, double dailyBurnRate) {
        this.daysElapsed = daysElapsed;
        this.totalSpent = totalSpent;
        this.dailyBurnRate = dailyBurnRate;
    }

}

