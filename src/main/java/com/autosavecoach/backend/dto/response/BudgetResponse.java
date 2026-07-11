package com.autosavecoach.backend.dto.response;

import lombok.Getter;

import java.util.UUID;

@Getter
public class BudgetResponse {

    private UUID id;
    private String category;
    private Double amount;
    private String month;

    public BudgetResponse(UUID id, String category, Double amount, String month) {
        this.id = id;
        this.category = category;
        this.amount = amount;
        this.month = month;
    }

}

