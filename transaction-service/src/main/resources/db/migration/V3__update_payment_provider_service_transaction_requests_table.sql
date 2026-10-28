ALTER TABLE payment_provider_service_transaction_requests
    DROP CONSTRAINT IF EXISTS payment_provider_service_transaction_requests_pkey;

ALTER TABLE payment_provider_service_transaction_requests
    ADD COLUMN id BIGSERIAL;

ALTER TABLE payment_provider_service_transaction_requests
    ALTER COLUMN id SET NOT NULL;

ALTER TABLE payment_provider_service_transaction_requests
    ADD CONSTRAINT payment_provider_service_transaction_requests_pkey
    PRIMARY KEY (id);

ALTER TABLE payment_provider_service_transaction_requests
    ADD COLUMN status VARCHAR(50);

ALTER TABLE payment_provider_service_transaction_requests
    ALTER COLUMN status SET NOT NULL;