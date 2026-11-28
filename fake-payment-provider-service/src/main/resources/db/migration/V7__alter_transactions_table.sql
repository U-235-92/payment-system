ALTER TABLE transactions
    ADD CONSTRAINT transactions_merchant_uuid_fkey
    FOREIGN KEY (merchant_uuid)
    REFERENCES merchants (uuid);