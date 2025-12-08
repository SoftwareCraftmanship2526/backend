package com.uber.backend.auth.infrastructure.security;

import com.uber.backend.auth.infrastructure.security.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * Utility class to extract user information from JWT tokens.
 */
@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final JwtService jwtService;

    /**
     * Extract user ID from JWT token in the request.
     */
    public Long extractUserIdFromRequest(HttpServletRequest request) {
        String token = extractTokenFromRequest(request);
        if (token == null) {
            throw new IllegalStateException("No JWT token found in request");
        }
        return jwtService.extractUserId(token);
    }

    /**
     * Extract user ID from Authentication object.
     * This assumes the JWT token is available in the request context.
     */
    public Long extractUserIdFromAuthentication(Authentication authentication, HttpServletRequest request) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User not authenticated");
        }
        return extractUserIdFromRequest(request);
    }

    /**
     * Extract JWT token from Authorization header.
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    /**
     * Extract role from JWT token in the request.
     */
    public String extractRoleFromRequest(HttpServletRequest request) {
        String token = extractTokenFromRequest(request);
        if (token == null) {
            throw new IllegalStateException("No JWT token found in request");
        }
        return jwtService.extractRole(token);
    }
}
