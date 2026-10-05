package com.autosavecoach.backend.service;

import com.autosavecoach.backend.exception.InvalidCategoryException;
import com.autosavecoach.backend.model.Category;
import com.autosavecoach.backend.model.User;
import com.autosavecoach.backend.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public Category getCategoryForUser(String categoryName, User user) {

        if (categoryName == null || categoryName.trim().isEmpty()) {
            throw new InvalidCategoryException("Category cannot be empty");
        }

        String normalizedName = categoryName.trim();

        return categoryRepository
                .findByNameIgnoreCaseAndSystemTrue(normalizedName)
                .or(() -> categoryRepository.findByNameIgnoreCaseAndUser(
                        normalizedName, user
                ))
                .orElseThrow(() ->
                        new InvalidCategoryException(
                                "Category not found: " + categoryName
                        )
                );
    }

    public List<Category> getCategoriesForUser(User user) {
        return categoryRepository.findBySystemTrueOrUser(user);
    }
}