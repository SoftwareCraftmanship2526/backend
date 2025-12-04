package com.uber.backend.api.web;

import com.uber.backend.infrastructure.seed.DataSeederService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/seed")
@RequiredArgsConstructor
public class DataSeederController {

    private final DataSeederService dataSeederService;

    @PostMapping("/reset")
    public ResponseEntity<Map<String, String>> resetDatabase() {
        dataSeederService.deleteAllData();
        dataSeederService.seedDatabase();

        Map<String, String> response = new HashMap<>();
        response.put("message", "Database has been reset and reseeded successfully!");
        response.put("status", "success");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/data")
    public ResponseEntity<Map<String, String>> seedDatabase() {
        dataSeederService.seedDatabase();

        Map<String, String> response = new HashMap<>();
        response.put("message", "Database seeded successfully!");
        response.put("status", "success");

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/data")
    public ResponseEntity<Map<String, String>> deleteAllData() {
        dataSeederService.deleteAllData();

        Map<String, String> response = new HashMap<>();
        response.put("message", "All data deleted successfully!");
        response.put("status", "success");

        return ResponseEntity.ok(response);
    }
}
