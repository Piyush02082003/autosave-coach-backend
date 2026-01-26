package com.autosavecoach.backend.dto;

import lombok.Getter;

@Getter
public class BudgetResponse {

    private Long id;
    private String category;
    private Double amount;
    private String month;

    public BudgetResponse(Long id, String category, Double amount, String month) {
        this.id = id;
        this.category = category;
        this.amount = amount;
        this.month = month;
    }

}

