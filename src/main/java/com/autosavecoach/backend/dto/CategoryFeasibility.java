package com.autosavecoach.backend.dto;

import com.autosavecoach.backend.model.Category;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class CategoryFeasibility {
    private Category category;
    private BigDecimal allowedPerDay;
    private BigDecimal historyPerDay;
    private String status;

    public CategoryFeasibility(
            Category category,
            BigDecimal allowedPerDay,
            BigDecimal historyPerDay,
            String status
    ) {
        this.category = category;
        this.allowedPerDay = allowedPerDay;
        this.historyPerDay = historyPerDay;
        this.status = status;
    }
}
