ALTER TABLE person.individuals RENAME COLUMN created TO created_at;
ALTER TABLE person.individuals RENAME COLUMN updated TO updated_at;
ALTER TABLE person.individuals ALTER COLUMN created_at TYPE TIMESTAMP WITH TIME ZONE USING to_timestamp(created_at / 1000.0);
ALTER TABLE person.individuals ALTER COLUMN created_at SET NOT NULL;
ALTER TABLE person.individuals ALTER COLUMN updated_at TYPE TIMESTAMP WITH TIME ZONE USING to_timestamp(updated_at / 1000.0);
ALTER TABLE person.individuals ALTER COLUMN updated_at SET NOT NULL;

ALTER TABLE person.individuals_aud RENAME COLUMN created TO created_at;
ALTER TABLE person.individuals_aud RENAME COLUMN updated TO updated_at;
ALTER TABLE person.individuals_aud ALTER COLUMN created_at TYPE TIMESTAMP WITH TIME ZONE USING to_timestamp(created_at / 1000.0);
ALTER TABLE person.individuals_aud ALTER COLUMN updated_at TYPE TIMESTAMP WITH TIME ZONE USING to_timestamp(updated_at / 1000.0);