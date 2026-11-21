CREATE TABLE payment_provider_callbacks (
    id UUID DEFAULT uuidv4() PRIMARY KEY,
    created_at TIMESTAMP DEFAULT now() NOT NULL,
    updated_at TIMESTAMP DEFAULT now() NOT NULL,
    body TEXT NOT NULL,
    transaction_id UUID NOT NULL UNIQUE,
    payment_id UUID NOT NULL UNIQUE,
    type VARCHAR(255),
    provider VARCHAR(255),
    target_webhook_status VARCHAR(25) NOT NULL,
    processed BOOLEAN
);

CREATE TABLE verification_callbacks (
    id UUID DEFAULT uuidv4() PRIMARY KEY,
    created_at TIMESTAMP DEFAULT now() NOT NULL,
    updated_at TIMESTAMP DEFAULT now() NOT NULL,
    body TEXT NOT NULL,
    transaction_id UUID,
    profile_id UUID NOT NULL,
    status VARCHAR(25),
    type VARCHAR(255),
    processed BOOLEAN
);

CREATE TABLE unknown_callbacks (
    id UUID DEFAULT uuidv4() PRIMARY KEY,
    created_at TIMESTAMP DEFAULT now() NOT NULL,
    updated_at TIMESTAMP DEFAULT now() NOT NULL,
    body TEXT NOT NULL,
    processed BOOLEAN
);