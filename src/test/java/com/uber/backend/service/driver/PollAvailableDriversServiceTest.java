package com.uber.backend.service.driver;

import com.uber.backend.driver.application.service.PollAvailableDriversService;
import com.uber.backend.driver.infrastructure.persistence.DriverEntity;
import com.uber.backend.driver.infrastructure.persistence.VehicleEntity;
import com.uber.backend.driver.infrastructure.repository.DriverRepository;
import com.uber.backend.passenger.infrastructure.persistence.PassengerEntity;
import com.uber.backend.ride.domain.enums.RideStatus;
import com.uber.backend.ride.domain.enums.RideType;
import com.uber.backend.ride.infrastructure.persistence.RideEntity;
import com.uber.backend.ride.infrastructure.repository.RideRepository;
import com.uber.backend.shared.domain.port.DistanceCalculatorPort;
import com.uber.backend.shared.domain.valueobject.Location;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test suite for driver polling service.
 * Tests automatic driver assignment based on proximity and availability.
 */
@ExtendWith(MockitoExtension.class)
class PollAvailableDriversServiceTest {

    @Mock
    private DriverRepository driverRepository;

    @Mock
    private RideRepository rideRepository;

    @Mock
    private DistanceCalculatorPort distanceCalculator;

    @InjectMocks
    private PollAvailableDriversService pollingService;

    @Nested
    class RequestedRideTests {

        private RideEntity requestedRide;
        private DriverEntity nearbyDriver;
        private VehicleEntity vehicle;

        @BeforeEach
        void setUp() {
            // Setup passenger
            PassengerEntity passenger = new PassengerEntity();
            passenger.setId(1L);

            // Setup vehicle
            vehicle = new VehicleEntity();
            vehicle.setId(10L);
            vehicle.setLicensePlate("ABC-123");
            vehicle.setModel("Toyota Camry");
            vehicle.setColor("Black");
            vehicle.setType(RideType.UBER_X);

            // Setup available driver
            nearbyDriver = new DriverEntity();
            nearbyDriver.setId(2L);
            nearbyDriver.setIsAvailable(true);
            nearbyDriver.setCurrentLocation(new Location(50.8503, 4.3517, "Driver Location"));
            nearbyDriver.setCurrentVehicle(vehicle);

            // Setup requested ride
            requestedRide = new RideEntity();
            requestedRide.setId(100L);
            requestedRide.setPassenger(passenger);
            requestedRide.setStatus(RideStatus.REQUESTED);
            requestedRide.setPickupLocation(new Location(50.8500, 4.3520, "Pickup"));
            requestedRide.setDropoffLocation(new Location(50.8467, 4.3525, "Dropoff"));
            requestedRide.setRideType(RideType.UBER_X);
        }

