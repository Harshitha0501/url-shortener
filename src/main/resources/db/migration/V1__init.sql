CREATE TABLE IF NOT EXISTS url_mapping (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    short_code      VARCHAR(32)   NOT NULL,
    long_url        VARCHAR(2048) NOT NULL,
    created_at      DATETIME(6)   NOT NULL,
    expires_at      DATETIME(6)   NULL,
    click_count     BIGINT        NOT NULL DEFAULT 0,
    custom_alias    BIT(1)        NOT NULL DEFAULT 0,
    created_by_ip   VARCHAR(128)  NULL,
    CONSTRAINT uk_short_code UNIQUE (short_code)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE INDEX idx_expires_at ON url_mapping (expires_at);

CREATE TABLE IF NOT EXISTS click_event (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    short_code      VARCHAR(32)   NOT NULL,
    clicked_at      DATETIME(6)   NOT NULL,
    ip_address      VARCHAR(64)   NULL,
    user_agent      VARCHAR(512)  NULL,
    device_type     VARCHAR(64)   NULL,
    browser         VARCHAR(64)   NULL,
    os              VARCHAR(64)   NULL,
    referrer        VARCHAR(2048) NULL,
    country_code    VARCHAR(8)    NULL,
    country_name    VARCHAR(128)  NULL,
    city            VARCHAR(128)  NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE INDEX idx_click_short_code ON click_event (short_code);
CREATE INDEX idx_click_ts         ON click_event (clicked_at);
