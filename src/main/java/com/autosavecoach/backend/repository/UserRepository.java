package com.autosavecoach.backend.repository;

import com.autosavecoach.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
