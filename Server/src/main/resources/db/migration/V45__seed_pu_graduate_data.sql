-- PU graduate data seed migration.
-- Idempotent import for the canonical PU graduate dataset.

DO $$
DECLARE
    v_university_id BIGINT;
BEGIN

    INSERT INTO university (name, name_ar, acronym, country, city, latitude, longitude, campus_name, campus_type)
    SELECT 'Phoenicia University', NULL, 'PU', 'Lebanon', NULL, NULL, NULL, NULL, NULL
    WHERE NOT EXISTS (SELECT 1 FROM university WHERE name = 'Phoenicia University');

    SELECT id INTO v_university_id FROM university WHERE name = 'Phoenicia University' ORDER BY id LIMIT 1;

    INSERT INTO degree_type (code, name) VALUES
        ('MASTER', 'Master'),
        ('PHD', 'Doctor of Philosophy'),
        ('DIPLOMA', 'Diploma'),
        ('CERTIFICATE', 'Certificate')
    ON CONFLICT (code) DO UPDATE SET name = EXCLUDED.name, updated_at = NOW();

    INSERT INTO language (name, code, native_name) VALUES
        ('English', 'en', 'English'),
        ('French', 'fr', 'Français'),
        ('Arabic', 'ar', 'العربية'),
        ('Multilingual', 'multi', 'Multilingual')
    ON CONFLICT (code) DO UPDATE SET name = EXCLUDED.name, native_name = EXCLUDED.native_name;

    INSERT INTO source (university_id, title, url, source_type, accessed_at, notes) VALUES
        (v_university_id, 'Home | Phoenicia University', 'https://pu.edu.lb/', 'official_webpage', '2026-07-05', 'Canonical official PU website encountered during discovery; page includes PU identity, colleges, contact details, and top-level navigation.'),
        (v_university_id, 'Admission Requirements | Phoenicia University', 'https://pu.edu.lb/admission-requirements', 'official_webpage', '2026-07-05', 'Admissions index listing graduate MBA and graduate LL.M. admission requirement pages.'),
        (v_university_id, 'Graduate – Master of Business Administration (MBA) | Phoenicia University', 'https://pu.edu.lb/graduate-master-business-administration', 'official_webpage', '2026-07-05', 'Graduate MBA admission requirements, required documents, interview requirement, application fee, English proficiency, foundation courses, and credit transfer notes.'),
        (v_university_id, 'Master of Business Administration (MBA) | Phoenicia University', 'https://pu.edu.lb/master-business-administration', 'official_webpage', '2026-07-05', 'MBA program overview, objectives, outcomes, and degree plan/curriculum links.'),
        (v_university_id, 'Graduate – Master of Laws (LL.M.) Program | Phoenicia University', 'https://pu.edu.lb/graduate-master-laws-llm-program', 'official_webpage', '2026-07-05', 'Graduate LL.M. admission requirements, required documents, interview requirement, application fee, English proficiency, language score thresholds, and enrollment options.'),
        (v_university_id, 'Master of Laws (LL.M.): Commercial Regulation, Litigation and Arbitration | Phoenicia University', 'https://pu.edu.lb/master-laws-llm-commercial-regulation-litigation-and-arbitration', 'official_webpage', '2026-07-05', 'LL.M. program overview, focus area, objectives, outcomes, and degree plan/curriculum links.'),
        (v_university_id, 'Majors and Degrees | Phoenicia University', 'https://pu.edu.lb/majors-and-degrees', 'official_webpage', '2026-07-05', 'Majors and degrees page listing Master of Laws (LL.M.) and Master of Business Administration (MBA) among PU degrees.'),
        (v_university_id, 'Graduate Tuition and Fees | Phoenicia University', 'https://pu.edu.lb/graduate-tuition-and-fees', 'official_webpage', '2026-07-05', 'Graduate tuition page listing per-credit fees for MBA and LL.M.; notes tuition is paid in fresh dollars.'),
        (v_university_id, 'Financial Aid and Scholarship | Phoenicia University', 'https://pu.edu.lb/financial-aid-and-scholarship', 'official_webpage', '2026-07-05', 'Financial aid page describing need-based grants, loans, work-study, merit scholarships, application access, and contact email.'),
        (v_university_id, 'Full Registrar Calendar | Phoenicia University', 'https://pu.edu.lb/full-registrar-calendar', 'official_webpage', '2026-07-05', 'Registrar calendar with MBA/LLM semester dates, registration/payment deadlines, financial aid deadlines, deferral deadlines, and installment payment deadlines.'),
        (v_university_id, 'College of Law and Political Science | Phoenicia University', 'https://pu.edu.lb/college-of-law-and-political-science', 'official_webpage', '2026-07-05', 'College page explicitly listing Master of Laws (LL.M.) under the College of Law and Political Science.'),
        (v_university_id, 'Self-Service - Apply | Phoenicia University', 'https://pusis.pu.edu.lb/SelfService/Admissions/ApplicationSelectEnabledForm.aspx', 'official_webpage', '2026-07-05', 'Official PU self-service admissions page listing Graduate Student Application (MBA) and Graduate Student Application (Master of Laws - LLM).')
    ON CONFLICT (university_id, url) DO UPDATE SET
        title = EXCLUDED.title,
        source_type = EXCLUDED.source_type,
        accessed_at = EXCLUDED.accessed_at,
        notes = EXCLUDED.notes,
        updated_at = NOW();

    INSERT INTO university_faculty (university_id, name, short_name, faculty_type, official_url, notes) VALUES
        (v_university_id, 'College of Business', NULL, 'SCHOOL', NULL, 'Imported from the official PU graduate inventory.'),
        (v_university_id, 'College of Law and Political Science', NULL, 'SCHOOL', NULL, 'Imported from the official PU graduate inventory.')
    ON CONFLICT (university_id, name) DO UPDATE SET
        short_name = EXCLUDED.short_name,
        faculty_type = EXCLUDED.faculty_type,
        official_url = EXCLUDED.official_url,
        notes = EXCLUDED.notes,
        updated_at = NOW();

    INSERT INTO graduate_program (
        university_id, faculty_id, department_id, degree_type_id, program_key, major_category, major,
        official_degree_name, thesis_or_non_thesis, credits, duration_value, duration_unit,
        primary_language_id, delivery_mode, program_description, official_program_url, source_id, notes
    )
    SELECT
        v_university_id,
        fac.id,
        NULL,
        dt.id,
        'pu-mba',
        'Business Administration',
        'Business Administration',
        'Master of Business Administration (MBA)',
        NULL,
        NULL,
        NULL,
        NULL,
        NULL,
        NULL,
        'PU''s MBA program focuses on Data Science and Artificial Intelligence and is presented as the university''s targeted graduate business program.',
        'https://pu.edu.lb/master-business-administration',
        src.id,
        'Official PU pages identify the MBA as a graduate program in the College of Business, with admissions, curriculum, tuition, and self-service application evidence.'
    FROM university_faculty fac
    JOIN degree_type dt ON dt.code = 'MASTER'
    JOIN source src ON src.university_id = v_university_id AND src.url = 'https://pu.edu.lb/master-business-administration'
    WHERE fac.university_id = v_university_id
      AND fac.name = 'College of Business'
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

    INSERT INTO graduate_program (
        university_id, faculty_id, department_id, degree_type_id, program_key, major_category, major,
        official_degree_name, thesis_or_non_thesis, credits, duration_value, duration_unit,
        primary_language_id, delivery_mode, program_description, official_program_url, source_id, notes
    )
    SELECT
        v_university_id,
        fac.id,
        NULL,
        dt.id,
        'pu-llm',
        'Law',
        'Commercial Regulation, Litigation and Arbitration',
        'Master of Laws (LL.M.)',
        NULL,
        NULL,
        NULL,
        NULL,
        NULL,
        NULL,
        'PU''s LL.M. program focuses on Commercial Regulation, Litigation, and Arbitration and is presented as the university''s targeted graduate law program.',
        'https://pu.edu.lb/master-laws-llm-commercial-regulation-litigation-and-arbitration',
        src.id,
        'Official PU pages identify the LL.M. as a graduate program in the College of Law and Political Science, with admissions, curriculum, tuition, and self-service application evidence.'
    FROM university_faculty fac
    JOIN degree_type dt ON dt.code = 'MASTER'
    JOIN source src ON src.university_id = v_university_id AND src.url = 'https://pu.edu.lb/master-laws-llm-commercial-regulation-litigation-and-arbitration'
    WHERE fac.university_id = v_university_id
      AND fac.name = 'College of Law and Political Science'
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

    INSERT INTO graduate_tuition_rate (
        university_id, faculty_id, department_id, program_id, scope_level, record_key, academic_year, currency,
        billing_basis, amount, category, notes, source_id
    )
    SELECT
        v_university_id,
        fac.id,
        NULL,
        gp.id,
        'PROGRAM',
        'pu-tuition-mba',
        '2025-2026',
        'USD',
        'PER_CREDIT',
        210,
        'College of Business',
        'Official graduate tuition page lists MBA graduate tuition at $210 per credit.',
        src.id
    FROM graduate_program gp
    JOIN university_faculty fac ON fac.id = gp.faculty_id
    JOIN source src ON src.university_id = v_university_id AND src.url = 'https://pu.edu.lb/graduate-tuition-and-fees'
    WHERE gp.university_id = v_university_id
      AND gp.program_key = 'pu-mba'
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

    INSERT INTO graduate_tuition_rate (
        university_id, faculty_id, department_id, program_id, scope_level, record_key, academic_year, currency,
        billing_basis, amount, category, notes, source_id
    )
    SELECT
        v_university_id,
        fac.id,
        NULL,
        gp.id,
        'PROGRAM',
        'pu-tuition-llm',
        '2025-2026',
        'USD',
        'PER_CREDIT',
        200,
        'College of Law and Political Science',
        'Official graduate tuition page lists LL.M. graduate tuition at $200 per credit.',
        src.id
    FROM graduate_program gp
    JOIN university_faculty fac ON fac.id = gp.faculty_id
    JOIN source src ON src.university_id = v_university_id AND src.url = 'https://pu.edu.lb/graduate-tuition-and-fees'
    WHERE gp.university_id = v_university_id
      AND gp.program_key = 'pu-llm'
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
        university_id, faculty_id, department_id, program_id, scope_level, record_key, academic_year, fee_name,
        billing_basis, currency, amount, category, notes, source_id
    ) VALUES
        (v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'pu-fee-application', NULL, 'Graduate application fee', 'FLAT_FEE', 'LBP', 1000000, 'Admissions', 'Non-refundable graduate application fee published on MBA and LL.M. admissions pages.', (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://pu.edu.lb/graduate-master-business-administration' LIMIT 1))
    ON CONFLICT (university_id, record_key) DO UPDATE SET
        fee_name = EXCLUDED.fee_name,
        billing_basis = EXCLUDED.billing_basis,
        currency = EXCLUDED.currency,
        amount = EXCLUDED.amount,
        category = EXCLUDED.category,
        notes = EXCLUDED.notes,
        source_id = EXCLUDED.source_id,
        updated_at = NOW();

    INSERT INTO graduate_admission_requirement (
        university_id, faculty_id, department_id, program_id, scope_level, record_key, requirement_type,
        requirement_text, comparison_operator, threshold_value, threshold_unit, is_required, notes, source_id
    ) VALUES
        (v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'pu-admission-general', 'GENERAL', 'PU publishes graduate MBA and LL.M. admissions pages that direct applicants to complete the online application, submit the required documents, meet English proficiency and interview requirements, and proceed through the admissions committee review process.', NULL, NULL, NULL, TRUE, 'Shared admissions summary from the official graduate admissions pages.', (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://pu.edu.lb/admission-requirements' LIMIT 1))
    ON CONFLICT (university_id, record_key) DO UPDATE SET
        requirement_text = EXCLUDED.requirement_text,
        notes = EXCLUDED.notes,
        source_id = EXCLUDED.source_id,
        updated_at = NOW();

    INSERT INTO graduate_required_document (
        university_id, faculty_id, department_id, program_id, scope_level, record_key, document_type, document_name,
        is_optional, sort_order, notes, source_id
    ) VALUES
        (v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'pu-doc-01', 'APPLICATION', 'Online graduate application form', FALSE, 1, 'Overlapping core document published on the MBA and LL.M. admissions pages.', (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://pu.edu.lb/graduate-master-business-administration' LIMIT 1)),
        (v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'pu-doc-02', 'APPLICATION', 'Photocopy of identity card or passport', FALSE, 2, 'Overlapping core document published on the MBA and LL.M. admissions pages.', (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://pu.edu.lb/graduate-master-business-administration' LIMIT 1)),
        (v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'pu-doc-03', 'APPLICATION', 'One passport-sized photo', FALSE, 3, 'Overlapping core document published on the MBA and LL.M. admissions pages.', (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://pu.edu.lb/graduate-master-business-administration' LIMIT 1)),
        (v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'pu-doc-04', 'APPLICATION', 'Official transcripts with GPA from prior university study', FALSE, 4, 'Overlapping core document published on the MBA and LL.M. admissions pages.', (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://pu.edu.lb/graduate-master-business-administration' LIMIT 1)),
        (v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'pu-doc-05', 'APPLICATION', 'Lebanese Baccalaureate or recognized equivalent', FALSE, 5, 'Overlapping core document published on the MBA and LL.M. admissions pages.', (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://pu.edu.lb/graduate-master-business-administration' LIMIT 1)),
        (v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'pu-doc-06', 'APPLICATION', 'Recommendation letter from a professor or employer', FALSE, 6, 'Overlapping core document published on the MBA and LL.M. admissions pages.', (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://pu.edu.lb/graduate-master-business-administration' LIMIT 1)),
        (v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'pu-doc-07', 'APPLICATION', 'Updated resume', FALSE, 7, 'Overlapping core document published on the MBA and LL.M. admissions pages.', (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://pu.edu.lb/graduate-master-business-administration' LIMIT 1)),
        (v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'pu-doc-08', 'APPLICATION', 'Proof of professional work experience for MBA applicants', FALSE, 8, 'Program-specific MBA requirement published on the MBA admissions page.', (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://pu.edu.lb/graduate-master-business-administration' LIMIT 1)),
        (v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'pu-doc-09', 'APPLICATION', 'Bachelor''s degree equivalency from the Lebanese Ministry of Education and Higher Education for MBA applicants', FALSE, 9, 'Program-specific MBA requirement published on the MBA admissions page.', (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://pu.edu.lb/graduate-master-business-administration' LIMIT 1)),
        (v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'pu-doc-10', 'APPLICATION', 'Bachelor''s degree in Law and official equivalency for LL.M. applicants', FALSE, 10, 'Program-specific LL.M. requirement published on the LL.M. admissions page.', (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://pu.edu.lb/graduate-master-laws-llm-program' LIMIT 1)),
        (v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'pu-doc-11', 'APPLICATION', 'English proficiency evidence where required', FALSE, 11, 'Published on the MBA and LL.M. admissions pages.', (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://pu.edu.lb/graduate-master-business-administration' LIMIT 1))
    ON CONFLICT (university_id, record_key) DO UPDATE SET
        document_name = EXCLUDED.document_name,
        notes = EXCLUDED.notes,
        source_id = EXCLUDED.source_id,
        updated_at = NOW();

    INSERT INTO graduate_admission_deadline (
        university_id, faculty_id, department_id, program_id, scope_level, record_key, academic_year, deadline_type,
        term, deadline_date, note, source_id
    ) VALUES
        (v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'pu-deadline-fall-2025', '2025-2026', 'OTHER', 'Fall 2025-26', NULL, 'Semester and payment deadlines are published in the registrar calendar.', (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://pu.edu.lb/full-registrar-calendar' LIMIT 1))
    ON CONFLICT (university_id, record_key) DO UPDATE SET
        note = EXCLUDED.note,
        source_id = EXCLUDED.source_id,
        updated_at = NOW();

    INSERT INTO graduate_scholarship (
        university_id, faculty_id, department_id, program_id, scope_level, record_key, academic_year, name,
        description, coverage, amount, currency, notes, source_id
    ) VALUES
        (v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'pu-sch-need-based', NULL, 'Need-based financial aid grants', NULL, NULL, NULL, NULL, 'Published on the financial-aid page.', (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://pu.edu.lb/financial-aid-and-scholarship' LIMIT 1)),
        (v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'pu-sch-loans', NULL, 'Loans', NULL, NULL, NULL, NULL, 'Published on the financial-aid page.', (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://pu.edu.lb/financial-aid-and-scholarship' LIMIT 1)),
        (v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'pu-sch-work-study', NULL, 'Work-study program', NULL, NULL, NULL, NULL, 'Published on the financial-aid page.', (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://pu.edu.lb/financial-aid-and-scholarship' LIMIT 1)),
        (v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'pu-sch-merit', NULL, 'Merit scholarships', NULL, NULL, NULL, NULL, 'Published on the financial-aid page.', (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://pu.edu.lb/financial-aid-and-scholarship' LIMIT 1))
    ON CONFLICT (university_id, record_key) DO UPDATE SET
        name = EXCLUDED.name,
        notes = EXCLUDED.notes,
        source_id = EXCLUDED.source_id,
        updated_at = NOW();

    INSERT INTO graduate_financial_aid (
        university_id, faculty_id, department_id, program_id, scope_level, record_key, academic_year, name,
        description, amount, currency, notes, source_id
    ) VALUES
        (v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'pu-fin-aid-committee', NULL, 'Financial aid committee review', 'Need-based grants, loans, work-study, and merit scholarships are reviewed by a financial-aid/scholarship committee.', NULL, NULL, 'Published on the financial-aid page.', (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://pu.edu.lb/financial-aid-and-scholarship' LIMIT 1))
    ON CONFLICT (university_id, record_key) DO UPDATE SET
        name = EXCLUDED.name,
        description = EXCLUDED.description,
        notes = EXCLUDED.notes,
        source_id = EXCLUDED.source_id,
        updated_at = NOW();

    INSERT INTO graduate_payment_plan (
        university_id, faculty_id, department_id, program_id, scope_level, record_key, academic_year, name,
        description, installments_count, down_payment_amount, down_payment_currency, interval_label, notes, source_id
    ) VALUES
        (v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'pu-payment-plan-calendar', '2025-2026', 'Installment deadlines published in registrar calendar', 'The registrar calendar publishes installment-payment deadlines, deferral deadlines, and payment deadlines.', NULL, NULL, NULL, NULL, 'No separate payment-plan policy page was published.', (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://pu.edu.lb/full-registrar-calendar' LIMIT 1))
    ON CONFLICT (university_id, record_key) DO UPDATE SET
        name = EXCLUDED.name,
        description = EXCLUDED.description,
        notes = EXCLUDED.notes,
        source_id = EXCLUDED.source_id,
        updated_at = NOW();

    WITH pu_program_source_seed(program_key, source_url, source_role, source_order, evidence_text, notes) AS (
        VALUES
            ('pu-mba', 'https://pu.edu.lb/admission-requirements', 'PRIMARY', 1, 'Admission Requirements', 'Graduate admissions index listing MBA requirements.'),
            ('pu-mba', 'https://pu.edu.lb/graduate-master-business-administration', 'SECONDARY', 2, 'Graduate MBA', 'MBA admissions page.'),
            ('pu-mba', 'https://pu.edu.lb/master-business-administration', 'SECONDARY', 3, 'MBA program page', 'MBA program overview page.'),
            ('pu-mba', 'https://pu.edu.lb/majors-and-degrees', 'SECONDARY', 4, 'Majors and Degrees', 'Degree list page.'),
            ('pu-mba', 'https://pu.edu.lb/graduate-tuition-and-fees', 'TUITION', 5, 'Graduate Tuition and Fees', 'MBA tuition page.'),
            ('pu-mba', 'https://pusis.pu.edu.lb/SelfService/Admissions/ApplicationSelectEnabledForm.aspx', 'ADMISSIONS', 6, 'Self-Service Apply', 'MBA self-service application listing.'),
            ('pu-mba', 'https://pu.edu.lb/graduate-tuition-and-fees', 'TUITION', 7, 'Graduate Tuition and Fees', 'MBA tuition page.'),
            ('pu-llm', 'https://pu.edu.lb/admission-requirements', 'PRIMARY', 1, 'Admission Requirements', 'Graduate admissions index listing LL.M. requirements.'),
            ('pu-llm', 'https://pu.edu.lb/graduate-master-laws-llm-program', 'SECONDARY', 2, 'Graduate LL.M.', 'LL.M. admissions page.'),
            ('pu-llm', 'https://pu.edu.lb/master-laws-llm-commercial-regulation-litigation-and-arbitration', 'SECONDARY', 3, 'LL.M. program page', 'LL.M. program overview page.'),
            ('pu-llm', 'https://pu.edu.lb/college-of-law-and-political-science', 'SECONDARY', 4, 'College of Law and Political Science', 'College listing page.'),
            ('pu-llm', 'https://pu.edu.lb/majors-and-degrees', 'SECONDARY', 5, 'Majors and Degrees', 'Degree list page.'),
            ('pu-llm', 'https://pu.edu.lb/graduate-tuition-and-fees', 'TUITION', 6, 'Graduate Tuition and Fees', 'LL.M. tuition page.'),
            ('pu-llm', 'https://pusis.pu.edu.lb/SelfService/Admissions/ApplicationSelectEnabledForm.aspx', 'ADMISSIONS', 7, 'Self-Service Apply', 'LL.M. self-service application listing.'),
            ('pu-llm', 'https://pu.edu.lb/graduate-tuition-and-fees', 'TUITION', 8, 'Graduate Tuition and Fees', 'LL.M. tuition page.')
    )
    INSERT INTO graduate_program_source (university_id, program_id, source_id, source_role, source_order, evidence_text, notes)
    SELECT
        v_university_id,
        gp.id,
        s.id,
        seed.source_role,
        seed.source_order,
        seed.evidence_text,
        seed.notes
    FROM pu_program_source_seed seed
    JOIN graduate_program gp ON gp.university_id = v_university_id AND gp.program_key = seed.program_key
    JOIN source s ON s.university_id = v_university_id AND s.url = seed.source_url
    ON CONFLICT (program_id, source_id, source_role) DO UPDATE SET
        source_order = EXCLUDED.source_order,
        evidence_text = EXCLUDED.evidence_text,
        notes = EXCLUDED.notes,
        updated_at = NOW();

END $$;
