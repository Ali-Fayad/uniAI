-- ULS graduate data seed migration.
-- Idempotent import for the canonical ULS graduate dataset.

DO $$
DECLARE
    v_university_id BIGINT;
    v_lang_en_id BIGINT;
    v_lang_fr_id BIGINT;
    v_lang_ar_id BIGINT;
    v_lang_it_id BIGINT;
    v_lang_multi_id BIGINT;
BEGIN

    INSERT INTO university (name, name_ar, acronym, country, city, latitude, longitude, campus_name, campus_type)
    SELECT 'Université La Sagesse', NULL, 'ULS', 'Lebanon', NULL, NULL, NULL, NULL, NULL
    WHERE NOT EXISTS (SELECT 1 FROM university WHERE name = 'Université La Sagesse');

    SELECT id INTO v_university_id FROM university WHERE name = 'Université La Sagesse' ORDER BY id LIMIT 1;

    -- ULS reuses hub/catalog URLs across multiple graduate programs.
    ALTER TABLE graduate_program DROP CONSTRAINT IF EXISTS uq_graduate_program_university_url;

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
        ('Italian', 'it', 'Italiano'),
        ('Multilingual', 'multi', 'Multilingual')
    ON CONFLICT (code) DO UPDATE SET name = EXCLUDED.name, native_name = EXCLUDED.native_name;

    SELECT id INTO v_lang_en_id FROM language WHERE code = 'en' ORDER BY id LIMIT 1;
    SELECT id INTO v_lang_fr_id FROM language WHERE code = 'fr' ORDER BY id LIMIT 1;
    SELECT id INTO v_lang_ar_id FROM language WHERE code = 'ar' ORDER BY id LIMIT 1;
    SELECT id INTO v_lang_it_id FROM language WHERE code = 'it' ORDER BY id LIMIT 1;
    SELECT id INTO v_lang_multi_id FROM language WHERE code = 'multi' ORDER BY id LIMIT 1;

    CREATE TEMP TABLE uls_source_seed (
        source_id TEXT PRIMARY KEY,
        title TEXT NOT NULL,
        url TEXT NOT NULL,
        source_type TEXT NOT NULL,
        accessed_at DATE,
        notes TEXT
    ) ON COMMIT DROP;

    INSERT INTO uls_source_seed (source_id, title, url, source_type, accessed_at, notes)
    SELECT source_id, title, url, source_type, accessed_at, notes FROM jsonb_to_recordset($ULS_SOURCES$
[
  {
    "source_id": "ULS-S001",
    "title": "Home | Université La Sagesse",
    "url": "https://www.uls.edu.lb/",
    "source_type": "WEB",
    "accessed_at": "2026-07-05",
    "notes": "Official homepage; used for institution identity, campuses, navigation, and public contacts."
  },
  {
    "source_id": "ULS-S002",
    "title": "Academics | Université La Sagesse",
    "url": "https://www.uls.edu.lb/academics/",
    "source_type": "WEB",
    "accessed_at": "2026-07-05",
    "notes": "Primary official consolidated list of undergraduate, graduate, doctorate, and freshman programs; used for master program inventory."
  },
  {
    "source_id": "ULS-S003",
    "title": "Faculty of Law | Université La Sagesse",
    "url": "https://www.uls.edu.lb/academics/law/",
    "source_type": "WEB",
    "accessed_at": "2026-07-05",
    "notes": "Faculty page confirming master's availability and official academic-plan download link."
  },
  {
    "source_id": "ULS-S004",
    "title": "Faculty of Economics and Business Administration | Université La Sagesse",
    "url": "https://www.uls.edu.lb/academics/economics-business/",
    "source_type": "WEB",
    "accessed_at": "2026-07-05",
    "notes": "Faculty page confirming bachelor's/master's degrees, doctoral degree availability, and MSc/MBA/MIAGE graduate programs."
  },
  {
    "source_id": "ULS-S005",
    "title": "Doctorate in Business Administration (DBA) | Université La Sagesse",
    "url": "https://www.uls.edu.lb/academics/economics-business/dba/",
    "source_type": "WEB",
    "accessed_at": "2026-07-05",
    "notes": "Official DBA page with partnership, admissions process, deadline, start date, and contact."
  },
  {
    "source_id": "ULS-S006",
    "title": "ULS x ISC Paris - DBA 2026 brochure",
    "url": "https://www.uls.edu.lb/download/1006706/?tmstv=1783237767",
    "source_type": "PDF",
    "accessed_at": "2026-07-05",
    "notes": "Official ULS-hosted DBA brochure / academic regulations PDF."
  },
  {
    "source_id": "ULS-S007",
    "title": "Faculty of Economics and Business Administration - International | Université La Sagesse",
    "url": "https://www.uls.edu.lb/academics/economics-business/faculty-of-economics-and-business-administration-international/",
    "source_type": "WEB",
    "accessed_at": "2026-07-05",
    "notes": "Official international partnerships page; confirms MIAGE double master with Université de Bordeaux and HR curriculum recognition."
  },
  {
    "source_id": "ULS-S008",
    "title": "Faculty of Political Science and International Relations | Université La Sagesse",
    "url": "https://www.uls.edu.lb/academics/political-science/",
    "source_type": "WEB",
    "accessed_at": "2026-07-05",
    "notes": "Faculty page confirming master's specializations in NGO Management and Diplomacy and Strategic Negotiations."
  },
  {
    "source_id": "ULS-S009",
    "title": "Faculty of Public Health | Université La Sagesse",
    "url": "https://www.uls.edu.lb/academics/public-health/",
    "source_type": "WEB",
    "accessed_at": "2026-07-05",
    "notes": "Faculty page confirming Master in Hospital Management in partnership with Université de Lille."
  },
  {
    "source_id": "ULS-S010",
    "title": "Faculty of Tourism and Hotel Management | Université La Sagesse",
    "url": "https://www.uls.edu.lb/academics/fthm/",
    "source_type": "WEB",
    "accessed_at": "2026-07-05",
    "notes": "Faculty page confirming MBA / Hospitality Management master."
  },
  {
    "source_id": "ULS-S011",
    "title": "Faculty of Religious and Theological Sciences | Université La Sagesse",
    "url": "https://www.uls.edu.lb/academics/religious-theological-sciences/",
    "source_type": "WEB",
    "accessed_at": "2026-07-05",
    "notes": "Faculty page confirming Master's in Ecclesiastical Sciences concentrations and language/exam requirements."
  },
  {
    "source_id": "ULS-S012",
    "title": "Faculty of Engineering | Université La Sagesse",
    "url": "https://www.uls.edu.lb/academics/engineering/",
    "source_type": "WEB",
    "accessed_at": "2026-07-05",
    "notes": "Faculty page reviewed; no official graduate/master/PhD engineering program evidence found."
  },
  {
    "source_id": "ULS-S013",
    "title": "University Council | Université La Sagesse",
    "url": "https://www.uls.edu.lb/university-council/",
    "source_type": "WEB",
    "accessed_at": "2026-07-05",
    "notes": "Leadership and faculty dean evidence."
  },
  {
    "source_id": "ULS-S014",
    "title": "Admissions | Université La Sagesse",
    "url": "https://www.uls.edu.lb/admissions/",
    "source_type": "WEB",
    "accessed_at": "2026-07-05",
    "notes": "Admissions office overview and contact."
  },
  {
    "source_id": "ULS-S015",
    "title": "How to Apply | Université La Sagesse",
    "url": "https://www.uls.edu.lb/admissions/how-to-apply/",
    "source_type": "WEB",
    "accessed_at": "2026-07-05",
    "notes": "Official 4-step admissions-process summary."
  },
  {
    "source_id": "ULS-S016",
    "title": "When to Apply | Université La Sagesse",
    "url": "https://www.uls.edu.lb/admissions/enrolments/",
    "source_type": "WEB",
    "accessed_at": "2026-07-05",
    "notes": "Official enrolment calendar and entrance-exam date page; links to academic calendar PDF."
  },
  {
    "source_id": "ULS-S017",
    "title": "2025 2026 - Academic Calendar",
    "url": "https://www.uls.edu.lb/download/1004518/?tmstv=1783253905",
    "source_type": "PDF",
    "accessed_at": "2026-07-05",
    "notes": "Official ULS-hosted academic calendar PDF."
  },
  {
    "source_id": "ULS-S018",
    "title": "Tuition Fees | Université La Sagesse",
    "url": "https://www.uls.edu.lb/admissions/tuition-fees/",
    "source_type": "WEB",
    "accessed_at": "2026-07-05",
    "notes": "Official tuition and fees page for 2025-2026, including graduate credit tuition by faculty."
  },
  {
    "source_id": "ULS-S019",
    "title": "Financial Support | Université La Sagesse",
    "url": "https://www.uls.edu.lb/admissions/financial-support/",
    "source_type": "WEB",
    "accessed_at": "2026-07-05",
    "notes": "Official financial aid, scholarship, siblings aid, student work, and protocol support page."
  },
  {
    "source_id": "ULS-S020",
    "title": "Office of the Registrar | Université La Sagesse",
    "url": "https://www.uls.edu.lb/registrar/",
    "source_type": "WEB",
    "accessed_at": "2026-07-05",
    "notes": "Registrar services page; confirms academic catalog, registration, academic calendar, transcripts, degree validation, and graduation services."
  }
]
$ULS_SOURCES$::jsonb) AS x(source_id TEXT, title TEXT, url TEXT, source_type TEXT, accessed_at DATE, notes TEXT);

    INSERT INTO source (university_id, title, url, source_type, accessed_at)
    SELECT v_university_id, title, url, source_type, accessed_at
    FROM uls_source_seed
    ON CONFLICT (university_id, url) DO UPDATE SET
        title = EXCLUDED.title,
        source_type = EXCLUDED.source_type,
        accessed_at = EXCLUDED.accessed_at,
        updated_at = NOW();

    CREATE TEMP TABLE uls_faculty_seed (
        name TEXT PRIMARY KEY,
        short_name TEXT,
        faculty_type TEXT NOT NULL,
        official_url TEXT,
        notes TEXT
    ) ON COMMIT DROP;

    INSERT INTO uls_faculty_seed (name, short_name, faculty_type, official_url, notes)
    SELECT name, short_name, COALESCE(faculty_type, 'FACULTY'), official_url, notes FROM jsonb_to_recordset($ULS_FACULTIES$
[
  {
    "name": "Faculty of Law",
    "short_name": null,
    "official_url": "https://www.uls.edu.lb/academics/law/",
    "notes": "Official faculty page for the graduate law programs.",
    "source_id": "ULS-S003"
  },
  {
    "name": "Faculty of Economics and Business Administration",
    "short_name": null,
    "official_url": "https://www.uls.edu.lb/academics/economics-business/",
    "notes": "Official faculty page for the graduate business programs and MIAGE.",
    "source_id": "ULS-S004"
  },
  {
    "name": "Faculty of Political Science and International Relations",
    "short_name": null,
    "official_url": "https://www.uls.edu.lb/academics/political-science/",
    "notes": "Official faculty page for the graduate political-science programs.",
    "source_id": "ULS-S008"
  },
  {
    "name": "Faculty of Public Health",
    "short_name": null,
    "official_url": "https://www.uls.edu.lb/academics/public-health/",
    "notes": "Official faculty page for the graduate public-health program.",
    "source_id": "ULS-S009"
  },
  {
    "name": "Faculty of Tourism and Hotel Management",
    "short_name": null,
    "official_url": "https://www.uls.edu.lb/academics/fthm/",
    "notes": "Official faculty page for the graduate hospitality-management program.",
    "source_id": "ULS-S010"
  },
  {
    "name": "Faculty of Religious and Theological Sciences",
    "short_name": null,
    "official_url": "https://www.uls.edu.lb/academics/religious-theological-sciences/",
    "notes": "Official faculty page for the graduate ecclesiastical-sciences program.",
    "source_id": "ULS-S011"
  },
  {
    "name": "Faculty of Canon Law",
    "short_name": null,
    "official_url": "https://www.uls.edu.lb/academics/",
    "notes": "Official academics page surfaces Canon Law as an academic unit, but its unlabeled master remains excluded from the graduate inventory.",
    "source_id": "ULS-S002"
  },
  {
    "name": "Faculty of Engineering",
    "short_name": null,
    "official_url": "https://www.uls.edu.lb/academics/engineering/",
    "notes": "Official faculty page was reviewed, but no in-scope graduate program was inventoried.",
    "source_id": "ULS-S012"
  }
]
$ULS_FACULTIES$::jsonb) AS x(name TEXT, short_name TEXT, faculty_type TEXT, official_url TEXT, notes TEXT);

    INSERT INTO university_faculty (university_id, name, short_name, faculty_type, official_url, notes)
    SELECT v_university_id, name, short_name, faculty_type, official_url, notes
    FROM uls_faculty_seed
    ON CONFLICT (university_id, name) DO UPDATE SET
        short_name = EXCLUDED.short_name,
        faculty_type = EXCLUDED.faculty_type,
        official_url = EXCLUDED.official_url,
        notes = EXCLUDED.notes,
        updated_at = NOW();

    CREATE TEMP TABLE uls_program_seed (
        program_key TEXT PRIMARY KEY,
        faculty_name TEXT NOT NULL,
        department_name TEXT,
        major_category TEXT,
        major TEXT,
        degree_type TEXT NOT NULL,
        official_degree_name TEXT NOT NULL,
        thesis_or_non_thesis TEXT,
        credits INTEGER,
        duration_value NUMERIC(10, 2),
        duration_unit TEXT,
        primary_language_code TEXT,
        delivery_mode TEXT,
        program_description TEXT,
        official_program_url TEXT NOT NULL,
        source_ids JSONB NOT NULL,
        notes TEXT,
        concentrations_or_tracks JSONB
    ) ON COMMIT DROP;

    INSERT INTO uls_program_seed (program_key, faculty_name, department_name, major_category, major, degree_type, official_degree_name, thesis_or_non_thesis, credits, duration_value, duration_unit, primary_language_code, delivery_mode, program_description, official_program_url, source_ids, notes, concentrations_or_tracks)
    SELECT program_key, faculty_name, department_name, major_category, major, degree_type, official_degree_name, thesis_or_non_thesis, credits, duration_value, duration_unit, primary_language_code, delivery_mode, program_description, official_program_url, source_ids, notes, concentrations_or_tracks FROM jsonb_to_recordset($ULS_PROGRAMS$
[
  {
    "program_key": "private-law",
    "faculty_name": "Faculty of Law",
    "department_name": null,
    "major_category": "Law",
    "major": "Private Law",
    "degree_type": "MASTER",
    "official_degree_name": "Master in Private Law",
    "thesis_or_non_thesis": null,
    "credits": 27,
    "duration_value": null,
    "duration_unit": null,
    "primary_language_code": "multi",
    "delivery_mode": null,
    "program_description": "Master in Private Law with four officially listed concentrations.",
    "official_program_url": "https://www.uls.edu.lb/academics/law/",
    "source_ids": [
      "ULS-S002",
      "ULS-S003"
    ],
    "notes": "The official faculty page lists the master in English and French and shows four private-law concentrations.",
    "concentrations_or_tracks": [
      "General Private Law",
      "Business Law",
      "Arbitration Law",
      "Energy Law"
    ]
  },
  {
    "program_key": "public-law",
    "faculty_name": "Faculty of Law",
    "department_name": null,
    "major_category": "Law",
    "major": "Public Law",
    "degree_type": "MASTER",
    "official_degree_name": "Master in Public Law",
    "thesis_or_non_thesis": null,
    "credits": 27,
    "duration_value": null,
    "duration_unit": null,
    "primary_language_code": "multi",
    "delivery_mode": null,
    "program_description": "Master in Public Law.",
    "official_program_url": "https://www.uls.edu.lb/academics/law/",
    "source_ids": [
      "ULS-S002",
      "ULS-S003"
    ],
    "notes": "The official faculty page lists the public-law master in English and French.",
    "concentrations_or_tracks": null
  },
  {
    "program_key": "comparative-law",
    "faculty_name": "Faculty of Law",
    "department_name": null,
    "major_category": "Law",
    "major": "Comparative Law",
    "degree_type": "MASTER",
    "official_degree_name": "Master in Comparative Law",
    "thesis_or_non_thesis": null,
    "credits": 27,
    "duration_value": null,
    "duration_unit": null,
    "primary_language_code": "multi",
    "delivery_mode": null,
    "program_description": "Master in Comparative Law.",
    "official_program_url": "https://www.uls.edu.lb/academics/law/",
    "source_ids": [
      "ULS-S002",
      "ULS-S003"
    ],
    "notes": "The official faculty page lists the comparative-law master in English and French.",
    "concentrations_or_tracks": null
  },
  {
    "program_key": "digital-law",
    "faculty_name": "Faculty of Law",
    "department_name": null,
    "major_category": "Law",
    "major": "Digital Law",
    "degree_type": "MASTER",
    "official_degree_name": "Master in Digital Law",
    "thesis_or_non_thesis": null,
    "credits": 27,
    "duration_value": null,
    "duration_unit": null,
    "primary_language_code": "multi",
    "delivery_mode": null,
    "program_description": "Master in Digital Law.",
    "official_program_url": "https://www.uls.edu.lb/academics/law/",
    "source_ids": [
      "ULS-S002",
      "ULS-S003"
    ],
    "notes": "The official faculty page lists the digital-law master in English and French.",
    "concentrations_or_tracks": null
  },
  {
    "program_key": "business-administration-finance-msc",
    "faculty_name": "Faculty of Economics and Business Administration",
    "department_name": null,
    "major_category": "Business Administration and Finance",
    "major": "Business Administration and Finance",
    "degree_type": "MASTER",
    "official_degree_name": "Master of Science in Business Administration and Finance (MSc)",
    "thesis_or_non_thesis": null,
    "credits": 42,
    "duration_value": null,
    "duration_unit": null,
    "primary_language_code": "multi",
    "delivery_mode": null,
    "program_description": "Master of Science in Business Administration and Finance with English concentration listings on the official graduate hub.",
    "official_program_url": "https://www.uls.edu.lb/academics/economics-business/",
    "source_ids": [
      "ULS-S002",
      "ULS-S004"
    ],
    "notes": "The official faculty page also publishes a French concentration list for the same MSc.",
    "concentrations_or_tracks": [
      "General Business",
      "Accounting and Auditing",
      "Banking and Finance",
      "Management",
      "Marketing"
    ]
  },
  {
    "program_key": "business-administration-finance-mba",
    "faculty_name": "Faculty of Economics and Business Administration",
    "department_name": null,
    "major_category": "Business Administration and Finance",
    "major": "Business Administration and Finance",
    "degree_type": "MASTER",
    "official_degree_name": "Professional Master in Business Administration and Finance (MBA)",
    "thesis_or_non_thesis": null,
    "credits": 42,
    "duration_value": null,
    "duration_unit": null,
    "primary_language_code": "multi",
    "delivery_mode": null,
    "program_description": "Professional Master in Business Administration and Finance with the officially published English concentration set.",
    "official_program_url": "https://www.uls.edu.lb/academics/economics-business/",
    "source_ids": [
      "ULS-S002",
      "ULS-S004"
    ],
    "notes": "The official faculty page also publishes a French concentration list for the same MBA.",
    "concentrations_or_tracks": [
      "General Business",
      "Accounting and Auditing",
      "Digital Marketing",
      "Financial Economics",
      "Financial Engineering",
      "Global Business and Economic Strategy",
      "Human Resource Management",
      "International Business Law",
      "Management",
      "Management Information Systems",
      "Petroleum and Gas Management",
      "Supply Chain Management"
    ]
  },
  {
    "program_key": "management-information-systems-miage",
    "faculty_name": "Faculty of Economics and Business Administration",
    "department_name": null,
    "major_category": "Business Administration and Finance",
    "major": "Management Information Systems (MIAGE)",
    "degree_type": "MASTER",
    "official_degree_name": "Double Master in Management Information Systems (MIAGE)",
    "thesis_or_non_thesis": null,
    "credits": 42,
    "duration_value": null,
    "duration_unit": null,
    "primary_language_code": "multi",
    "delivery_mode": null,
    "program_description": "Double master in Management Information Systems (MIAGE), jointly offered by Université La Sagesse and Université de Bordeaux, France.",
    "official_program_url": "https://www.uls.edu.lb/academics/economics-business/faculty-of-economics-and-business-administration-international/",
    "source_ids": [
      "ULS-S002",
      "ULS-S004",
      "ULS-S007"
    ],
    "notes": "The official international faculty page confirms the dual degree with Université de Bordeaux.",
    "concentrations_or_tracks": null
  },
  {
    "program_key": "political-science-international-relations",
    "faculty_name": "Faculty of Political Science and International Relations",
    "department_name": null,
    "major_category": "Political Science and International Relations",
    "major": "Political Science and International Relations",
    "degree_type": "MASTER",
    "official_degree_name": "Master in Political Science and International Relations",
    "thesis_or_non_thesis": null,
    "credits": 39,
    "duration_value": null,
    "duration_unit": null,
    "primary_language_code": "multi",
    "delivery_mode": null,
    "program_description": "Master in Political Science and International Relations with the Diplomacy and Strategic Negotiations concentration.",
    "official_program_url": "https://www.uls.edu.lb/academics/political-science/",
    "source_ids": [
      "ULS-S002",
      "ULS-S008"
    ],
    "notes": "The official faculty page also lists a professional master row with the same concentration.",
    "concentrations_or_tracks": [
      "Diplomacy and Strategic Negotiations"
    ]
  },
  {
    "program_key": "political-science-international-relations-diplomacy-strategic-negotiations",
    "faculty_name": "Faculty of Political Science and International Relations",
    "department_name": null,
    "major_category": "Political Science and International Relations",
    "major": "Political Science and International Relations",
    "degree_type": "MASTER",
    "official_degree_name": "Professional Master in Political Science and International Relations",
    "thesis_or_non_thesis": null,
    "credits": 39,
    "duration_value": null,
    "duration_unit": null,
    "primary_language_code": "multi",
    "delivery_mode": null,
    "program_description": "Professional Master in Political Science and International Relations with the Diplomacy and Strategic Negotiations concentration.",
    "official_program_url": "https://www.uls.edu.lb/academics/political-science/",
    "source_ids": [
      "ULS-S002",
      "ULS-S008"
    ],
    "notes": "The official faculty page lists this professional master with oral interview admission.",
    "concentrations_or_tracks": [
      "Diplomacy and Strategic Negotiations"
    ]
  },
  {
    "program_key": "political-science-international-relations-ngo-management",
    "faculty_name": "Faculty of Political Science and International Relations",
    "department_name": null,
    "major_category": "Political Science and International Relations",
    "major": "Political Science and International Relations",
    "degree_type": "MASTER",
    "official_degree_name": "Professional Master in Political Science and International Relations",
    "thesis_or_non_thesis": null,
    "credits": 39,
    "duration_value": null,
    "duration_unit": null,
    "primary_language_code": "multi",
    "delivery_mode": null,
    "program_description": "Professional Master in Political Science and International Relations with the NGO Management concentration.",
    "official_program_url": "https://www.uls.edu.lb/academics/political-science/",
    "source_ids": [
      "ULS-S002",
      "ULS-S008"
    ],
    "notes": "The official faculty page lists this professional master with oral interview admission.",
    "concentrations_or_tracks": [
      "NGO Management"
    ]
  },
  {
    "program_key": "hospital-management",
    "faculty_name": "Faculty of Public Health",
    "department_name": null,
    "major_category": "Public Health",
    "major": "Hospital Management",
    "degree_type": "MASTER",
    "official_degree_name": "Master in Hospital Management",
    "thesis_or_non_thesis": null,
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "primary_language_code": "multi",
    "delivery_mode": null,
    "program_description": "Master in Hospital Management offered in partnership with Université de Lille, France.",
    "official_program_url": "https://www.uls.edu.lb/academics/public-health/",
    "source_ids": [
      "ULS-S002",
      "ULS-S009"
    ],
    "notes": "The official faculty page states 39 + 6 credits and an oral interview requirement.",
    "concentrations_or_tracks": null
  },
  {
    "program_key": "hospitality-management",
    "faculty_name": "Faculty of Tourism and Hotel Management",
    "department_name": null,
    "major_category": "Tourism and Hotel Management",
    "major": "Hospitality Management",
    "degree_type": "MASTER",
    "official_degree_name": "Master in Hospitality Management",
    "thesis_or_non_thesis": null,
    "credits": 39,
    "duration_value": null,
    "duration_unit": null,
    "primary_language_code": "en",
    "delivery_mode": null,
    "program_description": "Master in Hospitality Management.",
    "official_program_url": "https://www.uls.edu.lb/academics/fthm/",
    "source_ids": [
      "ULS-S002",
      "ULS-S010"
    ],
    "notes": "The official faculty page lists an oral interview and 39 credits.",
    "concentrations_or_tracks": null
  },
  {
    "program_key": "ecclesiastical-sciences",
    "faculty_name": "Faculty of Religious and Theological Sciences",
    "department_name": null,
    "major_category": "Religious and Theological Sciences",
    "major": "Ecclesiastical Sciences",
    "degree_type": "MASTER",
    "official_degree_name": "Master in Ecclesiastical Sciences",
    "thesis_or_non_thesis": null,
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "primary_language_code": "ar",
    "delivery_mode": null,
    "program_description": "Master in Ecclesiastical Sciences with five officially listed concentrations.",
    "official_program_url": "https://www.uls.edu.lb/academics/religious-theological-sciences/",
    "source_ids": [
      "ULS-S002",
      "ULS-S011"
    ],
    "notes": "The official faculty page publishes the five concentration names and their track-specific credit values.",
    "concentrations_or_tracks": [
      "Accompagnement familial",
      "Sciences du mariage et de la famille",
      "Théologie biblique et dogmatique",
      "Théologie pastorale",
      "Doctrine sociale de l'église"
    ]
  }
]
$ULS_PROGRAMS$::jsonb) AS x(program_key TEXT, faculty_name TEXT, department_name TEXT, major_category TEXT, major TEXT, degree_type TEXT, official_degree_name TEXT, thesis_or_non_thesis TEXT, credits INTEGER, duration_value NUMERIC(10, 2), duration_unit TEXT, primary_language_code TEXT, delivery_mode TEXT, program_description TEXT, official_program_url TEXT, source_ids JSONB, notes TEXT, concentrations_or_tracks JSONB);

    INSERT INTO graduate_program (
        university_id, faculty_id, department_id, degree_type_id, program_key, major_category, major, official_degree_name,
        thesis_or_non_thesis, credits, duration_value, duration_unit, primary_language_id, delivery_mode,
        program_description, official_program_url, source_id, notes
    )
    SELECT
        v_university_id,
        f.id,
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
        CASE p.primary_language_code
            WHEN 'en' THEN v_lang_en_id
            WHEN 'fr' THEN v_lang_fr_id
            WHEN 'ar' THEN v_lang_ar_id
            WHEN 'it' THEN v_lang_it_id
            WHEN 'multi' THEN v_lang_multi_id
            ELSE NULL
        END,
        p.delivery_mode,
        p.program_description,
        p.official_program_url,
        s.id,
        p.notes
    FROM uls_program_seed p
    JOIN university_faculty f
      ON f.university_id = v_university_id
     AND f.name = p.faculty_name
    LEFT JOIN degree_type dt
      ON dt.code = p.degree_type
    JOIN LATERAL (
        SELECT src.id
        FROM jsonb_array_elements_text(p.source_ids) WITH ORDINALITY AS ss(source_seed_id, ord)
        JOIN uls_source_seed us
          ON us.source_id = ss.source_seed_id
        JOIN source src
          ON src.university_id = v_university_id
         AND src.url = us.url
        ORDER BY ord
        LIMIT 1
    ) s ON TRUE
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

    INSERT INTO graduate_program_track (
        university_id, program_id, track_type, track_name, track_order, is_primary, description, source_id, notes
    )
    SELECT
        v_university_id,
        gp.id,
        'CONCENTRATION',
        t.track_name,
        t.track_order,
        t.track_order = 1,
        NULL,
        s.id,
        p.notes
    FROM uls_program_seed p
    JOIN graduate_program gp
      ON gp.university_id = v_university_id
     AND gp.program_key = p.program_key
    JOIN LATERAL jsonb_array_elements_text(COALESCE(p.concentrations_or_tracks, '[]'::jsonb)) WITH ORDINALITY AS t(track_name, track_order)
      ON TRUE
    JOIN LATERAL (
        SELECT src.id
        FROM jsonb_array_elements_text(p.source_ids) WITH ORDINALITY AS ss(source_seed_id, ord)
        JOIN uls_source_seed us
          ON us.source_id = ss.source_seed_id
        JOIN source src
          ON src.university_id = v_university_id
         AND src.url = us.url
        ORDER BY ord DESC
        LIMIT 1
    ) s ON TRUE
    ON CONFLICT (program_id, track_type, track_name) DO UPDATE SET
        track_order = EXCLUDED.track_order,
        is_primary = EXCLUDED.is_primary,
        source_id = EXCLUDED.source_id,
        notes = EXCLUDED.notes,
        updated_at = NOW();

    INSERT INTO graduate_program_source (
        university_id, program_id, source_id, source_role, source_order, evidence_text, notes
    )
    SELECT
        v_university_id,
        gp.id,
        s.id,
        CASE ss.ord
            WHEN 1 THEN 'PRIMARY'
            WHEN 2 THEN 'SECONDARY'
            ELSE 'FACULTY'
        END,
        ss.ord,
        NULL,
        p.notes
    FROM uls_program_seed p
    JOIN graduate_program gp
      ON gp.university_id = v_university_id
     AND gp.program_key = p.program_key
    JOIN LATERAL jsonb_array_elements_text(p.source_ids) WITH ORDINALITY AS ss(source_seed_id, ord)
      ON TRUE
    JOIN uls_source_seed us
      ON us.source_id = ss.source_seed_id
    JOIN source s
      ON s.university_id = v_university_id
     AND s.url = us.url
    ON CONFLICT (program_id, source_id, source_role) DO UPDATE SET
        source_order = EXCLUDED.source_order,
        evidence_text = EXCLUDED.evidence_text,
        notes = EXCLUDED.notes,
        updated_at = NOW();

    CREATE TEMP TABLE uls_tuition_seed (
        record_key TEXT PRIMARY KEY,
        program_key TEXT NOT NULL UNIQUE,
        faculty_name TEXT NOT NULL,
        academic_year TEXT,
        currency TEXT NOT NULL,
        billing_basis TEXT NOT NULL,
        amount NUMERIC(12, 2) NOT NULL,
        category TEXT NOT NULL,
        notes TEXT,
        source_id TEXT NOT NULL
    ) ON COMMIT DROP;

    INSERT INTO uls_tuition_seed (record_key, program_key, faculty_name, academic_year, currency, billing_basis, amount, category, notes, source_id)
    VALUES
        ('uls-tuition-law-private-law', 'uls-law-master-private-law', 'Faculty of Law', '2025-2026', 'USD', 'PER_CREDIT', 300, 'Graduate Tuition', 'Official tuition-fees page lists the Faculty of Law graduate rate as $300 per credit.', 'ULS-S018'),
        ('uls-tuition-law-public-law', 'uls-law-master-public-law', 'Faculty of Law', '2025-2026', 'USD', 'PER_CREDIT', 300, 'Graduate Tuition', 'Official tuition-fees page lists the Faculty of Law graduate rate as $300 per credit.', 'ULS-S018'),
        ('uls-tuition-law-comparative-law', 'uls-law-master-comparative-law', 'Faculty of Law', '2025-2026', 'USD', 'PER_CREDIT', 300, 'Graduate Tuition', 'Official tuition-fees page lists the Faculty of Law graduate rate as $300 per credit.', 'ULS-S018'),
        ('uls-tuition-law-digital-law', 'uls-law-master-digital-law', 'Faculty of Law', '2025-2026', 'USD', 'PER_CREDIT', 300, 'Graduate Tuition', 'Official tuition-fees page lists the Faculty of Law graduate rate as $300 per credit.', 'ULS-S018'),
        ('uls-tuition-feba-msc-finance', 'uls-feba-master-business-administration-finance-msc', 'Faculty of Economics and Business Administration', '2025-2026', 'USD', 'PER_CREDIT', 230, 'Graduate Tuition', 'Official tuition-fees page lists the MSc Business Administration in Finance graduate rate as $230 per credit.', 'ULS-S018'),
        ('uls-tuition-feba-mba-finance', 'uls-feba-master-business-administration-finance-mba', 'Faculty of Economics and Business Administration', '2025-2026', 'USD', 'PER_CREDIT', 200, 'Graduate Tuition', 'Official tuition-fees page lists the MBA in Finance graduate rate as $200 per credit.', 'ULS-S018'),
        ('uls-tuition-polisci-master', 'uls-polisci-master-political-science-international-relations', 'Faculty of Political Science and International Relations', '2025-2026', 'USD', 'PER_CREDIT', 230, 'Graduate Tuition', 'Official tuition-fees page lists the Faculty of Political Science and International Relations graduate rate as $230 per credit.', 'ULS-S018'),
        ('uls-tuition-polisci-diplomacy', 'uls-polisci-professional-master-political-science-international-relations-diplomacy-strategic-negotiations', 'Faculty of Political Science and International Relations', '2025-2026', 'USD', 'PER_CREDIT', 230, 'Graduate Tuition', 'Official tuition-fees page lists the Faculty of Political Science and International Relations graduate rate as $230 per credit.', 'ULS-S018'),
        ('uls-tuition-polisci-ngo', 'uls-polisci-professional-master-political-science-international-relations-ngo-management', 'Faculty of Political Science and International Relations', '2025-2026', 'USD', 'PER_CREDIT', 230, 'Graduate Tuition', 'Official tuition-fees page lists the Faculty of Political Science and International Relations graduate rate as $230 per credit.', 'ULS-S018'),
        ('uls-tuition-public-health', 'uls-public-health-master-hospital-management', 'Faculty of Public Health', '2025-2026', 'USD', 'PER_CREDIT', 230, 'Graduate Tuition', 'Official tuition-fees page lists the Faculty of Public Health graduate rate as $230 per credit.', 'ULS-S018'),
        ('uls-tuition-tourism', 'uls-tourism-master-hospitality-management', 'Faculty of Tourism and Hotel Management', '2025-2026', 'USD', 'PER_CREDIT', 300, 'Graduate Tuition', 'Official tuition-fees page lists the Faculty of Tourism and Hotel Management graduate rate as $300 per credit.', 'ULS-S018'),
        ('uls-tuition-religious', 'uls-religious-master-ecclesiastical-sciences', 'Faculty of Religious and Theological Sciences', '2025-2026', 'USD', 'PER_CREDIT', 55, 'Graduate Tuition', 'Official tuition-fees page lists the Faculty of Religious and Theological Sciences graduate rate as $55 per credit.', 'ULS-S018');

    INSERT INTO graduate_tuition_rate (
        university_id, faculty_id, department_id, program_id, scope_level, record_key, academic_year, currency, billing_basis, amount, category, notes, source_id
    )
    SELECT
        v_university_id,
        f.id,
        NULL,
        NULL,
        'FACULTY',
        t.record_key,
        t.academic_year,
        t.currency,
        t.billing_basis,
        t.amount,
        t.category,
        t.notes,
        s.id
    FROM uls_tuition_seed t
    JOIN university_faculty f
      ON f.university_id = v_university_id
     AND f.name = t.faculty_name
    JOIN LATERAL (
        SELECT src.id
        FROM uls_source_seed us
        JOIN source src ON src.university_id = v_university_id AND src.url = us.url
        WHERE us.source_id = t.source_id
        LIMIT 1
    ) s ON TRUE
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

    CREATE TEMP TABLE uls_fee_seed (
        record_key TEXT PRIMARY KEY,
        fee_name TEXT NOT NULL,
        scope_level TEXT NOT NULL,
        faculty_name TEXT,
        program_key TEXT,
        academic_year TEXT,
        billing_basis TEXT NOT NULL,
        currency TEXT NOT NULL,
        amount NUMERIC(12, 2),
        secondary_amount NUMERIC(12, 2),
        secondary_currency TEXT,
        category TEXT,
        notes TEXT,
        source_id TEXT NOT NULL
    ) ON COMMIT DROP;

    INSERT INTO uls_fee_seed (record_key, fee_name, scope_level, faculty_name, program_key, academic_year, billing_basis, currency, amount, secondary_amount, secondary_currency, category, notes, source_id)
    SELECT record_key, fee_name, scope_level, faculty_name, program_key, academic_year, billing_basis, currency, amount, secondary_amount, secondary_currency, category, notes, source_id FROM jsonb_to_recordset($ULS_FEES$
[
  {
    "record_key": "uls-fee-application",
    "fee_name": "Graduate Application Fee",
    "scope_level": "UNIVERSITY",
    "faculty_name": null,
    "program_key": null,
    "academic_year": "2025-2026",
    "billing_basis": "FLAT_FEE",
    "currency": "USD",
    "amount": 50,
    "category": "Graduate Fees",
    "notes": "Official tuition-fees page lists a $50 application fee across the graduate fee table.",
    "source_id": "ULS-S018"
  },
  {
    "record_key": "uls-fee-registration-law",
    "fee_name": "Graduate Registration Fee - Faculty of Law",
    "scope_level": "FACULTY",
    "faculty_name": "Faculty of Law",
    "program_key": null,
    "academic_year": "2025-2026",
    "billing_basis": "FLAT_FEE",
    "currency": "USD",
    "amount": 300,
    "secondary_amount": 7000000,
    "secondary_currency": "LBP",
    "category": "Graduate Fees",
    "notes": "Official tuition-fees page lists a $300 + 7,000,000 LBP graduate registration fee.",
    "source_id": "ULS-S018"
  },
  {
    "record_key": "uls-fee-registration-feba",
    "fee_name": "Graduate Registration Fee - Faculty of Economics and Business Administration",
    "scope_level": "FACULTY",
    "faculty_name": "Faculty of Economics and Business Administration",
    "program_key": null,
    "academic_year": "2025-2026",
    "billing_basis": "FLAT_FEE",
    "currency": "USD",
    "amount": 300,
    "secondary_amount": 7000000,
    "secondary_currency": "LBP",
    "category": "Graduate Fees",
    "notes": "Official tuition-fees page lists a $300 + 7,000,000 LBP graduate registration fee.",
    "source_id": "ULS-S018"
  },
  {
    "record_key": "uls-fee-registration-polisci",
    "fee_name": "Graduate Registration Fee - Faculty of Political Science and International Relations",
    "scope_level": "FACULTY",
    "faculty_name": "Faculty of Political Science and International Relations",
    "program_key": null,
    "academic_year": "2025-2026",
    "billing_basis": "FLAT_FEE",
    "currency": "USD",
    "amount": 300,
    "secondary_amount": 7000000,
    "secondary_currency": "LBP",
    "category": "Graduate Fees",
    "notes": "Official tuition-fees page lists a $300 + 7,000,000 LBP graduate registration fee.",
    "source_id": "ULS-S018"
  },
  {
    "record_key": "uls-fee-registration-public-health",
    "fee_name": "Graduate Registration Fee - Faculty of Public Health",
    "scope_level": "FACULTY",
    "faculty_name": "Faculty of Public Health",
    "program_key": null,
    "academic_year": "2025-2026",
    "billing_basis": "FLAT_FEE",
    "currency": "USD",
    "amount": 300,
    "secondary_amount": 7000000,
    "secondary_currency": "LBP",
    "category": "Graduate Fees",
    "notes": "Official tuition-fees page lists a $300 + 7,000,000 LBP graduate registration fee.",
    "source_id": "ULS-S018"
  },
  {
    "record_key": "uls-fee-registration-tourism",
    "fee_name": "Graduate Registration Fee - Faculty of Tourism and Hotel Management",
    "scope_level": "FACULTY",
    "faculty_name": "Faculty of Tourism and Hotel Management",
    "program_key": null,
    "academic_year": "2025-2026",
    "billing_basis": "FLAT_FEE",
    "currency": "USD",
    "amount": 300,
    "secondary_amount": 7000000,
    "secondary_currency": "LBP",
    "category": "Graduate Fees",
    "notes": "Official tuition-fees page lists a $300 + 7,000,000 LBP graduate registration fee.",
    "source_id": "ULS-S018"
  },
  {
    "record_key": "uls-fee-registration-religious",
    "fee_name": "Graduate Registration Fee - Faculty of Religious and Theological Sciences",
    "scope_level": "FACULTY",
    "faculty_name": "Faculty of Religious and Theological Sciences",
    "program_key": null,
    "academic_year": "2025-2026",
    "billing_basis": "FLAT_FEE",
    "currency": "USD",
    "amount": 40,
    "secondary_amount": 1000000,
    "secondary_currency": "LBP",
    "category": "Graduate Fees",
    "notes": "Official tuition-fees page lists a $40 + 1,000,000 LBP graduate registration fee.",
    "source_id": "ULS-S018"
  },
  {
    "record_key": "uls-fee-support-services-general",
    "fee_name": "Support Services Fee",
    "scope_level": "UNIVERSITY",
    "faculty_name": null,
    "program_key": null,
    "academic_year": "2025-2026",
    "billing_basis": "FLAT_FEE",
    "currency": "USD",
    "amount": 120,
    "category": "Graduate Fees",
    "notes": "Official tuition-fees page lists support services at $120 for most graduate faculties.",
    "source_id": "ULS-S018"
  },
  {
    "record_key": "uls-fee-support-services-religious",
    "fee_name": "Support Services Fee - Faculty of Religious and Theological Sciences",
    "scope_level": "FACULTY",
    "faculty_name": "Faculty of Religious and Theological Sciences",
    "program_key": null,
    "academic_year": "2025-2026",
    "billing_basis": "FLAT_FEE",
    "currency": "USD",
    "amount": 20,
    "category": "Graduate Fees",
    "notes": "Official tuition-fees page lists a $20 support-services fee for the Faculty of Religious and Theological Sciences.",
    "source_id": "ULS-S018"
  },
  {
    "record_key": "uls-fee-down-payment-general",
    "fee_name": "Down Payment",
    "scope_level": "UNIVERSITY",
    "faculty_name": null,
    "program_key": null,
    "academic_year": "2025-2026",
    "billing_basis": "FLAT_FEE",
    "currency": "USD",
    "amount": 330,
    "category": "Graduate Fees",
    "notes": "Official tuition-fees page lists a $330 down payment for most graduate faculties.",
    "source_id": "ULS-S018"
  },
  {
    "record_key": "uls-fee-down-payment-religious",
    "fee_name": "Down Payment - Faculty of Religious and Theological Sciences",
    "scope_level": "FACULTY",
    "faculty_name": "Faculty of Religious and Theological Sciences",
    "program_key": null,
    "academic_year": "2025-2026",
    "billing_basis": "FLAT_FEE",
    "currency": "USD",
    "amount": 60,
    "category": "Graduate Fees",
    "notes": "Official tuition-fees page lists a $60 down payment for the Faculty of Religious and Theological Sciences.",
    "source_id": "ULS-S018"
  }
]
$ULS_FEES$::jsonb) AS x(record_key TEXT, fee_name TEXT, scope_level TEXT, faculty_name TEXT, program_key TEXT, academic_year TEXT, billing_basis TEXT, currency TEXT, amount NUMERIC(12, 2), secondary_amount NUMERIC(12, 2), secondary_currency TEXT, category TEXT, notes TEXT, source_id TEXT);

    INSERT INTO graduate_fee_item (
        university_id, faculty_id, department_id, program_id, scope_level, record_key, academic_year, fee_name, billing_basis, currency, amount, category, notes, source_id
    )
    SELECT
        v_university_id,
        f.id,
        NULL,
        NULL,
        fs.scope_level,
        fs.record_key,
        fs.academic_year,
        fs.fee_name,
        fs.billing_basis,
        fs.currency,
        fs.amount,
        fs.category,
        fs.notes,
        s.id
    FROM uls_fee_seed fs
    LEFT JOIN university_faculty f
      ON f.university_id = v_university_id
     AND f.name = fs.faculty_name
    JOIN LATERAL (
        SELECT src.id
        FROM uls_source_seed us
        JOIN source src ON src.university_id = v_university_id AND src.url = us.url
        WHERE us.source_id = fs.source_id
        LIMIT 1
    ) s ON TRUE
    ON CONFLICT (university_id, record_key) DO UPDATE SET
        faculty_id = EXCLUDED.faculty_id,
        department_id = EXCLUDED.department_id,
        program_id = EXCLUDED.program_id,
        scope_level = EXCLUDED.scope_level,
        academic_year = EXCLUDED.academic_year,
        fee_name = EXCLUDED.fee_name,
        billing_basis = EXCLUDED.billing_basis,
        currency = EXCLUDED.currency,
        amount = EXCLUDED.amount,
        category = EXCLUDED.category,
        notes = EXCLUDED.notes,
        source_id = EXCLUDED.source_id,
        updated_at = NOW();

    CREATE TEMP TABLE uls_admission_seed (
        record_key TEXT PRIMARY KEY,
        program_key TEXT,
        scope_level TEXT NOT NULL,
        requirement_type TEXT NOT NULL,
        requirement_text TEXT NOT NULL,
        comparison_operator TEXT,
        threshold_value NUMERIC(12, 2),
        threshold_unit TEXT,
        is_required BOOLEAN NOT NULL,
        notes TEXT,
        source_id TEXT NOT NULL
    ) ON COMMIT DROP;

    INSERT INTO uls_admission_seed (record_key, program_key, scope_level, requirement_type, requirement_text, comparison_operator, threshold_value, threshold_unit, is_required, notes, source_id)
    SELECT record_key, program_key, scope_level, requirement_type, requirement_text, comparison_operator, threshold_value, threshold_unit, is_required, notes, source_id FROM jsonb_to_recordset($ULS_ADM$
[
  {
    "record_key": "uls-adm-bachelor-degree",
    "program_key": null,
    "scope_level": "UNIVERSITY",
    "requirement_type": "ACADEMIC",
    "requirement_text": "Applicants must hold a bachelor's degree or equivalent from an accredited institution recognized by MEHE.",
    "comparison_operator": null,
    "threshold_value": null,
    "threshold_unit": null,
    "is_required": true,
    "notes": "General graduate admission criterion.",
    "source_id": "ULS-S008"
  },
  {
    "record_key": "uls-adm-min-gpa-275",
    "program_key": null,
    "scope_level": "UNIVERSITY",
    "requirement_type": "ACADEMIC",
    "requirement_text": "Regular graduate status requires a minimum undergraduate cumulative GPA of 2.75 in the major area of study.",
    "comparison_operator": ">=",
    "threshold_value": 2.75,
    "threshold_unit": "GPA",
    "is_required": true,
    "notes": "Regular graduate status criterion.",
    "source_id": "ULS-S008"
  },
  {
    "record_key": "uls-adm-apply-one-month-prior",
    "program_key": null,
    "scope_level": "UNIVERSITY",
    "requirement_type": "GENERAL",
    "requirement_text": "Applicants should submit the application file at least one month before the beginning of the semester.",
    "comparison_operator": null,
    "threshold_value": null,
    "threshold_unit": null,
    "is_required": true,
    "notes": "Admission timing guidance from the catalog.",
    "source_id": "ULS-S008"
  },
  {
    "record_key": "uls-adm-english-proficiency",
    "program_key": null,
    "scope_level": "UNIVERSITY",
    "requirement_type": "ENGLISH",
    "requirement_text": "Graduate applicants must demonstrate English proficiency. Accepted thresholds listed in the catalog are TOEFL ITP 600, TOEFL iBT 100, IELTS 7.0, and Duolingo 120.",
    "comparison_operator": null,
    "threshold_value": null,
    "threshold_unit": null,
    "is_required": true,
    "notes": "Language requirement from the catalog.",
    "source_id": "ULS-S008"
  },
  {
    "record_key": "uls-adm-gre-gmat-note",
    "program_key": null,
    "scope_level": "UNIVERSITY",
    "requirement_type": "OTHER",
    "requirement_text": "Departments offering MA, MAT, and MS may require GRE. MBA applicants may be required to sit for GMAT.",
    "comparison_operator": null,
    "threshold_value": null,
    "threshold_unit": null,
    "is_required": true,
    "notes": "General departmental testing note.",
    "source_id": "ULS-S008"
  },
  {
    "record_key": "uls-adm-public-health-recommendation-letters",
    "program_key": "uls-public-health-master-hospital-management",
    "scope_level": "PROGRAM",
    "requirement_type": "PREREQUISITE",
    "requirement_text": "For Public Health candidates: two recommendation letters in a sealed envelope.",
    "comparison_operator": null,
    "threshold_value": null,
    "threshold_unit": null,
    "is_required": true,
    "notes": "Program-specific requirement published in the application instructions.",
    "source_id": "ULS-S015"
  },
  {
    "record_key": "uls-adm-polisci-master-interview",
    "program_key": "uls-polisci-master-political-science-international-relations",
    "scope_level": "PROGRAM",
    "requirement_type": "INTERVIEW",
    "requirement_text": "Oral interview required for the Master in Political Science and International Relations.",
    "comparison_operator": null,
    "threshold_value": null,
    "threshold_unit": null,
    "is_required": true,
    "notes": "Interview requirement published on the faculty page.",
    "source_id": "ULS-S008"
  },
  {
    "record_key": "uls-adm-polisci-professional-diplomacy-interview",
    "program_key": "uls-polisci-professional-master-political-science-international-relations-diplomacy-strategic-negotiations",
    "scope_level": "PROGRAM",
    "requirement_type": "INTERVIEW",
    "requirement_text": "Oral interview required for the professional master in Diplomacy and Strategic Negotiations.",
    "comparison_operator": null,
    "threshold_value": null,
    "threshold_unit": null,
    "is_required": true,
    "notes": "Interview requirement published on the faculty page.",
    "source_id": "ULS-S008"
  },
  {
    "record_key": "uls-adm-polisci-professional-ngo-interview",
    "program_key": "uls-polisci-professional-master-political-science-international-relations-ngo-management",
    "scope_level": "PROGRAM",
    "requirement_type": "INTERVIEW",
    "requirement_text": "Oral interview required for the professional master in NGO Management.",
    "comparison_operator": null,
    "threshold_value": null,
    "threshold_unit": null,
    "is_required": true,
    "notes": "Interview requirement published on the faculty page.",
    "source_id": "ULS-S008"
  },
  {
    "record_key": "uls-adm-public-health-interview",
    "program_key": "uls-public-health-master-hospital-management",
    "scope_level": "PROGRAM",
    "requirement_type": "INTERVIEW",
    "requirement_text": "Oral interview required for the Master in Hospital Management.",
    "comparison_operator": null,
    "threshold_value": null,
    "threshold_unit": null,
    "is_required": true,
    "notes": "Interview requirement published on the faculty page.",
    "source_id": "ULS-S009"
  },
  {
    "record_key": "uls-adm-tourism-interview",
    "program_key": "uls-tourism-master-hospitality-management",
    "scope_level": "PROGRAM",
    "requirement_type": "INTERVIEW",
    "requirement_text": "Oral interview required for the Master in Hospitality Management.",
    "comparison_operator": null,
    "threshold_value": null,
    "threshold_unit": null,
    "is_required": true,
    "notes": "Interview requirement published on the faculty page.",
    "source_id": "ULS-S010"
  },
  {
    "record_key": "uls-adm-religious-language-interview",
    "program_key": "uls-religious-master-ecclesiastical-sciences",
    "scope_level": "PROGRAM",
    "requirement_type": "PREREQUISITE",
    "requirement_text": "French or English or Italian; oral interview.",
    "comparison_operator": null,
    "threshold_value": null,
    "threshold_unit": null,
    "is_required": true,
    "notes": "Program-specific condition published on the faculty page.",
    "source_id": "ULS-S011"
  }
]
$ULS_ADM$::jsonb) AS x(record_key TEXT, program_key TEXT, scope_level TEXT, requirement_type TEXT, requirement_text TEXT, comparison_operator TEXT, threshold_value NUMERIC(12, 2), threshold_unit TEXT, is_required BOOLEAN, notes TEXT, source_id TEXT);

    INSERT INTO graduate_admission_requirement (
        university_id, faculty_id, department_id, program_id, scope_level, record_key, requirement_type, requirement_text, comparison_operator, threshold_value, threshold_unit, is_required, notes, source_id
    )
    SELECT
        v_university_id,
        gp.faculty_id,
        NULL,
        gp.id,
        a.scope_level,
        a.record_key,
        a.requirement_type,
        a.requirement_text,
        a.comparison_operator,
        a.threshold_value,
        a.threshold_unit,
        a.is_required,
        a.notes,
        s.id
    FROM uls_admission_seed a
    LEFT JOIN graduate_program gp
      ON gp.university_id = v_university_id
     AND gp.program_key = a.program_key
    JOIN LATERAL (
        SELECT src.id
        FROM uls_source_seed us
        JOIN source src ON src.university_id = v_university_id AND src.url = us.url
        WHERE us.source_id = a.source_id
        LIMIT 1
    ) s ON TRUE
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

    CREATE TEMP TABLE uls_required_document_seed (
        record_key TEXT PRIMARY KEY,
        document_type TEXT NOT NULL,
        document_name TEXT NOT NULL,
        sort_order INTEGER,
        notes TEXT,
        source_id TEXT NOT NULL
    ) ON COMMIT DROP;

    INSERT INTO uls_required_document_seed (record_key, document_type, document_name, sort_order, notes, source_id)
    SELECT record_key, document_type, document_name, sort_order, notes, source_id FROM jsonb_to_recordset($ULS_DOCS$
[
  {
    "record_key": "uls-doc-bachelor-degree",
    "document_type": "ACADEMIC_RECORD",
    "document_name": "Certified copy of the bachelor's degree",
    "sort_order": 1,
    "notes": "Required for graduate application.",
    "source_id": "ULS-S015"
  },
  {
    "record_key": "uls-doc-university-transcripts",
    "document_type": "ACADEMIC_RECORD",
    "document_name": "Certified copy of the university transcripts",
    "sort_order": 2,
    "notes": "Required for graduate application.",
    "source_id": "ULS-S015"
  },
  {
    "record_key": "uls-doc-cv",
    "document_type": "OTHER",
    "document_name": "Curriculum Vitae",
    "sort_order": 3,
    "notes": "Required for graduate application.",
    "source_id": "ULS-S015"
  },
  {
    "record_key": "uls-doc-public-health-recommendations",
    "document_type": "OTHER",
    "document_name": "Two recommendation letters for Public Health candidates",
    "sort_order": 4,
    "notes": "Template form to be presented in a sealed envelope.",
    "source_id": "ULS-S015"
  }
]
$ULS_DOCS$::jsonb) AS x(record_key TEXT, document_type TEXT, document_name TEXT, sort_order INTEGER, notes TEXT, source_id TEXT);

    INSERT INTO graduate_required_document (
        university_id, faculty_id, department_id, program_id, scope_level, record_key, document_type, document_name, is_optional, sort_order, notes, source_id
    )
    SELECT
        v_university_id, NULL, NULL, NULL, 'UNIVERSITY', d.record_key, d.document_type, d.document_name, FALSE, d.sort_order, d.notes, s.id
    FROM uls_required_document_seed d
    JOIN LATERAL (
        SELECT src.id
        FROM uls_source_seed us
        JOIN source src ON src.university_id = v_university_id AND src.url = us.url
        WHERE us.source_id = d.source_id
        LIMIT 1
    ) s ON TRUE
    ON CONFLICT (university_id, record_key) DO UPDATE SET
        scope_level = EXCLUDED.scope_level,
        document_type = EXCLUDED.document_type,
        document_name = EXCLUDED.document_name,
        is_optional = EXCLUDED.is_optional,
        sort_order = EXCLUDED.sort_order,
        notes = EXCLUDED.notes,
        source_id = EXCLUDED.source_id,
        updated_at = NOW();

    CREATE TEMP TABLE uls_deadline_seed (
        record_key TEXT PRIMARY KEY,
        academic_year TEXT,
        deadline_type TEXT NOT NULL,
        term TEXT,
        deadline_date DATE,
        note TEXT,
        source_id TEXT NOT NULL
    ) ON COMMIT DROP;

    INSERT INTO uls_deadline_seed (record_key, academic_year, deadline_type, term, deadline_date, note, source_id)
    SELECT record_key, academic_year, deadline_type, term, deadline_date, note, source_id FROM jsonb_to_recordset($ULS_DEADLINES$
[
  {
    "record_key": "uls-deadline-2026-early-registration",
    "academic_year": "2026-2027",
    "deadline_type": "APPLICATION_OPEN",
    "term": "Early Admission Registration",
    "deadline_date": "2025-12-01",
    "note": "Early admission registration for Fall Semester 2026-2027.",
    "source_id": "ULS-S016"
  },
  {
    "record_key": "uls-deadline-2026-regular-registration",
    "academic_year": "2026-2027",
    "deadline_type": "REGULAR",
    "term": "Regular Admission Registration",
    "deadline_date": "2026-02-02",
    "note": "Regular admission registration for Fall Semester 2026-2027.",
    "source_id": "ULS-S016"
  },
  {
    "record_key": "uls-deadline-2026-early-application-final",
    "academic_year": "2026-2027",
    "deadline_type": "FINAL",
    "term": "Early Admission Application",
    "deadline_date": "2026-03-31",
    "note": "Deadline to submit an early admission application for Fall Semester 2026-2027.",
    "source_id": "ULS-S016"
  },
  {
    "record_key": "uls-deadline-2025-fall-exam-1",
    "academic_year": "2025-2026",
    "deadline_type": "INTERVIEW",
    "term": "Admission Entrance Exam",
    "deadline_date": "2025-11-19",
    "note": "Faculty entrance exam date listed on the official When to Apply page.",
    "source_id": "ULS-S016"
  },
  {
    "record_key": "uls-deadline-2025-fall-exam-2",
    "academic_year": "2025-2026",
    "deadline_type": "INTERVIEW",
    "term": "Admission Entrance Exam",
    "deadline_date": "2025-12-03",
    "note": "Faculty entrance exam date listed on the official When to Apply page.",
    "source_id": "ULS-S016"
  },
  {
    "record_key": "uls-deadline-2025-fall-exam-3",
    "academic_year": "2025-2026",
    "deadline_type": "INTERVIEW",
    "term": "Admission Entrance Exam",
    "deadline_date": "2025-12-10",
    "note": "Faculty entrance exam date listed on the official When to Apply page.",
    "source_id": "ULS-S016"
  },
  {
    "record_key": "uls-deadline-2026-fall-exam-1",
    "academic_year": "2025-2026",
    "deadline_type": "INTERVIEW",
    "term": "Admission Entrance Exam",
    "deadline_date": "2026-01-14",
    "note": "Faculty entrance exam date listed on the official When to Apply page.",
    "source_id": "ULS-S016"
  },
  {
    "record_key": "uls-deadline-2026-fall-exam-2",
    "academic_year": "2025-2026",
    "deadline_type": "INTERVIEW",
    "term": "Admission Entrance Exam",
    "deadline_date": "2026-01-21",
    "note": "Faculty entrance exam date listed on the official When to Apply page.",
    "source_id": "ULS-S016"
  },
  {
    "record_key": "uls-deadline-2026-fall-exam-3",
    "academic_year": "2025-2026",
    "deadline_type": "INTERVIEW",
    "term": "Admission Entrance Exam",
    "deadline_date": "2026-01-28",
    "note": "Faculty entrance exam date listed on the official When to Apply page.",
    "source_id": "ULS-S016"
  }
]
$ULS_DEADLINES$::jsonb) AS x(record_key TEXT, academic_year TEXT, deadline_type TEXT, term TEXT, deadline_date DATE, note TEXT, source_id TEXT);

    INSERT INTO graduate_admission_deadline (
        university_id, faculty_id, department_id, program_id, scope_level, record_key, academic_year, deadline_type, term, deadline_date, note, source_id
    )
    SELECT
        v_university_id, NULL, NULL, NULL, 'UNIVERSITY', d.record_key, d.academic_year, d.deadline_type, d.term, d.deadline_date, d.note, s.id
    FROM uls_deadline_seed d
    JOIN LATERAL (
        SELECT src.id
        FROM uls_source_seed us
        JOIN source src ON src.university_id = v_university_id AND src.url = us.url
        WHERE us.source_id = d.source_id
        LIMIT 1
    ) s ON TRUE
    ON CONFLICT (university_id, record_key) DO UPDATE SET
        academic_year = EXCLUDED.academic_year,
        deadline_type = EXCLUDED.deadline_type,
        term = EXCLUDED.term,
        deadline_date = EXCLUDED.deadline_date,
        note = EXCLUDED.note,
        source_id = EXCLUDED.source_id,
        updated_at = NOW();

    CREATE TEMP TABLE uls_scholarship_seed (
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

    INSERT INTO uls_scholarship_seed (record_key, academic_year, name, description, coverage, amount, currency, notes, source_id)
    SELECT record_key, academic_year, name, description, coverage, amount, currency, notes, source_id FROM jsonb_to_recordset($ULS_SCHOLARSHIPS$
[
  {
    "record_key": "uls-scholarship-1",
    "academic_year": "2025-2026",
    "name": "Prodigy Achievement Scholarship",
    "description": "Prodigy Achievement Scholarship",
    "coverage": null,
    "amount": null,
    "currency": null,
    "notes": "ULS publishes scholarship and merit-based support options on its financial-support page.",
    "source_id": "ULS-S019"
  },
  {
    "record_key": "uls-scholarship-2",
    "academic_year": "2025-2026",
    "name": "Academic Distinction Scholarship",
    "description": "Academic Distinction Scholarship",
    "coverage": null,
    "amount": null,
    "currency": null,
    "notes": "ULS publishes scholarship and merit-based support options on its financial-support page.",
    "source_id": "ULS-S019"
  },
  {
    "record_key": "uls-scholarship-3",
    "academic_year": "2025-2026",
    "name": "Presidential Scholarship",
    "description": "Presidential Scholarship",
    "coverage": null,
    "amount": null,
    "currency": null,
    "notes": "ULS publishes scholarship and merit-based support options on its financial-support page.",
    "source_id": "ULS-S019"
  },
  {
    "record_key": "uls-scholarship-4",
    "academic_year": "2025-2026",
    "name": "Third Party Scholarship",
    "description": "Third Party Scholarship",
    "coverage": null,
    "amount": null,
    "currency": null,
    "notes": "ULS publishes scholarship and merit-based support options on its financial-support page.",
    "source_id": "ULS-S019"
  }
]
$ULS_SCHOLARSHIPS$::jsonb) AS x(record_key TEXT, academic_year TEXT, name TEXT, description TEXT, coverage TEXT, amount NUMERIC(12, 2), currency TEXT, notes TEXT, source_id TEXT);

    INSERT INTO graduate_scholarship (
        university_id, faculty_id, department_id, program_id, scope_level, record_key, academic_year, name, description, coverage, amount, currency, notes, source_id
    )
    SELECT
        v_university_id, NULL, NULL, NULL, 'UNIVERSITY', s.record_key, s.academic_year, s.name, s.description, s.coverage, s.amount, s.currency, s.notes, src.id
    FROM uls_scholarship_seed s
    JOIN LATERAL (
        SELECT src.id
        FROM uls_source_seed us
        JOIN source src ON src.university_id = v_university_id AND src.url = us.url
        WHERE us.source_id = s.source_id
        LIMIT 1
    ) src ON TRUE
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

    CREATE TEMP TABLE uls_financial_aid_seed (
        record_key TEXT PRIMARY KEY,
        academic_year TEXT,
        name TEXT NOT NULL,
        description TEXT,
        amount NUMERIC(12, 2),
        currency TEXT,
        notes TEXT,
        source_id TEXT NOT NULL
    ) ON COMMIT DROP;

    INSERT INTO uls_financial_aid_seed (record_key, academic_year, name, description, amount, currency, notes, source_id)
    SELECT record_key, academic_year, name, description, amount, currency, notes, source_id FROM jsonb_to_recordset($ULS_FIN_AID$
[
  {
    "record_key": "uls-fin-aid-1",
    "academic_year": "2025-2026",
    "name": "Siblings Aid",
    "description": "Siblings Aid",
    "amount": null,
    "currency": null,
    "notes": "ULS publishes financial support beyond scholarships, including sibling aid, student work, and La Sagesse network support.",
    "source_id": "ULS-S019"
  },
  {
    "record_key": "uls-fin-aid-2",
    "academic_year": "2025-2026",
    "name": "Student Work",
    "description": "Student Work",
    "amount": null,
    "currency": null,
    "notes": "ULS publishes financial support beyond scholarships, including sibling aid, student work, and La Sagesse network support.",
    "source_id": "ULS-S019"
  },
  {
    "record_key": "uls-fin-aid-3",
    "academic_year": "2025-2026",
    "name": "La Sagesse Schools support",
    "description": "La Sagesse Schools support",
    "amount": null,
    "currency": null,
    "notes": "ULS publishes financial support beyond scholarships, including sibling aid, student work, and La Sagesse network support.",
    "source_id": "ULS-S019"
  },
  {
    "record_key": "uls-fin-aid-4",
    "academic_year": "2025-2026",
    "name": "La Sagesse Network Protocol support",
    "description": "La Sagesse Network Protocol support",
    "amount": null,
    "currency": null,
    "notes": "ULS publishes financial support beyond scholarships, including sibling aid, student work, and La Sagesse network support.",
    "source_id": "ULS-S019"
  }
]
$ULS_FIN_AID$::jsonb) AS x(record_key TEXT, academic_year TEXT, name TEXT, description TEXT, amount NUMERIC(12, 2), currency TEXT, notes TEXT, source_id TEXT);

    INSERT INTO graduate_financial_aid (
        university_id, faculty_id, department_id, program_id, scope_level, record_key, academic_year, name, description, amount, currency, notes, source_id
    )
    SELECT
        v_university_id, NULL, NULL, NULL, 'UNIVERSITY', f.record_key, f.academic_year, f.name, f.description, f.amount, f.currency, f.notes, src.id
    FROM uls_financial_aid_seed f
    JOIN LATERAL (
        SELECT src.id
        FROM uls_source_seed us
        JOIN source src ON src.university_id = v_university_id AND src.url = us.url
        WHERE us.source_id = f.source_id
        LIMIT 1
    ) src ON TRUE
    ON CONFLICT (university_id, record_key) DO UPDATE SET
        academic_year = EXCLUDED.academic_year,
        name = EXCLUDED.name,
        description = EXCLUDED.description,
        amount = EXCLUDED.amount,
        currency = EXCLUDED.currency,
        notes = EXCLUDED.notes,
        source_id = EXCLUDED.source_id,
        updated_at = NOW();

    CREATE TEMP TABLE uls_payment_plan_seed (
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

    INSERT INTO uls_payment_plan_seed (record_key, academic_year, name, description, installments_count, down_payment_amount, down_payment_currency, interval_label, notes, source_id)
    SELECT record_key, academic_year, name, description, installments_count, down_payment_amount, down_payment_currency, interval_label, notes, source_id FROM jsonb_to_recordset($ULS_PAYMENT_PLANS$
[
  {
    "record_key": "uls-payment-plan-none-published",
    "academic_year": "2025-2026",
    "name": "No formal graduate installment schedule published",
    "description": "No formal graduate installment schedule was recovered in the inspected official sources.",
    "installments_count": null,
    "down_payment_amount": null,
    "down_payment_currency": null,
    "interval_label": null,
    "notes": "No formal graduate installment plan was recovered from the inspected official pages.",
    "source_id": "ULS-S018"
  }
]
$ULS_PAYMENT_PLANS$::jsonb) AS x(record_key TEXT, academic_year TEXT, name TEXT, description TEXT, installments_count INTEGER, down_payment_amount NUMERIC(12, 2), down_payment_currency TEXT, interval_label TEXT, notes TEXT, source_id TEXT);

    INSERT INTO graduate_payment_plan (
        university_id, faculty_id, department_id, program_id, scope_level, record_key, academic_year, name, description, installments_count, down_payment_amount, down_payment_currency, interval_label, notes, source_id
    )
    SELECT
        v_university_id, NULL, NULL, NULL, 'UNIVERSITY', p.record_key, p.academic_year, p.name, p.description, p.installments_count, p.down_payment_amount, p.down_payment_currency, p.interval_label, p.notes, src.id
    FROM uls_payment_plan_seed p
    JOIN LATERAL (
        SELECT src.id
        FROM uls_source_seed us
        JOIN source src ON src.university_id = v_university_id AND src.url = us.url
        WHERE us.source_id = p.source_id
        LIMIT 1
    ) src ON TRUE
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

    CREATE TEMP TABLE uls_accreditation_seed (
        record_key TEXT PRIMARY KEY,
        faculty_name TEXT,
        scope_level TEXT NOT NULL,
        name TEXT NOT NULL,
        authority TEXT,
        status TEXT,
        notes TEXT,
        source_id TEXT NOT NULL
    ) ON COMMIT DROP;

    INSERT INTO uls_accreditation_seed (record_key, faculty_name, scope_level, name, authority, status, notes, source_id)
    SELECT record_key, faculty_name, scope_level, name, authority, status, notes, source_id FROM jsonb_to_recordset($ULS_ACCREDITATION$
[
  {
    "record_key": "uls-accreditation-law-fibaa",
    "faculty_name": "Faculty of Law",
    "scope_level": "FACULTY",
    "name": "Faculty of Law accreditation",
    "authority": "FIBAA",
    "status": "Accredited",
    "notes": "Faculty of Law accredited by FIBAA",
    "source_id": "ULS-S003"
  },
  {
    "record_key": "uls-accreditation-feba-fibaa",
    "faculty_name": "Faculty of Economics and Business Administration",
    "scope_level": "FACULTY",
    "name": "Faculty of Economics and Business Administration accreditation",
    "authority": "FIBAA",
    "status": "Accredited",
    "notes": "Faculty of Economics and Business Administration accredited by FIBAA",
    "source_id": "ULS-S004"
  },
  {
    "record_key": "uls-accreditation-tourism-ehl",
    "faculty_name": "Faculty of Tourism and Hotel Management",
    "scope_level": "FACULTY",
    "name": "Faculty of Tourism and Hotel Management certification",
    "authority": "École Hôtelière de Lausanne",
    "status": "Certified",
    "notes": "Faculty of Tourism and Hotel Management has Academic Certification of École Hôtelière de Lausanne",
    "source_id": "ULS-S010"
  }
]
$ULS_ACCREDITATION$::jsonb) AS x(record_key TEXT, faculty_name TEXT, scope_level TEXT, name TEXT, authority TEXT, status TEXT, notes TEXT, source_id TEXT);

    INSERT INTO graduate_accreditation (
        university_id, faculty_id, department_id, program_id, scope_level, record_key, name, authority, status, valid_from, valid_until, notes, source_id
    )
    SELECT
        v_university_id, f.id, NULL, NULL, a.scope_level, a.record_key, a.name, a.authority, a.status, NULL, NULL, a.notes, src.id
    FROM uls_accreditation_seed a
    JOIN university_faculty f
      ON f.university_id = v_university_id
     AND f.name = a.faculty_name
    JOIN LATERAL (
        SELECT src.id
        FROM uls_source_seed us
        JOIN source src ON src.university_id = v_university_id AND src.url = us.url
        WHERE us.source_id = a.source_id
        LIMIT 1
    ) src ON TRUE
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

    CREATE TEMP TABLE uls_alias_seed (
        program_key TEXT NOT NULL,
        alias_type TEXT NOT NULL,
        alias TEXT NOT NULL,
        source_id TEXT NOT NULL,
        note TEXT
    ) ON COMMIT DROP;

    INSERT INTO uls_alias_seed (program_key, alias_type, alias, source_id, note)
    SELECT program_key, alias_type, alias, source_id, note FROM jsonb_to_recordset($ULS_ALIASES$
[
  {
    "program_key": "uls-feba-master-business-administration-finance-mba",
    "alias_type": "DISPLAY_NAME",
    "alias": "Business Administration",
    "source_id": "ULS-S004",
    "note": "Official page heading for the MBA."
  },
  {
    "program_key": "uls-feba-master-business-administration-finance-msc",
    "alias_type": "DISPLAY_NAME",
    "alias": "Business Administration",
    "source_id": "ULS-S004",
    "note": "Official page heading for the MSc."
  },
  {
    "program_key": "uls-religious-master-ecclesiastical-sciences",
    "alias_type": "DISPLAY_NAME",
    "alias": "Ecclesiastical Sciences",
    "source_id": "ULS-S011",
    "note": "Official page heading for the master."
  },
  {
    "program_key": "uls-polisci-master-political-science-international-relations",
    "alias_type": "DISPLAY_NAME",
    "alias": "Political Science and International Relations",
    "source_id": "ULS-S008",
    "note": "Official page heading for the master."
  }
]
$ULS_ALIASES$::jsonb) AS x(program_key TEXT, alias_type TEXT, alias TEXT, source_id TEXT, note TEXT);

    INSERT INTO graduate_program_alias (
        university_id, program_id, alias_type, alias, source_id, note
    )
    SELECT
        v_university_id, gp.id, a.alias_type, a.alias, src.id, a.note
    FROM uls_alias_seed a
    JOIN graduate_program gp
      ON gp.university_id = v_university_id
     AND gp.program_key = a.program_key
    JOIN LATERAL (
        SELECT src.id
        FROM uls_source_seed us
        JOIN source src ON src.university_id = v_university_id AND src.url = us.url
        WHERE us.source_id = a.source_id
        LIMIT 1
    ) src ON TRUE
    ON CONFLICT (university_id, alias_type, alias) DO UPDATE SET
        program_id = EXCLUDED.program_id,
        source_id = EXCLUDED.source_id,
        note = EXCLUDED.note,
        updated_at = NOW();

END $$;
