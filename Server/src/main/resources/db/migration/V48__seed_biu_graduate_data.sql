-- BIU graduate data seed migration.
-- Idempotent import for the canonical BIU graduate dataset.

DO $$
DECLARE
    v_university_id BIGINT;
BEGIN
    INSERT INTO university (name, name_ar, acronym, country, city, latitude, longitude, campus_name, campus_type)
    SELECT 'Beirut Islamic University', NULL, 'BIU', 'Lebanon', NULL, NULL, NULL, NULL, NULL
    WHERE NOT EXISTS (
        SELECT 1
        FROM university
        WHERE name = 'Beirut Islamic University'
    );

    SELECT id
    INTO v_university_id
    FROM university
    WHERE name = 'Beirut Islamic University'
    ORDER BY id
    LIMIT 1;

    INSERT INTO degree_type (code, name)
    VALUES
        ('MASTER', 'Master'),
        ('PHD', 'Doctor of Philosophy')
    ON CONFLICT (code) DO UPDATE SET
        name = EXCLUDED.name,
        updated_at = NOW();

    CREATE TEMP TABLE biu_source_seed (
        source_id TEXT PRIMARY KEY,
        title TEXT NOT NULL,
        url TEXT NOT NULL,
        source_type TEXT NOT NULL,
        accessed_at DATE,
        notes TEXT
    ) ON COMMIT DROP;

    INSERT INTO biu_source_seed (source_id, title, url, source_type, accessed_at, notes)
    VALUES
        ('BIU_OFFICIAL_HOME', 'BIU - جامعة بيروت الإسلامية- كلية الشريعة', 'https://www.biu.edu.lb/', 'WEB', '2026-07-06', 'Official BIU homepage navigation exposes graduate, admissions, tuition, thesis/dissertation, and postgraduate links.'),
        ('BIU_MASTERS_REQUIREMENTS', 'متطلبات التسجيل لمرحلة الماجستير', 'https://biu.edu.lb/pages/majors/masters/requirements', 'WEB', '2026-07-06', 'Official master''s requirements page naming the master''s specializations and thesis-stage structure.'),
        ('BIU_DOCTORATE_REQUIREMENTS', 'متطلبات التسجيل مرحلة العالمية الدكتوراه', 'https://biu.edu.lb/pages/majors/doctorat/requirements', 'WEB', '2026-07-06', 'Official doctorate requirements page naming the three doctorate specializations and dissertation-stage structure.'),
        ('BIU_ADMISSION_REQUIREMENTS', 'متطلبات التسجيل', 'https://biu.edu.lb/pages/admission/requirements', 'WEB', '2026-07-06', 'Official admissions requirements page listing the required documents for master''s, preparatory master''s, and doctorate stages.'),
        ('BIU_APPLICATIONS', 'طلبات التسجيل', 'https://biu.edu.lb/pages/admission/applications', 'WEB', '2026-07-06', 'Official application forms page for master''s, preparatory master''s, and doctorate registration.'),
        ('BIU_SYLLABUS', 'توصيف المواد', 'https://biu.edu.lb/pages/academics/syllabus', 'WEB', '2026-07-06', 'Official curriculum index listing graduate tracks including Islamic Studies, Comparative Fiqh, and Sharia Judiciary master''s items.'),
        ('BIU_PREP_MASTERS_BOOKS', 'أسماء الكتب - السنة التحضيرية للماجستير شعبة الدراسات الإسلامية', 'https://www.biu.edu.lb/pages/books/prepdirasat', 'WEB', '2026-07-06', 'Official book list for the preparatory master''s in Islamic Studies.'),
        ('BIU_POSTGRAD_GUIDANCE', 'إرشادات مهمة لطلاب الماجستير والدكتوراه', 'https://biu.edu.lb/pages/news/mdinfo', 'WEB', '2026-07-06', 'Official postgraduate research guidance page for master''s and doctorate students.'),
        ('BIU_ACCOMPLISHMENTS_RECOGNITION', 'انجازات الكلية', 'https://biu.edu.lb/pages/about/accomplishment', 'WEB', '2026-07-06', 'Official recognition/history page stating the faculty recognition for master''s specializations and doctorate study.'),
        ('BIU_FEES', 'الأقساط السنوية', 'https://biu.edu.lb/pages/admission/fees', 'WEB', '2026-07-06', 'Official annual fees page for academic year 1447 / 2025-2026.'),
        ('BIU_SCHEDULE', 'جدول المحاضرات', 'https://biu.edu.lb/pages/academics/schedule', 'WEB', '2026-07-06', 'Official lecture schedule page.'),
        ('BIU_EXAMS', 'جدول الامتحانات', 'https://biu.edu.lb/pages/academics/examList', 'WEB', '2026-07-06', 'Official exam schedule page.'),
        ('BIU_INTERNAL_REGULATIONS', 'النظام الداخلي', 'https://biu.edu.lb/pages/administration/internal', 'WEB', '2026-07-06', 'Official internal regulations page.');

    INSERT INTO source (university_id, title, url, source_type, accessed_at, notes)
    SELECT v_university_id, title, url, source_type, accessed_at, notes
    FROM biu_source_seed
    ON CONFLICT (university_id, url) DO UPDATE SET
        title = EXCLUDED.title,
        source_type = EXCLUDED.source_type,
        accessed_at = EXCLUDED.accessed_at,
        notes = EXCLUDED.notes,
        updated_at = NOW();

    CREATE TEMP TABLE biu_source_map AS
    SELECT ss.source_id, s.id AS db_source_id, s.url
    FROM biu_source_seed ss
    JOIN source s
      ON s.university_id = v_university_id
     AND s.url = ss.url;

    INSERT INTO university_faculty (university_id, name, short_name, faculty_type, official_url, notes)
    VALUES
        (v_university_id, 'Faculty of Sharia', NULL, 'FACULTY', NULL, 'Official BIU graduate faculty for the seeded master''s and doctorate programs.')
    ON CONFLICT (university_id, name) DO UPDATE SET
        short_name = EXCLUDED.short_name,
        faculty_type = EXCLUDED.faculty_type,
        official_url = EXCLUDED.official_url,
        notes = EXCLUDED.notes,
        updated_at = NOW();

    CREATE TEMP TABLE biu_program_seed (
        program_key TEXT PRIMARY KEY,
        title TEXT NOT NULL,
        degree_type TEXT NOT NULL,
        official_degree_name TEXT NOT NULL,
        program_description TEXT NOT NULL,
        thesis_or_non_thesis TEXT,
        duration_value NUMERIC(10, 2),
        duration_unit TEXT,
        official_program_url TEXT,
        primary_source_id TEXT NOT NULL,
        notes TEXT NOT NULL
    ) ON COMMIT DROP;

    INSERT INTO biu_program_seed (
        program_key,
        title,
        degree_type,
        official_degree_name,
        program_description,
        thesis_or_non_thesis,
        duration_value,
        duration_unit,
        official_program_url,
        primary_source_id,
        notes
    )
    VALUES
        (
            'biu-master-usul-al-fiqh',
            'Master of Usul al-Fiqh',
            'MASTER',
            'Master of Usul al-Fiqh',
            'Official BIU master''s specialization in Usul al-Fiqh listed on the master''s requirements page.',
            'THESIS',
            NULL,
            NULL,
            NULL,
            'BIU_MASTERS_REQUIREMENTS',
            'Official pages confirm the program, admissions flow, application forms, and syllabus index. The official requirements page states one academic year of coursework followed by a thesis period of 1-2 years.'
        ),
        (
            'biu-master-comparative-fiqh',
            'Master of Comparative Fiqh',
            'MASTER',
            'Master of Comparative Fiqh',
            'Official BIU master''s specialization in Comparative Fiqh listed on the master''s requirements page.',
            'THESIS',
            NULL,
            NULL,
            NULL,
            'BIU_MASTERS_REQUIREMENTS',
            'Official pages confirm the program, admissions flow, application forms, and syllabus index. The official requirements page states one academic year of coursework followed by a thesis period of 1-2 years.'
        ),
        (
            'biu-master-islamic-studies',
            'Master of Islamic Studies',
            'MASTER',
            'Master of Islamic Studies',
            'Official BIU master''s specialization in Islamic Studies listed on the master''s requirements page.',
            'THESIS',
            NULL,
            NULL,
            NULL,
            'BIU_MASTERS_REQUIREMENTS',
            'Official pages confirm the program, admissions flow, application forms, and syllabus index. The official requirements page states one academic year of coursework followed by a thesis period of 1-2 years.'
        ),
        (
            'biu-phd-usul-al-fiqh',
            'Doctorate in Usul al-Fiqh',
            'PHD',
            'Doctorate in Usul al-Fiqh',
            'Official BIU doctorate specialization in Usul al-Fiqh listed on the doctorate requirements page.',
            'THESIS',
            3,
            'YEARS',
            NULL,
            'BIU_DOCTORATE_REQUIREMENTS',
            'Official pages confirm the doctorate program, admissions flow, and application forms. The official doctorate page states dissertation preparation over three years.'
        ),
        (
            'biu-phd-comparative-fiqh',
            'Doctorate in Comparative Fiqh',
            'PHD',
            'Doctorate in Comparative Fiqh',
            'Official BIU doctorate specialization in Comparative Fiqh listed on the doctorate requirements page.',
            'THESIS',
            3,
            'YEARS',
            NULL,
            'BIU_DOCTORATE_REQUIREMENTS',
            'Official pages confirm the doctorate program, admissions flow, and application forms. The official doctorate page states dissertation preparation over three years.'
        ),
        (
            'biu-phd-islamic-studies',
            'Doctorate in Islamic Studies',
            'PHD',
            'Doctorate in Islamic Studies',
            'Official BIU doctorate specialization in Islamic Studies listed on the doctorate requirements page.',
            'THESIS',
            3,
            'YEARS',
            NULL,
            'BIU_DOCTORATE_REQUIREMENTS',
            'Official pages confirm the doctorate program, admissions flow, and application forms. The official doctorate page states dissertation preparation over three years.'
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
        NULL,
        dt.id,
        seed.program_key,
        NULL,
        NULL,
        seed.official_degree_name,
        seed.thesis_or_non_thesis,
        NULL,
        seed.duration_value,
        seed.duration_unit,
        NULL,
        NULL,
        seed.program_description,
        seed.official_program_url,
        sm.db_source_id,
        seed.notes
    FROM biu_program_seed seed
    JOIN university_faculty f
      ON f.university_id = v_university_id
     AND f.name = 'Faculty of Sharia'
    JOIN degree_type dt
      ON dt.code = seed.degree_type
    JOIN biu_source_map sm
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

    CREATE TEMP TABLE biu_program_source_seed (
        program_key TEXT NOT NULL,
        source_id TEXT NOT NULL,
        source_role TEXT NOT NULL,
        source_order INTEGER NOT NULL,
        evidence_text TEXT NOT NULL,
        notes TEXT
    ) ON COMMIT DROP;

    INSERT INTO biu_program_source_seed (
        program_key,
        source_id,
        source_role,
        source_order,
        evidence_text,
        notes
    )
    VALUES
        ('biu-master-usul-al-fiqh', 'BIU_MASTERS_REQUIREMENTS', 'PRIMARY', 1, 'Official master''s requirements page listing Usul al-Fiqh.', 'Primary official source.'),
        ('biu-master-usul-al-fiqh', 'BIU_ADMISSION_REQUIREMENTS', 'ADMISSIONS', 2, 'Official admissions requirements page confirming master''s-stage documents.', 'Admissions source.'),
        ('biu-master-usul-al-fiqh', 'BIU_APPLICATIONS', 'SECONDARY', 3, 'Official applications page confirming the master''s registration forms.', 'Application source.'),
        ('biu-master-usul-al-fiqh', 'BIU_SYLLABUS', 'CATALOG', 4, 'Official syllabus index listing the graduate postgraduate track.', 'Catalog source.'),
        ('biu-master-comparative-fiqh', 'BIU_MASTERS_REQUIREMENTS', 'PRIMARY', 1, 'Official master''s requirements page listing Comparative Fiqh.', 'Primary official source.'),
        ('biu-master-comparative-fiqh', 'BIU_ADMISSION_REQUIREMENTS', 'ADMISSIONS', 2, 'Official admissions requirements page confirming master''s-stage documents.', 'Admissions source.'),
        ('biu-master-comparative-fiqh', 'BIU_APPLICATIONS', 'SECONDARY', 3, 'Official applications page confirming the master''s registration forms.', 'Application source.'),
        ('biu-master-comparative-fiqh', 'BIU_SYLLABUS', 'CATALOG', 4, 'Official syllabus index listing the graduate postgraduate track.', 'Catalog source.'),
        ('biu-master-islamic-studies', 'BIU_MASTERS_REQUIREMENTS', 'PRIMARY', 1, 'Official master''s requirements page listing Islamic Studies.', 'Primary official source.'),
        ('biu-master-islamic-studies', 'BIU_ADMISSION_REQUIREMENTS', 'ADMISSIONS', 2, 'Official admissions requirements page confirming master''s-stage documents.', 'Admissions source.'),
        ('biu-master-islamic-studies', 'BIU_APPLICATIONS', 'SECONDARY', 3, 'Official applications page confirming the master''s registration forms.', 'Application source.'),
        ('biu-master-islamic-studies', 'BIU_SYLLABUS', 'CATALOG', 4, 'Official syllabus index listing the graduate postgraduate track.', 'Catalog source.'),
        ('biu-phd-usul-al-fiqh', 'BIU_DOCTORATE_REQUIREMENTS', 'PRIMARY', 1, 'Official doctorate requirements page listing Usul al-Fiqh.', 'Primary official source.'),
        ('biu-phd-usul-al-fiqh', 'BIU_ADMISSION_REQUIREMENTS', 'ADMISSIONS', 2, 'Official admissions requirements page confirming doctorate-stage documents.', 'Admissions source.'),
        ('biu-phd-usul-al-fiqh', 'BIU_APPLICATIONS', 'SECONDARY', 3, 'Official applications page confirming the doctorate registration forms.', 'Application source.'),
        ('biu-phd-usul-al-fiqh', 'BIU_ACCOMPLISHMENTS_RECOGNITION', 'OTHER', 4, 'Official recognition page confirming doctorate specializations.', 'Recognition source.'),
        ('biu-phd-comparative-fiqh', 'BIU_DOCTORATE_REQUIREMENTS', 'PRIMARY', 1, 'Official doctorate requirements page listing Comparative Fiqh.', 'Primary official source.'),
        ('biu-phd-comparative-fiqh', 'BIU_ADMISSION_REQUIREMENTS', 'ADMISSIONS', 2, 'Official admissions requirements page confirming doctorate-stage documents.', 'Admissions source.'),
        ('biu-phd-comparative-fiqh', 'BIU_APPLICATIONS', 'SECONDARY', 3, 'Official applications page confirming the doctorate registration forms.', 'Application source.'),
        ('biu-phd-comparative-fiqh', 'BIU_ACCOMPLISHMENTS_RECOGNITION', 'OTHER', 4, 'Official recognition page confirming doctorate specializations.', 'Recognition source.'),
        ('biu-phd-islamic-studies', 'BIU_DOCTORATE_REQUIREMENTS', 'PRIMARY', 1, 'Official doctorate requirements page listing Islamic Studies.', 'Primary official source.'),
        ('biu-phd-islamic-studies', 'BIU_ADMISSION_REQUIREMENTS', 'ADMISSIONS', 2, 'Official admissions requirements page confirming doctorate-stage documents.', 'Admissions source.'),
        ('biu-phd-islamic-studies', 'BIU_APPLICATIONS', 'SECONDARY', 3, 'Official applications page confirming the doctorate registration forms.', 'Application source.'),
        ('biu-phd-islamic-studies', 'BIU_ACCOMPLISHMENTS_RECOGNITION', 'OTHER', 4, 'Official recognition page confirming doctorate specializations.', 'Recognition source.');

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
        seed.source_role,
        seed.source_order,
        seed.evidence_text,
        seed.notes
    FROM biu_program_source_seed seed
    JOIN graduate_program gp
      ON gp.university_id = v_university_id
     AND gp.program_key = seed.program_key
    JOIN biu_source_map sm
      ON sm.source_id = seed.source_id
    ON CONFLICT (program_id, source_id, source_role) DO UPDATE SET
        source_order = EXCLUDED.source_order,
        evidence_text = EXCLUDED.evidence_text,
        notes = EXCLUDED.notes,
        updated_at = NOW();
END
$$;
