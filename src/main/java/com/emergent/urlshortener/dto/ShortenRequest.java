package com.emergent.urlshortener.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShortenRequest {

    @Size(min = 1, max = 2048, message = "URL must be between 1 and 2048 characters")
    private String url;

    @Pattern(
            regexp = "^[a-zA-Z0-9_-]{3,32}$",
            message = "Custom alias must be 3-32 chars: letters, digits, '-' or '_'"
    )
    private String customAlias;

    /** Optional expiry (ISO-8601). If null, link never expires. */
    private Instant expiresAt;
}
