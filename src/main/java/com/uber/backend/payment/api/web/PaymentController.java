package com.uber.backend.payment.api.web;

import com.uber.backend.payment.application.GetPaymentHistoryQueryHandler;
import com.uber.backend.payment.application.ProcessPaymentCommandHandler;
import com.uber.backend.payment.application.command.ProcessPaymentCommand;
import com.uber.backend.payment.application.command.ProcessPaymentResult;
import com.uber.backend.payment.application.query.GetPaymentHistoryQuery;
import com.uber.backend.payment.application.query.PaymentHistoryResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for payment operations.
 */
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payment", description = "Payment processing and management APIs")
public class PaymentController {

    private final ProcessPaymentCommandHandler processPaymentCommandHandler;
    private final GetPaymentHistoryQueryHandler getPaymentHistoryQueryHandler;

    @PostMapping("/process")
    @Operation(summary = "Process payment", description = "Process a payment for a ride")
    public ResponseEntity<ProcessPaymentResult> processPayment(@Valid @RequestBody ProcessPaymentCommand command) {
        ProcessPaymentResult result = processPaymentCommandHandler.handle(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping("/history")
    @Operation(summary = "Get payment history", description = "Retrieve payment history for a user")
    public ResponseEntity<PaymentHistoryResult> getPaymentHistory(@RequestParam Long userId) {
        GetPaymentHistoryQuery query = new GetPaymentHistoryQuery(userId);
        PaymentHistoryResult result = getPaymentHistoryQueryHandler.handle(query);
        return ResponseEntity.ok(result);
    }
}
