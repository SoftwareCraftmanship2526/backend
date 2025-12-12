package com.uber.backend.shared.api.exception;

import com.uber.backend.ride.application.exception.RideNotFoundException;
import com.uber.backend.shared.api.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Global exception handler for application-wide errors.
 * Handles exceptions that are not specific to authentication.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle ride not found exceptions.
     */
    @ExceptionHandler(RideNotFoundException.class)
    public ResponseEntity<ApiError> handleRideNotFound(RideNotFoundException ex) {
        ApiError error = new ApiError(
                "Ride Not Found",
                ex.getMessage(),
                HttpStatus.NOT_FOUND.value()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Handle illegal argument exceptions (business logic validation errors).
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException ex) {
        ApiError error = new ApiError(
                "Bad Request",
                ex.getMessage(),
                HttpStatus.BAD_REQUEST.value()
        );
        return ResponseEntity.badRequest().body(error);
    }

    /**
     * Handle general exceptions (fallback for unexpected errors).
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneralException(Exception ex) {
        // If exception has @ResponseStatus, rethrow it so Spring handles it properly
        ResponseStatus responseStatus = ex.getClass().getAnnotation(ResponseStatus.class);
        if (responseStatus != null) {
            throw new RuntimeException(ex);
        }

        ApiError error = new ApiError(
                "Internal Server Error",
                "An unexpected error occurred",
                HttpStatus.INTERNAL_SERVER_ERROR.value()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
