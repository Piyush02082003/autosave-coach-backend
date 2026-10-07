package com.autosavecoach.backend.service;

import com.autosavecoach.backend.exception.InvalidCategoryException;
import com.autosavecoach.backend.model.Category;
import com.autosavecoach.backend.model.User;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class TransactionCategorizationService {

    private final CategoryService categoryService;

    private final Map<String, String> merchantCategories = new LinkedHashMap<>();

    public TransactionCategorizationService(CategoryService categoryService) {
        this.categoryService = categoryService;

        merchantCategories.put("Swiggy", "Food & Dining");
        merchantCategories.put("Zomato", "Food & Dining");
        merchantCategories.put("Domino's", "Food & Dining");
        merchantCategories.put("McDonald's", "Food & Dining");
        merchantCategories.put("Starbucks", "Food & Dining");

        merchantCategories.put("BigBasket", "Groceries");
        merchantCategories.put("Blinkit", "Groceries");
        merchantCategories.put("Zepto", "Groceries");

        merchantCategories.put("Uber", "Transport");
        merchantCategories.put("Ola", "Transport");

        merchantCategories.put("Amazon", "Shopping");
        merchantCategories.put("Flipkart", "Shopping");
        merchantCategories.put("Myntra", "Shopping");

        merchantCategories.put("Netflix", "Entertainment");
        merchantCategories.put("Spotify", "Entertainment");
    }

    public Category categorize(String merchantName, User user) {

        if (merchantName == null || merchantName.trim().isEmpty()) {
            return getOtherCategory(user);
        }

        String normalizedMerchant = merchantName.trim();

        String categoryName = merchantCategories.entrySet()
                .stream()
                .filter(entry ->
                        entry.getKey().equalsIgnoreCase(normalizedMerchant)
                )
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse("Other");

        return categoryService.getCategoryForUser(categoryName, user);
    }

    private Category getOtherCategory(User user) {

        try {
            return categoryService.getCategoryForUser("Other", user);
        } catch (InvalidCategoryException exception) {
            throw new InvalidCategoryException(
                    "Default category 'Other' is not configured"
            );
        }
    }
}