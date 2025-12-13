package com.uber.backend.payment.api.web;

import com.uber.backend.auth.infrastructure.security.JwtUtil;
import com.uber.backend.payment.application.GetPaymentHistoryQueryHandler;
import com.uber.backend.payment.application.ProcessPaymentCommandHandler;
import com.uber.backend.payment.application.command.ProcessPaymentCommand;
import com.uber.backend.payment.application.command.ProcessPaymentResult;
import com.uber.backend.payment.application.query.GetPaymentHistoryQuery;
import com.uber.backend.payment.application.query.PaymentHistoryResult;
import com.uber.backend.shared.applicaition.CheckRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for payment operations.
 */
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payment", description = "Payment processing and management APIs")
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {

    private final ProcessPaymentCommandHandler processPaymentCommandHandler;
    private final GetPaymentHistoryQueryHandler getPaymentHistoryQueryHandler;
    private final JwtUtil jwtUtil;
    private final CheckRoleService checkRoleService;

    @PostMapping("/process")
    @Operation(summary = "Process payment", description = "Passenger processes a payment for a completed ride")
    public ResponseEntity<ProcessPaymentResult> processPayment(
            @Valid @RequestBody ProcessPaymentCommand command,
            HttpServletRequest httpRequest) {
        Long passengerId = jwtUtil.extractUserIdFromRequest(httpRequest);
        if (!checkRoleService.isPassenger(passengerId)) {
            throw new IllegalArgumentException("Only passengers can process payments. Please log in as a passenger.");
        }
        ProcessPaymentResult result = processPaymentCommandHandler.handle(command, passengerId);
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
