ALTER TABLE merchants ADD COLUMN uuid UUID DEFAULT uuidv4();

UPDATE merchants SET uuid = gen_random_uuid();

ALTER TABLE merchants DROP CONSTRAINT IF EXISTS merchants_pkey;

ALTER TABLE merchants DROP COLUMN id;

ALTER TABLE merchants ADD CONSTRAINT merchants_pkey PRIMARY KEY (uuid);