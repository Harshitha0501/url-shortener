package com.emergent.urlshortener.controller;

import com.emergent.urlshortener.model.UrlMapping;
import com.emergent.urlshortener.service.AnalyticsService;
import com.emergent.urlshortener.service.UrlShortenerService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;

@Tag(name = "Redirect", description = "Public redirect endpoint")
@RestController
@RequiredArgsConstructor
public class RedirectController {

    private final UrlShortenerService urlService;
    private final AnalyticsService analyticsService;

    @Hidden
    @GetMapping("/favicon.ico")
    public ResponseEntity<Void> favicon() {
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Redirect a short code to its long URL and record analytics")
    @GetMapping("/{shortCode:[a-zA-Z0-9_-]{3,32}}")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode, HttpServletRequest request) {
        UrlMapping mapping = urlService.getMapping(shortCode);
        urlService.incrementClickCount(shortCode);
        analyticsService.recordClick(
                shortCode,
                clientIp(request),
                request.getHeader("User-Agent"),
                request.getHeader("Referer"));

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(mapping.getLongUrl()));
        headers.setCacheControl("no-store");
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isEmpty()) return forwarded.split(",")[0].trim();
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isEmpty()) return realIp;
        return request.getRemoteAddr();
    }
}
