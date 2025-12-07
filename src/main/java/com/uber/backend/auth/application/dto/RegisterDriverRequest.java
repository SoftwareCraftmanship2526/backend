package com.uber.backend.auth.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * DTO for driver registration, extends passenger registration with driver-specific fields.
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterDriverRequest extends RegisterRequest {

    @NotBlank(message = "License number is required")
    private String licenseNumber;
}
