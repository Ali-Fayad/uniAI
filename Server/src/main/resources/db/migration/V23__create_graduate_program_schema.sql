CREATE TABLE IF NOT EXISTS degree_type (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_degree_type_code UNIQUE (code),
    CONSTRAINT uq_degree_type_name UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS major_category (
    id BIGSERIAL PRIMARY KEY,
    university_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_major_category_university
        FOREIGN KEY (university_id) REFERENCES university(id) ON DELETE CASCADE,
    CONSTRAINT uq_major_category_university_name UNIQUE (university_id, name)
);

CREATE INDEX IF NOT EXISTS idx_major_category_university_id ON major_category(university_id);

CREATE TABLE IF NOT EXISTS major (
    id BIGSERIAL PRIMARY KEY,
    university_id BIGINT NOT NULL,
    major_category_id BIGINT,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_major_university
        FOREIGN KEY (university_id) REFERENCES university(id) ON DELETE CASCADE,
    CONSTRAINT fk_major_category
        FOREIGN KEY (major_category_id) REFERENCES major_category(id) ON DELETE SET NULL,
    CONSTRAINT uq_major_university_category_name UNIQUE (university_id, major_category_id, name)
);

CREATE INDEX IF NOT EXISTS idx_major_university_id ON major(university_id);
CREATE INDEX IF NOT EXISTS idx_major_major_category_id ON major(major_category_id);

CREATE TABLE IF NOT EXISTS major_degree (
    id BIGSERIAL PRIMARY KEY,
    university_id BIGINT NOT NULL,
    major_id BIGINT,
    degree_type_id BIGINT,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    duration_value INTEGER,
    duration_unit VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_major_degree_university
        FOREIGN KEY (university_id) REFERENCES university(id) ON DELETE CASCADE,
    CONSTRAINT fk_major_degree_major
        FOREIGN KEY (major_id) REFERENCES major(id) ON DELETE SET NULL,
    CONSTRAINT fk_major_degree_degree_type
        FOREIGN KEY (degree_type_id) REFERENCES degree_type(id) ON DELETE SET NULL,
    CONSTRAINT uq_major_degree_university_name UNIQUE (university_id, name)
);

CREATE INDEX IF NOT EXISTS idx_major_degree_university_id ON major_degree(university_id);
CREATE INDEX IF NOT EXISTS idx_major_degree_major_id ON major_degree(major_id);
CREATE INDEX IF NOT EXISTS idx_major_degree_degree_type_id ON major_degree(degree_type_id);

CREATE TABLE IF NOT EXISTS source (
    id BIGSERIAL PRIMARY KEY,
    university_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    url TEXT NOT NULL,
    source_type VARCHAR(50) NOT NULL DEFAULT 'WEB',
    accessed_at DATE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_source_university
        FOREIGN KEY (university_id) REFERENCES university(id) ON DELETE CASCADE,
    CONSTRAINT uq_source_university_url UNIQUE (university_id, url)
);

CREATE INDEX IF NOT EXISTS idx_source_university_id ON source(university_id);

CREATE TABLE IF NOT EXISTS tuition_fee (
    id BIGSERIAL PRIMARY KEY,
    university_id BIGINT NOT NULL,
    major_degree_id BIGINT,
    source_id BIGINT,
    amount NUMERIC(12, 2),
    currency VARCHAR(10),
    period VARCHAR(50),
    note TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_tuition_fee_university
        FOREIGN KEY (university_id) REFERENCES university(id) ON DELETE CASCADE,
    CONSTRAINT fk_tuition_fee_major_degree
        FOREIGN KEY (major_degree_id) REFERENCES major_degree(id) ON DELETE SET NULL,
    CONSTRAINT fk_tuition_fee_source
        FOREIGN KEY (source_id) REFERENCES source(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_tuition_fee_university_id ON tuition_fee(university_id);
CREATE INDEX IF NOT EXISTS idx_tuition_fee_major_degree_id ON tuition_fee(major_degree_id);
CREATE INDEX IF NOT EXISTS idx_tuition_fee_source_id ON tuition_fee(source_id);

CREATE TABLE IF NOT EXISTS fee_item (
    id BIGSERIAL PRIMARY KEY,
    tuition_fee_id BIGINT NOT NULL,
    source_id BIGINT,
    name VARCHAR(255) NOT NULL,
    amount NUMERIC(12, 2),
    currency VARCHAR(10),
    note TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_fee_item_tuition_fee
        FOREIGN KEY (tuition_fee_id) REFERENCES tuition_fee(id) ON DELETE CASCADE,
    CONSTRAINT fk_fee_item_source
        FOREIGN KEY (source_id) REFERENCES source(id) ON DELETE SET NULL,
    CONSTRAINT uq_fee_item_tuition_fee_name UNIQUE (tuition_fee_id, name)
);

CREATE INDEX IF NOT EXISTS idx_fee_item_tuition_fee_id ON fee_item(tuition_fee_id);
CREATE INDEX IF NOT EXISTS idx_fee_item_source_id ON fee_item(source_id);

CREATE TABLE IF NOT EXISTS admission_requirement (
    id BIGSERIAL PRIMARY KEY,
    university_id BIGINT NOT NULL,
    major_degree_id BIGINT,
    source_id BIGINT,
    requirement_type VARCHAR(100),
    requirement_text TEXT NOT NULL,
    sort_order INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_admission_requirement_university
        FOREIGN KEY (university_id) REFERENCES university(id) ON DELETE CASCADE,
    CONSTRAINT fk_admission_requirement_major_degree
        FOREIGN KEY (major_degree_id) REFERENCES major_degree(id) ON DELETE SET NULL,
    CONSTRAINT fk_admission_requirement_source
        FOREIGN KEY (source_id) REFERENCES source(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_admission_requirement_university_id ON admission_requirement(university_id);
CREATE INDEX IF NOT EXISTS idx_admission_requirement_major_degree_id ON admission_requirement(major_degree_id);
CREATE INDEX IF NOT EXISTS idx_admission_requirement_source_id ON admission_requirement(source_id);

CREATE TABLE IF NOT EXISTS required_document (
    id BIGSERIAL PRIMARY KEY,
    university_id BIGINT NOT NULL,
    major_degree_id BIGINT,
    admission_requirement_id BIGINT,
    source_id BIGINT,
    name VARCHAR(255) NOT NULL,
    is_optional BOOLEAN NOT NULL DEFAULT FALSE,
    sort_order INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_required_document_university
        FOREIGN KEY (university_id) REFERENCES university(id) ON DELETE CASCADE,
    CONSTRAINT fk_required_document_major_degree
        FOREIGN KEY (major_degree_id) REFERENCES major_degree(id) ON DELETE SET NULL,
    CONSTRAINT fk_required_document_admission_requirement
        FOREIGN KEY (admission_requirement_id) REFERENCES admission_requirement(id) ON DELETE SET NULL,
    CONSTRAINT fk_required_document_source
        FOREIGN KEY (source_id) REFERENCES source(id) ON DELETE SET NULL,
    CONSTRAINT uq_required_document_requirement_name UNIQUE (admission_requirement_id, name)
);

CREATE INDEX IF NOT EXISTS idx_required_document_university_id ON required_document(university_id);
CREATE INDEX IF NOT EXISTS idx_required_document_major_degree_id ON required_document(major_degree_id);
CREATE INDEX IF NOT EXISTS idx_required_document_admission_requirement_id ON required_document(admission_requirement_id);
CREATE INDEX IF NOT EXISTS idx_required_document_source_id ON required_document(source_id);

CREATE TABLE IF NOT EXISTS admission_deadline (
    id BIGSERIAL PRIMARY KEY,
    university_id BIGINT NOT NULL,
    major_degree_id BIGINT,
    source_id BIGINT,
    deadline_type VARCHAR(100),
    term VARCHAR(100),
    deadline_date DATE,
    note TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_admission_deadline_university
        FOREIGN KEY (university_id) REFERENCES university(id) ON DELETE CASCADE,
    CONSTRAINT fk_admission_deadline_major_degree
        FOREIGN KEY (major_degree_id) REFERENCES major_degree(id) ON DELETE SET NULL,
    CONSTRAINT fk_admission_deadline_source
        FOREIGN KEY (source_id) REFERENCES source(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_admission_deadline_university_id ON admission_deadline(university_id);
CREATE INDEX IF NOT EXISTS idx_admission_deadline_major_degree_id ON admission_deadline(major_degree_id);
CREATE INDEX IF NOT EXISTS idx_admission_deadline_source_id ON admission_deadline(source_id);

CREATE TABLE IF NOT EXISTS scholarship (
    id BIGSERIAL PRIMARY KEY,
    university_id BIGINT NOT NULL,
    major_degree_id BIGINT,
    source_id BIGINT,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    amount NUMERIC(12, 2),
    currency VARCHAR(10),
    coverage TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_scholarship_university
        FOREIGN KEY (university_id) REFERENCES university(id) ON DELETE CASCADE,
    CONSTRAINT fk_scholarship_major_degree
        FOREIGN KEY (major_degree_id) REFERENCES major_degree(id) ON DELETE SET NULL,
    CONSTRAINT fk_scholarship_source
        FOREIGN KEY (source_id) REFERENCES source(id) ON DELETE SET NULL,
    CONSTRAINT uq_scholarship_university_name UNIQUE (university_id, name)
);

CREATE INDEX IF NOT EXISTS idx_scholarship_university_id ON scholarship(university_id);
CREATE INDEX IF NOT EXISTS idx_scholarship_major_degree_id ON scholarship(major_degree_id);
CREATE INDEX IF NOT EXISTS idx_scholarship_source_id ON scholarship(source_id);

CREATE TABLE IF NOT EXISTS financial_aid (
    id BIGSERIAL PRIMARY KEY,
    university_id BIGINT NOT NULL,
    major_degree_id BIGINT,
    source_id BIGINT,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    amount NUMERIC(12, 2),
    currency VARCHAR(10),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_financial_aid_university
        FOREIGN KEY (university_id) REFERENCES university(id) ON DELETE CASCADE,
    CONSTRAINT fk_financial_aid_major_degree
        FOREIGN KEY (major_degree_id) REFERENCES major_degree(id) ON DELETE SET NULL,
    CONSTRAINT fk_financial_aid_source
        FOREIGN KEY (source_id) REFERENCES source(id) ON DELETE SET NULL,
    CONSTRAINT uq_financial_aid_university_name UNIQUE (university_id, name)
);

CREATE INDEX IF NOT EXISTS idx_financial_aid_university_id ON financial_aid(university_id);
CREATE INDEX IF NOT EXISTS idx_financial_aid_major_degree_id ON financial_aid(major_degree_id);
CREATE INDEX IF NOT EXISTS idx_financial_aid_source_id ON financial_aid(source_id);

CREATE TABLE IF NOT EXISTS payment_plan (
    id BIGSERIAL PRIMARY KEY,
    university_id BIGINT NOT NULL,
    major_degree_id BIGINT,
    source_id BIGINT,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    installments_count INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_payment_plan_university
        FOREIGN KEY (university_id) REFERENCES university(id) ON DELETE CASCADE,
    CONSTRAINT fk_payment_plan_major_degree
        FOREIGN KEY (major_degree_id) REFERENCES major_degree(id) ON DELETE SET NULL,
    CONSTRAINT fk_payment_plan_source
        FOREIGN KEY (source_id) REFERENCES source(id) ON DELETE SET NULL,
    CONSTRAINT uq_payment_plan_university_name UNIQUE (university_id, name)
);

CREATE INDEX IF NOT EXISTS idx_payment_plan_university_id ON payment_plan(university_id);
CREATE INDEX IF NOT EXISTS idx_payment_plan_major_degree_id ON payment_plan(major_degree_id);
CREATE INDEX IF NOT EXISTS idx_payment_plan_source_id ON payment_plan(source_id);

CREATE TABLE IF NOT EXISTS accreditation (
    id BIGSERIAL PRIMARY KEY,
    university_id BIGINT NOT NULL,
    major_degree_id BIGINT,
    source_id BIGINT,
    name VARCHAR(255) NOT NULL,
    authority VARCHAR(255),
    status VARCHAR(100),
    valid_from DATE,
    valid_until DATE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_accreditation_university
        FOREIGN KEY (university_id) REFERENCES university(id) ON DELETE CASCADE,
    CONSTRAINT fk_accreditation_major_degree
        FOREIGN KEY (major_degree_id) REFERENCES major_degree(id) ON DELETE SET NULL,
    CONSTRAINT fk_accreditation_source
        FOREIGN KEY (source_id) REFERENCES source(id) ON DELETE SET NULL,
    CONSTRAINT uq_accreditation_university_name UNIQUE (university_id, name)
);

CREATE INDEX IF NOT EXISTS idx_accreditation_university_id ON accreditation(university_id);
CREATE INDEX IF NOT EXISTS idx_accreditation_major_degree_id ON accreditation(major_degree_id);
CREATE INDEX IF NOT EXISTS idx_accreditation_source_id ON accreditation(source_id);
