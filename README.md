# URL Shortener with Analytics

A **scalable URL shortener** built with **Java 8 + Spring Boot 2.7 + MySQL 8 + Redis**.
Focused backend project demonstrating REST API design, relational persistence,
Redis caching, distributed rate limiting and click analytics.

---

## Features

- **Shorten URL** — `POST /api/urls` (auto-generated Base62 code, 7 chars)
- **Custom aliases** — pass `customAlias` in the shorten request
- **Expiry dates** — pass `expiresAt` (ISO-8601); expired links return `410 Gone`
- **Redirect + click tracking** — `GET /{shortCode}` → HTTP 302
- **Click analytics** — device / browser / OS / country / referrer breakdown
  plus the last 100 raw click events (`GET /api/analytics/{shortCode}`)
- **Redis caching** — `shortCode → longUrl` cached with TTL for hot-path redirects
- **Redis-backed distributed rate limiting** — Bucket4j, per-IP, on the shorten endpoint
- **Async click persistence** — redirects respond immediately, analytics recorded async
- **Automatic expiry cleanup** — cron job purges expired links
- **Swagger UI / OpenAPI 3** — interactive docs at `/swagger-ui.html`
- **Flyway migrations** — versioned SQL under `src/main/resources/db/migration`
- **Actuator** — `/actuator/health`, `/actuator/prometheus`, etc.

---

## Tech Stack

| Layer                  | Choice                                          |
|------------------------|-------------------------------------------------|
| Language               | Java 8                                          |
| Framework              | Spring Boot 2.7.18                              |
| Persistence            | Spring Data JPA + MySQL 8 + Flyway              |
| Caching                | Spring Cache + Redis (Lettuce)                  |
| Rate limiting          | Bucket4j + Redis (Jedis)                        |
| API docs               | springdoc-openapi 1.7 (Swagger UI)              |
| User-Agent parsing     | Yauaa 7.x                                       |
| Geo lookup             | MaxMind GeoIP2 (optional GeoLite2-City DB)      |
| Build                  | Maven                                           |

---

## Quick Start (Docker Compose — recommended)

Requires Docker + Docker Compose.

```bash
docker compose up --build
```

The stack exposes:

- App:     http://localhost:8080
- Swagger: http://localhost:8080/swagger-ui.html
- MySQL:   localhost:3306 (user `urlshortener`, password `urlshortener`)
- Redis:   localhost:6379

Stop with `Ctrl+C`, remove volumes with `docker compose down -v`.

---

## Quick Start (Local, without Docker)

1. Install **Java 8**, **Maven 3.6+**, **MySQL 8**, **Redis 6/7**.
2. Create the database:
   ```sql
   CREATE DATABASE urlshortener CHARACTER SET utf8mb4;
   CREATE USER 'urlshortener'@'%' IDENTIFIED BY 'urlshortener';
   GRANT ALL PRIVILEGES ON urlshortener.* TO 'urlshortener'@'%';
   FLUSH PRIVILEGES;
   ```
3. Copy `.env.example` → `.env` and edit values if needed (or export the vars).
4. Run:
   ```bash
   mvn spring-boot:run
   ```

Flyway will apply `V1__init.sql` automatically on first startup.

---

## API Reference

Full interactive spec at **`/swagger-ui.html`**. OpenAPI JSON at **`/v3/api-docs`**.

### 1. Shorten a URL

```bash
curl -X POST http://localhost:8080/api/urls \
  -H "Content-Type: application/json" \
  -d '{
    "url": "https://www.example.com/some/very/long/path?with=params",
    "customAlias": "my-link",
    "expiresAt": "2026-12-31T23:59:59Z"
  }'
```

Response `201 Created`:

```json
{
  "shortCode": "my-link",
  "shortUrl":  "http://localhost:8080/my-link",
  "longUrl":   "https://www.example.com/some/very/long/path?with=params",
  "createdAt": "2026-01-15T10:00:00Z",
  "expiresAt": "2026-12-31T23:59:59Z",
  "clickCount": 0,
  "customAlias": true
}
```

`customAlias` and `expiresAt` are optional. Without a custom alias, a 7-char
Base62 code is generated.

### 2. Redirect

```bash
curl -I http://localhost:8080/my-link
# HTTP/1.1 302
# Location: https://www.example.com/some/very/long/path?with=params
```

### 3. Analytics

```bash
curl http://localhost:8080/api/analytics/my-link
```

```json
{
  "shortCode": "my-link",
  "longUrl": "https://www.example.com/...",
  "totalClicks": 42,
  "byDevice":   { "Desktop": 30, "Phone": 12 },
  "byBrowser":  { "Chrome": 25, "Safari": 12, "Firefox": 5 },
  "byOs":       { "Mac OS": 20, "Windows": 10, "iOS": 12 },
  "byCountry":  { "US": 22, "DE": 10, "IN": 10 },
  "byReferrer": { "direct": 30, "https://twitter.com": 12 },
  "recentClicks": [ /* last 100 events */ ]
}
```

