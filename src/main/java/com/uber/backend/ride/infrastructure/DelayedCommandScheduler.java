package com.uber.backend.ride.infrastructure;

import java.time.LocalDateTime;

public interface DelayedCommandScheduler {
    void schedule(Object command, LocalDateTime executeAt);
}

