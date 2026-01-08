package com.autosavecoach.backend.dto;

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

    public Long getId() {
        return id;
    }

    public String getCategory() {
        return category;
    }

    public Double getAmount() {
        return amount;
    }

    public String getMonth() {
        return month;
    }
}

