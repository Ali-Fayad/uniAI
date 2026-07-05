-- RHU graduate data seed migration.
-- Idempotent import for the canonical RHU graduate dataset.

DO $$
DECLARE
    v_university_id BIGINT;
BEGIN

    INSERT INTO university (name, name_ar, acronym, country, city, latitude, longitude, campus_name, campus_type)
    SELECT 'Rafik Hariri University', NULL, 'RHU', 'Lebanon', NULL, NULL, NULL, NULL, NULL
    WHERE NOT EXISTS (SELECT 1 FROM university WHERE name = 'Rafik Hariri University');

    SELECT id INTO v_university_id FROM university WHERE name = 'Rafik Hariri University' ORDER BY id LIMIT 1;

    INSERT INTO degree_type (code, name) VALUES
        ('MASTER', 'Master'),
        ('PHD', 'Doctor of Philosophy'),
        ('DIPLOMA', 'Diploma'),
        ('CERTIFICATE', 'Certificate')
    ON CONFLICT (code) DO UPDATE SET name = EXCLUDED.name, updated_at = NOW();

    INSERT INTO language (name, code, native_name) VALUES
        ('English', 'en', 'English'),
        ('Arabic', 'ar', 'العربية'),
        ('French', 'fr', 'Français'),
        ('Multilingual', 'multi', 'Multilingual')
    ON CONFLICT (code) DO UPDATE SET name = EXCLUDED.name, native_name = EXCLUDED.native_name;

    CREATE TEMP TABLE rhu_source_seed (source_id TEXT PRIMARY KEY, title TEXT NOT NULL, url TEXT NOT NULL, source_type TEXT NOT NULL, accessed_at DATE, notes TEXT) ON COMMIT DROP;
    INSERT INTO rhu_source_seed SELECT source_id, page_title, url, source_type, date_accessed, notes FROM jsonb_to_recordset($RHU_SOURCES$[
  {
    "source_id": "RHU-SRC-001",
    "page_title": "RHU Home",
    "url": "https://www.rhu.edu.lb/",
    "date_accessed": "2026-07-04",
    "source_type": "html",
    "notes": "Official RHU homepage used for top-level navigation and current site structure."
  },
  {
    "source_id": "RHU-SRC-002",
    "page_title": "Graduate",
    "url": "https://www.rhu.edu.lb/admission/graduate",
    "date_accessed": "2026-07-04",
    "source_type": "html",
    "notes": "Graduate admissions landing page."
  },
  {
    "source_id": "RHU-SRC-003",
    "page_title": "Graduate Programs",
    "url": "https://www.rhu.edu.lb/admission/graduate/graduate-programs",
    "date_accessed": "2026-07-04",
    "source_type": "html",
    "notes": "Primary web page listing graduate programs and credits by college."
  },
  {
    "source_id": "RHU-SRC-004",
    "page_title": "Admission Requirements - Graduate",
    "url": "https://www.rhu.edu.lb/admission/graduate/admission-requirements",
    "date_accessed": "2026-07-04",
    "source_type": "html",
    "notes": "Graduate required documents and English entrance/competency requirement."
  },
  {
    "source_id": "RHU-SRC-005",
    "page_title": "Before Applying",
    "url": "https://www.rhu.edu.lb/prospective-students/before-applying",
    "date_accessed": "2026-07-04",
    "source_type": "html",
    "notes": "Application deadlines, application methods, enrollment next steps, payment and housing/transportation summary."
  },
  {
    "source_id": "RHU-SRC-006",
    "page_title": "How to Apply",
    "url": "https://www.rhu.edu.lb/prospective-students/before-applying/how-to-apply",
    "date_accessed": "2026-07-04",
    "source_type": "html",
    "notes": "In-person and online application instructions; links to graduate online application from official RHU page."
  },
  {
    "source_id": "RHU-SRC-007",
    "page_title": "Fees & Expenses",
    "url": "https://www.rhu.edu.lb/admission/fees-expenses",
    "date_accessed": "2026-07-04",
    "source_type": "html",
    "notes": "Tuition and fees index page linking current and prior fee PDFs."
  },
  {
    "source_id": "RHU-SRC-008",
    "page_title": "Tuition and Fees Academic Year 2025-2026",
    "url": "https://www.rhu.edu.lb/Library/Assets/Gallery/Files/tuitionandfees2526.pdf",
    "date_accessed": "2026-07-04",
    "source_type": "pdf",
    "notes": "Current tuition/fees PDF; includes graduate per-credit rates, service fees, deferred payment, late payment, transportation, and dormitory fees."
  },
  {
    "source_id": "RHU-SRC-009",
    "page_title": "Payment of Tuition and Fees",
    "url": "https://www.rhu.edu.lb/rhu-offices/finance-office/payment-of-tuition-and-fees",
    "date_accessed": "2026-07-04",
    "source_type": "html",
    "notes": "Finance Office payment page discovered for payment process coverage."
  },
  {
    "source_id": "RHU-SRC-010",
    "page_title": "Student Catalog",
    "url": "https://www.rhu.edu.lb/admission/student-catalog",
    "date_accessed": "2026-07-04",
    "source_type": "html",
    "notes": "Catalog index page linking RHU Graduate Catalog 2025-2026 and earlier catalogs."
  },
  {
    "source_id": "RHU-SRC-011",
    "page_title": "RHU Graduate Catalog 2025-2026",
    "url": "https://www.rhu.edu.lb/Library/Assets/Gallery/Files/RHU_Graduate_Catalog_2025-2026.pdf",
    "date_accessed": "2026-07-04",
    "source_type": "pdf",
    "notes": "Primary discovery source for graduate programs, admissions, application package, academic calendar, regulations, graduate assistantship, financial aid, MBA, and engineering MS requirements."
  },
  {
    "source_id": "RHU-SRC-012",
    "page_title": "RHU Graduate Catalog 2024-2025",
    "url": "https://www.rhu.edu.lb/Library/Assets/Gallery/Files/RHU_Graduate_Catalog_2024-2025.pdf",
    "date_accessed": "2026-07-04",
    "source_type": "pdf",
    "notes": "Prior graduate catalog used only as historical cross-check; current 2025-2026 catalog is preferred."
  },
  {
    "source_id": "RHU-SRC-013",
    "page_title": "College of Business Administration",
    "url": "https://www.rhu.edu.lb/academics/college-of-business-administration",
    "date_accessed": "2026-07-04",
    "source_type": "html",
    "notes": "College page listing MBA Program links and CBA context."
  },
  {
    "source_id": "RHU-SRC-014",
    "page_title": "MBA Program Description",
    "url": "https://www.rhu.edu.lb/home/academics/college-of-business-administration/majors-and-programs/mba-program/program-description",
    "date_accessed": "2026-07-04",
    "source_type": "html",
    "notes": "MBA program overview page."
  },
  {
    "source_id": "RHU-SRC-015",
    "page_title": "College of Engineering",
    "url": "https://www.rhu.edu.lb/academics/college-of-engineering",
    "date_accessed": "2026-07-04",
    "source_type": "html",
    "notes": "Engineering college page used to validate college structure and engineering program navigation."
  },
  {
    "source_id": "RHU-SRC-016",
    "page_title": "Master of Science in Computer and Communications Engineering",
    "url": "https://www.rhu.edu.lb/academics/college-of-engineering/departments-programs/electrical-and-computer-engineering/master-of-science-in-computer-and-communications-engineering",
    "date_accessed": "2026-07-04",
    "source_type": "html",
    "notes": "Engineering MS page discovered; content extraction appears course-heavy, so catalog remains primary for complete requirements."
  },
  {
    "source_id": "RHU-SRC-017",
    "page_title": "Master of Science in Mechanical Engineering",
    "url": "https://www.rhu.edu.lb/academics/college-of-engineering/departments-programs/mechanical-and-mechatronic-engineering/master-of-science-in-mechanical--engineering",
    "date_accessed": "2026-07-04",
    "source_type": "html",
    "notes": "Engineering MS page discovered; catalog remains primary for complete requirements."
  },
  {
    "source_id": "RHU-SRC-018",
    "page_title": "Academic Calendar",
    "url": "https://www.rhu.edu.lb/academics/academic-calendar",
    "date_accessed": "2026-07-04",
    "source_type": "html",
    "notes": "Academic calendar page; current graduate catalog also includes 2025-2026 calendar."
  }
]$RHU_SOURCES$::jsonb) AS x(source_id TEXT, page_title TEXT, url TEXT, source_type TEXT, date_accessed DATE, notes TEXT);

    INSERT INTO source (university_id, title, url, source_type, accessed_at)
    SELECT v_university_id, title, url, source_type, accessed_at
    FROM rhu_source_seed
    ON CONFLICT (university_id, url) DO UPDATE SET title = EXCLUDED.title, source_type = EXCLUDED.source_type, accessed_at = EXCLUDED.accessed_at, updated_at = NOW();

    CREATE TEMP TABLE rhu_faculty_seed (name TEXT PRIMARY KEY, short_name TEXT, faculty_type TEXT NOT NULL, official_url TEXT, notes TEXT) ON COMMIT DROP;
    INSERT INTO rhu_faculty_seed SELECT name, short_name, faculty_type, official_url, notes FROM jsonb_to_recordset($RHU_FACULTIES$[
  {
    "name": "College of Business Administration",
    "short_name": "CBA",
    "faculty_type": "SCHOOL",
    "official_url": "https://www.rhu.edu.lb/academics/college-of-business-administration",
    "notes": "Imported from the finalized RHU graduate inventory."
  },
  {
    "name": "College of Engineering",
    "short_name": "COE",
    "faculty_type": "SCHOOL",
    "official_url": "https://www.rhu.edu.lb/academics/college-of-engineering",
    "notes": "Imported from the finalized RHU graduate inventory."
  }
]$RHU_FACULTIES$::jsonb) AS x(name TEXT, short_name TEXT, faculty_type TEXT, official_url TEXT, notes TEXT);

    INSERT INTO university_faculty (university_id, name, short_name, faculty_type, official_url, notes)
    SELECT v_university_id, name, short_name, faculty_type, official_url, notes
    FROM rhu_faculty_seed
    ON CONFLICT (university_id, name) DO UPDATE SET short_name = EXCLUDED.short_name, faculty_type = EXCLUDED.faculty_type, official_url = EXCLUDED.official_url, notes = EXCLUDED.notes, updated_at = NOW();

    CREATE TEMP TABLE rhu_program_seed (
        id TEXT PRIMARY KEY,
        program_key TEXT NOT NULL,
        title TEXT NOT NULL,
        degree_type TEXT NOT NULL,
        faculty TEXT NOT NULL,
        official_program_url TEXT,
        source_ids JSONB NOT NULL,
        major_category TEXT,
        major TEXT,
        official_degree_name TEXT,
        thesis_or_non_thesis TEXT,
        credits INTEGER,
        program_description TEXT,
        notes TEXT,
        tuition_academic_year TEXT,
        tuition_currency TEXT,
        tuition_billing_basis TEXT,
        tuition_amount NUMERIC(12, 2),
        tuition_category TEXT,
        tuition_notes TEXT,
        tuition_source_ids JSONB NOT NULL
    ) ON COMMIT DROP;
    INSERT INTO rhu_program_seed SELECT id, program_key, title, degree_type, faculty, official_program_url, source_ids, major_category, major, official_degree_name, thesis_or_non_thesis, credits, program_description, notes, tuition_academic_year, tuition_currency, tuition_billing_basis, tuition_amount, tuition_category, tuition_notes, tuition_source_ids FROM jsonb_to_recordset($RHU_PROGRAMS$[
  {
    "id": "rhu-cba-master-business-administration",
    "program_key": "business-administration",
    "title": "Master of Business Administration",
    "degree_type": "MASTER",
    "faculty": "College of Business Administration",
    "department": null,
    "official_program_url": "https://www.rhu.edu.lb/home/academics/college-of-business-administration/majors-and-programs/mba-program/program-description",
    "source_ids": [
      "RHU-SRC-003",
      "RHU-SRC-011",
      "RHU-SRC-013",
      "RHU-SRC-014"
    ],
    "tuition": {
      "academic_year": "2025-2026",
      "currency": "USD",
      "billing_basis": "PER_CREDIT",
      "amount": 200,
      "category": "College of Business Administration",
      "notes": "Official 2025-2026 tuition and fees PDF lists graduate College of Business Administration tuition at USD 200 per credit hour.",
      "source_ids": [
        "RHU-SRC-008"
      ]
    },
    "program_description": "Graduate MBA program with two official emphases: General Business Management and Oil and Gas Management.",
    "credits": 36,
    "concentrations_or_tracks": [
      "General Business Management",
      "Oil and Gas Management"
    ],
    "notes": "RHU publishes the MBA as one graduate degree with General Business Management and Oil and Gas Management emphases; modeled as concentrations rather than separate graduate degree rows. The 2025-2026 Graduate Catalog is the canonical source.",
    "major_category": "Business Administration",
    "major": "Business Administration",
    "official_degree_name": "Master of Business Administration",
    "tuition_academic_year": "2025-2026",
    "tuition_currency": "USD",
    "tuition_billing_basis": "PER_CREDIT",
    "tuition_amount": 200,
    "tuition_category": "College of Business Administration",
    "tuition_notes": "Official 2025-2026 tuition and fees PDF lists graduate College of Business Administration tuition at USD 200 per credit hour.",
    "tuition_source_ids": [
      "RHU-SRC-008"
    ],
    "first_source_id": "RHU-SRC-003",
    "source_ids_json": [
      "RHU-SRC-003",
      "RHU-SRC-011",
      "RHU-SRC-013",
      "RHU-SRC-014"
    ]
  },
  {
    "id": "rhu-coe-master-biomedical-engineering",
    "program_key": "biomedical-engineering",
    "title": "Master of Science in Biomedical Engineering",
    "degree_type": "MASTER",
    "faculty": "College of Engineering",
    "department": null,
    "official_program_url": null,
    "source_ids": [
      "RHU-SRC-003",
      "RHU-SRC-011",
      "RHU-SRC-015"
    ],
    "tuition": {
      "academic_year": "2025-2026",
      "currency": "USD",
      "billing_basis": "PER_CREDIT",
      "amount": 280,
      "category": "College of Engineering",
      "notes": "Official 2025-2026 tuition and fees PDF lists graduate College of Engineering tuition at USD 280 per credit hour.",
      "source_ids": [
        "RHU-SRC-008"
      ]
    },
    "program_description": "Graduate engineering master’s program recognized in the official RHU Graduate Catalog.",
    "thesis_or_non_thesis": "THESIS_OR_NON_THESIS",
    "notes": "Official graduate catalog and graduate programs page identify Biomedical Engineering as an RHU master's program. No distinct public program page was recovered in this pass.",
    "major_category": "Engineering",
    "major": "Biomedical Engineering",
    "official_degree_name": "Master of Science in Biomedical Engineering",
    "tuition_academic_year": "2025-2026",
    "tuition_currency": "USD",
    "tuition_billing_basis": "PER_CREDIT",
    "tuition_amount": 280,
    "tuition_category": "College of Engineering",
    "tuition_notes": "Official 2025-2026 tuition and fees PDF lists graduate College of Engineering tuition at USD 280 per credit hour.",
    "tuition_source_ids": [
      "RHU-SRC-008"
    ],
    "first_source_id": "RHU-SRC-003",
    "source_ids_json": [
      "RHU-SRC-003",
      "RHU-SRC-011",
      "RHU-SRC-015"
    ]
  },
  {
    "id": "rhu-coe-master-civil-environmental-engineering",
    "program_key": "civil-environmental-engineering",
    "title": "Master of Science in Civil and Environmental Engineering",
    "degree_type": "MASTER",
    "faculty": "College of Engineering",
    "department": null,
    "official_program_url": null,
    "source_ids": [
      "RHU-SRC-003",
      "RHU-SRC-011",
      "RHU-SRC-015"
    ],
    "tuition": {
      "academic_year": "2025-2026",
      "currency": "USD",
      "billing_basis": "PER_CREDIT",
      "amount": 280,
      "category": "College of Engineering",
      "notes": "Official 2025-2026 tuition and fees PDF lists graduate College of Engineering tuition at USD 280 per credit hour.",
      "source_ids": [
        "RHU-SRC-008"
      ]
    },
    "program_description": "Graduate engineering master’s program recognized in the official RHU Graduate Catalog.",
    "thesis_or_non_thesis": "THESIS_OR_NON_THESIS",
    "notes": "Official graduate catalog and graduate programs page identify Civil and Environmental Engineering as an RHU master's program. No distinct public program page was recovered in this pass.",
    "major_category": "Engineering",
    "major": "Civil and Environmental Engineering",
    "official_degree_name": "Master of Science in Civil and Environmental Engineering",
    "tuition_academic_year": "2025-2026",
    "tuition_currency": "USD",
    "tuition_billing_basis": "PER_CREDIT",
    "tuition_amount": 280,
    "tuition_category": "College of Engineering",
    "tuition_notes": "Official 2025-2026 tuition and fees PDF lists graduate College of Engineering tuition at USD 280 per credit hour.",
    "tuition_source_ids": [
      "RHU-SRC-008"
    ],
    "first_source_id": "RHU-SRC-003",
    "source_ids_json": [
      "RHU-SRC-003",
      "RHU-SRC-011",
      "RHU-SRC-015"
    ]
  },
  {
    "id": "rhu-coe-master-computer-communications-engineering",
    "program_key": "computer-communications-engineering",
    "title": "Master of Science in Computer and Communications Engineering",
    "degree_type": "MASTER",
    "faculty": "College of Engineering",
    "department": null,
    "official_program_url": "https://www.rhu.edu.lb/academics/college-of-engineering/departments-programs/electrical-and-computer-engineering/master-of-science-in-computer-and-communications-engineering",
    "source_ids": [
      "RHU-SRC-003",
      "RHU-SRC-011",
      "RHU-SRC-015",
      "RHU-SRC-016"
    ],
    "tuition": {
      "academic_year": "2025-2026",
      "currency": "USD",
      "billing_basis": "PER_CREDIT",
      "amount": 280,
      "category": "College of Engineering",
      "notes": "Official 2025-2026 tuition and fees PDF lists graduate College of Engineering tuition at USD 280 per credit hour.",
      "source_ids": [
        "RHU-SRC-008"
      ]
    },
    "program_description": "Graduate engineering master’s program recognized in the official RHU Graduate Catalog.",
    "thesis_or_non_thesis": "THESIS_OR_NON_THESIS",
    "notes": "Official graduate programs page and catalog identify Computer and Communications Engineering as an RHU master's program. The recovered public program page is course-heavy, so the graduate catalog remains the canonical requirements source.",
    "major_category": "Engineering",
    "major": "Computer and Communications Engineering",
    "official_degree_name": "Master of Science in Computer and Communications Engineering",
    "tuition_academic_year": "2025-2026",
    "tuition_currency": "USD",
    "tuition_billing_basis": "PER_CREDIT",
    "tuition_amount": 280,
    "tuition_category": "College of Engineering",
    "tuition_notes": "Official 2025-2026 tuition and fees PDF lists graduate College of Engineering tuition at USD 280 per credit hour.",
    "tuition_source_ids": [
      "RHU-SRC-008"
    ],
    "first_source_id": "RHU-SRC-003",
    "source_ids_json": [
      "RHU-SRC-003",
      "RHU-SRC-011",
      "RHU-SRC-015",
      "RHU-SRC-016"
    ]
  },
  {
    "id": "rhu-coe-master-electrical-engineering",
    "program_key": "electrical-engineering",
    "title": "Master of Science in Electrical Engineering",
    "degree_type": "MASTER",
    "faculty": "College of Engineering",
    "department": null,
    "official_program_url": null,
    "source_ids": [
      "RHU-SRC-003",
      "RHU-SRC-011",
      "RHU-SRC-015"
    ],
    "tuition": {
      "academic_year": "2025-2026",
      "currency": "USD",
      "billing_basis": "PER_CREDIT",
      "amount": 280,
      "category": "College of Engineering",
      "notes": "Official 2025-2026 tuition and fees PDF lists graduate College of Engineering tuition at USD 280 per credit hour.",
      "source_ids": [
        "RHU-SRC-008"
      ]
    },
    "program_description": "Graduate engineering master’s program recognized in the official RHU Graduate Catalog.",
    "thesis_or_non_thesis": "THESIS_OR_NON_THESIS",
    "notes": "Official graduate catalog and graduate programs page identify Electrical Engineering as an RHU master's program. No distinct public program page was recovered in this pass.",
    "major_category": "Engineering",
    "major": "Electrical Engineering",
    "official_degree_name": "Master of Science in Electrical Engineering",
    "tuition_academic_year": "2025-2026",
    "tuition_currency": "USD",
    "tuition_billing_basis": "PER_CREDIT",
    "tuition_amount": 280,
    "tuition_category": "College of Engineering",
    "tuition_notes": "Official 2025-2026 tuition and fees PDF lists graduate College of Engineering tuition at USD 280 per credit hour.",
    "tuition_source_ids": [
      "RHU-SRC-008"
    ],
    "first_source_id": "RHU-SRC-003",
    "source_ids_json": [
      "RHU-SRC-003",
      "RHU-SRC-011",
      "RHU-SRC-015"
    ]
  },
  {
    "id": "rhu-coe-master-mechanical-engineering",
    "program_key": "mechanical-engineering",
    "title": "Master of Science in Mechanical Engineering",
    "degree_type": "MASTER",
    "faculty": "College of Engineering",
    "department": null,
    "official_program_url": "https://www.rhu.edu.lb/academics/college-of-engineering/departments-programs/mechanical-and-mechatronic-engineering/master-of-science-in-mechanical--engineering",
    "source_ids": [
      "RHU-SRC-003",
      "RHU-SRC-011",
      "RHU-SRC-015",
      "RHU-SRC-017"
    ],
    "tuition": {
      "academic_year": "2025-2026",
      "currency": "USD",
      "billing_basis": "PER_CREDIT",
      "amount": 280,
      "category": "College of Engineering",
      "notes": "Official 2025-2026 tuition and fees PDF lists graduate College of Engineering tuition at USD 280 per credit hour.",
      "source_ids": [
        "RHU-SRC-008"
      ]
    },
    "program_description": "Graduate engineering master’s program recognized in the official RHU Graduate Catalog.",
    "thesis_or_non_thesis": "THESIS_OR_NON_THESIS",
    "notes": "Official graduate programs page and catalog identify Mechanical Engineering as an RHU master's program. The recovered public program page is used as the official program URL.",
    "major_category": "Engineering",
    "major": "Mechanical Engineering",
    "official_degree_name": "Master of Science in Mechanical Engineering",
    "tuition_academic_year": "2025-2026",
    "tuition_currency": "USD",
    "tuition_billing_basis": "PER_CREDIT",
    "tuition_amount": 280,
    "tuition_category": "College of Engineering",
    "tuition_notes": "Official 2025-2026 tuition and fees PDF lists graduate College of Engineering tuition at USD 280 per credit hour.",
    "tuition_source_ids": [
      "RHU-SRC-008"
    ],
    "first_source_id": "RHU-SRC-003",
    "source_ids_json": [
      "RHU-SRC-003",
      "RHU-SRC-011",
      "RHU-SRC-015",
      "RHU-SRC-017"
    ]
  },
  {
    "id": "rhu-coe-master-mechatronics-engineering",
    "program_key": "mechatronics-engineering",
    "title": "Master of Science in Mechatronics Engineering",
    "degree_type": "MASTER",
    "faculty": "College of Engineering",
    "department": null,
    "official_program_url": null,
    "source_ids": [
      "RHU-SRC-003",
      "RHU-SRC-011",
      "RHU-SRC-015"
    ],
    "tuition": {
      "academic_year": "2025-2026",
      "currency": "USD",
      "billing_basis": "PER_CREDIT",
      "amount": 280,
      "category": "College of Engineering",
      "notes": "Official 2025-2026 tuition and fees PDF lists graduate College of Engineering tuition at USD 280 per credit hour.",
      "source_ids": [
        "RHU-SRC-008"
      ]
    },
    "program_description": "Graduate engineering master’s program recognized in the official RHU Graduate Catalog.",
    "thesis_or_non_thesis": "THESIS_OR_NON_THESIS",
    "notes": "Official graduate catalog and graduate programs page identify Mechatronics Engineering as an RHU master's program. No distinct public program page was recovered in this pass.",
    "major_category": "Engineering",
    "major": "Mechatronics Engineering",
    "official_degree_name": "Master of Science in Mechatronics Engineering",
    "tuition_academic_year": "2025-2026",
    "tuition_currency": "USD",
    "tuition_billing_basis": "PER_CREDIT",
    "tuition_amount": 280,
    "tuition_category": "College of Engineering",
    "tuition_notes": "Official 2025-2026 tuition and fees PDF lists graduate College of Engineering tuition at USD 280 per credit hour.",
    "tuition_source_ids": [
      "RHU-SRC-008"
    ],
    "first_source_id": "RHU-SRC-003",
    "source_ids_json": [
      "RHU-SRC-003",
      "RHU-SRC-011",
      "RHU-SRC-015"
    ]
  }
]$RHU_PROGRAMS$::jsonb) AS x(
        id TEXT,
        program_key TEXT,
        title TEXT,
        degree_type TEXT,
        faculty TEXT,
        official_program_url TEXT,
        source_ids JSONB,
        major_category TEXT,
        major TEXT,
        official_degree_name TEXT,
        thesis_or_non_thesis TEXT,
        credits INTEGER,
        program_description TEXT,
        notes TEXT,
        tuition_academic_year TEXT,
        tuition_currency TEXT,
        tuition_billing_basis TEXT,
        tuition_amount NUMERIC(12, 2),
        tuition_category TEXT,
        tuition_notes TEXT,
        tuition_source_ids JSONB
    );

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
        NULL,
        NULL,
        NULL,
        NULL,
        seed.program_description,
        seed.official_program_url,
        src.id,
        seed.notes
    FROM rhu_program_seed seed
    JOIN university_faculty fac ON fac.university_id = v_university_id AND fac.name = seed.faculty
    JOIN degree_type dt ON dt.code = seed.degree_type
    JOIN LATERAL (
        SELECT s.id
        FROM jsonb_array_elements_text(seed.source_ids) WITH ORDINALITY AS src_seed(source_seed_id, ord)
        JOIN rhu_source_seed ss ON ss.source_id = src_seed.source_seed_id
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

    CREATE TEMP TABLE rhu_track_seed (
        program_key TEXT NOT NULL,
        track_type TEXT NOT NULL,
        track_name TEXT NOT NULL,
        track_order INTEGER NOT NULL,
        is_primary BOOLEAN NOT NULL,
        source_id TEXT NOT NULL
    ) ON COMMIT DROP;
    INSERT INTO rhu_track_seed (program_key, track_type, track_name, track_order, is_primary, source_id) VALUES
        ('business-administration', 'CONCENTRATION', 'General Business Management', 1, TRUE, 'RHU-SRC-011'),
        ('business-administration', 'CONCENTRATION', 'Oil and Gas Management', 2, FALSE, 'RHU-SRC-011');

    INSERT INTO graduate_program_track (university_id, program_id, track_type, track_name, track_order, is_primary, description, source_id, notes)
    SELECT
        v_university_id,
        gp.id,
        t.track_type,
        t.track_name,
        t.track_order,
        t.is_primary,
        NULL,
        s.id,
        'Imported from concentrations_or_tracks.'
    FROM rhu_track_seed t
    JOIN graduate_program gp ON gp.university_id = v_university_id AND gp.program_key = t.program_key
    JOIN rhu_source_seed ss ON ss.source_id = t.source_id
    JOIN source s ON s.university_id = v_university_id AND s.url = ss.url
    ON CONFLICT (program_id, track_type, track_name) DO UPDATE SET track_order = EXCLUDED.track_order, is_primary = EXCLUDED.is_primary, source_id = EXCLUDED.source_id, notes = EXCLUDED.notes, updated_at = NOW();

    INSERT INTO graduate_tuition_rate (university_id, faculty_id, department_id, program_id, scope_level, record_key, academic_year, currency, billing_basis, amount, category, notes, source_id)
    SELECT
        v_university_id,
        fac.id,
        NULL,
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
    FROM rhu_program_seed seed
    JOIN graduate_program gp ON gp.university_id = v_university_id AND gp.program_key = seed.program_key
    JOIN university_faculty fac ON fac.id = gp.faculty_id
    JOIN LATERAL (
        SELECT s.id
        FROM jsonb_array_elements_text(seed.tuition_source_ids) WITH ORDINALITY AS src_seed(source_seed_id, ord)
        JOIN rhu_source_seed ss ON ss.source_id = src_seed.source_seed_id
        JOIN source s ON s.university_id = v_university_id AND s.url = ss.url
        ORDER BY src_seed.ord
        LIMIT 1
    ) tuition_src ON TRUE
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

    CREATE TEMP TABLE rhu_fee_seed (record_key TEXT PRIMARY KEY, academic_year TEXT, fee_name TEXT, billing_basis TEXT, currency TEXT, amount NUMERIC(12, 2), category TEXT, notes TEXT, source_id TEXT) ON COMMIT DROP;
    INSERT INTO rhu_fee_seed SELECT record_key, academic_year, fee_name, billing_basis, currency, amount, category, notes, source_id FROM jsonb_to_recordset($RHU_FEES$[
  {
    "record_key": "rhu-fee-application",
    "academic_year": null,
    "fee_name": "Admission File Fee",
    "billing_basis": "FLAT_FEE",
    "currency": "USD",
    "amount": 30,
    "category": "Admissions",
    "notes": "Non-refundable graduate admission file fee.",
    "source_id": "RHU-SRC-005"
  },
  {
    "record_key": "rhu-fee-registration",
    "academic_year": null,
    "fee_name": "Registration Fee",
    "billing_basis": "FLAT_FEE",
    "currency": "USD",
    "amount": 200,
    "category": "Registration",
    "notes": "First payment includes a USD 200 registration fee.",
    "source_id": "RHU-SRC-008"
  },
  {
    "record_key": "rhu-fee-operational",
    "academic_year": null,
    "fee_name": "Operational Fee",
    "billing_basis": "PER_SEMESTER",
    "currency": "USD",
    "amount": 300,
    "category": "Student Services",
    "notes": "The current fee PDF lists a USD 300 operational fee per semester.",
    "source_id": "RHU-SRC-008"
  },
  {
    "record_key": "rhu-fee-nssf-membership",
    "academic_year": null,
    "fee_name": "NSSF Membership Fee",
    "billing_basis": "PER_ACADEMIC_YEAR",
    "currency": "USD",
    "amount": 94,
    "category": "Student Services",
    "notes": "The current fee PDF lists an NSSF membership fee of USD 94 per academic year.",
    "source_id": "RHU-SRC-008"
  }
]$RHU_FEES$::jsonb) AS x(record_key TEXT, academic_year TEXT, fee_name TEXT, billing_basis TEXT, currency TEXT, amount NUMERIC(12, 2), category TEXT, notes TEXT, source_id TEXT);
    INSERT INTO graduate_fee_item (university_id, faculty_id, department_id, program_id, scope_level, record_key, academic_year, fee_name, billing_basis, currency, amount, category, notes, source_id)
    SELECT v_university_id, NULL, NULL, NULL, 'UNIVERSITY', f.record_key, f.academic_year, f.fee_name, f.billing_basis, f.currency, f.amount, f.category, f.notes, s.id
    FROM rhu_fee_seed f
    JOIN rhu_source_seed ss ON ss.source_id = f.source_id
    JOIN source s ON s.university_id = v_university_id AND s.url = ss.url
    ON CONFLICT (university_id, record_key) DO UPDATE SET academic_year = EXCLUDED.academic_year, fee_name = EXCLUDED.fee_name, billing_basis = EXCLUDED.billing_basis, currency = EXCLUDED.currency, amount = EXCLUDED.amount, category = EXCLUDED.category, notes = EXCLUDED.notes, source_id = EXCLUDED.source_id, updated_at = NOW();

    CREATE TEMP TABLE rhu_admission_requirement_seed (record_key TEXT PRIMARY KEY, requirement_type TEXT, requirement_text TEXT, comparison_operator TEXT, threshold_value NUMERIC(12, 2), threshold_unit TEXT, is_required BOOLEAN, notes TEXT, source_id TEXT) ON COMMIT DROP;
    INSERT INTO rhu_admission_requirement_seed SELECT record_key, requirement_type, requirement_text, comparison_operator, threshold_value, threshold_unit, is_required, notes, source_id FROM jsonb_to_recordset($RHU_ADMISSION_REQUIREMENTS$[
  {
    "record_key": "rhu-admission-general",
    "requirement_type": "GENERAL",
    "requirement_text": "Applicants must hold a recognized bachelor's degree or equivalent and satisfy RHU's graduate admission review.",
    "comparison_operator": null,
    "threshold_value": null,
    "threshold_unit": null,
    "is_required": true,
    "notes": "General graduate admission requirement from the official Graduate Catalog and graduate admission pages.",
    "source_id": "RHU-SRC-004"
  },
  {
    "record_key": "rhu-admission-english",
    "requirement_type": "ENGLISH",
    "requirement_text": "Applicants must present the RHU English Entrance Exam or another standardized English competency exam accepted by RHU.",
    "comparison_operator": null,
    "threshold_value": null,
    "threshold_unit": null,
    "is_required": true,
    "notes": "English competency requirement published in the graduate admission sources.",
    "source_id": "RHU-SRC-004"
  }
]$RHU_ADMISSION_REQUIREMENTS$::jsonb) AS x(record_key TEXT, requirement_type TEXT, requirement_text TEXT, comparison_operator TEXT, threshold_value NUMERIC(12, 2), threshold_unit TEXT, is_required BOOLEAN, notes TEXT, source_id TEXT);
    INSERT INTO graduate_admission_requirement (university_id, faculty_id, department_id, program_id, scope_level, record_key, requirement_type, requirement_text, comparison_operator, threshold_value, threshold_unit, is_required, notes, source_id)
    SELECT v_university_id, NULL, NULL, NULL, 'UNIVERSITY', a.record_key, a.requirement_type, a.requirement_text, a.comparison_operator, a.threshold_value, a.threshold_unit, a.is_required, a.notes, s.id
    FROM rhu_admission_requirement_seed a
    JOIN rhu_source_seed ss ON ss.source_id = a.source_id
    JOIN source s ON s.university_id = v_university_id AND s.url = ss.url
    ON CONFLICT (university_id, record_key) DO UPDATE SET requirement_type = EXCLUDED.requirement_type, requirement_text = EXCLUDED.requirement_text, comparison_operator = EXCLUDED.comparison_operator, threshold_value = EXCLUDED.threshold_value, threshold_unit = EXCLUDED.threshold_unit, is_required = EXCLUDED.is_required, notes = EXCLUDED.notes, source_id = EXCLUDED.source_id, updated_at = NOW();

    CREATE TEMP TABLE rhu_required_document_seed (record_key TEXT PRIMARY KEY, document_type TEXT, document_name TEXT, is_optional BOOLEAN, sort_order INTEGER, notes TEXT, source_id TEXT) ON COMMIT DROP;
    INSERT INTO rhu_required_document_seed SELECT record_key, document_type, document_name, is_optional, sort_order, notes, source_id FROM jsonb_to_recordset($RHU_REQUIRED_DOCUMENTS$[
  {
    "record_key": "rhu-doc-undergraduate-degree-equivalence",
    "document_type": "ACADEMIC",
    "document_name": "Certified copy of undergraduate degree and equivalence",
    "is_optional": false,
    "sort_order": 1,
    "notes": "Equivalent recognition from the Ministry of Higher Education where applicable.",
    "source_id": "RHU-SRC-004"
  },
  {
    "record_key": "rhu-doc-official-transcripts",
    "document_type": "ACADEMIC",
    "document_name": "Official transcripts attested by the Ministry of Higher Education",
    "is_optional": false,
    "sort_order": 2,
    "notes": null,
    "source_id": "RHU-SRC-004"
  },
  {
    "record_key": "rhu-doc-high-school-certificate",
    "document_type": "ACADEMIC",
    "document_name": "Certified copy of Lebanese Official High School Certificate or equivalent",
    "is_optional": false,
    "sort_order": 3,
    "notes": null,
    "source_id": "RHU-SRC-004"
  },
  {
    "record_key": "rhu-doc-english-competency",
    "document_type": "ENGLISH",
    "document_name": "RHU English Entrance Exam or standardized English competency exam",
    "is_optional": false,
    "sort_order": 4,
    "notes": null,
    "source_id": "RHU-SRC-004"
  },
  {
    "record_key": "rhu-doc-recommendation-letters",
    "document_type": "RECOMMENDATION",
    "document_name": "Recommendation letters",
    "is_optional": false,
    "sort_order": 5,
    "notes": null,
    "source_id": "RHU-SRC-004"
  },
  {
    "record_key": "rhu-doc-additional-package-items",
    "document_type": "OTHER",
    "document_name": "Additional documents listed in the Graduate Catalog and application package",
    "is_optional": false,
    "sort_order": 6,
    "notes": null,
    "source_id": "RHU-SRC-011"
  }
]$RHU_REQUIRED_DOCUMENTS$::jsonb) AS x(record_key TEXT, document_type TEXT, document_name TEXT, is_optional BOOLEAN, sort_order INTEGER, notes TEXT, source_id TEXT);
    INSERT INTO graduate_required_document (university_id, faculty_id, department_id, program_id, scope_level, record_key, document_type, document_name, is_optional, sort_order, notes, source_id)
    SELECT v_university_id, NULL, NULL, NULL, 'UNIVERSITY', d.record_key, d.document_type, d.document_name, d.is_optional, d.sort_order, d.notes, s.id
    FROM rhu_required_document_seed d
    JOIN rhu_source_seed ss ON ss.source_id = d.source_id
    JOIN source s ON s.university_id = v_university_id AND s.url = ss.url
    ON CONFLICT (university_id, record_key) DO UPDATE SET document_type = EXCLUDED.document_type, document_name = EXCLUDED.document_name, is_optional = EXCLUDED.is_optional, sort_order = EXCLUDED.sort_order, notes = EXCLUDED.notes, source_id = EXCLUDED.source_id, updated_at = NOW();

    CREATE TEMP TABLE rhu_deadline_seed (record_key TEXT PRIMARY KEY, academic_year TEXT, deadline_type TEXT, term TEXT, deadline_date DATE, note TEXT, source_id TEXT) ON COMMIT DROP;
    INSERT INTO rhu_deadline_seed SELECT record_key, academic_year, deadline_type, term, deadline_date, note, source_id FROM jsonb_to_recordset($RHU_DEADLINES$[
  {
    "record_key": "rhu-deadline-fall",
    "academic_year": "2025-2026",
    "deadline_type": "FINAL",
    "term": "Fall Semester",
    "deadline_date": null,
    "note": "Applications must be submitted no later than August 25 for Fall Semester.",
    "source_id": "RHU-SRC-005"
  },
  {
    "record_key": "rhu-deadline-spring",
    "academic_year": "2025-2026",
    "deadline_type": "FINAL",
    "term": "Spring Semester",
    "deadline_date": null,
    "note": "Applications must be submitted no later than December 25 for Spring Semester.",
    "source_id": "RHU-SRC-005"
  }
]$RHU_DEADLINES$::jsonb) AS x(record_key TEXT, academic_year TEXT, deadline_type TEXT, term TEXT, deadline_date DATE, note TEXT, source_id TEXT);
    INSERT INTO graduate_admission_deadline (university_id, faculty_id, department_id, program_id, scope_level, record_key, academic_year, deadline_type, term, deadline_date, note, source_id)
    SELECT v_university_id, NULL, NULL, NULL, 'UNIVERSITY', d.record_key, d.academic_year, d.deadline_type, d.term, d.deadline_date, d.note, s.id
    FROM rhu_deadline_seed d
    JOIN rhu_source_seed ss ON ss.source_id = d.source_id
    JOIN source s ON s.university_id = v_university_id AND s.url = ss.url
    ON CONFLICT (university_id, record_key) DO UPDATE SET academic_year = EXCLUDED.academic_year, deadline_type = EXCLUDED.deadline_type, term = EXCLUDED.term, deadline_date = EXCLUDED.deadline_date, note = EXCLUDED.note, source_id = EXCLUDED.source_id, updated_at = NOW();

    CREATE TEMP TABLE rhu_scholarship_seed (record_key TEXT PRIMARY KEY, academic_year TEXT, name TEXT, description TEXT, coverage TEXT, amount NUMERIC(12, 2), currency TEXT, notes TEXT, source_id TEXT) ON COMMIT DROP;
    INSERT INTO rhu_scholarship_seed SELECT record_key, academic_year, name, description, coverage, amount, currency, notes, source_id FROM jsonb_to_recordset($RHU_SCHOLARSHIPS$[
  {
    "record_key": "rhu-scholarship-financial-aid-discounts",
    "academic_year": "2025-2026",
    "name": "Financial-Aid Discounts",
    "description": "Discounts and reduced charges referenced by the Financial Aid Office.",
    "coverage": "Partial",
    "amount": null,
    "currency": null,
    "notes": "Published in the financial-aid sources and tied to RHU aid conditions.",
    "source_id": "RHU-SRC-005"
  },
  {
    "record_key": "rhu-scholarship-agreement-reductions",
    "academic_year": "2025-2026",
    "name": "Agreement-Based Reductions",
    "description": "Reductions granted under RHU agreements or arrangements.",
    "coverage": "Conditional",
    "amount": null,
    "currency": null,
    "notes": "Refer to the Financial Aid Office and Graduate Catalog for conditions.",
    "source_id": "RHU-SRC-011"
  },
  {
    "record_key": "rhu-scholarship-graduate-assistantship",
    "academic_year": "2025-2026",
    "name": "Graduate Assistantship",
    "description": "Limited merit-based graduate assistantships offered every term excluding summer.",
    "coverage": "Limited",
    "amount": null,
    "currency": null,
    "notes": "Assistantship language is published in the Graduate Catalog.",
    "source_id": "RHU-SRC-011"
  }
]$RHU_SCHOLARSHIPS$::jsonb) AS x(record_key TEXT, academic_year TEXT, name TEXT, description TEXT, coverage TEXT, amount NUMERIC(12, 2), currency TEXT, notes TEXT, source_id TEXT);
    INSERT INTO graduate_scholarship (university_id, faculty_id, department_id, program_id, scope_level, record_key, academic_year, name, description, coverage, amount, currency, notes, source_id)
    SELECT v_university_id, NULL, NULL, NULL, 'UNIVERSITY', sdata.record_key, sdata.academic_year, sdata.name, sdata.description, sdata.coverage, sdata.amount, sdata.currency, sdata.notes, src.id
    FROM rhu_scholarship_seed sdata
    JOIN rhu_source_seed ss ON ss.source_id = sdata.source_id
    JOIN source src ON src.university_id = v_university_id AND src.url = ss.url
    ON CONFLICT (university_id, record_key) DO UPDATE SET academic_year = EXCLUDED.academic_year, name = EXCLUDED.name, description = EXCLUDED.description, coverage = EXCLUDED.coverage, amount = EXCLUDED.amount, currency = EXCLUDED.currency, notes = EXCLUDED.notes, source_id = EXCLUDED.source_id, updated_at = NOW();

    CREATE TEMP TABLE rhu_fin_aid_seed (record_key TEXT PRIMARY KEY, academic_year TEXT, name TEXT, description TEXT, amount NUMERIC(12, 2), currency TEXT, notes TEXT, source_id TEXT) ON COMMIT DROP;
    INSERT INTO rhu_fin_aid_seed SELECT record_key, academic_year, name, description, amount, currency, notes, source_id FROM jsonb_to_recordset($RHU_FIN_AID$[
  {
    "record_key": "rhu-fin-aid-need-based",
    "academic_year": "2025-2026",
    "name": "Need-Based Financial Aid",
    "description": "Need-based financial aid handled by the Financial Aid Office.",
    "amount": null,
    "currency": null,
    "notes": "The official aid page confirms partial aid for eligible candidates.",
    "source_id": "RHU-SRC-005"
  },
  {
    "record_key": "rhu-fin-aid-assistantship-support",
    "academic_year": "2025-2026",
    "name": "Graduate Assistantship Support",
    "description": "Limited merit-based graduate assistantships.",
    "amount": null,
    "currency": null,
    "notes": "Published in the Graduate Catalog.",
    "source_id": "RHU-SRC-011"
  },
  {
    "record_key": "rhu-fin-aid-eligibility-conditions",
    "academic_year": "2025-2026",
    "name": "Aid Eligibility Conditions",
    "description": "Aid remains subject to academic and registration conditions.",
    "amount": null,
    "currency": null,
    "notes": "Conditions include academic standing and registration expectations from the official sources.",
    "source_id": "RHU-SRC-011"
  }
]$RHU_FIN_AID$::jsonb) AS x(record_key TEXT, academic_year TEXT, name TEXT, description TEXT, amount NUMERIC(12, 2), currency TEXT, notes TEXT, source_id TEXT);
    INSERT INTO graduate_financial_aid (university_id, faculty_id, department_id, program_id, scope_level, record_key, academic_year, name, description, amount, currency, notes, source_id)
    SELECT v_university_id, NULL, NULL, NULL, 'UNIVERSITY', ad.record_key, ad.academic_year, ad.name, ad.description, ad.amount, ad.currency, ad.notes, src.id
    FROM rhu_fin_aid_seed ad
    JOIN rhu_source_seed ss ON ss.source_id = ad.source_id
    JOIN source src ON src.university_id = v_university_id AND src.url = ss.url
    ON CONFLICT (university_id, record_key) DO UPDATE SET academic_year = EXCLUDED.academic_year, name = EXCLUDED.name, description = EXCLUDED.description, amount = EXCLUDED.amount, currency = EXCLUDED.currency, notes = EXCLUDED.notes, source_id = EXCLUDED.source_id, updated_at = NOW();

    CREATE TEMP TABLE rhu_payment_plan_seed (record_key TEXT PRIMARY KEY, academic_year TEXT, name TEXT, description TEXT, installments_count INTEGER, down_payment_amount NUMERIC(12, 2), down_payment_currency TEXT, interval_label TEXT, notes TEXT, source_id TEXT) ON COMMIT DROP;
    INSERT INTO rhu_payment_plan_seed SELECT record_key, academic_year, name, description, installments_count, down_payment_amount, down_payment_currency, interval_label, notes, source_id FROM jsonb_to_recordset($RHU_PAYMENT_PLANS$[
  {
    "record_key": "rhu-payment-plan-deferred-contract",
    "academic_year": "2025-2026",
    "name": "Deferred Payment by Contract",
    "description": "Deferred payment by signed contract.",
    "installments_count": null,
    "down_payment_amount": null,
    "down_payment_currency": null,
    "interval_label": "As contracted",
    "notes": "Deferred payment is stated in the fee and finance sources.",
    "source_id": "RHU-SRC-008"
  },
  {
    "record_key": "rhu-payment-plan-fall-spring",
    "academic_year": "2025-2026",
    "name": "Fall and Spring Installment Plan",
    "description": "Four installments for Fall and Spring semesters.",
    "installments_count": 4,
    "down_payment_amount": null,
    "down_payment_currency": null,
    "interval_label": "Semester",
    "notes": "The current fee PDF states that tuition fees may be scheduled over four installments for Fall and Spring.",
    "source_id": "RHU-SRC-008"
  },
  {
    "record_key": "rhu-payment-plan-summer",
    "academic_year": "2025-2026",
    "name": "Summer Installment Plan",
    "description": "Two installments for Summer session.",
    "installments_count": 2,
    "down_payment_amount": null,
    "down_payment_currency": null,
    "interval_label": "Session",
    "notes": "The current fee PDF states that summer fees may be scheduled over two installments.",
    "source_id": "RHU-SRC-008"
  }
]$RHU_PAYMENT_PLANS$::jsonb) AS x(record_key TEXT, academic_year TEXT, name TEXT, description TEXT, installments_count INTEGER, down_payment_amount NUMERIC(12, 2), down_payment_currency TEXT, interval_label TEXT, notes TEXT, source_id TEXT);
    INSERT INTO graduate_payment_plan (university_id, faculty_id, department_id, program_id, scope_level, record_key, academic_year, name, description, installments_count, down_payment_amount, down_payment_currency, interval_label, notes, source_id)
    SELECT v_university_id, NULL, NULL, NULL, 'UNIVERSITY', pp.record_key, pp.academic_year, pp.name, pp.description, pp.installments_count, pp.down_payment_amount, pp.down_payment_currency, pp.interval_label, pp.notes, src.id
    FROM rhu_payment_plan_seed pp
    JOIN rhu_source_seed ss ON ss.source_id = pp.source_id
    JOIN source src ON src.university_id = v_university_id AND src.url = ss.url
    ON CONFLICT (university_id, record_key) DO UPDATE SET academic_year = EXCLUDED.academic_year, name = EXCLUDED.name, description = EXCLUDED.description, installments_count = EXCLUDED.installments_count, down_payment_amount = EXCLUDED.down_payment_amount, down_payment_currency = EXCLUDED.down_payment_currency, interval_label = EXCLUDED.interval_label, notes = EXCLUDED.notes, source_id = EXCLUDED.source_id, updated_at = NOW();

    INSERT INTO graduate_program_source (university_id, program_id, source_id, source_role, source_order, evidence_text, notes)
    SELECT v_university_id, gp.id, s.id, CASE WHEN src.ord = 1 THEN 'PRIMARY' ELSE 'SECONDARY' END, src.ord, ss.title, NULL
    FROM rhu_program_seed seed
    JOIN graduate_program gp ON gp.university_id = v_university_id AND gp.program_key = seed.program_key
    JOIN LATERAL jsonb_array_elements_text(seed.source_ids) WITH ORDINALITY AS src(source_seed_id, ord) ON TRUE
    JOIN rhu_source_seed ss ON ss.source_id = src.source_seed_id
    JOIN source s ON s.university_id = v_university_id AND s.url = ss.url
    ON CONFLICT (program_id, source_id, source_role) DO UPDATE SET source_order = EXCLUDED.source_order, evidence_text = EXCLUDED.evidence_text, notes = EXCLUDED.notes, updated_at = NOW();

    INSERT INTO graduate_program_source (university_id, program_id, source_id, source_role, source_order, evidence_text, notes)
    SELECT v_university_id, gp.id, s.id, 'TUITION', 1, 'Tuition source', NULL
    FROM rhu_program_seed seed
    JOIN graduate_program gp ON gp.university_id = v_university_id AND gp.program_key = seed.program_key
    JOIN LATERAL jsonb_array_elements_text(seed.tuition_source_ids) WITH ORDINALITY AS src(source_seed_id, ord) ON TRUE
    JOIN rhu_source_seed ss ON ss.source_id = src.source_seed_id
    JOIN source s ON s.university_id = v_university_id AND s.url = ss.url
    ON CONFLICT (program_id, source_id, source_role) DO UPDATE SET source_order = EXCLUDED.source_order, evidence_text = EXCLUDED.evidence_text, notes = EXCLUDED.notes, updated_at = NOW();

END $$;
