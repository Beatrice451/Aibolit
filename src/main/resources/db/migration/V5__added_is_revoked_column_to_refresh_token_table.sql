ALTER TABLE pharmacy.refresh_tokens
    ADD COLUMN
        is_revoked BOOLEAN NOT NULL DEFAULT FALSE;

CREATE INDEX idx_refresh_tokens_user_id ON pharmacy.refresh_tokens (user_id);

ALTER TABLE pharmacy.refresh_tokens
    DROP COLUMN user_id;

ALTER TABLE pharmacy.refresh_tokens
    ADD COLUMN user_id INT NOT NULL REFERENCES pharmacy.users (id) ON DELETE CASCADE;

