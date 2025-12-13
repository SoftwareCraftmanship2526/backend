package com.uber.backend.shared.domain.port;

/**
 * Port for reverse geocoding - converting coordinates to addresses.
 */
public interface GeocodingPort {
    
    /**
     * Convert latitude/longitude to a human-readable address.
     * 
     * @param latitude Latitude coordinate
     * @param longitude Longitude coordinate
     * @return Human-readable address (e.g., "Rue de la Loi 16, 1000 Brussels, Belgium")
     */
    String getAddressFromCoordinates(Double latitude, Double longitude);
}
