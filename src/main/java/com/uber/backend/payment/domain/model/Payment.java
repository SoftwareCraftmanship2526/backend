package com.uber.backend.payment.domain.model;

import com.uber.backend.payment.domain.enums.PaymentMethod;
import com.uber.backend.payment.domain.enums.PaymentStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    private Long id;

    @NotNull(message = "Payment amount is required")
    @Positive(message = "Payment amount must be positive")
    @DecimalMin(value = "0.01", message = "Payment amount must be at least 0.01")
    private BigDecimal amount;

    @NotNull(message = "Payment method is required")
    private PaymentMethod method;

    @NotNull(message = "Payment status is required")
    private PaymentStatus status;

    private String transactionId;

    @NotNull(message = "Ride ID is required")
    private Long rideId;
}
