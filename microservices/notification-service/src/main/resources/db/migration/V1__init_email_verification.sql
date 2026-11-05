CREATE SEQUENCE IF NOT EXISTS notification_sequence START WITH 1 INCREMENT BY 1;

CREATE TABLE IF NOT EXISTS email_verification_codes (
    id BIGINT PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    code_hash VARCHAR(255) NOT NULL,
    purpose VARCHAR(32) NOT NULL,
    attempts INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    verified BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_email_purpose ON email_verification_codes (email, purpose);
