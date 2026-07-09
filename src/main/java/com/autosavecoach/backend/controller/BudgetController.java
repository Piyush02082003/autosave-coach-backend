package com.autosavecoach.backend.controller;

import com.autosavecoach.backend.dto.BudgetRequest;
import com.autosavecoach.backend.dto.BudgetResponse;
import com.autosavecoach.backend.model.User;
import com.autosavecoach.backend.service.BudgetService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

import java.util.List;
import java.util.UUID;

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

    @GetMapping("/{id}")
    public BudgetResponse getBudgetById(@PathVariable UUID id){
        return budgetService.getBudgetById(id);
    }

    @GetMapping
    public List<BudgetResponse> getBudgets(
            @RequestParam(required = false) String month,
            @RequestParam(required = false) String category
    ) {
        return budgetService.getBudgets(month, category);
    }
}


