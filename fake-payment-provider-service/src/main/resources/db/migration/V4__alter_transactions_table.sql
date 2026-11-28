ALTER TABLE transactions ADD COLUMN uuid UUID DEFAULT uuidv4();

UPDATE transactions SET uuid = gen_random_uuid();

ALTER TABLE transactions DROP CONSTRAINT IF EXISTS transactions_pkey;

ALTER TABLE transactions DROP COLUMN id;

ALTER TABLE transactions ADD CONSTRAINT transactions_pkey PRIMARY KEY (uuid);

ALTER TABLE transactions ADD COLUMN merchant_uuid UUID;

ALTER TABLE transactions DROP CONSTRAINT IF EXISTS transactions_merchant_id_fkey;

ALTER TABLE transactions DROP COLUMN merchant_id;