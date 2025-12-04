package com.uber.backend.shared.domain.valueobject;

public record Location(
    Double latitude,
    Double longitude,
    String address
) {}
