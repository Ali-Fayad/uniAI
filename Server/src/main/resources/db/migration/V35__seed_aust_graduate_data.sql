-- AUST graduate data seed migration.
-- Idempotent import for the canonical AUST graduate dataset.

DO $$
DECLARE
    v_university_id BIGINT;
BEGIN

    INSERT INTO university (name, name_ar, acronym, country, city, latitude, longitude, campus_name, campus_type)
    SELECT 'American University of Science and Technology', NULL, 'AUST', 'Lebanon', NULL, NULL, NULL, NULL, NULL
    WHERE NOT EXISTS (SELECT 1 FROM university WHERE name = 'American University of Science and Technology');

    SELECT id INTO v_university_id FROM university WHERE name = 'American University of Science and Technology' ORDER BY id LIMIT 1;

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

    CREATE TEMP TABLE aust_source_seed (source_id TEXT PRIMARY KEY, title TEXT NOT NULL, url TEXT NOT NULL, source_type TEXT NOT NULL, accessed_at DATE, notes TEXT) ON COMMIT DROP;
    INSERT INTO aust_source_seed SELECT source_id, title, url, source_type, accessed_at, notes FROM jsonb_to_recordset($AUST_SOURCES$[
  {
    "source_id": "AUST-SRC-001",
    "title": "AUST official homepage",
    "url": "https://www.aust.edu.lb/",
    "source_type": "WEB",
    "accessed_at": "2026-07-04",
    "notes": "Official homepage used to confirm official domain, navigation, faculties, calendar, online services, application links, and campus/news context."
  },
  {
    "source_id": "AUST-SRC-002",
    "title": "Faculties & Programs",
    "url": "https://www.aust.edu.lb/facultyandprograms",
    "source_type": "WEB",
    "accessed_at": "2026-07-04",
    "notes": "Official faculty/program index; lists four faculties and indicates graduate programs under Arts and Sciences, Health Sciences, Engineering, and Business and Economics."
  },
  {
    "source_id": "AUST-SRC-003",
    "title": "Graduate Application Instructions",
    "url": "https://s1.aust.edu.lb/GraduateApp/GraduateInstructions.aspx",
    "source_type": "WEB",
    "accessed_at": "2026-07-04",
    "notes": "Primary graduate-admissions source. Lists required documents, English entrance requirements, and graduate-level majors by faculty."
  },
  {
    "source_id": "AUST-SRC-004",
    "title": "AUST Online Graduate Application",
    "url": "https://s1.aust.edu.lb/GraduateApp/Default.aspx",
    "source_type": "WEB",
    "accessed_at": "2026-07-04",
    "notes": "Official graduate application portal. Confirms online graduate application workflow and requirement for valid email/account."
  },
  {
    "source_id": "AUST-SRC-005",
    "title": "Graduate Programs - Admission Requirements",
    "url": "https://www.aust.edu.lb/section/the-admission/graduate-programs/admission-requirements/203",
    "source_type": "WEB",
    "accessed_at": "2026-07-04",
    "notes": "Graduate admission requirements page. Search result content confirms bachelor's/equivalent degree, TOEFL or AUST English equivalent, statement of purpose, and advisor assignment."
  },
  {
    "source_id": "AUST-SRC-006",
    "title": "Graduate Programs - Academic Rules",
    "url": "https://www.aust.edu.lb/section/the-admission/graduate-programs/academic-rules/205",
    "source_type": "WEB",
    "accessed_at": "2026-07-04",
    "notes": "Graduate academic rules page. Confirms master's degree requires 30-39 graduate credits depending on program, minimum cumulative GPA 3.00/4.00, and no more than two C grades."
  },
  {
    "source_id": "AUST-SRC-007",
    "title": "Graduate Tuition Fees",
    "url": "https://www.aust.edu.lb/section/the-admission/tuition-fees-and-expenses/graduate-tuition-fees/225",
    "source_type": "WEB",
    "accessed_at": "2026-07-04",
    "notes": "Official graduate tuition-fee page. Confirms MBA/MS graduate clear tuition at USD 320 per credit hour, probation/remedial or prerequisite rates, other fees, and installment/deferred-payment method."
  },
  {
    "source_id": "AUST-SRC-008",
    "title": "Financial Aid",
    "url": "https://www.aust.edu.lb/section/the-admission/tuition-fees-and-expenses/financial-aid/227",
    "source_type": "WEB",
    "accessed_at": "2026-07-04",
    "notes": "Official financial-aid page. Confirms partial scholarships based on need, minimum completed credits, full-time registration, and GPA condition."
  },
  {
    "source_id": "AUST-SRC-009",
    "title": "University Calendar - Summer I Semester 2025-2026",
    "url": "https://www.aust.edu.lb/section/university-calendar/summer-i-semester-2025--2026/2661",
    "source_type": "WEB",
    "accessed_at": "2026-07-04",
    "notes": "Official academic calendar page for Summer I 2025-2026."
  },
  {
    "source_id": "AUST-SRC-010",
    "title": "University Calendar - Summer II Semester 2025-2026",
    "url": "https://www.aust.edu.lb/section/university-calendar/summer-ii-semester-2025--2026/2662",
    "source_type": "WEB",
    "accessed_at": "2026-07-04",
    "notes": "Official academic calendar page for Summer II 2025-2026."
  },
  {
    "source_id": "AUST-SRC-011",
    "title": "University Calendar - Fall Semester 2026-2027",
    "url": "https://www.aust.edu.lb/section/university-calendar/fall-semester-2026-2027/2663",
    "source_type": "WEB",
    "accessed_at": "2026-07-04",
    "notes": "Official academic calendar page for Fall 2026-2027."
  },
  {
    "source_id": "AUST-SRC-012",
    "title": "Faculty of Arts and Sciences - MA in Communication Arts (39 cr.hr.)",
    "url": "https://www.aust.edu.lb/facultydetails/faculty-of-arts-and-sciences/graduate-programs/ma-in-communication-arts-39-crhr/318",
    "source_type": "WEB",
    "accessed_at": "2026-07-04",
    "notes": "Program page discovered for MA in Communication Arts. Also shows Arts and Sciences graduate navigation including MS in Computer Science and MS in Information & Communications Technology."
  },
  {
    "source_id": "AUST-SRC-013",
    "title": "Faculty of Arts and Sciences - MS in Computer Science (39 cr.hr.)",
    "url": "https://www.aust.edu.lb/facultydetails/faculty-of-arts-and-sciences/graduate-programs/ms-in-computer-science-39-crhr/major-description/450",
    "source_type": "WEB",
    "accessed_at": "2026-07-04",
    "notes": "Program/major-description page for MS in Computer Science."
  },
  {
    "source_id": "AUST-SRC-014",
    "title": "Faculty of Arts and Sciences - MS in Information & Communications Technology (39 cr.hr.)",
    "url": "https://www.aust.edu.lb/facultydetails/faculty-of-arts-and-sciences/graduate-programs/ms-in-information-communications-technology-39-crhr/major-description/451",
    "source_type": "WEB",
    "accessed_at": "2026-07-04",
    "notes": "Program/major-description page for MS in Information & Communications Technology."
  },
  {
    "source_id": "AUST-SRC-015",
    "title": "Faculty of Business and Economics - Master in Business Administration (39 cr.hr.)",
    "url": "https://www.aust.edu.lb/facultydetails/faculty-of-business-and-economics/graduate-programs/master-in-business-administration-39-crhr/640",
    "source_type": "WEB",
    "accessed_at": "2026-07-04",
    "notes": "Program page for Master in Business Administration."
  },
  {
    "source_id": "AUST-SRC-016",
    "title": "Faculty of Business and Economics - MS in Business Administration Track Finance (39 cr.hr.)",
    "url": "https://www.aust.edu.lb/facultydetails/faculty-of-business-and-economics/graduate-programs/ms-in-business-administration-track-finance-39-crhr/387",
    "source_type": "WEB",
    "accessed_at": "2026-07-04",
    "notes": "Program page for MS in Business Administration Track Finance. Search-result navigation also confirms Accounting, Economics, Management, MIS, and Marketing tracks."
  },
  {
    "source_id": "AUST-SRC-017",
    "title": "Faculty of Business and Economics - MS/MBA in Management (39 cr.hr.)",
    "url": "https://www.aust.edu.lb/facultydetails/faculty-of-business-and-economics/graduate-programs/ms-mba-in-management-%2839-crhr%29/390",
    "source_type": "WEB",
    "accessed_at": "2026-07-04",
    "notes": "Program/navigation page for management-related graduate business program."
  },
  {
    "source_id": "AUST-SRC-018",
    "title": "Faculty of Engineering - MS in Mechatronics Engineering (39 cr.hr.)",
    "url": "https://www.aust.edu.lb/facultydetails/faculty-of-engineering/graduate-programs/ms-in-mechatronics-engineering-39-crhr/2658",
    "source_type": "WEB",
    "accessed_at": "2026-07-04",
    "notes": "Program page for MS in Mechatronics Engineering."
  },
  {
    "source_id": "AUST-SRC-019",
    "title": "Faculty of Engineering - MS in CCE highlighting Mechatronics Engineering (39 cr.hr.)",
    "url": "https://www.aust.edu.lb/facultydetails/faculty-of-engineering/graduate-programs/ms-in-cce-highlighting-mechatronics-engineering-39-crhr/major-description/382",
    "source_type": "WEB",
    "accessed_at": "2026-07-04",
    "notes": "Official engineering graduate major-description page found during search; title/content references MS in Mechatronics Engineering and engineering graduate research orientation."
  },
  {
    "source_id": "AUST-SRC-020",
    "title": "Faculty of Health Sciences - MS Biotechnology in Forensic Science (36 cr.hr.)",
    "url": "https://www.aust.edu.lb/facultydetails/faculty-of-health-sciences/graduate-programs/ms-biotechnology-in-forensic-science-%2836-crhr%29/major-description/407",
    "source_type": "WEB",
    "accessed_at": "2026-07-04",
    "notes": "FHS graduate page. Confirms FHS offers MS degrees in Biotechnology, Bio-analytical Toxicology, and Optics and Optometry; Biotechnology includes DNA Technologies and Forensic Science tracks."
  },
  {
    "source_id": "AUST-SRC-021",
    "title": "Students Handbook PDF",
    "url": "https://api.aust.edu.lb/content/uploads/files/226~Students-Handbook.pdf",
    "source_type": "PDF",
    "accessed_at": "2026-07-04",
    "notes": "Official AUST student handbook PDF. Contains policy/regulatory statements covering admission, registration, tuition/fees, conduct, academic standing, candidacy, graduation, and student life."
  },
  {
    "source_id": "AUST-SRC-022",
    "title": "Rules and Regulations PDF",
    "url": "https://api.aust.edu.lb/content/uploads/files/927~rules-and-regulations.pdf",
    "source_type": "PDF",
    "accessed_at": "2026-07-04",
    "notes": "Official rules/regulations PDF. Includes international-student rule and graduate full-time load note."
  },
  {
    "source_id": "AUST-SRC-023",
    "title": "Blended MBA Program Brochure PDF",
    "url": "https://api.aust.edu.lb/content/uploads/files/315~BMBA-Brochure.pdf",
    "source_type": "PDF",
    "accessed_at": "2026-07-04",
    "notes": "Official B-MBA brochure PDF. Describes blended MBA structure and program components. Marked for review because the brochure says 36 credits, while the current graduate application/program page lists MBA as 39 credits."
  },
  {
    "source_id": "AUST-SRC-024",
    "title": "MBA Finance Brochure PDF",
    "url": "https://api.aust.edu.lb/content/uploads/files/MBA-Finance.pdf",
    "source_type": "PDF",
    "accessed_at": "2026-07-04",
    "notes": "Official MBA Finance brochure PDF. Program marketing source; use lower priority than current program/admissions pages for structured data."
  },
  {
    "source_id": "AUST-SRC-025",
    "title": "MBA Management Brochure PDF",
    "url": "https://api.aust.edu.lb/content/uploads/files/MBA-MGT.pdf",
    "source_type": "PDF",
    "accessed_at": "2026-07-04",
    "notes": "Official MBA Management brochure PDF. Program marketing source; use lower priority than current program/admissions pages for structured data."
  },
  {
    "source_id": "AUST-SRC-026",
    "title": "Facts and Figures 2025-2026",
    "url": "https://www.aust.edu.lb/section/about-aust/facts-and-figures/57",
    "source_type": "WEB",
    "accessed_at": "2026-07-04",
    "notes": "Official facts-and-figures page for institutional identity, campuses, and academic-year context."
  },
  {
    "source_id": "AUST-SRC-027",
    "title": "International Students Office",
    "url": "https://www.aust.edu.lb/section/international-students-office/international-students-office/615",
    "source_type": "WEB",
    "accessed_at": "2026-07-04",
    "notes": "Official International Students Office page discovered in site navigation; content extraction was limited, so rules/regulations PDF is stronger for concrete international-student admission rule."
  }
]$AUST_SOURCES$) AS x(source_id TEXT, title TEXT, url TEXT, source_type TEXT, accessed_at DATE, notes TEXT);

    INSERT INTO source (university_id, title, url, source_type, accessed_at) SELECT v_university_id, title, url, source_type, accessed_at FROM aust_source_seed ON CONFLICT (university_id, url) DO UPDATE SET title = EXCLUDED.title, source_type = EXCLUDED.source_type, accessed_at = EXCLUDED.accessed_at, updated_at = NOW();

    CREATE TEMP TABLE aust_faculty_seed (name TEXT PRIMARY KEY, short_name TEXT, faculty_type TEXT NOT NULL, official_url TEXT, notes TEXT) ON COMMIT DROP;
    INSERT INTO aust_faculty_seed SELECT name, short_name, faculty_type, official_url, notes FROM jsonb_to_recordset($AUST_FACULTIES$[
  {
    "name": "Faculty of Arts and Sciences",
    "short_name": null,
    "faculty_type": "FACULTY",
    "official_url": null,
    "notes": "Imported from the finalized AUST graduate inventory."
  },
  {
    "name": "Faculty of Business and Economics",
    "short_name": null,
    "faculty_type": "FACULTY",
    "official_url": null,
    "notes": "Imported from the finalized AUST graduate inventory."
  },
  {
    "name": "Faculty of Engineering",
    "short_name": null,
    "faculty_type": "FACULTY",
    "official_url": null,
    "notes": "Imported from the finalized AUST graduate inventory."
  },
  {
    "name": "Faculty of Health Sciences",
    "short_name": null,
    "faculty_type": "FACULTY",
    "official_url": null,
    "notes": "Imported from the finalized AUST graduate inventory."
  }
]$AUST_FACULTIES$) AS x(name TEXT, short_name TEXT, faculty_type TEXT, official_url TEXT, notes TEXT);

    INSERT INTO university_faculty (university_id, name, short_name, faculty_type, official_url, notes) SELECT v_university_id, name, short_name, faculty_type, official_url, notes FROM aust_faculty_seed ON CONFLICT (university_id, name) DO UPDATE SET short_name = EXCLUDED.short_name, faculty_type = EXCLUDED.faculty_type, official_url = EXCLUDED.official_url, notes = EXCLUDED.notes, updated_at = NOW();

    CREATE TEMP TABLE aust_program_seed (
        id TEXT PRIMARY KEY, faculty TEXT NOT NULL, department TEXT, major_category TEXT, major TEXT, degree_type TEXT NOT NULL, official_degree_name TEXT NOT NULL, thesis_or_non_thesis TEXT, credits INTEGER, duration_value NUMERIC(10, 2), duration_unit TEXT, language TEXT, delivery_mode TEXT, program_description TEXT, admission_requirements TEXT, gre_requirement TEXT, gmat_requirement TEXT, portfolio_requirement TEXT, interview_requirement TEXT, experience_requirement TEXT, accreditation TEXT, official_program_url TEXT, source_ids JSONB NOT NULL, notes TEXT, tuition_academic_year TEXT NOT NULL, tuition_currency TEXT NOT NULL, tuition_billing_basis TEXT NOT NULL, tuition_amount NUMERIC(12, 2) NOT NULL, tuition_category TEXT NOT NULL, tuition_notes TEXT NOT NULL, tuition_source_ids JSONB NOT NULL, tuition_scope_level TEXT NOT NULL, concentrations_or_tracks JSONB NOT NULL
    ) ON COMMIT DROP;
    INSERT INTO aust_program_seed SELECT * FROM jsonb_to_recordset($AUST_PROGRAMS$[
  {
    "id": "aust-fas-master-communication-arts",
    "faculty": "Faculty of Arts and Sciences",
    "department": null,
    "major_category": "Arts and Sciences",
    "major": "Communication Arts",
    "degree_type": "MASTER",
    "official_degree_name": "Master of Arts in Communication Arts",
    "thesis_or_non_thesis": null,
    "credits": 39,
    "duration_value": null,
    "duration_unit": null,
    "language": null,
    "delivery_mode": null,
    "program_description": null,
    "admission_requirements": null,
    "gre_requirement": null,
    "gmat_requirement": null,
    "portfolio_requirement": null,
    "interview_requirement": null,
    "experience_requirement": null,
    "accreditation": null,
    "official_program_url": "https://www.aust.edu.lb/facultydetails/faculty-of-arts-and-sciences/graduate-programs/ma-in-communication-arts-39-crhr/318",
    "source_ids": [
      "AUST-SRC-003",
      "AUST-SRC-002",
      "AUST-SRC-012"
    ],
    "notes": "Listed in the graduate application instructions under the Faculty of Arts and Sciences and confirmed by the public program page.",
    "tuition_academic_year": "2025-2026",
    "tuition_currency": "USD",
    "tuition_billing_basis": "PER_CREDIT",
    "tuition_amount": 320,
    "tuition_category": "Graduate Programs",
    "tuition_notes": "Official graduate tuition page lists MBA/MS graduate clear tuition at USD 320 per credit hour.",
    "tuition_source_ids": [
      "AUST-SRC-007"
    ],
    "tuition_scope_level": "PROGRAM",
    "concentrations_or_tracks": []
  },
  {
    "id": "aust-fas-master-computer-science",
    "faculty": "Faculty of Arts and Sciences",
    "department": null,
    "major_category": "Arts and Sciences",
    "major": "Computer Science",
    "degree_type": "MASTER",
    "official_degree_name": "Master of Science in Computer Science",
    "thesis_or_non_thesis": null,
    "credits": 39,
    "duration_value": null,
    "duration_unit": null,
    "language": null,
    "delivery_mode": null,
    "program_description": null,
    "admission_requirements": null,
    "gre_requirement": null,
    "gmat_requirement": null,
    "portfolio_requirement": null,
    "interview_requirement": null,
    "experience_requirement": null,
    "accreditation": null,
    "official_program_url": "https://www.aust.edu.lb/facultydetails/faculty-of-arts-and-sciences/graduate-programs/ms-in-computer-science-39-crhr/major-description/450",
    "source_ids": [
      "AUST-SRC-003",
      "AUST-SRC-002",
      "AUST-SRC-013"
    ],
    "notes": "Listed in the graduate application instructions and confirmed by the program page.",
    "tuition_academic_year": "2025-2026",
    "tuition_currency": "USD",
    "tuition_billing_basis": "PER_CREDIT",
    "tuition_amount": 320,
    "tuition_category": "Graduate Programs",
    "tuition_notes": "Official graduate tuition page lists MBA/MS graduate clear tuition at USD 320 per credit hour.",
    "tuition_source_ids": [
      "AUST-SRC-007"
    ],
    "tuition_scope_level": "PROGRAM",
    "concentrations_or_tracks": []
  },
  {
    "id": "aust-fas-master-information-communications-technology",
    "faculty": "Faculty of Arts and Sciences",
    "department": null,
    "major_category": "Arts and Sciences",
    "major": "Information & Communications Technology",
    "degree_type": "MASTER",
    "official_degree_name": "Master of Science in Information & Communications Technology",
    "thesis_or_non_thesis": null,
    "credits": 39,
    "duration_value": null,
    "duration_unit": null,
    "language": null,
    "delivery_mode": null,
    "program_description": null,
    "admission_requirements": null,
    "gre_requirement": null,
    "gmat_requirement": null,
    "portfolio_requirement": null,
    "interview_requirement": null,
    "experience_requirement": null,
    "accreditation": null,
    "official_program_url": "https://www.aust.edu.lb/facultydetails/faculty-of-arts-and-sciences/graduate-programs/ms-in-information-communications-technology-39-crhr/major-description/451",
    "source_ids": [
      "AUST-SRC-003",
      "AUST-SRC-002",
      "AUST-SRC-014"
    ],
    "notes": "Listed in the graduate application instructions and confirmed by the program page.",
    "tuition_academic_year": "2025-2026",
    "tuition_currency": "USD",
    "tuition_billing_basis": "PER_CREDIT",
    "tuition_amount": 320,
    "tuition_category": "Graduate Programs",
    "tuition_notes": "Official graduate tuition page lists MBA/MS graduate clear tuition at USD 320 per credit hour.",
    "tuition_source_ids": [
      "AUST-SRC-007"
    ],
    "tuition_scope_level": "PROGRAM",
    "concentrations_or_tracks": []
  },
  {
    "id": "aust-fbe-master-business-administration",
    "faculty": "Faculty of Business and Economics",
    "department": null,
    "major_category": "Business",
    "major": "Business Administration",
    "degree_type": "MASTER",
    "official_degree_name": "Master of Business Administration",
    "thesis_or_non_thesis": null,
    "credits": 39,
    "duration_value": null,
    "duration_unit": null,
    "language": null,
    "delivery_mode": null,
    "program_description": null,
    "admission_requirements": null,
    "gre_requirement": null,
    "gmat_requirement": null,
    "portfolio_requirement": null,
    "interview_requirement": null,
    "experience_requirement": null,
    "accreditation": null,
    "official_program_url": "https://www.aust.edu.lb/facultydetails/faculty-of-business-and-economics/graduate-programs/master-in-business-administration-39-crhr/640",
    "source_ids": [
      "AUST-SRC-003",
      "AUST-SRC-002",
      "AUST-SRC-015",
      "AUST-SRC-023"
    ],
    "notes": "Inventory follows the current admissions/program page at 39 credits; the official blended MBA brochure lists 36 credits, so the brochure discrepancy is recorded but not resolved here.",
    "tuition_academic_year": "2025-2026",
    "tuition_currency": "USD",
    "tuition_billing_basis": "PER_CREDIT",
    "tuition_amount": 320,
    "tuition_category": "Graduate Programs",
    "tuition_notes": "Official graduate tuition page lists MBA/MS graduate clear tuition at USD 320 per credit hour. MBA brochure lists 36 credits, but the current admissions/program page uses 39 credits.",
    "tuition_source_ids": [
      "AUST-SRC-007"
    ],
    "tuition_scope_level": "PROGRAM",
    "concentrations_or_tracks": []
  },
  {
    "id": "aust-fbe-master-business-administration-accounting",
    "faculty": "Faculty of Business and Economics",
    "department": null,
    "major_category": "Business",
    "major": "Business Administration",
    "degree_type": "MASTER",
    "official_degree_name": "Master of Science in Business Administration, Emphasis/Track Accounting",
    "thesis_or_non_thesis": null,
    "credits": 39,
    "duration_value": null,
    "duration_unit": null,
    "language": null,
    "delivery_mode": null,
    "program_description": null,
    "admission_requirements": null,
    "gre_requirement": null,
    "gmat_requirement": null,
    "portfolio_requirement": null,
    "interview_requirement": null,
    "experience_requirement": null,
    "accreditation": null,
    "official_program_url": null,
    "source_ids": [
      "AUST-SRC-003",
      "AUST-SRC-002"
    ],
    "notes": "Listed in the graduate application instructions; no distinct public program page was recovered in this pass.",
    "tuition_academic_year": "2025-2026",
    "tuition_currency": "USD",
    "tuition_billing_basis": "PER_CREDIT",
    "tuition_amount": 320,
    "tuition_category": "Graduate Programs",
    "tuition_notes": "Official graduate tuition page lists MBA/MS graduate clear tuition at USD 320 per credit hour.",
    "tuition_source_ids": [
      "AUST-SRC-007"
    ],
    "tuition_scope_level": "PROGRAM",
    "concentrations_or_tracks": []
  },
  {
    "id": "aust-fbe-master-business-administration-finance",
    "faculty": "Faculty of Business and Economics",
    "department": null,
    "major_category": "Business",
    "major": "Business Administration",
    "degree_type": "MASTER",
    "official_degree_name": "Master of Science in Business Administration, Emphasis/Track Finance",
    "thesis_or_non_thesis": null,
    "credits": 39,
    "duration_value": null,
    "duration_unit": null,
    "language": null,
    "delivery_mode": null,
    "program_description": null,
    "admission_requirements": null,
    "gre_requirement": null,
    "gmat_requirement": null,
    "portfolio_requirement": null,
    "interview_requirement": null,
    "experience_requirement": null,
    "accreditation": null,
    "official_program_url": "https://www.aust.edu.lb/facultydetails/faculty-of-business-and-economics/graduate-programs/ms-in-business-administration-track-finance-39-crhr/387",
    "source_ids": [
      "AUST-SRC-003",
      "AUST-SRC-002",
      "AUST-SRC-016"
    ],
    "notes": "Listed in the graduate application instructions and confirmed by the finance program page.",
    "tuition_academic_year": "2025-2026",
    "tuition_currency": "USD",
    "tuition_billing_basis": "PER_CREDIT",
    "tuition_amount": 320,
    "tuition_category": "Graduate Programs",
    "tuition_notes": "Official graduate tuition page lists MBA/MS graduate clear tuition at USD 320 per credit hour.",
    "tuition_source_ids": [
      "AUST-SRC-007"
    ],
    "tuition_scope_level": "PROGRAM",
    "concentrations_or_tracks": []
  },
  {
    "id": "aust-fbe-master-business-administration-economics",
    "faculty": "Faculty of Business and Economics",
    "department": null,
    "major_category": "Business",
    "major": "Business Administration",
    "degree_type": "MASTER",
    "official_degree_name": "Master of Science in Business Administration, Emphasis/Track Economics",
    "thesis_or_non_thesis": null,
    "credits": 39,
    "duration_value": null,
    "duration_unit": null,
    "language": null,
    "delivery_mode": null,
    "program_description": null,
    "admission_requirements": null,
    "gre_requirement": null,
    "gmat_requirement": null,
    "portfolio_requirement": null,
    "interview_requirement": null,
    "experience_requirement": null,
    "accreditation": null,
    "official_program_url": null,
    "source_ids": [
      "AUST-SRC-003",
      "AUST-SRC-002"
    ],
    "notes": "Listed in the graduate application instructions; no distinct public program page was recovered in this pass.",
    "tuition_academic_year": "2025-2026",
    "tuition_currency": "USD",
    "tuition_billing_basis": "PER_CREDIT",
    "tuition_amount": 320,
    "tuition_category": "Graduate Programs",
    "tuition_notes": "Official graduate tuition page lists MBA/MS graduate clear tuition at USD 320 per credit hour.",
    "tuition_source_ids": [
      "AUST-SRC-007"
    ],
    "tuition_scope_level": "PROGRAM",
    "concentrations_or_tracks": []
  },
  {
    "id": "aust-fbe-master-business-administration-hospitality-management",
    "faculty": "Faculty of Business and Economics",
    "department": null,
    "major_category": "Business",
    "major": "Business Administration",
    "degree_type": "MASTER",
    "official_degree_name": "Master of Science in Business Administration, Emphasis/Track Hospitality Management",
    "thesis_or_non_thesis": null,
    "credits": 39,
    "duration_value": null,
    "duration_unit": null,
    "language": null,
    "delivery_mode": null,
    "program_description": null,
    "admission_requirements": null,
    "gre_requirement": null,
    "gmat_requirement": null,
    "portfolio_requirement": null,
    "interview_requirement": null,
    "experience_requirement": null,
    "accreditation": null,
    "official_program_url": null,
    "source_ids": [
      "AUST-SRC-003",
      "AUST-SRC-002"
    ],
    "notes": "Listed in the graduate application instructions; no distinct public program page was recovered in this pass.",
    "tuition_academic_year": "2025-2026",
    "tuition_currency": "USD",
    "tuition_billing_basis": "PER_CREDIT",
    "tuition_amount": 320,
    "tuition_category": "Graduate Programs",
    "tuition_notes": "Official graduate tuition page lists MBA/MS graduate clear tuition at USD 320 per credit hour.",
    "tuition_source_ids": [
      "AUST-SRC-007"
    ],
    "tuition_scope_level": "PROGRAM",
    "concentrations_or_tracks": []
  },
  {
    "id": "aust-fbe-master-business-administration-management",
    "faculty": "Faculty of Business and Economics",
    "department": null,
    "major_category": "Business",
    "major": "Business Administration",
    "degree_type": "MASTER",
    "official_degree_name": "Master of Science in Business Administration, Emphasis/Track Management",
    "thesis_or_non_thesis": null,
    "credits": 39,
    "duration_value": null,
    "duration_unit": null,
    "language": null,
    "delivery_mode": null,
    "program_description": null,
    "admission_requirements": null,
    "gre_requirement": null,
    "gmat_requirement": null,
    "portfolio_requirement": null,
    "interview_requirement": null,
    "experience_requirement": null,
    "accreditation": null,
    "official_program_url": "https://www.aust.edu.lb/facultydetails/faculty-of-business-and-economics/graduate-programs/ms-mba-in-management-%2839-crhr%29/390",
    "source_ids": [
      "AUST-SRC-003",
      "AUST-SRC-002",
      "AUST-SRC-017"
    ],
    "notes": "Listed in the graduate application instructions; the public program page is titled MS/MBA in Management.",
    "tuition_academic_year": "2025-2026",
    "tuition_currency": "USD",
    "tuition_billing_basis": "PER_CREDIT",
    "tuition_amount": 320,
    "tuition_category": "Graduate Programs",
    "tuition_notes": "Official graduate tuition page lists MBA/MS graduate clear tuition at USD 320 per credit hour.",
    "tuition_source_ids": [
      "AUST-SRC-007"
    ],
    "tuition_scope_level": "PROGRAM",
    "concentrations_or_tracks": []
  },
  {
    "id": "aust-fbe-master-business-administration-management-information-systems",
    "faculty": "Faculty of Business and Economics",
    "department": null,
    "major_category": "Business",
    "major": "Business Administration",
    "degree_type": "MASTER",
    "official_degree_name": "Master of Science in Business Administration, Emphasis/Track Management Information Systems",
    "thesis_or_non_thesis": null,
    "credits": 39,
    "duration_value": null,
    "duration_unit": null,
    "language": null,
    "delivery_mode": null,
    "program_description": null,
    "admission_requirements": null,
    "gre_requirement": null,
    "gmat_requirement": null,
    "portfolio_requirement": null,
    "interview_requirement": null,
    "experience_requirement": null,
    "accreditation": null,
    "official_program_url": null,
    "source_ids": [
      "AUST-SRC-003",
      "AUST-SRC-002"
    ],
    "notes": "Listed in the graduate application instructions; no distinct public program page was recovered in this pass.",
    "tuition_academic_year": "2025-2026",
    "tuition_currency": "USD",
    "tuition_billing_basis": "PER_CREDIT",
    "tuition_amount": 320,
    "tuition_category": "Graduate Programs",
    "tuition_notes": "Official graduate tuition page lists MBA/MS graduate clear tuition at USD 320 per credit hour.",
    "tuition_source_ids": [
      "AUST-SRC-007"
    ],
    "tuition_scope_level": "PROGRAM",
    "concentrations_or_tracks": []
  },
  {
    "id": "aust-fbe-master-business-administration-marketing",
    "faculty": "Faculty of Business and Economics",
    "department": null,
    "major_category": "Business",
    "major": "Business Administration",
    "degree_type": "MASTER",
    "official_degree_name": "Master of Science in Business Administration, Emphasis/Track Marketing",
    "thesis_or_non_thesis": null,
    "credits": 39,
    "duration_value": null,
    "duration_unit": null,
    "language": null,
    "delivery_mode": null,
    "program_description": null,
    "admission_requirements": null,
    "gre_requirement": null,
    "gmat_requirement": null,
    "portfolio_requirement": null,
    "interview_requirement": null,
    "experience_requirement": null,
    "accreditation": null,
    "official_program_url": null,
    "source_ids": [
      "AUST-SRC-003",
      "AUST-SRC-002"
    ],
    "notes": "Listed in the graduate application instructions; no distinct public program page was recovered in this pass.",
    "tuition_academic_year": "2025-2026",
    "tuition_currency": "USD",
    "tuition_billing_basis": "PER_CREDIT",
    "tuition_amount": 320,
    "tuition_category": "Graduate Programs",
    "tuition_notes": "Official graduate tuition page lists MBA/MS graduate clear tuition at USD 320 per credit hour.",
    "tuition_source_ids": [
      "AUST-SRC-007"
    ],
    "tuition_scope_level": "PROGRAM",
    "concentrations_or_tracks": []
  },
  {
    "id": "aust-fe-master-computer-communications-engineering",
    "faculty": "Faculty of Engineering",
    "department": null,
    "major_category": "Engineering",
    "major": "Computer & Communications Engineering",
    "degree_type": "MASTER",
    "official_degree_name": "Master of Science in Computer & Communications Engineering",
    "thesis_or_non_thesis": null,
    "credits": 39,
    "duration_value": null,
    "duration_unit": null,
    "language": null,
    "delivery_mode": null,
    "program_description": null,
    "admission_requirements": null,
    "gre_requirement": null,
    "gmat_requirement": null,
    "portfolio_requirement": null,
    "interview_requirement": null,
    "experience_requirement": null,
    "accreditation": null,
    "official_program_url": null,
    "source_ids": [
      "AUST-SRC-003",
      "AUST-SRC-002"
    ],
    "notes": "Listed in the graduate application instructions; no distinct public program page was recovered in this pass.",
    "tuition_academic_year": "2025-2026",
    "tuition_currency": "USD",
    "tuition_billing_basis": "PER_CREDIT",
    "tuition_amount": 320,
    "tuition_category": "Graduate Programs",
    "tuition_notes": "Official graduate tuition page lists MBA/MS graduate clear tuition at USD 320 per credit hour.",
    "tuition_source_ids": [
      "AUST-SRC-007"
    ],
    "tuition_scope_level": "PROGRAM",
    "concentrations_or_tracks": []
  },
  {
    "id": "aust-fe-master-mechatronics-engineering",
    "faculty": "Faculty of Engineering",
    "department": null,
    "major_category": "Engineering",
    "major": "Mechatronics Engineering",
    "degree_type": "MASTER",
    "official_degree_name": "Master of Science in Mechatronics Engineering",
    "thesis_or_non_thesis": null,
    "credits": 39,
    "duration_value": null,
    "duration_unit": null,
    "language": null,
    "delivery_mode": null,
    "program_description": null,
    "admission_requirements": null,
    "gre_requirement": null,
    "gmat_requirement": null,
    "portfolio_requirement": null,
    "interview_requirement": null,
    "experience_requirement": null,
    "accreditation": null,
    "official_program_url": "https://www.aust.edu.lb/facultydetails/faculty-of-engineering/graduate-programs/ms-in-mechatronics-engineering-39-crhr/2658",
    "source_ids": [
      "AUST-SRC-003",
      "AUST-SRC-002",
      "AUST-SRC-018",
      "AUST-SRC-019"
    ],
    "notes": "Listed in the graduate application instructions and confirmed by the Mechatronics Engineering program page.",
    "tuition_academic_year": "2025-2026",
    "tuition_currency": "USD",
    "tuition_billing_basis": "PER_CREDIT",
    "tuition_amount": 320,
    "tuition_category": "Graduate Programs",
    "tuition_notes": "Official graduate tuition page lists MBA/MS graduate clear tuition at USD 320 per credit hour.",
    "tuition_source_ids": [
      "AUST-SRC-007"
    ],
    "tuition_scope_level": "PROGRAM",
    "concentrations_or_tracks": []
  },
  {
    "id": "aust-fe-master-biomedical-engineering",
    "faculty": "Faculty of Engineering",
    "department": null,
    "major_category": "Engineering",
    "major": "Biomedical Engineering",
    "degree_type": "MASTER",
    "official_degree_name": "Master of Science in Biomedical Engineering",
    "thesis_or_non_thesis": null,
    "credits": 39,
    "duration_value": null,
    "duration_unit": null,
    "language": null,
    "delivery_mode": null,
    "program_description": null,
    "admission_requirements": null,
    "gre_requirement": null,
    "gmat_requirement": null,
    "portfolio_requirement": null,
    "interview_requirement": null,
    "experience_requirement": null,
    "accreditation": null,
    "official_program_url": null,
    "source_ids": [
      "AUST-SRC-003",
      "AUST-SRC-002"
    ],
    "notes": "Listed in the graduate application instructions; no distinct public program page was recovered in this pass.",
    "tuition_academic_year": "2025-2026",
    "tuition_currency": "USD",
    "tuition_billing_basis": "PER_CREDIT",
    "tuition_amount": 320,
    "tuition_category": "Graduate Programs",
    "tuition_notes": "Official graduate tuition page lists MBA/MS graduate clear tuition at USD 320 per credit hour.",
    "tuition_source_ids": [
      "AUST-SRC-007"
    ],
    "tuition_scope_level": "PROGRAM",
    "concentrations_or_tracks": []
  },
  {
    "id": "aust-fhs-master-biotechnology",
    "faculty": "Faculty of Health Sciences",
    "department": null,
    "major_category": "Health Sciences",
    "major": "Biotechnology",
    "degree_type": "MASTER",
    "official_degree_name": "Master of Science in Biotechnology",
    "thesis_or_non_thesis": null,
    "credits": 36,
    "duration_value": null,
    "duration_unit": null,
    "language": null,
    "delivery_mode": null,
    "program_description": null,
    "admission_requirements": null,
    "gre_requirement": null,
    "gmat_requirement": null,
    "portfolio_requirement": null,
    "interview_requirement": null,
    "experience_requirement": null,
    "accreditation": null,
    "official_program_url": "https://www.aust.edu.lb/facultydetails/faculty-of-health-sciences/graduate-programs/ms-biotechnology-in-forensic-science-%2836-crhr%29/major-description/407",
    "source_ids": [
      "AUST-SRC-003",
      "AUST-SRC-002",
      "AUST-SRC-020"
    ],
    "notes": "Official materials indicate Biotechnology tracks in DNA Technologies and Forensic Science; the public page recovered in this pass highlights the Forensic Science track.",
    "tuition_academic_year": "2025-2026",
    "tuition_currency": "USD",
    "tuition_billing_basis": "PER_CREDIT",
    "tuition_amount": 320,
    "tuition_category": "Graduate Programs",
    "tuition_notes": "Official graduate tuition page lists MBA/MS graduate clear tuition at USD 320 per credit hour.",
    "tuition_source_ids": [
      "AUST-SRC-007"
    ],
    "tuition_scope_level": "PROGRAM",
    "concentrations_or_tracks": [
      "DNA Technologies",
      "Forensic Science"
    ]
  },
  {
    "id": "aust-fhs-master-bio-analytical-toxicology",
    "faculty": "Faculty of Health Sciences",
    "department": null,
    "major_category": "Health Sciences",
    "major": "Bio-analytical Toxicology",
    "degree_type": "MASTER",
    "official_degree_name": "Master of Science in Bio-analytical Toxicology",
    "thesis_or_non_thesis": null,
    "credits": 36,
    "duration_value": null,
    "duration_unit": null,
    "language": null,
    "delivery_mode": null,
    "program_description": null,
    "admission_requirements": null,
    "gre_requirement": null,
    "gmat_requirement": null,
    "portfolio_requirement": null,
    "interview_requirement": null,
    "experience_requirement": null,
    "accreditation": null,
    "official_program_url": null,
    "source_ids": [
      "AUST-SRC-003",
      "AUST-SRC-002",
      "AUST-SRC-020"
    ],
    "notes": "Listed in the graduate application instructions and confirmed by the Faculty of Health Sciences graduate page.",
    "tuition_academic_year": "2025-2026",
    "tuition_currency": "USD",
    "tuition_billing_basis": "PER_CREDIT",
    "tuition_amount": 320,
    "tuition_category": "Graduate Programs",
    "tuition_notes": "Official graduate tuition page lists MBA/MS graduate clear tuition at USD 320 per credit hour.",
    "tuition_source_ids": [
      "AUST-SRC-007"
    ],
    "tuition_scope_level": "PROGRAM",
    "concentrations_or_tracks": []
  },
  {
    "id": "aust-fhs-master-optics-optometry",
    "faculty": "Faculty of Health Sciences",
    "department": null,
    "major_category": "Health Sciences",
    "major": "Optics and Optometry",
    "degree_type": "MASTER",
    "official_degree_name": "Master of Science in Optics and Optometry",
    "thesis_or_non_thesis": null,
    "credits": 36,
    "duration_value": null,
    "duration_unit": null,
    "language": null,
    "delivery_mode": null,
    "program_description": null,
    "admission_requirements": null,
    "gre_requirement": null,
    "gmat_requirement": null,
    "portfolio_requirement": null,
    "interview_requirement": null,
    "experience_requirement": null,
    "accreditation": null,
    "official_program_url": null,
    "source_ids": [
      "AUST-SRC-003",
      "AUST-SRC-002",
      "AUST-SRC-020"
    ],
    "notes": "Listed in the graduate application instructions and confirmed by the Faculty of Health Sciences graduate page.",
    "tuition_academic_year": "2025-2026",
    "tuition_currency": "USD",
    "tuition_billing_basis": "PER_CREDIT",
    "tuition_amount": 320,
    "tuition_category": "Graduate Programs",
    "tuition_notes": "Official graduate tuition page lists MBA/MS graduate clear tuition at USD 320 per credit hour.",
    "tuition_source_ids": [
      "AUST-SRC-007"
    ],
    "tuition_scope_level": "PROGRAM",
    "concentrations_or_tracks": []
  }
]$AUST_PROGRAMS$) AS x(id TEXT, faculty TEXT, department TEXT, major_category TEXT, major TEXT, degree_type TEXT, official_degree_name TEXT, thesis_or_non_thesis TEXT, credits INTEGER, duration_value NUMERIC(10, 2), duration_unit TEXT, language TEXT, delivery_mode TEXT, program_description TEXT, admission_requirements TEXT, gre_requirement TEXT, gmat_requirement TEXT, portfolio_requirement TEXT, interview_requirement TEXT, experience_requirement TEXT, accreditation TEXT, official_program_url TEXT, source_ids JSONB, notes TEXT, tuition_academic_year TEXT, tuition_currency TEXT, tuition_billing_basis TEXT, tuition_amount NUMERIC(12, 2), tuition_category TEXT, tuition_notes TEXT, tuition_source_ids JSONB, tuition_scope_level TEXT, concentrations_or_tracks JSONB);

    INSERT INTO graduate_program (university_id, faculty_id, department_id, degree_type_id, program_key, major_category, major, official_degree_name, thesis_or_non_thesis, credits, duration_value, duration_unit, primary_language_id, delivery_mode, program_description, official_program_url, source_id, notes)
    SELECT v_university_id, fac.id, NULL, dt.id, seed.id, seed.major_category, seed.major, seed.official_degree_name, seed.thesis_or_non_thesis, seed.credits, seed.duration_value, seed.duration_unit, NULL, seed.delivery_mode, seed.program_description, seed.official_program_url, (SELECT s.id FROM jsonb_array_elements_text(seed.source_ids) WITH ORDINALITY AS src(source_seed_id, ord) JOIN aust_source_seed ss ON ss.source_id = src.source_seed_id JOIN source s ON s.university_id = v_university_id AND s.url = ss.url ORDER BY src.ord LIMIT 1), concat_ws(' ', NULLIF(seed.notes, ''), CASE WHEN seed.id = 'aust-fbe-master-business-administration' THEN 'MBA brochure lists 36 credits, but the current admissions/program page uses 39 credits.' END) FROM aust_program_seed seed JOIN university_faculty fac ON fac.university_id = v_university_id AND fac.name = seed.faculty LEFT JOIN degree_type dt ON dt.code = seed.degree_type ON CONFLICT (university_id, program_key) DO UPDATE SET faculty_id = EXCLUDED.faculty_id, department_id = EXCLUDED.department_id, degree_type_id = EXCLUDED.degree_type_id, major_category = EXCLUDED.major_category, major = EXCLUDED.major, official_degree_name = EXCLUDED.official_degree_name, thesis_or_non_thesis = EXCLUDED.thesis_or_non_thesis, credits = EXCLUDED.credits, duration_value = EXCLUDED.duration_value, duration_unit = EXCLUDED.duration_unit, primary_language_id = EXCLUDED.primary_language_id, delivery_mode = EXCLUDED.delivery_mode, program_description = EXCLUDED.program_description, official_program_url = EXCLUDED.official_program_url, source_id = EXCLUDED.source_id, notes = EXCLUDED.notes, updated_at = NOW();

    INSERT INTO graduate_program_track (university_id, program_id, track_type, track_name, track_order, is_primary, description, source_id, notes)
    SELECT v_university_id, gp.id, 'CONCENTRATION', track_vals.track_name, track_vals.ord, track_vals.ord = 1, NULL, s.id, 'Imported from concentrations_or_tracks.' FROM aust_program_seed seed JOIN graduate_program gp ON gp.university_id = v_university_id AND gp.program_key = seed.id JOIN LATERAL jsonb_array_elements_text(COALESCE(seed.concentrations_or_tracks, '[]'::jsonb)) WITH ORDINALITY AS track_vals(track_name, ord) ON TRUE JOIN aust_source_seed ss ON ss.source_id = CASE WHEN seed.id = 'aust-fhs-master-biotechnology' THEN 'AUST-SRC-020' ELSE 'AUST-SRC-003' END JOIN source s ON s.university_id = v_university_id AND s.url = ss.url ON CONFLICT (program_id, track_type, track_name) DO UPDATE SET track_order = EXCLUDED.track_order, is_primary = EXCLUDED.is_primary, source_id = EXCLUDED.source_id, notes = EXCLUDED.notes, updated_at = NOW();

    INSERT INTO graduate_tuition_rate (university_id, faculty_id, department_id, program_id, scope_level, record_key, academic_year, currency, billing_basis, amount, category, notes, source_id)
    SELECT v_university_id, gp.faculty_id, gp.department_id, gp.id, 'PROGRAM', seed.id || ':tuition:' || seed.tuition_academic_year, seed.tuition_academic_year, seed.tuition_currency, seed.tuition_billing_basis, seed.tuition_amount, seed.tuition_category, seed.tuition_notes, tuition_src.id FROM aust_program_seed seed JOIN graduate_program gp ON gp.university_id = v_university_id AND gp.program_key = seed.id LEFT JOIN LATERAL ( SELECT s.id FROM jsonb_array_elements_text(seed.tuition_source_ids) WITH ORDINALITY AS ts(source_seed_id, ord) JOIN aust_source_seed ss ON ss.source_id = ts.source_seed_id JOIN source s ON s.university_id = v_university_id AND s.url = ss.url ORDER BY ts.ord LIMIT 1 ) tuition_src ON TRUE WHERE seed.tuition_academic_year IS NOT NULL ON CONFLICT (university_id, record_key) DO UPDATE SET faculty_id = EXCLUDED.faculty_id, department_id = EXCLUDED.department_id, program_id = EXCLUDED.program_id, scope_level = EXCLUDED.scope_level, academic_year = EXCLUDED.academic_year, currency = EXCLUDED.currency, billing_basis = EXCLUDED.billing_basis, amount = EXCLUDED.amount, category = EXCLUDED.category, notes = EXCLUDED.notes, source_id = EXCLUDED.source_id, updated_at = NOW();

    INSERT INTO graduate_fee_item (university_id, faculty_id, department_id, program_id, scope_level, record_key, academic_year, fee_name, billing_basis, currency, amount, category, notes, source_id) SELECT v_university_id, NULL, NULL, NULL, 'UNIVERSITY', f.record_key, f.academic_year, f.fee_name, f.billing_basis, f.currency, f.amount, f.category, f.notes, s.id FROM jsonb_to_recordset($AUST_FEES$[
  {
    "record_key": "aust-fee-application-fee",
    "fee_name": "Application Fee",
    "amount": null,
    "currency": "USD",
    "billing_basis": "FLAT_FEE",
    "category": "Graduate Fees",
    "notes": "The graduate tuition page references an application fee, but the accessible source set did not expose a stable amount.",
    "source_id": "AUST-SRC-007",
    "academic_year": "2025-2026"
  },
  {
    "record_key": "aust-fee-registration-fee",
    "fee_name": "Registration Fee",
    "amount": null,
    "currency": "USD",
    "billing_basis": "FLAT_FEE",
    "category": "Graduate Fees",
    "notes": "Referenced on the graduate tuition page, but no stable amount was recovered in this pass.",
    "source_id": "AUST-SRC-007",
    "academic_year": "2025-2026"
  },
  {
    "record_key": "aust-fee-accident-insurance-fee",
    "fee_name": "Accident Insurance Fee",
    "amount": null,
    "currency": "USD",
    "billing_basis": "FLAT_FEE",
    "category": "Graduate Fees",
    "notes": "Referenced on the graduate tuition page, but no stable amount was recovered in this pass.",
    "source_id": "AUST-SRC-007",
    "academic_year": "2025-2026"
  },
  {
    "record_key": "aust-fee-technology-fee",
    "fee_name": "Technology Fee",
    "amount": null,
    "currency": "USD",
    "billing_basis": "FLAT_FEE",
    "category": "Graduate Fees",
    "notes": "Referenced on the graduate tuition page, but no stable amount was recovered in this pass.",
    "source_id": "AUST-SRC-007",
    "academic_year": "2025-2026"
  },
  {
    "record_key": "aust-fee-social-security-fee",
    "fee_name": "Social Security Fee",
    "amount": null,
    "currency": "USD",
    "billing_basis": "FLAT_FEE",
    "category": "Graduate Fees",
    "notes": "Referenced on the graduate tuition page, but no stable amount was recovered in this pass.",
    "source_id": "AUST-SRC-007",
    "academic_year": "2025-2026"
  },
  {
    "record_key": "aust-fee-lab-fee",
    "fee_name": "Laboratory Fee",
    "amount": null,
    "currency": "USD",
    "billing_basis": "FLAT_FEE",
    "category": "Graduate Fees",
    "notes": "Referenced on the graduate tuition page, but no stable amount was recovered in this pass.",
    "source_id": "AUST-SRC-007",
    "academic_year": "2025-2026"
  }
]$AUST_FEES$) AS f(record_key TEXT, fee_name TEXT, amount NUMERIC(12, 2), currency TEXT, billing_basis TEXT, category TEXT, notes TEXT, source_id TEXT, academic_year TEXT) JOIN aust_source_seed ss ON ss.source_id = f.source_id JOIN source s ON s.university_id = v_university_id AND s.url = ss.url ON CONFLICT (university_id, record_key) DO UPDATE SET academic_year = EXCLUDED.academic_year, fee_name = EXCLUDED.fee_name, billing_basis = EXCLUDED.billing_basis, currency = EXCLUDED.currency, amount = EXCLUDED.amount, category = EXCLUDED.category, notes = EXCLUDED.notes, source_id = EXCLUDED.source_id, updated_at = NOW();

    INSERT INTO graduate_admission_requirement (university_id, faculty_id, department_id, program_id, scope_level, record_key, requirement_type, requirement_text, comparison_operator, threshold_value, threshold_unit, is_required, notes, source_id) SELECT v_university_id, NULL, NULL, NULL, 'UNIVERSITY', a.record_key, a.requirement_type, a.requirement_text, NULL, NULL, NULL, TRUE, a.notes, s.id FROM jsonb_to_recordset($AUST_ADM_REQS$[
  {
    "record_key": "aust-adm-general",
    "requirement_type": "GENERAL",
    "requirement_text": "Graduate applicants apply through the official online graduate application, submit the required supporting documents, and are reviewed under the Graduate Committee and graduate admission requirements.",
    "notes": "Centralized from the official graduate application instructions and admission requirements pages.",
    "source_id": "AUST-SRC-003"
  },
  {
    "record_key": "aust-adm-english",
    "requirement_type": "ENGLISH",
    "requirement_text": "English proficiency is required; official sources conflict between TOEFL 575 and TOEFL 600, so the discrepancy is preserved without choosing a threshold.",
    "notes": "Current admissions/program pages take precedence for inventory, but the threshold discrepancy is intentionally not resolved.",
    "source_id": "AUST-SRC-005"
  }
]$AUST_ADM_REQS$) AS a(record_key TEXT, requirement_type TEXT, requirement_text TEXT, notes TEXT, source_id TEXT) JOIN aust_source_seed ss ON ss.source_id = a.source_id JOIN source s ON s.university_id = v_university_id AND s.url = ss.url ON CONFLICT (university_id, record_key) DO UPDATE SET requirement_type = EXCLUDED.requirement_type, requirement_text = EXCLUDED.requirement_text, is_required = EXCLUDED.is_required, notes = EXCLUDED.notes, source_id = EXCLUDED.source_id, updated_at = NOW();

    INSERT INTO graduate_required_document (university_id, faculty_id, department_id, program_id, scope_level, record_key, document_type, document_name, is_optional, sort_order, notes, source_id) SELECT v_university_id, NULL, NULL, NULL, 'UNIVERSITY', d.record_key, d.document_type, d.document_name, FALSE, d.sort_order, 'Listed in the official graduate application instructions.', s.id FROM jsonb_to_recordset($AUST_DOCS$[
  {
    "record_key": "aust-doc-cv",
    "document_type": "CV",
    "document_name": "Curriculum vitae",
    "sort_order": 1,
    "notes": "Listed in the official graduate application instructions.",
    "source_id": "AUST-SRC-003"
  },
  {
    "record_key": "aust-doc-id-passport",
    "document_type": "IDENTITY",
    "document_name": "Identity or passport copy",
    "sort_order": 2,
    "notes": "Listed in the official graduate application instructions.",
    "source_id": "AUST-SRC-003"
  },
  {
    "record_key": "aust-doc-photos",
    "document_type": "PHOTO",
    "document_name": "Passport-sized photos",
    "sort_order": 3,
    "notes": "Listed in the official graduate application instructions.",
    "source_id": "AUST-SRC-003"
  },
  {
    "record_key": "aust-doc-application-fee",
    "document_type": "FEE",
    "document_name": "Application fee",
    "sort_order": 4,
    "notes": "Listed in the official graduate application instructions.",
    "source_id": "AUST-SRC-003"
  },
  {
    "record_key": "aust-doc-civil-status",
    "document_type": "CIVIL_STATUS",
    "document_name": "Lebanese family civil-status record where applicable",
    "sort_order": 5,
    "notes": "Listed in the official graduate application instructions.",
    "source_id": "AUST-SRC-003"
  },
  {
    "record_key": "aust-doc-certificates",
    "document_type": "ACADEMIC",
    "document_name": "Certified educational and professional certificates",
    "sort_order": 6,
    "notes": "Listed in the official graduate application instructions.",
    "source_id": "AUST-SRC-003"
  },
  {
    "record_key": "aust-doc-transcript",
    "document_type": "TRANSCRIPT",
    "document_name": "Official transcript",
    "sort_order": 7,
    "notes": "Listed in the official graduate application instructions.",
    "source_id": "AUST-SRC-003"
  },
  {
    "record_key": "aust-doc-recommendations",
    "document_type": "RECOMMENDATION",
    "document_name": "Recommendations",
    "sort_order": 8,
    "notes": "Listed in the official graduate application instructions.",
    "source_id": "AUST-SRC-003"
  }
]$AUST_DOCS$) AS d(record_key TEXT, document_type TEXT, document_name TEXT, sort_order INTEGER, source_id TEXT) JOIN aust_source_seed ss ON ss.source_id = d.source_id JOIN source s ON s.university_id = v_university_id AND s.url = ss.url ON CONFLICT (university_id, record_key) DO UPDATE SET document_type = EXCLUDED.document_type, document_name = EXCLUDED.document_name, is_optional = EXCLUDED.is_optional, sort_order = EXCLUDED.sort_order, notes = EXCLUDED.notes, source_id = EXCLUDED.source_id, updated_at = NOW();

    INSERT INTO graduate_admission_deadline (university_id, faculty_id, department_id, program_id, scope_level, record_key, academic_year, deadline_type, term, deadline_date, note, source_id) SELECT v_university_id, NULL, NULL, NULL, 'UNIVERSITY', d.record_key, d.academic_year, d.deadline_type, d.term, d.deadline_date, d.note, s.id FROM jsonb_to_recordset($AUST_DEADLINES$[
  {
    "record_key": "aust-deadline-summer-i-2025-2026",
    "academic_year": "2025-2026",
    "deadline_type": "OTHER",
    "term": "Summer I Semester 2025-2026",
    "deadline_date": null,
    "note": "Official academic calendar page.",
    "source_id": "AUST-SRC-009"
  },
  {
    "record_key": "aust-deadline-summer-ii-2025-2026",
    "academic_year": "2025-2026",
    "deadline_type": "OTHER",
    "term": "Summer II Semester 2025-2026",
    "deadline_date": null,
    "note": "Official academic calendar page.",
    "source_id": "AUST-SRC-010"
  },
  {
    "record_key": "aust-deadline-fall-2026-2027",
    "academic_year": "2026-2027",
    "deadline_type": "OTHER",
    "term": "Fall Semester 2026-2027",
    "deadline_date": null,
    "note": "Official academic calendar page.",
    "source_id": "AUST-SRC-011"
  }
]$AUST_DEADLINES$) AS d(record_key TEXT, academic_year TEXT, deadline_type TEXT, term TEXT, deadline_date DATE, note TEXT, source_id TEXT) JOIN aust_source_seed ss ON ss.source_id = d.source_id JOIN source s ON s.university_id = v_university_id AND s.url = ss.url ON CONFLICT (university_id, record_key) DO UPDATE SET academic_year = EXCLUDED.academic_year, deadline_type = EXCLUDED.deadline_type, term = EXCLUDED.term, deadline_date = EXCLUDED.deadline_date, note = EXCLUDED.note, source_id = EXCLUDED.source_id, updated_at = NOW();

    INSERT INTO graduate_scholarship (university_id, faculty_id, department_id, program_id, scope_level, record_key, academic_year, name, description, coverage, amount, currency, notes, source_id) SELECT v_university_id, NULL, NULL, NULL, 'UNIVERSITY', sdata.record_key, sdata.academic_year, sdata.name, sdata.description, sdata.coverage, sdata.amount, sdata.currency, sdata.notes, src.id FROM jsonb_to_recordset($AUST_SCHOLARSHIPS$[
  {
    "record_key": "aust-scholarship-partial-scholarships",
    "academic_year": "2025-2026",
    "name": "Partial Scholarships",
    "description": "Official financial aid page confirms partial scholarships for candidates with financial need who meet the relevant academic conditions.",
    "coverage": "Need-based partial scholarships",
    "amount": null,
    "currency": "USD",
    "notes": "Centralized university-level scholarship summary from the official financial aid page.",
    "source_id": "AUST-SRC-008"
  }
]$AUST_SCHOLARSHIPS$) AS sdata(record_key TEXT, academic_year TEXT, name TEXT, description TEXT, coverage TEXT, amount NUMERIC(12, 2), currency TEXT, notes TEXT, source_id TEXT) JOIN aust_source_seed ss ON ss.source_id = sdata.source_id JOIN source src ON src.university_id = v_university_id AND src.url = ss.url ON CONFLICT (university_id, record_key) DO UPDATE SET academic_year = EXCLUDED.academic_year, name = EXCLUDED.name, description = EXCLUDED.description, coverage = EXCLUDED.coverage, amount = EXCLUDED.amount, currency = EXCLUDED.currency, notes = EXCLUDED.notes, source_id = EXCLUDED.source_id, updated_at = NOW();

    INSERT INTO graduate_financial_aid (university_id, faculty_id, department_id, program_id, scope_level, record_key, academic_year, name, description, amount, currency, notes, source_id) SELECT v_university_id, NULL, NULL, NULL, 'UNIVERSITY', ad.record_key, ad.academic_year, ad.name, ad.description, ad.amount, ad.currency, ad.notes, src.id FROM jsonb_to_recordset($AUST_FIN_AID$[
  {
    "record_key": "aust-fin-aid-need-based-partial",
    "academic_year": "2025-2026",
    "name": "Need-Based Partial Financial Aid",
    "description": "Need-based partial aid is available and tied to completed credits, full-time registration, and GPA conditions.",
    "amount": null,
    "currency": "USD",
    "notes": "Centralized university-level financial aid summary from the official financial aid page.",
    "source_id": "AUST-SRC-008"
  }
]$AUST_FIN_AID$) AS ad(record_key TEXT, academic_year TEXT, name TEXT, description TEXT, amount NUMERIC(12, 2), currency TEXT, notes TEXT, source_id TEXT) JOIN aust_source_seed ss ON ss.source_id = ad.source_id JOIN source src ON src.university_id = v_university_id AND src.url = ss.url ON CONFLICT (university_id, record_key) DO UPDATE SET academic_year = EXCLUDED.academic_year, name = EXCLUDED.name, description = EXCLUDED.description, amount = EXCLUDED.amount, currency = EXCLUDED.currency, notes = EXCLUDED.notes, source_id = EXCLUDED.source_id, updated_at = NOW();

    INSERT INTO graduate_payment_plan (university_id, faculty_id, department_id, program_id, scope_level, record_key, academic_year, name, description, installments_count, down_payment_amount, down_payment_currency, interval_label, notes, source_id) SELECT v_university_id, NULL, NULL, NULL, 'UNIVERSITY', pp.record_key, pp.academic_year, pp.name, pp.description, pp.installments_count, pp.down_payment_amount, pp.down_payment_currency, pp.interval_label, pp.notes, src.id FROM jsonb_to_recordset($AUST_PAYMENT_PLANS$[
  {
    "record_key": "aust-payment-plan-deferred",
    "academic_year": "2025-2026",
    "name": "Deferred Payment by Signed Contract",
    "description": "Deferred payment is available by signed contract.",
    "installments_count": null,
    "down_payment_amount": null,
    "down_payment_currency": null,
    "interval_label": null,
    "notes": "Students pay 25% of tuition plus other fees at the beginning of each semester; installments are payable at the bank.",
    "source_id": "AUST-SRC-007"
  },
  {
    "record_key": "aust-payment-plan-installment",
    "academic_year": "2025-2026",
    "name": "Installment Payment",
    "description": "Installment payment is available and payable at the bank.",
    "installments_count": null,
    "down_payment_amount": null,
    "down_payment_currency": null,
    "interval_label": null,
    "notes": "Students pay 25% of tuition plus other fees at the beginning of each semester; installment payments are payable at the bank.",
    "source_id": "AUST-SRC-007"
  }
]$AUST_PAYMENT_PLANS$) AS pp(record_key TEXT, academic_year TEXT, name TEXT, description TEXT, installments_count INTEGER, down_payment_amount NUMERIC(12, 2), down_payment_currency TEXT, interval_label TEXT, notes TEXT, source_id TEXT) JOIN aust_source_seed ss ON ss.source_id = pp.source_id JOIN source src ON src.university_id = v_university_id AND src.url = ss.url ON CONFLICT (university_id, record_key) DO UPDATE SET academic_year = EXCLUDED.academic_year, name = EXCLUDED.name, description = EXCLUDED.description, installments_count = EXCLUDED.installments_count, down_payment_amount = EXCLUDED.down_payment_amount, down_payment_currency = EXCLUDED.down_payment_currency, interval_label = EXCLUDED.interval_label, notes = EXCLUDED.notes, source_id = EXCLUDED.source_id, updated_at = NOW();

    INSERT INTO graduate_program_source (university_id, program_id, source_id, source_role, source_order, evidence_text, notes) SELECT v_university_id, gp.id, s.id, CASE WHEN src.ord = 1 THEN 'PRIMARY' ELSE 'SECONDARY' END, src.ord, ss.title, NULL FROM aust_program_seed seed JOIN graduate_program gp ON gp.university_id = v_university_id AND gp.program_key = seed.id JOIN LATERAL jsonb_array_elements_text(seed.source_ids) WITH ORDINALITY AS src(source_seed_id, ord) ON TRUE JOIN aust_source_seed ss ON ss.source_id = src.source_seed_id JOIN source s ON s.university_id = v_university_id AND s.url = ss.url ON CONFLICT (program_id, source_id, source_role) DO UPDATE SET source_order = EXCLUDED.source_order, evidence_text = EXCLUDED.evidence_text, notes = EXCLUDED.notes, updated_at = NOW();

    INSERT INTO graduate_program_source (university_id, program_id, source_id, source_role, source_order, evidence_text, notes) SELECT v_university_id, gp.id, s.id, 'TUITION', 1, 'Tuition source', NULL FROM aust_program_seed seed JOIN graduate_program gp ON gp.university_id = v_university_id AND gp.program_key = seed.id JOIN aust_source_seed ss ON ss.source_id = (seed.tuition_source_ids->>0) JOIN source s ON s.university_id = v_university_id AND s.url = ss.url ON CONFLICT (program_id, source_id, source_role) DO UPDATE SET source_order = EXCLUDED.source_order, evidence_text = EXCLUDED.evidence_text, notes = EXCLUDED.notes, updated_at = NOW();

END $$;
