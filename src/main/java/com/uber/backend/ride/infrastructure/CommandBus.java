package com.uber.backend.ride.infrastructure;

import com.uber.backend.ride.application.CancelRideIfUnmatchedCommandHandler;
import com.uber.backend.ride.application.command.CancelRideIfUnmatchedCommand;
import org.springframework.stereotype.Component;

@Component
public class CommandBus {

    private final CancelRideIfUnmatchedCommandHandler cancelRideIfUnmatchedCommandHandler;

    public CommandBus(CancelRideIfUnmatchedCommandHandler cancelRideIfUnmatchedCommandHandler) {
        this.cancelRideIfUnmatchedCommandHandler = cancelRideIfUnmatchedCommandHandler;
    }

    public void dispatch(Object command) {
        if (command instanceof CancelRideIfUnmatchedCommand c) {
            cancelRideIfUnmatchedCommandHandler.handle(c);
        }
    }
}

