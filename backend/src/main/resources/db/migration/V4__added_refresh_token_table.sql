CREATE TABLE pharmacy.refresh_tokens
(
    id         INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    token      TEXT      NOT NULL UNIQUE,
    user_id    INT       NOT NULL REFERENCES pharmacy.users (id),
    expiry_date TIMESTAMP NOT NULL
);
