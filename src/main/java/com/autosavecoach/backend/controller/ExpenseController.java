package com.autosavecoach.backend.controller;

import java.time.LocalDate;
import java.util.List;
import java.time.YearMonth;
import java.util.Map;
import java.util.UUID;

import com.autosavecoach.backend.dto.BurnRateResponse;
import com.autosavecoach.backend.dto.request.ExpenseRequest;
import com.autosavecoach.backend.dto.response.ExpenseResponse;
import com.autosavecoach.backend.model.Category;
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

    // Add Expense
    @PostMapping
    public ExpenseResponse addExpense(@Valid @RequestBody ExpenseRequest request) {
        // System.out.println("hello");
        // System.out.println("AUTH = " + SecurityContextHolder.getContext().getAuthentication());
        return expenseService.addExpense(request);
    }

    // Get Expense of logged-in user
    @GetMapping
    public List<ExpenseResponse> getMyExpenses(){
        return expenseService.getMyExpenses();
    }

    // Get single expense by id
    @GetMapping("/{expenseId}")
    public ExpenseResponse getExpenseById(@PathVariable UUID expenseId) {
        return expenseService.getExpenseById(expenseId);
    }

    // Total spent by user
    @GetMapping("/total")
    public Double getTotalSpent() {
        return expenseService.getTotalSpent();
    }

    // Monthly expense by user
    @GetMapping("/monthly")
    public Map<YearMonth, Double> getMonthlySpend() {
        return expenseService.getMonthlySpend();
    }

    // Category wise total spent by user
    @GetMapping("/category")
    public Map<Category, Double> getCategoryWiseSpend() {
        return expenseService.getCategoryWiseSpend();
    }

    // Weekly expense of user
    @GetMapping("/weekly")
    public Map<LocalDate, Double> getWeeklySpend() {
        return expenseService.getWeeklySpend();
    }

    // Expense in range
    @GetMapping("/range")
    public List<ExpenseResponse> getExpensesInRange(@RequestParam LocalDate from, @RequestParam LocalDate to){
        return expenseService.getExpensesInRange(from, to);
    }

    // Burn-Rate
    @GetMapping("/burn-rate")
    public BurnRateResponse getBurnRate(){
        return expenseService.getMonthlyBurnRate();
    }
}
