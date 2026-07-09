package com.autosavecoach.backend.repository;

import java.util.*;
import java.time.LocalDate;
import java.util.stream.Collectors;

import com.autosavecoach.backend.model.Category;
import com.autosavecoach.backend.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ExpenseRepository extends JpaRepository<Expense, UUID> {
    List<Expense> findByUserId(UUID userId);

    Optional<Expense> findByIdAndUserId(UUID id, UUID userId);

    List<Expense> findByUserIdAndCategoryAndDateBetween(
            UUID userId,
            Category category,
            LocalDate startDate,
            LocalDate endDate
    );

    List<Expense> findByUserIdAndCategory(
            UUID userId,
            Category category
    );

    List<Expense> findByUserIdAndDateBetween(
            UUID userId,
            LocalDate startDate,
            LocalDate endDate
    );

    @Query("""
        SELECT e.category, SUM(e.amount)
        FROM Expense e
        WHERE e.user.id = :userId
        AND e.date BETWEEN :startDate AND :endDate
        GROUP BY e.category
    """)
    List<Object[]> sumExpensesRaw(
            @Param("userId") UUID userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    default Map<Category, Double> sumExpensesByCategory(
            UUID userId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        return sumExpensesRaw(userId, startDate, endDate)
                .stream()
                .collect(Collectors.toMap(
                        r -> (Category) r[0],
                        r -> (Double) r[1]
                ));
    }

    @Query("""
SELECT 
    e.category,
    YEAR(e.date),
    MONTH(e.date),
    SUM(e.amount)
FROM Expense e
WHERE e.user.id = :userId
  AND e.date >= :fromDate
GROUP BY e.category, YEAR(e.date), MONTH(e.date)
""")
    List<Object[]> avgSpendLastMonths(
            @Param("userId") UUID userId,
            @Param("fromDate") LocalDate fromDate
    );

    @Query("""
    SELECT COALESCE(SUM(e.amount), 0)
    FROM Expense e
    WHERE e.user.id = :userId
      AND e.date BETWEEN :startDate AND :endDate
""")
    double sumExpenses(
            @Param("userId") UUID userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("""
    SELECT COALESCE(SUM(e.amount) / COUNT(DISTINCT e.date), 0)
    FROM Expense e
    WHERE e.user.id = :userId
      AND e.category = :category
      AND e.date >= :fromDate
""")
    double avgDailySpend(
            @Param("userId") UUID userId,
            @Param("category") Category category,
            @Param("fromDate") LocalDate fromDate
    );

    @Query("""
    SELECT COALESCE(SUM(e.amount) / COUNT(DISTINCT e.date), 0)
    FROM Expense e
    WHERE e.user.id = :userId
      AND e.date >= :fromDate
""")
    double avgDailySpendOverall(
            @Param("userId") UUID userId,
            @Param("fromDate") LocalDate fromDate
    );
}



