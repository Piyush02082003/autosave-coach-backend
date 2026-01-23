package com.autosavecoach.backend.dto;

import lombok.Getter;
import java.time.LocalDate;

@Getter
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

}

