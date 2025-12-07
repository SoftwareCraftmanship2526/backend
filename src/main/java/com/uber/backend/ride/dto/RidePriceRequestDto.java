package com.uber.backend.ride.dto;

import java.math.BigDecimal;

public class RidePriceRequestDto {
    private String type;
    private double distanceKm;
    private int durationMin;
    private double demandMultiplier;

    public RidePriceRequestDto() {}

    public RidePriceRequestDto(String type, double distanceKm, int durationMin, double demandMultiplier) {
        this.type = type;
        this.distanceKm = distanceKm;
        this.durationMin = durationMin;
        this.demandMultiplier = demandMultiplier;
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(double distanceKm) { this.distanceKm = distanceKm; }

    public int getDurationMin() { return durationMin; }
    public void setDurationMin(int durationMin) { this.durationMin = durationMin; }

    public double getDemandMultiplier() { return demandMultiplier; }
    public void setDemandMultiplier(double demandMultiplier) { this.demandMultiplier = demandMultiplier; }


}

