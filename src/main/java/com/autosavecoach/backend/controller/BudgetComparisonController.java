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
            @RequestParam String startMonth,
            @RequestParam(required = false) String endMonth,
            @RequestParam(required = false) String category
    ) {

        YearMonth start = YearMonth.parse(startMonth);
        YearMonth end = (endMonth != null)
                ? YearMonth.parse(endMonth)
                : start;

        return comparisonService.compareBudgets(
                userId,
                start,
                end,
                category
        );
    }
}
