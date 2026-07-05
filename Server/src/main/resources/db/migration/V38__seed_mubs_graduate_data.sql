-- MUBS graduate data seed migration.
-- Idempotent import for the canonical MUBS graduate dataset.

DO $$
DECLARE
    v_university_id BIGINT;
BEGIN

    INSERT INTO university (name, name_ar, acronym, country, city, latitude, longitude, campus_name, campus_type)
    SELECT 'Modern University for Business and Science', NULL, 'MUBS', 'Lebanon', NULL, NULL, NULL, NULL, NULL
    WHERE NOT EXISTS (
        SELECT 1 FROM university WHERE name = 'Modern University for Business and Science'
    );

    SELECT id INTO v_university_id
    FROM university
    WHERE name = 'Modern University for Business and Science'
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
        ('Arabic', 'ar', 'العربية'),
        ('French', 'fr', 'Français'),
        ('Multilingual', 'multi', 'Multilingual')
    ON CONFLICT (code) DO UPDATE SET
        name = EXCLUDED.name,
        native_name = EXCLUDED.native_name;

    CREATE TEMP TABLE mubs_source_seed (
        source_id TEXT PRIMARY KEY,
        title TEXT NOT NULL,
        url TEXT NOT NULL,
        source_type TEXT NOT NULL,
        accessed_at DATE,
        notes TEXT
    ) ON COMMIT DROP;

    INSERT INTO mubs_source_seed (source_id, title, url, source_type, accessed_at, notes)
    VALUES
        ('MUBS-SRC-001', 'Home - Modern University for Business & Science', 'https://www.mubs.edu.lb/', 'WEB', '2026-07-04', 'Official homepage used to confirm site navigation, faculties, academics, student services, financial aid, and calendar context.'),
        ('MUBS-SRC-002', 'Admission - Modern University for Business & Science', 'https://www.mubs.edu.lb/en/admission.aspx', 'WEB', '2026-07-04', 'Admission landing page listing graduate majors, requirements, tuition, how to apply, and entrance exams.'),
        ('MUBS-SRC-003', 'Graduate Majors - Modern University for Business & Science', 'https://mubs.edu.lb/en/admission/graduate-majors.aspx', 'WEB', '2026-07-04', 'Target graduate majors page; verification behavior during browser fetch.'),
        ('MUBS-SRC-004', 'Admission Requirements - Modern University for Business & Science', 'https://www.mubs.edu.lb/en/admission/admission-requirements.aspx', 'WEB', '2026-07-04', 'Target admissions requirement page; browser fetch returned verification behavior.'),
        ('MUBS-SRC-005', 'Tuition & Fees - Modern University for Business & Science', 'https://mubs.edu.lb/en/admission/tuition.aspx', 'WEB', '2026-07-04', 'Target MUBS tuition page; no stable numeric graduate tuition amount was recovered.'),
        ('MUBS-SRC-006', 'Financial Aid - Modern University for Business & Science', 'https://mubs.edu.lb/en/financial-aid.aspx', 'WEB', '2026-07-04', 'Financial aid overview; lists scholarships, academic merits, athletic scholarship, and need-based grants.'),
        ('MUBS-SRC-009', 'Computer Science Department - Modern University for Business & Science', 'https://mubs.edu.lb/en/faculties/artsandsciences/csd.aspx', 'WEB', '2026-07-04', 'Confirms the MCS and the four specialization areas, and states MEHE accreditation for BCS and MCS.'),
        ('MUBS-SRC-010', 'Faculty of Arts and Sciences - Modern University for Business & Science', 'https://mubs.edu.lb/en/faculties/artsandsciences.aspx', 'WEB', '2026-07-04', 'Faculty landing page for Arts and Sciences.'),
        ('MUBS-SRC-012', 'Cardiff Metropolitan University at MUBS - CMU', 'https://www.mubs.edu.lb/en/faculties/cmu.aspx', 'WEB', '2026-07-04', 'Cardiff Met at MUBS overview; lists MBA programmes at a glance.'),
        ('MUBS-SRC-013', 'MBA Master of Business Administration (General) - CMU', 'https://mubs.edu.lb/en/faculties/cmu/programs/postgraduate-level/7741.aspx', 'WEB', '2026-07-04', 'Program page with MBA overview, core modules, pathway modules, final project, and application steps.'),
        ('MUBS-SRC-014', 'Tuition Fees - CMU', 'https://mubs.edu.lb/en/faculties/cmu/tuitionfees.aspx', 'WEB', '2026-07-04', 'Cardiff tuition page; fees are available upon request through MUBS forms.'),
        ('MUBS-SRC-016', 'Cardiff Met Programme at MUBS | Info Request Form', 'https://www.mubs.edu.lb/cmu/info.aspx', 'WEB', '2026-07-04', 'Official MUBS info request page describing the British master degree, hybrid/flexible structure, and Cardiff Met recognition.'),
        ('MUBS-SRC-017', 'CMU Dual Degree | Info Request - MUBS Forms', 'https://forms.mubs.edu.lb/index.php/944744?lang=en&newtest=Y', 'WEB', '2026-07-04', 'Official MUBS forms page for Cardiff Met dual-degree interest.'),
        ('MUBS-SRC-018', 'Apply Online - Master of Science in Computer Science (MCS)', 'https://ums.mubs.edu.lb/application/applicant/newApplicant.php?major=Master+of+Science+in+Computer+Science+%28MCS%29', 'WEB', '2026-07-04', 'Official UMS application URL confirming the MCS major name.'),
        ('MUBS-SRC-019', 'Catalogue 2012 - Modern University for Business and Science', 'https://www.mubs.edu.lb/assets/templates/mubs/files/catalogue2012.pdf', 'PDF', '2026-07-04', 'Official catalogue PDF used for graduate admissions, TOEFL/GPA requirements, and legacy program references.'),
        ('MUBS-SRC-020', 'Academic Catalogue 2010-2011 - Modern University for Business and Science', 'https://www.mubs.edu.lb/assets/templates/mubs/files/academiccatalogue.pdf', 'PDF', '2026-07-04', 'Older official catalogue; useful for historical regulations and graduate references.'),
        ('MUBS-SRC-022', 'Catalogue 2011 - Modern University for Business and Science', 'https://mubs.edu.lb/assets/templates/mubs/files/catalogue2011.pdf', 'PDF', '2026-07-04', 'Older official catalogue; confirms graduate admission GPA baseline in discovery notes.'),
        ('MUBS-SRC-023', 'MBA Cardiff PDF - MUBS / Cardiff Metropolitan University', 'https://www.mubs.edu.lb/assets/templates/bsml/files/MBA-Cardiff.pdf', 'PDF', '2026-07-04', 'Official PDF describing Cardiff MBA structure, double degree, evening classes, and Lebanese/UK recognition.'),
        ('MUBS-SRC-024', 'Academic Calendar - Modern University for Business and Science', 'https://www.mubs.edu.lb/en/academics_mubs/academic-calendar.aspx', 'WEB', '2026-07-04', 'Academic calendar URL discovered in navigation; full page fetch returned verification behavior.'),
        ('MUBS-SRC-025', 'Rules & Regulations - Modern University for Business and Science', 'https://www.mubs.edu.lb/en/academics_mubs/rules.aspx', 'WEB', '2026-07-04', 'Rules and regulations page discovered in navigation; direct fetch failed.');

    INSERT INTO source (university_id, title, url, source_type, accessed_at)
    SELECT v_university_id, title, url, source_type, accessed_at
    FROM mubs_source_seed
    ON CONFLICT (university_id, url) DO UPDATE SET
        title = EXCLUDED.title,
        source_type = EXCLUDED.source_type,
        accessed_at = EXCLUDED.accessed_at,
        updated_at = NOW();

    CREATE TEMP TABLE mubs_faculty_seed (
        name TEXT PRIMARY KEY,
        short_name TEXT,
        faculty_type TEXT NOT NULL,
        official_url TEXT,
        notes TEXT
    ) ON COMMIT DROP;

    INSERT INTO mubs_faculty_seed (name, short_name, faculty_type, official_url, notes)
    VALUES
        ('Faculty of Arts and Sciences', NULL, 'FACULTY', 'https://mubs.edu.lb/en/faculties/artsandsciences.aspx', 'Imported from the official MUBS graduate dataset.'),
        ('Cardiff Metropolitan University at MUBS', NULL, 'INTERFACULTY', 'https://www.mubs.edu.lb/en/faculties/cmu.aspx', 'Imported from the official MUBS graduate dataset.');

    INSERT INTO university_faculty (university_id, name, short_name, faculty_type, official_url, notes)
    SELECT v_university_id, name, short_name, faculty_type, official_url, notes
    FROM mubs_faculty_seed
    ON CONFLICT (university_id, name) DO UPDATE SET
        short_name = EXCLUDED.short_name,
        faculty_type = EXCLUDED.faculty_type,
        official_url = EXCLUDED.official_url,
        notes = EXCLUDED.notes,
        updated_at = NOW();

    CREATE TEMP TABLE mubs_department_seed (
        faculty_name TEXT NOT NULL,
        name TEXT PRIMARY KEY,
        short_name TEXT,
        official_url TEXT,
        notes TEXT
    ) ON COMMIT DROP;

    INSERT INTO mubs_department_seed (faculty_name, name, short_name, official_url, notes)
    VALUES
        ('Faculty of Arts and Sciences', 'Computer Science Department', NULL, 'https://mubs.edu.lb/en/faculties/artsandsciences/csd.aspx', 'Imported from the official MUBS graduate dataset.');

    INSERT INTO university_department (university_id, faculty_id, name, short_name, official_url, notes)
    SELECT v_university_id, f.id, d.name, d.short_name, d.official_url, d.notes
    FROM mubs_department_seed d
    JOIN university_faculty f
      ON f.university_id = v_university_id
     AND f.name = d.faculty_name
    ON CONFLICT (university_id, faculty_id, name) DO UPDATE SET
        short_name = EXCLUDED.short_name,
        official_url = EXCLUDED.official_url,
        notes = EXCLUDED.notes,
        updated_at = NOW();

    CREATE TEMP TABLE mubs_program_seed (
        program_key TEXT PRIMARY KEY,
        faculty_name TEXT NOT NULL,
        department_name TEXT,
        degree_type TEXT NOT NULL,
        official_degree_name TEXT NOT NULL,
        thesis_or_non_thesis TEXT,
        credits INTEGER,
        duration_value NUMERIC(10, 2),
        duration_unit TEXT,
        primary_language TEXT,
        delivery_mode TEXT,
        program_description TEXT,
        official_program_url TEXT NOT NULL,
        source_id TEXT NOT NULL,
        notes TEXT
    ) ON COMMIT DROP;

    INSERT INTO mubs_program_seed (
        program_key, faculty_name, department_name, degree_type, official_degree_name,
        thesis_or_non_thesis, credits, duration_value, duration_unit, primary_language,
        delivery_mode, program_description, official_program_url, source_id, notes
    )
    VALUES
        (
            'mubs-fas-master-computer-science',
            'Faculty of Arts and Sciences',
            'Computer Science Department',
            'MASTER',
            'Master of Science in Computer Science',
            NULL,
            NULL,
            NULL,
            NULL,
            NULL,
            NULL,
            'Graduate computer science program for CS degree holders that offers advanced study in Software Development, Cyber Security, Artificial Intelligence, and Internet of Things.',
            'https://mubs.edu.lb/en/faculties/artsandsciences/csd.aspx',
            'MUBS-SRC-009',
            'Current department page confirms the MCS, names four specialization areas, and states MEHE accreditation for BCS and MCS.'
        ),
        (
            'mubs-cmu-master-business-administration',
            'Cardiff Metropolitan University at MUBS',
            NULL,
            'MASTER',
            'Master of Business Administration (MBA)',
            NULL,
            NULL,
            NULL,
            NULL,
            NULL,
            NULL,
            'Cardiff Metropolitan University MBA at MUBS with core modules, pathway modules, a final project, employability content, and official application steps.',
            'https://mubs.edu.lb/en/faculties/cmu/programs/postgraduate-level/7741.aspx',
            'MUBS-SRC-013',
            'The current Cardiff Met at MUBS page lists these named MBA pathways; the program is modeled as one MBA with documented tracks rather than separate records.'
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
        fac.id,
        dep.id,
        dt.id,
        p.program_key,
        NULL,
        NULL,
        p.official_degree_name,
        p.thesis_or_non_thesis,
        p.credits,
        p.duration_value,
        p.duration_unit,
        lang.id,
        p.delivery_mode,
        p.program_description,
        p.official_program_url,
        src.id,
        p.notes
    FROM mubs_program_seed p
    JOIN university_faculty fac
      ON fac.university_id = v_university_id
     AND fac.name = p.faculty_name
    LEFT JOIN university_department dep
      ON dep.university_id = v_university_id
     AND dep.faculty_id = fac.id
     AND dep.name = p.department_name
    LEFT JOIN degree_type dt
      ON dt.code = p.degree_type
    LEFT JOIN language lang
      ON LOWER(lang.name) = LOWER(p.primary_language)
    JOIN mubs_source_seed ss
      ON ss.source_id = p.source_id
    JOIN source src
      ON src.university_id = v_university_id
     AND src.url = ss.url
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

    CREATE TEMP TABLE mubs_track_seed (
        program_key TEXT NOT NULL,
        track_type TEXT NOT NULL,
        track_name TEXT NOT NULL,
        track_order INTEGER,
        is_primary BOOLEAN NOT NULL DEFAULT FALSE,
        description TEXT,
        source_id TEXT NOT NULL,
        notes TEXT
    ) ON COMMIT DROP;

    INSERT INTO mubs_track_seed (program_key, track_type, track_name, track_order, is_primary, description, source_id, notes)
    VALUES
        ('mubs-fas-master-computer-science', 'SPECIALIZATION', 'Software Development', 1, TRUE, NULL, 'MUBS-SRC-009', 'Official specialization listed on the MCS department page.'),
        ('mubs-fas-master-computer-science', 'SPECIALIZATION', 'Cyber Security', 2, FALSE, NULL, 'MUBS-SRC-009', 'Official specialization listed on the MCS department page.'),
        ('mubs-fas-master-computer-science', 'SPECIALIZATION', 'Artificial Intelligence', 3, FALSE, NULL, 'MUBS-SRC-009', 'Official specialization listed on the MCS department page.'),
        ('mubs-fas-master-computer-science', 'SPECIALIZATION', 'Internet of Things', 4, FALSE, NULL, 'MUBS-SRC-009', 'Official specialization listed on the MCS department page.'),
        ('mubs-cmu-master-business-administration', 'PATHWAY', 'Project Management', 1, TRUE, NULL, 'MUBS-SRC-012', 'Official MBA pathway listed on the Cardiff Met at MUBS overview page.'),
        ('mubs-cmu-master-business-administration', 'PATHWAY', 'Human Resource Management', 2, FALSE, NULL, 'MUBS-SRC-012', 'Official MBA pathway listed on the Cardiff Met at MUBS overview page.'),
        ('mubs-cmu-master-business-administration', 'PATHWAY', 'Marketing', 3, FALSE, NULL, 'MUBS-SRC-012', 'Official MBA pathway listed on the Cardiff Met at MUBS overview page.'),
        ('mubs-cmu-master-business-administration', 'PATHWAY', 'Health Sector Management', 4, FALSE, NULL, 'MUBS-SRC-012', 'Official MBA pathway listed on the Cardiff Met at MUBS overview page.'),
        ('mubs-cmu-master-business-administration', 'PATHWAY', 'Supply Chain & Logistics Management', 5, FALSE, NULL, 'MUBS-SRC-012', 'Official MBA pathway listed on the Cardiff Met at MUBS overview page.');

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
    FROM mubs_track_seed t
    JOIN graduate_program gp
      ON gp.university_id = v_university_id
     AND gp.program_key = t.program_key
    JOIN mubs_source_seed ss
      ON ss.source_id = t.source_id
    JOIN source s
      ON s.university_id = v_university_id
     AND s.url = ss.url
    ON CONFLICT (program_id, track_type, track_name) DO UPDATE SET
        track_order = EXCLUDED.track_order,
        is_primary = EXCLUDED.is_primary,
        description = EXCLUDED.description,
        source_id = EXCLUDED.source_id,
        notes = EXCLUDED.notes,
        updated_at = NOW();

    CREATE TEMP TABLE mubs_program_source_seed (
        program_key TEXT NOT NULL,
        source_id TEXT NOT NULL,
        source_role TEXT NOT NULL,
        source_order INTEGER NOT NULL,
        evidence_text TEXT,
        notes TEXT
    ) ON COMMIT DROP;

    INSERT INTO mubs_program_source_seed (program_key, source_id, source_role, source_order, evidence_text, notes)
    VALUES
        ('mubs-fas-master-computer-science', 'MUBS-SRC-009', 'PRIMARY', 1, 'Computer Science Department page confirming the MCS and its specialization areas.', 'Primary official source.'),
        ('mubs-fas-master-computer-science', 'MUBS-SRC-018', 'ADMISSIONS', 2, 'Official UMS application URL confirming the MCS major name.', 'Admissions application source.'),
        ('mubs-cmu-master-business-administration', 'MUBS-SRC-013', 'PRIMARY', 1, 'MBA General program page with overview, core modules, pathways, and application steps.', 'Primary official source.'),
        ('mubs-cmu-master-business-administration', 'MUBS-SRC-012', 'FACULTY', 2, 'Cardiff Met at MUBS overview page listing the MBA pathways at a glance.', 'Faculty overview source.'),
        ('mubs-cmu-master-business-administration', 'MUBS-SRC-016', 'ADMISSIONS', 3, 'Official MUBS info request page describing the British master degree and hybrid/flexible structure.', 'Admissions information source.'),
        ('mubs-cmu-master-business-administration', 'MUBS-SRC-023', 'PDF', 4, 'Cardiff MBA PDF describing structure, double degree, evening classes, and recognition.', 'Supporting PDF source.');

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
    FROM mubs_program_source_seed ps
    JOIN graduate_program gp
      ON gp.university_id = v_university_id
     AND gp.program_key = ps.program_key
    JOIN mubs_source_seed ss
      ON ss.source_id = ps.source_id
    JOIN source s
      ON s.university_id = v_university_id
     AND s.url = ss.url
    ON CONFLICT (program_id, source_id, source_role) DO UPDATE SET
        source_order = EXCLUDED.source_order,
        evidence_text = EXCLUDED.evidence_text,
        notes = EXCLUDED.notes,
        updated_at = NOW();

    CREATE TEMP TABLE mubs_admission_requirement_seed (
        scope_level TEXT NOT NULL,
        record_key TEXT PRIMARY KEY,
        program_key TEXT,
        requirement_type TEXT NOT NULL,
        requirement_text TEXT NOT NULL,
        comparison_operator TEXT,
        threshold_value NUMERIC(12, 2),
        threshold_unit TEXT,
        is_required BOOLEAN NOT NULL DEFAULT TRUE,
        notes TEXT,
        source_id TEXT NOT NULL
    ) ON COMMIT DROP;

    INSERT INTO mubs_admission_requirement_seed (
        scope_level, record_key, program_key, requirement_type, requirement_text,
        comparison_operator, threshold_value, threshold_unit, is_required, notes, source_id
    )
    VALUES
        ('UNIVERSITY', 'mubs-adm-university-application-flow', NULL, 'GENERAL', 'Use the official MUBS graduate application flow and supporting documents.', NULL, NULL, NULL, TRUE, 'Admission landing page and catalogue references.', 'MUBS-SRC-002'),
        ('UNIVERSITY', 'mubs-adm-university-bachelor-degree', NULL, 'ACADEMIC', 'Bachelor degree from a fully accredited higher-education institution.', NULL, NULL, NULL, TRUE, 'Recovered from the official catalogue admissions text.', 'MUBS-SRC-019'),
        ('UNIVERSITY', 'mubs-adm-university-gpa-275', NULL, 'ACADEMIC', 'Minimum overall undergraduate GPA of 2.75.', '>=', 2.75, 'GPA', TRUE, 'Recovered from the official catalogue admissions text.', 'MUBS-SRC-022'),
        ('UNIVERSITY', 'mubs-adm-university-toefl-pbt-600', NULL, 'ENGLISH', 'TOEFL paper-based score of 600.', '>=', 600, 'TOEFL score', TRUE, 'Recovered from the official catalogue admissions text.', 'MUBS-SRC-019'),
        ('UNIVERSITY', 'mubs-adm-university-toefl-cbt-250', NULL, 'ENGLISH', 'TOEFL computer-based score of 250.', '>=', 250, 'TOEFL score', TRUE, 'Recovered from the official catalogue admissions text.', 'MUBS-SRC-019'),
        ('UNIVERSITY', 'mubs-adm-university-toefl-ibt-100', NULL, 'ENGLISH', 'TOEFL internet-based score of 100.', '>=', 100, 'TOEFL score', TRUE, 'Recovered from the official catalogue admissions text.', 'MUBS-SRC-019'),
        ('PROGRAM', 'mubs-adm-mcs-prerequisite-cs-degree', 'mubs-fas-master-computer-science', 'PREREQUISITE', 'Designed for graduates with a computer science bachelor degree.', NULL, NULL, NULL, TRUE, 'Program-specific MCS admission wording from the department page.', 'MUBS-SRC-009');

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
        CASE WHEN a.scope_level = 'PROGRAM' AND a.program_key = 'mubs-fas-master-computer-science' THEN f.id ELSE NULL END,
        CASE WHEN a.scope_level = 'PROGRAM' AND a.program_key = 'mubs-fas-master-computer-science' THEN d.id ELSE NULL END,
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
    FROM mubs_admission_requirement_seed a
    LEFT JOIN graduate_program gp
      ON gp.university_id = v_university_id
     AND gp.program_key = a.program_key
    LEFT JOIN university_faculty f
      ON f.university_id = v_university_id
     AND f.name = 'Faculty of Arts and Sciences'
    LEFT JOIN university_department d
      ON d.university_id = v_university_id
     AND d.name = 'Computer Science Department'
    JOIN mubs_source_seed ss
      ON ss.source_id = a.source_id
    JOIN source s
      ON s.university_id = v_university_id
     AND s.url = ss.url
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

    CREATE TEMP TABLE mubs_required_document_seed (
        scope_level TEXT NOT NULL,
        record_key TEXT PRIMARY KEY,
        document_type TEXT NOT NULL,
        document_name TEXT NOT NULL,
        is_optional BOOLEAN NOT NULL DEFAULT FALSE,
        sort_order INTEGER,
        notes TEXT,
        source_id TEXT NOT NULL
    ) ON COMMIT DROP;

    INSERT INTO mubs_required_document_seed (
        scope_level, record_key, document_type, document_name, is_optional, sort_order, notes, source_id
    )
    VALUES
        ('UNIVERSITY', 'mubs-doc-application-form', 'APPLICATION_FORM', 'Graduate application form', FALSE, 1, 'Listed in the official catalogue admissions text.', 'MUBS-SRC-019'),
        ('UNIVERSITY', 'mubs-doc-certified-certificates', 'ACADEMIC_RECORD', 'Certified copies of official certificates or diplomas', FALSE, 2, 'Listed in the official catalogue admissions text.', 'MUBS-SRC-019'),
        ('UNIVERSITY', 'mubs-doc-official-transcripts', 'ACADEMIC_RECORD', 'Official transcripts', FALSE, 3, 'Listed in the official catalogue admissions text.', 'MUBS-SRC-019'),
        ('UNIVERSITY', 'mubs-doc-id-passport-copy', 'IDENTIFICATION', 'Identity card or passport copy', FALSE, 4, 'Listed in the official catalogue admissions text.', 'MUBS-SRC-019'),
        ('UNIVERSITY', 'mubs-doc-family-status', 'CIVIL_STATUS', 'Family status document', FALSE, 5, 'Listed in the official catalogue admissions text.', 'MUBS-SRC-019'),
        ('UNIVERSITY', 'mubs-doc-recommendation-letters', 'REFERENCE', 'Two recommendation letters', FALSE, 6, 'Listed in the official catalogue admissions text.', 'MUBS-SRC-019'),
        ('UNIVERSITY', 'mubs-doc-admission-test', 'TEST', 'MUBS Graduate Admission Test', FALSE, 7, 'Listed in the official catalogue admissions text.', 'MUBS-SRC-019'),
        ('UNIVERSITY', 'mubs-doc-cv', 'CURRICULUM_VITAE', 'CV', FALSE, 8, 'Listed in the official catalogue admissions text.', 'MUBS-SRC-019'),
        ('UNIVERSITY', 'mubs-doc-photos', 'PHOTOGRAPH', 'Two photos', FALSE, 9, 'Listed in the official catalogue admissions text.', 'MUBS-SRC-019');

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
        d.scope_level,
        d.record_key,
        d.document_type,
        d.document_name,
        d.is_optional,
        d.sort_order,
        d.notes,
        s.id
    FROM mubs_required_document_seed d
    JOIN mubs_source_seed ss
      ON ss.source_id = d.source_id
    JOIN source s
      ON s.university_id = v_university_id
     AND s.url = ss.url
    ON CONFLICT (university_id, record_key) DO UPDATE SET
        scope_level = EXCLUDED.scope_level,
        document_type = EXCLUDED.document_type,
        document_name = EXCLUDED.document_name,
        is_optional = EXCLUDED.is_optional,
        sort_order = EXCLUDED.sort_order,
        notes = EXCLUDED.notes,
        source_id = EXCLUDED.source_id,
        updated_at = NOW();

    CREATE TEMP TABLE mubs_scholarship_seed (
        record_key TEXT PRIMARY KEY,
        name TEXT NOT NULL,
        description TEXT,
        coverage TEXT,
        notes TEXT,
        source_id TEXT NOT NULL
    ) ON COMMIT DROP;

    INSERT INTO mubs_scholarship_seed (record_key, name, description, coverage, notes, source_id)
    VALUES
        ('mubs-scholarship-overview', 'Scholarships', 'MUBS financial aid page lists scholarships as a support type.', 'Scholarship support', 'University-level scholarship overview.', 'MUBS-SRC-006'),
        ('mubs-scholarship-academic-merits', 'Academic merits', 'MUBS financial aid page lists academic merits as a support type.', 'Merit-based support', 'University-level merit support.', 'MUBS-SRC-006'),
        ('mubs-scholarship-athletic', 'Athletic scholarship', 'MUBS financial aid page lists athletic scholarship as a support type.', 'Athletic support', 'University-level athletic support.', 'MUBS-SRC-006');

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
    FROM mubs_scholarship_seed s
    JOIN mubs_source_seed ss
      ON ss.source_id = s.source_id
    JOIN source src
      ON src.university_id = v_university_id
     AND src.url = ss.url
    ON CONFLICT (university_id, record_key) DO UPDATE SET
        name = EXCLUDED.name,
        description = EXCLUDED.description,
        coverage = EXCLUDED.coverage,
        notes = EXCLUDED.notes,
        source_id = EXCLUDED.source_id,
        updated_at = NOW();

    CREATE TEMP TABLE mubs_financial_aid_seed (
        record_key TEXT PRIMARY KEY,
        name TEXT NOT NULL,
        description TEXT,
        notes TEXT,
        source_id TEXT NOT NULL
    ) ON COMMIT DROP;

    INSERT INTO mubs_financial_aid_seed (record_key, name, description, notes, source_id)
    VALUES
        ('mubs-aid-need-based-grants', 'Need-based grants', 'Financial aid intended to help qualifying students and families cover higher-education expenses.', 'Need-based aid overview from the official financial-aid page.', 'MUBS-SRC-006'),
        ('mubs-aid-general-support', 'Financial aid for qualifying students and families', 'MUBS financial aid supports students and families according to academic achievement and demonstrated need.', 'General financial-aid overview from the official financial-aid page.', 'MUBS-SRC-006');

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
    FROM mubs_financial_aid_seed f
    JOIN mubs_source_seed ss
      ON ss.source_id = f.source_id
    JOIN source src
      ON src.university_id = v_university_id
     AND src.url = ss.url
    ON CONFLICT (university_id, record_key) DO UPDATE SET
        name = EXCLUDED.name,
        description = EXCLUDED.description,
        notes = EXCLUDED.notes,
        source_id = EXCLUDED.source_id,
        updated_at = NOW();

    CREATE TEMP TABLE mubs_accreditation_seed (
        scope_level TEXT NOT NULL,
        record_key TEXT PRIMARY KEY,
        program_key TEXT,
        name TEXT NOT NULL,
        authority TEXT,
        status TEXT,
        notes TEXT,
        source_id TEXT NOT NULL
    ) ON COMMIT DROP;

    INSERT INTO mubs_accreditation_seed (scope_level, record_key, program_key, name, authority, status, notes, source_id)
    VALUES
        ('PROGRAM', 'mubs-accred-mcs-mehe', 'mubs-fas-master-computer-science', 'MEHE accreditation', 'Ministry of Education and Higher Education', 'ACCREDITED', 'Computer Science Department page states MEHE accreditation for BCS and MCS.', 'MUBS-SRC-009'),
        ('PROGRAM', 'mubs-accred-cardiff-mba-recognition', 'mubs-cmu-master-business-administration', 'Cardiff Metropolitan University recognition', 'Cardiff Metropolitan University', 'RECOGNIZED', 'Official MUBS pages and the Cardiff MBA PDF describe the British degree and recognition context.', 'MUBS-SRC-016');

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
        CASE WHEN a.program_key = 'mubs-fas-master-computer-science' THEN f.id WHEN a.program_key = 'mubs-cmu-master-business-administration' THEN cf.id ELSE NULL END,
        CASE WHEN a.program_key = 'mubs-fas-master-computer-science' THEN d.id ELSE NULL END,
        gp.id,
        a.scope_level,
        a.record_key,
        a.name,
        a.authority,
        a.status,
        NULL,
        NULL,
        a.notes,
        s.id
    FROM mubs_accreditation_seed a
    LEFT JOIN graduate_program gp
      ON gp.university_id = v_university_id
     AND gp.program_key = a.program_key
    LEFT JOIN university_faculty f
      ON f.university_id = v_university_id
     AND f.name = 'Faculty of Arts and Sciences'
    LEFT JOIN university_faculty cf
      ON cf.university_id = v_university_id
     AND cf.name = 'Cardiff Metropolitan University at MUBS'
    LEFT JOIN university_department d
      ON d.university_id = v_university_id
     AND d.name = 'Computer Science Department'
    JOIN mubs_source_seed ss
      ON ss.source_id = a.source_id
    JOIN source s
      ON s.university_id = v_university_id
     AND s.url = ss.url
    ON CONFLICT (university_id, record_key) DO UPDATE SET
        faculty_id = EXCLUDED.faculty_id,
        department_id = EXCLUDED.department_id,
        program_id = EXCLUDED.program_id,
        name = EXCLUDED.name,
        authority = EXCLUDED.authority,
        status = EXCLUDED.status,
        notes = EXCLUDED.notes,
        source_id = EXCLUDED.source_id,
        updated_at = NOW();

END $$;
