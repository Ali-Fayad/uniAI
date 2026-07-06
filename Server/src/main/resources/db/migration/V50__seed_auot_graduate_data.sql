-- AUOT graduate data seed migration.
-- Idempotent import for the canonical AUOT graduate dataset.

DO $$
DECLARE
    v_university_id BIGINT;
BEGIN

    INSERT INTO university (name, name_ar, acronym, country, city, latitude, longitude, campus_name, campus_type)
    SELECT 'American University of Technology', NULL, 'AUOT', 'Lebanon', NULL, NULL, NULL, NULL, NULL
    WHERE NOT EXISTS (SELECT 1 FROM university WHERE name = 'American University of Technology');

    SELECT id INTO v_university_id
    FROM university
    WHERE name = 'American University of Technology'
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
        (v_university_id, 'AUT Homepage', 'https://www.aut.edu/', 'official_page', '2026-07-06', 'Official homepage used to confirm institution identity and footer navigation.'),
        (v_university_id, 'AUT Academics', 'https://www.aut.edu/aut-academics/', 'official_page', '2026-07-06', 'Academics landing page; confirms undergraduate and graduate degree structure.'),
        (v_university_id, 'AUT Admissions', 'https://www.aut.edu/admissions/', 'official_page', '2026-07-06', 'Graduate admission criteria, required documents, tuition, financial aid, and testing notes.'),
        (v_university_id, 'AUT Catalogue 2025-2026', 'https://www.aut.edu/wp-content/uploads/2025/10/Updated-Catalogue-2025-2026-.pdf', 'official_pdf', '2026-07-06', 'Primary current catalog evidence for AUT graduate programs, fees, regulations, aid, international students, and accreditation.'),
        (v_university_id, 'Master of Science in Computer Science', 'https://www.aut.edu/computer-science-master/', 'official_program_page', '2026-07-06', 'Official program page for the Master of Science in Computer Science.'),
        (v_university_id, 'LLM Program "Masters of Laws"', 'https://www.aut.edu/llm-program-masters-of-laws/', 'official_program_page', '2026-07-06', 'AUT support / recognized teaching centre page for the University of London LLM; excluded from the AUT-awarded graduate inventory.'),
        (v_university_id, 'AUT Student Manual', 'https://www.aut.edu/wp-content/uploads/2026/04/AUT-Student-Manual-1.pdf', 'official_pdf', '2026-07-06', 'Official student manual and regulations source.'),
        (v_university_id, 'AUT Graduation', 'https://www.aut.edu/graduation/', 'official_page', '2026-07-06', 'Corroborating graduation/award categories; not used as primary program evidence.'),
        (v_university_id, 'Financial Aid Application Form', 'https://www.aut.edu/fa/', 'official_page', '2026-07-06', 'Financial aid application landing page.')
    ON CONFLICT (university_id, url) DO UPDATE SET
        title = EXCLUDED.title,
        source_type = EXCLUDED.source_type,
        accessed_at = EXCLUDED.accessed_at,
        notes = EXCLUDED.notes,
        updated_at = NOW();

    INSERT INTO university_faculty (university_id, name, short_name, faculty_type, official_url, notes) VALUES
        (v_university_id, 'Faculty of Business Administration', NULL, 'FACULTY', NULL, 'Imported from the official AUOT graduate inventory.'),
        (v_university_id, 'Faculty of Applied Science & Technology', NULL, 'FACULTY', NULL, 'Imported from the official AUOT graduate inventory.')
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
        'Department of Computer Science',
        NULL,
        NULL,
        'Imported from the official AUOT graduate inventory.'
    FROM university_faculty f
    WHERE f.university_id = v_university_id
      AND f.name = 'Faculty of Applied Science & Technology'
    ON CONFLICT (university_id, faculty_id, name) DO UPDATE SET
        short_name = EXCLUDED.short_name,
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
        f.id,
        NULL,
        dt.id,
        'auot-mba',
        'Business Administration',
        'Business Administration',
        'Master of Business Administration',
        NULL,
        39,
        NULL,
        NULL,
        NULL,
        NULL,
        'The MBA is the university''s graduate business program and is offered with nine official concentrations.',
        NULL,
        s.id,
        'The official 2025-2026 AUT catalogue lists the MBA as a 39-credit graduate degree and enumerates the concentrations shown in research/auot/programs.json. No dedicated current MBA program page was found in this discovery pass, so official_program_url is null.'
    FROM university_faculty f
    JOIN degree_type dt ON dt.code = 'MASTER'
    JOIN source s ON s.university_id = v_university_id AND s.url = 'https://www.aut.edu/wp-content/uploads/2025/10/Updated-Catalogue-2025-2026-.pdf'
    WHERE f.university_id = v_university_id
      AND f.name = 'Faculty of Business Administration'
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
        f.id,
        d.id,
        dt.id,
        'auot-ms-computer-science',
        'Computer Science',
        'Computer Science',
        'Master of Science in Computer Science',
        NULL,
        39,
        NULL,
        NULL,
        NULL,
        NULL,
        'A 39-credit graduate program in computer science.',
        'https://www.aut.edu/computer-science-master/',
        s.id,
        'The AUT catalogue and the official program page both identify this 39-credit master of science program in Computer Science.'
    FROM university_faculty f
    JOIN university_department d
      ON d.university_id = v_university_id
     AND d.faculty_id = f.id
     AND d.name = 'Department of Computer Science'
    JOIN degree_type dt ON dt.code = 'MASTER'
    JOIN source s ON s.university_id = v_university_id AND s.url = 'https://www.aut.edu/computer-science-master/'
    WHERE f.university_id = v_university_id
      AND f.name = 'Faculty of Applied Science & Technology'
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
        f.id,
        d.id,
        dt.id,
        'auot-ms-information-technology',
        'Information Technology',
        'Information Technology',
        'Master of Science in Information Technology',
        NULL,
        NULL,
        NULL,
        NULL,
        NULL,
        NULL,
        'A graduate program in information technology listed in the official AUT 2025-2026 catalogue.',
        NULL,
        s.id,
        'The AUT catalogue states that the Department of Computer Science offers a Master of Science in Information Technology. No separate official program page was found in this pass, so official_program_url is null.'
    FROM university_faculty f
    JOIN university_department d
      ON d.university_id = v_university_id
     AND d.faculty_id = f.id
     AND d.name = 'Department of Computer Science'
    JOIN degree_type dt ON dt.code = 'MASTER'
    JOIN source s ON s.university_id = v_university_id AND s.url = 'https://www.aut.edu/wp-content/uploads/2025/10/Updated-Catalogue-2025-2026-.pdf'
    WHERE f.university_id = v_university_id
      AND f.name = 'Faculty of Applied Science & Technology'
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
            v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'auot-tuition-per-credit', '2025-2026',
            'USD', 'PER_CREDIT', 315, 'Graduate tuition per credit',
            'AUT publishes graduate tuition centrally at university scope; program rows remain without tuition values.',
            (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://www.aut.edu/wp-content/uploads/2025/10/Updated-Catalogue-2025-2026-.pdf' LIMIT 1)
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
            v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'auot-fee-application', '2025-2026',
            'Graduate application fee', 'FLAT_FEE', 'USD', 50, 'Admissions',
            'Official graduate application fee published on the admissions page and catalogue.',
            (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://www.aut.edu/admissions/' LIMIT 1)
        ),
        (
            v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'auot-fee-registration', '2025-2026',
            'Graduate registration fee per semester', 'PER_SEMESTER', 'USD', 400, 'Registration',
            'Official graduate registration fee per semester published in the catalogue.',
            (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://www.aut.edu/wp-content/uploads/2025/10/Updated-Catalogue-2025-2026-.pdf' LIMIT 1)
        ),
        (
            v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'auot-fee-student-services', '2025-2026',
            'Activities / yearbook / technology / internet / library access fee', 'PER_SEMESTER', 'USD', 250, 'Student Services',
            'Official graduate semester fee published in the catalogue.',
            (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://www.aut.edu/wp-content/uploads/2025/10/Updated-Catalogue-2025-2026-.pdf' LIMIT 1)
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
            v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'auot-adm-general-degree',
            'ACADEMIC', 'Graduate applicants must hold a university degree from an accredited institution recognized by AUT.', NULL, NULL, NULL, TRUE,
            'General graduate admission requirement from the official admissions page and catalog.',
            (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://www.aut.edu/admissions/' LIMIT 1)
        ),
        (
            v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'auot-adm-gpa-27',
            'ACADEMIC', 'Graduate applicants must have a minimum GPA of 2.7 on a 4.0 scale.', '>=', 2.7, 'GPA', TRUE,
            'Minimum GPA requirement published on the admissions page and catalog.',
            (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://www.aut.edu/admissions/' LIMIT 1)
        ),
        (
            v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'auot-adm-english-toefl-550',
            'ENGLISH', 'Applicants from non-English instruction institutions may be required to submit International TOEFL with a minimum score of 550 or another internationally recognized English test.', '>=', 550, 'TOEFL', TRUE,
            'Language testing requirement published on the admissions page and catalog.',
            (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://www.aut.edu/admissions/' LIMIT 1)
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
            v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'auot-doc-degree-copy',
            'ACADEMIC_RECORD', 'Certified copy of the university degree', FALSE, 1,
            'Core graduate application file item published on the admissions page and catalog.',
            (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://www.aut.edu/admissions/' LIMIT 1)
        ),
        (
            v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'auot-doc-cv',
            'APPLICATION', 'Updated CV', FALSE, 2,
            'Core graduate application file item published on the admissions page and catalog.',
            (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://www.aut.edu/admissions/' LIMIT 1)
        ),
        (
            v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'auot-doc-recommendations',
            'RECOMMENDATION', 'Two recommendation letters', FALSE, 3,
            'Core graduate application file item published on the admissions page and catalog.',
            (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://www.aut.edu/admissions/' LIMIT 1)
        ),
        (
            v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'auot-doc-transcript',
            'TRANSCRIPT', 'Official bachelor''s transcript', FALSE, 4,
            'Core graduate application file item published on the admissions page and catalog.',
            (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://www.aut.edu/admissions/' LIMIT 1)
        ),
        (
            v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'auot-doc-equivalency',
            'ACADEMIC_RECORD', 'Bachelor''s degree equivalency, if needed', FALSE, 5,
            'Published on the admissions page and catalog for applicants whose prior degree needs equivalency validation.',
            (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://www.aut.edu/admissions/' LIMIT 1)
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
            v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'auot-deadline-academic-calendar',
            '2025-2026', 'OTHER', '2025-2026', NULL,
            'The official website links to academic, financial, and exam calendars, but no specific graduate application deadline date was published in the reviewed sources.',
            (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://www.aut.edu/' LIMIT 1)
        )
    ON CONFLICT (university_id, record_key) DO UPDATE SET
        academic_year = EXCLUDED.academic_year,
        deadline_type = EXCLUDED.deadline_type,
        term = EXCLUDED.term,
        deadline_date = EXCLUDED.deadline_date,
        note = EXCLUDED.note,
        source_id = EXCLUDED.source_id,
        updated_at = NOW();

    INSERT INTO graduate_scholarship (
        university_id, faculty_id, department_id, program_id, scope_level, record_key, academic_year,
        name, description, coverage, amount, currency, notes, source_id
    ) VALUES
        (
            v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'auot-scholarship-discounts', '2025-2026',
            'Discounts', 'Discounts referenced in the admissions and catalog materials.', 'Discounts', NULL, NULL,
            'University-wide scholarship category published in the official sources.',
            (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://www.aut.edu/wp-content/uploads/2025/10/Updated-Catalogue-2025-2026-.pdf' LIMIT 1)
        ),
        (
            v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'auot-scholarship-merit', '2025-2026',
            'Merit scholarships', 'Merit scholarships referenced in the admissions and catalog materials.', 'Merit-based', NULL, NULL,
            'University-wide scholarship category published in the official sources.',
            (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://www.aut.edu/wp-content/uploads/2025/10/Updated-Catalogue-2025-2026-.pdf' LIMIT 1)
        ),
        (
            v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'auot-scholarship-sibling', '2025-2026',
            'Sibling scholarships', 'Sibling scholarships referenced in the admissions and catalog materials.', 'Sibling-based', NULL, NULL,
            'University-wide scholarship category published in the official sources.',
            (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://www.aut.edu/wp-content/uploads/2025/10/Updated-Catalogue-2025-2026-.pdf' LIMIT 1)
        ),
        (
            v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'auot-scholarship-sport', '2025-2026',
            'Sport scholarships', 'Sport scholarships referenced in the admissions and catalog materials.', 'Sport-based', NULL, NULL,
            'University-wide scholarship category published in the official sources.',
            (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://www.aut.edu/wp-content/uploads/2025/10/Updated-Catalogue-2025-2026-.pdf' LIMIT 1)
        ),
        (
            v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'auot-scholarship-grants', '2025-2026',
            'Grants', 'Grants referenced in the admissions and catalog materials.', 'Grant-based', NULL, NULL,
            'University-wide scholarship category published in the official sources.',
            (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://www.aut.edu/wp-content/uploads/2025/10/Updated-Catalogue-2025-2026-.pdf' LIMIT 1)
        )
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

    INSERT INTO graduate_financial_aid (
        university_id, faculty_id, department_id, program_id, scope_level, record_key, academic_year,
        name, description, amount, currency, notes, source_id
    ) VALUES
        (
            v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'auot-fin-aid-application', '2025-2026',
            'Financial aid', 'Financial aid application and support referenced on the official AUOT financial aid page.', NULL, NULL,
            'University-wide financial aid category published in the official sources.',
            (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://www.aut.edu/fa/' LIMIT 1)
        )
    ON CONFLICT (university_id, record_key) DO UPDATE SET
        academic_year = EXCLUDED.academic_year,
        name = EXCLUDED.name,
        description = EXCLUDED.description,
        amount = EXCLUDED.amount,
        currency = EXCLUDED.currency,
        notes = EXCLUDED.notes,
        source_id = EXCLUDED.source_id,
        updated_at = NOW();

    INSERT INTO graduate_accreditation (
        university_id, faculty_id, department_id, program_id, scope_level, record_key,
        name, authority, status, valid_from, valid_until, notes, source_id
    ) VALUES
        (
            v_university_id,
            (SELECT id FROM university_faculty WHERE university_id = v_university_id AND name = 'Faculty of Business Administration' LIMIT 1),
            NULL,
            (SELECT id FROM graduate_program WHERE university_id = v_university_id AND program_key = 'auot-mba' LIMIT 1),
            'PROGRAM', 'auot-accreditation-mba',
            'MBA Accreditation', 'Ministry of Education and Higher Education', 'Accredited', NULL, NULL,
            'The official catalogue states that the MBA is fully accredited by the Ministry of Education and Higher Education.',
            (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://www.aut.edu/wp-content/uploads/2025/10/Updated-Catalogue-2025-2026-.pdf' LIMIT 1)
        )
    ON CONFLICT (university_id, record_key) DO UPDATE SET
        faculty_id = EXCLUDED.faculty_id,
        department_id = EXCLUDED.department_id,
        program_id = EXCLUDED.program_id,
        name = EXCLUDED.name,
        authority = EXCLUDED.authority,
        status = EXCLUDED.status,
        valid_from = EXCLUDED.valid_from,
        valid_until = EXCLUDED.valid_until,
        notes = EXCLUDED.notes,
        source_id = EXCLUDED.source_id,
        updated_at = NOW();

    INSERT INTO graduate_program_track (
        university_id, program_id, track_type, track_name, track_order, is_primary, description, source_id, notes
    ) VALUES
        (
            v_university_id,
            (SELECT id FROM graduate_program WHERE university_id = v_university_id AND program_key = 'auot-mba' LIMIT 1),
            'CONCENTRATION', 'Accounting', 1, TRUE, NULL,
            (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://www.aut.edu/wp-content/uploads/2025/10/Updated-Catalogue-2025-2026-.pdf' LIMIT 1),
            'Official MBA concentration listed in the catalogue.'
        ),
        (
            v_university_id,
            (SELECT id FROM graduate_program WHERE university_id = v_university_id AND program_key = 'auot-mba' LIMIT 1),
            'CONCENTRATION', 'Banking & Finance', 2, FALSE, NULL,
            (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://www.aut.edu/wp-content/uploads/2025/10/Updated-Catalogue-2025-2026-.pdf' LIMIT 1),
            'Official MBA concentration listed in the catalogue.'
        ),
        (
            v_university_id,
            (SELECT id FROM graduate_program WHERE university_id = v_university_id AND program_key = 'auot-mba' LIMIT 1),
            'CONCENTRATION', 'Entrepreneurship', 3, FALSE, NULL,
            (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://www.aut.edu/wp-content/uploads/2025/10/Updated-Catalogue-2025-2026-.pdf' LIMIT 1),
            'Official MBA concentration listed in the catalogue.'
        ),
        (
            v_university_id,
            (SELECT id FROM graduate_program WHERE university_id = v_university_id AND program_key = 'auot-mba' LIMIT 1),
            'CONCENTRATION', 'Hospitality and Tourism Management', 4, FALSE, NULL,
            (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://www.aut.edu/wp-content/uploads/2025/10/Updated-Catalogue-2025-2026-.pdf' LIMIT 1),
            'Official MBA concentration listed in the catalogue.'
        ),
        (
            v_university_id,
            (SELECT id FROM graduate_program WHERE university_id = v_university_id AND program_key = 'auot-mba' LIMIT 1),
            'CONCENTRATION', 'Human Resources Management', 5, FALSE, NULL,
            (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://www.aut.edu/wp-content/uploads/2025/10/Updated-Catalogue-2025-2026-.pdf' LIMIT 1),
            'Official MBA concentration listed in the catalogue.'
        ),
        (
            v_university_id,
            (SELECT id FROM graduate_program WHERE university_id = v_university_id AND program_key = 'auot-mba' LIMIT 1),
            'CONCENTRATION', 'Management', 6, FALSE, NULL,
            (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://www.aut.edu/wp-content/uploads/2025/10/Updated-Catalogue-2025-2026-.pdf' LIMIT 1),
            'Official MBA concentration listed in the catalogue.'
        ),
        (
            v_university_id,
            (SELECT id FROM graduate_program WHERE university_id = v_university_id AND program_key = 'auot-mba' LIMIT 1),
            'CONCENTRATION', 'Management Information Systems', 7, FALSE, NULL,
            (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://www.aut.edu/wp-content/uploads/2025/10/Updated-Catalogue-2025-2026-.pdf' LIMIT 1),
            'Official MBA concentration listed in the catalogue.'
        ),
        (
            v_university_id,
            (SELECT id FROM graduate_program WHERE university_id = v_university_id AND program_key = 'auot-mba' LIMIT 1),
            'CONCENTRATION', 'Marketing', 8, FALSE, NULL,
            (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://www.aut.edu/wp-content/uploads/2025/10/Updated-Catalogue-2025-2026-.pdf' LIMIT 1),
            'Official MBA concentration listed in the catalogue.'
        ),
        (
            v_university_id,
            (SELECT id FROM graduate_program WHERE university_id = v_university_id AND program_key = 'auot-mba' LIMIT 1),
            'CONCENTRATION', 'School Administration', 9, FALSE, NULL,
            (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://www.aut.edu/wp-content/uploads/2025/10/Updated-Catalogue-2025-2026-.pdf' LIMIT 1),
            'Official MBA concentration listed in the catalogue.'
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
            (SELECT id FROM graduate_program WHERE university_id = v_university_id AND program_key = 'auot-mba' LIMIT 1),
            (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://www.aut.edu/wp-content/uploads/2025/10/Updated-Catalogue-2025-2026-.pdf' LIMIT 1),
            'PRIMARY', 1,
            'The official 2025-2026 AUT catalogue lists the MBA as a 39-credit graduate degree and enumerates the MBA concentrations.',
            'Primary graduate evidence.'
        ),
        (
            v_university_id,
            (SELECT id FROM graduate_program WHERE university_id = v_university_id AND program_key = 'auot-ms-computer-science' LIMIT 1),
            (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://www.aut.edu/computer-science-master/' LIMIT 1),
            'PRIMARY', 1,
            'Official MS Computer Science program page.',
            'Primary graduate evidence.'
        ),
        (
            v_university_id,
            (SELECT id FROM graduate_program WHERE university_id = v_university_id AND program_key = 'auot-ms-computer-science' LIMIT 1),
            (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://www.aut.edu/wp-content/uploads/2025/10/Updated-Catalogue-2025-2026-.pdf' LIMIT 1),
            'SECONDARY', 2,
            'The AUT catalogue also identifies the 39-credit MS Computer Science program.',
            'Supporting catalog evidence.'
        ),
        (
            v_university_id,
            (SELECT id FROM graduate_program WHERE university_id = v_university_id AND program_key = 'auot-ms-information-technology' LIMIT 1),
            (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://www.aut.edu/wp-content/uploads/2025/10/Updated-Catalogue-2025-2026-.pdf' LIMIT 1),
            'PRIMARY', 1,
            'The AUT catalogue states that the Department of Computer Science offers a Master of Science in Information Technology.',
            'Primary graduate evidence.'
        )
    ON CONFLICT (program_id, source_id, source_role) DO UPDATE SET
        source_order = EXCLUDED.source_order,
        evidence_text = EXCLUDED.evidence_text,
        notes = EXCLUDED.notes,
        updated_at = NOW();

END $$;
