package com.emergent.urlshortener.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserAgentService {

    private UserAgentAnalyzer analyzer;

    @PostConstruct
    public void init() {
        this.analyzer = UserAgentAnalyzer.newBuilder()
                .hideMatcherLoadStats()
                .withCache(10_000)
                .withField(UserAgent.DEVICE_CLASS)
                .withField(UserAgent.AGENT_NAME)
                .withField(UserAgent.OPERATING_SYSTEM_NAME)
                .build();
    }

    public Map<String, String> parse(String uaString) {
        Map<String, String> result = new HashMap<>();
        if (uaString == null || uaString.isEmpty()) {
            result.put("device", "Unknown");
            result.put("browser", "Unknown");
            result.put("os", "Unknown");
            return result;
        }
        try {
            UserAgent ua = analyzer.parse(uaString);
            result.put("device", ua.getValue(UserAgent.DEVICE_CLASS));
            result.put("browser", ua.getValue(UserAgent.AGENT_NAME));
            result.put("os", ua.getValue(UserAgent.OPERATING_SYSTEM_NAME));
        } catch (Exception e) {
            log.warn("Failed to parse user agent: {}", e.getMessage());
            result.put("device", "Unknown");
            result.put("browser", "Unknown");
            result.put("os", "Unknown");
        }
        return result;
    }
}
