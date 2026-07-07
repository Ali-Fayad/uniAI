-- ESA graduate data seed migration.
-- Idempotent import for the canonical ESA graduate dataset.

DO $$
DECLARE
    v_university_id BIGINT;
    v_faculty_id BIGINT;
    v_lang_en_id BIGINT;
    v_lang_fr_id BIGINT;
    v_lang_ar_id BIGINT;
    v_lang_multi_id BIGINT;
    v_adm_general_id BIGINT;
    v_adm_mba_id BIGINT;
    v_adm_emfm_id BIGINT;
    v_adm_mems_id BIGINT;
BEGIN

    INSERT INTO university (name, name_ar, acronym, country, city, latitude, longitude, campus_name, campus_type)
    SELECT 'École Supérieure des Affaires', NULL, 'ESA', 'Lebanon', NULL, NULL, NULL, NULL, NULL
    WHERE NOT EXISTS (SELECT 1 FROM university WHERE name = 'École Supérieure des Affaires');

    SELECT id INTO v_university_id
    FROM university
    WHERE name = 'École Supérieure des Affaires'
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

    SELECT id INTO v_lang_en_id FROM language WHERE code = 'en' ORDER BY id LIMIT 1;
    SELECT id INTO v_lang_fr_id FROM language WHERE code = 'fr' ORDER BY id LIMIT 1;
    SELECT id INTO v_lang_ar_id FROM language WHERE code = 'ar' ORDER BY id LIMIT 1;
    SELECT id INTO v_lang_multi_id FROM language WHERE code = 'multi' ORDER BY id LIMIT 1;

    INSERT INTO source (university_id, title, url, source_type, accessed_at, notes) VALUES
        (v_university_id, 'ESA | Home', 'https://www.esa.edu.lb/english/home', 'official_webpage', '2026-07-07', 'Official ESA home page; lists current graduate programs and separates degree programs from executive education/certificates.'),
        (v_university_id, 'ESA | Overview', 'https://www.esa.edu.lb/en/programs', 'official_webpage', '2026-07-07', 'Official degree-program overview page; states that MIM, MBA, EMBA, specialized masters, and DBA are part of ESA degree programs.'),
        (v_university_id, 'ESA | Aperçu', 'https://www.esa.edu.lb/french/formations-diplomantes/apercu', 'official_webpage', '2026-07-07', 'French official degree-program overview page; lists all programs including MIM, MENT, MIAD, MBA, EMLux, MMC, MEMS, EMFM, EMBA, GEMBA, and DBA.'),
        (v_university_id, 'ESA | Master in International Management (MIM)', 'https://www.esa.edu.lb/english/master-in-international-management', 'official_webpage', '2026-07-07', 'Official MIM program page; confirms diplomas, 24-month duration, schedule, and program structure.'),
        (v_university_id, 'ESA | Master in Innovation and Entrepreneurship (MENT)', 'https://www.esa.edu.lb/program/master-in-entrepreneurship', 'official_webpage', '2026-07-07', 'Official MENT program page; confirms Master in Innovation and Entrepreneurship co-signed by ESA and HEC Paris, duration, target, and language.'),
        (v_university_id, 'ESA | Specialized Master in International Affairs and Diplomacy (MIAD)', 'https://www.esa.edu.lb/english/specialized-master-in-international-affairs-and-diplomacy', 'official_webpage', '2026-07-07', 'Official MIAD program page; confirms specialized master co-signed by ESA and UNITAR and 20-month structure.'),
        (v_university_id, 'ESA | Master in Business Administration (MBA)', 'https://www.esa.edu.lb/master-in-business-administration', 'official_webpage', '2026-07-07', 'Official MBA page; confirms admissions criteria and AMBA/BGA context.'),
        (v_university_id, 'ESA | Executive Master in Luxury Transformation and Leadership (EMLux)', 'https://www.esa.edu.lb/english/executive-master-in-luxury-transformation-and-leadership/emlux', 'official_webpage', '2026-07-07', 'Official EMLux page; confirms an Executive Master program, 15-month part-time format, and target audience.'),
        (v_university_id, 'ESA | Specialized Master in Marketing and Communication (MMC)', 'https://www.esa.edu.lb/english/specialized-master-in-marketing-and-communication', 'official_webpage', '2026-07-07', 'Official MMC page; confirms two international Specialized Master diplomas from ESA and ESCP.'),
        (v_university_id, 'ESA | Master Exécutif en Management de la Santé (MEMS)', 'https://www.esa.edu.lb/french/formation-diplomante/master-executif-en-management-de-la-sante', 'official_webpage', '2026-07-07', 'Official MEMS page; confirms ESA Executive Master and Université Paris Cité Master 2 AMES, admissions, dates, tuition/aid summary.'),
        (v_university_id, 'ESA | Executive Master in Financial Management (EMFM)', 'https://www.esa.edu.lb/english/executive-master-in-financial-management', 'official_webpage', '2026-07-07', 'Official EMFM page; confirms admissions criteria, tuition-fee brochure pointer, scholarships, and financial aid summary.'),
        (v_university_id, 'ESA | Executive MBA (EMBA)', 'https://www.esa.edu.lb/executivemba', 'official_webpage', '2026-07-07', 'Official EMBA page; confirms Executive MBA program and leadership/professional target.'),
        (v_university_id, 'ESA | Global Executive MBA (GEMBA)', 'https://www.esa.edu.lb/english/global-emba', 'official_webpage', '2026-07-07', 'Official Global EMBA page; describes optional international program for ESA EMBA participants with ESCP seminars/electives/final project.'),
        (v_university_id, 'ESA | Doctorate in Business Administration (DBA)', 'https://www.esa.edu.lb/english/program/doctorate-in-business-administration', 'official_webpage', '2026-07-07', 'Official DBA page; confirms Doctorate in Business Administration objectives and doctoral-level research/professional program.'),
        (v_university_id, 'ESA | Admissions', 'https://www.esa.edu.lb/english/formation-diplomante/admission', 'official_webpage', '2026-07-07', 'Official admissions/orientation page for degree programs.'),
        (v_university_id, 'ESA | FAQs', 'https://www.esa.edu.lb/english/formation-diplomante/faqs', 'official_webpage', '2026-07-07', 'Official FAQ page; summarizes prerequisites, duration, work-experience requirements, schedules, and language notes across programs.'),
        (v_university_id, 'ESA | Financial Aid', 'https://www.esa.edu.lb/financial-aid', 'official_webpage', '2026-07-07', 'Official financial aid page; describes scholarships, solidarity aid, application deadlines/requirements, and exceptions.'),
        (v_university_id, 'ESA Solidarity Financial Aid Application 2025-2026', 'https://www.esa.edu.lb/Library/EditorFiles/ESA%20Solidarity%20Financial%20Aid%20Application%202025-2026%20%28procedure%20and%20list%20of%20required%20documents%29.pdf', 'official_pdf', '2026-07-07', 'Official PDF with solidarity financial aid procedure and required documents for 2025-2026.')
    ON CONFLICT (university_id, url) DO UPDATE SET
        title = EXCLUDED.title,
        source_type = EXCLUDED.source_type,
        accessed_at = EXCLUDED.accessed_at,
        notes = EXCLUDED.notes,
        updated_at = NOW();

    INSERT INTO university_faculty (university_id, name, short_name, faculty_type, official_url, notes) VALUES
        (v_university_id, 'ESA Business School', NULL, 'SCHOOL', NULL, 'Imported from the official ESA graduate inventory.')
    ON CONFLICT (university_id, name) DO UPDATE SET
        short_name = EXCLUDED.short_name,
        faculty_type = EXCLUDED.faculty_type,
        official_url = EXCLUDED.official_url,
        notes = EXCLUDED.notes,
        updated_at = NOW();

    SELECT id INTO v_faculty_id
    FROM university_faculty
    WHERE university_id = v_university_id
      AND name = 'ESA Business School'
    ORDER BY id
    LIMIT 1;

    INSERT INTO graduate_program (
        university_id, faculty_id, department_id, degree_type_id, program_key, major_category, major,
        official_degree_name, thesis_or_non_thesis, credits, duration_value, duration_unit,
        primary_language_id, delivery_mode, program_description, official_program_url, source_id, notes
    )
    SELECT
        v_university_id,
        v_faculty_id,
        NULL,
        dt.id,
        'esa-mim',
        'International Management',
        'International Management',
        'Master in International Management',
        NULL,
        NULL,
        24,
        'MONTHS',
        NULL,
        NULL,
        'Official ESA Master in International Management program with a 24-month structure and program diplomas/tracks described on the program page.',
        'https://www.esa.edu.lb/english/master-in-international-management',
        src.id,
        'Official ESA degree-program evidence confirms the Master in International Management (MIM). The page also references the program''s structure and duration.'
    FROM degree_type dt
    JOIN source src ON src.university_id = v_university_id AND src.url = 'https://www.esa.edu.lb/english/master-in-international-management'
    WHERE dt.code = 'MASTER'
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
        v_faculty_id,
        NULL,
        dt.id,
        'esa-ment',
        'Innovation and Entrepreneurship',
        'Innovation and Entrepreneurship',
        'Master in Innovation and Entrepreneurship',
        NULL,
        NULL,
        NULL,
        NULL,
        NULL,
        NULL,
        'Official ESA Master in Innovation and Entrepreneurship co-signed by ESA Business School and HEC Paris.',
        'https://www.esa.edu.lb/program/master-in-entrepreneurship',
        src.id,
        'Official ESA evidence confirms the Master in Innovation and Entrepreneurship (MENT), co-signed by ESA Business School and HEC Paris.'
    FROM degree_type dt
    JOIN source src ON src.university_id = v_university_id AND src.url = 'https://www.esa.edu.lb/program/master-in-entrepreneurship'
    WHERE dt.code = 'MASTER'
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
        v_faculty_id,
        NULL,
        dt.id,
        'esa-miad',
        'International Affairs and Diplomacy',
        'International Affairs and Diplomacy',
        'Specialized Master in International Affairs and Diplomacy',
        NULL,
        NULL,
        20,
        'MONTHS',
        NULL,
        NULL,
        'Official ESA Specialized Master in International Affairs and Diplomacy co-signed by ESA Business School and UNITAR.',
        'https://www.esa.edu.lb/english/specialized-master-in-international-affairs-and-diplomacy',
        src.id,
        'Official ESA evidence confirms the Specialized Master in International Affairs and Diplomacy (MIAD), co-signed by ESA Business School and UNITAR.'
    FROM degree_type dt
    JOIN source src ON src.university_id = v_university_id AND src.url = 'https://www.esa.edu.lb/english/specialized-master-in-international-affairs-and-diplomacy'
    WHERE dt.code = 'MASTER'
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
        v_faculty_id,
        NULL,
        dt.id,
        'esa-mba',
        'Business Administration',
        'Business Administration',
        'Master in Business Administration',
        NULL,
        NULL,
        NULL,
        NULL,
        v_lang_multi_id,
        NULL,
        'Official ESA Master in Business Administration program with admissions criteria and AMBA/BGA accreditation context published on the MBA page.',
        'https://www.esa.edu.lb/master-in-business-administration',
        src.id,
        'Official ESA evidence confirms the Master in Business Administration (MBA). Any MBA variant content was not split into separate program rows because the discovery notes did not confirm a separate degree award.'
    FROM degree_type dt
    JOIN source src ON src.university_id = v_university_id AND src.url = 'https://www.esa.edu.lb/master-in-business-administration'
    WHERE dt.code = 'MASTER'
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
        v_faculty_id,
        NULL,
        dt.id,
        'esa-emlux',
        'Luxury Transformation and Leadership',
        'Luxury Transformation and Leadership',
        'Executive Master in Luxury Transformation and Leadership',
        NULL,
        NULL,
        15,
        'MONTHS',
        NULL,
        NULL,
        'Official ESA Executive Master in Luxury Transformation and Leadership.',
        'https://www.esa.edu.lb/english/executive-master-in-luxury-transformation-and-leadership/emlux',
        src.id,
        'Official ESA evidence confirms the Executive Master in Luxury Transformation and Leadership (EMLux).'
    FROM degree_type dt
    JOIN source src ON src.university_id = v_university_id AND src.url = 'https://www.esa.edu.lb/english/executive-master-in-luxury-transformation-and-leadership/emlux'
    WHERE dt.code = 'MASTER'
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
        v_faculty_id,
        NULL,
        dt.id,
        'esa-mmc',
        'Marketing and Communication',
        'Marketing and Communication',
        'Specialized Master in Marketing and Communication',
        NULL,
        NULL,
        NULL,
        NULL,
        v_lang_fr_id,
        NULL,
        'Official ESA Specialized Master in Marketing and Communication co-signed by ESA Business School and ESCP Business School.',
        'https://www.esa.edu.lb/english/specialized-master-in-marketing-and-communication',
        src.id,
        'Official ESA evidence confirms the Specialized Master in Marketing and Communication (MMC), co-signed by ESA Business School and ESCP Business School.'
    FROM degree_type dt
    JOIN source src ON src.university_id = v_university_id AND src.url = 'https://www.esa.edu.lb/english/specialized-master-in-marketing-and-communication'
    WHERE dt.code = 'MASTER'
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
        v_faculty_id,
        NULL,
        dt.id,
        'esa-mems',
        'Health Management',
        'Healthcare Management',
        'Master Exécutif en Management de la Santé / Executive Master in Healthcare Management',
        NULL,
        NULL,
        NULL,
        NULL,
        NULL,
        NULL,
        'Official ESA Executive Master in Healthcare Management jointly described with Université Paris Cité''s Master 2 AMES.',
        'https://www.esa.edu.lb/french/formation-diplomante/master-executif-en-management-de-la-sante',
        src.id,
        'Official ESA evidence confirms the MEMS executive master and its connection to Université Paris Cité (Master 2 AMES).'
    FROM degree_type dt
    JOIN source src ON src.university_id = v_university_id AND src.url = 'https://www.esa.edu.lb/french/formation-diplomante/master-executif-en-management-de-la-sante'
    WHERE dt.code = 'MASTER'
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
        v_faculty_id,
        NULL,
        dt.id,
        'esa-emfm',
        'Financial Management',
        'Financial Management',
        'Executive Master in Financial Management',
        NULL,
        NULL,
        NULL,
        NULL,
        NULL,
        NULL,
        'Official ESA Executive Master in Financial Management program.',
        'https://www.esa.edu.lb/english/executive-master-in-financial-management',
        src.id,
        'Official ESA evidence confirms the Executive Master in Financial Management (EMFM).'
    FROM degree_type dt
    JOIN source src ON src.university_id = v_university_id AND src.url = 'https://www.esa.edu.lb/english/executive-master-in-financial-management'
    WHERE dt.code = 'MASTER'
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
        v_faculty_id,
        NULL,
        dt.id,
        'esa-emba',
        'Business Administration',
        'Executive Business Administration',
        'Executive MBA',
        NULL,
        NULL,
        NULL,
        NULL,
        NULL,
        NULL,
        'Official ESA Executive MBA program for professional participants, with GEMBA described as an optional international pathway.',
        'https://www.esa.edu.lb/executivemba',
        src.id,
        'Official ESA evidence confirms the Executive MBA (EMBA). The Global Executive MBA page describes GEMBA as an optional international pathway for EMBA participants rather than a separate degree, so it is modeled here as a track.'
    FROM degree_type dt
    JOIN source src ON src.university_id = v_university_id AND src.url = 'https://www.esa.edu.lb/executivemba'
    WHERE dt.code = 'MASTER'
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
        v_faculty_id,
        NULL,
        dt.id,
        'esa-dba',
        'Business Administration',
        'Business Administration',
        'Doctorate in Business Administration',
        NULL,
        NULL,
        NULL,
        NULL,
        NULL,
        NULL,
        'Official ESA doctorate-level program for executives and business leaders focused on business administration research.',
        'https://www.esa.edu.lb/english/program/doctorate-in-business-administration',
        src.id,
        'ESA publishes a doctorate-level DBA rather than a separate PhD-branded program, so the doctoral evidence is recorded as a PHD-row degree with the official DBA title.'
    FROM degree_type dt
    JOIN source src ON src.university_id = v_university_id AND src.url = 'https://www.esa.edu.lb/english/program/doctorate-in-business-administration'
    WHERE dt.code = 'PHD'
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

    INSERT INTO graduate_program_source (university_id, program_id, source_id, source_role, source_order, evidence_text, notes)
    SELECT v_university_id, gp.id, s.id, 'PRIMARY', 1, 'Official MIM program page.', 'Primary ESA program source.'
    FROM graduate_program gp
    JOIN source s ON s.university_id = v_university_id AND s.url = 'https://www.esa.edu.lb/english/master-in-international-management'
    WHERE gp.university_id = v_university_id AND gp.program_key = 'esa-mim'
    ON CONFLICT (program_id, source_id, source_role) DO UPDATE SET evidence_text = EXCLUDED.evidence_text, notes = EXCLUDED.notes, updated_at = NOW();

    INSERT INTO graduate_program_source (university_id, program_id, source_id, source_role, source_order, evidence_text, notes)
    SELECT v_university_id, gp.id, s.id, 'SECONDARY', 2, 'Official degree-program overview page.', 'Cross-check source.'
    FROM graduate_program gp
    JOIN source s ON s.university_id = v_university_id AND s.url = 'https://www.esa.edu.lb/en/programs'
    WHERE gp.university_id = v_university_id AND gp.program_key = 'esa-mim'
    ON CONFLICT (program_id, source_id, source_role) DO UPDATE SET evidence_text = EXCLUDED.evidence_text, notes = EXCLUDED.notes, updated_at = NOW();

    INSERT INTO graduate_program_source (university_id, program_id, source_id, source_role, source_order, evidence_text, notes)
    SELECT v_university_id, gp.id, s.id, 'OTHER', 3, 'Official ESA home page listing current graduate programs.', 'Context source.'
    FROM graduate_program gp
    JOIN source s ON s.university_id = v_university_id AND s.url = 'https://www.esa.edu.lb/english/home'
    WHERE gp.university_id = v_university_id AND gp.program_key = 'esa-mim'
    ON CONFLICT (program_id, source_id, source_role) DO UPDATE SET evidence_text = EXCLUDED.evidence_text, notes = EXCLUDED.notes, updated_at = NOW();

    INSERT INTO graduate_program_source (university_id, program_id, source_id, source_role, source_order, evidence_text, notes)
    SELECT v_university_id, gp.id, s.id, 'PRIMARY', 1, 'Official MENT program page.', 'Primary ESA program source.'
    FROM graduate_program gp
    JOIN source s ON s.university_id = v_university_id AND s.url = 'https://www.esa.edu.lb/program/master-in-entrepreneurship'
    WHERE gp.university_id = v_university_id AND gp.program_key = 'esa-ment'
    ON CONFLICT (program_id, source_id, source_role) DO UPDATE SET evidence_text = EXCLUDED.evidence_text, notes = EXCLUDED.notes, updated_at = NOW();

    INSERT INTO graduate_program_source (university_id, program_id, source_id, source_role, source_order, evidence_text, notes)
    SELECT v_university_id, gp.id, s.id, 'SECONDARY', 2, 'Official French program overview page.', 'Cross-check source.'
    FROM graduate_program gp
    JOIN source s ON s.university_id = v_university_id AND s.url = 'https://www.esa.edu.lb/french/formations-diplomantes/apercu'
    WHERE gp.university_id = v_university_id AND gp.program_key = 'esa-ment'
    ON CONFLICT (program_id, source_id, source_role) DO UPDATE SET evidence_text = EXCLUDED.evidence_text, notes = EXCLUDED.notes, updated_at = NOW();

    INSERT INTO graduate_program_source (university_id, program_id, source_id, source_role, source_order, evidence_text, notes)
    SELECT v_university_id, gp.id, s.id, 'PRIMARY', 1, 'Official MIAD program page.', 'Primary ESA program source.'
    FROM graduate_program gp
    JOIN source s ON s.university_id = v_university_id AND s.url = 'https://www.esa.edu.lb/english/specialized-master-in-international-affairs-and-diplomacy'
    WHERE gp.university_id = v_university_id AND gp.program_key = 'esa-miad'
    ON CONFLICT (program_id, source_id, source_role) DO UPDATE SET evidence_text = EXCLUDED.evidence_text, notes = EXCLUDED.notes, updated_at = NOW();

    INSERT INTO graduate_program_source (university_id, program_id, source_id, source_role, source_order, evidence_text, notes)
    SELECT v_university_id, gp.id, s.id, 'SECONDARY', 2, 'Official French program overview page.', 'Cross-check source.'
    FROM graduate_program gp
    JOIN source s ON s.university_id = v_university_id AND s.url = 'https://www.esa.edu.lb/french/formations-diplomantes/apercu'
    WHERE gp.university_id = v_university_id AND gp.program_key = 'esa-miad'
    ON CONFLICT (program_id, source_id, source_role) DO UPDATE SET evidence_text = EXCLUDED.evidence_text, notes = EXCLUDED.notes, updated_at = NOW();

    INSERT INTO graduate_program_source (university_id, program_id, source_id, source_role, source_order, evidence_text, notes)
    SELECT v_university_id, gp.id, s.id, 'PRIMARY', 1, 'Official MBA program page.', 'Primary ESA program source.'
    FROM graduate_program gp
    JOIN source s ON s.university_id = v_university_id AND s.url = 'https://www.esa.edu.lb/master-in-business-administration'
    WHERE gp.university_id = v_university_id AND gp.program_key = 'esa-mba'
    ON CONFLICT (program_id, source_id, source_role) DO UPDATE SET evidence_text = EXCLUDED.evidence_text, notes = EXCLUDED.notes, updated_at = NOW();

    INSERT INTO graduate_program_source (university_id, program_id, source_id, source_role, source_order, evidence_text, notes)
    SELECT v_university_id, gp.id, s.id, 'SECONDARY', 2, 'Official degree-program overview page.', 'Cross-check source.'
    FROM graduate_program gp
    JOIN source s ON s.university_id = v_university_id AND s.url = 'https://www.esa.edu.lb/en/programs'
    WHERE gp.university_id = v_university_id AND gp.program_key = 'esa-mba'
    ON CONFLICT (program_id, source_id, source_role) DO UPDATE SET evidence_text = EXCLUDED.evidence_text, notes = EXCLUDED.notes, updated_at = NOW();

    INSERT INTO graduate_program_source (university_id, program_id, source_id, source_role, source_order, evidence_text, notes)
    SELECT v_university_id, gp.id, s.id, 'OTHER', 3, 'Official ESA home page listing current graduate programs.', 'Context source.'
    FROM graduate_program gp
    JOIN source s ON s.university_id = v_university_id AND s.url = 'https://www.esa.edu.lb/english/home'
    WHERE gp.university_id = v_university_id AND gp.program_key = 'esa-mba'
    ON CONFLICT (program_id, source_id, source_role) DO UPDATE SET evidence_text = EXCLUDED.evidence_text, notes = EXCLUDED.notes, updated_at = NOW();

    INSERT INTO graduate_program_source (university_id, program_id, source_id, source_role, source_order, evidence_text, notes)
    SELECT v_university_id, gp.id, s.id, 'PRIMARY', 1, 'Official EMLux program page.', 'Primary ESA program source.'
    FROM graduate_program gp
    JOIN source s ON s.university_id = v_university_id AND s.url = 'https://www.esa.edu.lb/english/executive-master-in-luxury-transformation-and-leadership/emlux'
    WHERE gp.university_id = v_university_id AND gp.program_key = 'esa-emlux'
    ON CONFLICT (program_id, source_id, source_role) DO UPDATE SET evidence_text = EXCLUDED.evidence_text, notes = EXCLUDED.notes, updated_at = NOW();

    INSERT INTO graduate_program_source (university_id, program_id, source_id, source_role, source_order, evidence_text, notes)
    SELECT v_university_id, gp.id, s.id, 'SECONDARY', 2, 'Official French program overview page.', 'Cross-check source.'
    FROM graduate_program gp
    JOIN source s ON s.university_id = v_university_id AND s.url = 'https://www.esa.edu.lb/french/formations-diplomantes/apercu'
    WHERE gp.university_id = v_university_id AND gp.program_key = 'esa-emlux'
    ON CONFLICT (program_id, source_id, source_role) DO UPDATE SET evidence_text = EXCLUDED.evidence_text, notes = EXCLUDED.notes, updated_at = NOW();

    INSERT INTO graduate_program_source (university_id, program_id, source_id, source_role, source_order, evidence_text, notes)
    SELECT v_university_id, gp.id, s.id, 'PRIMARY', 1, 'Official MMC program page.', 'Primary ESA program source.'
    FROM graduate_program gp
    JOIN source s ON s.university_id = v_university_id AND s.url = 'https://www.esa.edu.lb/english/specialized-master-in-marketing-and-communication'
    WHERE gp.university_id = v_university_id AND gp.program_key = 'esa-mmc'
    ON CONFLICT (program_id, source_id, source_role) DO UPDATE SET evidence_text = EXCLUDED.evidence_text, notes = EXCLUDED.notes, updated_at = NOW();

    INSERT INTO graduate_program_source (university_id, program_id, source_id, source_role, source_order, evidence_text, notes)
    SELECT v_university_id, gp.id, s.id, 'SECONDARY', 2, 'Official French program overview page.', 'Cross-check source.'
    FROM graduate_program gp
    JOIN source s ON s.university_id = v_university_id AND s.url = 'https://www.esa.edu.lb/french/formations-diplomantes/apercu'
    WHERE gp.university_id = v_university_id AND gp.program_key = 'esa-mmc'
    ON CONFLICT (program_id, source_id, source_role) DO UPDATE SET evidence_text = EXCLUDED.evidence_text, notes = EXCLUDED.notes, updated_at = NOW();

    INSERT INTO graduate_program_source (university_id, program_id, source_id, source_role, source_order, evidence_text, notes)
    SELECT v_university_id, gp.id, s.id, 'PRIMARY', 1, 'Official MEMS page.', 'Primary ESA program source.'
    FROM graduate_program gp
    JOIN source s ON s.university_id = v_university_id AND s.url = 'https://www.esa.edu.lb/french/formation-diplomante/master-executif-en-management-de-la-sante'
    WHERE gp.university_id = v_university_id AND gp.program_key = 'esa-mems'
    ON CONFLICT (program_id, source_id, source_role) DO UPDATE SET evidence_text = EXCLUDED.evidence_text, notes = EXCLUDED.notes, updated_at = NOW();

    INSERT INTO graduate_program_source (university_id, program_id, source_id, source_role, source_order, evidence_text, notes)
    SELECT v_university_id, gp.id, s.id, 'SECONDARY', 2, 'Official French program overview page.', 'Cross-check source.'
    FROM graduate_program gp
    JOIN source s ON s.university_id = v_university_id AND s.url = 'https://www.esa.edu.lb/french/formations-diplomantes/apercu'
    WHERE gp.university_id = v_university_id AND gp.program_key = 'esa-mems'
    ON CONFLICT (program_id, source_id, source_role) DO UPDATE SET evidence_text = EXCLUDED.evidence_text, notes = EXCLUDED.notes, updated_at = NOW();

    INSERT INTO graduate_program_source (university_id, program_id, source_id, source_role, source_order, evidence_text, notes)
    SELECT v_university_id, gp.id, s.id, 'PRIMARY', 1, 'Official EMFM page.', 'Primary ESA program source.'
    FROM graduate_program gp
    JOIN source s ON s.university_id = v_university_id AND s.url = 'https://www.esa.edu.lb/english/executive-master-in-financial-management'
    WHERE gp.university_id = v_university_id AND gp.program_key = 'esa-emfm'
    ON CONFLICT (program_id, source_id, source_role) DO UPDATE SET evidence_text = EXCLUDED.evidence_text, notes = EXCLUDED.notes, updated_at = NOW();

    INSERT INTO graduate_program_source (university_id, program_id, source_id, source_role, source_order, evidence_text, notes)
    SELECT v_university_id, gp.id, s.id, 'SECONDARY', 2, 'Official degree-program overview page.', 'Cross-check source.'
    FROM graduate_program gp
    JOIN source s ON s.university_id = v_university_id AND s.url = 'https://www.esa.edu.lb/en/programs'
    WHERE gp.university_id = v_university_id AND gp.program_key = 'esa-emfm'
    ON CONFLICT (program_id, source_id, source_role) DO UPDATE SET evidence_text = EXCLUDED.evidence_text, notes = EXCLUDED.notes, updated_at = NOW();

    INSERT INTO graduate_program_source (university_id, program_id, source_id, source_role, source_order, evidence_text, notes)
    SELECT v_university_id, gp.id, s.id, 'PRIMARY', 1, 'Official EMBA page.', 'Primary ESA program source.'
    FROM graduate_program gp
    JOIN source s ON s.university_id = v_university_id AND s.url = 'https://www.esa.edu.lb/executivemba'
    WHERE gp.university_id = v_university_id AND gp.program_key = 'esa-emba'
    ON CONFLICT (program_id, source_id, source_role) DO UPDATE SET evidence_text = EXCLUDED.evidence_text, notes = EXCLUDED.notes, updated_at = NOW();

    INSERT INTO graduate_program_source (university_id, program_id, source_id, source_role, source_order, evidence_text, notes)
    SELECT v_university_id, gp.id, s.id, 'SECONDARY', 2, 'Official degree-program overview page.', 'Cross-check source.'
    FROM graduate_program gp
    JOIN source s ON s.university_id = v_university_id AND s.url = 'https://www.esa.edu.lb/en/programs'
    WHERE gp.university_id = v_university_id AND gp.program_key = 'esa-emba'
    ON CONFLICT (program_id, source_id, source_role) DO UPDATE SET evidence_text = EXCLUDED.evidence_text, notes = EXCLUDED.notes, updated_at = NOW();

    INSERT INTO graduate_program_source (university_id, program_id, source_id, source_role, source_order, evidence_text, notes)
    SELECT v_university_id, gp.id, s.id, 'OTHER', 3, 'Official GEMBA pathway page.', 'Optional EMBA pathway source.'
    FROM graduate_program gp
    JOIN source s ON s.university_id = v_university_id AND s.url = 'https://www.esa.edu.lb/english/global-emba'
    WHERE gp.university_id = v_university_id AND gp.program_key = 'esa-emba'
    ON CONFLICT (program_id, source_id, source_role) DO UPDATE SET evidence_text = EXCLUDED.evidence_text, notes = EXCLUDED.notes, updated_at = NOW();

    INSERT INTO graduate_program_source (university_id, program_id, source_id, source_role, source_order, evidence_text, notes)
    SELECT v_university_id, gp.id, s.id, 'PRIMARY', 1, 'Official DBA page.', 'Primary ESA program source.'
    FROM graduate_program gp
    JOIN source s ON s.university_id = v_university_id AND s.url = 'https://www.esa.edu.lb/english/program/doctorate-in-business-administration'
    WHERE gp.university_id = v_university_id AND gp.program_key = 'esa-dba'
    ON CONFLICT (program_id, source_id, source_role) DO UPDATE SET evidence_text = EXCLUDED.evidence_text, notes = EXCLUDED.notes, updated_at = NOW();

    INSERT INTO graduate_program_source (university_id, program_id, source_id, source_role, source_order, evidence_text, notes)
    SELECT v_university_id, gp.id, s.id, 'SECONDARY', 2, 'Official degree-program overview page.', 'Cross-check source.'
    FROM graduate_program gp
    JOIN source s ON s.university_id = v_university_id AND s.url = 'https://www.esa.edu.lb/en/programs'
    WHERE gp.university_id = v_university_id AND gp.program_key = 'esa-dba'
    ON CONFLICT (program_id, source_id, source_role) DO UPDATE SET evidence_text = EXCLUDED.evidence_text, notes = EXCLUDED.notes, updated_at = NOW();

    INSERT INTO graduate_program_source (university_id, program_id, source_id, source_role, source_order, evidence_text, notes)
    SELECT v_university_id, gp.id, s.id, 'OTHER', 3, 'Official FAQ page.', 'Cross-check source.'
    FROM graduate_program gp
    JOIN source s ON s.university_id = v_university_id AND s.url = 'https://www.esa.edu.lb/english/formation-diplomante/faqs'
    WHERE gp.university_id = v_university_id AND gp.program_key = 'esa-dba'
    ON CONFLICT (program_id, source_id, source_role) DO UPDATE SET evidence_text = EXCLUDED.evidence_text, notes = EXCLUDED.notes, updated_at = NOW();

    INSERT INTO graduate_program_track (
        university_id, program_id, track_type, track_name, track_order, is_primary, description, source_id, notes
    )
    SELECT
        v_university_id,
        gp.id,
        'PATHWAY',
        'Global Executive MBA (GEMBA)',
        1,
        FALSE,
        'Optional international pathway for ESA EMBA participants with seminars, electives, and final project components.',
        src.id,
        'The official GEMBA page describes the pathway as optional for EMBA participants rather than a separate degree.'
    FROM graduate_program gp
    JOIN source src ON src.university_id = v_university_id AND src.url = 'https://www.esa.edu.lb/english/global-emba'
    WHERE gp.university_id = v_university_id
      AND gp.program_key = 'esa-emba'
    ON CONFLICT (program_id, track_type, track_name) DO UPDATE SET
        track_order = EXCLUDED.track_order,
        is_primary = EXCLUDED.is_primary,
        description = EXCLUDED.description,
        source_id = EXCLUDED.source_id,
        notes = EXCLUDED.notes,
        updated_at = NOW();

    INSERT INTO graduate_admission_requirement (
        university_id, faculty_id, department_id, program_id, scope_level, record_key, requirement_type,
        requirement_text, comparison_operator, threshold_value, threshold_unit, is_required, notes, source_id
    ) VALUES
        (v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'esa-adm-general', 'GENERAL', 'ESA publishes a general admissions/orientation page for degree programs and expects applicants to review the relevant program page, meet stated prerequisites, and complete any interview or entrance-test steps where required.', NULL, NULL, NULL, TRUE, 'General graduate admissions summary derived from the official admissions/orientation and FAQ pages.', (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://www.esa.edu.lb/english/formation-diplomante/admission' LIMIT 1))
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

    SELECT id INTO v_adm_general_id
    FROM graduate_admission_requirement
    WHERE university_id = v_university_id
      AND record_key = 'esa-adm-general'
    ORDER BY id
    LIMIT 1;

    INSERT INTO graduate_admission_requirement (
        university_id, faculty_id, department_id, program_id, scope_level, record_key, requirement_type,
        requirement_text, comparison_operator, threshold_value, threshold_unit, is_required, notes, source_id
    ) VALUES
        (v_university_id, v_faculty_id, NULL, (SELECT id FROM graduate_program WHERE university_id = v_university_id AND program_key = 'esa-mba' LIMIT 1), 'PROGRAM', 'esa-adm-mba', 'GENERAL', 'MBA applicants must hold a university degree, have minimum professional experience, and pass either the GMAT or the ESA MBA entrance exam; a jury interview is also required.', NULL, NULL, NULL, TRUE, 'MBA admissions criteria published on the MBA page.', (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://www.esa.edu.lb/master-in-business-administration' LIMIT 1)),
        (v_university_id, v_faculty_id, NULL, (SELECT id FROM graduate_program WHERE university_id = v_university_id AND program_key = 'esa-emfm' LIMIT 1), 'PROGRAM', 'esa-adm-emfm', 'ACADEMIC', 'EMFM applicants must hold a Bachelor''s degree with professional experience, or a Master''s degree.', NULL, NULL, NULL, TRUE, 'EMFM admissions criteria published on the EMFM page.', (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://www.esa.edu.lb/english/executive-master-in-financial-management' LIMIT 1)),
        (v_university_id, v_faculty_id, NULL, (SELECT id FROM graduate_program WHERE university_id = v_university_id AND program_key = 'esa-mems' LIMIT 1), 'PROGRAM', 'esa-adm-mems', 'ACADEMIC', 'MEMS applicants must present Bac+4 / Master-level standing and minimum professional experience.', NULL, NULL, NULL, TRUE, 'MEMS admissions criteria published on the French MEMS page.', (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://www.esa.edu.lb/french/formation-diplomante/master-executif-en-management-de-la-sante' LIMIT 1))
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
        university_id, faculty_id, department_id, program_id, admission_requirement_id, scope_level, record_key, document_type, document_name,
        is_optional, sort_order, notes, source_id
    ) VALUES
        (v_university_id, NULL, NULL, NULL, v_adm_general_id, 'UNIVERSITY', 'esa-doc-application-file', 'APPLICATION', 'Admissions or application file', FALSE, 1, 'Shared graduate admissions file reference from the official admissions page.', (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://www.esa.edu.lb/english/formation-diplomante/admission' LIMIT 1)),
        (v_university_id, NULL, NULL, NULL, v_adm_general_id, 'UNIVERSITY', 'esa-doc-aid-documents', 'APPLICATION', 'Documents required for solidarity financial-aid applications', FALSE, 2, 'Official required-document set for solidarity financial aid.', (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://www.esa.edu.lb/Library/EditorFiles/ESA%20Solidarity%20Financial%20Aid%20Application%202025-2026%20%28procedure%20and%20list%20of%20required%20documents%29.pdf' LIMIT 1))
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

    INSERT INTO graduate_scholarship (
        university_id, faculty_id, department_id, program_id, scope_level, record_key, academic_year, name, description,
        coverage, amount, currency, notes, source_id
    ) VALUES
        (v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'esa-scholarship-main', NULL, 'Scholarships', 'ESA publishes scholarships as part of its financial-aid framework.', 'Scholarship category', NULL, NULL, 'Scholarship category published on the ESA financial-aid page.', (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://www.esa.edu.lb/financial-aid' LIMIT 1)),
        (v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'esa-scholarship-earlybird', NULL, 'Early-bird discounts', 'ESA publishes early-bird discounts as part of its financial-aid framework.', 'Discount category', NULL, NULL, 'Early-bird category published on the ESA financial-aid page.', (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://www.esa.edu.lb/financial-aid' LIMIT 1))
    ON CONFLICT (university_id, record_key) DO UPDATE SET
        faculty_id = EXCLUDED.faculty_id,
        department_id = EXCLUDED.department_id,
        program_id = EXCLUDED.program_id,
        scope_level = EXCLUDED.scope_level,
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
        university_id, faculty_id, department_id, program_id, scope_level, record_key, academic_year, name, description,
        amount, currency, notes, source_id
    ) VALUES
        (v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'esa-fin-aid-solidarity', NULL, 'Solidarity financial aid', 'ESA publishes solidarity financial aid and a corresponding application procedure.', NULL, NULL, 'Solidarity aid page and application PDF.', (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://www.esa.edu.lb/financial-aid' LIMIT 1))
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

    INSERT INTO graduate_accreditation (
        university_id, faculty_id, department_id, program_id, scope_level, record_key, name, authority, status,
        valid_from, valid_until, notes, source_id
    ) VALUES
        (v_university_id, v_faculty_id, NULL, NULL, 'FACULTY', 'esa-accreditation-mba-context', 'AMBA/BGA accreditation context', 'AMBA/BGA', 'Referenced on MBA page', NULL, NULL, 'ESA''s MBA page references AMBA/BGA accreditation context; no broader university-wide graduate accreditation statement was isolated.', (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://www.esa.edu.lb/master-in-business-administration' LIMIT 1))
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
