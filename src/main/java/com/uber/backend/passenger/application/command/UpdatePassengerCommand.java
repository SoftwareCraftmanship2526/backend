package com.uber.backend.passenger.application.command;

import jakarta.validation.constraints.NotBlank;

public record UpdatePassengerCommand(
        Long passengerId,

        @NotBlank(message = "First name is required")
        String firstName,

        @NotBlank(message = "Last name is required")
        String lastName,

        @NotBlank(message = "Phone number is required")
        String phoneNumber
) {}
