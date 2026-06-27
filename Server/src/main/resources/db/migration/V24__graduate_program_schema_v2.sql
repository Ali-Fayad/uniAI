-- Canonical graduate program schema for AUB and future universities.
-- Legacy graduate tables from V23 remain in place for compatibility.

ALTER TABLE degree_type
    ADD CONSTRAINT ck_degree_type_code_uppercase
        CHECK (code = UPPER(code));

CREATE TABLE IF NOT EXISTS university_faculty (
    id BIGSERIAL PRIMARY KEY,
    university_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    short_name VARCHAR(255),
    faculty_type VARCHAR(50) NOT NULL DEFAULT 'FACULTY',
    official_url TEXT,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_university_faculty_university
        FOREIGN KEY (university_id) REFERENCES university(id) ON DELETE CASCADE,
    CONSTRAINT ck_university_faculty_type
        CHECK (faculty_type IN ('FACULTY', 'SCHOOL', 'INTERFACULTY', 'INSTITUTE', 'CENTER', 'OTHER')),
    CONSTRAINT uq_university_faculty_name UNIQUE (university_id, name)
);

CREATE INDEX IF NOT EXISTS idx_university_faculty_university_id ON university_faculty(university_id);
CREATE INDEX IF NOT EXISTS idx_university_faculty_name ON university_faculty(name);

CREATE TABLE IF NOT EXISTS university_department (
    id BIGSERIAL PRIMARY KEY,
    university_id BIGINT NOT NULL,
    faculty_id BIGINT,
    name VARCHAR(255) NOT NULL,
    short_name VARCHAR(255),
    official_url TEXT,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_university_department_university
        FOREIGN KEY (university_id) REFERENCES university(id) ON DELETE CASCADE,
    CONSTRAINT fk_university_department_faculty
        FOREIGN KEY (faculty_id) REFERENCES university_faculty(id) ON DELETE SET NULL,
    CONSTRAINT uq_university_department_name UNIQUE (university_id, faculty_id, name)
);

CREATE INDEX IF NOT EXISTS idx_university_department_university_id ON university_department(university_id);
CREATE INDEX IF NOT EXISTS idx_university_department_faculty_id ON university_department(faculty_id);
CREATE INDEX IF NOT EXISTS idx_university_department_name ON university_department(name);

CREATE TABLE IF NOT EXISTS graduate_program (
    id BIGSERIAL PRIMARY KEY,
    university_id BIGINT NOT NULL,
    faculty_id BIGINT,
    department_id BIGINT,
    degree_type_id BIGINT,
    program_key VARCHAR(255) NOT NULL,
    major_category VARCHAR(255),
    major VARCHAR(255),
    official_degree_name VARCHAR(255),
    thesis_or_non_thesis VARCHAR(50),
    credits INTEGER,
    duration_value NUMERIC(10, 2),
    duration_unit VARCHAR(30),
    primary_language_id BIGINT,
    delivery_mode VARCHAR(30),
    program_description TEXT,
    official_program_url TEXT,
    source_id BIGINT NOT NULL,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_graduate_program_university
        FOREIGN KEY (university_id) REFERENCES university(id) ON DELETE CASCADE,
    CONSTRAINT fk_graduate_program_faculty
        FOREIGN KEY (faculty_id) REFERENCES university_faculty(id) ON DELETE SET NULL,
    CONSTRAINT fk_graduate_program_department
        FOREIGN KEY (department_id) REFERENCES university_department(id) ON DELETE SET NULL,
    CONSTRAINT fk_graduate_program_degree_type
        FOREIGN KEY (degree_type_id) REFERENCES degree_type(id) ON DELETE SET NULL,
    CONSTRAINT fk_graduate_program_language
        FOREIGN KEY (primary_language_id) REFERENCES language(id) ON DELETE SET NULL,
    CONSTRAINT fk_graduate_program_source
        FOREIGN KEY (source_id) REFERENCES source(id) ON DELETE RESTRICT,
    CONSTRAINT ck_graduate_program_thesis_or_non_thesis
        CHECK (thesis_or_non_thesis IS NULL OR thesis_or_non_thesis IN ('THESIS', 'NON_THESIS', 'THESIS_OR_NON_THESIS', 'PROJECT', 'THESIS_OR_PROJECT', 'UNKNOWN')),
    CONSTRAINT ck_graduate_program_delivery_mode
        CHECK (delivery_mode IS NULL OR delivery_mode IN ('ON_CAMPUS', 'ONLINE', 'HYBRID', 'BLENDED', 'MIXED', 'DISTANCE', 'UNKNOWN')),
    CONSTRAINT ck_graduate_program_duration_unit
        CHECK (duration_unit IS NULL OR duration_unit IN ('CREDITS', 'SEMESTERS', 'YEARS', 'MONTHS', 'WEEKS', 'DAYS', 'HOURS')),
    CONSTRAINT ck_graduate_program_credits_nonnegative
        CHECK (credits IS NULL OR credits >= 0),
    CONSTRAINT ck_graduate_program_duration_value_nonnegative
        CHECK (duration_value IS NULL OR duration_value >= 0),
    CONSTRAINT ck_graduate_program_program_key_not_blank
        CHECK (btrim(program_key) <> ''),
    CONSTRAINT uq_graduate_program_university_program_key UNIQUE (university_id, program_key),
    CONSTRAINT uq_graduate_program_university_url UNIQUE (university_id, official_program_url)
);

