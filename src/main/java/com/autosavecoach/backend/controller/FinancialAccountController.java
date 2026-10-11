package com.autosavecoach.backend.controller;

import com.autosavecoach.backend.model.FinancialAccount;
import com.autosavecoach.backend.model.User;
import com.autosavecoach.backend.repository.FinancialAccountRepository;
import com.autosavecoach.backend.repository.UserRepository;
import com.autosavecoach.backend.model.AccountSource;
import com.autosavecoach.backend.model.AccountType;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class FinancialAccountController {

    private final FinancialAccountRepository accountRepository;
    private final UserRepository userRepository;

    @PostMapping("/demo")
    public ResponseEntity<AccountResponse> createDemoAccount(
            Authentication authentication) {

        User user = getAuthenticatedUser(authentication);

        FinancialAccount account = new FinancialAccount();
        account.setUser(user);
        account.setName("Demo Savings Account");
        account.setInstitutionName("Autosave Coach Demo");
        account.setAccountType(AccountType.SAVINGS);
        account.setMaskedAccountNumber("****0000");
        account.setCurrency("INR");
        account.setSource(AccountSource.DEMO);

        FinancialAccount savedAccount = accountRepository.save(account);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toResponse(savedAccount));
    }

    @GetMapping
    public ResponseEntity<List<AccountResponse>> getAccounts(
            Authentication authentication) {

        User user = getAuthenticatedUser(authentication);

        List<AccountResponse> accounts = accountRepository
                .findByUserId(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(accounts);
    }

    private User getAuthenticatedUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new org.springframework.security.authentication
                    .AuthenticationCredentialsNotFoundException(
                    "Authentication required");
        }

        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new org.springframework.security.core.userdetails
                                .UsernameNotFoundException(
                                "Authenticated user not found"));
    }

    private AccountResponse toResponse(FinancialAccount account) {
        return new AccountResponse(
                account.getId(),
                account.getName(),
                account.getInstitutionName(),
                account.getAccountType().name(),
                account.getMaskedAccountNumber(),
                account.getCurrency(),
                account.getSource().name()
        );
    }

    public record AccountResponse(
            UUID id,
            String name,
            String institutionName,
            String accountType,
            String maskedAccountNumber,
            String currency,
            String source
    ) {}
}