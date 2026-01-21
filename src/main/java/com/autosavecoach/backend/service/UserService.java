package com.autosavecoach.backend.service;

import com.autosavecoach.backend.exception.BadRequestException;
import com.autosavecoach.backend.exception.ConflictException;
import com.autosavecoach.backend.model.User;
import com.autosavecoach.backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService{
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User createUser(User user){

        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new BadRequestException("Email is required");
        }

        if (user.getName() == null || user.getName().isBlank()) {
            throw new BadRequestException("Name is required");
        }

        if (user.getPassword() == null || user.getPassword().isBlank()) {
            throw new BadRequestException("Password is required");
        }

        if (user.getPassword().length() < 6) {
            throw new BadRequestException("Password must be at least 6 characters");
        }

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new ConflictException("Email already registered");
        }

        user.setPassword(
                passwordEncoder.encode(user.getPassword())
        );

        // Save user
        return userRepository.save(user);
    }
}