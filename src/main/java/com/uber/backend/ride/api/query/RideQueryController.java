package com.uber.backend.ride.api.query;

import com.uber.backend.ride.api.dto.PriceRequestDto;
import com.uber.backend.ride.application.query.RideQueryService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/ride/query")
public class RideQueryController {

    private final RideQueryService rideQueryService;

    public RideQueryController(RideQueryService rideQueryService) {
        this.rideQueryService = rideQueryService;
    }

    @PostMapping("/price")
    public Map<String, BigDecimal> priceRequest(@RequestBody PriceRequestDto request) {
        return rideQueryService.priceRequest(
                request.getStart(),
                request.getEnd(),
                request.getDurationMin(),
                request.getDemandMultiplier()
        );
    }
}
