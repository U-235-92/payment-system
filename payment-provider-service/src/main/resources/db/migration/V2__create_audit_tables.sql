CREATE SEQUENCE public.revinfo_seq
    START WITH 1
    INCREMENT BY 50
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1
    NO CYCLE;

CREATE TABLE public.revinfo (
    rev INTEGER PRIMARY KEY,
    revtstmp BIGINT
);

CREATE TABLE merchants_aud (
    rev         INTEGER NOT NULL REFERENCES public.revinfo(rev),
    revtype     SMALLINT,
    id          VARCHAR(50),
    secret_key  VARCHAR(255),
    name        VARCHAR(100),
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    role        VARCHAR(50),
    PRIMARY KEY (rev, id)
);

CREATE TABLE transactions_aud (
    id                UUID,
    rev               INTEGER NOT NULL REFERENCES public.revinfo(rev),
    revtype           SMALLINT,
    merchant_id       VARCHAR(50),
    amount            NUMERIC(18,2),
    currency          VARCHAR(3),
    operation         VARCHAR(50),
    status            VARCHAR(20),
    description       VARCHAR(2048),
    notification_url  VARCHAR(2048),
    metadata_id       BIGINT,
    PRIMARY KEY (rev, id)
);

CREATE TABLE transaction_metadata_aud (
    id        BIGINT,
    rev       INTEGER NOT NULL REFERENCES public.revinfo(rev),
    revtype   SMALLINT,
    timestamp TIMESTAMP,
    trace_id  VARCHAR(255),
    PRIMARY KEY (rev, id)
);