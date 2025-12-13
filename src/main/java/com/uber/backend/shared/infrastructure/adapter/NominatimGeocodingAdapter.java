package com.uber.backend.shared.infrastructure.adapter;

import com.uber.backend.shared.domain.port.GeocodingPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
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

    public NominatimGeocodingAdapter(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
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
            
            // Nominatim requires User-Agent header
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "UberBackendApp/1.0");
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<Map> responseEntity = restTemplate.exchange(
                url, 
                HttpMethod.GET, 
                entity, 
                Map.class
            );
            
            Map<String, Object> response = responseEntity.getBody();

            if (response != null && response.containsKey("address")) {
                @SuppressWarnings("unchecked")
                Map<String, String> addressParts = (Map<String, String>) response.get("address");
                
                // Build a clean address: house_number, street, city, postcode, country
                StringBuilder cleanAddress = new StringBuilder();
                
                // Add house number if available
                String houseNumber = addressParts.get("house_number");
                if (houseNumber != null && !houseNumber.isEmpty()) {
                    cleanAddress.append(houseNumber);
                }
                
                // Add road/street if available
                String road = addressParts.get("road");
                if (road != null && !road.isEmpty()) {
                    if (cleanAddress.length() > 0) cleanAddress.append(", ");
                    cleanAddress.append(road);
                }
                
                // Add city/town/village
                String city = addressParts.getOrDefault("city", 
                             addressParts.getOrDefault("town", 
                             addressParts.get("village")));
                if (city != null && !city.isEmpty()) {
                    if (cleanAddress.length() > 0) cleanAddress.append(", ");
                    cleanAddress.append(city);
                }
                
                // Add postal code if available
                String postcode = addressParts.get("postcode");
                if (postcode != null && !postcode.isEmpty()) {
                    if (cleanAddress.length() > 0) cleanAddress.append(", ");
                    cleanAddress.append(postcode);
                }
                
                // Add country (only first language, not all translations)
                String country = addressParts.get("country");
                if (country != null && !country.isEmpty()) {
                    // Split by "/" to get only first language (e.g. "België" instead of "België / Belgique / Belgien")
                    String cleanCountry = country.split("/")[0].trim();
                    if (cleanAddress.length() > 0) cleanAddress.append(", ");
                    cleanAddress.append(cleanCountry);
                }
                
                String finalAddress = cleanAddress.toString();
                if (!finalAddress.isEmpty()) {
                    log.debug("Address found: {}", finalAddress);
                    return finalAddress;
                }
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
