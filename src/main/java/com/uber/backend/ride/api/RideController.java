package com.uber.backend.ride.api;

import com.uber.backend.ride.application.RideService;
import com.uber.backend.ride.dto.RidePriceRequestDto;
import com.uber.backend.ride.dto.PriceEstimateDto;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/ride")
public class RideController {

    private final RideService rideService;

    public RideController(RideService rideService) {
        this.rideService = rideService;
    }

    @PostMapping("/price")
    public BigDecimal estimatePrice(@RequestBody RidePriceRequestDto request) {

        return rideService.calculatePrice(
                request.getType(),
                request.getStart(),
                request.getEnd(),
                request.getDurationMin(),
                request.getDemandMultiplier()
        );
    }

}
