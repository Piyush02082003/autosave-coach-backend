package com.autosavecoach.backend.dto;

import com.autosavecoach.backend.model.Category;
import lombok.Getter;

@Getter
public class CategoryFeasibility {
    private Category category;
    private double allowedPerDay;
    private double historyPerDay;
    private String status;

    public CategoryFeasibility(
            Category category,
            double allowedPerDay,
            double historyPerDay,
            String status
    ) {
        this.category = category;
        this.allowedPerDay = allowedPerDay;
        this.historyPerDay = historyPerDay;
        this.status = status;
    }
}
