package com.uber.backend.auth.application.service;

import com.uber.backend.auth.application.dto.AuthResponse;
import com.uber.backend.auth.application.dto.LoginRequest;
import com.uber.backend.auth.application.dto.RegisterDriverRequest;
import com.uber.backend.auth.application.dto.RegisterRequest;
import com.uber.backend.auth.domain.enums.Role;
import com.uber.backend.auth.infrastructure.security.JwtService;
import com.uber.backend.driver.infrastructure.persistence.DriverEntity;
import com.uber.backend.driver.infrastructure.repository.DriverRepository;
import com.uber.backend.passenger.infrastructure.persistence.PassengerEntity;
import com.uber.backend.passenger.infrastructure.repository.PassengerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service handling authentication operations: registration and login.
 * Follows clean architecture principles with clear separation of concerns.
 */
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final PassengerRepository passengerRepository;
    private final DriverRepository driverRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final VerificationService verificationService;

    /**
     * Register a new passenger account.
     * Sends verification email with 6-digit code.
     *
     * @param request Registration details
     * @throws IllegalArgumentException if email already exists
     */
    @Transactional
    public void registerPassenger(RegisterRequest request) {
        validateEmailNotExists(request.getEmail());

        PassengerEntity passenger = new PassengerEntity();
        passenger.setFirstName(request.getFirstName());
        passenger.setLastName(request.getLastName());
        passenger.setEmail(request.getEmail());
        passenger.setPassword(passwordEncoder.encode(request.getPassword()));
        passenger.setPhoneNumber(request.getPhoneNumber());
        passenger.setRole(Role.PASSENGER);
        passenger.setPassengerRating(5.0);
        passenger.setEmailVerified(false);

        passenger = passengerRepository.save(passenger);

        // Send verification email
        verificationService.generateAndSendVerificationCode(passenger.getEmail());
    }

    /**
     * Register a new driver account.
     * Sends verification email with 6-digit code.
     *
     * @param request Driver registration details
     * @throws IllegalArgumentException if email already exists
     */
    @Transactional
    public void registerDriver(RegisterDriverRequest request) {
        validateEmailNotExists(request.getEmail());
        validateLicenseNumberNotExists(request.getLicenseNumber());

        DriverEntity driver = new DriverEntity();
        driver.setFirstName(request.getFirstName());
        driver.setLastName(request.getLastName());
        driver.setEmail(request.getEmail());
        driver.setPassword(passwordEncoder.encode(request.getPassword()));
        driver.setPhoneNumber(request.getPhoneNumber());
        driver.setRole(Role.DRIVER);
        driver.setLicenseNumber(request.getLicenseNumber());
        driver.setDriverRating(5.0);
        driver.setIsAvailable(false);
        driver.setEmailVerified(false);

        driver = driverRepository.save(driver);

        // Send verification email
        verificationService.generateAndSendVerificationCode(driver.getEmail());
    }

    /**
     * Authenticate user and generate JWT token.
     *
     * @param request Login credentials
     * @return Authentication response with JWT token
     * @throws org.springframework.security.authentication.BadCredentialsException if credentials are invalid
     * @throws IllegalArgumentException if email is not verified
     */
    public AuthResponse login(LoginRequest request) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());

        Long userId;
        String firstName;
        String lastName;
        Role role;
        Boolean emailVerified;

        var passengerOpt = passengerRepository.findByEmail(request.getEmail());
        if (passengerOpt.isPresent()) {
            var passenger = passengerOpt.get();
            userId = passenger.getId();
            firstName = passenger.getFirstName();
            lastName = passenger.getLastName();
            role = passenger.getRole();
            emailVerified = passenger.getEmailVerified();

            // Check if email is verified
            if (!emailVerified) {
                throw new IllegalArgumentException("Email not verified. Please verify your email before logging in.");
            }
        } else {
            var driver = driverRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new IllegalStateException("User not found after authentication"));
            userId = driver.getId();
            firstName = driver.getFirstName();
            lastName = driver.getLastName();
            role = driver.getRole();
            emailVerified = driver.getEmailVerified();

            // Check if email is verified
            if (!emailVerified) {
                throw new IllegalArgumentException("Email not verified. Please verify your email before logging in.");
            }
        }

        return generateAuthResponse(userId, request.getEmail(), firstName, lastName, role, emailVerified);
    }

    /**
     * Validate that email doesn't already exist in system.
     *
     * @param email Email to check
     * @throws IllegalArgumentException if email exists
     */
    private void validateEmailNotExists(String email) {
        if (passengerRepository.findByEmail(email).isPresent() ||
            driverRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email already registered");
        }
    }

    /**
     * Validate that license number doesn't already exist in system.
     *
     * @param licenseNumber License number to check
     * @throws IllegalArgumentException if license number exists
     */
    private void validateLicenseNumberNotExists(String licenseNumber) {
        if (driverRepository.findByLicenseNumber(licenseNumber).isPresent()) {
            throw new IllegalArgumentException("License number already registered");
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
     * @param emailVerified Email verification status
     * @return Authentication response
     */
    private AuthResponse generateAuthResponse(Long userId, String email, String firstName,
                                              String lastName, Role role, Boolean emailVerified) {
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
                .emailVerified(emailVerified)
                .build();
    }


}
