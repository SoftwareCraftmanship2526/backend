package com.uber.backend.passenger.application.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for passenger responses.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PassengerDTO {
    
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    
    @DecimalMin(value = "0.0", message = "Rating must be at least 0.0")
    @DecimalMax(value = "5.0", message = "Rating must be at most 5.0")
    private Double passengerRating;
    
    private List<String> savedAddresses;
    private Integer totalRides;
}
