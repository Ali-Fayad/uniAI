-- MUB graduate data seed migration.
-- Idempotent import for the canonical MUB graduate dataset.

DO $$
DECLARE
    v_university_id BIGINT;
BEGIN

    INSERT INTO university (name, name_ar, acronym, country, city, latitude, longitude, campus_name, campus_type)
    SELECT 'Makassed University of Beirut', NULL, 'MUB', 'Lebanon', NULL, NULL, NULL, NULL, NULL
    WHERE NOT EXISTS (SELECT 1 FROM university WHERE name = 'Makassed University of Beirut');

    SELECT id INTO v_university_id
    FROM university
    WHERE name = 'Makassed University of Beirut'
    ORDER BY id
    LIMIT 1;

    INSERT INTO degree_type (code, name) VALUES
        ('MASTER', 'Master'),
        ('PHD', 'Doctor of Philosophy'),
        ('DIPLOMA', 'Diploma'),
        ('CERTIFICATE', 'Certificate')
    ON CONFLICT (code) DO UPDATE SET
        name = EXCLUDED.name,
        updated_at = NOW();

    INSERT INTO language (name, code, native_name) VALUES
        ('English', 'en', 'English'),
        ('French', 'fr', 'Français'),
        ('Arabic', 'ar', 'العربية'),
        ('Multilingual', 'multi', 'Multilingual')
    ON CONFLICT (code) DO UPDATE SET
        name = EXCLUDED.name,
        native_name = EXCLUDED.native_name;

    INSERT INTO source (university_id, title, url, source_type, accessed_at, notes) VALUES
        (v_university_id, 'Home | Makassed University of Beirut', 'https://mub.edu.lb/', 'official_webpage', '2026-07-06', 'Official MUB homepage used to confirm institution identity and navigation.'),
        (v_university_id, 'Faculties | Makassed University of Beirut', 'https://mub.edu.lb/faculties/', 'official_webpage', '2026-07-06', 'Official faculty index listing the university faculties.'),
        (v_university_id, 'Faculty of Islamic Studies | Makassed University of Beirut', 'https://mub.edu.lb/faculty-of-islamic-studies/', 'official_program_page', '2026-07-06', 'Primary official evidence for the master and doctoral Islamic Studies programs, admissions requirements, focus areas, and program structure.'),
        (v_university_id, 'Tuition fees | Makassed University of Beirut', 'https://mub.edu.lb/tuition-fees/', 'official_financial_page', '2026-07-06', 'Official tuition page listing graduate fee rows for the Faculty of Islamic Studies master and PhD programs.'),
        (v_university_id, 'Apply Online | Makassed University of Beirut', 'https://mub.edu.lb/apply-online/', 'official_admissions_form', '2026-07-06', 'Official online application form used to support graduate admissions and document evidence.'),
        (v_university_id, 'Financial Support | Makassed University of Beirut', 'https://mub.edu.lb/financial-support/', 'official_financial_support_page', '2026-07-06', 'Official financial support form.'),
        (v_university_id, 'Academic Calendar | Makassed University of Beirut', 'https://mub.edu.lb/news-and-events/academic-calendar/', 'official_calendar_page', '2026-07-06', 'Official 2025-2026 academic calendar / holiday page.'),
        (v_university_id, 'Faculty of Nursing and Health Sciences | Makassed University of Beirut', 'https://mub.edu.lb/faculty-of-nursing-and-health-sciences/', 'official_faculty_page', '2026-07-06', 'Reviewed during discovery; only weak mission-level graduate wording, not inventoried.'),
        (v_university_id, 'Faculty of Teacher Education | Makassed University of Beirut', 'https://mub.edu.lb/faculties/the-faculty-of-teacher-education/', 'official_faculty_page', '2026-07-06', 'Reviewed during discovery; contained only out-of-scope diploma and certificate offerings.')
    ON CONFLICT (university_id, url) DO UPDATE SET
        title = EXCLUDED.title,
        source_type = EXCLUDED.source_type,
        accessed_at = EXCLUDED.accessed_at,
        notes = EXCLUDED.notes,
        updated_at = NOW();

    INSERT INTO university_faculty (university_id, name, short_name, faculty_type, official_url, notes) VALUES
        (v_university_id, 'Faculty of Islamic Studies', NULL, 'FACULTY', NULL, 'Imported from the official MUB graduate inventory.')
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
        'mub-islamic-studies-master',
        'Islamic Studies',
        'Islamic Studies',
        'Master''s Degree in Islamic Studies',
        'THESIS',
        NULL,
        4,
        'SEMESTERS',
        NULL,
        NULL,
        'A graduate program in Islamic Studies that spans four semesters and is organized into preparatory and regular phases, with the fourth semester dedicated to research and thesis defense.',
        NULL,
        src.id,
        'Official graduate evidence is on the Faculty of Islamic Studies page. The page describes the master structure, admission criteria, and eight focus areas. No standalone dedicated program page was found, so official_program_url is null.'
    FROM university_faculty fac
    JOIN degree_type dt ON dt.code = 'MASTER'
    JOIN source src ON src.university_id = v_university_id AND src.url = 'https://mub.edu.lb/faculty-of-islamic-studies/'
    WHERE fac.university_id = v_university_id
      AND fac.name = 'Faculty of Islamic Studies'
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
        'mub-islamic-studies-phd',
        'Islamic Studies',
        'Islamic Studies',
        'Doctoral Program in Islamic Studies',
        'THESIS',
        NULL,
        3,
        'YEARS',
        NULL,
        NULL,
        'A doctoral program in Islamic Studies that requires a minimum of three years of study focused on thesis preparation.',
        NULL,
        src.id,
        'Official doctoral evidence is on the Faculty of Islamic Studies page. The page describes doctoral duration and admission criteria. No standalone dedicated program page was found, so official_program_url is null.'
    FROM university_faculty fac
    JOIN degree_type dt ON dt.code = 'PHD'
    JOIN source src ON src.university_id = v_university_id AND src.url = 'https://mub.edu.lb/faculty-of-islamic-studies/'
    WHERE fac.university_id = v_university_id
      AND fac.name = 'Faculty of Islamic Studies'
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
        university_id, faculty_id, department_id, program_id, scope_level, record_key, academic_year,
        currency, billing_basis, amount, category, notes, source_id
    ) VALUES
        (
            v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'mub-tuition-master-program-total', '2025-2026',
            'USD', 'PER_PROGRAM', 2950, 'Tuition',
            'Tuition fees for first and second year Master Diploma published on the official Faculty of Islamic Studies fee page.',
            (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://mub.edu.lb/tuition-fees/' LIMIT 1)
        ),
        (
            v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'mub-tuition-phd-program-total', '2025-2026',
            'USD', 'PER_PROGRAM', 6704, 'Tuition',
            'PHD Diploma tuition fees published on the official Faculty of Islamic Studies fee page.',
            (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://mub.edu.lb/tuition-fees/' LIMIT 1)
        ),
        (
            v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'mub-tuition-credit-rate', '2025-2026',
            'USD', 'PER_CREDIT', 120, 'Tuition',
            'Fee per one credit published on the official Faculty of Islamic Studies fee page.',
            (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://mub.edu.lb/tuition-fees/' LIMIT 1)
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
        university_id, faculty_id, department_id, program_id, scope_level, record_key, academic_year,
        fee_name, billing_basis, currency, amount, category, notes, source_id
    ) VALUES
        (
            v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'mub-fee-master-application', '2025-2026',
            'Application Fees for Master Diploma', 'FLAT_FEE', 'USD', 25, 'Admissions',
            'Application fee published on the official Faculty of Islamic Studies fee page.',
            (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://mub.edu.lb/tuition-fees/' LIMIT 1)
        ),
        (
            v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'mub-fee-master-registration', '2025-2026',
            'Registration fees/printed matter for Master Diploma', 'FLAT_FEE', 'USD', 100, 'Admissions',
            'Registration fee / printed matter published on the official Faculty of Islamic Studies fee page.',
            (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://mub.edu.lb/tuition-fees/' LIMIT 1)
        ),
        (
            v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'mub-fee-master-defense', '2025-2026',
            'Defense tuition fee for Master Diploma', 'FLAT_FEE', 'USD', 1300, 'Defense',
            'Defense tuition fee published on the official Faculty of Islamic Studies fee page.',
            (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://mub.edu.lb/tuition-fees/' LIMIT 1)
        ),
        (
            v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'mub-fee-phd-application', '2025-2026',
            'Application Fees for PHD Diploma', 'FLAT_FEE', 'USD', 25, 'Admissions',
            'Application fee published on the official Faculty of Islamic Studies fee page.',
            (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://mub.edu.lb/tuition-fees/' LIMIT 1)
        ),
        (
            v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'mub-fee-phd-registration', '2025-2026',
            'Registration Fees/printed matter for PHD Diploma', 'FLAT_FEE', 'USD', 150, 'Admissions',
            'Registration fee / printed matter published on the official Faculty of Islamic Studies fee page.',
            (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://mub.edu.lb/tuition-fees/' LIMIT 1)
        ),
        (
            v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'mub-fee-phd-defense', '2025-2026',
            'Defense tuition fee for PHD Diploma', 'FLAT_FEE', 'USD', 3250, 'Defense',
            'Defense tuition fee published on the official Faculty of Islamic Studies fee page.',
            (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://mub.edu.lb/tuition-fees/' LIMIT 1)
        )
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
        university_id, faculty_id, department_id, program_id, scope_level, record_key,
        requirement_type, requirement_text, comparison_operator, threshold_value, threshold_unit,
        is_required, notes, source_id
    ) VALUES
        (
            v_university_id, NULL, NULL,
            (SELECT id FROM graduate_program WHERE university_id = v_university_id AND program_key = 'mub-islamic-studies-master' LIMIT 1),
            'PROGRAM', 'mub-adm-master-general', 'GENERAL',
            'Applicants must hold a bachelor''s degree with a minimum grade of Pass or higher, demonstrate acceptable oration and linguistic communication skills, submit a personal statement for graduate study in Islamic Studies, and complete a personal interview.',
            NULL, NULL, NULL, TRUE,
            'Consolidated from the official Faculty of Islamic Studies master admission section.',
            (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://mub.edu.lb/faculty-of-islamic-studies/' LIMIT 1)
        ),
        (
            v_university_id, NULL, NULL,
            (SELECT id FROM graduate_program WHERE university_id = v_university_id AND program_key = 'mub-islamic-studies-phd' LIMIT 1),
            'PROGRAM', 'mub-adm-phd-general', 'ACADEMIC',
            'Applicants must hold a master''s degree in Islamic studies or equivalent with at least a good grade.',
            NULL, NULL, NULL, TRUE,
            'Consolidated from the official Faculty of Islamic Studies doctoral admission section.',
            (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://mub.edu.lb/faculty-of-islamic-studies/' LIMIT 1)
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
        university_id, faculty_id, department_id, program_id, scope_level, record_key,
        document_type, document_name, is_optional, sort_order, notes, source_id
    ) VALUES
        (
            v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'mub-doc-id-passport',
            'IDENTITY', 'ID card or passport information', FALSE, 1,
            'Explicitly requested in the official application form.',
            (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://mub.edu.lb/apply-online/' LIMIT 1)
        ),
        (
            v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'mub-doc-bachelor-degree',
            'ACADEMIC_RECORD', 'Proof of a bachelor''s degree for the master''s program', FALSE, 2,
            'Explicitly required on the Faculty of Islamic Studies master admissions page.',
            (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://mub.edu.lb/faculty-of-islamic-studies/' LIMIT 1)
        ),
        (
            v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'mub-doc-personal-statement',
            'APPLICATION', 'Personal statement', FALSE, 3,
            'Explicitly required on the Faculty of Islamic Studies master admissions page.',
            (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://mub.edu.lb/faculty-of-islamic-studies/' LIMIT 1)
        ),
        (
            v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'mub-doc-phd-masters-degree',
            'ACADEMIC_RECORD', 'Master''s degree or equivalent for doctoral applicants', FALSE, 4,
            'Explicitly required on the Faculty of Islamic Studies doctoral admissions page.',
            (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://mub.edu.lb/faculty-of-islamic-studies/' LIMIT 1)
        )
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
        university_id, faculty_id, department_id, program_id, scope_level, record_key, academic_year,
        deadline_type, term, deadline_date, note, source_id
    ) VALUES
        (
            v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'mub-deadline-academic-calendar-2025-2026',
            '2025-2026', 'OTHER', '2025-2026', NULL,
            'University academic calendar / holiday page published; no separate graduate admissions deadline was found in the reviewed official sources.',
            (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://mub.edu.lb/news-and-events/academic-calendar/' LIMIT 1)
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

    INSERT INTO graduate_financial_aid (
        university_id, faculty_id, department_id, program_id, scope_level, record_key, academic_year,
        name, description, amount, currency, notes, source_id
    ) VALUES
        (
            v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'mub-financial-aid-support-form',
            NULL, 'Graduate financial support request', 'The university publishes a financial support form, but no graduate-wide aid table or award amounts were published.', NULL, NULL,
            'Financial support form published on the official university site.',
            (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://mub.edu.lb/financial-support/' LIMIT 1)
        )
    ON CONFLICT (university_id, record_key) DO UPDATE SET
        faculty_id = EXCLUDED.faculty_id,
        department_id = EXCLUDED.department_id,
        program_id = EXCLUDED.program_id,
        scope_level = EXCLUDED.scope_level,
        academic_year = EXCLUDED.academic_year,
        name = EXCLUDED.name,
        description = EXCLUDED.description,
        amount = EXCLUDED.amount,
        currency = EXCLUDED.currency,
        notes = EXCLUDED.notes,
        source_id = EXCLUDED.source_id,
        updated_at = NOW();

    INSERT INTO graduate_program_track (
        university_id, program_id, track_type, track_name, track_order, is_primary, description, source_id, notes
    ) VALUES
        (
            v_university_id,
            (SELECT id FROM graduate_program WHERE university_id = v_university_id AND program_key = 'mub-islamic-studies-master' LIMIT 1),
            'CONCENTRATION', 'Charitable Institutions Management', 1, TRUE, NULL,
            (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://mub.edu.lb/faculty-of-islamic-studies/' LIMIT 1),
            'One of the eight focus areas listed on the Faculty of Islamic Studies page.'
        ),
        (
            v_university_id,
            (SELECT id FROM graduate_program WHERE university_id = v_university_id AND program_key = 'mub-islamic-studies-master' LIMIT 1),
            'CONCENTRATION', 'Media and Publishing', 2, FALSE, NULL,
            (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://mub.edu.lb/faculty-of-islamic-studies/' LIMIT 1),
            'One of the eight focus areas listed on the Faculty of Islamic Studies page.'
        ),
        (
            v_university_id,
            (SELECT id FROM graduate_program WHERE university_id = v_university_id AND program_key = 'mub-islamic-studies-master' LIMIT 1),
            'CONCENTRATION', 'Scientific Research and Academia', 3, FALSE, NULL,
            (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://mub.edu.lb/faculty-of-islamic-studies/' LIMIT 1),
            'One of the eight focus areas listed on the Faculty of Islamic Studies page.'
        ),
        (
            v_university_id,
            (SELECT id FROM graduate_program WHERE university_id = v_university_id AND program_key = 'mub-islamic-studies-master' LIMIT 1),
            'CONCENTRATION', 'Religious Relations and Conflict Resolution', 4, FALSE, NULL,
            (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://mub.edu.lb/faculty-of-islamic-studies/' LIMIT 1),
            'One of the eight focus areas listed on the Faculty of Islamic Studies page.'
        ),
        (
            v_university_id,
            (SELECT id FROM graduate_program WHERE university_id = v_university_id AND program_key = 'mub-islamic-studies-master' LIMIT 1),
            'CONCENTRATION', 'Financial Management and Economic Development', 5, FALSE, NULL,
            (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://mub.edu.lb/faculty-of-islamic-studies/' LIMIT 1),
            'One of the eight focus areas listed on the Faculty of Islamic Studies page.'
        ),
        (
            v_university_id,
            (SELECT id FROM graduate_program WHERE university_id = v_university_id AND program_key = 'mub-islamic-studies-master' LIMIT 1),
            'CONCENTRATION', 'Family Affairs', 6, FALSE, NULL,
            (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://mub.edu.lb/faculty-of-islamic-studies/' LIMIT 1),
            'One of the eight focus areas listed on the Faculty of Islamic Studies page.'
        ),
        (
            v_university_id,
            (SELECT id FROM graduate_program WHERE university_id = v_university_id AND program_key = 'mub-islamic-studies-master' LIMIT 1),
            'CONCENTRATION', 'Cultural Documentation and Manuscript Management', 7, FALSE, NULL,
            (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://mub.edu.lb/faculty-of-islamic-studies/' LIMIT 1),
            'One of the eight focus areas listed on the Faculty of Islamic Studies page.'
        ),
        (
            v_university_id,
            (SELECT id FROM graduate_program WHERE university_id = v_university_id AND program_key = 'mub-islamic-studies-master' LIMIT 1),
            'CONCENTRATION', 'Education', 8, FALSE, NULL,
            (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://mub.edu.lb/faculty-of-islamic-studies/' LIMIT 1),
            'One of the eight focus areas listed on the Faculty of Islamic Studies page.'
        )
    ON CONFLICT (program_id, track_type, track_name) DO UPDATE SET
        track_order = EXCLUDED.track_order,
        is_primary = EXCLUDED.is_primary,
        description = EXCLUDED.description,
        source_id = EXCLUDED.source_id,
        notes = EXCLUDED.notes,
        updated_at = NOW();

    INSERT INTO graduate_program_source (
        university_id, program_id, source_id, source_role, source_order, evidence_text, notes
    ) VALUES
        (
            v_university_id,
            (SELECT id FROM graduate_program WHERE university_id = v_university_id AND program_key = 'mub-islamic-studies-master' LIMIT 1),
            (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://mub.edu.lb/faculty-of-islamic-studies/' LIMIT 1),
            'PRIMARY', 1,
            'Faculty page explicitly states the faculty offers master’s and doctoral programs and describes the master structure, admissions, and focus areas.',
            'Primary graduate evidence.'
        ),
        (
            v_university_id,
            (SELECT id FROM graduate_program WHERE university_id = v_university_id AND program_key = 'mub-islamic-studies-master' LIMIT 1),
            (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://mub.edu.lb/tuition-fees/' LIMIT 1),
            'TUITION', 2,
            'Tuition page lists 2025-2026 graduate tuition rows for the Faculty of Islamic Studies master program.',
            'Supporting tuition evidence.'
        ),
        (
            v_university_id,
            (SELECT id FROM graduate_program WHERE university_id = v_university_id AND program_key = 'mub-islamic-studies-phd' LIMIT 1),
            (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://mub.edu.lb/faculty-of-islamic-studies/' LIMIT 1),
            'PRIMARY', 1,
            'Faculty page explicitly states the faculty offers doctoral programs and describes the doctoral program and admissions.',
            'Primary graduate evidence.'
        ),
        (
            v_university_id,
            (SELECT id FROM graduate_program WHERE university_id = v_university_id AND program_key = 'mub-islamic-studies-phd' LIMIT 1),
            (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://mub.edu.lb/tuition-fees/' LIMIT 1),
            'TUITION', 2,
            'Tuition page lists 2025-2026 graduate tuition rows for the Faculty of Islamic Studies doctoral program.',
            'Supporting tuition evidence.'
        )
    ON CONFLICT (program_id, source_id, source_role) DO UPDATE SET
        source_order = EXCLUDED.source_order,
        evidence_text = EXCLUDED.evidence_text,
        notes = EXCLUDED.notes,
        updated_at = NOW();

END $$;
