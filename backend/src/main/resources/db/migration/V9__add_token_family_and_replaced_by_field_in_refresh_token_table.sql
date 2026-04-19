ALTER TABLE pharmacy.refresh_tokens
    ADD COLUMN token_family  UUID    NOT NULL DEFAULT gen_random_uuid(),
    ADD COLUMN replaced_by   INT REFERENCES pharmacy.refresh_tokens (id),
    ADD COLUMN is_current    BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN revoke_reason TEXT;

CREATE INDEX idx_rt_family_active ON pharmacy.refresh_tokens (token_family, is_revoked) WHERE is_revoked = FALSE;
CREATE INDEX idx_rt_user_families ON pharmacy.refresh_tokens (user_id, token_family DESC);