package com.autosavecoach.backend.service;

import com.autosavecoach.backend.dto.BudgetRequest;
import com.autosavecoach.backend.dto.BudgetResponse;
import com.autosavecoach.backend.exception.ForbiddenException;
import com.autosavecoach.backend.exception.InvalidMonthException;
import com.autosavecoach.backend.exception.NotFoundException;
import com.autosavecoach.backend.model.Budget;
import com.autosavecoach.backend.model.Category;
import com.autosavecoach.backend.model.User;
import com.autosavecoach.backend.repository.BudgetRepository;
import com.autosavecoach.backend.repository.UserRepository;
import com.autosavecoach.backend.util.CategoryUtil;
import com.autosavecoach.backend.util.MonthUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.time.DateTimeException;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;

@Service
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final UserRepository userRepository;

    public BudgetService(BudgetRepository budgetRepository,
                         UserRepository userRepository) {
        this.budgetRepository = budgetRepository;
        this.userRepository = userRepository;
    }

    public BudgetResponse setBudget(BudgetRequest request) {

        User user = getCurrentUser();
        Category category = CategoryUtil.parse(request.getCategory());
        YearMonth month = YearMonth.parse(request.getMonth());

        validateMonth(month);

        Budget budget = budgetRepository.findByUserIdAndCategoryAndMonth(
                user.getId(),
                category,
                month
        ).orElseGet(() -> {
            Budget b = new Budget();
            b.setUser(user);
            b.setCategory(category);
            b.setMonth(month);
            return b;
        });

        budget.setAmount(request.getAmount());

        Budget saved = budgetRepository.save(budget);

        return mapToResponse(saved);
    }

    public BudgetResponse getBudgetById(UUID budgetId){

        User user = getCurrentUser();

        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() ->
                        new NotFoundException(
                                "Budget not found with id: " + budgetId
                        )
                );

        if (!budget.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("You are not allowed to access this budget");
        }

        return mapToResponse(budget);
    }

    public List<BudgetResponse> getBudgets(String month, String category) {

        User user = getCurrentUser();

        YearMonth parsedMonth = null;
        if (month != null) {
            try {
                parsedMonth = YearMonth.parse(month);
            } catch (DateTimeParseException e) {
                throw new InvalidMonthException("Month must be in YYYY-MM format");
            }
        }

        Category parsedCategory = null;
        if (category != null) {
            parsedCategory = CategoryUtil.parse(category);
        }

        List<Budget> budgets;

        if (parsedMonth != null && parsedCategory != null) {
            budgets = budgetRepository.findByUserIdAndCategoryAndMonth(
                    user.getId(),
                    parsedCategory,
                    parsedMonth
            ).map(List::of).orElse(List.of());
        }
        else if (parsedMonth != null) {
            budgets = budgetRepository.findByUserIdAndMonth(
                    user.getId(),
                    parsedMonth
            );
        }
        else if (parsedCategory != null) {
            budgets = budgetRepository.findByUserIdAndCategory(
                    user.getId(),
                    parsedCategory
            );
        }
        else {
            budgets = budgetRepository.findByUserId(user.getId());
        }

        return budgets.stream()
                .map(this::mapToResponse)
                .toList();
    }

    private User getCurrentUser() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Unauthenticated request");
        }

        String email = authentication.getPrincipal().toString();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private void validateMonth(YearMonth month) {
        YearMonth currentMonth = YearMonth.now();

        if (month.isBefore(currentMonth)) {
            throw new InvalidMonthException(
                    "Cannot set budget for past month: " + month
            );
        }
    }

    private BudgetResponse mapToResponse(Budget budget) {
        return new BudgetResponse(
                budget.getId(),
                budget.getCategory().name(),
                budget.getAmount(),
                budget.getMonth().toString()
        );
    }
}


