package com.emergent.urlshortener;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Placeholder test to verify the test source tree compiles.
 * A full @SpringBootTest requires MySQL + Redis and is intended for CI.
 */
class SmokeTest {

    @Test
    void appClassAnnotated() {
        assert UrlShortenerApplication.class.isAnnotationPresent(SpringBootApplication.class);
    }
}