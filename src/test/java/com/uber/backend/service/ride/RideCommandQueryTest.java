package com.uber.backend.service.ride;

import com.uber.backend.driver.infrastructure.persistence.DriverEntity;
import com.uber.backend.driver.infrastructure.repository.DriverRepository;
import com.uber.backend.passenger.infrastructure.persistence.PassengerEntity;
import com.uber.backend.passenger.infrastructure.repository.PassengerRepository;
import com.uber.backend.payment.application.CalculateFareQueryHandler;
import com.uber.backend.payment.application.query.CalculateFareQuery;
import com.uber.backend.payment.application.query.FareCalculationResult;
import com.uber.backend.payment.domain.enums.PaymentStatus;
import com.uber.backend.payment.infrastructure.persistence.PaymentEntity;
import com.uber.backend.payment.infrastructure.repository.PaymentRepository;
import com.uber.backend.ride.application.*;
import com.uber.backend.ride.application.command.*;
import com.uber.backend.ride.application.exception.RideNotFoundException;
import com.uber.backend.ride.application.query.RideResult;
import com.uber.backend.ride.domain.enums.RideStatus;
import com.uber.backend.ride.domain.enums.RideType;
import com.uber.backend.ride.infrastructure.persistence.RideEntity;
import com.uber.backend.ride.infrastructure.repository.RideRepository;
import com.uber.backend.shared.domain.valueobject.Location;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test suite for ride command handlers.
 * Tests the complete ride lifecycle from request to completion.
 */
@ExtendWith(MockitoExtension.class)
class RideCommandQueryTest {

    @Mock
    private RideRepository rideRepository;

    @Mock
    private PassengerRepository passengerRepository;

    @Mock
    private DriverRepository driverRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private CalculateFareQueryHandler calculateFareQueryHandler;

    @InjectMocks
    private RequestRideCommandHandler requestRideHandler;

    @InjectMocks
    private DriverAcceptCommandHandler driverAcceptHandler;

    @InjectMocks
    private DenyRideCommandHandler denyRideHandler;

    @InjectMocks
    private StartRideCommandHandler startRideHandler;

    @InjectMocks
    private CompleteRideCommandHandler completeRideHandler;

    @InjectMocks
    private CancelRideCommandHandler cancelRideHandler;

    @Nested
    class RequestRideCommandTests {

        private RequestRideCommand command;
        private PassengerEntity passenger;

        @BeforeEach
        void setUp() {
            // Setup passenger
            passenger = new PassengerEntity();
            passenger.setId(1L);

            // Setup command
            command = new RequestRideCommand(
                    50.8503,
                    4.3517,
                    50.8467,
                    4.3525,
                    RideType.UBER_X
            );
        }

        @Test
        void givenValidRequest_whenRequestingRide_thenRideCreated() {
            // Given
            when(passengerRepository.findById(1L)).thenReturn(Optional.of(passenger));
            when(rideRepository.save(any(RideEntity.class))).thenAnswer(invocation -> {
                RideEntity ride = invocation.getArgument(0);
                ride.setId(100L);
                return ride;
            });

            // When
            RideRequestResult result = requestRideHandler.handle(command, 1L);

            // Then
            assertNotNull(result);
            assertEquals(100L, result.rideId());
            assertEquals(1L, result.passengerId());
            assertEquals(RideStatus.REQUESTED, result.status());
            assertNotNull(result.requestedAt());

            verify(passengerRepository).findById(1L);
            verify(rideRepository).save(any(RideEntity.class));
        }

        @Test
        void givenPassengerNotFound_whenRequestingRide_thenThrowsException() {
            // Given
            when(passengerRepository.findById(1L)).thenReturn(Optional.empty());

            // When & Then
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> requestRideHandler.handle(command, 1L)
            );
            assertEquals("Passenger not found: 1", exception.getMessage());
            verify(passengerRepository).findById(1L);
            verify(rideRepository, never()).save(any());
        }