### 4. Get short link metadata

```bash
curl http://localhost:8080/api/urls/my-link
```

### 5. Delete a short link

```bash
curl -X DELETE http://localhost:8080/api/urls/my-link
```

### Error responses

| Status | When                                               |
|--------|----------------------------------------------------|
| 400    | Invalid URL / bad request / validation error       |
| 404    | Short code not found                               |
| 409    | Custom alias already taken or reserved             |
| 410    | Short link has expired                             |
| 429    | Rate limit exceeded (per client IP)                |

---

## Configuration

All configuration is env-driven (see `.env.example`).

| Variable                    | Default                                        | Purpose                          |
|-----------------------------|------------------------------------------------|----------------------------------|
| `SERVER_PORT`               | `8080`                                         | HTTP port                        |
| `APP_BASE_URL`              | `http://localhost:8080`                        | Used to build `shortUrl` field   |
| `MYSQL_URL`                 | `jdbc:mysql://localhost:3306/urlshortener...`  | JDBC URL                         |
| `MYSQL_USER` / `PASSWORD`   | `urlshortener` / `urlshortener`                | DB creds                         |
| `REDIS_HOST` / `PORT`       | `localhost` / `6379`                           | Redis connection                 |
| `REDIS_PASSWORD`            | *(empty)*                                      | Optional Redis auth              |
| `APP_CACHE_TTL_SECONDS`     | `3600`                                         | TTL for cached URL mappings      |
| `APP_RATE_LIMIT_ENABLED`    | `true`                                         | Toggle rate limiting             |
| `APP_RATE_LIMIT_CAPACITY`   | `20`                                           | Bucket capacity per IP           |
| `APP_RATE_LIMIT_REFILL`     | `20`                                           | Tokens per refill window         |
| `APP_RATE_LIMIT_PERIOD`     | `60`                                           | Refill period in seconds         |
| `GEOIP_DB_PATH`             | *(empty)*                                      | Path to GeoLite2-City.mmdb       |
| `APP_EXPIRY_CRON`           | `0 0 * * * *` (every hour)                     | Expired-link cleanup schedule    |

---

## GeoIP Setup (optional)

Geo lookups gracefully degrade to `"Unknown"` if the DB is missing.

To enable:

1. Sign up (free) at <https://www.maxmind.com/en/geolite2/signup>.
2. Download **GeoLite2-City.mmdb**.
3. Mount / place the file, then set:
   ```
   GEOIP_DB_PATH=/absolute/path/to/GeoLite2-City.mmdb
   ```
4. Restart the app.

---

## Architecture Notes

- **Redirect hot path** (`GET /{code}`) hits Redis first (`@Cacheable`). On a hit
  we avoid MySQL entirely, then fire off async click recording so the redirect
  responds in single-digit milliseconds.
- **Click counter** is a `UPDATE ... SET click_count = click_count + 1` — safe
  under concurrent load. Detailed analytics live in a separate `click_event`
  table so heavy read queries don't lock hot paths.
- **Custom aliases** and generated codes share the same unique index. A
  small collision-retry loop guards against birthday collisions for random codes.
- **Rate limiting** is Redis-backed via Bucket4j, so limits work correctly
  across multiple app instances. Applied only to `POST /api/urls` — redirects
  are intentionally NOT rate limited so short links stay fast for real users.
- **Expiry** is enforced at read-time (`410 Gone`) and swept periodically by a
  scheduled cleanup job.

---

## Load / Performance Testing

Any HTTP load tool works. Example with **k6**:

```js
// smoke.js
import http from 'k6/http';
import { check } from 'k6';

export const options = { vus: 200, duration: '30s' };

export default function () {
  const res = http.get('http://localhost:8080/my-link', { redirects: 0 });
  check(res, { 'is 302': (r) => r.status === 302 });
}
```

```bash
k6 run smoke.js
```

Or Apache Bench:

```bash
ab -n 20000 -c 200 http://localhost:8080/my-link
```

Watch cache hit ratio and DB CPU as you tune `APP_CACHE_TTL_SECONDS`.

---

## Project Structure

```
src/main/java/com/emergent/urlshortener/
├── UrlShortenerApplication.java
├── config/          # Redis, OpenAPI, Rate-limit beans
├── controller/      # UrlController, RedirectController, AnalyticsController
├── dto/             # Request/response DTOs
├── exception/       # Custom exceptions + @RestControllerAdvice
├── filter/          # RateLimitFilter
├── model/           # JPA entities: UrlMapping, ClickEvent
├── repository/      # Spring Data repositories
├── service/         # UrlShortener, Analytics, UserAgent, GeoLocation, ExpiryCleanup
└── util/            # Base62Encoder

src/main/resources/
├── application.yml
└── db/migration/V1__init.sql
```

---

## Build

```bash
# Run tests
mvn test

# Package a fat JAR
mvn -DskipTests package

# Run the JAR
java -jar target/url-shortener-1.0.0.jar
```

---

## License

MIT
