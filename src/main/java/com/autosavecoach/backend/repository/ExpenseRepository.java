package com.autosavecoach.backend.repository;

import java.util.*;
import java.time.LocalDate;
import java.util.stream.Collectors;

import com.autosavecoach.backend.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ExpenseRepository extends JpaRepository<Transaction, UUID> {
    List<Transaction> findByUserId(UUID userId);

    Optional<Transaction> findByIdAndUserId(UUID id, UUID userId);

    List<Transaction> findByUserIdAndCategoryAndDateBetween(
            UUID userId,
            TransactionCategory transactionCategory,
            LocalDate startDate,
            LocalDate endDate
    );

    List<Transaction> findByUserIdAndCategory(
            UUID userId,
            TransactionCategory transactionCategory
    );

    List<Transaction> findByUserIdAndDateBetween(
            UUID userId,
            LocalDate startDate,
            LocalDate endDate
    );

    @Query("""
        SELECT e.transactionCategory, SUM(e.amount)
        FROM Expense e
        WHERE e.user.id = :userId
        AND e.date BETWEEN :startDate AND :endDate
        GROUP BY e.transactionCategory
    """)
    List<Object[]> sumExpensesRaw(
            @Param("userId") UUID userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    default Map<TransactionCategory, Double> sumExpensesByCategory(
            UUID userId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        return sumExpensesRaw(userId, startDate, endDate)
                .stream()
                .collect(Collectors.toMap(
                        r -> (TransactionCategory) r[0],
                        r -> (Double) r[1]
                ));
    }

    @Query("""
SELECT 
    e.transactionCategory,
    YEAR(e.date),
    MONTH(e.date),
    SUM(e.amount)
FROM Expense e
WHERE e.user.id = :userId
  AND e.date >= :fromDate
GROUP BY e.transactionCategory, YEAR(e.date), MONTH(e.date)
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
      AND e.transactionCategory = :transactionCategory
      AND e.date >= :fromDate
""")
    double avgDailySpend(
            @Param("userId") UUID userId,
            @Param("transactionCategory") TransactionCategory transactionCategory,
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