        @Test
        void givenDifferentRideTypes_whenRequestingRide_thenRideTypeSet() {
            // Given - Test UBER_BLACK
            RequestRideCommand blackCommand = new RequestRideCommand(
                    50.8503, 4.3517,
                    50.8467, 4.3525,
                    RideType.UBER_BLACK
            );
            when(passengerRepository.findById(1L)).thenReturn(Optional.of(passenger));
            when(rideRepository.save(any(RideEntity.class))).thenAnswer(invocation -> {
                RideEntity ride = invocation.getArgument(0);
                ride.setId(100L);
                assertEquals(RideType.UBER_BLACK, ride.getRideType());
                return ride;
            });

            // When
            requestRideHandler.handle(blackCommand, 1L);

            // Then
            verify(rideRepository).save(any(RideEntity.class));
        }
    }

    @Nested
    class DriverAcceptCommandTests {

        private DriverAcceptCommand command;
        private RideEntity ride;
        private DriverEntity driver;
        private PassengerEntity passenger;

        @BeforeEach
        void setUp() {
            // Setup passenger
            passenger = new PassengerEntity();
            passenger.setId(1L);

            // Setup driver
            driver = new DriverEntity();
            driver.setId(2L);

            // Setup ride
            ride = new RideEntity();
            ride.setId(100L);
            ride.setPassenger(passenger);
            ride.setStatus(RideStatus.REQUESTED);
            ride.setPickupLocation(new Location(50.8503, 4.3517, "Pickup"));
            ride.setDropoffLocation(new Location(50.8467, 4.3525, "Dropoff"));
            ride.setRideType(RideType.UBER_X);

            // Setup command
            command = new DriverAcceptCommand(100L);
        }

        @Test
        void givenRequestedRide_whenDriverAccepts_thenRideAccepted() {
            // Given
            when(rideRepository.findById(100L)).thenReturn(Optional.of(ride));
            when(driverRepository.findById(2L)).thenReturn(Optional.of(driver));
            when(rideRepository.save(any(RideEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            RideResult result = driverAcceptHandler.handle(command, 2L);

            // Then
            assertNotNull(result);
            assertEquals(100L, result.rideId());
            assertEquals(2L, result.driverId());
            assertEquals(RideStatus.ACCEPTED, result.status());

            verify(rideRepository).findById(100L);
            verify(driverRepository).findById(2L);
            verify(rideRepository).save(ride);
            assertEquals(RideStatus.ACCEPTED, ride.getStatus());
            assertEquals(driver, ride.getDriver());
        }

        @Test
        void givenInvitedRide_whenInvitedDriverAccepts_thenRideAccepted() {
            // Given
            ride.setStatus(RideStatus.INVITED);
            ride.setDriver(driver); // Already assigned by poller
            when(rideRepository.findById(100L)).thenReturn(Optional.of(ride));
            when(driverRepository.findById(2L)).thenReturn(Optional.of(driver));
            when(rideRepository.save(any(RideEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            RideResult result = driverAcceptHandler.handle(command, 2L);

            // Then
            assertEquals(RideStatus.ACCEPTED, result.status());
            verify(rideRepository).save(ride);
        }

        @Test
        void givenInvitedRide_whenDifferentDriverAccepts_thenThrowsException() {
            // Given
            ride.setStatus(RideStatus.INVITED);
            ride.setDriver(driver); // Assigned to driver with ID 2

            DriverEntity differentDriver = new DriverEntity();
            differentDriver.setId(999L);

            when(rideRepository.findById(100L)).thenReturn(Optional.of(ride));
            when(driverRepository.findById(999L)).thenReturn(Optional.of(differentDriver));

            // When & Then - Different driver (ID 999) tries to accept
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> driverAcceptHandler.handle(command, 999L)
            );
            assertTrue(exception.getMessage().contains("Cannot accept ride"));
            assertTrue(exception.getMessage().contains("This ride is assigned to a different driver"));
            verify(rideRepository, never()).save(any());
        }

        @Test
        void givenRideNotFound_whenDriverAccepts_thenThrowsException() {
            // Given
            when(rideRepository.findById(100L)).thenReturn(Optional.empty());

            // When & Then
            RideNotFoundException exception = assertThrows(
                    RideNotFoundException.class,
                    () -> driverAcceptHandler.handle(command, 2L)
            );
            verify(rideRepository).findById(100L);
            verify(rideRepository, never()).save(any());
        }

        @Test
        void givenWrongStatus_whenDriverAccepts_thenThrowsException() {
            // Given
            ride.setStatus(RideStatus.IN_PROGRESS);
            when(rideRepository.findById(100L)).thenReturn(Optional.of(ride));

            // When & Then
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> driverAcceptHandler.handle(command, 2L)
            );
            assertTrue(exception.getMessage().contains("Ride must be in REQUESTED or INVITED status"));
            verify(rideRepository, never()).save(any());
        }

        @Test
        void givenDriverNotFound_whenAccepting_thenThrowsException() {
            // Given
            when(rideRepository.findById(100L)).thenReturn(Optional.of(ride));
            when(driverRepository.findById(2L)).thenReturn(Optional.empty());

            // When & Then
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> driverAcceptHandler.handle(command, 2L)
            );
            assertEquals("Driver not found: 2", exception.getMessage());
            verify(rideRepository, never()).save(any());
        }
    }

    @Nested
    class DenyRideCommandTests {

        private DenyRideCommand command;
        private RideEntity ride;
        private DriverEntity driver;
        private PassengerEntity passenger;

        @BeforeEach
        void setUp() {
            // Setup passenger
            passenger = new PassengerEntity();
            passenger.setId(1L);

            // Setup driver
            driver = new DriverEntity();
            driver.setId(2L);

            // Setup ride
            ride = new RideEntity();
            ride.setId(100L);
            ride.setPassenger(passenger);
            ride.setDriver(driver);
            ride.setStatus(RideStatus.INVITED);
            ride.setPickupLocation(new Location(50.8503, 4.3517, "Pickup"));
            ride.setDropoffLocation(new Location(50.8467, 4.3525, "Dropoff"));
            ride.setRideType(RideType.UBER_X);

            // Setup command
            command = new DenyRideCommand(100L);
        }

        @Test
        void givenInvitedRide_whenDriverDenies_thenRideDenied() {
            // Given
            when(rideRepository.findById(100L)).thenReturn(Optional.of(ride));
            when(rideRepository.save(any(RideEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            RideResult result = denyRideHandler.handle(command, 2L);

            // Then
            assertNotNull(result);
            assertEquals(100L, result.rideId());
            assertEquals(RideStatus.DENIED, result.status());
            assertNull(result.driverId()); // Driver assignment cleared

            verify(rideRepository).findById(100L);
            verify(rideRepository).save(ride);
            assertEquals(RideStatus.DENIED, ride.getStatus());
            assertNull(ride.getDriver());
            assertTrue(ride.getDeniedDriverIds().contains(2L));
        }

        @Test
        void givenWrongStatus_whenDriverDenies_thenThrowsException() {
            // Given
            ride.setStatus(RideStatus.REQUESTED);
            when(rideRepository.findById(100L)).thenReturn(Optional.of(ride));

            // When & Then
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> denyRideHandler.handle(command, 2L)
            );
            assertEquals("Only INVITED rides can be denied. Current status: REQUESTED", exception.getMessage());
            verify(rideRepository, never()).save(any());
        }

        @Test
        void givenDifferentDriver_whenDenying_thenThrowsException() {
            // Given
            when(rideRepository.findById(100L)).thenReturn(Optional.of(ride));

            // When & Then - Different driver (ID 999) tries to deny
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> denyRideHandler.handle(command, 999L)
            );
            assertTrue(exception.getMessage().contains("This ride is assigned to a different driver"));
            verify(rideRepository, never()).save(any());
        }

        @Test
        void givenRideNotFound_whenDenying_thenThrowsException() {
            // Given
            when(rideRepository.findById(100L)).thenReturn(Optional.empty());

            // When & Then
            RideNotFoundException exception = assertThrows(
                    RideNotFoundException.class,
                    () -> denyRideHandler.handle(command, 2L)
            );
            verify(rideRepository, never()).save(any());
        }

        @Test
        void givenMultipleDenials_whenTrackingDeniedDrivers_thenAllDriversTracked() {
            // Given
            ride.getDeniedDriverIds().add(3L); // Previous denial
            when(rideRepository.findById(100L)).thenReturn(Optional.of(ride));
            when(rideRepository.save(any(RideEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            denyRideHandler.handle(command, 2L);

            // Then
            assertTrue(ride.getDeniedDriverIds().contains(2L));
            assertTrue(ride.getDeniedDriverIds().contains(3L));
            assertEquals(2, ride.getDeniedDriverIds().size());
        }
    }

    @Nested
    class StartRideCommandTests {

        private StartRideCommand command;
        private RideEntity ride;
        private DriverEntity driver;
        private PassengerEntity passenger;

        @BeforeEach
        void setUp() {
            // Setup passenger
            passenger = new PassengerEntity();
            passenger.setId(1L);

            // Setup driver
            driver = new DriverEntity();
            driver.setId(2L);

            // Setup ride
            ride = new RideEntity();
            ride.setId(100L);
            ride.setPassenger(passenger);
            ride.setDriver(driver);
            ride.setStatus(RideStatus.ACCEPTED);
            ride.setPickupLocation(new Location(50.8503, 4.3517, "Pickup"));
            ride.setDropoffLocation(new Location(50.8467, 4.3525, "Dropoff"));
            ride.setRideType(RideType.UBER_X);

            // Setup command
            command = new StartRideCommand(100L);
        }

        @Test
        void givenAcceptedRide_whenDriverStarts_thenRideStarted() {
            // Given
            when(rideRepository.findById(100L)).thenReturn(Optional.of(ride));
            when(rideRepository.save(any(RideEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            RideResult result = startRideHandler.handle(command, 2L);

            // Then
            assertNotNull(result);
            assertEquals(100L, result.rideId());
            assertEquals(RideStatus.IN_PROGRESS, result.status());
            assertNotNull(result.startedAt());

            verify(rideRepository).findById(100L);
            verify(rideRepository).save(ride);
            assertEquals(RideStatus.IN_PROGRESS, ride.getStatus());
            assertNotNull(ride.getStartedAt());
        }

        @Test
        void givenWrongStatus_whenStarting_thenThrowsException() {
            // Given
            ride.setStatus(RideStatus.REQUESTED);
            when(rideRepository.findById(100L)).thenReturn(Optional.of(ride));

            // When & Then
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> startRideHandler.handle(command, 2L)
            );
            assertTrue(exception.getMessage().contains("Ride must be in ACCEPTED status"));
            verify(rideRepository, never()).save(any());
        }

        @Test
        void givenDifferentDriver_whenStarting_thenThrowsException() {
            // Given
            when(rideRepository.findById(100L)).thenReturn(Optional.of(ride));

            // When & Then
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> startRideHandler.handle(command, 999L)
            );
            assertTrue(exception.getMessage().contains("This ride is assigned to a different driver"));
            verify(rideRepository, never()).save(any());
        }

        @Test
        void givenRideNotFound_whenStarting_thenThrowsException() {
            // Given
            when(rideRepository.findById(100L)).thenReturn(Optional.empty());

            // When & Then
            RideNotFoundException exception = assertThrows(
                    RideNotFoundException.class,
                    () -> startRideHandler.handle(command, 2L)
            );
            verify(rideRepository, never()).save(any());
        }
    }

    @Nested
    class CompleteRideCommandTests {

        private CompleteRideCommand command;
        private RideEntity ride;
        private DriverEntity driver;
        private PassengerEntity passenger;

        @BeforeEach
        void setUp() {
            // Setup passenger
            passenger = new PassengerEntity();
            passenger.setId(1L);

            // Setup driver
            driver = new DriverEntity();
            driver.setId(2L);

            // Setup ride
            ride = new RideEntity();
            ride.setId(100L);
            ride.setPassenger(passenger);
            ride.setDriver(driver);
            ride.setStatus(RideStatus.IN_PROGRESS);
            ride.setPickupLocation(new Location(50.8503, 4.3517, "Pickup"));
            ride.setDropoffLocation(new Location(50.8467, 4.3525, "Dropoff"));
            ride.setRideType(RideType.UBER_X);
            ride.setStartedAt(LocalDateTime.now().minusMinutes(15));

            // Setup command
            command = new CompleteRideCommand(100L, 10.5, 15, 1.2);
        }

        @Test
        void givenInProgressRide_whenDriverCompletes_thenRideCompletedAndPaymentCreated() {
            // Given
            FareCalculationResult.FareBreakdown breakdown = new FareCalculationResult.FareBreakdown(
                    new BigDecimal("5.00"),   // baseFare
                    new BigDecimal("10.00"),  // distanceFare
                    new BigDecimal("5.00"),   // durationFare
                    new BigDecimal("20.00"),  // subtotal
                    new BigDecimal("4.00"),   // demandMultiplierAmount
                    new BigDecimal("0.00"),   // discount
                    new BigDecimal("24.00")   // total
            );
            FareCalculationResult fareResult = new FareCalculationResult(
                    RideType.UBER_X,
                    10.5,
                    15,
                    1.2,
                    new BigDecimal("24.00"),
                    "USD",
                    breakdown
            );
            when(rideRepository.findById(100L)).thenReturn(Optional.of(ride));
            when(calculateFareQueryHandler.handle(any(CalculateFareQuery.class))).thenReturn(fareResult);
            when(paymentRepository.save(any(PaymentEntity.class))).thenAnswer(invocation -> {
                PaymentEntity payment = invocation.getArgument(0);
                payment.setId(1L);
                return payment;
            });
            when(rideRepository.save(any(RideEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            RideResult result = completeRideHandler.handle(command, 2L);

            // Then
            assertNotNull(result);
            assertEquals(100L, result.rideId());
            assertEquals(RideStatus.COMPLETED, result.status());
            assertNotNull(result.completedAt());
            assertEquals(10.5, result.distanceKm());
            assertEquals(15, result.durationMin());
            assertEquals(1.2, result.demandMultiplier());

            verify(rideRepository).findById(100L);
            verify(calculateFareQueryHandler).handle(any(CalculateFareQuery.class));
            verify(paymentRepository).save(any(PaymentEntity.class));
            verify(rideRepository).save(ride);
            assertEquals(RideStatus.COMPLETED, ride.getStatus());
            assertNotNull(ride.getCompletedAt());
        }

        @Test
        void givenWrongStatus_whenCompleting_thenThrowsException() {
            // Given
            ride.setStatus(RideStatus.ACCEPTED);
            when(rideRepository.findById(100L)).thenReturn(Optional.of(ride));

            // When & Then
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> completeRideHandler.handle(command, 2L)
            );
            assertTrue(exception.getMessage().contains("Ride must be in IN_PROGRESS status"));
            verify(rideRepository, never()).save(any());
            verify(paymentRepository, never()).save(any());
        }

        @Test
        void givenDifferentDriver_whenCompleting_thenThrowsException() {
            // Given
            when(rideRepository.findById(100L)).thenReturn(Optional.of(ride));

            // When & Then
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> completeRideHandler.handle(command, 999L)
            );
            assertTrue(exception.getMessage().contains("This ride is assigned to a different driver"));
            verify(rideRepository, never()).save(any());
            verify(paymentRepository, never()).save(any());
        }

        @Test
        void givenRideNotFound_whenCompleting_thenThrowsException() {
            // Given
            when(rideRepository.findById(100L)).thenReturn(Optional.empty());

            // When & Then
            RideNotFoundException exception = assertThrows(
                    RideNotFoundException.class,
                    () -> completeRideHandler.handle(command, 2L)
            );
            verify(rideRepository, never()).save(any());
            verify(paymentRepository, never()).save(any());
        }

        @Test
        void givenNoDemandMultiplier_whenCompleting_thenDefaultsToOne() {
            // Given
            CompleteRideCommand commandWithoutMultiplier = new CompleteRideCommand(100L, 10.5, 15, null);
            FareCalculationResult.FareBreakdown breakdown = new FareCalculationResult.FareBreakdown(
                    new BigDecimal("5.00"),
                    new BigDecimal("10.00"),
                    new BigDecimal("5.00"),
                    new BigDecimal("20.00"),
                    new BigDecimal("0.00"),
                    new BigDecimal("0.00"),
                    new BigDecimal("20.00")
            );
            FareCalculationResult fareResult = new FareCalculationResult(
                    RideType.UBER_X,
                    10.5,
                    15,
                    1.0,
                    new BigDecimal("20.00"),
                    "USD",
                    breakdown
            );
            when(rideRepository.findById(100L)).thenReturn(Optional.of(ride));
            when(calculateFareQueryHandler.handle(any(CalculateFareQuery.class))).thenReturn(fareResult);
            when(paymentRepository.save(any(PaymentEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(rideRepository.save(any(RideEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            RideResult result = completeRideHandler.handle(commandWithoutMultiplier, 2L);

            // Then
            assertEquals(1.0, result.demandMultiplier());
            verify(calculateFareQueryHandler).handle(any(CalculateFareQuery.class));
        }

        @Test
        void givenCompletion_whenPaymentCreated_thenPaymentIsPending() {
            // Given
            FareCalculationResult.FareBreakdown breakdown = new FareCalculationResult.FareBreakdown(
                    new BigDecimal("5.00"),
                    new BigDecimal("10.00"),
                    new BigDecimal("5.00"),
                    new BigDecimal("20.00"),
                    new BigDecimal("4.00"),
                    new BigDecimal("0.00"),
                    new BigDecimal("24.00")
            );
            FareCalculationResult fareResult = new FareCalculationResult(
                    RideType.UBER_X,
                    10.5,
                    15,
                    1.2,
                    new BigDecimal("24.00"),
                    "USD",
                    breakdown
            );
            when(rideRepository.findById(100L)).thenReturn(Optional.of(ride));
            when(calculateFareQueryHandler.handle(any(CalculateFareQuery.class))).thenReturn(fareResult);
            when(paymentRepository.save(any(PaymentEntity.class))).thenAnswer(invocation -> {
                PaymentEntity payment = invocation.getArgument(0);
                assertEquals(PaymentStatus.PENDING, payment.getStatus());
                assertEquals(new BigDecimal("24.00"), payment.getAmount());
                assertEquals(ride, payment.getRide());
                payment.setId(1L);
                return payment;
            });
            when(rideRepository.save(any(RideEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            completeRideHandler.handle(command, 2L);

            // Then
            verify(paymentRepository).save(any(PaymentEntity.class));
        }
    }

    @Nested
    class CancelRideCommandTests {

        private static final BigDecimal BASE_CANCELLATION_FEE = new BigDecimal("5.00");
        private static final BigDecimal ADDITIONAL_FEE_PER_MINUTE = new BigDecimal("1.00");

        @Nested
        class CancelBeforeAcceptanceTests {

            @Test
            void givenRequestedRide_whenCancelled_thenNoFee() {
                // Given
                PassengerEntity passenger = new PassengerEntity();
                passenger.setId(1L);

                RideEntity requestedRide = new RideEntity();
                requestedRide.setId(100L);
                requestedRide.setPassenger(passenger);
                requestedRide.setStatus(RideStatus.REQUESTED);
                requestedRide.setPickupLocation(new Location(50.8500, 4.3520, "Pickup"));
                requestedRide.setDropoffLocation(new Location(50.8467, 4.3525, "Dropoff"));
                requestedRide.setRideType(RideType.UBER_X);

                CancelRideCommand command = new CancelRideCommand(100L);
                when(rideRepository.findById(100L)).thenReturn(Optional.of(requestedRide));
                when(rideRepository.save(any(RideEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

                // When
                CancelRideResult result = cancelRideHandler.handle(command, 1L);

                // Then
                verify(rideRepository).save(requestedRide);
                assertEquals(RideStatus.CANCELLED, requestedRide.getStatus());
                assertNotNull(requestedRide.getCancelledAt());
                assertNull(requestedRide.getPayment());
                assertTrue(result.message().contains("No cancellation fee"));
                assertTrue(result.message().contains("before driver acceptance"));
                assertNull(result.payment());
            }

            @Test
            void givenInvitedRide_whenCancelled_thenNoFee() {
                // Given
                PassengerEntity passenger = new PassengerEntity();
                passenger.setId(1L);

                DriverEntity driver = new DriverEntity();
                driver.setId(2L);

                RideEntity invitedRide = new RideEntity();
                invitedRide.setId(100L);
                invitedRide.setPassenger(passenger);
                invitedRide.setDriver(driver);
                invitedRide.setStatus(RideStatus.INVITED);
                invitedRide.setInvitedAt(LocalDateTime.now().minusMinutes(1));
                invitedRide.setPickupLocation(new Location(50.8500, 4.3520, "Pickup"));
                invitedRide.setDropoffLocation(new Location(50.8467, 4.3525, "Dropoff"));
                invitedRide.setRideType(RideType.UBER_X);

                CancelRideCommand command = new CancelRideCommand(100L);
                when(rideRepository.findById(100L)).thenReturn(Optional.of(invitedRide));
                when(rideRepository.save(any(RideEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
                when(driverRepository.findById(2L)).thenReturn(Optional.of(driver));

                // When
                CancelRideResult result = cancelRideHandler.handle(command, 1L);

                // Then
                verify(rideRepository).save(invitedRide);
                assertEquals(RideStatus.CANCELLED, invitedRide.getStatus());
                assertNotNull(invitedRide.getCancelledAt());
                assertNull(invitedRide.getPayment());
                assertTrue(result.message().contains("No cancellation fee"));
                assertNull(result.payment());
                verify(driverRepository).save(driver);
                assertTrue(driver.getIsAvailable());
            }
        }

        @Nested
        class CancelWithin5MinutesTests {

            @Test
            void givenAcceptedRide_whenCancelledAfter1Minute_thenNoFee() {
                // Given
                PassengerEntity passenger = new PassengerEntity();
                passenger.setId(1L);

                DriverEntity driver = new DriverEntity();
                driver.setId(2L);

                RideEntity acceptedRide = new RideEntity();
                acceptedRide.setId(100L);
                acceptedRide.setPassenger(passenger);
                acceptedRide.setDriver(driver);
                acceptedRide.setStatus(RideStatus.ACCEPTED);
                acceptedRide.setAcceptedAt(LocalDateTime.now().minusMinutes(1));
                acceptedRide.setPickupLocation(new Location(50.8500, 4.3520, "Pickup"));
                acceptedRide.setDropoffLocation(new Location(50.8467, 4.3525, "Dropoff"));
                acceptedRide.setRideType(RideType.UBER_X);

                CancelRideCommand command = new CancelRideCommand(100L);
                when(rideRepository.findById(100L)).thenReturn(Optional.of(acceptedRide));
                when(rideRepository.save(any(RideEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
                when(driverRepository.findById(2L)).thenReturn(Optional.of(driver));

                // When
                CancelRideResult result = cancelRideHandler.handle(command, 1L);

                // Then
                verify(rideRepository).save(acceptedRide);
                assertEquals(RideStatus.CANCELLED, acceptedRide.getStatus());
                assertNotNull(acceptedRide.getCancelledAt());
                assertNull(acceptedRide.getPayment());
                assertTrue(result.message().contains("No cancellation fee"));
                assertTrue(result.message().contains("within 1 minutes"));
                assertNull(result.payment());
            }

            @Test
            void givenAcceptedRide_whenCancelledAfter3Minutes_thenNoFee() {
                // Given
                PassengerEntity passenger = new PassengerEntity();
                passenger.setId(1L);

                DriverEntity driver = new DriverEntity();
                driver.setId(2L);

                RideEntity acceptedRide = new RideEntity();
                acceptedRide.setId(100L);
                acceptedRide.setPassenger(passenger);
                acceptedRide.setDriver(driver);
                acceptedRide.setStatus(RideStatus.ACCEPTED);
                acceptedRide.setAcceptedAt(LocalDateTime.now().minusMinutes(3));
                acceptedRide.setPickupLocation(new Location(50.8500, 4.3520, "Pickup"));
                acceptedRide.setDropoffLocation(new Location(50.8467, 4.3525, "Dropoff"));
                acceptedRide.setRideType(RideType.UBER_X);

                CancelRideCommand command = new CancelRideCommand(100L);
                when(rideRepository.findById(100L)).thenReturn(Optional.of(acceptedRide));
                when(rideRepository.save(any(RideEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
                when(driverRepository.findById(2L)).thenReturn(Optional.of(driver));

                // When
                CancelRideResult result = cancelRideHandler.handle(command, 1L);

                // Then
                verify(rideRepository).save(acceptedRide);
                assertEquals(RideStatus.CANCELLED, acceptedRide.getStatus());
                assertNotNull(acceptedRide.getCancelledAt());
                assertNull(acceptedRide.getPayment());
                assertTrue(result.message().contains("No cancellation fee"));
                assertTrue(result.message().contains("within 3 minutes"));
                assertNull(result.payment());
            }
        }

        @Nested
        class CancelAfter5MinutesTests {

            @Test
            void givenAcceptedRide_whenCancelledAfter5Minutes_thenBaseFeeCharged() {
                // Given
                PassengerEntity passenger = new PassengerEntity();
                passenger.setId(1L);

                DriverEntity driver = new DriverEntity();
                driver.setId(2L);

                RideEntity acceptedRide = new RideEntity();
                acceptedRide.setId(100L);
                acceptedRide.setPassenger(passenger);
                acceptedRide.setDriver(driver);
                acceptedRide.setStatus(RideStatus.ACCEPTED);
                acceptedRide.setAcceptedAt(LocalDateTime.now().minusMinutes(5));
                acceptedRide.setPickupLocation(new Location(50.8500, 4.3520, "Pickup"));
                acceptedRide.setDropoffLocation(new Location(50.8467, 4.3525, "Dropoff"));
                acceptedRide.setRideType(RideType.UBER_X);

                CancelRideCommand command = new CancelRideCommand(100L);
                when(rideRepository.findById(100L)).thenReturn(Optional.of(acceptedRide));
                when(rideRepository.save(any(RideEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
                when(driverRepository.findById(2L)).thenReturn(Optional.of(driver));

                // When
                CancelRideResult result = cancelRideHandler.handle(command, 1L);

                // Then
                verify(rideRepository).save(acceptedRide);
                assertEquals(RideStatus.CANCELLED, acceptedRide.getStatus());
                assertNotNull(acceptedRide.getCancelledAt());

                // Verify payment was created - 5 minutes = €5.00 (base fee, 0 additional minutes)
                assertNotNull(acceptedRide.getPayment());
                assertEquals(BASE_CANCELLATION_FEE, acceptedRide.getPayment().getAmount());
                assertEquals(PaymentStatus.PENDING, acceptedRide.getPayment().getStatus());

                assertTrue(result.message().contains("Cancellation fee of €5.00"));
                assertTrue(result.message().contains("pending payment"));
                assertNotNull(result.payment());
                assertEquals(BASE_CANCELLATION_FEE, result.payment().amount());
            }

            @Test
            void givenAcceptedRide_whenCancelledAfter6Minutes_thenProgressiveFeeCharged() {
                // Given
                PassengerEntity passenger = new PassengerEntity();
                passenger.setId(1L);

                DriverEntity driver = new DriverEntity();
                driver.setId(2L);

                RideEntity acceptedRide = new RideEntity();
                acceptedRide.setId(100L);
                acceptedRide.setPassenger(passenger);
                acceptedRide.setDriver(driver);
                acceptedRide.setStatus(RideStatus.ACCEPTED);
                acceptedRide.setAcceptedAt(LocalDateTime.now().minusMinutes(6));
                acceptedRide.setPickupLocation(new Location(50.8500, 4.3520, "Pickup"));
                acceptedRide.setDropoffLocation(new Location(50.8467, 4.3525, "Dropoff"));
                acceptedRide.setRideType(RideType.UBER_X);

                CancelRideCommand command = new CancelRideCommand(100L);
                when(rideRepository.findById(100L)).thenReturn(Optional.of(acceptedRide));
                when(rideRepository.save(any(RideEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
                when(driverRepository.findById(2L)).thenReturn(Optional.of(driver));

                // When
                CancelRideResult result = cancelRideHandler.handle(command, 1L);

                // Then
                verify(rideRepository).save(acceptedRide);
                assertEquals(RideStatus.CANCELLED, acceptedRide.getStatus());
                assertNotNull(acceptedRide.getCancelledAt());

                // Verify payment was created - 6 minutes = €5.00 + €1.00 = €6.00 (1 additional minute beyond 5-min grace)
                assertNotNull(acceptedRide.getPayment());
                BigDecimal expectedFee = BASE_CANCELLATION_FEE.add(ADDITIONAL_FEE_PER_MINUTE); // €5 + €1 = €6
                assertEquals(expectedFee, acceptedRide.getPayment().getAmount());
                assertEquals(PaymentStatus.PENDING, acceptedRide.getPayment().getStatus());

                assertTrue(result.message().contains("Cancellation fee"));
                assertTrue(result.message().contains("6 minutes"));
                assertNotNull(result.payment());
                assertEquals(expectedFee, result.payment().amount());
            }

            @Test
            void givenAcceptedRide_whenCancelledAfter8Minutes_thenProgressiveFeeCharged() {
                // Given
                PassengerEntity passenger = new PassengerEntity();
                passenger.setId(1L);

                DriverEntity driver = new DriverEntity();
                driver.setId(2L);

                RideEntity acceptedRide = new RideEntity();
                acceptedRide.setId(100L);
                acceptedRide.setPassenger(passenger);
                acceptedRide.setDriver(driver);
                acceptedRide.setStatus(RideStatus.ACCEPTED);
                acceptedRide.setAcceptedAt(LocalDateTime.now().minusMinutes(8));
                acceptedRide.setPickupLocation(new Location(50.8500, 4.3520, "Pickup"));
                acceptedRide.setDropoffLocation(new Location(50.8467, 4.3525, "Dropoff"));
                acceptedRide.setRideType(RideType.UBER_X);

                CancelRideCommand command = new CancelRideCommand(100L);
                when(rideRepository.findById(100L)).thenReturn(Optional.of(acceptedRide));
                when(rideRepository.save(any(RideEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
                when(driverRepository.findById(2L)).thenReturn(Optional.of(driver));

                // When
                CancelRideResult result = cancelRideHandler.handle(command, 1L);

                // Then
                verify(rideRepository).save(acceptedRide);
                assertNotNull(acceptedRide.getPayment());
                // 8 minutes = €5.00 + (3 × €1.00) = €8.00 (3 additional minutes beyond the 5-minute grace period)
                BigDecimal expectedFee = BASE_CANCELLATION_FEE.add(ADDITIONAL_FEE_PER_MINUTE.multiply(new BigDecimal("3"))); // €5 + €3 = €8
                assertEquals(expectedFee, acceptedRide.getPayment().getAmount());
                assertEquals(new BigDecimal("8.00"), acceptedRide.getPayment().getAmount());
                assertTrue(result.message().contains("€8.00"));
                assertNotNull(result.payment());
                assertEquals(new BigDecimal("8.00"), result.payment().amount());
            }

            @Test
            void givenAcceptedRide_whenCancelledWithFee_thenDriverSetAvailable() {
                // Given
                PassengerEntity passenger = new PassengerEntity();
                passenger.setId(1L);

                DriverEntity driver = new DriverEntity();
                driver.setId(2L);

                RideEntity acceptedRide = new RideEntity();
                acceptedRide.setId(100L);
                acceptedRide.setPassenger(passenger);
                acceptedRide.setDriver(driver);
                acceptedRide.setStatus(RideStatus.ACCEPTED);
                acceptedRide.setAcceptedAt(LocalDateTime.now().minusMinutes(6)); // 6 minutes > 5-minute grace, so fee applies
                acceptedRide.setPickupLocation(new Location(50.8500, 4.3520, "Pickup"));
                acceptedRide.setDropoffLocation(new Location(50.8467, 4.3525, "Dropoff"));
                acceptedRide.setRideType(RideType.UBER_X);

                CancelRideCommand command = new CancelRideCommand(100L);
                when(rideRepository.findById(100L)).thenReturn(Optional.of(acceptedRide));
                when(rideRepository.save(any(RideEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
                when(driverRepository.findById(2L)).thenReturn(Optional.of(driver));

                // When
                cancelRideHandler.handle(command, 1L);

                // Then
                verify(driverRepository).save(driver);
                assertTrue(driver.getIsAvailable());
            }
        }

        @Nested
        class AuthorizationTests {

            @Test
            void givenDifferentPassenger_whenCancelling_thenThrowsException() {
                // Given
                PassengerEntity passenger = new PassengerEntity();
                passenger.setId(1L);

                RideEntity ride = new RideEntity();
                ride.setId(100L);
                ride.setPassenger(passenger);
                ride.setStatus(RideStatus.REQUESTED);
                ride.setPickupLocation(new Location(50.8500, 4.3520, "Pickup"));
                ride.setDropoffLocation(new Location(50.8467, 4.3525, "Dropoff"));
                ride.setRideType(RideType.UBER_X);

                CancelRideCommand command = new CancelRideCommand(100L);
                when(rideRepository.findById(100L)).thenReturn(Optional.of(ride));

                // When & Then
                IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                        cancelRideHandler.handle(command, 999L) // Wrong passenger ID
                );

                assertTrue(exception.getMessage().contains("Unauthorized"));
                verify(rideRepository, never()).save(any());
            }

            @Test
            void givenCorrectPassenger_whenCancelling_thenSucceeds() {
                // Given
                PassengerEntity passenger = new PassengerEntity();
                passenger.setId(1L);

                RideEntity ride = new RideEntity();
                ride.setId(100L);
                ride.setPassenger(passenger);
                ride.setStatus(RideStatus.REQUESTED);
                ride.setPickupLocation(new Location(50.8500, 4.3520, "Pickup"));
                ride.setDropoffLocation(new Location(50.8467, 4.3525, "Dropoff"));
                ride.setRideType(RideType.UBER_X);

                CancelRideCommand command = new CancelRideCommand(100L);
                when(rideRepository.findById(100L)).thenReturn(Optional.of(ride));
                when(rideRepository.save(any(RideEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

                // When
                CancelRideResult result = cancelRideHandler.handle(command, 1L);

                // Then
                verify(rideRepository).save(ride);
                assertEquals(RideStatus.CANCELLED, ride.getStatus());
                assertNotNull(result);
                assertNotNull(result.message());
            }
        }

        @Nested
        class EdgeCaseTests {

            @Test
            void givenNonExistentRide_whenCancelling_thenThrowsException() {
                // Given
                CancelRideCommand command = new CancelRideCommand(999L);
                when(rideRepository.findById(999L)).thenReturn(Optional.empty());

                // When & Then
                assertThrows(RideNotFoundException.class, () ->
                        cancelRideHandler.handle(command, 1L)
                );

                verify(rideRepository, never()).save(any());
            }

            @Test
            void givenRideWithoutDriver_whenCancelled_thenNoDriverUpdate() {
                // Given
                PassengerEntity passenger = new PassengerEntity();
                passenger.setId(1L);

                RideEntity ride = new RideEntity();
                ride.setId(100L);
                ride.setPassenger(passenger);
                ride.setStatus(RideStatus.REQUESTED);
                ride.setDriver(null); // No driver assigned
                ride.setPickupLocation(new Location(50.8500, 4.3520, "Pickup"));
                ride.setDropoffLocation(new Location(50.8467, 4.3525, "Dropoff"));
                ride.setRideType(RideType.UBER_X);

                CancelRideCommand command = new CancelRideCommand(100L);
                when(rideRepository.findById(100L)).thenReturn(Optional.of(ride));
                when(rideRepository.save(any(RideEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

                // When
                cancelRideHandler.handle(command, 1L);

                // Then
                verify(driverRepository, never()).findById(any());
                verify(driverRepository, never()).save(any());
            }

            @Test
            void givenAlreadyCancelledRide_whenCancelling_thenThrowsException() {
                // Given
                PassengerEntity passenger = new PassengerEntity();
                passenger.setId(1L);

                RideEntity ride = new RideEntity();
                ride.setId(100L);
                ride.setPassenger(passenger);
                ride.setStatus(RideStatus.CANCELLED);
                ride.setCancelledAt(LocalDateTime.now().minusMinutes(10));
                ride.setPickupLocation(new Location(50.8500, 4.3520, "Pickup"));
                ride.setDropoffLocation(new Location(50.8467, 4.3525, "Dropoff"));
                ride.setRideType(RideType.UBER_X);

                CancelRideCommand command = new CancelRideCommand(100L);
                when(rideRepository.findById(100L)).thenReturn(Optional.of(ride));

                // When & Then
                IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                        cancelRideHandler.handle(command, 1L)
                );

                assertTrue(exception.getMessage().contains("already been cancelled"));
                assertTrue(exception.getMessage().contains("100"));
                verify(rideRepository, never()).save(any());
            }
        }
    }
}
