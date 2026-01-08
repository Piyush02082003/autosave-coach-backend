package com.autosavecoach.backend.repository;

import java.util.List;
import java.time.LocalDate;

import com.autosavecoach.backend.model.Category;
import com.autosavecoach.backend.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByUserId(Long userId);

    List<Expense> findByUserIdAndCategoryAndDateBetween(
            Long userId,
            Category category,
            LocalDate startDate,
            LocalDate endDate
    );

    List<Expense> findByUserIdAndCategory(
            Long userId,
            Category category
    );
}



