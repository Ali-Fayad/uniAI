-- USEK graduate data seed migration.
-- Idempotent import for the canonical USEK graduate dataset.

DO $$
DECLARE
    v_university_id BIGINT;
BEGIN

    INSERT INTO university (name, name_ar, acronym, country, city, latitude, longitude, campus_name, campus_type)
    SELECT 'Holy Spirit University of Kaslik', NULL, 'USEK', 'Lebanon', 'Kaslik', NULL, NULL, NULL, NULL
    WHERE NOT EXISTS (SELECT 1 FROM university WHERE name = 'Holy Spirit University of Kaslik');

    SELECT id INTO v_university_id FROM university WHERE name = 'Holy Spirit University of Kaslik' ORDER BY id LIMIT 1;

    ALTER TABLE graduate_program DROP CONSTRAINT IF EXISTS uq_graduate_program_university_url;

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

    CREATE TEMP TABLE usek_source_seed (
        source_id TEXT PRIMARY KEY,
        page_title TEXT NOT NULL,
        url TEXT NOT NULL,
        date_accessed DATE,
        source_type TEXT,
        notes TEXT
    ) ON COMMIT DROP;

    INSERT INTO usek_source_seed (source_id, page_title, url, date_accessed, source_type, notes)
    SELECT source_id, page_title, url, date_accessed, source_type, notes
    FROM jsonb_to_recordset($USEK$[
  {
    "source_id": "usek_home_en",
    "page_title": "Holy Spirit University of Kaslik | Home",
    "url": "https://www.usek.edu.lb/en/home",
    "date_accessed": "2026-06-27",
    "source_type": "OTHER",
    "notes": "Official home page. Redirects from the bare domain and exposes academics, admissions, fees, scholarships, calendar, and program navigation."
  },
  {
    "source_id": "usek_academics",
    "page_title": "Holy Spirit University of Kaslik | Academics",
    "url": "https://www.usek.edu.lb/en/academics",
    "date_accessed": "2026-06-27",
    "source_type": "PROGRAM_LIST",
    "notes": "Central academics hub listing faculties, doctoral college, and graduate program entry points."
  },
  {
    "source_id": "usek_university_catalogue",
    "page_title": "Holy Spirit University of Kaslik | University Catalogue",
    "url": "https://www.usek.edu.lb/university-catalogue",
    "date_accessed": "2026-06-27",
    "source_type": "CATALOG",
    "notes": "Official catalogue hub for program discovery and later graduate parsing."
  },
  {
    "source_id": "usek_admission_grad_requirements",
    "page_title": "Holy Spirit University of Kaslik | Requirements",
    "url": "https://www.usek.edu.lb/en/admission/for-graduate-studies/requirements?parent=1",
    "date_accessed": "2026-06-27",
    "source_type": "ADMISSIONS",
    "notes": "Graduate admissions requirements page. Lists required documents, file submission steps, application fee, and registration guidance."
  },
  {
    "source_id": "usek_admission_grad_deadlines",
    "page_title": "Holy Spirit University of Kaslik | Dates and deadlines",
    "url": "https://www.usek.edu.lb/en/admission/for-graduate-studies/dates-and-deadlines?parent=1",
    "date_accessed": "2026-06-27",
    "source_type": "DEADLINE",
    "notes": "Graduate admissions deadline page. Includes application deadline, exam dates, results, and file-transfer deadline."
  },
  {
    "source_id": "usek_admission_doctoral",
    "page_title": "Holy Spirit University of Kaslik | Doctoral Studies",
    "url": "https://www.usek.edu.lb/en/admission/for-doctoral-studies",
    "date_accessed": "2026-06-27",
    "source_type": "ADMISSIONS",
    "notes": "Doctoral admissions surface with requirements and links to the list of PhD programs."
  },
  {
    "source_id": "usek_admission_guide_pdf",
    "page_title": "USEK Admissions Guide PDF",
    "url": "https://www.usek.edu.lb/Content/Assets/20260319AdmissionGuide-021354.pdf",
    "date_accessed": "2026-06-27",
    "source_type": "PDF",
    "notes": "Official admissions guide PDF linked from the graduate admissions page."
  },
  {
    "source_id": "usek_calendar_pdf",
    "page_title": "USEK Academic Calendar PDF",
    "url": "https://www.usek.edu.lb/ContentFiles/78FullCalendar.pdf?v=202604082131",
    "date_accessed": "2026-06-27",
    "source_type": "PDF",
    "notes": "Official academic calendar PDF linked from the calendar page."
  },
  {
    "source_id": "usek_grad_fees",
    "page_title": "Holy Spirit University of Kaslik | Graduate Studies",
    "url": "https://www.usek.edu.lb/en/university-fees/graduate-studies-1",
    "date_accessed": "2026-06-27",
    "source_type": "TUITION_FEES",
    "notes": "Official graduate-fees page. Contains graduate registration/operational/NSSF fees and the graduate credit-fee table."
  },
  {
    "source_id": "usek_scholarships",
    "page_title": "Holy Spirit University of Kaslik | Scholarships and Financial Aid",
    "url": "https://www.usek.edu.lb/students/scholarships-and-financial-aids",
    "date_accessed": "2026-06-27",
    "source_type": "FINANCIAL_AID",
    "notes": "University-wide scholarships and financial aid hub. References discounts, allowances, and aid applications."
  },
  {
    "source_id": "usek_doctoral_college",
    "page_title": "Holy Spirit University of Kaslik | Doctoral College",
    "url": "https://www.usek.edu.lb/doctoral-college",
    "date_accessed": "2026-06-27",
    "source_type": "OTHER",
    "notes": "Doctoral college hub. Useful for PhD governance, supervision, rules, and thesis-related pages."
  },
  {
    "source_id": "usek_business_school",
    "page_title": "Holy Spirit University of Kaslik | Business School",
    "url": "https://www.usek.edu.lb/business",
    "date_accessed": "2026-06-27",
    "source_type": "SCHOOL_PAGE",
    "notes": "Business School landing page with graduate and doctoral tabs."
  },
  {
    "source_id": "usek_engineering_school",
    "page_title": "Holy Spirit University of Kaslik | School of Engineering",
    "url": "https://www.usek.edu.lb/en/academics/school-of-engineering",
    "date_accessed": "2026-06-27",
    "source_type": "SCHOOL_PAGE",
    "notes": "Engineering school landing page with graduate-program tab and program pages nested under faculty paths."
  },
  {
    "source_id": "usek_architecture_school",
    "page_title": "Holy Spirit University of Kaslik | School of Architecture and Design",
    "url": "https://www.usek.edu.lb/academics/school-of-architecture-and-design",
    "date_accessed": "2026-06-27",
    "source_type": "SCHOOL_PAGE",
    "notes": "Architecture and design school landing page with graduate-program tab."
  },
  {
    "source_id": "usek_law_school",
    "page_title": "Holy Spirit University of Kaslik | School of Law and Political Sciences",
    "url": "https://www.usek.edu.lb/academics/school-of-law-and-political-sciences",
    "date_accessed": "2026-06-27",
    "source_type": "SCHOOL_PAGE",
    "notes": "Law and political sciences landing page with graduate and doctoral tabs."
  },
  {
    "source_id": "usek_medicine_school",
    "page_title": "Holy Spirit University of Kaslik | School of Medicine and Medical Sciences",
    "url": "https://www.usek.edu.lb/school-of-medicine-and-medical-sciences",
    "date_accessed": "2026-06-27",
    "source_type": "SCHOOL_PAGE",
    "notes": "Medicine and medical sciences landing page with graduate, doctoral, and postdoctoral tabs."
  },
  {
    "source_id": "usek_theology_school",
    "page_title": "Holy Spirit University of Kaslik | Pontifical School of Theology",
    "url": "https://www.usek.edu.lb/academics/pontifical-school-of-theology",
    "date_accessed": "2026-06-27",
    "source_type": "SCHOOL_PAGE",
    "notes": "Pontifical School of Theology landing page with graduate and doctoral tabs."
  },
  {
    "source_id": "usek_nursing_school",
    "page_title": "Holy Spirit University of Kaslik | Higher Institute of Nursing Sciences",
    "url": "https://www.usek.edu.lb/en/academics/higher-institute-of-nursing-sciences",
    "date_accessed": "2026-06-27",
    "source_type": "SCHOOL_PAGE",
    "notes": "Nursing institute landing page. Graduate-fee schedule references nursing as a graduate-credit line, but a program page was not canonicalized in this pass."
  },
  {
    "source_id": "usek_arts_sciences_school",
    "page_title": "Holy Spirit University of Kaslik | Faculty of Arts and Sciences",
    "url": "https://www.usek.edu.lb/academics/faculty-of-arts-and-sciences",
    "date_accessed": "2026-06-27",
    "source_type": "SCHOOL_PAGE",
    "notes": "Faculty of Arts and Sciences landing page with graduate and doctoral tabs."
  },
  {
    "source_id": "usek_mba_page",
    "page_title": "Holy Spirit University of Kaslik | Master of Business Administration",
    "url": "https://www.usek.edu.lb/en/faculty-of-business-and-commercial-sciences/academic-programs/business-administration/master-business-administration?t=2",
    "date_accessed": "2026-06-27",
    "source_type": "PROGRAM_PAGE",
    "notes": "Confirmed graduate program page. Lists English, 39 credits, thesis requirement, and emphasis options."
  },
  {
    "source_id": "usek_dba_page",
    "page_title": "Holy Spirit University of Kaslik | Doctorate in Business Administration",
    "url": "https://www.usek.edu.lb/en/academic-programs/doctoral-studies/doctorate-in-business-administration?t=5",
    "date_accessed": "2026-06-27",
    "source_type": "PROGRAM_PAGE",
    "notes": "Confirmed doctoral program page. Lists English and 60 credits."
  },
  {
    "source_id": "usek_mba_fe_page",
    "page_title": "Holy Spirit University of Kaslik | Master of Business Administration - Financial Engineering",
    "url": "https://www.usek.edu.lb/en/finance-department/master-business-administration-financial-engineering-2?t=2",
    "date_accessed": "2026-06-27",
    "source_type": "PROGRAM_PAGE",
    "notes": "Confirmed graduate program page nested under Finance Department."
  },
  {
    "source_id": "usek_mba_hr_page",
    "page_title": "Holy Spirit University of Kaslik | Master of Business Administration - Human Resources",
    "url": "https://www.usek.edu.lb/en/management-department/master-business-administration-human-resources?t=2",
    "date_accessed": "2026-06-27",
    "source_type": "PROGRAM_PAGE",
    "notes": "Confirmed graduate program page nested under Management Department."
  },
  {
    "source_id": "usek_business_law_page",
    "page_title": "Holy Spirit University of Kaslik | Master in Business Law",
    "url": "https://www.usek.edu.lb/law/academic-programs/master-in-business-law?t=2",
    "date_accessed": "2026-06-27",
    "source_type": "PROGRAM_PAGE",
    "notes": "Confirmed graduate program page in the School of Law and Political Sciences."
  },
  {
    "source_id": "usek_criminology_page",
    "page_title": "Holy Spirit University of Kaslik | Master in Criminology",
    "url": "https://www.usek.edu.lb/en/department-of-criminology/master-in-criminology-2?t=2",
    "date_accessed": "2026-06-27",
    "source_type": "PROGRAM_PAGE",
    "notes": "Confirmed graduate program page nested under the Department of Criminology."
  },
  {
    "source_id": "usek_theology_ma_page",
    "page_title": "Holy Spirit University of Kaslik | Master of Arts in Theology",
    "url": "https://www.usek.edu.lb/en/pontifical-school-of-theology/master-of-arts-in-theology?t=2",
    "date_accessed": "2026-06-27",
    "source_type": "PROGRAM_PAGE",
    "notes": "Confirmed graduate program page in theology."
  },
  {
    "source_id": "usek_theology_phd_page",
    "page_title": "Holy Spirit University of Kaslik | PhD in Theology",
    "url": "https://www.usek.edu.lb/en/pontifical-school-of-theology/phd-in-theology?t=5",
    "date_accessed": "2026-06-27",
    "source_type": "PROGRAM_PAGE",
    "notes": "Confirmed doctoral program page in theology."
  },
  {
    "source_id": "usek_biomedical_engineering_page",
    "page_title": "Holy Spirit University of Kaslik | Master of Science in Biomedical Engineering",
    "url": "https://www.usek.edu.lb/en/fi-academic-programs/department-of-biomedical-engineering-2/master-of-science-in-biomedical-engineering?t=2",
    "date_accessed": "2026-06-27",
    "source_type": "PROGRAM_PAGE",
    "notes": "Confirmed engineering graduate page with credits and English language shown."
  },
  {
    "source_id": "usek_architecture_combined_page",
    "page_title": "Holy Spirit University of Kaslik | Bachelor and Master in Architecture (Combined Program)",
    "url": "https://www.usek.edu.lb/en/department-of-architecture/bachelor-of-science-in-architectural-studies-22?t=2",
    "date_accessed": "2026-06-27",
    "source_type": "PROGRAM_PAGE",
    "notes": "Combined program page. Relevant later because it contains the graduate architecture component."
  },
  {
    "source_id": "usek_multiple_sclerosis_diploma",
    "page_title": "Holy Spirit University of Kaslik | University Diploma in Multiple Sclerosis",
    "url": "https://www.usek.edu.lb/en/academic-programs/department-of-doctoral-studies/university-diploma-in-multiple-sclerosis?t=2",
    "date_accessed": "2026-06-27",
    "source_type": "PROGRAM_PAGE",
    "notes": "Official program page surfaced during discovery but is out of scope for the current MASTER/PHD inventory."
  },
  {
    "source_id": "usek_interpretation_diploma",
    "page_title": "Holy Spirit University of Kaslik | Diploma in Interpretation",
    "url": "https://www.usek.edu.lb/academics/faculty-of-arts-and-sciences/department-of-languages-and-literatures/diploma-in-interpretation?t=2",
    "date_accessed": "2026-06-27",
    "source_type": "PROGRAM_PAGE",
    "notes": "Official program page surfaced during discovery but is out of scope for the current MASTER/PHD inventory."
  },
  {
    "source_id": "usek_business_mia_page",
    "page_title": "Holy Spirit University of Kaslik | Master of Business Administration – Management and International Affairs",
    "url": "https://www.usek.edu.lb/en/management-department/master-in-business-administration-management-and-international-affairs-2?t=2",
    "date_accessed": "2026-06-27",
    "source_type": "PROGRAM_PAGE",
    "notes": "Confirmed graduate program page. Lists English, 39 credits, and a dual diploma partnership with HEC Montréal."
  },
  {
    "source_id": "usek_business_phd_page",
    "page_title": "Holy Spirit University of Kaslik | Ph.D. in Business",
    "url": "https://www.usek.edu.lb/en/doctoral-studies/phd-in-business-2?t=5",
    "date_accessed": "2026-06-27",
    "source_type": "PROGRAM_PAGE",
    "notes": "Confirmed doctoral program page. Lists multilingual instruction and 60 credits."
  },
  {
    "source_id": "usek_law_phd_page",
    "page_title": "Holy Spirit University of Kaslik | Ph.D. in Law",
    "url": "https://www.usek.edu.lb/en/law/phd-in-law-2?t=5",
    "date_accessed": "2026-06-27",
    "source_type": "PROGRAM_PAGE",
    "notes": "Confirmed doctoral program page. Lists multilingual instruction and 60 credits."
  },
  {
    "source_id": "usek_medicine_doctorate_page",
    "page_title": "Holy Spirit University of Kaslik | Doctorate of Medicine",
    "url": "https://www.usek.edu.lb/en/department-of-medical-sciences/doctorate-of-medicine-2?t=5",
    "date_accessed": "2026-06-27",
    "source_type": "PROGRAM_PAGE",
    "notes": "Confirmed doctorate page. Lists multilingual instruction and 120 credits."
  },
  {
    "source_id": "usek_music_school",
    "page_title": "Holy Spirit University of Kaslik | School of Music and Performing Arts",
    "url": "https://www.usek.edu.lb/school-of-music-and-performing-arts",
    "date_accessed": "2026-06-27",
    "source_type": "SCHOOL_PAGE",
    "notes": "School hub with graduate and doctoral tabs. Linked from the music program pages."
  },
  {
    "source_id": "usek_music_master_page",
    "page_title": "Holy Spirit University of Kaslik | Master of Arts in Music",
    "url": "https://www.usek.edu.lb/fmus-academic-programs/department-of-music/master-of-arts-in-music?t=2",
    "date_accessed": "2026-06-27",
    "source_type": "PROGRAM_PAGE",
    "notes": "Confirmed graduate program page. Lists multilingual instruction, 36 credits, and Musicology emphasis."
  },
  {
    "source_id": "usek_music_performing_arts_page",
    "page_title": "Holy Spirit University of Kaslik | Master of Arts in Performing Arts",
    "url": "https://www.usek.edu.lb/fmus/department-of-performing-arts/master-in-performing-arts?t=2",
    "date_accessed": "2026-06-27",
    "source_type": "PROGRAM_PAGE",
    "notes": "Confirmed graduate program page. Lists multilingual instruction and 36 credits."
  },
  {
    "source_id": "usek_music_phd_page",
    "page_title": "Holy Spirit University of Kaslik | Ph.D. in Music and Ph.D. in Higher and Specialized Music Education",
    "url": "https://www.usek.edu.lb/fmus/academic-program/phd?t=5",
    "date_accessed": "2026-06-27",
    "source_type": "PROGRAM_PAGE",
    "notes": "Doctoral page listing both Ph.D. in Music and Ph.D. in Higher and Specialized Music Education; multilingual; 60 credits."
  },
  {
    "source_id": "usek_architecture_interior_page",
    "page_title": "Holy Spirit University of Kaslik | Master in Interior Architecture",
    "url": "https://www.usek.edu.lb/en/department-of-interior-design/master-in-interior-design-2?t=2",
    "date_accessed": "2026-06-27",
    "source_type": "PROGRAM_PAGE",
    "notes": "Confirmed graduate program page. Lists multilingual instruction and 42 credits."
  },
  {
    "source_id": "usek_architecture_comm_visual_page",
    "page_title": "Holy Spirit University of Kaslik | Master of Arts in Communication and Visual Arts",
    "url": "https://www.usek.edu.lb/fba/department-of-communication-and-visual-arts/master-of-arts-in-communication-and-visual-arts?t=2",
    "date_accessed": "2026-06-27",
    "source_type": "PROGRAM_PAGE",
    "notes": "Confirmed graduate program page. Lists English instruction and 42 credits."
  },
  {
    "source_id": "usek_architecture_contemporary_art_page",
    "page_title": "Holy Spirit University of Kaslik | Master of Arts in Contemporary Art",
    "url": "https://www.usek.edu.lb/fbaaa-academic-programs/master-contemporary-art?t=2",
    "date_accessed": "2026-06-27",
    "source_type": "PROGRAM_PAGE",
    "notes": "Confirmed graduate program page. Lists multilingual instruction and 36 credits."
  },
  {
    "source_id": "usek_architecture_digital_media_page",
    "page_title": "Holy Spirit University of Kaslik | Master of Arts in Digital Media",
    "url": "https://www.usek.edu.lb/fbaaa/department-of-audio-visual/master-of-arts-in-digital-media?t=2",
    "date_accessed": "2026-06-27",
    "source_type": "PROGRAM_PAGE",
    "notes": "Confirmed graduate program page. Lists English instruction and 42 credits."
  },
  {
    "source_id": "usek_fas_phd_list",
    "page_title": "Holy Spirit University of Kaslik | Ph.D. in Agricultural and Food Sciences",
    "url": "https://www.usek.edu.lb/faculty-of-arts-and-sciences/academic-programs/department-of-nutrition-and-food-sciences/phd-in-agricultural-and-food-sciences?t=5",
    "date_accessed": "2026-06-27",
    "source_type": "PROGRAM_PAGE",
    "notes": "Doctoral hub page listing all Faculty of Arts and Sciences PhD titles; also the direct page for Ph.D. in Agricultural and Food Sciences."
  },
  {
    "source_id": "usek_doctoral_eligibility_pdf",
    "page_title": "USEK Doctoral Rules and regulations / Article 3 Admission Eligibility Requirements",
    "url": "https://www.usek.edu.lb/Content/Assets/20220622AdmissionEligibilityRequirements-013855.pdf",
    "date_accessed": "2026-06-27",
    "source_type": "PDF",
    "notes": "Official doctoral eligibility appendix linked from the doctoral studies page. Lists program-specific GPA thresholds for the PhD inventory and is useful for doctoral admissions."
  }
]$USEK$::jsonb) AS seed(source_id TEXT, page_title TEXT, url TEXT, date_accessed DATE, source_type TEXT, notes TEXT);

    INSERT INTO source (university_id, title, url, source_type, accessed_at)
    SELECT v_university_id, page_title, url, source_type, date_accessed
    FROM usek_source_seed
    ON CONFLICT (university_id, url) DO UPDATE SET
        title = EXCLUDED.title,
        source_type = EXCLUDED.source_type,
        accessed_at = EXCLUDED.accessed_at,
        updated_at = NOW();

    INSERT INTO university_faculty (university_id, name, short_name, faculty_type, official_url, notes)
    SELECT v_university_id, name, short_name, faculty_type, official_url, notes
    FROM jsonb_to_recordset($USEK$[
  {
    "name": "Business School",
    "short_name": "Business",
    "faculty_type": "SCHOOL",
    "official_url": "https://www.usek.edu.lb/business",
    "notes": null
  },
  {
    "name": "School of Law and Political Sciences",
    "short_name": "Law and Political Sciences",
    "faculty_type": "SCHOOL",
    "official_url": "https://www.usek.edu.lb/academics/school-of-law-and-political-sciences",
    "notes": null
  },
  {
    "name": "Pontifical School of Theology",
    "short_name": "Theology",
    "faculty_type": "SCHOOL",
    "official_url": "https://www.usek.edu.lb/academics/pontifical-school-of-theology",
    "notes": null
  },
  {
    "name": "School of Engineering",
    "short_name": "Engineering",
    "faculty_type": "SCHOOL",
    "official_url": "https://www.usek.edu.lb/en/academics/school-of-engineering",
    "notes": null
  },
  {
    "name": "School of Architecture and Design",
    "short_name": "Architecture and Design",
    "faculty_type": "SCHOOL",
    "official_url": "https://www.usek.edu.lb/academics/school-of-architecture-and-design",
    "notes": null
  },
  {
    "name": "School of Medicine and Medical Sciences",
    "short_name": "Medicine",
    "faculty_type": "SCHOOL",
    "official_url": "https://www.usek.edu.lb/school-of-medicine-and-medical-sciences",
    "notes": null
  },
  {
    "name": "School of Music and Performing Arts",
    "short_name": "Music and Performing Arts",
    "faculty_type": "SCHOOL",
    "official_url": "https://www.usek.edu.lb/school-of-music-and-performing-arts",
    "notes": null
  },
  {
    "name": "Faculty of Arts and Sciences",
    "short_name": "Arts and Sciences",
    "faculty_type": "FACULTY",
    "official_url": "https://www.usek.edu.lb/academics/faculty-of-arts-and-sciences",
    "notes": null
  }
]$USEK$::jsonb) AS seed(name TEXT, short_name TEXT, faculty_type TEXT, official_url TEXT, notes TEXT)
    ON CONFLICT (university_id, name) DO UPDATE SET
        short_name = EXCLUDED.short_name,
        faculty_type = EXCLUDED.faculty_type,
        official_url = EXCLUDED.official_url,
        notes = EXCLUDED.notes,
        updated_at = NOW();

    INSERT INTO university_department (university_id, faculty_id, name, short_name, official_url, notes)
    SELECT v_university_id, fac.id, dep.name, dep.short_name, dep.official_url, dep.notes
    FROM jsonb_to_recordset($USEK$[
  {
    "faculty_name": "Business School",
    "name": "Business Administration",
    "short_name": null,
    "official_url": null,
    "notes": null
  },
  {
    "faculty_name": "Business School",
    "name": "Finance Department",
    "short_name": null,
    "official_url": null,
    "notes": null
  },
  {
    "faculty_name": "Business School",
    "name": "Management Department",
    "short_name": null,
    "official_url": null,
    "notes": null
  },
  {
    "faculty_name": "Business School",
    "name": "Doctoral Studies",
    "short_name": null,
    "official_url": null,
    "notes": null
  },
  {
    "faculty_name": "School of Law and Political Sciences",
    "name": "Law",
    "short_name": null,
    "official_url": null,
    "notes": null
  },
  {
    "faculty_name": "School of Law and Political Sciences",
    "name": "Department of Criminology",
    "short_name": null,
    "official_url": null,
    "notes": null
  },
  {
    "faculty_name": "School of Engineering",
    "name": "Department of Biomedical Engineering",
    "short_name": null,
    "official_url": null,
    "notes": null
  },
  {
    "faculty_name": "School of Architecture and Design",
    "name": "Department of Design and Interior Architecture",
    "short_name": null,
    "official_url": null,
    "notes": null
  },
  {
    "faculty_name": "School of Architecture and Design",
    "name": "Department of Visual Communication",
    "short_name": null,
    "official_url": null,
    "notes": null
  },
  {
    "faculty_name": "School of Architecture and Design",
    "name": "Department of Digital Media",
    "short_name": null,
    "official_url": null,
    "notes": null
  },
  {
    "faculty_name": "School of Medicine and Medical Sciences",
    "name": "Department of Medical Sciences",
    "short_name": null,
    "official_url": null,
    "notes": null
  },
  {
    "faculty_name": "School of Music and Performing Arts",
    "name": "Department of Music",
    "short_name": null,
    "official_url": null,
    "notes": null
  },
  {
    "faculty_name": "School of Music and Performing Arts",
    "name": "Department of Performing Arts",
    "short_name": null,
    "official_url": null,
    "notes": null
  },
  {
    "faculty_name": "School of Music and Performing Arts",
    "name": "Department of Higher and Specialized Musical Education",
    "short_name": null,
    "official_url": null,
    "notes": null
  },
  {
    "faculty_name": "Faculty of Arts and Sciences",
    "name": "Department of Nutrition and Food Sciences",
    "short_name": null,
    "official_url": null,
    "notes": null
  }
]$USEK$::jsonb) AS dep(faculty_name TEXT, name TEXT, short_name TEXT, official_url TEXT, notes TEXT)
    JOIN university_faculty fac
      ON fac.university_id = v_university_id
     AND fac.name = dep.faculty_name
    ON CONFLICT (university_id, faculty_id, name) DO UPDATE SET
        short_name = EXCLUDED.short_name,
        official_url = EXCLUDED.official_url,
        notes = EXCLUDED.notes,
        updated_at = NOW();

    CREATE TEMP TABLE usek_program_seed (
        id TEXT PRIMARY KEY,
        faculty TEXT,
        department TEXT,
        major_category TEXT,
        major TEXT,
        degree_type TEXT,
        official_degree_name TEXT,
        thesis_or_non_thesis TEXT,
        credits INTEGER,
        duration_value NUMERIC(10,2),
        duration_unit TEXT,
        duration_raw_text TEXT,
        language TEXT,
        delivery_mode TEXT,
        program_description TEXT,
        official_program_url TEXT,
        notes TEXT,
        source_ids JSONB,
        tuition_academic_year TEXT,
        tuition_currency TEXT,
        tuition_billing_basis TEXT,
        tuition_amount NUMERIC(12,2),
        tuition_category TEXT,
        tuition_notes TEXT,
        tuition_source_ids JSONB,
        track_names JSONB
    ) ON COMMIT DROP;

    INSERT INTO usek_program_seed (id, faculty, department, major_category, major, degree_type, official_degree_name, thesis_or_non_thesis, credits, duration_value, duration_unit, duration_raw_text, language, delivery_mode, program_description, official_program_url, notes, source_ids, tuition_academic_year, tuition_currency, tuition_billing_basis, tuition_amount, tuition_category, tuition_notes, tuition_source_ids, track_names)
    SELECT id, faculty, department, major_category, major, degree_type, official_degree_name, thesis_or_non_thesis, credits, duration_value, duration_unit, duration_raw_text, language, delivery_mode, program_description, official_program_url, notes, source_ids, tuition_academic_year, tuition_currency, tuition_billing_basis, tuition_amount, tuition_category, tuition_notes, tuition_source_ids, track_names
    FROM jsonb_to_recordset($USEK$[
  {
    "id": "usek-business-master-business-administration",
    "faculty": "Business School",
    "department": "Business Administration",
    "major_category": "Business",
    "major": "Business Administration",
    "degree_type": "MASTER",
    "official_degree_name": "Master of Business Administration",
    "thesis_or_non_thesis": "THESIS",
    "credits": 39,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": "English",
    "delivery_mode": null,
    "program_description": "MBA with emphases in Audit, Finance, and Marketing.",
    "official_program_url": "https://www.usek.edu.lb/en/faculty-of-business-and-commercial-sciences/academic-programs/business-administration/master-business-administration?t=2",
    "notes": "Official page lists the three emphases and a thesis requirement.",
    "source_ids": [
      "usek_mba_page",
      "usek_business_school"
    ],
    "tuition_academic_year": "2024-2025",
    "tuition_currency": "USD",
    "tuition_billing_basis": "PER_CREDIT",
    "tuition_amount": 480,
    "tuition_category": "School of Business - Business Administration",
    "tuition_notes": "Official USEK graduate credit-fee table (2024-2025).",
    "tuition_source_ids": [
      "usek_grad_fees"
    ],
    "track_names": [
      "Audit",
      "Finance",
      "Marketing"
    ]
  },
  {
    "id": "usek-business-master-business-administration-financial-engineering",
    "faculty": "Business School",
    "department": "Finance Department",
    "major_category": "Business",
    "major": "Business Administration - Financial Engineering",
    "degree_type": "MASTER",
    "official_degree_name": "Master of Business Administration - Financial Engineering",
    "thesis_or_non_thesis": "THESIS",
    "credits": 39,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": "English",
    "delivery_mode": null,
    "program_description": "MBA emphasis in Financial Engineering.",
    "official_program_url": "https://www.usek.edu.lb/en/finance-department/master-business-administration-financial-engineering-2?t=2",
    "notes": "Official page lists Financial Engineering emphasis and a master professional thesis.",
    "source_ids": [
      "usek_mba_fe_page",
      "usek_business_school"
    ],
    "tuition_academic_year": "2024-2025",
    "tuition_currency": "USD",
    "tuition_billing_basis": "PER_CREDIT",
    "tuition_amount": 480,
    "tuition_category": "School of Business - Business Administration",
    "tuition_notes": "Official USEK graduate credit-fee table (2024-2025).",
    "tuition_source_ids": [
      "usek_grad_fees"
    ],
    "track_names": [
      "Financial Engineering"
    ]
  },
  {
    "id": "usek-business-master-business-administration-human-resources",
    "faculty": "Business School",
    "department": "Management Department",
    "major_category": "Business",
    "major": "Business Administration - Human Resources",
    "degree_type": "MASTER",
    "official_degree_name": "Master of Business Administration - Human Resources",
    "thesis_or_non_thesis": "THESIS",
    "credits": 39,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": "English",
    "delivery_mode": null,
    "program_description": "MBA emphasis in Human Resources; offered in collaboration with Université Panthéon-Assas - Paris II.",
    "official_program_url": "https://www.usek.edu.lb/en/management-department/master-business-administration-human-resources?t=2",
    "notes": "Official page states collaboration with Université Panthéon-Assas - Paris II and a master thesis requirement.",
    "source_ids": [
      "usek_mba_hr_page",
      "usek_business_school"
    ],
    "tuition_academic_year": "2024-2025",
    "tuition_currency": "USD",
    "tuition_billing_basis": "PER_CREDIT",
    "tuition_amount": 480,
    "tuition_category": "School of Business - Business Administration",
    "tuition_notes": "Official USEK graduate credit-fee table (2024-2025).",
    "tuition_source_ids": [
      "usek_grad_fees"
    ],
    "track_names": [
      "Human Resources"
    ]
  },
  {
    "id": "usek-business-master-business-administration-management-and-international-affairs",
    "faculty": "Business School",
    "department": "Management Department",
    "major_category": "Business",
    "major": "Business Administration - Management and International Affairs",
    "degree_type": "MASTER",
    "official_degree_name": "Master of Business Administration – Management and International Affairs",
    "thesis_or_non_thesis": "THESIS",
    "credits": 39,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": "English",
    "delivery_mode": null,
    "program_description": "MBA emphasis in Management and International Affairs; dual diploma with HEC Montréal.",
    "official_program_url": "https://www.usek.edu.lb/en/management-department/master-in-business-administration-management-and-international-affairs-2?t=2",
    "notes": "Official page describes a dual diploma in management, equivalent to a D.E.S.S. from HEC Montréal and an MBA from USEK.",
    "source_ids": [
      "usek_business_mia_page",
      "usek_business_school"
    ],
    "tuition_academic_year": "2024-2025",
    "tuition_currency": "USD",
    "tuition_billing_basis": "PER_CREDIT",
    "tuition_amount": 480,
    "tuition_category": "School of Business - Business Administration",
    "tuition_notes": "Official USEK graduate credit-fee table (2024-2025).",
    "tuition_source_ids": [
      "usek_grad_fees"
    ],
    "track_names": [
      "Management and International Affairs"
    ]
  },
  {
    "id": "usek-business-phd-business-administration",
    "faculty": "Business School",
    "department": "Doctoral Studies",
    "major_category": "Business",
    "major": "Business",
    "degree_type": "PHD",
    "official_degree_name": "Ph.D. in Business",
    "thesis_or_non_thesis": null,
    "credits": 60,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": "Multilingual",
    "delivery_mode": null,
    "program_description": "Doctoral program in business administration focused on original research and business research methods.",
    "official_program_url": "https://www.usek.edu.lb/en/doctoral-studies/phd-in-business-2?t=5",
    "notes": "Doctoral page lists multilingual instruction and 60 credits.",
    "source_ids": [
      "usek_business_phd_page",
      "usek_business_school",
      "usek_doctoral_college"
    ],
    "tuition_academic_year": null,
    "tuition_currency": null,
    "tuition_billing_basis": null,
    "tuition_amount": null,
    "tuition_category": null,
    "tuition_notes": null,
    "tuition_source_ids": [],
    "track_names": []
  },
  {
    "id": "usek-business-doctorate-business-administration",
    "faculty": "Business School",
    "department": "Doctoral Studies",
    "major_category": "Business",
    "major": "Business Administration",
    "degree_type": "PHD",
    "official_degree_name": "Doctorate in Business Administration",
    "thesis_or_non_thesis": null,
    "credits": 60,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": "English",
    "delivery_mode": null,
    "program_description": "Dual Doctorate in Business Administration with Excelia Business School.",
    "official_program_url": "https://www.usek.edu.lb/en/academic-programs/doctoral-studies/doctorate-in-business-administration?t=5",
    "notes": "Official page lists English instruction, 60 credits, and a partnership with Excelia Business School.",
    "source_ids": [
      "usek_dba_page",
      "usek_business_school",
      "usek_doctoral_college"
    ],
    "tuition_academic_year": null,
    "tuition_currency": null,
    "tuition_billing_basis": null,
    "tuition_amount": null,
    "tuition_category": null,
    "tuition_notes": null,
    "tuition_source_ids": [],
    "track_names": []
  },
  {
    "id": "usek-law-master-business-law",
    "faculty": "School of Law and Political Sciences",
    "department": "Law",
    "major_category": "Law and Political Sciences",
    "major": "Business Law",
    "degree_type": "MASTER",
    "official_degree_name": "Master in Business Law",
    "thesis_or_non_thesis": "THESIS_OR_PROJECT",
    "credits": 26,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": "Multilingual",
    "delivery_mode": null,
    "program_description": "Comparative training in French, European, and Lebanese business law in partnership with Université de Poitiers.",
    "official_program_url": "https://www.usek.edu.lb/law/academic-programs/master-in-business-law?t=2",
    "notes": "Official page lists multilingual instruction, 26 credits, and a research dissertation / internship project structure.",
    "source_ids": [
      "usek_business_law_page",
      "usek_law_school"
    ],
    "tuition_academic_year": "2024-2025",
    "tuition_currency": "USD",
    "tuition_billing_basis": "PER_CREDIT",
    "tuition_amount": 309,
    "tuition_category": "School of Law and Political Sciences - Law / Political Sciences",
    "tuition_notes": "Official USEK graduate credit-fee table (2024-2025).",
    "tuition_source_ids": [
      "usek_grad_fees"
    ],
    "track_names": []
  },
  {
    "id": "usek-law-master-criminology",
    "faculty": "School of Law and Political Sciences",
    "department": "Department of Criminology",
    "major_category": "Law and Political Sciences",
    "major": "Criminology",
    "degree_type": "MASTER",
    "official_degree_name": "Master in Criminology",
    "thesis_or_non_thesis": null,
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": null,
    "delivery_mode": null,
    "program_description": null,
    "official_program_url": "https://www.usek.edu.lb/en/department-of-criminology/master-in-criminology-2?t=2",
    "notes": "Direct program page title confirmed. The captured HTML snapshot reused business-law content, so only the title/breadcrumb were used for inventory purposes.",
    "source_ids": [
      "usek_criminology_page",
      "usek_business_law_page",
      "usek_law_school"
    ],
    "tuition_academic_year": "2024-2025",
    "tuition_currency": "USD",
    "tuition_billing_basis": "PER_CREDIT",
    "tuition_amount": 309,
    "tuition_category": "School of Law and Political Sciences - Law / Political Sciences",
    "tuition_notes": "Official USEK graduate credit-fee table (2024-2025).",
    "tuition_source_ids": [
      "usek_grad_fees"
    ],
    "track_names": []
  },
  {
    "id": "usek-law-master-diplomacy-and-international-security",
    "faculty": "School of Law and Political Sciences",
    "department": null,
    "major_category": "Law and Political Sciences",
    "major": "Diplomacy and International Security",
    "degree_type": "MASTER",
    "official_degree_name": "Master in Diplomacy and International Security",
    "thesis_or_non_thesis": null,
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": null,
    "delivery_mode": null,
    "program_description": null,
    "official_program_url": "https://www.usek.edu.lb/law/academic-programs/master-in-business-law?t=2",
    "notes": "Listed on the School of Law and Political Sciences graduate hub page; individual program page was not canonicalized in this pass.",
    "source_ids": [
      "usek_business_law_page",
      "usek_law_school"
    ],
    "tuition_academic_year": "2024-2025",
    "tuition_currency": "USD",
    "tuition_billing_basis": "PER_CREDIT",
    "tuition_amount": 309,
    "tuition_category": "School of Law and Political Sciences - Law / Political Sciences",
    "tuition_notes": "Official USEK graduate credit-fee table (2024-2025).",
    "tuition_source_ids": [
      "usek_grad_fees"
    ],
    "track_names": []
  },
  {
    "id": "usek-law-master-international-contracts",
    "faculty": "School of Law and Political Sciences",
    "department": null,
    "major_category": "Law and Political Sciences",
    "major": "International Contracts",
    "degree_type": "MASTER",
    "official_degree_name": "Master in International Contracts",
    "thesis_or_non_thesis": null,
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": null,
    "delivery_mode": null,
    "program_description": null,
    "official_program_url": "https://www.usek.edu.lb/law/academic-programs/master-in-business-law?t=2",
    "notes": "Listed on the School of Law and Political Sciences graduate hub page; individual program page was not canonicalized in this pass.",
    "source_ids": [
      "usek_business_law_page",
      "usek_law_school"
    ],
    "tuition_academic_year": "2024-2025",
    "tuition_currency": "USD",
    "tuition_billing_basis": "PER_CREDIT",
    "tuition_amount": 309,
    "tuition_category": "School of Law and Political Sciences - Law / Political Sciences",
    "tuition_notes": "Official USEK graduate credit-fee table (2024-2025).",
    "tuition_source_ids": [
      "usek_grad_fees"
    ],
    "track_names": []
  },
  {
    "id": "usek-law-master-political-sciences",
    "faculty": "School of Law and Political Sciences",
    "department": null,
    "major_category": "Law and Political Sciences",
    "major": "Political Sciences",
    "degree_type": "MASTER",
    "official_degree_name": "Master in Political Sciences",
    "thesis_or_non_thesis": null,
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": null,
    "delivery_mode": null,
    "program_description": null,
    "official_program_url": "https://www.usek.edu.lb/law/academic-programs/master-in-business-law?t=2",
    "notes": "Listed on the School of Law and Political Sciences graduate hub page; individual program page was not canonicalized in this pass.",
    "source_ids": [
      "usek_business_law_page",
      "usek_law_school"
    ],
    "tuition_academic_year": "2024-2025",
    "tuition_currency": "USD",
    "tuition_billing_basis": "PER_CREDIT",
    "tuition_amount": 309,
    "tuition_category": "School of Law and Political Sciences - Law / Political Sciences",
    "tuition_notes": "Official USEK graduate credit-fee table (2024-2025).",
    "tuition_source_ids": [
      "usek_grad_fees"
    ],
    "track_names": []
  },
  {
    "id": "usek-law-master-political-sciences-international-relations",
    "faculty": "School of Law and Political Sciences",
    "department": null,
    "major_category": "Law and Political Sciences",
    "major": "Political Sciences - International Relations",
    "degree_type": "MASTER",
    "official_degree_name": "Master in Political Sciences - International Relations",
    "thesis_or_non_thesis": null,
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": null,
    "delivery_mode": null,
    "program_description": null,
    "official_program_url": "https://www.usek.edu.lb/law/academic-programs/master-in-business-law?t=2",
    "notes": "Listed on the School of Law and Political Sciences graduate hub page; individual program page was not canonicalized in this pass.",
    "source_ids": [
      "usek_business_law_page",
      "usek_law_school"
    ],
    "tuition_academic_year": "2024-2025",
    "tuition_currency": "USD",
    "tuition_billing_basis": "PER_CREDIT",
    "tuition_amount": 309,
    "tuition_category": "School of Law and Political Sciences - Law / Political Sciences",
    "tuition_notes": "Official USEK graduate credit-fee table (2024-2025).",
    "tuition_source_ids": [
      "usek_grad_fees"
    ],
    "track_names": [
      "International Relations"
    ]
  },
  {
    "id": "usek-law-master-private-law",
    "faculty": "School of Law and Political Sciences",
    "department": null,
    "major_category": "Law and Political Sciences",
    "major": "Private Law",
    "degree_type": "MASTER",
    "official_degree_name": "Master in Private Law",
    "thesis_or_non_thesis": null,
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": null,
    "delivery_mode": null,
    "program_description": null,
    "official_program_url": "https://www.usek.edu.lb/law/academic-programs/master-in-business-law?t=2",
    "notes": "Listed on the School of Law and Political Sciences graduate hub page; individual program page was not canonicalized in this pass.",
    "source_ids": [
      "usek_business_law_page",
      "usek_law_school"
    ],
    "tuition_academic_year": "2024-2025",
    "tuition_currency": "USD",
    "tuition_billing_basis": "PER_CREDIT",
    "tuition_amount": 309,
    "tuition_category": "School of Law and Political Sciences - Law / Political Sciences",
    "tuition_notes": "Official USEK graduate credit-fee table (2024-2025).",
    "tuition_source_ids": [
      "usek_grad_fees"
    ],
    "track_names": []
  },
  {
    "id": "usek-law-master-public-law",
    "faculty": "School of Law and Political Sciences",
    "department": null,
    "major_category": "Law and Political Sciences",
    "major": "Public Law",
    "degree_type": "MASTER",
    "official_degree_name": "Master in Public Law",
    "thesis_or_non_thesis": null,
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": null,
    "delivery_mode": null,
    "program_description": null,
    "official_program_url": "https://www.usek.edu.lb/law/academic-programs/master-in-business-law?t=2",
    "notes": "Listed on the School of Law and Political Sciences graduate hub page; individual program page was not canonicalized in this pass.",
    "source_ids": [
      "usek_business_law_page",
      "usek_law_school"
    ],
    "tuition_academic_year": "2024-2025",
    "tuition_currency": "USD",
    "tuition_billing_basis": "PER_CREDIT",
    "tuition_amount": 309,
    "tuition_category": "School of Law and Political Sciences - Law / Political Sciences",
    "tuition_notes": "Official USEK graduate credit-fee table (2024-2025).",
    "tuition_source_ids": [
      "usek_grad_fees"
    ],
    "track_names": []
  },
  {
    "id": "usek-law-master-middle-eastern-studies",
    "faculty": "School of Law and Political Sciences",
    "department": null,
    "major_category": "Law and Political Sciences",
    "major": "Middle Eastern Studies",
    "degree_type": "MASTER",
    "official_degree_name": "Master of Arts in Middle Eastern Studies",
    "thesis_or_non_thesis": null,
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": null,
    "delivery_mode": null,
    "program_description": null,
    "official_program_url": "https://www.usek.edu.lb/law/academic-programs/master-in-business-law?t=2",
    "notes": "Listed on the School of Law and Political Sciences graduate hub page; individual program page was not canonicalized in this pass.",
    "source_ids": [
      "usek_business_law_page",
      "usek_law_school"
    ],
    "tuition_academic_year": "2024-2025",
    "tuition_currency": "USD",
    "tuition_billing_basis": "PER_CREDIT",
    "tuition_amount": 309,
    "tuition_category": "School of Law and Political Sciences - Law / Political Sciences",
    "tuition_notes": "Official USEK graduate credit-fee table (2024-2025).",
    "tuition_source_ids": [
      "usek_grad_fees"
    ],
    "track_names": []
  },
  {
    "id": "usek-law-master-international-comparative-law",
    "faculty": "School of Law and Political Sciences",
    "department": null,
    "major_category": "Law and Political Sciences",
    "major": "International and Comparative Law",
    "degree_type": "MASTER",
    "official_degree_name": "Master of Laws in International and Comparative Law",
    "thesis_or_non_thesis": null,
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": null,
    "delivery_mode": null,
    "program_description": null,
    "official_program_url": "https://www.usek.edu.lb/law/academic-programs/master-in-business-law?t=2",
    "notes": "Listed on the School of Law and Political Sciences graduate hub page; individual program page was not canonicalized in this pass.",
    "source_ids": [
      "usek_business_law_page",
      "usek_law_school"
    ],
    "tuition_academic_year": "2024-2025",
    "tuition_currency": "USD",
    "tuition_billing_basis": "PER_CREDIT",
    "tuition_amount": 309,
    "tuition_category": "School of Law and Political Sciences - Law / Political Sciences",
    "tuition_notes": "Official USEK graduate credit-fee table (2024-2025).",
    "tuition_source_ids": [
      "usek_grad_fees"
    ],
    "track_names": []
  },
  {
    "id": "usek-law-phd-law",
    "faculty": "School of Law and Political Sciences",
    "department": "Law",
    "major_category": "Law and Political Sciences",
    "major": "Law",
    "degree_type": "PHD",
    "official_degree_name": "Ph.D. in Law",
    "thesis_or_non_thesis": null,
    "credits": 60,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": "Multilingual",
    "delivery_mode": null,
    "program_description": "Doctoral program training independent researchers in law.",
    "official_program_url": "https://www.usek.edu.lb/en/law/phd-in-law-2?t=5",
    "notes": "Official page lists multilingual instruction and 60 credits.",
    "source_ids": [
      "usek_law_phd_page",
      "usek_law_school",
      "usek_doctoral_college"
    ],
    "tuition_academic_year": null,
    "tuition_currency": null,
    "tuition_billing_basis": null,
    "tuition_amount": null,
    "tuition_category": null,
    "tuition_notes": null,
    "tuition_source_ids": [],
    "track_names": []
  },
  {
    "id": "usek-theology-master-arts-theology",
    "faculty": "Pontifical School of Theology",
    "department": null,
    "major_category": "Theology",
    "major": "Theology",
    "degree_type": "MASTER",
    "official_degree_name": "Master of Arts in Theology",
    "thesis_or_non_thesis": null,
    "credits": 60,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": "Multilingual",
    "delivery_mode": null,
    "program_description": null,
    "official_program_url": "https://www.usek.edu.lb/en/pontifical-school-of-theology/master-of-arts-in-theology?t=2",
    "notes": "Official page lists multilingual instruction and 60 credits.",
    "source_ids": [
      "usek_theology_ma_page",
      "usek_theology_school"
    ],
    "tuition_academic_year": "2024-2025",
    "tuition_currency": "USD",
    "tuition_billing_basis": "PER_CREDIT",
    "tuition_amount": 309,
    "tuition_category": "Pontifical School of Theology - Theology",
    "tuition_notes": "Official USEK graduate credit-fee table (2024-2025).",
    "tuition_source_ids": [
      "usek_grad_fees"
    ],
    "track_names": []
  },
  {
    "id": "usek-theology-phd-theology",
    "faculty": "Pontifical School of Theology",
    "department": null,
    "major_category": "Theology",
    "major": "Theology",
    "degree_type": "PHD",
    "official_degree_name": "Ph.D. in Theology",
    "thesis_or_non_thesis": null,
    "credits": 60,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": "Multilingual",
    "delivery_mode": null,
    "program_description": null,
    "official_program_url": "https://www.usek.edu.lb/en/pontifical-school-of-theology/phd-in-theology?t=5",
    "notes": "Official page lists multilingual instruction and 60 credits.",
    "source_ids": [
      "usek_theology_phd_page",
      "usek_theology_school",
      "usek_doctoral_college"
    ],
    "tuition_academic_year": null,
    "tuition_currency": null,
    "tuition_billing_basis": null,
    "tuition_amount": null,
    "tuition_category": null,
    "tuition_notes": null,
    "tuition_source_ids": [],
    "track_names": []
  },
  {
    "id": "usek-engineering-master-biomedical-engineering",
    "faculty": "School of Engineering",
    "department": "Department of Biomedical Engineering",
    "major_category": "Engineering",
    "major": "Biomedical Engineering",
    "degree_type": "MASTER",
    "official_degree_name": "Master of Science in Biomedical Engineering",
    "thesis_or_non_thesis": null,
    "credits": 30,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": "English",
    "delivery_mode": null,
    "program_description": "Graduate program preparing students for advanced study and research in biomedical engineering.",
    "official_program_url": "https://www.usek.edu.lb/en/fi-academic-programs/department-of-biomedical-engineering-2/master-of-science-in-biomedical-engineering?t=2",
    "notes": "Listed on the School of Engineering graduate hub page that surfaces all 10 engineering master titles.",
    "source_ids": [
      "usek_biomedical_engineering_page",
      "usek_engineering_school"
    ],
    "tuition_academic_year": "2024-2025",
    "tuition_currency": "USD",
    "tuition_billing_basis": "PER_CREDIT",
    "tuition_amount": 480,
    "tuition_category": "School of Engineering - Biomedical/Chemical/Civil/Electrical/Mechanical/Petroleum Engineering",
    "tuition_notes": "Official USEK graduate credit-fee table (2024-2025).",
    "tuition_source_ids": [
      "usek_grad_fees"
    ],
    "track_names": []
  },
  {
    "id": "usek-engineering-master-chemical-engineering",
    "faculty": "School of Engineering",
    "department": null,
    "major_category": "Engineering",
    "major": "Chemical Engineering",
    "degree_type": "MASTER",
    "official_degree_name": "Master of Science in Chemical Engineering",
    "thesis_or_non_thesis": null,
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": null,
    "delivery_mode": null,
    "program_description": null,
    "official_program_url": "https://www.usek.edu.lb/en/fi-academic-programs/department-of-biomedical-engineering-2/master-of-science-in-biomedical-engineering?t=2",
    "notes": "Listed on the School of Engineering graduate hub page that surfaces all 10 engineering master titles.",
    "source_ids": [
      "usek_biomedical_engineering_page",
      "usek_engineering_school"
    ],
    "tuition_academic_year": "2024-2025",
    "tuition_currency": "USD",
    "tuition_billing_basis": "PER_CREDIT",
    "tuition_amount": 480,
    "tuition_category": "School of Engineering - Biomedical/Chemical/Civil/Electrical/Mechanical/Petroleum Engineering",
    "tuition_notes": "Official USEK graduate credit-fee table (2024-2025).",
    "tuition_source_ids": [
      "usek_grad_fees"
    ],
    "track_names": []
  },
  {
    "id": "usek-engineering-master-civil-engineering",
    "faculty": "School of Engineering",
    "department": null,
    "major_category": "Engineering",
    "major": "Civil Engineering",
    "degree_type": "MASTER",
    "official_degree_name": "Master of Science in Civil Engineering",
    "thesis_or_non_thesis": null,
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": null,
    "delivery_mode": null,
    "program_description": null,
    "official_program_url": "https://www.usek.edu.lb/en/fi-academic-programs/department-of-biomedical-engineering-2/master-of-science-in-biomedical-engineering?t=2",
    "notes": "Listed on the School of Engineering graduate hub page that surfaces all 10 engineering master titles.",
    "source_ids": [
      "usek_biomedical_engineering_page",
      "usek_engineering_school"
    ],
    "tuition_academic_year": "2024-2025",
    "tuition_currency": "USD",
    "tuition_billing_basis": "PER_CREDIT",
    "tuition_amount": 480,
    "tuition_category": "School of Engineering - Biomedical/Chemical/Civil/Electrical/Mechanical/Petroleum Engineering",
    "tuition_notes": "Official USEK graduate credit-fee table (2024-2025).",
    "tuition_source_ids": [
      "usek_grad_fees"
    ],
    "track_names": []
  },
  {
    "id": "usek-engineering-master-communication-engineering",
    "faculty": "School of Engineering",
    "department": null,
    "major_category": "Engineering",
    "major": "Communication Engineering",
    "degree_type": "MASTER",
    "official_degree_name": "Master of Science in Communication Engineering",
    "thesis_or_non_thesis": null,
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": null,
    "delivery_mode": null,
    "program_description": null,
    "official_program_url": "https://www.usek.edu.lb/en/fi-academic-programs/department-of-biomedical-engineering-2/master-of-science-in-biomedical-engineering?t=2",
    "notes": "Listed on the School of Engineering graduate hub page that surfaces all 10 engineering master titles.",
    "source_ids": [
      "usek_biomedical_engineering_page",
      "usek_engineering_school"
    ],
    "tuition_academic_year": "2024-2025",
    "tuition_currency": "USD",
    "tuition_billing_basis": "PER_CREDIT",
    "tuition_amount": 480,
    "tuition_category": "School of Engineering - Biomedical/Chemical/Civil/Electrical/Mechanical/Petroleum Engineering",
    "tuition_notes": "Official USEK graduate credit-fee table (2024-2025).",
    "tuition_source_ids": [
      "usek_grad_fees"
    ],
    "track_names": []
  },
  {
    "id": "usek-engineering-master-computer-engineering",
    "faculty": "School of Engineering",
    "department": null,
    "major_category": "Engineering",
    "major": "Computer Engineering",
    "degree_type": "MASTER",
    "official_degree_name": "Master of Science in Computer Engineering",
    "thesis_or_non_thesis": null,
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": null,
    "delivery_mode": null,
    "program_description": null,
    "official_program_url": "https://www.usek.edu.lb/en/fi-academic-programs/department-of-biomedical-engineering-2/master-of-science-in-biomedical-engineering?t=2",
    "notes": "Listed on the School of Engineering graduate hub page that surfaces all 10 engineering master titles.",
    "source_ids": [
      "usek_biomedical_engineering_page",
      "usek_engineering_school"
    ],
    "tuition_academic_year": "2024-2025",
    "tuition_currency": "USD",
    "tuition_billing_basis": "PER_CREDIT",
    "tuition_amount": 480,
    "tuition_category": "School of Engineering - Biomedical/Chemical/Civil/Electrical/Mechanical/Petroleum Engineering",
    "tuition_notes": "Official USEK graduate credit-fee table (2024-2025).",
    "tuition_source_ids": [
      "usek_grad_fees"
    ],
    "track_names": []
  },
  {
    "id": "usek-engineering-master-electrical-electronics-engineering",
    "faculty": "School of Engineering",
    "department": null,
    "major_category": "Engineering",
    "major": "Electrical and Electronics Engineering",
    "degree_type": "MASTER",
    "official_degree_name": "Master of Science in Electrical and Electronics Engineering",
    "thesis_or_non_thesis": null,
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": null,
    "delivery_mode": null,
    "program_description": null,
    "official_program_url": "https://www.usek.edu.lb/en/fi-academic-programs/department-of-biomedical-engineering-2/master-of-science-in-biomedical-engineering?t=2",
    "notes": "Listed on the School of Engineering graduate hub page that surfaces all 10 engineering master titles.",
    "source_ids": [
      "usek_biomedical_engineering_page",
      "usek_engineering_school"
    ],
    "tuition_academic_year": "2024-2025",
    "tuition_currency": "USD",
    "tuition_billing_basis": "PER_CREDIT",
    "tuition_amount": 480,
    "tuition_category": "School of Engineering - Biomedical/Chemical/Civil/Electrical/Mechanical/Petroleum Engineering",
    "tuition_notes": "Official USEK graduate credit-fee table (2024-2025).",
    "tuition_source_ids": [
      "usek_grad_fees"
    ],
    "track_names": []
  },
  {
    "id": "usek-engineering-master-enology",
    "faculty": "School of Engineering",
    "department": null,
    "major_category": "Engineering",
    "major": "Enology",
    "degree_type": "MASTER",
    "official_degree_name": "Master of Science in Enology",
    "thesis_or_non_thesis": null,
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": null,
    "delivery_mode": null,
    "program_description": null,
    "official_program_url": "https://www.usek.edu.lb/en/fi-academic-programs/department-of-biomedical-engineering-2/master-of-science-in-biomedical-engineering?t=2",
    "notes": "Listed on the School of Engineering graduate hub page that surfaces all 10 engineering master titles.",
    "source_ids": [
      "usek_biomedical_engineering_page",
      "usek_engineering_school"
    ],
    "tuition_academic_year": "2024-2025",
    "tuition_currency": "USD",
    "tuition_billing_basis": "PER_CREDIT",
    "tuition_amount": 387,
    "tuition_category": "School of Engineering - Agricultural & Food Engineering",
    "tuition_notes": "Official USEK graduate credit-fee table (2024-2025).",
    "tuition_source_ids": [
      "usek_grad_fees"
    ],
    "track_names": []
  },
  {
    "id": "usek-engineering-master-food-engineering",
    "faculty": "School of Engineering",
    "department": null,
    "major_category": "Engineering",
    "major": "Food Engineering",
    "degree_type": "MASTER",
    "official_degree_name": "Master of Science in Food Engineering",
    "thesis_or_non_thesis": null,
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": null,
    "delivery_mode": null,
    "program_description": null,
    "official_program_url": "https://www.usek.edu.lb/en/fi-academic-programs/department-of-biomedical-engineering-2/master-of-science-in-biomedical-engineering?t=2",
    "notes": "Listed on the School of Engineering graduate hub page that surfaces all 10 engineering master titles.",
    "source_ids": [
      "usek_biomedical_engineering_page",
      "usek_engineering_school"
    ],
    "tuition_academic_year": "2024-2025",
    "tuition_currency": "USD",
    "tuition_billing_basis": "PER_CREDIT",
    "tuition_amount": 387,
    "tuition_category": "School of Engineering - Agricultural & Food Engineering",
    "tuition_notes": "Official USEK graduate credit-fee table (2024-2025).",
    "tuition_source_ids": [
      "usek_grad_fees"
    ],
    "track_names": []
  },
  {
    "id": "usek-engineering-master-mechanical-engineering",
    "faculty": "School of Engineering",
    "department": null,
    "major_category": "Engineering",
    "major": "Mechanical Engineering",
    "degree_type": "MASTER",
    "official_degree_name": "Master of Science in Mechanical Engineering",
    "thesis_or_non_thesis": null,
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": null,
    "delivery_mode": null,
    "program_description": null,
    "official_program_url": "https://www.usek.edu.lb/en/fi-academic-programs/department-of-biomedical-engineering-2/master-of-science-in-biomedical-engineering?t=2",
    "notes": "Listed on the School of Engineering graduate hub page that surfaces all 10 engineering master titles.",
    "source_ids": [
      "usek_biomedical_engineering_page",
      "usek_engineering_school"
    ],
    "tuition_academic_year": "2024-2025",
    "tuition_currency": "USD",
    "tuition_billing_basis": "PER_CREDIT",
    "tuition_amount": 480,
    "tuition_category": "School of Engineering - Biomedical/Chemical/Civil/Electrical/Mechanical/Petroleum Engineering",
    "tuition_notes": "Official USEK graduate credit-fee table (2024-2025).",
    "tuition_source_ids": [
      "usek_grad_fees"
    ],
    "track_names": []
  },
  {
    "id": "usek-engineering-master-petroleum-engineering",
    "faculty": "School of Engineering",
    "department": null,
    "major_category": "Engineering",
    "major": "Petroleum Engineering",
    "degree_type": "MASTER",
    "official_degree_name": "Master of Science in Petroleum Engineering",
    "thesis_or_non_thesis": null,
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": null,
    "delivery_mode": null,
    "program_description": null,
    "official_program_url": "https://www.usek.edu.lb/en/fi-academic-programs/department-of-biomedical-engineering-2/master-of-science-in-biomedical-engineering?t=2",
    "notes": "Listed on the School of Engineering graduate hub page that surfaces all 10 engineering master titles.",
    "source_ids": [
      "usek_biomedical_engineering_page",
      "usek_engineering_school"
    ],
    "tuition_academic_year": "2024-2025",
    "tuition_currency": "USD",
    "tuition_billing_basis": "PER_CREDIT",
    "tuition_amount": 480,
    "tuition_category": "School of Engineering - Biomedical/Chemical/Civil/Electrical/Mechanical/Petroleum Engineering",
    "tuition_notes": "Official USEK graduate credit-fee table (2024-2025).",
    "tuition_source_ids": [
      "usek_grad_fees"
    ],
    "track_names": []
  },
  {
    "id": "usek-architecture-master-interior-architecture",
    "faculty": "School of Architecture and Design",
    "department": "Department of Design and Interior Architecture",
    "major_category": "Architecture and Design",
    "major": "Interior Architecture",
    "degree_type": "MASTER",
    "official_degree_name": "Master in Interior Architecture",
    "thesis_or_non_thesis": "THESIS_OR_PROJECT",
    "credits": 42,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": "Multilingual",
    "delivery_mode": null,
    "program_description": "Program preparing students to enter the field as skilled designers, creative thinkers, professional leaders, and responsible citizens.",
    "official_program_url": "https://www.usek.edu.lb/en/department-of-interior-design/master-in-interior-design-2?t=2",
    "notes": "Official page lists multilingual instruction, 42 credits, and final thesis/project components.",
    "source_ids": [
      "usek_architecture_interior_page",
      "usek_architecture_school"
    ],
    "tuition_academic_year": "2024-2025",
    "tuition_currency": "USD",
    "tuition_billing_basis": "PER_CREDIT",
    "tuition_amount": 387,
    "tuition_category": "School of Architecture & Design - Interior Design",
    "tuition_notes": "Official USEK graduate credit-fee table (2024-2025).",
    "tuition_source_ids": [
      "usek_grad_fees"
    ],
    "track_names": []
  },
  {
    "id": "usek-architecture-master-communication-visual-arts",
    "faculty": "School of Architecture and Design",
    "department": "Department of Visual Communication",
    "major_category": "Architecture and Design",
    "major": "Communication and Visual Arts",
    "degree_type": "MASTER",
    "official_degree_name": "Master of Arts in Communication and Visual Arts",
    "thesis_or_non_thesis": "THESIS_OR_PROJECT",
    "credits": 42,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": "English",
    "delivery_mode": null,
    "program_description": "Multidisciplinary training in graphic design and advertising with a strong digital, social, and visual communication component.",
    "official_program_url": "https://www.usek.edu.lb/fba/department-of-communication-and-visual-arts/master-of-arts-in-communication-and-visual-arts?t=2",
    "notes": "Official page lists English instruction, 42 credits, and thesis/project requirements.",
    "source_ids": [
      "usek_architecture_comm_visual_page",
      "usek_architecture_school"
    ],
    "tuition_academic_year": "2024-2025",
    "tuition_currency": "USD",
    "tuition_billing_basis": "PER_CREDIT",
    "tuition_amount": 387,
    "tuition_category": "School of Architecture & Design - Architecture / Design &Digital Media",
    "tuition_notes": "Official USEK graduate credit-fee table (2024-2025).",
    "tuition_source_ids": [
      "usek_grad_fees"
    ],
    "track_names": []
  },
  {
    "id": "usek-architecture-master-contemporary-art",
    "faculty": "School of Architecture and Design",
    "department": null,
    "major_category": "Architecture and Design",
    "major": "Contemporary Art",
    "degree_type": "MASTER",
    "official_degree_name": "Master of Arts in Contemporary Art",
    "thesis_or_non_thesis": "THESIS",
    "credits": 36,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": "Multilingual",
    "delivery_mode": null,
    "program_description": "Interdisciplinary master that links art to communication, philosophy, literature, anthropology, history, and semiotics.",
    "official_program_url": "https://www.usek.edu.lb/fbaaa-academic-programs/master-contemporary-art?t=2",
    "notes": "Official page lists multilingual instruction, 36 credits, and a research-thesis orientation.",
    "source_ids": [
      "usek_architecture_contemporary_art_page",
      "usek_architecture_school"
    ],
    "tuition_academic_year": "2024-2025",
    "tuition_currency": "USD",
    "tuition_billing_basis": "PER_CREDIT",
    "tuition_amount": 387,
    "tuition_category": "School of Architecture & Design - Architecture / Design &Digital Media",
    "tuition_notes": "Official USEK graduate credit-fee table (2024-2025).",
    "tuition_source_ids": [
      "usek_grad_fees"
    ],
    "track_names": []
  },
  {
    "id": "usek-architecture-master-digital-media",
    "faculty": "School of Architecture and Design",
    "department": "Department of Digital Media",
    "major_category": "Architecture and Design",
    "major": "Digital Media",
    "degree_type": "MASTER",
    "official_degree_name": "Master of Arts in Digital Media",
    "thesis_or_non_thesis": "PROJECT",
    "credits": 42,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": "English",
    "delivery_mode": null,
    "program_description": "Graduate program preparing students to contribute to film and animation through original research and production projects.",
    "official_program_url": "https://www.usek.edu.lb/fbaaa/department-of-audio-visual/master-of-arts-in-digital-media?t=2",
    "notes": "Official page lists English instruction, 42 credits, and a final project structure.",
    "source_ids": [
      "usek_architecture_digital_media_page",
      "usek_architecture_school"
    ],
    "tuition_academic_year": "2024-2025",
    "tuition_currency": "USD",
    "tuition_billing_basis": "PER_CREDIT",
    "tuition_amount": 387,
    "tuition_category": "School of Architecture & Design - Design &Digital Media",
    "tuition_notes": "Official USEK graduate credit-fee table (2024-2025).",
    "tuition_source_ids": [
      "usek_grad_fees"
    ],
    "track_names": []
  },
  {
    "id": "usek-medicine-doctorate-medicine",
    "faculty": "School of Medicine and Medical Sciences",
    "department": "Department of Medical Sciences",
    "major_category": "Medicine and Medical Sciences",
    "major": "Medicine",
    "degree_type": "PHD",
    "official_degree_name": "Doctorate of Medicine",
    "thesis_or_non_thesis": null,
    "credits": 120,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": "Multilingual",
    "delivery_mode": null,
    "program_description": "Professional doctorate training general practitioners and preparing graduates for medical specializations.",
    "official_program_url": "https://www.usek.edu.lb/en/department-of-medical-sciences/doctorate-of-medicine-2?t=5",
    "notes": "Official page lists multilingual instruction and 120 credits.",
    "source_ids": [
      "usek_medicine_doctorate_page",
      "usek_medicine_school",
      "usek_doctoral_college"
    ],
    "tuition_academic_year": null,
    "tuition_currency": null,
    "tuition_billing_basis": null,
    "tuition_amount": null,
    "tuition_category": null,
    "tuition_notes": null,
    "tuition_source_ids": [],
    "track_names": []
  },
  {
    "id": "usek-music-master-music",
    "faculty": "School of Music and Performing Arts",
    "department": "Department of Music",
    "major_category": "Music and Performing Arts",
    "major": "Music",
    "degree_type": "MASTER",
    "official_degree_name": "Master of Arts in Music",
    "thesis_or_non_thesis": null,
    "credits": 36,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": "Multilingual",
    "delivery_mode": null,
    "program_description": "Graduate music program with a Musicology emphasis.",
    "official_program_url": "https://www.usek.edu.lb/fmus-academic-programs/department-of-music/master-of-arts-in-music?t=2",
    "notes": "Official page lists multilingual instruction, 36 credits, and a Musicology emphasis. The school page also lists Master of Arts in Performing Arts.",
    "source_ids": [
      "usek_music_master_page",
      "usek_music_school"
    ],
    "tuition_academic_year": "2024-2025",
    "tuition_currency": "USD",
    "tuition_billing_basis": "PER_CREDIT",
    "tuition_amount": 309,
    "tuition_category": "Faculty of Arts & Sciences - Music and Performing Arts",
    "tuition_notes": "Official USEK graduate credit-fee table (2024-2025).",
    "tuition_source_ids": [
      "usek_grad_fees"
    ],
    "track_names": [
      "Musicology"
    ]
  },
  {
    "id": "usek-music-master-performing-arts",
    "faculty": "School of Music and Performing Arts",
    "department": "Department of Performing Arts",
    "major_category": "Music and Performing Arts",
    "major": "Performing Arts",
    "degree_type": "MASTER",
    "official_degree_name": "Master of Arts in Performing Arts",
    "thesis_or_non_thesis": "THESIS_OR_PROJECT",
    "credits": 36,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": "Multilingual",
    "delivery_mode": null,
    "program_description": "Graduate program in performing arts with theory, production, and embodied research components.",
    "official_program_url": "https://www.usek.edu.lb/fmus/department-of-performing-arts/master-in-performing-arts?t=2",
    "notes": "Official page lists multilingual instruction, 36 credits, and thesis/project language in the mission statement.",
    "source_ids": [
      "usek_music_performing_arts_page",
      "usek_music_school"
    ],
    "tuition_academic_year": "2024-2025",
    "tuition_currency": "USD",
    "tuition_billing_basis": "PER_CREDIT",
    "tuition_amount": 309,
    "tuition_category": "Faculty of Arts & Sciences - Music and Performing Arts",
    "tuition_notes": "Official USEK graduate credit-fee table (2024-2025).",
    "tuition_source_ids": [
      "usek_grad_fees"
    ],
    "track_names": []
  },
  {
    "id": "usek-music-phd-music",
    "faculty": "School of Music and Performing Arts",
    "department": "Department of Higher and Specialized Musical Education",
    "major_category": "Music and Performing Arts",
    "major": "Music",
    "degree_type": "PHD",
    "official_degree_name": "Ph.D. in Music",
    "thesis_or_non_thesis": null,
    "credits": 60,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": "Multilingual",
    "delivery_mode": null,
    "program_description": "Doctoral program in music research and scholarship.",
    "official_program_url": "https://www.usek.edu.lb/fmus/academic-program/phd?t=5",
    "notes": "Shared doctoral page also lists the Ph.D. in Higher and Specialized Music Education.",
    "source_ids": [
      "usek_music_phd_page",
      "usek_music_school",
      "usek_doctoral_college"
    ],
    "tuition_academic_year": null,
    "tuition_currency": null,
    "tuition_billing_basis": null,
    "tuition_amount": null,
    "tuition_category": null,
    "tuition_notes": null,
    "tuition_source_ids": [],
    "track_names": []
  },
  {
    "id": "usek-music-phd-higher-specialized-music-education",
    "faculty": "School of Music and Performing Arts",
    "department": "Department of Higher and Specialized Musical Education",
    "major_category": "Music and Performing Arts",
    "major": "Higher and Specialized Music Education",
    "degree_type": "PHD",
    "official_degree_name": "Ph.D. in Higher and Specialized Music Education",
    "thesis_or_non_thesis": null,
    "credits": 60,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": "Multilingual",
    "delivery_mode": null,
    "program_description": "Doctoral program in higher and specialized music education.",
    "official_program_url": "https://www.usek.edu.lb/fmus/academic-program/phd?t=5",
    "notes": "Shared doctoral page also lists the Ph.D. in Music.",
    "source_ids": [
      "usek_music_phd_page",
      "usek_music_school",
      "usek_doctoral_college"
    ],
    "tuition_academic_year": null,
    "tuition_currency": null,
    "tuition_billing_basis": null,
    "tuition_amount": null,
    "tuition_category": null,
    "tuition_notes": null,
    "tuition_source_ids": [],
    "track_names": []
  },
  {
    "id": "usek-fas-master-arabic-language-literature",
    "faculty": "Faculty of Arts and Sciences",
    "department": null,
    "major_category": "Arts and Sciences",
    "major": "Arabic Language and Literature",
    "degree_type": "MASTER",
    "official_degree_name": "MA in Arabic Language and Literature",
    "thesis_or_non_thesis": null,
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": null,
    "delivery_mode": null,
    "program_description": null,
    "official_program_url": "https://www.usek.edu.lb/academics/faculty-of-arts-and-sciences/department-of-languages-and-literatures/diploma-in-interpretation?t=2",
    "notes": "Listed on the Faculty of Arts and Sciences graduate hub page; individual program page was not canonicalized in this pass.",
    "source_ids": [
      "usek_interpretation_diploma",
      "usek_arts_sciences_school"
    ],
    "tuition_academic_year": "2024-2025",
    "tuition_currency": "USD",
    "tuition_billing_basis": "PER_CREDIT",
    "tuition_amount": 309,
    "tuition_category": "Faculty of Arts & Sciences - humanities / social sciences / education",
    "tuition_notes": "Official USEK graduate credit-fee table (2024-2025).",
    "tuition_source_ids": [
      "usek_grad_fees"
    ],
    "track_names": []
  },
  {
    "id": "usek-fas-master-cinema-television",
    "faculty": "Faculty of Arts and Sciences",
    "department": null,
    "major_category": "Arts and Sciences",
    "major": "Cinema and Television",
    "degree_type": "MASTER",
    "official_degree_name": "MA in Cinema and Television",
    "thesis_or_non_thesis": null,
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": null,
    "delivery_mode": null,
    "program_description": null,
    "official_program_url": "https://www.usek.edu.lb/academics/faculty-of-arts-and-sciences/department-of-languages-and-literatures/diploma-in-interpretation?t=2",
    "notes": "Listed on the Faculty of Arts and Sciences graduate hub page; individual program page was not canonicalized in this pass.",
    "source_ids": [
      "usek_interpretation_diploma",
      "usek_arts_sciences_school"
    ],
    "tuition_academic_year": "2024-2025",
    "tuition_currency": "USD",
    "tuition_billing_basis": "PER_CREDIT",
    "tuition_amount": 387,
    "tuition_category": "Faculty of Arts & Sciences - Communication - Cinema and Television",
    "tuition_notes": "Official USEK graduate credit-fee table (2024-2025).",
    "tuition_source_ids": [
      "usek_grad_fees"
    ],
    "track_names": []
  },
  {
    "id": "usek-fas-master-conservation-restoration-cultural-property-sacred-art",
    "faculty": "Faculty of Arts and Sciences",
    "department": null,
    "major_category": "Arts and Sciences",
    "major": "Conservation, Restoration of Cultural Property & Sacred Art",
    "degree_type": "MASTER",
    "official_degree_name": "MA in Conservation, Restoration of Cultural Property & Sacred Art",
    "thesis_or_non_thesis": null,
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": null,
    "delivery_mode": null,
    "program_description": null,
    "official_program_url": "https://www.usek.edu.lb/academics/faculty-of-arts-and-sciences/department-of-languages-and-literatures/diploma-in-interpretation?t=2",
    "notes": "Listed on the Faculty of Arts and Sciences graduate hub page; individual program page was not canonicalized in this pass.",
    "source_ids": [
      "usek_interpretation_diploma",
      "usek_arts_sciences_school"
    ],
    "tuition_academic_year": "2024-2025",
    "tuition_currency": "USD",
    "tuition_billing_basis": "PER_CREDIT",
    "tuition_amount": 309,
    "tuition_category": "Faculty of Arts & Sciences - humanities / social sciences / education",
    "tuition_notes": "Official USEK graduate credit-fee table (2024-2025).",
    "tuition_source_ids": [
      "usek_grad_fees"
    ],
    "track_names": []
  },
  {
    "id": "usek-fas-master-education",
    "faculty": "Faculty of Arts and Sciences",
    "department": null,
    "major_category": "Arts and Sciences",
    "major": "Education",
    "degree_type": "MASTER",
    "official_degree_name": "MA in Education",
    "thesis_or_non_thesis": "THESIS_OR_PROJECT",
    "credits": 30,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": null,
    "delivery_mode": null,
    "program_description": null,
    "official_program_url": "https://www.usek.edu.lb/academics/faculty-of-arts-and-sciences/department-of-languages-and-literatures/diploma-in-interpretation?t=2",
    "notes": "Listed on the Faculty of Arts and Sciences graduate hub page; individual program page was not canonicalized in this pass.",
    "source_ids": [
      "usek_interpretation_diploma",
      "usek_arts_sciences_school"
    ],
    "tuition_academic_year": "2024-2025",
    "tuition_currency": "USD",
    "tuition_billing_basis": "PER_CREDIT",
    "tuition_amount": 309,
    "tuition_category": "Faculty of Arts & Sciences - humanities / social sciences / education",
    "tuition_notes": "Official USEK graduate credit-fee table (2024-2025).",
    "tuition_source_ids": [
      "usek_grad_fees"
    ],
    "track_names": [
      "Teaching English to Speakers of Other Languages (TESOL)",
      "Science, Technology, Engineering, and Mathematics (STEM) Education",
      "Educational Leadership and Policy Studies"
    ]
  },
  {
    "id": "usek-fas-master-education-administration",
    "faculty": "Faculty of Arts and Sciences",
    "department": null,
    "major_category": "Arts and Sciences",
    "major": "Education - Administration of Education",
    "degree_type": "MASTER",
    "official_degree_name": "MA in Education - Administration of Education",
    "thesis_or_non_thesis": null,
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": null,
    "delivery_mode": null,
    "program_description": null,
    "official_program_url": "https://www.usek.edu.lb/academics/faculty-of-arts-and-sciences/department-of-languages-and-literatures/diploma-in-interpretation?t=2",
    "notes": "Listed on the Faculty of Arts and Sciences graduate hub page; individual program page was not canonicalized in this pass.",
    "source_ids": [
      "usek_interpretation_diploma",
      "usek_arts_sciences_school"
    ],
    "tuition_academic_year": "2024-2025",
    "tuition_currency": "USD",
    "tuition_billing_basis": "PER_CREDIT",
    "tuition_amount": 309,
    "tuition_category": "Faculty of Arts & Sciences - humanities / social sciences / education",
    "tuition_notes": "Official USEK graduate credit-fee table (2024-2025).",
    "tuition_source_ids": [
      "usek_grad_fees"
    ],
    "track_names": []
  },
  {
    "id": "usek-fas-master-education-technology",
    "faculty": "Faculty of Arts and Sciences",
    "department": null,
    "major_category": "Arts and Sciences",
    "major": "Education - Technology of Education",
    "degree_type": "MASTER",
    "official_degree_name": "MA in Education - Technology of Education",
    "thesis_or_non_thesis": null,
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": null,
    "delivery_mode": null,
    "program_description": null,
    "official_program_url": "https://www.usek.edu.lb/academics/faculty-of-arts-and-sciences/department-of-languages-and-literatures/diploma-in-interpretation?t=2",
    "notes": "Listed on the Faculty of Arts and Sciences graduate hub page; individual program page was not canonicalized in this pass.",
    "source_ids": [
      "usek_interpretation_diploma",
      "usek_arts_sciences_school"
    ],
    "tuition_academic_year": "2024-2025",
    "tuition_currency": "USD",
    "tuition_billing_basis": "PER_CREDIT",
    "tuition_amount": 309,
    "tuition_category": "Faculty of Arts & Sciences - humanities / social sciences / education",
    "tuition_notes": "Official USEK graduate credit-fee table (2024-2025).",
    "tuition_source_ids": [
      "usek_grad_fees"
    ],
    "track_names": []
  },
  {
    "id": "usek-fas-master-e-journalism-e-communication",
    "faculty": "Faculty of Arts and Sciences",
    "department": null,
    "major_category": "Arts and Sciences",
    "major": "E-Journalism and E-Communication",
    "degree_type": "MASTER",
    "official_degree_name": "MA in E-Journalism and E-Communication",
    "thesis_or_non_thesis": null,
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": null,
    "delivery_mode": null,
    "program_description": null,
    "official_program_url": "https://www.usek.edu.lb/academics/faculty-of-arts-and-sciences/department-of-languages-and-literatures/diploma-in-interpretation?t=2",
    "notes": "Listed on the Faculty of Arts and Sciences graduate hub page; individual program page was not canonicalized in this pass.",
    "source_ids": [
      "usek_interpretation_diploma",
      "usek_arts_sciences_school"
    ],
    "tuition_academic_year": "2024-2025",
    "tuition_currency": "USD",
    "tuition_billing_basis": "PER_CREDIT",
    "tuition_amount": 309,
    "tuition_category": "Faculty of Arts & Sciences - humanities / social sciences / education",
    "tuition_notes": "Official USEK graduate credit-fee table (2024-2025).",
    "tuition_source_ids": [
      "usek_grad_fees"
    ],
    "track_names": []
  },
  {
    "id": "usek-fas-master-english-language-literature",
    "faculty": "Faculty of Arts and Sciences",
    "department": null,
    "major_category": "Arts and Sciences",
    "major": "English Language and Literature",
    "degree_type": "MASTER",
    "official_degree_name": "MA in English Language and Literature",
    "thesis_or_non_thesis": null,
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": null,
    "delivery_mode": null,
    "program_description": null,
    "official_program_url": "https://www.usek.edu.lb/academics/faculty-of-arts-and-sciences/department-of-languages-and-literatures/diploma-in-interpretation?t=2",
    "notes": "Listed on the Faculty of Arts and Sciences graduate hub page; individual program page was not canonicalized in this pass.",
    "source_ids": [
      "usek_interpretation_diploma",
      "usek_arts_sciences_school"
    ],
    "tuition_academic_year": "2024-2025",
    "tuition_currency": "USD",
    "tuition_billing_basis": "PER_CREDIT",
    "tuition_amount": 309,
    "tuition_category": "Faculty of Arts & Sciences - humanities / social sciences / education",
    "tuition_notes": "Official USEK graduate credit-fee table (2024-2025).",
    "tuition_source_ids": [
      "usek_grad_fees"
    ],
    "track_names": []
  },
  {
    "id": "usek-fas-master-french-language-literature",
    "faculty": "Faculty of Arts and Sciences",
    "department": null,
    "major_category": "Arts and Sciences",
    "major": "French Language and Literature",
    "degree_type": "MASTER",
    "official_degree_name": "MA in French Language and Literature",
    "thesis_or_non_thesis": null,
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": null,
    "delivery_mode": null,
    "program_description": null,
    "official_program_url": "https://www.usek.edu.lb/academics/faculty-of-arts-and-sciences/department-of-languages-and-literatures/diploma-in-interpretation?t=2",
    "notes": "Listed on the Faculty of Arts and Sciences graduate hub page; individual program page was not canonicalized in this pass.",
    "source_ids": [
      "usek_interpretation_diploma",
      "usek_arts_sciences_school"
    ],
    "tuition_academic_year": "2024-2025",
    "tuition_currency": "USD",
    "tuition_billing_basis": "PER_CREDIT",
    "tuition_amount": 309,
    "tuition_category": "Faculty of Arts & Sciences - humanities / social sciences / education",
    "tuition_notes": "Official USEK graduate credit-fee table (2024-2025).",
    "tuition_source_ids": [
      "usek_grad_fees"
    ],
    "track_names": []
  },
  {
    "id": "usek-fas-master-history",
    "faculty": "Faculty of Arts and Sciences",
    "department": null,
    "major_category": "Arts and Sciences",
    "major": "History",
    "degree_type": "MASTER",
    "official_degree_name": "MA in History",
    "thesis_or_non_thesis": null,
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": null,
    "delivery_mode": null,
    "program_description": null,
    "official_program_url": "https://www.usek.edu.lb/academics/faculty-of-arts-and-sciences/department-of-languages-and-literatures/diploma-in-interpretation?t=2",
    "notes": "Listed on the Faculty of Arts and Sciences graduate hub page; individual program page was not canonicalized in this pass.",
    "source_ids": [
      "usek_interpretation_diploma",
      "usek_arts_sciences_school"
    ],
    "tuition_academic_year": "2024-2025",
    "tuition_currency": "USD",
    "tuition_billing_basis": "PER_CREDIT",
    "tuition_amount": 309,
    "tuition_category": "Faculty of Arts & Sciences - humanities / social sciences / education",
    "tuition_notes": "Official USEK graduate credit-fee table (2024-2025).",
    "tuition_source_ids": [
      "usek_grad_fees"
    ],
    "track_names": []
  },
  {
    "id": "usek-fas-master-information-studies",
    "faculty": "Faculty of Arts and Sciences",
    "department": null,
    "major_category": "Arts and Sciences",
    "major": "Information Studies",
    "degree_type": "MASTER",
    "official_degree_name": "MA in Information Studies",
    "thesis_or_non_thesis": null,
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": null,
    "delivery_mode": null,
    "program_description": null,
    "official_program_url": "https://www.usek.edu.lb/academics/faculty-of-arts-and-sciences/department-of-languages-and-literatures/diploma-in-interpretation?t=2",
    "notes": "Listed on the Faculty of Arts and Sciences graduate hub page; individual program page was not canonicalized in this pass.",
    "source_ids": [
      "usek_interpretation_diploma",
      "usek_arts_sciences_school"
    ],
    "tuition_academic_year": "2024-2025",
    "tuition_currency": "USD",
    "tuition_billing_basis": "PER_CREDIT",
    "tuition_amount": 309,
    "tuition_category": "Faculty of Arts & Sciences - humanities / social sciences / education",
    "tuition_notes": "Official USEK graduate credit-fee table (2024-2025).",
    "tuition_source_ids": [
      "usek_grad_fees"
    ],
    "track_names": []
  },
  {
    "id": "usek-fas-master-intervention-social-work",
    "faculty": "Faculty of Arts and Sciences",
    "department": null,
    "major_category": "Arts and Sciences",
    "major": "Intervention and Social Work",
    "degree_type": "MASTER",
    "official_degree_name": "MA in Intervention and Social Work",
    "thesis_or_non_thesis": null,
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": null,
    "delivery_mode": null,
    "program_description": null,
    "official_program_url": "https://www.usek.edu.lb/academics/faculty-of-arts-and-sciences/department-of-languages-and-literatures/diploma-in-interpretation?t=2",
    "notes": "Listed on the Faculty of Arts and Sciences graduate hub page; individual program page was not canonicalized in this pass.",
    "source_ids": [
      "usek_interpretation_diploma",
      "usek_arts_sciences_school"
    ],
    "tuition_academic_year": "2024-2025",
    "tuition_currency": "USD",
    "tuition_billing_basis": "PER_CREDIT",
    "tuition_amount": 309,
    "tuition_category": "Faculty of Arts & Sciences - humanities / social sciences / education",
    "tuition_notes": "Official USEK graduate credit-fee table (2024-2025).",
    "tuition_source_ids": [
      "usek_grad_fees"
    ],
    "track_names": []
  },
  {
    "id": "usek-fas-master-journalism-communication",
    "faculty": "Faculty of Arts and Sciences",
    "department": null,
    "major_category": "Arts and Sciences",
    "major": "Journalism and Communication",
    "degree_type": "MASTER",
    "official_degree_name": "MA in Journalism and Communication",
    "thesis_or_non_thesis": null,
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": null,
    "delivery_mode": null,
    "program_description": null,
    "official_program_url": "https://www.usek.edu.lb/academics/faculty-of-arts-and-sciences/department-of-languages-and-literatures/diploma-in-interpretation?t=2",
    "notes": "Listed on the Faculty of Arts and Sciences graduate hub page; individual program page was not canonicalized in this pass.",
    "source_ids": [
      "usek_interpretation_diploma",
      "usek_arts_sciences_school"
    ],
    "tuition_academic_year": "2024-2025",
    "tuition_currency": "USD",
    "tuition_billing_basis": "PER_CREDIT",
    "tuition_amount": 309,
    "tuition_category": "Faculty of Arts & Sciences - humanities / social sciences / education",
    "tuition_notes": "Official USEK graduate credit-fee table (2024-2025).",
    "tuition_source_ids": [
      "usek_grad_fees"
    ],
    "track_names": []
  },
  {
    "id": "usek-fas-master-philosophy",
    "faculty": "Faculty of Arts and Sciences",
    "department": null,
    "major_category": "Arts and Sciences",
    "major": "Philosophy",
    "degree_type": "MASTER",
    "official_degree_name": "MA in Philosophy",
    "thesis_or_non_thesis": null,
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": null,
    "delivery_mode": null,
    "program_description": null,
    "official_program_url": "https://www.usek.edu.lb/academics/faculty-of-arts-and-sciences/department-of-languages-and-literatures/diploma-in-interpretation?t=2",
    "notes": "Listed on the Faculty of Arts and Sciences graduate hub page; individual program page was not canonicalized in this pass.",
    "source_ids": [
      "usek_interpretation_diploma",
      "usek_arts_sciences_school"
    ],
    "tuition_academic_year": "2024-2025",
    "tuition_currency": "USD",
    "tuition_billing_basis": "PER_CREDIT",
    "tuition_amount": 309,
    "tuition_category": "Faculty of Arts & Sciences - humanities / social sciences / education",
    "tuition_notes": "Official USEK graduate credit-fee table (2024-2025).",
    "tuition_source_ids": [
      "usek_grad_fees"
    ],
    "track_names": []
  },
  {
    "id": "usek-fas-master-psychology",
    "faculty": "Faculty of Arts and Sciences",
    "department": null,
    "major_category": "Arts and Sciences",
    "major": "Psychology",
    "degree_type": "MASTER",
    "official_degree_name": "MA in Psychology",
    "thesis_or_non_thesis": null,
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": null,
    "delivery_mode": null,
    "program_description": null,
    "official_program_url": "https://www.usek.edu.lb/academics/faculty-of-arts-and-sciences/department-of-languages-and-literatures/diploma-in-interpretation?t=2",
    "notes": "Listed on the Faculty of Arts and Sciences graduate hub page; individual program page was not canonicalized in this pass.",
    "source_ids": [
      "usek_interpretation_diploma",
      "usek_arts_sciences_school"
    ],
    "tuition_academic_year": "2024-2025",
    "tuition_currency": "USD",
    "tuition_billing_basis": "PER_CREDIT",
    "tuition_amount": 309,
    "tuition_category": "Faculty of Arts & Sciences - humanities / social sciences / education",
    "tuition_notes": "Official USEK graduate credit-fee table (2024-2025).",
    "tuition_source_ids": [
      "usek_grad_fees"
    ],
    "track_names": []
  },
  {
    "id": "usek-fas-master-religious-sciences",
    "faculty": "Faculty of Arts and Sciences",
    "department": null,
    "major_category": "Arts and Sciences",
    "major": "Religious Sciences",
    "degree_type": "MASTER",
    "official_degree_name": "MA in Religious Sciences",
    "thesis_or_non_thesis": null,
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": null,
    "delivery_mode": null,
    "program_description": null,
    "official_program_url": "https://www.usek.edu.lb/academics/faculty-of-arts-and-sciences/department-of-languages-and-literatures/diploma-in-interpretation?t=2",
    "notes": "Listed on the Faculty of Arts and Sciences graduate hub page; individual program page was not canonicalized in this pass.",
    "source_ids": [
      "usek_interpretation_diploma",
      "usek_arts_sciences_school"
    ],
    "tuition_academic_year": "2024-2025",
    "tuition_currency": "USD",
    "tuition_billing_basis": "PER_CREDIT",
    "tuition_amount": 309,
    "tuition_category": "Faculty of Arts & Sciences - humanities / social sciences / education",
    "tuition_notes": "Official USEK graduate credit-fee table (2024-2025).",
    "tuition_source_ids": [
      "usek_grad_fees"
    ],
    "track_names": []
  },
  {
    "id": "usek-fas-master-social-sciences",
    "faculty": "Faculty of Arts and Sciences",
    "department": null,
    "major_category": "Arts and Sciences",
    "major": "Social Sciences",
    "degree_type": "MASTER",
    "official_degree_name": "MA in Social Sciences",
    "thesis_or_non_thesis": null,
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": null,
    "delivery_mode": null,
    "program_description": null,
    "official_program_url": "https://www.usek.edu.lb/academics/faculty-of-arts-and-sciences/department-of-languages-and-literatures/diploma-in-interpretation?t=2",
    "notes": "Listed on the Faculty of Arts and Sciences graduate hub page; individual program page was not canonicalized in this pass.",
    "source_ids": [
      "usek_interpretation_diploma",
      "usek_arts_sciences_school"
    ],
    "tuition_academic_year": "2024-2025",
    "tuition_currency": "USD",
    "tuition_billing_basis": "PER_CREDIT",
    "tuition_amount": 309,
    "tuition_category": "Faculty of Arts & Sciences - humanities / social sciences / education",
    "tuition_notes": "Official USEK graduate credit-fee table (2024-2025).",
    "tuition_source_ids": [
      "usek_grad_fees"
    ],
    "track_names": []
  },
  {
    "id": "usek-fas-master-translation",
    "faculty": "Faculty of Arts and Sciences",
    "department": null,
    "major_category": "Arts and Sciences",
    "major": "Translation",
    "degree_type": "MASTER",
    "official_degree_name": "MA in Translation",
    "thesis_or_non_thesis": null,
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": null,
    "delivery_mode": null,
    "program_description": null,
    "official_program_url": "https://www.usek.edu.lb/academics/faculty-of-arts-and-sciences/department-of-languages-and-literatures/diploma-in-interpretation?t=2",
    "notes": "Listed on the Faculty of Arts and Sciences graduate hub page; individual program page was not canonicalized in this pass.",
    "source_ids": [
      "usek_interpretation_diploma",
      "usek_arts_sciences_school"
    ],
    "tuition_academic_year": "2024-2025",
    "tuition_currency": "USD",
    "tuition_billing_basis": "PER_CREDIT",
    "tuition_amount": 309,
    "tuition_category": "Faculty of Arts & Sciences - humanities / social sciences / education",
    "tuition_notes": "Official USEK graduate credit-fee table (2024-2025).",
    "tuition_source_ids": [
      "usek_grad_fees"
    ],
    "track_names": []
  },
  {
    "id": "usek-fas-master-actuarial-financial-mathematics",
    "faculty": "Faculty of Arts and Sciences",
    "department": null,
    "major_category": "Arts and Sciences",
    "major": "Actuarial and Financial Mathematics",
    "degree_type": "MASTER",
    "official_degree_name": "Master of Science in Actuarial and Financial Mathematics",
    "thesis_or_non_thesis": null,
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": null,
    "delivery_mode": null,
    "program_description": null,
    "official_program_url": "https://www.usek.edu.lb/academics/faculty-of-arts-and-sciences/department-of-languages-and-literatures/diploma-in-interpretation?t=2",
    "notes": "Listed on the Faculty of Arts and Sciences graduate hub page; individual program page was not canonicalized in this pass.",
    "source_ids": [
      "usek_interpretation_diploma",
      "usek_arts_sciences_school"
    ],
    "tuition_academic_year": "2024-2025",
    "tuition_currency": "USD",
    "tuition_billing_basis": "PER_CREDIT",
    "tuition_amount": 450,
    "tuition_category": "Faculty of Arts & Sciences - Mathematics",
    "tuition_notes": "Official USEK graduate credit-fee table (2024-2025).",
    "tuition_source_ids": [
      "usek_grad_fees"
    ],
    "track_names": []
  },
  {
    "id": "usek-fas-master-biochemistry",
    "faculty": "Faculty of Arts and Sciences",
    "department": null,
    "major_category": "Arts and Sciences",
    "major": "Biochemistry",
    "degree_type": "MASTER",
    "official_degree_name": "Master of Science in Biochemistry",
    "thesis_or_non_thesis": null,
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": null,
    "delivery_mode": null,
    "program_description": null,
    "official_program_url": "https://www.usek.edu.lb/academics/faculty-of-arts-and-sciences/department-of-languages-and-literatures/diploma-in-interpretation?t=2",
    "notes": "Listed on the Faculty of Arts and Sciences graduate hub page; individual program page was not canonicalized in this pass.",
    "source_ids": [
      "usek_interpretation_diploma",
      "usek_arts_sciences_school"
    ],
    "tuition_academic_year": "2024-2025",
    "tuition_currency": "USD",
    "tuition_billing_basis": "PER_CREDIT",
    "tuition_amount": 450,
    "tuition_category": "Faculty of Arts & Sciences - Chemistry and Biochemistry",
    "tuition_notes": "Official USEK graduate credit-fee table (2024-2025).",
    "tuition_source_ids": [
      "usek_grad_fees"
    ],
    "track_names": []
  },
  {
    "id": "usek-fas-master-biology",
    "faculty": "Faculty of Arts and Sciences",
    "department": null,
    "major_category": "Arts and Sciences",
    "major": "Biology",
    "degree_type": "MASTER",
    "official_degree_name": "Master of Science in Biology",
    "thesis_or_non_thesis": null,
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": null,
    "delivery_mode": null,
    "program_description": null,
    "official_program_url": "https://www.usek.edu.lb/academics/faculty-of-arts-and-sciences/department-of-languages-and-literatures/diploma-in-interpretation?t=2",
    "notes": "Listed on the Faculty of Arts and Sciences graduate hub page; individual program page was not canonicalized in this pass.",
    "source_ids": [
      "usek_interpretation_diploma",
      "usek_arts_sciences_school"
    ],
    "tuition_academic_year": "2024-2025",
    "tuition_currency": "USD",
    "tuition_billing_basis": "PER_CREDIT",
    "tuition_amount": 450,
    "tuition_category": "Faculty of Arts & Sciences - Biology",
    "tuition_notes": "Official USEK graduate credit-fee table (2024-2025).",
    "tuition_source_ids": [
      "usek_grad_fees"
    ],
    "track_names": []
  },
  {
    "id": "usek-fas-master-chemistry",
    "faculty": "Faculty of Arts and Sciences",
    "department": null,
    "major_category": "Arts and Sciences",
    "major": "Chemistry",
    "degree_type": "MASTER",
    "official_degree_name": "Master of Science in Chemistry",
    "thesis_or_non_thesis": null,
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": null,
    "delivery_mode": null,
    "program_description": null,
    "official_program_url": "https://www.usek.edu.lb/academics/faculty-of-arts-and-sciences/department-of-languages-and-literatures/diploma-in-interpretation?t=2",
    "notes": "Listed on the Faculty of Arts and Sciences graduate hub page; individual program page was not canonicalized in this pass.",
    "source_ids": [
      "usek_interpretation_diploma",
      "usek_arts_sciences_school"
    ],
    "tuition_academic_year": "2024-2025",
    "tuition_currency": "USD",
    "tuition_billing_basis": "PER_CREDIT",
    "tuition_amount": 450,
    "tuition_category": "Faculty of Arts & Sciences - Chemistry and Biochemistry",
    "tuition_notes": "Official USEK graduate credit-fee table (2024-2025).",
    "tuition_source_ids": [
      "usek_grad_fees"
    ],
    "track_names": []
  },
  {
    "id": "usek-fas-master-computer-science",
    "faculty": "Faculty of Arts and Sciences",
    "department": null,
    "major_category": "Arts and Sciences",
    "major": "Computer Science",
    "degree_type": "MASTER",
    "official_degree_name": "Master of Science in Computer Science",
    "thesis_or_non_thesis": null,
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": null,
    "delivery_mode": null,
    "program_description": null,
    "official_program_url": "https://www.usek.edu.lb/academics/faculty-of-arts-and-sciences/department-of-languages-and-literatures/diploma-in-interpretation?t=2",
    "notes": "Listed on the Faculty of Arts and Sciences graduate hub page; individual program page was not canonicalized in this pass.",
    "source_ids": [
      "usek_interpretation_diploma",
      "usek_arts_sciences_school"
    ],
    "tuition_academic_year": "2024-2025",
    "tuition_currency": "USD",
    "tuition_billing_basis": "PER_CREDIT",
    "tuition_amount": 450,
    "tuition_category": "Faculty of Arts & Sciences - Computer science and IT",
    "tuition_notes": "Official USEK graduate credit-fee table (2024-2025).",
    "tuition_source_ids": [
      "usek_grad_fees"
    ],
    "track_names": []
  },
  {
    "id": "usek-fas-master-cybersecurity-cyberdefence",
    "faculty": "Faculty of Arts and Sciences",
    "department": null,
    "major_category": "Arts and Sciences",
    "major": "Cybersecurity and Cyberdefence",
    "degree_type": "MASTER",
    "official_degree_name": "Master of Science in Cybersecurity and Cyberdefence",
    "thesis_or_non_thesis": null,
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": null,
    "delivery_mode": null,
    "program_description": null,
    "official_program_url": "https://www.usek.edu.lb/academics/faculty-of-arts-and-sciences/department-of-languages-and-literatures/diploma-in-interpretation?t=2",
    "notes": "Listed on the Faculty of Arts and Sciences graduate hub page; individual program page was not canonicalized in this pass.",
    "source_ids": [
      "usek_interpretation_diploma",
      "usek_arts_sciences_school"
    ],
    "tuition_academic_year": "2024-2025",
    "tuition_currency": "USD",
    "tuition_billing_basis": "PER_CREDIT",
    "tuition_amount": 450,
    "tuition_category": "Faculty of Arts & Sciences - Computer science and IT",
    "tuition_notes": "Official USEK graduate credit-fee table (2024-2025).",
    "tuition_source_ids": [
      "usek_grad_fees"
    ],
    "track_names": []
  },
  {
    "id": "usek-fas-master-environmental-technologies",
    "faculty": "Faculty of Arts and Sciences",
    "department": null,
    "major_category": "Arts and Sciences",
    "major": "Environmental Technologies",
    "degree_type": "MASTER",
    "official_degree_name": "Master of Science in Environmental Technologies",
    "thesis_or_non_thesis": null,
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": null,
    "delivery_mode": null,
    "program_description": null,
    "official_program_url": "https://www.usek.edu.lb/academics/faculty-of-arts-and-sciences/department-of-languages-and-literatures/diploma-in-interpretation?t=2",
    "notes": "Listed on the Faculty of Arts and Sciences graduate hub page; individual program page was not canonicalized in this pass.",
    "source_ids": [
      "usek_interpretation_diploma",
      "usek_arts_sciences_school"
    ],
    "tuition_academic_year": "2024-2025",
    "tuition_currency": "USD",
    "tuition_billing_basis": "PER_CREDIT",
    "tuition_amount": 450,
    "tuition_category": "Faculty of Arts & Sciences - science programs",
    "tuition_notes": "Official USEK graduate credit-fee table (2024-2025).",
    "tuition_source_ids": [
      "usek_grad_fees"
    ],
    "track_names": []
  },
  {
    "id": "usek-fas-master-mathematics",
    "faculty": "Faculty of Arts and Sciences",
    "department": null,
    "major_category": "Arts and Sciences",
    "major": "Mathematics",
    "degree_type": "MASTER",
    "official_degree_name": "Master of Science in Mathematics",
    "thesis_or_non_thesis": null,
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": null,
    "delivery_mode": null,
    "program_description": null,
    "official_program_url": "https://www.usek.edu.lb/academics/faculty-of-arts-and-sciences/department-of-languages-and-literatures/diploma-in-interpretation?t=2",
    "notes": "Listed on the Faculty of Arts and Sciences graduate hub page; individual program page was not canonicalized in this pass.",
    "source_ids": [
      "usek_interpretation_diploma",
      "usek_arts_sciences_school"
    ],
    "tuition_academic_year": "2024-2025",
    "tuition_currency": "USD",
    "tuition_billing_basis": "PER_CREDIT",
    "tuition_amount": 450,
    "tuition_category": "Faculty of Arts & Sciences - Mathematics",
    "tuition_notes": "Official USEK graduate credit-fee table (2024-2025).",
    "tuition_source_ids": [
      "usek_grad_fees"
    ],
    "track_names": []
  },
  {
    "id": "usek-fas-master-neuroscience-biotechnology",
    "faculty": "Faculty of Arts and Sciences",
    "department": null,
    "major_category": "Arts and Sciences",
    "major": "Neuroscience and Biotechnology",
    "degree_type": "MASTER",
    "official_degree_name": "Master of Science in Neuroscience and Biotechnology",
    "thesis_or_non_thesis": null,
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": null,
    "delivery_mode": null,
    "program_description": null,
    "official_program_url": "https://www.usek.edu.lb/academics/faculty-of-arts-and-sciences/department-of-languages-and-literatures/diploma-in-interpretation?t=2",
    "notes": "Listed on the Faculty of Arts and Sciences graduate hub page; individual program page was not canonicalized in this pass.",
    "source_ids": [
      "usek_interpretation_diploma",
      "usek_arts_sciences_school"
    ],
    "tuition_academic_year": "2024-2025",
    "tuition_currency": "USD",
    "tuition_billing_basis": "PER_CREDIT",
    "tuition_amount": 450,
    "tuition_category": "Faculty of Arts & Sciences - science programs",
    "tuition_notes": "Official USEK graduate credit-fee table (2024-2025).",
    "tuition_source_ids": [
      "usek_grad_fees"
    ],
    "track_names": []
  },
  {
    "id": "usek-fas-master-nutrition",
    "faculty": "Faculty of Arts and Sciences",
    "department": null,
    "major_category": "Arts and Sciences",
    "major": "Nutrition",
    "degree_type": "MASTER",
    "official_degree_name": "Master of Science in Nutrition",
    "thesis_or_non_thesis": null,
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": null,
    "delivery_mode": null,
    "program_description": null,
    "official_program_url": "https://www.usek.edu.lb/academics/faculty-of-arts-and-sciences/department-of-languages-and-literatures/diploma-in-interpretation?t=2",
    "notes": "Listed on the Faculty of Arts and Sciences graduate hub page; individual program page was not canonicalized in this pass.",
    "source_ids": [
      "usek_interpretation_diploma",
      "usek_arts_sciences_school"
    ],
    "tuition_academic_year": "2024-2025",
    "tuition_currency": "USD",
    "tuition_billing_basis": "PER_CREDIT",
    "tuition_amount": 387,
    "tuition_category": "Faculty of Arts & Sciences - Nutrition and Food Sciences",
    "tuition_notes": "Official USEK graduate credit-fee table (2024-2025).",
    "tuition_source_ids": [
      "usek_grad_fees"
    ],
    "track_names": []
  },
  {
    "id": "usek-fas-phd-agricultural-food-sciences",
    "faculty": "Faculty of Arts and Sciences",
    "department": "Department of Nutrition and Food Sciences",
    "major_category": "Arts and Sciences",
    "major": "Agricultural and Food Sciences",
    "degree_type": "PHD",
    "official_degree_name": "Ph.D. in Agricultural and Food Sciences",
    "thesis_or_non_thesis": null,
    "credits": 60,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": "English",
    "delivery_mode": null,
    "program_description": "The first doctoral degree in the discipline in Lebanon.",
    "official_program_url": "https://www.usek.edu.lb/faculty-of-arts-and-sciences/academic-programs/department-of-nutrition-and-food-sciences/phd-in-agricultural-and-food-sciences?t=5",
    "notes": "Listed on the Faculty of Arts and Sciences doctoral hub page; individual program pages were not canonicalized in this pass.",
    "source_ids": [
      "usek_fas_phd_list",
      "usek_arts_sciences_school"
    ],
    "tuition_academic_year": null,
    "tuition_currency": null,
    "tuition_billing_basis": null,
    "tuition_amount": null,
    "tuition_category": null,
    "tuition_notes": null,
    "tuition_source_ids": [],
    "track_names": []
  },
  {
    "id": "usek-fas-phd-arabic-language-literature",
    "faculty": "Faculty of Arts and Sciences",
    "department": null,
    "major_category": "Arts and Sciences",
    "major": "Arabic Language and Literature",
    "degree_type": "PHD",
    "official_degree_name": "Ph.D. in Arabic Language and Literature",
    "thesis_or_non_thesis": null,
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": null,
    "delivery_mode": null,
    "program_description": null,
    "official_program_url": "https://www.usek.edu.lb/faculty-of-arts-and-sciences/academic-programs/department-of-nutrition-and-food-sciences/phd-in-agricultural-and-food-sciences?t=5",
    "notes": "Listed on the Faculty of Arts and Sciences doctoral hub page; individual program pages were not canonicalized in this pass.",
    "source_ids": [
      "usek_fas_phd_list",
      "usek_arts_sciences_school"
    ],
    "tuition_academic_year": null,
    "tuition_currency": null,
    "tuition_billing_basis": null,
    "tuition_amount": null,
    "tuition_category": null,
    "tuition_notes": null,
    "tuition_source_ids": [],
    "track_names": []
  },
  {
    "id": "usek-fas-phd-archeology-art-history",
    "faculty": "Faculty of Arts and Sciences",
    "department": null,
    "major_category": "Arts and Sciences",
    "major": "Archeology and Art History",
    "degree_type": "PHD",
    "official_degree_name": "Ph.D. in Archeology and Art History",
    "thesis_or_non_thesis": null,
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": null,
    "delivery_mode": null,
    "program_description": null,
    "official_program_url": "https://www.usek.edu.lb/faculty-of-arts-and-sciences/academic-programs/department-of-nutrition-and-food-sciences/phd-in-agricultural-and-food-sciences?t=5",
    "notes": "Listed on the Faculty of Arts and Sciences doctoral hub page; individual program pages were not canonicalized in this pass.",
    "source_ids": [
      "usek_fas_phd_list",
      "usek_arts_sciences_school"
    ],
    "tuition_academic_year": null,
    "tuition_currency": null,
    "tuition_billing_basis": null,
    "tuition_amount": null,
    "tuition_category": null,
    "tuition_notes": null,
    "tuition_source_ids": [],
    "track_names": []
  },
  {
    "id": "usek-fas-phd-chemistry-life-earth-sciences",
    "faculty": "Faculty of Arts and Sciences",
    "department": null,
    "major_category": "Arts and Sciences",
    "major": "Chemistry and Life and Earth Sciences",
    "degree_type": "PHD",
    "official_degree_name": "Ph.D. in Chemistry and Life and Earth Sciences",
    "thesis_or_non_thesis": null,
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": null,
    "delivery_mode": null,
    "program_description": null,
    "official_program_url": "https://www.usek.edu.lb/faculty-of-arts-and-sciences/academic-programs/department-of-nutrition-and-food-sciences/phd-in-agricultural-and-food-sciences?t=5",
    "notes": "Listed on the Faculty of Arts and Sciences doctoral hub page; individual program pages were not canonicalized in this pass.",
    "source_ids": [
      "usek_fas_phd_list",
      "usek_arts_sciences_school"
    ],
    "tuition_academic_year": null,
    "tuition_currency": null,
    "tuition_billing_basis": null,
    "tuition_amount": null,
    "tuition_category": null,
    "tuition_notes": null,
    "tuition_source_ids": [],
    "track_names": []
  },
  {
    "id": "usek-fas-phd-conservation-restoration-cultural-property-sacred-art",
    "faculty": "Faculty of Arts and Sciences",
    "department": null,
    "major_category": "Arts and Sciences",
    "major": "Conservation, Restoration of Cultural Property & Sacred Art",
    "degree_type": "PHD",
    "official_degree_name": "Ph.D. in Conservation, Restoration of Cultural Property & Sacred Art",
    "thesis_or_non_thesis": null,
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": null,
    "delivery_mode": null,
    "program_description": null,
    "official_program_url": "https://www.usek.edu.lb/faculty-of-arts-and-sciences/academic-programs/department-of-nutrition-and-food-sciences/phd-in-agricultural-and-food-sciences?t=5",
    "notes": "Listed on the Faculty of Arts and Sciences doctoral hub page; individual program pages were not canonicalized in this pass.",
    "source_ids": [
      "usek_fas_phd_list",
      "usek_arts_sciences_school"
    ],
    "tuition_academic_year": null,
    "tuition_currency": null,
    "tuition_billing_basis": null,
    "tuition_amount": null,
    "tuition_category": null,
    "tuition_notes": null,
    "tuition_source_ids": [],
    "track_names": []
  },
  {
    "id": "usek-fas-phd-education-sciences",
    "faculty": "Faculty of Arts and Sciences",
    "department": null,
    "major_category": "Arts and Sciences",
    "major": "Education Sciences",
    "degree_type": "PHD",
    "official_degree_name": "Ph.D. in Education Sciences",
    "thesis_or_non_thesis": null,
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": null,
    "delivery_mode": null,
    "program_description": null,
    "official_program_url": "https://www.usek.edu.lb/faculty-of-arts-and-sciences/academic-programs/department-of-nutrition-and-food-sciences/phd-in-agricultural-and-food-sciences?t=5",
    "notes": "Listed on the Faculty of Arts and Sciences doctoral hub page; individual program pages were not canonicalized in this pass.",
    "source_ids": [
      "usek_fas_phd_list",
      "usek_arts_sciences_school"
    ],
    "tuition_academic_year": null,
    "tuition_currency": null,
    "tuition_billing_basis": null,
    "tuition_amount": null,
    "tuition_category": null,
    "tuition_notes": null,
    "tuition_source_ids": [],
    "track_names": []
  },
  {
    "id": "usek-fas-phd-english-language-literature",
    "faculty": "Faculty of Arts and Sciences",
    "department": null,
    "major_category": "Arts and Sciences",
    "major": "English Language and Literature",
    "degree_type": "PHD",
    "official_degree_name": "Ph.D. in English Language and Literature",
    "thesis_or_non_thesis": null,
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": null,
    "delivery_mode": null,
    "program_description": null,
    "official_program_url": "https://www.usek.edu.lb/faculty-of-arts-and-sciences/academic-programs/department-of-nutrition-and-food-sciences/phd-in-agricultural-and-food-sciences?t=5",
    "notes": "Listed on the Faculty of Arts and Sciences doctoral hub page; individual program pages were not canonicalized in this pass.",
    "source_ids": [
      "usek_fas_phd_list",
      "usek_arts_sciences_school"
    ],
    "tuition_academic_year": null,
    "tuition_currency": null,
    "tuition_billing_basis": null,
    "tuition_amount": null,
    "tuition_category": null,
    "tuition_notes": null,
    "tuition_source_ids": [],
    "track_names": []
  },
  {
    "id": "usek-fas-phd-french-language-literature",
    "faculty": "Faculty of Arts and Sciences",
    "department": null,
    "major_category": "Arts and Sciences",
    "major": "French Language and Literature",
    "degree_type": "PHD",
    "official_degree_name": "Ph.D. in French Language and Literature",
    "thesis_or_non_thesis": null,
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": null,
    "delivery_mode": null,
    "program_description": null,
    "official_program_url": "https://www.usek.edu.lb/faculty-of-arts-and-sciences/academic-programs/department-of-nutrition-and-food-sciences/phd-in-agricultural-and-food-sciences?t=5",
    "notes": "Listed on the Faculty of Arts and Sciences doctoral hub page; individual program pages were not canonicalized in this pass.",
    "source_ids": [
      "usek_fas_phd_list",
      "usek_arts_sciences_school"
    ],
    "tuition_academic_year": null,
    "tuition_currency": null,
    "tuition_billing_basis": null,
    "tuition_amount": null,
    "tuition_category": null,
    "tuition_notes": null,
    "tuition_source_ids": [],
    "track_names": []
  },
  {
    "id": "usek-fas-phd-history",
    "faculty": "Faculty of Arts and Sciences",
    "department": null,
    "major_category": "Arts and Sciences",
    "major": "History",
    "degree_type": "PHD",
    "official_degree_name": "Ph.D. in History",
    "thesis_or_non_thesis": null,
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": null,
    "delivery_mode": null,
    "program_description": null,
    "official_program_url": "https://www.usek.edu.lb/faculty-of-arts-and-sciences/academic-programs/department-of-nutrition-and-food-sciences/phd-in-agricultural-and-food-sciences?t=5",
    "notes": "Listed on the Faculty of Arts and Sciences doctoral hub page; individual program pages were not canonicalized in this pass.",
    "source_ids": [
      "usek_fas_phd_list",
      "usek_arts_sciences_school"
    ],
    "tuition_academic_year": null,
    "tuition_currency": null,
    "tuition_billing_basis": null,
    "tuition_amount": null,
    "tuition_category": null,
    "tuition_notes": null,
    "tuition_source_ids": [],
    "track_names": []
  },
  {
    "id": "usek-fas-phd-language-sciences-traductology",
    "faculty": "Faculty of Arts and Sciences",
    "department": null,
    "major_category": "Arts and Sciences",
    "major": "Language Sciences and Traductology",
    "degree_type": "PHD",
    "official_degree_name": "Ph.D. in Language Sciences and Traductology",
    "thesis_or_non_thesis": null,
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": null,
    "delivery_mode": null,
    "program_description": null,
    "official_program_url": "https://www.usek.edu.lb/faculty-of-arts-and-sciences/academic-programs/department-of-nutrition-and-food-sciences/phd-in-agricultural-and-food-sciences?t=5",
    "notes": "Listed on the Faculty of Arts and Sciences doctoral hub page; individual program pages were not canonicalized in this pass.",
    "source_ids": [
      "usek_fas_phd_list",
      "usek_arts_sciences_school"
    ],
    "tuition_academic_year": null,
    "tuition_currency": null,
    "tuition_billing_basis": null,
    "tuition_amount": null,
    "tuition_category": null,
    "tuition_notes": null,
    "tuition_source_ids": [],
    "track_names": []
  },
  {
    "id": "usek-fas-phd-philosophy",
    "faculty": "Faculty of Arts and Sciences",
    "department": null,
    "major_category": "Arts and Sciences",
    "major": "Philosophy",
    "degree_type": "PHD",
    "official_degree_name": "Ph.D. in Philosophy",
    "thesis_or_non_thesis": null,
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": null,
    "delivery_mode": null,
    "program_description": null,
    "official_program_url": "https://www.usek.edu.lb/faculty-of-arts-and-sciences/academic-programs/department-of-nutrition-and-food-sciences/phd-in-agricultural-and-food-sciences?t=5",
    "notes": "Listed on the Faculty of Arts and Sciences doctoral hub page; individual program pages were not canonicalized in this pass.",
    "source_ids": [
      "usek_fas_phd_list",
      "usek_arts_sciences_school"
    ],
    "tuition_academic_year": null,
    "tuition_currency": null,
    "tuition_billing_basis": null,
    "tuition_amount": null,
    "tuition_category": null,
    "tuition_notes": null,
    "tuition_source_ids": [],
    "track_names": []
  },
  {
    "id": "usek-fas-phd-psychology",
    "faculty": "Faculty of Arts and Sciences",
    "department": null,
    "major_category": "Arts and Sciences",
    "major": "Psychology",
    "degree_type": "PHD",
    "official_degree_name": "Ph.D. in Psychology",
    "thesis_or_non_thesis": null,
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": null,
    "delivery_mode": null,
    "program_description": null,
    "official_program_url": "https://www.usek.edu.lb/faculty-of-arts-and-sciences/academic-programs/department-of-nutrition-and-food-sciences/phd-in-agricultural-and-food-sciences?t=5",
    "notes": "Listed on the Faculty of Arts and Sciences doctoral hub page; individual program pages were not canonicalized in this pass.",
    "source_ids": [
      "usek_fas_phd_list",
      "usek_arts_sciences_school"
    ],
    "tuition_academic_year": null,
    "tuition_currency": null,
    "tuition_billing_basis": null,
    "tuition_amount": null,
    "tuition_category": null,
    "tuition_notes": null,
    "tuition_source_ids": [],
    "track_names": []
  },
  {
    "id": "usek-fas-phd-social-sciences",
    "faculty": "Faculty of Arts and Sciences",
    "department": null,
    "major_category": "Arts and Sciences",
    "major": "Social Sciences",
    "degree_type": "PHD",
    "official_degree_name": "Ph.D. in Social Sciences",
    "thesis_or_non_thesis": null,
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": null,
    "delivery_mode": null,
    "program_description": null,
    "official_program_url": "https://www.usek.edu.lb/faculty-of-arts-and-sciences/academic-programs/department-of-nutrition-and-food-sciences/phd-in-agricultural-and-food-sciences?t=5",
    "notes": "Listed on the Faculty of Arts and Sciences doctoral hub page; individual program pages were not canonicalized in this pass.",
    "source_ids": [
      "usek_fas_phd_list",
      "usek_arts_sciences_school"
    ],
    "tuition_academic_year": null,
    "tuition_currency": null,
    "tuition_billing_basis": null,
    "tuition_amount": null,
    "tuition_category": null,
    "tuition_notes": null,
    "tuition_source_ids": [],
    "track_names": []
  },
  {
    "id": "usek-fas-phd-visual-arts",
    "faculty": "Faculty of Arts and Sciences",
    "department": null,
    "major_category": "Arts and Sciences",
    "major": "Visual Arts",
    "degree_type": "PHD",
    "official_degree_name": "Ph.D. in Visual Arts",
    "thesis_or_non_thesis": null,
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "duration_raw_text": null,
    "language": null,
    "delivery_mode": null,
    "program_description": null,
    "official_program_url": "https://www.usek.edu.lb/faculty-of-arts-and-sciences/academic-programs/department-of-nutrition-and-food-sciences/phd-in-agricultural-and-food-sciences?t=5",
    "notes": "Listed on the Faculty of Arts and Sciences doctoral hub page; individual program pages were not canonicalized in this pass.",
    "source_ids": [
      "usek_fas_phd_list",
      "usek_arts_sciences_school"
    ],
    "tuition_academic_year": null,
    "tuition_currency": null,
    "tuition_billing_basis": null,
    "tuition_amount": null,
    "tuition_category": null,
    "tuition_notes": null,
    "tuition_source_ids": [],
    "track_names": []
  }
]$USEK$::jsonb) AS seed(id TEXT, faculty TEXT, department TEXT, major_category TEXT, major TEXT, degree_type TEXT, official_degree_name TEXT, thesis_or_non_thesis TEXT, credits INTEGER, duration_value NUMERIC(10,2), duration_unit TEXT, duration_raw_text TEXT, language TEXT, delivery_mode TEXT, program_description TEXT, official_program_url TEXT, notes TEXT, source_ids JSONB, tuition_academic_year TEXT, tuition_currency TEXT, tuition_billing_basis TEXT, tuition_amount NUMERIC(12,2), tuition_category TEXT, tuition_notes TEXT, tuition_source_ids JSONB, track_names JSONB);

    INSERT INTO graduate_program (university_id, faculty_id, department_id, degree_type_id, program_key, major_category, major, official_degree_name, thesis_or_non_thesis, credits, duration_value, duration_unit, primary_language_id, delivery_mode, program_description, official_program_url, source_id, notes)
    SELECT v_university_id, fac.id, dep.id, dt.id, seed.id, seed.major_category, seed.major, seed.official_degree_name, seed.thesis_or_non_thesis, seed.credits, seed.duration_value, seed.duration_unit, lang.id, seed.delivery_mode, seed.program_description, seed.official_program_url,
           (SELECT s.id FROM jsonb_array_elements_text(COALESCE(seed.source_ids, '[]'::jsonb)) WITH ORDINALITY AS src(source_seed_id, ord) JOIN usek_source_seed ss ON ss.source_id = src.source_seed_id JOIN source s ON s.university_id = v_university_id AND s.url = ss.url ORDER BY src.ord LIMIT 1),
           seed.notes
    FROM usek_program_seed seed
    JOIN university_faculty fac ON fac.university_id = v_university_id AND fac.name = seed.faculty
    LEFT JOIN university_department dep ON dep.university_id = v_university_id AND dep.faculty_id = fac.id AND dep.name = seed.department
    LEFT JOIN degree_type dt ON dt.code = seed.degree_type
    LEFT JOIN language lang ON LOWER(lang.name) = LOWER(seed.language)
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

    INSERT INTO graduate_program_source (university_id, program_id, source_id, source_role, source_order, evidence_text, notes)
    SELECT v_university_id, gp.id, s.id, CASE WHEN src.ord = 1 THEN 'PRIMARY' ELSE 'SECONDARY' END, src.ord, ss.page_title, 'Imported from USEK program sources.'
    FROM usek_program_seed seed
    JOIN graduate_program gp ON gp.university_id = v_university_id AND gp.program_key = seed.id
    JOIN LATERAL jsonb_array_elements_text(COALESCE(seed.source_ids, '[]'::jsonb)) WITH ORDINALITY AS src(source_seed_id, ord) ON TRUE
    JOIN usek_source_seed ss ON ss.source_id = src.source_seed_id
    JOIN source s ON s.university_id = v_university_id AND s.url = ss.url
    ON CONFLICT (program_id, source_id, source_role) DO UPDATE SET
        source_order = EXCLUDED.source_order,
        evidence_text = EXCLUDED.evidence_text,
        notes = EXCLUDED.notes,
        updated_at = NOW();

    INSERT INTO graduate_program_track (university_id, program_id, track_type, track_name, track_order, is_primary, description, source_id, notes)
    SELECT v_university_id, gp.id, 'TRACK', tr.track_name, tr.ord, CASE WHEN tr.ord = 1 THEN TRUE ELSE FALSE END, NULL, s.id, 'Imported from USEK program inventory.'
    FROM usek_program_seed seed
    JOIN graduate_program gp ON gp.university_id = v_university_id AND gp.program_key = seed.id
    JOIN LATERAL jsonb_array_elements_text(COALESCE(seed.track_names, '[]'::jsonb)) WITH ORDINALITY AS tr(track_name, ord) ON TRUE
    JOIN LATERAL (
        SELECT s.id
        FROM jsonb_array_elements_text(COALESCE(seed.source_ids, '[]'::jsonb)) WITH ORDINALITY AS src(source_seed_id, ord)
        JOIN usek_source_seed ss ON ss.source_id = src.source_seed_id
        JOIN source s ON s.university_id = v_university_id AND s.url = ss.url
        ORDER BY src.ord LIMIT 1
    ) s ON TRUE
    ON CONFLICT (program_id, track_type, track_name) DO UPDATE SET
        track_order = EXCLUDED.track_order,
        is_primary = EXCLUDED.is_primary,
        description = EXCLUDED.description,
        notes = EXCLUDED.notes,
        updated_at = NOW();

    INSERT INTO graduate_tuition_rate (university_id, faculty_id, department_id, program_id, scope_level, record_key, academic_year, currency, billing_basis, amount, category, notes, source_id)
    SELECT v_university_id, gp.faculty_id, gp.department_id, gp.id, 'PROGRAM', seed.id || ':tuition', seed.tuition_academic_year, seed.tuition_currency, seed.tuition_billing_basis, seed.tuition_amount, seed.tuition_category, seed.tuition_notes, s.id
    FROM usek_program_seed seed
    JOIN graduate_program gp ON gp.university_id = v_university_id AND gp.program_key = seed.id
    JOIN source s ON s.university_id = v_university_id AND s.url = 'https://www.usek.edu.lb/en/university-fees/graduate-studies-1'
    WHERE seed.tuition_amount IS NOT NULL
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

    INSERT INTO graduate_fee_item (university_id, faculty_id, department_id, program_id, scope_level, record_key, academic_year, fee_name, billing_basis, currency, amount, category, notes, source_id)
    SELECT v_university_id, NULL, NULL, NULL, 'UNIVERSITY', seed.record_key, seed.academic_year, seed.fee_name, seed.billing_basis, seed.currency, seed.amount, seed.category, seed.notes, s.id
    FROM jsonb_to_recordset($USEK$[
  {
    "record_key": "usek:fee:1",
    "academic_year": null,
    "fee_name": "Admission file fee",
    "billing_basis": "FLAT_FEE",
    "currency": "USD",
    "amount": 30,
    "category": "Admission file fee",
    "notes": "Non-refundable; paid at a bank specified by USEK or at any OMT branch with no additional fees.",
    "source_url": "https://www.usek.edu.lb/Content/Assets/20260319AdmissionGuide-021354.pdf"
  },
  {
    "record_key": "usek:fee:2",
    "academic_year": null,
    "fee_name": "Registration fee",
    "billing_basis": "PER_SEMESTER",
    "currency": "USD",
    "amount": 200,
    "category": "Registration fee",
    "notes": "Paid per semester (Fall, Spring, Summer Session). Included in the first payment.",
    "source_url": "https://www.usek.edu.lb/en/university-fees/graduate-studies-1"
  },
  {
    "record_key": "usek:fee:3",
    "academic_year": null,
    "fee_name": "Operational fee",
    "billing_basis": "PER_SEMESTER",
    "currency": "USD",
    "amount": 300,
    "category": "Operational fee",
    "notes": "Charged in Fall and Spring semesters and included in the first payment.",
    "source_url": "https://www.usek.edu.lb/en/university-fees/graduate-studies-1"
  },
  {
    "record_key": "usek:fee:4",
    "academic_year": null,
    "fee_name": "NSSF membership fee",
    "billing_basis": "PER_ACADEMIC_YEAR",
    "currency": "USD",
    "amount": 94,
    "category": "NSSF membership fee",
    "notes": "Paid in fresh dollars at the market rate; subject to NSSF exemption rules for affiliated students and students over 30.",
    "source_url": "https://www.usek.edu.lb/en/university-fees/graduate-studies-1"
  },
  {
    "record_key": "usek:fee:5",
    "academic_year": null,
    "fee_name": "First payment",
    "billing_basis": "FLAT_FEE",
    "currency": "USD",
    "amount": 1000,
    "category": "First payment",
    "notes": "First payment includes the USD 200 registration fee.",
    "source_url": "https://www.usek.edu.lb/en/university-fees/graduate-studies-1"
  }
]$USEK$::jsonb) AS seed(record_key TEXT, academic_year TEXT, fee_name TEXT, billing_basis TEXT, currency TEXT, amount NUMERIC(12,2), category TEXT, notes TEXT, source_url TEXT)
    JOIN source s ON s.university_id = v_university_id AND s.url = seed.source_url
    ON CONFLICT (university_id, record_key) DO UPDATE SET fee_name = EXCLUDED.fee_name, billing_basis = EXCLUDED.billing_basis, currency = EXCLUDED.currency, amount = EXCLUDED.amount, category = EXCLUDED.category, notes = EXCLUDED.notes, source_id = EXCLUDED.source_id, updated_at = NOW();

    INSERT INTO graduate_admission_requirement (university_id, faculty_id, department_id, program_id, scope_level, record_key, requirement_type, requirement_text, comparison_operator, threshold_value, threshold_unit, is_required, notes, source_id)
    SELECT v_university_id, NULL, NULL, NULL, 'UNIVERSITY', seed.record_key, seed.requirement_type, seed.requirement_text, NULL, NULL, NULL, TRUE, seed.notes, s.id
    FROM jsonb_to_recordset($USEK$[
  {
    "record_key": "usek:admissions:general",
    "requirement_type": "GENERAL",
    "requirement_text": "Graduate applicants must hold an undergraduate degree or equivalent recognized by the Lebanese Ministry of Education and Higher Education, receive the recommendation of the unit Graduate Admission Interview Committee, meet any program-specific requirements, submit the online graduate application with the required documents, and pay the non-refundable admission file fee.",
    "notes": "Review the graduate program and any program-specific requirements. | Submit the online graduate application. | Upload the required documents. | Pay the non-refundable USD 30 admission file fee. | Complete the admission interview and any language proficiency test if required. | For doctoral study, submit the PhD Thesis Proposal path requested by the Doctoral College or the iPTP template when applicable.",
    "source_url": "https://www.usek.edu.lb/Content/Assets/20260319AdmissionGuide-021354.pdf"
  },
  {
    "record_key": "usek:admissions:english",
    "requirement_type": "ENGLISH",
    "requirement_text": "USEK states that graduate applicants may be required to pass language proficiency tests if needed. For some graduate programs, the guide specifies International English Tests or the USEK Language Test; some programs are multilingual and some are not applicable.",
    "notes": "International English Tests, USEK Language Test. Exact language conditions vary by program and are not centralized into a single graduate-wide score table in the official materials used here.",
    "source_url": "https://www.usek.edu.lb/Content/Assets/20260319AdmissionGuide-021354.pdf"
  },
  {
    "record_key": "usek:admissions:doctoral-prerequisite",
    "requirement_type": "PREREQUISITE",
    "requirement_text": "Doctoral applicants must follow the doctoral studies route and submit the required doctoral proposal materials when applicable.",
    "notes": "Derived from the doctoral admissions page and the doctoral eligibility appendix.",
    "source_url": "https://www.usek.edu.lb/en/admission/for-doctoral-studies"
  },
  {
    "record_key": "usek:admissions:interview",
    "requirement_type": "INTERVIEW",
    "requirement_text": "A graduate admission interview may be required and applications are reviewed by the unit Graduate Admission Interview Committee.",
    "notes": "Derived from the graduate admission guide and doctoral admissions page.",
    "source_url": "https://www.usek.edu.lb/Content/Assets/20260319AdmissionGuide-021354.pdf"
  }
]$USEK$::jsonb) AS seed(record_key TEXT, requirement_type TEXT, requirement_text TEXT, notes TEXT, source_url TEXT)
    JOIN source s ON s.university_id = v_university_id AND s.url = seed.source_url
    ON CONFLICT (university_id, record_key) DO UPDATE SET requirement_type = EXCLUDED.requirement_type, requirement_text = EXCLUDED.requirement_text, notes = EXCLUDED.notes, source_id = EXCLUDED.source_id, updated_at = NOW();

    INSERT INTO graduate_required_document (university_id, faculty_id, department_id, program_id, scope_level, record_key, document_type, document_name, is_optional, sort_order, notes, source_id)
    SELECT v_university_id, NULL, NULL, NULL, 'UNIVERSITY', seed.record_key, seed.document_type, seed.document_name, seed.is_optional, seed.sort_order, seed.notes, s.id
    FROM jsonb_to_recordset($USEK$[
  {
    "record_key": "usek:doc:general:1",
    "document_type": "IDENTITY",
    "document_name": "Photocopy of the national identity card or valid passport (or individual civil extract).",
    "is_optional": false,
    "sort_order": 1,
    "notes": "General graduate admissions document.",
    "source_url": "https://www.usek.edu.lb/Content/Assets/20260319AdmissionGuide-021354.pdf"
  },
  {
    "record_key": "usek:doc:general:2",
    "document_type": "BACCALAUREATE",
    "document_name": "Baccalaureate certificate or equivalent, certified by the Lebanese Ministry of Education and Higher Education when needed.",
    "is_optional": false,
    "sort_order": 2,
    "notes": "General graduate admissions document.",
    "source_url": "https://www.usek.edu.lb/Content/Assets/20260319AdmissionGuide-021354.pdf"
  },
  {
    "record_key": "usek:doc:general:3",
    "document_type": "EQUIVALENCE",
    "document_name": "Certified copies of university degree equivalence(s) when needed.",
    "is_optional": false,
    "sort_order": 3,
    "notes": "General graduate admissions document.",
    "source_url": "https://www.usek.edu.lb/Content/Assets/20260319AdmissionGuide-021354.pdf"
  },
  {
    "record_key": "usek:doc:general:4",
    "document_type": "TRANSCRIPT",
    "document_name": "Official transcript(s) for all previous academic work.",
    "is_optional": false,
    "sort_order": 4,
    "notes": "General graduate admissions document.",
    "source_url": "https://www.usek.edu.lb/Content/Assets/20260319AdmissionGuide-021354.pdf"
  },
  {
    "record_key": "usek:doc:general:5",
    "document_type": "RECOMMENDATION",
    "document_name": "Two recommendation letters (optional at the graduate level; doctoral requirements are stricter).",
    "is_optional": true,
    "sort_order": 5,
    "notes": "General graduate admissions document.",
    "source_url": "https://www.usek.edu.lb/Content/Assets/20260319AdmissionGuide-021354.pdf"
  },
  {
    "record_key": "usek:doc:general:6",
    "document_type": "MOTIVATION",
    "document_name": "Motivation letter.",
    "is_optional": false,
    "sort_order": 6,
    "notes": "General graduate admissions document.",
    "source_url": "https://www.usek.edu.lb/Content/Assets/20260319AdmissionGuide-021354.pdf"
  },
  {
    "record_key": "usek:doc:general:7",
    "document_type": "CV",
    "document_name": "Curriculum vitae and/or e-portfolio.",
    "is_optional": false,
    "sort_order": 7,
    "notes": "General graduate admissions document.",
    "source_url": "https://www.usek.edu.lb/Content/Assets/20260319AdmissionGuide-021354.pdf"
  },
  {
    "record_key": "usek:doc:general:8",
    "document_type": "EMPLOYMENT",
    "document_name": "Employment certificate(s), if applicable.",
    "is_optional": true,
    "sort_order": 8,
    "notes": "General graduate admissions document.",
    "source_url": "https://www.usek.edu.lb/Content/Assets/20260319AdmissionGuide-021354.pdf"
  },
  {
    "record_key": "usek:doc:doctoral:1",
    "document_type": "PASSPORT",
    "document_name": "Passport copy for foreign applicants or national identity card copy for local applicants.",
    "is_optional": false,
    "sort_order": 1,
    "notes": "Doctoral admissions document.",
    "source_url": "https://www.usek.edu.lb/en/admission/for-doctoral-studies"
  },
  {
    "record_key": "usek:doc:doctoral:2",
    "document_type": "PHOTO",
    "document_name": "Recent passport-size photo.",
    "is_optional": false,
    "sort_order": 2,
    "notes": "Doctoral admissions document.",
    "source_url": "https://www.usek.edu.lb/en/admission/for-doctoral-studies"
  },
  {
    "record_key": "usek:doc:doctoral:3",
    "document_type": "BACCALAUREATE",
    "document_name": "Lebanese Baccalaureate certificate or equivalent, certified when required.",
    "is_optional": false,
    "sort_order": 3,
    "notes": "Doctoral admissions document.",
    "source_url": "https://www.usek.edu.lb/en/admission/for-doctoral-studies"
  },
  {
    "record_key": "usek:doc:doctoral:4",
    "document_type": "BACHELOR_DEGREE",
    "document_name": "Certified bachelor’s degree and transcript.",
    "is_optional": false,
    "sort_order": 4,
    "notes": "Doctoral admissions document.",
    "source_url": "https://www.usek.edu.lb/en/admission/for-doctoral-studies"
  },
  {
    "record_key": "usek:doc:doctoral:5",
    "document_type": "MASTER_DEGREE",
    "document_name": "Certified master’s degree and transcript.",
    "is_optional": false,
    "sort_order": 5,
    "notes": "Doctoral admissions document.",
    "source_url": "https://www.usek.edu.lb/en/admission/for-doctoral-studies"
  },
  {
    "record_key": "usek:doc:doctoral:6",
    "document_type": "EQUIVALENCE",
    "document_name": "Equivalence of bachelor’s and master’s degrees certified by the Ministry of Education and Higher Education.",
    "is_optional": false,
    "sort_order": 6,
    "notes": "Doctoral admissions document.",
    "source_url": "https://www.usek.edu.lb/en/admission/for-doctoral-studies"
  },
  {
    "record_key": "usek:doc:doctoral:7",
    "document_type": "THESIS",
    "document_name": "Master’s dissertation / thesis / final-year project.",
    "is_optional": false,
    "sort_order": 7,
    "notes": "Doctoral admissions document.",
    "source_url": "https://www.usek.edu.lb/en/admission/for-doctoral-studies"
  },
  {
    "record_key": "usek:doc:doctoral:8",
    "document_type": "RECOMMENDATION",
    "document_name": "Two recommendation letters from faculty who are not prospective supervisors.",
    "is_optional": false,
    "sort_order": 8,
    "notes": "Doctoral admissions document.",
    "source_url": "https://www.usek.edu.lb/en/admission/for-doctoral-studies"
  },
  {
    "record_key": "usek:doc:doctoral:9",
    "document_type": "MOTIVATION",
    "document_name": "Motivation letter (400-500 words).",
    "is_optional": false,
    "sort_order": 9,
    "notes": "Doctoral admissions document.",
    "source_url": "https://www.usek.edu.lb/en/admission/for-doctoral-studies"
  },
  {
    "record_key": "usek:doc:doctoral:10",
    "document_type": "CV",
    "document_name": "Resume / CV.",
    "is_optional": false,
    "sort_order": 10,
    "notes": "Doctoral admissions document.",
    "source_url": "https://www.usek.edu.lb/en/admission/for-doctoral-studies"
  },
  {
    "record_key": "usek:doc:doctoral:11",
    "document_type": "EMPLOYMENT",
    "document_name": "Employment certificates, if any.",
    "is_optional": true,
    "sort_order": 11,
    "notes": "Doctoral admissions document.",
    "source_url": "https://www.usek.edu.lb/en/admission/for-doctoral-studies"
  },
  {
    "record_key": "usek:doc:doctoral:12",
    "document_type": "RESEARCH",
    "document_name": "Evidence of research activity such as books, publications, and conference participations.",
    "is_optional": true,
    "sort_order": 12,
    "notes": "Doctoral admissions document.",
    "source_url": "https://www.usek.edu.lb/en/admission/for-doctoral-studies"
  },
  {
    "record_key": "usek:doc:doctoral:13",
    "document_type": "ADMISSION_FILE",
    "document_name": "Admission File information.",
    "is_optional": false,
    "sort_order": 13,
    "notes": "Doctoral admissions document.",
    "source_url": "https://www.usek.edu.lb/en/admission/for-doctoral-studies"
  },
  {
    "record_key": "usek:doc:doctoral:14",
    "document_type": "PROPOSAL",
    "document_name": "Individual PhD Thesis Proposal (iPTP) for candidates submitting a PhD proposal.",
    "is_optional": false,
    "sort_order": 14,
    "notes": "Doctoral admissions document.",
    "source_url": "https://www.usek.edu.lb/en/admission/for-doctoral-studies"
  }
]$USEK$::jsonb) AS seed(record_key TEXT, document_type TEXT, document_name TEXT, is_optional BOOLEAN, sort_order INTEGER, notes TEXT, source_url TEXT)
    JOIN source s ON s.university_id = v_university_id AND s.url = seed.source_url
    ON CONFLICT (university_id, record_key) DO UPDATE SET document_type = EXCLUDED.document_type, document_name = EXCLUDED.document_name, is_optional = EXCLUDED.is_optional, sort_order = EXCLUDED.sort_order, notes = EXCLUDED.notes, source_id = EXCLUDED.source_id, updated_at = NOW();

    INSERT INTO graduate_admission_deadline (university_id, faculty_id, department_id, program_id, scope_level, record_key, academic_year, deadline_type, term, deadline_date, note, source_id)
    SELECT v_university_id, NULL, NULL, NULL, 'UNIVERSITY', seed.record_key, NULL, seed.deadline_type, seed.term, seed.deadline_date, seed.note, s.id
    FROM jsonb_to_recordset($USEK$[
  {
    "record_key": "usek:deadline:graduate-application",
    "deadline_type": "FINAL",
    "term": "Graduate application deadline",
    "deadline_date": "2026-07-15",
    "note": "Graduate application deadline published by USEK.",
    "source_url": "https://www.usek.edu.lb/en/admission/for-graduate-studies/dates-and-deadlines?parent=1"
  },
  {
    "record_key": "usek:deadline:graduate-exam-1",
    "deadline_type": "INTERVIEW",
    "term": "Graduate exam session",
    "deadline_date": "2026-06-02",
    "note": "Graduate exam date / session 1.",
    "source_url": "https://www.usek.edu.lb/en/admission/for-graduate-studies/dates-and-deadlines?parent=1"
  },
  {
    "record_key": "usek:deadline:graduate-exam-2",
    "deadline_type": "INTERVIEW",
    "term": "Graduate exam session",
    "deadline_date": "2026-07-23",
    "note": "Graduate exam date / session 2.",
    "source_url": "https://www.usek.edu.lb/en/admission/for-graduate-studies/dates-and-deadlines?parent=1"
  },
  {
    "record_key": "usek:deadline:graduate-results",
    "deadline_type": "OTHER",
    "term": "Graduate results",
    "deadline_date": "2026-07-28",
    "note": "Graduate results publication date.",
    "source_url": "https://www.usek.edu.lb/en/admission/for-graduate-studies/dates-and-deadlines?parent=1"
  },
  {
    "record_key": "usek:deadline:graduate-file-transfer",
    "deadline_type": "FINAL",
    "term": "Graduate file transfer deadline",
    "deadline_date": "2026-06-30",
    "note": "Deadline to transfer file from another university.",
    "source_url": "https://www.usek.edu.lb/en/admission/for-graduate-studies/dates-and-deadlines?parent=1"
  },
  {
    "record_key": "usek:deadline:doctoral-comps-1",
    "deadline_type": "INTERVIEW",
    "term": "Doctoral comprehensive exam session 1",
    "deadline_date": "2026-04-29",
    "note": "Doctoral exam window 1; application deadline 2026-03-13.",
    "source_url": "https://www.usek.edu.lb/en/admission/for-doctoral-studies"
  },
  {
    "record_key": "usek:deadline:doctoral-comps-2",
    "deadline_type": "INTERVIEW",
    "term": "Doctoral comprehensive exam session 2",
    "deadline_date": "2026-07-08",
    "note": "Doctoral exam window 2; application deadline 2026-06-05.",
    "source_url": "https://www.usek.edu.lb/en/admission/for-doctoral-studies"
  }
]$USEK$::jsonb) AS seed(record_key TEXT, deadline_type TEXT, term TEXT, deadline_date DATE, note TEXT, source_url TEXT)
    JOIN source s ON s.university_id = v_university_id AND s.url = seed.source_url
    ON CONFLICT (university_id, record_key) DO UPDATE SET academic_year = EXCLUDED.academic_year, deadline_type = EXCLUDED.deadline_type, term = EXCLUDED.term, deadline_date = EXCLUDED.deadline_date, note = EXCLUDED.note, source_id = EXCLUDED.source_id, updated_at = NOW();

    INSERT INTO graduate_scholarship (university_id, faculty_id, department_id, program_id, scope_level, record_key, academic_year, name, description, coverage, amount, currency, notes, source_id)
    SELECT v_university_id, NULL, NULL, NULL, 'UNIVERSITY', seed.record_key, seed.academic_year, seed.name, seed.description, seed.coverage, seed.amount, seed.currency, seed.notes, s.id
    FROM jsonb_to_recordset($USEK$[
  {
    "record_key": "usek:scholarship:1",
    "academic_year": null,
    "name": "Financial aid applications through the Financial Aid Office",
    "description": "USEK publishes a university-wide scholarships and financial-aid hub. The official page emphasizes aid, discounts, and agreement-based reductions rather than a separate graduate-only scholarship table.",
    "coverage": null,
    "amount": null,
    "currency": null,
    "notes": "Graduate-specific scholarship amounts were not published in the official materials used for this pass.",
    "source_url": "https://www.usek.edu.lb/students/scholarships-and-financial-aids"
  },
  {
    "record_key": "usek:scholarship:2",
    "academic_year": null,
    "name": "Sibling / OLM-relative / employee / agreement-based reductions",
    "description": "USEK publishes a university-wide scholarships and financial-aid hub. The official page emphasizes aid, discounts, and agreement-based reductions rather than a separate graduate-only scholarship table.",
    "coverage": null,
    "amount": null,
    "currency": null,
    "notes": "Graduate-specific scholarship amounts were not published in the official materials used for this pass.",
    "source_url": "https://www.usek.edu.lb/students/scholarships-and-financial-aids"
  },
  {
    "record_key": "usek:scholarship:3",
    "academic_year": null,
    "name": "Scholarship offers for admission with scholarship are described on the university page, but they are primarily framed around admission cycles and are not published as a separate graduate-only table",
    "description": "USEK publishes a university-wide scholarships and financial-aid hub. The official page emphasizes aid, discounts, and agreement-based reductions rather than a separate graduate-only scholarship table.",
    "coverage": null,
    "amount": null,
    "currency": null,
    "notes": "Graduate-specific scholarship amounts were not published in the official materials used for this pass.",
    "source_url": "https://www.usek.edu.lb/students/scholarships-and-financial-aids"
  }
]$USEK$::jsonb) AS seed(record_key TEXT, academic_year TEXT, name TEXT, description TEXT, coverage TEXT, amount NUMERIC(12,2), currency TEXT, notes TEXT, source_url TEXT)
    JOIN source s ON s.university_id = v_university_id AND s.url = seed.source_url
    ON CONFLICT (university_id, record_key) DO UPDATE SET academic_year = EXCLUDED.academic_year, name = EXCLUDED.name, description = EXCLUDED.description, coverage = EXCLUDED.coverage, amount = EXCLUDED.amount, currency = EXCLUDED.currency, notes = EXCLUDED.notes, source_id = EXCLUDED.source_id, updated_at = NOW();

    INSERT INTO graduate_financial_aid (university_id, faculty_id, department_id, program_id, scope_level, record_key, academic_year, name, description, amount, currency, notes, source_id)
    SELECT v_university_id, NULL, NULL, NULL, 'UNIVERSITY', seed.record_key, seed.academic_year, seed.name, seed.description, seed.amount, seed.currency, seed.notes, s.id
    FROM jsonb_to_recordset($USEK$[
  {
    "record_key": "usek:aid:1",
    "academic_year": null,
    "name": "Financial aid application through the Financial Aid Office",
    "description": "USEK states that the Financial Aid Office helps students facing financial difficulties through discounts and formal aid applications.",
    "amount": null,
    "currency": null,
    "notes": null,
    "source_url": "https://www.usek.edu.lb/students/scholarships-and-financial-aids"
  },
  {
    "record_key": "usek:aid:2",
    "academic_year": null,
    "name": "Agreement-based discounts and allowances",
    "description": "USEK states that the Financial Aid Office helps students facing financial difficulties through discounts and formal aid applications.",
    "amount": null,
    "currency": null,
    "notes": null,
    "source_url": "https://www.usek.edu.lb/students/scholarships-and-financial-aids"
  },
  {
    "record_key": "usek:aid:3",
    "academic_year": null,
    "name": "Special cases for siblings, OLM relatives, faculty and employees, and several partner agreements",
    "description": "USEK states that the Financial Aid Office helps students facing financial difficulties through discounts and formal aid applications.",
    "amount": null,
    "currency": null,
    "notes": null,
    "source_url": "https://www.usek.edu.lb/students/scholarships-and-financial-aids"
  }
]$USEK$::jsonb) AS seed(record_key TEXT, academic_year TEXT, name TEXT, description TEXT, amount NUMERIC(12,2), currency TEXT, notes TEXT, source_url TEXT)
    JOIN source s ON s.university_id = v_university_id AND s.url = seed.source_url
    ON CONFLICT (university_id, record_key) DO UPDATE SET academic_year = EXCLUDED.academic_year, name = EXCLUDED.name, description = EXCLUDED.description, amount = EXCLUDED.amount, currency = EXCLUDED.currency, notes = EXCLUDED.notes, source_id = EXCLUDED.source_id, updated_at = NOW();

    INSERT INTO graduate_payment_plan (university_id, faculty_id, department_id, program_id, scope_level, record_key, academic_year, name, description, installments_count, down_payment_amount, down_payment_currency, interval_label, notes, source_id)
    SELECT v_university_id, NULL, NULL, NULL, 'UNIVERSITY', seed.record_key, seed.academic_year, seed.name, seed.description, seed.installments_count, seed.down_payment_amount, seed.down_payment_currency, seed.interval_label, seed.notes, s.id
    FROM jsonb_to_recordset($USEK$[
  {
    "record_key": "usek:payment-plan:fall-spring",
    "academic_year": null,
    "name": "Fall and Spring installment plan",
    "description": "Four installments per semester for Fall and Spring",
    "installments_count": 4,
    "down_payment_amount": 1000,
    "down_payment_currency": "USD",
    "interval_label": "Semester",
    "notes": "First payment is USD 1000 and includes the USD 200 registration fee Penalty applies if deadlines in the academic calendar are missed",
    "source_url": "https://www.usek.edu.lb/en/university-fees/graduate-studies-1"
  },
  {
    "record_key": "usek:payment-plan:summer-session",
    "academic_year": null,
    "name": "Summer Session installment plan",
    "description": "Two installments for Summer Session",
    "installments_count": 2,
    "down_payment_amount": 1000,
    "down_payment_currency": "USD",
    "interval_label": "Summer Session",
    "notes": "First payment is USD 1000 and includes the USD 200 registration fee Penalty applies if deadlines in the academic calendar are missed",
    "source_url": "https://www.usek.edu.lb/en/university-fees/graduate-studies-1"
  }
]$USEK$::jsonb) AS seed(record_key TEXT, academic_year TEXT, name TEXT, description TEXT, installments_count INTEGER, down_payment_amount NUMERIC(12,2), down_payment_currency TEXT, interval_label TEXT, notes TEXT, source_url TEXT)
    JOIN source s ON s.university_id = v_university_id AND s.url = seed.source_url
    ON CONFLICT (university_id, record_key) DO UPDATE SET academic_year = EXCLUDED.academic_year, name = EXCLUDED.name, description = EXCLUDED.description, installments_count = EXCLUDED.installments_count, down_payment_amount = EXCLUDED.down_payment_amount, down_payment_currency = EXCLUDED.down_payment_currency, interval_label = EXCLUDED.interval_label, notes = EXCLUDED.notes, source_id = EXCLUDED.source_id, updated_at = NOW();

    INSERT INTO graduate_accreditation (university_id, faculty_id, department_id, program_id, scope_level, record_key, name, authority, status, valid_from, valid_until, notes, source_id)
    SELECT v_university_id, NULL, NULL, NULL, 'UNIVERSITY', seed.record_key, seed.name, seed.authority, seed.status, seed.valid_from, seed.valid_until, seed.notes, s.id
    FROM jsonb_to_recordset($USEK$[
  {
    "record_key": "usek:accreditation:1",
    "name": "Initial accreditation by the New England Commission of Higher Education (NECHE)",
    "authority": "New England Commission of Higher Education",
    "status": "University-level accreditation",
    "valid_from": null,
    "valid_until": null,
    "notes": "University-level",
    "source_url": "https://www.usek.edu.lb/en/home"
  }
]$USEK$::jsonb) AS seed(record_key TEXT, name TEXT, authority TEXT, status TEXT, valid_from DATE, valid_until DATE, notes TEXT, source_url TEXT)
    JOIN source s ON s.university_id = v_university_id AND s.url = seed.source_url
    ON CONFLICT (university_id, record_key) DO UPDATE SET name = EXCLUDED.name, authority = EXCLUDED.authority, status = EXCLUDED.status, valid_from = EXCLUDED.valid_from, valid_until = EXCLUDED.valid_until, notes = EXCLUDED.notes, source_id = EXCLUDED.source_id, updated_at = NOW();

END $$;
