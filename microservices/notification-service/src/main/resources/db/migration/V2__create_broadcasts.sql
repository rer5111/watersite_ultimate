CREATE TABLE IF NOT EXISTS broadcasts (
    id BIGINT PRIMARY KEY,
    subject VARCHAR(120) NOT NULL,
    category VARCHAR(16) NOT NULL,
    audience VARCHAR(32) NOT NULL,
    message VARCHAR(2000) NOT NULL,
    sent_at TIMESTAMP NOT NULL,
    recipients INT NOT NULL DEFAULT 0,
    status VARCHAR(16) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_broadcasts_sent_at ON broadcasts (sent_at);
