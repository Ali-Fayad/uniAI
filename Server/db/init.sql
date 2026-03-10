-- ─────────────────────────────────────────────────────────────
--  uniAI – database initialisation script
--  Runs automatically on first postgres container start-up
--  (placed in /docker-entrypoint-initdb.d via compose volume)
--
--  JPA / Hibernate will still manage DDL at runtime
--  (spring.jpa.hibernate.ddl-auto=update).
--  This script ensures the schema exists even before the app
--  starts, and can also seed initial data.
-- ─────────────────────────────────────────────────────────────

-- ── Extension ─────────────────────────────────────────────────
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ── users ──────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS users (
    id            BIGSERIAL    PRIMARY KEY,
    first_name    VARCHAR(100),
    last_name     VARCHAR(100),
    username      VARCHAR(50)  UNIQUE NOT NULL,
    email         VARCHAR(100) UNIQUE NOT NULL,
    password      VARCHAR(100) NOT NULL,
    is_verified   BOOLEAN      NOT NULL DEFAULT FALSE,
    is_two_fac_auth BOOLEAN    NOT NULL DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_users_email    ON users (email);
CREATE INDEX IF NOT EXISTS idx_users_username ON users (username);

-- ── chats ──────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS chats (
    id         BIGSERIAL    PRIMARY KEY,
    user_id    BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title      VARCHAR(255),                         -- NULL until first message
    created_at TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_chats_user_id ON chats (user_id);

-- ── messages ───────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS messages (
    id        BIGSERIAL  PRIMARY KEY,
    chat_id   BIGINT     NOT NULL REFERENCES chats(id) ON DELETE CASCADE,
    sender_id BIGINT     NOT NULL,   -- 0 = AI assistant, >0 = users.id
    content   TEXT       NOT NULL,
    timestamp TIMESTAMP  NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_messages_chat_id ON messages (chat_id);

-- ── feedbacks ──────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS feedbacks (
    id      BIGSERIAL    PRIMARY KEY,
    email   VARCHAR(255) NOT NULL,
    comment TEXT         NOT NULL
);

-- ── verify_codes ───────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS verify_codes (
    id              BIGSERIAL   PRIMARY KEY,
    email           VARCHAR(255),
    code            VARCHAR(20),
    type            VARCHAR(30) CHECK (type IN ('VERIFY','TWO_FACT_AUTH','PASSWORD_RESET')),
    expiration_time TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_verify_codes_email ON verify_codes (email);
