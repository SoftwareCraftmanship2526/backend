package com.uber.backend.shared.api.web;

import com.uber.backend.shared.api.seed.DataSeederService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for database seeding operations.
 * Provides endpoints to seed, reset, and delete test data.
 */
@RestController
@RequestMapping("/api/seed")
@RequiredArgsConstructor
@Tag(name = "Data Seeder", description = "Database seeding and test data management endpoints")
public class DataSeederController {

    private final DataSeederService dataSeederService;

    /**
     * Reset database with fresh seed data.
     *
     * @return Success message
     */
    @PostMapping("/reset")
    @Operation(
            summary = "Reset Database",
            description = "Delete all existing data and reseed the database with fresh test data. Use for development/testing only."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Database reset and reseeded successfully",
                    content = @Content(schema = @Schema(implementation = Map.class))
            )
    })
    public ResponseEntity<Map<String, String>> resetDatabase() {
        dataSeederService.deleteAllData();
        dataSeederService.seedDatabase();

        Map<String, String> response = new HashMap<>();
        response.put("message", "Database has been reset and reseeded successfully!");
        response.put("status", "success");

        return ResponseEntity.ok(response);
    }

    /**
     * Seed database with test data.
     *
     * @return Success message
     */
    @PostMapping("/data")
    @Operation(
            summary = "Seed Database",
            description = "Add test data to the database including sample users, drivers, and vehicles. For development/testing only."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Database seeded successfully",
                    content = @Content(schema = @Schema(implementation = Map.class))
            )
    })
    public ResponseEntity<Map<String, String>> seedDatabase() {
        dataSeederService.seedDatabase();

        Map<String, String> response = new HashMap<>();
        response.put("message", "Database seeded successfully!");
        response.put("status", "success");

        return ResponseEntity.ok(response);
    }

    /**
     * Delete all data from database.
     *
     * @return Success message
     */
    @DeleteMapping("/data")
    @Operation(
            summary = "Delete All Data",
            description = "Remove all data from the database. Use with caution! For development/testing only."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "All data deleted successfully",
                    content = @Content(schema = @Schema(implementation = Map.class))
            )
    })
    public ResponseEntity<Map<String, String>> deleteAllData() {
        dataSeederService.deleteAllData();

        Map<String, String> response = new HashMap<>();
        response.put("message", "All data deleted successfully!");
        response.put("status", "success");

        return ResponseEntity.ok(response);
    }
}
