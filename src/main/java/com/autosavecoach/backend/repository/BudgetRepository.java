package com.autosavecoach.backend.repository;

import com.autosavecoach.backend.model.Budget;
import com.autosavecoach.backend.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.time.YearMonth;

public interface BudgetRepository extends JpaRepository<Budget, Long> {

    Optional<Budget> findByUserIdAndCategoryAndMonth(
            Long userId,
            Category category,
            String month
    );

}


