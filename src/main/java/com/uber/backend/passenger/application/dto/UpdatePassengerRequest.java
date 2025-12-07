package com.uber.backend.passenger.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * DTO for updating passenger profile.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePassengerRequest {
    
    private String firstName;
    private String lastName;
    private String phoneNumber;
}
