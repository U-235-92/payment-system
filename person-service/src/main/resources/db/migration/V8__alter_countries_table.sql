ALTER TABLE person.countries RENAME COLUMN created TO created_at;
ALTER TABLE person.countries RENAME COLUMN updated TO updated_at;
ALTER TABLE person.countries ALTER COLUMN created_at TYPE TIMESTAMP WITH TIME ZONE USING to_timestamp(created_at / 1000.0);
ALTER TABLE person.countries ALTER COLUMN created_at SET NOT NULL;
ALTER TABLE person.countries ALTER COLUMN updated_at TYPE TIMESTAMP WITH TIME ZONE USING to_timestamp(updated_at / 1000.0);
ALTER TABLE person.countries ALTER COLUMN updated_at SET NOT NULL;
ALTER TABLE person.countries DROP COLUMN id;
ALTER TABLE person.countries ADD CONSTRAINT countries_pkey PRIMARY KEY (code);

DROP SEQUENCE IF EXISTS person.countries_id_seq;

ALTER TABLE person.countries_aud RENAME COLUMN created TO created_at;
ALTER TABLE person.countries_aud RENAME COLUMN updated TO updated_at;
ALTER TABLE person.countries_aud ALTER COLUMN created_at TYPE TIMESTAMP WITH TIME ZONE USING to_timestamp(created_at / 1000.0);
ALTER TABLE person.countries_aud ALTER COLUMN updated_at TYPE TIMESTAMP WITH TIME ZONE USING to_timestamp(updated_at / 1000.0);
ALTER TABLE person.countries_aud DROP CONSTRAINT IF EXISTS countries_aud_pkey;
ALTER TABLE person.countries_aud DROP COLUMN id;
ALTER TABLE person.countries_aud ADD CONSTRAINT countries_aud_pkey PRIMARY KEY (rev, code);