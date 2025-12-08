package com.uber.backend.ride.api;

import com.uber.backend.ride.api.dto.PriceRequestDto;
import com.uber.backend.ride.application.RideService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ride")
public class RideController {
    private final RideService rideService;

    public RideController(RideService rideService) {
        this.rideService = rideService;
    }

    @PostMapping("/price")
    public Map<String, BigDecimal> priceRequest(@RequestBody PriceRequestDto request) {
        return rideService.priceRequest(request.getStart(), request.getEnd(), request.getDurationMin(), request.getDemandMutiplier());
    }
}