CREATE INDEX IF NOT EXISTS idx_graduate_program_university_id ON graduate_program(university_id);
CREATE INDEX IF NOT EXISTS idx_graduate_program_faculty_id ON graduate_program(faculty_id);
CREATE INDEX IF NOT EXISTS idx_graduate_program_department_id ON graduate_program(department_id);
CREATE INDEX IF NOT EXISTS idx_graduate_program_degree_type_id ON graduate_program(degree_type_id);
CREATE INDEX IF NOT EXISTS idx_graduate_program_language_id ON graduate_program(primary_language_id);
CREATE INDEX IF NOT EXISTS idx_graduate_program_source_id ON graduate_program(source_id);
CREATE INDEX IF NOT EXISTS idx_graduate_program_key ON graduate_program(program_key);
CREATE INDEX IF NOT EXISTS idx_graduate_program_official_url ON graduate_program(official_program_url);

CREATE TABLE IF NOT EXISTS graduate_program_alias (
    id BIGSERIAL PRIMARY KEY,
    university_id BIGINT NOT NULL,
    program_id BIGINT NOT NULL,
    alias_type VARCHAR(50) NOT NULL DEFAULT 'LEGACY_ID',
    alias VARCHAR(255) NOT NULL,
    source_id BIGINT,
    note TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_graduate_program_alias_university
        FOREIGN KEY (university_id) REFERENCES university(id) ON DELETE CASCADE,
    CONSTRAINT fk_graduate_program_alias_program
        FOREIGN KEY (program_id) REFERENCES graduate_program(id) ON DELETE CASCADE,
    CONSTRAINT fk_graduate_program_alias_source
        FOREIGN KEY (source_id) REFERENCES source(id) ON DELETE SET NULL,
    CONSTRAINT ck_graduate_program_alias_type
        CHECK (alias_type IN ('LEGACY_ID', 'ALT_ID', 'SOURCE_ID', 'SLUG', 'DISPLAY_NAME', 'OLD_ID')),
    CONSTRAINT uq_graduate_program_alias UNIQUE (university_id, alias_type, alias)
);

CREATE INDEX IF NOT EXISTS idx_graduate_program_alias_university_id ON graduate_program_alias(university_id);
CREATE INDEX IF NOT EXISTS idx_graduate_program_alias_program_id ON graduate_program_alias(program_id);
CREATE INDEX IF NOT EXISTS idx_graduate_program_alias_source_id ON graduate_program_alias(source_id);
CREATE INDEX IF NOT EXISTS idx_graduate_program_alias_alias ON graduate_program_alias(alias);

CREATE TABLE IF NOT EXISTS graduate_program_track (
    id BIGSERIAL PRIMARY KEY,
    university_id BIGINT NOT NULL,
    program_id BIGINT NOT NULL,
    track_type VARCHAR(50) NOT NULL DEFAULT 'TRACK',
    track_name VARCHAR(255) NOT NULL,
    track_order INTEGER,
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    description TEXT,
    source_id BIGINT NOT NULL,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_graduate_program_track_university
        FOREIGN KEY (university_id) REFERENCES university(id) ON DELETE CASCADE,
    CONSTRAINT fk_graduate_program_track_program
        FOREIGN KEY (program_id) REFERENCES graduate_program(id) ON DELETE CASCADE,
    CONSTRAINT fk_graduate_program_track_source
        FOREIGN KEY (source_id) REFERENCES source(id) ON DELETE RESTRICT,
    CONSTRAINT ck_graduate_program_track_type
        CHECK (track_type IN ('TRACK', 'CONCENTRATION', 'SPECIALIZATION', 'OPTION', 'PATHWAY', 'OTHER')),
    CONSTRAINT uq_graduate_program_track UNIQUE (program_id, track_type, track_name)
);

CREATE INDEX IF NOT EXISTS idx_graduate_program_track_university_id ON graduate_program_track(university_id);
CREATE INDEX IF NOT EXISTS idx_graduate_program_track_program_id ON graduate_program_track(program_id);
CREATE INDEX IF NOT EXISTS idx_graduate_program_track_source_id ON graduate_program_track(source_id);
CREATE INDEX IF NOT EXISTS idx_graduate_program_track_name ON graduate_program_track(track_name);

