package com.uber.backend.service.driver;

import com.uber.backend.driver.application.service.CheckDriverResponseTimeoutService;
import com.uber.backend.driver.infrastructure.persistence.DriverEntity;
import com.uber.backend.passenger.infrastructure.persistence.PassengerEntity;
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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Test suite for driver response timeout service.
 * Tests automatic re-assignment when drivers don't respond within 60 seconds.
 */
@ExtendWith(MockitoExtension.class)
class CheckDriverResponseTimeoutServiceTest {

    @Mock
    private RideRepository rideRepository;

    @InjectMocks
    private CheckDriverResponseTimeoutService timeoutService;

    @Nested
    class TimeoutDetectionTests {

        private RideEntity timedOutRide;
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

            // Setup timed-out ride (invited 65 seconds ago)
            timedOutRide = new RideEntity();
            timedOutRide.setId(100L);
            timedOutRide.setPassenger(passenger);
            timedOutRide.setDriver(driver);
            timedOutRide.setStatus(RideStatus.INVITED);
            timedOutRide.setInvitedAt(LocalDateTime.now().minusSeconds(65));
            timedOutRide.setPickupLocation(new Location(50.8503, 4.3517, "Pickup"));
            timedOutRide.setDropoffLocation(new Location(50.8467, 4.3525, "Dropoff"));
            timedOutRide.setRideType(RideType.UBER_X);
        }

