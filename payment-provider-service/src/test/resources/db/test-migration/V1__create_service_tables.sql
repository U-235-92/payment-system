CREATE TABLE merchants (
    id          VARCHAR(50) PRIMARY KEY,
    secret_key  VARCHAR(255) NOT NULL,
    name        VARCHAR(100),
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    role        VARCHAR(50) NOT NULL
);

CREATE TABLE transactions (
    id           UUID PRIMARY KEY,
    merchant_id  VARCHAR(50) NOT NULL REFERENCES merchants(id),
    amount       NUMERIC(18,2) NOT NULL,
    currency     VARCHAR(3) NOT NULL,
    type         VARCHAR(50) NOT NULL,
    status       VARCHAR(20) NOT NULL,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP,
    description  VARCHAR(255),
    external_id  VARCHAR(100),
    notification_url  VARCHAR(2048)
);

CREATE INDEX idx_transactions_merchant_date ON transactions(merchant_id, created_at);

CREATE TABLE webhooks (
    id               BIGSERIAL PRIMARY KEY,
    event_type       VARCHAR(50) NOT NULL,
    transaction_id   UUID NOT NULL,
    payload          JSONB,
    received_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    notification_url VARCHAR(2048)
);

CREATE INDEX idx_webhooks_entity ON webhooks(event_type, transaction_id);