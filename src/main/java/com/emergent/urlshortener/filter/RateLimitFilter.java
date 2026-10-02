package com.emergent.urlshortener.filter;

import com.emergent.urlshortener.exception.RateLimitExceededException;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

/**
 * Applies per-IP rate limiting to POST /api/urls (shorten endpoint) using
 * Redis-backed Bucket4j. Rate limits for other endpoints can be added by
 * extending the shouldFilter check.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final ProxyManager<byte[]> proxyManager;
    private final Supplier<BucketConfiguration> bucketConfiguration;

    @Value("${app.rate-limit.enabled:true}")
    private boolean enabled;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        if (!enabled || !shouldFilter(request)) {
            chain.doFilter(request, response);
            return;
        }
        String key = "rl:" + clientIp(request);
        Bucket bucket = proxyManager.builder().build(key.getBytes(StandardCharsets.UTF_8), bucketConfiguration);
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            response.setHeader("X-RateLimit-Remaining", String.valueOf(probe.getRemainingTokens()));
            chain.doFilter(request, response);
        } else {
            long waitSeconds = probe.getNanosToWaitForRefill() / 1_000_000_000L;
            response.setHeader("Retry-After", String.valueOf(Math.max(1, waitSeconds)));
            throw new RateLimitExceededException("Rate limit exceeded. Retry after " + waitSeconds + "s");
        }
    }

    private boolean shouldFilter(HttpServletRequest request) {
        return HttpMethod.POST.matches(request.getMethod())
                && request.getRequestURI().startsWith("/api/urls");
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isEmpty()) {
            return forwarded.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isEmpty()) return realIp;
        return request.getRemoteAddr();
    }
}
