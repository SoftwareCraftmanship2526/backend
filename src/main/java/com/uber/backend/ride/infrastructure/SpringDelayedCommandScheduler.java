package com.uber.backend.ride.infrastructure;

import org.springframework.scheduling.TaskScheduler;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class SpringDelayedCommandScheduler implements DelayedCommandScheduler {

    private final TaskScheduler taskScheduler;
    private final CommandBus commandBus;
    private final ZoneId zoneId;

    public SpringDelayedCommandScheduler(TaskScheduler taskScheduler,
                                         CommandBus commandBus,
                                         ZoneId zoneId) {
        this.taskScheduler = taskScheduler;
        this.commandBus = commandBus;
        this.zoneId = zoneId;
    }

    @Override
    public void schedule(Object command, LocalDateTime executeAt) {
        Date executionDate = Date.from(
                executeAt.atZone(zoneId).toInstant()
        );

        taskScheduler.schedule(
                () -> commandBus.dispatch(command),
                executionDate
        );
    }
}
