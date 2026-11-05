CREATE TABLE IF NOT EXISTS notification_reads (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    broadcast_id BIGINT NOT NULL,
    read_at TIMESTAMP NOT NULL,
    CONSTRAINT uq_notification_read UNIQUE (user_id, broadcast_id),
    CONSTRAINT fk_notification_read_broadcast FOREIGN KEY (broadcast_id) REFERENCES broadcasts (id)
);

CREATE INDEX IF NOT EXISTS idx_notification_reads_user ON notification_reads (user_id);
