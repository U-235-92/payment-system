CREATE TABLE currencies (
    iso_code VARCHAR(3) NOT NULL,
    iso_numeric INT NOT NULL,
    created_at DATE NOT NULL,
    modified_at DATE,
    description VARCHAR(64) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    symbol VARCHAR(10) NOT NULL,
    CONSTRAINT pk_currencies PRIMARY KEY (iso_code)
);

CREATE TABLE rate_providers (
    provider_code VARCHAR(10) NOT NULL,
    created_at DATE NOT NULL,
    modified_at DATE,
    provider_name VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_rate_providers PRIMARY KEY (provider_code),
    CONSTRAINT uq_rate_providers_name UNIQUE (provider_name)
);

CREATE TABLE conversion_rates (
    id BIGSERIAL NOT NULL,
    source_iso_code VARCHAR(3) NOT NULL,
    destination_iso_code VARCHAR(3) NOT NULL,
    rate_date DATE NOT NULL,
    rate DOUBLE PRECISION NOT NULL,
    CONSTRAINT pk_conversion_rates PRIMARY KEY (id),
    CONSTRAINT fk_conversion_rates_source FOREIGN KEY (source_iso_code) REFERENCES currencies (iso_code) ON DELETE RESTRICT,
    CONSTRAINT fk_conversion_rates_destination FOREIGN KEY (destination_iso_code) REFERENCES currencies (iso_code) ON DELETE RESTRICT
);

CREATE TABLE conversion_providers_rates (
    conversion_rate_id BIGINT NOT NULL,
    provider_code VARCHAR(10) NOT NULL,
    provider_rate DOUBLE PRECISION NOT NULL,
    CONSTRAINT pk_conversion_providers_rates PRIMARY KEY (conversion_rate_id, provider_code),
    CONSTRAINT fk_provider_rates_conversion_rate FOREIGN KEY (conversion_rate_id) REFERENCES conversion_rates (id) ON DELETE CASCADE,
    CONSTRAINT fk_provider_rates_provider_code FOREIGN KEY (provider_code) REFERENCES rate_providers (provider_code) ON DELETE RESTRICT
);

CREATE TABLE shedlock (
    name varchar(64) not null primary key,
    lock_until timestamp not null,
    locked_at timestamp not null,
    locked_by varchar(255) not null
);

CREATE TABLE adjustment_factors (
    id BIGINT NOT NULL,
    provider_code VARCHAR(10) NOT NULL,
    factor DOUBLE PRECISION NOT NULL,
    created_at DATE NOT NULL,
    modified_at DATE,
    CONSTRAINT pk_adjustment_factors_id PRIMARY KEY (id),
    CONSTRAINT fk_adjustment_factors_provider_code FOREIGN KEY (provider_code) REFERENCES rate_providers (provider_code)
);

CREATE INDEX idx_conversion_rates_pair ON conversion_rates (source_iso_code, destination_iso_code);