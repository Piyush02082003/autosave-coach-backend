package com.autosavecoach.backend.controller;

import java.util.List;
import java.time.YearMonth;
import java.util.Map;

import com.autosavecoach.backend.dto.ExpenseRequest;
import com.autosavecoach.backend.dto.ExpenseResponse;
import com.autosavecoach.backend.model.Category;
import com.autosavecoach.backend.model.Expense;
import com.autosavecoach.backend.service.ExpenseService;
import jakarta.validation.Valid;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    // Add Expense
    @PostMapping
    public ExpenseResponse addExpense(@Valid @RequestBody ExpenseRequest request) {
        System.out.println("hello");
        System.out.println("AUTH = " + SecurityContextHolder.getContext().getAuthentication());
        return expenseService.addExpense(request);
    }

    // Get Expense of logged-in user
    @GetMapping
    public List<ExpenseResponse> getMyExpenses(){
        return expenseService.getMyExpenses();
    }

    // Get single expense by id
    @GetMapping("/{expenseId}")
    public ExpenseResponse getExpenseById(@PathVariable Long expenseId) {
        return expenseService.getExpenseById(expenseId);
    }

    @GetMapping("/total")
    public Double getTotalSpent() {
        return expenseService.getTotalSpent();
    }

    @GetMapping("/monthly")
    public Map<YearMonth, Double> getMonthlySpend() {
        return expenseService.getMonthlySpend();
    }

    @GetMapping("/category")
    public Map<Category, Double> getCategoryWiseSpend() {
        return expenseService.getCategoryWiseSpend();
    }
}
