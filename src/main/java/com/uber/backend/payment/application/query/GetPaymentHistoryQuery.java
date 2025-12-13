package com.uber.backend.payment.application.query;

import jakarta.validation.constraints.NotNull;

public record GetPaymentHistoryQuery(
        @NotNull(message = "User ID is required")
        Long userId
) {}
