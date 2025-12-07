package com.uber.backend.domain.driver;

import com.uber.backend.auth.domain.enums.Role;
import com.uber.backend.driver.domain.model.Account;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import lombok.experimental.SuperBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccountTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @SuperBuilder
    static class TestAccount extends Account {
    }

    @Test
    void givenValidEmail_whenValidating_thenNoViolations() {
        // Given
        Account account = TestAccount.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .password("password123")
                .phoneNumber("+32471234567")
                .role(Role.DRIVER)
                .build();

        // When
        Set<ConstraintViolation<Account>> violations = validator.validate(account);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void givenInvalidEmail_whenValidating_thenEmailViolation() {
        // Given
        Account account = TestAccount.builder()
                .firstName("John")
                .lastName("Doe")
                .email("invalid-email")
                .password("password123")
                .phoneNumber("+32471234567")
                .role(Role.DRIVER)
                .build();

        // When
        Set<ConstraintViolation<Account>> violations = validator.validate(account);

        // Then
        assertEquals(1, violations.size());
        assertEquals("email", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void givenValidPhone_whenValidating_thenNoViolations() {
        // Given
        Account account = TestAccount.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .password("password123")
                .phoneNumber("+32471234567")
                .role(Role.DRIVER)
                .build();

        // When
        Set<ConstraintViolation<Account>> violations = validator.validate(account);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void givenTooShortPhone_whenValidating_thenPhoneViolation() {
        // Given
        Account account = TestAccount.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .password("password123")
                .phoneNumber("123")
                .role(Role.DRIVER)
                .build();

        // When
        Set<ConstraintViolation<Account>> violations = validator.validate(account);

        // Then
        assertEquals(1, violations.size());
        assertEquals("phoneNumber", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void givenValidNames_whenValidating_thenNoViolations() {
        // Given
        Account account = TestAccount.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .password("password123")
                .phoneNumber("+32471234567")
                .role(Role.DRIVER)
                .build();

        // When
        Set<ConstraintViolation<Account>> violations = validator.validate(account);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void givenEmptyFirstName_whenValidating_thenFirstNameViolation() {
        // Given
        Account account = TestAccount.builder()
                .firstName("")
                .lastName("Doe")
                .email("john@example.com")
                .password("password123")
                .phoneNumber("+32471234567")
                .role(Role.DRIVER)
                .build();

        // When
        Set<ConstraintViolation<Account>> violations = validator.validate(account);

        // Then
        assertEquals(2, violations.size());
        assertTrue(violations.stream()
                .allMatch(v -> v.getPropertyPath().toString().equals("firstName")));
    }
}
