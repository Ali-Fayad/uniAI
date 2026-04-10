CREATE TABLE IF NOT EXISTS verification_code (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    code VARCHAR(32) NOT NULL,
    type VARCHAR(32) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_verification_code_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT ck_verification_code_type
        CHECK (type IN ('REGISTRATION', 'PASSWORD_RESET', 'EMAIL_CHANGE', 'TWO_FA'))
);

CREATE INDEX IF NOT EXISTS idx_verification_code_user_id ON verification_code(user_id);
CREATE INDEX IF NOT EXISTS idx_verification_code_type ON verification_code(type);
