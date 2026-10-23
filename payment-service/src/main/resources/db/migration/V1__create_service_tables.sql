CREATE TABLE payment_providers (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    description VARCHAR(255)
);

CREATE TABLE payment_method_definitions (
    id BIGSERIAL PRIMARY KEY,
    is_all_currencies BOOLEAN DEFAULT FALSE,
    is_all_countries BOOLEAN DEFAULT FALSE,
    is_priority BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE
);

CREATE TABLE payment_method_required_fields (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    country_alpha3_code VARCHAR(3),
    currency_code VARCHAR(3),
    name VARCHAR(128) NOT NULL,
    data_type VARCHAR(128) NOT NULL,
    validation_type VARCHAR(128),
    validation_rule VARCHAR(256),
    default_value VARCHAR(128),
    values_options TEXT,
    description VARCHAR(255),
    placeholder VARCHAR(255),
    representation_name VARCHAR(255),
    language VARCHAR(2),
    image_url VARCHAR(2048),
    is_active BOOLEAN DEFAULT TRUE,
    UNIQUE(country_alpha3_code, currency_code, name, language)
);

CREATE TABLE payment_methods (
    id BIGSERIAL PRIMARY KEY,
    provider_id BIGINT REFERENCES payment_providers(id),
    payment_method_definitions_id BIGINT REFERENCES payment_method_definitions(id),
    payment_method_required_fields_id BIGINT REFERENCES payment_method_required_fields(id),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    type VARCHAR(64) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    logo TEXT,
    profile_type VARCHAR(24) DEFAULT 'INDIVIDUAL' NOT NULL
);

CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    payment_id UUID UNIQUE,
    transaction_id UUID NOT NULL UNIQUE,
    payment_method_id BIGINT REFERENCES payment_methods(id),
    amount NUMERIC(18, 2) NOT NULL,
    currency_code VARCHAR(3) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);