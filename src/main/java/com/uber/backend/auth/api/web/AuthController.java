package com.uber.backend.auth.api.web;

import com.uber.backend.auth.application.dto.AuthResponse;
import com.uber.backend.auth.application.dto.LoginRequest;
import com.uber.backend.auth.application.dto.RegisterDriverRequest;
import com.uber.backend.auth.application.dto.RegisterRequest;
import com.uber.backend.auth.application.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for authentication operations.
 * Provides endpoints for registration and login.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;

    /**
     * Register a new passenger account.
     * 
     * @param request Passenger registration details
     * @return Authentication response with JWT token
     */
    @PostMapping("/register/passenger")
    public ResponseEntity<AuthResponse> registerPassenger(@Valid @RequestBody RegisterRequest request) {
        try {
            AuthResponse response = authenticationService.registerPassenger(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            throw e;
        }
    }

    /**
     * Register a new driver account.
     * 
     * @param request Driver registration details
     * @return Authentication response with JWT token
     */
    @PostMapping("/register/driver")
    public ResponseEntity<AuthResponse> registerDriver(@Valid @RequestBody RegisterDriverRequest request) {
        try {
            AuthResponse response = authenticationService.registerDriver(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            throw e;
        }
    }

    /**
     * Authenticate user and return JWT token.
     * 
     * @param request Login credentials
     * @return Authentication response with JWT token
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authenticationService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Get current authenticated user information.
     * Requires valid JWT token.
     * 
     * @return Current user details
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        Map<String, Object> response = new HashMap<>();
        response.put("email", authentication.getName());
        response.put("authorities", authentication.getAuthorities());
        response.put("authenticated", authentication.isAuthenticated());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Health check endpoint for authentication service.
     * 
     * @return Status message
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "OK");
        response.put("service", "Authentication Service");
        return ResponseEntity.ok(response);
    }
}
