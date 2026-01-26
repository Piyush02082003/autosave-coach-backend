package com.autosavecoach.backend.controller;

import com.autosavecoach.backend.dto.BudgetRequest;
import com.autosavecoach.backend.dto.BudgetResponse;
import com.autosavecoach.backend.service.BudgetService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @PostMapping
    public BudgetResponse setBudget(@Valid @RequestBody BudgetRequest request) {
        return budgetService.setBudget(request);
    }

    @GetMapping
    public List<BudgetResponse> getAllBudgets(@RequestParam(required = false) String month){
        if(month == null){
            return budgetService.getAllBudgets();
        }

        return budgetService.getBudgetsByMonth(month);
    }

    @GetMapping("/{category}")
    public BudgetResponse getBudget(@PathVariable String category, @RequestParam String month){
        return budgetService.getBudget(category, month);
    }
}


