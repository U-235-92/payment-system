CREATE SEQUENCE revinfo_seq
    START WITH 1
    INCREMENT BY 50
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1
    NO CYCLE;

CREATE TABLE revinfo (
    rev BIGSERIAL NOT NULL PRIMARY KEY,
    revtstmp BIGINT
);

CREATE TABLE transaction_service_transaction_metadata_aud (
    id BIGINT NOT NULL,
    rev BIGINT NOT NULL REFERENCES revinfo(rev),
    revtype SMALLINT,
    trace_id VARCHAR(255),
    timestamp TIMESTAMP,
    CONSTRAINT pk_transaction_metadata_aud PRIMARY KEY (id, rev)
);

CREATE TABLE transaction_service_deposit_transactions_aud (
    id UUID NOT NULL,
    rev BIGINT NOT NULL REFERENCES revinfo(rev),
    revtype SMALLINT,
    wallet_id UUID,
    amount DECIMAL(19,2),
    currency_code VARCHAR(3),
    conversion_rate DECIMAL(19,2),
    status VARCHAR(50),
    notification_url VARCHAR(2048),
    description VARCHAR(2048),
    metadata_id BIGINT,
    CONSTRAINT pk_deposit_transactions_aud PRIMARY KEY (id, rev)
);

CREATE TABLE transaction_service_transfer_transactions_aud (
    id UUID NOT NULL,
    rev BIGINT NOT NULL REFERENCES revinfo(rev),
    revtype SMALLINT,
    sender_wallet_id UUID,
    recipient_wallet_id UUID,
    amount DECIMAL(19,2),
    currency_code VARCHAR(3),
    sender_conversion_rate DECIMAL(19,2),
    recipient_conversion_rate DECIMAL(19,2),
    status VARCHAR(50),
    notification_url VARCHAR(2048),
    description VARCHAR(2048),
    metadata_id BIGINT,
    CONSTRAINT pk_transfer_transactions_aud PRIMARY KEY (id, rev)
);

CREATE TABLE transaction_service_withdraw_transactions_aud (
    id UUID NOT NULL,
    rev BIGINT NOT NULL REFERENCES revinfo(rev),
    revtype SMALLINT,
    wallet_id UUID,
    amount DECIMAL(19,2),
    currency_code VARCHAR(3),
    conversion_rate DECIMAL(19,2),
    status VARCHAR(50),
    notification_url VARCHAR(2048),
    description VARCHAR(2048),
    metadata_id BIGINT,
    CONSTRAINT pk_withdraw_transactions_aud PRIMARY KEY (id, rev)
);