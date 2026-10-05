package com.autosavecoach.backend.service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.autosavecoach.backend.dto.BurnRateResponse;
import com.autosavecoach.backend.dto.request.ExpenseRequest;
import com.autosavecoach.backend.dto.response.ExpenseResponse;
import com.autosavecoach.backend.exception.InvalidDateException;
import com.autosavecoach.backend.model.Category;
import com.autosavecoach.backend.model.FinancialAccount;
import com.autosavecoach.backend.model.Transaction;
import com.autosavecoach.backend.model.TransactionMode;
import com.autosavecoach.backend.model.TransactionSource;
import com.autosavecoach.backend.model.TransactionType;
import com.autosavecoach.backend.model.User;
import com.autosavecoach.backend.repository.ExpenseRepository;
import com.autosavecoach.backend.repository.FinancialAccountRepository;
import com.autosavecoach.backend.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;
    private final FinancialAccountRepository financialAccountRepository;
    private final CategoryService categoryService;

    public ExpenseService(
            ExpenseRepository expenseRepository,
            UserRepository userRepository,
            FinancialAccountRepository financialAccountRepository,
            CategoryService categoryService
    ) {
        this.expenseRepository = expenseRepository;
        this.userRepository = userRepository;
        this.financialAccountRepository = financialAccountRepository;
        this.categoryService = categoryService;
    }

    private User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Unauthenticated request");
        }

        String email = authentication.getPrincipal().toString();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public ExpenseResponse addExpense(ExpenseRequest request) {

        validateDate(request.getDate());

        User user = getCurrentUser();

        FinancialAccount account =
                financialAccountRepository.findByUserId(user.getId())
                        .stream()
                        .findFirst()
                        .orElseThrow(() ->
                                new RuntimeException("No financial account found")
                        );

        Category category = categoryService.getCategoryForUser(
                request.getCategory(),
                user
        );

        Transaction transaction = new Transaction();

        transaction.setFinancialAccount(account);
        transaction.setAmount(request.getAmount());
        transaction.setTransactionDate(request.getDate());
        transaction.setCategory(category);

        transaction.setType(TransactionType.DEBIT);
        transaction.setTransactionMode(TransactionMode.CASH);
        transaction.setSource(TransactionSource.MANUAL);
        transaction.setCreatedAt(java.time.LocalDateTime.now());

        Transaction saved = expenseRepository.save(transaction);

        return mapToResponse(saved);
    }

    private void validateDate(LocalDate date) {
        if (date.isAfter(LocalDate.now())) {
            throw new InvalidDateException(
                    "Expense date cannot be in the future"
            );
        }
    }

    public List<ExpenseResponse> getMyExpenses() {

        User user = getCurrentUser();

        return expenseRepository.findByFinancialAccountUserId(user.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ExpenseResponse getExpenseById(UUID expenseId) {

        User user = getCurrentUser();

        Transaction transaction = expenseRepository
                .findByIdAndFinancialAccountUserId(
                        expenseId,
                        user.getId()
                )
                .orElseThrow(() ->
                        new RuntimeException("Expense not found")
                );

        return mapToResponse(transaction);
    }

    public Double getTotalSpent() {

        User user = getCurrentUser();

        return expenseRepository
                .findByFinancialAccountUserId(user.getId())
                .stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .doubleValue();
    }

    public Map<YearMonth, Double> getMonthlySpend() {

        User user = getCurrentUser();

        return expenseRepository
                .findByFinancialAccountUserId(user.getId())
                .stream()
                .collect(Collectors.groupingBy(
                        transaction ->
                                YearMonth.from(
                                        transaction.getTransactionDate()
                                ),
                        Collectors.summingDouble(
                                transaction ->
                                        transaction.getAmount().doubleValue()
                        )
                ));
    }

    public Map<Category, Double> getCategoryWiseSpend() {

        User user = getCurrentUser();

        return expenseRepository
                .findByFinancialAccountUserId(user.getId())
                .stream()
                .collect(Collectors.groupingBy(
                        Transaction::getCategory,
                        Collectors.summingDouble(
                                transaction ->
                                        transaction.getAmount().doubleValue()
                        )
                ));
    }

    public Map<LocalDate, Double> getWeeklySpend() {

        User user = getCurrentUser();

        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(DayOfWeek.MONDAY);
        LocalDate endOfWeek = today.with(DayOfWeek.SUNDAY);

        return expenseRepository
                .findByFinancialAccountUserIdAndTransactionDateBetween(
                        user.getId(),
                        startOfWeek,
                        endOfWeek
                )
                .stream()
                .collect(Collectors.groupingBy(
                        Transaction::getTransactionDate,
                        Collectors.summingDouble(
                                transaction ->
                                        transaction.getAmount().doubleValue()
                        )
                ));
    }

    public List<ExpenseResponse> getExpensesInRange(
            LocalDate from,
            LocalDate to
    ) {

        if (from.isAfter(to)) {
            throw new RuntimeException(
                    "From date cannot be after to date"
            );
        }

        User user = getCurrentUser();

        return expenseRepository
                .findByFinancialAccountUserIdAndTransactionDateBetween(
                        user.getId(),
                        from,
                        to
                )
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public BurnRateResponse getMonthlyBurnRate() {

        User user = getCurrentUser();

        LocalDate today = LocalDate.now();
        YearMonth currentMonth = YearMonth.from(today);

        LocalDate start = currentMonth.atDay(1);

        List<Transaction> expens =
                expenseRepository
                        .findByFinancialAccountUserIdAndTransactionDateBetween(
                                user.getId(),
                                start,
                                today
                        );

        double totalSpent = expens.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .doubleValue();

        int daysElapsed = today.getDayOfMonth();

        double burnRate =
                daysElapsed == 0 ? 0 : totalSpent / daysElapsed;

        return new BurnRateResponse(
                daysElapsed,
                totalSpent,
                burnRate
        );
    }

    private ExpenseResponse mapToResponse(Transaction transaction) {

        return new ExpenseResponse(
                transaction.getId(),
                transaction.getMerchantName(),
                transaction.getCategory().getName(),
                transaction.getAmount(),
                transaction.getTransactionDate()
        );
    }
}