CREATE TABLE person.countries (
    id SERIAL PRIMARY KEY,
    created BIGINT NOT NULL,
    updated BIGINT NOT NULL,
    name VARCHAR(64),
    code VARCHAR(3)
);

CREATE TABLE person.addresses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created BIGINT NOT NULL,
    updated BIGINT NOT NULL,
    country_id INTEGER REFERENCES person.countries(id),
    state VARCHAR(64),
    city VARCHAR(64),
    address VARCHAR(128),
    zip_code VARCHAR(32)
);

CREATE TABLE person.individuals (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(1024),
    passport_number VARCHAR(32),
    phone_number VARCHAR(32),
    created BIGINT NOT NULL,
    updated BIGINT NOT NULL
);

CREATE TABLE person.users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    keycloak_id VARCHAR(36) REFERENCES public.user_entity(id) UNIQUE NOT NULL,
    first_name VARCHAR(32),
    last_name VARCHAR(32),
    created BIGINT NOT NULL,
    updated BIGINT NOT NULL,
    active BOOLEAN DEFAULT TRUE,
    address_id UUID REFERENCES person.addresses(id),
    individual_id UUID REFERENCES person.individuals(id)
);