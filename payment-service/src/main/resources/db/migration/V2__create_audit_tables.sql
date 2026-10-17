CREATE SEQUENCE revinfo_seq
    START WITH 1
    INCREMENT BY 50
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1
    NO CYCLE;

CREATE TABLE revinfo (
    rev INTEGER PRIMARY KEY,
    revtstmp BIGINT
);

CREATE TABLE payments_aud (
    revtype SMALLINT,
    rev INTEGER NOT NULL REFERENCES revinfo(rev),
    id BIGINT,
    payment_id UUID,
    transaction_id UUID,
    payment_method_id BIGINT,
    amount NUMERIC(18, 2),
    currency_code VARCHAR(3),
    status VARCHAR(20),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    PRIMARY KEY (rev, id)
);