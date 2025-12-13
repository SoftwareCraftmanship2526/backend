package com.uber.backend.payment.api.web;

import com.uber.backend.payment.application.CalculateFareQueryHandler;
import com.uber.backend.payment.application.query.CalculateFareQuery;
import com.uber.backend.payment.application.query.FareCalculationResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for fare calculation operations.
 */
@RestController
@RequestMapping("/api/fares")
@RequiredArgsConstructor
@Tag(name = "Fare", description = "Fare calculation APIs")
@SecurityRequirement(name = "bearerAuth")
public class FareController {

    private final CalculateFareQueryHandler calculateFareQueryHandler;

    @PostMapping("/calculate")
    @Operation(summary = "Calculate fare", description = "Calculate fare for a ride based on distance, duration, ride type, and demand multiplier")
    public ResponseEntity<FareCalculationResult> calculateFare(@Valid @RequestBody CalculateFareQuery query) {
        FareCalculationResult result = calculateFareQueryHandler.handle(query);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/estimate")
    @Operation(summary = "Estimate fare", description = "Get fare estimate with query parameters")
    public ResponseEntity<FareCalculationResult> estimateFare(
            @RequestParam Double distanceKm,
            @RequestParam Integer durationMin,
            @RequestParam String rideType,
            @RequestParam(defaultValue = "1.0") Double demandMultiplier) {

        CalculateFareQuery query = new CalculateFareQuery(
                distanceKm,
                durationMin,
                com.uber.backend.ride.domain.enums.RideType.valueOf(rideType.toUpperCase()),
                demandMultiplier
        );

        FareCalculationResult result = calculateFareQueryHandler.handle(query);
        return ResponseEntity.ok(result);
    }
}
