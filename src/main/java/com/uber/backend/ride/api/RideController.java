package com.uber.backend.ride.api;

import com.uber.backend.ride.application.RideService;
import com.uber.backend.ride.dto.RidePriceRequestDto;
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
    public BigDecimal getPrice(@RequestBody RidePriceRequestDto ridePriceRequestDto) {
        return rideService.calculatePrice(ridePriceRequestDto.getType(), ridePriceRequestDto.getDistanceKm(), ridePriceRequestDto.getDurationMin(), ridePriceRequestDto.getDemandMultiplier());
    }

}
