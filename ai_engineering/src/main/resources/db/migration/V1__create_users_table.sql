-- ============================================================
-- V1 — Initial schema: Users table
-- ============================================================
-- Managed by Flyway. Do NOT modify this migration after it has
-- been applied to any environment. Create a new migration instead.
-- ============================================================

CREATE TABLE IF NOT EXISTS "Users" (
    id            BIGSERIAL    PRIMARY KEY,
    user_name     VARCHAR(100) NOT NULL UNIQUE,
    email         VARCHAR(255) NOT NULL UNIQUE,
    phone_number  VARCHAR(30)  UNIQUE,
    password      VARCHAR(255) NOT NULL,
    role          VARCHAR(50)  NOT NULL DEFAULT 'USER'
);

-- Index on email for fast authentication lookups
CREATE INDEX IF NOT EXISTS idx_users_email ON "Users" (email);
