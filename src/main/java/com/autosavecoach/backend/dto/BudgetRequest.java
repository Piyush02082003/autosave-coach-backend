package com.autosavecoach.backend.dto;

import com.autosavecoach.backend.model.Category;
import jakarta.validation.constraints.*;

public class BudgetRequest {

    @NotNull(message = "UserId is required")
    private Long userId;

    @NotBlank(message = "Category is required")
    private String category;

    @NotNull(message = "Amount is required")
    @Positive(message = "Budget amount must be greater than 0")
    private Double amount;

    @NotBlank(message = "Month is required (YYYY-MM)")
    @Pattern(
            regexp = "\\d{4}-\\d{2}",
            message = "Month must be in YYYY-MM format"
    )
    private String month;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }
}

