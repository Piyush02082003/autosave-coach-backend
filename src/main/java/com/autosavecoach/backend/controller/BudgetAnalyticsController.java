package com.autosavecoach.backend.controller;

import com.autosavecoach.backend.dto.BudgetAnalyticsResponse;
import com.autosavecoach.backend.dto.BudgetCalibrationResponse;
import com.autosavecoach.backend.exception.InvalidMonthException;
import com.autosavecoach.backend.service.BudgetAnalyticsService;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/budgets/analytics")
public class BudgetAnalyticsController {

    private final BudgetAnalyticsService budgetAnalyticsService;

    public BudgetAnalyticsController(BudgetAnalyticsService budgetAnalyticsService) {
        this.budgetAnalyticsService = budgetAnalyticsService;
    }

    @GetMapping("/summary")
    public List<BudgetAnalyticsResponse> compareBudgets(
            @RequestParam String startMonth,
            @RequestParam(required = false) String endMonth,
            @RequestParam(required = false) String category
    ) {

        YearMonth start = parseMonth(startMonth);
        YearMonth end = (endMonth != null) ? parseMonth(endMonth) : start;

        return budgetAnalyticsService.getBudgetSummary(start, end, category);
    }

    @GetMapping("/calibration")
    public List<BudgetCalibrationResponse> calibration(@RequestParam(defaultValue = "6") int months, @RequestParam(required = false) String category){
        return budgetAnalyticsService.getCalibration(months, category);
    }

    private YearMonth parseMonth(String value) {
        try {
            return YearMonth.parse(value);
        } catch (Exception e) {
            throw new InvalidMonthException("Month must be in YYYY-MM format");
        }
    }
}
