package com.emergent.urlshortener.controller;

import com.emergent.urlshortener.dto.AnalyticsResponse;
import com.emergent.urlshortener.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Analytics", description = "Click analytics for short links")
@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService service;

    @Operation(summary = "Get analytics for a short code",
            description = "Returns total clicks, breakdown by device / browser / OS / country / referrer, and the last 100 clicks.")
    @GetMapping("/{shortCode}")
    public ResponseEntity<AnalyticsResponse> analytics(@PathVariable String shortCode) {
        return ResponseEntity.ok(service.getAnalytics(shortCode));
    }
}
