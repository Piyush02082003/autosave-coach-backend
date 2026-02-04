package com.autosavecoach.backend.service;

import com.autosavecoach.backend.dto.*;
import com.autosavecoach.backend.exception.BadRequestException;
import com.autosavecoach.backend.model.Budget;
import com.autosavecoach.backend.model.Category;
import com.autosavecoach.backend.model.User;
import com.autosavecoach.backend.repository.BudgetRepository;
import com.autosavecoach.backend.repository.ExpenseRepository;
import com.autosavecoach.backend.repository.UserRepository;
import com.autosavecoach.backend.util.CategoryUtil;
import org.springframework.cglib.core.Local;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
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

    public List<BudgetCalibrationResponse> getCalibration(int month, String categoryFilter) {

        User user = getCurrentUser();
        LocalDate fromDate = LocalDate.now().minusMonths(month);
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

            double recommended = Math.round(avgSpend * 1.10 * 100.0) / 100.0;

            result.add(new BudgetCalibrationResponse(
                    cat,
                    current,
                    avgSpend,
                    recommended,
                    status,
                    deviation
            ));
        }

        return result;
    }

    public List<BudgetDriftResponse> calDrift(YearMonth month, String category){
        User user = getCurrentUser();
        Category filter = category==null ? null : CategoryUtil.parse(category);

        LocalDate recentStart = month.atDay(1);
        LocalDate recentEnd = month.atEndOfMonth();
        System.out.println(recentStart+" "+recentEnd);

        LocalDate historyStart = month.minusMonths(3).atDay(1);
        LocalDate historyEnd = month.minusMonths(1).atEndOfMonth();
        System.out.println(historyStart+" "+historyEnd);

        Map<Category, Double> recentSpend = expenseRepository.sumExpensesByCategory(
                user.getId(),
                recentStart,
                recentEnd
        );

        Map<Category, Double> historicalSpend = expenseRepository.sumExpensesByCategory(
                user.getId(),
                historyStart,
                historyEnd
        );

        List<BudgetDriftResponse> result = new ArrayList<>();
        for(Category cat: recentSpend.keySet()){
            if(filter!=null && cat!=filter){
                continue;
            }

            double recentAvg = round(recentSpend.getOrDefault(cat, 0.0));
            double historicalAvg = round(historicalSpend.getOrDefault(cat, 0.0) / 3.0);

            double driftPercent = historicalAvg == 0 ? 0 : round(((recentAvg - historicalAvg) / historicalAvg) * 100);

            result.add(new BudgetDriftResponse(
                    month,
                    cat,
                    determineDriftStatus(driftPercent),
                    recentAvg,
                    historicalAvg,
                    driftPercent
            ));
        }
        return result;
    }

    public BudgetFeasibilityResponse calFeasibility() {
        User user = getCurrentUser();
        YearMonth month = YearMonth.now();

        LocalDate start = month.atDay(1);
        LocalDate today = LocalDate.now();
        LocalDate end = month.atEndOfMonth();

        int daysLeft = (int) ChronoUnit.DAYS.between(today, end) + 1;

        if(daysLeft<=0){
            throw new BadRequestException("Month already ended");
        }

        double totalBudget = budgetRepository.sumBudgetsForMonth(user.getId(), month);
        double spentSoFar = expenseRepository.sumExpenses(user.getId(), start, today);

        double remainingBudget = totalBudget - spentSoFar;
        double requiredPerDay = remainingBudget<=0 ? 0 : remainingBudget / daysLeft;

        double overallHistory =
                expenseRepository.avgDailySpend(
                        user.getId(),
                        null,
                        LocalDate.now().minusMonths(3)
                );

        OverallFeasibility overall = new OverallFeasibility(
                round(totalBudget),
                round(spentSoFar),
                round(remainingBudget),
                daysLeft,
                round(requiredPerDay),
                determineFeasibility(requiredPerDay, overallHistory)
        );

        Map<Category, Double> spentByCategory = expenseRepository.sumExpensesByCategory(
                user.getId(),
                start,
                today
        );

        Map<Category, Double> budgetByCategory =
                budgetRepository.findByUserIdAndMonth(user.getId(), month)
                        .stream()
                        .collect(Collectors.toMap(
                                Budget::getCategory,
                                Budget::getAmount
                        ));

        List<CategoryFeasibility> categories = new ArrayList<>();

        for(Category cat: budgetByCategory.keySet()){
            double catBudget = budgetByCategory.getOrDefault(cat, 0.0);
            double catSpent = spentByCategory.getOrDefault(cat, 0.0);

            double remaining = catBudget - catSpent;
            double requiredDaily  = remaining<= 0 ? 0 : remaining / daysLeft;

            double historyPerDay =
                    expenseRepository.avgDailySpend(
                            user.getId(),
                            cat,
                            LocalDate.now().minusMonths(3)
                    );

            categories.add(new CategoryFeasibility(
                    cat,
                    round(requiredDaily),
                    round(historyPerDay),
                    determineFeasibility(requiredDaily, historyPerDay)
            ));
        }

        return new BudgetFeasibilityResponse(
                month,
                overall,
                categories
        );
    }

    private double round(double val) {
        return Math.round(val * 100.0) / 100.0;
    }

    private String determineFeasibility(double requiredPerDay, double historyPerDay) {

        if (historyPerDay <= 0) return "UNKNOWN";

        if (requiredPerDay <= historyPerDay * 1.1) return "SAFE";
        if (requiredPerDay <= historyPerDay * 1.4) return "TIGHT";

        return "UNLIKELY";
    }

    private String determineDriftStatus(double driftPercent) {

        double driftAbs = Math.abs(driftPercent);
        if(driftAbs < 15){
            return "NONE";
        } else if (driftAbs < 35) {
            return "MINOR";
        } else {
            return "MAJOR";
        }
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

