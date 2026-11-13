ALTER TABLE broadcasts ADD COLUMN IF NOT EXISTS recipient_user_id BIGINT;
CREATE INDEX IF NOT EXISTS idx_broadcasts_recipient_user ON broadcasts (recipient_user_id);
