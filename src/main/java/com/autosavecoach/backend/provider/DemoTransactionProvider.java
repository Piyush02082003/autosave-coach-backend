package com.autosavecoach.backend.provider;

import com.autosavecoach.backend.dto.RawTransactionDto;
import com.autosavecoach.backend.model.FinancialAccount;
import com.autosavecoach.backend.model.TransactionMode;
import com.autosavecoach.backend.model.TransactionType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class DemoTransactionProvider implements TransactionProvider {

    private static final BigDecimal OPENING_BALANCE =
            new BigDecimal("50000.00");

    private static final Random RANDOM = new Random(42);

    @Override
    public List<RawTransactionDto> fetchTransactions(
            FinancialAccount account
    ) {

        List<RawTransactionDto> transactions = new ArrayList<>();

        BigDecimal balance = OPENING_BALANCE;
        int transactionNumber = 1;

        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2026, 10, 1);

        LocalDate month = startDate;

        while (!month.isAfter(endDate)) {

            int year = month.getYear();

            // =========================
            // SALARY
            // =========================

            BigDecimal salary = switch (year) {
                case 2024 -> new BigDecimal("50000");
                case 2025 -> new BigDecimal("55000");
                default -> new BigDecimal("60000");
            };

            balance = balance.add(salary);

            transactions.add(
                    createTransaction(
                            transactionNumber++,
                            salary,
                            TransactionType.CREDIT,
                            month.withDayOfMonth(1),
                            TransactionMode.BANK_TRANSFER,
                            "SALARY/COMPANY/PAYROLL",
                            balance
                    )
            );

            // =========================
            // RENT
            // =========================

            BigDecimal rent = switch (year) {
                case 2024 -> new BigDecimal("15000");
                case 2025 -> new BigDecimal("16000");
                default -> new BigDecimal("17000");
            };

            balance = balance.subtract(rent);

            transactions.add(
                    createTransaction(
                            transactionNumber++,
                            rent,
                            TransactionType.DEBIT,
                            month.withDayOfMonth(3),
                            TransactionMode.BANK_TRANSFER,
                            "BANK_TRANSFER/RENT/RAHUL_LANDLORD",
                            balance
                    )
            );

            // =========================
            // ELECTRICITY
            // =========================

            BigDecimal electricity = switch (year) {
                case 2024 -> new BigDecimal("1200");
                case 2025 -> new BigDecimal("1400");
                default -> new BigDecimal("1650");
            };

            balance = balance.subtract(electricity);

            transactions.add(
                    createTransaction(
                            transactionNumber++,
                            electricity,
                            TransactionType.DEBIT,
                            month.withDayOfMonth(7),
                            TransactionMode.UPI,
                            "BILL/ELECTRICITY/BSES",
                            balance
                    )
            );

            // =========================
            // MOBILE RECHARGE
            // =========================

            BigDecimal mobileRecharge = switch (year) {
                case 2024 -> new BigDecimal("599");
                case 2025 -> new BigDecimal("699");
                default -> new BigDecimal("799");
            };

            balance = balance.subtract(mobileRecharge);

            transactions.add(
                    createTransaction(
                            transactionNumber++,
                            mobileRecharge,
                            TransactionType.DEBIT,
                            month.withDayOfMonth(8),
                            TransactionMode.UPI,
                            mobileRechargeNarration(),
                            balance
                    )
            );

            // =========================
            // INVESTMENT
            // =========================

            BigDecimal investment = switch (year) {
                case 2024 -> new BigDecimal("5000");
                case 2025 -> new BigDecimal("7000");
                default -> new BigDecimal("10000");
            };

            balance = balance.subtract(investment);

            transactions.add(
                    createTransaction(
                            transactionNumber++,
                            investment,
                            TransactionType.DEBIT,
                            month.withDayOfMonth(10),
                            TransactionMode.UPI,
                            investmentNarration(),
                            balance
                    )
            );

            // =========================
            // SUBSCRIPTION
            // =========================

            BigDecimal subscription = subscriptionAmount();

            balance = balance.subtract(subscription);

            transactions.add(
                    createTransaction(
                            transactionNumber++,
                            subscription,
                            TransactionType.DEBIT,
                            month.withDayOfMonth(12),
                            TransactionMode.CARD,
                            subscriptionNarration(),
                            balance
                    )
            );

            // =========================
            // RANDOM VARIABLE EXPENSES
            // =========================

            for (int i = 0; i < 10; i++) {

                TransactionData data =
                        generateExpense(year);

                int day = 14 + i;

                LocalDate transactionDate =
                        month.withDayOfMonth(day);

                balance = applyBalance(balance, data);

                transactions.add(
                        createTransaction(
                                transactionNumber++,
                                data.amount(),
                                data.type(),
                                transactionDate,
                                data.mode(),
                                data.narration(),
                                balance
                        )
                );
            }

            // Extra transactions for first 24 months
            long monthsFromStart =
                    ChronoUnit.MONTHS.between(
                            startDate.withDayOfMonth(1),
                            month.withDayOfMonth(1)
                    );

            if (monthsFromStart < 24) {

                TransactionData data =
                        generateExpense(year);

                LocalDate transactionDate =
                        month.withDayOfMonth(27);

                balance = applyBalance(balance, data);

                transactions.add(
                        createTransaction(
                                transactionNumber++,
                                data.amount(),
                                data.type(),
                                transactionDate,
                                data.mode(),
                                data.narration(),
                                balance
                        )
                );
            }

            month = month.plusMonths(1);
        }

        return transactions;
    }

    // =========================================================
    // RANDOM EXPENSES
    // =========================================================

    private TransactionData generateExpense(int year) {

        int type = RANDOM.nextInt(18);

        return switch (type) {

            // Food
            case 0 -> new TransactionData(
                    randomAmount(180, 900),
                    TransactionType.DEBIT,
                    TransactionMode.UPI,
                    "UPI/RAHUL123/SWIGGY"
            );

            case 1 -> new TransactionData(
                    randomAmount(200, 1000),
                    TransactionType.DEBIT,
                    TransactionMode.UPI,
                    "UPI/AMIT452/ZOMATO"
            );

            case 2 -> new TransactionData(
                    randomAmount(300, 1500),
                    TransactionType.DEBIT,
                    TransactionMode.CARD,
                    restaurantNarration()
            );

            // Groceries
            case 3 -> new TransactionData(
                    randomAmount(300, 1800),
                    TransactionType.DEBIT,
                    TransactionMode.UPI,
                    "UPI/USER789/BLINKIT"
            );

            case 4 -> new TransactionData(
                    randomAmount(250, 1800),
                    TransactionType.DEBIT,
                    TransactionMode.UPI,
                    "UPI/USER654/ZEPTO"
            );

            case 5 -> new TransactionData(
                    randomAmount(500, 2500),
                    TransactionType.DEBIT,
                    TransactionMode.UPI,
                    "UPI/USER321/SWIGGYINSTAMART"
            );

            // Transport
            case 6 -> new TransactionData(
                    randomAmount(200, 1400),
                    TransactionType.DEBIT,
                    TransactionMode.UPI,
                    "UPI/USER111/UBER"
            );

            case 7 -> new TransactionData(
                    randomAmount(150, 1200),
                    TransactionType.DEBIT,
                    TransactionMode.UPI,
                    "UPI/USER222/OLA"
            );

            case 8 -> new TransactionData(
                    randomAmount(100, 900),
                    TransactionType.DEBIT,
                    TransactionMode.UPI,
                    "UPI/USER333/RAPIDO"
            );

            case 9 -> new TransactionData(
                    randomAmount(100, 800),
                    TransactionType.DEBIT,
                    TransactionMode.UPI,
                    "UPI/USER444/NAMAYATRI"
            );

            // Shopping
            case 10 -> new TransactionData(
                    randomAmount(1000, 6000),
                    TransactionType.DEBIT,
                    TransactionMode.CARD,
                    "POS/AMAZON/ORDER" + RANDOM.nextInt(99999)
            );

            case 11 -> new TransactionData(
                    randomAmount(800, 5000),
                    TransactionType.DEBIT,
                    TransactionMode.CARD,
                    "POS/MYNTRA/ORDER" + RANDOM.nextInt(99999)
            );

            case 12 -> new TransactionData(
                    randomAmount(1000, 6000),
                    TransactionType.DEBIT,
                    TransactionMode.CARD,
                    "POS/FLIPKART/ORDER" + RANDOM.nextInt(99999)
            );

            // Entertainment
            case 13 -> new TransactionData(
                    randomAmount(200, 1200),
                    TransactionType.DEBIT,
                    TransactionMode.CARD,
                    entertainmentNarration()
            );

            // Health
            case 14 -> new TransactionData(
                    randomAmount(300, 3000),
                    TransactionType.DEBIT,
                    TransactionMode.UPI,
                    healthNarration()
            );

            // Personal transfer
            case 15 -> new TransactionData(
                    randomAmount(500, 5000),
                    TransactionType.DEBIT,
                    TransactionMode.UPI,
                    randomPersonTransfer()
            );

            // Cash withdrawal
            case 16 -> new TransactionData(
                    randomAmount(1000, 5000),
                    TransactionType.DEBIT,
                    TransactionMode.ATM,
                    cashWithdrawalNarration()
            );

            // Other
            default -> new TransactionData(
                    randomAmount(200, 2500),
                    TransactionType.DEBIT,
                    TransactionMode.UPI,
                    "UPI/UNKNOWN/MISCELLANEOUS_PAYMENT"
            );
        };
    }

    // =========================================================
    // NARRATIONS
    // =========================================================

    private String mobileRechargeNarration() {

        String[] providers = {
                "UPI/JIO/MOBILE_RECHARGE",
                "UPI/AIRTEL/MOBILE_RECHARGE",
                "UPI/VI/MOBILE_RECHARGE"
        };

        return providers[RANDOM.nextInt(providers.length)];
    }

    private String subscriptionNarration() {

        String[] subscriptions = {
                "CARD/NETFLIX/SUBSCRIPTION",
                "CARD/SPOTIFY/PREMIUM",
                "CARD/YOUTUBE_PREMIUM/SUBSCRIPTION",
                "CARD/AMAZON_PRIME/SUBSCRIPTION"
        };

        return subscriptions[RANDOM.nextInt(subscriptions.length)];
    }

    private BigDecimal subscriptionAmount() {

        String[] providers = {
                "NETFLIX",
                "SPOTIFY",
                "YOUTUBE_PREMIUM",
                "AMAZON_PRIME"
        };

        return switch (
                providers[RANDOM.nextInt(providers.length)]
                ) {
            case "NETFLIX" -> new BigDecimal("649");
            case "SPOTIFY" -> new BigDecimal("119");
            case "YOUTUBE_PREMIUM" -> new BigDecimal("129");
            default -> new BigDecimal("299");
        };
    }

    private String investmentNarration() {

        String[] investments = {
                "UPI/GROWW/MUTUAL_FUND_SIP",
                "UPI/ZERODHA/MUTUAL_FUND_SIP",
                "UPI/GROWW/BONDS",
                "UPI/ZERODHA/STOCK_INVESTMENT",
                "UPI/GROWW/INDEX_FUND"
        };

        return investments[RANDOM.nextInt(investments.length)];
    }

    private String restaurantNarration() {

        String[] restaurants = {
                "CARD/DOMINOS/PIZZA",
                "CARD/MCDONALDS/ORDER",
                "CARD/STARBUCKS/CAFE",
                "UPI/HALDIRAMS/RESTAURANT",
                "UPI/LOCAL_RESTAURANT/FOOD"
        };

        return restaurants[RANDOM.nextInt(restaurants.length)];
    }

    private String entertainmentNarration() {

        String[] entertainment = {
                "UPI/DISTRICT/MOVIE",
                "CARD/BOOKMYSHOW/TICKET",
                "CARD/PVR/CINEMA",
                "CARD/INOX/CINEMA"
        };

        return entertainment[RANDOM.nextInt(entertainment.length)];
    }

    private String healthNarration() {

        String[] health = {
                "UPI/APOLLO_PHARMACY/MEDICINES",
                "UPI/APOLLO_HOSPITAL/HOSPITAL",
                "UPI/MAX_HOSPITAL/CONSULTATION",
                "UPI/DR_SHARMA/DOCTOR_CONSULTATION",
                "CARD/PHARMACY/MEDICINES"
        };

        return health[RANDOM.nextInt(health.length)];
    }

    private String randomPersonTransfer() {

        String[] people = {
                "UPI/ROHIT_SHARMA/PERSONAL_TRANSFER",
                "UPI/AMAN_GUPTA/PERSONAL_TRANSFER",
                "UPI/PRIYA_SINGH/PERSONAL_TRANSFER",
                "UPI/NEHA_VERMA/PERSONAL_TRANSFER",
                "UPI/ARJUN_MEHTA/PERSONAL_TRANSFER"
        };

        return people[RANDOM.nextInt(people.length)];
    }

    private String cashWithdrawalNarration() {

        String[] banks = {
                "ATM/HDFC_BANK/CASH_WITHDRAWAL",
                "ATM/ICICI_BANK/CASH_WITHDRAWAL",
                "ATM/SBI/CASH_WITHDRAWAL",
                "ATM/AXIS_BANK/CASH_WITHDRAWAL"
        };

        return banks[RANDOM.nextInt(banks.length)];
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private BigDecimal applyBalance(
            BigDecimal balance,
            TransactionData data
    ) {

        if (data.type() == TransactionType.CREDIT) {
            return balance.add(data.amount());
        }

        return balance.subtract(data.amount());
    }

    private BigDecimal randomAmount(int min, int max) {

        int amount =
                RANDOM.nextInt(max - min + 1) + min;

        return BigDecimal.valueOf(amount);
    }

    private RawTransactionDto createTransaction(
            int transactionNumber,
            BigDecimal amount,
            TransactionType type,
            LocalDate transactionDate,
            TransactionMode transactionMode,
            String rawNarration,
            BigDecimal balance
    ) {

        RawTransactionDto transaction =
                new RawTransactionDto();

        transaction.setExternalTransactionId(
                "DEMO-" +
                        String.format("%04d", transactionNumber)
        );

        transaction.setAmount(amount);
        transaction.setType(type);
        transaction.setTransactionDate(transactionDate);
        transaction.setTransactionMode(transactionMode);
        transaction.setRawNarration(rawNarration);
        transaction.setBalance(balance);

        return transaction;
    }

    private record TransactionData(
            BigDecimal amount,
            TransactionType type,
            TransactionMode mode,
            String narration
    ) {
    }
}