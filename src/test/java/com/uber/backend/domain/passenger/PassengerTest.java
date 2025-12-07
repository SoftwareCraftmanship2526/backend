package com.uber.backend.domain.passenger;

import com.uber.backend.auth.domain.enums.Role;
import com.uber.backend.passenger.domain.model.Passenger;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PassengerTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void givenValidRating_whenValidating_thenNoViolations() {
        // Given
        Passenger passenger = Passenger.builder()
                .firstName("Jane")
                .lastName("Smith")
                .email("jane@example.com")
                .password("password123")
                .phoneNumber("+32471234567")
                .role(Role.PASSENGER)
                .passengerRating(4.8)
                .build();

        // When
        Set<ConstraintViolation<Passenger>> violations = validator.validate(passenger);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void givenRatingOutOfRange_whenValidating_thenRatingViolation() {
        // Given
        Passenger passenger = Passenger.builder()
                .firstName("Jane")
                .lastName("Smith")
                .email("jane@example.com")
                .password("password123")
                .phoneNumber("+32471234567")
                .role(Role.PASSENGER)
                .passengerRating(5.5)
                .build();

        // When
        Set<ConstraintViolation<Passenger>> violations = validator.validate(passenger);

        // Then
        assertEquals(1, violations.size());
        assertEquals("passengerRating", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void givenSavedAddresses_whenCheckingAddresses_thenHasAddresses() {
        // Given
        Passenger passenger = Passenger.builder()
                .savedAddresses(Arrays.asList("Home", "Work", "Gym"))
                .build();

        // When
        boolean result = passenger.hasSavedAddresses();

        // Then
        assertTrue(result);
    }

    @Test
    void givenEmptyAddressList_whenCheckingAddresses_thenNoAddresses() {
        // Given
        Passenger passenger = Passenger.builder()
                .savedAddresses(Collections.emptyList())
                .build();

        // When
        boolean result = passenger.hasSavedAddresses();

        // Then
        assertFalse(result);
    }
}
