package com.uber.backend.controller;

import com.uber.backend.service.DataSeederService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for manually triggering database seeding operations.
 *
 * Endpoints:
 * - POST /api/seed/reset - Delete all data and reseed the database
 * - POST /api/seed/data - Seed the database (without deleting existing data)
 * - DELETE /api/seed/data - Delete all data from the database
 */
@RestController
@RequestMapping("/api/seed")
@RequiredArgsConstructor
public class DataSeederController {

    private final DataSeederService dataSeederService;

    /**
     * Reset and reseed the entire database.
     * DELETE all existing data first, then INSERT seed data.
     *
     * Usage: POST http://localhost:8080/api/seed/reset
     */
    @PostMapping("/reset")
    public ResponseEntity<Map<String, String>> resetDatabase() {
        dataSeederService.deleteAllData();
        dataSeederService.seedDatabase();

        Map<String, String> response = new HashMap<>();
        response.put("message", "Database has been reset and reseeded successfully!");
        response.put("status", "success");

        return ResponseEntity.ok(response);
    }

    /**
     * Seed the database with sample data (without deleting existing data).
     *
     * Usage: POST http://localhost:8080/api/seed/data
     */
    @PostMapping("/data")
    public ResponseEntity<Map<String, String>> seedDatabase() {
        dataSeederService.seedDatabase();

        Map<String, String> response = new HashMap<>();
        response.put("message", "Database seeded successfully!");
        response.put("status", "success");

        return ResponseEntity.ok(response);
    }

    /**
     * Delete all data from the database.
     *
     * Usage: DELETE http://localhost:8080/api/seed/data
     */
    @DeleteMapping("/data")
    public ResponseEntity<Map<String, String>> deleteAllData() {
        dataSeederService.deleteAllData();

        Map<String, String> response = new HashMap<>();
        response.put("message", "All data deleted successfully!");
        response.put("status", "success");

        return ResponseEntity.ok(response);
    }
}
