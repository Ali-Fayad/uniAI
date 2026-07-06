-- TUI graduate data seed migration.
-- Idempotent import for the canonical TUI graduate dataset.

DO $$
DECLARE
    v_university_id BIGINT;
BEGIN

    INSERT INTO university (name, name_ar, acronym, country, city, latitude, longitude, campus_name, campus_type)
    SELECT 'Tripoli University Institute / University of Tripoli', NULL, 'TUI', 'Lebanon', NULL, NULL, NULL, NULL, NULL
    WHERE NOT EXISTS (
        SELECT 1
        FROM university
        WHERE name = 'Tripoli University Institute / University of Tripoli'
    );

    SELECT id
    INTO v_university_id
    FROM university
    WHERE name = 'Tripoli University Institute / University of Tripoli'
    ORDER BY id
    LIMIT 1;

    INSERT INTO degree_type (code, name)
    VALUES
        ('MASTER', 'Master'),
        ('PHD', 'Doctor of Philosophy')
    ON CONFLICT (code) DO UPDATE SET
        name = EXCLUDED.name,
        updated_at = NOW();

    CREATE TEMP TABLE tui_source_seed (
        source_id TEXT PRIMARY KEY,
        title TEXT NOT NULL,
        url TEXT NOT NULL,
        source_type TEXT NOT NULL,
        accessed_at DATE NOT NULL
    ) ON COMMIT DROP;

    INSERT INTO tui_source_seed (source_id, title, url, source_type, accessed_at)
    VALUES
        ('TUI_SRC_001', 'University of Tripoli official home page', 'https://new.ut.edu.lb/', 'official_homepage', '2026-07-06'),
        ('TUI_SRC_002', 'القبول والتسجيل', 'https://new.ut.edu.lb/pre-registration/', 'official_admissions_index', '2026-07-06'),
        ('TUI_SRC_003', 'مرحلة الماجستير', 'https://new.ut.edu.lb/pre-registration/master/', 'official_graduate_admissions_page', '2026-07-06'),
        ('TUI_SRC_004', 'مرحلة الدكتوراه', 'https://new.ut.edu.lb/pre-registration/phd/', 'official_graduate_admissions_page', '2026-07-06'),
        ('TUI_SRC_005', 'كلية الشريعة والدراسات الإسلامية', 'https://new.ut.edu.lb/fac/shriaa/', 'official_faculty_page', '2026-07-06'),
        ('TUI_SRC_006', 'الطلاب الأجانب / Foreign students', 'https://new.ut.edu.lb/admission/foreign-students/', 'official_international_students_page', '2026-07-06'),
        ('TUI_SRC_007', 'الرسائل الجامعية', 'https://new.ut.edu.lb/%D8%A7%D9%84%D8%B1%D8%B3%D8%A7%D8%A6%D9%84-%D8%A7%D9%84%D8%AC%D8%A7%D9%85%D8%B9%D9%8A%D8%A9/', 'official_theses_index', '2026-07-06'),
        ('TUI_SRC_008', 'الروزنامة الجامعية 2025-2026', 'https://new.ut.edu.lb/%d8%a7%d9%84%d8%b1%d9%88%d8%b2%d9%86%d8%a7%d9%85%d8%a9-%d8%a7%d9%84%d8%ac%d8%a7%d9%85%d8%b9%d9%8a%d8%a9-2025-2026/', 'official_academic_calendar', '2026-07-06');

    INSERT INTO source (university_id, title, url, source_type, accessed_at)
    SELECT
        v_university_id,
        title,
        url,
        source_type,
        accessed_at
    FROM tui_source_seed
    ON CONFLICT (university_id, url) DO UPDATE SET
        title = EXCLUDED.title,
        source_type = EXCLUDED.source_type,
        accessed_at = EXCLUDED.accessed_at,
        updated_at = NOW();

    CREATE TEMP TABLE tui_faculty_seed (
        name TEXT PRIMARY KEY,
        short_name TEXT,
        faculty_type TEXT NOT NULL,
        official_url TEXT,
        notes TEXT
    ) ON COMMIT DROP;

    INSERT INTO tui_faculty_seed (name, short_name, faculty_type, official_url, notes)
    VALUES
        ('Faculty of Sharia and Islamic Studies', NULL, 'FACULTY', 'https://new.ut.edu.lb/fac/shriaa/', 'Imported from the official TUI graduate dataset.');

    INSERT INTO university_faculty (university_id, name, short_name, faculty_type, official_url, notes)
    SELECT
        v_university_id,
        name,
        short_name,
        faculty_type,
        official_url,
        notes
    FROM tui_faculty_seed
    ON CONFLICT (university_id, name) DO UPDATE SET
        short_name = EXCLUDED.short_name,
        faculty_type = EXCLUDED.faculty_type,
        official_url = EXCLUDED.official_url,
        notes = EXCLUDED.notes,
        updated_at = NOW();

    CREATE TEMP TABLE tui_department_seed (
        faculty_name TEXT NOT NULL,
        name TEXT NOT NULL,
        short_name TEXT,
        official_url TEXT,
        notes TEXT
    ) ON COMMIT DROP;

    INSERT INTO tui_department_seed (faculty_name, name, short_name, official_url, notes)
    VALUES
        ('Faculty of Sharia and Islamic Studies', 'Department of Sharia', NULL, NULL, 'Imported from the official TUI graduate dataset.'),
        ('Faculty of Sharia and Islamic Studies', 'Department of Islamic Studies', NULL, NULL, 'Imported from the official TUI graduate dataset.');

    INSERT INTO university_department (university_id, faculty_id, name, short_name, official_url, notes)
    SELECT
        v_university_id,
        fac.id,
        d.name,
        d.short_name,
        d.official_url,
        d.notes
    FROM tui_department_seed d
    JOIN university_faculty fac
      ON fac.university_id = v_university_id
     AND fac.name = d.faculty_name
    ON CONFLICT (university_id, faculty_id, name) DO UPDATE SET
        short_name = EXCLUDED.short_name,
        official_url = EXCLUDED.official_url,
        notes = EXCLUDED.notes,
        updated_at = NOW();

    CREATE TEMP TABLE tui_program_seed (
        program_key TEXT PRIMARY KEY,
        faculty_name TEXT NOT NULL,
        department_name TEXT NOT NULL,
        major_category TEXT,
        major TEXT,
        degree_type TEXT NOT NULL,
        official_degree_name TEXT NOT NULL,
        thesis_or_non_thesis TEXT,
        credits INTEGER,
        duration_value NUMERIC(10, 2),
        duration_unit TEXT,
        program_description TEXT,
        official_program_url TEXT,
        source_ids JSONB NOT NULL,
        primary_source_id TEXT NOT NULL,
        notes TEXT,
        tracks JSONB NOT NULL
    ) ON COMMIT DROP;

    INSERT INTO tui_program_seed (
        program_key,
        faculty_name,
        department_name,
        major_category,
        major,
        degree_type,
        official_degree_name,
        thesis_or_non_thesis,
        credits,
        duration_value,
        duration_unit,
        program_description,
        official_program_url,
        source_ids,
        primary_source_id,
        notes,
        tracks
    )
    VALUES
        (
            'tui-master-islamic-sharia',
            'Faculty of Sharia and Islamic Studies',
            'Department of Sharia',
            'Islamic Sharia',
            'Islamic Sharia',
            'MASTER',
            'Master of Islamic Sharia',
            'THESIS_OR_NON_THESIS',
            NULL,
            NULL,
            NULL,
            'Official master''s degree in Islamic Sharia granted by the Faculty of Sharia and Islamic Studies. The faculty page explicitly lists the master''s degree and its specializations.',
            NULL,
            '["TUI_SRC_002","TUI_SRC_003","TUI_SRC_005","TUI_SRC_006","TUI_SRC_007"]'::jsonb,
            'TUI_SRC_005',
            'Imported from the official TUI graduate inventory. official_program_url is null because no dedicated program page was recovered.',
            '["التفسير وعلوم القرآن الكريم","الحديث النبوي الشريف وعلومه","العقائد والأديان","الفقه وأصوله","الفقه وأصوله / أصول الفقه","الفقه وأصوله / الفقه المقارن","الفقه وأصوله / فقه المعاملات المالية والمصرفية الإسلامية","الفقه وأصوله / فقه الأحوال الشخصية","الفقه وأصوله / الشريعة والقانون","الفقه وأصوله / الدراسات الوقفية","الفقه وأصوله / الفقه الجنائي المقارن","السياسة الشرعية","فقه القضية الفلسطينية"]'::jsonb
        ),
        (
            'tui-master-islamic-studies',
            'Faculty of Sharia and Islamic Studies',
            'Department of Islamic Studies',
            'Islamic Studies',
            'Islamic Studies',
            'MASTER',
            'Master of Islamic Studies',
            'THESIS_OR_NON_THESIS',
            NULL,
            NULL,
            NULL,
            'Official master''s degree in Islamic Studies granted by the Faculty of Sharia and Islamic Studies. The faculty page explicitly lists the master''s degree and its specializations.',
            NULL,
            '["TUI_SRC_002","TUI_SRC_003","TUI_SRC_005","TUI_SRC_006","TUI_SRC_007"]'::jsonb,
            'TUI_SRC_005',
            'Imported from the official TUI graduate inventory. official_program_url is null because no dedicated program page was recovered.',
            '["دراسات في العقائد والأديان","الاقتصاد الإسلامي","الدراسات الأسرية","دراسات في الشريعة والقانون","العلوم التربوية","العلوم الإدارية والمالية الإسلامية","الفن الإسلامي","الدعوة والإعلام","علوم الخط والمخطوط","الدراسات الفلسطينية","التاريخ الإسلامي","حقوق الإنسان","السياسة الشرعية","الدراسات الوقفية","المعالجة الآلية للنصوص الشرعية واللغوية"]'::jsonb
        ),
        (
            'tui-phd-islamic-sharia',
            'Faculty of Sharia and Islamic Studies',
            'Department of Sharia',
            'Islamic Sharia',
            'Islamic Sharia',
            'PHD',
            'Doctor of Philosophy in Islamic Sharia',
            'THESIS',
            42,
            NULL,
            NULL,
            'Official doctoral degree in Islamic Sharia granted by the Faculty of Sharia and Islamic Studies. The faculty page explicitly lists the doctoral degree and its specializations.',
            NULL,
            '["TUI_SRC_002","TUI_SRC_004","TUI_SRC_005","TUI_SRC_006","TUI_SRC_007"]'::jsonb,
            'TUI_SRC_005',
            'Imported from the official TUI graduate inventory. official_program_url is null because no dedicated program page was recovered.',
            '["التفسير وعلوم القرآن الكريم","الحديث النبوي الشريف وعلومه","العقائد والأديان","الفقه وأصوله","الفقه وأصوله / أصول الفقه","الفقه وأصوله / الفقه المقارن","الفقه وأصوله / فقه المعاملات المالية والمصرفية الإسلامية","الفقه وأصوله / فقه الأحوال الشخصية","الفقه وأصوله / الشريعة والقانون","الفقه وأصوله / السياسة الشرعية","الفقه وأصوله / الدراسات الوقفية","الفقه وأصوله / الفقه الجنائي المقارن","السياسة الشرعية","السياسة الشرعية / فقه القضية الفلسطينية"]'::jsonb
        ),
        (
            'tui-phd-islamic-studies',
            'Faculty of Sharia and Islamic Studies',
            'Department of Islamic Studies',
            'Islamic Studies',
            'Islamic Studies',
            'PHD',
            'Doctor of Philosophy in Islamic Studies',
            'THESIS',
            42,
            NULL,
            NULL,
            'Official doctoral degree in Islamic Studies granted by the Faculty of Sharia and Islamic Studies. The faculty page explicitly lists the doctoral degree and its specializations.',
            NULL,
            '["TUI_SRC_002","TUI_SRC_004","TUI_SRC_005","TUI_SRC_006","TUI_SRC_007"]'::jsonb,
            'TUI_SRC_005',
            'Imported from the official TUI graduate inventory. official_program_url is null because no dedicated program page was recovered.',
            '["دراسات في العقائد والأديان","الاقتصاد الإسلامي","الدراسات الأسرية","دراسات في الشريعة والقانون","العلوم التربوية","العلوم الإدارية والمالية الإسلامية","الفن الإسلامي","الدعوة والإعلام","علوم الخط والمخطوط","الدراسات الفلسطينية","التاريخ الإسلامي","حقوق الإنسان","السياسة الشرعية"]'::jsonb
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
        s.id,
        seed.notes
    FROM tui_program_seed seed
    JOIN university_faculty fac
      ON fac.university_id = v_university_id
     AND fac.name = seed.faculty_name
    JOIN university_department dep
      ON dep.university_id = v_university_id
     AND dep.faculty_id = fac.id
     AND dep.name = seed.department_name
    JOIN degree_type dt
      ON dt.code = seed.degree_type
    JOIN tui_source_seed hs
      ON hs.source_id = seed.primary_source_id
    JOIN source s
      ON s.university_id = v_university_id
     AND s.url = hs.url
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
        CASE
            WHEN src.source_seed_id = seed.primary_source_id THEN 'PRIMARY'
            WHEN src.source_seed_id = 'TUI_SRC_005' THEN 'FACULTY'
            WHEN src.source_seed_id IN ('TUI_SRC_002', 'TUI_SRC_003', 'TUI_SRC_004') THEN 'ADMISSIONS'
            ELSE 'OTHER'
        END,
        src.ord,
        ss.title,
        'Imported from the official TUI graduate dataset.'
    FROM tui_program_seed seed
    JOIN graduate_program gp
      ON gp.university_id = v_university_id
     AND gp.program_key = seed.program_key
    JOIN LATERAL jsonb_array_elements_text(seed.source_ids) WITH ORDINALITY AS src(source_seed_id, ord)
      ON TRUE
    JOIN tui_source_seed ss
      ON ss.source_id = src.source_seed_id
    JOIN source s
      ON s.university_id = v_university_id
     AND s.url = ss.url
    ON CONFLICT (program_id, source_id, source_role) DO UPDATE SET
        source_order = EXCLUDED.source_order,
        evidence_text = EXCLUDED.evidence_text,
        notes = EXCLUDED.notes,
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
    SELECT
        v_university_id,
        gp.faculty_id,
        gp.department_id,
        gp.id,
        'PROGRAM',
        seed.program_key || ':doctoral-credit-requirement',
        'ACADEMIC',
        'Doctoral study requires at least 42 credit hours, including 30 credit hours for the doctoral dissertation and successful defense.',
        '>=',
        42,
        'CREDITS',
        TRUE,
        'Imported from the official PhD admissions page.',
        s.id
    FROM tui_program_seed seed
    JOIN graduate_program gp
      ON gp.university_id = v_university_id
     AND gp.program_key = seed.program_key
    JOIN tui_source_seed ss
      ON ss.source_id = 'TUI_SRC_004'
    JOIN source s
      ON s.university_id = v_university_id
     AND s.url = ss.url
    WHERE seed.degree_type = 'PHD'
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

END $$;
