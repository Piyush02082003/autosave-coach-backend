package com.autosavecoach.backend.controller;

import com.autosavecoach.backend.dto.BudgetComparisonResponse;
import com.autosavecoach.backend.service.BudgetComparisonService;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/budgets")
public class BudgetComparisonController {

    private final BudgetComparisonService comparisonService;

    public BudgetComparisonController(BudgetComparisonService comparisonService) {
        this.comparisonService = comparisonService;
    }

    @GetMapping("/compare")
    public List<BudgetComparisonResponse> compareBudgets(
            @RequestParam Long userId,
            @RequestParam(required = false) String month,
            @RequestParam(required = false) String startMonth,
            @RequestParam(required = false) String endMonth,
            @RequestParam(required = false) String category
    ) {
        YearMonth start;
        YearMonth end;

        if (month != null) {
            start = YearMonth.parse(month);
            end = start;
        } else if (startMonth != null && endMonth != null) {
            start = YearMonth.parse(startMonth);
            end = YearMonth.parse(endMonth);
        } else {
            throw new IllegalArgumentException(
                    "Either 'month' OR 'startMonth' and 'endMonth' must be provided"
            );
        }

        return comparisonService.compareBudgets(
                userId,
                start,
                end,
                category
        );
    }
}