CREATE TABLE IF NOT EXISTS graduate_program_source (
    id BIGSERIAL PRIMARY KEY,
    university_id BIGINT NOT NULL,
    program_id BIGINT NOT NULL,
    source_id BIGINT NOT NULL,
    source_role VARCHAR(50) NOT NULL DEFAULT 'PRIMARY',
    source_order INTEGER NOT NULL DEFAULT 0,
    evidence_text TEXT,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_graduate_program_source_university
        FOREIGN KEY (university_id) REFERENCES university(id) ON DELETE CASCADE,
    CONSTRAINT fk_graduate_program_source_program
        FOREIGN KEY (program_id) REFERENCES graduate_program(id) ON DELETE CASCADE,
    CONSTRAINT fk_graduate_program_source_source
        FOREIGN KEY (source_id) REFERENCES source(id) ON DELETE RESTRICT,
    CONSTRAINT ck_graduate_program_source_role
        CHECK (source_role IN ('PRIMARY', 'SECONDARY', 'TUITION', 'ADMISSIONS', 'CATALOG', 'PDF', 'DEPARTMENT', 'FACULTY', 'OTHER')),
    CONSTRAINT uq_graduate_program_source UNIQUE (program_id, source_id, source_role)
);

CREATE INDEX IF NOT EXISTS idx_graduate_program_source_university_id ON graduate_program_source(university_id);
CREATE INDEX IF NOT EXISTS idx_graduate_program_source_program_id ON graduate_program_source(program_id);
CREATE INDEX IF NOT EXISTS idx_graduate_program_source_source_id ON graduate_program_source(source_id);
CREATE INDEX IF NOT EXISTS idx_graduate_program_source_role ON graduate_program_source(source_role);

CREATE TABLE IF NOT EXISTS graduate_program_relationship (
    id BIGSERIAL PRIMARY KEY,
    university_id BIGINT NOT NULL,
    program_id BIGINT NOT NULL,
    related_program_id BIGINT NOT NULL,
    relation_type VARCHAR(50) NOT NULL,
    note TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_graduate_program_relationship_university
        FOREIGN KEY (university_id) REFERENCES university(id) ON DELETE CASCADE,
    CONSTRAINT fk_graduate_program_relationship_program
        FOREIGN KEY (program_id) REFERENCES graduate_program(id) ON DELETE CASCADE,
    CONSTRAINT fk_graduate_program_relationship_related_program
        FOREIGN KEY (related_program_id) REFERENCES graduate_program(id) ON DELETE CASCADE,
    CONSTRAINT ck_graduate_program_relationship_type
        CHECK (relation_type IN ('CROSS_LISTED', 'INTERFACULTY', 'LEGACY_DUPLICATE', 'ALIAS_OF')),
    CONSTRAINT uq_graduate_program_relationship UNIQUE (program_id, related_program_id, relation_type)
);

CREATE INDEX IF NOT EXISTS idx_graduate_program_relationship_university_id ON graduate_program_relationship(university_id);
CREATE INDEX IF NOT EXISTS idx_graduate_program_relationship_program_id ON graduate_program_relationship(program_id);
CREATE INDEX IF NOT EXISTS idx_graduate_program_relationship_related_program_id ON graduate_program_relationship(related_program_id);
CREATE INDEX IF NOT EXISTS idx_graduate_program_relationship_type ON graduate_program_relationship(relation_type);

CREATE TABLE IF NOT EXISTS graduate_tuition_rate (
    id BIGSERIAL PRIMARY KEY,
    university_id BIGINT NOT NULL,
    faculty_id BIGINT,
    department_id BIGINT,
    program_id BIGINT,
    scope_level VARCHAR(20) NOT NULL,
    record_key TEXT NOT NULL,
    academic_year VARCHAR(20) NOT NULL,
    currency VARCHAR(10) NOT NULL,
    billing_basis VARCHAR(30) NOT NULL,
    amount NUMERIC(12, 2) NOT NULL,
    category VARCHAR(255) NOT NULL,
    notes TEXT,
    source_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_graduate_tuition_rate_university
        FOREIGN KEY (university_id) REFERENCES university(id) ON DELETE CASCADE,
    CONSTRAINT fk_graduate_tuition_rate_faculty
        FOREIGN KEY (faculty_id) REFERENCES university_faculty(id) ON DELETE SET NULL,
    CONSTRAINT fk_graduate_tuition_rate_department
        FOREIGN KEY (department_id) REFERENCES university_department(id) ON DELETE SET NULL,
    CONSTRAINT fk_graduate_tuition_rate_program
        FOREIGN KEY (program_id) REFERENCES graduate_program(id) ON DELETE SET NULL,
    CONSTRAINT fk_graduate_tuition_rate_source
        FOREIGN KEY (source_id) REFERENCES source(id) ON DELETE RESTRICT,
    CONSTRAINT ck_graduate_tuition_rate_scope_level
        CHECK (scope_level IN ('UNIVERSITY', 'FACULTY', 'DEPARTMENT', 'PROGRAM')),
    CONSTRAINT ck_graduate_tuition_rate_billing_basis
        CHECK (billing_basis IN ('PER_CREDIT', 'PER_SEMESTER', 'PER_YEAR', 'PER_TERM', 'PER_PROGRAM', 'FLAT_FEE', 'PER_APPLICATION', 'PER_ACADEMIC_YEAR')),
    CONSTRAINT ck_graduate_tuition_rate_record_key_not_blank
        CHECK (btrim(record_key) <> ''),
    CONSTRAINT uq_graduate_tuition_rate_record_key UNIQUE (university_id, record_key)
);

