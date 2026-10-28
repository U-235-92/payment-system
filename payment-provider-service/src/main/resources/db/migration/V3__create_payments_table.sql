CREATE TABLE payments (
    id UUID PRIMARY KEY,
    transaction_id UUID NOT NULL REFERENCES transactions(id) UNIQUE,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE payments_aud (
    id UUID,
    rev INTEGER NOT NULL REFERENCES public.revinfo(rev),
    revtype SMALLINT,
    transaction_id UUID,
    status VARCHAR(20),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    PRIMARY KEY (rev, id)
);