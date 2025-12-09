package com.uber.backend.passenger.application.command;

public record AddSavedAddressCommand(
        Long passengerId,
        String address
) {}