CREATE INDEX IF NOT EXISTS idx_graduate_tuition_rate_university_id ON graduate_tuition_rate(university_id);
CREATE INDEX IF NOT EXISTS idx_graduate_tuition_rate_faculty_id ON graduate_tuition_rate(faculty_id);
CREATE INDEX IF NOT EXISTS idx_graduate_tuition_rate_department_id ON graduate_tuition_rate(department_id);
CREATE INDEX IF NOT EXISTS idx_graduate_tuition_rate_program_id ON graduate_tuition_rate(program_id);
CREATE INDEX IF NOT EXISTS idx_graduate_tuition_rate_source_id ON graduate_tuition_rate(source_id);
CREATE INDEX IF NOT EXISTS idx_graduate_tuition_rate_scope_level ON graduate_tuition_rate(scope_level);
CREATE INDEX IF NOT EXISTS idx_graduate_tuition_rate_academic_year ON graduate_tuition_rate(academic_year);

CREATE TABLE IF NOT EXISTS graduate_fee_item (
    id BIGSERIAL PRIMARY KEY,
    university_id BIGINT NOT NULL,
    faculty_id BIGINT,
    department_id BIGINT,
    program_id BIGINT,
    scope_level VARCHAR(20) NOT NULL,
    record_key TEXT NOT NULL,
    academic_year VARCHAR(20),
    fee_name VARCHAR(255) NOT NULL,
    billing_basis VARCHAR(30) NOT NULL DEFAULT 'FLAT_FEE',
    currency VARCHAR(10) NOT NULL,
    amount NUMERIC(12, 2),
    category VARCHAR(255),
    notes TEXT,
    source_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_graduate_fee_item_university
        FOREIGN KEY (university_id) REFERENCES university(id) ON DELETE CASCADE,
    CONSTRAINT fk_graduate_fee_item_faculty
        FOREIGN KEY (faculty_id) REFERENCES university_faculty(id) ON DELETE SET NULL,
    CONSTRAINT fk_graduate_fee_item_department
        FOREIGN KEY (department_id) REFERENCES university_department(id) ON DELETE SET NULL,
    CONSTRAINT fk_graduate_fee_item_program
        FOREIGN KEY (program_id) REFERENCES graduate_program(id) ON DELETE SET NULL,
    CONSTRAINT fk_graduate_fee_item_source
        FOREIGN KEY (source_id) REFERENCES source(id) ON DELETE RESTRICT,
    CONSTRAINT ck_graduate_fee_item_scope_level
        CHECK (scope_level IN ('UNIVERSITY', 'FACULTY', 'DEPARTMENT', 'PROGRAM')),
    CONSTRAINT ck_graduate_fee_item_billing_basis
        CHECK (billing_basis IN ('PER_CREDIT', 'PER_SEMESTER', 'PER_YEAR', 'PER_TERM', 'PER_PROGRAM', 'FLAT_FEE', 'PER_APPLICATION', 'PER_ACADEMIC_YEAR')),
    CONSTRAINT ck_graduate_fee_item_record_key_not_blank
        CHECK (btrim(record_key) <> ''),
    CONSTRAINT uq_graduate_fee_item_record_key UNIQUE (university_id, record_key)
);

CREATE INDEX IF NOT EXISTS idx_graduate_fee_item_university_id ON graduate_fee_item(university_id);
CREATE INDEX IF NOT EXISTS idx_graduate_fee_item_faculty_id ON graduate_fee_item(faculty_id);
CREATE INDEX IF NOT EXISTS idx_graduate_fee_item_department_id ON graduate_fee_item(department_id);
CREATE INDEX IF NOT EXISTS idx_graduate_fee_item_program_id ON graduate_fee_item(program_id);
CREATE INDEX IF NOT EXISTS idx_graduate_fee_item_source_id ON graduate_fee_item(source_id);
CREATE INDEX IF NOT EXISTS idx_graduate_fee_item_scope_level ON graduate_fee_item(scope_level);
CREATE INDEX IF NOT EXISTS idx_graduate_fee_item_academic_year ON graduate_fee_item(academic_year);
CREATE INDEX IF NOT EXISTS idx_graduate_fee_item_name ON graduate_fee_item(fee_name);

