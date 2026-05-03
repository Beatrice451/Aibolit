ALTER TABLE pharmacy.users
    ADD COLUMN email_verified    BOOLEAN   NOT NULL DEFAULT FALSE,
    ADD COLUMN email_verified_at TIMESTAMP NULL;

CREATE TABLE pharmacy.email_verification_tokens
(
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY, -- Почему везде INT, а тут BIGINT? Потому что я идиот и так надо было делать с самого начала
    user_id     INT          NOT NULL REFERENCES pharmacy.users (id) ON DELETE CASCADE,
    token       VARCHAR(255) NOT NULL UNIQUE,
    expires_at  TIMESTAMP    NOT NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    verified_at TIMESTAMP    NULL,
    is_used     BOOLEAN      NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_verification_tokens_token ON pharmacy.email_verification_tokens (token) WHERE is_used = FALSE;

CREATE INDEX idx_verification_tokens_expires_at ON pharmacy.email_verification_tokens (expires_at) WHERE is_used = FALSE;

CREATE INDEX idx_verification_tokens_user_id ON pharmacy.email_verification_tokens (user_id);