        @Test
        void givenRequestedRide_whenDriverAvailable_thenDriverAssigned() {
            // Given
            when(rideRepository.findByStatusEquals(RideStatus.REQUESTED))
                    .thenReturn(Collections.singletonList(requestedRide));
            when(rideRepository.findByStatusEquals(RideStatus.DENIED))
                    .thenReturn(Collections.emptyList());
            when(driverRepository.findByIsAvailableTrue())
                    .thenReturn(Collections.singletonList(nearbyDriver));
            when(distanceCalculator.calculateDistance(any(Location.class), any(Location.class)))
                    .thenReturn(2.5); // 2.5 km away
            when(rideRepository.save(any(RideEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            pollingService.pollForAvailableDriversForAllRides();

            // Then
            verify(rideRepository).save(requestedRide);
            assertEquals(RideStatus.INVITED, requestedRide.getStatus());
            assertEquals(nearbyDriver, requestedRide.getDriver());
            assertEquals(vehicle, requestedRide.getVehicle());
            assertNotNull(requestedRide.getInvitedAt());
        }

        @Test
        void givenRequestedRide_whenNoAvailableDrivers_thenNoAssignment() {
            // Given
            when(rideRepository.findByStatusEquals(RideStatus.REQUESTED))
                    .thenReturn(Collections.singletonList(requestedRide));
            when(rideRepository.findByStatusEquals(RideStatus.DENIED))
                    .thenReturn(Collections.emptyList());
            when(driverRepository.findByIsAvailableTrue())
                    .thenReturn(Collections.emptyList()); // No available drivers

            // When
            pollingService.pollForAvailableDriversForAllRides();

            // Then
            verify(rideRepository, never()).save(any());
            assertEquals(RideStatus.REQUESTED, requestedRide.getStatus());
            assertNull(requestedRide.getDriver());
        }

        @Test
        void givenRequestedRide_whenNoPickupLocation_thenSkipRide() {
            // Given
            requestedRide.setPickupLocation(null); // Missing pickup location
            when(rideRepository.findByStatusEquals(RideStatus.REQUESTED))
                    .thenReturn(Collections.singletonList(requestedRide));
            when(rideRepository.findByStatusEquals(RideStatus.DENIED))
                    .thenReturn(Collections.emptyList());
            when(driverRepository.findByIsAvailableTrue())
                    .thenReturn(Collections.singletonList(nearbyDriver));

            // When
            pollingService.pollForAvailableDriversForAllRides();

            // Then
            verify(rideRepository, never()).save(any());
            verify(distanceCalculator, never()).calculateDistance(any(), any());
        }

        @Test
        void givenRequestedRide_whenDriverNoLocation_thenSkipDriver() {
            // Given
            nearbyDriver.setCurrentLocation(null); // Driver has no location
            when(rideRepository.findByStatusEquals(RideStatus.REQUESTED))
                    .thenReturn(Collections.singletonList(requestedRide));
            when(rideRepository.findByStatusEquals(RideStatus.DENIED))
                    .thenReturn(Collections.emptyList());
            when(driverRepository.findByIsAvailableTrue())
                    .thenReturn(Collections.singletonList(nearbyDriver));

            // When
            pollingService.pollForAvailableDriversForAllRides();

            // Then
            verify(rideRepository, never()).save(any());
            verify(distanceCalculator, never()).calculateDistance(any(), any());
        }
    }

    @Nested
    class DeniedRideTests {

        private RideEntity deniedRide;
        private DriverEntity driver1;
        private DriverEntity driver2;
        private VehicleEntity vehicle1;
        private VehicleEntity vehicle2;

        @BeforeEach
        void setUp() {
            // Setup passenger
            PassengerEntity passenger = new PassengerEntity();
            passenger.setId(1L);

            // Setup vehicles
            vehicle1 = new VehicleEntity();
            vehicle1.setId(10L);
            vehicle1.setLicensePlate("ABC-123");
            vehicle1.setModel("Toyota Camry");
            vehicle1.setType(RideType.UBER_X);

            vehicle2 = new VehicleEntity();
            vehicle2.setId(11L);
            vehicle2.setLicensePlate("XYZ-789");
            vehicle2.setModel("Honda Accord");
            vehicle2.setType(RideType.UBER_X);

            // Setup first driver (previously denied)
            driver1 = new DriverEntity();
            driver1.setId(2L);
            driver1.setIsAvailable(true);
            driver1.setCurrentLocation(new Location(50.8503, 4.3517, "Driver 1 Location"));
            driver1.setCurrentVehicle(vehicle1);

            // Setup second driver (new driver)
            driver2 = new DriverEntity();
            driver2.setId(3L);
            driver2.setIsAvailable(true);
            driver2.setCurrentLocation(new Location(50.8505, 4.3519, "Driver 2 Location"));
            driver2.setCurrentVehicle(vehicle2);

            // Setup denied ride with driver1 in denied list
            deniedRide = new RideEntity();
            deniedRide.setId(100L);
            deniedRide.setPassenger(passenger);
            deniedRide.setStatus(RideStatus.DENIED);
            deniedRide.setPickupLocation(new Location(50.8500, 4.3520, "Pickup"));
            deniedRide.setDropoffLocation(new Location(50.8467, 4.3525, "Dropoff"));
            deniedRide.setRideType(RideType.UBER_X);
            deniedRide.getDeniedDriverIds().add(2L); // Driver 1 already denied
        }

        @Test
        void givenDeniedRide_whenNewDriverAvailable_thenNewDriverAssigned() {
            // Given
            when(rideRepository.findByStatusEquals(RideStatus.REQUESTED))
                    .thenReturn(Collections.emptyList());
            when(rideRepository.findByStatusEquals(RideStatus.DENIED))
                    .thenReturn(Collections.singletonList(deniedRide));
            when(driverRepository.findByIsAvailableTrue())
                    .thenReturn(Arrays.asList(driver1, driver2));
            when(distanceCalculator.calculateDistance(any(Location.class), any(Location.class)))
                    .thenReturn(3.0); // Both drivers same distance
            when(rideRepository.save(any(RideEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            pollingService.pollForAvailableDriversForAllRides();

            // Then
            verify(rideRepository).save(deniedRide);
            assertEquals(RideStatus.INVITED, deniedRide.getStatus());
            assertEquals(driver2, deniedRide.getDriver()); // Should be driver2, not driver1 (denied)
            assertEquals(vehicle2, deniedRide.getVehicle());
            assertNotNull(deniedRide.getInvitedAt());
        }

        @Test
        void givenDeniedRide_whenOnlyDeniedDriverAvailable_thenNoAssignment() {
            // Given - Only driver1 available (who is in denied list)
            when(rideRepository.findByStatusEquals(RideStatus.REQUESTED))
                    .thenReturn(Collections.emptyList());
            when(rideRepository.findByStatusEquals(RideStatus.DENIED))
                    .thenReturn(Collections.singletonList(deniedRide));
            when(driverRepository.findByIsAvailableTrue())
                    .thenReturn(Collections.singletonList(driver1)); // Only denied driver

            // When
            pollingService.pollForAvailableDriversForAllRides();

            // Then
            verify(rideRepository, never()).save(any());
            assertEquals(RideStatus.DENIED, deniedRide.getStatus());
            assertNull(deniedRide.getDriver());
        }

        @Test
        void givenDeniedRide_whenMultipleDeniedDrivers_thenOnlyNonDeniedDriversConsidered() {
            // Given - Add driver2 to denied list as well
            deniedRide.getDeniedDriverIds().add(3L);

            DriverEntity driver3 = new DriverEntity();
            driver3.setId(4L);
            driver3.setIsAvailable(true);
            driver3.setCurrentLocation(new Location(50.8508, 4.3522, "Driver 3 Location"));
            VehicleEntity vehicle3 = new VehicleEntity();
            vehicle3.setId(12L);
            vehicle3.setType(RideType.UBER_X);
            driver3.setCurrentVehicle(vehicle3);

            when(rideRepository.findByStatusEquals(RideStatus.REQUESTED))
                    .thenReturn(Collections.emptyList());
            when(rideRepository.findByStatusEquals(RideStatus.DENIED))
                    .thenReturn(Collections.singletonList(deniedRide));
            when(driverRepository.findByIsAvailableTrue())
                    .thenReturn(Arrays.asList(driver1, driver2, driver3));
            when(distanceCalculator.calculateDistance(any(Location.class), any(Location.class)))
                    .thenReturn(3.0);
            when(rideRepository.save(any(RideEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            pollingService.pollForAvailableDriversForAllRides();

            // Then
            verify(rideRepository).save(deniedRide);
            assertEquals(driver3, deniedRide.getDriver()); // Only driver3 is not denied
        }
    }

    @Nested
    class NearestDriverSelectionTests {

        private RideEntity ride;
        private DriverEntity nearDriver;
        private DriverEntity farDriver;
        private VehicleEntity nearVehicle;
        private VehicleEntity farVehicle;

        @BeforeEach
        void setUp() {
            PassengerEntity passenger = new PassengerEntity();
            passenger.setId(1L);

            nearVehicle = new VehicleEntity();
            nearVehicle.setId(10L);
            nearVehicle.setType(RideType.UBER_X);

            farVehicle = new VehicleEntity();
            farVehicle.setId(11L);
            farVehicle.setType(RideType.UBER_X);

            // Near driver (1 km away)
            nearDriver = new DriverEntity();
            nearDriver.setId(2L);
            nearDriver.setIsAvailable(true);
            nearDriver.setCurrentLocation(new Location(50.8510, 4.3520, "Near Driver"));
            nearDriver.setCurrentVehicle(nearVehicle);

            // Far driver (5 km away)
            farDriver = new DriverEntity();
            farDriver.setId(3L);
            farDriver.setIsAvailable(true);
            farDriver.setCurrentLocation(new Location(50.9000, 4.4000, "Far Driver"));
            farDriver.setCurrentVehicle(farVehicle);

            ride = new RideEntity();
            ride.setId(100L);
            ride.setPassenger(passenger);
            ride.setStatus(RideStatus.REQUESTED);
            ride.setPickupLocation(new Location(50.8500, 4.3520, "Pickup"));
            ride.setDropoffLocation(new Location(50.8467, 4.3525, "Dropoff"));
            ride.setRideType(RideType.UBER_X);
        }

        @Test
        void givenMultipleDrivers_whenDifferentDistances_thenNearestDriverSelected() {
            // Given
            when(rideRepository.findByStatusEquals(RideStatus.REQUESTED))
                    .thenReturn(Collections.singletonList(ride));
            when(rideRepository.findByStatusEquals(RideStatus.DENIED))
                    .thenReturn(Collections.emptyList());
            when(driverRepository.findByIsAvailableTrue())
                    .thenReturn(Arrays.asList(farDriver, nearDriver)); // Far driver first in list
            when(distanceCalculator.calculateDistance(eq(ride.getPickupLocation()), eq(nearDriver.getCurrentLocation())))
                    .thenReturn(1.0); // 1 km
            when(distanceCalculator.calculateDistance(eq(ride.getPickupLocation()), eq(farDriver.getCurrentLocation())))
                    .thenReturn(5.0); // 5 km
            when(rideRepository.save(any(RideEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            pollingService.pollForAvailableDriversForAllRides();

            // Then
            verify(rideRepository).save(ride);
            assertEquals(nearDriver, ride.getDriver()); // Should pick nearest driver
            assertEquals(nearVehicle, ride.getVehicle());
        }

        @Test
        void givenMultipleDrivers_whenSameDistance_thenAnyDriverSelected() {
            // Given - Both drivers same distance
            when(rideRepository.findByStatusEquals(RideStatus.REQUESTED))
                    .thenReturn(Collections.singletonList(ride));
            when(rideRepository.findByStatusEquals(RideStatus.DENIED))
                    .thenReturn(Collections.emptyList());
            when(driverRepository.findByIsAvailableTrue())
                    .thenReturn(Arrays.asList(nearDriver, farDriver));
            when(distanceCalculator.calculateDistance(any(Location.class), any(Location.class)))
                    .thenReturn(3.0); // Same distance for both
            when(rideRepository.save(any(RideEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            pollingService.pollForAvailableDriversForAllRides();

            // Then
            verify(rideRepository).save(ride);
            assertNotNull(ride.getDriver()); // Either driver is acceptable
            assertTrue(ride.getDriver().equals(nearDriver) || ride.getDriver().equals(farDriver));
        }
    }

    @Nested
    class MultipleRidesTests {

        private RideEntity ride1;
        private RideEntity ride2;
        private DriverEntity driver1;
        private DriverEntity driver2;
        private VehicleEntity vehicle1;
        private VehicleEntity vehicle2;

        @BeforeEach
        void setUp() {
            PassengerEntity passenger1 = new PassengerEntity();
            passenger1.setId(1L);
            PassengerEntity passenger2 = new PassengerEntity();
            passenger2.setId(2L);

            vehicle1 = new VehicleEntity();
            vehicle1.setId(10L);
            vehicle1.setType(RideType.UBER_X);

            vehicle2 = new VehicleEntity();
            vehicle2.setId(11L);
            vehicle2.setType(RideType.UBER_X);

            driver1 = new DriverEntity();
            driver1.setId(10L);
            driver1.setIsAvailable(true);
            driver1.setCurrentLocation(new Location(50.8503, 4.3517, "Driver 1"));
            driver1.setCurrentVehicle(vehicle1);

            driver2 = new DriverEntity();
            driver2.setId(11L);
            driver2.setIsAvailable(true);
            driver2.setCurrentLocation(new Location(50.8600, 4.3600, "Driver 2"));
            driver2.setCurrentVehicle(vehicle2);

            ride1 = new RideEntity();
            ride1.setId(100L);
            ride1.setPassenger(passenger1);
            ride1.setStatus(RideStatus.REQUESTED);
            ride1.setPickupLocation(new Location(50.8500, 4.3520, "Pickup 1"));
            ride1.setDropoffLocation(new Location(50.8467, 4.3525, "Dropoff 1"));
            ride1.setRideType(RideType.UBER_X);

            ride2 = new RideEntity();
            ride2.setId(101L);
            ride2.setPassenger(passenger2);
            ride2.setStatus(RideStatus.REQUESTED);
            ride2.setPickupLocation(new Location(50.8590, 4.3595, "Pickup 2"));
            ride2.setDropoffLocation(new Location(50.8550, 4.3560, "Dropoff 2"));
            ride2.setRideType(RideType.UBER_X);
        }

        @Test
        void givenMultipleRides_whenEnoughDrivers_thenAllRidesAssigned() {
            // Given
            when(rideRepository.findByStatusEquals(RideStatus.REQUESTED))
                    .thenReturn(Arrays.asList(ride1, ride2));
            when(rideRepository.findByStatusEquals(RideStatus.DENIED))
                    .thenReturn(Collections.emptyList());
            when(driverRepository.findByIsAvailableTrue())
                    .thenReturn(Arrays.asList(driver1, driver2));
            when(distanceCalculator.calculateDistance(eq(ride1.getPickupLocation()), eq(driver1.getCurrentLocation())))
                    .thenReturn(1.0);
            when(distanceCalculator.calculateDistance(eq(ride1.getPickupLocation()), eq(driver2.getCurrentLocation())))
                    .thenReturn(10.0);
            when(distanceCalculator.calculateDistance(eq(ride2.getPickupLocation()), eq(driver1.getCurrentLocation())))
                    .thenReturn(10.0);
            when(distanceCalculator.calculateDistance(eq(ride2.getPickupLocation()), eq(driver2.getCurrentLocation())))
                    .thenReturn(1.0);
            when(rideRepository.save(any(RideEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            pollingService.pollForAvailableDriversForAllRides();

            // Then
            verify(rideRepository, times(2)).save(any(RideEntity.class));
            assertEquals(RideStatus.INVITED, ride1.getStatus());
            assertEquals(RideStatus.INVITED, ride2.getStatus());
            assertEquals(driver1, ride1.getDriver()); // driver1 is closer to ride1
            assertEquals(driver2, ride2.getDriver()); // driver2 is closer to ride2
        }

        @Test
        void givenMultipleRides_whenNotEnoughDrivers_thenOnlyFirstRideAssigned() {
            // Given - Only one driver for two rides
            when(rideRepository.findByStatusEquals(RideStatus.REQUESTED))
                    .thenReturn(Arrays.asList(ride1, ride2));
            when(rideRepository.findByStatusEquals(RideStatus.DENIED))
                    .thenReturn(Collections.emptyList());
            when(driverRepository.findByIsAvailableTrue())
                    .thenReturn(Collections.singletonList(driver1)); // Only one driver
            when(distanceCalculator.calculateDistance(any(Location.class), any(Location.class)))
                    .thenReturn(2.0);
            when(rideRepository.save(any(RideEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            pollingService.pollForAvailableDriversForAllRides();

            // Then
            verify(rideRepository, times(2)).save(any(RideEntity.class));
            // Both rides will get the same driver (TreeMap allows duplicate values)
            assertEquals(RideStatus.INVITED, ride1.getStatus());
            assertEquals(RideStatus.INVITED, ride2.getStatus());
        }
    }

    @Nested
    class MixedStatusRidesTests {

        private RideEntity requestedRide;
        private RideEntity deniedRide;
        private DriverEntity driver;
        private VehicleEntity vehicle;

        @BeforeEach
        void setUp() {
            PassengerEntity passenger1 = new PassengerEntity();
            passenger1.setId(1L);
            PassengerEntity passenger2 = new PassengerEntity();
            passenger2.setId(2L);

            vehicle = new VehicleEntity();
            vehicle.setId(10L);
            vehicle.setType(RideType.UBER_X);

            driver = new DriverEntity();
            driver.setId(2L);
            driver.setIsAvailable(true);
            driver.setCurrentLocation(new Location(50.8503, 4.3517, "Driver"));
            driver.setCurrentVehicle(vehicle);

            requestedRide = new RideEntity();
            requestedRide.setId(100L);
            requestedRide.setPassenger(passenger1);
            requestedRide.setStatus(RideStatus.REQUESTED);
            requestedRide.setPickupLocation(new Location(50.8500, 4.3520, "Pickup 1"));
            requestedRide.setDropoffLocation(new Location(50.8467, 4.3525, "Dropoff 1"));
            requestedRide.setRideType(RideType.UBER_X);

            deniedRide = new RideEntity();
            deniedRide.setId(101L);
            deniedRide.setPassenger(passenger2);
            deniedRide.setStatus(RideStatus.DENIED);
            deniedRide.setPickupLocation(new Location(50.8505, 4.3525, "Pickup 2"));
            deniedRide.setDropoffLocation(new Location(50.8470, 4.3530, "Dropoff 2"));
            deniedRide.setRideType(RideType.UBER_X);
        }

        @Test
        void givenRequestedAndDeniedRides_whenPolling_thenBothProcessed() {
            // Given
            when(rideRepository.findByStatusEquals(RideStatus.REQUESTED))
                    .thenReturn(Collections.singletonList(requestedRide));
            when(rideRepository.findByStatusEquals(RideStatus.DENIED))
                    .thenReturn(Collections.singletonList(deniedRide));
            when(driverRepository.findByIsAvailableTrue())
                    .thenReturn(Collections.singletonList(driver));
            when(distanceCalculator.calculateDistance(any(Location.class), any(Location.class)))
                    .thenReturn(2.0);
            when(rideRepository.save(any(RideEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            pollingService.pollForAvailableDriversForAllRides();

            // Then
            verify(rideRepository, times(2)).save(any(RideEntity.class));
            assertEquals(RideStatus.INVITED, requestedRide.getStatus());
            assertEquals(RideStatus.INVITED, deniedRide.getStatus());
        }
    }

    @Nested
    class InvitedAtTimestampTests {

        private RideEntity ride;
        private DriverEntity driver;
        private VehicleEntity vehicle;

        @BeforeEach
        void setUp() {
            PassengerEntity passenger = new PassengerEntity();
            passenger.setId(1L);

            vehicle = new VehicleEntity();
            vehicle.setId(10L);
            vehicle.setType(RideType.UBER_X);

            driver = new DriverEntity();
            driver.setId(2L);
            driver.setIsAvailable(true);
            driver.setCurrentLocation(new Location(50.8503, 4.3517, "Driver"));
            driver.setCurrentVehicle(vehicle);

            ride = new RideEntity();
            ride.setId(100L);
            ride.setPassenger(passenger);
            ride.setStatus(RideStatus.REQUESTED);
            ride.setPickupLocation(new Location(50.8500, 4.3520, "Pickup"));
            ride.setDropoffLocation(new Location(50.8467, 4.3525, "Dropoff"));
            ride.setRideType(RideType.UBER_X);
        }

        @Test
        void givenDriverAssigned_whenSaved_thenInvitedAtTimestampSet() {
            // Given
            when(rideRepository.findByStatusEquals(RideStatus.REQUESTED))
                    .thenReturn(Collections.singletonList(ride));
            when(rideRepository.findByStatusEquals(RideStatus.DENIED))
                    .thenReturn(Collections.emptyList());
            when(driverRepository.findByIsAvailableTrue())
                    .thenReturn(Collections.singletonList(driver));
            when(distanceCalculator.calculateDistance(any(Location.class), any(Location.class)))
                    .thenReturn(2.0);

            ArgumentCaptor<RideEntity> rideCaptor = ArgumentCaptor.forClass(RideEntity.class);
            when(rideRepository.save(rideCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            pollingService.pollForAvailableDriversForAllRides();

            // Then
            RideEntity savedRide = rideCaptor.getValue();
            assertNotNull(savedRide.getInvitedAt(), "InvitedAt timestamp should be set");
            assertEquals(RideStatus.INVITED, savedRide.getStatus());
            assertEquals(driver, savedRide.getDriver());
        }
    }
}
