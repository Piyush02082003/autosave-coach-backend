package com.autosavecoach.backend.dto.response;

import lombok.Getter;
import java.time.LocalDate;
import java.util.UUID;

@Getter
public class ExpenseResponse {

    private UUID id;
    private String title;
    private String category;
    private Double amount;
    private LocalDate date;

    public ExpenseResponse(UUID id, String title, String category, Double amount, LocalDate date) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.amount = amount;
        this.date = date;
    }

}

