-- AUCE graduate data seed migration.
-- Idempotent import for the canonical AUCE graduate dataset.

DO $$
DECLARE
    v_university_id BIGINT;
BEGIN

    INSERT INTO university (name, name_ar, acronym, country, city, latitude, longitude, campus_name, campus_type)
    SELECT 'American University of Culture & Education', NULL, 'AUCE', 'Lebanon', NULL, NULL, NULL, NULL, NULL
    WHERE NOT EXISTS (
        SELECT 1 FROM university WHERE name = 'American University of Culture & Education'
    );

    SELECT id INTO v_university_id
    FROM university
    WHERE name = 'American University of Culture & Education'
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

    CREATE TEMP TABLE auce_source_seed (
        source_id TEXT PRIMARY KEY,
        title TEXT NOT NULL,
        url TEXT NOT NULL,
        source_type TEXT NOT NULL,
        accessed_at DATE NOT NULL,
        notes TEXT NOT NULL
    ) ON COMMIT DROP;

    INSERT INTO auce_source_seed (source_id, title, url, source_type, accessed_at, notes)
    VALUES
        ('auce-home', 'AUCE - American University of Culture and Education', 'https://www.auce.edu.lb/', 'official_webpage', '2026-07-06', 'Homepage/navigation lists Graduate links for MBA Program and Master of Computer Science; used as navigation signal only.'),
        ('auce-academics-programs', 'Academic Programs', 'https://auce.edu.lb/fees-and-tuitions/index.php?page=academics', 'official_webpage', '2026-07-06', 'Official academics page lists Master of Business Administration and Master of Computer Science as graduate programs, both at 39 credits.'),
        ('auce-admissions', 'Admissions', 'https://auce.edu.lb/index.php?page=admissions', 'official_webpage', '2026-07-06', 'Official admissions page states Masters Programs admission requirements for MBA and Master of Computer Science applicants and lists scholarship/financial-aid categories.'),
        ('auce-contact-faq', 'Contact / FAQ', 'https://auce.edu.lb/index.php?page=contact', 'official_webpage', '2026-07-06', 'Contact FAQ confirms graduate English requirements: TOEFL 97+ or IELTS 7.0+.'),
        ('auce-arts-sciences', 'Faculty of Arts & Sciences', 'https://auce.edu.lb/departements/masters-of-science/index.php?page=faculty-arts-sciences', 'official_webpage', '2026-07-06', 'Faculty page describes undergraduate Arts & Sciences majors and links to Master of Computer Science; used as supporting navigation only.'),
        ('auce-business-faculty', 'Faculty of Business Administration', 'https://auce.edu.lb/4011/index.php?page=faculty-business', 'official_webpage', '2026-07-06', 'Faculty page describes undergraduate business majors; not standalone graduate-program evidence.'),
        ('auce-campuses', 'Campuses', 'https://auce.edu.lb/departements/masters-of-science/index.php?page=campuses', 'official_webpage', '2026-07-06', 'Campus page states Beirut Campus has a full range of undergraduate and graduate programs; campus context only.'),
        ('auce-about-accreditation', 'About AUCE', 'https://www.auce.edu.lb/index.php?page=about', 'official_webpage', '2026-07-06', 'About page states AUCE is licensed/recognized in Lebanon and includes MBA Program and Master of Computer Science footer navigation.');

    INSERT INTO source (university_id, title, url, source_type, accessed_at, notes)
    SELECT
        v_university_id,
        title,
        url,
        source_type,
        accessed_at,
        notes
    FROM auce_source_seed
    ON CONFLICT (university_id, url) DO UPDATE SET
        title = EXCLUDED.title,
        source_type = EXCLUDED.source_type,
        accessed_at = EXCLUDED.accessed_at,
        notes = EXCLUDED.notes,
        updated_at = NOW();

    CREATE TEMP TABLE auce_program_seed (
        program_key TEXT PRIMARY KEY,
        official_degree_name TEXT NOT NULL,
        major_category TEXT NOT NULL,
        major TEXT NOT NULL,
        thesis_or_non_thesis TEXT,
        credits INTEGER,
        duration_value NUMERIC(10, 2),
        duration_unit TEXT,
        program_description TEXT,
        official_program_url TEXT,
        source_id TEXT NOT NULL,
        notes TEXT NOT NULL
    ) ON COMMIT DROP;

    INSERT INTO auce_program_seed (
        program_key,
        official_degree_name,
        major_category,
        major,
        thesis_or_non_thesis,
        credits,
        duration_value,
        duration_unit,
        program_description,
        official_program_url,
        source_id,
        notes
    )
    VALUES
        (
            'auce-master-business-administration',
            'Master of Business Administration',
            'Business Administration',
            'Business Administration',
            NULL,
            39,
            2,
            'YEARS',
            'Official AUCE Master of Business Administration listed on the Academic Programs page as a 39-credit full-time master''s program with the published MBA concentration set.',
            NULL,
            'auce-academics-programs',
            'Official AUCE Academic Programs and Admissions pages identify the MBA as a current master''s program. No dedicated program page was recovered in the source set, so official_program_url is null.'
        ),
        (
            'auce-master-computer-science',
            'Master of Computer Science',
            'Computer Science',
            'Computer Science',
            NULL,
            39,
            NULL,
            NULL,
            'Official AUCE Master of Computer Science listed on the Academic Programs page as a 39-credit full-time master''s program with the published focus areas.',
            NULL,
            'auce-academics-programs',
            'Official AUCE Academic Programs, Admissions, and Contact/FAQ pages identify the MCS as a current master''s program. No dedicated program page was recovered in the source set, so official_program_url is null.'
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
        NULL,
        NULL,
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
        NULL,
        p.program_description,
        p.official_program_url,
        s.id,
        p.notes
    FROM auce_program_seed p
    JOIN degree_type dt
      ON dt.code = 'MASTER'
    JOIN auce_source_seed ss
      ON ss.source_id = p.source_id
    JOIN source s
      ON s.university_id = v_university_id
     AND s.url = ss.url
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

    CREATE TEMP TABLE auce_track_seed (
        program_key TEXT NOT NULL,
        track_name TEXT NOT NULL,
        track_order INTEGER NOT NULL,
        is_primary BOOLEAN NOT NULL DEFAULT FALSE,
        source_id TEXT NOT NULL,
        notes TEXT NOT NULL
    ) ON COMMIT DROP;

    INSERT INTO auce_track_seed (program_key, track_name, track_order, is_primary, source_id, notes)
    VALUES
        ('auce-master-business-administration', 'Accounting', 1, TRUE, 'auce-academics-programs', 'Official MBA concentration listed on the Academic Programs page.'),
        ('auce-master-business-administration', 'Business Management', 2, FALSE, 'auce-academics-programs', 'Official MBA concentration listed on the Academic Programs page.'),
        ('auce-master-business-administration', 'Marketing & Advertising', 3, FALSE, 'auce-academics-programs', 'Official MBA concentration listed on the Academic Programs page.'),
        ('auce-master-business-administration', 'Human Resources', 4, FALSE, 'auce-academics-programs', 'Official MBA concentration listed on the Academic Programs page.'),
        ('auce-master-business-administration', 'International Hospitality', 5, FALSE, 'auce-academics-programs', 'Official MBA concentration listed on the Academic Programs page.'),
        ('auce-master-business-administration', 'International Tourism', 6, FALSE, 'auce-academics-programs', 'Official MBA concentration listed on the Academic Programs page.'),
        ('auce-master-business-administration', 'Economics', 7, FALSE, 'auce-academics-programs', 'Official MBA concentration listed on the Academic Programs page.'),
        ('auce-master-business-administration', 'Management Information Systems', 8, FALSE, 'auce-academics-programs', 'Official MBA concentration listed on the Academic Programs page.'),
        ('auce-master-business-administration', 'Banking & Finance', 9, FALSE, 'auce-academics-programs', 'Official MBA concentration listed on the Academic Programs page.'),
        ('auce-master-computer-science', 'Artificial Intelligence', 1, TRUE, 'auce-academics-programs', 'Official MCS focus area listed on the Academic Programs page.'),
        ('auce-master-computer-science', 'Cybersecurity', 2, FALSE, 'auce-academics-programs', 'Official MCS focus area listed on the Academic Programs page.'),
        ('auce-master-computer-science', 'Data Science', 3, FALSE, 'auce-academics-programs', 'Official MCS focus area listed on the Academic Programs page.'),
        ('auce-master-computer-science', 'Cloud Computing', 4, FALSE, 'auce-academics-programs', 'Official MCS focus area listed on the Academic Programs page.'),
        ('auce-master-computer-science', 'Software Engineering', 5, FALSE, 'auce-academics-programs', 'Official MCS focus area listed on the Academic Programs page.'),
        ('auce-master-computer-science', 'Network Architecture', 6, FALSE, 'auce-academics-programs', 'Official MCS focus area listed on the Academic Programs page.');

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
        'CONCENTRATION',
        t.track_name,
        t.track_order,
        t.is_primary,
        NULL,
        s.id,
        t.notes
    FROM auce_track_seed t
    JOIN graduate_program gp
      ON gp.university_id = v_university_id
     AND gp.program_key = t.program_key
    JOIN auce_source_seed ss
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

    CREATE TEMP TABLE auce_program_source_seed (
        program_key TEXT NOT NULL,
        source_id TEXT NOT NULL,
        source_role TEXT NOT NULL,
        source_order INTEGER NOT NULL,
        evidence_text TEXT NOT NULL,
        notes TEXT NOT NULL
    ) ON COMMIT DROP;

    INSERT INTO auce_program_source_seed (program_key, source_id, source_role, source_order, evidence_text, notes)
    VALUES
        ('auce-master-business-administration', 'auce-academics-programs', 'PRIMARY', 1, 'Academic Programs page lists the MBA as a current graduate program with 39 credits and the MBA concentration set.', 'Primary official source.'),
        ('auce-master-business-administration', 'auce-admissions', 'ADMISSIONS', 2, 'Admissions page confirms masters-program admission requirements for MBA applicants.', 'Admissions source.'),
        ('auce-master-computer-science', 'auce-academics-programs', 'PRIMARY', 1, 'Academic Programs page lists the Master of Computer Science as a current graduate program with 39 credits and focus areas.', 'Primary official source.'),
        ('auce-master-computer-science', 'auce-admissions', 'ADMISSIONS', 2, 'Admissions page confirms masters-program admission requirements for Master of Computer Science applicants.', 'Admissions source.'),
        ('auce-master-computer-science', 'auce-contact-faq', 'OTHER', 3, 'Contact / FAQ page confirms the graduate English requirement of TOEFL 97+ or IELTS 7.0+.', 'Language requirement source.');

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
    FROM auce_program_source_seed ps
    JOIN graduate_program gp
      ON gp.university_id = v_university_id
     AND gp.program_key = ps.program_key
    JOIN auce_source_seed ss
      ON ss.source_id = ps.source_id
    JOIN source s
      ON s.university_id = v_university_id
     AND s.url = ss.url
    ON CONFLICT (program_id, source_id, source_role) DO UPDATE SET
        source_order = EXCLUDED.source_order,
        evidence_text = EXCLUDED.evidence_text,
        notes = EXCLUDED.notes,
        updated_at = NOW();

    CREATE TEMP TABLE auce_requirement_seed (
        record_key TEXT PRIMARY KEY,
        requirement_type TEXT NOT NULL,
        requirement_text TEXT NOT NULL,
        notes TEXT NOT NULL,
        source_id TEXT NOT NULL
    ) ON COMMIT DROP;

    INSERT INTO auce_requirement_seed (record_key, requirement_type, requirement_text, notes, source_id)
    VALUES
        ('auce-req-general-graduate-admission', 'GENERAL', 'Official university transcript(s); completed graduate application; 2 recommendation letters; $50 USD application fee.', 'Shared graduate admissions summary published on the Academic Programs and Admissions pages.', 'auce-admissions'),
        ('auce-req-english-proficiency', 'ENGLISH', 'English Entrance Exam or TOEFL 97+ / IELTS 7.0+.', 'Graduate English requirement confirmed on the Academic Programs, Admissions, and Contact / FAQ pages.', 'auce-contact-faq');

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
        NULL,
        NULL,
        NULL,
        'UNIVERSITY',
        r.record_key,
        r.requirement_type,
        r.requirement_text,
        NULL,
        NULL,
        NULL,
        TRUE,
        r.notes,
        s.id
    FROM auce_requirement_seed r
    JOIN auce_source_seed ss
      ON ss.source_id = r.source_id
    JOIN source s
      ON s.university_id = v_university_id
     AND s.url = ss.url
    ON CONFLICT (university_id, record_key) DO UPDATE SET
        requirement_type = EXCLUDED.requirement_type,
        requirement_text = EXCLUDED.requirement_text,
        is_required = EXCLUDED.is_required,
        notes = EXCLUDED.notes,
        source_id = EXCLUDED.source_id,
        updated_at = NOW();

    CREATE TEMP TABLE auce_document_seed (
        record_key TEXT PRIMARY KEY,
        document_name TEXT NOT NULL,
        sort_order INTEGER NOT NULL,
        notes TEXT NOT NULL,
        source_id TEXT NOT NULL
    ) ON COMMIT DROP;

    INSERT INTO auce_document_seed (record_key, document_name, sort_order, notes, source_id)
    VALUES
        ('auce-doc-transcript', 'Official university transcript(s)', 1, 'Published on the Academic Programs and Admissions pages.', 'auce-admissions'),
        ('auce-doc-application', 'Completed graduate application', 2, 'Published on the Academic Programs and Admissions pages.', 'auce-admissions'),
        ('auce-doc-recommendations', 'Two recommendation letters', 3, 'Published on the Academic Programs and Admissions pages.', 'auce-admissions');

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
        'APPLICATION',
        d.document_name,
        FALSE,
        d.sort_order,
        d.notes,
        s.id
    FROM auce_document_seed d
    JOIN auce_source_seed ss
      ON ss.source_id = d.source_id
    JOIN source s
      ON s.university_id = v_university_id
     AND s.url = ss.url
    ON CONFLICT (university_id, record_key) DO UPDATE SET
        document_type = EXCLUDED.document_type,
        document_name = EXCLUDED.document_name,
        is_optional = EXCLUDED.is_optional,
        sort_order = EXCLUDED.sort_order,
        notes = EXCLUDED.notes,
        source_id = EXCLUDED.source_id,
        updated_at = NOW();

    CREATE TEMP TABLE auce_scholarship_seed (
        record_key TEXT PRIMARY KEY,
        name TEXT NOT NULL,
        description TEXT NOT NULL,
        coverage TEXT,
        notes TEXT NOT NULL,
        source_id TEXT NOT NULL
    ) ON COMMIT DROP;

    INSERT INTO auce_scholarship_seed (record_key, name, description, coverage, notes, source_id)
    VALUES
        ('auce-scholarship-excellence', 'AUCE Excellence Scholarship', 'General graduate scholarship category published on the Admissions page.', 'General graduate scholarship support', 'No award amount published.', 'auce-admissions'),
        ('auce-scholarship-lebanon-scholars', 'Lebanon Scholars Program', 'General graduate scholarship category published on the Admissions page.', 'General graduate scholarship support', 'No award amount published.', 'auce-admissions'),
        ('auce-scholarship-early-bird', 'Early Bird Scholarship', 'General graduate scholarship category published on the Admissions page.', 'General graduate scholarship support', 'No award amount published.', 'auce-admissions'),
        ('auce-scholarship-design-challenge', 'Design Challenge Scholarship', 'General graduate scholarship category published on the Admissions page.', 'General graduate scholarship support', 'No award amount published.', 'auce-admissions'),
        ('auce-scholarship-athletic', 'Athletic Scholarship', 'General graduate scholarship category published on the Admissions page.', 'General graduate scholarship support', 'No award amount published.', 'auce-admissions'),
        ('auce-scholarship-custom-aid', 'Custom Aid Packages', 'General graduate scholarship category published on the Admissions page.', 'General graduate scholarship support', 'No award amount published.', 'auce-admissions');

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
    FROM auce_scholarship_seed s
    JOIN auce_source_seed ss
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

    CREATE TEMP TABLE auce_financial_aid_seed (
        record_key TEXT PRIMARY KEY,
        name TEXT NOT NULL,
        description TEXT NOT NULL,
        notes TEXT NOT NULL,
        source_id TEXT NOT NULL
    ) ON COMMIT DROP;

    INSERT INTO auce_financial_aid_seed (record_key, name, description, notes, source_id)
    VALUES
        ('auce-aid-excellence', 'AUCE Excellence Scholarship', 'General graduate financial-aid category published on the Admissions page.', 'No award amount published.', 'auce-admissions'),
        ('auce-aid-lebanon-scholars', 'Lebanon Scholars Program', 'General graduate financial-aid category published on the Admissions page.', 'No award amount published.', 'auce-admissions'),
        ('auce-aid-early-bird', 'Early Bird Scholarship', 'General graduate financial-aid category published on the Admissions page.', 'No award amount published.', 'auce-admissions'),
        ('auce-aid-design-challenge', 'Design Challenge Scholarship', 'General graduate financial-aid category published on the Admissions page.', 'No award amount published.', 'auce-admissions'),
        ('auce-aid-athletic', 'Athletic Scholarship', 'General graduate financial-aid category published on the Admissions page.', 'No award amount published.', 'auce-admissions'),
        ('auce-aid-custom-packages', 'Custom Aid Packages', 'General graduate financial-aid category published on the Admissions page.', 'No award amount published.', 'auce-admissions');

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
    FROM auce_financial_aid_seed f
    JOIN auce_source_seed ss
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
        NULL,
        NULL,
        NULL,
        'UNIVERSITY',
        'auce-fee-application',
        NULL,
        'Graduate application fee',
        'FLAT_FEE',
        'USD',
        50,
        'Admissions',
        'Official Academic Programs and Admissions pages state a non-refundable $50 USD graduate application fee.',
        s.id
    FROM auce_source_seed ss
    JOIN source s
      ON s.university_id = v_university_id
     AND s.url = ss.url
    WHERE ss.source_id = 'auce-admissions'
    ON CONFLICT (university_id, record_key) DO UPDATE SET
        academic_year = EXCLUDED.academic_year,
        fee_name = EXCLUDED.fee_name,
        billing_basis = EXCLUDED.billing_basis,
        currency = EXCLUDED.currency,
        amount = EXCLUDED.amount,
        category = EXCLUDED.category,
        notes = EXCLUDED.notes,
        source_id = EXCLUDED.source_id,
        updated_at = NOW();

END $$;
