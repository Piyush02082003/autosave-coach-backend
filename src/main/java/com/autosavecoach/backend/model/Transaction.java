package com.autosavecoach.backend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transactions")
@Data
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private FinancialAccount financialAccount;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(nullable = false)
    private LocalDate transactionDate;

    private String merchantName;

    @Enumerated(EnumType.STRING)
    private TransactionCategory transactionCategory;

    private String subcategory;

    @Enumerated(EnumType.STRING)
    private TransactionMode transactionMode;

    @Column(length = 500)
    private String rawNarration;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionSource source;

    private String externalTransactionId;

    private BigDecimal balance;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}