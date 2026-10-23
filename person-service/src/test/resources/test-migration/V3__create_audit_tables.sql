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

CREATE TABLE person.countries_aud (
    code VARCHAR(3) NOT NULL,
    rev INTEGER NOT NULL REFERENCES public.revinfo(rev),
    revtype SMALLINT,
    name VARCHAR(64),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    PRIMARY KEY (rev, code)
);

CREATE TABLE person.addresses_aud (
    id UUID NOT NULL,
    rev INTEGER NOT NULL REFERENCES public.revinfo(rev),
    revtype SMALLINT,
    address VARCHAR(128),
    city VARCHAR(64),
    state VARCHAR(64),
    zip_code VARCHAR(32),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    PRIMARY KEY (rev, id)
);

CREATE TABLE person.individuals_aud (
    id UUID NOT NULL,
    rev INTEGER NOT NULL REFERENCES public.revinfo(rev),
    revtype SMALLINT,
    email VARCHAR(1024),
    passport_number VARCHAR(32),
    phone_number VARCHAR(32),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    PRIMARY KEY (rev, id)
);

CREATE TABLE person.users_aud (
    id UUID NOT NULL,
    rev INTEGER NOT NULL REFERENCES public.revinfo(rev),
    revtype SMALLINT,
    keycloak_id VARCHAR(36),
    country_code VARCHAR(3),
    address_id UUID,
    individual_id UUID,
    first_name VARCHAR(32),
    last_name VARCHAR(32),
    active BOOLEAN,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    PRIMARY KEY (rev, id)
);