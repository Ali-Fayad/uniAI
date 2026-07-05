-- LCU graduate data seed migration.
-- Idempotent import for the canonical LCU graduate dataset.

DO $$
DECLARE
    v_university_id BIGINT;
BEGIN

    INSERT INTO university (name, name_ar, acronym, country, city, latitude, longitude, campus_name, campus_type)
    SELECT 'Lebanese Canadian University', NULL, 'LCU', 'Lebanon', NULL, NULL, NULL, NULL, NULL
    WHERE NOT EXISTS (SELECT 1 FROM university WHERE name = 'Lebanese Canadian University');

    SELECT id INTO v_university_id FROM university WHERE name = 'Lebanese Canadian University' ORDER BY id LIMIT 1;

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
        (v_university_id, 'LCU Programs', 'https://www.lcu.edu.lb/lcu-programs/', 'official_page', '2026-07-05', 'Program overview page states LCU has Bachelor and Master programs and describes the purpose of Master''s programs.'),
        (v_university_id, 'LCU Master Programs', 'https://www.lcu.edu.lb/lcu-master-programs/', 'official_page', '2026-07-05', 'Master programs page identifies two graduate programs in the Faculty of Business Administration: Research MBA and Executive MBA.'),
        (v_university_id, 'LCU Faculty of Business Administration', 'https://www.lcu.edu.lb/lcu-faculty-of-business-administration/', 'official_page', '2026-07-05', 'Faculty page lists graduate programs, credits, and duration: Research MBA 36 credits / 2 years; Executive MBA 39 credits / 2 years.'),
        (v_university_id, 'LCU Research MBA', 'https://www.lcu.edu.lb/lcu-research-mba/', 'official_page', '2026-07-05', 'Research MBA page describes the program, doctoral-study preparation, and course requirements totaling 36 credits.'),
        (v_university_id, 'LCU Executive MBA', 'https://www.lcu.edu.lb/lcu-executive-mba/', 'official_page', '2026-07-05', 'Executive MBA page describes the program for working professionals/executives and course requirements totaling 39 credits plus remedial courses for non-business majors.'),
        (v_university_id, 'LCU Tuition Fees', 'https://www.lcu.edu.lb/lcu-tuition-fees/', 'official_page', '2026-07-05', 'Tuition page includes Academic Year/Tuition Fees 2025-2026 and a Graduate Tuition Fees section, but the accessible extracted content did not expose the graduate numeric value.'),
        (v_university_id, 'LCU Apply Now', 'https://www.lcu.edu.lb/lcu-apply-now/', 'official_page', '2026-07-05', 'Application page provides required admission documents and application procedure. The visible requirements are written for undergraduate/freshman applicants; no graduate-specific admissions requirements were found there.'),
        (v_university_id, 'LCU Faculty of Arts & Sciences', 'https://www.lcu.edu.lb/lcu-faculty-of-arts-and-science/', 'official_page', '2026-07-05', 'Faculty page lists only undergraduate programs in the accessible content. No Master or PhD program evidence found on this faculty page.'),
        (v_university_id, 'LCU Faculty of Humanities', 'https://www.lcu.edu.lb/lcu-faculty-of-humanities/', 'official_page', '2026-07-05', 'Faculty page lists only undergraduate programs in the accessible content. No Master or PhD program evidence found on this faculty page.'),
        (v_university_id, 'LCU at a Glance', 'https://www.lcu.edu.lb/lcu-at-a-glance/', 'official_page', '2026-07-05', 'Institutional overview page states LCU operates three faculties and offers education at Bachelor and Master levels; no PhD evidence found.')
    ON CONFLICT (university_id, url) DO UPDATE SET
        title = EXCLUDED.title,
        source_type = EXCLUDED.source_type,
        accessed_at = EXCLUDED.accessed_at,
        notes = EXCLUDED.notes,
        updated_at = NOW();

    INSERT INTO university_faculty (university_id, name, short_name, faculty_type, official_url, notes) VALUES
        (v_university_id, 'Faculty of Business Administration', NULL, 'FACULTY', NULL, 'Imported from the official LCU graduate inventory.')
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
        'lcu-research-mba',
        'Business Administration',
        'Business Administration',
        'Research Master of Business Administration',
        'THESIS',
        36,
        2,
        'YEARS',
        NULL,
        NULL,
        'The Research MBA provides advanced theoretical knowledge and research competencies in business and management, including strategic management, finance, marketing, organizational behavior, economics, quantitative research methods, research methodology, statistical analysis, and academic writing.',
        'https://www.lcu.edu.lb/lcu-research-mba/',
        src.id,
        'Official LCU graduate pages identify this as the Research MBA and give the program''s 36-credit, 2-year structure. No separate concentration or department-level breakdown was published in the reviewed official sources.'
    FROM university_faculty fac
    JOIN degree_type dt ON dt.code = 'MASTER'
    JOIN source src ON src.university_id = v_university_id AND src.url = 'https://www.lcu.edu.lb/lcu-master-programs/'
    WHERE fac.university_id = v_university_id
      AND fac.name = 'Faculty of Business Administration'
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
        'lcu-executive-mba',
        'Business Administration',
        'Business Administration',
        'Executive Master of Business Administration',
        'THESIS',
        39,
        2,
        'YEARS',
        NULL,
        NULL,
        'The Executive MBA is designed for working professionals and executives who want to strengthen leadership and managerial skills while continuing their careers. It focuses on strategic management, finance, marketing, leadership, operations, business analytics, practical application, and case-based learning.',
        'https://www.lcu.edu.lb/lcu-executive-mba/',
        src.id,
        'Official LCU graduate pages identify this as the Executive MBA and give the program''s 39-credit, 2-year structure. The reviewed sources do not treat any emphases as separate graduate degrees.'
    FROM university_faculty fac
    JOIN degree_type dt ON dt.code = 'MASTER'
    JOIN source src ON src.university_id = v_university_id AND src.url = 'https://www.lcu.edu.lb/lcu-master-programs/'
    WHERE fac.university_id = v_university_id
      AND fac.name = 'Faculty of Business Administration'
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

    INSERT INTO graduate_fee_item (
        university_id, faculty_id, department_id, program_id, scope_level, record_key, academic_year, fee_name,
        billing_basis, currency, amount, category, notes, source_id
    ) VALUES
        (v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'lcu-fee-application', NULL, 'Graduate application fee', 'FLAT_FEE', 'USD', NULL, 'Admissions', 'The accessible Apply Now page provides admissions instructions but no numeric graduate application fee was exposed.', (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://www.lcu.edu.lb/lcu-apply-now/' LIMIT 1)),
        (v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'lcu-fee-registration', NULL, 'Graduate registration fee', 'FLAT_FEE', 'USD', NULL, 'Admissions', 'The tuition-fees page references graduate tuition fees, but no standalone numeric registration fee was captured in the accessible text.', (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://www.lcu.edu.lb/lcu-tuition-fees/' LIMIT 1))
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
        (v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'lcu-admission-general', 'GENERAL', 'LCU does not publish a separate graduate-specific admissions checklist in the accessible official pages; applicants should review the graduate program pages and consult admissions for any program-specific file requirements not exposed publicly.', NULL, NULL, NULL, TRUE, 'Shared admissions summary based on the official graduate pages and the public Apply Now page.', (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://www.lcu.edu.lb/lcu-apply-now/' LIMIT 1))
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
        university_id, faculty_id, department_id, program_id, scope_level, record_key, document_type, document_name,
        is_optional, sort_order, notes, source_id
    ) VALUES
        (v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'lcu-doc-01', 'APPLICATION', '3 recent passport-size photos', FALSE, 1, 'Published on the public Apply Now page in undergraduate/freshman context; no graduate-specific document list was published.', (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://www.lcu.edu.lb/lcu-apply-now/' LIMIT 1)),
        (v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'lcu-doc-02', 'APPLICATION', 'Recent personal Civil Status Record or National Identity Card copy', FALSE, 2, 'Published on the public Apply Now page in undergraduate/freshman context; no graduate-specific document list was published.', (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://www.lcu.edu.lb/lcu-apply-now/' LIMIT 1)),
        (v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'lcu-doc-03', 'APPLICATION', 'Passport and residence permit copy', FALSE, 3, 'Published on the public Apply Now page in undergraduate/freshman context; no graduate-specific document list was published.', (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://www.lcu.edu.lb/lcu-apply-now/' LIMIT 1)),
        (v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'lcu-doc-04', 'APPLICATION', 'Recent Family Civil Status Record', FALSE, 4, 'Published on the public Apply Now page in undergraduate/freshman context; no graduate-specific document list was published.', (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://www.lcu.edu.lb/lcu-apply-now/' LIMIT 1)),
        (v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'lcu-doc-05', 'APPLICATION', 'NSSF attestation or equivalent, if applicable', FALSE, 5, 'Published on the public Apply Now page in undergraduate/freshman context; no graduate-specific document list was published.', (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://www.lcu.edu.lb/lcu-apply-now/' LIMIT 1)),
        (v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'lcu-doc-06', 'APPLICATION', 'Recent certificate of employment for the student or guarantor specifying monthly salary', FALSE, 6, 'Published on the public Apply Now page in undergraduate/freshman context; no graduate-specific document list was published.', (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://www.lcu.edu.lb/lcu-apply-now/' LIMIT 1)),
        (v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'lcu-doc-07', 'APPLICATION', 'Official transcripts for the last three years of secondary school, certified by the issuing school', FALSE, 7, 'Published on the public Apply Now page in undergraduate/freshman context; no graduate-specific document list was published.', (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://www.lcu.edu.lb/lcu-apply-now/' LIMIT 1)),
        (v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'lcu-doc-08', 'APPLICATION', 'Certified Lebanese Baccalaureate copy or equivalent issued by the Lebanese Ministry of Higher Education', FALSE, 8, 'Published on the public Apply Now page in undergraduate/freshman context; no graduate-specific document list was published.', (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://www.lcu.edu.lb/lcu-apply-now/' LIMIT 1)),
        (v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'lcu-doc-09', 'APPLICATION', 'Official transcript of prior university study for transfer students', FALSE, 9, 'Published on the public Apply Now page in undergraduate/freshman context; no graduate-specific document list was published.', (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://www.lcu.edu.lb/lcu-apply-now/' LIMIT 1))
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

    WITH lcu_program_source_seed(program_key, source_url, source_role, source_order, evidence_text, notes) AS (
        VALUES
            ('lcu-research-mba', 'https://www.lcu.edu.lb/lcu-master-programs/', 'PRIMARY', 1, 'LCU Master Programs', 'Primary graduate landing page.'),
            ('lcu-research-mba', 'https://www.lcu.edu.lb/lcu-faculty-of-business-administration/', 'SECONDARY', 2, 'LCU Faculty of Business Administration', 'Faculty page with credits and duration.'),
            ('lcu-research-mba', 'https://www.lcu.edu.lb/lcu-research-mba/', 'SECONDARY', 3, 'LCU Research MBA', 'Program detail page.'),
            ('lcu-executive-mba', 'https://www.lcu.edu.lb/lcu-master-programs/', 'PRIMARY', 1, 'LCU Master Programs', 'Primary graduate landing page.'),
            ('lcu-executive-mba', 'https://www.lcu.edu.lb/lcu-faculty-of-business-administration/', 'SECONDARY', 2, 'LCU Faculty of Business Administration', 'Faculty page with credits and duration.'),
            ('lcu-executive-mba', 'https://www.lcu.edu.lb/lcu-executive-mba/', 'SECONDARY', 3, 'LCU Executive MBA', 'Program detail page.')
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
    FROM lcu_program_source_seed seed
    JOIN graduate_program gp ON gp.university_id = v_university_id AND gp.program_key = seed.program_key
    JOIN source s ON s.university_id = v_university_id AND s.url = seed.source_url
    ON CONFLICT (program_id, source_id, source_role) DO UPDATE SET
        source_order = EXCLUDED.source_order,
        evidence_text = EXCLUDED.evidence_text,
        notes = EXCLUDED.notes,
        updated_at = NOW();

END $$;
