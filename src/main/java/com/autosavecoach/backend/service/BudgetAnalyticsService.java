package com.autosavecoach.backend.service;

import com.autosavecoach.backend.dto.BudgetAnalyticsResponse;
import com.autosavecoach.backend.dto.BudgetCalibrationResponse;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        System.out.println(user);
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

    public List<BudgetCalibrationResponse> getCalibration(int months, String categoryFilter) {

        User user = getCurrentUser();
        LocalDate fromDate = LocalDate.now().minusMonths(months);
        Category filter = (categoryFilter != null)
                ? CategoryUtil.parse(categoryFilter)
                : null;

        // 1️⃣ Monthly totals
        List<Object[]> rows =
                expenseRepository.avgSpendLastMonths(user.getId(), fromDate);

        // 2️⃣ category → list of monthly totals
        Map<Category, List<Double>> monthlyMap = new HashMap<>();

        for (Object[] r : rows) {
            Category cat = (Category) r[0];
            Double monthlyTotal = (Double) r[3];

            monthlyMap
                    .computeIfAbsent(cat, k -> new ArrayList<>())
                    .add(monthlyTotal);
        }

        // 3️⃣ category → avg monthly spend
        Map<Category, Double> avgMonthlySpend = new HashMap<>();
        for (var entry : monthlyMap.entrySet()) {
            avgMonthlySpend.put(
                    entry.getKey(),
                    entry.getValue().stream().mapToDouble(Double::doubleValue).average().orElse(0)
            );
        }

        // 4️⃣ Latest budget per category (IMPORTANT)
        List<Budget> budgets =
                budgetRepository.findLatestBudgetsPerCategory(
                        user.getId(), filter
                );

        List<BudgetCalibrationResponse> result = new ArrayList<>();

        for (Budget budget : budgets) {

            Category cat = budget.getCategory();
            double avgSpend = avgMonthlySpend.getOrDefault(cat, 0.0);
            double current = budget.getAmount();

            double deviation =
                    avgSpend == 0 ? 0 : ((current - avgSpend) / avgSpend) * 100;

            String status;
            if (current < avgSpend * 0.85) status = "UNDERSET";
            else if (current > avgSpend * 1.25) status = "OVERSET";
            else status = "WELL_CALIBRATED";

            result.add(new BudgetCalibrationResponse(
                    cat,
                    current,
                    avgSpend,
                    avgSpend,
                    status,
                    deviation
            ));
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

