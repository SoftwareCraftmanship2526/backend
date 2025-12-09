package com.uber.backend.passenger.application.command;

public record RemoveSavedAddressCommand(
        Long passengerId,
        String address
) {}
