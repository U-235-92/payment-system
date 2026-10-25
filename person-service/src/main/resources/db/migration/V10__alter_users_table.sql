ALTER TABLE person.users RENAME COLUMN created TO created_at;
ALTER TABLE person.users RENAME COLUMN updated TO updated_at;
ALTER TABLE person.users ALTER COLUMN created_at TYPE TIMESTAMP WITH TIME ZONE USING to_timestamp(created_at / 1000.0);
ALTER TABLE person.users ALTER COLUMN created_at SET NOT NULL;
ALTER TABLE person.users ALTER COLUMN updated_at TYPE TIMESTAMP WITH TIME ZONE USING to_timestamp(updated_at / 1000.0);
ALTER TABLE person.users ALTER COLUMN updated_at SET NOT NULL;
ALTER TABLE person.users ADD COLUMN country_code VARCHAR(3);
ALTER TABLE person.users ALTER COLUMN country_code SET NOT NULL;
ALTER TABLE person.users ADD CONSTRAINT users_country_code_fkey FOREIGN KEY (country_code) REFERENCES person.countries(code);

ALTER TABLE person.users_aud RENAME COLUMN created TO created_at;
ALTER TABLE person.users_aud RENAME COLUMN updated TO updated_at;
ALTER TABLE person.users_aud ALTER COLUMN created_at TYPE TIMESTAMP WITH TIME ZONE USING to_timestamp(created_at / 1000.0);
ALTER TABLE person.users_aud ALTER COLUMN updated_at TYPE TIMESTAMP WITH TIME ZONE USING to_timestamp(updated_at / 1000.0);
ALTER TABLE person.users_aud ADD COLUMN country_code VARCHAR(3);