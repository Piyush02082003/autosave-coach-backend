package com.autosavecoach.backend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.UUID;

@Entity
@Table(name = "budgets")
@Data
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JoinColumn(name = "user_id", nullable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    private TransactionCategory transactionCategory;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(name = "budget_month")
    private YearMonth month;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
