package com.uber.backend.auth.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    /**
     * Send verification code email to user.
     *
     * @param toEmail Recipient email address
     * @param verificationCode 6-digit verification code
     */
    public void sendVerificationEmail(String toEmail, String verificationCode) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Verify Your Email - MJT Rides");
            message.setText(buildVerificationEmailText(verificationCode));

            mailSender.send(message);
            log.info("Verification email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send verification email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send verification email", e);
        }
    }

    private String buildVerificationEmailText(String code) {
        return String.format("""
                Welcome to MJT Rides!

                Thank you for registering. To complete your registration, please verify your email address.

                Your verification code is: %s

                This code will expire in 15 minutes.

                If you did not create an account, please ignore this email.

                Best regards,
                The MJT Rides Team
                """, code);
    }
}
