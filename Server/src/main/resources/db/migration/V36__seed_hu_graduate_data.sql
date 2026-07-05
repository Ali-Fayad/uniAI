-- HU graduate data seed migration.
-- Idempotent import for the canonical HU graduate dataset.

DO $$
DECLARE
    v_university_id BIGINT;
    v_language_en_id BIGINT;
BEGIN

    INSERT INTO university (name, name_ar, acronym, country, city, latitude, longitude, campus_name, campus_type)
    SELECT 'Haigazian University', NULL, 'HU', 'Lebanon', NULL, NULL, NULL, NULL, NULL
    WHERE NOT EXISTS (SELECT 1 FROM university WHERE name = 'Haigazian University');

    SELECT id INTO v_university_id FROM university WHERE name = 'Haigazian University' ORDER BY id LIMIT 1;

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

    SELECT id INTO v_language_en_id FROM language WHERE code = 'en' ORDER BY id LIMIT 1;

    CREATE TEMP TABLE hu_source_seed (source_id TEXT PRIMARY KEY, title TEXT NOT NULL, url TEXT NOT NULL, source_type TEXT NOT NULL, accessed_at DATE, notes TEXT) ON COMMIT DROP;
    INSERT INTO hu_source_seed SELECT source_id, title, url, source_type, accessed_at, notes FROM jsonb_to_recordset($HU_SOURCES$[
  {
    "source_id": "HU_SRC_001",
    "title": "Haigazian University homepage",
    "url": "https://www.haigazian.edu.lb/",
    "source_type": "HTML",
    "accessed_at": "2026-07-04",
    "notes": "Official homepage; navigation source for admissions, academics, resources, and current site structure."
  },
  {
    "source_id": "HU_SRC_002",
    "title": "Academics",
    "url": "https://www.haigazian.edu.lb/academics/",
    "source_type": "HTML",
    "accessed_at": "2026-07-04",
    "notes": "States HU uses the U.S. higher-education model, English as language of instruction, and offers MA and MBA degrees."
  },
  {
    "source_id": "HU_SRC_003",
    "title": "Graduate Programs",
    "url": "https://www.haigazian.edu.lb/academics/graduate-programs/",
    "source_type": "HTML",
    "accessed_at": "2026-07-04",
    "notes": "Primary graduate program index; lists MBA and MA offerings with credits and degree labels."
  },
  {
    "source_id": "HU_SRC_004",
    "title": "MBA General Business Administration 2024-2026",
    "url": "https://www.haigazian.edu.lb/wp-content/uploads/2026/01/MBA-Generalbus24-26.pdf",
    "source_type": "PDF",
    "accessed_at": "2026-07-04",
    "notes": "Official program PDF for MBA General Business Administration; includes MBA overview, admissions background, credits, prerequisites, core courses, thesis."
  },
  {
    "source_id": "HU_SRC_005",
    "title": "MBA Accounting 2024-2026",
    "url": "https://www.haigazian.edu.lb/wp-content/uploads/2026/01/MBA-Accounting24-26.pdf",
    "source_type": "PDF",
    "accessed_at": "2026-07-04",
    "notes": "Official program PDF for MBA Accounting; also includes shared MBA overview and Finance section."
  },
  {
    "source_id": "HU_SRC_006",
    "title": "MBA Finance 2024-2026",
    "url": "https://www.haigazian.edu.lb/wp-content/uploads/2026/01/MBA-Finance24-26.pdf",
    "source_type": "PDF",
    "accessed_at": "2026-07-04",
    "notes": "Official program PDF for MBA Finance; same catalog extract as Accounting/Finance page."
  },
  {
    "source_id": "HU_SRC_007",
    "title": "MBA Human Resources Management 2024-2026",
    "url": "https://www.haigazian.edu.lb/wp-content/uploads/2026/01/MBA-HumanRessmanag24-26.pdf",
    "source_type": "PDF",
    "accessed_at": "2026-07-04",
    "notes": "Official program PDF for MBA Human Resources Management; also includes Management section."
  },
  {
    "source_id": "HU_SRC_008",
    "title": "MBA Management 2024-2026",
    "url": "https://www.haigazian.edu.lb/wp-content/uploads/2026/01/MBA-Management24-26.pdf",
    "source_type": "PDF",
    "accessed_at": "2026-07-04",
    "notes": "Official program PDF for MBA Management; duplicated extract with HRM/Management content."
  },
  {
    "source_id": "HU_SRC_009",
    "title": "MBA Marketing 2024-2026",
    "url": "https://www.haigazian.edu.lb/wp-content/uploads/2026/01/MBA-Marketing24-26.pdf",
    "source_type": "PDF",
    "accessed_at": "2026-07-04",
    "notes": "Official program PDF for MBA Marketing."
  },
  {
    "source_id": "HU_SRC_010",
    "title": "MA Education 2024-2026",
    "url": "https://www.haigazian.edu.lb/wp-content/uploads/2026/01/MA-Education24-26.pdf",
    "source_type": "PDF",
    "accessed_at": "2026-07-04",
    "notes": "Official program PDF for MA Education."
  },
  {
    "source_id": "HU_SRC_011",
    "title": "MA Psychology 2024-2026",
    "url": "https://www.haigazian.edu.lb/wp-content/uploads/2026/01/MA-Psychology24-26.pdf",
    "source_type": "PDF",
    "accessed_at": "2026-07-04",
    "notes": "Official program PDF for MA Psychology."
  },
  {
    "source_id": "HU_SRC_012",
    "title": "Graduate Academic Information",
    "url": "https://www.haigazian.edu.lb/academics/graduate-academic-information/",
    "source_type": "HTML",
    "accessed_at": "2026-07-04",
    "notes": "Graduate rules: time limits, normal progress, grading, graduation, MA and MBA academic rules."
  },
  {
    "source_id": "HU_SRC_013",
    "title": "Graduate Handbook",
    "url": "https://www.haigazian.edu.lb/academics/graduate-academic-information/graduate-handbook/",
    "source_type": "HTML",
    "accessed_at": "2026-07-04",
    "notes": "Graduate handbook landing page; links to handbook PDF and thesis appendices."
  },
  {
    "source_id": "HU_SRC_014",
    "title": "Graduate Handbook PDF",
    "url": "https://www.haigazian.edu.lb/wp-content/uploads/2023/08/Graduate-Handbook.pdf",
    "source_type": "PDF",
    "accessed_at": "2026-07-04",
    "notes": "Official student graduate program handbook with thesis process and graduate study regulations."
  },
  {
    "source_id": "HU_SRC_015",
    "title": "Graduate Admissions",
    "url": "https://www.haigazian.edu.lb/admissions/graduate-admissions/",
    "source_type": "HTML",
    "accessed_at": "2026-07-04",
    "notes": "Graduate admissions landing page; sparse content and deadline widget."
  },
  {
    "source_id": "HU_SRC_016",
    "title": "Graduate Admissions Information",
    "url": "https://www.haigazian.edu.lb/admissions/graduate-admissions/graduate-admissions-information/",
    "source_type": "HTML",
    "accessed_at": "2026-07-04",
    "notes": "Graduate admissions categories: regular graduate students, undergraduate students, special students, auditors, transfer students, re-admission."
  },
  {
    "source_id": "HU_SRC_017",
    "title": "Admissions",
    "url": "https://www.haigazian.edu.lb/admissions/",
    "source_type": "HTML",
    "accessed_at": "2026-07-04",
    "notes": "General admissions philosophy and categories; official HU admissions source."
  },
  {
    "source_id": "HU_SRC_018",
    "title": "Apply Now",
    "url": "https://www.haigazian.edu.lb/admissions/apply-now/",
    "source_type": "HTML",
    "accessed_at": "2026-07-04",
    "notes": "Online application landing page and general HU positioning."
  },
  {
    "source_id": "HU_SRC_019",
    "title": "Tuition & Fees",
    "url": "https://www.haigazian.edu.lb/admissions/tuition-fees/",
    "source_type": "HTML",
    "accessed_at": "2026-07-04",
    "notes": "Tuition/fee policy: payment deadlines, installment payments, annual review, 2025-2026 10% subsidy."
  },
  {
    "source_id": "HU_SRC_020",
    "title": "Graduate Tuition and Fees",
    "url": "https://www.haigazian.edu.lb/admissions/tuition-fees/graduate-tuition-and-fees/",
    "source_type": "HTML",
    "accessed_at": "2026-07-04",
    "notes": "Graduate tuition/fees page; visible extracted text mainly delinquent payment policy."
  },
  {
    "source_id": "HU_SRC_021",
    "title": "Graduate Tuition Fee Calculator",
    "url": "https://www.haigazian.edu.lb/graduatetuitioncal/",
    "source_type": "HTML",
    "accessed_at": "2026-07-04",
    "notes": "Graduate tuition calculator landing page; no machine-readable rates found in extracted content."
  },
  {
    "source_id": "HU_SRC_022",
    "title": "Financial Aid",
    "url": "https://www.haigazian.edu.lb/admissions/financial-aid/",
    "source_type": "HTML",
    "accessed_at": "2026-07-04",
    "notes": "General financial aid policy; annual aid for regular full-time students, good-standing requirement, work program."
  },
  {
    "source_id": "HU_SRC_023",
    "title": "Apply for Financial Aid",
    "url": "https://www.haigazian.edu.lb/admissions/financial-aid/apply-for-financial-aid/",
    "source_type": "HTML",
    "accessed_at": "2026-07-04",
    "notes": "Financial aid application document list and application notes."
  },
  {
    "source_id": "HU_SRC_024",
    "title": "Types of Financial Aid",
    "url": "https://www.haigazian.edu.lb/admissions/financial-aid/types-of-financial-aid/",
    "source_type": "HTML",
    "accessed_at": "2026-07-04",
    "notes": "Need-based aid and academic scholarship types; includes percentage ranges and scholarship notes."
  },
  {
    "source_id": "HU_SRC_025",
    "title": "Financial Aid FAQs",
    "url": "https://www.haigazian.edu.lb/admissions/financial-aid/financial-aid-faqs/",
    "source_type": "HTML",
    "accessed_at": "2026-07-04",
    "notes": "FAQ includes explicit graduate aid eligibility: full-time graduate students may apply, limited recipients, priority by demonstrated need."
  },
  {
    "source_id": "HU_SRC_026",
    "title": "Financial Aid Bulletin",
    "url": "https://www.haigazian.edu.lb/admissions/financial-aid/financial-aid-bulletin/",
    "source_type": "HTML",
    "accessed_at": "2026-07-04",
    "notes": "Other sources of additional aid; mostly external funds, so not used for official HU program seeding except as advisory context."
  },
  {
    "source_id": "HU_SRC_027",
    "title": "Contact Financial Aid",
    "url": "https://www.haigazian.edu.lb/admissions/financial-aid/contact-financial-aid/",
    "source_type": "HTML",
    "accessed_at": "2026-07-04",
    "notes": "Financial Aid Office contact information."
  },
  {
    "source_id": "HU_SRC_028",
    "title": "Academic Calendar",
    "url": "https://www.haigazian.edu.lb/academics/academic-calendar/",
    "source_type": "HTML",
    "accessed_at": "2026-07-04",
    "notes": "Academic calendar index; includes 2026-2027 and 2025-2026 calendar PDFs/links."
  },
  {
    "source_id": "HU_SRC_029",
    "title": "Academic Forms",
    "url": "https://www.haigazian.edu.lb/academics/academic-forms-resources/",
    "source_type": "HTML",
    "accessed_at": "2026-07-04",
    "notes": "Application, transcript, financial aid, other, graduate, and financial certificate forms; graduate thesis appendices."
  },
  {
    "source_id": "HU_SRC_030",
    "title": "International Students",
    "url": "https://www.haigazian.edu.lb/admissions/international-students/",
    "source_type": "HTML",
    "accessed_at": "2026-07-04",
    "notes": "International-student page; appears undergraduate/special-category focused, no graduate-specific international policy found."
  },
  {
    "source_id": "HU_SRC_031",
    "title": "HU Faculty",
    "url": "https://www.haigazian.edu.lb/academics/hu-faculty/",
    "source_type": "HTML",
    "accessed_at": "2026-07-04",
    "notes": "Faculty listing; identifies Faculty of Business Administration and Economics and School of Arts and Sciences subdivisions including Social and Behavioral Sciences."
  },
  {
    "source_id": "HU_SRC_032",
    "title": "Haigazian University Catalog 2024-2026",
    "url": "https://www.haigazian.edu.lb/wp-content/uploads/2025/08/Catalog-2024-2026.pdf",
    "source_type": "PDF",
    "accessed_at": "2026-07-04",
    "notes": "Official catalog PDF; includes graduate catalog section, admissions, financial information, academic information, MBA and MA programs, language/institution profile."
  }
]$HU_SOURCES$) AS x(source_id TEXT, title TEXT, url TEXT, source_type TEXT, accessed_at DATE, notes TEXT);
    INSERT INTO source (university_id, title, url, source_type, accessed_at) SELECT v_university_id, title, url, source_type, accessed_at FROM hu_source_seed ON CONFLICT (university_id, url) DO UPDATE SET title = EXCLUDED.title, source_type = EXCLUDED.source_type, accessed_at = EXCLUDED.accessed_at, updated_at = NOW();

    CREATE TEMP TABLE hu_faculty_seed (name TEXT PRIMARY KEY, short_name TEXT, faculty_type TEXT NOT NULL, official_url TEXT, notes TEXT) ON COMMIT DROP;
    INSERT INTO hu_faculty_seed VALUES
        ('Faculty of Business Administration and Economics', NULL, 'FACULTY', NULL, 'Imported from HU graduate inventory.'),
        ('Faculty of Social and Behavioral Sciences', NULL, 'FACULTY', NULL, 'Imported from HU graduate inventory.');
    INSERT INTO university_faculty (university_id, name, short_name, faculty_type, official_url, notes) SELECT v_university_id, name, short_name, faculty_type, official_url, notes FROM hu_faculty_seed ON CONFLICT (university_id, name) DO UPDATE SET short_name = EXCLUDED.short_name, faculty_type = EXCLUDED.faculty_type, official_url = EXCLUDED.official_url, notes = EXCLUDED.notes, updated_at = NOW();

    CREATE TEMP TABLE hu_program_seed (program_key TEXT PRIMARY KEY, faculty_name TEXT NOT NULL, major_category TEXT, major TEXT, degree_type TEXT NOT NULL, official_degree_name TEXT, thesis_or_non_thesis TEXT, credits INTEGER, duration_value NUMERIC(10, 2), duration_unit TEXT, primary_language_code TEXT, delivery_mode TEXT, program_description TEXT, official_program_url TEXT, source_ids JSONB NOT NULL, notes TEXT, concentrations JSONB NOT NULL, tuition JSONB NOT NULL) ON COMMIT DROP;
    INSERT INTO hu_program_seed SELECT program_key, faculty_name, major_category, major, degree_type, official_degree_name, thesis_or_non_thesis, credits, duration_value, duration_unit, primary_language_code, delivery_mode, program_description, official_program_url, source_ids::jsonb, notes, concentrations::jsonb, tuition::jsonb FROM jsonb_to_recordset($HU_PROGRAMS$[
  {
    "program_key": "hu-mba",
    "faculty_name": "Faculty of Business Administration and Economics",
    "major_category": "Business Administration",
    "major": "MBA",
    "degree_type": "MASTER",
    "official_degree_name": "Master of Business Administration",
    "thesis_or_non_thesis": "THESIS",
    "credits": 39,
    "duration_value": null,
    "duration_unit": null,
    "primary_language_code": "en",
    "delivery_mode": null,
    "program_description": "The MBA develops analytical, decision-making, and problem-solving skills for leadership in professional management roles. HU's MBA framework allows specialization in six areas while keeping one common degree structure.",
    "official_program_url": "https://www.haigazian.edu.lb/academics/graduate-programs/",
    "source_ids": [
      "HU_SRC_002",
      "HU_SRC_003",
      "HU_SRC_004",
      "HU_SRC_005",
      "HU_SRC_006",
      "HU_SRC_007",
      "HU_SRC_008",
      "HU_SRC_009",
      "HU_SRC_032"
    ],
    "notes": "HU's graduate index lists six MBA rows, but the official MBA PDFs share one common MBA framework with specialization areas. This inventory models them as one MBA record with concentrations instead of six separate programs.",
    "concentrations": [
      "General Business Administration",
      "Accounting",
      "Finance",
      "Human Resources Management",
      "Management",
      "Marketing"
    ],
    "tuition": {
      "academic_year": "2024-2025",
      "currency": "USD",
      "billing_basis": "PER_CREDIT",
      "amount": 455,
      "category": "Faculty of Business Administration and Economics",
      "source_ids": [
        "HU_SRC_019",
        "HU_SRC_032"
      ],
      "notes": "HU's official graduate catalog lists graduate tuition at USD 455 per credit; the tuition-fees page confirms current annual review and term-based payment policy."
    }
  },
  {
    "program_key": "hu-ma-education",
    "faculty_name": "Faculty of Social and Behavioral Sciences",
    "major_category": "Social and Behavioral Sciences",
    "major": "Education",
    "degree_type": "MASTER",
    "official_degree_name": "Master of Arts in Education",
    "thesis_or_non_thesis": "THESIS",
    "credits": 33,
    "duration_value": null,
    "duration_unit": null,
    "primary_language_code": "en",
    "delivery_mode": null,
    "program_description": "The MA in Education is designed to prepare leaders in education and provides a path for advanced study. HU offers emphases in Educational Administration and Supervision and Special Education.",
    "official_program_url": "https://www.haigazian.edu.lb/wp-content/uploads/2026/01/MA-Education24-26.pdf",
    "source_ids": [
      "HU_SRC_002",
      "HU_SRC_003",
      "HU_SRC_010",
      "HU_SRC_032"
    ],
    "notes": "Official graduate program listed in the HU index and MA program PDF. No additional title-level specialization was treated as a separate program.",
    "concentrations": [
      "Educational Administration and Supervision",
      "Special Education"
    ],
    "tuition": {
      "academic_year": "2024-2025",
      "currency": "USD",
      "billing_basis": "PER_CREDIT",
      "amount": 455,
      "category": "Faculty of Social and Behavioral Sciences",
      "source_ids": [
        "HU_SRC_019",
        "HU_SRC_032"
      ],
      "notes": "HU's official graduate catalog lists graduate tuition at USD 455 per credit; the tuition-fees page confirms current annual review and term-based payment policy."
    }
  },
  {
    "program_key": "hu-ma-psychology",
    "faculty_name": "Faculty of Social and Behavioral Sciences",
    "major_category": "Social and Behavioral Sciences",
    "major": "Psychology",
    "degree_type": "MASTER",
    "official_degree_name": "Master of Arts in Psychology",
    "thesis_or_non_thesis": "THESIS",
    "credits": 33,
    "duration_value": null,
    "duration_unit": null,
    "primary_language_code": "en",
    "delivery_mode": null,
    "program_description": "The MA in Psychology focuses on the broader social context and is organized into five modules: general psychology, clinical psychology, counseling, industrial/organizational psychology, and marketing and advertising psychology.",
    "official_program_url": "https://www.haigazian.edu.lb/wp-content/uploads/2026/01/MA-Psychology24-26.pdf",
    "source_ids": [
      "HU_SRC_002",
      "HU_SRC_003",
      "HU_SRC_011",
      "HU_SRC_032"
    ],
    "notes": "Official graduate program listed in the HU index and MA program PDF. No separate PhD program or additional psychology graduate degree was found in the discovery set.",
    "concentrations": [
      "General Psychology",
      "Clinical Psychology",
      "Counseling",
      "Industrial/Organizational Psychology",
      "Marketing and Advertising Psychology"
    ],
    "tuition": {
      "academic_year": "2024-2025",
      "currency": "USD",
      "billing_basis": "PER_CREDIT",
      "amount": 455,
      "category": "Faculty of Social and Behavioral Sciences",
      "source_ids": [
        "HU_SRC_019",
        "HU_SRC_032"
      ],
      "notes": "HU's official graduate catalog lists graduate tuition at USD 455 per credit; the tuition-fees page confirms current annual review and term-based payment policy."
    }
  }
]$HU_PROGRAMS$) AS x(program_key TEXT, faculty_name TEXT, major_category TEXT, major TEXT, degree_type TEXT, official_degree_name TEXT, thesis_or_non_thesis TEXT, credits INTEGER, duration_value NUMERIC(10,2), duration_unit TEXT, primary_language_code TEXT, delivery_mode TEXT, program_description TEXT, official_program_url TEXT, source_ids JSONB, notes TEXT, concentrations JSONB, tuition JSONB);

    INSERT INTO graduate_program (university_id, faculty_id, department_id, degree_type_id, program_key, major_category, major, official_degree_name, thesis_or_non_thesis, credits, duration_value, duration_unit, primary_language_id, delivery_mode, program_description, official_program_url, source_id, notes)
    SELECT
        v_university_id,
        fac.id,
        NULL,
        dt.id,
        seed.program_key,
        seed.major_category,
        seed.major,
        seed.official_degree_name,
        seed.thesis_or_non_thesis,
        seed.credits,
        seed.duration_value,
        seed.duration_unit,
        v_language_en_id,
        seed.delivery_mode,
        seed.program_description,
        seed.official_program_url,
        src.id,
        seed.notes
    FROM hu_program_seed seed
    JOIN university_faculty fac ON fac.university_id = v_university_id AND fac.name = seed.faculty_name
    JOIN degree_type dt ON dt.code = seed.degree_type
    JOIN hu_source_seed hs ON hs.source_id = (seed.source_ids->>0)
    JOIN source src ON src.university_id = v_university_id AND src.url = hs.url
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

    INSERT INTO graduate_program_track (university_id, program_id, track_type, track_name, track_order, is_primary, description, source_id, notes)
    SELECT
        v_university_id,
        gp.id,
        'CONCENTRATION',
        track.track_name,
        track.track_order,
        CASE WHEN track.track_order = 1 THEN TRUE ELSE FALSE END,
        NULL,
        src.id,
        seed.notes
    FROM hu_program_seed seed
    JOIN graduate_program gp ON gp.university_id = v_university_id AND gp.program_key = seed.program_key
    JOIN LATERAL jsonb_array_elements_text(seed.concentrations) WITH ORDINALITY AS track(track_name, track_order) ON TRUE
    JOIN hu_source_seed hs ON hs.source_id = CASE
        WHEN seed.program_key = 'hu-mba' AND track.track_name = 'General Business Administration' THEN 'HU_SRC_004'
        WHEN seed.program_key = 'hu-mba' AND track.track_name = 'Accounting' THEN 'HU_SRC_005'
        WHEN seed.program_key = 'hu-mba' AND track.track_name = 'Finance' THEN 'HU_SRC_006'
        WHEN seed.program_key = 'hu-mba' AND track.track_name = 'Human Resources Management' THEN 'HU_SRC_007'
        WHEN seed.program_key = 'hu-mba' AND track.track_name = 'Management' THEN 'HU_SRC_008'
        WHEN seed.program_key = 'hu-mba' AND track.track_name = 'Marketing' THEN 'HU_SRC_009'
        WHEN seed.program_key = 'hu-ma-education' THEN 'HU_SRC_010'
        WHEN seed.program_key = 'hu-ma-psychology' THEN 'HU_SRC_011'
        ELSE (seed.source_ids->>0)
    END
    JOIN source src ON src.university_id = v_university_id AND src.url = hs.url
    ON CONFLICT (program_id, track_type, track_name) DO UPDATE SET
        track_order = EXCLUDED.track_order,
        is_primary = EXCLUDED.is_primary,
        source_id = EXCLUDED.source_id,
        notes = EXCLUDED.notes,
        updated_at = NOW();

    INSERT INTO graduate_program_source (university_id, program_id, source_id, source_role, source_order, evidence_text, notes)
    SELECT
        v_university_id,
        gp.id,
        src.id,
        CASE WHEN src_ord.ord = 1 THEN 'PRIMARY' ELSE 'SECONDARY' END,
        src_ord.ord,
        hs.title,
        NULL
    FROM hu_program_seed seed
    JOIN graduate_program gp ON gp.university_id = v_university_id AND gp.program_key = seed.program_key
    JOIN LATERAL jsonb_array_elements_text(seed.source_ids) WITH ORDINALITY AS src_ord(source_seed_id, ord) ON TRUE
    JOIN hu_source_seed hs ON hs.source_id = src_ord.source_seed_id
    JOIN source src ON src.university_id = v_university_id AND src.url = hs.url
    ON CONFLICT (program_id, source_id, source_role) DO UPDATE SET
        source_order = EXCLUDED.source_order,
        evidence_text = EXCLUDED.evidence_text,
        notes = EXCLUDED.notes,
        updated_at = NOW();

    INSERT INTO graduate_program_source (university_id, program_id, source_id, source_role, source_order, evidence_text, notes)
    SELECT
        v_university_id,
        gp.id,
        src.id,
        'TUITION',
        100 + src_ord.ord,
        'Tuition evidence source',
        NULL
    FROM hu_program_seed seed
    JOIN graduate_program gp ON gp.university_id = v_university_id AND gp.program_key = seed.program_key
    JOIN LATERAL jsonb_array_elements_text(seed.tuition->'source_ids') WITH ORDINALITY AS src_ord(source_seed_id, ord) ON TRUE
    JOIN hu_source_seed hs ON hs.source_id = src_ord.source_seed_id
    JOIN source src ON src.university_id = v_university_id AND src.url = hs.url
    ON CONFLICT (program_id, source_id, source_role) DO UPDATE SET
        source_order = EXCLUDED.source_order,
        evidence_text = EXCLUDED.evidence_text,
        notes = EXCLUDED.notes,
        updated_at = NOW();

    INSERT INTO graduate_tuition_rate (university_id, faculty_id, department_id, program_id, scope_level, record_key, academic_year, currency, billing_basis, amount, category, notes, source_id)
    SELECT
        v_university_id,
        fac.id,
        NULL,
        gp.id,
        'PROGRAM',
        'hu-tuition-' || seed.program_key,
        COALESCE(seed.tuition->>'academic_year', '2024-2025'),
        seed.tuition->>'currency',
        seed.tuition->>'billing_basis',
        (seed.tuition->>'amount')::NUMERIC(12,2),
        seed.tuition->>'category',
        seed.tuition->>'notes',
        src.id
    FROM hu_program_seed seed
    JOIN graduate_program gp ON gp.university_id = v_university_id AND gp.program_key = seed.program_key
    JOIN university_faculty fac ON fac.university_id = v_university_id AND fac.name = seed.faculty_name
    JOIN hu_source_seed hs ON hs.source_id = (seed.tuition->'source_ids'->>0)
    JOIN source src ON src.university_id = v_university_id AND src.url = hs.url
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

    INSERT INTO graduate_fee_item (university_id, faculty_id, department_id, program_id, scope_level, record_key, academic_year, fee_name, billing_basis, currency, amount, category, notes, source_id) VALUES
        (v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'hu-fee-application', NULL, 'Graduate application fee', 'FLAT_FEE', 'USD', 40, 'Admissions', 'Graduate application fee published in the official catalog financial section.', (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://www.haigazian.edu.lb/wp-content/uploads/2025/08/Catalog-2024-2026.pdf' LIMIT 1)),
        (v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'hu-fee-registration', NULL, 'Graduate registration fee', 'FLAT_FEE', 'USD', 50, 'Admissions', 'Registration fee for new and readmitted students published in the official catalog financial section.', (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://www.haigazian.edu.lb/wp-content/uploads/2025/08/Catalog-2024-2026.pdf' LIMIT 1)),
        (v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'hu-fee-reservation', NULL, 'Reservation fee', 'FLAT_FEE', 'USD', 250, 'Admissions', 'Reservation fee published in the official catalog financial section.', (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://www.haigazian.edu.lb/wp-content/uploads/2025/08/Catalog-2024-2026.pdf' LIMIT 1))
    ON CONFLICT (university_id, record_key) DO UPDATE SET
        fee_name = EXCLUDED.fee_name,
        billing_basis = EXCLUDED.billing_basis,
        currency = EXCLUDED.currency,
        amount = EXCLUDED.amount,
        category = EXCLUDED.category,
        notes = EXCLUDED.notes,
        source_id = EXCLUDED.source_id,
        updated_at = NOW();

    INSERT INTO graduate_admission_requirement (university_id, faculty_id, department_id, program_id, scope_level, record_key, requirement_type, requirement_text, comparison_operator, threshold_value, threshold_unit, is_required, notes, source_id) VALUES
        (v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'hu-admission-general', 'GENERAL', 'Graduate applicants review the program requirements, submit the application file and required documents, meet faculty-specific criteria, and complete any requested interview or entrance exam steps.', NULL, NULL, NULL, TRUE, 'Shared graduate admissions process from the university-level summary.', (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://www.haigazian.edu.lb/academics/graduate-admissions/' LIMIT 1)),
        (v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'hu-admission-english-mba', 'ENGLISH', 'MBA applicants must submit TOEFL 550 or equivalent.', '>=', 550, 'TOEFL', TRUE, 'Language requirement centralized at university scope.', (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://www.haigazian.edu.lb/wp-content/uploads/2025/08/Catalog-2024-2026.pdf' LIMIT 1)),
        (v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'hu-admission-english-ma', 'ENGLISH', 'MA applicants must submit TOEFL 600 or equivalent.', '>=', 600, 'TOEFL', TRUE, 'Language requirement centralized at university scope.', (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://www.haigazian.edu.lb/wp-content/uploads/2025/08/Catalog-2024-2026.pdf' LIMIT 1))
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

    INSERT INTO graduate_required_document (university_id, faculty_id, department_id, program_id, scope_level, record_key, document_type, document_name, is_optional, sort_order, notes, source_id) VALUES
        (v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'hu-doc-application-file', 'APPLICATION', 'Completed graduate application file', FALSE, 1, NULL, (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://www.haigazian.edu.lb/admissions/graduate-admissions/' LIMIT 1)),
        (v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'hu-doc-photo', 'IDENTITY', 'Passport-size photo', FALSE, 2, NULL, (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://www.haigazian.edu.lb/admissions/graduate-admissions/graduate-admissions-information/' LIMIT 1)),
        (v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'hu-doc-civil-id', 'IDENTITY', 'Civil ID copy for Lebanese applicants', FALSE, 3, NULL, (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://www.haigazian.edu.lb/admissions/graduate-admissions/graduate-admissions-information/' LIMIT 1)),
        (v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'hu-doc-passport-copy', 'IDENTITY', 'Passport copy for non-Lebanese applicants', FALSE, 4, NULL, (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://www.haigazian.edu.lb/admissions/graduate-admissions/graduate-admissions-information/' LIMIT 1)),
        (v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'hu-doc-bachelor-degree', 'ACADEMIC_RECORD', 'Certified bachelor''s degree or equivalent', FALSE, 5, NULL, (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://www.haigazian.edu.lb/academics/graduate-programs/' LIMIT 1)),
        (v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'hu-doc-transcript', 'ACADEMIC_RECORD', 'Official undergraduate transcript', FALSE, 6, NULL, (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://www.haigazian.edu.lb/academics/graduate-programs/' LIMIT 1)),
        (v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'hu-doc-supporting', 'OTHER', 'Other faculty-specific supporting documents if requested', FALSE, 7, NULL, (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://www.haigazian.edu.lb/wp-content/uploads/2025/08/Catalog-2024-2026.pdf' LIMIT 1))
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

    INSERT INTO graduate_admission_deadline (university_id, faculty_id, department_id, program_id, scope_level, record_key, academic_year, deadline_type, term, deadline_date, note, source_id) VALUES
        (v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'hu-deadline-fall', NULL, 'OTHER', 'Fall', NULL, 'Fall application deadline published in the official catalog.', (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://www.haigazian.edu.lb/wp-content/uploads/2025/08/Catalog-2024-2026.pdf' LIMIT 1)),
        (v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'hu-deadline-spring', NULL, 'OTHER', 'Spring', NULL, 'Spring application deadline published in the official catalog.', (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://www.haigazian.edu.lb/wp-content/uploads/2025/08/Catalog-2024-2026.pdf' LIMIT 1))
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

    INSERT INTO graduate_scholarship (university_id, faculty_id, department_id, program_id, scope_level, record_key, academic_year, name, description, coverage, amount, currency, notes, source_id) VALUES
        (v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'hu-scholarship-need-based', NULL, 'Need-based financial aid', 'Need-based financial assistance for eligible full-time graduate students.', NULL, NULL, NULL, 'Need-based aid published on the financial-aid pages.', (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://www.haigazian.edu.lb/admissions/financial-aid/' LIMIT 1)),
        (v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'hu-scholarship-academic', NULL, 'Academic scholarship', 'Academic scholarship for eligible students based on merit and university policy.', NULL, NULL, NULL, 'Academic scholarship published on the financial-aid pages.', (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://www.haigazian.edu.lb/admissions/financial-aid/types-of-financial-aid/' LIMIT 1))
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

    INSERT INTO graduate_financial_aid (university_id, faculty_id, department_id, program_id, scope_level, record_key, academic_year, name, description, amount, currency, notes, source_id) VALUES
        (v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'hu-financial-aid-graduate', NULL, 'Graduate financial aid', 'Full-time graduate students may apply for financial aid subject to need and availability.', NULL, NULL, 'Graduate aid published on the financial-aid FAQ and policy pages.', (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://www.haigazian.edu.lb/admissions/financial-aid/financial-aid-faqs/' LIMIT 1))
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

    INSERT INTO graduate_payment_plan (university_id, faculty_id, department_id, program_id, scope_level, record_key, academic_year, name, description, installments_count, down_payment_amount, down_payment_currency, interval_label, notes, source_id) VALUES
        (v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'hu-payment-plan-installments', NULL, 'Installment payment plan', 'Installment payments are allowed through a signed request/declaration form.', NULL, NULL, NULL, 'Term-based', 'Payment plan published on the tuition and fees pages.', (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://www.haigazian.edu.lb/admissions/tuition-fees/' LIMIT 1))
    ON CONFLICT (university_id, record_key) DO UPDATE SET
        faculty_id = EXCLUDED.faculty_id,
        department_id = EXCLUDED.department_id,
        program_id = EXCLUDED.program_id,
        scope_level = EXCLUDED.scope_level,
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

    INSERT INTO graduate_accreditation (university_id, faculty_id, department_id, program_id, scope_level, record_key, name, authority, status, valid_from, valid_until, notes, source_id) VALUES
        (v_university_id, (SELECT id FROM university_faculty WHERE university_id = v_university_id AND name = 'Faculty of Business Administration and Economics' LIMIT 1), NULL, NULL, 'FACULTY', 'hu-accreditation-business-iacbe', 'IACBE Accreditation', 'International Accreditation Council for Business Education', 'Accredited', NULL, NULL, 'Faculty-level accreditation published in the official HU catalog.', (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://www.haigazian.edu.lb/wp-content/uploads/2025/08/Catalog-2024-2026.pdf' LIMIT 1))
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
