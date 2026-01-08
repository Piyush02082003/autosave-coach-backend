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

    public List<BudgetComparisonResponse> compareBudgets(Long userId, YearMonth month) {

        // Fetch all budgets
        List<Budget> budgets = budgetRepository.findAll();

        List<BudgetComparisonResponse> response = new ArrayList<>();

        for (Budget budget : budgets) {

            // Skip other users / months
            if (!budget.getUser().getId().equals(userId)
                    || !budget.getMonth().equals(month)) {
                continue;
            }

            Category category = budget.getCategory();

            LocalDate startDate = month.atDay(1);
            LocalDate endDate = month.atEndOfMonth();

            double spent = expenseRepository
                    .findByUserIdAndCategoryAndDateBetween(
                            userId,
                            category,
                            startDate,
                            endDate
                    )
                    .stream()
                    .mapToDouble(expense -> expense.getAmount())
                    .sum();

            double budgetAmount = budget.getAmount();
            double remaining = budgetAmount - spent;
            double percentageUsed = (spent / budgetAmount) * 100;

            String status;
            if (percentageUsed < 80) {
                status = "ON_TRACK";
            } else if (percentageUsed < 100) {
                status = "WARNING";
            } else {
                status = "EXCEEDED";
            }

            System.out.println(status);

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
}

