-- Clean baseline schema migration.
-- This migration defines the full target schema in one linear step.

CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    username VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL,
    password VARCHAR(100) NOT NULL,
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    is_two_fac_auth BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT users_username_key UNIQUE (username),
    CONSTRAINT users_email_key UNIQUE (email)
);

CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);

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

CREATE TABLE IF NOT EXISTS language (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(16) NOT NULL,
    native_name VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_language_name UNIQUE (name),
    CONSTRAINT uq_language_code UNIQUE (code)
);

CREATE TABLE IF NOT EXISTS skill (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    category VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_skill_name UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS position (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_position_name UNIQUE (name)
);

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
    education_json TEXT,
    skills_json TEXT,
    languages_json TEXT,
    experience_json TEXT,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    CONSTRAINT fk_personal_info_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_personal_info_user_id ON personal_info(user_id);

-- Legacy CV aggregate tables (still used by existing CV flows).
CREATE TABLE IF NOT EXISTS cvs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    cv_name VARCHAR(255) NOT NULL,
    template VARCHAR(255),
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    CONSTRAINT fk_cvs_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_cvs_user_id ON cvs(user_id);

CREATE TABLE IF NOT EXISTS educations (
    id BIGSERIAL PRIMARY KEY,
    cv_id BIGINT NOT NULL,
    university_id BIGINT,
    degree VARCHAR(255) NOT NULL,
    field_of_study VARCHAR(255) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE,
    grade VARCHAR(100),
    description TEXT,
    CONSTRAINT fk_education_cv FOREIGN KEY (cv_id) REFERENCES cvs(id) ON DELETE CASCADE,
    CONSTRAINT fk_education_university FOREIGN KEY (university_id) REFERENCES university(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_education_cv_id ON educations(cv_id);
CREATE INDEX IF NOT EXISTS idx_education_university_id ON educations(university_id);

CREATE TABLE IF NOT EXISTS experiences (
    id BIGSERIAL PRIMARY KEY,
    cv_id BIGINT NOT NULL,
    position VARCHAR(255) NOT NULL,
    company VARCHAR(255) NOT NULL,
    location VARCHAR(255),
    start_date DATE NOT NULL,
    end_date DATE,
    is_current BOOLEAN NOT NULL DEFAULT FALSE,
    description TEXT,
    CONSTRAINT fk_experience_cv FOREIGN KEY (cv_id) REFERENCES cvs(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_experience_cv_id ON experiences(cv_id);

CREATE TABLE IF NOT EXISTS experience_achievements (
    id BIGSERIAL PRIMARY KEY,
    experience_id BIGINT NOT NULL,
    achievement TEXT NOT NULL,
    CONSTRAINT fk_experience_achievement_exp FOREIGN KEY (experience_id) REFERENCES experiences(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS projects (
    id BIGSERIAL PRIMARY KEY,
    cv_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    github_url VARCHAR(255),
    live_url VARCHAR(255),
    start_date DATE,
    end_date DATE,
    CONSTRAINT fk_project_cv FOREIGN KEY (cv_id) REFERENCES cvs(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_project_cv_id ON projects(cv_id);

CREATE TABLE IF NOT EXISTS project_technologies (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL,
    technology VARCHAR(255) NOT NULL,
    CONSTRAINT fk_project_technology_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS skills (
    id BIGSERIAL PRIMARY KEY,
    cv_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    level VARCHAR(100),
    skill_order INTEGER,
    CONSTRAINT fk_skills_cv FOREIGN KEY (cv_id) REFERENCES cvs(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_skill_cv_id ON skills(cv_id);

CREATE TABLE IF NOT EXISTS languages (
    id BIGSERIAL PRIMARY KEY,
    cv_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    proficiency VARCHAR(255),
    CONSTRAINT fk_languages_cv FOREIGN KEY (cv_id) REFERENCES cvs(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_language_cv_id ON languages(cv_id);

CREATE TABLE IF NOT EXISTS certificates (
    id BIGSERIAL PRIMARY KEY,
    cv_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    issuer VARCHAR(255),
    issued_date DATE,
    credential_url VARCHAR(255),
    CONSTRAINT fk_certificates_cv FOREIGN KEY (cv_id) REFERENCES cvs(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_certificate_cv_id ON certificates(cv_id);

-- Junction/link tables used by newer catalog-driven flows.
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
        FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    CONSTRAINT fk_project_skill_skill
        FOREIGN KEY (skill_id) REFERENCES skill(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_project_skill_project_id ON project_skill(project_id);
CREATE INDEX IF NOT EXISTS idx_project_skill_skill_id ON project_skill(skill_id);

-- Requested generic skill-entry relation.
CREATE TABLE IF NOT EXISTS skill_entry (
    cv_id BIGINT NOT NULL,
    skill_id BIGINT NOT NULL,
    PRIMARY KEY (cv_id, skill_id),
    CONSTRAINT fk_skill_entry_cv FOREIGN KEY (cv_id) REFERENCES cvs(id) ON DELETE CASCADE,
    CONSTRAINT fk_skill_entry_skill FOREIGN KEY (skill_id) REFERENCES skill(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_skill_entry_cv_id ON skill_entry(cv_id);
CREATE INDEX IF NOT EXISTS idx_skill_entry_skill_id ON skill_entry(skill_id);
