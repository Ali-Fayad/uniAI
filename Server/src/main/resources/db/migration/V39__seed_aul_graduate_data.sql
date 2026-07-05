-- AUL graduate data seed migration.
-- Idempotent import for the canonical AUL graduate dataset.

DO $$
DECLARE
    v_university_id BIGINT;
    v_language_en_id BIGINT;
BEGIN

    INSERT INTO university (name, name_ar, acronym, country, city, latitude, longitude, campus_name, campus_type)
    SELECT 'Arts, Sciences and Technology University in Lebanon', NULL, 'AUL', 'Lebanon', NULL, NULL, NULL, NULL, NULL
    WHERE NOT EXISTS (SELECT 1 FROM university WHERE name = 'Arts, Sciences and Technology University in Lebanon');

    SELECT id INTO v_university_id FROM university WHERE name = 'Arts, Sciences and Technology University in Lebanon' ORDER BY id LIMIT 1;

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

    CREATE TEMP TABLE aul_source_seed (source_id TEXT PRIMARY KEY, title TEXT NOT NULL, url TEXT NOT NULL, source_type TEXT NOT NULL, accessed_at DATE, notes TEXT) ON COMMIT DROP;
    INSERT INTO aul_source_seed SELECT source_id, title, url, source_type, accessed_at, notes FROM jsonb_to_recordset($AUL_SOURCES$[
  {
    "source_id": "AUL-001",
    "title": "AUL University | Your Higher Education",
    "url": "https://aul.edu.lb/",
    "source_type": "OFFICIAL_PAGE",
    "accessed_at": "2026-07-05",
    "notes": "Official homepage used to identify official navigation, faculties, admissions, research, and academic pages."
  },
  {
    "source_id": "AUL-002",
    "title": "ADMISSIONS | AUL University",
    "url": "https://aul.edu.lb/?page_id=78",
    "source_type": "OFFICIAL_PAGE",
    "accessed_at": "2026-07-05",
    "notes": "Admissions process, required documents, graduate-program document requirement, financial aid, scholarships, and pre-application major list."
  },
  {
    "source_id": "AUL-003",
    "title": "BUSINESS | AUL University",
    "url": "https://aul.edu.lb/?page_id=142",
    "source_type": "OFFICIAL_PAGE",
    "accessed_at": "2026-07-05",
    "notes": "Faculty of Business page containing the Master of Business Administration (MBA)."
  },
  {
    "source_id": "AUL-004",
    "title": "ENGINEERING | AUL University",
    "url": "https://aul.edu.lb/?page_id=144",
    "source_type": "OFFICIAL_PAGE",
    "accessed_at": "2026-07-05",
    "notes": "Faculty of Engineering page containing the Master of Science in Engineering / Master in CCE."
  },
  {
    "source_id": "AUL-005",
    "title": "SCIENCES & FINE ARTS | AUL University",
    "url": "https://aul.edu.lb/?page_id=146",
    "source_type": "OFFICIAL_PAGE",
    "accessed_at": "2026-07-05",
    "notes": "Faculty of Sciences & Fine Arts page containing the Master of Science / Master in CSC."
  },
  {
    "source_id": "AUL-006",
    "title": "Research | AUL University",
    "url": "https://aul.edu.lb/?page_id=1001",
    "source_type": "OFFICIAL_PAGE",
    "accessed_at": "2026-07-05",
    "notes": "Official research page mentioning master and PhD candidates and research features; no official PhD program page found."
  },
  {
    "source_id": "AUL-007",
    "title": "ACADEMICS | AUL University",
    "url": "https://aul.edu.lb/?page_id=76",
    "source_type": "OFFICIAL_PAGE",
    "accessed_at": "2026-07-05",
    "notes": "Academics page and academic calendar navigation; continuing education content treated as out of scope."
  },
  {
    "source_id": "AUL-008",
    "title": "ABOUT AUL | AUL University",
    "url": "https://aul.edu.lb/?page_id=80",
    "source_type": "OFFICIAL_PAGE",
    "accessed_at": "2026-07-05",
    "notes": "Official institutional page used for accreditation and faculty navigation context."
  },
  {
    "source_id": "AUL-009",
    "title": "Student Work Program Handbook (Assistantship)",
    "url": "https://www.aul.edu.lb/wp-content/uploads/2025/12/Student-Work-Program-Handbook-2025.pdf",
    "source_type": "OFFICIAL_PDF",
    "accessed_at": "2026-07-05",
    "notes": "Official AUL-hosted PDF mentioning graduate students in MS or MBA programs are eligible for student work/assistantship."
  }
]$AUL_SOURCES$) AS x(source_id TEXT, title TEXT, url TEXT, source_type TEXT, accessed_at DATE, notes TEXT);
    INSERT INTO source (university_id, title, url, source_type, accessed_at) SELECT v_university_id, title, url, source_type, accessed_at FROM aul_source_seed ON CONFLICT (university_id, url) DO UPDATE SET title = EXCLUDED.title, source_type = EXCLUDED.source_type, accessed_at = EXCLUDED.accessed_at, updated_at = NOW();

    CREATE TEMP TABLE aul_faculty_seed (name TEXT PRIMARY KEY, short_name TEXT, faculty_type TEXT NOT NULL, official_url TEXT, notes TEXT) ON COMMIT DROP;
    INSERT INTO aul_faculty_seed VALUES
        ('Faculty of Business', NULL, 'FACULTY', NULL, 'Imported from AUL graduate inventory.'),
        ('Faculty of Engineering', NULL, 'FACULTY', NULL, 'Imported from AUL graduate inventory.'),
        ('Faculty of Sciences & Fine Arts', NULL, 'FACULTY', NULL, 'Imported from AUL graduate inventory.');
    INSERT INTO university_faculty (university_id, name, short_name, faculty_type, official_url, notes) SELECT v_university_id, name, short_name, faculty_type, official_url, notes FROM aul_faculty_seed ON CONFLICT (university_id, name) DO UPDATE SET short_name = EXCLUDED.short_name, faculty_type = EXCLUDED.faculty_type, official_url = EXCLUDED.official_url, notes = EXCLUDED.notes, updated_at = NOW();

    CREATE TEMP TABLE aul_program_seed (program_key TEXT PRIMARY KEY, faculty_name TEXT NOT NULL, major_category TEXT, major TEXT, degree_type TEXT NOT NULL, official_degree_name TEXT, thesis_or_non_thesis TEXT, credits INTEGER, duration_value NUMERIC(10, 2), duration_unit TEXT, primary_language_code TEXT, delivery_mode TEXT, program_description TEXT, official_program_url TEXT, source_ids JSONB NOT NULL, notes TEXT) ON COMMIT DROP;
    INSERT INTO aul_program_seed SELECT program_key, faculty_name, major_category, major, degree_type, official_degree_name, NULL, credits, duration_value, duration_unit, primary_language_code, delivery_mode, program_description, official_program_url, source_ids::jsonb, notes FROM jsonb_to_recordset($AUL_PROGRAMS$[
  {
    "id": "aul-mba",
    "program_key": "aul-mba",
    "official_title": "Master of Business Administration",
    "degree_type": "MASTER",
    "faculty": "Faculty of Business",
    "department": "Business Administration",
    "official_program_url": "https://aul.edu.lb/?page_id=142",
    "source_ids": [
      "AUL-002",
      "AUL-003"
    ],
    "credits": 38,
    "duration": "2 YEARS",
    "language": null,
    "concentrations_or_tracks": null,
    "tuition": null,
    "description": "AUL's Faculty of Business page lists the MBA under Business Administration with major highlights, acquired skills, credits, and expected graduation time.",
    "notes": "Discovery found one official MBA degree record. No separate MBA concentrations or additional graduate business degrees were identified in the official source set.",
    "source_primary_url": "https://aul.edu.lb/?page_id=142",
    "major_category": "Business Administration",
    "major": "Business Administration",
    "primary_source_id": "AUL-003"
  },
  {
    "id": "aul-master-of-science-in-engineering",
    "program_key": "aul-master-of-science-in-engineering",
    "official_title": "Master of Science in Engineering",
    "degree_type": "MASTER",
    "faculty": "Faculty of Engineering",
    "department": "Computer & Communication Department",
    "official_program_url": "https://aul.edu.lb/?page_id=144",
    "source_ids": [
      "AUL-002",
      "AUL-004"
    ],
    "credits": 46,
    "duration": "2 YEARS",
    "language": null,
    "concentrations_or_tracks": null,
    "tuition": null,
    "description": "AUL's Faculty of Engineering page lists Master in CCE under the Master of Science in Engineering umbrella with credits, duration, and program highlights.",
    "notes": "The official page shows a Master of Science in Engineering title and a Master in CCE program label. No separate PhD program was found.",
    "source_primary_url": "https://aul.edu.lb/?page_id=144",
    "major_category": "Engineering",
    "major": "Computer & Communication",
    "primary_source_id": "AUL-004"
  },
  {
    "id": "aul-master-of-science",
    "program_key": "aul-master-of-science",
    "official_title": "Master of Science",
    "degree_type": "MASTER",
    "faculty": "Faculty of Sciences & Fine Arts",
    "department": "Computer Science Department",
    "official_program_url": "https://aul.edu.lb/?page_id=146",
    "source_ids": [
      "AUL-002",
      "AUL-005"
    ],
    "credits": 39,
    "duration": "2 YEARS",
    "language": null,
    "concentrations_or_tracks": null,
    "tuition": null,
    "description": "AUL's Faculty of Sciences & Fine Arts page lists Master in CSC under the Master of Science degree with credits, duration, and major highlights.",
    "notes": "The official page shows a Master of Science title and a Master in CSC program label. No additional graduate science degree pages were identified.",
    "source_primary_url": "https://aul.edu.lb/?page_id=146",
    "major_category": "Sciences & Fine Arts",
    "major": "Computer Science",
    "primary_source_id": "AUL-005"
  }
]$AUL_PROGRAMS$) AS x(program_key TEXT, faculty_name TEXT, major_category TEXT, major TEXT, degree_type TEXT, official_degree_name TEXT, thesis_or_non_thesis TEXT, credits INTEGER, duration_value NUMERIC(10,2), duration_unit TEXT, primary_language_code TEXT, delivery_mode TEXT, program_description TEXT, official_program_url TEXT, source_ids JSONB, notes TEXT);

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
    FROM aul_program_seed seed
    JOIN university_faculty fac ON fac.university_id = v_university_id AND fac.name = seed.faculty_name
    JOIN degree_type dt ON dt.code = seed.degree_type
    JOIN aul_source_seed hs ON hs.source_id = (seed.source_ids->>0)
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

    INSERT INTO graduate_fee_item (university_id, faculty_id, department_id, program_id, scope_level, record_key, academic_year, fee_name, billing_basis, currency, amount, category, notes, source_id) VALUES
        (v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'aul-fee-application', NULL, 'Graduate application fee', 'FLAT_FEE', 'USD', NULL, 'Admissions', 'AUL publishes application-fee labels, but no public graduate amount was found.', (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://aul.edu.lb/?page_id=78' LIMIT 1)),
        (v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'aul-fee-registration', NULL, 'Graduate registration fee', 'FLAT_FEE', 'USD', NULL, 'Admissions', 'AUL publishes registration-fee labels, but no public graduate amount was found.', (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://aul.edu.lb/?page_id=78' LIMIT 1)),
        (v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'aul-fee-entrance-exam', NULL, 'Graduate entrance exam fee', 'FLAT_FEE', 'USD', NULL, 'Admissions', 'AUL publishes language and faculty-specific entrance exam labels, but no numeric amount was published in the reviewed content.', (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://aul.edu.lb/?page_id=78' LIMIT 1))
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
        (v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'aul-admission-general', 'GENERAL', 'Graduate applicants must complete the admission application at the campus of registration and submit the required documents by the deadline assigned by Admissions.', NULL, NULL, NULL, TRUE, 'Shared graduate admissions process from the official admissions page.', (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://aul.edu.lb/?page_id=78' LIMIT 1))
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
        (v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'aul-doc-01', 'APPLICATION', 'Admission Application Form (4 pages)', FALSE, 1, NULL, (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://aul.edu.lb/?page_id=78' LIMIT 1))
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

    INSERT INTO graduate_required_document (university_id, faculty_id, department_id, program_id, scope_level, record_key, document_type, document_name, is_optional, sort_order, notes, source_id) VALUES
        (v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'aul-doc-02', 'APPLICATION', 'Three recent passport-size colored photos', FALSE, 2, NULL, (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://aul.edu.lb/?page_id=78' LIMIT 1))
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

    INSERT INTO graduate_required_document (university_id, faculty_id, department_id, program_id, scope_level, record_key, document_type, document_name, is_optional, sort_order, notes, source_id) VALUES
        (v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'aul-doc-03', 'APPLICATION', 'Certified copy of Identity Card or Passport', FALSE, 3, NULL, (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://aul.edu.lb/?page_id=78' LIMIT 1))
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

    INSERT INTO graduate_required_document (university_id, faculty_id, department_id, program_id, scope_level, record_key, document_type, document_name, is_optional, sort_order, notes, source_id) VALUES
        (v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'aul-doc-04', 'APPLICATION', 'Certified copy of Family Register', FALSE, 4, NULL, (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://aul.edu.lb/?page_id=78' LIMIT 1))
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

    INSERT INTO graduate_required_document (university_id, faculty_id, department_id, program_id, scope_level, record_key, document_type, document_name, is_optional, sort_order, notes, source_id) VALUES
        (v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'aul-doc-05', 'APPLICATION', 'Certified copy of Official Lebanese BAC II Certificate or equivalent / freshman authorization with SAT', FALSE, 5, NULL, (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://aul.edu.lb/?page_id=78' LIMIT 1))
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

    INSERT INTO graduate_required_document (university_id, faculty_id, department_id, program_id, scope_level, record_key, document_type, document_name, is_optional, sort_order, notes, source_id) VALUES
        (v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'aul-doc-06', 'APPLICATION', 'Certified copy of high-school grades for the last three years', FALSE, 6, NULL, (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://aul.edu.lb/?page_id=78' LIMIT 1))
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

    INSERT INTO graduate_required_document (university_id, faculty_id, department_id, program_id, scope_level, record_key, document_type, document_name, is_optional, sort_order, notes, source_id) VALUES
        (v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'aul-doc-07', 'APPLICATION', 'Certified copies of degree(s) and official transcript(s) from previous university stamped by the Lebanese Ministry of Education & Higher Education, with equivalence for graduate programs', FALSE, 7, NULL, (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://aul.edu.lb/?page_id=78' LIMIT 1))
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

    INSERT INTO graduate_required_document (university_id, faculty_id, department_id, program_id, scope_level, record_key, document_type, document_name, is_optional, sort_order, notes, source_id) VALUES
        (v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'aul-doc-08', 'APPLICATION', 'International testing scores and certificates, if available', FALSE, 8, NULL, (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://aul.edu.lb/?page_id=78' LIMIT 1))
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

    INSERT INTO graduate_required_document (university_id, faculty_id, department_id, program_id, scope_level, record_key, document_type, document_name, is_optional, sort_order, notes, source_id) VALUES
        (v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'aul-doc-09', 'APPLICATION', 'Medical report', FALSE, 9, NULL, (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://aul.edu.lb/?page_id=78' LIMIT 1))
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

    INSERT INTO graduate_required_document (university_id, faculty_id, department_id, program_id, scope_level, record_key, document_type, document_name, is_optional, sort_order, notes, source_id) VALUES
        (v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'aul-doc-10', 'APPLICATION', 'NSSF letter, if available', FALSE, 10, NULL, (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://aul.edu.lb/?page_id=78' LIMIT 1))
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

    INSERT INTO graduate_required_document (university_id, faculty_id, department_id, program_id, scope_level, record_key, document_type, document_name, is_optional, sort_order, notes, source_id) VALUES
        (v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'aul-doc-11', 'APPLICATION', 'Proof of residency letter', FALSE, 11, NULL, (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://aul.edu.lb/?page_id=78' LIMIT 1))
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
        (v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'aul-deadline-assigned', NULL, 'OTHER', NULL, NULL, 'Documents must be submitted by the deadlines assigned by Admissions.', (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://aul.edu.lb/?page_id=78' LIMIT 1))
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
        (v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'aul-sch-1', NULL, 'SIBLINGS', NULL, NULL, NULL, NULL, 'Scholarship label visible on the admissions page; no graduate award details were publicly available.', (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://aul.edu.lb/?page_id=78' LIMIT 1))
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

    INSERT INTO graduate_scholarship (university_id, faculty_id, department_id, program_id, scope_level, record_key, academic_year, name, description, coverage, amount, currency, notes, source_id) VALUES
        (v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'aul-sch-2', NULL, 'SPORTS', NULL, NULL, NULL, NULL, 'Scholarship label visible on the admissions page; no graduate award details were publicly available.', (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://aul.edu.lb/?page_id=78' LIMIT 1))
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

    INSERT INTO graduate_scholarship (university_id, faculty_id, department_id, program_id, scope_level, record_key, academic_year, name, description, coverage, amount, currency, notes, source_id) VALUES
        (v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'aul-sch-3', NULL, 'SCHOLARSHIPS', NULL, NULL, NULL, NULL, 'Scholarship label visible on the admissions page; no graduate award details were publicly available.', (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://aul.edu.lb/?page_id=78' LIMIT 1))
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
        (v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'aul-fin-aid-degree-seeking', NULL, 'Degree-seeking financial aid', 'Degree-seeking students may qualify for financial aid at AUL and must apply through Student Affairs before the university calendar deadline.', NULL, NULL, 'Financial-aid summary from the admissions page.', (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://aul.edu.lb/?page_id=78' LIMIT 1))
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
        (v_university_id, NULL, NULL, NULL, 'UNIVERSITY', 'aul-payment-plan-not-published', NULL, 'Payment plan not published', 'No detailed graduate payment-plan information was published in the reviewed official sources.', NULL, NULL, NULL, NULL, 'No published payment-plan table was available in the reviewed AUL sources.', (SELECT id FROM source WHERE university_id = v_university_id AND url = 'https://aul.edu.lb/?page_id=78' LIMIT 1))
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

END $$;
