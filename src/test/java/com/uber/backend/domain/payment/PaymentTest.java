package com.uber.backend.domain.payment;

import com.uber.backend.payment.domain.enums.PaymentMethod;
import com.uber.backend.payment.domain.enums.PaymentStatus;
import com.uber.backend.payment.domain.model.Payment;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaymentTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void givenPositiveAmount_whenValidating_thenNoViolations() {
        // Given
        Payment payment = Payment.builder()
                .amount(new BigDecimal("50.00"))
                .method(PaymentMethod.CREDIT_CARD)
                .status(PaymentStatus.COMPLETED)
                .rideId(1L)
                .build();

        // When
        Set<ConstraintViolation<Payment>> violations = validator.validate(payment);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void givenNegativeAmount_whenValidating_thenAmountViolation() {
        // Given
        Payment payment = Payment.builder()
                .amount(new BigDecimal("-10.00"))
                .method(PaymentMethod.CREDIT_CARD)
                .status(PaymentStatus.FAILED)
                .rideId(1L)
                .build();

        // When
        Set<ConstraintViolation<Payment>> violations = validator.validate(payment);

        // Then
        assertEquals(2, violations.size());
        assertTrue(violations.stream()
                .allMatch(v -> v.getPropertyPath().toString().equals("amount")));
    }

    @Test
    void givenValidMethod_whenValidating_thenNoViolations() {
        // Given
        Payment payment = Payment.builder()
                .amount(new BigDecimal("50.00"))
                .method(PaymentMethod.WALLET)
                .status(PaymentStatus.COMPLETED)
                .rideId(1L)
                .build();

        // When
        Set<ConstraintViolation<Payment>> violations = validator.validate(payment);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void givenNullMethod_whenValidating_thenMethodViolation() {
        // Given
        Payment payment = Payment.builder()
                .amount(new BigDecimal("50.00"))
                .method(null)
                .status(PaymentStatus.PENDING)
                .rideId(1L)
                .build();

        // When
        Set<ConstraintViolation<Payment>> violations = validator.validate(payment);

        // Then
        assertEquals(1, violations.size());
        assertEquals("method", violations.iterator().next().getPropertyPath().toString());
    }
}
