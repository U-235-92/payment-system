ALTER TABLE payouts
    ADD CONSTRAINT payouts_merchant_uuid_fkey
    FOREIGN KEY (merchant_uuid)
    REFERENCES merchants (uuid);