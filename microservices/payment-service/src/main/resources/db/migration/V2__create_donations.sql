CREATE TABLE donations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id BIGINT NOT NULL,
    payer_username VARCHAR(120) NOT NULL,
    payment_reference VARCHAR(64) UNIQUE,
    amount_rub NUMERIC(10, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'RUB',
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    message TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT ck_donations_amount_min CHECK (amount_rub >= 10.00),
    CONSTRAINT ck_donations_status_valid CHECK (status IN ('PENDING', 'SUCCEEDED', 'CANCELED'))
);

CREATE INDEX ix_donations_user_id ON donations (user_id);
CREATE INDEX ix_donations_status ON donations (status);
CREATE INDEX ix_donations_created_at ON donations (created_at DESC);
CREATE INDEX ix_donations_payment_reference ON donations (payment_reference);
