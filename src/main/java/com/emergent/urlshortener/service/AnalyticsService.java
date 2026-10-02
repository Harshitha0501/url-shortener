package com.emergent.urlshortener.service;

import com.emergent.urlshortener.dto.AnalyticsResponse;
import com.emergent.urlshortener.exception.ResourceNotFoundException;
import com.emergent.urlshortener.model.ClickEvent;
import com.emergent.urlshortener.model.UrlMapping;
import com.emergent.urlshortener.repository.ClickEventRepository;
import com.emergent.urlshortener.repository.UrlMappingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final ClickEventRepository clickRepo;
    private final UrlMappingRepository urlRepo;
    private final UserAgentService userAgentService;
    private final GeoLocationService geoService;

    @Async
    @Transactional
    public void recordClick(String shortCode, String ip, String userAgent, String referrer) {
        try {
            Map<String, String> ua = userAgentService.parse(userAgent);
            Map<String, String> geo = geoService.lookup(ip);

            ClickEvent event = ClickEvent.builder()
                    .shortCode(shortCode)
                    .clickedAt(Instant.now())
                    .ipAddress(ip)
                    .userAgent(truncate(userAgent, 512))
                    .deviceType(ua.get("device"))
                    .browser(ua.get("browser"))
                    .os(ua.get("os"))
                    .referrer(truncate(referrer == null ? "direct" : referrer, 2048))
                    .countryCode(geo.get("countryCode"))
                    .countryName(geo.get("countryName"))
                    .city(geo.get("city"))
                    .build();
            clickRepo.save(event);
        } catch (Exception e) {
            log.warn("Failed to record click for {}: {}", shortCode, e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public AnalyticsResponse getAnalytics(String shortCode) {
        UrlMapping mapping = urlRepo.findByShortCode(shortCode)
                .orElseThrow(() -> new ResourceNotFoundException("Short code not found: " + shortCode));

        long total = clickRepo.countByShortCode(shortCode);
        List<ClickEvent> recent = clickRepo.findTop100ByShortCodeOrderByClickedAtDesc(shortCode);

        return AnalyticsResponse.builder()
                .shortCode(shortCode)
                .longUrl(mapping.getLongUrl())
                .totalClicks(total)
                .byDevice(toMap(clickRepo.countByDevice(shortCode)))
                .byBrowser(toMap(clickRepo.countByBrowser(shortCode)))
                .byOs(toMap(clickRepo.countByOs(shortCode)))
                .byCountry(toMap(clickRepo.countByCountry(shortCode)))
                .byReferrer(toMap(clickRepo.countByReferrer(shortCode)))
                .recentClicks(recent.stream().map(this::toRecent).collect(Collectors.toList()))
                .build();
    }

    private Map<String, Long> toMap(List<Object[]> rows) {
        Map<String, Long> map = new LinkedHashMap<>();
        for (Object[] row : rows) {
            String key = row[0] == null ? "Unknown" : row[0].toString();
            Long value = ((Number) row[1]).longValue();
            map.put(key, value);
        }
        return map;
    }

    private AnalyticsResponse.RecentClick toRecent(ClickEvent c) {
        return AnalyticsResponse.RecentClick.builder()
                .clickedAt(DateTimeFormatter.ISO_INSTANT.format(c.getClickedAt()))
                .ipAddress(c.getIpAddress())
                .deviceType(c.getDeviceType())
                .browser(c.getBrowser())
                .os(c.getOs())
                .referrer(c.getReferrer())
                .countryCode(c.getCountryCode())
                .city(c.getCity())
                .build();
    }

    private String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max);
    }
}
