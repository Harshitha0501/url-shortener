package com.emergent.urlshortener.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
public class RedisConfig {

    @Value("${app.cache.ttl-seconds:3600}")
    private long cacheTtlSeconds;

    @Bean
    public RedisTemplate<String, Object> redisTemplate(
            RedisConnectionFactory cf,
            ObjectMapper mapper) {

        RedisTemplate<String, Object> template = new RedisTemplate<>();

        template.setConnectionFactory(cf);

        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        GenericJackson2JsonRedisSerializer value =
                new GenericJackson2JsonRedisSerializer();

        template.setValueSerializer(value);
        template.setHashValueSerializer(value);

        template.afterPropertiesSet();

        return template;
    }

    @Bean
    public CacheManager cacheManager(
            RedisConnectionFactory cf,
            ObjectMapper mapper) {

        GenericJackson2JsonRedisSerializer valueSerializer =
                new GenericJackson2JsonRedisSerializer();

        RedisCacheConfiguration config =
                RedisCacheConfiguration.defaultCacheConfig()
                        .entryTtl(Duration.ofSeconds(cacheTtlSeconds))
                        .disableCachingNullValues()
                        .serializeKeysWith(
                                RedisSerializationContext.SerializationPair
                                        .fromSerializer(
                                                new StringRedisSerializer()
                                        )
                        )
                        .serializeValuesWith(
                                RedisSerializationContext.SerializationPair
                                        .fromSerializer(
                                                valueSerializer
                                        )
                        );

        return RedisCacheManager.builder(cf)
                .cacheDefaults(config)
                .build();
    }
}