        @Test
        void givenTimedOutInvitation_whenChecking_thenRideSetToDenied() {
            // Given
            when(rideRepository.findTimedOutInvitations(eq(RideStatus.INVITED), any(LocalDateTime.class)))
                    .thenReturn(Collections.singletonList(timedOutRide));
            when(rideRepository.save(any(RideEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            timeoutService.checkForTimedOutInvitations();

            // Then
            verify(rideRepository).findTimedOutInvitations(eq(RideStatus.INVITED), any(LocalDateTime.class));
            verify(rideRepository).save(timedOutRide);
            assertEquals(RideStatus.DENIED, timedOutRide.getStatus());
            assertNull(timedOutRide.getDriver());
            assertNull(timedOutRide.getInvitedAt());
        }

        @Test
        void givenTimedOutInvitation_whenChecking_thenDriverAddedToDeniedList() {
            // Given
            when(rideRepository.findTimedOutInvitations(eq(RideStatus.INVITED), any(LocalDateTime.class)))
                    .thenReturn(Collections.singletonList(timedOutRide));
            when(rideRepository.save(any(RideEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            timeoutService.checkForTimedOutInvitations();

            // Then
            assertTrue(timedOutRide.getDeniedDriverIds().contains(2L));
            assertEquals(1, timedOutRide.getDeniedDriverIds().size());
        }

        @Test
        void givenMultipleTimedOutRides_whenChecking_thenAllProcessed() {
            // Given
            RideEntity timedOutRide2 = new RideEntity();
            timedOutRide2.setId(101L);
            timedOutRide2.setPassenger(passenger);

            DriverEntity driver2 = new DriverEntity();
            driver2.setId(3L);
            timedOutRide2.setDriver(driver2);
            timedOutRide2.setStatus(RideStatus.INVITED);
            timedOutRide2.setInvitedAt(LocalDateTime.now().minusSeconds(70));
            timedOutRide2.setPickupLocation(new Location(50.8503, 4.3517, "Pickup"));
            timedOutRide2.setDropoffLocation(new Location(50.8467, 4.3525, "Dropoff"));
            timedOutRide2.setRideType(RideType.UBER_X);

            when(rideRepository.findTimedOutInvitations(eq(RideStatus.INVITED), any(LocalDateTime.class)))
                    .thenReturn(Arrays.asList(timedOutRide, timedOutRide2));
            when(rideRepository.save(any(RideEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            timeoutService.checkForTimedOutInvitations();

            // Then
            verify(rideRepository, times(2)).save(any(RideEntity.class));
            assertEquals(RideStatus.DENIED, timedOutRide.getStatus());
            assertEquals(RideStatus.DENIED, timedOutRide2.getStatus());
            assertTrue(timedOutRide.getDeniedDriverIds().contains(2L));
            assertTrue(timedOutRide2.getDeniedDriverIds().contains(3L));
        }

        @Test
        void givenNoTimedOutRides_whenChecking_thenNoChanges() {
            // Given
            when(rideRepository.findTimedOutInvitations(eq(RideStatus.INVITED), any(LocalDateTime.class)))
                    .thenReturn(Collections.emptyList());

            // When
            timeoutService.checkForTimedOutInvitations();

            // Then
            verify(rideRepository).findTimedOutInvitations(eq(RideStatus.INVITED), any(LocalDateTime.class));
            verify(rideRepository, never()).save(any());
        }

        @Test
        void givenDriverAlreadyInDeniedList_whenChecking_thenNotAddedTwice() {
            // Given
            timedOutRide.getDeniedDriverIds().add(2L); // Driver already denied once
            when(rideRepository.findTimedOutInvitations(eq(RideStatus.INVITED), any(LocalDateTime.class)))
                    .thenReturn(Collections.singletonList(timedOutRide));
            when(rideRepository.save(any(RideEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            timeoutService.checkForTimedOutInvitations();

            // Then
            assertEquals(1, timedOutRide.getDeniedDriverIds().size());
            assertTrue(timedOutRide.getDeniedDriverIds().contains(2L));
        }

        @Test
        void givenRideWithNoDriver_whenChecking_thenStillProcessed() {
            // Given
            timedOutRide.setDriver(null); // Edge case: no driver assigned
            when(rideRepository.findTimedOutInvitations(eq(RideStatus.INVITED), any(LocalDateTime.class)))
                    .thenReturn(Collections.singletonList(timedOutRide));
            when(rideRepository.save(any(RideEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            timeoutService.checkForTimedOutInvitations();

            // Then
            verify(rideRepository).save(timedOutRide);
            assertEquals(RideStatus.DENIED, timedOutRide.getStatus());
            assertNull(timedOutRide.getInvitedAt());
            // No driver to add to denied list
            assertEquals(0, timedOutRide.getDeniedDriverIds().size());
        }
    }

    @Nested
    class TimeoutThresholdTests {

        @Test
        void givenInvitedRide_whenLessThan60Seconds_thenNotTimedOut() {
            // Given - Ride invited 50 seconds ago (within timeout window)
            RideEntity recentRide = new RideEntity();
            recentRide.setId(100L);
            recentRide.setStatus(RideStatus.INVITED);
            recentRide.setInvitedAt(LocalDateTime.now().minusSeconds(50));

            // Repository should filter this out (not returned in timed-out list)
            when(rideRepository.findTimedOutInvitations(eq(RideStatus.INVITED), any(LocalDateTime.class)))
                    .thenReturn(Collections.emptyList());

            // When
            timeoutService.checkForTimedOutInvitations();

            // Then
            verify(rideRepository, never()).save(any());
        }

        @Test
        void givenInvitedRide_whenExactly60Seconds_thenConsideredTimedOut() {
            // Given - Ride invited exactly 60 seconds ago
            PassengerEntity passenger = new PassengerEntity();
            passenger.setId(1L);

            DriverEntity driver = new DriverEntity();
            driver.setId(2L);

            RideEntity borderlineRide = new RideEntity();
            borderlineRide.setId(100L);
            borderlineRide.setPassenger(passenger);
            borderlineRide.setDriver(driver);
            borderlineRide.setStatus(RideStatus.INVITED);
            borderlineRide.setInvitedAt(LocalDateTime.now().minusSeconds(60));
            borderlineRide.setPickupLocation(new Location(50.8503, 4.3517, "Pickup"));
            borderlineRide.setDropoffLocation(new Location(50.8467, 4.3525, "Dropoff"));
            borderlineRide.setRideType(RideType.UBER_X);

            when(rideRepository.findTimedOutInvitations(eq(RideStatus.INVITED), any(LocalDateTime.class)))
                    .thenReturn(Collections.singletonList(borderlineRide));
            when(rideRepository.save(any(RideEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            timeoutService.checkForTimedOutInvitations();

            // Then
            verify(rideRepository).save(borderlineRide);
            assertEquals(RideStatus.DENIED, borderlineRide.getStatus());
        }
    }

    @Nested
    class StateTransitionTests {

        private RideEntity timedOutRide;
        private DriverEntity driver;

        @BeforeEach
        void setUp() {
            PassengerEntity passenger = new PassengerEntity();
            passenger.setId(1L);

            driver = new DriverEntity();
            driver.setId(2L);

            timedOutRide = new RideEntity();
            timedOutRide.setId(100L);
            timedOutRide.setPassenger(passenger);
            timedOutRide.setDriver(driver);
            timedOutRide.setStatus(RideStatus.INVITED);
            timedOutRide.setInvitedAt(LocalDateTime.now().minusSeconds(65));
            timedOutRide.setPickupLocation(new Location(50.8503, 4.3517, "Pickup"));
            timedOutRide.setDropoffLocation(new Location(50.8467, 4.3525, "Dropoff"));
            timedOutRide.setRideType(RideType.UBER_X);
        }

        @Test
        void givenInvitedStatus_whenTimedOut_thenChangesToDenied() {
            // Given
            assertEquals(RideStatus.INVITED, timedOutRide.getStatus());
            when(rideRepository.findTimedOutInvitations(eq(RideStatus.INVITED), any(LocalDateTime.class)))
                    .thenReturn(Collections.singletonList(timedOutRide));
            when(rideRepository.save(any(RideEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            timeoutService.checkForTimedOutInvitations();

            // Then
            assertEquals(RideStatus.DENIED, timedOutRide.getStatus());
        }

        @Test
        void givenDriverAssigned_whenTimedOut_thenDriverCleared() {
            // Given
            assertNotNull(timedOutRide.getDriver());
            when(rideRepository.findTimedOutInvitations(eq(RideStatus.INVITED), any(LocalDateTime.class)))
                    .thenReturn(Collections.singletonList(timedOutRide));
            when(rideRepository.save(any(RideEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            timeoutService.checkForTimedOutInvitations();

            // Then
            assertNull(timedOutRide.getDriver());
        }

        @Test
        void givenInvitedAtSet_whenTimedOut_thenInvitedAtCleared() {
            // Given
            assertNotNull(timedOutRide.getInvitedAt());
            when(rideRepository.findTimedOutInvitations(eq(RideStatus.INVITED), any(LocalDateTime.class)))
                    .thenReturn(Collections.singletonList(timedOutRide));
            when(rideRepository.save(any(RideEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            timeoutService.checkForTimedOutInvitations();

            // Then
            assertNull(timedOutRide.getInvitedAt());
        }

        @Test
        void givenCompleteRide_whenTimedOut_thenAllFieldsSetCorrectly() {
            // Given
            when(rideRepository.findTimedOutInvitations(eq(RideStatus.INVITED), any(LocalDateTime.class)))
                    .thenReturn(Collections.singletonList(timedOutRide));
            when(rideRepository.save(any(RideEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            timeoutService.checkForTimedOutInvitations();

            // Then - All state changes verified together
            assertEquals(RideStatus.DENIED, timedOutRide.getStatus());
            assertNull(timedOutRide.getDriver());
            assertNull(timedOutRide.getInvitedAt());
            assertTrue(timedOutRide.getDeniedDriverIds().contains(2L));

            // Passenger and ride details should remain unchanged
            assertNotNull(timedOutRide.getPassenger());
            assertEquals(1L, timedOutRide.getPassenger().getId());
            assertEquals(100L, timedOutRide.getId());
            assertNotNull(timedOutRide.getPickupLocation());
            assertNotNull(timedOutRide.getDropoffLocation());
        }
    }
}