CREATE TABLE IF NOT EXISTS graduate_admission_requirement (
    id BIGSERIAL PRIMARY KEY,
    university_id BIGINT NOT NULL,
    faculty_id BIGINT,
    department_id BIGINT,
    program_id BIGINT,
    scope_level VARCHAR(20) NOT NULL,
    record_key TEXT NOT NULL,
    requirement_type VARCHAR(50) NOT NULL,
    requirement_text TEXT NOT NULL,
    comparison_operator VARCHAR(10),
    threshold_value NUMERIC(12, 2),
    threshold_unit VARCHAR(50),
    is_required BOOLEAN NOT NULL DEFAULT TRUE,
    notes TEXT,
    source_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_graduate_admission_requirement_university
        FOREIGN KEY (university_id) REFERENCES university(id) ON DELETE CASCADE,
    CONSTRAINT fk_graduate_admission_requirement_faculty
        FOREIGN KEY (faculty_id) REFERENCES university_faculty(id) ON DELETE SET NULL,
    CONSTRAINT fk_graduate_admission_requirement_department
        FOREIGN KEY (department_id) REFERENCES university_department(id) ON DELETE SET NULL,
    CONSTRAINT fk_graduate_admission_requirement_program
        FOREIGN KEY (program_id) REFERENCES graduate_program(id) ON DELETE SET NULL,
    CONSTRAINT fk_graduate_admission_requirement_source
        FOREIGN KEY (source_id) REFERENCES source(id) ON DELETE RESTRICT,
    CONSTRAINT ck_graduate_admission_requirement_scope_level
        CHECK (scope_level IN ('UNIVERSITY', 'FACULTY', 'DEPARTMENT', 'PROGRAM')),
    CONSTRAINT ck_graduate_admission_requirement_type
        CHECK (requirement_type IN ('GENERAL', 'GRE', 'GMAT', 'ENGLISH', 'PORTFOLIO', 'INTERVIEW', 'EXPERIENCE', 'ACADEMIC', 'PREREQUISITE', 'OTHER')),
    CONSTRAINT ck_graduate_admission_requirement_record_key_not_blank
        CHECK (btrim(record_key) <> ''),
    CONSTRAINT uq_graduate_admission_requirement_record_key UNIQUE (university_id, record_key)
);

CREATE INDEX IF NOT EXISTS idx_graduate_admission_requirement_university_id ON graduate_admission_requirement(university_id);
CREATE INDEX IF NOT EXISTS idx_graduate_admission_requirement_faculty_id ON graduate_admission_requirement(faculty_id);
CREATE INDEX IF NOT EXISTS idx_graduate_admission_requirement_department_id ON graduate_admission_requirement(department_id);
CREATE INDEX IF NOT EXISTS idx_graduate_admission_requirement_program_id ON graduate_admission_requirement(program_id);
CREATE INDEX IF NOT EXISTS idx_graduate_admission_requirement_source_id ON graduate_admission_requirement(source_id);
CREATE INDEX IF NOT EXISTS idx_graduate_admission_requirement_scope_level ON graduate_admission_requirement(scope_level);
CREATE INDEX IF NOT EXISTS idx_graduate_admission_requirement_type ON graduate_admission_requirement(requirement_type);

CREATE TABLE IF NOT EXISTS graduate_required_document (
    id BIGSERIAL PRIMARY KEY,
    university_id BIGINT NOT NULL,
    faculty_id BIGINT,
    department_id BIGINT,
    program_id BIGINT,
    scope_level VARCHAR(20) NOT NULL,
    record_key TEXT NOT NULL,
    document_type VARCHAR(100) NOT NULL,
    document_name VARCHAR(255) NOT NULL,
    is_optional BOOLEAN NOT NULL DEFAULT FALSE,
    sort_order INTEGER,
    notes TEXT,
    source_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_graduate_required_document_university
        FOREIGN KEY (university_id) REFERENCES university(id) ON DELETE CASCADE,
    CONSTRAINT fk_graduate_required_document_faculty
        FOREIGN KEY (faculty_id) REFERENCES university_faculty(id) ON DELETE SET NULL,
    CONSTRAINT fk_graduate_required_document_department
        FOREIGN KEY (department_id) REFERENCES university_department(id) ON DELETE SET NULL,
    CONSTRAINT fk_graduate_required_document_program
        FOREIGN KEY (program_id) REFERENCES graduate_program(id) ON DELETE SET NULL,
    CONSTRAINT fk_graduate_required_document_source
        FOREIGN KEY (source_id) REFERENCES source(id) ON DELETE RESTRICT,
    CONSTRAINT ck_graduate_required_document_scope_level
        CHECK (scope_level IN ('UNIVERSITY', 'FACULTY', 'DEPARTMENT', 'PROGRAM')),
    CONSTRAINT ck_graduate_required_document_record_key_not_blank
        CHECK (btrim(record_key) <> ''),
    CONSTRAINT uq_graduate_required_document_record_key UNIQUE (university_id, record_key)
);

