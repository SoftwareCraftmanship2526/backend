package com.uber.backend.ride.dto;

import com.uber.backend.shared.domain.valueobject.Location;

public class RidePriceRequestDto {

    private String type;
    private Location start;
    private Location end;
    private int durationMin;
    private double demandMultiplier;

    public RidePriceRequestDto() {}

    public RidePriceRequestDto(String type,
                               Location start,
                               Location end,
                               int durationMin,
                               double demandMultiplier) {
        this.type = type;
        this.start = start;
        this.end = end;
        this.durationMin = durationMin;
        this.demandMultiplier = demandMultiplier;
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Location getStart() { return start; }
    public void setStart(Location start) { this.start = start; }

    public Location getEnd() { return end; }
    public void setEnd(Location end) { this.end = end; }

    public int getDurationMin() { return durationMin; }
    public void setDurationMin(int durationMin) { this.durationMin = durationMin; }

    public double getDemandMultiplier() { return demandMultiplier; }
    public void setDemandMultiplier(double demandMultiplier) { this.demandMultiplier = demandMultiplier; }
}
