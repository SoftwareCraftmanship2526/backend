package com.uber.backend.domain.ride;

import com.uber.backend.ride.domain.strategy.UberXStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UberXStrategyTest {

    private UberXStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new UberXStrategy();
    }

    @Test
    void givenValidInputs_whenCalculatingFare_thenReturnsCorrectFare() {
        // Given
        double distanceKm = 10.0;
        int durationMin = 15;
        double demandMultiplier = 1.0;

        // When
        BigDecimal fare = strategy.calculateFare(distanceKm, durationMin, demandMultiplier);

        // Then
        BigDecimal expected = new BigDecimal("19.00");
        assertEquals(expected, fare);
    }

    @Test
    void givenSmallTrip_whenCalculatingFare_thenReturnsMinimumFare() {
        // Given
        double distanceKm = 0.5;
        int durationMin = 2;
        double demandMultiplier = 1.0;

        // When
        BigDecimal fare = strategy.calculateFare(distanceKm, durationMin, demandMultiplier);

        // Then
        BigDecimal minimumFare = new BigDecimal("5.00");
        assertEquals(minimumFare, fare);
    }
}
