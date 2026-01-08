package com.autosavecoach.backend.util;

import com.autosavecoach.backend.exception.InvalidCategoryException;
import com.autosavecoach.backend.model.Category;

public class CategoryUtil {

    public static Category parse(String category) {

        if (category == null || category.trim().isEmpty()) {
            throw new InvalidCategoryException("Category cannot be empty");
        }

        try {
            return Category.valueOf(category.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new InvalidCategoryException(
                    "Invalid category: " + category
            );
        }

    }
}

