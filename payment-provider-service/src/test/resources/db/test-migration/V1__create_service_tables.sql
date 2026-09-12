CREATE TABLE merchants (
    id          VARCHAR(50) PRIMARY KEY,
    secret_key  VARCHAR(255) NOT NULL,
    name        VARCHAR(100),
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    role        VARCHAR(50) NOT NULL
);

CREATE TABLE transactions (
    id                     UUID PRIMARY KEY,
    merchant_id            VARCHAR(50) NOT NULL REFERENCES merchants(id),
    amount                 NUMERIC(18,2) NOT NULL,
    currency_code          VARCHAR(3) NOT NULL,
    operation              VARCHAR(50) NOT NULL,
    status                 VARCHAR(20) NOT NULL,
    description            VARCHAR(2048),
    notification_url       VARCHAR(2048),
    metadata_id            BIGINT REFERENCES transaction_metadata(id)
);

CREATE TABLE transaction_metadata (
    id        BIGSERIAL PRIMARY KEY,
    timestamp TIMESTAMP,
    trace_id  VARCHAR(255)
);