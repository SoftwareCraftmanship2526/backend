package com.uber.backend.auth.api.web;

import com.uber.backend.auth.application.dto.*;
import com.uber.backend.auth.application.service.AuthenticationService;
import com.uber.backend.auth.application.service.VerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Authentication", description = "Authentication and registration endpoints")
public class AuthController {

    private final AuthenticationService authenticationService;
    private final VerificationService verificationService;

    /**
     * Register a new passenger account.
     *
     * @param request Passenger registration details
     * @return Authentication response with JWT token
     */
    @PostMapping("/register/passenger")
    @Operation(
            summary = "Register as Passenger",
            description = "Create a new passenger account and send verification email with 6-digit code"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Passenger registered successfully, verification email sent"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input data or email already exists"
            )
    })
    public ResponseEntity<Map<String, String>> registerPassenger(@Valid @RequestBody RegisterRequest request) {
        try {
            authenticationService.registerPassenger(request);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Registration successful! A verification code has been sent to " + request.getEmail());
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
    @Operation(
            summary = "Register as Driver",
            description = "Create a new driver account with license information and send verification email with 6-digit code"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Driver registered successfully, verification email sent"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input data or email already exists"
            )
    })
    public ResponseEntity<Map<String, String>> registerDriver(@Valid @RequestBody RegisterDriverRequest request) {
        try {
            authenticationService.registerDriver(request);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Registration successful! A verification code has been sent to " + request.getEmail());
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
    @Operation(
            summary = "Login",
            description = "Authenticate with email and password to receive JWT token"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Login successful",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid credentials"
            )
    })
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
    @Operation(
            summary = "Get Current User",
            description = "Retrieve information about the currently authenticated user",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User information retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Not authenticated - Invalid or missing JWT token"
            )
    })
    public ResponseEntity<Map<String, Object>> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        Map<String, Object> response = new HashMap<>();
        response.put("email", authentication.getName());
        response.put("authorities", authentication.getAuthorities());
        response.put("authenticated", authentication.isAuthenticated());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Verify email with 6-digit code.
     *
     * @param request Email and verification code
     * @return Success message
     */
    @PostMapping("/verify-email")
    @Operation(
            summary = "Verify Email",
            description = "Verify user email address with 6-digit code and receive JWT authentication token"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Email verified successfully, returns authentication token",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid or expired verification code"
            )
    })
    public ResponseEntity<AuthResponse> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        AuthResponse response = verificationService.verifyEmail(request.getEmail(), request.getCode());
        return ResponseEntity.ok(response);
    }

    /**
     * Resend verification code to email.
     *
     * @param request Email to resend code to
     * @return Success message
     */
    @PostMapping("/resend-verification")
    @Operation(
            summary = "Resend Verification Code",
            description = "Resend verification code to user's email"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Verification code sent successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Email not found or already verified"
            )
    })
    public ResponseEntity<Map<String, String>> resendVerification(@Valid @RequestBody ResendCodeRequest request) {
        verificationService.resendVerificationCode(request.getEmail());

        Map<String, String> response = new HashMap<>();
        response.put("message", "Verification code sent to your email.");
        return ResponseEntity.ok(response);
    }

    /**
     * Health check endpoint for authentication service.
     *
     * @return Status message
     */
    @GetMapping("/health")
    @Operation(
            summary = "Health Check",
            description = "Check if the authentication service is running"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Service is healthy"
            )
    })
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "OK");
        response.put("service", "Authentication Service");
        return ResponseEntity.ok(response);
    }
}
