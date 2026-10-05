package com.autosavecoach.backend.repository;

import com.autosavecoach.backend.model.Category;
import com.autosavecoach.backend.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public interface ExpenseRepository extends JpaRepository<Transaction, UUID> {

    List<Transaction> findByFinancialAccountUserId(UUID userId);

    Optional<Transaction> findByIdAndFinancialAccountUserId(
            UUID id,
            UUID userId
    );

    List<Transaction> findByFinancialAccountUserIdAndCategoryAndTransactionDateBetween(
            UUID userId,
            Category category,
            LocalDate startDate,
            LocalDate endDate
    );

    List<Transaction> findByFinancialAccountUserIdAndCategory(
            UUID userId,
            Category category
    );

    List<Transaction> findByFinancialAccountUserIdAndTransactionDateBetween(
            UUID userId,
            LocalDate startDate,
            LocalDate endDate
    );

    @Query("""
        SELECT e.category, SUM(e.amount)
        FROM Transaction e
        WHERE e.financialAccount.user.id = :userId
          AND e.transactionDate BETWEEN :startDate AND :endDate
        GROUP BY e.category
        """)
    List<Object[]> sumExpensesRaw(
            @Param("userId") UUID userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    default Map<Category, BigDecimal> sumExpensesByCategory(
            UUID userId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        return sumExpensesRaw(userId, startDate, endDate)
                .stream()
                .collect(Collectors.toMap(
                        r -> (Category) r[0],
                        r -> (BigDecimal) r[1]
                ));
    }

    @Query("""
        SELECT
            e.category,
            YEAR(e.transactionDate),
            MONTH(e.transactionDate),
            SUM(e.amount)
        FROM Transaction e
        WHERE e.financialAccount.user.id = :userId
          AND e.transactionDate >= :fromDate
        GROUP BY e.category, YEAR(e.transactionDate), MONTH(e.transactionDate)
        """)
    List<Object[]> avgSpendLastMonths(
            @Param("userId") UUID userId,
            @Param("fromDate") LocalDate fromDate
    );

    @Query("""
        SELECT COALESCE(SUM(e.amount), 0)
        FROM Transaction e
        WHERE e.financialAccount.user.id = :userId
          AND e.transactionDate BETWEEN :startDate AND :endDate
        """)
    BigDecimal sumExpenses(
            @Param("userId") UUID userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("""
        SELECT COALESCE(SUM(e.amount) / COUNT(DISTINCT e.transactionDate), 0)
        FROM Transaction e
        WHERE e.financialAccount.user.id = :userId
          AND e.category = :category
          AND e.transactionDate >= :fromDate
        """)
    BigDecimal avgDailySpend(
            @Param("userId") UUID userId,
            @Param("category") Category category,
            @Param("fromDate") LocalDate fromDate
    );

    @Query("""
        SELECT COALESCE(SUM(e.amount) / COUNT(DISTINCT e.transactionDate), 0)
        FROM Transaction e
        WHERE e.financialAccount.user.id = :userId
          AND e.transactionDate >= :fromDate
        """)
    BigDecimal avgDailySpendOverall(
            @Param("userId") UUID userId,
            @Param("fromDate") LocalDate fromDate
    );
}