-- MEU graduate data seed migration.
-- Idempotent import for the canonical MEU graduate dataset.

DO $$
DECLARE
    v_university_id BIGINT;
BEGIN

    INSERT INTO university (name, name_ar, acronym, country, city, latitude, longitude, campus_name, campus_type)
    SELECT 'Middle East University', NULL, 'MEU', 'Lebanon', NULL, NULL, NULL, NULL, NULL
    WHERE NOT EXISTS (
        SELECT 1 FROM university WHERE name = 'Middle East University'
    );

    SELECT id INTO v_university_id
    FROM university
    WHERE name = 'Middle East University'
    ORDER BY id
    LIMIT 1;

    INSERT INTO degree_type (code, name)
    VALUES
        ('MASTER', 'Master'),
        ('PHD', 'Doctor of Philosophy'),
        ('DIPLOMA', 'Diploma'),
        ('CERTIFICATE', 'Certificate')
    ON CONFLICT (code) DO UPDATE SET
        name = EXCLUDED.name,
        updated_at = NOW();

    INSERT INTO language (name, code, native_name)
    VALUES
        ('English', 'en', 'English'),
        ('Arabic', 'ar', 'العربية')
    ON CONFLICT (code) DO UPDATE SET
        name = EXCLUDED.name,
        native_name = EXCLUDED.native_name;

    CREATE TEMP TABLE meu_source_seed (
        source_id TEXT PRIMARY KEY,
        title TEXT NOT NULL,
        url TEXT NOT NULL,
        source_type TEXT NOT NULL,
        accessed_at DATE,
        notes TEXT
    ) ON COMMIT DROP;

    INSERT INTO meu_source_seed (source_id, title, url, source_type, accessed_at, notes)
    VALUES
        ('MEU-S001', 'Middle East University Home', 'https://meu.edu.lb/', 'WEB', '2026-07-05', 'University homepage; confirms institutional identity, contact details, and quick links.'),
        ('MEU-S002', 'Programs - Middle East University', 'https://meu.edu.lb/programs/', 'WEB', '2026-07-05', 'Primary program inventory page; lists graduate programs and links to program detail pages.'),
        ('MEU-S003', 'Business Administration - Master of Business Administration', 'https://meu.edu.lb/business-administration-mba/', 'WEB', '2026-07-05', 'Official MBA program page; includes overview and concentrations.'),
        ('MEU-S004', 'Education - Master of Arts', 'https://meu.edu.lb/master-education/', 'WEB', '2026-07-05', 'Official MA Education program page; includes overview and concentrations.'),
        ('MEU-S005', 'Islamic Studies - Master of Arts', 'https://meu.edu.lb/islamic-studies/', 'WEB', '2026-07-05', 'Official MA Islamic Studies program page; links to catalog curriculum.'),
        ('MEU-S006', 'Teaching - Master of Arts', 'https://meu.edu.lb/teaching-master/', 'WEB', '2026-07-05', 'Official MA Teaching program page; includes overview.'),
        ('MEU-S007', 'Application Procedure - Middle East University', 'https://meu.edu.lb/application-procedure/', 'WEB', '2026-07-05', 'Official admissions/application page; application fee and required document process.'),
        ('MEU-S008', 'Admissions | MEU Academic Catalog', 'https://catalog.meu.edu.lb/catalog/admissions.jsp', 'WEB', '2026-07-05', 'Graduate admissions, required documents, English proficiency, international applicants, graduate statuses, transfer, and graduate regulations.'),
        ('MEU-S009', 'MEU Academic Catalog 2024-2026', 'https://catalog.meu.edu.lb/catalog/index.jsp', 'WEB', '2026-07-05', 'Catalog index; maps faculties/departments and catalog sections.'),
        ('MEU-S010', 'Financial Information | MEU Academic Catalog', 'https://catalog.meu.edu.lb/catalog/financialInformation.jsp', 'WEB', '2026-07-05', 'Tuition and fees policy, payment arrangements, international deposit note, financial aid, refund policy.'),
        ('MEU-S011', 'Financial Aid & Scholarships - Middle East University', 'https://meu.edu.lb/scholarships/', 'WEB', '2026-07-05', 'Financial aid and scholarship options; includes graduate-relevant needs-based aid, merit scholarship, work-study, and terms.'),
        ('MEU-S012', 'Department of Mediterranean and Near Eastern Studies | MEU Academic Catalog', 'https://catalog.meu.edu.lb/catalog/departmentOfMediterranean.jsp', 'WEB', '2026-07-05', 'Catalog details for MA Islamic Studies, including delivery, Arabic prerequisites, course list, and total credits.'),
        ('MEU-S013', 'Graduate Program 2025-2026', 'https://meu.edu.lb/wp-content/uploads/2025/07/Graduate-Program-2025-26.pdf', 'PDF', '2026-07-05', 'Official graduate tuition/fees PDF for 2025-2026.'),
        ('MEU-S014', 'Tuition and Fees - Master 2025-2026', 'https://meu.edu.lb/wp-content/uploads/2025/08/Tuition-Master-2025-2026-1.pdf', 'PDF', '2026-07-05', 'Official master tuition and fees PDF; includes deposit notes for Lebanese and foreign students.'),
        ('MEU-S015', 'Academic Calendar 2025-2026', 'https://meu.edu.lb/wp-content/uploads/2026/01/MEU-Acad-Cal-2025-2026_compressed-2.pdf', 'PDF', '2026-07-05', 'Official academic calendar PDF; includes semester dates, exam periods, and international student application deadlines.'),
        ('MEU-S016', 'Academic Calendar 2026-2027', 'https://meu.edu.lb/wp-content/uploads/2026/04/MEU-Acad-Cal-2026-2027.pdf', 'PDF', '2026-07-05', 'Official next academic calendar PDF for upcoming intake/year reference.'),
        ('MEU-S017', 'Student Handbook 2024', 'https://meu.edu.lb/wp-content/uploads/2025/08/Student-Handbook-2024.pdf', 'PDF', '2026-07-05', 'Official student handbook; useful for general student rules, but not primary for graduate program catalog data.');

    INSERT INTO source (university_id, title, url, source_type, accessed_at)
    SELECT v_university_id, title, url, source_type, accessed_at
    FROM meu_source_seed
    ON CONFLICT (university_id, url) DO UPDATE SET
        title = EXCLUDED.title,
        source_type = EXCLUDED.source_type,
        accessed_at = EXCLUDED.accessed_at,
        updated_at = NOW();

    CREATE TEMP TABLE meu_faculty_seed (
        name TEXT PRIMARY KEY,
        short_name TEXT,
        faculty_type TEXT NOT NULL,
        official_url TEXT,
        notes TEXT
    ) ON COMMIT DROP;

    INSERT INTO meu_faculty_seed (name, short_name, faculty_type, official_url, notes)
    VALUES
        ('Faculty of Business Administration', NULL, 'FACULTY', NULL, 'Official faculty discovered in the MEU graduate program set.'),
        ('Faculty of Education', NULL, 'FACULTY', NULL, 'Official faculty discovered in the MEU graduate program set.'),
        ('Faculty of Philosophy and Theology', NULL, 'FACULTY', NULL, 'Official faculty discovered in the MEU graduate program set.'),
        ('Faculty of Arts and Sciences', NULL, 'FACULTY', NULL, 'Official faculty discovered in the MEU catalog navigation.');

    INSERT INTO university_faculty (university_id, name, short_name, faculty_type, official_url, notes)
    SELECT v_university_id, name, short_name, faculty_type, official_url, notes
    FROM meu_faculty_seed
    ON CONFLICT (university_id, name) DO UPDATE SET
        short_name = EXCLUDED.short_name,
        faculty_type = EXCLUDED.faculty_type,
        official_url = EXCLUDED.official_url,
        notes = EXCLUDED.notes,
        updated_at = NOW();

    CREATE TEMP TABLE meu_department_seed (
        faculty_name TEXT NOT NULL,
        name TEXT PRIMARY KEY,
        short_name TEXT,
        official_url TEXT,
        notes TEXT
    ) ON COMMIT DROP;

    INSERT INTO meu_department_seed (faculty_name, name, short_name, official_url, notes)
    VALUES
        ('Faculty of Business Administration', 'Department of Business Administration', NULL, NULL, 'Official department referenced by the MBA program context and catalog navigation.'),
        ('Faculty of Education', 'Department of Teacher Education', NULL, NULL, 'Official department referenced by MA Education and MA Teaching.'),
        ('Faculty of Philosophy and Theology', 'Department of Mediterranean and Near Eastern Studies', NULL, NULL, 'Official department referenced by MA Islamic Studies.');

    INSERT INTO university_department (university_id, faculty_id, name, short_name, official_url, notes)
    SELECT v_university_id, f.id, d.name, d.short_name, d.official_url, d.notes
    FROM meu_department_seed d
    JOIN university_faculty f
      ON f.university_id = v_university_id
     AND f.name = d.faculty_name
    ON CONFLICT (university_id, faculty_id, name) DO UPDATE SET
        short_name = EXCLUDED.short_name,
        official_url = EXCLUDED.official_url,
        notes = EXCLUDED.notes,
        updated_at = NOW();

    CREATE TEMP TABLE meu_program_seed (
        program_key TEXT PRIMARY KEY,
        faculty_name TEXT NOT NULL,
        department_name TEXT,
        major_category TEXT,
        major TEXT,
        degree_type TEXT NOT NULL,
        official_degree_name TEXT NOT NULL,
        thesis_or_non_thesis TEXT,
        credits INTEGER,
        duration_value NUMERIC(10, 2),
        duration_unit TEXT,
        delivery_mode TEXT,
        program_description TEXT,
        official_program_url TEXT NOT NULL,
        source_id TEXT NOT NULL,
        notes TEXT
    ) ON COMMIT DROP;

    INSERT INTO meu_program_seed (
        program_key, faculty_name, department_name, major_category, major, degree_type, official_degree_name,
        thesis_or_non_thesis, credits, duration_value, duration_unit, delivery_mode, program_description,
        official_program_url, source_id, notes
    )
    VALUES
        (
            'meu-fba-master-business-administration',
            'Faculty of Business Administration',
            'Department of Business Administration',
            'Business Administration',
            'Business Administration',
            'MASTER',
            'Master of Business Administration',
            NULL,
            NULL,
            NULL,
            NULL,
            NULL,
            'Master of Business Administration with concentrations in Finance, General Business, Management, and Marketing.',
            'https://meu.edu.lb/business-administration-mba/',
            'MEU-S003',
            'Official MBA page confirms the program and the four concentrations.'
        ),
        (
            'meu-fe-master-education',
            'Faculty of Education',
            'Department of Teacher Education',
            'Education',
            'Education',
            'MASTER',
            'Master of Arts in Education',
            NULL,
            NULL,
            NULL,
            NULL,
            NULL,
            'The Master of Arts in Education is designed for teachers who are seeking to enhance their classroom skills, become curriculum supervisors, or assume leadership roles in many educational settings.',
            'https://meu.edu.lb/master-education/',
            'MEU-S004',
            'Official MA Education page confirms the program and its two concentrations.'
        ),
        (
            'meu-ft-master-islamic-studies',
            'Faculty of Philosophy and Theology',
            'Department of Mediterranean and Near Eastern Studies',
            'Philosophy and Theology',
            'Islamic Studies',
            'MASTER',
            'Master of Arts in Islamic Studies',
            'THESIS',
            44,
            NULL,
            NULL,
            'ON_CAMPUS',
            'Master of Arts in Islamic Studies with 44 credits, catalog-based course structure, and Arabic prerequisite notes for students without sufficient Arabic background.',
            'https://meu.edu.lb/islamic-studies/',
            'MEU-S005',
            'Official program and catalog sources confirm the degree, department, 44-credit structure, on-campus delivery, and Arabic prerequisite condition.'
        ),
        (
            'meu-fe-master-teaching',
            'Faculty of Education',
            'Department of Teacher Education',
            'Education',
            'Teaching',
            'MASTER',
            'Master of Arts in Teaching',
            NULL,
            NULL,
            NULL,
            NULL,
            NULL,
            'Master of Arts in Teaching for professional educators seeking advanced preparation in teaching and instructional practice.',
            'https://meu.edu.lb/teaching-master/',
            'MEU-S006',
            'Official MA Teaching page confirms the program and its focus on professional educators.'
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
        p.program_key,
        p.major_category,
        p.major,
        p.official_degree_name,
        p.thesis_or_non_thesis,
        p.credits,
        p.duration_value,
        p.duration_unit,
        NULL,
        p.delivery_mode,
        p.program_description,
        p.official_program_url,
        s.id,
        p.notes
    FROM meu_program_seed p
    JOIN university_faculty f
      ON f.university_id = v_university_id
     AND f.name = p.faculty_name
    LEFT JOIN university_department d
      ON d.university_id = v_university_id
     AND d.faculty_id = f.id
     AND d.name = p.department_name
    LEFT JOIN degree_type dt
      ON dt.code = p.degree_type
    JOIN source s
      ON s.university_id = v_university_id
     AND s.id = (SELECT id FROM source WHERE university_id = v_university_id AND url = (SELECT url FROM meu_source_seed WHERE source_id = p.source_id) LIMIT 1)
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

    CREATE TEMP TABLE meu_track_seed (
        program_key TEXT NOT NULL,
        track_type TEXT NOT NULL,
        track_name TEXT NOT NULL,
        track_order INTEGER,
        is_primary BOOLEAN NOT NULL DEFAULT FALSE,
        description TEXT,
        source_id TEXT NOT NULL,
        notes TEXT
    ) ON COMMIT DROP;

    INSERT INTO meu_track_seed (program_key, track_type, track_name, track_order, is_primary, description, source_id, notes)
    VALUES
        ('meu-fba-master-business-administration', 'CONCENTRATION', 'Finance', 1, TRUE, NULL, 'MEU-S003', 'Official MBA concentration listed on the program page.'),
        ('meu-fba-master-business-administration', 'CONCENTRATION', 'General Business', 2, FALSE, NULL, 'MEU-S003', 'Official MBA concentration listed on the program page.'),
        ('meu-fba-master-business-administration', 'CONCENTRATION', 'Management', 3, FALSE, NULL, 'MEU-S003', 'Official MBA concentration listed on the program page.'),
        ('meu-fba-master-business-administration', 'CONCENTRATION', 'Marketing', 4, FALSE, NULL, 'MEU-S003', 'Official MBA concentration listed on the program page.'),
        ('meu-fe-master-education', 'CONCENTRATION', 'Curriculum and Instruction', 1, TRUE, NULL, 'MEU-S004', 'Official MA Education concentration listed on the program page.'),
        ('meu-fe-master-education', 'CONCENTRATION', 'Educational Leadership', 2, FALSE, NULL, 'MEU-S004', 'Official MA Education concentration listed on the program page.');

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
        t.track_type,
        t.track_name,
        t.track_order,
        t.is_primary,
        t.description,
        s.id,
        t.notes
    FROM meu_track_seed t
    JOIN graduate_program gp
      ON gp.university_id = v_university_id
     AND gp.program_key = t.program_key
    JOIN source s
      ON s.university_id = v_university_id
     AND s.url = (SELECT url FROM meu_source_seed WHERE source_id = t.source_id)
    ON CONFLICT (program_id, track_type, track_name) DO UPDATE SET
        track_order = EXCLUDED.track_order,
        is_primary = EXCLUDED.is_primary,
        description = EXCLUDED.description,
        source_id = EXCLUDED.source_id,
        notes = EXCLUDED.notes,
        updated_at = NOW();

    CREATE TEMP TABLE meu_program_source_seed (
        program_key TEXT NOT NULL,
        source_id TEXT NOT NULL,
        source_role TEXT NOT NULL,
        source_order INTEGER NOT NULL,
        evidence_text TEXT,
        notes TEXT
    ) ON COMMIT DROP;

    INSERT INTO meu_program_source_seed (program_key, source_id, source_role, source_order, evidence_text, notes)
    VALUES
        ('meu-fba-master-business-administration', 'MEU-S003', 'PRIMARY', 1, 'MBA program page confirming the program and concentrations.', 'Primary official source.'),
        ('meu-fba-master-business-administration', 'MEU-S002', 'SECONDARY', 2, 'Programs page listing the MBA among official graduate programs.', 'Inventory source.'),
        ('meu-fba-master-business-administration', 'MEU-S009', 'FACULTY', 3, 'Catalog index confirming the faculty and department structure used for the MBA.', 'Catalog source.'),
        ('meu-fba-master-business-administration', 'MEU-S007', 'ADMISSIONS', 4, 'Application procedure page confirming graduate application routing and fee context.', 'Admissions source.'),
        ('meu-fba-master-business-administration', 'MEU-S013', 'TUITION', 5, 'Graduate Program 2025-2026 fee schedule confirms the shared graduate tuition rate.', 'Tuition source.'),
        ('meu-fe-master-education', 'MEU-S004', 'PRIMARY', 1, 'MA Education program page confirming the program and concentrations.', 'Primary official source.'),
        ('meu-fe-master-education', 'MEU-S002', 'SECONDARY', 2, 'Programs page listing the MA Education among official graduate programs.', 'Inventory source.'),
        ('meu-fe-master-education', 'MEU-S009', 'FACULTY', 3, 'Catalog index confirming the faculty and department structure used for MA Education.', 'Catalog source.'),
        ('meu-fe-master-education', 'MEU-S007', 'ADMISSIONS', 4, 'Application procedure page confirming graduate application routing and fee context.', 'Admissions source.'),
        ('meu-fe-master-education', 'MEU-S013', 'TUITION', 5, 'Graduate Program 2025-2026 fee schedule confirms the shared graduate tuition rate.', 'Tuition source.'),
        ('meu-ft-master-islamic-studies', 'MEU-S005', 'PRIMARY', 1, 'MA Islamic Studies program page confirming the degree and catalog link.', 'Primary official source.'),
        ('meu-ft-master-islamic-studies', 'MEU-S012', 'CATALOG', 2, 'Catalog page confirming 44 credits, on-campus delivery, and Arabic prerequisite conditions.', 'Catalog source.'),
        ('meu-ft-master-islamic-studies', 'MEU-S009', 'FACULTY', 3, 'Catalog index confirming the faculty and department structure used for MA Islamic Studies.', 'Catalog source.'),
        ('meu-ft-master-islamic-studies', 'MEU-S002', 'SECONDARY', 4, 'Programs page listing the MA Islamic Studies among official graduate programs.', 'Inventory source.'),
        ('meu-ft-master-islamic-studies', 'MEU-S007', 'ADMISSIONS', 5, 'Application procedure page confirming graduate application routing and fee context.', 'Admissions source.'),
        ('meu-ft-master-islamic-studies', 'MEU-S013', 'TUITION', 6, 'Graduate Program 2025-2026 fee schedule confirms the shared graduate tuition rate.', 'Tuition source.'),
        ('meu-fe-master-teaching', 'MEU-S006', 'PRIMARY', 1, 'MA Teaching program page confirming the degree and program focus.', 'Primary official source.'),
        ('meu-fe-master-teaching', 'MEU-S002', 'SECONDARY', 2, 'Programs page listing the MA Teaching among official graduate programs.', 'Inventory source.'),
        ('meu-fe-master-teaching', 'MEU-S009', 'FACULTY', 3, 'Catalog index confirming the faculty and department structure used for MA Teaching.', 'Catalog source.'),
        ('meu-fe-master-teaching', 'MEU-S007', 'ADMISSIONS', 4, 'Application procedure page confirming graduate application routing and fee context.', 'Admissions source.'),
        ('meu-fe-master-teaching', 'MEU-S013', 'TUITION', 5, 'Graduate Program 2025-2026 fee schedule confirms the shared graduate tuition rate.', 'Tuition source.');

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
        s.id,
        ps.source_role,
        ps.source_order,
        ps.evidence_text,
        ps.notes
    FROM meu_program_source_seed ps
    JOIN graduate_program gp
      ON gp.university_id = v_university_id
     AND gp.program_key = ps.program_key
    JOIN source s
      ON s.university_id = v_university_id
     AND s.url = (SELECT url FROM meu_source_seed WHERE source_id = ps.source_id)
    ON CONFLICT (program_id, source_id, source_role) DO UPDATE SET
        source_order = EXCLUDED.source_order,
        evidence_text = EXCLUDED.evidence_text,
        notes = EXCLUDED.notes,
        updated_at = NOW();

    CREATE TEMP TABLE meu_tuition_seed (
        program_key TEXT PRIMARY KEY,
        faculty_name TEXT NOT NULL,
        academic_year TEXT NOT NULL,
        currency TEXT NOT NULL,
        billing_basis TEXT NOT NULL,
        amount NUMERIC(12, 2) NOT NULL,
        category TEXT NOT NULL,
        notes TEXT NOT NULL,
        source_id TEXT NOT NULL
    ) ON COMMIT DROP;

    INSERT INTO meu_tuition_seed (program_key, faculty_name, academic_year, currency, billing_basis, amount, category, notes, source_id)
    VALUES
        ('meu-fba-master-business-administration', 'Faculty of Business Administration', '2025-2026', 'USD', 'PER_CREDIT', 305, 'Graduate Students', 'Official MEU graduate tuition schedule; MBA also carries a separate business program fee recorded below.', 'MEU-S013'),
        ('meu-fe-master-education', 'Faculty of Education', '2025-2026', 'USD', 'PER_CREDIT', 305, 'Graduate Students', 'Official MEU graduate tuition schedule.', 'MEU-S013'),
        ('meu-ft-master-islamic-studies', 'Faculty of Philosophy and Theology', '2025-2026', 'USD', 'PER_CREDIT', 305, 'Graduate Students', 'Official MEU graduate tuition schedule.', 'MEU-S013'),
        ('meu-fe-master-teaching', 'Faculty of Education', '2025-2026', 'USD', 'PER_CREDIT', 305, 'Graduate Students', 'Official MEU graduate tuition schedule.', 'MEU-S013');

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
    SELECT
        v_university_id,
        f.id,
        NULL,
        gp.id,
        'PROGRAM',
        CONCAT('meu-tuition-', t.program_key),
        t.academic_year,
        t.currency,
        t.billing_basis,
        t.amount,
        t.category,
        t.notes,
        s.id
    FROM meu_tuition_seed t
    JOIN graduate_program gp
      ON gp.university_id = v_university_id
     AND gp.program_key = t.program_key
    JOIN university_faculty f
      ON f.university_id = v_university_id
     AND f.name = t.faculty_name
    JOIN source s
      ON s.university_id = v_university_id
     AND s.url = (SELECT url FROM meu_source_seed WHERE source_id = t.source_id)
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

    CREATE TEMP TABLE meu_fee_seed (
        record_key TEXT PRIMARY KEY,
        fee_name TEXT NOT NULL,
        scope_level TEXT NOT NULL,
        faculty_name TEXT,
        program_key TEXT,
        academic_year TEXT,
        billing_basis TEXT NOT NULL,
        currency TEXT NOT NULL,
        amount NUMERIC(12, 2),
        category TEXT,
        notes TEXT,
        source_id TEXT NOT NULL
    ) ON COMMIT DROP;

    INSERT INTO meu_fee_seed (record_key, fee_name, scope_level, faculty_name, program_key, academic_year, billing_basis, currency, amount, category, notes, source_id)
    VALUES
        ('meu-fee-application', 'Graduate Application Fee', 'UNIVERSITY', NULL, NULL, '2025-2026', 'PER_APPLICATION', 'USD', 85, 'University Graduate Fees', 'Official graduate fee schedule; application fee for graduate applicants.', 'MEU-S014'),
        ('meu-fee-registration', 'Graduate Registration Fee', 'UNIVERSITY', NULL, NULL, '2025-2026', 'FLAT_FEE', 'USD', 220, 'University Graduate Fees', 'Official graduate fee schedule; registration fee for graduate students.', 'MEU-S014'),
        ('meu-fee-business-program-semester', 'Business Program Fee', 'PROGRAM', 'Faculty of Business Administration', 'meu-fba-master-business-administration', '2025-2026', 'PER_SEMESTER', 'USD', 135, 'MBA Business Program Fee', 'Official graduate fee schedule for MBA students. Summer session fee is also published separately.', 'MEU-S014'),
        ('meu-fee-business-program-summer', 'Business Program Fee - Summer Session', 'PROGRAM', 'Faculty of Business Administration', 'meu-fba-master-business-administration', '2025-2026', 'PER_TERM', 'USD', 85, 'MBA Business Program Fee', 'Official graduate fee schedule for MBA students; summer-session amount published separately.', 'MEU-S014'),
        ('meu-fee-foreign-deposit', 'Foreign Student Advance Deposit', 'UNIVERSITY', NULL, NULL, '2025-2026', 'FLAT_FEE', 'USD', 2500, 'International Students', 'Credited to tuition and fees for foreign students.', 'MEU-S014'),
        ('meu-fee-lebanese-deposit', 'Lebanese Registration Deposit', 'UNIVERSITY', NULL, NULL, '2025-2026', 'FLAT_FEE', 'USD', 200, 'Lebanese Students', 'Credited to tuition and fees for Lebanese students.', 'MEU-S014'),
        ('meu-fee-development', 'Development Fee', 'UNIVERSITY', NULL, NULL, '2025-2026', 'PER_SEMESTER', 'USD', 45, 'University Graduate Fees', 'Published in the official graduate fee schedule.', 'MEU-S014'),
        ('meu-fee-parking', 'Parking Fee', 'UNIVERSITY', NULL, NULL, '2025-2026', 'PER_SEMESTER', 'USD', 35, 'University Graduate Fees', 'Published in the official graduate fee schedule.', 'MEU-S014'),
        ('meu-fee-accident-insurance', 'Accident Insurance for Non-Lebanese Students', 'UNIVERSITY', NULL, NULL, '2025-2026', 'PER_SEMESTER', 'USD', 30, 'University Graduate Fees', 'Published in the official graduate fee schedule.', 'MEU-S014'),
        ('meu-fee-residency-permit', 'Student Residency Permit Fee', 'UNIVERSITY', NULL, NULL, '2025-2026', 'FLAT_FEE', 'USD', 450, 'University Graduate Fees', 'Published in the official graduate fee schedule.', 'MEU-S014'),
        ('meu-fee-nssf', 'National Social Security Fee for Lebanese Students', 'UNIVERSITY', NULL, NULL, '2025-2026', 'PER_SEMESTER', 'LBP', 3000000, 'University Graduate Fees', 'If applicable; published in the official graduate fee schedule.', 'MEU-S014');

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
        f.id,
        d.id,
        gp.id,
        fs.scope_level,
        fs.record_key,
        fs.academic_year,
        fs.fee_name,
        fs.billing_basis,
        fs.currency,
        fs.amount,
        fs.category,
        fs.notes,
        s.id
    FROM meu_fee_seed fs
    LEFT JOIN university_faculty f
      ON f.university_id = v_university_id
     AND f.name = fs.faculty_name
    LEFT JOIN graduate_program gp
      ON gp.university_id = v_university_id
     AND gp.program_key = fs.program_key
    LEFT JOIN university_department d
      ON d.university_id = v_university_id
     AND d.faculty_id = f.id
     AND d.name = 'Department of Business Administration'
    JOIN source s
      ON s.university_id = v_university_id
     AND s.url = (SELECT url FROM meu_source_seed WHERE source_id = fs.source_id)
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

    CREATE TEMP TABLE meu_admission_seed (
        record_key TEXT PRIMARY KEY,
        program_key TEXT,
        scope_level TEXT NOT NULL,
        requirement_type TEXT NOT NULL,
        requirement_text TEXT NOT NULL,
        comparison_operator TEXT,
        threshold_value NUMERIC(12, 2),
        threshold_unit TEXT,
        is_required BOOLEAN NOT NULL,
        notes TEXT,
        source_id TEXT NOT NULL
    ) ON COMMIT DROP;

    INSERT INTO meu_admission_seed (record_key, program_key, scope_level, requirement_type, requirement_text, comparison_operator, threshold_value, threshold_unit, is_required, notes, source_id)
    VALUES
        ('meu-adm-bachelor-degree', NULL, 'UNIVERSITY', 'ACADEMIC', 'Applicants must hold a bachelor''s degree or equivalent from an accredited institution recognized by MEHE.', NULL, NULL, NULL, TRUE, 'General graduate admission criterion.', 'MEU-S008'),
        ('meu-adm-gpa-275', NULL, 'UNIVERSITY', 'ACADEMIC', 'Regular graduate status requires a minimum undergraduate cumulative GPA of 2.75 in the major area of study.', '>=', 2.75, 'GPA', TRUE, 'Regular graduate status criterion.', 'MEU-S008'),
        ('meu-adm-application-file-timing', NULL, 'UNIVERSITY', 'GENERAL', 'Applicants should submit the application file at least one month before the beginning of the semester.', NULL, NULL, NULL, TRUE, 'Admission timing guidance from the catalog.', 'MEU-S008'),
        ('meu-adm-english-proficiency', NULL, 'UNIVERSITY', 'ENGLISH', 'Graduate applicants must demonstrate English proficiency. Accepted thresholds listed in the catalog are TOEFL ITP 600, TOEFL iBT 100, IELTS 7.0, and Duolingo 120.', NULL, NULL, NULL, TRUE, 'Language requirement from the catalog.', 'MEU-S008'),
        ('meu-adm-gre-gmat-note', NULL, 'UNIVERSITY', 'OTHER', 'Departments offering MA, MAT, and MS may require GRE. MBA applicants may be required to sit for GMAT.', NULL, NULL, NULL, TRUE, 'General departmental testing note.', 'MEU-S008'),
        ('meu-adm-islamic-studies-arabic', 'meu-ft-master-islamic-studies', 'PROGRAM', 'PREREQUISITE', 'Applicants without adequate Arabic background must complete two prerequisite Arabic language courses outside the MA program.', NULL, NULL, NULL, TRUE, 'Program-specific condition published on the MA Islamic Studies catalog page.', 'MEU-S012');

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
        CASE WHEN a.program_key = 'meu-ft-master-islamic-studies' THEN f.id ELSE NULL END,
        CASE WHEN a.program_key = 'meu-ft-master-islamic-studies' THEN d.id ELSE NULL END,
        gp.id,
        a.scope_level,
        a.record_key,
        a.requirement_type,
        a.requirement_text,
        a.comparison_operator,
        a.threshold_value,
        a.threshold_unit,
        a.is_required,
        a.notes,
        s.id
    FROM meu_admission_seed a
    LEFT JOIN graduate_program gp
      ON gp.university_id = v_university_id
     AND gp.program_key = a.program_key
    LEFT JOIN university_faculty f
      ON f.university_id = v_university_id
     AND f.name = 'Faculty of Philosophy and Theology'
    LEFT JOIN university_department d
      ON d.university_id = v_university_id
     AND d.name = 'Department of Mediterranean and Near Eastern Studies'
    JOIN source s
      ON s.university_id = v_university_id
     AND s.url = (SELECT url FROM meu_source_seed WHERE source_id = a.source_id)
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

    CREATE TEMP TABLE meu_required_document_seed (
        record_key TEXT PRIMARY KEY,
        document_type TEXT NOT NULL,
        document_name TEXT NOT NULL,
        sort_order INTEGER,
        notes TEXT,
        source_id TEXT NOT NULL
    ) ON COMMIT DROP;

    INSERT INTO meu_required_document_seed (record_key, document_type, document_name, sort_order, notes, source_id)
    VALUES
        ('meu-doc-application-form', 'APPLICATION_FORM', 'Completed application form', 1, 'Listed in the catalog admissions page.', 'MEU-S008'),
        ('meu-doc-photo', 'PHOTOGRAPH', 'Recent digital passport-style photograph', 2, 'Listed in the catalog admissions page.', 'MEU-S008'),
        ('meu-doc-bachelor-diploma-transcript', 'ACADEMIC_RECORD', 'Authenticated copy of the bachelor''s diploma and official undergraduate transcript(s)', 3, 'Listed in the catalog admissions page.', 'MEU-S008'),
        ('meu-doc-baccalaureate-equivalent', 'ACADEMIC_RECORD', 'Certified copy of the baccalaureate certificate or equivalent', 4, 'Listed in the catalog admissions page.', 'MEU-S008'),
        ('meu-doc-english-proficiency', 'LANGUAGE', 'English proficiency certificate if required and not older than two years', 5, 'Listed in the catalog admissions page.', 'MEU-S008'),
        ('meu-doc-passport-main-page', 'IDENTIFICATION', 'Passport main page for international applicants', 6, 'Listed in the catalog admissions page.', 'MEU-S008'),
        ('meu-doc-national-id', 'IDENTIFICATION', 'National identity card for Lebanese applicants', 7, 'Listed in the catalog admissions page.', 'MEU-S008'),
        ('meu-doc-family-status', 'CIVIL_STATUS', 'Family civil status record for Lebanese applicants', 8, 'Listed in the catalog admissions page.', 'MEU-S008'),
        ('meu-doc-transfer-transcript', 'ACADEMIC_RECORD', 'Official transcript in a sealed envelope for transfer applicants', 9, 'Listed in the catalog admissions page.', 'MEU-S008');

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
        d.record_key,
        d.document_type,
        d.document_name,
        FALSE,
        d.sort_order,
        d.notes,
        s.id
    FROM meu_required_document_seed d
    JOIN source s
      ON s.university_id = v_university_id
     AND s.url = (SELECT url FROM meu_source_seed WHERE source_id = d.source_id)
    ON CONFLICT (university_id, record_key) DO UPDATE SET
        scope_level = EXCLUDED.scope_level,
        document_type = EXCLUDED.document_type,
        document_name = EXCLUDED.document_name,
        is_optional = EXCLUDED.is_optional,
        sort_order = EXCLUDED.sort_order,
        notes = EXCLUDED.notes,
        source_id = EXCLUDED.source_id,
        updated_at = NOW();

    CREATE TEMP TABLE meu_deadline_seed (
        record_key TEXT PRIMARY KEY,
        academic_year TEXT,
        deadline_type TEXT NOT NULL,
        term TEXT,
        deadline_date DATE,
        note TEXT,
        source_id TEXT NOT NULL
    ) ON COMMIT DROP;

    INSERT INTO meu_deadline_seed (record_key, academic_year, deadline_type, term, deadline_date, note, source_id)
    VALUES
        ('meu-deadline-spring-2026-international-application', '2025-2026', 'FINAL', 'Spring Semester 2026', '2025-11-26', 'International student application deadline listed in the 2025-2026 academic calendar.', 'MEU-S015'),
        ('meu-deadline-fall-2026-international-application', '2025-2026', 'FINAL', 'Fall Semester 2026', '2026-06-24', 'International student application deadline listed in the 2025-2026 academic calendar.', 'MEU-S015'),
        ('meu-deadline-spring-2027-international-application', '2026-2027', 'FINAL', 'Spring Semester 2027', '2026-12-11', 'International student application deadline listed in the 2026-2027 academic calendar.', 'MEU-S016'),
        ('meu-deadline-fall-2027-international-application', '2026-2027', 'FINAL', 'Fall Semester 2027', '2027-07-30', 'International student application deadline listed in the 2026-2027 academic calendar.', 'MEU-S016');

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
        d.record_key,
        d.academic_year,
        d.deadline_type,
        d.term,
        d.deadline_date,
        d.note,
        s.id
    FROM meu_deadline_seed d
    JOIN source s
      ON s.university_id = v_university_id
     AND s.url = (SELECT url FROM meu_source_seed WHERE source_id = d.source_id)
    ON CONFLICT (university_id, record_key) DO UPDATE SET
        academic_year = EXCLUDED.academic_year,
        deadline_type = EXCLUDED.deadline_type,
        term = EXCLUDED.term,
        deadline_date = EXCLUDED.deadline_date,
        note = EXCLUDED.note,
        source_id = EXCLUDED.source_id,
        updated_at = NOW();

    CREATE TEMP TABLE meu_scholarship_seed (
        record_key TEXT PRIMARY KEY,
        name TEXT NOT NULL,
        description TEXT,
        coverage TEXT,
        notes TEXT,
        source_id TEXT NOT NULL
    ) ON COMMIT DROP;

    INSERT INTO meu_scholarship_seed (record_key, name, description, coverage, notes, source_id)
    VALUES
        ('meu-scholarship-merit-tuition', 'Merit Tuition Scholarship', 'Merit tuition scholarship for new and current graduate students.', 'Merit-based tuition support', 'Published on the financial aid and scholarships page.', 'MEU-S011'),
        ('meu-scholarship-general-support', 'Scholarship support for graduate students', 'Scholarship support for eligible graduate students.', 'Graduate scholarship support', 'Published on the financial aid and scholarships page.', 'MEU-S011');

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
        s.record_key,
        NULL,
        s.name,
        s.description,
        s.coverage,
        NULL,
        NULL,
        s.notes,
        src.id
    FROM meu_scholarship_seed s
    JOIN source src
      ON src.university_id = v_university_id
     AND src.url = (SELECT url FROM meu_source_seed WHERE source_id = s.source_id)
    ON CONFLICT (university_id, record_key) DO UPDATE SET
        name = EXCLUDED.name,
        description = EXCLUDED.description,
        coverage = EXCLUDED.coverage,
        notes = EXCLUDED.notes,
        source_id = EXCLUDED.source_id,
        updated_at = NOW();

    CREATE TEMP TABLE meu_financial_aid_seed (
        record_key TEXT PRIMARY KEY,
        name TEXT NOT NULL,
        description TEXT,
        notes TEXT,
        source_id TEXT NOT NULL
    ) ON COMMIT DROP;

    INSERT INTO meu_financial_aid_seed (record_key, name, description, notes, source_id)
    VALUES
        ('meu-aid-needs-based', 'Needs-Based Financial Aid', 'Needs-based financial aid up to 40 percent of tuition only for eligible Lebanese undergraduate and graduate students.', 'Published on the financial aid and scholarships page.', 'MEU-S011'),
        ('meu-aid-work-study', 'Work-Study Program', 'Work-study support for eligible graduate students.', 'Published on the financial aid and scholarships page.', 'MEU-S011'),
        ('meu-aid-family-discount', 'Family Discount', 'Family discount support for tuition paid only.', 'Published on the financial aid and scholarships page.', 'MEU-S011');

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
        f.record_key,
        NULL,
        f.name,
        f.description,
        NULL,
        NULL,
        f.notes,
        src.id
    FROM meu_financial_aid_seed f
    JOIN source src
      ON src.university_id = v_university_id
     AND src.url = (SELECT url FROM meu_source_seed WHERE source_id = f.source_id)
    ON CONFLICT (university_id, record_key) DO UPDATE SET
        name = EXCLUDED.name,
        description = EXCLUDED.description,
        notes = EXCLUDED.notes,
        source_id = EXCLUDED.source_id,
        updated_at = NOW();

    CREATE TEMP TABLE meu_payment_plan_seed (
        record_key TEXT PRIMARY KEY,
        name TEXT NOT NULL,
        description TEXT,
        installments_count INTEGER,
        down_payment_amount NUMERIC(12, 2),
        down_payment_currency TEXT,
        interval_label TEXT,
        notes TEXT,
        source_id TEXT NOT NULL
    ) ON COMMIT DROP;

    INSERT INTO meu_payment_plan_seed (record_key, name, description, installments_count, down_payment_amount, down_payment_currency, interval_label, notes, source_id)
    VALUES
        ('meu-payment-plan-business-office-arrangement', 'Business Office payment arrangement', 'Students pay tuition and fees in full during registration or make alternative arrangements with the Business Office.', NULL, NULL, NULL, NULL, 'No formal installment schedule was published in the reviewed official sources.', 'MEU-S010');

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
        p.record_key,
        '2025-2026',
        p.name,
        p.description,
        p.installments_count,
        p.down_payment_amount,
        p.down_payment_currency,
        p.interval_label,
        p.notes,
        s.id
    FROM meu_payment_plan_seed p
    JOIN source s
      ON s.university_id = v_university_id
     AND s.url = (SELECT url FROM meu_source_seed WHERE source_id = p.source_id)
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

    CREATE TEMP TABLE meu_accreditation_seed (
        record_key TEXT PRIMARY KEY,
        name TEXT NOT NULL,
        authority TEXT,
        status TEXT,
        notes TEXT,
        source_id TEXT NOT NULL
    ) ON COMMIT DROP;

    INSERT INTO meu_accreditation_seed (record_key, name, authority, status, notes, source_id)
    VALUES
        ('meu-accreditation-overview', 'Accreditations and Affiliations', 'Middle East University', 'PUBLISHED', 'The homepage indicates that the university is accredited and affiliated, but the accessible excerpt does not enumerate the specific bodies.', 'MEU-S001');

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
    SELECT
        v_university_id,
        NULL,
        NULL,
        NULL,
        'UNIVERSITY',
        a.record_key,
        a.name,
        a.authority,
        a.status,
        NULL,
        NULL,
        a.notes,
        s.id
    FROM meu_accreditation_seed a
    JOIN source s
      ON s.university_id = v_university_id
     AND s.url = (SELECT url FROM meu_source_seed WHERE source_id = a.source_id)
    ON CONFLICT (university_id, record_key) DO UPDATE SET
        name = EXCLUDED.name,
        authority = EXCLUDED.authority,
        status = EXCLUDED.status,
        notes = EXCLUDED.notes,
        source_id = EXCLUDED.source_id,
        updated_at = NOW();

    CREATE TEMP TABLE meu_alias_seed (
        program_key TEXT NOT NULL,
        alias_type TEXT NOT NULL,
        alias TEXT NOT NULL,
        source_id TEXT NOT NULL,
        note TEXT
    ) ON COMMIT DROP;

    INSERT INTO meu_alias_seed (program_key, alias_type, alias, source_id, note)
    VALUES
        ('meu-fba-master-business-administration', 'DISPLAY_NAME', 'Business Administration', 'MEU-S003', 'Official page heading for the MBA.'),
        ('meu-fe-master-education', 'DISPLAY_NAME', 'Education', 'MEU-S004', 'Official page heading for the MA Education.'),
        ('meu-ft-master-islamic-studies', 'DISPLAY_NAME', 'Islamic Studies', 'MEU-S005', 'Official page heading for the MA Islamic Studies.'),
        ('meu-fe-master-teaching', 'DISPLAY_NAME', 'Teaching', 'MEU-S006', 'Official page heading for the MA Teaching.');

    INSERT INTO graduate_program_alias (
        university_id,
        program_id,
        alias_type,
        alias,
        source_id,
        note
    )
    SELECT
        v_university_id,
        gp.id,
        a.alias_type,
        a.alias,
        s.id,
        a.note
    FROM meu_alias_seed a
    JOIN graduate_program gp
      ON gp.university_id = v_university_id
     AND gp.program_key = a.program_key
    JOIN source s
      ON s.university_id = v_university_id
     AND s.url = (SELECT url FROM meu_source_seed WHERE source_id = a.source_id)
    ON CONFLICT (university_id, alias_type, alias) DO UPDATE SET
        program_id = EXCLUDED.program_id,
        source_id = EXCLUDED.source_id,
        note = EXCLUDED.note,
        updated_at = NOW();

END $$;
