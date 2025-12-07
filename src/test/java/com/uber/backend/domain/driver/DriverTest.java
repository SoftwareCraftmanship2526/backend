package com.uber.backend.domain.driver;

import com.uber.backend.driver.domain.model.Driver;
import com.uber.backend.shared.domain.valueobject.Location;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DriverTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void givenValidRating_whenValidating_thenNoViolations() {
        // Given
        Driver driver = Driver.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .password("password123")
                .phoneNumber("+32471234567")
                .driverRating(4.5)
                .licenseNumber("DL123456")
                .isAvailable(true)
                .build();

        // When
        Set<ConstraintViolation<Driver>> violations = validator.validate(driver);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void givenRatingOutOfRange_whenValidating_thenRatingViolation() {
        // Given
        Driver driver = Driver.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .password("password123")
                .phoneNumber("+32471234567")
                .driverRating(6.0)
                .licenseNumber("DL123456")
                .isAvailable(true)
                .build();

        // When
        Set<ConstraintViolation<Driver>> violations = validator.validate(driver);

        // Then
        assertEquals(1, violations.size());
        assertEquals("driverRating", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void givenValidLicense_whenValidating_thenNoViolations() {
        // Given
        Driver driver = Driver.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .password("password123")
                .phoneNumber("+32471234567")
                .licenseNumber("DL123456")
                .driverRating(4.5)
                .isAvailable(true)
                .build();

        // When
        Set<ConstraintViolation<Driver>> violations = validator.validate(driver);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void givenEmptyLicense_whenValidating_thenLicenseViolation() {
        // Given
        Driver driver = Driver.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .password("password123")
                .phoneNumber("+32471234567")
                .licenseNumber("")
                .driverRating(4.5)
                .isAvailable(true)
                .build();

        // When
        Set<ConstraintViolation<Driver>> violations = validator.validate(driver);

        // Then
        assertEquals(1, violations.size());
        assertEquals("licenseNumber", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void givenValidLocation_whenValidating_thenNoViolations() {
        // Given
        Location location = new Location(50.8503, 4.3517, "Brussels");
        Driver driver = Driver.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .password("password123")
                .phoneNumber("+32471234567")
                .currentLocation(location)
                .licenseNumber("DL123456")
                .isAvailable(true)
                .build();

        // When
        Set<ConstraintViolation<Driver>> violations = validator.validate(driver);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void givenInvalidLocation_whenValidating_thenLocationViolation() {
        // Given
        Location location = new Location(95.0, 4.3517, "Invalid");
        Driver driver = Driver.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .password("password123")
                .phoneNumber("+32471234567")
                .currentLocation(location)
                .licenseNumber("DL123456")
                .isAvailable(true)
                .build();

        // When
        Set<ConstraintViolation<Driver>> violations = validator.validate(driver);

        // Then
        assertEquals(1, violations.size());
        assertEquals("currentLocation.latitude", violations.iterator().next().getPropertyPath().toString());
    }
}
