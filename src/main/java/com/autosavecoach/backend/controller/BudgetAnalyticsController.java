package com.autosavecoach.backend.controller;

import com.autosavecoach.backend.dto.BudgetAnalyticsResponse;
import com.autosavecoach.backend.dto.BudgetCalibrationResponse;
import com.autosavecoach.backend.dto.BudgetDriftResponse;
import com.autosavecoach.backend.dto.BudgetFeasibilityResponse;
import com.autosavecoach.backend.exception.BadRequestException;
import com.autosavecoach.backend.exception.InvalidMonthException;
import com.autosavecoach.backend.model.Category;
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
    public List<BudgetCalibrationResponse> calibration(@RequestParam(defaultValue = "6") int month, @RequestParam(required = false) String category){
        System.out.println("inside controller");
        if (month < 1 || month > 12) {
            throw new BadRequestException("Months must be between 1 and 12");
        }

        return budgetAnalyticsService.getCalibration(month, category);
    }

    @GetMapping("/drift")
    public List<BudgetDriftResponse> drift(@RequestParam(defaultValue = "0") int month, @RequestParam(required = false)String category){
        if (month < 0 || month > 12) {
            throw new BadRequestException("Months must be between 1 and 12");
        }

        YearMonth targetMonth =
                month == 0 ? YearMonth.now() : YearMonth.now().minusMonths(month);

        System.out.println(targetMonth);

        return budgetAnalyticsService.calDrift(
                targetMonth,
                category
        );
    }

    @GetMapping("/feasibility")
    public BudgetFeasibilityResponse feasibility() {
        return budgetAnalyticsService.calFeasibility();
    }

    private YearMonth parseMonth(String value) {
        try {
            return YearMonth.parse(value);
        } catch (Exception e) {
            throw new InvalidMonthException("Month must be in YYYY-MM format");
        }
    }
}
