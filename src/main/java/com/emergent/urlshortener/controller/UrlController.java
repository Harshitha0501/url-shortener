package com.emergent.urlshortener.controller;

import com.emergent.urlshortener.dto.ShortenRequest;
import com.emergent.urlshortener.dto.ShortenResponse;
import com.emergent.urlshortener.service.UrlShortenerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@Tag(name = "URLs", description = "Create, inspect and delete short links")
@RestController
@RequestMapping("/api/urls")
@RequiredArgsConstructor
public class UrlController {

    private final UrlShortenerService service;

    @Operation(summary = "Shorten a long URL",
            description = "Creates a short link. Supports optional custom alias and expiry date.")
    @PostMapping
    public ResponseEntity<ShortenResponse> shorten(@Valid @RequestBody ShortenRequest req,
                                                   HttpServletRequest request) {
        ShortenResponse resp = service.shorten(req, clientIp(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    @Operation(summary = "Get short link metadata")
    @GetMapping("/{shortCode}")
    public ResponseEntity<ShortenResponse> info(@PathVariable String shortCode) {
        return ResponseEntity.ok(service.getInfo(shortCode));
    }

    @Operation(summary = "Delete a short link")
    @DeleteMapping("/{shortCode}")
    public ResponseEntity<Void> delete(@PathVariable String shortCode) {
        service.delete(shortCode);
        return ResponseEntity.noContent().build();
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isEmpty()) return forwarded.split(",")[0].trim();
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isEmpty()) return realIp;
        return request.getRemoteAddr();
    }
}
