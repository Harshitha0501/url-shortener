package com.emergent.urlshortener.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShortenResponse {
    private String shortCode;
    private String shortUrl;
    private String longUrl;
    private Instant createdAt;
    private Instant expiresAt;
    private long clickCount;
    private boolean customAlias;
}
