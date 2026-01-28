package com.autosavecoach.backend.service;

import com.autosavecoach.backend.dto.BudgetAnalyticsResponse;
import com.autosavecoach.backend.model.Budget;
import com.autosavecoach.backend.model.Category;
import com.autosavecoach.backend.model.User;
import com.autosavecoach.backend.repository.BudgetRepository;
import com.autosavecoach.backend.repository.ExpenseRepository;
import com.autosavecoach.backend.repository.UserRepository;
import com.autosavecoach.backend.util.CategoryUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class BudgetAnalyticsService {

    private final BudgetRepository budgetRepository;
    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;

    public BudgetAnalyticsService(BudgetRepository budgetRepository, ExpenseRepository expenseRepository, UserRepository userRepository) {
        this.budgetRepository = budgetRepository;
        this.expenseRepository = expenseRepository;
        this.userRepository = userRepository;
    }

    public List<BudgetAnalyticsResponse> getBudgetSummary(YearMonth startMonth, YearMonth endMonth, String categoryFilter) {

        User user = getCurrentUser();
        Category category = (categoryFilter != null) ? CategoryUtil.parse(categoryFilter) : null;

        // Fetch all budgets
        List<Budget> budgets = budgetRepository.findBudgetsForAnalytics(
                user.getId(),
                startMonth,
                endMonth,
                category
        );
        List<BudgetAnalyticsResponse> result = new ArrayList<>();

        for (Budget budget : budgets) {

            YearMonth month = budget.getMonth();
            LocalDate startDate = month.atDay(1);
            LocalDate endDate = month.atEndOfMonth();

            Map<Category, Double> expenseMap =
                    expenseRepository.sumExpensesByCategory(
                            user.getId(),
                            startDate,
                            endDate
                    );

            double spent = expenseMap.getOrDefault(
                    budget.getCategory(), 0.0
            );

            double budgetAmount = budget.getAmount();
            double remaining = budgetAmount - spent;
            double percentageUsed =
                    budgetAmount == 0 ? 0 : (spent / budgetAmount) * 100;

            result.add(
                    new BudgetAnalyticsResponse(
                            month,
                            budget.getCategory(),
                            budgetAmount,
                            spent,
                            remaining,
                            percentageUsed,
                            determineStatus(spent, percentageUsed)
                    )
            );
        }

        return result;
    }

    private String determineStatus(double spent, double percentageUsed) {

        if (spent == 0) return "NOT_STARTED";
        if (percentageUsed < 70) return "ON_TRACK";
        if (percentageUsed < 90) return "WARNING";
        if (percentageUsed <= 100) return "LIMIT_REACHED";
        return "EXCEEDED";
    }

    private User getCurrentUser() {

        Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("Unauthenticated request");
        }

        return userRepository.findByEmail(auth.getPrincipal().toString())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}

