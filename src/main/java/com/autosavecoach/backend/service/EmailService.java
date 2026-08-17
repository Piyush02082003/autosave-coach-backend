package com.autosavecoach.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final String mailUsername;

    public EmailService(
            JavaMailSender mailSender,
            @Value("${spring.mail.username}") String mailUsername) {

        this.mailSender = mailSender;
        this.mailUsername = mailUsername;
    }

    public void sendPasswordResetEmail(
            String email,
            String resetLink) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(mailUsername);
        message.setTo(email);
        message.setSubject("AutoSave Coach - Password Reset");

        message.setText("""
                Hello,

                We received a request to reset your AutoSave Coach password.

                Click the link below to reset your password:

                %s

                This link will expire in 15 minutes.

                If you did not request a password reset, you can safely ignore this email.

                Regards,
                AutoSave Coach
                """.formatted(resetLink));

        mailSender.send(message);
    }
}