package com.uber.backend.domain.model;

import com.uber.backend.domain.enums.PaymentMethod;
import com.uber.backend.domain.enums.PaymentStatus;
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
    private Long rideId;  // Reference by ID

    public void initializePayment() {
        if (this.status == null) {
            this.status = PaymentStatus.PENDING;
        }
    }

    public void markAsCompleted(String transactionId) {
        if (this.status == PaymentStatus.PENDING) {
            this.status = PaymentStatus.COMPLETED;
            this.transactionId = transactionId;
        }
    }

    public void markAsFailed() {
        if (this.status == PaymentStatus.PENDING) {
            this.status = PaymentStatus.FAILED;
        }
    }

    public boolean isCompleted() {
        return this.status == PaymentStatus.COMPLETED;
    }

    public boolean isPending() {
        return this.status == PaymentStatus.PENDING;
    }

    public boolean hasValidAmount() {
        return amount != null && amount.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isValid() {
        return hasValidAmount() && method != null && rideId != null;
    }
}
