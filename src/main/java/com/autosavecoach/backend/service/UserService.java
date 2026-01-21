package com.autosavecoach.backend.service;

import com.autosavecoach.backend.dto.LoginRequest;
import com.autosavecoach.backend.exception.BadRequestException;
import com.autosavecoach.backend.exception.ConflictException;
import com.autosavecoach.backend.exception.UnauthorizedException;
import com.autosavecoach.backend.model.User;
import com.autosavecoach.backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService{
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    //  User Sign up
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


    // User Login
    public User login(LoginRequest request){
        if(request.getEmail() == null || request.getEmail().isBlank()){
            throw new BadRequestException("Email is required");
        }

        if(request.getPassword() == null || request.getPassword().isBlank()){
            throw new BadRequestException("Password is required");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                    new UnauthorizedException("Invalid email or password")
                );

        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())){
            throw new UnauthorizedException("Invalid email or password");
        }

        return user;
    }

    public String generateTokenForUser(User user) {
        return jwtService.generateToken(user.getEmail());
    }
}