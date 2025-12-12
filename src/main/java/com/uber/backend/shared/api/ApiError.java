package com.uber.backend.shared.api;

import java.time.Instant;

public class ApiError {

    private final String error;
    private final String message;
    private final Instant timestamp;
    private final int status;

    public ApiError(String error, String message, int status) {
        this.error = error;
        this.message = message;
        this.timestamp = Instant.now();
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }
}
