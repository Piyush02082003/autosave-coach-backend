package com.autosavecoach.backend.repository;

import com.autosavecoach.backend.model.Budget;
import com.autosavecoach.backend.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.time.YearMonth;

public interface BudgetRepository extends JpaRepository<Budget, Long> {
    Optional<Budget> findByUserIdAndCategoryAndMonth(
            Long userId,
            Category category,
            YearMonth month
    );

    List<Budget> findByUserId(Long userId);

    List<Budget> findByUserIdAndMonth(Long userId, YearMonth month);

    List<Budget> findByUserIdAndCategory(Long id, Category category);
}