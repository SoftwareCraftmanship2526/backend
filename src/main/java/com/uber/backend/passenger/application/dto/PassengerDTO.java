package com.uber.backend.passenger.application.dto;

import java.util.List;

/**
 * Response DTO for passenger information.
 * Used to return passenger data from the application layer,
 * avoiding circular references and N+1 query issues from entity relationships.
 */
public record PassengerDTO(
        Long id,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        Double passengerRating,
        List<String> savedAddresses,
        Integer totalRides
) {}
