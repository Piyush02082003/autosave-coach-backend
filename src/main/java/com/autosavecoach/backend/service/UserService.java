package com.autosavecoach.backend.service;

import com.autosavecoach.backend.dto.request.ChangePasswordRequest;
import com.autosavecoach.backend.dto.request.ForgotPasswordRequest;
import com.autosavecoach.backend.dto.request.LoginRequest;
import com.autosavecoach.backend.dto.request.ResetPasswordRequest;
import com.autosavecoach.backend.exception.BadRequestException;
import com.autosavecoach.backend.exception.ConflictException;
import com.autosavecoach.backend.exception.NotFoundException;
import com.autosavecoach.backend.exception.UnauthorizedException;
import com.autosavecoach.backend.model.PasswordResetToken;
import com.autosavecoach.backend.model.User;
import com.autosavecoach.backend.repository.PasswordResetTokenRepository;
import com.autosavecoach.backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService{
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService, PasswordResetTokenRepository passwordResetTokenRepository){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
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

    // Generate JWT Token
    public String generateTokenForUser(User user) {
        return jwtService.generateToken(user.getEmail());
    }

    // Get User
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new NotFoundException("User not found"));
    }

    // Change Password
    public void changePassword(String email, ChangePasswordRequest request){
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new NotFoundException("User not found"));

        if(!passwordEncoder.matches(
                request.getOldPassword(),
                user.getPassword())){
            throw new UnauthorizedException("Old password is Incorrect");
        }

        if(!request.getNewPassword().equals(request.getConfirmPassword())){
            throw new BadRequestException(
                    "New password and confirm password do not match");
        }

        if (request.getNewPassword().length() < 6) {
            throw new BadRequestException(
                    "Password must be at least 6 characters");
        }

        if (passwordEncoder.matches(
                request.getNewPassword(),
                user.getPassword())) {

            throw new BadRequestException(
                    "New password cannot be the same as the old password");
        }

        user.setPassword(
                passwordEncoder.encode(request.getNewPassword())
        );

        userRepository.save(user);
    }

    // Send forgot password link
    public void forgotPassword(ForgotPasswordRequest request) {

        Optional<User> optionalUser = userRepository.findByEmail(request.getEmail());

        if (optionalUser.isEmpty()) {
            return;
        }

        User user = optionalUser.get();

        passwordResetTokenRepository.deleteByUser(user);

        PasswordResetToken token = new PasswordResetToken();
        token.setToken(UUID.randomUUID().toString());
        token.setUser(user);
        token.setExpiryTime(LocalDateTime.now().plusMinutes(15));

        passwordResetTokenRepository.save(token);

        // TODO:
        // Send email containing:
        // https://your-frontend/reset-password?token=<token>
    }

    // Reset password
    public void resetPassword(ResetPasswordRequest request) {

        PasswordResetToken resetToken = passwordResetTokenRepository
                .findByToken(request.getToken())
                .orElseThrow(() ->
                        new BadRequestException("Invalid or expired reset token"));

        if (resetToken.getExpiryTime().isBefore(LocalDateTime.now())) {
            passwordResetTokenRepository.delete(resetToken);
            throw new BadRequestException("Reset token has expired");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException(
                    "New password and confirm password do not match");
        }

        if (request.getNewPassword().length() < 6) {
            throw new BadRequestException(
                    "Password must be at least 6 characters");
        }

        User user = resetToken.getUser();

        if (passwordEncoder.matches(
                request.getNewPassword(),
                user.getPassword())) {

            throw new BadRequestException(
                    "New password cannot be the same as the old password");
        }

        user.setPassword(
                passwordEncoder.encode(request.getNewPassword())
        );

        userRepository.save(user);

        passwordResetTokenRepository.delete(resetToken);
    }

}