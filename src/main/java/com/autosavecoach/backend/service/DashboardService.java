package com.autosavecoach.backend.service;

import com.autosavecoach.backend.dto.BudgetComparisonResponse;
import com.autosavecoach.backend.dto.DashboardResponse;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.util.List;

@Service
public class DashboardService {

    private final BudgetComparisonService budgetComparisonService;

    public DashboardService(BudgetComparisonService budgetComparisonService) {
        this.budgetComparisonService = budgetComparisonService;
    }

    public DashboardResponse getDashboard(Long userId, YearMonth month) {

        List<BudgetComparisonResponse> comparisons = budgetComparisonService.compareBudgets(userId, month);

        double totalBudget = comparisons.stream()
                .mapToDouble(BudgetComparisonResponse::getBudget)
                .sum();

        double totalSpent = comparisons.stream()
                .mapToDouble(BudgetComparisonResponse::getSpent)
                .sum();

        double remaining = totalBudget - totalSpent;

        double percentageUsed = totalBudget == 0 ? 0 :
                (totalSpent / totalBudget) * 100;

        return new DashboardResponse(
                month.toString(),
                totalBudget,
                totalSpent,
                remaining,
                percentageUsed,
                comparisons
        );
    }
}
