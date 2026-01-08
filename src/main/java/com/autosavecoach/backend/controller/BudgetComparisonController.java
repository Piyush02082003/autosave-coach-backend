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
            @RequestParam String month
    ) {
        YearMonth yearMonth = YearMonth.parse(month);
        return comparisonService.compareBudgets(userId, yearMonth);
    }
}
