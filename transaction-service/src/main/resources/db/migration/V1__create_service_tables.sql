CREATE TABLE transaction_service_transaction_metadata (
    id BIGSERIAL PRIMARY KEY NOT NULL,
    trace_id VARCHAR(255) NOT NULL,
    timestamp TIMESTAMP NOT NULL
);

CREATE TABLE transaction_service_deposit_transactions (
    id UUID PRIMARY KEY NOT NULL,
    wallet_id UUID NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    currency_code VARCHAR(3) NOT NULL,
    conversion_rate DECIMAL(19,2) NOT NULL,
    status VARCHAR(50) NOT NULL,
    notification_url VARCHAR(2048) NOT NULL,
    description VARCHAR(2048),
    metadata_id BIGINT NOT NULL,
    CONSTRAINT fk_deposit_transactions_metadata
        FOREIGN KEY (metadata_id)
        REFERENCES transaction_service_transaction_metadata(id)
);

CREATE TABLE transaction_service_transfer_transactions (
    id UUID PRIMARY KEY NOT NULL,
    sender_wallet_id UUID NOT NULL,
    recipient_wallet_id UUID NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    currency_code VARCHAR(3) NOT NULL,
    sender_conversion_rate DECIMAL(19,2) NOT NULL,
    recipient_conversion_rate DECIMAL(19,2) NOT NULL,
    status VARCHAR(50) NOT NULL,
    notification_url VARCHAR(2048) NOT NULL,
    description VARCHAR(2048),
    metadata_id BIGINT NOT NULL,
    CONSTRAINT fk_transfer_transactions_metadata
        FOREIGN KEY (metadata_id)
        REFERENCES transaction_service_transaction_metadata(id)
);

CREATE TABLE transaction_service_withdraw_transactions (
    id UUID PRIMARY KEY NOT NULL,
    wallet_id UUID NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    currency_code VARCHAR(3) NOT NULL,
    conversion_rate DECIMAL(19,2) NOT NULL,
    status VARCHAR(50) NOT NULL,
    notification_url VARCHAR(2048) NOT NULL,
    description VARCHAR(2048),
    metadata_id BIGINT NOT NULL,
    CONSTRAINT fk_withdraw_transactions_metadata
        FOREIGN KEY (metadata_id)
        REFERENCES transaction_service_transaction_metadata(id)
);

CREATE TABLE payment_provider_service_transaction_request_metadata (
    id BIGSERIAL NOT NULL PRIMARY KEY,
    trace_id VARCHAR(255) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    is_processed BOOLEAN NOT NULL
);

CREATE TABLE payment_provider_service_transaction_requests (
    transaction_id UUID PRIMARY KEY NOT NULL,
    merchant_id VARCHAR(255) NOT NULL,
    operation VARCHAR(255) NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    currency_code VARCHAR(3) NOT NULL,
    notification_url VARCHAR(2048) NOT NULL,
    description VARCHAR(2048),
    metadata_id BIGINT NOT NULL,
    CONSTRAINT fk_payment_provider_service_transaction_request_metadata
        FOREIGN KEY (metadata_id)
        REFERENCES payment_provider_service_transaction_request_metadata(id)
);

CREATE TABLE wallet_service_transaction_request_metadata (
    id BIGSERIAL NOT NULL PRIMARY KEY,
    trace_id VARCHAR(255) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    is_processed BOOLEAN NOT NULL
);

CREATE TABLE wallet_service_deposit_transaction_requests (
    transaction_id UUID PRIMARY KEY NOT NULL,
    wallet_id UUID NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    currency_code VARCHAR(3) NOT NULL,
    conversion_rate DECIMAL(19,2) NOT NULL,
    metadata_id BIGINT NOT NULL,
    CONSTRAINT fk_wallet_service_transaction_request_metadata
        FOREIGN KEY (metadata_id)
        REFERENCES wallet_service_transaction_request_metadata(id)
);

CREATE TABLE wallet_service_transfer_transaction_requests (
    transaction_id UUID PRIMARY KEY NOT NULL,
    sender_wallet_id UUID NOT NULL,
    recipient_wallet_id UUID NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    currency_code VARCHAR(3) NOT NULL,
    sender_conversion_rate DECIMAL(19,2) NOT NULL,
    recipient_conversion_rate DECIMAL(19,2) NOT NULL,
    metadata_id BIGINT NOT NULL,
    CONSTRAINT fk_wallet_service_transaction_request_metadata
        FOREIGN KEY (metadata_id)
        REFERENCES wallet_service_transaction_request_metadata(id)
);

CREATE TABLE wallet_service_withdraw_transaction_requests (
    transaction_id UUID PRIMARY KEY NOT NULL,
    wallet_id UUID NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    currency_code VARCHAR(3) NOT NULL,
    conversion_rate DECIMAL(19,2) NOT NULL,
    metadata_id BIGINT NOT NULL,
    CONSTRAINT fk_wallet_service_transaction_request_metadata
        FOREIGN KEY (metadata_id)
        REFERENCES wallet_service_transaction_request_metadata(id)
);