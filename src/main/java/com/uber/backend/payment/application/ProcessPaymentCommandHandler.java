package com.uber.backend.payment.application;

import com.uber.backend.payment.application.command.ProcessPaymentCommand;
import com.uber.backend.payment.application.command.ProcessPaymentResult;
import com.uber.backend.payment.domain.enums.PaymentStatus;
import com.uber.backend.payment.infrastructure.persistence.PaymentEntity;
import com.uber.backend.payment.infrastructure.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Command handler for processing payments.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ProcessPaymentCommandHandler {

    private final PaymentRepository paymentRepository;

    @Transactional
    public ProcessPaymentResult handle(ProcessPaymentCommand command) {
        log.info("Processing payment for ride: {}", command.rideId());

        // Find existing payment for this ride
        PaymentEntity payment = paymentRepository.findByRideId(command.rideId())
                .orElseThrow(() -> new IllegalArgumentException("No pending payment found for ride: " + command.rideId()));

        // Verify payment is in pending status
        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new IllegalStateException("Payment already processed with status: " + payment.getStatus());
        }

        // Process payment (mock - just generate transaction ID)
        try {
            // Update payment method from command
            // Amount is already set from the ride's calculated fare
            payment.setMethod(command.paymentMethod());

            // Generate mock transaction ID
            String transactionId = "TXN-" + UUID.randomUUID().toString();
            log.info("Payment processed successfully: transactionId={}", transactionId);

            // Update payment status
            payment.setStatus(PaymentStatus.COMPLETED);
            payment.setTransactionId(transactionId);
            payment = paymentRepository.save(payment);

            log.info("Payment processed successfully: paymentId={}, transactionId={}",
                    payment.getId(), transactionId);

            return new ProcessPaymentResult(
                    payment.getId(),
                    command.rideId(),
                    payment.getAmount(),
                    payment.getMethod(),
                    PaymentStatus.COMPLETED,
                    transactionId
            );

        } catch (Exception e) {
            // Update payment status to failed
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);

            log.error("Payment processing failed: paymentId={}", payment.getId(), e);
            throw new RuntimeException("Payment processing failed", e);
        }
    }
}
