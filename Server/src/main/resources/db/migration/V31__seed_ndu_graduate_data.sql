-- NDU graduate data seed migration.
-- Idempotent import for the canonical NDU graduate dataset.

DO $$
DECLARE
    v_university_id BIGINT;
BEGIN

    INSERT INTO university (name, name_ar, acronym, country, city, latitude, longitude, campus_name, campus_type)
    SELECT 'Notre Dame University-Louaize', NULL, 'NDU', 'Lebanon', NULL, NULL, NULL, NULL, NULL
    WHERE NOT EXISTS (
        SELECT 1 FROM university WHERE name = 'Notre Dame University-Louaize'
    );

    SELECT id INTO v_university_id FROM university WHERE name = 'Notre Dame University-Louaize' ORDER BY id LIMIT 1;

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
        ('French', 'fr', 'Français'),
        ('Arabic', 'ar', 'العربية'),
        ('Multilingual', 'multi', 'Multilingual')
    ON CONFLICT (code) DO UPDATE SET
        name = EXCLUDED.name,
        native_name = EXCLUDED.native_name;

    CREATE TEMP TABLE ndu_source_seed (source_id TEXT PRIMARY KEY, title TEXT NOT NULL, url TEXT NOT NULL, source_type TEXT NOT NULL, accessed_at DATE, notes TEXT) ON COMMIT DROP;
    INSERT INTO ndu_source_seed (source_id, title, url, source_type, accessed_at, notes) SELECT source_id, title, url, source_type, accessed_at, notes FROM jsonb_to_recordset($NDU_SOURCES$[
  {
    "source_id": "NDU-SRC-001",
    "title": "Official website",
    "url": "https://www.ndu.edu.lb",
    "source_type": "WEB",
    "accessed_at": "2026-06-29",
    "notes": "Official NDU website listed in discovery_report.md. Starting point for the discovery pass."
  },
  {
    "source_id": "NDU-SRC-002",
    "title": "Graduate",
    "url": "https://www.ndu.edu.lb/academics/graduate",
    "source_type": "WEB",
    "accessed_at": "2026-06-29",
    "notes": "Main graduate overview. Confirms graduate studies, admission eligibility, English proficiency, required documents, MBA/Engineering extra requirements, and registration guidance."
  },
  {
    "source_id": "NDU-SRC-003",
    "title": "Degree Programs",
    "url": "https://www.ndu.edu.lb/academics/degree-programs",
    "source_type": "WEB",
    "accessed_at": "2026-06-29",
    "notes": "Central program index. Listed 28 graduate entries during this pass."
  },
  {
    "source_id": "NDU-SRC-004",
    "title": "Faculties",
    "url": "https://www.ndu.edu.lb/academics/faculties",
    "source_type": "WEB",
    "accessed_at": "2026-06-29",
    "notes": "Official faculty list. Graduate programs were discovered under FAAD, FBAE, FE, FH, FLPS, FNAS, and FNHS."
  },
  {
    "source_id": "NDU-SRC-005",
    "title": "Application Process",
    "url": "https://www.ndu.edu.lb/admissions/application-process",
    "source_type": "WEB",
    "accessed_at": "2026-06-29",
    "notes": "Confirms online application, USD 30 application fee, document submission, entrance exams, and evaluation process."
  },
  {
    "source_id": "NDU-SRC-006",
    "title": "Graduate Admission Requirements",
    "url": "https://www.ndu.edu.lb/admissions/admission-requirements/graduate",
    "source_type": "WEB",
    "accessed_at": "2026-06-29",
    "notes": "Graduate admission requirement source discovered through official admissions path/search."
  },
  {
    "source_id": "NDU-SRC-007",
    "title": "Important Dates",
    "url": "https://www.ndu.edu.lb/admissions/important-dates",
    "source_type": "WEB",
    "accessed_at": "2026-06-29",
    "notes": "Admissions dates and Fall 2026 admission cycle notes."
  },
  {
    "source_id": "NDU-SRC-008",
    "title": "Tuition Fees",
    "url": "https://www.ndu.edu.lb/office-of-finance/tuition-fees",
    "source_type": "WEB",
    "accessed_at": "2026-06-29",
    "notes": "Official graduate tuition and fee table."
  },
  {
    "source_id": "NDU-SRC-009",
    "title": "Yearly Schedule",
    "url": "https://www.ndu.edu.lb/offices/office-of-finance/yearly-schedule",
    "source_type": "WEB",
    "accessed_at": "2026-06-29",
    "notes": "Payment schedule/installment timing."
  },
  {
    "source_id": "NDU-SRC-010",
    "title": "Graduate Tuition Reduction",
    "url": "https://www.ndu.edu.lb/admissions/graduate-tuition-reduction",
    "source_type": "WEB",
    "accessed_at": "2026-06-29",
    "notes": "Graduate-specific 25% tuition reduction for eligible recent NDU graduates."
  },
  {
    "source_id": "NDU-SRC-011",
    "title": "Scholarship and Financial Aid - New Students",
    "url": "https://www.ndu.edu.lb/scholarship-and-financial-aid/new-student",
    "source_type": "WEB",
    "accessed_at": "2026-06-29",
    "notes": "Financial aid upon admission source; appears undergraduate/full-time oriented in visible content, so follow-up needed for graduate applicability."
  },
  {
    "source_id": "NDU-SRC-012",
    "title": "Academic Rules and Regulations - Graduates",
    "url": "https://www.ndu.edu.lb/offices/office-of-the-registrar/academic-rules-and-regulations/graduates",
    "source_type": "WEB",
    "accessed_at": "2026-06-29",
    "notes": "Graduate regulations source."
  },
  {
    "source_id": "NDU-SRC-013",
    "title": "Office of Research and Graduate Studies",
    "url": "https://www.ndu.edu.lb/offices/office-of-research-and-graduate-studies/message",
    "source_type": "WEB",
    "accessed_at": "2026-06-29",
    "notes": "Office source for graduate programs and research environment."
  },
  {
    "source_id": "NDU-SRC-014",
    "title": "Academic Calendar",
    "url": "https://www.ndu.edu.lb/academics/academic-calendar",
    "source_type": "WEB",
    "accessed_at": "2026-06-29",
    "notes": "Academic calendar source."
  },
  {
    "source_id": "NDU-SRC-015",
    "title": "NDU Catalog 2025-2026",
    "url": "https://www.ndu.edu.lb/Assets/Library/Gallery/PDFs/NDU%20Catalog%202025-2026.pdf",
    "source_type": "PDF",
    "accessed_at": "2026-06-29",
    "notes": "Official catalog PDF."
  },
  {
    "source_id": "NDU-SRC-016",
    "title": "Admission Guide 2025-2026",
    "url": "https://www.ndu.edu.lb/Assets/ContentFiles/admission-guide-2025_151350.pdf",
    "source_type": "PDF",
    "accessed_at": "2026-06-29",
    "notes": "Official admission guide PDF."
  },
  {
    "source_id": "NDU-SRC-017",
    "title": "NDU Catalog 2024-2025",
    "url": "https://www.ndu.edu.lb/Library/Assets/Files/Documents/OfficeoftheRegistrar/Catalog%202024-2025.pdf",
    "source_type": "PDF",
    "accessed_at": "2026-06-29",
    "notes": "Prior catalog PDF; useful fallback only."
  },
  {
    "source_id": "NDU-SRC-018",
    "title": "Institutional Accreditation",
    "url": "https://www.ndu.edu.lb/about/accreditation/institutional-accreditation",
    "source_type": "WEB",
    "accessed_at": "2026-07-01",
    "notes": "Confirms NDU institutional accreditation by NECHE and was reused during the enrichment pass."
  }
]$NDU_SOURCES$::jsonb) AS x(source_id TEXT, title TEXT, url TEXT, source_type TEXT, accessed_at DATE, notes TEXT);
    INSERT INTO source (university_id, title, url, source_type, accessed_at) SELECT v_university_id, title, url, source_type, accessed_at FROM ndu_source_seed ON CONFLICT (university_id, url) DO UPDATE SET title = EXCLUDED.title, source_type = EXCLUDED.source_type, accessed_at = EXCLUDED.accessed_at, updated_at = NOW();

    CREATE TEMP TABLE ndu_faculty_seed (name TEXT PRIMARY KEY, short_name TEXT, faculty_type TEXT NOT NULL, official_url TEXT, notes TEXT) ON COMMIT DROP;
    INSERT INTO ndu_faculty_seed (name, short_name, faculty_type, official_url, notes) SELECT name, short_name, faculty_type, official_url, notes FROM jsonb_to_recordset($NDU_FACULTIES$[
  {
    "name": "Ramez G. Chagoury Faculty of Architecture, Arts, and Design",
    "short_name": null,
    "faculty_type": "FACULTY",
    "official_url": null,
    "notes": null
  },
  {
    "name": "Faculty of Humanities",
    "short_name": null,
    "faculty_type": "FACULTY",
    "official_url": null,
    "notes": null
  },
  {
    "name": "Faculty of Law and Political Science",
    "short_name": null,
    "faculty_type": "FACULTY",
    "official_url": null,
    "notes": null
  },
  {
    "name": "Faculty of Natural and Applied Sciences",
    "short_name": null,
    "faculty_type": "FACULTY",
    "official_url": null,
    "notes": null
  },
  {
    "name": "Faculty of Business Administration and Economics",
    "short_name": null,
    "faculty_type": "FACULTY",
    "official_url": null,
    "notes": null
  },
  {
    "name": "Faculty of Engineering",
    "short_name": null,
    "faculty_type": "FACULTY",
    "official_url": null,
    "notes": null
  },
  {
    "name": "Faculty of Nursing and Health Sciences",
    "short_name": null,
    "faculty_type": "FACULTY",
    "official_url": null,
    "notes": null
  }
]$NDU_FACULTIES$::jsonb) AS x(name TEXT, short_name TEXT, faculty_type TEXT, official_url TEXT, notes TEXT);
    INSERT INTO university_faculty (university_id, name, short_name, faculty_type, official_url, notes) SELECT v_university_id, name, short_name, faculty_type, official_url, notes FROM ndu_faculty_seed ON CONFLICT (university_id, name) DO UPDATE SET short_name = EXCLUDED.short_name, faculty_type = EXCLUDED.faculty_type, official_url = EXCLUDED.official_url, notes = EXCLUDED.notes, updated_at = NOW();

    CREATE TEMP TABLE ndu_department_seed (faculty_name TEXT NOT NULL, name TEXT NOT NULL, short_name TEXT, official_url TEXT, notes TEXT) ON COMMIT DROP;
    INSERT INTO ndu_department_seed (faculty_name, name, short_name, official_url, notes) SELECT faculty_name, name, short_name, official_url, notes FROM jsonb_to_recordset($NDU_DEPARTMENTS$[
  {
    "faculty_name": "Ramez G. Chagoury Faculty of Architecture, Arts, and Design",
    "name": "Department of Design",
    "short_name": null,
    "official_url": null,
    "notes": null
  },
  {
    "faculty_name": "Faculty of Humanities",
    "name": "Department of Psychology, Education and Physical Education",
    "short_name": null,
    "official_url": null,
    "notes": null
  },
  {
    "faculty_name": "Faculty of Humanities",
    "name": "Department of English and Translation",
    "short_name": null,
    "official_url": null,
    "notes": null
  },
  {
    "faculty_name": "Faculty of Law and Political Science",
    "name": "Department of Government and International Relations",
    "short_name": null,
    "official_url": null,
    "notes": null
  },
  {
    "faculty_name": "Faculty of Humanities",
    "name": "Department of Media Studies",
    "short_name": null,
    "official_url": null,
    "notes": null
  },
  {
    "faculty_name": "Ramez G. Chagoury Faculty of Architecture, Arts, and Design",
    "name": "Department of Music",
    "short_name": null,
    "official_url": null,
    "notes": null
  },
  {
    "faculty_name": "Faculty of Natural and Applied Sciences",
    "name": "Department of Mathematics and Statistics",
    "short_name": null,
    "official_url": null,
    "notes": null
  },
  {
    "faculty_name": "Faculty of Natural and Applied Sciences",
    "name": "Department of Sciences",
    "short_name": null,
    "official_url": null,
    "notes": null
  },
  {
    "faculty_name": "Faculty of Business Administration and Economics",
    "name": "Department of Accounting and Finance",
    "short_name": null,
    "official_url": null,
    "notes": null
  },
  {
    "faculty_name": "Faculty of Engineering",
    "name": "Department of Civil and Environmental Engineering",
    "short_name": null,
    "official_url": null,
    "notes": null
  },
  {
    "faculty_name": "Faculty of Natural and Applied Sciences",
    "name": "Department of Computer Science",
    "short_name": null,
    "official_url": null,
    "notes": null
  },
  {
    "faculty_name": "Faculty of Engineering",
    "name": "Department of Electrical, Computer and Communication Engineering",
    "short_name": null,
    "official_url": null,
    "notes": null
  },
  {
    "faculty_name": "Faculty of Nursing and Health Sciences",
    "name": "Department of Nursing and Health Sciences",
    "short_name": null,
    "official_url": null,
    "notes": null
  },
  {
    "faculty_name": "Faculty of Engineering",
    "name": "Department of Mechanical Engineering",
    "short_name": null,
    "official_url": null,
    "notes": null
  },
  {
    "faculty_name": "Ramez G. Chagoury Faculty of Architecture, Arts, and Design",
    "name": "Department of Architecture",
    "short_name": null,
    "official_url": null,
    "notes": null
  },
  {
    "faculty_name": "Faculty of Business Administration and Economics",
    "name": "Department of Management and Marketing",
    "short_name": null,
    "official_url": null,
    "notes": null
  }
]$NDU_DEPARTMENTS$::jsonb) AS x(faculty_name TEXT, name TEXT, short_name TEXT, official_url TEXT, notes TEXT);
    INSERT INTO university_department (university_id, faculty_id, name, short_name, official_url, notes) SELECT v_university_id, fac.id, dep.name, dep.short_name, dep.official_url, dep.notes FROM ndu_department_seed dep JOIN university_faculty fac ON fac.university_id = v_university_id AND fac.name = dep.faculty_name ON CONFLICT (university_id, faculty_id, name) DO UPDATE SET short_name = EXCLUDED.short_name, official_url = EXCLUDED.official_url, notes = EXCLUDED.notes, updated_at = NOW();

    CREATE TEMP TABLE ndu_program_seed (id TEXT PRIMARY KEY, faculty TEXT NOT NULL, department TEXT NOT NULL, major_category TEXT, major TEXT, degree_type TEXT NOT NULL, official_degree_name TEXT, thesis_or_non_thesis TEXT, credits INTEGER, duration_value NUMERIC(10, 2), duration_unit TEXT, program_description TEXT, official_program_url TEXT NOT NULL, primary_source_id TEXT NOT NULL, notes TEXT, delivery_mode TEXT, language TEXT, admission_requirements TEXT, gre_requirement TEXT, gmat_requirement TEXT, interview_requirement TEXT, experience_requirement TEXT, accreditation TEXT, concentrations_or_tracks JSONB, source_ids JSONB, tuition JSONB) ON COMMIT DROP;
    INSERT INTO ndu_program_seed (id, faculty, department, major_category, major, degree_type, official_degree_name, thesis_or_non_thesis, credits, duration_value, duration_unit, program_description, official_program_url, primary_source_id, notes, delivery_mode, language, admission_requirements, gre_requirement, gmat_requirement, interview_requirement, experience_requirement, accreditation, concentrations_or_tracks, source_ids, tuition) SELECT id, faculty, department, major_category, major, degree_type, official_degree_name, thesis_or_non_thesis, credits, duration_value, duration_unit, program_description, official_program_url, primary_source_id, notes, delivery_mode, language, admission_requirements, gre_requirement, gmat_requirement, interview_requirement, experience_requirement, accreditation, concentrations_or_tracks, source_ids, tuition FROM jsonb_to_recordset($NDU_PROGRAMS$[
  {
    "id": "ndu-faad-master-design",
    "faculty": "Ramez G. Chagoury Faculty of Architecture, Arts, and Design",
    "department": "Department of Design",
    "major_category": "Architecture and Design",
    "major": "Master of Arts in Design",
    "degree_type": "MASTER",
    "official_degree_name": "Master of Arts in Design",
    "thesis_or_non_thesis": null,
    "credits": 36,
    "duration_value": null,
    "duration_unit": null,
    "program_description": "The program emphasizes the relationship between theory and practice in design, including professional and managerial aspects of project research and development, and the social and cultural context of design work.",
    "official_program_url": "https://www.ndu.edu.lb/academics/faculties/faad/departments/department-of-design/programs/master-of-arts-in-design-1",
    "primary_source_id": "NDU-SRC-003",
    "notes": "The page specifies both part-time and full-time study loads.",
    "delivery_mode": "ON_CAMPUS",
    "language": null,
    "admission_requirements": "Applicants from visual arts and design backgrounds are preferred. Candidates from other disciplines may be considered after meeting University graduate admission requirements. A portfolio and an interview with the MA course faculty are required. Full-time candidates must take at least 9 credits per semester; part-time candidates must take at least 6.",
    "gre_requirement": null,
    "gmat_requirement": null,
    "interview_requirement": "Required with the MA course faculty.",
    "experience_requirement": null,
    "accreditation": null,
    "source_ids": [
      "NDU-SRC-003",
      "NDU-SRC-002",
      "NDU-SRC-006",
      "NDU-SRC-008"
    ],
    "tuition": {
      "academic_year": "2025-2026",
      "currency": "USD",
      "billing_basis": "PER_CREDIT",
      "amount": 670,
      "category": "Architecture",
      "notes": "Official NDU graduate tuition and fee table.",
      "source_ids": [
        "NDU-SRC-008"
      ]
    }
  },
  {
    "id": "ndu-fh-master-education",
    "faculty": "Faculty of Humanities",
    "department": "Department of Psychology, Education and Physical Education",
    "major_category": "Humanities",
    "major": "Master of Arts in Education",
    "degree_type": "MASTER",
    "official_degree_name": "Master of Arts in Education",
    "thesis_or_non_thesis": "THESIS",
    "credits": 33,
    "duration_value": null,
    "duration_unit": null,
    "program_description": "The program prepares students for advanced studies and careers in education, with a foundation in school leadership, educational technology, and special needs education.",
    "official_program_url": "https://www.ndu.edu.lb/academics/faculties/fh/departments/department-of-psychology-education-and-physical-education/programs/master-of-arts-in-education",
    "primary_source_id": "NDU-SRC-003",
    "notes": "The graduation requirements include a thesis.",
    "delivery_mode": "ON_CAMPUS",
    "language": null,
    "admission_requirements": "Applicants should hold a BA in Education or a BA with a Teaching Diploma from an accredited university. Related bachelor’s degrees may be considered case by case by the department.",
    "gre_requirement": null,
    "gmat_requirement": null,
    "interview_requirement": null,
    "experience_requirement": null,
    "accreditation": null,
    "concentrations_or_tracks": [
      "School Leadership",
      "Educational Technology",
      "Special Needs Education"
    ],
    "source_ids": [
      "NDU-SRC-003",
      "NDU-SRC-002",
      "NDU-SRC-006",
      "NDU-SRC-008"
    ],
    "tuition": {
      "academic_year": "2025-2026",
      "currency": "USD",
      "billing_basis": "PER_CREDIT",
      "amount": 590,
      "category": "All Others",
      "notes": "Official NDU graduate tuition and fee table.",
      "source_ids": [
        "NDU-SRC-008"
      ]
    }
  },
  {
    "id": "ndu-fh-master-english-language-literature-applied-linguistics-tefl",
    "faculty": "Faculty of Humanities",
    "department": "Department of English and Translation",
    "major_category": "Humanities",
    "major": "Master of Arts in English Language and Literature - Applied Linguistics and TEFL Emphasis",
    "degree_type": "MASTER",
    "official_degree_name": "Master of Arts in English Language and Literature - Applied Linguistics and TEFL Emphasis",
    "thesis_or_non_thesis": "THESIS",
    "credits": 30,
    "duration_value": null,
    "duration_unit": null,
    "program_description": "The program deepens students’ expertise in English literature and the mechanics of the English language, and prepares graduates for advanced study, foreign language teaching, program administration, and communications.",
    "official_program_url": "https://www.ndu.edu.lb/academics/faculties/fh/departments/department-of-english-and-translation/programs/master-of-arts-in-english-language-and-literature---applied-linguistics-and-tefl-emphasis",
    "primary_source_id": "NDU-SRC-003",
    "notes": "The page requires a minimum EET score of 60 for admission and states that GRE performance is required only if an applicant submits GRE results.",
    "delivery_mode": "ON_CAMPUS",
    "language": null,
    "admission_requirements": "Applicants are expected to show strong English proficiency; the page gives priority to applicants with added qualifications and professional experience such as teaching.",
    "gre_requirement": null,
    "gmat_requirement": null,
    "interview_requirement": null,
    "experience_requirement": null,
    "accreditation": null,
    "concentrations_or_tracks": [
      "Applied Linguistics and TEFL Emphasis"
    ],
    "source_ids": [
      "NDU-SRC-003",
      "NDU-SRC-002",
      "NDU-SRC-006",
      "NDU-SRC-008"
    ],
    "tuition": {
      "academic_year": "2025-2026",
      "currency": "USD",
      "billing_basis": "PER_CREDIT",
      "amount": 590,
      "category": "All Others",
      "notes": "Official NDU graduate tuition and fee table.",
      "source_ids": [
        "NDU-SRC-008"
      ]
    }
  },
  {
    "id": "ndu-fh-master-english-language-literature-literature",
    "faculty": "Faculty of Humanities",
    "department": "Department of English and Translation",
    "major_category": "Humanities",
    "major": "Master of Arts in English Language and Literature - Literature Emphasis",
    "degree_type": "MASTER",
    "official_degree_name": "Master of Arts in English Language and Literature - Literature Emphasis",
    "thesis_or_non_thesis": "THESIS",
    "credits": 30,
    "duration_value": null,
    "duration_unit": null,
    "program_description": "The program deepens students’ expertise in English literature and the structure of the English language for advanced study and careers in teaching, program administration, and communications.",
    "official_program_url": "https://www.ndu.edu.lb/academics/faculties/fh/departments/department-of-english-and-translation/programs/master-of-arts-in-english-language-and-literature---literature-emphasis",
    "primary_source_id": "NDU-SRC-003",
    "notes": "The page states that graduation requires 30 credits and thesis submission and defense.",
    "delivery_mode": "ON_CAMPUS",
    "language": null,
    "admission_requirements": null,
    "gre_requirement": null,
    "gmat_requirement": null,
    "interview_requirement": null,
    "experience_requirement": null,
    "accreditation": null,
    "concentrations_or_tracks": [
      "Literature Emphasis"
    ],
    "source_ids": [
      "NDU-SRC-003",
      "NDU-SRC-002",
      "NDU-SRC-006",
      "NDU-SRC-008"
    ],
    "tuition": {
      "academic_year": "2025-2026",
      "currency": "USD",
      "billing_basis": "PER_CREDIT",
      "amount": 590,
      "category": "All Others",
      "notes": "Official NDU graduate tuition and fee table.",
      "source_ids": [
        "NDU-SRC-008"
      ]
    }
  },
  {
    "id": "ndu-flps-master-international-affairs-diplomacy",
    "faculty": "Faculty of Law and Political Science",
    "department": "Department of Government and International Relations",
    "major_category": "Law and Political Science",
    "major": "Master of Arts in International Affairs and Diplomacy",
    "degree_type": "MASTER",
    "official_degree_name": "Master of Arts in International Affairs and Diplomacy",
    "thesis_or_non_thesis": "THESIS_OR_NON_THESIS",
    "credits": 36,
    "duration_value": null,
    "duration_unit": null,
    "program_description": "The program provides advanced university training in international affairs and diplomacy and prepares graduates for specialized careers and doctoral study.",
    "official_program_url": "https://www.ndu.edu.lb/academics/faculties/flps/departments/department-of-government-and-international-relations/programs/master-of-arts-in-international-affairs-and-diplomacy",
    "primary_source_id": "NDU-SRC-003",
    "notes": "The program page allows either comprehensive exam coursework or 30 credits of coursework plus 6 thesis credits.",
    "delivery_mode": "ON_CAMPUS",
    "language": null,
    "admission_requirements": "Applicants should hold a BA in Political Science, Public Administration, International Affairs and Diplomacy, International Law, or another related field. Non-major applicants may be asked to complete prerequisite courses.",
    "gre_requirement": null,
    "gmat_requirement": null,
    "interview_requirement": null,
    "experience_requirement": null,
    "accreditation": null,
    "source_ids": [
      "NDU-SRC-003",
      "NDU-SRC-002",
      "NDU-SRC-006",
      "NDU-SRC-008"
    ],
    "tuition": {
      "academic_year": "2025-2026",
      "currency": "USD",
      "billing_basis": "PER_CREDIT",
      "amount": 590,
      "category": "All Others",
      "notes": "Official NDU graduate tuition and fee table.",
      "source_ids": [
        "NDU-SRC-008"
      ]
    }
  },
  {
    "id": "ndu-flps-master-international-affairs-diplomacy-international-law",
    "faculty": "Faculty of Law and Political Science",
    "department": "Department of Government and International Relations",
    "major_category": "Law and Political Science",
    "major": "Master of Arts in International Affairs and Diplomacy - International Law Emphasis",
    "degree_type": "MASTER",
    "official_degree_name": "Master of Arts in International Affairs and Diplomacy - International Law Emphasis",
    "thesis_or_non_thesis": "THESIS_OR_NON_THESIS",
    "credits": 36,
    "duration_value": null,
    "duration_unit": null,
    "program_description": "The international law emphasis builds on the broader international affairs and diplomacy curriculum and adds a specialization in international law.",
    "official_program_url": "https://www.ndu.edu.lb/academics/faculties/flps/departments/department-of-government-and-international-relations/programs/master-of-arts-in-international-affairs-and-diplomacy---international-law-emphasis",
    "primary_source_id": "NDU-SRC-003",
    "notes": "The emphasis-specific prerequisite list includes IAF 211, IAF 401, and POS 442 or equivalents.",
    "delivery_mode": "ON_CAMPUS",
    "language": null,
    "admission_requirements": "Applicants should hold a BA in Political Science, Public Administration, International Affairs and Diplomacy, International Law, or another related field. Non-major applicants may be asked to complete prerequisite courses.",
    "gre_requirement": null,
    "gmat_requirement": null,
    "interview_requirement": null,
    "experience_requirement": null,
    "accreditation": null,
    "concentrations_or_tracks": [
      "International Law Emphasis"
    ],
    "source_ids": [
      "NDU-SRC-003",
      "NDU-SRC-002",
      "NDU-SRC-006",
      "NDU-SRC-008"
    ],
    "tuition": {
      "academic_year": "2025-2026",
      "currency": "USD",
      "billing_basis": "PER_CREDIT",
      "amount": 590,
      "category": "All Others",
      "notes": "Official NDU graduate tuition and fee table.",
      "source_ids": [
        "NDU-SRC-008"
      ]
    }
  },
  {
    "id": "ndu-fh-master-media-studies",
    "faculty": "Faculty of Humanities",
    "department": "Department of Media Studies",
    "major_category": "Humanities",
    "major": "Master of Arts in Media Studies",
    "degree_type": "MASTER",
    "official_degree_name": "Master of Arts in Media Studies",
    "thesis_or_non_thesis": "THESIS",
    "credits": 36,
    "duration_value": null,
    "duration_unit": null,
    "program_description": "The program equips graduates with conceptual, analytical, and hands-on tools for careers in communication and for doctoral study in media-related fields.",
    "official_program_url": "https://www.ndu.edu.lb/academics/faculties/fh/departments/department-of-media-studies/programs/master-of-arts-in-media-studies",
    "primary_source_id": "NDU-SRC-003",
    "notes": "Applicants must submit three recommendation letters, an updated CV, and a personal statement.",
    "delivery_mode": "ON_CAMPUS",
    "language": null,
    "admission_requirements": "Applicants may enter from the fields listed by the department. Unrelated majors may need preparatory courses. The page states a minimum GPA of 3.0, with probationary admission possible for GPAs between 2.8 and 3.0.",
    "gre_requirement": null,
    "gmat_requirement": null,
    "interview_requirement": null,
    "experience_requirement": null,
    "accreditation": null,
    "source_ids": [
      "NDU-SRC-003",
      "NDU-SRC-002",
      "NDU-SRC-006",
      "NDU-SRC-008"
    ],
    "tuition": {
      "academic_year": "2025-2026",
      "currency": "USD",
      "billing_basis": "PER_CREDIT",
      "amount": 590,
      "category": "All Others",
      "notes": "Official NDU graduate tuition and fee table.",
      "source_ids": [
        "NDU-SRC-008"
      ]
    }
  },
  {
    "id": "ndu-faad-master-music",
    "faculty": "Ramez G. Chagoury Faculty of Architecture, Arts, and Design",
    "department": "Department of Music",
    "major_category": "Architecture and Design",
    "major": "Master of Arts in Music",
    "degree_type": "MASTER",
    "official_degree_name": "Master of Arts in Music",
    "thesis_or_non_thesis": null,
    "credits": 36,
    "duration_value": null,
    "duration_unit": null,
    "program_description": "Graduate study in musicology covers historical and ethnomusicological investigation, hermeneutics, semiotics, criticism, and independent study with faculty consultation.",
    "official_program_url": "https://www.ndu.edu.lb/academics/faculties/faad/departments/department-of-music/programs/master-of-arts-in-music",
    "primary_source_id": "NDU-SRC-003",
    "notes": "The page does not explicitly publish a thesis requirement in the visible content.",
    "delivery_mode": "ON_CAMPUS",
    "language": null,
    "admission_requirements": "Applicants normally hold a bachelor degree in music or an equivalent qualification. They must submit an extended written piece on a musical subject. An English test is required except for students majoring in Arabic music.",
    "gre_requirement": null,
    "gmat_requirement": null,
    "interview_requirement": "Selection is based on applicant information and, when necessary, an interview.",
    "experience_requirement": null,
    "accreditation": null,
    "source_ids": [
      "NDU-SRC-003",
      "NDU-SRC-002",
      "NDU-SRC-006",
      "NDU-SRC-008"
    ],
    "tuition": {
      "academic_year": "2025-2026",
      "currency": "USD",
      "billing_basis": "PER_CREDIT",
      "amount": 670,
      "category": "Architecture",
      "notes": "Official NDU graduate tuition and fee table.",
      "source_ids": [
        "NDU-SRC-008"
      ]
    }
  },
  {
    "id": "ndu-flps-master-political-science",
    "faculty": "Faculty of Law and Political Science",
    "department": "Department of Government and International Relations",
    "major_category": "Law and Political Science",
    "major": "Master of Arts in Political Science",
    "degree_type": "MASTER",
    "official_degree_name": "Master of Arts in Political Science",
    "thesis_or_non_thesis": "THESIS_OR_NON_THESIS",
    "credits": 36,
    "duration_value": null,
    "duration_unit": null,
    "program_description": "The program is aimed at students preparing for public service and related fields and builds a theoretical and professional foundation in political science.",
    "official_program_url": "https://www.ndu.edu.lb/academics/faculties/flps/departments/department-of-government-and-international-relations/programs/master-of-arts-in-political-science",
    "primary_source_id": "NDU-SRC-003",
    "notes": "The program page allows either a comprehensive written and oral examination or 30 credits of coursework plus 6 thesis credits.",
    "delivery_mode": "ON_CAMPUS",
    "language": null,
    "admission_requirements": "Applicants should hold a BA in Political Science, Public Administration, International Affairs and Diplomacy, International Law, or another related field. The page requires the EET minimum, a minimum undergraduate GPA of 3.0, and may require GRE for non-NDU students. Non-major applicants may need prerequisite courses.",
    "gre_requirement": null,
    "gmat_requirement": null,
    "interview_requirement": null,
    "experience_requirement": null,
    "accreditation": null,
    "source_ids": [
      "NDU-SRC-003",
      "NDU-SRC-002",
      "NDU-SRC-006",
      "NDU-SRC-008"
    ],
    "tuition": {
      "academic_year": "2025-2026",
      "currency": "USD",
      "billing_basis": "PER_CREDIT",
      "amount": 590,
      "category": "All Others",
      "notes": "Official NDU graduate tuition and fee table.",
      "source_ids": [
        "NDU-SRC-008"
      ]
    }
  },
  {
    "id": "ndu-flps-master-political-science-human-rights",
    "faculty": "Faculty of Law and Political Science",
    "department": "Department of Government and International Relations",
    "major_category": "Law and Political Science",
    "major": "Master of Arts in Political Science - Human Rights Emphasis",
    "degree_type": "MASTER",
    "official_degree_name": "Master of Arts in Political Science - Human Rights Emphasis",
    "thesis_or_non_thesis": "THESIS_OR_NON_THESIS",
    "credits": 36,
    "duration_value": null,
    "duration_unit": null,
    "program_description": "The human rights emphasis builds the political science curriculum around human rights conventions, analysis, and advocacy.",
    "official_program_url": "https://www.ndu.edu.lb/academics/faculties/flps/departments/department-of-government-and-international-relations/programs/master-of-arts-in-political-science---human-rights-emphasis",
    "primary_source_id": "NDU-SRC-003",
    "notes": "The visible page content names prerequisites for the political science track and allows the same thesis/non-thesis path structure as the main program.",
    "delivery_mode": "ON_CAMPUS",
    "language": null,
    "admission_requirements": "Applicants should hold a BA in Political Science, Public Administration, International Affairs and Diplomacy, International Law, or another related field. The page requires the EET minimum, a minimum undergraduate GPA of 3.0, and may require GRE for non-NDU students. Non-major applicants may need prerequisite courses.",
    "gre_requirement": null,
    "gmat_requirement": null,
    "interview_requirement": null,
    "experience_requirement": null,
    "accreditation": null,
    "concentrations_or_tracks": [
      "Human Rights Emphasis"
    ],
    "source_ids": [
      "NDU-SRC-003",
      "NDU-SRC-002",
      "NDU-SRC-006",
      "NDU-SRC-008"
    ],
    "tuition": {
      "academic_year": "2025-2026",
      "currency": "USD",
      "billing_basis": "PER_CREDIT",
      "amount": 590,
      "category": "All Others",
      "notes": "Official NDU graduate tuition and fee table.",
      "source_ids": [
        "NDU-SRC-008"
      ]
    }
  },
  {
    "id": "ndu-flps-master-political-science-ngos",
    "faculty": "Faculty of Law and Political Science",
    "department": "Department of Government and International Relations",
    "major_category": "Law and Political Science",
    "major": "Master of Arts in Political Science - NGOs Emphasis",
    "degree_type": "MASTER",
    "official_degree_name": "Master of Arts in Political Science - NGOs Emphasis",
    "thesis_or_non_thesis": "THESIS_OR_NON_THESIS",
    "credits": 36,
    "duration_value": null,
    "duration_unit": null,
    "program_description": "The NGOs emphasis focuses on international and civil society organizations, NGOs, NPOs, humanitarian aid, and policy frameworks.",
    "official_program_url": "https://www.ndu.edu.lb/academics/faculties/flps/departments/department-of-government-and-international-relations/programs/master-of-arts-in-political-science---ngos-emphasis",
    "primary_source_id": "NDU-SRC-003",
    "notes": "The visible page content includes program-specific prerequisite lists for Political Science, Public Administration, International Affairs and Diplomacy, and International Affairs and Diplomacy - International Law Emphasis.",
    "delivery_mode": "ON_CAMPUS",
    "language": null,
    "admission_requirements": "Applicants should hold a BA in Political Science, Public Administration, International Affairs and Diplomacy, International Law, or another related field. The page requires the EET minimum, a minimum undergraduate GPA of 3.0, and may require GRE for non-NDU students. Non-major applicants may need prerequisite courses.",
    "gre_requirement": null,
    "gmat_requirement": null,
    "interview_requirement": null,
    "experience_requirement": null,
    "accreditation": null,
    "concentrations_or_tracks": [
      "NGOs Emphasis"
    ],
    "source_ids": [
      "NDU-SRC-003",
      "NDU-SRC-002",
      "NDU-SRC-006",
      "NDU-SRC-008"
    ],
    "tuition": {
      "academic_year": "2025-2026",
      "currency": "USD",
      "billing_basis": "PER_CREDIT",
      "amount": 590,
      "category": "All Others",
      "notes": "Official NDU graduate tuition and fee table.",
      "source_ids": [
        "NDU-SRC-008"
      ]
    }
  },
  {
    "id": "ndu-fh-master-psychology-educational",
    "faculty": "Faculty of Humanities",
    "department": "Department of Psychology, Education and Physical Education",
    "major_category": "Humanities",
    "major": "Master of Arts in Psychology - Educational Psychology",
    "degree_type": "MASTER",
    "official_degree_name": "Master of Arts in Psychology - Educational Psychology",
    "thesis_or_non_thesis": "THESIS_OR_NON_THESIS",
    "credits": 36,
    "duration_value": null,
    "duration_unit": null,
    "program_description": "The program applies psychological principles to teaching and learning and prepares students for postgraduate work in educational psychology.",
    "official_program_url": "https://www.ndu.edu.lb/academics/faculties/fh/departments/department-of-psychology-education-and-physical-education/programs/master-of-arts-in-psychology-educational-psychology",
    "primary_source_id": "NDU-SRC-003",
    "notes": "Graduation may follow either a thesis track or a non-thesis track.",
    "delivery_mode": "ON_CAMPUS",
    "language": null,
    "admission_requirements": "Applicants should hold a BA in Psychology or an equivalent degree from an accredited university. Other majors are evaluated separately. The page also requires a minimum undergraduate GPA of 2.75, a personal statement, and three professional recommendations.",
    "gre_requirement": null,
    "gmat_requirement": null,
    "interview_requirement": "A personal interview is at the discretion of the department.",
    "experience_requirement": null,
    "accreditation": null,
    "concentrations_or_tracks": [
      "Educational Psychology"
    ],
    "source_ids": [
      "NDU-SRC-003",
      "NDU-SRC-002",
      "NDU-SRC-006",
      "NDU-SRC-008"
    ],
    "tuition": {
      "academic_year": "2025-2026",
      "currency": "USD",
      "billing_basis": "PER_CREDIT",
      "amount": 590,
      "category": "All Others",
      "notes": "Official NDU graduate tuition and fee table.",
      "source_ids": [
        "NDU-SRC-008"
      ]
    }
  },
  {
    "id": "ndu-flps-master-public-administration",
    "faculty": "Faculty of Law and Political Science",
    "department": "Department of Government and International Relations",
    "major_category": "Law and Political Science",
    "major": "Master of Arts in Public Administration",
    "degree_type": "MASTER",
    "official_degree_name": "Master of Arts in Public Administration",
    "thesis_or_non_thesis": "THESIS_OR_NON_THESIS",
    "credits": 36,
    "duration_value": null,
    "duration_unit": null,
    "program_description": "The program teaches public administration theories and concepts and prepares graduates for public-sector and NGO careers through practice-oriented and research-based training.",
    "official_program_url": "https://www.ndu.edu.lb/academics/faculties/flps/departments/department-of-government-and-international-relations/programs/master-of-arts-in-public-administration",
    "primary_source_id": "NDU-SRC-003",
    "notes": "The graduation page allows either comprehensive examination coursework or 30 credits plus 6 thesis credits.",
    "delivery_mode": "ON_CAMPUS",
    "language": null,
    "admission_requirements": "Applicants should hold a BA in Political Science, Public Administration, International Affairs and Diplomacy, International Law, or another related field. The page requires the EET minimum and a minimum undergraduate GPA of 3.0. Non-major applicants may need prerequisite courses.",
    "gre_requirement": null,
    "gmat_requirement": null,
    "interview_requirement": null,
    "experience_requirement": null,
    "accreditation": null,
    "source_ids": [
      "NDU-SRC-003",
      "NDU-SRC-002",
      "NDU-SRC-006",
      "NDU-SRC-008"
    ],
    "tuition": {
      "academic_year": "2025-2026",
      "currency": "USD",
      "billing_basis": "PER_CREDIT",
      "amount": 590,
      "category": "All Others",
      "notes": "Official NDU graduate tuition and fee table.",
      "source_ids": [
        "NDU-SRC-008"
      ]
    }
  },
  {
    "id": "ndu-fh-master-translation",
    "faculty": "Faculty of Humanities",
    "department": "Department of English and Translation",
    "major_category": "Humanities",
    "major": "Master of Arts in Translation",
    "degree_type": "MASTER",
    "official_degree_name": "Master of Arts in Translation",
    "thesis_or_non_thesis": "THESIS",
    "credits": 36,
    "duration_value": null,
    "duration_unit": null,
    "program_description": "The program offers advanced training in translation and interpretation for students and working professionals across English, French, and Arabic.",
    "official_program_url": "https://www.ndu.edu.lb/academics/faculties/fh/departments/department-of-english-and-translation/programs/master-of-arts-in-translation",
    "primary_source_id": "NDU-SRC-003",
    "notes": "The graduation requirements include a thesis.",
    "delivery_mode": "ON_CAMPUS",
    "language": "English, French, Arabic",
    "admission_requirements": "Applicants must pass written French and Arabic language proficiency tests with a grade of 70 or above in both. An interview in English, French, and Arabic is also required. Applicants with a bachelor’s degree other than Translation must take TRA 201 and TRA 311 or TRA 401 in the first semester and earn a grade of B or higher.",
    "gre_requirement": null,
    "gmat_requirement": null,
    "interview_requirement": "Required in English, French, and Arabic.",
    "experience_requirement": null,
    "accreditation": null,
    "source_ids": [
      "NDU-SRC-003",
      "NDU-SRC-002",
      "NDU-SRC-006",
      "NDU-SRC-008"
    ],
    "tuition": {
      "academic_year": "2025-2026",
      "currency": "USD",
      "billing_basis": "PER_CREDIT",
      "amount": 590,
      "category": "All Others",
      "notes": "Official NDU graduate tuition and fee table.",
      "source_ids": [
        "NDU-SRC-008"
      ]
    }
  },
  {
    "id": "ndu-fnas-master-actuarial-sciences",
    "faculty": "Faculty of Natural and Applied Sciences",
    "department": "Department of Mathematics and Statistics",
    "major_category": "Natural and Applied Sciences",
    "major": "Master of Science in Actuarial Sciences - In partnership with Chedid Re",
    "degree_type": "MASTER",
    "official_degree_name": "Master of Science in Actuarial Sciences - In partnership with Chedid Re",
    "thesis_or_non_thesis": null,
    "credits": 30,
    "duration_value": null,
    "duration_unit": null,
    "program_description": "The program responds to the regional demand for risk specialists and prepares graduates for professional actuarial credentials and doctoral study.",
    "official_program_url": "https://www.ndu.edu.lb/academics/faculties/fnas/departments/department-of-mathematics-and-statistics/programs/master-of-science-in-actuarial-sciences---in-partnership-with-chedid-re",
    "primary_source_id": "NDU-SRC-003",
    "notes": "The official page emphasizes preparation for actuarial professional examinations and the partnership with Chedid Re.",
    "delivery_mode": "ON_CAMPUS",
    "language": null,
    "admission_requirements": "Applicants holding a BS in Mathematics or Actuarial Sciences with a cumulative GPA of at least 3.0 are accepted; 2.7 to 2.99 may be conditionally accepted. Remedial courses may be required. Up to 9 transfer credits may be approved.",
    "gre_requirement": null,
    "gmat_requirement": null,
    "interview_requirement": null,
    "experience_requirement": null,
    "accreditation": null,
    "source_ids": [
      "NDU-SRC-003",
      "NDU-SRC-002",
      "NDU-SRC-006",
      "NDU-SRC-008"
    ],
    "tuition": {
      "academic_year": "2025-2026",
      "currency": "USD",
      "billing_basis": "PER_CREDIT",
      "amount": 590,
      "category": "All Others",
      "notes": "Official NDU graduate tuition and fee table.",
      "source_ids": [
        "NDU-SRC-008"
      ]
    }
  },
  {
    "id": "ndu-fnas-master-biology",
    "faculty": "Faculty of Natural and Applied Sciences",
    "department": "Department of Sciences",
    "major_category": "Natural and Applied Sciences",
    "major": "Master of Science in Biology",
    "degree_type": "MASTER",
    "official_degree_name": "Master of Science in Biology",
    "thesis_or_non_thesis": "THESIS",
    "credits": 36,
    "duration_value": null,
    "duration_unit": null,
    "program_description": "The program combines coursework, practical lab experience, and thesis research to deepen advanced biological knowledge and support doctoral or professional paths.",
    "official_program_url": "https://www.ndu.edu.lb/academics/faculties/fnas/departments/department-of-sciences/programs/master-of-science-in-biology",
    "primary_source_id": "NDU-SRC-003",
    "notes": "The graduation requirements include 6 thesis credits.",
    "delivery_mode": "ON_CAMPUS",
    "language": null,
    "admission_requirements": "Applicants holding a BS in Biology with a cumulative GPA of at least 3.0 are accepted; 2.7 to 2.99 may be conditionally accepted. Remedial biology courses may be required, and up to 9 transfer credits may be approved.",
    "gre_requirement": null,
    "gmat_requirement": null,
    "interview_requirement": null,
    "experience_requirement": null,
    "accreditation": null,
    "source_ids": [
      "NDU-SRC-003",
      "NDU-SRC-002",
      "NDU-SRC-006",
      "NDU-SRC-008"
    ],
    "tuition": {
      "academic_year": "2025-2026",
      "currency": "USD",
      "billing_basis": "PER_CREDIT",
      "amount": 590,
      "category": "All Others",
      "notes": "Official NDU graduate tuition and fee table.",
      "source_ids": [
        "NDU-SRC-008"
      ]
    }
  },
  {
    "id": "ndu-fbae-master-business-strategy",
    "faculty": "Faculty of Business Administration and Economics",
    "department": "Department of Accounting and Finance",
    "major_category": "Business",
    "major": "Master of Science in Business Strategy - AACSB Accredited Program",
    "degree_type": "MASTER",
    "official_degree_name": "Master of Science in Business Strategy - AACSB Accredited Program",
    "thesis_or_non_thesis": null,
    "credits": 30,
    "duration_value": null,
    "duration_unit": null,
    "program_description": "The program is a specialized business strategy degree designed for professionals, business graduates, and other graduates seeking strategic management skills and doctoral preparation.",
    "official_program_url": "https://www.ndu.edu.lb/academics/faculties/fbae/departments/department-of-accounting-and-finance/programs/master-of-science-in-business-strategy---aacsb-accredited-program",
    "primary_source_id": "NDU-SRC-003",
    "notes": "The visible page lists a 5:30 p.m. course schedule and a maximum load of 12 credits per semester. It does not publish GRE or GMAT requirements in the visible content.",
    "delivery_mode": "ON_CAMPUS",
    "language": null,
    "admission_requirements": "Business and economics graduates need a completed application, two recommendation letters, and a minimum undergraduate GPA of 2.7. Applicants from other disciplines may need up to 18 credits of remedial courses. Applicants from non-English instruction institutions need an EET score of 60.",
    "gre_requirement": null,
    "gmat_requirement": null,
    "interview_requirement": null,
    "experience_requirement": null,
    "accreditation": "AACSB Accredited Program",
    "source_ids": [
      "NDU-SRC-003",
      "NDU-SRC-002",
      "NDU-SRC-006",
      "NDU-SRC-008"
    ],
    "tuition": {
      "academic_year": "2025-2026",
      "currency": "USD",
      "billing_basis": "PER_CREDIT",
      "amount": 635,
      "category": "Business",
      "notes": "Official NDU graduate tuition and fee table.",
      "source_ids": [
        "NDU-SRC-008"
      ]
    }
  },
  {
    "id": "ndu-fe-master-civil-engineering",
    "faculty": "Faculty of Engineering",
    "department": "Department of Civil and Environmental Engineering",
    "major_category": "Engineering",
    "major": "Master of Science in Civil Engineering",
    "degree_type": "MASTER",
    "official_degree_name": "Master of Science in Civil Engineering",
    "thesis_or_non_thesis": null,
    "credits": 30,
    "duration_value": null,
    "duration_unit": null,
    "program_description": "The program develops theoretical and applied civil engineering knowledge, leadership, and research readiness for advanced professional practice or doctoral study.",
    "official_program_url": "https://www.ndu.edu.lb/academics/faculties/fe/departments/department-of-civil-and-environmental-engineering/programs/-master-of-science-in-civil-engineering",
    "primary_source_id": "NDU-SRC-003",
    "notes": "Holders of BS degrees in Civil Engineering may need remedial courses on a case-by-case basis.",
    "delivery_mode": "ON_CAMPUS",
    "language": null,
    "admission_requirements": "Applicants must hold a bachelor degree in Civil Engineering or its equivalent, have a minimum GPA of 3.0, obtain GRE scores, and receive Faculty Graduate Committee approval. English proficiency requirements in the catalog must also be met.",
    "gre_requirement": "Official GRE score required.",
    "gmat_requirement": null,
    "interview_requirement": null,
    "experience_requirement": null,
    "accreditation": null,
    "source_ids": [
      "NDU-SRC-003",
      "NDU-SRC-002",
      "NDU-SRC-006",
      "NDU-SRC-008"
    ],
    "tuition": {
      "academic_year": "2025-2026",
      "currency": "USD",
      "billing_basis": "PER_CREDIT",
      "amount": 765,
      "category": "Engineering",
      "notes": "Official NDU graduate tuition and fee table.",
      "source_ids": [
        "NDU-SRC-008"
      ]
    }
  },
  {
    "id": "ndu-fnas-master-computer-science",
    "faculty": "Faculty of Natural and Applied Sciences",
    "department": "Department of Computer Science",
    "major_category": "Natural and Applied Sciences",
    "major": "Master of Science in Computer Science",
    "degree_type": "MASTER",
    "official_degree_name": "Master of Science in Computer Science",
    "thesis_or_non_thesis": null,
    "credits": 30,
    "duration_value": null,
    "duration_unit": null,
    "program_description": "The program prepares practitioners and future researchers in computing through advanced study in systems, software, databases, networks, security, and related fields.",
    "official_program_url": "https://www.ndu.edu.lb/academics/faculties/fnas/departments/department-of-computer-science/programs/master-of-science-in-computer-science",
    "primary_source_id": "NDU-SRC-003",
    "notes": "The page lists career tracks including software development, data science, cybersecurity, cloud computing, and research roles.",
    "delivery_mode": "ON_CAMPUS",
    "language": null,
    "admission_requirements": "Applicants holding a BS in Computer Science with a cumulative GPA of at least 3.0 are accepted; 2.7 to 2.99 may be conditionally accepted. Remedial undergraduate computer science courses may be required, and up to 9 transfer credits may be approved.",
    "gre_requirement": null,
    "gmat_requirement": null,
    "interview_requirement": null,
    "experience_requirement": null,
    "accreditation": null,
    "source_ids": [
      "NDU-SRC-003",
      "NDU-SRC-002",
      "NDU-SRC-006",
      "NDU-SRC-008"
    ],
    "tuition": {
      "academic_year": "2025-2026",
      "currency": "USD",
      "billing_basis": "PER_CREDIT",
      "amount": 670,
      "category": "Computer Sciences",
      "notes": "Official NDU graduate tuition and fee table.",
      "source_ids": [
        "NDU-SRC-008"
      ]
    }
  },
  {
    "id": "ndu-fe-master-electrical-computer-engineering",
    "faculty": "Faculty of Engineering",
    "department": "Department of Electrical, Computer and Communication Engineering",
    "major_category": "Engineering",
    "major": "Master of Science in Electrical and Computer Engineering",
    "degree_type": "MASTER",
    "official_degree_name": "Master of Science in Electrical and Computer Engineering",
    "thesis_or_non_thesis": null,
    "credits": 30,
    "duration_value": null,
    "duration_unit": null,
    "program_description": "The program extends undergraduate engineering fundamentals into advanced electrical and computer engineering practice and doctoral preparation.",
    "official_program_url": "https://www.ndu.edu.lb/academics/faculties/fe/departments/department-of-electrical-computer-and-communication-engineering/programs/master-of-science-in-electrical-and-computer-engineering",
    "primary_source_id": "NDU-SRC-003",
    "notes": "Holders of BE degrees may transfer up to 18 undergraduate major elective credits if the courses meet the page conditions.",
    "delivery_mode": "ON_CAMPUS",
    "language": null,
    "admission_requirements": "Applicants must hold a bachelor degree in Electrical/Computer Engineering or its equivalent, have a minimum GPA of 3.0, obtain GRE scores, and receive Faculty Graduate Committee approval. English proficiency requirements in the catalog must also be met.",
    "gre_requirement": "Official GRE score required.",
    "gmat_requirement": null,
    "interview_requirement": null,
    "experience_requirement": null,
    "accreditation": null,
    "source_ids": [
      "NDU-SRC-003",
      "NDU-SRC-002",
      "NDU-SRC-006",
      "NDU-SRC-008"
    ],
    "tuition": {
      "academic_year": "2025-2026",
      "currency": "USD",
      "billing_basis": "PER_CREDIT",
      "amount": 765,
      "category": "Engineering",
      "notes": "Official NDU graduate tuition and fee table.",
      "source_ids": [
        "NDU-SRC-008"
      ]
    }
  },
  {
    "id": "ndu-fbae-master-financial-risk-management",
    "faculty": "Faculty of Business Administration and Economics",
    "department": "Department of Accounting and Finance",
    "major_category": "Business",
    "major": "Master of Science in Financial Risk Management - AACSB Accredited Program",
    "degree_type": "MASTER",
    "official_degree_name": "Master of Science in Financial Risk Management - AACSB Accredited Program",
    "thesis_or_non_thesis": null,
    "credits": 30,
    "duration_value": null,
    "duration_unit": null,
    "program_description": "The program develops professional and technical skills for identifying, measuring, and managing financial risk and prepares students for professional FRM study or doctoral work.",
    "official_program_url": "https://www.ndu.edu.lb/academics/faculties/fbae/departments/department-of-accounting-and-finance/programs/master-of-science-in-financial-risk-management---aacsb-accredited-program",
    "primary_source_id": "NDU-SRC-003",
    "notes": "The page states a 30-credit program and a per-credit tuition price of USD 635.",
    "delivery_mode": "ON_CAMPUS",
    "language": null,
    "admission_requirements": "Business and economics graduates must submit a completed application, two recommendation letters, and show a cumulative average of 80% or 3.0/4.0. Other scientific discipline graduates may be admitted with relevant undergraduate business courses. Applicants from non-English instruction institutions need an EET score of 60.",
    "gre_requirement": null,
    "gmat_requirement": null,
    "interview_requirement": null,
    "experience_requirement": null,
    "accreditation": "AACSB Accredited Program",
    "source_ids": [
      "NDU-SRC-003",
      "NDU-SRC-002",
      "NDU-SRC-006",
      "NDU-SRC-008"
    ],
    "tuition": {
      "academic_year": "2025-2026",
      "currency": "USD",
      "billing_basis": "PER_CREDIT",
      "amount": 635,
      "category": "Business",
      "notes": "Official NDU graduate tuition and fee table.",
      "source_ids": [
        "NDU-SRC-008"
      ]
    }
  },
  {
    "id": "ndu-fnhs-master-food-safety-quality-management",
    "faculty": "Faculty of Nursing and Health Sciences",
    "department": "Department of Nursing and Health Sciences",
    "major_category": "Nursing and Health Sciences",
    "major": "Master of Science in Food Safety and Quality Management",
    "degree_type": "MASTER",
    "official_degree_name": "Master of Science in Food Safety and Quality Management",
    "thesis_or_non_thesis": "THESIS_OR_NON_THESIS",
    "credits": 36,
    "duration_value": null,
    "duration_unit": null,
    "program_description": "The program prepares graduates to manage food safety and quality from farm to fork with advanced knowledge, hands-on practice, and research capacity.",
    "official_program_url": "https://www.ndu.edu.lb/academics/faculties/fnhs/departments/department-of-nursing-and-health-sciences/programs/master-of-science-in-food-safety-and-quality-management",
    "primary_source_id": "NDU-SRC-003",
    "notes": "The page offers both thesis and applied non-thesis study paths.",
    "delivery_mode": "ON_CAMPUS",
    "language": null,
    "admission_requirements": "Applicants with a BS in Food Safety and Quality Management or a related field may apply. Other health-science backgrounds may need undergraduate remedial courses. The page requires a minimum GPA of 3.0, allows conditional admission for 2.7 to 2.99, and may require an interview for non-NDU applicants.",
    "gre_requirement": null,
    "gmat_requirement": null,
    "interview_requirement": "Applicants whose undergraduate degree is not from NDU may be asked for an interview.",
    "experience_requirement": null,
    "accreditation": null,
    "source_ids": [
      "NDU-SRC-003",
      "NDU-SRC-002",
      "NDU-SRC-006",
      "NDU-SRC-008"
    ],
    "tuition": {
      "academic_year": "2025-2026",
      "currency": "USD",
      "billing_basis": "PER_CREDIT",
      "amount": 590,
      "category": "All Others",
      "notes": "Official NDU graduate tuition and fee table.",
      "source_ids": [
        "NDU-SRC-008"
      ]
    }
  },
  {
    "id": "ndu-fnhs-master-human-nutrition",
    "faculty": "Faculty of Nursing and Health Sciences",
    "department": "Department of Nursing and Health Sciences",
    "major_category": "Nursing and Health Sciences",
    "major": "Master of Science in Human Nutrition",
    "degree_type": "MASTER",
    "official_degree_name": "Master of Science in Human Nutrition",
    "thesis_or_non_thesis": "THESIS_OR_NON_THESIS",
    "credits": 35,
    "duration_value": null,
    "duration_unit": null,
    "program_description": "The program advances nutrition research and dietetics practice through tailored mentorship, coursework, research training, and thesis work.",
    "official_program_url": "https://www.ndu.edu.lb/academics/faculties/fnhs/departments/department-of-nursing-and-health-sciences/programs/department-of-nursing-and-health-sciences/programs/master-of-science-in-human-nutrition",
    "primary_source_id": "NDU-SRC-003",
    "notes": "The program offers thesis and non-thesis paths and lists research areas including nutrition and psychology, biochemistry, clinical nutrition, sports nutrition, and public health nutrition.",
    "delivery_mode": "ON_CAMPUS",
    "language": null,
    "admission_requirements": "Applicants with a BS in Nutrition or a related field may apply. Other health-science backgrounds may need undergraduate nutrition courses. The page requires a minimum GPA of 3.0, allows conditional admission for 2.7 to 2.99, and may require an interview for non-NDU applicants.",
    "gre_requirement": null,
    "gmat_requirement": null,
    "interview_requirement": "Applicants whose undergraduate degree is not from NDU may be asked for an interview.",
    "experience_requirement": null,
    "accreditation": null,
    "source_ids": [
      "NDU-SRC-003",
      "NDU-SRC-002",
      "NDU-SRC-006",
      "NDU-SRC-008"
    ],
    "tuition": {
      "academic_year": "2025-2026",
      "currency": "USD",
      "billing_basis": "PER_CREDIT",
      "amount": 590,
      "category": "All Others",
      "notes": "Official NDU graduate tuition and fee table.",
      "source_ids": [
        "NDU-SRC-008"
      ]
    }
  },
  {
    "id": "ndu-fnas-master-industrial-chemistry",
    "faculty": "Faculty of Natural and Applied Sciences",
    "department": "Department of Sciences",
    "major_category": "Natural and Applied Sciences",
    "major": "Master of Science in Industrial Chemistry",
    "degree_type": "MASTER",
    "official_degree_name": "Master of Science in Industrial Chemistry",
    "thesis_or_non_thesis": "THESIS",
    "credits": 36,
    "duration_value": null,
    "duration_unit": null,
    "program_description": "The program prepares highly qualified students for applied chemistry careers and PhD study through interactive learning, hands-on research, and thesis work.",
    "official_program_url": "https://www.ndu.edu.lb/academics/faculties/fnas/departments/department-of-sciences/programs/master-of-science-in-industrial-chemistry",
    "primary_source_id": "NDU-SRC-003",
    "notes": "The visible page says the program is not offered at the present time.",
    "delivery_mode": "ON_CAMPUS",
    "language": null,
    "admission_requirements": "Applicants holding a BS in Chemistry with a cumulative GPA of at least 3.0 are accepted; 2.7 to 2.99 may be conditionally accepted. Chemistry remedial courses may be required, and up to 9 transfer credits may be approved.",
    "gre_requirement": null,
    "gmat_requirement": null,
    "interview_requirement": null,
    "experience_requirement": null,
    "accreditation": null,
    "source_ids": [
      "NDU-SRC-003",
      "NDU-SRC-002",
      "NDU-SRC-006",
      "NDU-SRC-008"
    ],
    "tuition": {
      "academic_year": "2025-2026",
      "currency": "USD",
      "billing_basis": "PER_CREDIT",
      "amount": 590,
      "category": "All Others",
      "notes": "Official NDU graduate tuition and fee table.",
      "source_ids": [
        "NDU-SRC-008"
      ]
    }
  },
  {
    "id": "ndu-fnas-master-mathematics",
    "faculty": "Faculty of Natural and Applied Sciences",
    "department": "Department of Mathematics and Statistics",
    "major_category": "Natural and Applied Sciences",
    "major": "Master of Science in Mathematics",
    "degree_type": "MASTER",
    "official_degree_name": "Master of Science in Mathematics",
    "thesis_or_non_thesis": "THESIS",
    "credits": 33,
    "duration_value": null,
    "duration_unit": null,
    "program_description": "The program provides advanced mathematical instruction, strengthens reasoning and analytical skills, and prepares graduates for teaching, research, and doctoral study.",
    "official_program_url": "https://www.ndu.edu.lb/academics/faculties/fnas/departments/department-of-mathematics-and-statistics/programs/master-of-science-in-mathematics",
    "primary_source_id": "NDU-SRC-003",
    "notes": "The page explicitly states that students communicate mathematical ideas effectively by completing a thesis.",
    "delivery_mode": "ON_CAMPUS",
    "language": null,
    "admission_requirements": "Applicants holding a BS in Mathematics with a cumulative GPA of at least 3.0 are accepted; 2.7 to 2.99 may be conditionally accepted. Mathematics remedial courses may be required, and up to 9 transfer credits may be approved.",
    "gre_requirement": null,
    "gmat_requirement": null,
    "interview_requirement": null,
    "experience_requirement": null,
    "accreditation": null,
    "source_ids": [
      "NDU-SRC-003",
      "NDU-SRC-002",
      "NDU-SRC-006",
      "NDU-SRC-008"
    ],
    "tuition": {
      "academic_year": "2025-2026",
      "currency": "USD",
      "billing_basis": "PER_CREDIT",
      "amount": 590,
      "category": "All Others",
      "notes": "Official NDU graduate tuition and fee table.",
      "source_ids": [
        "NDU-SRC-008"
      ]
    }
  },
  {
    "id": "ndu-fe-master-mechanical-engineering",
    "faculty": "Faculty of Engineering",
    "department": "Department of Mechanical Engineering",
    "major_category": "Engineering",
    "major": "Master of Science in Mechanical Engineering",
    "degree_type": "MASTER",
    "official_degree_name": "Master of Science in Mechanical Engineering",
    "thesis_or_non_thesis": null,
    "credits": 30,
    "duration_value": null,
    "duration_unit": null,
    "program_description": "The program builds advanced mechanical engineering knowledge and skills for professional practice and doctoral study.",
    "official_program_url": "https://www.ndu.edu.lb/academics/faculties/fe/departments/department-of-mechanical-engineering/programs/master-of-science-in-mechanical-engineering",
    "primary_source_id": "NDU-SRC-003",
    "notes": "The page does not publish a thesis requirement in the visible content.",
    "delivery_mode": "ON_CAMPUS",
    "language": null,
    "admission_requirements": "Applicants must hold a bachelor degree in Mechanical Engineering or its equivalent, have a minimum GPA of 3.0, obtain GRE scores, and receive Faculty Graduate Committee approval. English proficiency requirements in the catalog must also be met.",
    "gre_requirement": "Official GRE score required.",
    "gmat_requirement": null,
    "interview_requirement": null,
    "experience_requirement": null,
    "accreditation": null,
    "source_ids": [
      "NDU-SRC-003",
      "NDU-SRC-002",
      "NDU-SRC-006",
      "NDU-SRC-008"
    ],
    "tuition": {
      "academic_year": "2025-2026",
      "currency": "USD",
      "billing_basis": "PER_CREDIT",
      "amount": 765,
      "category": "Engineering",
      "notes": "Official NDU graduate tuition and fee table.",
      "source_ids": [
        "NDU-SRC-008"
      ]
    }
  },
  {
    "id": "ndu-faad-master-sustainable-architecture",
    "faculty": "Ramez G. Chagoury Faculty of Architecture, Arts, and Design",
    "department": "Department of Architecture",
    "major_category": "Architecture and Design",
    "major": "Masters of Arts in Sustainable Architecture",
    "degree_type": "MASTER",
    "official_degree_name": "Masters of Arts in Sustainable Architecture",
    "thesis_or_non_thesis": "THESIS",
    "credits": 30,
    "duration_value": null,
    "duration_unit": null,
    "program_description": "The program addresses the built environment and sustainability at the urban and building scales, with concentration work culminating in a thesis.",
    "official_program_url": "https://www.ndu.edu.lb/academics/faculties/faad/department-of-architecture/programs/masters-of-arts-in-sustainable-arachitecture",
    "primary_source_id": "NDU-SRC-003",
    "notes": "The page states that the urban design master is currently not offered and that both concentration paths end with thesis research.",
    "delivery_mode": "ON_CAMPUS",
    "language": null,
    "admission_requirements": "Applicants must submit a letter of intent and schedule an interview with the Department Graduate Committee. The program targets Architecture, Landscape Architecture, and Civil Engineering graduates, while other degrees are considered case by case. Full-time students must take at least 9 credits per semester.",
    "gre_requirement": null,
    "gmat_requirement": null,
    "interview_requirement": "Required with the Department Graduate Committee.",
    "experience_requirement": null,
    "accreditation": null,
    "source_ids": [
      "NDU-SRC-003",
      "NDU-SRC-002",
      "NDU-SRC-006",
      "NDU-SRC-008"
    ],
    "tuition": {
      "academic_year": "2025-2026",
      "currency": "USD",
      "billing_basis": "PER_CREDIT",
      "amount": 670,
      "category": "Architecture",
      "notes": "Official NDU graduate tuition and fee table.",
      "source_ids": [
        "NDU-SRC-008"
      ]
    }
  },
  {
    "id": "ndu-fbae-master-mba",
    "faculty": "Faculty of Business Administration and Economics",
    "department": "Department of Management and Marketing",
    "major_category": "Business",
    "major": "MBA - AACSB Accredited Program",
    "degree_type": "MASTER",
    "official_degree_name": "MBA - AACSB Accredited Program",
    "thesis_or_non_thesis": "THESIS_OR_NON_THESIS",
    "credits": 33,
    "duration_value": null,
    "duration_unit": null,
    "program_description": "The MBA program is designed for driven professionals seeking leadership, strategic management, and analytical business skills, with either a General MBA or a Project Management emphasis.",
    "official_program_url": "https://www.ndu.edu.lb/academics/faculties/fbae/departments/department-of-management-and-marketing/programs/mba---aacsb-accredited-program",
    "primary_source_id": "NDU-SRC-003",
    "notes": "The MBA page states that students may choose a Project Management emphasis or a General MBA and that the degree can be completed on a full- or part-time basis.",
    "delivery_mode": "ON_CAMPUS",
    "language": null,
    "admission_requirements": "Applicants must submit a completed application, two passport-sized photos, official transcripts with a minimum cumulative GPA of 2.7, two recommendation letters, a CV, and employment letters stating current position and years of service. Applicants from non-English instruction institutions need an EET score of 60. Non-business graduates may need up to 18 remedial credits.",
    "gre_requirement": null,
    "gmat_requirement": null,
    "interview_requirement": null,
    "experience_requirement": "Employment letters specifying current position and years of service are required.",
    "accreditation": "AACSB Accredited Program",
    "concentrations_or_tracks": [
      "Project Management"
    ],
    "source_ids": [
      "NDU-SRC-003",
      "NDU-SRC-002",
      "NDU-SRC-006",
      "NDU-SRC-008"
    ],
    "tuition": {
      "academic_year": "2025-2026",
      "currency": "USD",
      "billing_basis": "PER_CREDIT",
      "amount": 635,
      "category": "Business",
      "notes": "Official NDU graduate tuition and fee table.",
      "source_ids": [
        "NDU-SRC-008"
      ]
    }
  },
  {
    "id": "ndu-fbae-master-ms-business-strategy",
    "faculty": "Faculty of Business Administration and Economics",
    "department": "Department of Management and Marketing",
    "major_category": "Business",
    "major": "MS Business Strategy - AACSB Accredited Program",
    "degree_type": "MASTER",
    "official_degree_name": "MS Business Strategy - AACSB Accredited Program",
    "thesis_or_non_thesis": null,
    "credits": 33,
    "duration_value": null,
    "duration_unit": null,
    "program_description": "The program is a specialized business strategy degree designed for professionals, business graduates, and other graduates seeking strategic management skills and doctoral preparation.",
    "official_program_url": "https://www.ndu.edu.lb/academics/faculties/fbae/departments/department-of-management-and-marketing/programs/mba-in-project-management---aacsb-accredited-program",
    "primary_source_id": "NDU-SRC-003",
    "notes": "The visible page lists a 5:30 p.m. course schedule and a maximum load of 12 credits per semester. It does not publish GRE or GMAT requirements in the visible content.",
    "delivery_mode": "ON_CAMPUS",
    "language": null,
    "admission_requirements": "Business and economics graduates need a completed application, two recommendation letters, and a minimum undergraduate GPA of 2.7. Applicants from other disciplines may need up to 18 credits of remedial courses. Applicants from non-English instruction institutions need an EET score of 60.",
    "gre_requirement": null,
    "gmat_requirement": null,
    "interview_requirement": null,
    "experience_requirement": null,
    "accreditation": "AACSB Accredited Program",
    "concentrations_or_tracks": null,
    "source_ids": [
      "NDU-SRC-003",
      "NDU-SRC-002",
      "NDU-SRC-006",
      "NDU-SRC-008"
    ],
    "tuition": {
      "academic_year": "2025-2026",
      "currency": "USD",
      "billing_basis": "PER_CREDIT",
      "amount": 635,
      "category": "Business",
      "notes": "Official NDU graduate tuition and fee table.",
      "source_ids": [
        "NDU-SRC-008"
      ]
    }
  }
]$NDU_PROGRAMS$::jsonb) AS x(id TEXT, faculty TEXT, department TEXT, major_category TEXT, major TEXT, degree_type TEXT, official_degree_name TEXT, thesis_or_non_thesis TEXT, credits INTEGER, duration_value NUMERIC(10,2), duration_unit TEXT, program_description TEXT, official_program_url TEXT, primary_source_id TEXT, notes TEXT, delivery_mode TEXT, language TEXT, admission_requirements TEXT, gre_requirement TEXT, gmat_requirement TEXT, interview_requirement TEXT, experience_requirement TEXT, accreditation TEXT, concentrations_or_tracks JSONB, source_ids JSONB, tuition JSONB);

    INSERT INTO graduate_program (university_id, faculty_id, department_id, degree_type_id, program_key, major_category, major, official_degree_name, thesis_or_non_thesis, credits, duration_value, duration_unit, primary_language_id, delivery_mode, program_description, official_program_url, source_id, notes)
    SELECT v_university_id, fac.id, dep.id, dt.id, seed.id, seed.major_category, seed.major, seed.official_degree_name, seed.thesis_or_non_thesis, seed.credits, seed.duration_value, seed.duration_unit, NULL::BIGINT, seed.delivery_mode, seed.program_description, seed.official_program_url, s.id, seed.notes
    FROM ndu_program_seed seed
    JOIN university_faculty fac ON fac.university_id = v_university_id AND fac.name = seed.faculty
    JOIN university_department dep ON dep.university_id = v_university_id AND dep.name = seed.department AND dep.faculty_id = fac.id
    JOIN degree_type dt ON dt.code = seed.degree_type
    JOIN ndu_source_seed ss ON ss.source_id = seed.primary_source_id
    JOIN source s ON s.university_id = v_university_id AND s.url = ss.url
    ON CONFLICT (university_id, program_key) DO UPDATE SET faculty_id = EXCLUDED.faculty_id, department_id = EXCLUDED.department_id, degree_type_id = EXCLUDED.degree_type_id, major_category = EXCLUDED.major_category, major = EXCLUDED.major, official_degree_name = EXCLUDED.official_degree_name, thesis_or_non_thesis = EXCLUDED.thesis_or_non_thesis, credits = EXCLUDED.credits, duration_value = EXCLUDED.duration_value, duration_unit = EXCLUDED.duration_unit, primary_language_id = EXCLUDED.primary_language_id, delivery_mode = EXCLUDED.delivery_mode, program_description = EXCLUDED.program_description, official_program_url = EXCLUDED.official_program_url, source_id = EXCLUDED.source_id, notes = EXCLUDED.notes, updated_at = NOW();

    INSERT INTO graduate_tuition_rate (university_id, faculty_id, department_id, program_id, scope_level, record_key, academic_year, currency, billing_basis, amount, category, notes, source_id)
    SELECT v_university_id, fac.id, dep.id, gp.id, 'PROGRAM', seed.id || ':tuition:2025-2026', seed.tuition->>'academic_year', seed.tuition->>'currency', seed.tuition->>'billing_basis', (seed.tuition->>'amount')::NUMERIC(12,2), seed.tuition->>'category', seed.tuition->>'notes', s.id
    FROM ndu_program_seed seed
    JOIN graduate_program gp ON gp.university_id = v_university_id AND gp.program_key = seed.id
    JOIN university_faculty fac ON fac.university_id = v_university_id AND fac.name = seed.faculty
    JOIN university_department dep ON dep.university_id = v_university_id AND dep.name = seed.department AND dep.faculty_id = fac.id
    JOIN ndu_source_seed ss ON ss.source_id = seed.tuition->'source_ids'->>0
    JOIN source s ON s.university_id = v_university_id AND s.url = ss.url
    ON CONFLICT (university_id, record_key) DO UPDATE SET faculty_id = EXCLUDED.faculty_id, department_id = EXCLUDED.department_id, program_id = EXCLUDED.program_id, scope_level = EXCLUDED.scope_level, academic_year = EXCLUDED.academic_year, currency = EXCLUDED.currency, billing_basis = EXCLUDED.billing_basis, amount = EXCLUDED.amount, category = EXCLUDED.category, notes = EXCLUDED.notes, source_id = EXCLUDED.source_id, updated_at = NOW();

    INSERT INTO graduate_fee_item (university_id, faculty_id, department_id, program_id, scope_level, record_key, academic_year, fee_name, billing_basis, currency, amount, category, notes, source_id)
    SELECT v_university_id, NULL, NULL, NULL, 'UNIVERSITY', record_key, academic_year, fee_name, billing_basis, currency, amount, category, x.notes, s.id
    FROM jsonb_to_recordset($NDU_FEES$[
  {
    "record_key": "ndu:fee_item:application_fee",
    "academic_year": "2025-2026",
    "fee_name": "Application Fee",
    "billing_basis": "FLAT_FEE",
    "currency": "USD",
    "amount": 30,
    "category": "Admissions",
    "notes": "Application fee may be paid online or on campus.",
    "source_id": "NDU-SRC-005"
  },
  {
    "record_key": "ndu:fee_item:first_payment",
    "academic_year": "2025-2026",
    "fee_name": "First Payment",
    "billing_basis": "FLAT_FEE",
    "currency": "USD",
    "amount": 1050,
    "category": "Finance",
    "notes": "Published first payment due at registration.",
    "source_id": "NDU-SRC-008"
  },
  {
    "record_key": "ndu:fee_item:technology_fee_fall",
    "academic_year": "2025-2026",
    "fee_name": "Technology and Student Services Fee - Fall Semester",
    "billing_basis": "PER_TERM",
    "currency": "USD",
    "amount": 450,
    "category": "Technology and Student Services",
    "notes": "Published fall semester amount.",
    "source_id": "NDU-SRC-008"
  },
  {
    "record_key": "ndu:fee_item:technology_fee_spring",
    "academic_year": "2025-2026",
    "fee_name": "Technology and Student Services Fee - Spring Semester",
    "billing_basis": "PER_TERM",
    "currency": "USD",
    "amount": 450,
    "category": "Technology and Student Services",
    "notes": "Published spring semester amount.",
    "source_id": "NDU-SRC-008"
  },
  {
    "record_key": "ndu:fee_item:technology_fee_summer",
    "academic_year": "2025-2026",
    "fee_name": "Technology and Student Services Fee - Summer Session",
    "billing_basis": "PER_TERM",
    "currency": "USD",
    "amount": 200,
    "category": "Technology and Student Services",
    "notes": "Published summer session amount.",
    "source_id": "NDU-SRC-008"
  },
  {
    "record_key": "ndu:fee_item:late_registration_fee",
    "academic_year": "2025-2026",
    "fee_name": "Late Registration Fee",
    "billing_basis": "FLAT_FEE",
    "currency": "USD",
    "amount": 100,
    "category": "Finance",
    "notes": null,
    "source_id": "NDU-SRC-008"
  },
  {
    "record_key": "ndu:fee_item:late_payment_fee",
    "academic_year": "2025-2026",
    "fee_name": "Late Payment Fee",
    "billing_basis": "FLAT_FEE",
    "currency": "USD",
    "amount": 75,
    "category": "Finance",
    "notes": null,
    "source_id": "NDU-SRC-008"
  },
  {
    "record_key": "ndu:fee_item:medical_fee_new_students",
    "academic_year": "2025-2026",
    "fee_name": "Medical Fee for New Students",
    "billing_basis": "FLAT_FEE",
    "currency": "USD",
    "amount": 60,
    "category": "Student Services",
    "notes": null,
    "source_id": "NDU-SRC-008"
  },
  {
    "record_key": "ndu:fee_item:smart_id_card_fee",
    "academic_year": "2025-2026",
    "fee_name": "Smart ID Card Fee",
    "billing_basis": "FLAT_FEE",
    "currency": "USD",
    "amount": 20,
    "category": "Student Services",
    "notes": null,
    "source_id": "NDU-SRC-008"
  },
  {
    "record_key": "ndu:fee_item:statement_of_fees_fee",
    "academic_year": "2025-2026",
    "fee_name": "Statement of Fees Fee",
    "billing_basis": "FLAT_FEE",
    "currency": "USD",
    "amount": 10,
    "category": "Student Services",
    "notes": null,
    "source_id": "NDU-SRC-008"
  },
  {
    "record_key": "ndu:fee_item:transcript_fee",
    "academic_year": "2025-2026",
    "fee_name": "Transcript Fee",
    "billing_basis": "FLAT_FEE",
    "currency": "USD",
    "amount": 10,
    "category": "Student Services",
    "notes": null,
    "source_id": "NDU-SRC-008"
  },
  {
    "record_key": "ndu:fee_item:entrance_exam_one",
    "academic_year": "2025-2026",
    "fee_name": "Entrance Examination Fee - One Test",
    "billing_basis": "FLAT_FEE",
    "currency": "USD",
    "amount": 30,
    "category": "Admissions",
    "notes": null,
    "source_id": "NDU-SRC-008"
  },
  {
    "record_key": "ndu:fee_item:entrance_exam_two",
    "academic_year": "2025-2026",
    "fee_name": "Entrance Examination Fee - Two Tests",
    "billing_basis": "FLAT_FEE",
    "currency": "USD",
    "amount": 50,
    "category": "Admissions",
    "notes": null,
    "source_id": "NDU-SRC-008"
  }
]$NDU_FEES$::jsonb) AS x(record_key TEXT, academic_year TEXT, fee_name TEXT, billing_basis TEXT, currency TEXT, amount NUMERIC(12,2), category TEXT, notes TEXT, source_id TEXT)
    JOIN ndu_source_seed ss ON ss.source_id = x.source_id
    JOIN source s ON s.university_id = v_university_id AND s.url = ss.url
    ON CONFLICT (university_id, record_key) DO UPDATE SET academic_year = EXCLUDED.academic_year, fee_name = EXCLUDED.fee_name, billing_basis = EXCLUDED.billing_basis, currency = EXCLUDED.currency, amount = EXCLUDED.amount, category = EXCLUDED.category, notes = EXCLUDED.notes, source_id = EXCLUDED.source_id, updated_at = NOW();

    INSERT INTO graduate_admission_requirement (university_id, faculty_id, department_id, program_id, scope_level, record_key, requirement_type, requirement_text, comparison_operator, threshold_value, threshold_unit, is_required, notes, source_id)
    SELECT v_university_id, NULL, NULL, NULL, scope_level, record_key, requirement_type, requirement_text, comparison_operator, threshold_value, threshold_unit, is_required, x.notes, s.id
    FROM jsonb_to_recordset($NDU_ADMISSIONS$[
  {
    "record_key": "ndu:admission:university:general",
    "scope_level": "UNIVERSITY",
    "requirement_type": "GENERAL",
    "requirement_text": "Graduate applicants apply online through SIS, pay the USD 30 application fee, and submit the required documents before the relevant admission decision date. Faculty- or program-specific additional requirements may also apply.",
    "comparison_operator": null,
    "threshold_value": null,
    "threshold_unit": null,
    "is_required": true,
    "notes": null,
    "source_id": "NDU-SRC-002"
  },
  {
    "record_key": "ndu:admission:university:english",
    "scope_level": "UNIVERSITY",
    "requirement_type": "ENGLISH",
    "requirement_text": "NDU graduates are exempt from the English Entrance Test. All other graduate applicants must submit TOEFL, SAT, or IELTS scores, or sit for the NDU EET.",
    "comparison_operator": null,
    "threshold_value": null,
    "threshold_unit": null,
    "is_required": true,
    "notes": null,
    "source_id": "NDU-SRC-002"
  },
  {
    "record_key": "ndu:admission:ndu-faad-master-design:general",
    "scope_level": "PROGRAM",
    "requirement_type": "GENERAL",
    "requirement_text": "Applicants from visual arts and design backgrounds are preferred. Candidates from other disciplines may be considered after meeting University graduate admission requirements. A portfolio and an interview with the MA course faculty are required. Full-time candidates must take at least 9 credits per semester; part-time candidates must take at least 6.",
    "comparison_operator": null,
    "threshold_value": null,
    "threshold_unit": null,
    "is_required": true,
    "notes": "The page specifies both part-time and full-time study loads.",
    "source_id": "NDU-SRC-006"
  },
  {
    "record_key": "ndu:admission:ndu-faad-master-design:interview",
    "scope_level": "PROGRAM",
    "requirement_type": "INTERVIEW",
    "requirement_text": "Required with the MA course faculty.",
    "comparison_operator": null,
    "threshold_value": null,
    "threshold_unit": null,
    "is_required": true,
    "notes": "The page specifies both part-time and full-time study loads.",
    "source_id": "NDU-SRC-006"
  },
  {
    "record_key": "ndu:admission:ndu-fh-master-education:general",
    "scope_level": "PROGRAM",
    "requirement_type": "GENERAL",
    "requirement_text": "Applicants should hold a BA in Education or a BA with a Teaching Diploma from an accredited university. Related bachelor’s degrees may be considered case by case by the department.",
    "comparison_operator": null,
    "threshold_value": null,
    "threshold_unit": null,
    "is_required": true,
    "notes": "The graduation requirements include a thesis.",
    "source_id": "NDU-SRC-006"
  },
  {
    "record_key": "ndu:admission:ndu-fh-master-english-language-literature-applied-linguistics-tefl:general",
    "scope_level": "PROGRAM",
    "requirement_type": "GENERAL",
    "requirement_text": "Applicants are expected to show strong English proficiency; the page gives priority to applicants with added qualifications and professional experience such as teaching.",
    "comparison_operator": null,
    "threshold_value": null,
    "threshold_unit": null,
    "is_required": true,
    "notes": "The page requires a minimum EET score of 60 for admission and states that GRE performance is required only if an applicant submits GRE results.",
    "source_id": "NDU-SRC-006"
  },
  {
    "record_key": "ndu:admission:ndu-flps-master-international-affairs-diplomacy:general",
    "scope_level": "PROGRAM",
    "requirement_type": "GENERAL",
    "requirement_text": "Applicants should hold a BA in Political Science, Public Administration, International Affairs and Diplomacy, International Law, or another related field. Non-major applicants may be asked to complete prerequisite courses.",
    "comparison_operator": null,
    "threshold_value": null,
    "threshold_unit": null,
    "is_required": true,
    "notes": "The program page allows either comprehensive exam coursework or 30 credits of coursework plus 6 thesis credits.",
    "source_id": "NDU-SRC-006"
  },
  {
    "record_key": "ndu:admission:ndu-flps-master-international-affairs-diplomacy-international-law:general",
    "scope_level": "PROGRAM",
    "requirement_type": "GENERAL",
    "requirement_text": "Applicants should hold a BA in Political Science, Public Administration, International Affairs and Diplomacy, International Law, or another related field. Non-major applicants may be asked to complete prerequisite courses.",
    "comparison_operator": null,
    "threshold_value": null,
    "threshold_unit": null,
    "is_required": true,
    "notes": "The emphasis-specific prerequisite list includes IAF 211, IAF 401, and POS 442 or equivalents.",
    "source_id": "NDU-SRC-006"
  },
  {
    "record_key": "ndu:admission:ndu-fh-master-media-studies:general",
    "scope_level": "PROGRAM",
    "requirement_type": "GENERAL",
    "requirement_text": "Applicants may enter from the fields listed by the department. Unrelated majors may need preparatory courses. The page states a minimum GPA of 3.0, with probationary admission possible for GPAs between 2.8 and 3.0.",
    "comparison_operator": null,
    "threshold_value": null,
    "threshold_unit": null,
    "is_required": true,
    "notes": "Applicants must submit three recommendation letters, an updated CV, and a personal statement.",
    "source_id": "NDU-SRC-006"
  },
  {
    "record_key": "ndu:admission:ndu-faad-master-music:general",
    "scope_level": "PROGRAM",
    "requirement_type": "GENERAL",
    "requirement_text": "Applicants normally hold a bachelor degree in music or an equivalent qualification. They must submit an extended written piece on a musical subject. An English test is required except for students majoring in Arabic music.",
    "comparison_operator": null,
    "threshold_value": null,
    "threshold_unit": null,
    "is_required": true,
    "notes": "The page does not explicitly publish a thesis requirement in the visible content.",
    "source_id": "NDU-SRC-006"
  },
  {
    "record_key": "ndu:admission:ndu-faad-master-music:interview",
    "scope_level": "PROGRAM",
    "requirement_type": "INTERVIEW",
    "requirement_text": "Selection is based on applicant information and, when necessary, an interview.",
    "comparison_operator": null,
    "threshold_value": null,
    "threshold_unit": null,
    "is_required": true,
    "notes": "The page does not explicitly publish a thesis requirement in the visible content.",
    "source_id": "NDU-SRC-006"
  },
  {
    "record_key": "ndu:admission:ndu-flps-master-political-science:general",
    "scope_level": "PROGRAM",
    "requirement_type": "GENERAL",
    "requirement_text": "Applicants should hold a BA in Political Science, Public Administration, International Affairs and Diplomacy, International Law, or another related field. The page requires the EET minimum, a minimum undergraduate GPA of 3.0, and may require GRE for non-NDU students. Non-major applicants may need prerequisite courses.",
    "comparison_operator": null,
    "threshold_value": null,
    "threshold_unit": null,
    "is_required": true,
    "notes": "The program page allows either a comprehensive written and oral examination or 30 credits of coursework plus 6 thesis credits.",
    "source_id": "NDU-SRC-006"
  },
  {
    "record_key": "ndu:admission:ndu-flps-master-political-science-human-rights:general",
    "scope_level": "PROGRAM",
    "requirement_type": "GENERAL",
    "requirement_text": "Applicants should hold a BA in Political Science, Public Administration, International Affairs and Diplomacy, International Law, or another related field. The page requires the EET minimum, a minimum undergraduate GPA of 3.0, and may require GRE for non-NDU students. Non-major applicants may need prerequisite courses.",
    "comparison_operator": null,
    "threshold_value": null,
    "threshold_unit": null,
    "is_required": true,
    "notes": "The visible page content names prerequisites for the political science track and allows the same thesis/non-thesis path structure as the main program.",
    "source_id": "NDU-SRC-006"
  },
  {
    "record_key": "ndu:admission:ndu-flps-master-political-science-ngos:general",
    "scope_level": "PROGRAM",
    "requirement_type": "GENERAL",
    "requirement_text": "Applicants should hold a BA in Political Science, Public Administration, International Affairs and Diplomacy, International Law, or another related field. The page requires the EET minimum, a minimum undergraduate GPA of 3.0, and may require GRE for non-NDU students. Non-major applicants may need prerequisite courses.",
    "comparison_operator": null,
    "threshold_value": null,
    "threshold_unit": null,
    "is_required": true,
    "notes": "The visible page content includes program-specific prerequisite lists for Political Science, Public Administration, International Affairs and Diplomacy, and International Affairs and Diplomacy - International Law Emphasis.",
    "source_id": "NDU-SRC-006"
  },
  {
    "record_key": "ndu:admission:ndu-fh-master-psychology-educational:general",
    "scope_level": "PROGRAM",
    "requirement_type": "GENERAL",
    "requirement_text": "Applicants should hold a BA in Psychology or an equivalent degree from an accredited university. Other majors are evaluated separately. The page also requires a minimum undergraduate GPA of 2.75, a personal statement, and three professional recommendations.",
    "comparison_operator": null,
    "threshold_value": null,
    "threshold_unit": null,
    "is_required": true,
    "notes": "Graduation may follow either a thesis track or a non-thesis track.",
    "source_id": "NDU-SRC-006"
  },
  {
    "record_key": "ndu:admission:ndu-fh-master-psychology-educational:interview",
    "scope_level": "PROGRAM",
    "requirement_type": "INTERVIEW",
    "requirement_text": "A personal interview is at the discretion of the department.",
    "comparison_operator": null,
    "threshold_value": null,
    "threshold_unit": null,
    "is_required": true,
    "notes": "Graduation may follow either a thesis track or a non-thesis track.",
    "source_id": "NDU-SRC-006"
  },
  {
    "record_key": "ndu:admission:ndu-flps-master-public-administration:general",
    "scope_level": "PROGRAM",
    "requirement_type": "GENERAL",
    "requirement_text": "Applicants should hold a BA in Political Science, Public Administration, International Affairs and Diplomacy, International Law, or another related field. The page requires the EET minimum and a minimum undergraduate GPA of 3.0. Non-major applicants may need prerequisite courses.",
    "comparison_operator": null,
    "threshold_value": null,
    "threshold_unit": null,
    "is_required": true,
    "notes": "The graduation page allows either comprehensive examination coursework or 30 credits plus 6 thesis credits.",
    "source_id": "NDU-SRC-006"
  },
  {
    "record_key": "ndu:admission:ndu-fh-master-translation:general",
    "scope_level": "PROGRAM",
    "requirement_type": "GENERAL",
    "requirement_text": "Applicants must pass written French and Arabic language proficiency tests with a grade of 70 or above in both. An interview in English, French, and Arabic is also required. Applicants with a bachelor’s degree other than Translation must take TRA 201 and TRA 311 or TRA 401 in the first semester and earn a grade of B or higher.",
    "comparison_operator": null,
    "threshold_value": null,
    "threshold_unit": null,
    "is_required": true,
    "notes": "The graduation requirements include a thesis.",
    "source_id": "NDU-SRC-006"
  },
  {
    "record_key": "ndu:admission:ndu-fh-master-translation:interview",
    "scope_level": "PROGRAM",
    "requirement_type": "INTERVIEW",
    "requirement_text": "Required in English, French, and Arabic.",
    "comparison_operator": null,
    "threshold_value": null,
    "threshold_unit": null,
    "is_required": true,
    "notes": "The graduation requirements include a thesis.",
    "source_id": "NDU-SRC-006"
  },
  {
    "record_key": "ndu:admission:ndu-fnas-master-actuarial-sciences:general",
    "scope_level": "PROGRAM",
    "requirement_type": "GENERAL",
    "requirement_text": "Applicants holding a BS in Mathematics or Actuarial Sciences with a cumulative GPA of at least 3.0 are accepted; 2.7 to 2.99 may be conditionally accepted. Remedial courses may be required. Up to 9 transfer credits may be approved.",
    "comparison_operator": null,
    "threshold_value": null,
    "threshold_unit": null,
    "is_required": true,
    "notes": "The official page emphasizes preparation for actuarial professional examinations and the partnership with Chedid Re.",
    "source_id": "NDU-SRC-006"
  },
  {
    "record_key": "ndu:admission:ndu-fnas-master-biology:general",
    "scope_level": "PROGRAM",
    "requirement_type": "GENERAL",
    "requirement_text": "Applicants holding a BS in Biology with a cumulative GPA of at least 3.0 are accepted; 2.7 to 2.99 may be conditionally accepted. Remedial biology courses may be required, and up to 9 transfer credits may be approved.",
    "comparison_operator": null,
    "threshold_value": null,
    "threshold_unit": null,
    "is_required": true,
    "notes": "The graduation requirements include 6 thesis credits.",
    "source_id": "NDU-SRC-006"
  },
  {
    "record_key": "ndu:admission:ndu-fbae-master-business-strategy:general",
    "scope_level": "PROGRAM",
    "requirement_type": "GENERAL",
    "requirement_text": "Business and economics graduates need a completed application, two recommendation letters, and a minimum undergraduate GPA of 2.7. Applicants from other disciplines may need up to 18 credits of remedial courses. Applicants from non-English instruction institutions need an EET score of 60.",
    "comparison_operator": null,
    "threshold_value": null,
    "threshold_unit": null,
    "is_required": true,
    "notes": "The visible page lists a 5:30 p.m. course schedule and a maximum load of 12 credits per semester. It does not publish GRE or GMAT requirements in the visible content.",
    "source_id": "NDU-SRC-006"
  },
  {
    "record_key": "ndu:admission:ndu-fe-master-civil-engineering:general",
    "scope_level": "PROGRAM",
    "requirement_type": "GENERAL",
    "requirement_text": "Applicants must hold a bachelor degree in Civil Engineering or its equivalent, have a minimum GPA of 3.0, obtain GRE scores, and receive Faculty Graduate Committee approval. English proficiency requirements in the catalog must also be met.",
    "comparison_operator": null,
    "threshold_value": null,
    "threshold_unit": null,
    "is_required": true,
    "notes": "Holders of BS degrees in Civil Engineering may need remedial courses on a case-by-case basis.",
    "source_id": "NDU-SRC-006"
  },
  {
    "record_key": "ndu:admission:ndu-fe-master-civil-engineering:gre",
    "scope_level": "PROGRAM",
    "requirement_type": "GRE",
    "requirement_text": "Official GRE score required.",
    "comparison_operator": null,
    "threshold_value": null,
    "threshold_unit": null,
    "is_required": true,
    "notes": "Holders of BS degrees in Civil Engineering may need remedial courses on a case-by-case basis.",
    "source_id": "NDU-SRC-006"
  },
  {
    "record_key": "ndu:admission:ndu-fnas-master-computer-science:general",
    "scope_level": "PROGRAM",
    "requirement_type": "GENERAL",
    "requirement_text": "Applicants holding a BS in Computer Science with a cumulative GPA of at least 3.0 are accepted; 2.7 to 2.99 may be conditionally accepted. Remedial undergraduate computer science courses may be required, and up to 9 transfer credits may be approved.",
    "comparison_operator": null,
    "threshold_value": null,
    "threshold_unit": null,
    "is_required": true,
    "notes": "The page lists career tracks including software development, data science, cybersecurity, cloud computing, and research roles.",
    "source_id": "NDU-SRC-006"
  },
  {
    "record_key": "ndu:admission:ndu-fe-master-electrical-computer-engineering:general",
    "scope_level": "PROGRAM",
    "requirement_type": "GENERAL",
    "requirement_text": "Applicants must hold a bachelor degree in Electrical/Computer Engineering or its equivalent, have a minimum GPA of 3.0, obtain GRE scores, and receive Faculty Graduate Committee approval. English proficiency requirements in the catalog must also be met.",
    "comparison_operator": null,
    "threshold_value": null,
    "threshold_unit": null,
    "is_required": true,
    "notes": "Holders of BE degrees may transfer up to 18 undergraduate major elective credits if the courses meet the page conditions.",
    "source_id": "NDU-SRC-006"
  },
  {
    "record_key": "ndu:admission:ndu-fe-master-electrical-computer-engineering:gre",
    "scope_level": "PROGRAM",
    "requirement_type": "GRE",
    "requirement_text": "Official GRE score required.",
    "comparison_operator": null,
    "threshold_value": null,
    "threshold_unit": null,
    "is_required": true,
    "notes": "Holders of BE degrees may transfer up to 18 undergraduate major elective credits if the courses meet the page conditions.",
    "source_id": "NDU-SRC-006"
  },
  {
    "record_key": "ndu:admission:ndu-fbae-master-financial-risk-management:general",
    "scope_level": "PROGRAM",
    "requirement_type": "GENERAL",
    "requirement_text": "Business and economics graduates must submit a completed application, two recommendation letters, and show a cumulative average of 80% or 3.0/4.0. Other scientific discipline graduates may be admitted with relevant undergraduate business courses. Applicants from non-English instruction institutions need an EET score of 60.",
    "comparison_operator": null,
    "threshold_value": null,
    "threshold_unit": null,
    "is_required": true,
    "notes": "The page states a 30-credit program and a per-credit tuition price of USD 635.",
    "source_id": "NDU-SRC-006"
  },
  {
    "record_key": "ndu:admission:ndu-fnhs-master-food-safety-quality-management:general",
    "scope_level": "PROGRAM",
    "requirement_type": "GENERAL",
    "requirement_text": "Applicants with a BS in Food Safety and Quality Management or a related field may apply. Other health-science backgrounds may need undergraduate remedial courses. The page requires a minimum GPA of 3.0, allows conditional admission for 2.7 to 2.99, and may require an interview for non-NDU applicants.",
    "comparison_operator": null,
    "threshold_value": null,
    "threshold_unit": null,
    "is_required": true,
    "notes": "The page offers both thesis and applied non-thesis study paths.",
    "source_id": "NDU-SRC-006"
  },
  {
    "record_key": "ndu:admission:ndu-fnhs-master-food-safety-quality-management:interview",
    "scope_level": "PROGRAM",
    "requirement_type": "INTERVIEW",
    "requirement_text": "Applicants whose undergraduate degree is not from NDU may be asked for an interview.",
    "comparison_operator": null,
    "threshold_value": null,
    "threshold_unit": null,
    "is_required": true,
    "notes": "The page offers both thesis and applied non-thesis study paths.",
    "source_id": "NDU-SRC-006"
  },
  {
    "record_key": "ndu:admission:ndu-fnhs-master-human-nutrition:general",
    "scope_level": "PROGRAM",
    "requirement_type": "GENERAL",
    "requirement_text": "Applicants with a BS in Nutrition or a related field may apply. Other health-science backgrounds may need undergraduate nutrition courses. The page requires a minimum GPA of 3.0, allows conditional admission for 2.7 to 2.99, and may require an interview for non-NDU applicants.",
    "comparison_operator": null,
    "threshold_value": null,
    "threshold_unit": null,
    "is_required": true,
    "notes": "The program offers thesis and non-thesis paths and lists research areas including nutrition and psychology, biochemistry, clinical nutrition, sports nutrition, and public health nutrition.",
    "source_id": "NDU-SRC-006"
  },
  {
    "record_key": "ndu:admission:ndu-fnhs-master-human-nutrition:interview",
    "scope_level": "PROGRAM",
    "requirement_type": "INTERVIEW",
    "requirement_text": "Applicants whose undergraduate degree is not from NDU may be asked for an interview.",
    "comparison_operator": null,
    "threshold_value": null,
    "threshold_unit": null,
    "is_required": true,
    "notes": "The program offers thesis and non-thesis paths and lists research areas including nutrition and psychology, biochemistry, clinical nutrition, sports nutrition, and public health nutrition.",
    "source_id": "NDU-SRC-006"
  },
  {
    "record_key": "ndu:admission:ndu-fnas-master-industrial-chemistry:general",
    "scope_level": "PROGRAM",
    "requirement_type": "GENERAL",
    "requirement_text": "Applicants holding a BS in Chemistry with a cumulative GPA of at least 3.0 are accepted; 2.7 to 2.99 may be conditionally accepted. Chemistry remedial courses may be required, and up to 9 transfer credits may be approved.",
    "comparison_operator": null,
    "threshold_value": null,
    "threshold_unit": null,
    "is_required": true,
    "notes": "The visible page says the program is not offered at the present time.",
    "source_id": "NDU-SRC-006"
  },
  {
    "record_key": "ndu:admission:ndu-fnas-master-mathematics:general",
    "scope_level": "PROGRAM",
    "requirement_type": "GENERAL",
    "requirement_text": "Applicants holding a BS in Mathematics with a cumulative GPA of at least 3.0 are accepted; 2.7 to 2.99 may be conditionally accepted. Mathematics remedial courses may be required, and up to 9 transfer credits may be approved.",
    "comparison_operator": null,
    "threshold_value": null,
    "threshold_unit": null,
    "is_required": true,
    "notes": "The page explicitly states that students communicate mathematical ideas effectively by completing a thesis.",
    "source_id": "NDU-SRC-006"
  },
  {
    "record_key": "ndu:admission:ndu-fe-master-mechanical-engineering:general",
    "scope_level": "PROGRAM",
    "requirement_type": "GENERAL",
    "requirement_text": "Applicants must hold a bachelor degree in Mechanical Engineering or its equivalent, have a minimum GPA of 3.0, obtain GRE scores, and receive Faculty Graduate Committee approval. English proficiency requirements in the catalog must also be met.",
    "comparison_operator": null,
    "threshold_value": null,
    "threshold_unit": null,
    "is_required": true,
    "notes": "The page does not publish a thesis requirement in the visible content.",
    "source_id": "NDU-SRC-006"
  },
  {
    "record_key": "ndu:admission:ndu-fe-master-mechanical-engineering:gre",
    "scope_level": "PROGRAM",
    "requirement_type": "GRE",
    "requirement_text": "Official GRE score required.",
    "comparison_operator": null,
    "threshold_value": null,
    "threshold_unit": null,
    "is_required": true,
    "notes": "The page does not publish a thesis requirement in the visible content.",
    "source_id": "NDU-SRC-006"
  },
  {
    "record_key": "ndu:admission:ndu-faad-master-sustainable-architecture:general",
    "scope_level": "PROGRAM",
    "requirement_type": "GENERAL",
    "requirement_text": "Applicants must submit a letter of intent and schedule an interview with the Department Graduate Committee. The program targets Architecture, Landscape Architecture, and Civil Engineering graduates, while other degrees are considered case by case. Full-time students must take at least 9 credits per semester.",
    "comparison_operator": null,
    "threshold_value": null,
    "threshold_unit": null,
    "is_required": true,
    "notes": "The page states that the urban design master is currently not offered and that both concentration paths end with thesis research.",
    "source_id": "NDU-SRC-006"
  },
  {
    "record_key": "ndu:admission:ndu-faad-master-sustainable-architecture:interview",
    "scope_level": "PROGRAM",
    "requirement_type": "INTERVIEW",
    "requirement_text": "Required with the Department Graduate Committee.",
    "comparison_operator": null,
    "threshold_value": null,
    "threshold_unit": null,
    "is_required": true,
    "notes": "The page states that the urban design master is currently not offered and that both concentration paths end with thesis research.",
    "source_id": "NDU-SRC-006"
  },
  {
    "record_key": "ndu:admission:ndu-fbae-master-mba:general",
    "scope_level": "PROGRAM",
    "requirement_type": "GENERAL",
    "requirement_text": "Applicants must submit a completed application, two passport-sized photos, official transcripts with a minimum cumulative GPA of 2.7, two recommendation letters, a CV, and employment letters stating current position and years of service. Applicants from non-English instruction institutions need an EET score of 60. Non-business graduates may need up to 18 remedial credits.",
    "comparison_operator": null,
    "threshold_value": null,
    "threshold_unit": null,
    "is_required": true,
    "notes": "The MBA page states that students may choose a Project Management emphasis or a General MBA and that the degree can be completed on a full- or part-time basis.",
    "source_id": "NDU-SRC-006"
  },
  {
    "record_key": "ndu:admission:ndu-fbae-master-mba:experience",
    "scope_level": "PROGRAM",
    "requirement_type": "EXPERIENCE",
    "requirement_text": "Employment letters specifying current position and years of service are required.",
    "comparison_operator": null,
    "threshold_value": null,
    "threshold_unit": null,
    "is_required": true,
    "notes": "The MBA page states that students may choose a Project Management emphasis or a General MBA and that the degree can be completed on a full- or part-time basis.",
    "source_id": "NDU-SRC-006"
  },
  {
    "record_key": "ndu:admission:ndu-fbae-master-ms-business-strategy:general",
    "scope_level": "PROGRAM",
    "requirement_type": "GENERAL",
    "requirement_text": "Business and economics graduates need a completed application, two recommendation letters, and a minimum undergraduate GPA of 2.7. Applicants from other disciplines may need up to 18 credits of remedial courses. Applicants from non-English instruction institutions need an EET score of 60.",
    "comparison_operator": null,
    "threshold_value": null,
    "threshold_unit": null,
    "is_required": true,
    "notes": "The visible page lists a 5:30 p.m. course schedule and a maximum load of 12 credits per semester. It does not publish GRE or GMAT requirements in the visible content.",
    "source_id": "NDU-SRC-006"
  }
]$NDU_ADMISSIONS$::jsonb) AS x(scope_level TEXT, record_key TEXT, requirement_type TEXT, requirement_text TEXT, comparison_operator TEXT, threshold_value NUMERIC(12,2), threshold_unit TEXT, is_required BOOLEAN, notes TEXT, source_id TEXT)
    JOIN ndu_source_seed ss ON ss.source_id = x.source_id
    JOIN source s ON s.university_id = v_university_id AND s.url = ss.url
    ON CONFLICT (university_id, record_key) DO UPDATE SET scope_level = EXCLUDED.scope_level, requirement_type = EXCLUDED.requirement_type, requirement_text = EXCLUDED.requirement_text, comparison_operator = EXCLUDED.comparison_operator, threshold_value = EXCLUDED.threshold_value, threshold_unit = EXCLUDED.threshold_unit, is_required = EXCLUDED.is_required, notes = EXCLUDED.notes, source_id = EXCLUDED.source_id, updated_at = NOW();

    INSERT INTO graduate_required_document (university_id, faculty_id, department_id, program_id, scope_level, record_key, document_type, document_name, is_optional, sort_order, notes, source_id)
    SELECT v_university_id, NULL, NULL, NULL, 'UNIVERSITY', record_key, document_type, document_name, is_optional, sort_order, x.notes, s.id
    FROM jsonb_to_recordset($NDU_DOCUMENTS$[
  {
    "record_key": "ndu:document:1",
    "document_type": "DEGREE",
    "document_name": "Certified bachelor's degree copy or equivalent",
    "is_optional": false,
    "sort_order": 1,
    "notes": "Certified copy required.",
    "source_id": "NDU-SRC-002"
  },
  {
    "record_key": "ndu:document:2",
    "document_type": "TRANSCRIPT",
    "document_name": "Official undergraduate transcript",
    "is_optional": false,
    "sort_order": 2,
    "notes": null,
    "source_id": "NDU-SRC-002"
  },
  {
    "record_key": "ndu:document:3",
    "document_type": "BACCALAUREATE",
    "document_name": "Certified Lebanese Baccalaureate Part II copy or equivalent",
    "is_optional": false,
    "sort_order": 3,
    "notes": null,
    "source_id": "NDU-SRC-002"
  },
  {
    "record_key": "ndu:document:4",
    "document_type": "IDENTITY",
    "document_name": "National ID card copy for Lebanese applicants or passport copy for international applicants",
    "is_optional": false,
    "sort_order": 4,
    "notes": null,
    "source_id": "NDU-SRC-002"
  },
  {
    "record_key": "ndu:document:5",
    "document_type": "PHOTO",
    "document_name": "One recent passport-sized photograph",
    "is_optional": false,
    "sort_order": 5,
    "notes": null,
    "source_id": "NDU-SRC-002"
  },
  {
    "record_key": "ndu:document:6",
    "document_type": "RECOMMENDATION",
    "document_name": "Two recommendation letters, one academic and one professional",
    "is_optional": false,
    "sort_order": 6,
    "notes": null,
    "source_id": "NDU-SRC-002"
  }
]$NDU_DOCUMENTS$::jsonb) AS x(record_key TEXT, document_type TEXT, document_name TEXT, is_optional BOOLEAN, sort_order INTEGER, notes TEXT, source_id TEXT)
    JOIN ndu_source_seed ss ON ss.source_id = x.source_id
    JOIN source s ON s.university_id = v_university_id AND s.url = ss.url
    ON CONFLICT (university_id, record_key) DO UPDATE SET scope_level = EXCLUDED.scope_level, document_type = EXCLUDED.document_type, document_name = EXCLUDED.document_name, is_optional = EXCLUDED.is_optional, sort_order = EXCLUDED.sort_order, notes = EXCLUDED.notes, source_id = EXCLUDED.source_id, updated_at = NOW();

    INSERT INTO graduate_admission_deadline (university_id, faculty_id, department_id, program_id, scope_level, record_key, academic_year, deadline_type, term, deadline_date, note, source_id)
    SELECT v_university_id, NULL, NULL, NULL, 'UNIVERSITY', record_key, NULL, deadline_type, term, deadline_date, note, s.id
    FROM jsonb_to_recordset($NDU_DEADLINES$[
  {
    "record_key": "ndu:deadline:fall_2026_application_window",
    "deadline_type": "APPLICATION_OPEN",
    "term": "Fall 2026",
    "deadline_date": null,
    "note": "December 8, 2025 to July 21, 2026",
    "source_id": "NDU-SRC-007"
  },
  {
    "record_key": "ndu:deadline:fall_2026_regular_admission_ii_decision_release",
    "deadline_type": "REGULAR",
    "term": "Fall 2026 Regular Admission II",
    "deadline_date": "2026-06-29",
    "note": "June 29, 2026",
    "source_id": "NDU-SRC-007"
  },
  {
    "record_key": "ndu:deadline:fall_2026_regular_admission_iii_exam_date",
    "deadline_type": "INTERVIEW",
    "term": "Fall 2026 Regular Admission III",
    "deadline_date": "2026-07-24",
    "note": "July 24, 2026",
    "source_id": "NDU-SRC-007"
  },
  {
    "record_key": "ndu:deadline:fall_2026_regular_admission_iii_decision_release",
    "deadline_type": "REGULAR",
    "term": "Fall 2026 Regular Admission III",
    "deadline_date": "2026-08-21",
    "note": "August 21, 2026",
    "source_id": "NDU-SRC-007"
  },
  {
    "record_key": "ndu:deadline:spring_2027_application_window",
    "deadline_type": "APPLICATION_OPEN",
    "term": "Spring 2027",
    "deadline_date": null,
    "note": "October 1, 2026 to December 2, 2026",
    "source_id": "NDU-SRC-007"
  },
  {
    "record_key": "ndu:deadline:financial_aid_application_period",
    "deadline_type": "OTHER",
    "term": "Fall 2026",
    "deadline_date": null,
    "note": "February 10, 2026 to June 30, 2026",
    "source_id": "NDU-SRC-007"
  }
]$NDU_DEADLINES$::jsonb) AS x(record_key TEXT, deadline_type TEXT, term TEXT, deadline_date DATE, note TEXT, source_id TEXT)
    JOIN ndu_source_seed ss ON ss.source_id = x.source_id
    JOIN source s ON s.university_id = v_university_id AND s.url = ss.url
    ON CONFLICT (university_id, record_key) DO UPDATE SET deadline_type = EXCLUDED.deadline_type, term = EXCLUDED.term, deadline_date = EXCLUDED.deadline_date, note = EXCLUDED.note, source_id = EXCLUDED.source_id, updated_at = NOW();

    INSERT INTO graduate_scholarship (university_id, faculty_id, department_id, program_id, scope_level, record_key, academic_year, name, description, coverage, amount, currency, notes, source_id)
    SELECT v_university_id, NULL, NULL, NULL, 'UNIVERSITY', record_key, academic_year, name, description, coverage, amount, currency, x.notes, s.id
    FROM jsonb_to_recordset($NDU_SCHOLARSHIPS$[
  {
    "record_key": "ndu:scholarship:graduate_tuition_reduction",
    "academic_year": "2025-2026",
    "name": "Graduate Tuition Reduction",
    "description": "The only graduate-specific scholarship/discount confirmed in this pass is the 25% tuition reduction for eligible NDU graduates who enter a graduate program within two consecutive regular semesters.",
    "coverage": "25% tuition reduction for eligible recent NDU graduates",
    "amount": null,
    "currency": "USD",
    "notes": "25% graduate tuition reduction for eligible NDU graduates",
    "source_id": "NDU-SRC-010"
  }
]$NDU_SCHOLARSHIPS$::jsonb) AS x(record_key TEXT, academic_year TEXT, name TEXT, description TEXT, coverage TEXT, amount NUMERIC(12,2), currency TEXT, notes TEXT, source_id TEXT)
    JOIN ndu_source_seed ss ON ss.source_id = x.source_id
    JOIN source s ON s.university_id = v_university_id AND s.url = ss.url
    ON CONFLICT (university_id, record_key) DO UPDATE SET academic_year = EXCLUDED.academic_year, name = EXCLUDED.name, description = EXCLUDED.description, coverage = EXCLUDED.coverage, amount = EXCLUDED.amount, currency = EXCLUDED.currency, notes = EXCLUDED.notes, source_id = EXCLUDED.source_id, updated_at = NOW();

    INSERT INTO graduate_financial_aid (university_id, faculty_id, department_id, program_id, scope_level, record_key, academic_year, name, description, amount, currency, notes, source_id)
    SELECT v_university_id, NULL, NULL, NULL, 'UNIVERSITY', record_key, academic_year, name, description, amount, currency, x.notes, s.id
    FROM jsonb_to_recordset($NDU_AID$[
  {
    "record_key": "ndu:financial_aid:work_study_grant",
    "academic_year": "2026-2027",
    "name": "Fall 2026 Work Study Grant",
    "description": "NDU publishes a Fall 2026 Work Study Grant notice for current and new students.",
    "amount": null,
    "currency": "USD",
    "notes": "Applications are time-bound and appear on the official scholarship and financial aid pages.",
    "source_id": "NDU-SRC-011"
  },
  {
    "record_key": "ndu:financial_aid:financial_aid_application_period",
    "academic_year": "2026-2027",
    "name": "Financial Aid Application Window",
    "description": "Official financial-aid application period for graduate applicants.",
    "amount": null,
    "currency": "USD",
    "notes": "NDU publishes a Fall 2026 Work Study Grant notice and a broader financial-aid application window on the official site. The public pages do not provide a single graduate-specific aid catalog beyond the tuition reduction.",
    "source_id": "NDU-SRC-007"
  }
]$NDU_AID$::jsonb) AS x(record_key TEXT, academic_year TEXT, name TEXT, description TEXT, amount NUMERIC(12,2), currency TEXT, notes TEXT, source_id TEXT)
    JOIN ndu_source_seed ss ON ss.source_id = x.source_id
    JOIN source s ON s.university_id = v_university_id AND s.url = ss.url
    ON CONFLICT (university_id, record_key) DO UPDATE SET academic_year = EXCLUDED.academic_year, name = EXCLUDED.name, description = EXCLUDED.description, amount = EXCLUDED.amount, currency = EXCLUDED.currency, notes = EXCLUDED.notes, source_id = EXCLUDED.source_id, updated_at = NOW();

    INSERT INTO graduate_payment_plan (university_id, faculty_id, department_id, program_id, scope_level, record_key, academic_year, name, description, installments_count, down_payment_amount, down_payment_currency, interval_label, notes, source_id)
    SELECT v_university_id, NULL, NULL, NULL, 'UNIVERSITY', record_key, academic_year, name, description, installments_count, down_payment_amount, down_payment_currency, interval_label, x.notes, s.id
    FROM jsonb_to_recordset($NDU_PAYMENT_PLANS$[
  {
    "record_key": "ndu:payment_plan:graduate_installment_schedule",
    "academic_year": "2025-2026",
    "name": "Graduate Installment Schedule",
    "description": "NDU publishes an installment schedule for the 2025-2026 academic year across fall, spring, and summer terms, with four payments in fall and spring and two payments in summer.",
    "installments_count": 10,
    "down_payment_amount": 1050,
    "down_payment_currency": "USD",
    "interval_label": "Term-based installments",
    "notes": "The tuition-fees page states that credit fees must be paid in full in cash USD. The application fee may be paid online or on campus.",
    "source_id": "NDU-SRC-009"
  }
]$NDU_PAYMENT_PLANS$::jsonb) AS x(record_key TEXT, academic_year TEXT, name TEXT, description TEXT, installments_count INTEGER, down_payment_amount NUMERIC(12,2), down_payment_currency TEXT, interval_label TEXT, notes TEXT, source_id TEXT)
    JOIN ndu_source_seed ss ON ss.source_id = x.source_id
    JOIN source s ON s.university_id = v_university_id AND s.url = ss.url
    ON CONFLICT (university_id, record_key) DO UPDATE SET academic_year = EXCLUDED.academic_year, name = EXCLUDED.name, description = EXCLUDED.description, installments_count = EXCLUDED.installments_count, down_payment_amount = EXCLUDED.down_payment_amount, down_payment_currency = EXCLUDED.down_payment_currency, interval_label = EXCLUDED.interval_label, notes = EXCLUDED.notes, source_id = EXCLUDED.source_id, updated_at = NOW();

    INSERT INTO graduate_accreditation (university_id, faculty_id, department_id, program_id, scope_level, record_key, name, authority, status, valid_from, valid_until, notes, source_id)
    SELECT v_university_id, fac.id, dep.id, gp.id, scope_level, record_key, x.name, authority, status, valid_from, valid_until, x.notes, s.id
    FROM jsonb_to_recordset($NDU_ACCREDITATION$[
  {
    "record_key": "ndu:accreditation:institutional",
    "name": "Institutional Accreditation",
    "authority": "New England Commission of Higher Education (NECHE)",
    "status": "Accredited",
    "valid_from": null,
    "valid_until": null,
    "notes": "Institution-wide accreditation.",
    "source_id": "NDU-SRC-018",
    "scope_level": "UNIVERSITY",
    "faculty": null,
    "department": null,
    "program_id": null
  },
  {
    "record_key": "ndu:accreditation:ndu-fbae-master-business-strategy",
    "name": "AACSB Accredited Program",
    "authority": "AACSB",
    "status": "Accredited",
    "valid_from": null,
    "valid_until": null,
    "notes": "The visible page lists a 5:30 p.m. course schedule and a maximum load of 12 credits per semester. It does not publish GRE or GMAT requirements in the visible content.",
    "source_id": "NDU-SRC-003",
    "scope_level": "PROGRAM",
    "faculty": "Faculty of Business Administration and Economics",
    "department": "Department of Accounting and Finance",
    "program_id": "ndu-fbae-master-business-strategy"
  },
  {
    "record_key": "ndu:accreditation:ndu-fbae-master-financial-risk-management",
    "name": "AACSB Accredited Program",
    "authority": "AACSB",
    "status": "Accredited",
    "valid_from": null,
    "valid_until": null,
    "notes": "The page states a 30-credit program and a per-credit tuition price of USD 635.",
    "source_id": "NDU-SRC-003",
    "scope_level": "PROGRAM",
    "faculty": "Faculty of Business Administration and Economics",
    "department": "Department of Accounting and Finance",
    "program_id": "ndu-fbae-master-financial-risk-management"
  },
  {
    "record_key": "ndu:accreditation:ndu-fbae-master-mba",
    "name": "AACSB Accredited Program",
    "authority": "AACSB",
    "status": "Accredited",
    "valid_from": null,
    "valid_until": null,
    "notes": "The MBA page states that students may choose a Project Management emphasis or a General MBA and that the degree can be completed on a full- or part-time basis.",
    "source_id": "NDU-SRC-003",
    "scope_level": "PROGRAM",
    "faculty": "Faculty of Business Administration and Economics",
    "department": "Department of Management and Marketing",
    "program_id": "ndu-fbae-master-mba"
  },
  {
    "record_key": "ndu:accreditation:ndu-fbae-master-ms-business-strategy",
    "name": "AACSB Accredited Program",
    "authority": "AACSB",
    "status": "Accredited",
    "valid_from": null,
    "valid_until": null,
    "notes": "The visible page lists a 5:30 p.m. course schedule and a maximum load of 12 credits per semester. It does not publish GRE or GMAT requirements in the visible content.",
    "source_id": "NDU-SRC-003",
    "scope_level": "PROGRAM",
    "faculty": "Faculty of Business Administration and Economics",
    "department": "Department of Management and Marketing",
    "program_id": "ndu-fbae-master-ms-business-strategy"
  }
]$NDU_ACCREDITATION$::jsonb) AS x(scope_level TEXT, record_key TEXT, name TEXT, authority TEXT, status TEXT, valid_from DATE, valid_until DATE, notes TEXT, source_id TEXT, faculty TEXT, department TEXT, program_id TEXT)
    LEFT JOIN university_faculty fac ON fac.university_id = v_university_id AND fac.name = x.faculty
    LEFT JOIN university_department dep ON dep.university_id = v_university_id AND dep.name = x.department AND dep.faculty_id = fac.id
    LEFT JOIN graduate_program gp ON gp.university_id = v_university_id AND gp.program_key = x.program_id
    JOIN ndu_source_seed ss ON ss.source_id = x.source_id
    JOIN source s ON s.university_id = v_university_id AND s.url = ss.url
    ON CONFLICT (university_id, record_key) DO UPDATE SET scope_level = EXCLUDED.scope_level, name = EXCLUDED.name, authority = EXCLUDED.authority, status = EXCLUDED.status, valid_from = EXCLUDED.valid_from, valid_until = EXCLUDED.valid_until, notes = EXCLUDED.notes, source_id = EXCLUDED.source_id, updated_at = NOW();

    INSERT INTO graduate_program_track (university_id, program_id, track_type, track_name, track_order, is_primary, description, source_id, notes)
    SELECT v_university_id, gp.id, track_type, track_name, track_order, is_primary, description, s.id, x.notes
    FROM jsonb_to_recordset($NDU_TRACKS$[
  {
    "record_key": "ndu:track:ndu-fh-master-education:1",
    "track_type": "CONCENTRATION",
    "track_name": "School Leadership",
    "track_order": 1,
    "is_primary": true,
    "description": null,
    "source_id": "NDU-SRC-003",
    "notes": "The graduation requirements include a thesis.",
    "faculty": "Faculty of Humanities",
    "department": "Department of Psychology, Education and Physical Education",
    "program_id": "ndu-fh-master-education"
  },
  {
    "record_key": "ndu:track:ndu-fh-master-education:2",
    "track_type": "CONCENTRATION",
    "track_name": "Educational Technology",
    "track_order": 2,
    "is_primary": false,
    "description": null,
    "source_id": "NDU-SRC-003",
    "notes": "The graduation requirements include a thesis.",
    "faculty": "Faculty of Humanities",
    "department": "Department of Psychology, Education and Physical Education",
    "program_id": "ndu-fh-master-education"
  },
  {
    "record_key": "ndu:track:ndu-fh-master-education:3",
    "track_type": "CONCENTRATION",
    "track_name": "Special Needs Education",
    "track_order": 3,
    "is_primary": false,
    "description": null,
    "source_id": "NDU-SRC-003",
    "notes": "The graduation requirements include a thesis.",
    "faculty": "Faculty of Humanities",
    "department": "Department of Psychology, Education and Physical Education",
    "program_id": "ndu-fh-master-education"
  },
  {
    "record_key": "ndu:track:ndu-fh-master-english-language-literature-applied-linguistics-tefl:1",
    "track_type": "CONCENTRATION",
    "track_name": "Applied Linguistics and TEFL Emphasis",
    "track_order": 1,
    "is_primary": true,
    "description": null,
    "source_id": "NDU-SRC-003",
    "notes": "The page requires a minimum EET score of 60 for admission and states that GRE performance is required only if an applicant submits GRE results.",
    "faculty": "Faculty of Humanities",
    "department": "Department of English and Translation",
    "program_id": "ndu-fh-master-english-language-literature-applied-linguistics-tefl"
  },
  {
    "record_key": "ndu:track:ndu-fh-master-english-language-literature-literature:1",
    "track_type": "CONCENTRATION",
    "track_name": "Literature Emphasis",
    "track_order": 1,
    "is_primary": true,
    "description": null,
    "source_id": "NDU-SRC-003",
    "notes": "The page states that graduation requires 30 credits and thesis submission and defense.",
    "faculty": "Faculty of Humanities",
    "department": "Department of English and Translation",
    "program_id": "ndu-fh-master-english-language-literature-literature"
  },
  {
    "record_key": "ndu:track:ndu-flps-master-international-affairs-diplomacy-international-law:1",
    "track_type": "CONCENTRATION",
    "track_name": "International Law Emphasis",
    "track_order": 1,
    "is_primary": true,
    "description": null,
    "source_id": "NDU-SRC-003",
    "notes": "The emphasis-specific prerequisite list includes IAF 211, IAF 401, and POS 442 or equivalents.",
    "faculty": "Faculty of Law and Political Science",
    "department": "Department of Government and International Relations",
    "program_id": "ndu-flps-master-international-affairs-diplomacy-international-law"
  },
  {
    "record_key": "ndu:track:ndu-flps-master-political-science-human-rights:1",
    "track_type": "CONCENTRATION",
    "track_name": "Human Rights Emphasis",
    "track_order": 1,
    "is_primary": true,
    "description": null,
    "source_id": "NDU-SRC-003",
    "notes": "The visible page content names prerequisites for the political science track and allows the same thesis/non-thesis path structure as the main program.",
    "faculty": "Faculty of Law and Political Science",
    "department": "Department of Government and International Relations",
    "program_id": "ndu-flps-master-political-science-human-rights"
  },
  {
    "record_key": "ndu:track:ndu-flps-master-political-science-ngos:1",
    "track_type": "CONCENTRATION",
    "track_name": "NGOs Emphasis",
    "track_order": 1,
    "is_primary": true,
    "description": null,
    "source_id": "NDU-SRC-003",
    "notes": "The visible page content includes program-specific prerequisite lists for Political Science, Public Administration, International Affairs and Diplomacy, and International Affairs and Diplomacy - International Law Emphasis.",
    "faculty": "Faculty of Law and Political Science",
    "department": "Department of Government and International Relations",
    "program_id": "ndu-flps-master-political-science-ngos"
  },
  {
    "record_key": "ndu:track:ndu-fh-master-psychology-educational:1",
    "track_type": "CONCENTRATION",
    "track_name": "Educational Psychology",
    "track_order": 1,
    "is_primary": true,
    "description": null,
    "source_id": "NDU-SRC-003",
    "notes": "Graduation may follow either a thesis track or a non-thesis track.",
    "faculty": "Faculty of Humanities",
    "department": "Department of Psychology, Education and Physical Education",
    "program_id": "ndu-fh-master-psychology-educational"
  },
  {
    "record_key": "ndu:track:ndu-fbae-master-mba:1",
    "track_type": "CONCENTRATION",
    "track_name": "Project Management",
    "track_order": 1,
    "is_primary": true,
    "description": null,
    "source_id": "NDU-SRC-003",
    "notes": "The MBA page states that students may choose a Project Management emphasis or a General MBA and that the degree can be completed on a full- or part-time basis.",
    "faculty": "Faculty of Business Administration and Economics",
    "department": "Department of Management and Marketing",
    "program_id": "ndu-fbae-master-mba"
  }
]$NDU_TRACKS$::jsonb) AS x(record_key TEXT, track_type TEXT, track_name TEXT, track_order INTEGER, is_primary BOOLEAN, description TEXT, source_id TEXT, notes TEXT, faculty TEXT, department TEXT, program_id TEXT)
    JOIN university_faculty fac ON fac.university_id = v_university_id AND fac.name = x.faculty
    JOIN university_department dep ON dep.university_id = v_university_id AND dep.name = x.department AND dep.faculty_id = fac.id
    JOIN graduate_program gp ON gp.university_id = v_university_id AND gp.program_key = x.program_id
    JOIN ndu_source_seed ss ON ss.source_id = x.source_id
    JOIN source s ON s.university_id = v_university_id AND s.url = ss.url
    ON CONFLICT (program_id, track_type, track_name) DO UPDATE SET track_order = EXCLUDED.track_order, is_primary = EXCLUDED.is_primary, description = EXCLUDED.description, source_id = EXCLUDED.source_id, notes = EXCLUDED.notes, updated_at = NOW();

    INSERT INTO graduate_program_source (university_id, program_id, source_id, source_role, source_order, evidence_text, notes)
    SELECT v_university_id, gp.id, s.id, source_role, source_order, evidence_text, x.notes
    FROM jsonb_to_recordset($NDU_PROGRAM_SOURCES$[
  {
    "record_key": "ndu:program_source:ndu-faad-master-design:1",
    "source_id": "NDU-SRC-003",
    "source_role": "PRIMARY",
    "source_order": 1,
    "evidence_text": "NDU-SRC-003",
    "notes": null,
    "program_id": "ndu-faad-master-design"
  },
  {
    "record_key": "ndu:program_source:ndu-faad-master-design:2",
    "source_id": "NDU-SRC-002",
    "source_role": "SECONDARY",
    "source_order": 2,
    "evidence_text": "NDU-SRC-002",
    "notes": null,
    "program_id": "ndu-faad-master-design"
  },
  {
    "record_key": "ndu:program_source:ndu-faad-master-design:3",
    "source_id": "NDU-SRC-006",
    "source_role": "ADMISSIONS",
    "source_order": 3,
    "evidence_text": "NDU-SRC-006",
    "notes": null,
    "program_id": "ndu-faad-master-design"
  },
  {
    "record_key": "ndu:program_source:ndu-faad-master-design:4",
    "source_id": "NDU-SRC-008",
    "source_role": "TUITION",
    "source_order": 4,
    "evidence_text": "NDU-SRC-008",
    "notes": null,
    "program_id": "ndu-faad-master-design"
  },
  {
    "record_key": "ndu:program_source:ndu-fh-master-education:1",
    "source_id": "NDU-SRC-003",
    "source_role": "PRIMARY",
    "source_order": 1,
    "evidence_text": "NDU-SRC-003",
    "notes": null,
    "program_id": "ndu-fh-master-education"
  },
  {
    "record_key": "ndu:program_source:ndu-fh-master-education:2",
    "source_id": "NDU-SRC-002",
    "source_role": "SECONDARY",
    "source_order": 2,
    "evidence_text": "NDU-SRC-002",
    "notes": null,
    "program_id": "ndu-fh-master-education"
  },
  {
    "record_key": "ndu:program_source:ndu-fh-master-education:3",
    "source_id": "NDU-SRC-006",
    "source_role": "ADMISSIONS",
    "source_order": 3,
    "evidence_text": "NDU-SRC-006",
    "notes": null,
    "program_id": "ndu-fh-master-education"
  },
  {
    "record_key": "ndu:program_source:ndu-fh-master-education:4",
    "source_id": "NDU-SRC-008",
    "source_role": "TUITION",
    "source_order": 4,
    "evidence_text": "NDU-SRC-008",
    "notes": null,
    "program_id": "ndu-fh-master-education"
  },
  {
    "record_key": "ndu:program_source:ndu-fh-master-english-language-literature-applied-linguistics-tefl:1",
    "source_id": "NDU-SRC-003",
    "source_role": "PRIMARY",
    "source_order": 1,
    "evidence_text": "NDU-SRC-003",
    "notes": null,
    "program_id": "ndu-fh-master-english-language-literature-applied-linguistics-tefl"
  },
  {
    "record_key": "ndu:program_source:ndu-fh-master-english-language-literature-applied-linguistics-tefl:2",
    "source_id": "NDU-SRC-002",
    "source_role": "SECONDARY",
    "source_order": 2,
    "evidence_text": "NDU-SRC-002",
    "notes": null,
    "program_id": "ndu-fh-master-english-language-literature-applied-linguistics-tefl"
  },
  {
    "record_key": "ndu:program_source:ndu-fh-master-english-language-literature-applied-linguistics-tefl:3",
    "source_id": "NDU-SRC-006",
    "source_role": "ADMISSIONS",
    "source_order": 3,
    "evidence_text": "NDU-SRC-006",
    "notes": null,
    "program_id": "ndu-fh-master-english-language-literature-applied-linguistics-tefl"
  },
  {
    "record_key": "ndu:program_source:ndu-fh-master-english-language-literature-applied-linguistics-tefl:4",
    "source_id": "NDU-SRC-008",
    "source_role": "TUITION",
    "source_order": 4,
    "evidence_text": "NDU-SRC-008",
    "notes": null,
    "program_id": "ndu-fh-master-english-language-literature-applied-linguistics-tefl"
  },
  {
    "record_key": "ndu:program_source:ndu-fh-master-english-language-literature-literature:1",
    "source_id": "NDU-SRC-003",
    "source_role": "PRIMARY",
    "source_order": 1,
    "evidence_text": "NDU-SRC-003",
    "notes": null,
    "program_id": "ndu-fh-master-english-language-literature-literature"
  },
  {
    "record_key": "ndu:program_source:ndu-fh-master-english-language-literature-literature:2",
    "source_id": "NDU-SRC-002",
    "source_role": "SECONDARY",
    "source_order": 2,
    "evidence_text": "NDU-SRC-002",
    "notes": null,
    "program_id": "ndu-fh-master-english-language-literature-literature"
  },
  {
    "record_key": "ndu:program_source:ndu-fh-master-english-language-literature-literature:3",
    "source_id": "NDU-SRC-006",
    "source_role": "ADMISSIONS",
    "source_order": 3,
    "evidence_text": "NDU-SRC-006",
    "notes": null,
    "program_id": "ndu-fh-master-english-language-literature-literature"
  },
  {
    "record_key": "ndu:program_source:ndu-fh-master-english-language-literature-literature:4",
    "source_id": "NDU-SRC-008",
    "source_role": "TUITION",
    "source_order": 4,
    "evidence_text": "NDU-SRC-008",
    "notes": null,
    "program_id": "ndu-fh-master-english-language-literature-literature"
  },
  {
    "record_key": "ndu:program_source:ndu-flps-master-international-affairs-diplomacy:1",
    "source_id": "NDU-SRC-003",
    "source_role": "PRIMARY",
    "source_order": 1,
    "evidence_text": "NDU-SRC-003",
    "notes": null,
    "program_id": "ndu-flps-master-international-affairs-diplomacy"
  },
  {
    "record_key": "ndu:program_source:ndu-flps-master-international-affairs-diplomacy:2",
    "source_id": "NDU-SRC-002",
    "source_role": "SECONDARY",
    "source_order": 2,
    "evidence_text": "NDU-SRC-002",
    "notes": null,
    "program_id": "ndu-flps-master-international-affairs-diplomacy"
  },
  {
    "record_key": "ndu:program_source:ndu-flps-master-international-affairs-diplomacy:3",
    "source_id": "NDU-SRC-006",
    "source_role": "ADMISSIONS",
    "source_order": 3,
    "evidence_text": "NDU-SRC-006",
    "notes": null,
    "program_id": "ndu-flps-master-international-affairs-diplomacy"
  },
  {
    "record_key": "ndu:program_source:ndu-flps-master-international-affairs-diplomacy:4",
    "source_id": "NDU-SRC-008",
    "source_role": "TUITION",
    "source_order": 4,
    "evidence_text": "NDU-SRC-008",
    "notes": null,
    "program_id": "ndu-flps-master-international-affairs-diplomacy"
  },
  {
    "record_key": "ndu:program_source:ndu-flps-master-international-affairs-diplomacy-international-law:1",
    "source_id": "NDU-SRC-003",
    "source_role": "PRIMARY",
    "source_order": 1,
    "evidence_text": "NDU-SRC-003",
    "notes": null,
    "program_id": "ndu-flps-master-international-affairs-diplomacy-international-law"
  },
  {
    "record_key": "ndu:program_source:ndu-flps-master-international-affairs-diplomacy-international-law:2",
    "source_id": "NDU-SRC-002",
    "source_role": "SECONDARY",
    "source_order": 2,
    "evidence_text": "NDU-SRC-002",
    "notes": null,
    "program_id": "ndu-flps-master-international-affairs-diplomacy-international-law"
  },
  {
    "record_key": "ndu:program_source:ndu-flps-master-international-affairs-diplomacy-international-law:3",
    "source_id": "NDU-SRC-006",
    "source_role": "ADMISSIONS",
    "source_order": 3,
    "evidence_text": "NDU-SRC-006",
    "notes": null,
    "program_id": "ndu-flps-master-international-affairs-diplomacy-international-law"
  },
  {
    "record_key": "ndu:program_source:ndu-flps-master-international-affairs-diplomacy-international-law:4",
    "source_id": "NDU-SRC-008",
    "source_role": "TUITION",
    "source_order": 4,
    "evidence_text": "NDU-SRC-008",
    "notes": null,
    "program_id": "ndu-flps-master-international-affairs-diplomacy-international-law"
  },
  {
    "record_key": "ndu:program_source:ndu-fh-master-media-studies:1",
    "source_id": "NDU-SRC-003",
    "source_role": "PRIMARY",
    "source_order": 1,
    "evidence_text": "NDU-SRC-003",
    "notes": null,
    "program_id": "ndu-fh-master-media-studies"
  },
  {
    "record_key": "ndu:program_source:ndu-fh-master-media-studies:2",
    "source_id": "NDU-SRC-002",
    "source_role": "SECONDARY",
    "source_order": 2,
    "evidence_text": "NDU-SRC-002",
    "notes": null,
    "program_id": "ndu-fh-master-media-studies"
  },
  {
    "record_key": "ndu:program_source:ndu-fh-master-media-studies:3",
    "source_id": "NDU-SRC-006",
    "source_role": "ADMISSIONS",
    "source_order": 3,
    "evidence_text": "NDU-SRC-006",
    "notes": null,
    "program_id": "ndu-fh-master-media-studies"
  },
  {
    "record_key": "ndu:program_source:ndu-fh-master-media-studies:4",
    "source_id": "NDU-SRC-008",
    "source_role": "TUITION",
    "source_order": 4,
    "evidence_text": "NDU-SRC-008",
    "notes": null,
    "program_id": "ndu-fh-master-media-studies"
  },
  {
    "record_key": "ndu:program_source:ndu-faad-master-music:1",
    "source_id": "NDU-SRC-003",
    "source_role": "PRIMARY",
    "source_order": 1,
    "evidence_text": "NDU-SRC-003",
    "notes": null,
    "program_id": "ndu-faad-master-music"
  },
  {
    "record_key": "ndu:program_source:ndu-faad-master-music:2",
    "source_id": "NDU-SRC-002",
    "source_role": "SECONDARY",
    "source_order": 2,
    "evidence_text": "NDU-SRC-002",
    "notes": null,
    "program_id": "ndu-faad-master-music"
  },
  {
    "record_key": "ndu:program_source:ndu-faad-master-music:3",
    "source_id": "NDU-SRC-006",
    "source_role": "ADMISSIONS",
    "source_order": 3,
    "evidence_text": "NDU-SRC-006",
    "notes": null,
    "program_id": "ndu-faad-master-music"
  },
  {
    "record_key": "ndu:program_source:ndu-faad-master-music:4",
    "source_id": "NDU-SRC-008",
    "source_role": "TUITION",
    "source_order": 4,
    "evidence_text": "NDU-SRC-008",
    "notes": null,
    "program_id": "ndu-faad-master-music"
  },
  {
    "record_key": "ndu:program_source:ndu-flps-master-political-science:1",
    "source_id": "NDU-SRC-003",
    "source_role": "PRIMARY",
    "source_order": 1,
    "evidence_text": "NDU-SRC-003",
    "notes": null,
    "program_id": "ndu-flps-master-political-science"
  },
  {
    "record_key": "ndu:program_source:ndu-flps-master-political-science:2",
    "source_id": "NDU-SRC-002",
    "source_role": "SECONDARY",
    "source_order": 2,
    "evidence_text": "NDU-SRC-002",
    "notes": null,
    "program_id": "ndu-flps-master-political-science"
  },
  {
    "record_key": "ndu:program_source:ndu-flps-master-political-science:3",
    "source_id": "NDU-SRC-006",
    "source_role": "ADMISSIONS",
    "source_order": 3,
    "evidence_text": "NDU-SRC-006",
    "notes": null,
    "program_id": "ndu-flps-master-political-science"
  },
  {
    "record_key": "ndu:program_source:ndu-flps-master-political-science:4",
    "source_id": "NDU-SRC-008",
    "source_role": "TUITION",
    "source_order": 4,
    "evidence_text": "NDU-SRC-008",
    "notes": null,
    "program_id": "ndu-flps-master-political-science"
  },
  {
    "record_key": "ndu:program_source:ndu-flps-master-political-science-human-rights:1",
    "source_id": "NDU-SRC-003",
    "source_role": "PRIMARY",
    "source_order": 1,
    "evidence_text": "NDU-SRC-003",
    "notes": null,
    "program_id": "ndu-flps-master-political-science-human-rights"
  },
  {
    "record_key": "ndu:program_source:ndu-flps-master-political-science-human-rights:2",
    "source_id": "NDU-SRC-002",
    "source_role": "SECONDARY",
    "source_order": 2,
    "evidence_text": "NDU-SRC-002",
    "notes": null,
    "program_id": "ndu-flps-master-political-science-human-rights"
  },
  {
    "record_key": "ndu:program_source:ndu-flps-master-political-science-human-rights:3",
    "source_id": "NDU-SRC-006",
    "source_role": "ADMISSIONS",
    "source_order": 3,
    "evidence_text": "NDU-SRC-006",
    "notes": null,
    "program_id": "ndu-flps-master-political-science-human-rights"
  },
  {
    "record_key": "ndu:program_source:ndu-flps-master-political-science-human-rights:4",
    "source_id": "NDU-SRC-008",
    "source_role": "TUITION",
    "source_order": 4,
    "evidence_text": "NDU-SRC-008",
    "notes": null,
    "program_id": "ndu-flps-master-political-science-human-rights"
  },
  {
    "record_key": "ndu:program_source:ndu-flps-master-political-science-ngos:1",
    "source_id": "NDU-SRC-003",
    "source_role": "PRIMARY",
    "source_order": 1,
    "evidence_text": "NDU-SRC-003",
    "notes": null,
    "program_id": "ndu-flps-master-political-science-ngos"
  },
  {
    "record_key": "ndu:program_source:ndu-flps-master-political-science-ngos:2",
    "source_id": "NDU-SRC-002",
    "source_role": "SECONDARY",
    "source_order": 2,
    "evidence_text": "NDU-SRC-002",
    "notes": null,
    "program_id": "ndu-flps-master-political-science-ngos"
  },
  {
    "record_key": "ndu:program_source:ndu-flps-master-political-science-ngos:3",
    "source_id": "NDU-SRC-006",
    "source_role": "ADMISSIONS",
    "source_order": 3,
    "evidence_text": "NDU-SRC-006",
    "notes": null,
    "program_id": "ndu-flps-master-political-science-ngos"
  },
  {
    "record_key": "ndu:program_source:ndu-flps-master-political-science-ngos:4",
    "source_id": "NDU-SRC-008",
    "source_role": "TUITION",
    "source_order": 4,
    "evidence_text": "NDU-SRC-008",
    "notes": null,
    "program_id": "ndu-flps-master-political-science-ngos"
  },
  {
    "record_key": "ndu:program_source:ndu-fh-master-psychology-educational:1",
    "source_id": "NDU-SRC-003",
    "source_role": "PRIMARY",
    "source_order": 1,
    "evidence_text": "NDU-SRC-003",
    "notes": null,
    "program_id": "ndu-fh-master-psychology-educational"
  },
  {
    "record_key": "ndu:program_source:ndu-fh-master-psychology-educational:2",
    "source_id": "NDU-SRC-002",
    "source_role": "SECONDARY",
    "source_order": 2,
    "evidence_text": "NDU-SRC-002",
    "notes": null,
    "program_id": "ndu-fh-master-psychology-educational"
  },
  {
    "record_key": "ndu:program_source:ndu-fh-master-psychology-educational:3",
    "source_id": "NDU-SRC-006",
    "source_role": "ADMISSIONS",
    "source_order": 3,
    "evidence_text": "NDU-SRC-006",
    "notes": null,
    "program_id": "ndu-fh-master-psychology-educational"
  },
  {
    "record_key": "ndu:program_source:ndu-fh-master-psychology-educational:4",
    "source_id": "NDU-SRC-008",
    "source_role": "TUITION",
    "source_order": 4,
    "evidence_text": "NDU-SRC-008",
    "notes": null,
    "program_id": "ndu-fh-master-psychology-educational"
  },
  {
    "record_key": "ndu:program_source:ndu-flps-master-public-administration:1",
    "source_id": "NDU-SRC-003",
    "source_role": "PRIMARY",
    "source_order": 1,
    "evidence_text": "NDU-SRC-003",
    "notes": null,
    "program_id": "ndu-flps-master-public-administration"
  },
  {
    "record_key": "ndu:program_source:ndu-flps-master-public-administration:2",
    "source_id": "NDU-SRC-002",
    "source_role": "SECONDARY",
    "source_order": 2,
    "evidence_text": "NDU-SRC-002",
    "notes": null,
    "program_id": "ndu-flps-master-public-administration"
  },
  {
    "record_key": "ndu:program_source:ndu-flps-master-public-administration:3",
    "source_id": "NDU-SRC-006",
    "source_role": "ADMISSIONS",
    "source_order": 3,
    "evidence_text": "NDU-SRC-006",
    "notes": null,
    "program_id": "ndu-flps-master-public-administration"
  },
  {
    "record_key": "ndu:program_source:ndu-flps-master-public-administration:4",
    "source_id": "NDU-SRC-008",
    "source_role": "TUITION",
    "source_order": 4,
    "evidence_text": "NDU-SRC-008",
    "notes": null,
    "program_id": "ndu-flps-master-public-administration"
  },
  {
    "record_key": "ndu:program_source:ndu-fh-master-translation:1",
    "source_id": "NDU-SRC-003",
    "source_role": "PRIMARY",
    "source_order": 1,
    "evidence_text": "NDU-SRC-003",
    "notes": null,
    "program_id": "ndu-fh-master-translation"
  },
  {
    "record_key": "ndu:program_source:ndu-fh-master-translation:2",
    "source_id": "NDU-SRC-002",
    "source_role": "SECONDARY",
    "source_order": 2,
    "evidence_text": "NDU-SRC-002",
    "notes": null,
    "program_id": "ndu-fh-master-translation"
  },
  {
    "record_key": "ndu:program_source:ndu-fh-master-translation:3",
    "source_id": "NDU-SRC-006",
    "source_role": "ADMISSIONS",
    "source_order": 3,
    "evidence_text": "NDU-SRC-006",
    "notes": null,
    "program_id": "ndu-fh-master-translation"
  },
  {
    "record_key": "ndu:program_source:ndu-fh-master-translation:4",
    "source_id": "NDU-SRC-008",
    "source_role": "TUITION",
    "source_order": 4,
    "evidence_text": "NDU-SRC-008",
    "notes": null,
    "program_id": "ndu-fh-master-translation"
  },
  {
    "record_key": "ndu:program_source:ndu-fnas-master-actuarial-sciences:1",
    "source_id": "NDU-SRC-003",
    "source_role": "PRIMARY",
    "source_order": 1,
    "evidence_text": "NDU-SRC-003",
    "notes": null,
    "program_id": "ndu-fnas-master-actuarial-sciences"
  },
  {
    "record_key": "ndu:program_source:ndu-fnas-master-actuarial-sciences:2",
    "source_id": "NDU-SRC-002",
    "source_role": "SECONDARY",
    "source_order": 2,
    "evidence_text": "NDU-SRC-002",
    "notes": null,
    "program_id": "ndu-fnas-master-actuarial-sciences"
  },
  {
    "record_key": "ndu:program_source:ndu-fnas-master-actuarial-sciences:3",
    "source_id": "NDU-SRC-006",
    "source_role": "ADMISSIONS",
    "source_order": 3,
    "evidence_text": "NDU-SRC-006",
    "notes": null,
    "program_id": "ndu-fnas-master-actuarial-sciences"
  },
  {
    "record_key": "ndu:program_source:ndu-fnas-master-actuarial-sciences:4",
    "source_id": "NDU-SRC-008",
    "source_role": "TUITION",
    "source_order": 4,
    "evidence_text": "NDU-SRC-008",
    "notes": null,
    "program_id": "ndu-fnas-master-actuarial-sciences"
  },
  {
    "record_key": "ndu:program_source:ndu-fnas-master-biology:1",
    "source_id": "NDU-SRC-003",
    "source_role": "PRIMARY",
    "source_order": 1,
    "evidence_text": "NDU-SRC-003",
    "notes": null,
    "program_id": "ndu-fnas-master-biology"
  },
  {
    "record_key": "ndu:program_source:ndu-fnas-master-biology:2",
    "source_id": "NDU-SRC-002",
    "source_role": "SECONDARY",
    "source_order": 2,
    "evidence_text": "NDU-SRC-002",
    "notes": null,
    "program_id": "ndu-fnas-master-biology"
  },
  {
    "record_key": "ndu:program_source:ndu-fnas-master-biology:3",
    "source_id": "NDU-SRC-006",
    "source_role": "ADMISSIONS",
    "source_order": 3,
    "evidence_text": "NDU-SRC-006",
    "notes": null,
    "program_id": "ndu-fnas-master-biology"
  },
  {
    "record_key": "ndu:program_source:ndu-fnas-master-biology:4",
    "source_id": "NDU-SRC-008",
    "source_role": "TUITION",
    "source_order": 4,
    "evidence_text": "NDU-SRC-008",
    "notes": null,
    "program_id": "ndu-fnas-master-biology"
  },
  {
    "record_key": "ndu:program_source:ndu-fbae-master-business-strategy:1",
    "source_id": "NDU-SRC-003",
    "source_role": "PRIMARY",
    "source_order": 1,
    "evidence_text": "NDU-SRC-003",
    "notes": null,
    "program_id": "ndu-fbae-master-business-strategy"
  },
  {
    "record_key": "ndu:program_source:ndu-fbae-master-business-strategy:2",
    "source_id": "NDU-SRC-002",
    "source_role": "SECONDARY",
    "source_order": 2,
    "evidence_text": "NDU-SRC-002",
    "notes": null,
    "program_id": "ndu-fbae-master-business-strategy"
  },
  {
    "record_key": "ndu:program_source:ndu-fbae-master-business-strategy:3",
    "source_id": "NDU-SRC-006",
    "source_role": "ADMISSIONS",
    "source_order": 3,
    "evidence_text": "NDU-SRC-006",
    "notes": null,
    "program_id": "ndu-fbae-master-business-strategy"
  },
  {
    "record_key": "ndu:program_source:ndu-fbae-master-business-strategy:4",
    "source_id": "NDU-SRC-008",
    "source_role": "TUITION",
    "source_order": 4,
    "evidence_text": "NDU-SRC-008",
    "notes": null,
    "program_id": "ndu-fbae-master-business-strategy"
  },
  {
    "record_key": "ndu:program_source:ndu-fe-master-civil-engineering:1",
    "source_id": "NDU-SRC-003",
    "source_role": "PRIMARY",
    "source_order": 1,
    "evidence_text": "NDU-SRC-003",
    "notes": null,
    "program_id": "ndu-fe-master-civil-engineering"
  },
  {
    "record_key": "ndu:program_source:ndu-fe-master-civil-engineering:2",
    "source_id": "NDU-SRC-002",
    "source_role": "SECONDARY",
    "source_order": 2,
    "evidence_text": "NDU-SRC-002",
    "notes": null,
    "program_id": "ndu-fe-master-civil-engineering"
  },
  {
    "record_key": "ndu:program_source:ndu-fe-master-civil-engineering:3",
    "source_id": "NDU-SRC-006",
    "source_role": "ADMISSIONS",
    "source_order": 3,
    "evidence_text": "NDU-SRC-006",
    "notes": null,
    "program_id": "ndu-fe-master-civil-engineering"
  },
  {
    "record_key": "ndu:program_source:ndu-fe-master-civil-engineering:4",
    "source_id": "NDU-SRC-008",
    "source_role": "TUITION",
    "source_order": 4,
    "evidence_text": "NDU-SRC-008",
    "notes": null,
    "program_id": "ndu-fe-master-civil-engineering"
  },
  {
    "record_key": "ndu:program_source:ndu-fnas-master-computer-science:1",
    "source_id": "NDU-SRC-003",
    "source_role": "PRIMARY",
    "source_order": 1,
    "evidence_text": "NDU-SRC-003",
    "notes": null,
    "program_id": "ndu-fnas-master-computer-science"
  },
  {
    "record_key": "ndu:program_source:ndu-fnas-master-computer-science:2",
    "source_id": "NDU-SRC-002",
    "source_role": "SECONDARY",
    "source_order": 2,
    "evidence_text": "NDU-SRC-002",
    "notes": null,
    "program_id": "ndu-fnas-master-computer-science"
  },
  {
    "record_key": "ndu:program_source:ndu-fnas-master-computer-science:3",
    "source_id": "NDU-SRC-006",
    "source_role": "ADMISSIONS",
    "source_order": 3,
    "evidence_text": "NDU-SRC-006",
    "notes": null,
    "program_id": "ndu-fnas-master-computer-science"
  },
  {
    "record_key": "ndu:program_source:ndu-fnas-master-computer-science:4",
    "source_id": "NDU-SRC-008",
    "source_role": "TUITION",
    "source_order": 4,
    "evidence_text": "NDU-SRC-008",
    "notes": null,
    "program_id": "ndu-fnas-master-computer-science"
  },
  {
    "record_key": "ndu:program_source:ndu-fe-master-electrical-computer-engineering:1",
    "source_id": "NDU-SRC-003",
    "source_role": "PRIMARY",
    "source_order": 1,
    "evidence_text": "NDU-SRC-003",
    "notes": null,
    "program_id": "ndu-fe-master-electrical-computer-engineering"
  },
  {
    "record_key": "ndu:program_source:ndu-fe-master-electrical-computer-engineering:2",
    "source_id": "NDU-SRC-002",
    "source_role": "SECONDARY",
    "source_order": 2,
    "evidence_text": "NDU-SRC-002",
    "notes": null,
    "program_id": "ndu-fe-master-electrical-computer-engineering"
  },
  {
    "record_key": "ndu:program_source:ndu-fe-master-electrical-computer-engineering:3",
    "source_id": "NDU-SRC-006",
    "source_role": "ADMISSIONS",
    "source_order": 3,
    "evidence_text": "NDU-SRC-006",
    "notes": null,
    "program_id": "ndu-fe-master-electrical-computer-engineering"
  },
  {
    "record_key": "ndu:program_source:ndu-fe-master-electrical-computer-engineering:4",
    "source_id": "NDU-SRC-008",
    "source_role": "TUITION",
    "source_order": 4,
    "evidence_text": "NDU-SRC-008",
    "notes": null,
    "program_id": "ndu-fe-master-electrical-computer-engineering"
  },
  {
    "record_key": "ndu:program_source:ndu-fbae-master-financial-risk-management:1",
    "source_id": "NDU-SRC-003",
    "source_role": "PRIMARY",
    "source_order": 1,
    "evidence_text": "NDU-SRC-003",
    "notes": null,
    "program_id": "ndu-fbae-master-financial-risk-management"
  },
  {
    "record_key": "ndu:program_source:ndu-fbae-master-financial-risk-management:2",
    "source_id": "NDU-SRC-002",
    "source_role": "SECONDARY",
    "source_order": 2,
    "evidence_text": "NDU-SRC-002",
    "notes": null,
    "program_id": "ndu-fbae-master-financial-risk-management"
  },
  {
    "record_key": "ndu:program_source:ndu-fbae-master-financial-risk-management:3",
    "source_id": "NDU-SRC-006",
    "source_role": "ADMISSIONS",
    "source_order": 3,
    "evidence_text": "NDU-SRC-006",
    "notes": null,
    "program_id": "ndu-fbae-master-financial-risk-management"
  },
  {
    "record_key": "ndu:program_source:ndu-fbae-master-financial-risk-management:4",
    "source_id": "NDU-SRC-008",
    "source_role": "TUITION",
    "source_order": 4,
    "evidence_text": "NDU-SRC-008",
    "notes": null,
    "program_id": "ndu-fbae-master-financial-risk-management"
  },
  {
    "record_key": "ndu:program_source:ndu-fnhs-master-food-safety-quality-management:1",
    "source_id": "NDU-SRC-003",
    "source_role": "PRIMARY",
    "source_order": 1,
    "evidence_text": "NDU-SRC-003",
    "notes": null,
    "program_id": "ndu-fnhs-master-food-safety-quality-management"
  },
  {
    "record_key": "ndu:program_source:ndu-fnhs-master-food-safety-quality-management:2",
    "source_id": "NDU-SRC-002",
    "source_role": "SECONDARY",
    "source_order": 2,
    "evidence_text": "NDU-SRC-002",
    "notes": null,
    "program_id": "ndu-fnhs-master-food-safety-quality-management"
  },
  {
    "record_key": "ndu:program_source:ndu-fnhs-master-food-safety-quality-management:3",
    "source_id": "NDU-SRC-006",
    "source_role": "ADMISSIONS",
    "source_order": 3,
    "evidence_text": "NDU-SRC-006",
    "notes": null,
    "program_id": "ndu-fnhs-master-food-safety-quality-management"
  },
  {
    "record_key": "ndu:program_source:ndu-fnhs-master-food-safety-quality-management:4",
    "source_id": "NDU-SRC-008",
    "source_role": "TUITION",
    "source_order": 4,
    "evidence_text": "NDU-SRC-008",
    "notes": null,
    "program_id": "ndu-fnhs-master-food-safety-quality-management"
  },
  {
    "record_key": "ndu:program_source:ndu-fnhs-master-human-nutrition:1",
    "source_id": "NDU-SRC-003",
    "source_role": "PRIMARY",
    "source_order": 1,
    "evidence_text": "NDU-SRC-003",
    "notes": null,
    "program_id": "ndu-fnhs-master-human-nutrition"
  },
  {
    "record_key": "ndu:program_source:ndu-fnhs-master-human-nutrition:2",
    "source_id": "NDU-SRC-002",
    "source_role": "SECONDARY",
    "source_order": 2,
    "evidence_text": "NDU-SRC-002",
    "notes": null,
    "program_id": "ndu-fnhs-master-human-nutrition"
  },
  {
    "record_key": "ndu:program_source:ndu-fnhs-master-human-nutrition:3",
    "source_id": "NDU-SRC-006",
    "source_role": "ADMISSIONS",
    "source_order": 3,
    "evidence_text": "NDU-SRC-006",
    "notes": null,
    "program_id": "ndu-fnhs-master-human-nutrition"
  },
  {
    "record_key": "ndu:program_source:ndu-fnhs-master-human-nutrition:4",
    "source_id": "NDU-SRC-008",
    "source_role": "TUITION",
    "source_order": 4,
    "evidence_text": "NDU-SRC-008",
    "notes": null,
    "program_id": "ndu-fnhs-master-human-nutrition"
  },
  {
    "record_key": "ndu:program_source:ndu-fnas-master-industrial-chemistry:1",
    "source_id": "NDU-SRC-003",
    "source_role": "PRIMARY",
    "source_order": 1,
    "evidence_text": "NDU-SRC-003",
    "notes": null,
    "program_id": "ndu-fnas-master-industrial-chemistry"
  },
  {
    "record_key": "ndu:program_source:ndu-fnas-master-industrial-chemistry:2",
    "source_id": "NDU-SRC-002",
    "source_role": "SECONDARY",
    "source_order": 2,
    "evidence_text": "NDU-SRC-002",
    "notes": null,
    "program_id": "ndu-fnas-master-industrial-chemistry"
  },
  {
    "record_key": "ndu:program_source:ndu-fnas-master-industrial-chemistry:3",
    "source_id": "NDU-SRC-006",
    "source_role": "ADMISSIONS",
    "source_order": 3,
    "evidence_text": "NDU-SRC-006",
    "notes": null,
    "program_id": "ndu-fnas-master-industrial-chemistry"
  },
  {
    "record_key": "ndu:program_source:ndu-fnas-master-industrial-chemistry:4",
    "source_id": "NDU-SRC-008",
    "source_role": "TUITION",
    "source_order": 4,
    "evidence_text": "NDU-SRC-008",
    "notes": null,
    "program_id": "ndu-fnas-master-industrial-chemistry"
  },
  {
    "record_key": "ndu:program_source:ndu-fnas-master-mathematics:1",
    "source_id": "NDU-SRC-003",
    "source_role": "PRIMARY",
    "source_order": 1,
    "evidence_text": "NDU-SRC-003",
    "notes": null,
    "program_id": "ndu-fnas-master-mathematics"
  },
  {
    "record_key": "ndu:program_source:ndu-fnas-master-mathematics:2",
    "source_id": "NDU-SRC-002",
    "source_role": "SECONDARY",
    "source_order": 2,
    "evidence_text": "NDU-SRC-002",
    "notes": null,
    "program_id": "ndu-fnas-master-mathematics"
  },
  {
    "record_key": "ndu:program_source:ndu-fnas-master-mathematics:3",
    "source_id": "NDU-SRC-006",
    "source_role": "ADMISSIONS",
    "source_order": 3,
    "evidence_text": "NDU-SRC-006",
    "notes": null,
    "program_id": "ndu-fnas-master-mathematics"
  },
  {
    "record_key": "ndu:program_source:ndu-fnas-master-mathematics:4",
    "source_id": "NDU-SRC-008",
    "source_role": "TUITION",
    "source_order": 4,
    "evidence_text": "NDU-SRC-008",
    "notes": null,
    "program_id": "ndu-fnas-master-mathematics"
  },
  {
    "record_key": "ndu:program_source:ndu-fe-master-mechanical-engineering:1",
    "source_id": "NDU-SRC-003",
    "source_role": "PRIMARY",
    "source_order": 1,
    "evidence_text": "NDU-SRC-003",
    "notes": null,
    "program_id": "ndu-fe-master-mechanical-engineering"
  },
  {
    "record_key": "ndu:program_source:ndu-fe-master-mechanical-engineering:2",
    "source_id": "NDU-SRC-002",
    "source_role": "SECONDARY",
    "source_order": 2,
    "evidence_text": "NDU-SRC-002",
    "notes": null,
    "program_id": "ndu-fe-master-mechanical-engineering"
  },
  {
    "record_key": "ndu:program_source:ndu-fe-master-mechanical-engineering:3",
    "source_id": "NDU-SRC-006",
    "source_role": "ADMISSIONS",
    "source_order": 3,
    "evidence_text": "NDU-SRC-006",
    "notes": null,
    "program_id": "ndu-fe-master-mechanical-engineering"
  },
  {
    "record_key": "ndu:program_source:ndu-fe-master-mechanical-engineering:4",
    "source_id": "NDU-SRC-008",
    "source_role": "TUITION",
    "source_order": 4,
    "evidence_text": "NDU-SRC-008",
    "notes": null,
    "program_id": "ndu-fe-master-mechanical-engineering"
  },
  {
    "record_key": "ndu:program_source:ndu-faad-master-sustainable-architecture:1",
    "source_id": "NDU-SRC-003",
    "source_role": "PRIMARY",
    "source_order": 1,
    "evidence_text": "NDU-SRC-003",
    "notes": null,
    "program_id": "ndu-faad-master-sustainable-architecture"
  },
  {
    "record_key": "ndu:program_source:ndu-faad-master-sustainable-architecture:2",
    "source_id": "NDU-SRC-002",
    "source_role": "SECONDARY",
    "source_order": 2,
    "evidence_text": "NDU-SRC-002",
    "notes": null,
    "program_id": "ndu-faad-master-sustainable-architecture"
  },
  {
    "record_key": "ndu:program_source:ndu-faad-master-sustainable-architecture:3",
    "source_id": "NDU-SRC-006",
    "source_role": "ADMISSIONS",
    "source_order": 3,
    "evidence_text": "NDU-SRC-006",
    "notes": null,
    "program_id": "ndu-faad-master-sustainable-architecture"
  },
  {
    "record_key": "ndu:program_source:ndu-faad-master-sustainable-architecture:4",
    "source_id": "NDU-SRC-008",
    "source_role": "TUITION",
    "source_order": 4,
    "evidence_text": "NDU-SRC-008",
    "notes": null,
    "program_id": "ndu-faad-master-sustainable-architecture"
  },
  {
    "record_key": "ndu:program_source:ndu-fbae-master-mba:1",
    "source_id": "NDU-SRC-003",
    "source_role": "PRIMARY",
    "source_order": 1,
    "evidence_text": "NDU-SRC-003",
    "notes": null,
    "program_id": "ndu-fbae-master-mba"
  },
  {
    "record_key": "ndu:program_source:ndu-fbae-master-mba:2",
    "source_id": "NDU-SRC-002",
    "source_role": "SECONDARY",
    "source_order": 2,
    "evidence_text": "NDU-SRC-002",
    "notes": null,
    "program_id": "ndu-fbae-master-mba"
  },
  {
    "record_key": "ndu:program_source:ndu-fbae-master-mba:3",
    "source_id": "NDU-SRC-006",
    "source_role": "ADMISSIONS",
    "source_order": 3,
    "evidence_text": "NDU-SRC-006",
    "notes": null,
    "program_id": "ndu-fbae-master-mba"
  },
  {
    "record_key": "ndu:program_source:ndu-fbae-master-mba:4",
    "source_id": "NDU-SRC-008",
    "source_role": "TUITION",
    "source_order": 4,
    "evidence_text": "NDU-SRC-008",
    "notes": null,
    "program_id": "ndu-fbae-master-mba"
  },
  {
    "record_key": "ndu:program_source:ndu-fbae-master-ms-business-strategy:1",
    "source_id": "NDU-SRC-003",
    "source_role": "PRIMARY",
    "source_order": 1,
    "evidence_text": "NDU-SRC-003",
    "notes": null,
    "program_id": "ndu-fbae-master-ms-business-strategy"
  },
  {
    "record_key": "ndu:program_source:ndu-fbae-master-ms-business-strategy:2",
    "source_id": "NDU-SRC-002",
    "source_role": "SECONDARY",
    "source_order": 2,
    "evidence_text": "NDU-SRC-002",
    "notes": null,
    "program_id": "ndu-fbae-master-ms-business-strategy"
  },
  {
    "record_key": "ndu:program_source:ndu-fbae-master-ms-business-strategy:3",
    "source_id": "NDU-SRC-006",
    "source_role": "ADMISSIONS",
    "source_order": 3,
    "evidence_text": "NDU-SRC-006",
    "notes": null,
    "program_id": "ndu-fbae-master-ms-business-strategy"
  },
  {
    "record_key": "ndu:program_source:ndu-fbae-master-ms-business-strategy:4",
    "source_id": "NDU-SRC-008",
    "source_role": "TUITION",
    "source_order": 4,
    "evidence_text": "NDU-SRC-008",
    "notes": null,
    "program_id": "ndu-fbae-master-ms-business-strategy"
  }
]$NDU_PROGRAM_SOURCES$::jsonb) AS x(record_key TEXT, source_id TEXT, source_role TEXT, source_order INTEGER, evidence_text TEXT, notes TEXT, program_id TEXT)
    JOIN graduate_program gp ON gp.university_id = v_university_id AND gp.program_key = x.program_id
    JOIN ndu_source_seed ss ON ss.source_id = x.source_id
    JOIN source s ON s.university_id = v_university_id AND s.url = ss.url
    ON CONFLICT (program_id, source_id, source_role) DO UPDATE SET source_order = EXCLUDED.source_order, evidence_text = EXCLUDED.evidence_text, notes = EXCLUDED.notes, updated_at = NOW();

END $$;
