-- LGU graduate data seed migration.
-- Idempotent import for the canonical LGU graduate dataset.

DO $$
DECLARE
    v_university_id BIGINT;
BEGIN
    INSERT INTO university (name, name_ar, acronym, country, city, latitude, longitude, campus_name, campus_type)
    SELECT 'Lebanese German University', NULL, 'LGU', 'Lebanon', NULL, NULL, NULL, NULL, NULL
    WHERE NOT EXISTS (
        SELECT 1
        FROM university
        WHERE name = 'Lebanese German University'
    );

    SELECT id
    INTO v_university_id
    FROM university
    WHERE name = 'Lebanese German University'
    ORDER BY id
    LIMIT 1;

    INSERT INTO degree_type (code, name)
    VALUES
        ('MASTER', 'Master'),
        ('PHD', 'Doctor of Philosophy')
    ON CONFLICT (code) DO UPDATE SET
        name = EXCLUDED.name,
        updated_at = NOW();

    INSERT INTO language (name, code, native_name)
    VALUES
        ('English', 'en', 'English'),
        ('French', 'fr', 'Français'),
        ('German', 'de', 'Deutsch')
    ON CONFLICT (code) DO UPDATE SET
        name = EXCLUDED.name,
        native_name = EXCLUDED.native_name;

    CREATE TEMP TABLE lgu_source_seed (
        source_id TEXT PRIMARY KEY,
        title TEXT NOT NULL,
        url TEXT NOT NULL,
        source_type TEXT NOT NULL,
        accessed_at DATE
    ) ON COMMIT DROP;

    INSERT INTO lgu_source_seed (source_id, title, url, source_type, accessed_at)
    VALUES
        ('LGU-SRC-001', 'Lebanese German University - Home', 'https://www.lgu.edu.lb/', 'official_page', '2026-07-05'),
        ('LGU-SRC-002', 'Admissions', 'https://lgu.edu.lb/admissions', 'official_page', '2026-07-05'),
        ('LGU-SRC-003', 'Graduate Studies', 'https://www.lgu.edu.lb/admissions/graduate-studies', 'official_page', '2026-07-05'),
        ('LGU-SRC-004', 'Academic Programs', 'https://www.lgu.edu.lb/programs', 'official_page', '2026-07-05'),
        ('LGU-SRC-005', 'Academics', 'https://www.lgu.edu.lb/academics', 'official_page', '2026-07-05'),
        ('LGU-SRC-006', 'Undergraduate Studies', 'https://www.lgu.edu.lb/admissions/undergraduate-studies', 'official_page', '2026-07-05'),
        ('LGU-SRC-007', 'Financial Aid', 'https://lgu.edu.lb/admissions/financial-aid', 'official_page', '2026-07-05'),
        ('LGU-SRC-008', 'Academic Calendar 2025 - 2026', 'https://www.lgu.edu.lb/academic-calendar', 'official_page', '2026-07-05'),
        ('LGU-SRC-009', 'Frequently Asked Questions', 'https://www.lgu.edu.lb/faqs', 'official_page', '2026-07-05'),
        ('LGU-SRC-010', 'Office of the Registrar', 'https://www.lgu.edu.lb/academics/office-of-the-registrar', 'official_page', '2026-07-05'),
        ('LGU-SRC-011', 'Faculty of Public Health', 'https://www.lgu.edu.lb/faculty/faculty-of-public-health', 'official_page', '2026-07-05'),
        ('LGU-SRC-012', 'Faculty of Business and Insurance', 'https://lgu.edu.lb/faculty/faculty-of-business-and-insurance', 'official_page', '2026-07-05'),
        ('LGU-SRC-013', 'Faculty of Arts and Education', 'https://www.lgu.edu.lb/faculty/faculty-of-arts-and-education', 'official_page', '2026-07-05'),
        ('LGU-SRC-014', 'Financial Aid V7 - FINAL 0', 'https://lgu.edu.lb/download/6b65c07f-abcf-434e-a6dd-aac533584daf', 'official_pdf', '2026-07-05'),
        ('LGU-SRC-015', 'Flyer Financial Aid', 'https://lgu.edu.lb/assets/files/Flyer%20Financial%20Aid.pdf', 'official_pdf', '2026-07-05');

    INSERT INTO source (university_id, title, url, source_type, accessed_at)
    SELECT v_university_id, title, url, source_type, accessed_at
    FROM lgu_source_seed
    ON CONFLICT (university_id, url) DO UPDATE SET
        title = EXCLUDED.title,
        source_type = EXCLUDED.source_type,
        accessed_at = EXCLUDED.accessed_at,
        updated_at = NOW();

    CREATE TEMP TABLE lgu_source_map AS
    SELECT
        ss.source_id,
        s.id AS db_source_id,
        s.url
    FROM lgu_source_seed ss
    JOIN source s
      ON s.university_id = v_university_id
     AND s.url = ss.url;

    CREATE TEMP TABLE lgu_faculty_seed (
        name TEXT PRIMARY KEY,
        official_url TEXT NOT NULL,
        notes TEXT
    ) ON COMMIT DROP;

    INSERT INTO lgu_faculty_seed (name, official_url, notes)
    VALUES
        ('Faculty of Business and Insurance', 'https://lgu.edu.lb/faculty/faculty-of-business-and-insurance', 'Official faculty identified in the graduate source set.'),
        ('Faculty of Arts and Education', 'https://www.lgu.edu.lb/faculty/faculty-of-arts-and-education', 'Official faculty identified in the graduate source set.'),
        ('Faculty of Public Health', 'https://www.lgu.edu.lb/faculty/faculty-of-public-health', 'Official faculty identified in the graduate source set.');

    INSERT INTO university_faculty (university_id, name, short_name, faculty_type, official_url, notes)
    SELECT v_university_id, name, NULL, 'FACULTY', official_url, notes
    FROM lgu_faculty_seed
    ON CONFLICT (university_id, name) DO UPDATE SET
        faculty_type = EXCLUDED.faculty_type,
        official_url = EXCLUDED.official_url,
        notes = EXCLUDED.notes,
        updated_at = NOW();

    CREATE TEMP TABLE lgu_program_seed (
        program_key TEXT PRIMARY KEY,
        title TEXT NOT NULL,
        faculty_name TEXT,
        program_description TEXT NOT NULL,
        notes TEXT NOT NULL,
        source_ids TEXT[] NOT NULL
    ) ON COMMIT DROP;

    INSERT INTO lgu_program_seed (program_key, title, faculty_name, program_description, notes, source_ids)
    VALUES
        (
            'master-business-administration',
            'Master of Business Administration (MBA)',
            'Faculty of Business and Insurance',
            'LGU''s official Graduate Studies page lists the Master of Business Administration as one of the university''s graduate offerings.',
            'Official graduate studies page names the MBA. No dedicated graduate program-detail page was recovered in this pass, so the official program URL is null.',
            ARRAY['LGU-SRC-003','LGU-SRC-005','LGU-SRC-012']
        ),
        (
            'master-science-engineering',
            'Master of Science in Engineering',
            NULL,
            'LGU''s official Graduate Studies page lists the Master of Science in Engineering as one of the university''s graduate offerings.',
            'Official graduate studies page names the program, but no dedicated graduate detail page or faculty assignment was recovered in this pass.',
            ARRAY['LGU-SRC-003']
        ),
        (
            'master-arts-education',
            'Master of Arts in Education',
            'Faculty of Arts and Education',
            'LGU''s official Graduate Studies page lists the Master of Arts in Education as one of the university''s graduate offerings.',
            'Official graduate studies page names the program. No dedicated graduate program-detail page was recovered in this pass, so the official program URL is null.',
            ARRAY['LGU-SRC-003','LGU-SRC-005','LGU-SRC-013']
        ),
        (
            'master-public-health',
            'Master of Public Health',
            'Faculty of Public Health',
            'LGU''s official Graduate Studies page lists the Master of Public Health as one of the university''s graduate offerings.',
            'Official graduate studies page names the program. No dedicated graduate program-detail page was recovered in this pass, so the official program URL is null.',
            ARRAY['LGU-SRC-003','LGU-SRC-005','LGU-SRC-011']
        );

    INSERT INTO graduate_program (
        university_id,
        faculty_id,
        department_id,
        degree_type_id,
        program_key,
        major_category,
        major,
        official_degree_name,
        thesis_or_non_thesis,
        credits,
        duration_value,
        duration_unit,
        primary_language_id,
        delivery_mode,
        program_description,
        official_program_url,
        source_id,
        notes
    )
    SELECT
        v_university_id,
        f.id,
        NULL,
        dt.id,
        seed.program_key,
        NULL,
        NULL,
        seed.title,
        NULL,
        NULL,
        NULL,
        NULL,
        NULL,
        NULL,
        seed.program_description,
        NULL,
        primary_source.db_source_id,
        seed.notes
    FROM lgu_program_seed seed
    LEFT JOIN lgu_faculty_seed fs
      ON fs.name = seed.faculty_name
    LEFT JOIN university_faculty f
      ON f.university_id = v_university_id
     AND f.name = fs.name
    JOIN degree_type dt
      ON dt.code = 'MASTER'
    JOIN LATERAL (
        SELECT db_source_id
        FROM lgu_source_map
        WHERE source_id = seed.source_ids[1]
        LIMIT 1
    ) primary_source ON TRUE
    ON CONFLICT (university_id, program_key) DO UPDATE SET
        faculty_id = EXCLUDED.faculty_id,
        department_id = EXCLUDED.department_id,
        degree_type_id = EXCLUDED.degree_type_id,
        major_category = EXCLUDED.major_category,
        major = EXCLUDED.major,
        official_degree_name = EXCLUDED.official_degree_name,
        thesis_or_non_thesis = EXCLUDED.thesis_or_non_thesis,
        credits = EXCLUDED.credits,
        duration_value = EXCLUDED.duration_value,
        duration_unit = EXCLUDED.duration_unit,
        primary_language_id = EXCLUDED.primary_language_id,
        delivery_mode = EXCLUDED.delivery_mode,
        program_description = EXCLUDED.program_description,
        official_program_url = EXCLUDED.official_program_url,
        source_id = EXCLUDED.source_id,
        notes = EXCLUDED.notes,
        updated_at = NOW();

    CREATE TEMP TABLE lgu_program_map AS
    SELECT gp.id AS db_program_id, seed.program_key, seed.source_ids
    FROM graduate_program gp
    JOIN lgu_program_seed seed
      ON seed.program_key = gp.program_key
    WHERE gp.university_id = v_university_id;

    INSERT INTO graduate_program_source (
        university_id,
        program_id,
        source_id,
        source_role,
        source_order,
        evidence_text,
        notes
    )
    SELECT
        v_university_id,
        pm.db_program_id,
        sm.db_source_id,
        CASE
            WHEN src.ord = 1 THEN 'PRIMARY'
            WHEN src.ord = 2 THEN 'FACULTY'
            ELSE 'SECONDARY'
        END,
        src.ord::INTEGER,
        seed.title || ' source link',
        'Imported from finalized LGU graduate dataset.'
    FROM lgu_program_seed seed
    JOIN lgu_program_map pm
      ON pm.program_key = seed.program_key
    JOIN LATERAL unnest(seed.source_ids) WITH ORDINALITY AS src(source_seed_id, ord) ON TRUE
    JOIN lgu_source_map sm
      ON sm.source_id = src.source_seed_id
    ON CONFLICT (program_id, source_id, source_role) DO UPDATE SET
        source_order = EXCLUDED.source_order,
        evidence_text = EXCLUDED.evidence_text,
        notes = EXCLUDED.notes,
        updated_at = NOW();

    CREATE TEMP TABLE lgu_admission_requirement_seed (
        record_key TEXT PRIMARY KEY,
        requirement_type TEXT NOT NULL,
        requirement_text TEXT NOT NULL,
        comparison_operator TEXT,
        threshold_value NUMERIC(12, 2),
        threshold_unit TEXT,
        notes TEXT,
        source_id TEXT NOT NULL
    ) ON COMMIT DROP;

    INSERT INTO lgu_admission_requirement_seed (
        record_key, requirement_type, requirement_text, comparison_operator, threshold_value, threshold_unit, notes, source_id
    )
    VALUES
        ('lgu-req-bachelors-degree', 'ACADEMIC', 'Bachelor''s degree from an accredited institution.', NULL, NULL, NULL, 'Graduate studies baseline requirement.', 'LGU-SRC-003'),
        ('lgu-req-min-gpa', 'ACADEMIC', 'Minimum GPA of 3.0 or equivalent.', '>=', 3.00, 'GPA', 'Graduate studies baseline requirement.', 'LGU-SRC-003'),
        ('lgu-req-letters-recommendation', 'GENERAL', 'Letters of recommendation.', NULL, NULL, NULL, 'Graduate studies baseline requirement.', 'LGU-SRC-003'),
        ('lgu-req-statement-purpose', 'GENERAL', 'Statement of purpose.', NULL, NULL, NULL, 'Graduate studies baseline requirement.', 'LGU-SRC-003'),
        ('lgu-req-gre', 'GRE', 'GRE may be required depending on the program.', NULL, NULL, NULL, 'Program-dependent graduate entrance requirement.', 'LGU-SRC-003'),
        ('lgu-req-gmat', 'GMAT', 'GMAT may be required depending on the program.', NULL, NULL, NULL, 'Program-dependent graduate entrance requirement.', 'LGU-SRC-003'),
        ('lgu-req-interview', 'INTERVIEW', 'Interview if required.', NULL, NULL, NULL, 'Program-dependent graduate interview step.', 'LGU-SRC-003');

    INSERT INTO graduate_admission_requirement (
        university_id,
        faculty_id,
        department_id,
        program_id,
        scope_level,
        record_key,
        requirement_type,
        requirement_text,
        comparison_operator,
        threshold_value,
        threshold_unit,
        is_required,
        notes,
        source_id
    )
    SELECT
        v_university_id,
        NULL,
        NULL,
        NULL,
        'UNIVERSITY',
        seed.record_key,
        seed.requirement_type,
        seed.requirement_text,
        seed.comparison_operator,
        seed.threshold_value,
        seed.threshold_unit,
        TRUE,
        seed.notes,
        sm.db_source_id
    FROM lgu_admission_requirement_seed seed
    JOIN lgu_source_map sm
      ON sm.source_id = seed.source_id
    ON CONFLICT (university_id, record_key) DO UPDATE SET
        requirement_type = EXCLUDED.requirement_type,
        requirement_text = EXCLUDED.requirement_text,
        comparison_operator = EXCLUDED.comparison_operator,
        threshold_value = EXCLUDED.threshold_value,
        threshold_unit = EXCLUDED.threshold_unit,
        is_required = EXCLUDED.is_required,
        notes = EXCLUDED.notes,
        source_id = EXCLUDED.source_id,
        updated_at = NOW();

    CREATE TEMP TABLE lgu_required_document_seed (
        record_key TEXT PRIMARY KEY,
        document_type TEXT NOT NULL,
        document_name TEXT NOT NULL,
        is_optional BOOLEAN NOT NULL,
        sort_order INTEGER,
        notes TEXT,
        source_id TEXT NOT NULL
    ) ON COMMIT DROP;

    INSERT INTO lgu_required_document_seed (
        record_key, document_type, document_name, is_optional, sort_order, notes, source_id
    )
    VALUES
        ('lgu-doc-application', 'APPLICATION', 'Completed graduate application.', FALSE, 1, 'Central graduate application document listed in the shared data.', 'LGU-SRC-014'),
        ('lgu-doc-equivalency', 'ACADEMIC', 'Bachelor''s degree equivalency or Ministry evaluation where applicable.', FALSE, 2, 'Graduate admissions and aid documents.', 'LGU-SRC-014'),
        ('lgu-doc-transcript', 'ACADEMIC', 'Official transcript.', FALSE, 3, 'Graduate admissions and aid documents.', 'LGU-SRC-014'),
        ('lgu-doc-employment', 'SUPPORTING', 'Employment or income-related documents when applying for aid.', FALSE, 4, 'Financial-aid supporting documents.', 'LGU-SRC-014'),
        ('lgu-doc-cv', 'SUPPORTING', 'Updated CV or resume.', FALSE, 5, 'Financial-aid supporting documents.', 'LGU-SRC-015'),
        ('lgu-doc-recommendations', 'REFERENCE', 'Two recommendation letters.', FALSE, 6, 'Financial-aid and admissions support documents.', 'LGU-SRC-015'),
        ('lgu-doc-id-passport', 'IDENTIFICATION', 'Copy of ID or passport where applicable.', FALSE, 7, 'Financial-aid and admissions support documents.', 'LGU-SRC-015');

    INSERT INTO graduate_required_document (
        university_id,
        faculty_id,
        department_id,
        program_id,
        scope_level,
        record_key,
        document_type,
        document_name,
        is_optional,
        sort_order,
        notes,
        source_id
    )
    SELECT
        v_university_id,
        NULL,
        NULL,
        NULL,
        'UNIVERSITY',
        seed.record_key,
        seed.document_type,
        seed.document_name,
        seed.is_optional,
        seed.sort_order,
        seed.notes,
        sm.db_source_id
    FROM lgu_required_document_seed seed
    JOIN lgu_source_map sm
      ON sm.source_id = seed.source_id
    ON CONFLICT (university_id, record_key) DO UPDATE SET
        document_type = EXCLUDED.document_type,
        document_name = EXCLUDED.document_name,
        is_optional = EXCLUDED.is_optional,
        sort_order = EXCLUDED.sort_order,
        notes = EXCLUDED.notes,
        source_id = EXCLUDED.source_id,
        updated_at = NOW();

    CREATE TEMP TABLE lgu_scholarship_seed (
        record_key TEXT PRIMARY KEY,
        name TEXT NOT NULL,
        description TEXT,
        coverage TEXT,
        notes TEXT,
        source_id TEXT NOT NULL
    ) ON COMMIT DROP;

    INSERT INTO lgu_scholarship_seed (record_key, name, description, coverage, notes, source_id)
    VALUES
        ('lgu-scholarship-early-merit', 'Early Merit Admission Aid', 'Early merit admission aid referenced by LGU financial-aid materials.', 'Merit aid', 'Scholarship label published on the financial-aid page and forms.', 'LGU-SRC-007'),
        ('lgu-scholarship-athletic', 'Athletic Scholarships', 'Athletic scholarships referenced by LGU financial-aid materials.', 'Athletic support', 'Scholarship label published on the financial-aid page and forms.', 'LGU-SRC-015'),
        ('lgu-scholarship-work-study', 'Work-study Grant', 'Work-study grant referenced by LGU financial-aid materials.', 'Work-study support', 'Scholarship label published on the financial-aid page and forms.', 'LGU-SRC-014');

    INSERT INTO graduate_scholarship (
        university_id,
        faculty_id,
        department_id,
        program_id,
        scope_level,
        record_key,
        academic_year,
        name,
        description,
        coverage,
        amount,
        currency,
        notes,
        source_id
    )
    SELECT
        v_university_id,
        NULL,
        NULL,
        NULL,
        'UNIVERSITY',
        seed.record_key,
        NULL,
        seed.name,
        seed.description,
        seed.coverage,
        NULL,
        NULL,
        seed.notes,
        sm.db_source_id
    FROM lgu_scholarship_seed seed
    JOIN lgu_source_map sm
      ON sm.source_id = seed.source_id
    ON CONFLICT (university_id, record_key) DO UPDATE SET
        academic_year = EXCLUDED.academic_year,
        name = EXCLUDED.name,
        description = EXCLUDED.description,
        coverage = EXCLUDED.coverage,
        amount = EXCLUDED.amount,
        currency = EXCLUDED.currency,
        notes = EXCLUDED.notes,
        source_id = EXCLUDED.source_id,
        updated_at = NOW();

    CREATE TEMP TABLE lgu_financial_aid_seed (
        record_key TEXT PRIMARY KEY,
        name TEXT NOT NULL,
        description TEXT,
        notes TEXT,
        source_id TEXT NOT NULL
    ) ON COMMIT DROP;

    INSERT INTO lgu_financial_aid_seed (record_key, name, description, notes, source_id)
    VALUES
        ('lgu-aid-application-process', 'Financial-aid application process', 'LGU publishes a financial-aid application process and related forms.', 'Financial-aid process reference from the official site and forms.', 'LGU-SRC-007'),
        ('lgu-aid-supporting-docs', 'Aid-related supporting documents', 'LGU publishes aid-related supporting documents and form instructions.', 'Aid document guidance from the official forms.', 'LGU-SRC-014'),
        ('lgu-aid-flexible-payment', 'Flexible payment references', 'LGU references flexible payment arrangements in financial-aid materials.', 'Payment-reference item from the official forms.', 'LGU-SRC-015');

    INSERT INTO graduate_financial_aid (
        university_id,
        faculty_id,
        department_id,
        program_id,
        scope_level,
        record_key,
        academic_year,
        name,
        description,
        amount,
        currency,
        notes,
        source_id
    )
    SELECT
        v_university_id,
        NULL,
        NULL,
        NULL,
        'UNIVERSITY',
        seed.record_key,
        NULL,
        seed.name,
        seed.description,
        NULL,
        NULL,
        seed.notes,
        sm.db_source_id
    FROM lgu_financial_aid_seed seed
    JOIN lgu_source_map sm
      ON sm.source_id = seed.source_id
    ON CONFLICT (university_id, record_key) DO UPDATE SET
        academic_year = EXCLUDED.academic_year,
        name = EXCLUDED.name,
        description = EXCLUDED.description,
        amount = EXCLUDED.amount,
        currency = EXCLUDED.currency,
        notes = EXCLUDED.notes,
        source_id = EXCLUDED.source_id,
        updated_at = NOW();

    CREATE TEMP TABLE lgu_payment_plan_seed (
        record_key TEXT PRIMARY KEY,
        name TEXT NOT NULL,
        description TEXT,
        notes TEXT,
        source_id TEXT NOT NULL
    ) ON COMMIT DROP;

    INSERT INTO lgu_payment_plan_seed (record_key, name, description, notes, source_id)
    VALUES
        ('lgu-payment-flexible', 'Flexible payment plan references', 'LGU references flexible or installment payment arrangements in the official material.', 'Shared payment-plan reference from the official site and forms.', 'LGU-SRC-007'),
        ('lgu-payment-monthly', 'Monthly payment references', 'LGU references monthly payment arrangements in the official forms.', 'Shared payment-plan reference from the official forms.', 'LGU-SRC-014');

    INSERT INTO graduate_payment_plan (
        university_id,
        faculty_id,
        department_id,
        program_id,
        scope_level,
        record_key,
        academic_year,
        name,
        description,
        installments_count,
        down_payment_amount,
        down_payment_currency,
        interval_label,
        notes,
        source_id
    )
    SELECT
        v_university_id,
        NULL,
        NULL,
        NULL,
        'UNIVERSITY',
        seed.record_key,
        NULL,
        seed.name,
        seed.description,
        NULL,
        NULL,
        NULL,
        NULL,
        seed.notes,
        sm.db_source_id
    FROM lgu_payment_plan_seed seed
    JOIN lgu_source_map sm
      ON sm.source_id = seed.source_id
    ON CONFLICT (university_id, record_key) DO UPDATE SET
        academic_year = EXCLUDED.academic_year,
        name = EXCLUDED.name,
        description = EXCLUDED.description,
        installments_count = EXCLUDED.installments_count,
        down_payment_amount = EXCLUDED.down_payment_amount,
        down_payment_currency = EXCLUDED.down_payment_currency,
        interval_label = EXCLUDED.interval_label,
        notes = EXCLUDED.notes,
        source_id = EXCLUDED.source_id,
        updated_at = NOW();

END $$;
