package com.uber.backend.domain.shared;

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

class LocationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void givenValidCoordinates_whenValidating_thenNoViolations() {
        // Given
        Location location = new Location(50.8503, 4.3517, "Brussels, Belgium");

        // When
        Set<ConstraintViolation<Location>> violations = validator.validate(location);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void givenLatitudeOutOfRange_whenValidating_thenLatitudeViolation() {
        // Given
        Location location = new Location(95.0, 4.3517, "Invalid Location");

        // When
        Set<ConstraintViolation<Location>> violations = validator.validate(location);

        // Then
        assertEquals(1, violations.size());
        assertEquals("latitude", violations.iterator().next().getPropertyPath().toString());
    }
}
