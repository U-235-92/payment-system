CREATE TABLE transactions (
    id UUID PRIMARY KEY DEFAULT uuidv4(),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    modified_at TIMESTAMP,
    user_id UUID NOT NULL,
    wallet_id UUID NOT NULL,
    target_wallet_id UUID,
    amount DECIMAL NOT NULL DEFAULT 0.0,
    currency_code VARCHAR(3) NOT NULL,
    type VARCHAR(50) NOT NULL,
    status VARCHAR(32) NOT NULL,
    description VARCHAR(256),
    fee DECIMAL
);