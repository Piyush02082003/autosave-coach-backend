package com.autosavecoach.backend.repository;

import java.util.Arrays;
import java.util.List;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.autosavecoach.backend.model.Category;
import com.autosavecoach.backend.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByUserId(Long userId);

    Optional<Expense> findByIdAndUserId(Long id, Long userId);

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

    List<Expense> findByUserIdAndDateBetween(
            Long userId,
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
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    default Map<Category, Double> sumExpensesByCategory(
            Long userId,
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
            @Param("userId") Long userId,
            @Param("fromDate") LocalDate fromDate
    );

    @Query("""
    SELECT COALESCE(SUM(e.amount), 0)
    FROM Expense e
    WHERE e.user.id = :userId
      AND e.date BETWEEN :startDate AND :endDate
""")
    double sumExpenses(
            @Param("userId") Long userId,
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
            @Param("userId") Long userId,
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
            @Param("userId") Long userId,
            @Param("fromDate") LocalDate fromDate
    );
}



