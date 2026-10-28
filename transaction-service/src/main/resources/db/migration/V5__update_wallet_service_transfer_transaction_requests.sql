ALTER TABLE wallet_service_transfer_transaction_requests
    DROP CONSTRAINT IF EXISTS wallet_service_transfer_transaction_requests_pkey;

ALTER TABLE wallet_service_transfer_transaction_requests
    ADD COLUMN id BIGSERIAL;

ALTER TABLE wallet_service_transfer_transaction_requests
    ALTER COLUMN id SET NOT NULL;

ALTER TABLE wallet_service_transfer_transaction_requests
    ADD CONSTRAINT wallet_service_transfer_transaction_requests_pkey
    PRIMARY KEY (id);

ALTER TABLE wallet_service_transfer_transaction_requests
    ADD COLUMN status VARCHAR(50);

ALTER TABLE wallet_service_transfer_transaction_requests
    ALTER COLUMN status SET NOT NULL;