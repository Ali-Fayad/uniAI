-- AOU graduate data seed migration.
-- Idempotent import for the canonical AOU graduate dataset.

DO $$
DECLARE
    v_university_id BIGINT;
BEGIN

    INSERT INTO university (name, name_ar, acronym, country, city, latitude, longitude, campus_name, campus_type)
    SELECT 'Arab Open University - Lebanon', NULL, 'AOU', 'Lebanon', NULL, NULL, NULL, NULL, NULL
    WHERE NOT EXISTS (SELECT 1 FROM university WHERE name = 'Arab Open University - Lebanon');

    SELECT id INTO v_university_id
    FROM university
    WHERE name = 'Arab Open University - Lebanon'
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

    CREATE TEMP TABLE aou_source_seed (
        source_id TEXT PRIMARY KEY,
        title TEXT NOT NULL,
        url TEXT NOT NULL,
        source_type TEXT NOT NULL,
        accessed_at DATE,
        notes TEXT
    ) ON COMMIT DROP;

    INSERT INTO aou_source_seed (source_id, title, url, source_type, accessed_at, notes)
    SELECT source_id, title, url, source_type, accessed_at, notes
    FROM jsonb_to_recordset($AOU_SOURCES$[
  {"source_id":"AOU_SRC_001","title":"Arab Open University - Lebanon Home","url":"https://web.aou.edu.lb/","source_type":"official_page","accessed_at":"2026-07-05","notes":"Main AOU Lebanon website; navigation hub for admissions, academics, students, faculties, and contact information."},
  {"source_id":"AOU_SRC_002","title":"Graduate Programs","url":"https://web.aou.edu.lb/admission/pages/graduate-programs.aspx","source_type":"official_page","accessed_at":"2026-07-05","notes":"Central graduate programs listing; identifies in-scope master's programs and out-of-scope diplomas."},
  {"source_id":"AOU_SRC_003","title":"Graduate Requirements","url":"https://web.aou.edu.lb/admission/pages/graduate-requirements.aspx","source_type":"official_page","accessed_at":"2026-07-05","notes":"Graduate admission requirements, English/GMAT exemptions, and required documents."},
  {"source_id":"AOU_SRC_004","title":"Admission Fees - Graduate","url":"https://web.aou.edu.lb/admission/pages/graduate-fees.aspx","source_type":"official_page","accessed_at":"2026-07-05","notes":"Graduate application, registration, placement/GEE fees, AGFUND support note, and installment plan statement."},
  {"source_id":"AOU_SRC_005","title":"Apply - Graduate","url":"https://web.aou.edu.lb/admission/pages/graduate-apply.aspx","source_type":"official_page","accessed_at":"2026-07-05","notes":"Graduate application page with MBA, MSc, and MA in TEFL admission requirements and application links."},
  {"source_id":"AOU_SRC_006","title":"FAQ for Admission and Registration - Graduate","url":"https://web.aou.edu.lb/admission/pages/graduate-faq.aspx?list=faq_graduate","source_type":"official_page","accessed_at":"2026-07-05","notes":"Graduate FAQ; confirms offered postgraduate majors, recognition, credit totals, installment payments, and delivery timing."},
  {"source_id":"AOU_SRC_007","title":"Admission Procedure","url":"https://web.aou.edu.lb/admission/Pages/default.aspx","source_type":"official_page","accessed_at":"2026-07-05","notes":"General admission and registration procedure; includes postgraduate acceptance and payment steps."},
  {"source_id":"AOU_SRC_008","title":"Faculty of Business Studies - Postgraduate Programs","url":"https://web.aou.edu.lb/faculties/business/Pages/postgraduate-programs.aspx","source_type":"official_page","accessed_at":"2026-07-05","notes":"Business postgraduate listing for MBA, MBA Finance, and MBA HRM."},
  {"source_id":"AOU_SRC_009","title":"Masters in Business Administration","url":"https://web.aou.edu.lb/faculties/business/Pages/program-details.aspx?degree=2&iid=4","source_type":"official_page","accessed_at":"2026-07-05","notes":"MBA General details: overview, admission requirements, duration, delivery, academic plan, tuition, additional fees, payment and refund rules."},
  {"source_id":"AOU_SRC_010","title":"Masters in Business Administration (Finance)","url":"https://web.aou.edu.lb/faculties/business/Pages/program-details.aspx?degree=2&iid=5","source_type":"official_page","accessed_at":"2026-07-05","notes":"MBA Finance detail page discovered through official postgraduate listing."},
  {"source_id":"AOU_SRC_011","title":"Masters in Business Administration (HRM)","url":"https://web.aou.edu.lb/faculties/business/Pages/program-details.aspx?degree=2&iid=6","source_type":"official_page","accessed_at":"2026-07-05","notes":"MBA HRM detail page discovered through official postgraduate listing."},
  {"source_id":"AOU_SRC_012","title":"Faculty of Computer Studies - Postgraduate Programs","url":"https://web.aou.edu.lb/faculties/computer/Pages/postgraduate-programs.aspx","source_type":"official_page","accessed_at":"2026-07-05","notes":"Computer postgraduate listing for MSc in Computing (Cyber Security and Forensics); includes PDF handbook/specification links."},
  {"source_id":"AOU_SRC_013","title":"MSc in Computing (Cyber Security and Forensics)","url":"https://web.aou.edu.lb/faculties/computer/Pages/program-details.aspx?degree=2&iid=7","source_type":"official_page","accessed_at":"2026-07-05","notes":"MSc Computing program detail page discovered from official postgraduate listing."},
  {"source_id":"AOU_SRC_014","title":"Faculty of Language Studies","url":"https://web.aou.edu.lb/faculties/language/Pages/default.aspx","source_type":"official_page","accessed_at":"2026-07-05","notes":"Faculty page describing MA in TEFL duration, delivery, staged awards, and dual degree structure."},
  {"source_id":"AOU_SRC_015","title":"Faculty of Language Studies - Postgraduate Programs","url":"https://web.aou.edu.lb/faculties/language/Pages/postgraduate-programs.aspx","source_type":"official_page","accessed_at":"2026-07-05","notes":"Language postgraduate listing for MA in Teaching English as a Foreign Language (TEFL) - Thesis Track."},
  {"source_id":"AOU_SRC_016","title":"MA in Teaching English as a Foreign Language (TEFL) - Thesis Track","url":"https://web.aou.edu.lb/faculties/language/Pages/program-details.aspx?degree=2&iid=17","source_type":"official_page","accessed_at":"2026-07-05","notes":"MA TEFL detail page: overview, admission requirements, teaching mode, academic plan, tuition, fees, installment and refund notes."},
  {"source_id":"AOU_SRC_017","title":"Academic Calendar","url":"https://web.aou.edu.lb/students/pages/academic-calendar.aspx","source_type":"official_page","accessed_at":"2026-07-05","notes":"Academic calendar page discovered in student navigation; page content was limited in crawl, but official calendar source recorded."},
  {"source_id":"AOU_SRC_018","title":"Academic Year","url":"https://web.aou.edu.lb/students/guide/Pages/academic-year.aspx","source_type":"official_page","accessed_at":"2026-07-05","notes":"Student guide page describing semester structure and summer term duration."},
  {"source_id":"AOU_SRC_019","title":"Fees - Student Guide","url":"https://web.aou.edu.lb/students/guide/Pages/fees.aspx","source_type":"official_page","accessed_at":"2026-07-05","notes":"Student guide fees page; repeats graduate application, registration, MBA GEE and English placement fees."},
  {"source_id":"AOU_SRC_020","title":"AOU Lebanon Student's Guide","url":"https://web.aou.edu.lb/students/guide","source_type":"official_page","accessed_at":"2026-07-05","notes":"Official student guide area linked from program detail pages and student navigation."}
]$AOU_SOURCES$::jsonb) AS x(source_id TEXT, title TEXT, url TEXT, source_type TEXT, accessed_at DATE, notes TEXT);

    INSERT INTO source (university_id, title, url, source_type, accessed_at)
    SELECT v_university_id, title, url, source_type, accessed_at
    FROM aou_source_seed
    ON CONFLICT (university_id, url) DO UPDATE SET
        title = EXCLUDED.title,
        source_type = EXCLUDED.source_type,
        accessed_at = EXCLUDED.accessed_at,
        updated_at = NOW();

    CREATE TEMP TABLE aou_faculty_seed (
        name TEXT PRIMARY KEY,
        short_name TEXT,
        faculty_type TEXT NOT NULL,
        official_url TEXT,
        notes TEXT
    ) ON COMMIT DROP;

    INSERT INTO aou_faculty_seed (name, short_name, faculty_type, official_url, notes)
    VALUES
        ('Faculty of Business Studies', NULL, 'FACULTY', 'https://web.aou.edu.lb/faculties/business/Pages/postgraduate-programs.aspx', 'Imported from the finalized AOU graduate inventory.'),
        ('Faculty of Computer Studies', NULL, 'FACULTY', 'https://web.aou.edu.lb/faculties/computer/Pages/postgraduate-programs.aspx', 'Imported from the finalized AOU graduate inventory.'),
        ('Faculty of Language Studies', NULL, 'FACULTY', 'https://web.aou.edu.lb/faculties/language/Pages/postgraduate-programs.aspx', 'Imported from the finalized AOU graduate inventory.');

    INSERT INTO university_faculty (university_id, name, short_name, faculty_type, official_url, notes)
    SELECT v_university_id, name, short_name, faculty_type, official_url, notes
    FROM aou_faculty_seed
    ON CONFLICT (university_id, name) DO UPDATE SET
        short_name = EXCLUDED.short_name,
        faculty_type = EXCLUDED.faculty_type,
        official_url = EXCLUDED.official_url,
        notes = EXCLUDED.notes,
        updated_at = NOW();

    CREATE TEMP TABLE aou_program_seed (
        program_key TEXT PRIMARY KEY,
        faculty_name TEXT NOT NULL,
        official_degree_name TEXT NOT NULL,
        degree_type TEXT NOT NULL,
        thesis_or_non_thesis TEXT,
        credits INTEGER,
        duration_value NUMERIC(10, 2),
        duration_unit TEXT,
        delivery_mode TEXT,
        program_description TEXT,
        official_program_url TEXT,
        notes TEXT,
        source_ids JSONB NOT NULL,
        tuition_academic_year TEXT NOT NULL,
        tuition_currency TEXT NOT NULL,
        tuition_billing_basis TEXT NOT NULL,
        tuition_amount NUMERIC(12, 2) NOT NULL,
        tuition_category TEXT NOT NULL,
        tuition_notes TEXT NOT NULL,
        tuition_source_id TEXT NOT NULL
    ) ON COMMIT DROP;

    INSERT INTO aou_program_seed (
        program_key, faculty_name, official_degree_name, degree_type, thesis_or_non_thesis, credits,
        duration_value, duration_unit, delivery_mode, program_description, official_program_url, notes,
        source_ids, tuition_academic_year, tuition_currency, tuition_billing_basis, tuition_amount,
        tuition_category, tuition_notes, tuition_source_id
    )
    SELECT
        program_key, faculty_name, official_degree_name, degree_type, thesis_or_non_thesis, credits,
        duration_value, duration_unit, delivery_mode, program_description, official_program_url, notes,
        source_ids::jsonb, tuition_academic_year, tuition_currency, tuition_billing_basis, tuition_amount,
        tuition_category, tuition_notes, tuition_source_id
    FROM jsonb_to_recordset($AOU_PROGRAMS$[
  {
    "program_key":"business-administration",
    "faculty_name":"Faculty of Business Studies",
    "official_degree_name":"Masters in Business Administration",
    "degree_type":"MASTER",
    "thesis_or_non_thesis":"NON_THESIS",
    "credits":48,
    "duration_value":null,
    "duration_unit":null,
    "delivery_mode":"ON_CAMPUS",
    "program_description":"MBA for practicing managers that emphasizes strategic analysis, interdisciplinary skills, and management practice.",
    "official_program_url":"https://web.aou.edu.lb/faculties/business/Pages/program-details.aspx?degree=2&iid=4",
    "notes":"Official MBA general program. The page states four regular semesters minimum and eight regular semesters maximum. Relevant professional experience and the GMAT-exemption rule are published on the official page.",
    "source_ids":["AOU_SRC_009","AOU_SRC_002","AOU_SRC_003","AOU_SRC_004","AOU_SRC_005","AOU_SRC_006","AOU_SRC_008","AOU_SRC_019"],
    "tuition_academic_year":"2025-2026",
    "tuition_currency":"USD",
    "tuition_billing_basis":"PER_CREDIT",
    "tuition_amount":170,
    "tuition_category":"Graduate Programs",
    "tuition_notes":"Official MBA program detail page lists tuition at USD 170 per credit hour.",
    "tuition_source_id":"AOU_SRC_009"
  },
  {
    "program_key":"business-administration-finance",
    "faculty_name":"Faculty of Business Studies",
    "official_degree_name":"Masters in Business Administration (Finance)",
    "degree_type":"MASTER",
    "thesis_or_non_thesis":"NON_THESIS",
    "credits":48,
    "duration_value":null,
    "duration_unit":null,
    "delivery_mode":"ON_CAMPUS",
    "program_description":"MBA specialization in finance that develops skills to analyze and solve complex business finance problems and communicate finance findings effectively.",
    "official_program_url":"https://web.aou.edu.lb/faculties/business/Pages/program-details.aspx?degree=2&iid=5",
    "notes":"Official MBA Finance program. The page follows the same four-to-eight-semester structure and publishes the same program-level admission framework as the MBA general record.",
    "source_ids":["AOU_SRC_010","AOU_SRC_002","AOU_SRC_003","AOU_SRC_005","AOU_SRC_006","AOU_SRC_008"],
    "tuition_academic_year":"2025-2026",
    "tuition_currency":"USD",
    "tuition_billing_basis":"PER_CREDIT",
    "tuition_amount":170,
    "tuition_category":"Graduate Programs",
    "tuition_notes":"Official MBA Finance program detail page lists tuition at USD 170 per credit hour.",
    "tuition_source_id":"AOU_SRC_010"
  },
  {
    "program_key":"business-administration-hrm",
    "faculty_name":"Faculty of Business Studies",
    "official_degree_name":"Masters in Business Administration (HRM)",
    "degree_type":"MASTER",
    "thesis_or_non_thesis":"NON_THESIS",
    "credits":48,
    "duration_value":null,
    "duration_unit":null,
    "delivery_mode":"ON_CAMPUS",
    "program_description":"MBA specialization in human resource management that develops management and HRM decision-making skills for professional practice.",
    "official_program_url":"https://web.aou.edu.lb/faculties/business/Pages/program-details.aspx?degree=2&iid=6",
    "notes":"Official MBA HRM program. The page follows the same four-to-eight-semester structure and publishes the same program-level admission framework as the MBA general record.",
    "source_ids":["AOU_SRC_011","AOU_SRC_002","AOU_SRC_003","AOU_SRC_005","AOU_SRC_006","AOU_SRC_008"],
    "tuition_academic_year":"2025-2026",
    "tuition_currency":"USD",
    "tuition_billing_basis":"PER_CREDIT",
    "tuition_amount":170,
    "tuition_category":"Graduate Programs",
    "tuition_notes":"Official MBA HRM program detail page lists tuition at USD 170 per credit hour.",
    "tuition_source_id":"AOU_SRC_011"
  },
  {
    "program_key":"computing-cyber-security-forensics",
    "faculty_name":"Faculty of Computer Studies",
    "official_degree_name":"MSc in Computing (Cyber Security and Forensics)",
    "degree_type":"MASTER",
    "thesis_or_non_thesis":"THESIS_OR_PROJECT",
    "credits":48,
    "duration_value":null,
    "duration_unit":null,
    "delivery_mode":"ON_CAMPUS",
    "program_description":"MSc in Computing focused on Cyber Security and Forensics, combining compulsory modules, electives, and a research project with dissertation.",
    "official_program_url":"https://web.aou.edu.lb/faculties/computer/Pages/program-details.aspx?degree=2&iid=7",
    "notes":"Official MSc Computing program. The page confirms the program title and the source set notes that some backgrounds may need additional ITC credits before starting the MSc.",
    "source_ids":["AOU_SRC_013","AOU_SRC_002","AOU_SRC_003","AOU_SRC_005","AOU_SRC_006","AOU_SRC_012"],
    "tuition_academic_year":"2025-2026",
    "tuition_currency":"USD",
    "tuition_billing_basis":"PER_CREDIT",
    "tuition_amount":170,
    "tuition_category":"Graduate Programs",
    "tuition_notes":"Official MSc Computing program detail page lists tuition at USD 170 per credit hour.",
    "tuition_source_id":"AOU_SRC_013"
  },
  {
    "program_key":"tefl-thesis-track",
    "faculty_name":"Faculty of Language Studies",
    "official_degree_name":"MA in Teaching English as a Foreign Language (TEFL) - Thesis Track",
    "degree_type":"MASTER",
    "thesis_or_non_thesis":"THESIS",
    "credits":48,
    "duration_value":null,
    "duration_unit":null,
    "delivery_mode":"ON_CAMPUS",
    "program_description":"MA in Teaching English as a Foreign Language with a thesis track, compulsory modules, and a thesis module.",
    "official_program_url":"https://web.aou.edu.lb/faculties/language/Pages/program-details.aspx?degree=2&iid=17",
    "notes":"Official TEFL thesis-track record. The faculty page also mentions dissertation and comprehensive-exam tracks; the central graduate listing exposes the thesis-track title.",
    "source_ids":["AOU_SRC_016","AOU_SRC_002","AOU_SRC_003","AOU_SRC_005","AOU_SRC_006","AOU_SRC_014","AOU_SRC_015"],
    "tuition_academic_year":"2025-2026",
    "tuition_currency":"USD",
    "tuition_billing_basis":"PER_CREDIT",
    "tuition_amount":170,
    "tuition_category":"Graduate Programs",
    "tuition_notes":"Official MA TEFL program detail page lists tuition at USD 170 per credit hour.",
    "tuition_source_id":"AOU_SRC_016"
  }
]$AOU_PROGRAMS$::jsonb) AS x(
        program_key TEXT,
        faculty_name TEXT,
        official_degree_name TEXT,
        degree_type TEXT,
        thesis_or_non_thesis TEXT,
        credits INTEGER,
        duration_value NUMERIC(10, 2),
        duration_unit TEXT,
        delivery_mode TEXT,
        program_description TEXT,
        official_program_url TEXT,
        notes TEXT,
        source_ids JSONB,
        tuition_academic_year TEXT,
        tuition_currency TEXT,
        tuition_billing_basis TEXT,
        tuition_amount NUMERIC(12, 2),
        tuition_category TEXT,
        tuition_notes TEXT,
        tuition_source_id TEXT
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
        NULL,
        dt.id,
        seed.program_key,
        NULL,
        NULL,
        seed.official_degree_name,
        seed.thesis_or_non_thesis,
        seed.credits,
        seed.duration_value,
        seed.duration_unit,
        NULL,
        seed.delivery_mode,
        seed.program_description,
        seed.official_program_url,
        src.id,
        seed.notes
    FROM aou_program_seed seed
    JOIN university_faculty fac
      ON fac.university_id = v_university_id
     AND fac.name = seed.faculty_name
    JOIN degree_type dt
      ON dt.code = seed.degree_type
    JOIN LATERAL (
        SELECT s.id
        FROM jsonb_array_elements_text(seed.source_ids) WITH ORDINALITY AS src_seed(source_seed_id, ord)
        JOIN aou_source_seed ss ON ss.source_id = src_seed.source_seed_id
        JOIN source s ON s.university_id = v_university_id AND s.url = ss.url
        ORDER BY src_seed.ord
        LIMIT 1
    ) src ON TRUE
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
        university_id, faculty_id, department_id, program_id, scope_level, record_key,
        academic_year, currency, billing_basis, amount, category, notes, source_id
    )
    SELECT
        v_university_id,
        gp.faculty_id,
        gp.department_id,
        gp.id,
        'PROGRAM',
        seed.program_key || ':tuition:' || seed.tuition_academic_year,
        seed.tuition_academic_year,
        seed.tuition_currency,
        seed.tuition_billing_basis,
        seed.tuition_amount,
        seed.tuition_category,
        seed.tuition_notes,
        tuition_src.id
    FROM aou_program_seed seed
    JOIN graduate_program gp
      ON gp.university_id = v_university_id
     AND gp.program_key = seed.program_key
    JOIN aou_source_seed tuition_seed
      ON tuition_seed.source_id = seed.tuition_source_id
    JOIN source tuition_src
      ON tuition_src.university_id = v_university_id
     AND tuition_src.url = tuition_seed.url
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

    CREATE TEMP TABLE aou_fee_seed (
        record_key TEXT PRIMARY KEY,
        fee_name TEXT NOT NULL,
        academic_year TEXT,
        amount NUMERIC(12, 2),
        currency TEXT NOT NULL,
        billing_basis TEXT NOT NULL,
        category TEXT,
        notes TEXT,
        source_id TEXT NOT NULL
    ) ON COMMIT DROP;

    INSERT INTO aou_fee_seed (record_key, fee_name, academic_year, amount, currency, billing_basis, category, notes, source_id)
    SELECT record_key, fee_name, academic_year, amount, currency, billing_basis, category, notes, source_id
    FROM jsonb_to_recordset($AOU_FEES$[
  {"record_key":"aou-fee-application","fee_name":"Application Fee","academic_year":"2025-2026","amount":50,"currency":"USD","billing_basis":"FLAT_FEE","category":"Graduate Fees","notes":"Graduate admission fees page and student guide list the graduate application fee.","source_id":"AOU_SRC_004"},
  {"record_key":"aou-fee-registration","fee_name":"Registration Fee","academic_year":"2025-2026","amount":200,"currency":"USD","billing_basis":"FLAT_FEE","category":"Graduate Fees","notes":"Graduate admission fees page and student guide list the graduate registration fee.","source_id":"AOU_SRC_004"},
  {"record_key":"aou-fee-english-placement","fee_name":"English Placement Test Fee","academic_year":"2025-2026","amount":30,"currency":"USD","billing_basis":"FLAT_FEE","category":"Graduate Fees","notes":"Graduate admission fees page lists the English placement fee for MBA applicants.","source_id":"AOU_SRC_004"},
  {"record_key":"aou-fee-gee","fee_name":"Graduate Entrance Exam Fee","academic_year":"2025-2026","amount":30,"currency":"USD","billing_basis":"FLAT_FEE","category":"Graduate Fees","notes":"Graduate admission fees page lists the MBA Graduate Entrance Exam fee.","source_id":"AOU_SRC_004"},
  {"record_key":"aou-fee-operational","fee_name":"Operational Fee","academic_year":"2025-2026","amount":100,"currency":"USD","billing_basis":"PER_SEMESTER","category":"Graduate Fees","notes":"Operational fee shown on the reviewed graduate program detail pages.","source_id":"AOU_SRC_009"},
  {"record_key":"aou-fee-nssf","fee_name":"NSSF Annual Fee","academic_year":"2025-2026","amount":8400000,"currency":"LBP","billing_basis":"PER_YEAR","category":"Graduate Fees","notes":"Annual NSSF amount shown on the reviewed graduate program detail pages.","source_id":"AOU_SRC_009"}
]$AOU_FEES$::jsonb) AS x(
        record_key TEXT,
        fee_name TEXT,
        academic_year TEXT,
        amount NUMERIC(12, 2),
        currency TEXT,
        billing_basis TEXT,
        category TEXT,
        notes TEXT,
        source_id TEXT
    );

    INSERT INTO graduate_fee_item (
        university_id, faculty_id, department_id, program_id, scope_level, record_key,
        academic_year, fee_name, billing_basis, currency, amount, category, notes, source_id
    )
    SELECT
        v_university_id,
        NULL,
        NULL,
        NULL,
        'UNIVERSITY',
        f.record_key,
        f.academic_year,
        f.fee_name,
        f.billing_basis,
        f.currency,
        f.amount,
        f.category,
        f.notes,
        s.id
    FROM aou_fee_seed f
    JOIN aou_source_seed ss ON ss.source_id = f.source_id
    JOIN source s ON s.university_id = v_university_id AND s.url = ss.url
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

    CREATE TEMP TABLE aou_admission_requirement_seed (
        record_key TEXT PRIMARY KEY,
        requirement_type TEXT NOT NULL,
        requirement_text TEXT NOT NULL,
        notes TEXT,
        source_id TEXT NOT NULL
    ) ON COMMIT DROP;

    INSERT INTO aou_admission_requirement_seed (record_key, requirement_type, requirement_text, notes, source_id)
    VALUES
        (
            'aou-adm-general',
            'GENERAL',
            'Graduate applicants apply through the official graduate application and submit the required supporting documents.',
            'Centralized from the official graduate application and requirements pages.',
            'AOU_SRC_003'
        ),
        (
            'aou-adm-english',
            'ENGLISH',
            'MBA and MSc applicants must sit for the English placement test unless exempted; the reviewed sources preserve the TOEFL 575 versus TOEFL 600 discrepancy.',
            'Centralized from the official graduate requirements and apply pages.',
            'AOU_SRC_003'
        );

    INSERT INTO graduate_admission_requirement (
        university_id, faculty_id, department_id, program_id, scope_level, record_key,
        requirement_type, requirement_text, comparison_operator, threshold_value, threshold_unit,
        is_required, notes, source_id
    )
    SELECT
        v_university_id,
        NULL,
        NULL,
        NULL,
        'UNIVERSITY',
        a.record_key,
        a.requirement_type,
        a.requirement_text,
        NULL,
        NULL,
        NULL,
        TRUE,
        a.notes,
        s.id
    FROM aou_admission_requirement_seed a
    JOIN aou_source_seed ss ON ss.source_id = a.source_id
    JOIN source s ON s.university_id = v_university_id AND s.url = ss.url
    ON CONFLICT (university_id, record_key) DO UPDATE SET
        requirement_type = EXCLUDED.requirement_type,
        requirement_text = EXCLUDED.requirement_text,
        is_required = EXCLUDED.is_required,
        notes = EXCLUDED.notes,
        source_id = EXCLUDED.source_id,
        updated_at = NOW();

    CREATE TEMP TABLE aou_required_document_seed (
        record_key TEXT PRIMARY KEY,
        document_type TEXT NOT NULL,
        document_name TEXT NOT NULL,
        sort_order INTEGER,
        notes TEXT,
        source_id TEXT NOT NULL
    ) ON COMMIT DROP;

    INSERT INTO aou_required_document_seed (record_key, document_type, document_name, sort_order, notes, source_id)
    VALUES
        ('aou-doc-application', 'APPLICATION', 'Completed graduate application', 1, 'Listed in the official graduate application instructions.', 'AOU_SRC_003'),
        ('aou-doc-equivalency', 'ACADEMIC', 'Certified copy of bachelor''s equivalency or Ministry of Higher Education evaluation where applicable', 2, 'Listed in the official graduate application instructions.', 'AOU_SRC_003'),
        ('aou-doc-transcript', 'TRANSCRIPT', 'Official transcript', 3, 'Listed in the official graduate application instructions.', 'AOU_SRC_003'),
        ('aou-doc-employment', 'PROFESSIONAL', 'Professional/employment certificates', 4, 'Listed in the official graduate application instructions.', 'AOU_SRC_003'),
        ('aou-doc-cv', 'CV', 'Updated CV or resume', 5, 'Listed in the official graduate application instructions.', 'AOU_SRC_003'),
        ('aou-doc-recommendations', 'RECOMMENDATION', 'Two recommendation letters', 6, 'Listed in the official graduate application instructions.', 'AOU_SRC_003'),
        ('aou-doc-national-id', 'IDENTITY', 'Copy of a valid national ID', 7, 'Listed in the official graduate application instructions.', 'AOU_SRC_003');

    INSERT INTO graduate_required_document (
        university_id, faculty_id, department_id, program_id, scope_level, record_key,
        document_type, document_name, is_optional, sort_order, notes, source_id
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
    FROM aou_required_document_seed d
    JOIN aou_source_seed ss ON ss.source_id = d.source_id
    JOIN source s ON s.university_id = v_university_id AND s.url = ss.url
    ON CONFLICT (university_id, record_key) DO UPDATE SET
        document_type = EXCLUDED.document_type,
        document_name = EXCLUDED.document_name,
        is_optional = EXCLUDED.is_optional,
        sort_order = EXCLUDED.sort_order,
        notes = EXCLUDED.notes,
        source_id = EXCLUDED.source_id,
        updated_at = NOW();

    CREATE TEMP TABLE aou_scholarship_seed (
        record_key TEXT PRIMARY KEY,
        academic_year TEXT,
        name TEXT NOT NULL,
        description TEXT,
        coverage TEXT,
        amount NUMERIC(12, 2),
        currency TEXT,
        notes TEXT,
        source_id TEXT NOT NULL
    ) ON COMMIT DROP;

    INSERT INTO aou_scholarship_seed (record_key, academic_year, name, description, coverage, amount, currency, notes, source_id)
    VALUES
        ('aou-scholarship-agfund', '2025-2026', 'AGFUND Financial Support', 'The graduate fees page states that students benefit from financial support from AGFUND.', 'Need-based support', NULL, 'USD', 'Centralized university-level scholarship summary from the official financial aid page.', 'AOU_SRC_004');

    INSERT INTO graduate_scholarship (
        university_id, faculty_id, department_id, program_id, scope_level, record_key,
        academic_year, name, description, coverage, amount, currency, notes, source_id
    )
    SELECT
        v_university_id,
        NULL,
        NULL,
        NULL,
        'UNIVERSITY',
        s.record_key,
        s.academic_year,
        s.name,
        s.description,
        s.coverage,
        s.amount,
        s.currency,
        s.notes,
        src.id
    FROM aou_scholarship_seed s
    JOIN aou_source_seed ss ON ss.source_id = s.source_id
    JOIN source src ON src.university_id = v_university_id AND src.url = ss.url
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

    CREATE TEMP TABLE aou_financial_aid_seed (
        record_key TEXT PRIMARY KEY,
        academic_year TEXT,
        name TEXT NOT NULL,
        description TEXT,
        amount NUMERIC(12, 2),
        currency TEXT,
        notes TEXT,
        source_id TEXT NOT NULL
    ) ON COMMIT DROP;

    INSERT INTO aou_financial_aid_seed (record_key, academic_year, name, description, amount, currency, notes, source_id)
    VALUES
        ('aou-fin-aid-agfund', '2025-2026', 'AGFUND Support', 'AOU confirms financial support through AGFUND.', NULL, 'USD', 'Centralized university-level financial aid summary from the official financial aid page.', 'AOU_SRC_004');

    INSERT INTO graduate_financial_aid (
        university_id, faculty_id, department_id, program_id, scope_level, record_key,
        academic_year, name, description, amount, currency, notes, source_id
    )
    SELECT
        v_university_id,
        NULL,
        NULL,
        NULL,
        'UNIVERSITY',
        f.record_key,
        f.academic_year,
        f.name,
        f.description,
        f.amount,
        f.currency,
        f.notes,
        s.id
    FROM aou_financial_aid_seed f
    JOIN aou_source_seed ss ON ss.source_id = f.source_id
    JOIN source s ON s.university_id = v_university_id AND s.url = ss.url
    ON CONFLICT (university_id, record_key) DO UPDATE SET
        academic_year = EXCLUDED.academic_year,
        name = EXCLUDED.name,
        description = EXCLUDED.description,
        amount = EXCLUDED.amount,
        currency = EXCLUDED.currency,
        notes = EXCLUDED.notes,
        source_id = EXCLUDED.source_id,
        updated_at = NOW();

    CREATE TEMP TABLE aou_payment_plan_seed (
        record_key TEXT PRIMARY KEY,
        academic_year TEXT,
        name TEXT NOT NULL,
        description TEXT,
        installments_count INTEGER,
        down_payment_amount NUMERIC(12, 2),
        down_payment_currency TEXT,
        interval_label TEXT,
        notes TEXT,
        source_id TEXT NOT NULL
    ) ON COMMIT DROP;

    INSERT INTO aou_payment_plan_seed (record_key, academic_year, name, description, installments_count, down_payment_amount, down_payment_currency, interval_label, notes, source_id)
    VALUES
        ('aou-payment-plan-installments', '2025-2026', 'Installment Payment Plan', 'Tuition fees may be paid in installments; the university notes 25% of tuition plus other fees at the beginning of each semester.', NULL, NULL, NULL, NULL, 'Installment plan statement from the graduate fees page and student guide.', 'AOU_SRC_004');

    INSERT INTO graduate_payment_plan (
        university_id, faculty_id, department_id, program_id, scope_level, record_key,
        academic_year, name, description, installments_count, down_payment_amount, down_payment_currency,
        interval_label, notes, source_id
    )
    SELECT
        v_university_id,
        NULL,
        NULL,
        NULL,
        'UNIVERSITY',
        p.record_key,
        p.academic_year,
        p.name,
        p.description,
        p.installments_count,
        p.down_payment_amount,
        p.down_payment_currency,
        p.interval_label,
        p.notes,
        s.id
    FROM aou_payment_plan_seed p
    JOIN aou_source_seed ss ON ss.source_id = p.source_id
    JOIN source s ON s.university_id = v_university_id AND s.url = ss.url
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

    CREATE TEMP TABLE aou_accreditation_seed (
        record_key TEXT PRIMARY KEY,
        program_key TEXT NOT NULL,
        name TEXT NOT NULL,
        authority TEXT,
        status TEXT,
        notes TEXT,
        source_id TEXT NOT NULL
    ) ON COMMIT DROP;

    INSERT INTO aou_accreditation_seed (record_key, program_key, name, authority, status, notes, source_id)
    VALUES
        ('aou-accred-mba-general', 'business-administration', 'OU Validated', 'Open University', 'Validated', 'Program page identifies the MBA as OU Validated.', 'AOU_SRC_009'),
        ('aou-accred-mba-finance', 'business-administration-finance', 'OU Validated', 'Open University', 'Validated', 'Program page identifies the MBA Finance degree as OU Validated.', 'AOU_SRC_010'),
        ('aou-accred-mba-hrm', 'business-administration-hrm', 'OU Validated', 'Open University', 'Validated', 'Program page identifies the MBA HRM degree as OU Validated.', 'AOU_SRC_011'),
        ('aou-accred-msc-computing', 'computing-cyber-security-forensics', 'OU Validated', 'Open University', 'Validated', 'Program page identifies the MSc in Computing as OU Validated.', 'AOU_SRC_013'),
        ('aou-accred-tefl', 'tefl-thesis-track', 'OU Validated', 'Open University', 'Validated', 'Program page identifies the MA TEFL degree as OU Validated.', 'AOU_SRC_016');

    INSERT INTO graduate_accreditation (
        university_id, faculty_id, department_id, program_id, scope_level, record_key,
        name, authority, status, valid_from, valid_until, notes, source_id
    )
    SELECT
        v_university_id,
        fac.id,
        NULL,
        gp.id,
        'PROGRAM',
        a.record_key,
        a.name,
        a.authority,
        a.status,
        NULL,
        NULL,
        a.notes,
        s.id
    FROM aou_accreditation_seed a
    JOIN graduate_program gp
      ON gp.university_id = v_university_id
     AND gp.program_key = a.program_key
    JOIN university_faculty fac
      ON fac.university_id = v_university_id
     AND fac.id = gp.faculty_id
    JOIN aou_source_seed ss ON ss.source_id = a.source_id
    JOIN source s ON s.university_id = v_university_id AND s.url = ss.url
    ON CONFLICT (university_id, record_key) DO UPDATE SET
        faculty_id = EXCLUDED.faculty_id,
        department_id = EXCLUDED.department_id,
        program_id = EXCLUDED.program_id,
        scope_level = EXCLUDED.scope_level,
        name = EXCLUDED.name,
        authority = EXCLUDED.authority,
        status = EXCLUDED.status,
        notes = EXCLUDED.notes,
        source_id = EXCLUDED.source_id,
        updated_at = NOW();

    INSERT INTO graduate_program_source (
        university_id, program_id, source_id, source_role, source_order, evidence_text, notes
    )
    SELECT
        v_university_id,
        gp.id,
        s.id,
        CASE WHEN src.ord = 1 THEN 'PRIMARY' ELSE 'SECONDARY' END,
        src.ord,
        ss.title,
        NULL
    FROM aou_program_seed seed
    JOIN graduate_program gp
      ON gp.university_id = v_university_id
     AND gp.program_key = seed.program_key
    JOIN LATERAL jsonb_array_elements_text(seed.source_ids) WITH ORDINALITY AS src(source_seed_id, ord)
      ON TRUE
    JOIN aou_source_seed ss ON ss.source_id = src.source_seed_id
    JOIN source s ON s.university_id = v_university_id AND s.url = ss.url
    ON CONFLICT (program_id, source_id, source_role) DO UPDATE SET
        source_order = EXCLUDED.source_order,
        evidence_text = EXCLUDED.evidence_text,
        notes = EXCLUDED.notes,
        updated_at = NOW();

    INSERT INTO graduate_program_source (
        university_id, program_id, source_id, source_role, source_order, evidence_text, notes
    )
    SELECT
        v_university_id,
        gp.id,
        s.id,
        'TUITION',
        100,
        'Tuition source',
        NULL
    FROM aou_program_seed seed
    JOIN graduate_program gp
      ON gp.university_id = v_university_id
     AND gp.program_key = seed.program_key
    JOIN aou_source_seed ss ON ss.source_id = seed.tuition_source_id
    JOIN source s ON s.university_id = v_university_id AND s.url = ss.url
    ON CONFLICT (program_id, source_id, source_role) DO UPDATE SET
        source_order = EXCLUDED.source_order,
        evidence_text = EXCLUDED.evidence_text,
        notes = EXCLUDED.notes,
        updated_at = NOW();

END $$;
