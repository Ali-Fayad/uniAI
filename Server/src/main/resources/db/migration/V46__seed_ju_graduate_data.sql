-- Jinan University graduate data seed migration.
-- Idempotent import for the finalized JU graduate dataset.

DO $$
DECLARE
    v_university_id BIGINT;
BEGIN
    INSERT INTO university (name, name_ar, acronym, country, city, latitude, longitude, campus_name, campus_type)
    SELECT 'Jinan University', NULL, 'JU', 'Lebanon', NULL, NULL, NULL, NULL, NULL
    WHERE NOT EXISTS (
        SELECT 1
        FROM university
        WHERE name = 'Jinan University'
    );

    SELECT id
    INTO v_university_id
    FROM university
    WHERE name = 'Jinan University'
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
        ('Arabic', 'ar', 'العربية'),
        ('English', 'en', 'English'),
        ('French', 'fr', 'Français')
    ON CONFLICT (code) DO UPDATE SET
        name = EXCLUDED.name,
        native_name = EXCLUDED.native_name;

    CREATE TEMP TABLE ju_source_seed (
        source_id TEXT PRIMARY KEY,
        title TEXT NOT NULL,
        url TEXT NOT NULL,
        source_type TEXT NOT NULL,
        accessed_at DATE NOT NULL,
        notes TEXT
    ) ON COMMIT DROP;

    INSERT INTO ju_source_seed (source_id, title, url, source_type, accessed_at, notes)
    VALUES
        ('JU_SRC_001', 'Jinan University - Home', 'https://www.jinan.edu.lb/', 'official_webpage', '2026-07-05', 'Official JU homepage and site navigation.'),
        ('JU_SRC_002', 'Majors & Programs | Jinan University', 'https://www.jinan.edu.lb/pages/en/majors-programs', 'official_webpage', '2026-07-05', 'Primary official listing of postgraduate Master''s and Ph.D. programs and credits.'),
        ('JU_SRC_003', 'Graduate | Jinan University', 'https://www.jinan.edu.lb/pages/en/graduate', 'official_webpage', '2026-07-05', 'Graduate admissions and postgraduate studies overview.'),
        ('JU_SRC_004', 'Entrance Registration Form | Jinan University', 'https://www.jinan.edu.lb/pages/en/entrance-registration', 'official_webpage', '2026-07-05', 'Official application page with graduate applicant text and required documents.'),
        ('JU_SRC_005', 'Graduate Academic Resources | Jinan University', 'https://www.jinan.edu.lb/pages/recruit-apply/graduate-academ', 'official_webpage', '2026-07-05', 'Graduate research guide / thesis-dissertation submission guidance.'),
        ('JU_SRC_006', 'Tuition & Fees | Jinan University', 'https://www.jinan.edu.lb/pages/about-corporation/tuition-and-fees', 'official_webpage', '2026-07-05', 'General tuition/fees methodology and payment channels.'),
        ('JU_SRC_007', 'Financial System | Jinan University', 'https://www.jinan.edu.lb/pages/en/financial-system', 'official_webpage', '2026-07-05', '2026-2027 payment system and installment deadlines.'),
        ('JU_SRC_008', 'Financial Aid | Jinan University', 'https://www.jinan.edu.lb/pages/en/financial-aid', 'official_webpage', '2026-07-05', 'Scholarship / financial-aid rules in Arabic.'),
        ('JU_SRC_009', 'Academic Calendar | Jinan University', 'https://www.jinan.edu.lb/pages/en/academic-calendar', 'official_webpage', '2026-07-05', 'Official academic calendar page.'),
        ('JU_SRC_010', 'Student Calendar PDF | Jinan University', 'https://www.jinan.edu.lb/pages/en/calendar-pdf', 'official_webpage', '2026-07-05', 'Official page linking student calendar PDFs.'),
        ('JU_SRC_011', 'Foreign Students | Jinan University', 'https://www.jinan.edu.lb/pages/en/foreign-students', 'official_webpage', '2026-07-05', 'International/foreign student fee and language-fee information.'),
        ('JU_SRC_012', 'Policies & Procedures | Jinan University', 'https://www.jinan.edu.lb/pages/en/policies-procedures', 'official_webpage', '2026-07-05', 'General academic policies page.');

    INSERT INTO source (university_id, title, url, source_type, accessed_at, notes)
    SELECT v_university_id, title, url, source_type, accessed_at, notes
    FROM ju_source_seed
    ON CONFLICT (university_id, url) DO UPDATE SET
        title = EXCLUDED.title,
        source_type = EXCLUDED.source_type,
        accessed_at = EXCLUDED.accessed_at,
        notes = EXCLUDED.notes,
        updated_at = NOW();

    CREATE TEMP TABLE ju_source_map AS
    SELECT ss.source_id, s.id AS db_source_id, s.url
    FROM ju_source_seed ss
    JOIN source s
      ON s.university_id = v_university_id
     AND s.url = ss.url;

    CREATE TEMP TABLE ju_faculty_seed (
        name TEXT PRIMARY KEY,
        short_name TEXT,
        faculty_type TEXT NOT NULL,
        official_url TEXT,
        notes TEXT
    ) ON COMMIT DROP;

    INSERT INTO ju_faculty_seed (name, short_name, faculty_type, official_url, notes)
    VALUES
        ('Faculty of Literature and Humanities', NULL, 'FACULTY', NULL, 'Official graduate listing source'),
        ('Faculty of Business Administration', NULL, 'FACULTY', NULL, 'Official graduate listing source'),
        ('Faculty of Communication', NULL, 'FACULTY', NULL, 'Official graduate listing source'),
        ('Faculty of Public Health', NULL, 'FACULTY', NULL, 'Official graduate listing source'),
        ('Faculty of Education', NULL, 'FACULTY', NULL, 'Official graduate listing source'),
        ('Political Science Institute', NULL, 'INSTITUTE', NULL, 'Official graduate listing source'),
        ('The Faculty of Shariaa & Islamic Studies', NULL, 'FACULTY', NULL, 'Official graduate listing source');

    INSERT INTO university_faculty (university_id, name, short_name, faculty_type, official_url, notes)
    SELECT v_university_id, name, short_name, faculty_type, official_url, notes
    FROM ju_faculty_seed
    ON CONFLICT (university_id, name) DO UPDATE SET
        short_name = EXCLUDED.short_name,
        faculty_type = EXCLUDED.faculty_type,
        official_url = EXCLUDED.official_url,
        notes = EXCLUDED.notes,
        updated_at = NOW();

    CREATE TEMP TABLE ju_program_seed (
        program_key TEXT PRIMARY KEY,
        title TEXT NOT NULL,
        faculty_name TEXT NOT NULL,
        degree_type TEXT NOT NULL,
        credits INTEGER NOT NULL,
        official_program_url TEXT,
        notes TEXT NOT NULL,
        source_id TEXT NOT NULL
    ) ON COMMIT DROP;

    INSERT INTO ju_program_seed (program_key, title, faculty_name, degree_type, credits, official_program_url, notes, source_id)
    VALUES
        ('translation-and-languages', 'Translation and Languages', 'Faculty of Literature and Humanities', 'MASTER', 42, NULL, 'Official Majors & Programs listing; Master''s Degree - 42 Credits.', 'JU_SRC_002'),
        ('arabic-language', 'Arabic Language', 'Faculty of Literature and Humanities', 'MASTER', 42, NULL, 'Official Majors & Programs listing; Master''s Degree - 42 Credits.', 'JU_SRC_002'),
        ('management', 'Management', 'Faculty of Business Administration', 'MASTER', 42, NULL, 'Official Majors & Programs listing; Master''s Degree - 42 Credits.', 'JU_SRC_002'),
        ('marketing', 'Marketing', 'Faculty of Business Administration', 'MASTER', 42, NULL, 'Official Majors & Programs listing; Master''s Degree - 42 Credits.', 'JU_SRC_002'),
        ('accounting', 'Accounting', 'Faculty of Business Administration', 'MASTER', 42, NULL, 'Official Majors & Programs listing; Master''s Degree - 42 Credits.', 'JU_SRC_002'),
        ('finance', 'Finance', 'Faculty of Business Administration', 'MASTER', 42, NULL, 'Official Majors & Programs listing; Master''s Degree - 42 Credits.', 'JU_SRC_002'),
        ('business-information-technology', 'Business Information Technology', 'Faculty of Business Administration', 'MASTER', 42, NULL, 'Official Majors & Programs listing; Master''s Degree - 42 Credits.', 'JU_SRC_002'),
        ('project-management', 'Project Management', 'Faculty of Business Administration', 'MASTER', 42, NULL, 'Official Majors & Programs listing; Master''s Degree - 42 Credits.', 'JU_SRC_002'),
        ('radio-television', 'Radio & Television', 'Faculty of Communication', 'MASTER', 42, NULL, 'Official Majors & Programs listing; Master''s Degree - 42 Credits.', 'JU_SRC_002'),
        ('communication-journalism', 'Communication & Journalism', 'Faculty of Communication', 'MASTER', 42, NULL, 'Official Majors & Programs listing; Master''s Degree - 42 Credits.', 'JU_SRC_002'),
        ('control-of-infectious-diseases', 'Control of Infectious Diseases', 'Faculty of Public Health', 'MASTER', 36, NULL, 'Official Majors & Programs listing; Masters in Public Health - 36 Credits.', 'JU_SRC_002'),
        ('health-education', 'Health Education', 'Faculty of Public Health', 'MASTER', 36, NULL, 'Official Majors & Programs listing; Masters in Public Health - 36 Credits.', 'JU_SRC_002'),
        ('public-health-genetics', 'Public Health Genetics', 'Faculty of Public Health', 'MASTER', 36, NULL, 'Official Majors & Programs listing; Masters in Public Health - 36 Credits.', 'JU_SRC_002'),
        ('management-and-educational-planning', 'Management and Educational Planning', 'Faculty of Education', 'MASTER', 42, NULL, 'Official Majors & Programs listing; Master''s Degree - 42 Credits.', 'JU_SRC_002'),
        ('curriculum-and-teaching-methods', 'Curriculum and Teaching Methods', 'Faculty of Education', 'MASTER', 42, NULL, 'Official Majors & Programs listing; Master''s Degree - 42 Credits.', 'JU_SRC_002'),
        ('human-rights', 'Human Rights', 'Political Science Institute', 'MASTER', 36, NULL, 'Official Majors & Programs listing; Master''s Degree - 36 Credits.', 'JU_SRC_002'),
        ('shariaa-and-islamic-studies', 'Shariaa and Islamic Studies', 'The Faculty of Shariaa & Islamic Studies', 'MASTER', 42, NULL, 'Official Majors & Programs listing; Master''s Degree - 42 Credits.', 'JU_SRC_002'),
        ('arabic-language-phd', 'Arabic Language', 'Faculty of Literature and Humanities', 'PHD', 54, NULL, 'Official Majors & Programs listing; Ph.D. Degree - 54 Credits.', 'JU_SRC_002'),
        ('philosophy-of-business-administration-phd', 'Philosophy of Business Administration', 'Faculty of Business Administration', 'PHD', 54, NULL, 'Official Majors & Programs listing; Ph.D. Degree - 54 Credits.', 'JU_SRC_002'),
        ('shariaa-and-islamic-studies-phd', 'Shariaa and Islamic Studies', 'The Faculty of Shariaa & Islamic Studies', 'PHD', 54, NULL, 'Official Majors & Programs listing; Ph.D. Degree - 54 Credits.', 'JU_SRC_002');

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
        seed.credits,
        NULL,
        NULL,
        NULL,
        NULL,
        NULL,
        seed.official_program_url,
        sm.db_source_id,
        seed.notes
    FROM ju_program_seed seed
    JOIN ju_faculty_seed fs
      ON fs.name = seed.faculty_name
    JOIN university_faculty f
      ON f.university_id = v_university_id
     AND f.name = fs.name
    JOIN degree_type dt
      ON dt.code = seed.degree_type
    JOIN ju_source_map sm
      ON sm.source_id = seed.source_id
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

    CREATE TEMP TABLE ju_program_map AS
    SELECT gp.id AS db_program_id, seed.program_key
    FROM graduate_program gp
    JOIN ju_program_seed seed
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
        'PRIMARY',
        1,
        'Official Majors & Programs page listing the graduate program.',
        'Imported from finalized JU graduate dataset.'
    FROM ju_program_map pm
    JOIN ju_program_seed seed
      ON seed.program_key = pm.program_key
    JOIN ju_source_map sm
      ON sm.source_id = seed.source_id
    ON CONFLICT (program_id, source_id, source_role) DO UPDATE SET
        source_order = EXCLUDED.source_order,
        evidence_text = EXCLUDED.evidence_text,
        notes = EXCLUDED.notes,
        updated_at = NOW();

    CREATE TEMP TABLE ju_admission_requirement_seed (
        record_key TEXT PRIMARY KEY,
        requirement_type TEXT NOT NULL,
        requirement_text TEXT NOT NULL,
        comparison_operator TEXT,
        threshold_value NUMERIC(12, 2),
        threshold_unit TEXT,
        is_required BOOLEAN NOT NULL,
        notes TEXT,
        source_id TEXT NOT NULL
    ) ON COMMIT DROP;

    INSERT INTO ju_admission_requirement_seed (record_key, requirement_type, requirement_text, comparison_operator, threshold_value, threshold_unit, is_required, notes, source_id)
    VALUES
        ('ju-admission-general-degree', 'GENERAL', 'Applicants must hold a bachelor''s degree or compatible university degree from an accredited university or higher institute.', NULL, NULL, NULL, TRUE, 'Official graduate admission text from the entrance-registration page.', 'JU_SRC_004'),
        ('ju-admission-complete-file', 'GENERAL', 'Applicants must complete the required documents and pay the required registration fees by the deadline.', NULL, NULL, NULL, TRUE, 'Official graduate admission text from the entrance-registration page.', 'JU_SRC_004');

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
        seed.is_required,
        seed.notes,
        sm.db_source_id
    FROM ju_admission_requirement_seed seed
    JOIN ju_source_map sm
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

    CREATE TEMP TABLE ju_required_document_seed (
        record_key TEXT PRIMARY KEY,
        document_type TEXT NOT NULL,
        document_name TEXT NOT NULL,
        is_optional BOOLEAN NOT NULL,
        sort_order INTEGER,
        notes TEXT,
        source_id TEXT NOT NULL
    ) ON COMMIT DROP;

    INSERT INTO ju_required_document_seed (record_key, document_type, document_name, is_optional, sort_order, notes, source_id)
    VALUES
        ('ju-doc-id-passport', 'IDENTIFICATION', 'Certified identification card or passport copy', FALSE, 1, 'Graduate admission document from the entrance-registration page.', 'JU_SRC_004'),
        ('ju-doc-high-school-diploma', 'ACADEMIC', 'Certified high school diploma', FALSE, 2, 'Graduate admission document from the entrance-registration page.', 'JU_SRC_004'),
        ('ju-doc-high-school-transcript', 'ACADEMIC', 'Certified high school transcript for three years', FALSE, 3, 'Graduate admission document from the entrance-registration page.', 'JU_SRC_004'),
        ('ju-doc-bachelor-degree', 'ACADEMIC', 'Certified bachelor''s degree or equivalent', FALSE, 4, 'Graduate admission document from the entrance-registration page.', 'JU_SRC_004'),
        ('ju-doc-bachelor-transcripts', 'ACADEMIC', 'Official transcripts for the bachelor''s degree or equivalent', FALSE, 5, 'Graduate admission document from the entrance-registration page.', 'JU_SRC_004'),
        ('ju-doc-photos', 'IDENTIFICATION', 'Two personal photographs', FALSE, 6, 'Graduate admission document from the entrance-registration page.', 'JU_SRC_004'),
        ('ju-doc-completed-application', 'APPLICATION', 'Completed application and payment of the required registration fees', FALSE, 7, 'Graduate admission document from the entrance-registration page.', 'JU_SRC_004');

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
    FROM ju_required_document_seed seed
    JOIN ju_source_map sm
      ON sm.source_id = seed.source_id
    ON CONFLICT (university_id, record_key) DO UPDATE SET
        document_type = EXCLUDED.document_type,
        document_name = EXCLUDED.document_name,
        is_optional = EXCLUDED.is_optional,
        sort_order = EXCLUDED.sort_order,
        notes = EXCLUDED.notes,
        source_id = EXCLUDED.source_id,
        updated_at = NOW();

    CREATE TEMP TABLE ju_fee_seed (
        record_key TEXT PRIMARY KEY,
        fee_name TEXT NOT NULL,
        academic_year TEXT,
        billing_basis TEXT NOT NULL,
        currency TEXT,
        amount NUMERIC(12, 2),
        category TEXT,
        notes TEXT,
        source_id TEXT NOT NULL
    ) ON COMMIT DROP;

    INSERT INTO ju_fee_seed (record_key, fee_name, academic_year, billing_basis, currency, amount, category, notes, source_id)
    VALUES
        ('ju-fee-application', 'Graduate Application Fee', NULL, 'FLAT_FEE', 'USD', NULL, 'Graduate Fees', 'Application fees are referenced on the official admission page, but no numeric graduate amount was exposed in the recovered extraction.', 'JU_SRC_004'),
        ('ju-fee-registration', 'Graduate Registration Fee', NULL, 'FLAT_FEE', 'USD', NULL, 'Graduate Fees', 'Registration and first-payment steps are referenced, but no standalone graduate registration amount was exposed in the recovered extraction.', 'JU_SRC_006'),
        ('ju-fee-foreign-language', 'Remedial Foreign-Language Fee', NULL, 'FLAT_FEE', 'USD', NULL, 'Foreign Students', 'Foreign-language remedial fees are published on the foreign-students page, but the recovered extraction did not expose a single safe numeric amount.', 'JU_SRC_011');

    INSERT INTO graduate_fee_item (
        university_id,
        faculty_id,
        department_id,
        program_id,
        scope_level,
        record_key,
        academic_year,
        fee_name,
        billing_basis,
        currency,
        amount,
        category,
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
        seed.academic_year,
        seed.fee_name,
        seed.billing_basis,
        seed.currency,
        seed.amount,
        seed.category,
        seed.notes,
        sm.db_source_id
    FROM ju_fee_seed seed
    JOIN ju_source_map sm
      ON sm.source_id = seed.source_id
    ON CONFLICT (university_id, record_key) DO UPDATE SET
        academic_year = EXCLUDED.academic_year,
        fee_name = EXCLUDED.fee_name,
        billing_basis = EXCLUDED.billing_basis,
        currency = EXCLUDED.currency,
        amount = EXCLUDED.amount,
        category = EXCLUDED.category,
        notes = EXCLUDED.notes,
        source_id = EXCLUDED.source_id,
        updated_at = NOW();

    CREATE TEMP TABLE ju_deadline_seed (
        record_key TEXT PRIMARY KEY,
        academic_year TEXT,
        deadline_type TEXT NOT NULL,
        term TEXT,
        deadline_date DATE,
        note TEXT,
        source_id TEXT NOT NULL
    ) ON COMMIT DROP;

    INSERT INTO ju_deadline_seed (record_key, academic_year, deadline_type, term, deadline_date, note, source_id)
    VALUES
        ('ju-deadline-fall-cycle', NULL, 'OTHER', 'Fall graduate cycle', NULL, 'The official academic calendar page publishes registration periods and examination schedules, but the extracted source did not expose a machine-safe exact deadline date.', 'JU_SRC_009'),
        ('ju-deadline-spring-cycle', NULL, 'OTHER', 'Spring graduate cycle', NULL, 'The student calendar PDF page lists term calendar options, but the extracted page did not expose a machine-safe exact deadline date.', 'JU_SRC_010');

    INSERT INTO graduate_admission_deadline (
        university_id,
        faculty_id,
        department_id,
        program_id,
        scope_level,
        record_key,
        academic_year,
        deadline_type,
        term,
        deadline_date,
        note,
        source_id
    )
    SELECT
        v_university_id,
        NULL,
        NULL,
        NULL,
        'UNIVERSITY',
        seed.record_key,
        seed.academic_year,
        seed.deadline_type,
        seed.term,
        seed.deadline_date,
        seed.note,
        sm.db_source_id
    FROM ju_deadline_seed seed
    JOIN ju_source_map sm
      ON sm.source_id = seed.source_id
    ON CONFLICT (university_id, record_key) DO UPDATE SET
        academic_year = EXCLUDED.academic_year,
        deadline_type = EXCLUDED.deadline_type,
        term = EXCLUDED.term,
        deadline_date = EXCLUDED.deadline_date,
        note = EXCLUDED.note,
        source_id = EXCLUDED.source_id,
        updated_at = NOW();

    CREATE TEMP TABLE ju_scholarship_seed (
        record_key TEXT PRIMARY KEY,
        academic_year TEXT,
        name TEXT NOT NULL,
        description TEXT,
        coverage TEXT,
        amount NUMERIC(12, 2),
        currency TEXT,
        notes TEXT,
        source_id TEXT NOT NULL
    ) ON COMMIT DROP;

    INSERT INTO ju_scholarship_seed (record_key, academic_year, name, description, coverage, amount, currency, notes, source_id)
    VALUES
        ('ju-scholarship-general', NULL, 'JU Scholarship Policy', 'Scholarships are allocated to different categories of enrolled students and may cover part or all of the prescribed stage.', 'Stage-based scholarship support', NULL, NULL, 'Scholarship policy summarized from the official financial-aid page.', 'JU_SRC_008');

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
        seed.academic_year,
        seed.name,
        seed.description,
        seed.coverage,
        seed.amount,
        seed.currency,
        seed.notes,
        sm.db_source_id
    FROM ju_scholarship_seed seed
    JOIN ju_source_map sm
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

    CREATE TEMP TABLE ju_financial_aid_seed (
        record_key TEXT PRIMARY KEY,
        academic_year TEXT,
        name TEXT NOT NULL,
        description TEXT,
        amount NUMERIC(12, 2),
        currency TEXT,
        notes TEXT,
        source_id TEXT NOT NULL
    ) ON COMMIT DROP;

    INSERT INTO ju_financial_aid_seed (record_key, academic_year, name, description, amount, currency, notes, source_id)
    VALUES
        ('ju-aid-general', NULL, 'JU Financial Aid', 'Scholarship percentages apply only to study-unit value and do not include semester or foreign-language fees.', NULL, NULL, 'Financial-aid policy summarized from the official financial-aid page.', 'JU_SRC_008');

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
        seed.academic_year,
        seed.name,
        seed.description,
        seed.amount,
        seed.currency,
        seed.notes,
        sm.db_source_id
    FROM ju_financial_aid_seed seed
    JOIN ju_source_map sm
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

    CREATE TEMP TABLE ju_payment_plan_seed (
        record_key TEXT PRIMARY KEY,
        academic_year TEXT,
        name TEXT NOT NULL,
        description TEXT,
        installments_count INTEGER,
        down_payment_amount NUMERIC(12, 2),
        down_payment_currency TEXT,
        interval_label TEXT,
        notes TEXT,
        source_id TEXT NOT NULL
    ) ON COMMIT DROP;

    INSERT INTO ju_payment_plan_seed (record_key, academic_year, name, description, installments_count, down_payment_amount, down_payment_currency, interval_label, notes, source_id)
    VALUES
        ('ju-payment-plan-2026-2027', '2026-2027', 'Graduate installment payment plan', 'JU publishes first-payment amounts and four installment deadlines for the 2026-2027 academic cycle.', 4, NULL, NULL, 'Monthly installment deadlines', 'Payment-plan summary from the financial-system page.', 'JU_SRC_007');

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
        seed.academic_year,
        seed.name,
        seed.description,
        seed.installments_count,
        seed.down_payment_amount,
        seed.down_payment_currency,
        seed.interval_label,
        seed.notes,
        sm.db_source_id
    FROM ju_payment_plan_seed seed
    JOIN ju_source_map sm
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
