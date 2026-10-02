package com.emergent.urlshortener.service;

import com.emergent.urlshortener.dto.ShortenRequest;
import com.emergent.urlshortener.dto.ShortenResponse;
import com.emergent.urlshortener.exception.BadRequestException;
import com.emergent.urlshortener.exception.ConflictException;
import com.emergent.urlshortener.exception.GoneException;
import com.emergent.urlshortener.exception.ResourceNotFoundException;
import com.emergent.urlshortener.model.UrlMapping;
import com.emergent.urlshortener.repository.UrlMappingRepository;
import com.emergent.urlshortener.util.Base62Encoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class UrlShortenerService {

    private static final Set<String> ALLOWED_SCHEMES =
            new HashSet<>(Arrays.asList("http", "https"));

    private static final Set<String> RESERVED_ALIASES =
            new HashSet<>(Arrays.asList(
                    "api",
                    "admin",
                    "swagger-ui",
                    "v3",
                    "actuator",
                    "docs",
                    "health"
            ));

    private static final int SHORT_CODE_LENGTH = 7;
    private static final int MAX_COLLISION_RETRIES = 5;

    private final UrlMappingRepository repository;

    @Value("${app.base-url}")
    private String baseUrl;

    @Transactional
    public ShortenResponse shorten(ShortenRequest request, String clientIp) {

        validateUrl(request.getUrl());

        if (request.getExpiresAt() != null &&
                request.getExpiresAt().isBefore(Instant.now())) {

            throw new BadRequestException(
                    "expiresAt must be in the future");
        }

        String code;

        if (request.getCustomAlias() != null &&
                !request.getCustomAlias().isEmpty()) {

            String alias = request.getCustomAlias();

            if (RESERVED_ALIASES.contains(alias.toLowerCase())) {
                throw new ConflictException(
                        "Alias '" + alias + "' is reserved");
            }

            if (repository.existsByShortCode(alias)) {
                throw new ConflictException(
                        "Alias '" + alias + "' is already taken");
            }

            code = alias;

        } else {
            code = generateUniqueCode();
        }

        UrlMapping mapping = UrlMapping.builder()
                .shortCode(code)
                .longUrl(request.getUrl())
                .createdAt(Instant.now())
                .expiresAt(request.getExpiresAt())
                .customAlias(
                        request.getCustomAlias() != null &&
                        !request.getCustomAlias().isEmpty()
                )
                .createdByIp(clientIp)
                .clickCount(0L)
                .build();

        try {
            mapping = repository.save(mapping);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException(
                    "Short code collision, please retry");
        }

        log.info(
                "Created short link {} -> {}",
                code,
                request.getUrl()
        );

        return toResponse(mapping);
    }

    @Transactional(readOnly = true)
    public UrlMapping getMapping(String shortCode) {

        UrlMapping mapping = repository.findByShortCode(shortCode)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Short code not found: " + shortCode
                        ));

        if (mapping.getExpiresAt() != null &&
                mapping.getExpiresAt().isBefore(Instant.now())) {

            throw new GoneException(
                    "Short link has expired");
        }

        return mapping;
    }

    @Transactional(readOnly = true)
    public ShortenResponse getInfo(String shortCode) {

        UrlMapping mapping = repository.findByShortCode(shortCode)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Short code not found: " + shortCode
                        ));

        return toResponse(mapping);
    }

    @CacheEvict(value = "urlmap", key = "#shortCode")
    @Transactional
    public void delete(String shortCode) {

        UrlMapping mapping = repository.findByShortCode(shortCode)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Short code not found: " + shortCode
                        ));

        repository.delete(mapping);
    }

    @Transactional
    public void incrementClickCount(String shortCode) {
        repository.incrementClickCount(shortCode);
    }

    private String generateUniqueCode() {

        for (int i = 0; i < MAX_COLLISION_RETRIES; i++) {

            String candidate =
                    Base62Encoder.random(SHORT_CODE_LENGTH);

            if (!repository.existsByShortCode(candidate)) {
                return candidate;
            }
        }

        throw new ConflictException(
                "Failed to generate unique short code after retries");
    }

    private void validateUrl(String url) {

        if (url == null || url.trim().isEmpty()) {
            throw new BadRequestException("url is required");
        }

        try {

            URI uri = new URI(url);

            if (uri.getScheme() == null ||
                    !ALLOWED_SCHEMES.contains(
                            uri.getScheme().toLowerCase())) {

                throw new BadRequestException(
                        "Only http and https URLs are supported");
            }

            if (uri.getHost() == null ||
                    uri.getHost().isEmpty()) {

                throw new BadRequestException(
                        "URL must contain a valid host");
            }

        } catch (URISyntaxException e) {

            throw new BadRequestException(
                    "Invalid URL: " + e.getMessage());
        }
    }

    private ShortenResponse toResponse(UrlMapping m) {

        String base = baseUrl.endsWith("/")
                ? baseUrl.substring(0, baseUrl.length() - 1)
                : baseUrl;

        return ShortenResponse.builder()
                .shortCode(m.getShortCode())
                .shortUrl(base + "/" + m.getShortCode())
                .longUrl(m.getLongUrl())
                .createdAt(m.getCreatedAt())
                .expiresAt(m.getExpiresAt())
                .clickCount(m.getClickCount())
                .customAlias(m.isCustomAlias())
                .build();
    }
}