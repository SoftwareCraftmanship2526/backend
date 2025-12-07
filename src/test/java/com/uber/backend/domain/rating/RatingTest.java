package com.uber.backend.domain.rating;

import com.uber.backend.rating.domain.enums.RatingSource;
import com.uber.backend.rating.domain.model.Rating;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RatingTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void givenValidStars_whenValidating_thenNoViolations() {
        // Given
        Rating rating = Rating.builder()
                .stars(5)
                .comment("Great ride!")
                .ratedBy(RatingSource.PASSENGER)
                .rideId(1L)
                .build();

        // When
        Set<ConstraintViolation<Rating>> violations = validator.validate(rating);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void givenStarsOutOfRange_whenValidating_thenStarsViolation() {
        // Given
        Rating rating = Rating.builder()
                .stars(6)
                .comment("Invalid rating")
                .ratedBy(RatingSource.DRIVER)
                .rideId(1L)
                .build();

        // When
        Set<ConstraintViolation<Rating>> violations = validator.validate(rating);

        // Then
        assertEquals(1, violations.size());
        assertEquals("stars", violations.iterator().next().getPropertyPath().toString());
    }
}
