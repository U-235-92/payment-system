ALTER TABLE payouts ADD COLUMN uuid UUID DEFAULT uuidv4();

UPDATE payouts SET uuid = gen_random_uuid();

ALTER TABLE payouts DROP CONSTRAINT IF EXISTS payouts_pkey;

ALTER TABLE payouts DROP COLUMN id;

ALTER TABLE payouts ADD CONSTRAINT payouts_pkey PRIMARY KEY (uuid);

ALTER TABLE payouts ADD COLUMN merchant_uuid UUID;

ALTER TABLE payouts DROP CONSTRAINT IF EXISTS payouts_merchant_id_fkey;

ALTER TABLE payouts DROP COLUMN merchant_id;