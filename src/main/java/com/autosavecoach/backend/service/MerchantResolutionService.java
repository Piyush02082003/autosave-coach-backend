package com.autosavecoach.backend.service;

import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class MerchantResolutionService {

    private final Map<String, String> merchantAliases = new LinkedHashMap<>();

    public MerchantResolutionService() {
        merchantAliases.put("SWIGGY", "Swiggy");
        merchantAliases.put("ZOMATO", "Zomato");
        merchantAliases.put("UBER", "Uber");
        merchantAliases.put("OLA", "Ola");
        merchantAliases.put("AMAZON", "Amazon");
        merchantAliases.put("AMZN", "Amazon");
        merchantAliases.put("FLIPKART", "Flipkart");
        merchantAliases.put("MYNTRA", "Myntra");
        merchantAliases.put("NETFLIX", "Netflix");
        merchantAliases.put("SPOTIFY", "Spotify");
        merchantAliases.put("DOMINOS", "Domino's");
        merchantAliases.put("MCDONALDS", "McDonald's");
        merchantAliases.put("STARBUCKS", "Starbucks");
        merchantAliases.put("BIGBASKET", "BigBasket");
        merchantAliases.put("BLINKIT", "Blinkit");
        merchantAliases.put("ZEPTO", "Zepto");
    }

    public String resolveMerchant(String rawNarration) {

        if (rawNarration == null || rawNarration.trim().isEmpty()) {
            return "Unknown";
        }

        String normalizedNarration = rawNarration
                .toUpperCase()
                .replaceAll("[^A-Z0-9]", " ");

        for (Map.Entry<String, String> entry : merchantAliases.entrySet()) {

            String merchantKeyword = entry.getKey();

            if (normalizedNarration.contains(merchantKeyword)) {
                return entry.getValue();
            }
        }

        return extractMerchantName(rawNarration);
    }

    private String extractMerchantName(String rawNarration) {

        String[] parts = rawNarration.split("/");

        for (String part : parts) {

            String cleaned = part.trim();

            if (cleaned.isEmpty()) {
                continue;
            }

            if (cleaned.matches(".*\\d.*")) {
                continue;
            }

            if (cleaned.length() >= 3) {
                return cleaned;
            }
        }

        return "Unknown";
    }
}