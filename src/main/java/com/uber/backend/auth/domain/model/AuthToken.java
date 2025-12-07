package com.uber.backend.auth.domain.model;

import com.uber.backend.auth.domain.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Domain model representing an authentication token.
 * Contains JWT token information and user details.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthToken {
    
    private String accessToken;
    private String tokenType;
    private Long expiresIn;
    private Long userId;
    private String email;
    private Role role;
    private Instant issuedAt;
    
    /**
     * Check if the token is expired.
     * 
     * @return true if expired, false otherwise
     */
    public boolean isExpired() {
        if (issuedAt == null || expiresIn == null) {
            return true;
        }
        return Instant.now().isAfter(issuedAt.plusMillis(expiresIn));
    }
}
