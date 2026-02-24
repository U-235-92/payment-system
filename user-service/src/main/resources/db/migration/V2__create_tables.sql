CREATE TABLE person.countries (
    id SERIAL PRIMARY KEY,
    created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    name VARCHAR(32),
    alpha2 VARCHAR(2),
    alpha3 VARCHAR(3),
    status VARCHAR(32)
);

CREATE TABLE person.addresses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created TIMESTAMP NOT NULL,
    updated TIMESTAMP NOT NULL,
    country_id INTEGER REFERENCES person.countries(id),
    address VARCHAR(128),
    zip_code VARCHAR(32),
    archived TIMESTAMP NOT NULL,
    city VARCHAR(32),
    state VARCHAR(32)
);

CREATE TABLE person.users (
    id UUID PRIMARY KEY NOT NULL,
    secret_key VARCHAR(32),
    email VARCHAR(1024),
    created TIMESTAMP NOT NULL,
    updated TIMESTAMP NOT NULL,
    first_name VARCHAR(32),
    last_name VARCHAR(32),
    filled BOOLEAN,
    address_id UUID REFERENCES person.addresses(id)
);

CREATE TABLE person.individuals (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID UNIQUE REFERENCES person.users(id),
    passport_number VARCHAR(32),
    phone_number VARCHAR(32),
    verified_at TIMESTAMP NOT NULL,
    archived_at TIMESTAMP NOT NULL,
    status VARCHAR(32)
);