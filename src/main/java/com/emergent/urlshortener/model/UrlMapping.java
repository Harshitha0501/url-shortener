package com.emergent.urlshortener.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "url_mapping", indexes = {
        @Index(name = "idx_short_code", columnList = "shortCode", unique = true),
        @Index(name = "idx_expires_at", columnList = "expiresAt")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UrlMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 32)
    private String shortCode;

    @Column(nullable = false, length = 2048)
    private String longUrl;

    @Column(nullable = false)
    private Instant createdAt;

    @Column
    private Instant expiresAt;

    @Column(nullable = false)
    @Builder.Default
    private long clickCount = 0L;

    @Column(nullable = false)
    @Builder.Default
    private boolean customAlias = false;

    @Column(length = 128)
    private String createdByIp;
}
