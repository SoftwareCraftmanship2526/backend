package com.uber.backend.service;

import com.uber.backend.domain.embeddable.Location;
import com.uber.backend.domain.entity.*;
import com.uber.backend.domain.enums.*;
import com.uber.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * DataSeederService - Service for manually seeding the database with sample data.
 * Call deleteAllData() first to clear the database, then seedDatabase() to populate it.
 */
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

    /**
     * Deletes all data from the database in the correct order to respect foreign keys.
     */
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

    /**
     * Seeds the database with sample data.
     */
    @Transactional
    public void seedDatabase() {
        log.info("Starting database seeding...");

        // Create Passengers
        List<Passenger> passengers = createPassengers();

        // Create Drivers
        List<Driver> drivers = createDrivers();

        // Create Vehicles
        List<Vehicle> vehicles = createVehicles(drivers);

        // Create Rides
        List<Ride> rides = createRides(passengers, drivers, vehicles);

        // Create Payments
        createPayments(rides);

        // Create Ratings
        createRatings(rides);

        log.info("Database seeding completed successfully!");
    }

    private List<Passenger> createPassengers() {
        Passenger passenger1 = Passenger.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.passenger@example.com")
                .password("$2a$10$dummyHashedPassword1")
                .phoneNumber("+1-555-0101")
                .passengerRating(4.8)
                .savedAddresses(Arrays.asList(
                        "Home: 123 Oak Street, New York, NY 10001",
                        "Work: 456 Corporate Plaza, New York, NY 10005"
                ))
                .build();

        Passenger passenger2 = Passenger.builder()
                .firstName("Sarah")
                .lastName("Johnson")
                .email("sarah.passenger@example.com")
                .password("$2a$10$dummyHashedPassword2")
                .phoneNumber("+1-555-0102")
                .passengerRating(4.9)
                .savedAddresses(Arrays.asList(
                        "Home: 789 Elm Avenue, Brooklyn, NY 11201"
                ))
                .build();

        Passenger passenger3 = Passenger.builder()
                .firstName("Michael")
                .lastName("Chen")
                .email("michael.passenger@example.com")
                .password("$2a$10$dummyHashedPassword3")
                .phoneNumber("+1-555-0103")
                .passengerRating(4.6)
                .savedAddresses(Arrays.asList(
                        "Home: 321 Pine Road, Queens, NY 11354",
                        "Gym: 555 Fitness Street, Queens, NY 11355"
                ))
                .build();

        return passengerRepository.saveAll(Arrays.asList(passenger1, passenger2, passenger3));
    }

    private List<Driver> createDrivers() {
        Driver driver1 = Driver.builder()
                .firstName("Robert")
                .lastName("Smith")
                .email("robert.driver@example.com")
                .password("$2a$10$dummyHashedPassword4")
                .phoneNumber("+1-555-0201")
                .driverRating(4.9)
                .isAvailable(true)
                .licenseNumber("DL-NY-123456")
                .currentLocation(Location.builder()
                        .latitude(40.7128)
                        .longitude(-74.0060)
                        .address("Times Square, New York, NY")
                        .build())
                .build();

        Driver driver2 = Driver.builder()
                .firstName("Emily")
                .lastName("Davis")
                .email("emily.driver@example.com")
                .password("$2a$10$dummyHashedPassword5")
                .phoneNumber("+1-555-0202")
                .driverRating(4.95)
                .isAvailable(true)
                .licenseNumber("DL-NY-789012")
                .currentLocation(Location.builder()
                        .latitude(40.7589)
                        .longitude(-73.9851)
                        .address("Central Park, New York, NY")
                        .build())
                .build();

        Driver driver3 = Driver.builder()
                .firstName("James")
                .lastName("Wilson")
                .email("james.driver@example.com")
                .password("$2a$10$dummyHashedPassword6")
                .phoneNumber("+1-555-0203")
                .driverRating(4.7)
                .isAvailable(false)
                .licenseNumber("DL-NY-345678")
                .currentLocation(Location.builder()
                        .latitude(40.7580)
                        .longitude(-73.9855)
                        .address("Columbus Circle, New York, NY")
                        .build())
                .build();

        return driverRepository.saveAll(Arrays.asList(driver1, driver2, driver3));
    }

    private List<Vehicle> createVehicles(List<Driver> drivers) {
        Vehicle vehicle1 = Vehicle.builder()
                .licensePlate("ABC-1234")
                .model("Toyota Camry")
                .color("Black")
                .type(RideType.UBER_X)
                .driver(drivers.get(0))
                .build();

        Vehicle vehicle2 = Vehicle.builder()
                .licensePlate("XYZ-5678")
                .model("Mercedes S-Class")
                .color("Black")
                .type(RideType.UBER_BLACK)
                .driver(drivers.get(1))
                .build();

        Vehicle vehicle3 = Vehicle.builder()
                .licensePlate("DEF-9012")
                .model("Honda Accord")
                .color("Silver")
                .type(RideType.UBER_POOL)
                .driver(drivers.get(2))
                .build();

        return vehicleRepository.saveAll(Arrays.asList(vehicle1, vehicle2, vehicle3));
    }

    private List<Ride> createRides(List<Passenger> passengers, List<Driver> drivers, List<Vehicle> vehicles) {
        // Completed Ride 1
        Ride ride1 = Ride.builder()
                .status(RideStatus.COMPLETED)
                .requestedAt(LocalDateTime.now().minusDays(2))
                .startedAt(LocalDateTime.now().minusDays(2).plusMinutes(5))
                .completedAt(LocalDateTime.now().minusDays(2).plusMinutes(25))
                .fareAmount(new BigDecimal("18.50"))
                .pickupLocation(Location.builder()
                        .latitude(40.7128)
                        .longitude(-74.0060)
                        .address("123 Oak Street, New York, NY 10001")
                        .build())
                .dropoffLocation(Location.builder()
                        .latitude(40.7589)
                        .longitude(-73.9851)
                        .address("456 Corporate Plaza, New York, NY 10005")
                        .build())
                .passenger(passengers.get(0))
                .driver(drivers.get(0))
                .vehicle(vehicles.get(0))
                .build();

        // In Progress Ride 2
        Ride ride2 = Ride.builder()
                .status(RideStatus.IN_PROGRESS)
                .requestedAt(LocalDateTime.now().minusMinutes(15))
                .startedAt(LocalDateTime.now().minusMinutes(10))
                .fareAmount(new BigDecimal("35.00"))
                .pickupLocation(Location.builder()
                        .latitude(40.7589)
                        .longitude(-73.9851)
                        .address("789 Elm Avenue, Brooklyn, NY 11201")
                        .build())
                .dropoffLocation(Location.builder()
                        .latitude(40.7580)
                        .longitude(-73.9855)
                        .address("JFK Airport, Queens, NY")
                        .build())
                .passenger(passengers.get(1))
                .driver(drivers.get(1))
                .vehicle(vehicles.get(1))
                .build();

        // Requested Ride 3
        Ride ride3 = Ride.builder()
                .status(RideStatus.REQUESTED)
                .requestedAt(LocalDateTime.now().minusMinutes(2))
                .pickupLocation(Location.builder()
                        .latitude(40.7580)
                        .longitude(-73.9855)
                        .address("321 Pine Road, Queens, NY 11354")
                        .build())
                .dropoffLocation(Location.builder()
                        .latitude(40.7128)
                        .longitude(-74.0060)
                        .address("555 Fitness Street, Queens, NY 11355")
                        .build())
                .passenger(passengers.get(2))
                .build();

        // Completed Ride 4
        Ride ride4 = Ride.builder()
                .status(RideStatus.COMPLETED)
                .requestedAt(LocalDateTime.now().minusDays(5))
                .startedAt(LocalDateTime.now().minusDays(5).plusMinutes(3))
                .completedAt(LocalDateTime.now().minusDays(5).plusMinutes(18))
                .fareAmount(new BigDecimal("22.75"))
                .pickupLocation(Location.builder()
                        .latitude(40.7128)
                        .longitude(-74.0060)
                        .address("Central Park West, New York, NY")
                        .build())
                .dropoffLocation(Location.builder()
                        .latitude(40.7589)
                        .longitude(-73.9851)
                        .address("Brooklyn Bridge, Brooklyn, NY")
                        .build())
                .passenger(passengers.get(0))
                .driver(drivers.get(1))
                .vehicle(vehicles.get(1))
                .build();

        return rideRepository.saveAll(Arrays.asList(ride1, ride2, ride3, ride4));
    }

    private void createPayments(List<Ride> rides) {
        // Payment for completed ride 1
        Payment payment1 = Payment.builder()
                .amount(rides.get(0).getFareAmount())
                .method(PaymentMethod.CREDIT_CARD)
                .status(PaymentStatus.COMPLETED)
                .transactionId("TXN-" + System.currentTimeMillis() + "-001")
                .ride(rides.get(0))
                .build();

        // Payment for in-progress ride 2
        Payment payment2 = Payment.builder()
                .amount(rides.get(1).getFareAmount())
                .method(PaymentMethod.APPLE_PAY)
                .status(PaymentStatus.PENDING)
                .transactionId("TXN-" + System.currentTimeMillis() + "-002")
                .ride(rides.get(1))
                .build();

        // Payment for completed ride 4
        Payment payment4 = Payment.builder()
                .amount(rides.get(3).getFareAmount())
                .method(PaymentMethod.CASH)
                .status(PaymentStatus.COMPLETED)
                .transactionId("TXN-" + System.currentTimeMillis() + "-004")
                .ride(rides.get(3))
                .build();

        paymentRepository.saveAll(Arrays.asList(payment1, payment2, payment4));
    }

    private void createRatings(List<Ride> rides) {
        // Ratings for completed ride 1
        Rating rating1Passenger = Rating.builder()
                .stars(5)
                .comment("Great driver! Very professional and safe.")
                .ratedBy(RatingSource.PASSENGER)
                .ride(rides.get(0))
                .build();

        Rating rating1Driver = Rating.builder()
                .stars(5)
                .comment("Excellent passenger. Respectful and on time.")
                .ratedBy(RatingSource.DRIVER)
                .ride(rides.get(0))
                .build();

        // Ratings for completed ride 4
        Rating rating4Passenger = Rating.builder()
                .stars(4)
                .comment("Good ride, but driver was a bit late.")
                .ratedBy(RatingSource.PASSENGER)
                .ride(rides.get(3))
                .build();

        Rating rating4Driver = Rating.builder()
                .stars(5)
                .comment("Nice passenger!")
                .ratedBy(RatingSource.DRIVER)
                .ride(rides.get(3))
                .build();

        ratingRepository.saveAll(Arrays.asList(rating1Passenger, rating1Driver, rating4Passenger, rating4Driver));
    }
}
