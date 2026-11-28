ALTER TABLE transactions
    ALTER COLUMN wallet_amount TYPE NUMERIC(18, 2);

ALTER TABLE transactions
    ALTER COLUMN target_wallet_amount TYPE NUMERIC(18, 2);

ALTER TABLE transactions
    ALTER COLUMN fee TYPE NUMERIC(18, 2);