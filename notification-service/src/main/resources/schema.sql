CREATE TABLE IF NOT EXISTS notifications (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    actor_id UUID NOT NULL,
    type VARCHAR(100) NOT NULL,
    message TEXT,
    resource_id UUID,
    created_at TIMESTAMPTZ NOT NULL,
    is_read BOOLEAN DEFAULT FALSE
);
