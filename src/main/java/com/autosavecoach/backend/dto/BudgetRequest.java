package com.autosavecoach.backend.dto;

import com.autosavecoach.backend.model.Category;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class BudgetRequest {

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

}

