ALTER TABLE webhooks
    ADD COLUMN entity_uuid UUID DEFAULT uuidv4();

ALTER TABLE webhooks
    ALTER COLUMN entity_id DROP NOT NULL;

ALTER TABLE webhooks
    DROP COLUMN entity_id;