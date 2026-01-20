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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @PostMapping
    public ExpenseResponse addExpense(@Valid @RequestBody ExpenseRequest request) {
        return expenseService.saveExpense(request);
    }

    @GetMapping("/{userId}")
    public List<ExpenseResponse> getUserExpenses(@PathVariable Long userId){
        return expenseService.getExpensesByUser(userId);
    }

    @GetMapping("/{userId}/total")
    public Double getTotalSpent(@PathVariable Long userId) {
        return expenseService.getTotalSpentByUser(userId);
    }

    @GetMapping("/{userId}/monthly")
    public Map<YearMonth, Double> getMonthlySpend(@PathVariable Long userId) {
        return expenseService.getMonthlySpend(userId);
    }

    @GetMapping("/{userId}/category")
    public Map<Category, Double> getCategoryWiseSpend(@PathVariable Long userId) {
        return expenseService.getCategoryWiseSpend(userId);
    }
}
