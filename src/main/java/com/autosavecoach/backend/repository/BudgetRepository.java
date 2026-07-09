package com.autosavecoach.backend.repository;

import com.autosavecoach.backend.model.Budget;
import com.autosavecoach.backend.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.time.YearMonth;
import java.util.UUID;

public interface BudgetRepository extends JpaRepository<Budget, UUID> {
    Optional<Budget> findByUserIdAndCategoryAndMonth(
            UUID userId,
            Category category,
            YearMonth month
    );

    List<Budget> findByUserId(UUID userId);

    List<Budget> findByUserIdAndMonth(UUID userId, YearMonth month);

    List<Budget> findByUserIdAndCategory(UUID id, Category category);

    @Query("""
    SELECT b FROM Budget b
    WHERE b.user.id = :userId
    AND b.month BETWEEN :start AND :end
    AND (:category IS NULL OR b.category = :category)
    """)
    List<Budget> findBudgetsForAnalytics(
            @Param("userId") UUID userId,
            @Param("start") YearMonth start,
            @Param("end") YearMonth end,
            @Param("category") Category category
    );

    @Query("""
SELECT b
FROM Budget b
WHERE b.user.id = :userId
  AND (:category IS NULL OR b.category = :category)
  AND b.month = (
      SELECT MAX(b2.month)
      FROM Budget b2
      WHERE b2.user.id = :userId
        AND b2.category = b.category
  )
""")
    List<Budget> findLatestBudgetsPerCategory(
            @Param("userId") UUID userId,
            @Param("category") Category category
    );

    @Query("""
    SELECT COALESCE(SUM(b.amount), 0)
    FROM Budget b
    WHERE b.user.id = :userId
      AND b.month = :month
""")
    double sumBudgetsForMonth(
            @Param("userId") UUID userId,
            @Param("month") YearMonth month
    );
}