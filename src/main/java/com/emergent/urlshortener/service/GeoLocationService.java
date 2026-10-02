package com.emergent.urlshortener.service;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.model.CityResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * Resolves an IP address to country/city using the MaxMind GeoLite2-City
 * database. If the database is not available at the configured path, geo
 * lookups gracefully return "Unknown" values so the service still works
 * out of the box.
 *
 * See README.md for instructions on downloading GeoLite2-City.mmdb.
 */
@Slf4j
@Service
public class GeoLocationService {

    @Value("${app.geoip.database-path:}")
    private String dbPath;

    private DatabaseReader reader;

    @PostConstruct
    public void init() {
        if (dbPath == null || dbPath.isEmpty()) {
            log.warn("GeoIP: no database path configured, geo lookups will be disabled");
            return;
        }
        File f = new File(dbPath);
        if (!f.exists()) {
            log.warn("GeoIP: database file not found at {} — geo lookups disabled. Download GeoLite2-City.mmdb from MaxMind and set app.geoip.database-path.", dbPath);
            return;
        }
        try {
            this.reader = new DatabaseReader.Builder(f).build();
            log.info("GeoIP database loaded from {}", dbPath);
        } catch (Exception e) {
            log.warn("GeoIP: failed to load database: {}", e.getMessage());
        }
    }

    public Map<String, String> lookup(String ip) {
        Map<String, String> out = new HashMap<>();
        out.put("countryCode", "Unknown");
        out.put("countryName", "Unknown");
        out.put("city", "Unknown");
        if (reader == null || ip == null || ip.isEmpty() || isLocal(ip)) {
            return out;
        }
        try {
            CityResponse resp = reader.city(InetAddress.getByName(ip));
            if (resp.getCountry() != null) {
                if (resp.getCountry().getIsoCode() != null) {
                    out.put("countryCode", resp.getCountry().getIsoCode());
                }
                if (resp.getCountry().getName() != null) {
                    out.put("countryName", resp.getCountry().getName());
                }
            }
            if (resp.getCity() != null && resp.getCity().getName() != null) {
                out.put("city", resp.getCity().getName());
            }
        } catch (Exception e) {
            log.debug("GeoIP lookup failed for {}: {}", ip, e.getMessage());
        }
        return out;
    }

    private boolean isLocal(String ip) {
        return ip.startsWith("127.") || ip.startsWith("10.")
                || ip.startsWith("192.168.") || ip.equals("::1")
                || ip.startsWith("172.") || ip.equalsIgnoreCase("localhost");
    }

    @PreDestroy
    public void close() {
        if (reader != null) {
            try { reader.close(); } catch (Exception ignored) { }
        }
    }
}
