package com.emergent.urlshortener.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalyticsResponse {
    private String shortCode;
    private String longUrl;
    private long totalClicks;
    private Map<String, Long> byDevice;
    private Map<String, Long> byBrowser;
    private Map<String, Long> byOs;
    private Map<String, Long> byCountry;
    private Map<String, Long> byReferrer;
    private List<RecentClick> recentClicks;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RecentClick {
        private String clickedAt;
        private String ipAddress;
        private String deviceType;
        private String browser;
        private String os;
        private String referrer;
        private String countryCode;
        private String city;
    }
}
