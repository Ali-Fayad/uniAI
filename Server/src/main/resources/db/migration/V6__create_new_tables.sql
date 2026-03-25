-- New normalized schema based on db_schema.puml.

-- Harden users.email uniqueness as final protection.
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_schema = 'public' AND table_name = 'users'
    ) THEN
        IF NOT EXISTS (
            SELECT 1
            FROM pg_constraint
            WHERE conname = 'uq_users_email'
              AND conrelid = 'users'::regclass
        ) THEN
            ALTER TABLE users
                ADD CONSTRAINT uq_users_email UNIQUE (email);
        END IF;
    END IF;
END $$;

CREATE TABLE IF NOT EXISTS personal_info (
    user_id BIGINT PRIMARY KEY,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    phone VARCHAR(100),
    address VARCHAR(255),
    city VARCHAR(120),
    country VARCHAR(120),
    linkedin_url VARCHAR(255),
    github_url VARCHAR(255),
    portfolio_url VARCHAR(255),
    summary TEXT,
    job_title VARCHAR(255),
    company VARCHAR(255),
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    CONSTRAINT fk_personal_info_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uq_personal_info_user_id UNIQUE (user_id)
);

CREATE INDEX IF NOT EXISTS idx_personal_info_user_id ON personal_info(user_id);

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

CREATE TABLE IF NOT EXISTS university (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    name_ar VARCHAR(255),
    acronym VARCHAR(20),
    country VARCHAR(120),
    city VARCHAR(120),
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    campus_name VARCHAR(255),
    campus_type VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_university_name_campus UNIQUE (name, campus_name)
);

CREATE TABLE IF NOT EXISTS position (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_position_name UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS skill (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    category VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_skill_name UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS language (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(16) NOT NULL,
    native_name VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_language_name UNIQUE (name),
    CONSTRAINT uq_language_code UNIQUE (code)
);

CREATE TABLE IF NOT EXISTS education (
    id BIGSERIAL PRIMARY KEY,
    personal_info_id BIGINT NOT NULL,
    university_id BIGINT,
    degree VARCHAR(255),
    field_of_study VARCHAR(255),
    start_date DATE,
    end_date DATE,
    grade VARCHAR(100),
    description TEXT,
    CONSTRAINT fk_education_personal_info
        FOREIGN KEY (personal_info_id) REFERENCES personal_info(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_education_university
        FOREIGN KEY (university_id) REFERENCES university(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_education_personal_info_id ON education(personal_info_id);
CREATE INDEX IF NOT EXISTS idx_education_university_id ON education(university_id);

CREATE TABLE IF NOT EXISTS experience (
    id BIGSERIAL PRIMARY KEY,
    personal_info_id BIGINT NOT NULL,
    position_id BIGINT,
    company_name VARCHAR(255),
    location VARCHAR(255),
    start_date DATE,
    end_date DATE,
    currently_working BOOLEAN NOT NULL DEFAULT FALSE,
    description TEXT,
    CONSTRAINT fk_experience_personal_info
        FOREIGN KEY (personal_info_id) REFERENCES personal_info(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_experience_position
        FOREIGN KEY (position_id) REFERENCES position(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_experience_personal_info_id ON experience(personal_info_id);
CREATE INDEX IF NOT EXISTS idx_experience_position_id ON experience(position_id);

CREATE TABLE IF NOT EXISTS project (
    id BIGSERIAL PRIMARY KEY,
    personal_info_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    repository_url VARCHAR(255),
    live_url VARCHAR(255),
    start_date DATE,
    end_date DATE,
    CONSTRAINT fk_project_personal_info
        FOREIGN KEY (personal_info_id) REFERENCES personal_info(user_id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_project_personal_info_id ON project(personal_info_id);

CREATE TABLE IF NOT EXISTS certificate (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    personal_info_id BIGINT,
    name VARCHAR(255) NOT NULL,
    issuer VARCHAR(255),
    issue_date DATE,
    expiration_date DATE,
    credential_id VARCHAR(255),
    credential_url VARCHAR(255),
    CONSTRAINT fk_certificate_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_certificate_personal_info
        FOREIGN KEY (personal_info_id) REFERENCES personal_info(user_id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_certificate_user_id ON certificate(user_id);
CREATE INDEX IF NOT EXISTS idx_certificate_personal_info_id ON certificate(personal_info_id);

CREATE TABLE IF NOT EXISTS personal_info_skill (
    personal_info_id BIGINT NOT NULL,
    skill_id BIGINT NOT NULL,
    level VARCHAR(100),
    PRIMARY KEY (personal_info_id, skill_id),
    CONSTRAINT fk_personal_info_skill_personal_info
        FOREIGN KEY (personal_info_id) REFERENCES personal_info(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_personal_info_skill_skill
        FOREIGN KEY (skill_id) REFERENCES skill(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_personal_info_skill_personal_info_id ON personal_info_skill(personal_info_id);
CREATE INDEX IF NOT EXISTS idx_personal_info_skill_skill_id ON personal_info_skill(skill_id);

CREATE TABLE IF NOT EXISTS personal_info_language (
    personal_info_id BIGINT NOT NULL,
    language_id BIGINT NOT NULL,
    proficiency_level VARCHAR(100),
    PRIMARY KEY (personal_info_id, language_id),
    CONSTRAINT fk_personal_info_language_personal_info
        FOREIGN KEY (personal_info_id) REFERENCES personal_info(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_personal_info_language_language
        FOREIGN KEY (language_id) REFERENCES language(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_personal_info_language_personal_info_id ON personal_info_language(personal_info_id);
CREATE INDEX IF NOT EXISTS idx_personal_info_language_language_id ON personal_info_language(language_id);

CREATE TABLE IF NOT EXISTS project_skill (
    project_id BIGINT NOT NULL,
    skill_id BIGINT NOT NULL,
    PRIMARY KEY (project_id, skill_id),
    CONSTRAINT fk_project_skill_project
        FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE CASCADE,
    CONSTRAINT fk_project_skill_skill
        FOREIGN KEY (skill_id) REFERENCES skill(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_project_skill_project_id ON project_skill(project_id);
CREATE INDEX IF NOT EXISTS idx_project_skill_skill_id ON project_skill(skill_id);
