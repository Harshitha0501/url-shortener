package com.emergent.urlshortener.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.Refill;
import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.jedis.cas.JedisBasedProxyManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.time.Duration;
import java.util.function.Supplier;

@Configuration
public class RateLimitConfig {

    @Value("${spring.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.redis.port:6379}")
    private int redisPort;

    @Value("${spring.redis.password:}")
    private String redisPassword;

    @Value("${app.rate-limit.capacity:20}")
    private long capacity;

    @Value("${app.rate-limit.refill-tokens:20}")
    private long refillTokens;

    @Value("${app.rate-limit.refill-period-seconds:60}")
    private long refillPeriodSeconds;

    @Bean(destroyMethod = "close")
    public JedisPool jedisPool() {
        JedisPoolConfig cfg = new JedisPoolConfig();
        cfg.setMaxTotal(64);
        cfg.setMaxIdle(16);
        if (redisPassword == null || redisPassword.isEmpty()) {
            return new JedisPool(cfg, redisHost, redisPort, 2000);
        }
        return new JedisPool(cfg, redisHost, redisPort, 2000, redisPassword);
    }

    @Bean
    public ProxyManager<byte[]> bucket4jProxyManager(JedisPool jedisPool) {
        return JedisBasedProxyManager.builderFor(jedisPool)
                .withExpirationStrategy(
                        ExpirationAfterWriteStrategy.basedOnTimeForRefillingBucketUpToMax(
                                Duration.ofMinutes(10)))
                .build();
    }

    @Bean
    public Supplier<BucketConfiguration> bucketConfiguration() {
        return () -> BucketConfiguration.builder()
                .addLimit(Bandwidth.classic(capacity,
                        Refill.intervally(refillTokens, Duration.ofSeconds(refillPeriodSeconds))))
                .build();
    }
}
