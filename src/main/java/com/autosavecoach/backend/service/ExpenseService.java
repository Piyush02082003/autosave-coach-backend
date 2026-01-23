package com.autosavecoach.backend.service;

import java.time.LocalDate;
import java.util.List;
import java.time.YearMonth;
import java.util.Map;
import java.util.stream.Collectors;

import com.autosavecoach.backend.dto.ExpenseRequest;
import com.autosavecoach.backend.dto.ExpenseResponse;
import com.autosavecoach.backend.exception.InvalidCategoryException;
import com.autosavecoach.backend.exception.InvalidDateException;
import com.autosavecoach.backend.model.Category;
import com.autosavecoach.backend.model.User;
import com.autosavecoach.backend.model.Expense;
import com.autosavecoach.backend.repository.UserRepository;
import com.autosavecoach.backend.repository.ExpenseRepository;
import com.autosavecoach.backend.util.CategoryUtil;
import com.autosavecoach.backend.util.DateUtil;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;

    public ExpenseService(ExpenseRepository expenseRepository, UserRepository userRepository) {
        this.expenseRepository = expenseRepository;
        this.userRepository = userRepository;
    }

    private User getCurrentUser() {

        System.out.println("AUTH = " + SecurityContextHolder.getContext().getAuthentication());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Unauthenticated request");
        }

        String email = authentication.getPrincipal().toString();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public ExpenseResponse addExpense(ExpenseRequest request) {
        validateDate(request.getDate());

        User user = getCurrentUser();

        Expense expense = new Expense();
        expense.setTitle(request.getTitle());
        expense.setAmount(request.getAmount());
        expense.setDate(request.getDate());
        expense.setCategory(parseCategory(request.getCategory()));
        expense.setUser(user);

        Expense saved = expenseRepository.save(expense);

        return mapToResponse(saved);
    }

    private void validateDate(LocalDate date) {
        if (date.isAfter(LocalDate.now())) {
            throw new InvalidDateException("Expense date cannot be in the future");
        }
    }

    private Category parseCategory(String category) {
        try {
            return Category.valueOf(category.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidCategoryException("Invalid category: " + category);
        }
    }

    public List<ExpenseResponse> getMyExpenses() {
        User user = getCurrentUser();

        return expenseRepository.findByUserId(user.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ExpenseResponse getExpenseById(Long expenseId) {
        User user = getCurrentUser();

        Expense expense = expenseRepository
                .findByIdAndUserId(expenseId, user.getId())
                .orElseThrow(() -> new RuntimeException("Expense not found"));

        return mapToResponse(expense);
    }

    public Double getTotalSpent() {
        User user = getCurrentUser();

        return expenseRepository.findByUserId(user.getId())
                .stream()
                .mapToDouble(Expense::getAmount)
                .sum();
    }

    public Map<YearMonth, Double> getMonthlySpend() {
        User user = getCurrentUser();

        return expenseRepository.findByUserId(user.getId())
                .stream()
                .collect(Collectors.groupingBy(
                        expense -> YearMonth.from(expense.getDate()),
                        Collectors.summingDouble(Expense::getAmount)
                ));
    }

    public Map<Category, Double> getCategoryWiseSpend() {
        User user = getCurrentUser();

        return expenseRepository.findByUserId(user.getId())
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
