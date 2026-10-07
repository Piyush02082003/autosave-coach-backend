package com.autosavecoach.backend.dto;
import com.autosavecoach.backend.model.TransactionMode;
import com.autosavecoach.backend.model.TransactionType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class RawTransactionDto {
    private BigDecimal amount;

    private TransactionType type;

    private LocalDate transactionDate;

    private TransactionMode transactionMode;

    private String rawNarration;

    private String externalTransactionId;

    private BigDecimal balance;
}