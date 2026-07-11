package com.autosavecoach.backend.controller;

import com.autosavecoach.backend.dto.request.ChangePasswordRequest;
import com.autosavecoach.backend.dto.request.ForgotPasswordRequest;
import com.autosavecoach.backend.dto.request.LoginRequest;
import com.autosavecoach.backend.dto.request.ResetPasswordRequest;
import com.autosavecoach.backend.dto.response.*;
import com.autosavecoach.backend.exception.UnauthorizedException;
import com.autosavecoach.backend.model.User;
import com.autosavecoach.backend.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // SIGN-UP
    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody User user) {

        User savedUser = userService.createUser(user);
        UserResponse response =  new UserResponse(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Get User Profile
    @GetMapping
    public ResponseEntity<UserResponse> getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("Unauthorized");
        }

        String email = authentication.getName(); // set by JwtAuthFilter

        User user = userService.getUserByEmail(email);

        UserResponse response = new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail()
        );

        return ResponseEntity.ok(response);
    }

    // LOGIN
    @PostMapping("/login")
    public ResponseEntity<LoginResponse > login(@RequestBody LoginRequest request){
        User user = userService.login(request);
        String token = userService.generateTokenForUser(user);

        LoginResponse response = new LoginResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                token
        );

        return ResponseEntity.ok(response);
    }

    // Change Password
    @PostMapping("/change-password")
    public ResponseEntity<ChangePasswordResponse> changePassword(@RequestBody ChangePasswordRequest request){
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        userService.changePassword(email, request);

        return ResponseEntity.ok(
          new ChangePasswordResponse("Password changed successfully")
        );
    }

    // Forgot Password
    @PostMapping("/forgot-password")
    public ResponseEntity<ForgotPasswordResponse> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {

        userService.forgotPassword(request);

        return ResponseEntity.ok(
                new ForgotPasswordResponse(
                        "If an account with that email exists, a password reset link has been sent."
                )
        );
    }

    // Reset Password
    @PostMapping("/reset-password")
    public ResponseEntity<ResetPasswordResponse> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        userService.resetPassword(request);

        return ResponseEntity.ok(
                new ResetPasswordResponse("Password reset successfully")
        );
    }
}
