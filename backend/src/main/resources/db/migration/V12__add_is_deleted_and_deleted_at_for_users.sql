ALTER TABLE pharmacy.users
    ADD COLUMN is_deleted BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE pharmacy.users
    ADD COLUMN deleted_at TIMESTAMP;