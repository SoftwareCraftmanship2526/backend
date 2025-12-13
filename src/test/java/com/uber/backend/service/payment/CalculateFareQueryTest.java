package com.uber.backend.service.payment;

import com.uber.backend.payment.application.CalculateFareQueryHandler;
import com.uber.backend.payment.application.query.CalculateFareQuery;
import com.uber.backend.payment.application.query.FareCalculationResult;
import com.uber.backend.ride.domain.enums.RideType;
import com.uber.backend.ride.domain.strategy.UberBlackStrategy;
import com.uber.backend.ride.domain.strategy.UberPoolStrategy;
import com.uber.backend.ride.domain.strategy.UberXStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Test suite for CalculateFareQueryHandler.
 * Tests fare calculation for different ride types and scenarios.
 */
@ExtendWith(MockitoExtension.class)
class CalculateFareQueryTest {

    @Mock
    private UberXStrategy uberXStrategy;

    @Mock
    private UberBlackStrategy uberBlackStrategy;

    @Mock
    private UberPoolStrategy uberPoolStrategy;

    @InjectMocks
    private CalculateFareQueryHandler handler;

    @BeforeEach
    void setUp() {
        // Setup will be done in individual tests
    }

    @Test
    void givenUberXRide_whenCalculateFare_thenReturnCorrectFare() {
        // Arrange
        CalculateFareQuery query = new CalculateFareQuery(
                10.0,  // distance
                20,    // duration
                RideType.UBER_X,
                1.0    // no surge
        );

        BigDecimal expectedFare = new BigDecimal("17.00");
        when(uberXStrategy.calculateFare(10.0, 20, 1.0))
                .thenReturn(expectedFare);

        // Act
        FareCalculationResult result = handler.handle(query);

        // Assert
        assertNotNull(result);
        assertEquals(RideType.UBER_X, result.rideType());
        assertEquals(10.0, result.distanceKm());
        assertEquals(20, result.durationMin());
        assertEquals(1.0, result.demandMultiplier());
        assertEquals(expectedFare, result.totalFare());
        assertEquals("EUR", result.currency());
        assertNotNull(result.breakdown());
    }

    @Test
    void givenUberBlackRide_whenCalculateFare_thenReturnPremiumFare() {
        // Arrange
        CalculateFareQuery query = new CalculateFareQuery(
                10.0,
                20,
                RideType.UBER_BLACK,
                1.0
        );

        BigDecimal expectedFare = new BigDecimal("59.00");
        when(uberBlackStrategy.calculateFare(10.0, 20, 1.0))
                .thenReturn(expectedFare);

        // Act
        FareCalculationResult result = handler.handle(query);

        // Assert
        assertNotNull(result);
        assertEquals(RideType.UBER_BLACK, result.rideType());
        assertEquals(expectedFare, result.totalFare());
        assertTrue(result.totalFare().compareTo(new BigDecimal("50.00")) > 0);
    }

    @Test
    void givenUberPoolRide_whenCalculateFare_thenReturnDiscountedFare() {
        // Arrange
        CalculateFareQuery query = new CalculateFareQuery(
                10.0,
                20,
                RideType.UBER_POOL,
                1.0
        );

        BigDecimal expectedFare = new BigDecimal("7.70");
        when(uberPoolStrategy.calculateFare(10.0, 20, 1.0))
                .thenReturn(expectedFare);

        // Act
        FareCalculationResult result = handler.handle(query);

        // Assert
        assertNotNull(result);
        assertEquals(RideType.UBER_POOL, result.rideType());
        assertEquals(expectedFare, result.totalFare());
    }

    @Test
    void givenSurgePricing_whenCalculateFare_thenApplyDemandMultiplier() {
        // Arrange
        CalculateFareQuery query = new CalculateFareQuery(
                10.0,
                20,
                RideType.UBER_X,
                1.5  // 50% surge
        );

        BigDecimal expectedFare = new BigDecimal("25.50");
        when(uberXStrategy.calculateFare(10.0, 20, 1.5))
                .thenReturn(expectedFare);

        // Act
        FareCalculationResult result = handler.handle(query);

        // Assert
        assertNotNull(result);
        assertEquals(1.5, result.demandMultiplier());
        assertEquals(expectedFare, result.totalFare());
    }

    @Test
    void givenShortDistance_whenCalculateFare_thenApplyMinimumFare() {
        // Arrange
        CalculateFareQuery query = new CalculateFareQuery(
                0.5,
                5,
                RideType.UBER_X,
                1.0
        );

        BigDecimal minimumFare = new BigDecimal("5.00");
        when(uberXStrategy.calculateFare(0.5, 5, 1.0))
                .thenReturn(minimumFare);

        // Act
        FareCalculationResult result = handler.handle(query);

        // Assert
        assertNotNull(result);
        assertEquals(minimumFare, result.totalFare());
    }

    @Test
    void givenZeroDistance_whenCalculateFare_thenReturnMinimumFare() {
        // Arrange
        CalculateFareQuery query = new CalculateFareQuery(
                0.0,
                0,
                RideType.UBER_X,
                1.0
        );

        BigDecimal minimumFare = new BigDecimal("5.00");
        when(uberXStrategy.calculateFare(0.0, 0, 1.0))
                .thenReturn(minimumFare);

        // Act
        FareCalculationResult result = handler.handle(query);

        // Assert
        assertNotNull(result);
        assertEquals(minimumFare, result.totalFare());
    }

    @Test
    void givenFareCalculation_whenBreakdownRequested_thenProvideDetailedBreakdown() {
        // Arrange
        CalculateFareQuery query = new CalculateFareQuery(
                10.0,
                20,
                RideType.UBER_X,
                1.0
        );

        BigDecimal expectedFare = new BigDecimal("17.00");
        when(uberXStrategy.calculateFare(10.0, 20, 1.0))
                .thenReturn(expectedFare);

        // Act
        FareCalculationResult result = handler.handle(query);

        // Assert
        FareCalculationResult.FareBreakdown breakdown = result.breakdown();
        assertNotNull(breakdown);
        assertNotNull(breakdown.baseFare());
        assertNotNull(breakdown.distanceFare());
        assertNotNull(breakdown.durationFare());
        assertNotNull(breakdown.subtotal());
        assertNotNull(breakdown.total());
        assertEquals(expectedFare, breakdown.total());
    }

    @Test
    void givenHighSurge_whenCalculateFare_thenMultiplierAppliedCorrectly() {
        // Arrange
        CalculateFareQuery query = new CalculateFareQuery(
                10.0,
                20,
                RideType.UBER_X,
                2.0  // 100% surge (2x)
        );

        BigDecimal expectedFare = new BigDecimal("34.00");
        when(uberXStrategy.calculateFare(10.0, 20, 2.0))
                .thenReturn(expectedFare);

        // Act
        FareCalculationResult result = handler.handle(query);

        // Assert
        assertNotNull(result);
        assertEquals(2.0, result.demandMultiplier());
        assertEquals(expectedFare, result.totalFare());
    }
}
