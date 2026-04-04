CREATE TABLE service.service_operations (
    id SERIAL PRIMARY KEY,
    timestamp BIGINT NOT NULL,
    operation VARCHAR(255) NOT NULL,
    status VARCHAR(255) NOT NULL,
    description VARCHAR(2048)
);