package com.uber.backend.auth.application.dto;

import com.uber.backend.auth.domain.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * DTO for authentication responses containing JWT token and user details.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token;
    
    @Builder.Default
    private String tokenType = "Bearer";
    
    private Long expiresIn;
    private Long userId;
    private String email;
    private String firstName;
    private String lastName;
    private Role role;
}
