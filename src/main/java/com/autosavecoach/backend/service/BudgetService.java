package com.autosavecoach.backend.service;

import com.autosavecoach.backend.dto.BudgetRequest;
import com.autosavecoach.backend.dto.BudgetResponse;
import com.autosavecoach.backend.model.Budget;
import com.autosavecoach.backend.model.Category;
import com.autosavecoach.backend.model.User;
import com.autosavecoach.backend.repository.BudgetRepository;
import com.autosavecoach.backend.repository.UserRepository;
import com.autosavecoach.backend.util.CategoryUtil;
import com.autosavecoach.backend.util.MonthUtil;
import org.springframework.stereotype.Service;

import java.time.YearMonth;

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

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Budget budget = new Budget();
        budget.setUser(user);
        budget.setCategory(CategoryUtil.parse(request.getCategory()));
        budget.setAmount(request.getAmount());
        budget.setMonth(MonthUtil.parse(request.getMonth()));

        Budget saved = budgetRepository.save(budget);

        return mapToResponse(saved);
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