CREATE INDEX IF NOT EXISTS idx_graduate_required_document_university_id ON graduate_required_document(university_id);
CREATE INDEX IF NOT EXISTS idx_graduate_required_document_faculty_id ON graduate_required_document(faculty_id);
CREATE INDEX IF NOT EXISTS idx_graduate_required_document_department_id ON graduate_required_document(department_id);
CREATE INDEX IF NOT EXISTS idx_graduate_required_document_program_id ON graduate_required_document(program_id);
CREATE INDEX IF NOT EXISTS idx_graduate_required_document_source_id ON graduate_required_document(source_id);
CREATE INDEX IF NOT EXISTS idx_graduate_required_document_scope_level ON graduate_required_document(scope_level);
CREATE INDEX IF NOT EXISTS idx_graduate_required_document_type ON graduate_required_document(document_type);

CREATE TABLE IF NOT EXISTS graduate_admission_deadline (
    id BIGSERIAL PRIMARY KEY,
    university_id BIGINT NOT NULL,
    faculty_id BIGINT,
    department_id BIGINT,
    program_id BIGINT,
    scope_level VARCHAR(20) NOT NULL,
    record_key TEXT NOT NULL,
    academic_year VARCHAR(20),
    deadline_type VARCHAR(50) NOT NULL,
    term VARCHAR(100),
    deadline_date DATE,
    note TEXT,
    source_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_graduate_admission_deadline_university
        FOREIGN KEY (university_id) REFERENCES university(id) ON DELETE CASCADE,
    CONSTRAINT fk_graduate_admission_deadline_faculty
        FOREIGN KEY (faculty_id) REFERENCES university_faculty(id) ON DELETE SET NULL,
    CONSTRAINT fk_graduate_admission_deadline_department
        FOREIGN KEY (department_id) REFERENCES university_department(id) ON DELETE SET NULL,
    CONSTRAINT fk_graduate_admission_deadline_program
        FOREIGN KEY (program_id) REFERENCES graduate_program(id) ON DELETE SET NULL,
    CONSTRAINT fk_graduate_admission_deadline_source
        FOREIGN KEY (source_id) REFERENCES source(id) ON DELETE RESTRICT,
    CONSTRAINT ck_graduate_admission_deadline_scope_level
        CHECK (scope_level IN ('UNIVERSITY', 'FACULTY', 'DEPARTMENT', 'PROGRAM')),
    CONSTRAINT ck_graduate_admission_deadline_type
        CHECK (deadline_type IN ('APPLICATION_OPEN', 'EARLY', 'PRIORITY', 'REGULAR', 'FINAL', 'INTERVIEW', 'ENROLLMENT', 'OTHER')),
    CONSTRAINT ck_graduate_admission_deadline_record_key_not_blank
        CHECK (btrim(record_key) <> ''),
    CONSTRAINT uq_graduate_admission_deadline_record_key UNIQUE (university_id, record_key)
);

CREATE INDEX IF NOT EXISTS idx_graduate_admission_deadline_university_id ON graduate_admission_deadline(university_id);
CREATE INDEX IF NOT EXISTS idx_graduate_admission_deadline_faculty_id ON graduate_admission_deadline(faculty_id);
CREATE INDEX IF NOT EXISTS idx_graduate_admission_deadline_department_id ON graduate_admission_deadline(department_id);
CREATE INDEX IF NOT EXISTS idx_graduate_admission_deadline_program_id ON graduate_admission_deadline(program_id);
CREATE INDEX IF NOT EXISTS idx_graduate_admission_deadline_source_id ON graduate_admission_deadline(source_id);
CREATE INDEX IF NOT EXISTS idx_graduate_admission_deadline_scope_level ON graduate_admission_deadline(scope_level);
CREATE INDEX IF NOT EXISTS idx_graduate_admission_deadline_academic_year ON graduate_admission_deadline(academic_year);

CREATE TABLE IF NOT EXISTS graduate_scholarship (
    id BIGSERIAL PRIMARY KEY,
    university_id BIGINT NOT NULL,
    faculty_id BIGINT,
    department_id BIGINT,
    program_id BIGINT,
    scope_level VARCHAR(20) NOT NULL,
    record_key TEXT NOT NULL,
    academic_year VARCHAR(20),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    coverage TEXT,
    amount NUMERIC(12, 2),
    currency VARCHAR(10),
    notes TEXT,
    source_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_graduate_scholarship_university
        FOREIGN KEY (university_id) REFERENCES university(id) ON DELETE CASCADE,
    CONSTRAINT fk_graduate_scholarship_faculty
        FOREIGN KEY (faculty_id) REFERENCES university_faculty(id) ON DELETE SET NULL,
    CONSTRAINT fk_graduate_scholarship_department
        FOREIGN KEY (department_id) REFERENCES university_department(id) ON DELETE SET NULL,
    CONSTRAINT fk_graduate_scholarship_program
        FOREIGN KEY (program_id) REFERENCES graduate_program(id) ON DELETE SET NULL,
    CONSTRAINT fk_graduate_scholarship_source
        FOREIGN KEY (source_id) REFERENCES source(id) ON DELETE RESTRICT,
    CONSTRAINT ck_graduate_scholarship_scope_level
        CHECK (scope_level IN ('UNIVERSITY', 'FACULTY', 'DEPARTMENT', 'PROGRAM')),
    CONSTRAINT ck_graduate_scholarship_record_key_not_blank
        CHECK (btrim(record_key) <> ''),
    CONSTRAINT uq_graduate_scholarship_record_key UNIQUE (university_id, record_key)
);

