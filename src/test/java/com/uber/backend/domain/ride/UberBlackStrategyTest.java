package com.uber.backend.domain.ride;

import com.uber.backend.ride.domain.strategy.UberBlackStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UberBlackStrategyTest {

    private UberBlackStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new UberBlackStrategy();
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
        BigDecimal expected = new BigDecimal("55.00");
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
        BigDecimal minimumFare = new BigDecimal("15.00");
        assertEquals(minimumFare, fare);
    }
}
