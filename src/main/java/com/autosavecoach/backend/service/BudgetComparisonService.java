package com.autosavecoach.backend.service;

import com.autosavecoach.backend.dto.BudgetComparisonResponse;
import com.autosavecoach.backend.model.Budget;
import com.autosavecoach.backend.model.Category;
import com.autosavecoach.backend.repository.BudgetRepository;
import com.autosavecoach.backend.repository.ExpenseRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
public class BudgetComparisonService {

    private final BudgetRepository budgetRepository;
    private final ExpenseRepository expenseRepository;

    public BudgetComparisonService(
            BudgetRepository budgetRepository,
            ExpenseRepository expenseRepository
    ) {
        this.budgetRepository = budgetRepository;
        this.expenseRepository = expenseRepository;
    }

    public List<BudgetComparisonResponse> compareBudgets(
            Long userId,
            YearMonth startMonth,
            YearMonth endMonth,
            String categoryFilter
    ) {
        // Fetch all budgets
        List<Budget> budgets = budgetRepository.findAll();
        List<BudgetComparisonResponse> response = new ArrayList<>();

        for (Budget budget : budgets) {

            // Skip other users
            if (!budget.getUser().getId().equals(userId)) continue;

            // Skip other months
            YearMonth budgetMonth = budget.getMonth();
            if (budgetMonth.isBefore(startMonth) || budgetMonth.isAfter(endMonth)) {
                continue;
            }

            // Skip other category
            Category category = budget.getCategory();
            if (categoryFilter != null &&
                    !category.name().equalsIgnoreCase(categoryFilter)) {
                continue;
            }

            LocalDate startDate = budgetMonth.atDay(1);
            LocalDate endDate = budgetMonth.atEndOfMonth();

            double spent = expenseRepository
                    .findByUserIdAndCategoryAndDateBetween(
                            userId,
                            category,
                            startDate,
                            endDate
                    )
                    .stream()
                    .mapToDouble(e -> e.getAmount())
                    .sum();

            double budgetAmount = budget.getAmount();
            double remaining = budgetAmount - spent;

            double percentageUsed = budgetAmount == 0 ? 0 : (spent / budgetAmount) * 100;

            String status = determineStatus(spent, percentageUsed);

            response.add(
                    new BudgetComparisonResponse(
                            category,
                            budgetAmount,
                            spent,
                            remaining,
                            percentageUsed,
                            status
                    )
            );
        }
        return response;
    }

    private String determineStatus(double spent, double percentageUsed) {

        if (spent == 0) return "NOT_STARTED";
        if (percentageUsed < 80) return "ON_TRACK";
        if (percentageUsed < 100) return "WARNING";
        if (percentageUsed == 100) return "LIMIT_REACHED";
        return "EXCEEDED";
    }
}

