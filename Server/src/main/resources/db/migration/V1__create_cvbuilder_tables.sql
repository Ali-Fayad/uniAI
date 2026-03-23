-- CV Builder schema

CREATE TABLE IF NOT EXISTS universities (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    name_ar VARCHAR(255),
    acronym VARCHAR(20),
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    campus_name VARCHAR(255),
    campus_type VARCHAR(50),
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS personal_info (
    user_id BIGINT PRIMARY KEY,
    phone VARCHAR(100),
    address VARCHAR(255),
    linkedin VARCHAR(255),
    github VARCHAR(255),
    portfolio VARCHAR(255),
    summary TEXT,
    job_title VARCHAR(255),
    company VARCHAR(255),
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS cvs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    cv_name VARCHAR(255) NOT NULL,
    template VARCHAR(255),
    is_default BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS educations (
    id BIGSERIAL PRIMARY KEY,
    cv_id BIGINT NOT NULL REFERENCES cvs(id) ON DELETE CASCADE,
    university_id BIGINT REFERENCES universities(id),
    degree VARCHAR(255) NOT NULL,
    field_of_study VARCHAR(255) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE,
    grade VARCHAR(50),
    description TEXT
);

CREATE TABLE IF NOT EXISTS experiences (
    id BIGSERIAL PRIMARY KEY,
    cv_id BIGINT NOT NULL REFERENCES cvs(id) ON DELETE CASCADE,
    position VARCHAR(255) NOT NULL,
    company VARCHAR(255) NOT NULL,
    location VARCHAR(255),
    start_date DATE NOT NULL,
    end_date DATE,
    is_current BOOLEAN DEFAULT FALSE,
    description TEXT
);

CREATE TABLE IF NOT EXISTS experience_achievements (
    id BIGSERIAL PRIMARY KEY,
    experience_id BIGINT NOT NULL REFERENCES experiences(id) ON DELETE CASCADE,
    achievement TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS skills (
    id BIGSERIAL PRIMARY KEY,
    cv_id BIGINT NOT NULL REFERENCES cvs(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    level VARCHAR(100),
    skill_order INTEGER
);

CREATE TABLE IF NOT EXISTS projects (
    id BIGSERIAL PRIMARY KEY,
    cv_id BIGINT NOT NULL REFERENCES cvs(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    github_url VARCHAR(255),
    live_url VARCHAR(255),
    start_date DATE,
    end_date DATE
);

CREATE TABLE IF NOT EXISTS project_technologies (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    technology VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS languages (
    id BIGSERIAL PRIMARY KEY,
    cv_id BIGINT NOT NULL REFERENCES cvs(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    proficiency VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS certificates (
    id BIGSERIAL PRIMARY KEY,
    cv_id BIGINT NOT NULL REFERENCES cvs(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    issuer VARCHAR(255),
    issued_date DATE,
    credential_url VARCHAR(255)
);

CREATE INDEX IF NOT EXISTS idx_cvs_user_id ON cvs(user_id);
CREATE INDEX IF NOT EXISTS idx_education_cv_id ON educations(cv_id);
CREATE INDEX IF NOT EXISTS idx_experience_cv_id ON experiences(cv_id);
CREATE INDEX IF NOT EXISTS idx_skill_cv_id ON skills(cv_id);
CREATE INDEX IF NOT EXISTS idx_project_cv_id ON projects(cv_id);
CREATE INDEX IF NOT EXISTS idx_language_cv_id ON languages(cv_id);
CREATE INDEX IF NOT EXISTS idx_certificate_cv_id ON certificates(cv_id);
