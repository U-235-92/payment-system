CREATE TABLE wallet_operations (
    id UUID PRIMARY KEY,
    wallet_id UUID NOT NULL REFERENCES wallets(id),
    target_wallet_id UUID REFERENCES wallets(id), -- for transfer only
    created_at TIMESTAMP NOT NULL,
    operation_type VARCHAR(255) NOT NULL
);