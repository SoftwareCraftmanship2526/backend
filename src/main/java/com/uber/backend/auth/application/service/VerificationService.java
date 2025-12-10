package com.uber.backend.auth.application.service;

import com.uber.backend.auth.application.dto.AuthResponse;
import com.uber.backend.auth.domain.enums.Role;
import com.uber.backend.auth.infrastructure.persistence.VerificationCodeEntity;
import com.uber.backend.auth.infrastructure.repository.VerificationCodeRepository;
import com.uber.backend.auth.infrastructure.security.JwtService;
import com.uber.backend.driver.infrastructure.repository.DriverRepository;
import com.uber.backend.passenger.infrastructure.repository.PassengerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
@Slf4j
public class VerificationService {

    private final VerificationCodeRepository verificationCodeRepository;
    private final PassengerRepository passengerRepository;
    private final DriverRepository driverRepository;
    private final EmailService emailService;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Generate and send a 6-digit verification code to the user's email.
     *
     * @param email Email address to send the code to
     */
    @Transactional
    public void generateAndSendVerificationCode(String email) {
        // Generate 6-digit code
        String code = String.format("%06d", secureRandom.nextInt(1000000));

        // Save code to database
        VerificationCodeEntity verificationCode = VerificationCodeEntity.builder()
                .email(email)
                .code(code)
                .verified(false)
                .build();

        verificationCodeRepository.save(verificationCode);

        // Send email
        emailService.sendVerificationEmail(email, code);

        log.info("Verification code generated and sent to: {}", email);
    }

    /**
     * Verify the email using the provided code and return JWT token.
     *
     * @param email Email address
     * @param code 6-digit verification code
     * @return Authentication response with JWT token
     * @throws IllegalArgumentException if code is invalid, expired, or already used
     */
    @Transactional
    public AuthResponse verifyEmail(String email, String code) {
        // Find the verification code
        VerificationCodeEntity verificationCode = verificationCodeRepository
                .findByEmailAndCodeAndVerifiedFalse(email, code)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or already used verification code"));

        // Check if expired
        if (verificationCode.isExpired()) {
            throw new IllegalArgumentException("Verification code has expired. Please request a new one.");
        }

        // Mark as verified
        verificationCode.setVerified(true);
        verificationCodeRepository.save(verificationCode);

        // Update user's emailVerified status and generate token
        var passengerOpt = passengerRepository.findByEmail(email);
        if (passengerOpt.isPresent()) {
            var passenger = passengerOpt.get();
            passenger.setEmailVerified(true);
            passengerRepository.save(passenger);
            log.info("Passenger email verified: {}", email);

            return generateAuthResponse(
                    passenger.getId(),
                    passenger.getEmail(),
                    passenger.getFirstName(),
                    passenger.getLastName(),
                    passenger.getRole()
            );
        } else {
            var driverOpt = driverRepository.findByEmail(email);
            if (driverOpt.isPresent()) {
                var driver = driverOpt.get();
                driver.setEmailVerified(true);
                driverRepository.save(driver);
                log.info("Driver email verified: {}", email);

                return generateAuthResponse(
                        driver.getId(),
                        driver.getEmail(),
                        driver.getFirstName(),
                        driver.getLastName(),
                        driver.getRole()
                );
            } else {
                throw new IllegalArgumentException("User not found with email: " + email);
            }
        }
    }

    /**
     * Generate authentication response with JWT token.
     *
     * @param userId User ID
     * @param email User email
     * @param firstName User first name
     * @param lastName User last name
     * @param role User role
     * @return Authentication response
     */
    private AuthResponse generateAuthResponse(Long userId, String email, String firstName,
                                              String lastName, Role role) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        String token = jwtService.generateToken(userDetails, userId, role.name());

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpiration())
                .userId(userId)
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .role(role)
                .emailVerified(true)
                .build();
    }

    /**
     * Resend verification code to user's email.
     *
     * @param email Email address
     * @throws IllegalArgumentException if email is already verified
     */
    @Transactional
    public void resendVerificationCode(String email) {
        // Check if user exists and is not already verified
        var passengerOpt = passengerRepository.findByEmail(email);
        var driverOpt = driverRepository.findByEmail(email);

        if (passengerOpt.isEmpty() && driverOpt.isEmpty()) {
            throw new IllegalArgumentException("No account found with email: " + email);
        }

        boolean isVerified = passengerOpt.map(p -> p.getEmailVerified()).orElse(false)
                || driverOpt.map(d -> d.getEmailVerified()).orElse(false);

        if (isVerified) {
            throw new IllegalArgumentException("Email is already verified");
        }

        // Generate and send new code
        generateAndSendVerificationCode(email);
    }
}