CREATE INDEX IF NOT EXISTS idx_graduate_scholarship_university_id ON graduate_scholarship(university_id);
CREATE INDEX IF NOT EXISTS idx_graduate_scholarship_faculty_id ON graduate_scholarship(faculty_id);
CREATE INDEX IF NOT EXISTS idx_graduate_scholarship_department_id ON graduate_scholarship(department_id);
CREATE INDEX IF NOT EXISTS idx_graduate_scholarship_program_id ON graduate_scholarship(program_id);
CREATE INDEX IF NOT EXISTS idx_graduate_scholarship_source_id ON graduate_scholarship(source_id);
CREATE INDEX IF NOT EXISTS idx_graduate_scholarship_scope_level ON graduate_scholarship(scope_level);
CREATE INDEX IF NOT EXISTS idx_graduate_scholarship_academic_year ON graduate_scholarship(academic_year);
CREATE INDEX IF NOT EXISTS idx_graduate_scholarship_name ON graduate_scholarship(name);

CREATE TABLE IF NOT EXISTS graduate_financial_aid (
    id BIGSERIAL PRIMARY KEY,
    university_id BIGINT NOT NULL,
    faculty_id BIGINT,
    department_id BIGINT,
    program_id BIGINT,
    scope_level VARCHAR(20) NOT NULL,
    record_key TEXT NOT NULL,
    academic_year VARCHAR(20),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    amount NUMERIC(12, 2),
    currency VARCHAR(10),
    notes TEXT,
    source_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_graduate_financial_aid_university
        FOREIGN KEY (university_id) REFERENCES university(id) ON DELETE CASCADE,
    CONSTRAINT fk_graduate_financial_aid_faculty
        FOREIGN KEY (faculty_id) REFERENCES university_faculty(id) ON DELETE SET NULL,
    CONSTRAINT fk_graduate_financial_aid_department
        FOREIGN KEY (department_id) REFERENCES university_department(id) ON DELETE SET NULL,
    CONSTRAINT fk_graduate_financial_aid_program
        FOREIGN KEY (program_id) REFERENCES graduate_program(id) ON DELETE SET NULL,
    CONSTRAINT fk_graduate_financial_aid_source
        FOREIGN KEY (source_id) REFERENCES source(id) ON DELETE RESTRICT,
    CONSTRAINT ck_graduate_financial_aid_scope_level
        CHECK (scope_level IN ('UNIVERSITY', 'FACULTY', 'DEPARTMENT', 'PROGRAM')),
    CONSTRAINT ck_graduate_financial_aid_record_key_not_blank
        CHECK (btrim(record_key) <> ''),
    CONSTRAINT uq_graduate_financial_aid_record_key UNIQUE (university_id, record_key)
);

CREATE INDEX IF NOT EXISTS idx_graduate_financial_aid_university_id ON graduate_financial_aid(university_id);
CREATE INDEX IF NOT EXISTS idx_graduate_financial_aid_faculty_id ON graduate_financial_aid(faculty_id);
CREATE INDEX IF NOT EXISTS idx_graduate_financial_aid_department_id ON graduate_financial_aid(department_id);
CREATE INDEX IF NOT EXISTS idx_graduate_financial_aid_program_id ON graduate_financial_aid(program_id);
CREATE INDEX IF NOT EXISTS idx_graduate_financial_aid_source_id ON graduate_financial_aid(source_id);
CREATE INDEX IF NOT EXISTS idx_graduate_financial_aid_scope_level ON graduate_financial_aid(scope_level);
CREATE INDEX IF NOT EXISTS idx_graduate_financial_aid_academic_year ON graduate_financial_aid(academic_year);
CREATE INDEX IF NOT EXISTS idx_graduate_financial_aid_name ON graduate_financial_aid(name);

