package com.emergent.urlshortener.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "click_event", indexes = {
        @Index(name = "idx_click_short_code", columnList = "shortCode"),
        @Index(name = "idx_click_ts", columnList = "clickedAt")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClickEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 32)
    private String shortCode;

    @Column(nullable = false)
    private Instant clickedAt;

    @Column(length = 64)
    private String ipAddress;

    @Column(length = 512)
    private String userAgent;

    @Column(length = 64)
    private String deviceType;

    @Column(length = 64)
    private String browser;

    @Column(length = 64)
    private String os;

    @Column(length = 2048)
    private String referrer;

    @Column(length = 8)
    private String countryCode;

    @Column(length = 128)
    private String countryName;

    @Column(length = 128)
    private String city;
}
