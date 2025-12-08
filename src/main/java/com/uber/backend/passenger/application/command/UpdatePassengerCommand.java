package com.uber.backend.passenger.application.command;

public record UpdatePassengerCommand(
        Long passengerId,
        String firstName,
        String lastName,
        String phoneNumber
) {}
