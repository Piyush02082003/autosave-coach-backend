package com.autosavecoach.backend.service;

import com.autosavecoach.backend.dto.*;
import com.autosavecoach.backend.exception.BadRequestException;
import com.autosavecoach.backend.model.Budget;
import com.autosavecoach.backend.model.User;
import com.autosavecoach.backend.repository.BudgetRepository;
import com.autosavecoach.backend.repository.ExpenseRepository;
import com.autosavecoach.backend.repository.UserRepository;
import com.autosavecoach.backend.model.Category;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
    private final CategoryService categoryService;

    public BudgetAnalyticsService(
            BudgetRepository budgetRepository,
            ExpenseRepository expenseRepository,
            UserRepository userRepository,
            CategoryService categoryService
    ) {
        this.budgetRepository = budgetRepository;
        this.expenseRepository = expenseRepository;
        this.userRepository = userRepository;
        this.categoryService = categoryService;
    }

    public List<BudgetAnalyticsResponse> getBudgetSummary(
            YearMonth startMonth,
            YearMonth endMonth,
            String categoryFilter
    ) {

        User user = getCurrentUser();

        System.out.println(user);

        Category category = (categoryFilter != null)
                ? categoryService.getCategoryForUser(categoryFilter, user)
                : null;

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

            Map<Category, BigDecimal> expenseMap =
                    expenseRepository.sumExpensesByCategory(
                            user.getId(),
                            startDate,
                            endDate
                    );

            BigDecimal spent = expenseMap.getOrDefault(
                    budget.getCategory(),
                    BigDecimal.ZERO
            );

            BigDecimal budgetAmount = budget.getAmount();

            BigDecimal remaining = budgetAmount.subtract(spent);

            BigDecimal percentageUsed =
                    budgetAmount.compareTo(BigDecimal.ZERO) == 0
                            ? BigDecimal.ZERO
                            : spent
                            .divide(budgetAmount, 6, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100));

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

    public List<BudgetCalibrationResponse> getCalibration(
            int month,
            String categoryFilter
    ) {

        User user = getCurrentUser();

        LocalDate fromDate = LocalDate.now().minusMonths(month);

        Category filter = (categoryFilter != null)
                ? categoryService.getCategoryForUser(categoryFilter, user)
                : null;

        // 1️⃣ Monthly totals
        List<Object[]> rows =
                expenseRepository.avgSpendLastMonths(
                        user.getId(),
                        fromDate
                );

        // 2️⃣ category → list of monthly totals
        Map<Category, List<BigDecimal>> monthlyMap = new HashMap<>();

        for (Object[] r : rows) {

            Category cat = (Category) r[0];

            BigDecimal monthlyTotal =
                    r[3] instanceof BigDecimal
                            ? (BigDecimal) r[3]
                            : BigDecimal.valueOf(
                            ((Number) r[3]).doubleValue()
                    );

            monthlyMap
                    .computeIfAbsent(cat, k -> new ArrayList<>())
                    .add(monthlyTotal);
        }

        // 3️⃣ category → avg monthly spend
        Map<Category, BigDecimal> avgMonthlySpend = new HashMap<>();

        for (var entry : monthlyMap.entrySet()) {

            BigDecimal total = entry.getValue()
                    .stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal average = entry.getValue().isEmpty()
                    ? BigDecimal.ZERO
                    : total.divide(
                    BigDecimal.valueOf(entry.getValue().size()),
                    6,
                    RoundingMode.HALF_UP
            );

            avgMonthlySpend.put(
                    entry.getKey(),
                    average
            );
        }

        // 4️⃣ Latest budget per category (IMPORTANT)
        List<Budget> budgets =
                budgetRepository.findLatestBudgetsPerCategory(
                        user.getId(),
                        filter
                );

        List<BudgetCalibrationResponse> result = new ArrayList<>();

        for (Budget budget : budgets) {

            Category cat = budget.getCategory();

            BigDecimal avgSpend =
                    avgMonthlySpend.getOrDefault(
                            cat,
                            BigDecimal.ZERO
                    );

            BigDecimal current = budget.getAmount();

            BigDecimal deviation =
                    avgSpend.compareTo(BigDecimal.ZERO) == 0
                            ? BigDecimal.ZERO
                            : current
                            .subtract(avgSpend)
                            .divide(
                                    avgSpend,
                                    6,
                                    RoundingMode.HALF_UP
                            )
                            .multiply(BigDecimal.valueOf(100));

            String status;

            if (current.compareTo(
                    avgSpend.multiply(BigDecimal.valueOf(0.85))
            ) < 0) {

                status = "UNDERSET";

            } else if (current.compareTo(
                    avgSpend.multiply(BigDecimal.valueOf(1.25))
            ) > 0) {

                status = "OVERSET";

            } else {

                status = "WELL_CALIBRATED";
            }

            BigDecimal recommended =
                    avgSpend
                            .multiply(BigDecimal.valueOf(1.10));

            result.add(
                    new BudgetCalibrationResponse(
                            cat,
                            current,
                            avgSpend,
                            round(recommended),
                            status,
                            round(deviation)
                    )
            );
        }

        return result;
    }

    public List<BudgetDriftResponse> calDrift(
            YearMonth month,
            String category
    ) {

        User user = getCurrentUser();

        Category filter = category == null
                ? null
                : categoryService.getCategoryForUser(
                category,
                user
        );

        LocalDate recentStart = month.atDay(1);
        LocalDate recentEnd = month.atEndOfMonth();

        System.out.println(
                recentStart + " " + recentEnd
        );

        LocalDate historyStart =
                month.minusMonths(3).atDay(1);

        LocalDate historyEnd =
                month.minusMonths(1).atEndOfMonth();

        System.out.println(
                historyStart + " " + historyEnd
        );

        Map<Category, BigDecimal> recentSpend =
                expenseRepository.sumExpensesByCategory(
                        user.getId(),
                        recentStart,
                        recentEnd
                );

        Map<Category, BigDecimal> historicalSpend =
                expenseRepository.sumExpensesByCategory(
                        user.getId(),
                        historyStart,
                        historyEnd
                );

        List<BudgetDriftResponse> result = new ArrayList<>();

        for (Category cat : recentSpend.keySet()) {

            if (filter != null && cat != filter) {
                continue;
            }

            BigDecimal recentAvg =
                    round(
                            recentSpend.getOrDefault(
                                    cat,
                                    BigDecimal.ZERO
                            )
                    );

            BigDecimal historicalAvg =
                    round(
                            historicalSpend.getOrDefault(
                                    cat,
                                    BigDecimal.ZERO
                            ).divide(
                                    BigDecimal.valueOf(3),
                                    6,
                                    RoundingMode.HALF_UP
                            )
                    );

            BigDecimal driftPercent =
                    historicalAvg.compareTo(BigDecimal.ZERO) == 0
                            ? BigDecimal.ZERO
                            : round(
                            recentAvg
                                    .subtract(historicalAvg)
                                    .divide(
                                            historicalAvg,
                                            6,
                                            RoundingMode.HALF_UP
                                    )
                                    .multiply(
                                            BigDecimal.valueOf(100)
                                    )
                    );

            result.add(
                    new BudgetDriftResponse(
                            month,
                            cat,
                            determineDriftStatus(driftPercent),
                            recentAvg,
                            historicalAvg,
                            driftPercent
                    )
            );
        }

        return result;
    }

    public BudgetFeasibilityResponse calFeasibility() {

        User user = getCurrentUser();

        YearMonth month = YearMonth.now();

        LocalDate start = month.atDay(1);
        LocalDate today = LocalDate.now();
        LocalDate end = month.atEndOfMonth();

        int daysLeft =
                (int) ChronoUnit.DAYS.between(today, end) + 1;

        if (daysLeft <= 0) {
            throw new BadRequestException(
                    "Month already ended"
            );
        }

        BigDecimal totalBudget =
                budgetRepository.sumBudgetsForMonth(
                        user.getId(),
                        month
                );

        BigDecimal spentSoFar =
                expenseRepository.sumExpenses(
                        user.getId(),
                        start,
                        today
                );

        BigDecimal remainingBudget =
                totalBudget.subtract(spentSoFar);

        BigDecimal requiredPerDay =
                remainingBudget.compareTo(BigDecimal.ZERO) <= 0
                        ? BigDecimal.ZERO
                        : remainingBudget.divide(
                        BigDecimal.valueOf(daysLeft),
                        6,
                        RoundingMode.HALF_UP
                );

        BigDecimal overallHistory =
                expenseRepository.avgDailySpend(
                        user.getId(),
                        null,
                        LocalDate.now().minusMonths(3)
                );

        OverallFeasibility overall =
                new OverallFeasibility(
                        round(totalBudget),
                        round(spentSoFar),
                        round(remainingBudget),
                        daysLeft,
                        round(requiredPerDay),
                        determineFeasibility(
                                requiredPerDay,
                                overallHistory
                        )
                );

        Map<Category, BigDecimal> spentByCategory =
                expenseRepository.sumExpensesByCategory(
                        user.getId(),
                        start,
                        today
                );

        Map<Category, BigDecimal> budgetByCategory =
                budgetRepository.findByUserIdAndMonth(
                                user.getId(),
                                month
                        )
                        .stream()
                        .collect(Collectors.toMap(
                                Budget::getCategory,
                                Budget::getAmount
                        ));

        List<CategoryFeasibility> categories =
                new ArrayList<>();

        for (Category cat : budgetByCategory.keySet()) {

            BigDecimal catBudget =
                    budgetByCategory.getOrDefault(
                            cat,
                            BigDecimal.ZERO
                    );

            BigDecimal catSpent =
                    spentByCategory.getOrDefault(
                            cat,
                            BigDecimal.ZERO
                    );

            BigDecimal remaining =
                    catBudget.subtract(catSpent);

            BigDecimal requiredDaily =
                    remaining.compareTo(BigDecimal.ZERO) <= 0
                            ? BigDecimal.ZERO
                            : remaining.divide(
                            BigDecimal.valueOf(daysLeft),
                            6,
                            RoundingMode.HALF_UP
                    );

            BigDecimal historyPerDay =
                    expenseRepository.avgDailySpend(
                            user.getId(),
                            cat,
                            LocalDate.now().minusMonths(3)
                    );

            categories.add(
                    new CategoryFeasibility(
                            cat,
                            round(requiredDaily),
                            round(historyPerDay),
                            determineFeasibility(
                                    requiredDaily,
                                    historyPerDay
                            )
                    )
            );
        }

        return new BudgetFeasibilityResponse(
                month,
                overall,
                categories
        );
    }

    private BigDecimal round(BigDecimal value) {

        return value.setScale(
                2,
                RoundingMode.HALF_UP
        );
    }

    private String determineFeasibility(
            BigDecimal requiredPerDay,
            BigDecimal historyPerDay
    ) {

        if (historyPerDay.compareTo(BigDecimal.ZERO) <= 0) {
            return "UNKNOWN";
        }

        BigDecimal safeLimit =
                historyPerDay.multiply(
                        BigDecimal.valueOf(1.1)
                );

        if (requiredPerDay.compareTo(safeLimit) <= 0) {
            return "SAFE";
        }

        BigDecimal tightLimit =
                historyPerDay.multiply(
                        BigDecimal.valueOf(1.4)
                );

        if (requiredPerDay.compareTo(tightLimit) <= 0) {
            return "TIGHT";
        }

        return "UNLIKELY";
    }

    private String determineDriftStatus(
            BigDecimal driftPercent
    ) {

        BigDecimal driftAbs =
                driftPercent.abs();

        if (driftAbs.compareTo(
                BigDecimal.valueOf(15)
        ) < 0) {

            return "NONE";

        } else if (driftAbs.compareTo(
                BigDecimal.valueOf(35)
        ) < 0) {

            return "MINOR";

        } else {

            return "MAJOR";
        }
    }

    private String determineStatus(
            BigDecimal spent,
            BigDecimal percentageUsed
    ) {

        if (spent.compareTo(BigDecimal.ZERO) == 0) {
            return "NOT_STARTED";
        }

        if (percentageUsed.compareTo(
                BigDecimal.valueOf(70)
        ) < 0) {

            return "ON_TRACK";
        }

        if (percentageUsed.compareTo(
                BigDecimal.valueOf(90)
        ) < 0) {

            return "WARNING";
        }

        if (percentageUsed.compareTo(
                BigDecimal.valueOf(100)
        ) <= 0) {

            return "LIMIT_REACHED";
        }

        return "EXCEEDED";
    }

    private User getCurrentUser() {

        Authentication auth =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException(
                    "Unauthenticated request"
            );
        }

        return userRepository
                .findByEmail(auth.getPrincipal().toString())
                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );
    }
}