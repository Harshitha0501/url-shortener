package com.emergent.urlshortener.service;

import com.emergent.urlshortener.repository.UrlMappingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Periodically purges expired short links from the database.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExpiryCleanupService {

    private final UrlMappingRepository repository;

    /** Runs every hour by default. */
    @Scheduled(cron = "${app.expiry.cleanup-cron:0 0 * * * *}")
    @Transactional
    public void purgeExpired() {
        int deleted = repository.deleteExpired(Instant.now());
        if (deleted > 0) {
            log.info("Purged {} expired short link(s)", deleted);
        }
    }
}
