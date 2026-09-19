CREATE TABLE transaction_metadata (
    id BIGSERIAL PRIMARY KEY,
    trace_id VARCHAR(255) NOT NULL,
    retry_count INT NOT NULL DEFAULT 0
);

CREATE TABLE transactions (
    id VARCHAR(255) PRIMARY KEY,
    operation_type VARCHAR(50) NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(20) NOT NULL,
    timestamp BIGINT NOT NULL,
    processed BOOLEAN NOT NULL DEFAULT FALSE,
    metadata_id BIGINT NOT NULL,
    CONSTRAINT fk_transactions_metadata FOREIGN KEY (metadata_id) REFERENCES transaction_metadata(id)
);

CREATE TABLE transactions_properties (
    transaction_id VARCHAR(255) NOT NULL,
    property_key VARCHAR(255) NOT NULL,
    property_value TEXT,
    PRIMARY KEY (transaction_id, property_key),
    CONSTRAINT fk_transactions_properties_transaction FOREIGN KEY (transaction_id) REFERENCES transactions(id) ON DELETE CASCADE
);