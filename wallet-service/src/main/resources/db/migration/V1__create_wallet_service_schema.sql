CREATE TABLE wallet_types (
    id UUID PRIMARY KEY DEFAULT uuidv4(),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    modified_at TIMESTAMP,
    currency_code VARCHAR(3) NOT NULL,
    user_type VARCHAR(15),
    creator VARCHAR(255),
    modifier VARCHAR(255)
);

CREATE TABLE wallets (
    id UUID PRIMARY KEY DEFAULT uuidv4(),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    modified_at TIMESTAMP,
    name VARCHAR(32) NOT NULL,
    wallet_type_id UUID NOT NULL REFERENCES wallet_types(id),
    user_id UUID NOT NULL,
    status VARCHAR(30) NOT NULL,
    balance DECIMAL NOT NULL DEFAULT 0.0,
    archived_at TIMESTAMP
);

