package com.uber.backend.payment.domain.model;

import com.uber.backend.payment.domain.enums.PaymentMethod;
import com.uber.backend.payment.domain.enums.PaymentStatus;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    private Long id;
    private BigDecimal amount;
    private PaymentMethod method;
    private PaymentStatus status;
    private String transactionId;
    private Long rideId;
}
