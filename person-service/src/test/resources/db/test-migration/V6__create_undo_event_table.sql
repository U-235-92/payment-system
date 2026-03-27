CREATE TABLE service.undo_operations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    person_keycloak_id UUID NOT NULL,
    operation VARCHAR(255) NOT NULL,
    timestamp BIGINT NOT NULL,
    description VARCHAR(2048)
);