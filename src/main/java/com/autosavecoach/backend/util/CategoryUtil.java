//package com.autosavecoach.backend.util;
//
//import com.autosavecoach.backend.exception.InvalidCategoryException;
//
//public class CategoryUtil {
//
//    public static TransactionCategory parse(String category) {
//
//        if (category == null || category.trim().isEmpty()) {
//            throw new InvalidCategoryException("Category cannot be empty");
//        }
//
//        try {
//            return TransactionCategory.valueOf(category.trim().toUpperCase());
//        } catch (IllegalArgumentException ex) {
//            throw new InvalidCategoryException(
//                    "Invalid category: " + category
//            );
//        }
//
//    }
//}
//
