package com.uber.backend.shared.application;

import com.uber.backend.auth.domain.enums.Role;
import com.uber.backend.shared.domain.port.GeocodingPort;
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
    private final GeocodingPort geocodingPort;

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
        createPassengers();

        // Create Drivers
        List<DriverEntity> drivers = createDrivers();

        // Create Vehicles
        createVehicles(drivers);

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
        String address1 = geocodingPort.getAddressFromCoordinates(40.7128, -74.0060);
        driver1.setCurrentLocation(new Location(40.7128, -74.0060, address1));

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
        String address2 = geocodingPort.getAddressFromCoordinates(40.7589, -73.9851);
        driver2.setCurrentLocation(new Location(40.7589, -73.9851, address2));

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
        String address3 = geocodingPort.getAddressFromCoordinates(40.7580, -73.9855);
        driver3.setCurrentLocation(new Location(40.7580, -73.9855, address3));

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
}
