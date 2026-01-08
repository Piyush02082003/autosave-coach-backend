package com.autosavecoach.backend.service;

import java.util.List;
import java.time.YearMonth;
import java.util.Map;
import java.util.stream.Collectors;

import com.autosavecoach.backend.dto.ExpenseRequest;
import com.autosavecoach.backend.dto.ExpenseResponse;
import com.autosavecoach.backend.model.Category;
import com.autosavecoach.backend.model.User;
import com.autosavecoach.backend.model.Expense;
import com.autosavecoach.backend.repository.UserRepository;
import com.autosavecoach.backend.repository.ExpenseRepository;
import com.autosavecoach.backend.util.CategoryUtil;
import com.autosavecoach.backend.util.DateUtil;
import org.springframework.stereotype.Service;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;

    public ExpenseService(ExpenseRepository expenseRepository, UserRepository userRepository) {
        this.expenseRepository = expenseRepository;
        this.userRepository = userRepository;
    }

    public ExpenseResponse saveExpense(ExpenseRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Expense expense = new Expense();
        expense.setTitle(request.getTitle());
        expense.setAmount(request.getAmount());
        expense.setDate(DateUtil.parse(request.getDate()));
        expense.setCategory(
                CategoryUtil.parse(request.getCategory())
        );
        expense.setUser(user);

        Expense saved = expenseRepository.save(expense);

        return mapToResponse(saved);
    }

    public List<ExpenseResponse> getExpensesByUser(Long userId) {
        return expenseRepository.findByUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public Double getTotalSpentByUser(Long userId) {
        return expenseRepository.findByUserId(userId)
                .stream()
                .mapToDouble(Expense::getAmount)
                .sum();
    }

    public Map<YearMonth, Double> getMonthlySpend(Long userId) {
        return expenseRepository.findByUserId(userId)
                .stream()
                .collect(Collectors.groupingBy(
                        expense -> YearMonth.from(expense.getDate()),
                        Collectors.summingDouble(Expense::getAmount)
                ));
    }

    public Map<Category, Double> getCategoryWiseSpend(Long userId) {
        return expenseRepository.findByUserId(userId)
                .stream()
                .collect(Collectors.groupingBy(
                        Expense::getCategory,
                        Collectors.summingDouble(Expense::getAmount)
                ));
    }

    private ExpenseResponse mapToResponse(Expense expense) {
        return new ExpenseResponse(
                expense.getId(),
                expense.getTitle(),
                expense.getCategory().name(),
                expense.getAmount(),
                expense.getDate()
        );
    }
}
