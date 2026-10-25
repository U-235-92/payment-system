CREATE TABLE person.countries (
    code VARCHAR(3) PRIMARY KEY,
    name VARCHAR(64),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE person.addresses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    state VARCHAR(64),
    city VARCHAR(64),
    address VARCHAR(128),
    zip_code VARCHAR(32),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE person.individuals (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(1024),
    passport_number VARCHAR(32),
    phone_number VARCHAR(32),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE person.users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    keycloak_id VARCHAR(36) REFERENCES public.user_entity(id) UNIQUE NOT NULL,
    country_code VARCHAR(3) REFERENCES person.countries(code),
    address_id UUID REFERENCES person.addresses(id),
    individual_id UUID REFERENCES person.individuals(id),
    first_name VARCHAR(32),
    last_name VARCHAR(32),
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);