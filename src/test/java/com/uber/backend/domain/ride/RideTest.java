package com.uber.backend.domain.ride;

import com.uber.backend.ride.domain.enums.RideStatus;
import com.uber.backend.ride.domain.model.Ride;
import com.uber.backend.shared.domain.valueobject.Location;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RideTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void givenValidLocations_whenValidating_thenNoViolations() {
        // Given
        Location pickup = new Location(50.8503, 4.3517, "Brussels Central");
        Location dropoff = new Location(50.8467, 4.3525, "Brussels South");

        Ride ride = Ride.builder()
                .pickupLocation(pickup)
                .dropoffLocation(dropoff)
                .status(RideStatus.REQUESTED)
                .requestedAt(LocalDateTime.now())
                .passengerId(1L)
                .build();

        // When
        Set<ConstraintViolation<Ride>> violations = validator.validate(ride);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void givenInvalidPickupLocation_whenValidating_thenLocationViolation() {
        // Given
        Location pickup = new Location(95.0, 4.3517, "Invalid Location");
        Location dropoff = new Location(50.8467, 4.3525, "Brussels South");

        Ride ride = Ride.builder()
                .pickupLocation(pickup)
                .dropoffLocation(dropoff)
                .status(RideStatus.REQUESTED)
                .requestedAt(LocalDateTime.now())
                .passengerId(1L)
                .build();

        // When
        Set<ConstraintViolation<Ride>> violations = validator.validate(ride);

        // Then
        assertEquals(1, violations.size());
        assertEquals("pickupLocation.latitude", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void givenPositiveFare_whenValidating_thenNoViolations() {
        // Given
        Ride ride = Ride.builder()
                .fareAmount(new BigDecimal("25.50"))
                .status(RideStatus.COMPLETED)
                .requestedAt(LocalDateTime.now())
                .passengerId(1L)
                .pickupLocation(new Location(50.8503, 4.3517, "Brussels Central"))
                .dropoffLocation(new Location(50.8467, 4.3525, "Brussels South"))
                .build();

        // When
        Set<ConstraintViolation<Ride>> violations = validator.validate(ride);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void givenZeroFare_whenValidating_thenFareViolation() {
        // Given
        Ride ride = Ride.builder()
                .fareAmount(BigDecimal.ZERO)
                .status(RideStatus.REQUESTED)
                .requestedAt(LocalDateTime.now())
                .passengerId(1L)
                .pickupLocation(new Location(50.8503, 4.3517, "Brussels Central"))
                .dropoffLocation(new Location(50.8467, 4.3525, "Brussels South"))
                .build();

        // When
        Set<ConstraintViolation<Ride>> violations = validator.validate(ride);

        // Then
        assertEquals(1, violations.size());
        assertEquals("fareAmount", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void givenValidStatus_whenValidating_thenNoViolations() {
        // Given
        Ride ride = Ride.builder()
                .status(RideStatus.IN_PROGRESS)
                .requestedAt(LocalDateTime.now())
                .passengerId(1L)
                .pickupLocation(new Location(50.8503, 4.3517, "Brussels Central"))
                .dropoffLocation(new Location(50.8467, 4.3525, "Brussels South"))
                .build();

        // When
        Set<ConstraintViolation<Ride>> violations = validator.validate(ride);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void givenNullStatus_whenValidating_thenStatusViolation() {
        // Given
        Ride ride = Ride.builder()
                .status(null)
                .requestedAt(LocalDateTime.now())
                .passengerId(1L)
                .pickupLocation(new Location(50.8503, 4.3517, "Brussels Central"))
                .dropoffLocation(new Location(50.8467, 4.3525, "Brussels South"))
                .build();

        // When
        Set<ConstraintViolation<Ride>> violations = validator.validate(ride);

        // Then
        assertEquals(1, violations.size());
        assertEquals("status", violations.iterator().next().getPropertyPath().toString());
    }
}
