CREATE TABLE refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    userid UUID NOT NULL REFERENCES users(userid) ON DELETE CASCADE,
    token UUID NOT NULL UNIQUE,
    expiry_time TIMESTAMP NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT now()

);