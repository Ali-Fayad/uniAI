-- GU graduate data seed migration.
-- Idempotent import for the canonical GU graduate dataset.

DO $$
DECLARE
    v_university_id BIGINT;
BEGIN
    INSERT INTO university (name, name_ar, acronym, country, city, latitude, longitude, campus_name, campus_type)
    SELECT 'Global University', NULL, 'GU', 'Lebanon', NULL, NULL, NULL, NULL, NULL
    WHERE NOT EXISTS (
        SELECT 1
        FROM university
        WHERE name = 'Global University'
    );

    SELECT id
    INTO v_university_id
    FROM university
    WHERE name = 'Global University'
    ORDER BY id
    LIMIT 1;

    INSERT INTO degree_type (code, name)
    VALUES
        ('MASTER', 'Master'),
        ('PHD', 'Doctor of Philosophy')
    ON CONFLICT (code) DO UPDATE SET
        name = EXCLUDED.name,
        updated_at = NOW();

    CREATE TEMP TABLE gu_source_seed (
        source_id TEXT PRIMARY KEY,
        title TEXT NOT NULL,
        url TEXT NOT NULL,
        source_type TEXT NOT NULL,
        accessed_at DATE,
        notes TEXT
    ) ON COMMIT DROP;

    INSERT INTO gu_source_seed (source_id, title, url, source_type, accessed_at, notes)
    VALUES
        ('GU-SRC-001', 'Global University Home', 'https://www.gu.edu.lb/', 'WEB', '2026-07-06', 'Official site entry point and navigation to graduate information.'),
        ('GU-SRC-002', 'Academics', 'https://www.gu.edu.lb/academics', 'WEB', '2026-07-06', 'Graduate listing: MBA, Master of Education, and Master of Arabic Literature.'),
        ('GU-SRC-003', 'Graduate Programs / Graduate Admissions', 'https://www.gu.edu.lb/graduate-programs', 'WEB', '2026-07-06', 'Graduate admissions requirements and required documents.'),
        ('GU-SRC-004', 'Application', 'https://www.gu.edu.lb/apply', 'WEB', '2026-07-06', 'Graduate application form and major options.'),
        ('GU-SRC-005', 'Business Department', 'https://www.gu.edu.lb/business-department', 'WEB', '2026-07-06', 'Business Department page with MBA evidence.'),
        ('GU-SRC-006', 'Education Department', 'https://www.gu.edu.lb/education-department', 'WEB', '2026-07-06', 'Education Department page with Master of Education evidence.'),
        ('GU-SRC-007', 'Arabic Language Department', 'https://www.gu.edu.lb/arabic-language-department', 'WEB', '2026-07-06', 'Arabic Language Department page with Master of Arabic Language and Literature evidence.'),
        ('GU-SRC-008', 'Global University Student''s Handbook / University Catalogue 2024-2025', 'https://www.gu.edu.lb/wp-content/uploads/2026/01/catalogue.pdf', 'PDF', '2026-07-06', 'Catalogue evidence for MBA, Islamic Studies tracks, academic policies, fees, and scholarships.'),
        ('GU-SRC-009', 'Master in Business Administration Contract Sheet', 'https://www.gu.edu.lb/wp-content/uploads/2026/01/MBA-contract-sheet.pdf', 'PDF', '2026-07-06', 'MBA course plan and contract sheet.'),
        ('GU-SRC-010', 'Financial Policies', 'https://www.gu.edu.lb/financial-policies', 'WEB', '2026-07-06', '2025-2026 tuition, fees, payment schedule, aid, and refund rules.'),
        ('GU-SRC-011', 'Academic Calendar', 'https://www.gu.edu.lb/calendar', 'WEB', '2026-07-06', '2026-2027 academic calendar.'),
        ('GU-SRC-012', 'Accreditation & Equivalence', 'https://www.gu.edu.lb/accreditation-and-equivalence', 'WEB', '2026-07-06', 'Accreditation and equivalence context for graduate degree names.');

    INSERT INTO source (university_id, title, url, source_type, accessed_at, notes)
    SELECT v_university_id, title, url, source_type, accessed_at, notes
    FROM gu_source_seed
    ON CONFLICT (university_id, url) DO UPDATE SET
        title = EXCLUDED.title,
        source_type = EXCLUDED.source_type,
        accessed_at = EXCLUDED.accessed_at,
        notes = EXCLUDED.notes,
        updated_at = NOW();

    CREATE TEMP TABLE gu_source_map AS
    SELECT ss.source_id, s.id AS db_source_id, s.url
    FROM gu_source_seed ss
    JOIN source s
      ON s.university_id = v_university_id
     AND s.url = ss.url;

    INSERT INTO university_faculty (university_id, name, short_name, faculty_type, official_url, notes)
    VALUES
        (v_university_id, 'Faculty of Administrative Sciences', NULL, 'FACULTY', NULL, 'Official graduate faculty containing the MBA.'),
        (v_university_id, 'Faculty of Literature and Humanities', NULL, 'FACULTY', NULL, 'Official graduate faculty containing Master of Education and Master of Arabic Language and Literature.')
    ON CONFLICT (university_id, name) DO UPDATE SET
        short_name = EXCLUDED.short_name,
        faculty_type = EXCLUDED.faculty_type,
        official_url = EXCLUDED.official_url,
        notes = EXCLUDED.notes,
        updated_at = NOW();

    INSERT INTO university_department (university_id, faculty_id, name, short_name, official_url, notes)
    SELECT
        v_university_id,
        f.id,
        d.name,
        d.short_name,
        d.official_url,
        d.notes
    FROM (
        VALUES
            ('Faculty of Administrative Sciences', 'Business Department', NULL, 'https://www.gu.edu.lb/business-department', 'Official department page for the MBA.'),
            ('Faculty of Literature and Humanities', 'Education Department', NULL, 'https://www.gu.edu.lb/education-department', 'Official department page for Master of Education.'),
            ('Faculty of Literature and Humanities', 'Arabic Language Department', NULL, 'https://www.gu.edu.lb/arabic-language-department', 'Official department page for Master of Arabic Language and Literature.')
    ) AS d(faculty_name, name, short_name, official_url, notes)
    JOIN university_faculty f
      ON f.university_id = v_university_id
     AND f.name = d.faculty_name
    ON CONFLICT (university_id, faculty_id, name) DO UPDATE SET
        short_name = EXCLUDED.short_name,
        official_url = EXCLUDED.official_url,
        notes = EXCLUDED.notes,
        updated_at = NOW();

    CREATE TEMP TABLE gu_program_seed (
        program_key TEXT PRIMARY KEY,
        title TEXT NOT NULL,
        degree_type TEXT NOT NULL,
        official_degree_name TEXT NOT NULL,
        major_category TEXT,
        major TEXT,
        faculty_name TEXT,
        department_name TEXT,
        thesis_or_non_thesis TEXT,
        credits INTEGER,
        duration_value NUMERIC(10, 2),
        duration_unit TEXT,
        official_program_url TEXT,
        primary_source_id TEXT NOT NULL,
        program_description TEXT NOT NULL,
        notes TEXT NOT NULL
    ) ON COMMIT DROP;

    INSERT INTO gu_program_seed (
        program_key,
        title,
        degree_type,
        official_degree_name,
        major_category,
        major,
        faculty_name,
        department_name,
        thesis_or_non_thesis,
        credits,
        duration_value,
        duration_unit,
        official_program_url,
        primary_source_id,
        program_description,
        notes
    )
    VALUES
        (
            'gu-master-business-administration',
            'Master of Business Administration (MBA)',
            'MASTER',
            'Master of Business Administration',
            'Business',
            'Business Administration',
            'Faculty of Administrative Sciences',
            'Business Department',
            'THESIS_OR_NON_THESIS',
            39,
            NULL,
            NULL,
            'https://www.gu.edu.lb/business-department',
            'GU-SRC-005',
            'Official GU MBA evidence appears on the Academics page, Application page, Business Department page, MBA contract sheet, and Financial Policies page.',
            'The catalogue and contract sheet show a 39-credit MBA plan and separate thesis/project tracks.'
        ),
        (
            'gu-master-education',
            'Master of Education',
            'MASTER',
            'Master of Education',
            'Education',
            'Education',
            'Faculty of Literature and Humanities',
            'Education Department',
            NULL,
            NULL,
            NULL,
            NULL,
            'https://www.gu.edu.lb/education-department',
            'GU-SRC-006',
            'Official GU Master of Education evidence appears on the Academics page, Application page, Education Department page, and Financial Policies page.',
            'The Education Department page explicitly names Master of Education as an advanced program.'
        ),
        (
            'gu-master-arabic-language-literature',
            'Master in Arabic Language and Literature',
            'MASTER',
            'Master in Arabic Language and Literature',
            'Language and Literature',
            'Arabic Language and Literature',
            'Faculty of Literature and Humanities',
            'Arabic Language Department',
            'THESIS',
            30,
            2,
            'YEARS',
            'https://www.gu.edu.lb/arabic-language-department',
            'GU-SRC-007',
            'Official GU Master in Arabic Language and Literature evidence appears on the Academics page, Application page, Arabic Language Department page, and Financial Policies page.',
            'The Arabic Language Department page states the master''s program is two years and 30 credits, including 7 thesis credits.'
        ),
        (
            'gu-master-islamic-studies',
            'Master in Islamic Studies',
            'MASTER',
            'Master in Islamic Studies',
            'Islamic Studies',
            'Islamic Studies',
            NULL,
            NULL,
            NULL,
            NULL,
            NULL,
            NULL,
            NULL,
            'GU-SRC-008',
            'Official GU catalogue evidence lists Master''s in Islamic Studies tracks, and the accreditation page confirms the master''s-level Islamic Studies naming context.',
            'The catalogue lists Islamic Studies as a master''s area with multiple tracks.'
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
        d.id,
        dt.id,
        seed.program_key,
        seed.major_category,
        seed.major,
        seed.official_degree_name,
        seed.thesis_or_non_thesis,
        seed.credits,
        seed.duration_value,
        seed.duration_unit,
        NULL,
        NULL,
        seed.program_description,
        seed.official_program_url,
        sm.db_source_id,
        seed.notes
    FROM gu_program_seed seed
    JOIN degree_type dt
      ON dt.code = seed.degree_type
    LEFT JOIN university_faculty f
      ON f.university_id = v_university_id
     AND f.name = seed.faculty_name
    LEFT JOIN university_department d
      ON d.university_id = v_university_id
     AND d.faculty_id = f.id
     AND d.name = seed.department_name
    JOIN gu_source_map sm
      ON sm.source_id = seed.primary_source_id
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

    CREATE TEMP TABLE gu_program_source_seed (
        program_key TEXT NOT NULL,
        source_id TEXT NOT NULL,
        source_role TEXT NOT NULL,
        source_order INTEGER NOT NULL,
        evidence_text TEXT NOT NULL,
        notes TEXT
    ) ON COMMIT DROP;

    INSERT INTO gu_program_source_seed (program_key, source_id, source_role, source_order, evidence_text, notes)
    VALUES
        ('gu-master-business-administration', 'GU-SRC-005', 'PRIMARY', 1, 'Business Department page naming the MBA and linking the contract sheet.', 'Primary program evidence.'),
        ('gu-master-business-administration', 'GU-SRC-002', 'SECONDARY', 2, 'Academics page listing the MBA under graduate programs.', NULL),
        ('gu-master-business-administration', 'GU-SRC-004', 'ADMISSIONS', 3, 'Application page including Masters of Business Administration as an option.', NULL),
        ('gu-master-business-administration', 'GU-SRC-008', 'CATALOG', 4, 'Catalogue listing MBA and MBA with thesis/project.', NULL),
        ('gu-master-business-administration', 'GU-SRC-009', 'PDF', 5, 'MBA contract sheet showing the 39-credit plan.', NULL),
        ('gu-master-business-administration', 'GU-SRC-010', 'TUITION', 6, 'Financial Policies page listing MBA tuition at 170 USD per credit.', NULL),
        ('gu-master-education', 'GU-SRC-006', 'PRIMARY', 1, 'Education Department page naming Master of Education.', 'Primary program evidence.'),
        ('gu-master-education', 'GU-SRC-002', 'SECONDARY', 2, 'Academics page listing Master of Education under graduate programs.', NULL),
        ('gu-master-education', 'GU-SRC-004', 'ADMISSIONS', 3, 'Application page including Masters in Education as an option.', NULL),
        ('gu-master-education', 'GU-SRC-010', 'TUITION', 4, 'Financial Policies page listing Master of Education tuition at 170 USD per credit.', NULL),
        ('gu-master-arabic-language-literature', 'GU-SRC-007', 'PRIMARY', 1, 'Arabic Language Department page naming the master''s program and its duration.', 'Primary program evidence.'),
        ('gu-master-arabic-language-literature', 'GU-SRC-002', 'SECONDARY', 2, 'Academics page listing Masters of Arabic Literature.', NULL),
        ('gu-master-arabic-language-literature', 'GU-SRC-004', 'ADMISSIONS', 3, 'Application page including Masters in Arabic Language and Literature.', NULL),
        ('gu-master-arabic-language-literature', 'GU-SRC-010', 'TUITION', 4, 'Financial Policies page listing Master of Arabic Literature tuition at 170 USD per credit.', NULL),
        ('gu-master-islamic-studies', 'GU-SRC-008', 'PRIMARY', 1, 'Catalogue listing Master''s in Islamic Studies tracks.', 'Primary program evidence.'),
        ('gu-master-islamic-studies', 'GU-SRC-012', 'SECONDARY', 2, 'Accreditation & Equivalence page naming Islamic Studies among accredited graduate degree names.', NULL);

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
        gp.id,
        sm.db_source_id,
        ps.source_role,
        ps.source_order,
        ps.evidence_text,
        ps.notes
    FROM gu_program_source_seed ps
    JOIN graduate_program gp
      ON gp.university_id = v_university_id
     AND gp.program_key = ps.program_key
    JOIN gu_source_map sm
      ON sm.source_id = ps.source_id
    ON CONFLICT (program_id, source_id, source_role) DO UPDATE SET
        source_order = EXCLUDED.source_order,
        evidence_text = EXCLUDED.evidence_text,
        notes = EXCLUDED.notes,
        updated_at = NOW();

    CREATE TEMP TABLE gu_track_seed (
        program_key TEXT NOT NULL,
        track_type TEXT NOT NULL,
        track_name TEXT NOT NULL,
        track_order INTEGER NOT NULL,
        is_primary BOOLEAN NOT NULL,
        source_id TEXT NOT NULL,
        description TEXT,
        notes TEXT
    ) ON COMMIT DROP;

    INSERT INTO gu_track_seed (program_key, track_type, track_name, track_order, is_primary, source_id, description, notes)
    VALUES
        ('gu-master-business-administration', 'OPTION', 'With Thesis', 1, TRUE, 'GU-SRC-008', 'MBA thesis option preserved from the official catalogue.', 'Preserved as a track rather than a separate degree.'),
        ('gu-master-business-administration', 'OPTION', 'With Project', 2, FALSE, 'GU-SRC-008', 'MBA project option preserved from the official catalogue.', 'Preserved as a track rather than a separate degree.'),
        ('gu-master-islamic-studies', 'CONCENTRATION', 'Islamic Jurisprudence / fiqh', 1, TRUE, 'GU-SRC-008', 'One of the Islamic Studies tracks listed in the official catalogue.', NULL),
        ('gu-master-islamic-studies', 'CONCENTRATION', 'Principles of Jurisprudence / usul al-fiqh', 2, FALSE, 'GU-SRC-008', 'One of the Islamic Studies tracks listed in the official catalogue.', NULL),
        ('gu-master-islamic-studies', 'CONCENTRATION', 'Comparative Jurisprudence', 3, FALSE, 'GU-SRC-008', 'One of the Islamic Studies tracks listed in the official catalogue.', NULL),
        ('gu-master-islamic-studies', 'CONCENTRATION', 'Hadith and its Sciences', 4, FALSE, 'GU-SRC-008', 'One of the Islamic Studies tracks listed in the official catalogue.', NULL),
        ('gu-master-islamic-studies', 'CONCENTRATION', 'Qur''anic Interpretation and Sciences', 5, FALSE, 'GU-SRC-008', 'One of the Islamic Studies tracks listed in the official catalogue.', NULL),
        ('gu-master-islamic-studies', 'CONCENTRATION', 'Creed and Philosophy', 6, FALSE, 'GU-SRC-008', 'One of the Islamic Studies tracks listed in the official catalogue.', NULL);

    INSERT INTO graduate_program_track (
        university_id,
        program_id,
        track_type,
        track_name,
        track_order,
        is_primary,
        description,
        source_id,
        notes
    )
    SELECT
        v_university_id,
        gp.id,
        ts.track_type,
        ts.track_name,
        ts.track_order,
        ts.is_primary,
        ts.description,
        sm.db_source_id,
        ts.notes
    FROM gu_track_seed ts
    JOIN graduate_program gp
      ON gp.university_id = v_university_id
     AND gp.program_key = ts.program_key
    JOIN gu_source_map sm
      ON sm.source_id = ts.source_id
    ON CONFLICT (program_id, track_type, track_name) DO UPDATE SET
        track_order = EXCLUDED.track_order,
        is_primary = EXCLUDED.is_primary,
        description = EXCLUDED.description,
        notes = EXCLUDED.notes,
        updated_at = NOW();

    INSERT INTO graduate_tuition_rate (
        university_id,
        faculty_id,
        department_id,
        program_id,
        scope_level,
        record_key,
        academic_year,
        currency,
        billing_basis,
        amount,
        category,
        notes,
        source_id
    )
    VALUES
        (
            v_university_id,
            (SELECT id FROM university_faculty WHERE university_id = v_university_id AND name = 'Faculty of Administrative Sciences' LIMIT 1),
            NULL,
            NULL,
            'FACULTY',
            'gu-tuition-faculty-administrative-sciences-2025-2026',
            '2025-2026',
            'USD',
            'PER_CREDIT',
            170,
            'Graduate Programs',
            'Financial Policies lists MBA tuition at 170 USD per credit hour.',
            (SELECT db_source_id FROM gu_source_map WHERE source_id = 'GU-SRC-010' LIMIT 1)
        ),
        (
            v_university_id,
            (SELECT id FROM university_faculty WHERE university_id = v_university_id AND name = 'Faculty of Literature and Humanities' LIMIT 1),
            NULL,
            NULL,
            'FACULTY',
            'gu-tuition-faculty-literature-humanities-2025-2026',
            '2025-2026',
            'USD',
            'PER_CREDIT',
            170,
            'Graduate Programs',
            'Financial Policies lists Master of Education and Master of Arabic Literature tuition at 170 USD per credit hour.',
            (SELECT db_source_id FROM gu_source_map WHERE source_id = 'GU-SRC-010' LIMIT 1)
        )
    ON CONFLICT (university_id, record_key) DO UPDATE SET
        faculty_id = EXCLUDED.faculty_id,
        department_id = EXCLUDED.department_id,
        program_id = EXCLUDED.program_id,
        scope_level = EXCLUDED.scope_level,
        academic_year = EXCLUDED.academic_year,
        currency = EXCLUDED.currency,
        billing_basis = EXCLUDED.billing_basis,
        amount = EXCLUDED.amount,
        category = EXCLUDED.category,
        notes = EXCLUDED.notes,
        source_id = EXCLUDED.source_id,
        updated_at = NOW();

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
    VALUES
        (
            v_university_id,
            NULL,
            NULL,
            NULL,
            'UNIVERSITY',
            'gu-fee-application-2025-2026',
            '2025-2026',
            'Graduate application fee',
            'FLAT_FEE',
            'USD',
            50,
            'Admissions',
            'The graduate admissions page lists a non-refundable 50 USD application fee.',
            (SELECT db_source_id FROM gu_source_map WHERE source_id = 'GU-SRC-003' LIMIT 1)
        ),
        (
            v_university_id,
            NULL,
            NULL,
            NULL,
            'UNIVERSITY',
            'gu-fee-registration-summer-2025-2026',
            '2025-2026',
            'Graduate registration fee',
            'FLAT_FEE',
            'USD',
            75,
            'Admissions',
            'The Financial Policies page lists a 75 USD registration fee for Summer.',
            (SELECT db_source_id FROM gu_source_map WHERE source_id = 'GU-SRC-010' LIMIT 1)
        ),
        (
            v_university_id,
            NULL,
            NULL,
            NULL,
            'UNIVERSITY',
            'gu-fee-registration-fall-spring-2025-2026',
            '2025-2026',
            'Graduate registration fee',
            'FLAT_FEE',
            'USD',
            150,
            'Admissions',
            'The Financial Policies page lists a 150 USD registration fee for Fall and Spring.',
            (SELECT db_source_id FROM gu_source_map WHERE source_id = 'GU-SRC-010' LIMIT 1)
        )
    ON CONFLICT (university_id, record_key) DO UPDATE SET
        faculty_id = EXCLUDED.faculty_id,
        department_id = EXCLUDED.department_id,
        program_id = EXCLUDED.program_id,
        scope_level = EXCLUDED.scope_level,
        academic_year = EXCLUDED.academic_year,
        fee_name = EXCLUDED.fee_name,
        billing_basis = EXCLUDED.billing_basis,
        currency = EXCLUDED.currency,
        amount = EXCLUDED.amount,
        category = EXCLUDED.category,
        notes = EXCLUDED.notes,
        source_id = EXCLUDED.source_id,
        updated_at = NOW();

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
    VALUES
        (
            v_university_id,
            NULL,
            NULL,
            NULL,
            'UNIVERSITY',
            'gu-adm-general-2025-2026',
            'GENERAL',
            'Graduate applicants must submit the published application form and the supporting documents listed on the official Graduate Programs page.',
            NULL,
            NULL,
            NULL,
            TRUE,
            'Shared graduate admissions process published on the official Graduate Programs page.',
            (SELECT db_source_id FROM gu_source_map WHERE source_id = 'GU-SRC-003' LIMIT 1)
        )
    ON CONFLICT (university_id, record_key) DO UPDATE SET
        faculty_id = EXCLUDED.faculty_id,
        department_id = EXCLUDED.department_id,
        program_id = EXCLUDED.program_id,
        scope_level = EXCLUDED.scope_level,
        requirement_type = EXCLUDED.requirement_type,
        requirement_text = EXCLUDED.requirement_text,
        comparison_operator = EXCLUDED.comparison_operator,
        threshold_value = EXCLUDED.threshold_value,
        threshold_unit = EXCLUDED.threshold_unit,
        is_required = EXCLUDED.is_required,
        notes = EXCLUDED.notes,
        source_id = EXCLUDED.source_id,
        updated_at = NOW();

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
    VALUES
        (v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'gu-doc-01-application-form', 'APPLICATION', 'Filled application form', FALSE, 1, NULL, (SELECT db_source_id FROM gu_source_map WHERE source_id = 'GU-SRC-003' LIMIT 1)),
        (v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'gu-doc-02-baccalaureate-equivalent', 'ACADEMIC_RECORD', 'Certified copy of Lebanese Baccalaureate or equivalent', FALSE, 2, NULL, (SELECT db_source_id FROM gu_source_map WHERE source_id = 'GU-SRC-003' LIMIT 1)),
        (v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'gu-doc-03-bachelor-degree', 'ACADEMIC_RECORD', 'Bachelor''s degree from a recognized institution of higher learning', FALSE, 3, NULL, (SELECT db_source_id FROM gu_source_map WHERE source_id = 'GU-SRC-003' LIMIT 1)),
        (v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'gu-doc-04-bachelor-equivalence', 'ACADEMIC_RECORD', 'Certified copy of bachelor''s degree equivalence', FALSE, 4, NULL, (SELECT db_source_id FROM gu_source_map WHERE source_id = 'GU-SRC-003' LIMIT 1)),
        (v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'gu-doc-05-transcript', 'ACADEMIC_RECORD', 'Official transcript of undergraduate courses', FALSE, 5, NULL, (SELECT db_source_id FROM gu_source_map WHERE source_id = 'GU-SRC-003' LIMIT 1)),
        (v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'gu-doc-06-photo', 'APPLICATION', 'Recent passport-size photo', FALSE, 6, NULL, (SELECT db_source_id FROM gu_source_map WHERE source_id = 'GU-SRC-003' LIMIT 1)),
        (v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'gu-doc-07-lebanese-id', 'IDENTITY', 'Passport or national identity card for Lebanese applicants', FALSE, 7, NULL, (SELECT db_source_id FROM gu_source_map WHERE source_id = 'GU-SRC-003' LIMIT 1)),
        (v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'gu-doc-08-family-civil-status', 'IDENTITY', 'Family civil status record for Lebanese applicants', FALSE, 8, NULL, (SELECT db_source_id FROM gu_source_map WHERE source_id = 'GU-SRC-003' LIMIT 1)),
        (v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'gu-doc-09-nssf-declaration', 'OTHER', 'NSSF declaration form for students under 30', FALSE, 9, NULL, (SELECT db_source_id FROM gu_source_map WHERE source_id = 'GU-SRC-003' LIMIT 1)),
        (v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'gu-doc-10-passport-copy', 'IDENTITY', 'Passport copy for non-Lebanese applicants', FALSE, 10, NULL, (SELECT db_source_id FROM gu_source_map WHERE source_id = 'GU-SRC-003' LIMIT 1)),
        (v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'gu-doc-11-residence-permit', 'RESIDENCY', 'Valid residence permit copy for non-Lebanese applicants', FALSE, 11, NULL, (SELECT db_source_id FROM gu_source_map WHERE source_id = 'GU-SRC-003' LIMIT 1))
    ON CONFLICT (university_id, record_key) DO UPDATE SET
        faculty_id = EXCLUDED.faculty_id,
        department_id = EXCLUDED.department_id,
        program_id = EXCLUDED.program_id,
        scope_level = EXCLUDED.scope_level,
        document_type = EXCLUDED.document_type,
        document_name = EXCLUDED.document_name,
        is_optional = EXCLUDED.is_optional,
        sort_order = EXCLUDED.sort_order,
        notes = EXCLUDED.notes,
        source_id = EXCLUDED.source_id,
        updated_at = NOW();

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
    VALUES
        (
            v_university_id,
            NULL,
            NULL,
            NULL,
            'UNIVERSITY',
            'gu-calendar-2026-2027',
            '2026-2027',
            'OTHER',
            '2026-2027',
            NULL,
            'Academic calendar lists registration periods, semester starts, late registration or add-drop periods, withdrawal deadlines, final exam periods, and holidays.',
            (SELECT db_source_id FROM gu_source_map WHERE source_id = 'GU-SRC-011' LIMIT 1)
        )
    ON CONFLICT (university_id, record_key) DO UPDATE SET
        faculty_id = EXCLUDED.faculty_id,
        department_id = EXCLUDED.department_id,
        program_id = EXCLUDED.program_id,
        scope_level = EXCLUDED.scope_level,
        academic_year = EXCLUDED.academic_year,
        deadline_type = EXCLUDED.deadline_type,
        term = EXCLUDED.term,
        deadline_date = EXCLUDED.deadline_date,
        note = EXCLUDED.note,
        source_id = EXCLUDED.source_id,
        updated_at = NOW();

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
    VALUES
        (
            v_university_id,
            NULL,
            NULL,
            NULL,
            'UNIVERSITY',
            'gu-payment-plan-fall-spring-2025-2026',
            '2025-2026',
            'Deferred payment contract (Fall/Spring)',
            'Students who want installments complete a deferred payment form, and accepted students sign a deferred payment contract. First payment is 500 USD in Fall and Spring.',
            NULL,
            500,
            'USD',
            'SEMESTER',
            'The Financial Policies page publishes the Fall/Spring first-payment amount.',
            (SELECT db_source_id FROM gu_source_map WHERE source_id = 'GU-SRC-010' LIMIT 1)
        ),
        (
            v_university_id,
            NULL,
            NULL,
            NULL,
            'UNIVERSITY',
            'gu-payment-plan-summer-2025-2026',
            '2025-2026',
            'Deferred payment contract (Summer)',
            'Students who want installments complete a deferred payment form, and accepted students sign a deferred payment contract. First payment is 50% of total fees in Summer.',
            NULL,
            NULL,
            NULL,
            'SUMMER',
            'The Financial Policies page publishes the Summer first-payment rule as a percentage rather than a numeric amount.',
            (SELECT db_source_id FROM gu_source_map WHERE source_id = 'GU-SRC-010' LIMIT 1)
        )
    ON CONFLICT (university_id, record_key) DO UPDATE SET
        faculty_id = EXCLUDED.faculty_id,
        department_id = EXCLUDED.department_id,
        program_id = EXCLUDED.program_id,
        scope_level = EXCLUDED.scope_level,
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

    INSERT INTO graduate_accreditation (
        university_id,
        faculty_id,
        department_id,
        program_id,
        scope_level,
        record_key,
        name,
        authority,
        status,
        valid_from,
        valid_until,
        notes,
        source_id
    )
    VALUES
        (
            v_university_id,
            NULL,
            NULL,
            NULL,
            'UNIVERSITY',
            'gu-accreditation-graduate-degrees',
            'Graduate degrees authenticated/accredited by the Directorate of Higher Education in Lebanon',
            'Directorate of Higher Education in Lebanon',
            'CURRENT',
            NULL,
            NULL,
            'Accreditation and equivalence page confirms graduate degree naming context for Business Administration, Education, Arabic Language and Literature, and Islamic Studies.',
            (SELECT db_source_id FROM gu_source_map WHERE source_id = 'GU-SRC-012' LIMIT 1)
        )
    ON CONFLICT (university_id, record_key) DO UPDATE SET
        faculty_id = EXCLUDED.faculty_id,
        department_id = EXCLUDED.department_id,
        program_id = EXCLUDED.program_id,
        scope_level = EXCLUDED.scope_level,
        name = EXCLUDED.name,
        authority = EXCLUDED.authority,
        status = EXCLUDED.status,
        valid_from = EXCLUDED.valid_from,
        valid_until = EXCLUDED.valid_until,
        notes = EXCLUDED.notes,
        source_id = EXCLUDED.source_id,
        updated_at = NOW();
END $$;
