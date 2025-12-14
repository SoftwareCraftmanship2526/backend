package com.uber.backend.shared.domain;

import java.util.ArrayList;
import java.util.List;

public class DomainEvents {

    private static final List<Object> events = new ArrayList<>();

    public static void raise(Object event) {
        events.add(event);
    }

    public static List<Object> pullEvents() {
        List<Object> raised = List.copyOf(events);
        events.clear();
        return raised;
    }
}

