package com.uber.backend.auth.domain.enums;

/**
 * User roles for authorization.
 * Maps to Spring Security authorities.
 */
public enum Role {
    PASSENGER,
    DRIVER,
    ADMIN;

    /**
     * Get the Spring Security authority string.
     * 
     * @return Authority string in format "ROLE_{name}"
     */
    public String getAuthority() {
        return "ROLE_" + this.name();
    }
}
