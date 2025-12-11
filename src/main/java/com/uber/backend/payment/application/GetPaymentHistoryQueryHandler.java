package com.uber.backend.payment.application;

import com.uber.backend.payment.application.query.GetPaymentHistoryQuery;
import com.uber.backend.payment.application.query.PaymentHistoryResult;
import com.uber.backend.payment.infrastructure.persistence.PaymentEntity;
import com.uber.backend.payment.infrastructure.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Query handler for retrieving payment history.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class GetPaymentHistoryQueryHandler {

    private final PaymentRepository paymentRepository;

    public PaymentHistoryResult handle(GetPaymentHistoryQuery query) {
        log.info("Retrieving payment history for user: {}", query.userId());

        List<PaymentEntity> payments = paymentRepository.findAll().stream()
                .filter(p -> isUserPayment(p, query.userId()))
                .toList();

        List<PaymentHistoryResult.PaymentRecord> records = payments.stream()
                .map(p -> new PaymentHistoryResult.PaymentRecord(
                        p.getId(),
                        p.getRide().getId(),
                        p.getAmount(),
                        p.getMethod(),
                        p.getStatus(),
                        p.getTransactionId()
                ))
                .toList();

        return new PaymentHistoryResult(records);
    }

    private boolean isUserPayment(PaymentEntity payment, Long userId) {
        // Check if user is either the passenger or driver of the ride
        return payment.getRide().getPassenger().getId().equals(userId) ||
               payment.getRide().getDriver().getId().equals(userId);
    }
}
