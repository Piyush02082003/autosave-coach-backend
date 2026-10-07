package com.autosavecoach.backend.provider;

import com.autosavecoach.backend.dto.RawTransactionDto;
import com.autosavecoach.backend.model.FinancialAccount;
import java.util.List;

public interface TransactionProvider {
    List<RawTransactionDto> fetchTransactions(FinancialAccount account);
}
