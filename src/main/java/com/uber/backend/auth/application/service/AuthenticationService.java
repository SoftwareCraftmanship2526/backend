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

    /**
     * Register a new passenger account.
     * 
     * @param request Registration details
     * @return Authentication response with JWT token
     * @throws IllegalArgumentException if email already exists
     */
    @Transactional
    public AuthResponse registerPassenger(RegisterRequest request) {
        validateEmailNotExists(request.getEmail());

        PassengerEntity passenger = new PassengerEntity();
        passenger.setFirstName(request.getFirstName());
        passenger.setLastName(request.getLastName());
        passenger.setEmail(request.getEmail());
        passenger.setPassword(passwordEncoder.encode(request.getPassword()));
        passenger.setPhoneNumber(request.getPhoneNumber());
        passenger.setRole(Role.PASSENGER);
        passenger.setPassengerRating(5.0);

        passenger = passengerRepository.save(passenger);

        return generateAuthResponse(passenger.getId(), passenger.getEmail(), 
                passenger.getFirstName(), passenger.getLastName(), Role.PASSENGER);
    }

    /**
     * Register a new driver account.
     * 
     * @param request Driver registration details
     * @return Authentication response with JWT token
     * @throws IllegalArgumentException if email already exists
     */
    @Transactional
    public AuthResponse registerDriver(RegisterDriverRequest request) {
        validateEmailNotExists(request.getEmail());

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

        driver = driverRepository.save(driver);

        return generateAuthResponse(driver.getId(), driver.getEmail(), 
                driver.getFirstName(), driver.getLastName(), Role.DRIVER);
    }

    /**
     * Authenticate user and generate JWT token.
     * 
     * @param request Login credentials
     * @return Authentication response with JWT token
     * @throws org.springframework.security.authentication.BadCredentialsException if credentials are invalid
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
        
        var passengerOpt = passengerRepository.findByEmail(request.getEmail());
        if (passengerOpt.isPresent()) {
            var passenger = passengerOpt.get();
            userId = passenger.getId();
            firstName = passenger.getFirstName();
            lastName = passenger.getLastName();
            role = passenger.getRole();
        } else {
            var driver = driverRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new IllegalStateException("User not found after authentication"));
            userId = driver.getId();
            firstName = driver.getFirstName();
            lastName = driver.getLastName();
            role = driver.getRole();
        }

        return generateAuthResponse(userId, request.getEmail(), firstName, lastName, role);
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
                .build();
    }
}
