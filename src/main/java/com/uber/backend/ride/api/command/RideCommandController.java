package com.uber.backend.ride.api.command;

import com.uber.backend.ride.application.command.RideCommandService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ride/command")
public class RideCommandController {

    private final RideCommandService rideCommandService;

    public RideCommandController(RideCommandService rideCommandService) {
        this.rideCommandService = rideCommandService;
    }

    // Example for later:
    // @PostMapping
    // public void bookRide(@RequestBody BookRideCommand command) {
    //     rideCommandService.bookRide(command);
    // }
}
