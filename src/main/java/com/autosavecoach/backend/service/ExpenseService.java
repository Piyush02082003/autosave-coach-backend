//package com.autosavecoach.backend.service;
//
//import java.time.DayOfWeek;
//import java.time.LocalDate;
//import java.util.List;
//import java.time.YearMonth;
//import java.util.Map;
//import java.util.UUID;
//import java.util.stream.Collectors;
//
//import com.autosavecoach.backend.dto.BurnRateResponse;
//import com.autosavecoach.backend.dto.request.ExpenseRequest;
//import com.autosavecoach.backend.dto.response.ExpenseResponse;
//import com.autosavecoach.backend.exception.InvalidCategoryException;
//import com.autosavecoach.backend.exception.InvalidDateException;
//import com.autosavecoach.backend.model.Transaction;
//import com.autosavecoach.backend.model.TransactionCategory;
//import com.autosavecoach.backend.model.User;
//import com.autosavecoach.backend.repository.UserRepository;
//import com.autosavecoach.backend.repository.ExpenseRepository;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.stereotype.Service;
//
//@Service
//public class ExpenseService {
//
//    private final ExpenseRepository expenseRepository;
//    private final UserRepository userRepository;
//
//    public ExpenseService(ExpenseRepository expenseRepository, UserRepository userRepository) {
//        this.expenseRepository = expenseRepository;
//        this.userRepository = userRepository;
//    }
//
//    private User getCurrentUser() {
//
//        System.out.println("AUTH = " + SecurityContextHolder.getContext().getAuthentication());
//
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//
//        if (authentication == null || !authentication.isAuthenticated()) {
//            throw new RuntimeException("Unauthenticated request");
//        }
//
//        String email = authentication.getPrincipal().toString();
//
//        return userRepository.findByEmail(email)
//                .orElseThrow(() -> new RuntimeException("User not found"));
//    }
//
//    public ExpenseResponse addExpense(ExpenseRequest request) {
//        validateDate(request.getDate());
//
//        User user = getCurrentUser();
//
//        Transaction transaction = new Transaction();
//        transaction.setTitle(request.getTitle());
//        transaction.setAmount(request.getAmount());
//        transaction.setDate(request.getDate());
//        transaction.setTransactionCategory(parseCategory(request.getCategory()));
//        transaction.setUser(user);
//
//        Transaction saved = expenseRepository.save(transaction);
//
//        return mapToResponse(saved);
//    }
//
//    private void validateDate(LocalDate date) {
//        if (date.isAfter(LocalDate.now())) {
//            throw new InvalidDateException("Expense date cannot be in the future");
//        }
//    }
//
//    private TransactionCategory parseCategory(String category) {
//        try {
//            return TransactionCategory.valueOf(category.toUpperCase());
//        } catch (IllegalArgumentException e) {
//            throw new InvalidCategoryException("Invalid category: " + category);
//        }
//    }
//
//    public List<ExpenseResponse> getMyExpenses() {
//        User user = getCurrentUser();
//
//        return expenseRepository.findByUserId(user.getId())
//                .stream()
//                .map(this::mapToResponse)
//                .collect(Collectors.toList());
//    }
//
//    public ExpenseResponse getExpenseById(UUID expenseId) {
//        User user = getCurrentUser();
//
//        Transaction transaction = expenseRepository
//                .findByIdAndUserId(expenseId, user.getId())
//                .orElseThrow(() -> new RuntimeException("Expense not found"));
//
//        return mapToResponse(transaction);
//    }
//
//    public Double getTotalSpent() {
//        User user = getCurrentUser();
//
//        return expenseRepository.findByUserId(user.getId())
//                .stream()
//                .mapToDouble(Transaction::getAmount)
//                .sum();
//    }
//
//    public Map<YearMonth, Double> getMonthlySpend() {
//        User user = getCurrentUser();
//
//        return expenseRepository.findByUserId(user.getId())
//                .stream()
//                .collect(Collectors.groupingBy(
//                        transaction -> YearMonth.from(transaction.getDate()),
//                        Collectors.summingDouble(Transaction::getAmount)
//                ));
//    }
//
//    public Map<TransactionCategory, Double> getCategoryWiseSpend() {
//        User user = getCurrentUser();
//
//        return expenseRepository.findByUserId(user.getId())
//                .stream()
//                .collect(Collectors.groupingBy(
//                        Transaction::getTransactionCategory,
//                        Collectors.summingDouble(Transaction::getAmount)
//                ));
//    }
//
//    public Map<LocalDate, Double> getWeeklySpend() {
//        User user = getCurrentUser();
//
//        LocalDate today = LocalDate.now();
//        LocalDate startOfWeek = today.with(DayOfWeek.MONDAY);
//        LocalDate endOfWeek = today.with(DayOfWeek.SUNDAY);
//
//        return expenseRepository.findByUserIdAndDateBetween(
//                user.getId(),
//                startOfWeek,
//                endOfWeek
//        ).stream()
//                .collect(Collectors.groupingBy(
//                        Transaction::getDate,
//                        Collectors.summingDouble(Transaction::getAmount)
//                ));
//    }
//
//
//    public List<ExpenseResponse> getExpensesInRange(LocalDate from, LocalDate to) {
//        if(from.isAfter(to)){
//            throw new RuntimeException("From date cannot be after to date");
//        }
//
//        User user = getCurrentUser();
//
//        return expenseRepository.findByUserIdAndDateBetween(
//                user.getId(),
//                from,
//                to
//        ).stream()
//                .map(this::mapToResponse)
//                .collect(Collectors.toList());
//    }
//
//    public BurnRateResponse getMonthlyBurnRate(){
//        User user = getCurrentUser();
//
//        LocalDate today =  LocalDate.now();
//        YearMonth  currentMonth = YearMonth.from(today);
//
//        LocalDate start = currentMonth.atDay(1);
//
//        List<Transaction> expens = expenseRepository.findByUserIdAndDateBetween(
//                user.getId(),
//                start,
//                today
//        );
//
//        double totalSpent = expens.stream()
//                .mapToDouble(Transaction::getAmount)
//                .sum();
//
//        int daysElapsed = today.getDayOfMonth();
//
//        double burnRate = daysElapsed == 0 ? 0 : totalSpent / daysElapsed;
//
//        return new BurnRateResponse(daysElapsed, totalSpent, burnRate);
//    }
//
//    private ExpenseResponse mapToResponse(Transaction transaction) {
//        return new ExpenseResponse(
//                transaction.getId(),
//                transaction.getTitle(),
//                transaction.getTransactionCategory().name(),
//                transaction.getAmount(),
//                transaction.getDate()
//        );
//    }
//}