CREATE TABLE IF NOT EXISTS graduate_payment_plan (
    id BIGSERIAL PRIMARY KEY,
    university_id BIGINT NOT NULL,
    faculty_id BIGINT,
    department_id BIGINT,
    program_id BIGINT,
    scope_level VARCHAR(20) NOT NULL,
    record_key TEXT NOT NULL,
    academic_year VARCHAR(20),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    installments_count INTEGER,
    down_payment_amount NUMERIC(12, 2),
    down_payment_currency VARCHAR(10),
    interval_label VARCHAR(100),
    notes TEXT,
    source_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_graduate_payment_plan_university
        FOREIGN KEY (university_id) REFERENCES university(id) ON DELETE CASCADE,
    CONSTRAINT fk_graduate_payment_plan_faculty
        FOREIGN KEY (faculty_id) REFERENCES university_faculty(id) ON DELETE SET NULL,
    CONSTRAINT fk_graduate_payment_plan_department
        FOREIGN KEY (department_id) REFERENCES university_department(id) ON DELETE SET NULL,
    CONSTRAINT fk_graduate_payment_plan_program
        FOREIGN KEY (program_id) REFERENCES graduate_program(id) ON DELETE SET NULL,
    CONSTRAINT fk_graduate_payment_plan_source
        FOREIGN KEY (source_id) REFERENCES source(id) ON DELETE RESTRICT,
    CONSTRAINT ck_graduate_payment_plan_scope_level
        CHECK (scope_level IN ('UNIVERSITY', 'FACULTY', 'DEPARTMENT', 'PROGRAM')),
    CONSTRAINT ck_graduate_payment_plan_record_key_not_blank
        CHECK (btrim(record_key) <> ''),
    CONSTRAINT uq_graduate_payment_plan_record_key UNIQUE (university_id, record_key)
);

CREATE INDEX IF NOT EXISTS idx_graduate_payment_plan_university_id ON graduate_payment_plan(university_id);
CREATE INDEX IF NOT EXISTS idx_graduate_payment_plan_faculty_id ON graduate_payment_plan(faculty_id);
CREATE INDEX IF NOT EXISTS idx_graduate_payment_plan_department_id ON graduate_payment_plan(department_id);
CREATE INDEX IF NOT EXISTS idx_graduate_payment_plan_program_id ON graduate_payment_plan(program_id);
CREATE INDEX IF NOT EXISTS idx_graduate_payment_plan_source_id ON graduate_payment_plan(source_id);
CREATE INDEX IF NOT EXISTS idx_graduate_payment_plan_scope_level ON graduate_payment_plan(scope_level);
CREATE INDEX IF NOT EXISTS idx_graduate_payment_plan_academic_year ON graduate_payment_plan(academic_year);
CREATE INDEX IF NOT EXISTS idx_graduate_payment_plan_name ON graduate_payment_plan(name);

CREATE TABLE IF NOT EXISTS graduate_accreditation (
    id BIGSERIAL PRIMARY KEY,
    university_id BIGINT NOT NULL,
    faculty_id BIGINT,
    department_id BIGINT,
    program_id BIGINT,
    scope_level VARCHAR(20) NOT NULL,
    record_key TEXT NOT NULL,
    name VARCHAR(255) NOT NULL,
    authority VARCHAR(255),
    status VARCHAR(100),
    valid_from DATE,
    valid_until DATE,
    notes TEXT,
    source_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_graduate_accreditation_university
        FOREIGN KEY (university_id) REFERENCES university(id) ON DELETE CASCADE,
    CONSTRAINT fk_graduate_accreditation_faculty
        FOREIGN KEY (faculty_id) REFERENCES university_faculty(id) ON DELETE SET NULL,
    CONSTRAINT fk_graduate_accreditation_department
        FOREIGN KEY (department_id) REFERENCES university_department(id) ON DELETE SET NULL,
    CONSTRAINT fk_graduate_accreditation_program
        FOREIGN KEY (program_id) REFERENCES graduate_program(id) ON DELETE SET NULL,
    CONSTRAINT fk_graduate_accreditation_source
        FOREIGN KEY (source_id) REFERENCES source(id) ON DELETE RESTRICT,
    CONSTRAINT ck_graduate_accreditation_scope_level
        CHECK (scope_level IN ('UNIVERSITY', 'FACULTY', 'DEPARTMENT', 'PROGRAM')),
    CONSTRAINT ck_graduate_accreditation_record_key_not_blank
        CHECK (btrim(record_key) <> ''),
    CONSTRAINT uq_graduate_accreditation_record_key UNIQUE (university_id, record_key)
);

CREATE INDEX IF NOT EXISTS idx_graduate_accreditation_university_id ON graduate_accreditation(university_id);
CREATE INDEX IF NOT EXISTS idx_graduate_accreditation_faculty_id ON graduate_accreditation(faculty_id);
CREATE INDEX IF NOT EXISTS idx_graduate_accreditation_department_id ON graduate_accreditation(department_id);
CREATE INDEX IF NOT EXISTS idx_graduate_accreditation_program_id ON graduate_accreditation(program_id);
CREATE INDEX IF NOT EXISTS idx_graduate_accreditation_source_id ON graduate_accreditation(source_id);
CREATE INDEX IF NOT EXISTS idx_graduate_accreditation_scope_level ON graduate_accreditation(scope_level);
CREATE INDEX IF NOT EXISTS idx_graduate_accreditation_name ON graduate_accreditation(name);
