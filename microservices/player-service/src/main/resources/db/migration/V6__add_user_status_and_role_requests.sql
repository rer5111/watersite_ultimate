ALTER TABLE IF EXISTS users
    ADD COLUMN IF NOT EXISTS status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE';

CREATE TABLE IF NOT EXISTS role_requests (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    requested_role VARCHAR(32) NOT NULL,
    note VARCHAR(2000) NOT NULL,
    status VARCHAR(16) NOT NULL,
    submitted_at TIMESTAMP NOT NULL,
    reviewed_at TIMESTAMP,
    rejection_reason VARCHAR(1000)
);

CREATE INDEX IF NOT EXISTS idx_role_requests_status ON role_requests (status);
