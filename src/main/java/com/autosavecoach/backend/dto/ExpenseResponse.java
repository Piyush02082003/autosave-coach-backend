package com.autosavecoach.backend.dto;

import java.time.LocalDate;

public class ExpenseResponse {

    private Long id;
    private String title;
    private String category;
    private Double amount;
    private LocalDate date;

    public ExpenseResponse(Long id, String title, String category, Double amount, LocalDate date) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.amount = amount;
        this.date = date;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getCategory() { return category; }
    public Double getAmount() { return amount; }
    public LocalDate getDate() { return date; }
}

