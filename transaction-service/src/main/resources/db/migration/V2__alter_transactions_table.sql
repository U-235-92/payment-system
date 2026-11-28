ALTER TABLE transactions
    DROP CONSTRAINT IF EXISTS transactions_target_wallet_amount_not_null;