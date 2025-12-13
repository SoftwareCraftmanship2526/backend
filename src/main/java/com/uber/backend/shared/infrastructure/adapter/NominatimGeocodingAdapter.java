package com.uber.backend.shared.infrastructure.adapter;

import com.uber.backend.shared.domain.port.GeocodingPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Reverse geocoding adapter using OpenStreetMap Nominatim API (free, no API key needed).
 */
@Component
@Slf4j
public class NominatimGeocodingAdapter implements GeocodingPort {

    private static final String NOMINATIM_URL = "https://nominatim.openstreetmap.org/reverse";
    private final RestTemplate restTemplate;

    public NominatimGeocodingAdapter() {
        this.restTemplate = new RestTemplate();
    }

    @Override
    public String getAddressFromCoordinates(Double latitude, Double longitude) {
        try {
            String url = String.format(
                "%s?lat=%s&lon=%s&format=json",
                NOMINATIM_URL,
                latitude,
                longitude
            );

            log.debug("Fetching address for coordinates ({}, {})", latitude, longitude);
            
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response != null && response.containsKey("display_name")) {
                String address = (String) response.get("display_name");
                log.debug("Address found: {}", address);
                return address;
            }

            log.warn("No address found for coordinates ({}, {})", latitude, longitude);
            return String.format("Location (%.4f, %.4f)", latitude, longitude);

        } catch (Exception e) {
            log.error("Failed to fetch address for coordinates ({}, {}): {}", 
                latitude, longitude, e.getMessage());
            return String.format("Location (%.4f, %.4f)", latitude, longitude);
        }
    }
}
