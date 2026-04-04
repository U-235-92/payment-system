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
    id SERIAL NOT NULL,
    rev INTEGER NOT NULL REFERENCES public.revinfo(rev),
    revtype SMALLINT,
    created BIGINT,
    updated BIGINT,
    name VARCHAR(64),
    code VARCHAR(3),
    PRIMARY KEY (rev, id)
);

CREATE TABLE person.addresses_aud (
    id UUID NOT NULL,
    rev INTEGER NOT NULL REFERENCES public.revinfo(rev),
    revtype SMALLINT,
    address VARCHAR(128),
    city VARCHAR(64),
    created BIGINT,
    updated BIGINT,
    state VARCHAR(64),
    zip_code VARCHAR(32),
    country_id INTEGER,
    PRIMARY KEY (rev, id)
);

CREATE TABLE person.individuals_aud (
    id UUID NOT NULL,
    rev INTEGER NOT NULL REFERENCES public.revinfo(rev),
    revtype SMALLINT,
    email VARCHAR(1024),
    created BIGINT,
    updated BIGINT,
    passport_number VARCHAR(32),
    phone_number VARCHAR(32),
    PRIMARY KEY (rev, id)
);

CREATE TABLE person.users_aud (
    id UUID NOT NULL,
    keycloak_id VARCHAR(36),
    rev INTEGER NOT NULL REFERENCES public.revinfo(rev),
    revtype SMALLINT,
    first_name VARCHAR(32),
    created BIGINT,
    updated BIGINT,
    active BOOLEAN,
    last_name VARCHAR(32),
    address_id UUID,
    individual_id UUID,
    PRIMARY KEY (rev, id)
);