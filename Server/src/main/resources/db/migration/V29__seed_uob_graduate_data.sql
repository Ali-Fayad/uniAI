-- UOB graduate data seed migration.
-- Idempotent import for the canonical UOB graduate dataset.

-- UOB reuses hub/catalog URLs across multiple graduate programs.
ALTER TABLE graduate_program DROP CONSTRAINT IF EXISTS uq_graduate_program_university_url;

DO $$
DECLARE
    v_university_id BIGINT;
BEGIN

    INSERT INTO university (name, name_ar, acronym, country, city, latitude, longitude, campus_name, campus_type)
    SELECT 'University of Balamand', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL
    WHERE NOT EXISTS (
        SELECT 1 FROM university WHERE name = 'University of Balamand'
    );

    SELECT id INTO v_university_id
    FROM university
    WHERE name = 'University of Balamand'
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
        ('French', 'fr', 'Français'),
        ('Arabic', 'ar', 'العربية'),
        ('Multilingual', 'multi', 'Multilingual')
    ON CONFLICT (code) DO UPDATE SET
        name = EXCLUDED.name,
        native_name = EXCLUDED.native_name;

    CREATE TEMP TABLE uob_source_seed (
        source_id TEXT PRIMARY KEY,
        page_title TEXT NOT NULL,
        url TEXT NOT NULL,
        date_accessed DATE,
        source_type TEXT,
        notes TEXT
    ) ON COMMIT DROP;

    INSERT INTO uob_source_seed (source_id, page_title, url, date_accessed, source_type, notes)
    SELECT source_id, page_title, url, date_accessed, source_type, notes
    FROM jsonb_to_recordset($UOB_SRC$[
  {
    "source_id": "uob_home",
    "page_title": "University of Balamand Home",
    "url": "https://www.balamand.edu.lb/",
    "date_accessed": "2026-06-28",
    "source_type": "OTHER",
    "notes": "University home page; graduate links are exposed from the main navigation."
  },
  {
    "source_id": "uob_grad_admissions",
    "page_title": "Graduate Admissions",
    "url": "https://www.balamand.edu.lb/ProspectiveStudents/Pages/GraduateAdmissions.aspx",
    "date_accessed": "2026-06-28",
    "source_type": "ADMISSIONS",
    "notes": "Central graduate admissions page; confirms graduate study and links to admissions criteria, tuition, financial support, and the graduate catalogue."
  },
  {
    "source_id": "uob_admission_guide_page",
    "page_title": "Admission Guide",
    "url": "https://www.balamand.edu.lb/ProspectiveStudents/AdmissionGuide/Pages/default.aspx",
    "date_accessed": "2026-06-28",
    "source_type": "ADMISSIONS",
    "notes": "Admissions guide landing page surfaced from the graduate admissions page."
  },
  {
    "source_id": "uob_admission_guide_pdf",
    "page_title": "Admission Guide PDF",
    "url": "https://www.balamand.edu.lb/Style%20Library/PDFs/ProspectiveStudents/AdmissionGuide.pdf",
    "date_accessed": "2026-06-28",
    "source_type": "PDF",
    "notes": "Official admissions guide PDF; used as a reference source for graduate admissions and applicant checklist context."
  },
  {
    "source_id": "uob_fees_expenses_pdf",
    "page_title": "Fees and Expenses",
    "url": "https://www.balamand.edu.lb/Style%20Library/PDFs/CurrentStudents/FeesExpenses.pdf",
    "date_accessed": "2026-06-28",
    "source_type": "TUITION_FEES",
    "notes": "Official university tuition/fees PDF; graduate application fee, graduate per-credit tuition, special yearly tuition rows, and common fees are listed here."
  },
  {
    "source_id": "uob_financial_support",
    "page_title": "Financial Support",
    "url": "https://www.balamand.edu.lb/CurrentStudents/FinancialSupport/Pages/FinancialSupport.aspx",
    "date_accessed": "2026-06-28",
    "source_type": "FINANCIAL_AID",
    "notes": "University-wide financial aid and scholarship hub; includes graduate assistantship references."
  },
  {
    "source_id": "uob_academic_calendar",
    "page_title": "Academic Calendar",
    "url": "https://www.balamand.edu.lb/CurrentStudents/Pages/AcademicCalendar.aspx",
    "date_accessed": "2026-06-28",
    "source_type": "DEADLINE",
    "notes": "University academic calendar; relevant for graduate deadline discovery."
  },
  {
    "source_id": "uob_grading_policy_faq",
    "page_title": "Grading Policy FAQs",
    "url": "https://www.balamand.edu.lb/CurrentStudents/Pages/GradingPolicyFAQ.aspx",
    "date_accessed": "2026-06-28",
    "source_type": "OTHER",
    "notes": "Official graduate grading and policy reference."
  },
  {
    "source_id": "uob_catalogue_page",
    "page_title": "Catalogue",
    "url": "https://www.balamand.edu.lb/CurrentStudents/Pages/Catalogue.aspx",
    "date_accessed": "2026-06-28",
    "source_type": "CATALOG",
    "notes": "Catalogue landing page with graduate sections by faculty and institute."
  },
  {
    "source_id": "uob_general_graduate_catalogue_pdf",
    "page_title": "General Section Graduate Catalogue",
    "url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/General-Section-Graduate.pdf",
    "date_accessed": "2026-06-28",
    "source_type": "CATALOG",
    "notes": "Official general graduate catalogue section. Confirms graduate masters and doctorate offerings across the university."
  },
  {
    "source_id": "uob_fas",
    "page_title": "Faculty of Arts and Sciences",
    "url": "https://www.balamand.edu.lb/faculties/FAS/Pages/default.aspx",
    "date_accessed": "2026-06-28",
    "source_type": "SCHOOL_PAGE",
    "notes": "Faculty hub page showing graduate arts and sciences offerings and linking to the FAS graduate catalogue."
  },
  {
    "source_id": "uob_fas_graduate_catalogue_pdf",
    "page_title": "Faculty of Arts and Sciences Graduate Catalogue",
    "url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/FASGraduate.pdf",
    "date_accessed": "2026-06-28",
    "source_type": "CATALOG",
    "notes": "Faculty graduate catalogue PDF; detailed graduate offerings for arts and sciences."
  },
  {
    "source_id": "uob_fas_food_science",
    "page_title": "Graduate Program in Food Science and Technology",
    "url": "https://www.balamand.edu.lb/faculties/FAS/Departments/Pages/FoodScience.aspx",
    "date_accessed": "2026-06-28",
    "source_type": "PROGRAM_PAGE",
    "notes": "Official program page under FAS."
  },
  {
    "source_id": "uob_fobm",
    "page_title": "Faculty of Business and Management",
    "url": "https://www.balamand.edu.lb/faculties/FOBM/Pages/default.aspx",
    "date_accessed": "2026-06-28",
    "source_type": "SCHOOL_PAGE",
    "notes": "Faculty hub page showing MBA and EMBA graduate offerings."
  },
  {
    "source_id": "uob_fobm_mba",
    "page_title": "Master of Business Administration (MBA)",
    "url": "https://www.balamand.edu.lb/faculties/FOBM/Departments/Pages/MBA.aspx",
    "date_accessed": "2026-06-28",
    "source_type": "PROGRAM_PAGE",
    "notes": "Official MBA page; includes duration, credits, and concentration tracks."
  },
  {
    "source_id": "uob_fobm_emba",
    "page_title": "Executive Master of Business Administration (EMBA)",
    "url": "https://www.balamand.edu.lb/faculties/FOBM/Departments/Pages/EMBA.aspx",
    "date_accessed": "2026-06-28",
    "source_type": "PROGRAM_PAGE",
    "notes": "Official EMBA page; executive graduate offering."
  },
  {
    "source_id": "uob_foe",
    "page_title": "Faculty of Engineering",
    "url": "https://www.balamand.edu.lb/faculties/FOE/Pages/default.aspx",
    "date_accessed": "2026-06-28",
    "source_type": "SCHOOL_PAGE",
    "notes": "Faculty hub page. Graduate offerings are referenced via the general graduate catalogue."
  },
  {
    "source_id": "uob_fhs",
    "page_title": "Faculty of Health Sciences",
    "url": "https://www.balamand.edu.lb/faculties/FHS/Pages/Default.aspx",
    "date_accessed": "2026-06-28",
    "source_type": "SCHOOL_PAGE",
    "notes": "Faculty hub page showing graduate offerings in public health, clinical laboratory sciences, and nursing."
  },
  {
    "source_id": "uob_fhs_graduate_catalogue_pdf",
    "page_title": "Faculty of Health Sciences Graduate Catalogue",
    "url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/FHSGraduate.pdf",
    "date_accessed": "2026-06-28",
    "source_type": "CATALOG",
    "notes": "Faculty graduate catalogue PDF."
  },
  {
    "source_id": "uob_fhs_mph",
    "page_title": "Master of Public Health (MPH)",
    "url": "https://www.balamand.edu.lb/faculties/FHS/AcademicPrograms/Pages/Programs/MSPublicHealth.aspx",
    "date_accessed": "2026-06-28",
    "source_type": "PROGRAM_PAGE",
    "notes": "Official MPH page."
  },
  {
    "source_id": "uob_fhs_ms_cls",
    "page_title": "MS in Clinical Laboratory Sciences",
    "url": "https://www.balamand.edu.lb/faculties/FHS/AcademicPrograms/Pages/Programs/MSClinicalLabSciences.aspx",
    "date_accessed": "2026-06-28",
    "source_type": "PROGRAM_PAGE",
    "notes": "Official graduate program page."
  },
  {
    "source_id": "uob_fhs_ms_nursing",
    "page_title": "MS in Nursing",
    "url": "https://www.balamand.edu.lb/faculties/FHS/AcademicPrograms/Pages/Programs/MSNursing.aspx",
    "date_accessed": "2026-06-28",
    "source_type": "PROGRAM_PAGE",
    "notes": "Official graduate nursing page."
  },
  {
    "source_id": "uob_fom",
    "page_title": "Faculty of Medicine and Medical Sciences",
    "url": "https://www.balamand.edu.lb/faculties/FOM/Pages/default.aspx",
    "date_accessed": "2026-06-28",
    "source_type": "SCHOOL_PAGE",
    "notes": "Faculty hub page showing the MS in Biomedical Sciences and graduate admissions links."
  },
  {
    "source_id": "uob_fom_ms_biomedical",
    "page_title": "MS in Biomedical Sciences",
    "url": "https://www.balamand.edu.lb/faculties/FOM/AcademicPrograms/Pages/MSBiomedical.aspx",
    "date_accessed": "2026-06-28",
    "source_type": "PROGRAM_PAGE",
    "notes": "Official biomedical sciences graduate program page."
  },
  {
    "source_id": "uob_fom_admissions",
    "page_title": "Faculty of Medicine Admissions",
    "url": "https://www.balamand.edu.lb/faculties/FOM/AcademicPrograms/Pages/Admissions.aspx",
    "date_accessed": "2026-06-28",
    "source_type": "ADMISSIONS",
    "notes": "Faculty-specific admissions page for medical and graduate entry points."
  },
  {
    "source_id": "uob_alba",
    "page_title": "Académie Libanaise des Beaux-Arts",
    "url": "https://www.balamand.edu.lb/faculties/ALBA/Pages/default.aspx",
    "date_accessed": "2026-06-28",
    "source_type": "SCHOOL_PAGE",
    "notes": "ALBA faculty hub page showing graduate admission link and graduate catalogue access."
  },
  {
    "source_id": "uob_alba_graduate_catalogue_pdf",
    "page_title": "ALBA Graduate Catalogue",
    "url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/ALBAGraduate.pdf",
    "date_accessed": "2026-06-28",
    "source_type": "CATALOG",
    "notes": "ALBA graduate catalogue PDF."
  },
  {
    "source_id": "uob_theology",
    "page_title": "Saint John of Damascus Institute of Theology",
    "url": "https://theology.balamand.edu.lb/",
    "date_accessed": "2026-06-28",
    "source_type": "SCHOOL_PAGE",
    "notes": "Official theology institute domain surfaced from UOB navigation; direct fetch timed out during discovery."
  }
]$UOB_SRC$::jsonb) AS src(
        source_id TEXT,
        page_title TEXT,
        url TEXT,
        date_accessed DATE,
        source_type TEXT,
        notes TEXT
    );

    INSERT INTO source (university_id, title, url, source_type, accessed_at, notes)
    SELECT v_university_id, page_title, url, source_type, date_accessed, notes
    FROM uob_source_seed
    ON CONFLICT (university_id, url) DO UPDATE SET
        title = EXCLUDED.title,
        source_type = EXCLUDED.source_type,
        accessed_at = EXCLUDED.accessed_at,
        notes = EXCLUDED.notes,
        updated_at = NOW();

    INSERT INTO university_faculty (university_id, name, short_name, faculty_type, official_url, notes)
    SELECT v_university_id, name, short_name, faculty_type, official_url, notes
    FROM jsonb_to_recordset($UOB_FAC$[
  {
    "name": "Académie Libanaise des Beaux-Arts - Al-Kurah Campus",
    "short_name": "ALBA",
    "faculty_type": "FACULTY",
    "official_url": "https://www.balamand.edu.lb/faculties/ALBA/Pages/default.aspx",
    "notes": "Graduate offerings for the Al-Kurah campus are represented through the ALBA hub and graduate catalogue."
  },
  {
    "name": "Académie Libanaise des Beaux-Arts - Dekouaneh Campus",
    "short_name": "ALBA",
    "faculty_type": "FACULTY",
    "official_url": "https://www.balamand.edu.lb/faculties/ALBA/Pages/default.aspx",
    "notes": "Graduate offerings for the Dekouaneh campus are represented through the ALBA hub and graduate catalogue."
  },
  {
    "name": "Faculty of Arts and Sciences - Al-Kurah Campus",
    "short_name": "FAS",
    "faculty_type": "FACULTY",
    "official_url": "https://www.balamand.edu.lb/faculties/FAS/Pages/default.aspx",
    "notes": "Graduate offerings are routed through the FAS faculty hub and graduate catalogue."
  },
  {
    "name": "Faculty of Arts and Sciences - Souk El Gharb-Aley Campus",
    "short_name": "FAS",
    "faculty_type": "FACULTY",
    "official_url": "https://www.balamand.edu.lb/faculties/FAS/Pages/default.aspx",
    "notes": "Graduate offerings are routed through the FAS faculty hub and graduate catalogue."
  },
  {
    "name": "Faculty of Business and Management - Al-Kurah Campus",
    "short_name": "FOBM",
    "faculty_type": "FACULTY",
    "official_url": "https://www.balamand.edu.lb/faculties/FOBM/Pages/default.aspx",
    "notes": "Graduate offerings are routed through the Faculty of Business and Management hub."
  },
  {
    "name": "Faculty of Engineering - Al-Kurah Campus",
    "short_name": "FOE",
    "faculty_type": "FACULTY",
    "official_url": "https://www.balamand.edu.lb/faculties/FOE/Pages/default.aspx",
    "notes": "Graduate offerings are routed through the Faculty of Engineering hub and graduate catalogue."
  },
  {
    "name": "Faculty of Health Sciences - Dekouaneh Campus",
    "short_name": "FHS",
    "faculty_type": "FACULTY",
    "official_url": "https://www.balamand.edu.lb/faculties/FHS/Pages/Default.aspx",
    "notes": "Graduate offerings are routed through the Faculty of Health Sciences hub and graduate catalogue."
  },
  {
    "name": "Faculty of Medicine and Medical Sciences - Al-Kurah Campus",
    "short_name": "FOM",
    "faculty_type": "FACULTY",
    "official_url": "https://www.balamand.edu.lb/faculties/FOM/Pages/default.aspx",
    "notes": "Graduate offerings are routed through the Faculty of Medicine and Medical Sciences hub."
  },
  {
    "name": "Saint John of Damascus Institute of Theology - Al-Kurah Campus",
    "short_name": "Theology",
    "faculty_type": "INSTITUTE",
    "official_url": "https://theology.balamand.edu.lb/",
    "notes": "Graduate offerings are routed through the theology institute domain and graduate catalogue."
  }
]$UOB_FAC$::jsonb) AS fac(
        name TEXT,
        short_name TEXT,
        faculty_type TEXT,
        official_url TEXT,
        notes TEXT
    )
    ON CONFLICT (university_id, name) DO UPDATE SET
        short_name = EXCLUDED.short_name,
        faculty_type = EXCLUDED.faculty_type,
        official_url = EXCLUDED.official_url,
        notes = EXCLUDED.notes,
        updated_at = NOW();

    INSERT INTO university_department (university_id, faculty_id, name, short_name, official_url, notes)
    SELECT v_university_id, fac.id, dep.name, dep.short_name, dep.official_url, dep.notes
    FROM jsonb_to_recordset($UOB_DEP$[
  {
    "faculty_name": "Faculty of Arts and Sciences - Al-Kurah Campus",
    "name": "Department of Arabic Language and Literature",
    "short_name": null,
    "official_url": null,
    "notes": null
  },
  {
    "faculty_name": "Faculty of Arts and Sciences - Al-Kurah Campus",
    "name": "Department of Biology",
    "short_name": null,
    "official_url": null,
    "notes": null
  },
  {
    "faculty_name": "Faculty of Arts and Sciences - Al-Kurah Campus",
    "name": "Department of Chemistry",
    "short_name": null,
    "official_url": null,
    "notes": null
  },
  {
    "faculty_name": "Faculty of Arts and Sciences - Al-Kurah Campus",
    "name": "Department of Christian Muslim Studies",
    "short_name": null,
    "official_url": null,
    "notes": null
  },
  {
    "faculty_name": "Faculty of Arts and Sciences - Al-Kurah Campus",
    "name": "Department of Computer Science",
    "short_name": null,
    "official_url": null,
    "notes": null
  },
  {
    "faculty_name": "Faculty of Arts and Sciences - Al-Kurah Campus",
    "name": "Department of Education",
    "short_name": null,
    "official_url": null,
    "notes": null
  },
  {
    "faculty_name": "Faculty of Arts and Sciences - Al-Kurah Campus",
    "name": "Department of English Language and Literature",
    "short_name": null,
    "official_url": null,
    "notes": null
  },
  {
    "faculty_name": "Faculty of Arts and Sciences - Al-Kurah Campus",
    "name": "Department of Environmental Sciences",
    "short_name": null,
    "official_url": null,
    "notes": null
  },
  {
    "faculty_name": "Faculty of Arts and Sciences - Al-Kurah Campus",
    "name": "Graduate Program in Food Science and Technology",
    "short_name": null,
    "official_url": null,
    "notes": null
  },
  {
    "faculty_name": "Faculty of Arts and Sciences - Al-Kurah Campus",
    "name": "Department of French Language and Literature",
    "short_name": null,
    "official_url": null,
    "notes": null
  },
  {
    "faculty_name": "Faculty of Arts and Sciences - Al-Kurah Campus",
    "name": "Department of History",
    "short_name": null,
    "official_url": null,
    "notes": null
  },
  {
    "faculty_name": "Faculty of Arts and Sciences - Al-Kurah Campus",
    "name": "Department of Languages and Translation",
    "short_name": null,
    "official_url": null,
    "notes": null
  },
  {
    "faculty_name": "Faculty of Arts and Sciences - Al-Kurah Campus",
    "name": "Department of Mass Media and Communication",
    "short_name": null,
    "official_url": null,
    "notes": null
  },
  {
    "faculty_name": "Faculty of Arts and Sciences - Al-Kurah Campus",
    "name": "Department of Mathematics",
    "short_name": null,
    "official_url": null,
    "notes": null
  },
  {
    "faculty_name": "Faculty of Arts and Sciences - Al-Kurah Campus",
    "name": "Department of Philosophy",
    "short_name": null,
    "official_url": null,
    "notes": null
  },
  {
    "faculty_name": "Faculty of Arts and Sciences - Al-Kurah Campus",
    "name": "Department of Physical Education",
    "short_name": null,
    "official_url": null,
    "notes": null
  },
  {
    "faculty_name": "Faculty of Arts and Sciences - Al-Kurah Campus",
    "name": "Department of Political Science and International Affairs",
    "short_name": null,
    "official_url": null,
    "notes": null
  },
  {
    "faculty_name": "Faculty of Arts and Sciences - Al-Kurah Campus",
    "name": "Department of Psychology",
    "short_name": null,
    "official_url": null,
    "notes": null
  },
  {
    "faculty_name": "Faculty of Arts and Sciences - Souk El Gharb-Aley Campus",
    "name": "Department of Education",
    "short_name": null,
    "official_url": null,
    "notes": null
  },
  {
    "faculty_name": "Faculty of Health Sciences - Dekouaneh Campus",
    "name": "Department of Medical Laboratory Sciences",
    "short_name": null,
    "official_url": null,
    "notes": null
  },
  {
    "faculty_name": "Faculty of Health Sciences - Dekouaneh Campus",
    "name": "Department of Nursing",
    "short_name": null,
    "official_url": null,
    "notes": null
  },
  {
    "faculty_name": "Faculty of Health Sciences - Dekouaneh Campus",
    "name": "Department of Public Health",
    "short_name": null,
    "official_url": null,
    "notes": null
  },
  {
    "faculty_name": "Faculty of Medicine and Medical Sciences - Al-Kurah Campus",
    "name": "Department of Biomedical Sciences",
    "short_name": null,
    "official_url": null,
    "notes": null
  }
]$UOB_DEP$::jsonb) AS dep(
        faculty_name TEXT,
        name TEXT,
        short_name TEXT,
        official_url TEXT,
        notes TEXT
    )
    JOIN university_faculty fac
      ON fac.university_id = v_university_id
     AND fac.name = dep.faculty_name
    ON CONFLICT (university_id, faculty_id, name) DO UPDATE SET
        short_name = EXCLUDED.short_name,
        official_url = EXCLUDED.official_url,
        notes = EXCLUDED.notes,
        updated_at = NOW();

    CREATE TEMP TABLE uob_program_seed (
        id TEXT PRIMARY KEY,
        faculty TEXT NOT NULL,
        department TEXT,
        major_category TEXT,
        major TEXT,
        degree_type TEXT NOT NULL,
        official_degree_name TEXT,
        thesis_or_non_thesis TEXT,
        concentrations_or_tracks JSONB,
        credits INTEGER,
        duration_value NUMERIC(10,2),
        duration_unit TEXT,
        duration_raw_text TEXT,
        language TEXT,
        delivery_mode TEXT,
        program_description TEXT,
        admission_requirements TEXT,
        gre_requirement TEXT,
        gmat_requirement TEXT,
        english_requirement TEXT,
        interview_requirement TEXT,
        experience_requirement TEXT,
        accreditation TEXT,
        official_program_url TEXT,
        sources JSONB,
        tuition JSONB,
        notes TEXT
    ) ON COMMIT DROP;

    INSERT INTO uob_program_seed (
        id, faculty, department, major_category, major, degree_type, official_degree_name,
        thesis_or_non_thesis, concentrations_or_tracks, credits, duration_value, duration_unit,
        duration_raw_text, language, delivery_mode, program_description, admission_requirements,
        gre_requirement, gmat_requirement, english_requirement, interview_requirement,
        experience_requirement, accreditation, official_program_url, sources, tuition, notes
    )
    SELECT
        id, faculty, department, major_category, major, degree_type, official_degree_name,
        thesis_or_non_thesis, concentrations_or_tracks, credits, duration_value, duration_unit,
        duration_raw_text, language, delivery_mode, program_description, admission_requirements,
        gre_requirement, gmat_requirement, english_requirement, interview_requirement,
        experience_requirement, accreditation, official_program_url, sources, tuition, notes
    FROM jsonb_to_recordset($UOB_PROG$[
  {
    "id": "uob-alba-master-architecture-march",
    "faculty": "Académie Libanaise des Beaux-Arts - Al-Kurah Campus",
    "department": null,
    "major_category": "Architecture",
    "major": "Architecture",
    "degree_type": "MASTER",
    "official_degree_name": "Architecture M.Arch.",
    "thesis_or_non_thesis": null,
    "concentrations_or_tracks": [],
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": "MULTILINGUAL",
    "delivery_mode": null,
    "program_description": "Professional graduate study in architecture or interior architecture that emphasizes advanced design practice, project development, and jury-reviewed studio work.",
    "admission_requirements": null,
    "gre_requirement": null,
    "gmat_requirement": null,
    "english_requirement": null,
    "interview_requirement": null,
    "experience_requirement": null,
    "accreditation": null,
    "official_program_url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/ALBAGraduate.pdf",
    "sources": [
      {
        "source_id": "uob_alba_graduate_catalogue_pdf",
        "page_title": "ALBA Graduate Catalogue",
        "url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/ALBAGraduate.pdf",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_catalogue_page",
        "page_title": "Catalogue",
        "url": "https://www.balamand.edu.lb/CurrentStudents/Pages/Catalogue.aspx",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_general_graduate_catalogue_pdf",
        "page_title": "General Section Graduate Catalogue",
        "url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/General-Section-Graduate.pdf",
        "date_accessed": "2026-06-28"
      }
    ],
    "tuition": {
      "academic_year": "2025-2026",
      "currency": "USD",
      "billing_basis": "PER_CREDIT",
      "amount": 510,
      "category": "Académie Libanaise des Beaux-Arts - Al-Kurah Campus",
      "notes": "Official UOB fee table lists ALBA - Balamand / Al-Kurah Campus graduate tuition (2025-2026).",
      "source_ids": [
        "uob_fees_expenses_pdf"
      ]
    },
    "notes": "Listed in the ALBA graduate catalogue under the Al-Kurah Campus; no individual program page surfaced during discovery."
  },
  {
    "id": "uob-alba-master-computer-graphics-interactive-media-mfa",
    "faculty": "Académie Libanaise des Beaux-Arts - Al-Kurah Campus",
    "department": null,
    "major_category": "Design",
    "major": "Computer Graphics and Interactive Media",
    "degree_type": "MASTER",
    "official_degree_name": "Computer Graphics and Interactive Media M.F.A.",
    "thesis_or_non_thesis": null,
    "concentrations_or_tracks": [],
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": "MULTILINGUAL",
    "delivery_mode": null,
    "program_description": "Graduate study in computer graphics and interactive media centered on creativity, digital tools, and self-directed multimedia projects.",
    "admission_requirements": null,
    "gre_requirement": null,
    "gmat_requirement": null,
    "english_requirement": null,
    "interview_requirement": null,
    "experience_requirement": null,
    "accreditation": null,
    "official_program_url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/ALBAGraduate.pdf",
    "sources": [
      {
        "source_id": "uob_alba_graduate_catalogue_pdf",
        "page_title": "ALBA Graduate Catalogue",
        "url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/ALBAGraduate.pdf",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_catalogue_page",
        "page_title": "Catalogue",
        "url": "https://www.balamand.edu.lb/CurrentStudents/Pages/Catalogue.aspx",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_general_graduate_catalogue_pdf",
        "page_title": "General Section Graduate Catalogue",
        "url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/General-Section-Graduate.pdf",
        "date_accessed": "2026-06-28"
      }
    ],
    "tuition": {
      "academic_year": "2025-2026",
      "currency": "USD",
      "billing_basis": "PER_CREDIT",
      "amount": 510,
      "category": "Académie Libanaise des Beaux-Arts - Al-Kurah Campus",
      "notes": "Official UOB fee table lists ALBA - Balamand / Al-Kurah Campus graduate tuition (2025-2026).",
      "source_ids": [
        "uob_fees_expenses_pdf"
      ]
    },
    "notes": "Listed in the ALBA graduate catalogue under the Al-Kurah Campus; no individual program page surfaced during discovery."
  },
  {
    "id": "uob-alba-master-graphic-design-mfa",
    "faculty": "Académie Libanaise des Beaux-Arts - Al-Kurah Campus",
    "department": null,
    "major_category": "Design",
    "major": "Graphic Design",
    "degree_type": "MASTER",
    "official_degree_name": "Graphic Design M.F.A.",
    "thesis_or_non_thesis": null,
    "concentrations_or_tracks": [],
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": "MULTILINGUAL",
    "delivery_mode": null,
    "program_description": "Graduate study in graphic design that emphasizes visual communication, typography, and project-based studio work.",
    "admission_requirements": null,
    "gre_requirement": null,
    "gmat_requirement": null,
    "english_requirement": null,
    "interview_requirement": null,
    "experience_requirement": null,
    "accreditation": null,
    "official_program_url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/ALBAGraduate.pdf",
    "sources": [
      {
        "source_id": "uob_alba_graduate_catalogue_pdf",
        "page_title": "ALBA Graduate Catalogue",
        "url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/ALBAGraduate.pdf",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_catalogue_page",
        "page_title": "Catalogue",
        "url": "https://www.balamand.edu.lb/CurrentStudents/Pages/Catalogue.aspx",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_general_graduate_catalogue_pdf",
        "page_title": "General Section Graduate Catalogue",
        "url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/General-Section-Graduate.pdf",
        "date_accessed": "2026-06-28"
      }
    ],
    "tuition": {
      "academic_year": "2025-2026",
      "currency": "USD",
      "billing_basis": "PER_CREDIT",
      "amount": 510,
      "category": "Académie Libanaise des Beaux-Arts - Al-Kurah Campus",
      "notes": "Official UOB fee table lists ALBA - Balamand / Al-Kurah Campus graduate tuition (2025-2026).",
      "source_ids": [
        "uob_fees_expenses_pdf"
      ]
    },
    "notes": "Listed in the ALBA graduate catalogue under the Al-Kurah Campus; no individual program page surfaced during discovery."
  },
  {
    "id": "uob-alba-master-interior-architecture-design-mfa",
    "faculty": "Académie Libanaise des Beaux-Arts - Al-Kurah Campus",
    "department": null,
    "major_category": "Architecture",
    "major": "Interior Architecture and Design",
    "degree_type": "MASTER",
    "official_degree_name": "Interior Architecture and Design M.F.A.",
    "thesis_or_non_thesis": null,
    "concentrations_or_tracks": [],
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": "MULTILINGUAL",
    "delivery_mode": null,
    "program_description": "Professional graduate study in architecture or interior architecture that emphasizes advanced design practice, project development, and jury-reviewed studio work.",
    "admission_requirements": null,
    "gre_requirement": null,
    "gmat_requirement": null,
    "english_requirement": null,
    "interview_requirement": null,
    "experience_requirement": null,
    "accreditation": null,
    "official_program_url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/ALBAGraduate.pdf",
    "sources": [
      {
        "source_id": "uob_alba_graduate_catalogue_pdf",
        "page_title": "ALBA Graduate Catalogue",
        "url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/ALBAGraduate.pdf",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_catalogue_page",
        "page_title": "Catalogue",
        "url": "https://www.balamand.edu.lb/CurrentStudents/Pages/Catalogue.aspx",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_general_graduate_catalogue_pdf",
        "page_title": "General Section Graduate Catalogue",
        "url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/General-Section-Graduate.pdf",
        "date_accessed": "2026-06-28"
      }
    ],
    "tuition": {
      "academic_year": "2025-2026",
      "currency": "USD",
      "billing_basis": "PER_CREDIT",
      "amount": 510,
      "category": "Académie Libanaise des Beaux-Arts - Al-Kurah Campus",
      "notes": "Official UOB fee table lists ALBA - Balamand / Al-Kurah Campus graduate tuition (2025-2026).",
      "source_ids": [
        "uob_fees_expenses_pdf"
      ]
    },
    "notes": "Listed in the ALBA graduate catalogue under the Al-Kurah Campus; no individual program page surfaced during discovery."
  },
  {
    "id": "uob-alba-master-animation-2d-3d",
    "faculty": "Académie Libanaise des Beaux-Arts - Dekouaneh Campus",
    "department": null,
    "major_category": "Animation",
    "major": "Animation",
    "degree_type": "MASTER",
    "official_degree_name": "Animation 2D/3D Master",
    "thesis_or_non_thesis": null,
    "concentrations_or_tracks": [],
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": "MULTILINGUAL",
    "delivery_mode": null,
    "program_description": "Graduate specialization in animation within ALBAs creative media catalogue.",
    "admission_requirements": null,
    "gre_requirement": null,
    "gmat_requirement": null,
    "english_requirement": null,
    "interview_requirement": null,
    "experience_requirement": null,
    "accreditation": null,
    "official_program_url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/ALBAGraduate.pdf",
    "sources": [
      {
        "source_id": "uob_alba_graduate_catalogue_pdf",
        "page_title": "ALBA Graduate Catalogue",
        "url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/ALBAGraduate.pdf",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_catalogue_page",
        "page_title": "Catalogue",
        "url": "https://www.balamand.edu.lb/CurrentStudents/Pages/Catalogue.aspx",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_general_graduate_catalogue_pdf",
        "page_title": "General Section Graduate Catalogue",
        "url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/General-Section-Graduate.pdf",
        "date_accessed": "2026-06-28"
      }
    ],
    "tuition": {
      "academic_year": "2025-2026",
      "currency": "USD",
      "billing_basis": "PER_CREDIT",
      "amount": 250,
      "category": "Académie Libanaise des Beaux-Arts - Dekouaneh Campus",
      "notes": "Official UOB fee table lists ALBA - Dekouaneh Campus graduate tuition (2025-2026).",
      "source_ids": [
        "uob_fees_expenses_pdf"
      ]
    },
    "notes": "Listed in the ALBA graduate catalogue under the Dekouaneh Campus; no individual program page surfaced during discovery."
  },
  {
    "id": "uob-alba-master-architecture",
    "faculty": "Académie Libanaise des Beaux-Arts - Dekouaneh Campus",
    "department": null,
    "major_category": "Architecture",
    "major": "Architecture",
    "degree_type": "MASTER",
    "official_degree_name": "Architecture Master",
    "thesis_or_non_thesis": null,
    "concentrations_or_tracks": [],
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": "MULTILINGUAL",
    "delivery_mode": null,
    "program_description": "Professional graduate study in architecture or interior architecture that emphasizes advanced design practice, project development, and jury-reviewed studio work.",
    "admission_requirements": null,
    "gre_requirement": null,
    "gmat_requirement": null,
    "english_requirement": null,
    "interview_requirement": null,
    "experience_requirement": null,
    "accreditation": null,
    "official_program_url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/ALBAGraduate.pdf",
    "sources": [
      {
        "source_id": "uob_alba_graduate_catalogue_pdf",
        "page_title": "ALBA Graduate Catalogue",
        "url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/ALBAGraduate.pdf",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_catalogue_page",
        "page_title": "Catalogue",
        "url": "https://www.balamand.edu.lb/CurrentStudents/Pages/Catalogue.aspx",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_general_graduate_catalogue_pdf",
        "page_title": "General Section Graduate Catalogue",
        "url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/General-Section-Graduate.pdf",
        "date_accessed": "2026-06-28"
      }
    ],
    "tuition": {
      "academic_year": "2025-2026",
      "currency": "USD",
      "billing_basis": "PER_CREDIT",
      "amount": 250,
      "category": "Académie Libanaise des Beaux-Arts - Dekouaneh Campus",
      "notes": "Official UOB fee table lists ALBA - Dekouaneh Campus graduate tuition (2025-2026).",
      "source_ids": [
        "uob_fees_expenses_pdf"
      ]
    },
    "notes": "Listed in the ALBA graduate catalogue under the Dekouaneh Campus; no individual program page surfaced during discovery."
  },
  {
    "id": "uob-alba-master-audiovisual-directing",
    "faculty": "Académie Libanaise des Beaux-Arts - Dekouaneh Campus",
    "department": null,
    "major_category": "Audiovisual Directing",
    "major": "Audiovisual Directing",
    "degree_type": "MASTER",
    "official_degree_name": "Réalisation Audiovisuelle Master",
    "thesis_or_non_thesis": null,
    "concentrations_or_tracks": [],
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": "MULTILINGUAL",
    "delivery_mode": null,
    "program_description": "Graduate specialization in audiovisual directing or production within ALBAs creative media catalogue.",
    "admission_requirements": null,
    "gre_requirement": null,
    "gmat_requirement": null,
    "english_requirement": null,
    "interview_requirement": null,
    "experience_requirement": null,
    "accreditation": null,
    "official_program_url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/ALBAGraduate.pdf",
    "sources": [
      {
        "source_id": "uob_alba_graduate_catalogue_pdf",
        "page_title": "ALBA Graduate Catalogue",
        "url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/ALBAGraduate.pdf",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_catalogue_page",
        "page_title": "Catalogue",
        "url": "https://www.balamand.edu.lb/CurrentStudents/Pages/Catalogue.aspx",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_general_graduate_catalogue_pdf",
        "page_title": "General Section Graduate Catalogue",
        "url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/General-Section-Graduate.pdf",
        "date_accessed": "2026-06-28"
      }
    ],
    "tuition": {
      "academic_year": "2025-2026",
      "currency": "USD",
      "billing_basis": "PER_CREDIT",
      "amount": 250,
      "category": "Académie Libanaise des Beaux-Arts - Dekouaneh Campus",
      "notes": "Official UOB fee table lists ALBA - Dekouaneh Campus graduate tuition (2025-2026).",
      "source_ids": [
        "uob_fees_expenses_pdf"
      ]
    },
    "notes": "Listed in the ALBA graduate catalogue under the Dekouaneh Campus; no individual program page surfaced during discovery."
  },
  {
    "id": "uob-alba-master-audiovisual-production",
    "faculty": "Académie Libanaise des Beaux-Arts - Dekouaneh Campus",
    "department": null,
    "major_category": "Audiovisual Production",
    "major": "Audiovisual Production",
    "degree_type": "MASTER",
    "official_degree_name": "Production Audiovisuelle Master",
    "thesis_or_non_thesis": null,
    "concentrations_or_tracks": [],
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": "MULTILINGUAL",
    "delivery_mode": null,
    "program_description": "Graduate specialization in audiovisual directing or production within ALBAs creative media catalogue.",
    "admission_requirements": null,
    "gre_requirement": null,
    "gmat_requirement": null,
    "english_requirement": null,
    "interview_requirement": null,
    "experience_requirement": null,
    "accreditation": null,
    "official_program_url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/ALBAGraduate.pdf",
    "sources": [
      {
        "source_id": "uob_alba_graduate_catalogue_pdf",
        "page_title": "ALBA Graduate Catalogue",
        "url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/ALBAGraduate.pdf",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_catalogue_page",
        "page_title": "Catalogue",
        "url": "https://www.balamand.edu.lb/CurrentStudents/Pages/Catalogue.aspx",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_general_graduate_catalogue_pdf",
        "page_title": "General Section Graduate Catalogue",
        "url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/General-Section-Graduate.pdf",
        "date_accessed": "2026-06-28"
      }
    ],
    "tuition": {
      "academic_year": "2025-2026",
      "currency": "USD",
      "billing_basis": "PER_CREDIT",
      "amount": 250,
      "category": "Académie Libanaise des Beaux-Arts - Dekouaneh Campus",
      "notes": "Official UOB fee table lists ALBA - Dekouaneh Campus graduate tuition (2025-2026).",
      "source_ids": [
        "uob_fees_expenses_pdf"
      ]
    },
    "notes": "Listed in the ALBA graduate catalogue under the Dekouaneh Campus; no individual program page surfaced during discovery."
  },
  {
    "id": "uob-alba-master-cinema-directing",
    "faculty": "Académie Libanaise des Beaux-Arts - Dekouaneh Campus",
    "department": null,
    "major_category": "Cinema Directing",
    "major": "Cinema Directing",
    "degree_type": "MASTER",
    "official_degree_name": "Réalisation Cinéma Master",
    "thesis_or_non_thesis": null,
    "concentrations_or_tracks": [],
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": "MULTILINGUAL",
    "delivery_mode": null,
    "program_description": "Graduate specialization in cinema directing within ALBAs creative media catalogue.",
    "admission_requirements": null,
    "gre_requirement": null,
    "gmat_requirement": null,
    "english_requirement": null,
    "interview_requirement": null,
    "experience_requirement": null,
    "accreditation": null,
    "official_program_url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/ALBAGraduate.pdf",
    "sources": [
      {
        "source_id": "uob_alba_graduate_catalogue_pdf",
        "page_title": "ALBA Graduate Catalogue",
        "url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/ALBAGraduate.pdf",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_catalogue_page",
        "page_title": "Catalogue",
        "url": "https://www.balamand.edu.lb/CurrentStudents/Pages/Catalogue.aspx",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_general_graduate_catalogue_pdf",
        "page_title": "General Section Graduate Catalogue",
        "url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/General-Section-Graduate.pdf",
        "date_accessed": "2026-06-28"
      }
    ],
    "tuition": {
      "academic_year": "2025-2026",
      "currency": "USD",
      "billing_basis": "PER_CREDIT",
      "amount": 250,
      "category": "Académie Libanaise des Beaux-Arts - Dekouaneh Campus",
      "notes": "Official UOB fee table lists ALBA - Dekouaneh Campus graduate tuition (2025-2026).",
      "source_ids": [
        "uob_fees_expenses_pdf"
      ]
    },
    "notes": "Listed in the ALBA graduate catalogue under the Dekouaneh Campus; no individual program page surfaced during discovery."
  },
  {
    "id": "uob-alba-master-global-design",
    "faculty": "Académie Libanaise des Beaux-Arts - Dekouaneh Campus",
    "department": null,
    "major_category": "Global Design",
    "major": "Global Design",
    "degree_type": "MASTER",
    "official_degree_name": "Design Global Master",
    "thesis_or_non_thesis": null,
    "concentrations_or_tracks": [],
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": "MULTILINGUAL",
    "delivery_mode": null,
    "program_description": "Graduate specialization in global design within ALBAs creative catalogue.",
    "admission_requirements": null,
    "gre_requirement": null,
    "gmat_requirement": null,
    "english_requirement": null,
    "interview_requirement": null,
    "experience_requirement": null,
    "accreditation": null,
    "official_program_url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/ALBAGraduate.pdf",
    "sources": [
      {
        "source_id": "uob_alba_graduate_catalogue_pdf",
        "page_title": "ALBA Graduate Catalogue",
        "url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/ALBAGraduate.pdf",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_catalogue_page",
        "page_title": "Catalogue",
        "url": "https://www.balamand.edu.lb/CurrentStudents/Pages/Catalogue.aspx",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_general_graduate_catalogue_pdf",
        "page_title": "General Section Graduate Catalogue",
        "url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/General-Section-Graduate.pdf",
        "date_accessed": "2026-06-28"
      }
    ],
    "tuition": {
      "academic_year": "2025-2026",
      "currency": "USD",
      "billing_basis": "PER_CREDIT",
      "amount": 250,
      "category": "Académie Libanaise des Beaux-Arts - Dekouaneh Campus",
      "notes": "Official UOB fee table lists ALBA - Dekouaneh Campus graduate tuition (2025-2026).",
      "source_ids": [
        "uob_fees_expenses_pdf"
      ]
    },
    "notes": "Listed in the ALBA graduate catalogue under the Dekouaneh Campus; no individual program page surfaced during discovery."
  },
  {
    "id": "uob-alba-master-graphic-design-publicity",
    "faculty": "Académie Libanaise des Beaux-Arts - Dekouaneh Campus",
    "department": null,
    "major_category": "Graphic Design and Advertising",
    "major": "Graphic Design and Advertising",
    "degree_type": "MASTER",
    "official_degree_name": "Graphisme et Publicité Master",
    "thesis_or_non_thesis": null,
    "concentrations_or_tracks": [],
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": "MULTILINGUAL",
    "delivery_mode": null,
    "program_description": "Graduate study in graphic design that emphasizes visual communication, typography, and project-based studio work.",
    "admission_requirements": null,
    "gre_requirement": null,
    "gmat_requirement": null,
    "english_requirement": null,
    "interview_requirement": null,
    "experience_requirement": null,
    "accreditation": null,
    "official_program_url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/ALBAGraduate.pdf",
    "sources": [
      {
        "source_id": "uob_alba_graduate_catalogue_pdf",
        "page_title": "ALBA Graduate Catalogue",
        "url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/ALBAGraduate.pdf",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_catalogue_page",
        "page_title": "Catalogue",
        "url": "https://www.balamand.edu.lb/CurrentStudents/Pages/Catalogue.aspx",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_general_graduate_catalogue_pdf",
        "page_title": "General Section Graduate Catalogue",
        "url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/General-Section-Graduate.pdf",
        "date_accessed": "2026-06-28"
      }
    ],
    "tuition": {
      "academic_year": "2025-2026",
      "currency": "USD",
      "billing_basis": "PER_CREDIT",
      "amount": 250,
      "category": "Académie Libanaise des Beaux-Arts - Dekouaneh Campus",
      "notes": "Official UOB fee table lists ALBA - Dekouaneh Campus graduate tuition (2025-2026).",
      "source_ids": [
        "uob_fees_expenses_pdf"
      ]
    },
    "notes": "Listed in the ALBA graduate catalogue under the Dekouaneh Campus; no individual program page surfaced during discovery."
  },
  {
    "id": "uob-alba-master-illustration-comic",
    "faculty": "Académie Libanaise des Beaux-Arts - Dekouaneh Campus",
    "department": null,
    "major_category": "Illustration and Comic Strip",
    "major": "Illustration and Comic Strip",
    "degree_type": "MASTER",
    "official_degree_name": "Illustration - Bande Dessinée Master",
    "thesis_or_non_thesis": null,
    "concentrations_or_tracks": [],
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": "MULTILINGUAL",
    "delivery_mode": null,
    "program_description": "Graduate specialization in illustration and comic strip within ALBAs creative catalogue.",
    "admission_requirements": null,
    "gre_requirement": null,
    "gmat_requirement": null,
    "english_requirement": null,
    "interview_requirement": null,
    "experience_requirement": null,
    "accreditation": null,
    "official_program_url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/ALBAGraduate.pdf",
    "sources": [
      {
        "source_id": "uob_alba_graduate_catalogue_pdf",
        "page_title": "ALBA Graduate Catalogue",
        "url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/ALBAGraduate.pdf",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_catalogue_page",
        "page_title": "Catalogue",
        "url": "https://www.balamand.edu.lb/CurrentStudents/Pages/Catalogue.aspx",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_general_graduate_catalogue_pdf",
        "page_title": "General Section Graduate Catalogue",
        "url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/General-Section-Graduate.pdf",
        "date_accessed": "2026-06-28"
      }
    ],
    "tuition": {
      "academic_year": "2025-2026",
      "currency": "USD",
      "billing_basis": "PER_CREDIT",
      "amount": 250,
      "category": "Académie Libanaise des Beaux-Arts - Dekouaneh Campus",
      "notes": "Official UOB fee table lists ALBA - Dekouaneh Campus graduate tuition (2025-2026).",
      "source_ids": [
        "uob_fees_expenses_pdf"
      ]
    },
    "notes": "Listed in the ALBA graduate catalogue under the Dekouaneh Campus; no individual program page surfaced during discovery."
  },
  {
    "id": "uob-alba-master-interior-architecture",
    "faculty": "Académie Libanaise des Beaux-Arts - Dekouaneh Campus",
    "department": null,
    "major_category": "Interior Architecture and Design",
    "major": "Interior Architecture",
    "degree_type": "MASTER",
    "official_degree_name": "Architecture d’Intérieur Master",
    "thesis_or_non_thesis": null,
    "concentrations_or_tracks": [],
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": "MULTILINGUAL",
    "delivery_mode": null,
    "program_description": "Professional graduate study in architecture or interior architecture that emphasizes advanced design practice, project development, and jury-reviewed studio work.",
    "admission_requirements": null,
    "gre_requirement": null,
    "gmat_requirement": null,
    "english_requirement": null,
    "interview_requirement": null,
    "experience_requirement": null,
    "accreditation": null,
    "official_program_url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/ALBAGraduate.pdf",
    "sources": [
      {
        "source_id": "uob_alba_graduate_catalogue_pdf",
        "page_title": "ALBA Graduate Catalogue",
        "url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/ALBAGraduate.pdf",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_catalogue_page",
        "page_title": "Catalogue",
        "url": "https://www.balamand.edu.lb/CurrentStudents/Pages/Catalogue.aspx",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_general_graduate_catalogue_pdf",
        "page_title": "General Section Graduate Catalogue",
        "url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/General-Section-Graduate.pdf",
        "date_accessed": "2026-06-28"
      }
    ],
    "tuition": {
      "academic_year": "2025-2026",
      "currency": "USD",
      "billing_basis": "PER_CREDIT",
      "amount": 250,
      "category": "Académie Libanaise des Beaux-Arts - Dekouaneh Campus",
      "notes": "Official UOB fee table lists ALBA - Dekouaneh Campus graduate tuition (2025-2026).",
      "source_ids": [
        "uob_fees_expenses_pdf"
      ]
    },
    "notes": "Listed in the ALBA graduate catalogue under the Dekouaneh Campus; no individual program page surfaced during discovery."
  },
  {
    "id": "uob-alba-master-landscape-management",
    "faculty": "Académie Libanaise des Beaux-Arts - Dekouaneh Campus",
    "department": null,
    "major_category": "Landscape Management",
    "major": "Landscape Management",
    "degree_type": "MASTER",
    "official_degree_name": "Aménagement du Paysage Master",
    "thesis_or_non_thesis": null,
    "concentrations_or_tracks": [],
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": "MULTILINGUAL",
    "delivery_mode": null,
    "program_description": "Graduate specialization in landscape management within ALBAs design catalogue.",
    "admission_requirements": null,
    "gre_requirement": null,
    "gmat_requirement": null,
    "english_requirement": null,
    "interview_requirement": null,
    "experience_requirement": null,
    "accreditation": null,
    "official_program_url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/ALBAGraduate.pdf",
    "sources": [
      {
        "source_id": "uob_alba_graduate_catalogue_pdf",
        "page_title": "ALBA Graduate Catalogue",
        "url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/ALBAGraduate.pdf",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_catalogue_page",
        "page_title": "Catalogue",
        "url": "https://www.balamand.edu.lb/CurrentStudents/Pages/Catalogue.aspx",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_general_graduate_catalogue_pdf",
        "page_title": "General Section Graduate Catalogue",
        "url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/General-Section-Graduate.pdf",
        "date_accessed": "2026-06-28"
      }
    ],
    "tuition": {
      "academic_year": "2025-2026",
      "currency": "USD",
      "billing_basis": "PER_CREDIT",
      "amount": 250,
      "category": "Académie Libanaise des Beaux-Arts - Dekouaneh Campus",
      "notes": "Official UOB fee table lists ALBA - Dekouaneh Campus graduate tuition (2025-2026).",
      "source_ids": [
        "uob_fees_expenses_pdf"
      ]
    },
    "notes": "Listed in the ALBA graduate catalogue under the Dekouaneh Campus; no individual program page surfaced during discovery."
  },
  {
    "id": "uob-alba-master-multimedia-creation",
    "faculty": "Académie Libanaise des Beaux-Arts - Dekouaneh Campus",
    "department": null,
    "major_category": "Multimedia Creation",
    "major": "Multimedia Creation",
    "degree_type": "MASTER",
    "official_degree_name": "Création Multimédia Master",
    "thesis_or_non_thesis": null,
    "concentrations_or_tracks": [],
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": "MULTILINGUAL",
    "delivery_mode": null,
    "program_description": "Graduate specialization in multimedia creation within ALBAs creative catalogue.",
    "admission_requirements": null,
    "gre_requirement": null,
    "gmat_requirement": null,
    "english_requirement": null,
    "interview_requirement": null,
    "experience_requirement": null,
    "accreditation": null,
    "official_program_url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/ALBAGraduate.pdf",
    "sources": [
      {
        "source_id": "uob_alba_graduate_catalogue_pdf",
        "page_title": "ALBA Graduate Catalogue",
        "url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/ALBAGraduate.pdf",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_catalogue_page",
        "page_title": "Catalogue",
        "url": "https://www.balamand.edu.lb/CurrentStudents/Pages/Catalogue.aspx",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_general_graduate_catalogue_pdf",
        "page_title": "General Section Graduate Catalogue",
        "url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/General-Section-Graduate.pdf",
        "date_accessed": "2026-06-28"
      }
    ],
    "tuition": {
      "academic_year": "2025-2026",
      "currency": "USD",
      "billing_basis": "PER_CREDIT",
      "amount": 250,
      "category": "Académie Libanaise des Beaux-Arts - Dekouaneh Campus",
      "notes": "Official UOB fee table lists ALBA - Dekouaneh Campus graduate tuition (2025-2026).",
      "source_ids": [
        "uob_fees_expenses_pdf"
      ]
    },
    "notes": "Listed in the ALBA graduate catalogue under the Dekouaneh Campus; no individual program page surfaced during discovery."
  },
  {
    "id": "uob-alba-master-photography",
    "faculty": "Académie Libanaise des Beaux-Arts - Dekouaneh Campus",
    "department": null,
    "major_category": "Photography",
    "major": "Photography",
    "degree_type": "MASTER",
    "official_degree_name": "Photographie Master",
    "thesis_or_non_thesis": null,
    "concentrations_or_tracks": [],
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": "MULTILINGUAL",
    "delivery_mode": null,
    "program_description": "Graduate specialization in photography within ALBAs creative catalogue.",
    "admission_requirements": null,
    "gre_requirement": null,
    "gmat_requirement": null,
    "english_requirement": null,
    "interview_requirement": null,
    "experience_requirement": null,
    "accreditation": null,
    "official_program_url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/ALBAGraduate.pdf",
    "sources": [
      {
        "source_id": "uob_alba_graduate_catalogue_pdf",
        "page_title": "ALBA Graduate Catalogue",
        "url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/ALBAGraduate.pdf",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_catalogue_page",
        "page_title": "Catalogue",
        "url": "https://www.balamand.edu.lb/CurrentStudents/Pages/Catalogue.aspx",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_general_graduate_catalogue_pdf",
        "page_title": "General Section Graduate Catalogue",
        "url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/General-Section-Graduate.pdf",
        "date_accessed": "2026-06-28"
      }
    ],
    "tuition": {
      "academic_year": "2025-2026",
      "currency": "USD",
      "billing_basis": "PER_CREDIT",
      "amount": 250,
      "category": "Académie Libanaise des Beaux-Arts - Dekouaneh Campus",
      "notes": "Official UOB fee table lists ALBA - Dekouaneh Campus graduate tuition (2025-2026).",
      "source_ids": [
        "uob_fees_expenses_pdf"
      ]
    },
    "notes": "Listed in the ALBA graduate catalogue under the Dekouaneh Campus; no individual program page surfaced during discovery."
  },
  {
    "id": "uob-alba-master-television-digital-media",
    "faculty": "Académie Libanaise des Beaux-Arts - Dekouaneh Campus",
    "department": null,
    "major_category": "Television and Digital Media",
    "major": "Television and Digital Media",
    "degree_type": "MASTER",
    "official_degree_name": "Television and Digital Media Master",
    "thesis_or_non_thesis": null,
    "concentrations_or_tracks": [],
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": "MULTILINGUAL",
    "delivery_mode": null,
    "program_description": "Graduate specialization in television and digital media within ALBAs creative catalogue.",
    "admission_requirements": null,
    "gre_requirement": null,
    "gmat_requirement": null,
    "english_requirement": null,
    "interview_requirement": null,
    "experience_requirement": null,
    "accreditation": null,
    "official_program_url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/ALBAGraduate.pdf",
    "sources": [
      {
        "source_id": "uob_alba_graduate_catalogue_pdf",
        "page_title": "ALBA Graduate Catalogue",
        "url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/ALBAGraduate.pdf",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_catalogue_page",
        "page_title": "Catalogue",
        "url": "https://www.balamand.edu.lb/CurrentStudents/Pages/Catalogue.aspx",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_general_graduate_catalogue_pdf",
        "page_title": "General Section Graduate Catalogue",
        "url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/General-Section-Graduate.pdf",
        "date_accessed": "2026-06-28"
      }
    ],
    "tuition": {
      "academic_year": "2025-2026",
      "currency": "USD",
      "billing_basis": "PER_CREDIT",
      "amount": 250,
      "category": "Académie Libanaise des Beaux-Arts - Dekouaneh Campus",
      "notes": "Official UOB fee table lists ALBA - Dekouaneh Campus graduate tuition (2025-2026).",
      "source_ids": [
        "uob_fees_expenses_pdf"
      ]
    },
    "notes": "Listed in the ALBA graduate catalogue under the Dekouaneh Campus; no individual program page surfaced during discovery."
  },
  {
    "id": "uob-alba-master-urban-design",
    "faculty": "Académie Libanaise des Beaux-Arts - Dekouaneh Campus",
    "department": null,
    "major_category": "Urban Design",
    "major": "Urban Design",
    "degree_type": "MASTER",
    "official_degree_name": "Design Urbain Master",
    "thesis_or_non_thesis": null,
    "concentrations_or_tracks": [],
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": "MULTILINGUAL",
    "delivery_mode": null,
    "program_description": "Graduate specialization in urban design within ALBAs architecture catalogue.",
    "admission_requirements": null,
    "gre_requirement": null,
    "gmat_requirement": null,
    "english_requirement": null,
    "interview_requirement": null,
    "experience_requirement": null,
    "accreditation": null,
    "official_program_url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/ALBAGraduate.pdf",
    "sources": [
      {
        "source_id": "uob_alba_graduate_catalogue_pdf",
        "page_title": "ALBA Graduate Catalogue",
        "url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/ALBAGraduate.pdf",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_catalogue_page",
        "page_title": "Catalogue",
        "url": "https://www.balamand.edu.lb/CurrentStudents/Pages/Catalogue.aspx",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_general_graduate_catalogue_pdf",
        "page_title": "General Section Graduate Catalogue",
        "url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/General-Section-Graduate.pdf",
        "date_accessed": "2026-06-28"
      }
    ],
    "tuition": {
      "academic_year": "2025-2026",
      "currency": "USD",
      "billing_basis": "PER_CREDIT",
      "amount": 250,
      "category": "Académie Libanaise des Beaux-Arts - Dekouaneh Campus",
      "notes": "Official UOB fee table lists ALBA - Dekouaneh Campus graduate tuition (2025-2026).",
      "source_ids": [
        "uob_fees_expenses_pdf"
      ]
    },
    "notes": "Listed in the ALBA graduate catalogue under the Dekouaneh Campus; no individual program page surfaced during discovery."
  },
  {
    "id": "uob-alba-master-visual-arts",
    "faculty": "Académie Libanaise des Beaux-Arts - Dekouaneh Campus",
    "department": null,
    "major_category": "Visual Arts",
    "major": "Visual Arts",
    "degree_type": "MASTER",
    "official_degree_name": "Arts visuels Master",
    "thesis_or_non_thesis": null,
    "concentrations_or_tracks": [],
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": "MULTILINGUAL",
    "delivery_mode": null,
    "program_description": "Graduate specialization in visual arts within ALBAs creative catalogue.",
    "admission_requirements": null,
    "gre_requirement": null,
    "gmat_requirement": null,
    "english_requirement": null,
    "interview_requirement": null,
    "experience_requirement": null,
    "accreditation": null,
    "official_program_url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/ALBAGraduate.pdf",
    "sources": [
      {
        "source_id": "uob_alba_graduate_catalogue_pdf",
        "page_title": "ALBA Graduate Catalogue",
        "url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/ALBAGraduate.pdf",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_catalogue_page",
        "page_title": "Catalogue",
        "url": "https://www.balamand.edu.lb/CurrentStudents/Pages/Catalogue.aspx",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_general_graduate_catalogue_pdf",
        "page_title": "General Section Graduate Catalogue",
        "url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/General-Section-Graduate.pdf",
        "date_accessed": "2026-06-28"
      }
    ],
    "tuition": {
      "academic_year": "2025-2026",
      "currency": "USD",
      "billing_basis": "PER_CREDIT",
      "amount": 250,
      "category": "Académie Libanaise des Beaux-Arts - Dekouaneh Campus",
      "notes": "Official UOB fee table lists ALBA - Dekouaneh Campus graduate tuition (2025-2026).",
      "source_ids": [
        "uob_fees_expenses_pdf"
      ]
    },
    "notes": "Listed in the ALBA graduate catalogue under the Dekouaneh Campus; no individual program page surfaced during discovery."
  },
  {
    "id": "uob-fas-master-arabic-language-literature",
    "faculty": "Faculty of Arts and Sciences - Al-Kurah Campus",
    "department": "Department of Arabic Language and Literature",
    "major_category": "Arts and Sciences",
    "major": "Arabic Language and Literature",
    "degree_type": "MASTER",
    "official_degree_name": "Arabic Language and Literature M.A.",
    "thesis_or_non_thesis": "THESIS",
    "concentrations_or_tracks": [
      "Arabic Language",
      "Arabic Literature"
    ],
    "credits": 30,
    "duration_value": 2,
    "duration_unit": "YEARS",
    "duration_raw_text": "2 years",
    "language": "ARABIC",
    "delivery_mode": null,
    "program_description": "The master program in Arabic Language and Literature offers two specializations, Arabic Language and Arabic Literature, and requires coursework plus a research thesis.",
    "admission_requirements": null,
    "gre_requirement": null,
    "gmat_requirement": null,
    "english_requirement": null,
    "interview_requirement": null,
    "experience_requirement": null,
    "accreditation": null,
    "official_program_url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/FASGraduate.pdf",
    "sources": [
      {
        "source_id": "uob_fas_graduate_catalogue_pdf",
        "page_title": "Faculty of Arts and Sciences Graduate Catalogue",
        "url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/FASGraduate.pdf",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_fas",
        "page_title": "Faculty of Arts and Sciences",
        "url": "https://www.balamand.edu.lb/faculties/FAS/Pages/default.aspx",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_catalogue_page",
        "page_title": "Catalogue",
        "url": "https://www.balamand.edu.lb/CurrentStudents/Pages/Catalogue.aspx",
        "date_accessed": "2026-06-28"
      }
    ],
    "tuition": {
      "academic_year": "2025-2026",
      "currency": "USD",
      "billing_basis": "PER_CREDIT",
      "amount": 725,
      "category": "Faculty of Arts and Sciences - Arts",
      "notes": "Official UOB graduate fee table (2025-2026).",
      "source_ids": [
        "uob_fees_expenses_pdf"
      ]
    },
    "notes": "Listed in the FAS graduate catalogue; no separate program page surfaced during discovery. The FAS graduate catalogue states that the Arabic master requires 24 coursework credits plus 6 thesis credits and is taught in Arabic."
  },
  {
    "id": "uob-fas-master-biology",
    "faculty": "Faculty of Arts and Sciences - Al-Kurah Campus",
    "department": "Department of Biology",
    "major_category": "Sciences",
    "major": "Biology",
    "degree_type": "MASTER",
    "official_degree_name": "Biology M.S.",
    "thesis_or_non_thesis": "THESIS_OR_PROJECT",
    "concentrations_or_tracks": [
      "Biochemistry",
      "Molecular Biology",
      "Immunology",
      "Microbiology"
    ],
    "credits": 30,
    "duration_value": 2,
    "duration_unit": "YEARS",
    "duration_raw_text": "2 years",
    "language": "MULTILINGUAL",
    "delivery_mode": null,
    "program_description": "The Biology MSc is a two-year, 30-credit graduate program with a thesis or project option. It emphasizes biochemistry, molecular biology, immunology, and microbiology.",
    "admission_requirements": null,
    "gre_requirement": null,
    "gmat_requirement": null,
    "english_requirement": null,
    "interview_requirement": null,
    "experience_requirement": null,
    "accreditation": null,
    "official_program_url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/FASGraduate.pdf",
    "sources": [
      {
        "source_id": "uob_fas_graduate_catalogue_pdf",
        "page_title": "Faculty of Arts and Sciences Graduate Catalogue",
        "url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/FASGraduate.pdf",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_fas",
        "page_title": "Faculty of Arts and Sciences",
        "url": "https://www.balamand.edu.lb/faculties/FAS/Pages/default.aspx",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_catalogue_page",
        "page_title": "Catalogue",
        "url": "https://www.balamand.edu.lb/CurrentStudents/Pages/Catalogue.aspx",
        "date_accessed": "2026-06-28"
      }
    ],
    "tuition": {
      "academic_year": "2025-2026",
      "currency": "USD",
      "billing_basis": "PER_CREDIT",
      "amount": 705,
      "category": "Faculty of Arts and Sciences - Sciences",
      "notes": "Official UOB graduate fee table (2025-2026).",
      "source_ids": [
        "uob_fees_expenses_pdf"
      ]
    },
    "notes": "Listed in the FAS graduate catalogue; no separate program page surfaced during discovery. The FAS graduate catalogue states that the Biology MSc is a two-year, 30-credit program with a thesis or project option."
  },
  {
    "id": "uob-fas-master-chemistry",
    "faculty": "Faculty of Arts and Sciences - Al-Kurah Campus",
    "department": "Department of Chemistry",
    "major_category": "Sciences",
    "major": "Chemistry",
    "degree_type": "MASTER",
    "official_degree_name": "Chemistry M.S.",
    "thesis_or_non_thesis": "THESIS_OR_PROJECT",
    "concentrations_or_tracks": [
      "Advanced Analytical Chemistry",
      "Advanced Organic Chemistry",
      "Advanced Physical Chemistry",
      "Advanced Inorganic Chemistry"
    ],
    "credits": 30,
    "duration_value": 2,
    "duration_unit": "YEARS",
    "duration_raw_text": "2 years",
    "language": "MULTILINGUAL",
    "delivery_mode": null,
    "program_description": "The Chemistry MSc requires at least 30 credits and combines advanced chemistry coursework with thesis or project work in the major subfields of chemistry.",
    "admission_requirements": null,
    "gre_requirement": null,
    "gmat_requirement": null,
    "english_requirement": null,
    "interview_requirement": null,
    "experience_requirement": null,
    "accreditation": null,
    "official_program_url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/FASGraduate.pdf",
    "sources": [
      {
        "source_id": "uob_fas_graduate_catalogue_pdf",
        "page_title": "Faculty of Arts and Sciences Graduate Catalogue",
        "url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/FASGraduate.pdf",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_fas",
        "page_title": "Faculty of Arts and Sciences",
        "url": "https://www.balamand.edu.lb/faculties/FAS/Pages/default.aspx",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_catalogue_page",
        "page_title": "Catalogue",
        "url": "https://www.balamand.edu.lb/CurrentStudents/Pages/Catalogue.aspx",
        "date_accessed": "2026-06-28"
      }
    ],
    "tuition": {
      "academic_year": "2025-2026",
      "currency": "USD",
      "billing_basis": "PER_CREDIT",
      "amount": 705,
      "category": "Faculty of Arts and Sciences - Sciences",
      "notes": "Official UOB graduate fee table (2025-2026).",
      "source_ids": [
        "uob_fees_expenses_pdf"
      ]
    },
    "notes": "Listed in the FAS graduate catalogue; no separate program page surfaced during discovery. The FAS graduate catalogue specifies a 30-credit Chemistry MSc with a thesis or project option and four advanced chemistry areas."
  },
  {
    "id": "uob-fas-master-christian-muslim-studies",
    "faculty": "Faculty of Arts and Sciences - Al-Kurah Campus",
    "department": "Department of Christian Muslim Studies",
    "major_category": "Arts and Sciences",
    "major": "Christian Muslim Studies",
    "degree_type": "MASTER",
    "official_degree_name": "Christian Muslim Studies M.A.",
    "thesis_or_non_thesis": "THESIS",
    "concentrations_or_tracks": [],
    "credits": 30,
    "duration_value": 2,
    "duration_unit": "YEARS",
    "duration_raw_text": "2 years",
    "language": "ARABIC",
    "delivery_mode": null,
    "program_description": "The Christian-Muslim Studies MA is a two-year, 30-credit graduate program focused on Christian-Muslim dialogue, ethics, and related intellectual traditions.",
    "admission_requirements": null,
    "gre_requirement": null,
    "gmat_requirement": null,
    "english_requirement": null,
    "interview_requirement": null,
    "experience_requirement": null,
    "accreditation": null,
    "official_program_url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/FASGraduate.pdf",
    "sources": [
      {
        "source_id": "uob_fas_graduate_catalogue_pdf",
        "page_title": "Faculty of Arts and Sciences Graduate Catalogue",
        "url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/FASGraduate.pdf",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_fas",
        "page_title": "Faculty of Arts and Sciences",
        "url": "https://www.balamand.edu.lb/faculties/FAS/Pages/default.aspx",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_catalogue_page",
        "page_title": "Catalogue",
        "url": "https://www.balamand.edu.lb/CurrentStudents/Pages/Catalogue.aspx",
        "date_accessed": "2026-06-28"
      }
    ],
    "tuition": {
      "academic_year": "2025-2026",
      "currency": "USD",
      "billing_basis": "PER_CREDIT",
      "amount": 725,
      "category": "Faculty of Arts and Sciences - Arts",
      "notes": "Official UOB graduate fee table (2025-2026).",
      "source_ids": [
        "uob_fees_expenses_pdf"
      ]
    },
    "notes": "Listed in the FAS graduate catalogue; no separate program page surfaced during discovery. The FAS graduate catalogue states that the MA in Christian-Muslim Studies requires 24 credits plus a 6-credit thesis and uses Arabic as the language of study."
  },
  {
    "id": "uob-fas-master-computer-science",
    "faculty": "Faculty of Arts and Sciences - Al-Kurah Campus",
    "department": "Department of Computer Science",
    "major_category": "Sciences",
    "major": "Computer Science",
    "degree_type": "MASTER",
    "official_degree_name": "Computer Science M.S.",
    "thesis_or_non_thesis": "THESIS_OR_PROJECT",
    "concentrations_or_tracks": [
      "Health Information Systems",
      "Software Engineering"
    ],
    "credits": 30,
    "duration_value": 4,
    "duration_unit": "SEMESTERS",
    "duration_raw_text": "4 semesters / 2 years",
    "language": "MULTILINGUAL",
    "delivery_mode": null,
    "program_description": "The MS in Computer Science is a multidisciplinary graduate program with options in Software Engineering and Health Information Systems. The degree can be completed through a thesis or a project-oriented path.",
    "admission_requirements": null,
    "gre_requirement": null,
    "gmat_requirement": null,
    "english_requirement": null,
    "interview_requirement": null,
    "experience_requirement": null,
    "accreditation": null,
    "official_program_url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/FASGraduate.pdf",
    "sources": [
      {
        "source_id": "uob_fas_graduate_catalogue_pdf",
        "page_title": "Faculty of Arts and Sciences Graduate Catalogue",
        "url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/FASGraduate.pdf",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_fas",
        "page_title": "Faculty of Arts and Sciences",
        "url": "https://www.balamand.edu.lb/faculties/FAS/Pages/default.aspx",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_catalogue_page",
        "page_title": "Catalogue",
        "url": "https://www.balamand.edu.lb/CurrentStudents/Pages/Catalogue.aspx",
        "date_accessed": "2026-06-28"
      }
    ],
    "tuition": {
      "academic_year": "2025-2026",
      "currency": "USD",
      "billing_basis": "PER_CREDIT",
      "amount": 705,
      "category": "Faculty of Arts and Sciences - Sciences",
      "notes": "Official UOB graduate fee table (2025-2026).",
      "source_ids": [
        "uob_fees_expenses_pdf"
      ]
    },
    "notes": "Listed in the FAS graduate catalogue; no separate program page surfaced during discovery. The FAS graduate catalogue describes the Computer Science MSc as a two-option program with thesis/project paths and a Health Information Systems option."
  },
  {
    "id": "uob-fas-master-education-kurah",
    "faculty": "Faculty of Arts and Sciences - Al-Kurah Campus",
    "department": "Department of Education",
    "major_category": "Arts and Sciences",
    "major": "Education",
    "degree_type": "MASTER",
    "official_degree_name": "Education M.A.",
    "thesis_or_non_thesis": "THESIS_OR_PROJECT",
    "concentrations_or_tracks": [
      "Curriculum and Educational Management",
      "Instructional Design and Technology"
    ],
    "credits": 30,
    "duration_value": 2,
    "duration_unit": "YEARS",
    "duration_raw_text": "2 years",
    "language": "MULTILINGUAL",
    "delivery_mode": null,
    "program_description": "The Education MA is offered through two graduate tracks: Curriculum and Educational Management, and Instructional Design and Technology.",
    "admission_requirements": null,
    "gre_requirement": null,
    "gmat_requirement": null,
    "english_requirement": null,
    "interview_requirement": null,
    "experience_requirement": null,
    "accreditation": null,
    "official_program_url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/FASGraduate.pdf",
    "sources": [
      {
        "source_id": "uob_fas_graduate_catalogue_pdf",
        "page_title": "Faculty of Arts and Sciences Graduate Catalogue",
        "url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/FASGraduate.pdf",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_fas",
        "page_title": "Faculty of Arts and Sciences",
        "url": "https://www.balamand.edu.lb/faculties/FAS/Pages/default.aspx",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_catalogue_page",
        "page_title": "Catalogue",
        "url": "https://www.balamand.edu.lb/CurrentStudents/Pages/Catalogue.aspx",
        "date_accessed": "2026-06-28"
      }
    ],
    "tuition": {
      "academic_year": "2025-2026",
      "currency": "USD",
      "billing_basis": "PER_CREDIT",
      "amount": 725,
      "category": "Faculty of Arts and Sciences - Arts",
      "notes": "Official UOB graduate fee table (2025-2026).",
      "source_ids": [
        "uob_fees_expenses_pdf"
      ]
    },
    "notes": "Listed in the FAS graduate catalogue; no separate program page surfaced during discovery. The FAS graduate catalogue presents the Education MA as a two-track program with a thesis track and a project-based track."
  },
  {
    "id": "uob-fas-master-english-language-literature",
    "faculty": "Faculty of Arts and Sciences - Al-Kurah Campus",
    "department": "Department of English Language and Literature",
    "major_category": "Arts and Sciences",
    "major": "English Language and Literature",
    "degree_type": "MASTER",
    "official_degree_name": "English Language and Literature M.A.",
    "thesis_or_non_thesis": "THESIS",
    "concentrations_or_tracks": [],
    "credits": 30,
    "duration_value": 2,
    "duration_unit": "YEARS",
    "duration_raw_text": "2 years",
    "language": "ENGLISH",
    "delivery_mode": null,
    "program_description": "The MA in English Language and Literature develops advanced study of literary theory, modern literatures, research, analysis, and writing for academic and professional goals.",
    "admission_requirements": null,
    "gre_requirement": null,
    "gmat_requirement": null,
    "english_requirement": null,
    "interview_requirement": null,
    "experience_requirement": null,
    "accreditation": null,
    "official_program_url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/FASGraduate.pdf",
    "sources": [
      {
        "source_id": "uob_fas_graduate_catalogue_pdf",
        "page_title": "Faculty of Arts and Sciences Graduate Catalogue",
        "url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/FASGraduate.pdf",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_fas",
        "page_title": "Faculty of Arts and Sciences",
        "url": "https://www.balamand.edu.lb/faculties/FAS/Pages/default.aspx",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_catalogue_page",
        "page_title": "Catalogue",
        "url": "https://www.balamand.edu.lb/CurrentStudents/Pages/Catalogue.aspx",
        "date_accessed": "2026-06-28"
      }
    ],
    "tuition": {
      "academic_year": "2025-2026",
      "currency": "USD",
      "billing_basis": "PER_CREDIT",
      "amount": 725,
      "category": "Faculty of Arts and Sciences - Arts",
      "notes": "Official UOB graduate fee table (2025-2026).",
      "source_ids": [
        "uob_fees_expenses_pdf"
      ]
    },
    "notes": "Listed in the FAS graduate catalogue; no separate program page surfaced during discovery. The FAS graduate catalogue describes the degree as a 30-credit thesis program designed for doctoral study, teaching, research, and writing."
  },
  {
    "id": "uob-fas-master-english-language-teaching",
    "faculty": "Faculty of Arts and Sciences - Al-Kurah Campus",
    "department": "Department of English Language and Literature",
    "major_category": "Arts and Sciences",
    "major": "English Language Teaching",
    "degree_type": "MASTER",
    "official_degree_name": "English Language Teaching M.A.",
    "thesis_or_non_thesis": "THESIS_OR_PROJECT",
    "concentrations_or_tracks": [],
    "credits": 30,
    "duration_value": 2,
    "duration_unit": "YEARS",
    "duration_raw_text": "2 years",
    "language": "ENGLISH",
    "delivery_mode": null,
    "program_description": "The ELT MA prepares English language teachers through advanced research opportunities, language-teaching methodology, and either a thesis or a non-thesis project track.",
    "admission_requirements": null,
    "gre_requirement": null,
    "gmat_requirement": null,
    "english_requirement": null,
    "interview_requirement": null,
    "experience_requirement": null,
    "accreditation": null,
    "official_program_url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/FASGraduate.pdf",
    "sources": [
      {
        "source_id": "uob_fas_graduate_catalogue_pdf",
        "page_title": "Faculty of Arts and Sciences Graduate Catalogue",
        "url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/FASGraduate.pdf",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_fas",
        "page_title": "Faculty of Arts and Sciences",
        "url": "https://www.balamand.edu.lb/faculties/FAS/Pages/default.aspx",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_catalogue_page",
        "page_title": "Catalogue",
        "url": "https://www.balamand.edu.lb/CurrentStudents/Pages/Catalogue.aspx",
        "date_accessed": "2026-06-28"
      }
    ],
    "tuition": {
      "academic_year": "2025-2026",
      "currency": "USD",
      "billing_basis": "PER_CREDIT",
      "amount": 725,
      "category": "Faculty of Arts and Sciences - Arts",
      "notes": "Official UOB graduate fee table (2025-2026).",
      "source_ids": [
        "uob_fees_expenses_pdf"
      ]
    },
    "notes": "Listed in the FAS graduate catalogue; no separate program page surfaced during discovery. The FAS graduate catalogue states that ELT is a 30-credit program with thesis and non-thesis professional tracks."
  },
  {
    "id": "uob-fas-master-environmental-sciences",
    "faculty": "Faculty of Arts and Sciences - Al-Kurah Campus",
    "department": "Department of Environmental Sciences",
    "major_category": "Sciences",
    "major": "Environmental Sciences",
    "degree_type": "MASTER",
    "official_degree_name": "Environmental Sciences M.S.",
    "thesis_or_non_thesis": "THESIS",
    "concentrations_or_tracks": [],
    "credits": 30,
    "duration_value": 2,
    "duration_unit": "YEARS",
    "duration_raw_text": "2 years",
    "language": "MULTILINGUAL",
    "delivery_mode": null,
    "program_description": "The MSc in Environmental Sciences is a 30-credit, two-year program that combines core environmental science coursework, electives, and a thesis.",
    "admission_requirements": null,
    "gre_requirement": null,
    "gmat_requirement": null,
    "english_requirement": null,
    "interview_requirement": null,
    "experience_requirement": null,
    "accreditation": null,
    "official_program_url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/FASGraduate.pdf",
    "sources": [
      {
        "source_id": "uob_fas_graduate_catalogue_pdf",
        "page_title": "Faculty of Arts and Sciences Graduate Catalogue",
        "url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/FASGraduate.pdf",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_fas",
        "page_title": "Faculty of Arts and Sciences",
        "url": "https://www.balamand.edu.lb/faculties/FAS/Pages/default.aspx",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_catalogue_page",
        "page_title": "Catalogue",
        "url": "https://www.balamand.edu.lb/CurrentStudents/Pages/Catalogue.aspx",
        "date_accessed": "2026-06-28"
      }
    ],
    "tuition": {
      "academic_year": "2025-2026",
      "currency": "USD",
      "billing_basis": "PER_CREDIT",
      "amount": 705,
      "category": "Faculty of Arts and Sciences - Sciences",
      "notes": "Official UOB graduate fee table (2025-2026).",
      "source_ids": [
        "uob_fees_expenses_pdf"
      ]
    },
    "notes": "Listed in the FAS graduate catalogue; no separate program page surfaced during discovery. The FAS graduate catalogue specifies a 30-credit MSc in Environmental Sciences with a thesis and interdisciplinary electives."
  },
  {
    "id": "uob-fas-master-food-science-technology",
    "faculty": "Faculty of Arts and Sciences - Al-Kurah Campus",
    "department": "Graduate Program in Food Science and Technology",
    "major_category": "Sciences",
    "major": "Food Science and Technology",
    "degree_type": "MASTER",
    "official_degree_name": "Food Science and Technology M.S.",
    "thesis_or_non_thesis": "PROJECT",
    "concentrations_or_tracks": [
      "Food Quality Assurance",
      "Food Processing Control"
    ],
    "credits": 42,
    "duration_value": 2,
    "duration_unit": "YEARS",
    "duration_raw_text": "2 years",
    "language": "MULTILINGUAL",
    "delivery_mode": null,
    "program_description": "The Food Science and Technology MSc is a multidisciplinary, two-year graduate program for science and engineering backgrounds. It consists of 42 credits and culminates in a placement and research project.",
    "admission_requirements": null,
    "gre_requirement": null,
    "gmat_requirement": null,
    "english_requirement": null,
    "interview_requirement": null,
    "experience_requirement": null,
    "accreditation": null,
    "official_program_url": "https://www.balamand.edu.lb/faculties/FAS/Departments/Pages/FoodScience.aspx",
    "sources": [
      {
        "source_id": "uob_fas_food_science",
        "page_title": "Graduate Program in Food Science and Technology",
        "url": "https://www.balamand.edu.lb/faculties/FAS/Departments/Pages/FoodScience.aspx",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_fas_graduate_catalogue_pdf",
        "page_title": "Faculty of Arts and Sciences Graduate Catalogue",
        "url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/FASGraduate.pdf",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_fas",
        "page_title": "Faculty of Arts and Sciences",
        "url": "https://www.balamand.edu.lb/faculties/FAS/Pages/default.aspx",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_catalogue_page",
        "page_title": "Catalogue",
        "url": "https://www.balamand.edu.lb/CurrentStudents/Pages/Catalogue.aspx",
        "date_accessed": "2026-06-28"
      }
    ],
    "tuition": {
      "academic_year": "2025-2026",
      "currency": "USD",
      "billing_basis": "PER_CREDIT",
      "amount": 705,
      "category": "Faculty of Arts and Sciences - Sciences",
      "notes": "Official UOB graduate fee table (2025-2026).",
      "source_ids": [
        "uob_fees_expenses_pdf"
      ]
    },
    "notes": "Listed in the FAS graduate catalogue; no separate program page surfaced during discovery. The program now uses the dedicated Food Science page as the canonical URL; the official page confirms the two-year structure, 42 credits, and two concentration tracks."
  },
  {
    "id": "uob-fas-master-french-language-literature",
    "faculty": "Faculty of Arts and Sciences - Al-Kurah Campus",
    "department": "Department of French Language and Literature",
    "major_category": "Arts and Sciences",
    "major": "French Language and Literature",
    "degree_type": "MASTER",
    "official_degree_name": "French Language and Literature M.A.",
    "thesis_or_non_thesis": "THESIS",
    "concentrations_or_tracks": [
      "Literature",
      "Linguistics",
      "French as a Foreign and Second Language"
    ],
    "credits": 30,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": "FRENCH",
    "delivery_mode": null,
    "program_description": "The French Language and Literature MA is organized around literary, linguistic, and French-as-a-foreign-language components and requires a 6-credit research thesis.",
    "admission_requirements": null,
    "gre_requirement": null,
    "gmat_requirement": null,
    "english_requirement": null,
    "interview_requirement": null,
    "experience_requirement": null,
    "accreditation": null,
    "official_program_url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/FASGraduate.pdf",
    "sources": [
      {
        "source_id": "uob_fas_graduate_catalogue_pdf",
        "page_title": "Faculty of Arts and Sciences Graduate Catalogue",
        "url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/FASGraduate.pdf",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_fas",
        "page_title": "Faculty of Arts and Sciences",
        "url": "https://www.balamand.edu.lb/faculties/FAS/Pages/default.aspx",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_catalogue_page",
        "page_title": "Catalogue",
        "url": "https://www.balamand.edu.lb/CurrentStudents/Pages/Catalogue.aspx",
        "date_accessed": "2026-06-28"
      }
    ],
    "tuition": {
      "academic_year": "2025-2026",
      "currency": "USD",
      "billing_basis": "PER_CREDIT",
      "amount": 725,
      "category": "Faculty of Arts and Sciences - Arts",
      "notes": "Official UOB graduate fee table (2025-2026).",
      "source_ids": [
        "uob_fees_expenses_pdf"
      ]
    },
    "notes": "Listed in the FAS graduate catalogue; no separate program page surfaced during discovery. The French department describes the MA as having three options: literature, linguistics, and French as a foreign/second language."
  },
  {
    "id": "uob-fas-master-history",
    "faculty": "Faculty of Arts and Sciences - Al-Kurah Campus",
    "department": "Department of History",
    "major_category": "Arts and Sciences",
    "major": "History",
    "degree_type": "MASTER",
    "official_degree_name": "History M.A.",
    "thesis_or_non_thesis": "THESIS",
    "concentrations_or_tracks": [],
    "credits": 30,
    "duration_value": 2,
    "duration_unit": "YEARS",
    "duration_raw_text": "2 years",
    "language": "MULTILINGUAL",
    "delivery_mode": null,
    "program_description": "The MA in History places the program within the history of the Middle East and North Africa and focuses on historical methodology, critical analysis, and the experience of Lebanon and its region.",
    "admission_requirements": null,
    "gre_requirement": null,
    "gmat_requirement": null,
    "english_requirement": null,
    "interview_requirement": null,
    "experience_requirement": null,
    "accreditation": null,
    "official_program_url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/FASGraduate.pdf",
    "sources": [
      {
        "source_id": "uob_fas_graduate_catalogue_pdf",
        "page_title": "Faculty of Arts and Sciences Graduate Catalogue",
        "url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/FASGraduate.pdf",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_fas",
        "page_title": "Faculty of Arts and Sciences",
        "url": "https://www.balamand.edu.lb/faculties/FAS/Pages/default.aspx",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_catalogue_page",
        "page_title": "Catalogue",
        "url": "https://www.balamand.edu.lb/CurrentStudents/Pages/Catalogue.aspx",
        "date_accessed": "2026-06-28"
      }
    ],
    "tuition": {
      "academic_year": "2025-2026",
      "currency": "USD",
      "billing_basis": "PER_CREDIT",
      "amount": 725,
      "category": "Faculty of Arts and Sciences - Arts",
      "notes": "Official UOB graduate fee table (2025-2026).",
      "source_ids": [
        "uob_fees_expenses_pdf"
      ]
    },
    "notes": "Listed in the FAS graduate catalogue; no separate program page surfaced during discovery. The FAS graduate catalogue states that History is a 30-credit thesis program focused on the Middle East and North Africa."
  },
  {
    "id": "uob-fas-master-languages-translation",
    "faculty": "Faculty of Arts and Sciences - Al-Kurah Campus",
    "department": "Department of Languages and Translation",
    "major_category": "Arts and Sciences",
    "major": "Languages and Translation",
    "degree_type": "MASTER",
    "official_degree_name": "Languages, Translation M.A.",
    "thesis_or_non_thesis": "THESIS",
    "concentrations_or_tracks": [],
    "credits": 30,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": "MULTILINGUAL",
    "delivery_mode": null,
    "program_description": "The Master in Translation Studies prepares translators through training in Arabic, French, and English translation techniques, translation theory, terminology, technologies, and a research thesis.",
    "admission_requirements": null,
    "gre_requirement": null,
    "gmat_requirement": null,
    "english_requirement": null,
    "interview_requirement": null,
    "experience_requirement": null,
    "accreditation": null,
    "official_program_url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/FASGraduate.pdf",
    "sources": [
      {
        "source_id": "uob_fas_graduate_catalogue_pdf",
        "page_title": "Faculty of Arts and Sciences Graduate Catalogue",
        "url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/FASGraduate.pdf",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_fas",
        "page_title": "Faculty of Arts and Sciences",
        "url": "https://www.balamand.edu.lb/faculties/FAS/Pages/default.aspx",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_catalogue_page",
        "page_title": "Catalogue",
        "url": "https://www.balamand.edu.lb/CurrentStudents/Pages/Catalogue.aspx",
        "date_accessed": "2026-06-28"
      }
    ],
    "tuition": {
      "academic_year": "2025-2026",
      "currency": "USD",
      "billing_basis": "PER_CREDIT",
      "amount": 725,
      "category": "Faculty of Arts and Sciences - Arts",
      "notes": "Official UOB graduate fee table (2025-2026).",
      "source_ids": [
        "uob_fees_expenses_pdf"
      ]
    },
    "notes": "Listed in the FAS graduate catalogue; no separate program page surfaced during discovery. The Translation department states that the master in translation studies is a 30-credit degree taught across Arabic, French, and English."
  },
  {
    "id": "uob-fas-master-mass-media-communication",
    "faculty": "Faculty of Arts and Sciences - Al-Kurah Campus",
    "department": "Department of Mass Media and Communication",
    "major_category": "Arts and Sciences",
    "major": "Mass Media and Communication",
    "degree_type": "MASTER",
    "official_degree_name": "Mass Media and Communication M.A.",
    "thesis_or_non_thesis": "THESIS_OR_PROJECT",
    "concentrations_or_tracks": [],
    "credits": 30,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": "MULTILINGUAL",
    "delivery_mode": null,
    "program_description": "The MA in Mass Communication provides advanced knowledge and professional skills for careers in media, advertising, public relations, broadcasting, and scholarly research. Students complete either a final project/report or a thesis.",
    "admission_requirements": null,
    "gre_requirement": null,
    "gmat_requirement": null,
    "english_requirement": null,
    "interview_requirement": null,
    "experience_requirement": null,
    "accreditation": null,
    "official_program_url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/FASGraduate.pdf",
    "sources": [
      {
        "source_id": "uob_fas_graduate_catalogue_pdf",
        "page_title": "Faculty of Arts and Sciences Graduate Catalogue",
        "url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/FASGraduate.pdf",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_fas",
        "page_title": "Faculty of Arts and Sciences",
        "url": "https://www.balamand.edu.lb/faculties/FAS/Pages/default.aspx",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_catalogue_page",
        "page_title": "Catalogue",
        "url": "https://www.balamand.edu.lb/CurrentStudents/Pages/Catalogue.aspx",
        "date_accessed": "2026-06-28"
      }
    ],
    "tuition": {
      "academic_year": "2025-2026",
      "currency": "USD",
      "billing_basis": "PER_CREDIT",
      "amount": 725,
      "category": "Faculty of Arts and Sciences - Arts",
      "notes": "Official UOB graduate fee table (2025-2026).",
      "source_ids": [
        "uob_fees_expenses_pdf"
      ]
    },
    "notes": "Listed in the FAS graduate catalogue; no separate program page surfaced during discovery. The Mass Media and Communication department states that all MA students complete 30 credits and choose between a thesis and a project/report capstone."
  },
  {
    "id": "uob-fas-master-mathematics",
    "faculty": "Faculty of Arts and Sciences - Al-Kurah Campus",
    "department": "Department of Mathematics",
    "major_category": "Sciences",
    "major": "Mathematics",
    "degree_type": "MASTER",
    "official_degree_name": "Mathematics M.S.",
    "thesis_or_non_thesis": "THESIS_OR_PROJECT",
    "concentrations_or_tracks": [],
    "credits": 30,
    "duration_value": 4,
    "duration_unit": "SEMESTERS",
    "duration_raw_text": "4 semesters / 2 years",
    "language": "MULTILINGUAL",
    "delivery_mode": null,
    "program_description": "The Computational Mathematics MSc is a 4-semester, 30-credit degree that combines theoretical mathematics with computational methods, numerical simulation, and applications relevant to science and engineering.",
    "admission_requirements": null,
    "gre_requirement": null,
    "gmat_requirement": null,
    "english_requirement": null,
    "interview_requirement": null,
    "experience_requirement": null,
    "accreditation": null,
    "official_program_url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/FASGraduate.pdf",
    "sources": [
      {
        "source_id": "uob_fas_graduate_catalogue_pdf",
        "page_title": "Faculty of Arts and Sciences Graduate Catalogue",
        "url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/FASGraduate.pdf",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_fas",
        "page_title": "Faculty of Arts and Sciences",
        "url": "https://www.balamand.edu.lb/faculties/FAS/Pages/default.aspx",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_catalogue_page",
        "page_title": "Catalogue",
        "url": "https://www.balamand.edu.lb/CurrentStudents/Pages/Catalogue.aspx",
        "date_accessed": "2026-06-28"
      }
    ],
    "tuition": {
      "academic_year": "2025-2026",
      "currency": "USD",
      "billing_basis": "PER_CREDIT",
      "amount": 705,
      "category": "Faculty of Arts and Sciences - Sciences",
      "notes": "Official UOB graduate fee table (2025-2026).",
      "source_ids": [
        "uob_fees_expenses_pdf"
      ]
    },
    "notes": "Listed in the FAS graduate catalogue; no separate program page surfaced during discovery. The Mathematics department states that the program requires 30 credits and offers either a 6-credit thesis or a 3-credit project plus an elective."
  },
  {
    "id": "uob-fas-master-philosophy",
    "faculty": "Faculty of Arts and Sciences - Al-Kurah Campus",
    "department": "Department of Philosophy",
    "major_category": "Arts and Sciences",
    "major": "Philosophy",
    "degree_type": "MASTER",
    "official_degree_name": "Philosophy M.A.",
    "thesis_or_non_thesis": null,
    "concentrations_or_tracks": [],
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": "MULTILINGUAL",
    "delivery_mode": null,
    "program_description": "Graduate courses in philosophy cover God and metaphysics, contemporary philosophy, existentialism, philosophy and literature, post-modernism, classical Islamic philosophy, and modern Arab and Islamic thought.",
    "admission_requirements": null,
    "gre_requirement": null,
    "gmat_requirement": null,
    "english_requirement": null,
    "interview_requirement": null,
    "experience_requirement": null,
    "accreditation": null,
    "official_program_url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/FASGraduate.pdf",
    "sources": [
      {
        "source_id": "uob_fas_graduate_catalogue_pdf",
        "page_title": "Faculty of Arts and Sciences Graduate Catalogue",
        "url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/FASGraduate.pdf",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_fas",
        "page_title": "Faculty of Arts and Sciences",
        "url": "https://www.balamand.edu.lb/faculties/FAS/Pages/default.aspx",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_catalogue_page",
        "page_title": "Catalogue",
        "url": "https://www.balamand.edu.lb/CurrentStudents/Pages/Catalogue.aspx",
        "date_accessed": "2026-06-28"
      }
    ],
    "tuition": {
      "academic_year": "2025-2026",
      "currency": "USD",
      "billing_basis": "PER_CREDIT",
      "amount": 725,
      "category": "Faculty of Arts and Sciences - Arts",
      "notes": "Official UOB graduate fee table (2025-2026).",
      "source_ids": [
        "uob_fees_expenses_pdf"
      ]
    },
    "notes": "Listed in the FAS graduate catalogue; no separate program page surfaced during discovery. The philosophy graduate course list is catalog-based and remains the official source for the program."
  },
  {
    "id": "uob-fas-master-physical-education",
    "faculty": "Faculty of Arts and Sciences - Al-Kurah Campus",
    "department": "Department of Physical Education",
    "major_category": "Arts and Sciences",
    "major": "Physical Education",
    "degree_type": "MASTER",
    "official_degree_name": "Physical Education M.A.",
    "thesis_or_non_thesis": "THESIS",
    "concentrations_or_tracks": [
      "Physical Conditioning",
      "Physical Activity and Health"
    ],
    "credits": 30,
    "duration_value": 2,
    "duration_unit": "YEARS",
    "duration_raw_text": "2 years",
    "language": "MULTILINGUAL",
    "delivery_mode": null,
    "program_description": "The Physical Education MA includes graduate study in physical conditioning and in physical activity and health, with a 30-credit thesis-based curriculum and specialized sport science coursework.",
    "admission_requirements": null,
    "gre_requirement": null,
    "gmat_requirement": null,
    "english_requirement": null,
    "interview_requirement": null,
    "experience_requirement": null,
    "accreditation": null,
    "official_program_url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/FASGraduate.pdf",
    "sources": [
      {
        "source_id": "uob_fas_graduate_catalogue_pdf",
        "page_title": "Faculty of Arts and Sciences Graduate Catalogue",
        "url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/FASGraduate.pdf",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_fas",
        "page_title": "Faculty of Arts and Sciences",
        "url": "https://www.balamand.edu.lb/faculties/FAS/Pages/default.aspx",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_catalogue_page",
        "page_title": "Catalogue",
        "url": "https://www.balamand.edu.lb/CurrentStudents/Pages/Catalogue.aspx",
        "date_accessed": "2026-06-28"
      }
    ],
    "tuition": {
      "academic_year": "2025-2026",
      "currency": "USD",
      "billing_basis": "PER_CREDIT",
      "amount": 725,
      "category": "Faculty of Arts and Sciences - Arts",
      "notes": "Official UOB graduate fee table (2025-2026).",
      "source_ids": [
        "uob_fees_expenses_pdf"
      ]
    },
    "notes": "Listed in the FAS graduate catalogue; no separate program page surfaced during discovery. The Physical Education department describes two thesis-based tracks under the MA program: Physical Conditioning and Physical Activity and Health."
  },
  {
    "id": "uob-fas-master-political-science-international-affairs",
    "faculty": "Faculty of Arts and Sciences - Al-Kurah Campus",
    "department": "Department of Political Science and International Affairs",
    "major_category": "Arts and Sciences",
    "major": "Political Science and International Affairs",
    "degree_type": "MASTER",
    "official_degree_name": "Political Science and International Affairs M.A.",
    "thesis_or_non_thesis": "THESIS",
    "concentrations_or_tracks": [],
    "credits": 30,
    "duration_value": 2,
    "duration_unit": "YEARS",
    "duration_raw_text": "2 years",
    "language": "MULTILINGUAL",
    "delivery_mode": null,
    "program_description": "The MA in Middle Eastern and Mediterranean Politics focuses on critical understanding of politics, political analysis, conflict resolution, and Mediterranean and international affairs.",
    "admission_requirements": "A Bachelor degree in Political Science with a minimum average of 80 is normally required; applicants slightly below 80 may be accepted on probation; applicants from other disciplines may be accepted on probation with bridging courses.",
    "gre_requirement": null,
    "gmat_requirement": null,
    "english_requirement": null,
    "interview_requirement": null,
    "experience_requirement": null,
    "accreditation": null,
    "official_program_url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/FASGraduate.pdf",
    "sources": [
      {
        "source_id": "uob_fas_graduate_catalogue_pdf",
        "page_title": "Faculty of Arts and Sciences Graduate Catalogue",
        "url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/FASGraduate.pdf",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_fas",
        "page_title": "Faculty of Arts and Sciences",
        "url": "https://www.balamand.edu.lb/faculties/FAS/Pages/default.aspx",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_catalogue_page",
        "page_title": "Catalogue",
        "url": "https://www.balamand.edu.lb/CurrentStudents/Pages/Catalogue.aspx",
        "date_accessed": "2026-06-28"
      }
    ],
    "tuition": {
      "academic_year": "2025-2026",
      "currency": "USD",
      "billing_basis": "PER_CREDIT",
      "amount": 725,
      "category": "Faculty of Arts and Sciences - Arts",
      "notes": "Official UOB graduate fee table (2025-2026).",
      "source_ids": [
        "uob_fees_expenses_pdf"
      ]
    },
    "notes": "Listed in the FAS graduate catalogue; no separate program page surfaced during discovery. The department states that the degree requires 24 credits of coursework plus a 6-credit thesis and that graduate courses are normally offered as seminars."
  },
  {
    "id": "uob-fas-master-psychology",
    "faculty": "Faculty of Arts and Sciences - Al-Kurah Campus",
    "department": "Department of Psychology",
    "major_category": "Arts and Sciences",
    "major": "Psychology",
    "degree_type": "MASTER",
    "official_degree_name": "Psychology M.A.",
    "thesis_or_non_thesis": "THESIS",
    "concentrations_or_tracks": [
      "Clinical Developmental Psychology",
      "Clinical Psychology"
    ],
    "credits": 40,
    "duration_value": 2,
    "duration_unit": "YEARS",
    "duration_raw_text": "2 years",
    "language": "ENGLISH",
    "delivery_mode": null,
    "program_description": "The Psychology MA prepares students for clinical practice through a scholar-practitioner model with research thesis writing, research methodology, and a two-year clinical training curriculum.",
    "admission_requirements": null,
    "gre_requirement": null,
    "gmat_requirement": null,
    "english_requirement": null,
    "interview_requirement": null,
    "experience_requirement": null,
    "accreditation": null,
    "official_program_url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/FASGraduate.pdf",
    "sources": [
      {
        "source_id": "uob_fas_graduate_catalogue_pdf",
        "page_title": "Faculty of Arts and Sciences Graduate Catalogue",
        "url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/FASGraduate.pdf",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_fas",
        "page_title": "Faculty of Arts and Sciences",
        "url": "https://www.balamand.edu.lb/faculties/FAS/Pages/default.aspx",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_catalogue_page",
        "page_title": "Catalogue",
        "url": "https://www.balamand.edu.lb/CurrentStudents/Pages/Catalogue.aspx",
        "date_accessed": "2026-06-28"
      }
    ],
    "tuition": {
      "academic_year": "2025-2026",
      "currency": "USD",
      "billing_basis": "PER_CREDIT",
      "amount": 725,
      "category": "Faculty of Arts and Sciences - Arts",
      "notes": "Official UOB graduate fee table (2025-2026).",
      "source_ids": [
        "uob_fees_expenses_pdf"
      ]
    },
    "notes": "Listed in the FAS graduate catalogue; no separate program page surfaced during discovery. The Psychology department states that both clinical psychology tracks culminate in a substantive thesis and more than 720 hours of supervised clinical training."
  },
  {
    "id": "uob-fas-master-sports-management",
    "faculty": "Faculty of Arts and Sciences - Al-Kurah Campus",
    "department": "Department of Physical Education",
    "major_category": "Arts and Sciences",
    "major": "Sports Management",
    "degree_type": "MASTER",
    "official_degree_name": "Sports Management M.A.",
    "thesis_or_non_thesis": "PROJECT",
    "concentrations_or_tracks": [],
    "credits": 30,
    "duration_value": 2,
    "duration_unit": "YEARS",
    "duration_raw_text": "2 years",
    "language": "MULTILINGUAL",
    "delivery_mode": null,
    "program_description": "The Sports Management MA offers a business perspective on the sports industry and prepares graduates to manage sports businesses and organizations through a 30-credit curriculum and a field project.",
    "admission_requirements": "Candidates holding a BA in Physical Education or a BS in Business are eligible for the program.",
    "gre_requirement": null,
    "gmat_requirement": null,
    "english_requirement": null,
    "interview_requirement": null,
    "experience_requirement": null,
    "accreditation": null,
    "official_program_url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/FASGraduate.pdf",
    "sources": [
      {
        "source_id": "uob_fas_graduate_catalogue_pdf",
        "page_title": "Faculty of Arts and Sciences Graduate Catalogue",
        "url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/FASGraduate.pdf",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_fas",
        "page_title": "Faculty of Arts and Sciences",
        "url": "https://www.balamand.edu.lb/faculties/FAS/Pages/default.aspx",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_catalogue_page",
        "page_title": "Catalogue",
        "url": "https://www.balamand.edu.lb/CurrentStudents/Pages/Catalogue.aspx",
        "date_accessed": "2026-06-28"
      }
    ],
    "tuition": {
      "academic_year": "2025-2026",
      "currency": "USD",
      "billing_basis": "PER_CREDIT",
      "amount": 725,
      "category": "Faculty of Arts and Sciences - Arts",
      "notes": "Official UOB graduate fee table (2025-2026).",
      "source_ids": [
        "uob_fees_expenses_pdf"
      ]
    },
    "notes": "Listed in the FAS graduate catalogue; no separate program page surfaced during discovery. The department states that Sports Management is a 30-credit program and that the field project is the capstone, with eligibility for BA Physical Education or BS Business holders."
  },
  {
    "id": "uob-fas-master-education-aley",
    "faculty": "Faculty of Arts and Sciences - Souk El Gharb-Aley Campus",
    "department": "Department of Education",
    "major_category": "Arts and Sciences",
    "major": "Education",
    "degree_type": "MASTER",
    "official_degree_name": "Education M.A.",
    "thesis_or_non_thesis": "THESIS_OR_PROJECT",
    "concentrations_or_tracks": [
      "Curriculum and Educational Management",
      "Instructional Design and Technology"
    ],
    "credits": 30,
    "duration_value": 2,
    "duration_unit": "YEARS",
    "duration_raw_text": "2 years",
    "language": "MULTILINGUAL",
    "delivery_mode": null,
    "program_description": "The Education MA is offered through two graduate tracks: Curriculum and Educational Management, and Instructional Design and Technology.",
    "admission_requirements": null,
    "gre_requirement": null,
    "gmat_requirement": null,
    "english_requirement": null,
    "interview_requirement": null,
    "experience_requirement": null,
    "accreditation": null,
    "official_program_url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/FASGraduate.pdf",
    "sources": [
      {
        "source_id": "uob_fas_graduate_catalogue_pdf",
        "page_title": "Faculty of Arts and Sciences Graduate Catalogue",
        "url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/FASGraduate.pdf",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_fas",
        "page_title": "Faculty of Arts and Sciences",
        "url": "https://www.balamand.edu.lb/faculties/FAS/Pages/default.aspx",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_catalogue_page",
        "page_title": "Catalogue",
        "url": "https://www.balamand.edu.lb/CurrentStudents/Pages/Catalogue.aspx",
        "date_accessed": "2026-06-28"
      }
    ],
    "tuition": {
      "academic_year": "2025-2026",
      "currency": "USD",
      "billing_basis": "PER_CREDIT",
      "amount": 725,
      "category": "Faculty of Arts and Sciences - Arts",
      "notes": "Official UOB graduate fee table (2025-2026).",
      "source_ids": [
        "uob_fees_expenses_pdf"
      ]
    },
    "notes": "The graduate catalogue lists Education M.A. at the Souk El Gharb-Aley Campus as a separate campus offering. The Aley campus listing is retained separately because the catalogue lists it as a campus-specific Education MA offering."
  },
  {
    "id": "uob-fobm-master-accounting-finance",
    "faculty": "Faculty of Business and Management - Al-Kurah Campus",
    "department": null,
    "major_category": "Business and Management",
    "major": "Accounting and Finance",
    "degree_type": "MASTER",
    "official_degree_name": "Accounting and Finance Master.",
    "thesis_or_non_thesis": null,
    "concentrations_or_tracks": [],
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": "ENGLISH",
    "delivery_mode": null,
    "program_description": null,
    "admission_requirements": null,
    "gre_requirement": null,
    "gmat_requirement": null,
    "english_requirement": null,
    "interview_requirement": null,
    "experience_requirement": null,
    "accreditation": null,
    "official_program_url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/General-Section-Graduate.pdf",
    "sources": [
      {
        "source_id": "uob_fobm",
        "page_title": "Faculty of Business and Management",
        "url": "https://www.balamand.edu.lb/faculties/FOBM/Pages/default.aspx",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_general_graduate_catalogue_pdf",
        "page_title": "General Section Graduate Catalogue",
        "url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/General-Section-Graduate.pdf",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_catalogue_page",
        "page_title": "Catalogue",
        "url": "https://www.balamand.edu.lb/CurrentStudents/Pages/Catalogue.aspx",
        "date_accessed": "2026-06-28"
      }
    ],
    "tuition": {
      "academic_year": "2025-2026",
      "currency": "USD",
      "billing_basis": "PER_CREDIT",
      "amount": 635,
      "category": "Faculty of Business and Management",
      "notes": "Official UOB graduate fee table (2025-2026).",
      "source_ids": [
        "uob_fees_expenses_pdf"
      ]
    },
    "notes": "Listed in the general graduate catalogue and on the FOBM hub; no separate program page surfaced during discovery."
  },
  {
    "id": "uob-fobm-mba",
    "faculty": "Faculty of Business and Management - Al-Kurah Campus",
    "department": null,
    "major_category": "Business and Management",
    "major": "Business Administration",
    "degree_type": "MASTER",
    "official_degree_name": "Master in Business Administration (MBA)",
    "thesis_or_non_thesis": "THESIS_OR_PROJECT",
    "concentrations_or_tracks": [
      "Accounting",
      "Finance",
      "Health Care Management",
      "Human Resources Management",
      "Management",
      "Marketing"
    ],
    "credits": 39,
    "duration_value": 1.5,
    "duration_unit": "YEARS",
    "duration_raw_text": "1.5-2 years",
    "language": "ENGLISH",
    "delivery_mode": null,
    "program_description": "The MBA program develops influential leaders and managers for complex business environments. The curriculum includes 21 core credits and 18 remaining credits that can be completed through elective-and-project or elective-and-thesis tracks.",
    "admission_requirements": null,
    "gre_requirement": null,
    "gmat_requirement": null,
    "english_requirement": null,
    "interview_requirement": null,
    "experience_requirement": null,
    "accreditation": null,
    "official_program_url": "https://www.balamand.edu.lb/faculties/FOBM/Departments/Pages/MBA.aspx",
    "sources": [
      {
        "source_id": "uob_fobm_mba",
        "page_title": "Master of Business Administration (MBA)",
        "url": "https://www.balamand.edu.lb/faculties/FOBM/Departments/Pages/MBA.aspx",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_fobm",
        "page_title": "Faculty of Business and Management",
        "url": "https://www.balamand.edu.lb/faculties/FOBM/Pages/default.aspx",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_general_graduate_catalogue_pdf",
        "page_title": "General Section Graduate Catalogue",
        "url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/General-Section-Graduate.pdf",
        "date_accessed": "2026-06-28"
      }
    ],
    "tuition": {
      "academic_year": "2025-2026",
      "currency": "USD",
      "billing_basis": "PER_CREDIT",
      "amount": 635,
      "category": "Faculty of Business and Management",
      "notes": "Official UOB graduate fee table (2025-2026).",
      "source_ids": [
        "uob_fees_expenses_pdf"
      ]
    },
    "notes": "Official MBA page lists 39 credits, a 1.5-2 year duration, and six concentration tracks; the graduate catalogue and faculty hub also list the program. Official MBA page confirms 39 credits, a 1.5-2 year duration, and six optional concentration tracks."
  },
  {
    "id": "uob-fobm-emba",
    "faculty": "Faculty of Business and Management - Al-Kurah Campus",
    "department": null,
    "major_category": "Business and Management",
    "major": "Executive Business Administration",
    "degree_type": "MASTER",
    "official_degree_name": "Executive Master in Business Administration (EMBA)",
    "thesis_or_non_thesis": null,
    "concentrations_or_tracks": [],
    "credits": 39,
    "duration_value": 19,
    "duration_unit": "MONTHS",
    "duration_raw_text": "19 months",
    "language": "ENGLISH",
    "delivery_mode": null,
    "program_description": "The EMBA is a module-based executive program themed Navigating Crises. It is designed for seasoned managers and entrepreneurs and consists of 19 monthly modules, including one three-credit opening module and eighteen two-credit modules.",
    "admission_requirements": "Bachelor degree licensed from the Ministry of Education; five years of managerial experience; completed application with required documents; English proficiency; interview with the program committee.",
    "gre_requirement": null,
    "gmat_requirement": null,
    "english_requirement": null,
    "interview_requirement": "Interview with the EMBA admission committee",
    "experience_requirement": "Five years of managerial experience",
    "accreditation": null,
    "official_program_url": "https://www.balamand.edu.lb/faculties/FOBM/Departments/Pages/EMBA.aspx",
    "sources": [
      {
        "source_id": "uob_fobm_emba",
        "page_title": "Executive Master of Business Administration (EMBA)",
        "url": "https://www.balamand.edu.lb/faculties/FOBM/Departments/Pages/EMBA.aspx",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_fobm",
        "page_title": "Faculty of Business and Management",
        "url": "https://www.balamand.edu.lb/faculties/FOBM/Pages/default.aspx",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_general_graduate_catalogue_pdf",
        "page_title": "General Section Graduate Catalogue",
        "url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/General-Section-Graduate.pdf",
        "date_accessed": "2026-06-28"
      }
    ],
    "tuition": {
      "academic_year": "2025-2026",
      "currency": "USD",
      "billing_basis": "PER_ACADEMIC_YEAR",
      "amount": 25500,
      "category": "Executive Master of Business Administration",
      "notes": "Official UOB fee table lists Executive MBA as yearly tuition (2025-2026).",
      "source_ids": [
        "uob_fees_expenses_pdf"
      ]
    },
    "notes": "Official EMBA page confirms the program; it is also listed in the graduate catalogue and FOBM hub. Official EMBA page confirms the module-based structure, 19-month completion time, admission criteria, required documents, and interview requirement."
  },
  {
    "id": "uob-fobm-master-financial-economics",
    "faculty": "Faculty of Business and Management - Al-Kurah Campus",
    "department": null,
    "major_category": "Business and Management",
    "major": "Financial Economics",
    "degree_type": "MASTER",
    "official_degree_name": "Financial Economics M.S.",
    "thesis_or_non_thesis": null,
    "concentrations_or_tracks": [],
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": "ENGLISH",
    "delivery_mode": null,
    "program_description": null,
    "admission_requirements": null,
    "gre_requirement": null,
    "gmat_requirement": null,
    "english_requirement": null,
    "interview_requirement": null,
    "experience_requirement": null,
    "accreditation": null,
    "official_program_url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/General-Section-Graduate.pdf",
    "sources": [
      {
        "source_id": "uob_fobm",
        "page_title": "Faculty of Business and Management",
        "url": "https://www.balamand.edu.lb/faculties/FOBM/Pages/default.aspx",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_general_graduate_catalogue_pdf",
        "page_title": "General Section Graduate Catalogue",
        "url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/General-Section-Graduate.pdf",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_catalogue_page",
        "page_title": "Catalogue",
        "url": "https://www.balamand.edu.lb/CurrentStudents/Pages/Catalogue.aspx",
        "date_accessed": "2026-06-28"
      }
    ],
    "tuition": {
      "academic_year": "2025-2026",
      "currency": "USD",
      "billing_basis": "PER_CREDIT",
      "amount": 635,
      "category": "Faculty of Business and Management",
      "notes": "Official UOB graduate fee table (2025-2026).",
      "source_ids": [
        "uob_fees_expenses_pdf"
      ]
    },
    "notes": "Listed in the general graduate catalogue and on the FOBM hub; no separate program page surfaced during discovery."
  },
  {
    "id": "uob-fobm-master-human-resources-management",
    "faculty": "Faculty of Business and Management - Al-Kurah Campus",
    "department": null,
    "major_category": "Business and Management",
    "major": "Human Resources Management",
    "degree_type": "MASTER",
    "official_degree_name": "Human Resources Management Master.",
    "thesis_or_non_thesis": null,
    "concentrations_or_tracks": [],
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": "ENGLISH",
    "delivery_mode": null,
    "program_description": null,
    "admission_requirements": null,
    "gre_requirement": null,
    "gmat_requirement": null,
    "english_requirement": null,
    "interview_requirement": null,
    "experience_requirement": null,
    "accreditation": null,
    "official_program_url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/General-Section-Graduate.pdf",
    "sources": [
      {
        "source_id": "uob_fobm",
        "page_title": "Faculty of Business and Management",
        "url": "https://www.balamand.edu.lb/faculties/FOBM/Pages/default.aspx",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_general_graduate_catalogue_pdf",
        "page_title": "General Section Graduate Catalogue",
        "url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/General-Section-Graduate.pdf",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_catalogue_page",
        "page_title": "Catalogue",
        "url": "https://www.balamand.edu.lb/CurrentStudents/Pages/Catalogue.aspx",
        "date_accessed": "2026-06-28"
      }
    ],
    "tuition": {
      "academic_year": "2025-2026",
      "currency": "USD",
      "billing_basis": "PER_CREDIT",
      "amount": 635,
      "category": "Faculty of Business and Management",
      "notes": "Official UOB graduate fee table (2025-2026).",
      "source_ids": [
        "uob_fees_expenses_pdf"
      ]
    },
    "notes": "Listed in the general graduate catalogue and on the FOBM hub; no separate program page surfaced during discovery."
  },
  {
    "id": "uob-foe-master-chemical-engineering",
    "faculty": "Faculty of Engineering - Al-Kurah Campus",
    "department": null,
    "major_category": "Engineering",
    "major": "Chemical Engineering",
    "degree_type": "MASTER",
    "official_degree_name": "Chemical Engineering M.S.",
    "thesis_or_non_thesis": null,
    "concentrations_or_tracks": [],
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": "ENGLISH",
    "delivery_mode": null,
    "program_description": null,
    "admission_requirements": null,
    "gre_requirement": null,
    "gmat_requirement": null,
    "english_requirement": null,
    "interview_requirement": null,
    "experience_requirement": null,
    "accreditation": null,
    "official_program_url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/General-Section-Graduate.pdf",
    "sources": [
      {
        "source_id": "uob_foe",
        "page_title": "Faculty of Engineering",
        "url": "https://www.balamand.edu.lb/faculties/FOE/Pages/default.aspx",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_general_graduate_catalogue_pdf",
        "page_title": "General Section Graduate Catalogue",
        "url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/General-Section-Graduate.pdf",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_catalogue_page",
        "page_title": "Catalogue",
        "url": "https://www.balamand.edu.lb/CurrentStudents/Pages/Catalogue.aspx",
        "date_accessed": "2026-06-28"
      }
    ],
    "tuition": {
      "academic_year": "2025-2026",
      "currency": "USD",
      "billing_basis": "PER_CREDIT",
      "amount": 540,
      "category": "Faculty of Engineering",
      "notes": "Official UOB graduate fee table (2025-2026).",
      "source_ids": [
        "uob_fees_expenses_pdf"
      ]
    },
    "notes": "Listed in the general graduate catalogue and reachable from the Faculty of Engineering hub; no individual graduate program page was canonicalized in this pass."
  },
  {
    "id": "uob-foe-master-civil-engineering",
    "faculty": "Faculty of Engineering - Al-Kurah Campus",
    "department": null,
    "major_category": "Engineering",
    "major": "Civil Engineering",
    "degree_type": "MASTER",
    "official_degree_name": "Civil Engineering M.S.",
    "thesis_or_non_thesis": null,
    "concentrations_or_tracks": [],
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": "ENGLISH",
    "delivery_mode": null,
    "program_description": null,
    "admission_requirements": null,
    "gre_requirement": null,
    "gmat_requirement": null,
    "english_requirement": null,
    "interview_requirement": null,
    "experience_requirement": null,
    "accreditation": null,
    "official_program_url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/General-Section-Graduate.pdf",
    "sources": [
      {
        "source_id": "uob_foe",
        "page_title": "Faculty of Engineering",
        "url": "https://www.balamand.edu.lb/faculties/FOE/Pages/default.aspx",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_general_graduate_catalogue_pdf",
        "page_title": "General Section Graduate Catalogue",
        "url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/General-Section-Graduate.pdf",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_catalogue_page",
        "page_title": "Catalogue",
        "url": "https://www.balamand.edu.lb/CurrentStudents/Pages/Catalogue.aspx",
        "date_accessed": "2026-06-28"
      }
    ],
    "tuition": {
      "academic_year": "2025-2026",
      "currency": "USD",
      "billing_basis": "PER_CREDIT",
      "amount": 540,
      "category": "Faculty of Engineering",
      "notes": "Official UOB graduate fee table (2025-2026).",
      "source_ids": [
        "uob_fees_expenses_pdf"
      ]
    },
    "notes": "Listed in the general graduate catalogue and reachable from the Faculty of Engineering hub; no individual graduate program page was canonicalized in this pass."
  },
  {
    "id": "uob-foe-master-computer-engineering",
    "faculty": "Faculty of Engineering - Al-Kurah Campus",
    "department": null,
    "major_category": "Engineering",
    "major": "Computer Engineering",
    "degree_type": "MASTER",
    "official_degree_name": "Computer Engineering M.S.",
    "thesis_or_non_thesis": null,
    "concentrations_or_tracks": [],
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": "ENGLISH",
    "delivery_mode": null,
    "program_description": null,
    "admission_requirements": null,
    "gre_requirement": null,
    "gmat_requirement": null,
    "english_requirement": null,
    "interview_requirement": null,
    "experience_requirement": null,
    "accreditation": null,
    "official_program_url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/General-Section-Graduate.pdf",
    "sources": [
      {
        "source_id": "uob_foe",
        "page_title": "Faculty of Engineering",
        "url": "https://www.balamand.edu.lb/faculties/FOE/Pages/default.aspx",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_general_graduate_catalogue_pdf",
        "page_title": "General Section Graduate Catalogue",
        "url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/General-Section-Graduate.pdf",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_catalogue_page",
        "page_title": "Catalogue",
        "url": "https://www.balamand.edu.lb/CurrentStudents/Pages/Catalogue.aspx",
        "date_accessed": "2026-06-28"
      }
    ],
    "tuition": {
      "academic_year": "2025-2026",
      "currency": "USD",
      "billing_basis": "PER_CREDIT",
      "amount": 540,
      "category": "Faculty of Engineering",
      "notes": "Official UOB graduate fee table (2025-2026).",
      "source_ids": [
        "uob_fees_expenses_pdf"
      ]
    },
    "notes": "Listed in the general graduate catalogue and reachable from the Faculty of Engineering hub; no individual graduate program page was canonicalized in this pass."
  },
  {
    "id": "uob-foe-master-electrical-engineering",
    "faculty": "Faculty of Engineering - Al-Kurah Campus",
    "department": null,
    "major_category": "Engineering",
    "major": "Electrical Engineering",
    "degree_type": "MASTER",
    "official_degree_name": "Electrical Engineering M.S.",
    "thesis_or_non_thesis": null,
    "concentrations_or_tracks": [],
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": "ENGLISH",
    "delivery_mode": null,
    "program_description": null,
    "admission_requirements": null,
    "gre_requirement": null,
    "gmat_requirement": null,
    "english_requirement": null,
    "interview_requirement": null,
    "experience_requirement": null,
    "accreditation": null,
    "official_program_url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/General-Section-Graduate.pdf",
    "sources": [
      {
        "source_id": "uob_foe",
        "page_title": "Faculty of Engineering",
        "url": "https://www.balamand.edu.lb/faculties/FOE/Pages/default.aspx",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_general_graduate_catalogue_pdf",
        "page_title": "General Section Graduate Catalogue",
        "url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/General-Section-Graduate.pdf",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_catalogue_page",
        "page_title": "Catalogue",
        "url": "https://www.balamand.edu.lb/CurrentStudents/Pages/Catalogue.aspx",
        "date_accessed": "2026-06-28"
      }
    ],
    "tuition": {
      "academic_year": "2025-2026",
      "currency": "USD",
      "billing_basis": "PER_CREDIT",
      "amount": 540,
      "category": "Faculty of Engineering",
      "notes": "Official UOB graduate fee table (2025-2026).",
      "source_ids": [
        "uob_fees_expenses_pdf"
      ]
    },
    "notes": "Listed in the general graduate catalogue and reachable from the Faculty of Engineering hub; no individual graduate program page was canonicalized in this pass."
  },
  {
    "id": "uob-foe-master-engineering-management",
    "faculty": "Faculty of Engineering - Al-Kurah Campus",
    "department": null,
    "major_category": "Engineering",
    "major": "Engineering Management",
    "degree_type": "MASTER",
    "official_degree_name": "Engineering Management M.S.",
    "thesis_or_non_thesis": null,
    "concentrations_or_tracks": [],
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": "ENGLISH",
    "delivery_mode": null,
    "program_description": null,
    "admission_requirements": null,
    "gre_requirement": null,
    "gmat_requirement": null,
    "english_requirement": null,
    "interview_requirement": null,
    "experience_requirement": null,
    "accreditation": null,
    "official_program_url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/General-Section-Graduate.pdf",
    "sources": [
      {
        "source_id": "uob_foe",
        "page_title": "Faculty of Engineering",
        "url": "https://www.balamand.edu.lb/faculties/FOE/Pages/default.aspx",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_general_graduate_catalogue_pdf",
        "page_title": "General Section Graduate Catalogue",
        "url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/General-Section-Graduate.pdf",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_catalogue_page",
        "page_title": "Catalogue",
        "url": "https://www.balamand.edu.lb/CurrentStudents/Pages/Catalogue.aspx",
        "date_accessed": "2026-06-28"
      }
    ],
    "tuition": {
      "academic_year": "2025-2026",
      "currency": "USD",
      "billing_basis": "PER_CREDIT",
      "amount": 540,
      "category": "Faculty of Engineering",
      "notes": "Official UOB graduate fee table (2025-2026).",
      "source_ids": [
        "uob_fees_expenses_pdf"
      ]
    },
    "notes": "Listed in the general graduate catalogue and reachable from the Faculty of Engineering hub; no individual graduate program page was canonicalized in this pass."
  },
  {
    "id": "uob-foe-master-environmental-engineering",
    "faculty": "Faculty of Engineering - Al-Kurah Campus",
    "department": null,
    "major_category": "Engineering",
    "major": "Environmental Engineering",
    "degree_type": "MASTER",
    "official_degree_name": "Environmental Engineering M.S.",
    "thesis_or_non_thesis": null,
    "concentrations_or_tracks": [],
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": "ENGLISH",
    "delivery_mode": null,
    "program_description": null,
    "admission_requirements": null,
    "gre_requirement": null,
    "gmat_requirement": null,
    "english_requirement": null,
    "interview_requirement": null,
    "experience_requirement": null,
    "accreditation": null,
    "official_program_url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/General-Section-Graduate.pdf",
    "sources": [
      {
        "source_id": "uob_foe",
        "page_title": "Faculty of Engineering",
        "url": "https://www.balamand.edu.lb/faculties/FOE/Pages/default.aspx",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_general_graduate_catalogue_pdf",
        "page_title": "General Section Graduate Catalogue",
        "url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/General-Section-Graduate.pdf",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_catalogue_page",
        "page_title": "Catalogue",
        "url": "https://www.balamand.edu.lb/CurrentStudents/Pages/Catalogue.aspx",
        "date_accessed": "2026-06-28"
      }
    ],
    "tuition": {
      "academic_year": "2025-2026",
      "currency": "USD",
      "billing_basis": "PER_CREDIT",
      "amount": 540,
      "category": "Faculty of Engineering",
      "notes": "Official UOB graduate fee table (2025-2026).",
      "source_ids": [
        "uob_fees_expenses_pdf"
      ]
    },
    "notes": "Listed in the general graduate catalogue and reachable from the Faculty of Engineering hub; no individual graduate program page was canonicalized in this pass."
  },
  {
    "id": "uob-foe-master-mechanical-engineering",
    "faculty": "Faculty of Engineering - Al-Kurah Campus",
    "department": null,
    "major_category": "Engineering",
    "major": "Mechanical Engineering",
    "degree_type": "MASTER",
    "official_degree_name": "Mechanical Engineering M.S.",
    "thesis_or_non_thesis": null,
    "concentrations_or_tracks": [],
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": "ENGLISH",
    "delivery_mode": null,
    "program_description": null,
    "admission_requirements": null,
    "gre_requirement": null,
    "gmat_requirement": null,
    "english_requirement": null,
    "interview_requirement": null,
    "experience_requirement": null,
    "accreditation": null,
    "official_program_url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/General-Section-Graduate.pdf",
    "sources": [
      {
        "source_id": "uob_foe",
        "page_title": "Faculty of Engineering",
        "url": "https://www.balamand.edu.lb/faculties/FOE/Pages/default.aspx",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_general_graduate_catalogue_pdf",
        "page_title": "General Section Graduate Catalogue",
        "url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/General-Section-Graduate.pdf",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_catalogue_page",
        "page_title": "Catalogue",
        "url": "https://www.balamand.edu.lb/CurrentStudents/Pages/Catalogue.aspx",
        "date_accessed": "2026-06-28"
      }
    ],
    "tuition": {
      "academic_year": "2025-2026",
      "currency": "USD",
      "billing_basis": "PER_CREDIT",
      "amount": 540,
      "category": "Faculty of Engineering",
      "notes": "Official UOB graduate fee table (2025-2026).",
      "source_ids": [
        "uob_fees_expenses_pdf"
      ]
    },
    "notes": "Listed in the general graduate catalogue and reachable from the Faculty of Engineering hub; no individual graduate program page was canonicalized in this pass."
  },
  {
    "id": "uob-fhs-master-clinical-laboratory-sciences-lab-management",
    "faculty": "Faculty of Health Sciences - Dekouaneh Campus",
    "department": "Department of Medical Laboratory Sciences",
    "major_category": "Health Sciences",
    "major": "Clinical Laboratory Sciences (Lab Management)",
    "degree_type": "MASTER",
    "official_degree_name": "Clinical Laboratory Sciences (Lab Management) M.S.",
    "thesis_or_non_thesis": "NON_THESIS",
    "concentrations_or_tracks": [
      "Laboratory Management"
    ],
    "credits": 34,
    "duration_value": 2,
    "duration_unit": "YEARS",
    "duration_raw_text": "2 years",
    "language": "ENGLISH",
    "delivery_mode": null,
    "program_description": "The professional MS-CLS laboratory-management track provides focused academic training and internship-based preparation for laboratory operations, management, and industry roles.",
    "admission_requirements": null,
    "gre_requirement": null,
    "gmat_requirement": null,
    "english_requirement": null,
    "interview_requirement": null,
    "experience_requirement": null,
    "accreditation": null,
    "official_program_url": "https://www.balamand.edu.lb/faculties/FHS/AcademicPrograms/Pages/Programs/MSClinicalLabSciences.aspx",
    "sources": [
      {
        "source_id": "uob_fhs_ms_cls",
        "page_title": "MS in Clinical Laboratory Sciences",
        "url": "https://www.balamand.edu.lb/faculties/FHS/AcademicPrograms/Pages/Programs/MSClinicalLabSciences.aspx",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_fhs",
        "page_title": "Faculty of Health Sciences",
        "url": "https://www.balamand.edu.lb/faculties/FHS/Pages/Default.aspx",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_fhs_graduate_catalogue_pdf",
        "page_title": "Faculty of Health Sciences Graduate Catalogue",
        "url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/FHSGraduate.pdf",
        "date_accessed": "2026-06-28"
      }
    ],
    "tuition": {
      "academic_year": "2025-2026",
      "currency": "USD",
      "billing_basis": "PER_CREDIT",
      "amount": 715,
      "category": "Faculty of Health Sciences",
      "notes": "Official UOB graduate fee table (2025-2026).",
      "source_ids": [
        "uob_fees_expenses_pdf"
      ]
    },
    "notes": "Official FHS graduate page exists; the catalogue uses the lab-management wording. Official MS-CLS page identifies the Laboratory Management professional track, which carries 34 credits and uses an internship capstone. Official MS-CLS page confirms the Laboratory Management professional track and its internship-based capstone."
  },
  {
    "id": "uob-fhs-master-medical-laboratory-sciences",
    "faculty": "Faculty of Health Sciences - Dekouaneh Campus",
    "department": "Department of Medical Laboratory Sciences",
    "major_category": "Health Sciences",
    "major": "Medical Laboratory Sciences",
    "degree_type": "MASTER",
    "official_degree_name": "Medical Laboratory Sciences M.S.",
    "thesis_or_non_thesis": "THESIS",
    "concentrations_or_tracks": [
      "Infectious Diseases and Immunology",
      "Molecular Biology"
    ],
    "credits": 30,
    "duration_value": 2,
    "duration_unit": "YEARS",
    "duration_raw_text": "2 years",
    "language": "ENGLISH",
    "delivery_mode": null,
    "program_description": "The thesis track in clinical laboratory sciences deepens expertise in molecular biology, infectious diseases, immunology, and related laboratory research.",
    "admission_requirements": null,
    "gre_requirement": null,
    "gmat_requirement": null,
    "english_requirement": null,
    "interview_requirement": null,
    "experience_requirement": null,
    "accreditation": null,
    "official_program_url": "https://www.balamand.edu.lb/faculties/FHS/AcademicPrograms/Pages/Programs/MSClinicalLabSciences.aspx",
    "sources": [
      {
        "source_id": "uob_fhs_ms_cls",
        "page_title": "MS in Clinical Laboratory Sciences",
        "url": "https://www.balamand.edu.lb/faculties/FHS/AcademicPrograms/Pages/Programs/MSClinicalLabSciences.aspx",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_fhs",
        "page_title": "Faculty of Health Sciences",
        "url": "https://www.balamand.edu.lb/faculties/FHS/Pages/Default.aspx",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_fhs_graduate_catalogue_pdf",
        "page_title": "Faculty of Health Sciences Graduate Catalogue",
        "url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/FHSGraduate.pdf",
        "date_accessed": "2026-06-28"
      }
    ],
    "tuition": {
      "academic_year": "2025-2026",
      "currency": "USD",
      "billing_basis": "PER_CREDIT",
      "amount": 715,
      "category": "Faculty of Health Sciences",
      "notes": "Official UOB graduate fee table (2025-2026).",
      "source_ids": [
        "uob_fees_expenses_pdf"
      ]
    },
    "notes": "Listed in the graduate catalogue and the FHS hub; no separate canonical program page was resolved during discovery. Updated to the thesis-track wording from the official MS-CLS page and handbook; the page lists Molecular Biology and Infectious Diseases and Immunology thesis tracks."
  },
  {
    "id": "uob-fhs-master-nursing",
    "faculty": "Faculty of Health Sciences - Dekouaneh Campus",
    "department": "Department of Nursing",
    "major_category": "Health Sciences",
    "major": "Nursing",
    "degree_type": "MASTER",
    "official_degree_name": "MS in Nursing",
    "thesis_or_non_thesis": "THESIS_OR_PROJECT",
    "concentrations_or_tracks": [
      "Adult Care",
      "Neonatal and Child Care"
    ],
    "credits": 36,
    "duration_value": 2,
    "duration_unit": "YEARS",
    "duration_raw_text": "2 years",
    "language": "ENGLISH",
    "delivery_mode": null,
    "program_description": "The MSN is an advanced research-based and clinical training degree that prepares qualified nurses for leadership and specialized practice in neonatal/child care and adult care.",
    "admission_requirements": null,
    "gre_requirement": null,
    "gmat_requirement": null,
    "english_requirement": null,
    "interview_requirement": null,
    "experience_requirement": null,
    "accreditation": "Candidate for initial accreditation by the Accreditation Commission for Education in Nursing (ACEN); candidacy effective December 20, 2022 and noted on the program page as expiring December 20, 2024.",
    "official_program_url": "https://www.balamand.edu.lb/faculties/FHS/AcademicPrograms/Pages/Programs/MSNursing.aspx",
    "sources": [
      {
        "source_id": "uob_fhs_ms_nursing",
        "page_title": "MS in Nursing",
        "url": "https://www.balamand.edu.lb/faculties/FHS/AcademicPrograms/Pages/Programs/MSNursing.aspx",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_fhs",
        "page_title": "Faculty of Health Sciences",
        "url": "https://www.balamand.edu.lb/faculties/FHS/Pages/Default.aspx",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_fhs_graduate_catalogue_pdf",
        "page_title": "Faculty of Health Sciences Graduate Catalogue",
        "url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/FHSGraduate.pdf",
        "date_accessed": "2026-06-28"
      }
    ],
    "tuition": {
      "academic_year": "2025-2026",
      "currency": "USD",
      "billing_basis": "PER_CREDIT",
      "amount": 715,
      "category": "Faculty of Health Sciences",
      "notes": "Official UOB graduate fee table (2025-2026).",
      "source_ids": [
        "uob_fees_expenses_pdf"
      ]
    },
    "notes": "Official nursing page states two fields of specialization; the FHS hub also lists the program. Official nursing page confirms the two tracks, 36-credit structure, thesis or project option, two-year completion time, and ACEN candidacy notice."
  },
  {
    "id": "uob-fhs-master-public-health",
    "faculty": "Faculty of Health Sciences - Dekouaneh Campus",
    "department": "Department of Public Health",
    "major_category": "Health Sciences",
    "major": "Public Health",
    "degree_type": "MASTER",
    "official_degree_name": "Master of Public Health (MPH)",
    "thesis_or_non_thesis": "NON_THESIS",
    "concentrations_or_tracks": [
      "Community Health",
      "Occupational and Environmental Health"
    ],
    "credits": 42,
    "duration_value": 20,
    "duration_unit": "MONTHS",
    "duration_raw_text": "Minimum 20 months full-time; up to 4 years part-time",
    "language": "ENGLISH",
    "delivery_mode": null,
    "program_description": "The MPH is a 42-credit practicum-based degree focused on community health leadership and on promoting health-sustaining environments in Lebanon and across the Middle East.",
    "admission_requirements": null,
    "gre_requirement": null,
    "gmat_requirement": null,
    "english_requirement": null,
    "interview_requirement": null,
    "experience_requirement": null,
    "accreditation": null,
    "official_program_url": "https://www.balamand.edu.lb/faculties/FHS/AcademicPrograms/Pages/Programs/MSPublicHealth.aspx",
    "sources": [
      {
        "source_id": "uob_fhs_mph",
        "page_title": "Master of Public Health (MPH)",
        "url": "https://www.balamand.edu.lb/faculties/FHS/AcademicPrograms/Pages/Programs/MSPublicHealth.aspx",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_fhs",
        "page_title": "Faculty of Health Sciences",
        "url": "https://www.balamand.edu.lb/faculties/FHS/Pages/Default.aspx",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_fhs_graduate_catalogue_pdf",
        "page_title": "Faculty of Health Sciences Graduate Catalogue",
        "url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/FHSGraduate.pdf",
        "date_accessed": "2026-06-28"
      }
    ],
    "tuition": {
      "academic_year": "2025-2026",
      "currency": "USD",
      "billing_basis": "PER_CREDIT",
      "amount": 715,
      "category": "Faculty of Health Sciences",
      "notes": "Official UOB graduate fee table (2025-2026).",
      "source_ids": [
        "uob_fees_expenses_pdf"
      ]
    },
    "notes": "Official MPH page exists; the Faculty of Health Sciences hub and graduate catalogue also list the program. Official MPH page confirms a 42-credit practicum-based degree with two tracks and a minimum 20-month full-time completion window."
  },
  {
    "id": "uob-fom-master-biomedical-sciences",
    "faculty": "Faculty of Medicine and Medical Sciences - Al-Kurah Campus",
    "department": "Department of Biomedical Sciences",
    "major_category": "Medicine and Medical Sciences",
    "major": "Biomedical Sciences",
    "degree_type": "MASTER",
    "official_degree_name": "MS in Biomedical Sciences",
    "thesis_or_non_thesis": "THESIS",
    "concentrations_or_tracks": [
      "Biochemistry",
      "Immunology",
      "Microbiology",
      "Molecular Biology",
      "Physiology"
    ],
    "credits": null,
    "duration_value": 2,
    "duration_unit": "YEARS",
    "duration_raw_text": "2 years",
    "language": "ENGLISH",
    "delivery_mode": null,
    "program_description": "The two-year MS in Biomedical Sciences prepares students for research careers and for further study toward a PhD or MD degree. The program offers course and research tracks in biochemistry, immunology, microbiology, molecular biology, and physiology.",
    "admission_requirements": null,
    "gre_requirement": null,
    "gmat_requirement": null,
    "english_requirement": null,
    "interview_requirement": null,
    "experience_requirement": null,
    "accreditation": null,
    "official_program_url": "https://www.balamand.edu.lb/faculties/FOM/AcademicPrograms/Pages/MSBiomedical.aspx",
    "sources": [
      {
        "source_id": "uob_fom_ms_biomedical",
        "page_title": "MS in Biomedical Sciences",
        "url": "https://www.balamand.edu.lb/faculties/FOM/AcademicPrograms/Pages/MSBiomedical.aspx",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_fom",
        "page_title": "Faculty of Medicine and Medical Sciences",
        "url": "https://www.balamand.edu.lb/faculties/FOM/Pages/default.aspx",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_general_graduate_catalogue_pdf",
        "page_title": "General Section Graduate Catalogue",
        "url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/General-Section-Graduate.pdf",
        "date_accessed": "2026-06-28"
      }
    ],
    "tuition": {
      "academic_year": "2025-2026",
      "currency": "USD",
      "billing_basis": "PER_ACADEMIC_YEAR",
      "amount": 30000,
      "category": "Faculty of Medicine and Medical Sciences",
      "notes": "Official UOB fee table lists Faculty of Medicine yearly tuition (2025-2026).",
      "source_ids": [
        "uob_fees_expenses_pdf"
      ]
    },
    "notes": "Official FOM graduate program page exists and the graduate catalogue also lists the MS in Biomedical Sciences. Official biomedical sciences page confirms the two-year research-oriented structure, thesis requirement, and five named research tracks."
  },
  {
    "id": "uob-fom-master-cognitive-behavior-therapy",
    "faculty": "Faculty of Medicine and Medical Sciences - Al-Kurah Campus",
    "department": null,
    "major_category": "Medicine and Medical Sciences",
    "major": "Cognitive Behavior Therapy",
    "degree_type": "MASTER",
    "official_degree_name": "Cognitive Behavior Therapy M.C.B.T.",
    "thesis_or_non_thesis": null,
    "concentrations_or_tracks": [],
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": "ENGLISH",
    "delivery_mode": null,
    "program_description": null,
    "admission_requirements": null,
    "gre_requirement": null,
    "gmat_requirement": null,
    "english_requirement": null,
    "interview_requirement": null,
    "experience_requirement": null,
    "accreditation": null,
    "official_program_url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/General-Section-Graduate.pdf",
    "sources": [
      {
        "source_id": "uob_fom",
        "page_title": "Faculty of Medicine and Medical Sciences",
        "url": "https://www.balamand.edu.lb/faculties/FOM/Pages/default.aspx",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_general_graduate_catalogue_pdf",
        "page_title": "General Section Graduate Catalogue",
        "url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/General-Section-Graduate.pdf",
        "date_accessed": "2026-06-28"
      }
    ],
    "tuition": {
      "academic_year": "2025-2026",
      "currency": "USD",
      "billing_basis": "PER_ACADEMIC_YEAR",
      "amount": 30000,
      "category": "Faculty of Medicine and Medical Sciences",
      "notes": "Official UOB fee table lists Faculty of Medicine yearly tuition (2025-2026).",
      "source_ids": [
        "uob_fees_expenses_pdf"
      ]
    },
    "notes": "The graduate catalogue lists M.C.B.T. under Faculty of Medicine and Medical Sciences; no separate program page was resolved during discovery."
  },
  {
    "id": "uob-theology-master-theology",
    "faculty": "Saint John of Damascus Institute of Theology - Al-Kurah Campus",
    "department": null,
    "major_category": "Theology",
    "major": "Theology",
    "degree_type": "MASTER",
    "official_degree_name": "Theology M.Th.",
    "thesis_or_non_thesis": null,
    "concentrations_or_tracks": [],
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": "MULTILINGUAL",
    "delivery_mode": null,
    "program_description": null,
    "admission_requirements": null,
    "gre_requirement": null,
    "gmat_requirement": null,
    "english_requirement": null,
    "interview_requirement": null,
    "experience_requirement": null,
    "accreditation": null,
    "official_program_url": "https://theology.balamand.edu.lb/",
    "sources": [
      {
        "source_id": "uob_theology",
        "page_title": "Saint John of Damascus Institute of Theology",
        "url": "https://theology.balamand.edu.lb/",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_general_graduate_catalogue_pdf",
        "page_title": "General Section Graduate Catalogue",
        "url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/General-Section-Graduate.pdf",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_catalogue_page",
        "page_title": "Catalogue",
        "url": "https://www.balamand.edu.lb/CurrentStudents/Pages/Catalogue.aspx",
        "date_accessed": "2026-06-28"
      }
    ],
    "tuition": null,
    "notes": "The official theology subdomain was surfaced from UOB navigation but was not fully fetched during discovery; the graduate catalogue also lists the M.Th. offering."
  },
  {
    "id": "uob-theology-phd-theology",
    "faculty": "Saint John of Damascus Institute of Theology - Al-Kurah Campus",
    "department": null,
    "major_category": "Theology",
    "major": "Theology",
    "degree_type": "PHD",
    "official_degree_name": "Theology Ph.D.",
    "thesis_or_non_thesis": null,
    "concentrations_or_tracks": [],
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": "MULTILINGUAL",
    "delivery_mode": null,
    "program_description": null,
    "admission_requirements": null,
    "gre_requirement": null,
    "gmat_requirement": null,
    "english_requirement": null,
    "interview_requirement": null,
    "experience_requirement": null,
    "accreditation": null,
    "official_program_url": "https://theology.balamand.edu.lb/",
    "sources": [
      {
        "source_id": "uob_theology",
        "page_title": "Saint John of Damascus Institute of Theology",
        "url": "https://theology.balamand.edu.lb/",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_general_graduate_catalogue_pdf",
        "page_title": "General Section Graduate Catalogue",
        "url": "https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/General-Section-Graduate.pdf",
        "date_accessed": "2026-06-28"
      },
      {
        "source_id": "uob_catalogue_page",
        "page_title": "Catalogue",
        "url": "https://www.balamand.edu.lb/CurrentStudents/Pages/Catalogue.aspx",
        "date_accessed": "2026-06-28"
      }
    ],
    "tuition": null,
    "notes": "The official theology subdomain was surfaced from UOB navigation but was not fully fetched during discovery; the graduate catalogue also lists the Ph.D. offering."
  }
]$UOB_PROG$::jsonb) AS seed(
        id TEXT,
        faculty TEXT,
        department TEXT,
        major_category TEXT,
        major TEXT,
        degree_type TEXT,
        official_degree_name TEXT,
        thesis_or_non_thesis TEXT,
        concentrations_or_tracks JSONB,
        credits INTEGER,
        duration_value NUMERIC(10,2),
        duration_unit TEXT,
        duration_raw_text TEXT,
        language TEXT,
        delivery_mode TEXT,
        program_description TEXT,
        admission_requirements TEXT,
        gre_requirement TEXT,
        gmat_requirement TEXT,
        english_requirement TEXT,
        interview_requirement TEXT,
        experience_requirement TEXT,
        accreditation TEXT,
        official_program_url TEXT,
        sources JSONB,
        tuition JSONB,
        notes TEXT
    );

    INSERT INTO graduate_program (
        university_id, faculty_id, department_id, degree_type_id, program_key,
        major_category, major, official_degree_name, thesis_or_non_thesis,
        credits, duration_value, duration_unit, primary_language_id, delivery_mode,
        program_description, official_program_url, source_id, notes
    )
    SELECT
        v_university_id,
        fac.id,
        dep.id,
        dt.id,
        seed.id,
        seed.major_category,
        seed.major,
        seed.official_degree_name,
        seed.thesis_or_non_thesis,
        seed.credits,
        seed.duration_value,
        seed.duration_unit,
        lang.id,
        seed.delivery_mode,
        seed.program_description,
        seed.official_program_url,
        (
            SELECT s.id
            FROM jsonb_array_elements(seed.sources) WITH ORDINALITY AS src(src_obj, ord)
            JOIN uob_source_seed us ON us.source_id = (src.src_obj->>'source_id')
            JOIN source s ON s.university_id = v_university_id AND s.url = us.url
            ORDER BY ord
            LIMIT 1
        ),
        concat_ws(' ', NULLIF(seed.notes, ''), CASE WHEN seed.duration_raw_text IS NOT NULL THEN 'Original duration text: ' || seed.duration_raw_text END)
    FROM uob_program_seed seed
    JOIN university_faculty fac
      ON fac.university_id = v_university_id
     AND fac.name = seed.faculty
    LEFT JOIN university_department dep
      ON dep.university_id = v_university_id
     AND dep.faculty_id = fac.id
     AND dep.name = seed.department
    LEFT JOIN degree_type dt
      ON dt.code = seed.degree_type
    LEFT JOIN language lang
      ON LOWER(lang.name) = LOWER(seed.language)
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
        university_id, program_id, source_id, source_role, source_order, evidence_text, notes
    )
    SELECT
        v_university_id,
        gp.id,
        s.id,
        CASE WHEN src.ord = 1 THEN 'PRIMARY' ELSE 'SECONDARY' END,
        src.ord,
        src.src_obj->>'page_title',
        'Imported from UOB program sources.'
    FROM uob_program_seed seed
    JOIN graduate_program gp
      ON gp.university_id = v_university_id
     AND gp.program_key = seed.id
    JOIN LATERAL jsonb_array_elements(seed.sources) WITH ORDINALITY AS src(src_obj, ord)
      ON TRUE
    JOIN uob_source_seed us ON us.source_id = (src.src_obj->>'source_id')
    JOIN source s ON s.university_id = v_university_id AND s.url = us.url
    ON CONFLICT (program_id, source_id, source_role) DO UPDATE SET
        source_order = EXCLUDED.source_order,
        evidence_text = EXCLUDED.evidence_text,
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
        'PROGRAM'::TEXT,
        seed.id || ':tuition:' || (seed.tuition->>'academic_year'),
        seed.tuition->>'academic_year',
        seed.tuition->>'currency',
        seed.tuition->>'billing_basis',
        (seed.tuition->>'amount')::NUMERIC(12,2),
        seed.tuition->>'category',
        seed.tuition->>'notes',
        s.id
    FROM uob_program_seed seed
    JOIN graduate_program gp
      ON gp.university_id = v_university_id
     AND gp.program_key = seed.id
    JOIN LATERAL jsonb_array_elements_text(COALESCE(seed.tuition->'source_ids', '[]'::jsonb)) WITH ORDINALITY AS ts(source_seed_id, ord)
      ON TRUE
    JOIN uob_source_seed us ON us.source_id = ts.source_seed_id
    JOIN source s ON s.university_id = v_university_id AND s.url = us.url
    WHERE seed.tuition IS NOT NULL
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

    INSERT INTO graduate_fee_item (
        university_id, faculty_id, department_id, program_id, scope_level, record_key,
        academic_year, fee_name, billing_basis, currency, amount, category, notes, source_id
    )
    SELECT
        v_university_id,
        NULL,
        NULL,
        NULL,
        'UNIVERSITY'::TEXT,
        seed.record_key,
        seed.academic_year,
        seed.fee_name,
        seed.billing_basis,
        seed.currency,
        seed.amount,
        seed.category,
        seed.notes,
        s.id
    FROM jsonb_to_recordset($UOB_FEES$[
  {
    "record_key": "graduate_application_fee",
    "academic_year": "2025-2026",
    "fee_name": "Graduate Application Fee",
    "billing_basis": "FLAT_FEE",
    "currency": "USD",
    "amount": 60,
    "category": "Application and entrance exam fees",
    "notes": "Non-refundable graduate application fee.",
    "source_ids": [
      "uob_fees_expenses_pdf"
    ],
    "source_url": "https://www.balamand.edu.lb/Style%20Library/PDFs/CurrentStudents/FeesExpenses.pdf"
  },
  {
    "record_key": "post_graduate_application_fee",
    "academic_year": "2025-2026",
    "fee_name": "Post-Graduate Application Fee",
    "billing_basis": "FLAT_FEE",
    "currency": "USD",
    "amount": 60,
    "category": "Application and entrance exam fees",
    "notes": "Non-refundable post-graduate application fee.",
    "source_ids": [
      "uob_fees_expenses_pdf"
    ],
    "source_url": "https://www.balamand.edu.lb/Style%20Library/PDFs/CurrentStudents/FeesExpenses.pdf"
  },
  {
    "record_key": "interview_fee",
    "academic_year": "2025-2026",
    "fee_name": "Interview",
    "billing_basis": "FLAT_FEE",
    "currency": "USD",
    "amount": 30,
    "category": "Application and entrance exam fees",
    "notes": "Interview fee listed in the official fee table.",
    "source_ids": [
      "uob_fees_expenses_pdf"
    ],
    "source_url": "https://www.balamand.edu.lb/Style%20Library/PDFs/CurrentStudents/FeesExpenses.pdf"
  },
  {
    "record_key": "medical_exam_fee",
    "academic_year": "2025-2026",
    "fee_name": "Medical Examination",
    "billing_basis": "FLAT_FEE",
    "currency": "USD",
    "amount": 50,
    "category": "Application and entrance exam fees",
    "notes": "Medical examination fee listed in the official fee table.",
    "source_ids": [
      "uob_fees_expenses_pdf"
    ],
    "source_url": "https://www.balamand.edu.lb/Style%20Library/PDFs/CurrentStudents/FeesExpenses.pdf"
  },
  {
    "record_key": "physical_exam_fee",
    "academic_year": "2025-2026",
    "fee_name": "Physical Examination",
    "billing_basis": "FLAT_FEE",
    "currency": "USD",
    "amount": 30,
    "category": "Application and entrance exam fees",
    "notes": "Physical examination fee listed in the official fee table.",
    "source_ids": [
      "uob_fees_expenses_pdf"
    ],
    "source_url": "https://www.balamand.edu.lb/Style%20Library/PDFs/CurrentStudents/FeesExpenses.pdf"
  },
  {
    "record_key": "english_language_test_fee",
    "academic_year": "2025-2026",
    "fee_name": "English Language Test",
    "billing_basis": "FLAT_FEE",
    "currency": "USD",
    "amount": 40,
    "category": "Application and entrance exam fees",
    "notes": "English language test fee listed in the official fee table.",
    "source_ids": [
      "uob_fees_expenses_pdf"
    ],
    "source_url": "https://www.balamand.edu.lb/Style%20Library/PDFs/CurrentStudents/FeesExpenses.pdf"
  },
  {
    "record_key": "french_language_test_fee",
    "academic_year": "2025-2026",
    "fee_name": "French Language Test",
    "billing_basis": "FLAT_FEE",
    "currency": "USD",
    "amount": 20,
    "category": "Application and entrance exam fees",
    "notes": "French language test fee listed in the official fee table.",
    "source_ids": [
      "uob_fees_expenses_pdf"
    ],
    "source_url": "https://www.balamand.edu.lb/Style%20Library/PDFs/CurrentStudents/FeesExpenses.pdf"
  },
  {
    "record_key": "library_laboratory_deposit",
    "academic_year": "2025-2026",
    "fee_name": "Library and Laboratory Deposit",
    "billing_basis": "FLAT_FEE",
    "currency": "USD",
    "amount": 170,
    "category": "Academic support",
    "notes": "Deposit required of every student enrolling in UOB.",
    "source_ids": [
      "uob_fees_expenses_pdf"
    ],
    "source_url": "https://www.balamand.edu.lb/Style%20Library/PDFs/CurrentStudents/FeesExpenses.pdf"
  },
  {
    "record_key": "student_activities_fee",
    "academic_year": "2025-2026",
    "fee_name": "Student Activities",
    "billing_basis": "PER_SEMESTER",
    "currency": "USD",
    "amount": 200,
    "category": "Student activities",
    "notes": "Charged every semester.",
    "source_ids": [
      "uob_fees_expenses_pdf"
    ],
    "source_url": "https://www.balamand.edu.lb/Style%20Library/PDFs/CurrentStudents/FeesExpenses.pdf"
  },
  {
    "record_key": "internet_fee",
    "academic_year": "2025-2026",
    "fee_name": "Internet Fee",
    "billing_basis": "PER_SEMESTER",
    "currency": "USD",
    "amount": 30,
    "category": "Technology services",
    "notes": "Charged every semester.",
    "source_ids": [
      "uob_fees_expenses_pdf"
    ],
    "source_url": "https://www.balamand.edu.lb/Style%20Library/PDFs/CurrentStudents/FeesExpenses.pdf"
  },
  {
    "record_key": "graduation_fee",
    "academic_year": "2025-2026",
    "fee_name": "Graduation Fee",
    "billing_basis": "FLAT_FEE",
    "currency": "USD",
    "amount": 125,
    "category": "Graduation fees",
    "notes": "Charged to every graduating student upon clearance.",
    "source_ids": [
      "uob_fees_expenses_pdf"
    ],
    "source_url": "https://www.balamand.edu.lb/Style%20Library/PDFs/CurrentStudents/FeesExpenses.pdf"
  },
  {
    "record_key": "late_payment_penalty",
    "academic_year": "2025-2026",
    "fee_name": "Late Registration / Payment Penalty",
    "billing_basis": "PER_SEMESTER",
    "currency": "USD",
    "amount": 100,
    "category": "Administrative fees",
    "notes": "Additional amount charged for late registration/payment of installments.",
    "source_ids": [
      "uob_fees_expenses_pdf"
    ],
    "source_url": "https://www.balamand.edu.lb/Style%20Library/PDFs/CurrentStudents/FeesExpenses.pdf"
  },
  {
    "record_key": "nssf_medical_branch",
    "academic_year": "2025-2026",
    "fee_name": "National Social Security Fund (Medical Branch)",
    "billing_basis": "PER_ACADEMIC_YEAR",
    "currency": "LBP",
    "amount": null,
    "category": "Insurance",
    "notes": "Lebanese students must enroll in the NSSF medical branch; fee is 30% of the legal minimum salary.",
    "source_ids": [
      "uob_fees_expenses_pdf"
    ],
    "source_url": "https://www.balamand.edu.lb/Style%20Library/PDFs/CurrentStudents/FeesExpenses.pdf"
  },
  {
    "record_key": "payment_method",
    "academic_year": "2025-2026",
    "fee_name": "Method of Payment",
    "billing_basis": "FLAT_FEE",
    "currency": "USD",
    "amount": null,
    "category": "Payment method",
    "notes": "Payment should be made in cash at the university cashier’s office.",
    "source_ids": [
      "uob_fees_expenses_pdf"
    ],
    "source_url": "https://www.balamand.edu.lb/Style%20Library/PDFs/CurrentStudents/FeesExpenses.pdf"
  }
]$UOB_FEES$::jsonb) AS seed(
        record_key TEXT,
        academic_year TEXT,
        fee_name TEXT,
        billing_basis TEXT,
        currency TEXT,
        amount NUMERIC(12,2),
        category TEXT,
        notes TEXT,
        source_ids JSONB,
        source_url TEXT
    )
    JOIN LATERAL jsonb_array_elements_text(COALESCE(seed.source_ids, '[]'::jsonb)) WITH ORDINALITY AS fs(source_seed_id, ord)
      ON TRUE
    JOIN uob_source_seed us ON us.source_id = fs.source_seed_id
    JOIN source s ON s.university_id = v_university_id AND s.url = us.url
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

    INSERT INTO graduate_admission_requirement (
        university_id, faculty_id, department_id, program_id, scope_level, record_key,
        requirement_type, requirement_text, comparison_operator, threshold_value, threshold_unit, is_required, notes, source_id
    )
    SELECT
        v_university_id, NULL, NULL, NULL, 'UNIVERSITY', seed.record_key,
        seed.requirement_type, seed.requirement_text, NULL, NULL, NULL, TRUE, seed.notes, s.id
    FROM jsonb_to_recordset($UOB_UNIV_ADM$[
  {
    "record_key": "uob:admissions:general",
    "requirement_type": "GENERAL",
    "requirement_text": "Graduate applicants must meet university and faculty requirements; departments and faculties supervise the criteria.",
    "notes": "Central graduate admissions summary from the university admissions guidance.",
    "source_url": "https://www.balamand.edu.lb/ProspectiveStudents/AdmissionGuide/Pages/default.aspx"
  },
  {
    "record_key": "uob:admissions:english",
    "requirement_type": "ENGLISH",
    "requirement_text": "English-track graduate majors require EPT 85 or TOEFL iBT 100 (or paper-based TOEFL 600). Applicants below the threshold may be admitted conditionally and complete ENGL101/ENGL102 during the first year. French-track majors require FREN201, with DELF/DALF-based placement rules.",
    "notes": "Central English/French language requirement summary from the university admissions guidance.",
    "source_url": "https://www.balamand.edu.lb/ProspectiveStudents/AdmissionGuide/Pages/default.aspx"
  },
  {
    "record_key": "uob:admissions:interview",
    "requirement_type": "INTERVIEW",
    "requirement_text": "A graduate admission interview may be required and applications are reviewed by the unit Graduate Admission Interview Committee.",
    "notes": "Shared graduate interview language from the admissions guide and graduate admissions page.",
    "source_url": "https://www.balamand.edu.lb/ProspectiveStudents/AdmissionGuide/Pages/default.aspx"
  }
]$UOB_UNIV_ADM$::jsonb) AS seed(
        record_key TEXT,
        requirement_type TEXT,
        requirement_text TEXT,
        notes TEXT,
        source_url TEXT
    )
    JOIN source s ON s.university_id = v_university_id AND s.url = seed.source_url
    ON CONFLICT (university_id, record_key) DO UPDATE SET
        requirement_type = EXCLUDED.requirement_type,
        requirement_text = EXCLUDED.requirement_text,
        notes = EXCLUDED.notes,
        source_id = EXCLUDED.source_id,
        updated_at = NOW();

    INSERT INTO graduate_admission_requirement (
        university_id, faculty_id, department_id, program_id, scope_level, record_key,
        requirement_type, requirement_text, comparison_operator, threshold_value, threshold_unit, is_required, notes, source_id
    )
    SELECT
        v_university_id,
        gp.faculty_id,
        gp.department_id,
        gp.id,
        'PROGRAM',
        seed.record_key,
        seed.requirement_type,
        seed.requirement_text,
        NULL,
        NULL,
        NULL,
        TRUE,
        seed.notes,
        gp.source_id
    FROM (
        SELECT * FROM jsonb_to_recordset($UOB_PROG_ADM$[
  {
    "program_key": "uob-fas-master-political-science-international-affairs",
    "record_key": "uob-fas-master-political-science-international-affairs:admission_requirements",
    "requirement_type": "GENERAL",
    "requirement_text": "A Bachelor degree in Political Science with a minimum average of 80 is normally required; applicants slightly below 80 may be accepted on probation; applicants from other disciplines may be accepted on probation with bridging courses.",
    "notes": null,
    "source_ids": [
      "uob_fas_graduate_catalogue_pdf",
      "uob_fas",
      "uob_catalogue_page"
    ]
  },
  {
    "program_key": "uob-fas-master-sports-management",
    "record_key": "uob-fas-master-sports-management:admission_requirements",
    "requirement_type": "GENERAL",
    "requirement_text": "Candidates holding a BA in Physical Education or a BS in Business are eligible for the program.",
    "notes": null,
    "source_ids": [
      "uob_fas_graduate_catalogue_pdf",
      "uob_fas",
      "uob_catalogue_page"
    ]
  },
  {
    "program_key": "uob-fobm-emba",
    "record_key": "uob-fobm-emba:admission_requirements",
    "requirement_type": "GENERAL",
    "requirement_text": "Bachelor degree licensed from the Ministry of Education; five years of managerial experience; completed application with required documents; English proficiency; interview with the program committee.",
    "notes": null,
    "source_ids": [
      "uob_fobm_emba",
      "uob_fobm",
      "uob_general_graduate_catalogue_pdf"
    ]
  },
  {
    "program_key": "uob-fobm-emba",
    "record_key": "uob-fobm-emba:interview_requirement",
    "requirement_type": "INTERVIEW",
    "requirement_text": "Interview with the EMBA admission committee",
    "notes": null,
    "source_ids": [
      "uob_fobm_emba",
      "uob_fobm",
      "uob_general_graduate_catalogue_pdf"
    ]
  },
  {
    "program_key": "uob-fobm-emba",
    "record_key": "uob-fobm-emba:experience_requirement",
    "requirement_type": "EXPERIENCE",
    "requirement_text": "Five years of managerial experience",
    "notes": null,
    "source_ids": [
      "uob_fobm_emba",
      "uob_fobm",
      "uob_general_graduate_catalogue_pdf"
    ]
  }
]$UOB_PROG_ADM$::jsonb) AS x(
            program_key TEXT,
            record_key TEXT,
            requirement_type TEXT,
            requirement_text TEXT,
            notes TEXT,
            source_ids JSONB
        )
    ) seed
    JOIN graduate_program gp ON gp.university_id = v_university_id AND gp.program_key = seed.program_key
    ON CONFLICT (university_id, record_key) DO UPDATE SET
        faculty_id = EXCLUDED.faculty_id,
        department_id = EXCLUDED.department_id,
        program_id = EXCLUDED.program_id,
        scope_level = EXCLUDED.scope_level,
        requirement_type = EXCLUDED.requirement_type,
        requirement_text = EXCLUDED.requirement_text,
        notes = EXCLUDED.notes,
        source_id = EXCLUDED.source_id,
        updated_at = NOW();

    INSERT INTO graduate_required_document (
        university_id, faculty_id, department_id, program_id, scope_level, record_key,
        document_type, document_name, is_optional, sort_order, notes, source_id
    )
    SELECT
        v_university_id, NULL, NULL, NULL, 'UNIVERSITY', seed.record_key,
        seed.document_type, seed.document_name, seed.is_optional, seed.sort_order, seed.notes, s.id
    FROM jsonb_to_recordset($UOB_DOCS$[
  {
    "record_key": "uob:doc:1",
    "document_type": "DIPLOMA",
    "document_name": "Certified copy of the diploma or degree certificate.",
    "is_optional": false,
    "sort_order": 1,
    "notes": "Central graduate admissions document.",
    "source_url": "https://www.balamand.edu.lb/Style%20Library/PDFs/ProspectiveStudents/AdmissionGuide.pdf"
  },
  {
    "record_key": "uob:doc:2",
    "document_type": "RECOMMENDATION",
    "document_name": "Two recommendation letters.",
    "is_optional": false,
    "sort_order": 2,
    "notes": "Central graduate admissions document.",
    "source_url": "https://www.balamand.edu.lb/Style%20Library/PDFs/ProspectiveStudents/AdmissionGuide.pdf"
  },
  {
    "record_key": "uob:doc:3",
    "document_type": "EQUIVALENCE",
    "document_name": "Diploma equivalence if applicable.",
    "is_optional": true,
    "sort_order": 3,
    "notes": "Central graduate admissions document.",
    "source_url": "https://www.balamand.edu.lb/Style%20Library/PDFs/ProspectiveStudents/AdmissionGuide.pdf"
  },
  {
    "record_key": "uob:doc:4",
    "document_type": "TRANSCRIPT",
    "document_name": "Official transcript with course descriptions for completed courses.",
    "is_optional": false,
    "sort_order": 4,
    "notes": "Central graduate admissions document.",
    "source_url": "https://www.balamand.edu.lb/Style%20Library/PDFs/ProspectiveStudents/AdmissionGuide.pdf"
  }
]$UOB_DOCS$::jsonb) AS seed(
        record_key TEXT,
        document_type TEXT,
        document_name TEXT,
        is_optional BOOLEAN,
        sort_order INTEGER,
        notes TEXT,
        source_url TEXT
    )
    JOIN source s ON s.university_id = v_university_id AND s.url = seed.source_url
    ON CONFLICT (university_id, record_key) DO UPDATE SET
        document_type = EXCLUDED.document_type,
        document_name = EXCLUDED.document_name,
        is_optional = EXCLUDED.is_optional,
        sort_order = EXCLUDED.sort_order,
        notes = EXCLUDED.notes,
        source_id = EXCLUDED.source_id,
        updated_at = NOW();

    INSERT INTO graduate_admission_deadline (
        university_id, faculty_id, department_id, program_id, scope_level, record_key,
        academic_year, deadline_type, term, deadline_date, note, source_id
    )
    SELECT
        v_university_id, NULL, NULL, NULL, 'UNIVERSITY', seed.record_key,
        NULL, seed.deadline_type, seed.term, seed.deadline_date, seed.note, s.id
    FROM jsonb_to_recordset($UOB_DEADLINES$[
  {
    "record_key": "uob:deadline:summary",
    "deadline_type": "OTHER",
    "term": "Graduate deadlines summary",
    "deadline_date": null,
    "note": "No single centralized graduate deadline page was identified; applicants are directed to the academic calendar and the graduate admissions pages for timing updates.",
    "source_url": "https://www.balamand.edu.lb/CurrentStudents/Pages/AcademicCalendar.aspx"
  }
]$UOB_DEADLINES$::jsonb) AS seed(
        record_key TEXT,
        deadline_type TEXT,
        term TEXT,
        deadline_date DATE,
        note TEXT,
        source_url TEXT
    )
    JOIN source s ON s.university_id = v_university_id AND s.url = seed.source_url
    ON CONFLICT (university_id, record_key) DO UPDATE SET
        deadline_type = EXCLUDED.deadline_type,
        term = EXCLUDED.term,
        deadline_date = EXCLUDED.deadline_date,
        note = EXCLUDED.note,
        source_id = EXCLUDED.source_id,
        updated_at = NOW();

    INSERT INTO graduate_scholarship (
        university_id, faculty_id, department_id, program_id, scope_level, record_key,
        academic_year, name, description, coverage, amount, currency, notes, source_id
    )
    SELECT
        v_university_id, NULL, NULL, NULL, 'UNIVERSITY', seed.record_key,
        seed.academic_year, seed.name, seed.description, seed.coverage, seed.amount, seed.currency, seed.notes, s.id
    FROM jsonb_to_recordset($UOB_SCHOL$[
  {
    "record_key": "uob:scholarship:summary",
    "academic_year": "2025-2026",
    "name": "Graduate scholarship and support overview",
    "description": "University-wide scholarships and financial support include merit scholarships, athletic scholarships, faculty support, and graduate assistantship references.",
    "coverage": null,
    "amount": null,
    "currency": null,
    "notes": "No graduate-only award table was published in the reviewed sources.",
    "source_url": "https://www.balamand.edu.lb/CurrentStudents/FinancialSupport/Pages/FinancialSupport.aspx"
  }
]$UOB_SCHOL$::jsonb) AS seed(
        record_key TEXT,
        academic_year TEXT,
        name TEXT,
        description TEXT,
        coverage TEXT,
        amount NUMERIC(12,2),
        currency TEXT,
        notes TEXT,
        source_url TEXT
    )
    JOIN source s ON s.university_id = v_university_id AND s.url = seed.source_url
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

    INSERT INTO graduate_financial_aid (
        university_id, faculty_id, department_id, program_id, scope_level, record_key,
        academic_year, name, description, amount, currency, notes, source_id
    )
    SELECT
        v_university_id, NULL, NULL, NULL, 'UNIVERSITY', seed.record_key,
        seed.academic_year, seed.name, seed.description, seed.amount, seed.currency, seed.notes, s.id
    FROM jsonb_to_recordset($UOB_AID$[
  {
    "record_key": "uob:aid:summary",
    "academic_year": "2025-2026",
    "name": "Graduate financial aid overview",
    "description": "UOB allocates part of its annual budget to support student tuition fees based on academic excellence and the social situation of the student; the financial support page also references aid applications and graduate assistantship.",
    "amount": null,
    "currency": null,
    "notes": "Graduate aid is described at the university level rather than as a separate graduate-only table.",
    "source_url": "https://www.balamand.edu.lb/CurrentStudents/FinancialSupport/Pages/FinancialSupport.aspx"
  }
]$UOB_AID$::jsonb) AS seed(
        record_key TEXT,
        academic_year TEXT,
        name TEXT,
        description TEXT,
        amount NUMERIC(12,2),
        currency TEXT,
        notes TEXT,
        source_url TEXT
    )
    JOIN source s ON s.university_id = v_university_id AND s.url = seed.source_url
    ON CONFLICT (university_id, record_key) DO UPDATE SET
        academic_year = EXCLUDED.academic_year,
        name = EXCLUDED.name,
        description = EXCLUDED.description,
        amount = EXCLUDED.amount,
        currency = EXCLUDED.currency,
        notes = EXCLUDED.notes,
        source_id = EXCLUDED.source_id,
        updated_at = NOW();

    INSERT INTO graduate_payment_plan (
        university_id, faculty_id, department_id, program_id, scope_level, record_key,
        academic_year, name, description, installments_count, down_payment_amount, down_payment_currency, interval_label, notes, source_id
    )
    SELECT
        v_university_id, NULL, NULL, NULL, 'UNIVERSITY', seed.record_key,
        seed.academic_year, seed.name, seed.description, seed.installments_count, seed.down_payment_amount, seed.down_payment_currency, seed.interval_label, seed.notes, s.id
    FROM jsonb_to_recordset($UOB_PAY$[
  {
    "record_key": "uob:payment-plan:summary",
    "academic_year": "2025-2026",
    "name": "Payment method and late-payment policy",
    "description": "Payment is made in cash at the university cashier’s office; the fee sheet also records a late registration/payment penalty and a refund schedule for withdrawals.",
    "installments_count": null,
    "down_payment_amount": null,
    "down_payment_currency": null,
    "interval_label": null,
    "notes": "No dedicated installment-plan page was published; this row captures the official payment-method and late-payment guidance.",
    "source_url": "https://www.balamand.edu.lb/Style%20Library/PDFs/CurrentStudents/FeesExpenses.pdf"
  }
]$UOB_PAY$::jsonb) AS seed(
        record_key TEXT,
        academic_year TEXT,
        name TEXT,
        description TEXT,
        installments_count INTEGER,
        down_payment_amount NUMERIC(12,2),
        down_payment_currency TEXT,
        interval_label TEXT,
        notes TEXT,
        source_url TEXT
    )
    JOIN source s ON s.university_id = v_university_id AND s.url = seed.source_url
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

    INSERT INTO graduate_accreditation (
        university_id, faculty_id, department_id, program_id, scope_level, record_key,
        name, authority, status, valid_from, valid_until, notes, source_id
    )
    SELECT
        v_university_id, NULL, NULL, NULL, 'UNIVERSITY', seed.record_key,
        seed.name, seed.authority, seed.status, seed.valid_from, seed.valid_until, seed.notes, s.id
    FROM jsonb_to_recordset($UOB_ACC$[
  {
    "record_key": "uob:accreditation:institutional",
    "name": "Institutional accreditation overview",
    "authority": null,
    "status": null,
    "valid_from": null,
    "valid_until": null,
    "notes": "UOB states in the admissions guide that it is accredited by multiple higher educational commissions; the Faculty of Engineering hub also highlights ABET and ACQUIN markers.",
    "source_url": "https://www.balamand.edu.lb/Style%20Library/PDFs/ProspectiveStudents/AdmissionGuide.pdf"
  },
  {
    "record_key": "uob:accreditation:nursing",
    "name": "ACEN candidate accreditation for the MSN program",
    "authority": "Accreditation Commission for Education in Nursing (ACEN)",
    "status": "Candidate for initial accreditation",
    "valid_from": "2022-12-20",
    "valid_until": "2024-12-20",
    "notes": "Program-level accreditation notice stated on the nursing program page.",
    "source_url": "https://www.balamand.edu.lb/faculties/FHS/Pages/Programs/MSN.aspx"
  }
]$UOB_ACC$::jsonb) AS seed(
        record_key TEXT,
        name TEXT,
        authority TEXT,
        status TEXT,
        valid_from DATE,
        valid_until DATE,
        notes TEXT,
        source_url TEXT
    )
    JOIN source s ON s.university_id = v_university_id AND s.url = seed.source_url
    ON CONFLICT (university_id, record_key) DO UPDATE SET
        name = EXCLUDED.name,
        authority = EXCLUDED.authority,
        status = EXCLUDED.status,
        valid_from = EXCLUDED.valid_from,
        valid_until = EXCLUDED.valid_until,
        notes = EXCLUDED.notes,
        source_id = EXCLUDED.source_id,
        updated_at = NOW();

    INSERT INTO graduate_accreditation (
        university_id, faculty_id, department_id, program_id, scope_level, record_key,
        name, authority, status, valid_from, valid_until, notes, source_id
    )
    SELECT
        v_university_id,
        gp.faculty_id,
        gp.department_id,
        gp.id,
        'PROGRAM',
        seed.id || ':accreditation',
        seed.accreditation,
        'Accreditation Commission for Education in Nursing (ACEN)',
        'Candidate for initial accreditation',
        '2022-12-20',
        '2024-12-20',
        seed.accreditation,
        gp.source_id
    FROM uob_program_seed seed
    JOIN graduate_program gp ON gp.university_id = v_university_id AND gp.program_key = seed.id
    WHERE seed.accreditation IS NOT NULL
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

    INSERT INTO graduate_program_track (
        university_id, program_id, track_type, track_name, track_order, is_primary, description, source_id, notes
    )
    SELECT
        v_university_id,
        gp.id,
        'TRACK',
        seed.track_name,
        seed.track_order,
        seed.is_primary,
        NULL,
        gp.source_id,
        seed.notes
    FROM jsonb_to_recordset($UOB_TRACKS$[
  {
    "program_key": "uob-fas-master-arabic-language-literature",
    "track_name": "Arabic Language",
    "track_order": 1,
    "is_primary": true,
    "notes": "Imported from concentrations_or_tracks.",
    "source_ids": [
      "uob_fas_graduate_catalogue_pdf"
    ]
  },
  {
    "program_key": "uob-fas-master-arabic-language-literature",
    "track_name": "Arabic Literature",
    "track_order": 2,
    "is_primary": false,
    "notes": "Imported from concentrations_or_tracks.",
    "source_ids": [
      "uob_fas_graduate_catalogue_pdf"
    ]
  },
  {
    "program_key": "uob-fas-master-biology",
    "track_name": "Biochemistry",
    "track_order": 1,
    "is_primary": true,
    "notes": "Imported from concentrations_or_tracks.",
    "source_ids": [
      "uob_fas_graduate_catalogue_pdf"
    ]
  },
  {
    "program_key": "uob-fas-master-biology",
    "track_name": "Molecular Biology",
    "track_order": 2,
    "is_primary": false,
    "notes": "Imported from concentrations_or_tracks.",
    "source_ids": [
      "uob_fas_graduate_catalogue_pdf"
    ]
  },
  {
    "program_key": "uob-fas-master-biology",
    "track_name": "Immunology",
    "track_order": 3,
    "is_primary": false,
    "notes": "Imported from concentrations_or_tracks.",
    "source_ids": [
      "uob_fas_graduate_catalogue_pdf"
    ]
  },
  {
    "program_key": "uob-fas-master-biology",
    "track_name": "Microbiology",
    "track_order": 4,
    "is_primary": false,
    "notes": "Imported from concentrations_or_tracks.",
    "source_ids": [
      "uob_fas_graduate_catalogue_pdf"
    ]
  },
  {
    "program_key": "uob-fas-master-chemistry",
    "track_name": "Advanced Analytical Chemistry",
    "track_order": 1,
    "is_primary": true,
    "notes": "Imported from concentrations_or_tracks.",
    "source_ids": [
      "uob_fas_graduate_catalogue_pdf"
    ]
  },
  {
    "program_key": "uob-fas-master-chemistry",
    "track_name": "Advanced Organic Chemistry",
    "track_order": 2,
    "is_primary": false,
    "notes": "Imported from concentrations_or_tracks.",
    "source_ids": [
      "uob_fas_graduate_catalogue_pdf"
    ]
  },
  {
    "program_key": "uob-fas-master-chemistry",
    "track_name": "Advanced Physical Chemistry",
    "track_order": 3,
    "is_primary": false,
    "notes": "Imported from concentrations_or_tracks.",
    "source_ids": [
      "uob_fas_graduate_catalogue_pdf"
    ]
  },
  {
    "program_key": "uob-fas-master-chemistry",
    "track_name": "Advanced Inorganic Chemistry",
    "track_order": 4,
    "is_primary": false,
    "notes": "Imported from concentrations_or_tracks.",
    "source_ids": [
      "uob_fas_graduate_catalogue_pdf"
    ]
  },
  {
    "program_key": "uob-fas-master-computer-science",
    "track_name": "Health Information Systems",
    "track_order": 1,
    "is_primary": true,
    "notes": "Imported from concentrations_or_tracks.",
    "source_ids": [
      "uob_fas_graduate_catalogue_pdf"
    ]
  },
  {
    "program_key": "uob-fas-master-computer-science",
    "track_name": "Software Engineering",
    "track_order": 2,
    "is_primary": false,
    "notes": "Imported from concentrations_or_tracks.",
    "source_ids": [
      "uob_fas_graduate_catalogue_pdf"
    ]
  },
  {
    "program_key": "uob-fas-master-education-kurah",
    "track_name": "Curriculum and Educational Management",
    "track_order": 1,
    "is_primary": true,
    "notes": "Imported from concentrations_or_tracks.",
    "source_ids": [
      "uob_fas_graduate_catalogue_pdf"
    ]
  },
  {
    "program_key": "uob-fas-master-education-kurah",
    "track_name": "Instructional Design and Technology",
    "track_order": 2,
    "is_primary": false,
    "notes": "Imported from concentrations_or_tracks.",
    "source_ids": [
      "uob_fas_graduate_catalogue_pdf"
    ]
  },
  {
    "program_key": "uob-fas-master-food-science-technology",
    "track_name": "Food Quality Assurance",
    "track_order": 1,
    "is_primary": true,
    "notes": "Imported from concentrations_or_tracks.",
    "source_ids": [
      "uob_fas_food_science"
    ]
  },
  {
    "program_key": "uob-fas-master-food-science-technology",
    "track_name": "Food Processing Control",
    "track_order": 2,
    "is_primary": false,
    "notes": "Imported from concentrations_or_tracks.",
    "source_ids": [
      "uob_fas_food_science"
    ]
  },
  {
    "program_key": "uob-fas-master-french-language-literature",
    "track_name": "Literature",
    "track_order": 1,
    "is_primary": true,
    "notes": "Imported from concentrations_or_tracks.",
    "source_ids": [
      "uob_fas_graduate_catalogue_pdf"
    ]
  },
  {
    "program_key": "uob-fas-master-french-language-literature",
    "track_name": "Linguistics",
    "track_order": 2,
    "is_primary": false,
    "notes": "Imported from concentrations_or_tracks.",
    "source_ids": [
      "uob_fas_graduate_catalogue_pdf"
    ]
  },
  {
    "program_key": "uob-fas-master-french-language-literature",
    "track_name": "French as a Foreign and Second Language",
    "track_order": 3,
    "is_primary": false,
    "notes": "Imported from concentrations_or_tracks.",
    "source_ids": [
      "uob_fas_graduate_catalogue_pdf"
    ]
  },
  {
    "program_key": "uob-fas-master-physical-education",
    "track_name": "Physical Conditioning",
    "track_order": 1,
    "is_primary": true,
    "notes": "Imported from concentrations_or_tracks.",
    "source_ids": [
      "uob_fas_graduate_catalogue_pdf"
    ]
  },
  {
    "program_key": "uob-fas-master-physical-education",
    "track_name": "Physical Activity and Health",
    "track_order": 2,
    "is_primary": false,
    "notes": "Imported from concentrations_or_tracks.",
    "source_ids": [
      "uob_fas_graduate_catalogue_pdf"
    ]
  },
  {
    "program_key": "uob-fas-master-psychology",
    "track_name": "Clinical Developmental Psychology",
    "track_order": 1,
    "is_primary": true,
    "notes": "Imported from concentrations_or_tracks.",
    "source_ids": [
      "uob_fas_graduate_catalogue_pdf"
    ]
  },
  {
    "program_key": "uob-fas-master-psychology",
    "track_name": "Clinical Psychology",
    "track_order": 2,
    "is_primary": false,
    "notes": "Imported from concentrations_or_tracks.",
    "source_ids": [
      "uob_fas_graduate_catalogue_pdf"
    ]
  },
  {
    "program_key": "uob-fas-master-education-aley",
    "track_name": "Curriculum and Educational Management",
    "track_order": 1,
    "is_primary": true,
    "notes": "Imported from concentrations_or_tracks.",
    "source_ids": [
      "uob_fas_graduate_catalogue_pdf"
    ]
  },
  {
    "program_key": "uob-fas-master-education-aley",
    "track_name": "Instructional Design and Technology",
    "track_order": 2,
    "is_primary": false,
    "notes": "Imported from concentrations_or_tracks.",
    "source_ids": [
      "uob_fas_graduate_catalogue_pdf"
    ]
  },
  {
    "program_key": "uob-fobm-mba",
    "track_name": "Accounting",
    "track_order": 1,
    "is_primary": true,
    "notes": "Imported from concentrations_or_tracks.",
    "source_ids": [
      "uob_fobm_mba"
    ]
  },
  {
    "program_key": "uob-fobm-mba",
    "track_name": "Finance",
    "track_order": 2,
    "is_primary": false,
    "notes": "Imported from concentrations_or_tracks.",
    "source_ids": [
      "uob_fobm_mba"
    ]
  },
  {
    "program_key": "uob-fobm-mba",
    "track_name": "Health Care Management",
    "track_order": 3,
    "is_primary": false,
    "notes": "Imported from concentrations_or_tracks.",
    "source_ids": [
      "uob_fobm_mba"
    ]
  },
  {
    "program_key": "uob-fobm-mba",
    "track_name": "Human Resources Management",
    "track_order": 4,
    "is_primary": false,
    "notes": "Imported from concentrations_or_tracks.",
    "source_ids": [
      "uob_fobm_mba"
    ]
  },
  {
    "program_key": "uob-fobm-mba",
    "track_name": "Management",
    "track_order": 5,
    "is_primary": false,
    "notes": "Imported from concentrations_or_tracks.",
    "source_ids": [
      "uob_fobm_mba"
    ]
  },
  {
    "program_key": "uob-fobm-mba",
    "track_name": "Marketing",
    "track_order": 6,
    "is_primary": false,
    "notes": "Imported from concentrations_or_tracks.",
    "source_ids": [
      "uob_fobm_mba"
    ]
  },
  {
    "program_key": "uob-fhs-master-clinical-laboratory-sciences-lab-management",
    "track_name": "Laboratory Management",
    "track_order": 1,
    "is_primary": true,
    "notes": "Imported from concentrations_or_tracks.",
    "source_ids": [
      "uob_fhs_ms_cls"
    ]
  },
  {
    "program_key": "uob-fhs-master-medical-laboratory-sciences",
    "track_name": "Infectious Diseases and Immunology",
    "track_order": 1,
    "is_primary": true,
    "notes": "Imported from concentrations_or_tracks.",
    "source_ids": [
      "uob_fhs_ms_cls"
    ]
  },
  {
    "program_key": "uob-fhs-master-medical-laboratory-sciences",
    "track_name": "Molecular Biology",
    "track_order": 2,
    "is_primary": false,
    "notes": "Imported from concentrations_or_tracks.",
    "source_ids": [
      "uob_fhs_ms_cls"
    ]
  },
  {
    "program_key": "uob-fhs-master-nursing",
    "track_name": "Adult Care",
    "track_order": 1,
    "is_primary": true,
    "notes": "Imported from concentrations_or_tracks.",
    "source_ids": [
      "uob_fhs_ms_nursing"
    ]
  },
  {
    "program_key": "uob-fhs-master-nursing",
    "track_name": "Neonatal and Child Care",
    "track_order": 2,
    "is_primary": false,
    "notes": "Imported from concentrations_or_tracks.",
    "source_ids": [
      "uob_fhs_ms_nursing"
    ]
  },
  {
    "program_key": "uob-fhs-master-public-health",
    "track_name": "Community Health",
    "track_order": 1,
    "is_primary": true,
    "notes": "Imported from concentrations_or_tracks.",
    "source_ids": [
      "uob_fhs_mph"
    ]
  },
  {
    "program_key": "uob-fhs-master-public-health",
    "track_name": "Occupational and Environmental Health",
    "track_order": 2,
    "is_primary": false,
    "notes": "Imported from concentrations_or_tracks.",
    "source_ids": [
      "uob_fhs_mph"
    ]
  },
  {
    "program_key": "uob-fom-master-biomedical-sciences",
    "track_name": "Biochemistry",
    "track_order": 1,
    "is_primary": true,
    "notes": "Imported from concentrations_or_tracks.",
    "source_ids": [
      "uob_fom_ms_biomedical"
    ]
  },
  {
    "program_key": "uob-fom-master-biomedical-sciences",
    "track_name": "Immunology",
    "track_order": 2,
    "is_primary": false,
    "notes": "Imported from concentrations_or_tracks.",
    "source_ids": [
      "uob_fom_ms_biomedical"
    ]
  },
  {
    "program_key": "uob-fom-master-biomedical-sciences",
    "track_name": "Microbiology",
    "track_order": 3,
    "is_primary": false,
    "notes": "Imported from concentrations_or_tracks.",
    "source_ids": [
      "uob_fom_ms_biomedical"
    ]
  },
  {
    "program_key": "uob-fom-master-biomedical-sciences",
    "track_name": "Molecular Biology",
    "track_order": 4,
    "is_primary": false,
    "notes": "Imported from concentrations_or_tracks.",
    "source_ids": [
      "uob_fom_ms_biomedical"
    ]
  },
  {
    "program_key": "uob-fom-master-biomedical-sciences",
    "track_name": "Physiology",
    "track_order": 5,
    "is_primary": false,
    "notes": "Imported from concentrations_or_tracks.",
    "source_ids": [
      "uob_fom_ms_biomedical"
    ]
  }
]$UOB_TRACKS$::jsonb) AS seed(
        program_key TEXT,
        track_name TEXT,
        track_order INTEGER,
        is_primary BOOLEAN,
        notes TEXT,
        source_ids JSONB
    )
    JOIN graduate_program gp ON gp.university_id = v_university_id AND gp.program_key = seed.program_key
    ON CONFLICT (program_id, track_type, track_name) DO UPDATE SET
        track_order = EXCLUDED.track_order,
        is_primary = EXCLUDED.is_primary,
        source_id = EXCLUDED.source_id,
        notes = EXCLUDED.notes,
        updated_at = NOW();

    INSERT INTO graduate_admission_requirement (
        university_id, faculty_id, department_id, program_id, scope_level, record_key,
        requirement_type, requirement_text, comparison_operator, threshold_value, threshold_unit, is_required, notes, source_id
    )
    SELECT
        v_university_id,
        gp.faculty_id,
        gp.department_id,
        gp.id,
        'PROGRAM'::TEXT,
        seed.record_key,
        seed.requirement_type,
        seed.requirement_text,
        NULL::TEXT,
        NULL::NUMERIC,
        NULL::TEXT,
        TRUE,
        seed.notes,
        gp.source_id
    FROM jsonb_to_recordset($UOB_PROG_ADM2$[
  {
    "program_key": "uob-fas-master-political-science-international-affairs",
    "record_key": "uob-fas-master-political-science-international-affairs:admission_requirements",
    "requirement_type": "GENERAL",
    "requirement_text": "A Bachelor degree in Political Science with a minimum average of 80 is normally required; applicants slightly below 80 may be accepted on probation; applicants from other disciplines may be accepted on probation with bridging courses.",
    "notes": null,
    "source_ids": [
      "uob_fas_graduate_catalogue_pdf",
      "uob_fas",
      "uob_catalogue_page"
    ]
  },
  {
    "program_key": "uob-fas-master-sports-management",
    "record_key": "uob-fas-master-sports-management:admission_requirements",
    "requirement_type": "GENERAL",
    "requirement_text": "Candidates holding a BA in Physical Education or a BS in Business are eligible for the program.",
    "notes": null,
    "source_ids": [
      "uob_fas_graduate_catalogue_pdf",
      "uob_fas",
      "uob_catalogue_page"
    ]
  },
  {
    "program_key": "uob-fobm-emba",
    "record_key": "uob-fobm-emba:admission_requirements",
    "requirement_type": "GENERAL",
    "requirement_text": "Bachelor degree licensed from the Ministry of Education; five years of managerial experience; completed application with required documents; English proficiency; interview with the program committee.",
    "notes": null,
    "source_ids": [
      "uob_fobm_emba",
      "uob_fobm",
      "uob_general_graduate_catalogue_pdf"
    ]
  },
  {
    "program_key": "uob-fobm-emba",
    "record_key": "uob-fobm-emba:interview_requirement",
    "requirement_type": "INTERVIEW",
    "requirement_text": "Interview with the EMBA admission committee",
    "notes": null,
    "source_ids": [
      "uob_fobm_emba",
      "uob_fobm",
      "uob_general_graduate_catalogue_pdf"
    ]
  },
  {
    "program_key": "uob-fobm-emba",
    "record_key": "uob-fobm-emba:experience_requirement",
    "requirement_type": "EXPERIENCE",
    "requirement_text": "Five years of managerial experience",
    "notes": null,
    "source_ids": [
      "uob_fobm_emba",
      "uob_fobm",
      "uob_general_graduate_catalogue_pdf"
    ]
  }
]$UOB_PROG_ADM2$::jsonb) AS seed(
        program_key TEXT,
        record_key TEXT,
        requirement_type TEXT,
        requirement_text TEXT,
        notes TEXT,
        source_ids JSONB
    )
    JOIN graduate_program gp ON gp.university_id = v_university_id AND gp.program_key = seed.program_key
    JOIN LATERAL jsonb_array_elements_text(COALESCE(seed.source_ids, '[]'::jsonb)) WITH ORDINALITY AS fs(source_seed_id, ord)
      ON TRUE
    JOIN uob_source_seed us ON us.source_id = fs.source_seed_id
    JOIN source s ON s.university_id = v_university_id AND s.url = us.url
    ON CONFLICT (university_id, record_key) DO UPDATE SET
        faculty_id = EXCLUDED.faculty_id,
        department_id = EXCLUDED.department_id,
        program_id = EXCLUDED.program_id,
        scope_level = EXCLUDED.scope_level,
        requirement_type = EXCLUDED.requirement_type,
        requirement_text = EXCLUDED.requirement_text,
        notes = EXCLUDED.notes,
        source_id = EXCLUDED.source_id,
        updated_at = NOW();

END $$;
