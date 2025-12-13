package com.uber.backend.shared.application;

import com.uber.backend.auth.domain.enums.Role;
import com.uber.backend.shared.domain.valueobject.Location;
import com.uber.backend.ride.domain.enums.*;
import com.uber.backend.payment.domain.enums.*;
import com.uber.backend.rating.domain.enums.RatingSource;
import com.uber.backend.ride.infrastructure.persistence.*;
import com.uber.backend.payment.infrastructure.persistence.*;
import com.uber.backend.driver.infrastructure.persistence.*;
import com.uber.backend.passenger.infrastructure.persistence.*;
import com.uber.backend.rating.infrastructure.persistence.*;
import com.uber.backend.ride.infrastructure.repository.*;
import com.uber.backend.payment.infrastructure.repository.*;
import com.uber.backend.driver.infrastructure.repository.*;
import com.uber.backend.passenger.infrastructure.repository.*;
import com.uber.backend.rating.infrastructure.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataSeederService {

    private final PassengerRepository passengerRepository;
    private final DriverRepository driverRepository;
    private final VehicleRepository vehicleRepository;
    private final RideRepository rideRepository;
    private final PaymentRepository paymentRepository;
    private final RatingRepository ratingRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void deleteAllData() {
        log.info("Starting database cleanup...");

        ratingRepository.deleteAllInBatch();

        paymentRepository.deleteAllInBatch();

        rideRepository.deleteAllInBatch();

        vehicleRepository.deleteAllInBatch();

        driverRepository.deleteAllInBatch();

        passengerRepository.deleteAllInBatch();

        log.info("Database cleanup completed!");
    }

    @Transactional
    public void seedDatabase() {
        log.info("Starting database seeding...");

        // Create Passengers
        List<PassengerEntity> passengers = createPassengers();

        // Create Drivers
        List<DriverEntity> drivers = createDrivers();

        // Create Vehicles
        List<VehicleEntity> vehicles = createVehicles(drivers);

        // Create Rides
        List<RideEntity> rides = createRides(passengers, drivers, vehicles);

        // Create Payments
        createPayments(rides);

        // Create Ratings
        createRatings(rides);

        log.info("Database seeding completed successfully!");
    }

    private List<PassengerEntity> createPassengers() {
        String encodedPassword = passwordEncoder.encode("Password123");

        PassengerEntity passenger1 = new PassengerEntity();
        passenger1.setFirstName("John");
        passenger1.setLastName("Doe");
        passenger1.setEmail("john.doe@gmail.com");
        passenger1.setPassword(encodedPassword);
        passenger1.setPhoneNumber("+1-555-0101");
        passenger1.setRole(Role.PASSENGER);
        passenger1.setPassengerRating(4.8);
        passenger1.setEmailVerified(true);
        passenger1.setSavedAddresses(Arrays.asList(
                "Home: 123 Oak Street, New York, NY 10001",
                "Work: 456 Corporate Plaza, New York, NY 10005"
        ));

        PassengerEntity passenger2 = new PassengerEntity();
        passenger2.setFirstName("Sarah");
        passenger2.setLastName("Johnson");
        passenger2.setEmail("sarah.johnson@gmail.com");
        passenger2.setPassword(encodedPassword);
        passenger2.setPhoneNumber("+1-555-0102");
        passenger2.setRole(Role.PASSENGER);
        passenger2.setPassengerRating(4.9);
        passenger2.setEmailVerified(true);
        passenger2.setSavedAddresses(Arrays.asList(
                "Home: 789 Elm Avenue, Brooklyn, NY 11201"
        ));

        PassengerEntity passenger3 = new PassengerEntity();
        passenger3.setFirstName("Michael");
        passenger3.setLastName("Chen");
        passenger3.setEmail("michael.chen@gmail.com");
        passenger3.setPassword(encodedPassword);
        passenger3.setPhoneNumber("+1-555-0103");
        passenger3.setRole(Role.PASSENGER);
        passenger3.setPassengerRating(4.6);
        passenger3.setEmailVerified(true);
        passenger3.setSavedAddresses(Arrays.asList(
                "Home: 321 Pine Road, Queens, NY 11354",
                "Gym: 555 Fitness Street, Queens, NY 11355"
        ));

        return passengerRepository.saveAll(Arrays.asList(passenger1, passenger2, passenger3));
    }

    private List<DriverEntity> createDrivers() {
        String encodedPassword = passwordEncoder.encode("Password123");

        DriverEntity driver1 = new DriverEntity();
        driver1.setFirstName("Robert");
        driver1.setLastName("Smith");
        driver1.setEmail("robert.smith@gmail.com");
        driver1.setPassword(encodedPassword);
        driver1.setPhoneNumber("+1-555-0201");
        driver1.setRole(Role.DRIVER);
        driver1.setDriverRating(4.9);
        driver1.setIsAvailable(true);
        driver1.setLicenseNumber("DL-NY-123456");
        driver1.setEmailVerified(true);
        driver1.setCurrentLocation(new Location(40.7128, -74.0060, "Times Square, New York, NY"));

        DriverEntity driver2 = new DriverEntity();
        driver2.setFirstName("Emily");
        driver2.setLastName("Davis");
        driver2.setEmail("emily.davis@gmail.com");
        driver2.setPassword(encodedPassword);
        driver2.setPhoneNumber("+1-555-0202");
        driver2.setRole(Role.DRIVER);
        driver2.setDriverRating(4.95);
        driver2.setIsAvailable(true);
        driver2.setLicenseNumber("DL-NY-789012");
        driver2.setEmailVerified(true);
        driver2.setCurrentLocation(new Location(40.7589, -73.9851, "Central Park, New York, NY"));

        DriverEntity driver3 = new DriverEntity();
        driver3.setFirstName("James");
        driver3.setLastName("Wilson");
        driver3.setEmail("james.wilson@gmail.com");
        driver3.setPassword(encodedPassword);
        driver3.setPhoneNumber("+1-555-0203");
        driver3.setRole(Role.DRIVER);
        driver3.setDriverRating(4.7);
        driver3.setIsAvailable(false);
        driver3.setLicenseNumber("DL-NY-345678");
        driver3.setEmailVerified(true);
        driver3.setCurrentLocation(new Location(40.7580, -73.9855, "Columbus Circle, New York, NY"));

        return driverRepository.saveAll(Arrays.asList(driver1, driver2, driver3));
    }

    private List<VehicleEntity> createVehicles(List<DriverEntity> drivers) {
        VehicleEntity vehicle1 = new VehicleEntity();
        vehicle1.setLicensePlate("ABC-1234");
        vehicle1.setModel("Toyota Camry");
        vehicle1.setColor("Black");
        vehicle1.setType(RideType.UBER_X);
        vehicle1.setDriver(drivers.get(0));

        VehicleEntity vehicle2 = new VehicleEntity();
        vehicle2.setLicensePlate("XYZ-5678");
        vehicle2.setModel("Mercedes S-Class");
        vehicle2.setColor("Black");
        vehicle2.setType(RideType.UBER_BLACK);
        vehicle2.setDriver(drivers.get(1));

        VehicleEntity vehicle3 = new VehicleEntity();
        vehicle3.setLicensePlate("DEF-9012");
        vehicle3.setModel("Honda Accord");
        vehicle3.setColor("Silver");
        vehicle3.setType(RideType.UBER_POOL);
        vehicle3.setDriver(drivers.get(2));

        return vehicleRepository.saveAll(Arrays.asList(vehicle1, vehicle2, vehicle3));
    }

    private List<RideEntity> createRides(List<PassengerEntity> passengers, List<DriverEntity> drivers, List<VehicleEntity> vehicles) {
        // Completed Ride 1
        RideEntity ride1 = new RideEntity();
        ride1.setStatus(RideStatus.COMPLETED);
        ride1.setRideType(com.uber.backend.ride.domain.enums.RideType.UBER_X);
        ride1.setRequestedAt(LocalDateTime.now().minusDays(2));
        ride1.setStartedAt(LocalDateTime.now().minusDays(2).plusMinutes(5));
        ride1.setCompletedAt(LocalDateTime.now().minusDays(2).plusMinutes(25));
        ride1.setDistanceKm(8.5);
        ride1.setDurationMin(20);
        ride1.setDemandMultiplier(1.0);
        ride1.setPickupLocation(new Location(40.7128, -74.0060, "123 Oak Street, New York, NY 10001"));
        ride1.setDropoffLocation(new Location(40.7589, -73.9851, "456 Corporate Plaza, New York, NY 10005"));
        ride1.setPassenger(passengers.get(0));
        ride1.setDriver(drivers.get(0));
        ride1.setVehicle(vehicles.get(0));

        // In Progress Ride 2
        RideEntity ride2 = new RideEntity();
        ride2.setStatus(RideStatus.IN_PROGRESS);
        ride2.setRideType(com.uber.backend.ride.domain.enums.RideType.UBER_BLACK);
        ride2.setRequestedAt(LocalDateTime.now().minusMinutes(15));
        ride2.setStartedAt(LocalDateTime.now().minusMinutes(10));
        ride2.setPickupLocation(new Location(40.7589, -73.9851, "789 Elm Avenue, Brooklyn, NY 11201"));
        ride2.setDropoffLocation(new Location(40.7580, -73.9855, "JFK Airport, Queens, NY"));
        ride2.setPassenger(passengers.get(1));
        ride2.setDriver(drivers.get(1));
        ride2.setVehicle(vehicles.get(1));

        // Requested Ride 3
        RideEntity ride3 = new RideEntity();
        ride3.setStatus(RideStatus.REQUESTED);
        ride3.setRideType(com.uber.backend.ride.domain.enums.RideType.UBER_POOL);
        ride3.setRequestedAt(LocalDateTime.now().minusMinutes(2));
        ride3.setPickupLocation(new Location(40.7580, -73.9855, "321 Pine Road, Queens, NY 11354"));
        ride3.setDropoffLocation(new Location(40.7128, -74.0060, "555 Fitness Street, Queens, NY 11355"));
        ride3.setPassenger(passengers.get(2));

        // Completed Ride 4
        RideEntity ride4 = new RideEntity();
        ride4.setStatus(RideStatus.COMPLETED);
        ride4.setRideType(com.uber.backend.ride.domain.enums.RideType.UBER_X);
        ride4.setRequestedAt(LocalDateTime.now().minusDays(5));
        ride4.setStartedAt(LocalDateTime.now().minusDays(5).plusMinutes(3));
        ride4.setCompletedAt(LocalDateTime.now().minusDays(5).plusMinutes(18));
        ride4.setDistanceKm(12.0);
        ride4.setDurationMin(15);
        ride4.setDemandMultiplier(1.0);
        ride4.setPickupLocation(new Location(40.7128, -74.0060, "Central Park West, New York, NY"));
        ride4.setDropoffLocation(new Location(40.7589, -73.9851, "Brooklyn Bridge, Brooklyn, NY"));
        ride4.setPassenger(passengers.get(0));
        ride4.setDriver(drivers.get(1));
        ride4.setVehicle(vehicles.get(1));

        return rideRepository.saveAll(Arrays.asList(ride1, ride2, ride3, ride4));
    }

    private void createPayments(List<RideEntity> rides) {
        // Payment for completed ride 1
        PaymentEntity payment1 = new PaymentEntity();
        payment1.setAmount(new BigDecimal("18.50"));
        payment1.setMethod(PaymentMethod.CREDIT_CARD);
        payment1.setStatus(PaymentStatus.COMPLETED);
        payment1.setTransactionId("TXN-" + System.currentTimeMillis() + "-001");
        payment1.setRide(rides.get(0));

        // Payment for in-progress ride 2
        PaymentEntity payment2 = new PaymentEntity();
        payment2.setAmount(new BigDecimal("35.00"));
        payment2.setMethod(PaymentMethod.APPLE_PAY);
        payment2.setStatus(PaymentStatus.PENDING);
        payment2.setTransactionId("TXN-" + System.currentTimeMillis() + "-002");
        payment2.setRide(rides.get(1));

        // Payment for completed ride 4
        PaymentEntity payment4 = new PaymentEntity();
        payment4.setAmount(new BigDecimal("22.75"));
        payment4.setMethod(PaymentMethod.CASH);
        payment4.setStatus(PaymentStatus.COMPLETED);
        payment4.setTransactionId("TXN-" + System.currentTimeMillis() + "-004");
        payment4.setRide(rides.get(3));

        paymentRepository.saveAll(Arrays.asList(payment1, payment2, payment4));
    }

    private void createRatings(List<RideEntity> rides) {
        // Ratings for completed ride 1
        RatingEntity rating1Passenger = new RatingEntity();
        rating1Passenger.setStars(5);
        rating1Passenger.setComment("Great driver! Very professional and safe.");
        rating1Passenger.setRatedBy(RatingSource.PASSENGER);
        rating1Passenger.setRide(rides.get(0));

        RatingEntity rating1Driver = new RatingEntity();
        rating1Driver.setStars(5);
        rating1Driver.setComment("Excellent passenger. Respectful and on time.");
        rating1Driver.setRatedBy(RatingSource.DRIVER);
        rating1Driver.setRide(rides.get(0));

        // Ratings for completed ride 4
        RatingEntity rating4Passenger = new RatingEntity();
        rating4Passenger.setStars(4);
        rating4Passenger.setComment("Good ride, but driver was a bit late.");
        rating4Passenger.setRatedBy(RatingSource.PASSENGER);
        rating4Passenger.setRide(rides.get(3));

        RatingEntity rating4Driver = new RatingEntity();
        rating4Driver.setStars(5);
        rating4Driver.setComment("Nice passenger!");
        rating4Driver.setRatedBy(RatingSource.DRIVER);
        rating4Driver.setRide(rides.get(3));

        ratingRepository.saveAll(Arrays.asList(rating1Passenger, rating1Driver, rating4Passenger, rating4Driver));
    }
}
