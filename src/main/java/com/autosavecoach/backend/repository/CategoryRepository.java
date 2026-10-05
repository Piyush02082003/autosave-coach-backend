package com.autosavecoach.backend.repository;

import com.autosavecoach.backend.model.Category;
import com.autosavecoach.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {

    List<Category> findBySystemTrueOrUser(User user);

    Optional<Category> findByNameIgnoreCaseAndSystemTrue(String name);

    Optional<Category> findByNameIgnoreCaseAndUser(String name, User user);
}