CREATE TABLE posts (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    content TEXT,
    media_url TEXT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE INDEX idx_posts_user_id ON posts(user_id);
CREATE INDEX idx_posts_created_at ON posts(created_at DESC);