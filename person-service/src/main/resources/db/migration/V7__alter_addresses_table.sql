ALTER TABLE person.addresses RENAME COLUMN created TO created_at;
ALTER TABLE person.addresses RENAME COLUMN updated TO updated_at;
ALTER TABLE person.addresses ALTER COLUMN created_at TYPE TIMESTAMP WITH TIME ZONE USING to_timestamp(created_at / 1000.0);
ALTER TABLE person.addresses ALTER COLUMN created_at SET NOT NULL;
ALTER TABLE person.addresses ALTER COLUMN updated_at TYPE TIMESTAMP WITH TIME ZONE USING to_timestamp(updated_at / 1000.0);
ALTER TABLE person.addresses ALTER COLUMN updated_at SET NOT NULL;
ALTER TABLE person.addresses DROP CONSTRAINT IF EXISTS addresses_country_id_fkey;
ALTER TABLE person.addresses DROP COLUMN country_id;

ALTER TABLE person.addresses_aud RENAME COLUMN created TO created_at;
ALTER TABLE person.addresses_aud RENAME COLUMN updated TO updated_at;
ALTER TABLE person.addresses_aud ALTER COLUMN created_at TYPE TIMESTAMP WITH TIME ZONE USING to_timestamp(created_at / 1000.0);
ALTER TABLE person.addresses_aud ALTER COLUMN updated_at TYPE TIMESTAMP WITH TIME ZONE USING to_timestamp(updated_at / 1000.0);
ALTER TABLE person.addresses_aud DROP COLUMN country_id;