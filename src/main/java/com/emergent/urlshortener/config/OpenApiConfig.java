package com.emergent.urlshortener.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

@Configuration
public class OpenApiConfig {

    @Value("${app.base-url}")
    private String baseUrl;

    @Bean
    public OpenAPI urlShortenerOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("URL Shortener API")
                        .description("Scalable URL Shortener with click tracking, analytics, expiry, custom aliases, Redis caching and Redis-backed rate limiting.")
                        .version("v1.0.0")
                        .contact(new Contact().name("Emergent").email("hello@emergent.sh"))
                        .license(new License().name("MIT")))
                .servers(Collections.singletonList(new Server().url(baseUrl).description("Default server")));
    }
}
