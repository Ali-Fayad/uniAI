-- UL graduate data seed migration.
-- Idempotent import for the canonical UL graduate dataset.

-- UL reuses hub/catalog URLs across multiple graduate programs.
ALTER TABLE graduate_program DROP CONSTRAINT IF EXISTS uq_graduate_program_university_url;

DO $$
DECLARE
    v_university_id BIGINT;
BEGIN

    INSERT INTO university (name, name_ar, acronym, country, city, latitude, longitude, campus_name, campus_type)
    SELECT 'Lebanese University', NULL, 'UL', 'Lebanon', NULL, NULL, NULL, NULL, NULL
    WHERE NOT EXISTS (
        SELECT 1 FROM university WHERE name = 'Lebanese University'
    );

    SELECT id INTO v_university_id
    FROM university
    WHERE name = 'Lebanese University'
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

    CREATE TEMP TABLE ul_source_seed (
        source_id TEXT PRIMARY KEY,
        title TEXT NOT NULL,
        url TEXT NOT NULL,
        source_type TEXT NOT NULL,
        accessed_at DATE,
        notes TEXT
    ) ON COMMIT DROP;

    INSERT INTO ul_source_seed (source_id, title, url, source_type, accessed_at, notes)
    SELECT source_id, title, url, source_type, accessed_at, notes
    FROM jsonb_to_recordset($UL_SOURCES$[
  {
    "source_id": "UL-SRC-001",
    "title": "Home | Lebanese University",
    "url": "https://www.ul.edu.lb/en",
    "source_type": "official_page",
    "accessed_at": "2026-07-02",
    "notes": "Homepage; entry point; exposes main navigation, faculties, admissions, news/announcements."
  },
  {
    "source_id": "UL-SRC-002",
    "title": "Admissions | Lebanese University",
    "url": "https://ul.edu.lb/en/admission",
    "source_type": "official_page",
    "accessed_at": "2026-07-02",
    "notes": "General admissions landing page; states admission and registration depend on specialization and university level."
  },
  {
    "source_id": "UL-SRC-003",
    "title": "Specializations | Lebanese University",
    "url": "https://ul.edu.lb/en/majors-main",
    "source_type": "official_page",
    "accessed_at": "2026-07-02",
    "notes": "Specializations landing/search page; reports 106 specialization entries and links to faculty/institute program listings."
  },
  {
    "source_id": "UL-SRC-004",
    "title": "Majors | Lebanese University",
    "url": "https://ul.edu.lb/en/majors",
    "source_type": "official_page",
    "accessed_at": "2026-07-02",
    "notes": "Global majors listing; includes degree-level filters and program cards."
  },
  {
    "source_id": "UL-SRC-005",
    "title": "Faculty of Engineering | Lebanese University",
    "url": "https://ul.edu.lb/en/colleges-faculties-majors/343/Faculty-of-Engineering",
    "source_type": "official_page",
    "accessed_at": "2026-07-02",
    "notes": "Graduate program listing found: research/professional masters including Hydrosciences, Mechanics, Natural Risks, Renewable Energies, Robotics, TCMIS, Telecoms, Civil Engineering, HTTE."
  },
  {
    "source_id": "UL-SRC-006",
    "title": "Faculty of Engineering | Lebanese University",
    "url": "https://ul.edu.lb/en/colleges-faculties-details/343/Faculty%20of%20Engineering",
    "source_type": "official_page",
    "accessed_at": "2026-07-02",
    "notes": "Faculty landing/details page."
  },
  {
    "source_id": "UL-SRC-007",
    "title": "Faculty of Science | Lebanese University",
    "url": "https://ul.edu.lb/en/colleges-faculties-details/311/Faculty%20of%20Science",
    "source_type": "official_page",
    "accessed_at": "2026-07-02",
    "notes": "Faculty landing/details page for Science."
  },
  {
    "source_id": "UL-SRC-008",
    "title": "Applications open for M1 programs at the Faculty of Science for the academic year 2025-2026",
    "url": "https://ul.edu.lb/en/applications-open-m1-programs-faculty-science-academic-year-2025%E2%80%932026",
    "source_type": "official_page",
    "accessed_at": "2026-07-02",
    "notes": "Faculty of Science M1 professional master's application announcement."
  },
  {
    "source_id": "UL-SRC-009",
    "title": "Applications open for M2 programs at the Faculty of Science for the academic year 2025-2026",
    "url": "https://ul.edu.lb/en/applications-open-m2-programs-faculty-science-academic-year-2025%E2%80%932026",
    "source_type": "official_page",
    "accessed_at": "2026-07-02",
    "notes": "Faculty of Science M2 professional/research master's application announcement."
  },
  {
    "source_id": "UL-SRC-010",
    "title": "Registration for master's programs at the Faculty of Science for the academic year 2025-2026",
    "url": "https://ul.edu.lb/en/registration-masters-programs-faculty-science-academic-year-2025%E2%80%932026",
    "source_type": "official_page",
    "accessed_at": "2026-07-02",
    "notes": "Faculty of Science master's registration announcement."
  },
  {
    "source_id": "UL-SRC-011",
    "title": "The Faculty of Science is accepting applications for the M1P for the academic year 2026-2027",
    "url": "https://ul.edu.lb/en/faculty-science-accepting-applications-m1p-academic-year-2026%E2%80%932027",
    "source_type": "official_page",
    "accessed_at": "2026-07-02",
    "notes": "Current Faculty of Science M1 professional master's application announcement."
  },
  {
    "source_id": "UL-SRC-012",
    "title": "Master 1 Professional Programs Description & Curriculum",
    "url": "https://ul.edu.lb/files/ann/20240531-ULFS-MSDescription001.pdf",
    "source_type": "official_pdf",
    "accessed_at": "2026-07-02",
    "notes": "Faculty of Science Master 1 professional programs description/curriculum."
  },
  {
    "source_id": "UL-SRC-013",
    "title": "Master 2 Programs Description & Curriculum - Biochemistry & Biology",
    "url": "https://ul.edu.lb/files/ann/20240531-ULFS-MSDescription002Biochemistry%26Biology.pdf",
    "source_type": "official_pdf",
    "accessed_at": "2026-07-02",
    "notes": "Faculty of Science Master 2 descriptions for Biochemistry & Biology."
  },
  {
    "source_id": "UL-SRC-014",
    "title": "Master 2 Programs Description & Curriculum - Chemistry",
    "url": "https://ul.edu.lb/files/ann/20240531-ULFS-MSDescription003Chemistry.pdf",
    "source_type": "official_pdf",
    "accessed_at": "2026-07-02",
    "notes": "Faculty of Science Master 2 descriptions for Chemistry."
  },
  {
    "source_id": "UL-SRC-015",
    "title": "Master 2 Programs Description & Curriculum - Informatics",
    "url": "https://ul.edu.lb/files/ann/20240531-ULFS-MSDescription004Informatics.pdf",
    "source_type": "official_pdf",
    "accessed_at": "2026-07-02",
    "notes": "Faculty of Science Master 2 descriptions for Informatics."
  },
  {
    "source_id": "UL-SRC-016",
    "title": "Master 2 Programs Description & Curriculum - Mathematics & Statistics",
    "url": "https://ul.edu.lb/files/ann/20240531-ULFS-MSDescription005Mathematics%26Statistics.pdf",
    "source_type": "official_pdf",
    "accessed_at": "2026-07-02",
    "notes": "Faculty of Science Master 2 descriptions for Mathematics & Statistics."
  },
  {
    "source_id": "UL-SRC-017",
    "title": "Master 2 Programs Description & Curriculum - Physics & Electronics",
    "url": "https://ul.edu.lb/files/ann/20240531-ULFS-MSDescription006Physics%26Electronics.pdf",
    "source_type": "official_pdf",
    "accessed_at": "2026-07-02",
    "notes": "Faculty of Science Master 2 descriptions for Physics & Electronics."
  },
  {
    "source_id": "UL-SRC-018",
    "title": "Faculty of Public Health | Lebanese University",
    "url": "https://ul.edu.lb/en/colleges-faculties-details/300/Faculty%20of%20Public%20Health",
    "source_type": "official_page",
    "accessed_at": "2026-07-02",
    "notes": "Faculty landing/details page for Public Health."
  },
  {
    "source_id": "UL-SRC-019",
    "title": "Applications open for enrollment in research and professional master at the FPH for 2025-2026",
    "url": "https://ul.edu.lb/en/applications-open-enrollment-research-and-professional-master-fph-academic-year-2025%E2%80%932026",
    "source_type": "official_page",
    "accessed_at": "2026-07-02",
    "notes": "Faculty of Public Health master's application procedure and requirements announcement."
  },
  {
    "source_id": "UL-SRC-020",
    "title": "Faculty of Public Health Master Information PDF",
    "url": "https://ul.edu.lb/files/ann/20240709-ULFPH-EnExAn-Master-Info-Updated.pdf",
    "source_type": "official_pdf",
    "accessed_at": "2026-07-02",
    "notes": "Public Health master specializations, sections, and admission requirements."
  },
  {
    "source_id": "UL-SRC-021",
    "title": "Faculty of Medical Sciences | Lebanese University",
    "url": "https://ul.edu.lb/en/colleges-faculties-details/332/Faculty%20of%20Medical%20Science",
    "source_type": "official_page",
    "accessed_at": "2026-07-02",
    "notes": "Faculty landing/details page for Medical Sciences."
  },
  {
    "source_id": "UL-SRC-022",
    "title": "Faculty of Medical Sciences majors",
    "url": "https://ul.edu.lb/en/colleges-faculties-majors/332/Faculty-of-Medical-Sciences",
    "source_type": "official_page",
    "accessed_at": "2026-07-02",
    "notes": "Master of Science in Clinical Investigation discovered."
  },
  {
    "source_id": "UL-SRC-023",
    "title": "Neuroscience Research Center majors",
    "url": "https://ul.edu.lb/en/colleges-faculties-majors/335/Neuroscience-Research-Center",
    "source_type": "official_page",
    "accessed_at": "2026-07-02",
    "notes": "Master of Science in Neuroimaging, Neuropsychology, Neuroscience discovered."
  },
  {
    "source_id": "UL-SRC-024",
    "title": "Registration open for new students in master's programs at the NRC and MRC for 2025-2026",
    "url": "https://ul.edu.lb/en/registration-open-new-students-masters-programs-nrc-and-mrc-academic-year-2025%E2%80%932026",
    "source_type": "official_page",
    "accessed_at": "2026-07-02",
    "notes": "Registration for accepted master students in NRC and MRC; lists Neuroscience, Neuropsychology, Neuroimaging, Health Administration."
  },
  {
    "source_id": "UL-SRC-025",
    "title": "Admission policy to the Master's programs",
    "url": "https://www.ul.edu.lb/files/ann/07-0-AdmissionPolicy-Mastersprograms.pdf",
    "source_type": "official_pdf",
    "accessed_at": "2026-07-02",
    "notes": "Master admissions policy; includes neuroscience, neuroimaging, neuropsychology, health administration, medical ethics/bioethics and doctorate coordination note."
  },
  {
    "source_id": "UL-SRC-026",
    "title": "Medical Research Center Masters Programs",
    "url": "https://ul.edu.lb/files/ann/07-1-MRCMastersPrograms.pdf",
    "source_type": "official_pdf",
    "accessed_at": "2026-07-02",
    "notes": "Medical Research Center master programs including Clinical Investigation and Health Administration."
  },
  {
    "source_id": "UL-SRC-027",
    "title": "Faculty of Medical Sciences Master announcement PDF",
    "url": "https://lu.ul.edu.lb/files/ann/20250814-ULFMS-CaApAn-Master.pdf",
    "source_type": "official_pdf",
    "accessed_at": "2026-07-02",
    "notes": "Arabic/scan-like PDF for Medical Sciences master application; OCR/search text indicates Neuroscience, Neuropsychology, Neuroimaging, Health Administration."
  },
  {
    "source_id": "UL-SRC-028",
    "title": "Faculty of Law and Political and Administrative Sciences majors",
    "url": "https://ul.edu.lb/en/colleges-faculties-majors/286/Faculty-of-Law-and-Political-and-Administrative-Sciences",
    "source_type": "official_page",
    "accessed_at": "2026-07-02",
    "notes": "Professional/research master's programs in legal, diplomatic, litigation, business law, etc."
  },
  {
    "source_id": "UL-SRC-029",
    "title": "Faculty of Law and Political and Administrative Sciences details",
    "url": "https://ul.edu.lb/en/colleges-faculties-details/286/%D9%83%D9%84%D9%8A%D8%A9%20%D8%A7%D9%84%D8%AD%D9%82%D9%88%D9%82%20%D9%88%D8%A7%D9%84%D8%B9%D9%84%D9%88%D9%85%20%D8%A7%D9%84%D8%B3%D9%8A%D8%A7%D8%B3%D9%8A%D8%A9%20%D9%88%D8%A7%D9%84%D8%A5%D8%AF%D8%A7%D8%B1%D9%8A%D8%A9",
    "source_type": "official_page",
    "accessed_at": "2026-07-02",
    "notes": "Faculty details page states faculty grants bachelor and research master's degrees."
  },
  {
    "source_id": "UL-SRC-030",
    "title": "Applications are accepted for enrollment in the master's program/FLPAS for 2025-2026",
    "url": "https://ul.edu.lb/en/applications-are-accepted-enrollment-masters-programflpas-academic-year-2025%E2%80%932026",
    "source_type": "official_page",
    "accessed_at": "2026-07-02",
    "notes": "FLPAS master's admissions announcement."
  },
  {
    "source_id": "UL-SRC-031",
    "title": "Applications open for the joint master's in strategic studies with the Lebanese Army for 2025-2026",
    "url": "https://ul.edu.lb/en/applications-open-joint-master%E2%80%99s-strategic-studies-lebanese-army-2025%E2%80%932026",
    "source_type": "official_page",
    "accessed_at": "2026-07-02",
    "notes": "Joint master in strategic studies; needs later scope decision because partly external partner."
  },
  {
    "source_id": "UL-SRC-032",
    "title": "Faculty of Information majors",
    "url": "https://ul.edu.lb/en/colleges-faculties-majors/265/Faculty-of-Information",
    "source_type": "official_page",
    "accessed_at": "2026-07-02",
    "notes": "Professional master's programs discovered: Corporate Communications, Digital Media, Economic & Development Journalism."
  },
  {
    "source_id": "UL-SRC-033",
    "title": "Faculty of Information details",
    "url": "https://ul.edu.lb/en/colleges-faculties-details/265/Faculty-of-Information",
    "source_type": "official_page",
    "accessed_at": "2026-07-02",
    "notes": "Faculty landing/details page for Information."
  },
  {
    "source_id": "UL-SRC-034",
    "title": "Applications open for M1 and M2 at the Faculty of Information for 2025-2026",
    "url": "https://ul.edu.lb/en/applications-open-m1-and-m2-faculty-information-academic-year-2025%E2%80%932026",
    "source_type": "official_page",
    "accessed_at": "2026-07-02",
    "notes": "Faculty of Information M1/M2 application announcement."
  },
  {
    "source_id": "UL-SRC-035",
    "title": "Registration open for master's programs at the Faculty of Information for 2025-2026",
    "url": "https://ul.edu.lb/en/registration-open-masters-programs-faculty-information-academic-year-2025%E2%80%932026",
    "source_type": "official_page",
    "accessed_at": "2026-07-02",
    "notes": "Faculty of Information master's registration announcement."
  },
  {
    "source_id": "UL-SRC-036",
    "title": "Faculty of Technology majors",
    "url": "https://ul.edu.lb/en/colleges-faculties-majors/282/Faculty-of-Technology",
    "source_type": "official_page",
    "accessed_at": "2026-07-02",
    "notes": "Professional/research master's in geotechnics/environment, mechatronics/energy, communications systems, information systems."
  },
  {
    "source_id": "UL-SRC-037",
    "title": "Faculty of Technology tuition fees",
    "url": "https://iut.ul.edu.lb/tuition-fees.php",
    "source_type": "official_subdomain_page",
    "accessed_at": "2026-07-02",
    "notes": "Faculty of Technology local tuition fee page; older LBP amounts likely stale; flagged for manual verification."
  },
  {
    "source_id": "UL-SRC-038",
    "title": "Faculty of Technology Communication Systems Engineering syllabus",
    "url": "https://ft.ul.edu.lb/Syllabus/CSESyllabus_EN.pdf",
    "source_type": "official_pdf",
    "accessed_at": "2026-07-02",
    "notes": "Professional Master in Communication Systems Engineering syllabus."
  },
  {
    "source_id": "UL-SRC-039",
    "title": "Faculty of Technology Civil Engineering master curriculum",
    "url": "https://iut.ul.edu.lb/Syllabus/Coursecemaster_EN.pdf",
    "source_type": "official_pdf",
    "accessed_at": "2026-07-02",
    "notes": "Master Civil Engineering / Geotechnics curriculum; 120 ECTS found."
  },
  {
    "source_id": "UL-SRC-040",
    "title": "Faculty of Pharmacy majors",
    "url": "https://ul.edu.lb/en/colleges-faculties-majors/309/Facult%C3%A9%20de%20Pharmacie",
    "source_type": "official_page",
    "accessed_at": "2026-07-02",
    "notes": "Professional and research master's programs: clinical pharmacy, industrial cosmetology/dermopharmacy, pharmaceutical industry, pharmaceutical MBA, clinical pharmacy/pharmacoepidemiology, pharmaceutical biotechnology, pharmacology/toxicology."
  },
  {
    "source_id": "UL-SRC-041",
    "title": "Faculty of Dental Medicine majors",
    "url": "https://ul.edu.lb/en/colleges-faculties-majors/348/Faculty-of-Dental-Medicine",
    "source_type": "official_page",
    "accessed_at": "2026-07-02",
    "notes": "Master programs found: Orthodontics, Forensic Dentistry/Anthropology/Human Identification, Oral Surgery, Restorative Esthetic Dentistry and Endodontics."
  },
  {
    "source_id": "UL-SRC-042",
    "title": "Faculty of Economics & Business Administration details",
    "url": "https://ul.edu.lb/en/colleges-faculties-details/324/Faculty-of-Economics-%26-Business-Administration",
    "source_type": "official_page",
    "accessed_at": "2026-07-02",
    "notes": "Faculty details page notes bachelor and master's degree students in business administration science."
  },
  {
    "source_id": "UL-SRC-043",
    "title": "Faculty of Economics & Business Administration majors",
    "url": "https://ul.edu.lb/en/colleges-faculties-majors/324/Faculty-of-Economics-%26-Business-Administration",
    "source_type": "official_page",
    "accessed_at": "2026-07-02",
    "notes": "Majors page fetched but search snippet mainly bachelor entries; requires follow-up for master rows/pagination."
  },
  {
    "source_id": "UL-SRC-044",
    "title": "Faculty of Economics & Business Administration Master announcement PDF",
    "url": "https://lu.ul.edu.lb/files/ann/20250813-ULFEBA-EnExAn-Master.pdf",
    "source_type": "official_pdf",
    "accessed_at": "2026-07-02",
    "notes": "FEBA master application/entrance exam PDF; needs direct PDF inspection later due limited extracted snippet."
  },
  {
    "source_id": "UL-SRC-045",
    "title": "Faculty of Letters and Human Sciences details",
    "url": "https://ul.edu.lb/en/colleges-faculties-details/269/Faculty-of-Letters-and-Human-Sciences",
    "source_type": "official_page",
    "accessed_at": "2026-07-02",
    "notes": "Faculty details page states accreditation for 37 majors at bachelor and master's levels; centers include Languages/Translation and Language Sciences/Communication."
  },
  {
    "source_id": "UL-SRC-046",
    "title": "Faculty of Fine Arts and Architecture details",
    "url": "https://ul.edu.lb/en/colleges-faculties-details/336/Faculty-of-Fine-Arts-and-Architecture",
    "source_type": "official_page",
    "accessed_at": "2026-07-02",
    "notes": "Faculty details page; majors snippet only showed bachelor entries; follow-up needed for master entries."
  },
  {
    "source_id": "UL-SRC-047",
    "title": "Institute of Social Science professional master application 2025-2026",
    "url": "https://ul.edu.lb/en/applications-open-enrollment-professional-master-iss-academic-year-2025%E2%80%932026",
    "source_type": "official_page",
    "accessed_at": "2026-07-02",
    "notes": "Institute of Social Sciences professional master's application announcement."
  },
  {
    "source_id": "UL-SRC-048",
    "title": "Doctoral School of Literature, Humanities & Social Sciences details",
    "url": "https://ul.edu.lb/en/colleges-faculties-details/259/Doctoral-School-of-Literature%2C-Humanities-%26-Social-Sciences",
    "source_type": "official_page",
    "accessed_at": "2026-07-02",
    "notes": "Doctoral school page; grants Lebanese Doctoral Degree and HDR in literature/humanities/social sciences specializations."
  },
  {
    "source_id": "UL-SRC-049",
    "title": "Doctoral School of Literature, Humanities & Social Sciences deanship",
    "url": "https://ul.edu.lb/en/colleges-faculties--deanship/259/%C3%89cole-Doctorale-des-Lettres-et-des-Sciences-Humaines-et-Sociales",
    "source_type": "official_page",
    "accessed_at": "2026-07-02",
    "notes": "Deanship/welcome page for DSLHSS; states preparation for Lebanese PhD."
  },
  {
    "source_id": "UL-SRC-050",
    "title": "Doctoral School of Literature, Humanities & Social Sciences internal system",
    "url": "https://ul.edu.lb/en/colleges-faculties--guide--internalsystem/259/%D8%A7%D9%84%D9%85%D8%B9%D9%87%D8%AF-%D8%A7%D9%84%D8%B9%D8%A7%D9%84%D9%8A-%D9%84%D9%84%D8%AF%D9%83%D8%AA%D9%88%D8%B1%D8%A7%D9%87-%D9%81%D9%8A-%D8%A7%D9%84%D8%A2%D8%AF%D8%A7%D8%A8-%D9%88%D8%A7%D9%84%D8%B9%D9%84%D9%88%D9%85-%D8%A7%D9%84%D8%A5%D9%86%D8%B3%D8%A7%D9%86%D9%8A%D8%A9-%D9%88%D8%A7%D9%84%D8%A7%D8%AC%D8%AA%D9%85%D8%A7%D8%B9%D9%8A%D8%A9",
    "source_type": "official_page",
    "accessed_at": "2026-07-02",
    "notes": "Internal system/bylaws page; states PhD/HDR granting specializations."
  },
  {
    "source_id": "UL-SRC-051",
    "title": "Applications for doctoral preparation start for academic year 2025-2026",
    "url": "https://ul.edu.lb/en/applications-doctoral-preparation-start-academic-year-2025-2026",
    "source_type": "official_page",
    "accessed_at": "2026-07-02",
    "notes": "DSLHSS PhD application announcement; eligible faculties/institutes include Letters, Pedagogy, Information, Fine Arts & Architecture, Social Sciences."
  },
  {
    "source_id": "UL-SRC-052",
    "title": "Doctoral School of Law, Political, Administrative & Economic Sciences details",
    "url": "https://ul.edu.lb/en/colleges-faculties-details/261/Doctoral-School-of-Law%2C-Political%2C-Administrative-%26-Economic-Sciences",
    "source_type": "official_page",
    "accessed_at": "2026-07-02",
    "notes": "Doctoral school page; prepares students for Lebanese PhD and provides grants for PhD students."
  },
  {
    "source_id": "UL-SRC-053",
    "title": "Doctoral School of Law, Political, Administrative & Economic Sciences internal system",
    "url": "https://ul.edu.lb/en/colleges-faculties--guide--internalsystem/261/Doctoral-School-of-Law%2C-Political%2C-Administrative-and-Economic-Sciences",
    "source_type": "official_page",
    "accessed_at": "2026-07-02",
    "notes": "Internal system/bylaws page; states preparation for Lebanese PhD in law, political, administrative and economic sciences."
  },
  {
    "source_id": "UL-SRC-054",
    "title": "Applications are now accepted for PhD programs at the DSLPAES for 2025-2026",
    "url": "https://ul.edu.lb/en/applications-are-now-accepted-phd-programs-dslpaes-academic-year-2025%E2%80%932026",
    "source_type": "official_page",
    "accessed_at": "2026-07-02",
    "notes": "DSLPAES doctoral degree application announcement with dates and submission location."
  },
  {
    "source_id": "UL-SRC-055",
    "title": "DSLPAES accepting applications for PhD degrees in Business Administration, Economics or Tourism",
    "url": "https://ul.edu.lb/en/dslpaes-accepting-applications-phd-degrees-business-administration-economics-or-tourism-lu-students",
    "source_type": "official_page",
    "accessed_at": "2026-07-02",
    "notes": "DSLPAES PhD announcement for Business Administration, Economics, Tourism."
  },
  {
    "source_id": "UL-SRC-056",
    "title": "Doctoral School of Science & Technology details",
    "url": "https://ul.edu.lb/en/colleges-faculties-details/263/Doctoral-School-of-Science-%26-Technology",
    "source_type": "official_page",
    "accessed_at": "2026-07-02",
    "notes": "DSST page; prepares students for Lebanese PhD in cooperation with international universities and grants HDR."
  },
  {
    "source_id": "UL-SRC-057",
    "title": "Doctoral School of Science & Technology deanship",
    "url": "https://ul.edu.lb/en/colleges-faculties--deanship/263/Doctoral-School-of-Science-%26-Technology",
    "source_type": "official_page",
    "accessed_at": "2026-07-02",
    "notes": "DSST welcome/deanship page; states it prepares students for Lebanese PhD in science and technology."
  },
  {
    "source_id": "UL-SRC-058",
    "title": "Doctoral School of Science & Technology internal system",
    "url": "https://ul.edu.lb/en/colleges-faculties--guide--internalsystem/263/%C3%89cole-Doctorale-des-Sciences-et-Technologies",
    "source_type": "official_page",
    "accessed_at": "2026-07-02",
    "notes": "Internal system page; states PhD equals 180 ECTS with 140 allocated to research/dissertation."
  },
  {
    "source_id": "UL-SRC-059",
    "title": "Foreign Students | Lebanese University",
    "url": "https://ul.edu.lb/en/new-students/foreign-students",
    "source_type": "official_page",
    "accessed_at": "2026-07-02",
    "notes": "International student admission/fees page; includes master/postgraduate and PhD fee amounts and required procedures."
  },
  {
    "source_id": "UL-SRC-060",
    "title": "Private University Students | Lebanese University",
    "url": "https://ul.edu.lb/en/new-students/private-university-students",
    "source_type": "official_page",
    "accessed_at": "2026-07-02",
    "notes": "Private university transfer/student registration page; includes master/postgraduate and PhD fee amounts."
  },
  {
    "source_id": "UL-SRC-061",
    "title": "Lebanese University legacy registration page",
    "url": "https://lu.ul.edu.lb/students/registration.aspx",
    "source_type": "official_subdomain_page",
    "accessed_at": "2026-07-02",
    "notes": "Legacy registration page; older fee amounts for Master & PhD degrees in all faculties and doctoral schools; flagged as stale against current admission fee pages."
  },
  {
    "source_id": "UL-SRC-062",
    "title": "Academic Calendar for the Academic Year 2025-2026",
    "url": "https://lu.ul.edu.lb/files/ann/20250611-LU-AcademicYear2025-2026.pdf",
    "source_type": "official_pdf",
    "accessed_at": "2026-07-02",
    "notes": "Academic calendar PDF linked from homepage."
  }
]$UL_SOURCES$) AS x(
        source_id TEXT,
        title TEXT,
        url TEXT,
        source_type TEXT,
        accessed_at DATE,
        notes TEXT
    );

    INSERT INTO source (university_id, title, url, source_type, accessed_at)
    SELECT v_university_id, title, url, source_type, accessed_at
    FROM ul_source_seed
    ON CONFLICT (university_id, url) DO UPDATE SET
        title = EXCLUDED.title,
        source_type = EXCLUDED.source_type,
        accessed_at = EXCLUDED.accessed_at,
        updated_at = NOW();

    CREATE TEMP TABLE ul_faculty_seed (
        name TEXT PRIMARY KEY,
        short_name TEXT,
        faculty_type TEXT NOT NULL,
        official_url TEXT,
        notes TEXT
    ) ON COMMIT DROP;

    INSERT INTO ul_faculty_seed (name, short_name, faculty_type, official_url, notes)
    SELECT name, short_name, faculty_type, official_url, notes
    FROM jsonb_to_recordset($UL_FACULTIES$[
  {
    "name": "Faculty of Engineering",
    "short_name": null,
    "faculty_type": "FACULTY",
    "official_url": null,
    "notes": null
  },
  {
    "name": "Faculty of Science",
    "short_name": null,
    "faculty_type": "FACULTY",
    "official_url": null,
    "notes": null
  },
  {
    "name": "Faculty of Public Health",
    "short_name": null,
    "faculty_type": "FACULTY",
    "official_url": null,
    "notes": null
  },
  {
    "name": "Faculty of Medical Sciences",
    "short_name": null,
    "faculty_type": "FACULTY",
    "official_url": null,
    "notes": null
  },
  {
    "name": "Neuroscience Research Center",
    "short_name": null,
    "faculty_type": "CENTER",
    "official_url": null,
    "notes": null
  },
  {
    "name": "Medical Research Center",
    "short_name": null,
    "faculty_type": "CENTER",
    "official_url": null,
    "notes": null
  },
  {
    "name": "Faculty of Law and Political and Administrative Sciences",
    "short_name": null,
    "faculty_type": "FACULTY",
    "official_url": null,
    "notes": null
  },
  {
    "name": "Faculty of Information",
    "short_name": null,
    "faculty_type": "FACULTY",
    "official_url": null,
    "notes": null
  },
  {
    "name": "Faculty of Technology",
    "short_name": null,
    "faculty_type": "FACULTY",
    "official_url": null,
    "notes": null
  },
  {
    "name": "Faculty of Pharmacy",
    "short_name": null,
    "faculty_type": "FACULTY",
    "official_url": null,
    "notes": null
  },
  {
    "name": "Faculty of Dental Medicine",
    "short_name": null,
    "faculty_type": "FACULTY",
    "official_url": null,
    "notes": null
  },
  {
    "name": "Faculty of Economics & Business Administration",
    "short_name": null,
    "faculty_type": "FACULTY",
    "official_url": null,
    "notes": null
  },
  {
    "name": "Faculty of Letters and Human Sciences",
    "short_name": null,
    "faculty_type": "FACULTY",
    "official_url": null,
    "notes": null
  },
  {
    "name": "Institute of Social Sciences",
    "short_name": null,
    "faculty_type": "INSTITUTE",
    "official_url": null,
    "notes": null
  },
  {
    "name": "Doctoral School of Literature, Humanities & Social Sciences",
    "short_name": null,
    "faculty_type": "SCHOOL",
    "official_url": null,
    "notes": null
  },
  {
    "name": "Doctoral School of Law, Political, Administrative & Economic Sciences",
    "short_name": null,
    "faculty_type": "SCHOOL",
    "official_url": null,
    "notes": null
  },
  {
    "name": "Doctoral School of Science & Technology",
    "short_name": null,
    "faculty_type": "SCHOOL",
    "official_url": null,
    "notes": null
  }
]$UL_FACULTIES$) AS x(
        name TEXT,
        short_name TEXT,
        faculty_type TEXT,
        official_url TEXT,
        notes TEXT
    );

    INSERT INTO university_faculty (university_id, name, short_name, faculty_type, official_url, notes)
    SELECT v_university_id, name, short_name, faculty_type, official_url, notes
    FROM ul_faculty_seed
    ON CONFLICT (university_id, name) DO UPDATE SET
        short_name = EXCLUDED.short_name,
        faculty_type = EXCLUDED.faculty_type,
        official_url = EXCLUDED.official_url,
        notes = EXCLUDED.notes,
        updated_at = NOW();

    CREATE TEMP TABLE ul_program_seed (
        id TEXT PRIMARY KEY,
        faculty TEXT NOT NULL,
        degree_type TEXT NOT NULL,
        major_category TEXT,
        major TEXT,
        official_degree_name TEXT,
        thesis_or_non_thesis TEXT,
        credits INTEGER,
        duration_value NUMERIC(10,2),
        duration_unit TEXT,
        program_description TEXT,
        official_program_url TEXT NOT NULL,
        notes TEXT,
        delivery_mode TEXT,
        source_ids JSONB NOT NULL,
        concentrations_or_tracks JSONB
    ) ON COMMIT DROP;

    INSERT INTO ul_program_seed (id, faculty, degree_type, major_category, major, official_degree_name, thesis_or_non_thesis, credits, duration_value, duration_unit, program_description, official_program_url, notes, delivery_mode, source_ids, concentrations_or_tracks)
    SELECT id, faculty, degree_type, major_category, major, official_degree_name, thesis_or_non_thesis, credits, duration_value, duration_unit, program_description, official_program_url, notes, delivery_mode, source_ids::jsonb, concentrations_or_tracks::jsonb
    FROM jsonb_to_recordset($UL_PROGRAMS$[
  {
    "id": "lu-fe-master-hydrosciences",
    "faculty": "Faculty of Engineering",
    "degree_type": "MASTER",
    "major_category": null,
    "major": "Master in Hydrosciences",
    "official_degree_name": "Master in Hydrosciences",
    "thesis_or_non_thesis": "THESIS_OR_NON_THESIS",
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "program_description": "Research/professional master listed on the Faculty of Engineering graduate programs page.",
    "official_program_url": "https://ul.edu.lb/en/colleges-faculties-majors/343/Faculty-of-Engineering",
    "notes": "Faculty of Engineering graduate hub lists Hydrosciences among the master's offerings. University-level graduate enrollment fee is centralized in university.json. UL source describes this cluster as research/professional or professional/research masters.",
    "delivery_mode": null,
    "source_ids": [
      "UL-SRC-005",
      "UL-SRC-006"
    ],
    "concentrations_or_tracks": null
  },
  {
    "id": "lu-fe-master-mechanics",
    "faculty": "Faculty of Engineering",
    "degree_type": "MASTER",
    "major_category": null,
    "major": "Master in Mechanics",
    "official_degree_name": "Master in Mechanics",
    "thesis_or_non_thesis": "THESIS_OR_NON_THESIS",
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "program_description": "Research/professional master listed on the Faculty of Engineering graduate programs page.",
    "official_program_url": "https://ul.edu.lb/en/colleges-faculties-majors/343/Faculty-of-Engineering",
    "notes": "Faculty of Engineering graduate hub lists Mechanics among the master's offerings. University-level graduate enrollment fee is centralized in university.json. UL source describes this cluster as research/professional or professional/research masters.",
    "delivery_mode": null,
    "source_ids": [
      "UL-SRC-005",
      "UL-SRC-006"
    ],
    "concentrations_or_tracks": null
  },
  {
    "id": "lu-fe-master-natural-risks",
    "faculty": "Faculty of Engineering",
    "degree_type": "MASTER",
    "major_category": null,
    "major": "Master in Natural Risks",
    "official_degree_name": "Master in Natural Risks",
    "thesis_or_non_thesis": "THESIS_OR_NON_THESIS",
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "program_description": "Research/professional master listed on the Faculty of Engineering graduate programs page.",
    "official_program_url": "https://ul.edu.lb/en/colleges-faculties-majors/343/Faculty-of-Engineering",
    "notes": "Faculty of Engineering graduate hub lists Natural Risks among the master's offerings. University-level graduate enrollment fee is centralized in university.json. UL source describes this cluster as research/professional or professional/research masters.",
    "delivery_mode": null,
    "source_ids": [
      "UL-SRC-005",
      "UL-SRC-006"
    ],
    "concentrations_or_tracks": null
  },
  {
    "id": "lu-fe-master-renewable-energies",
    "faculty": "Faculty of Engineering",
    "degree_type": "MASTER",
    "major_category": null,
    "major": "Master in Renewable Energies",
    "official_degree_name": "Master in Renewable Energies",
    "thesis_or_non_thesis": "THESIS_OR_NON_THESIS",
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "program_description": "Research/professional master listed on the Faculty of Engineering graduate programs page.",
    "official_program_url": "https://ul.edu.lb/en/colleges-faculties-majors/343/Faculty-of-Engineering",
    "notes": "Faculty of Engineering graduate hub lists Renewable Energies among the master's offerings. University-level graduate enrollment fee is centralized in university.json. UL source describes this cluster as research/professional or professional/research masters.",
    "delivery_mode": null,
    "source_ids": [
      "UL-SRC-005",
      "UL-SRC-006"
    ],
    "concentrations_or_tracks": null
  },
  {
    "id": "lu-fe-master-robotics",
    "faculty": "Faculty of Engineering",
    "degree_type": "MASTER",
    "major_category": null,
    "major": "Master in Robotics",
    "official_degree_name": "Master in Robotics",
    "thesis_or_non_thesis": "THESIS_OR_NON_THESIS",
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "program_description": "Research/professional master listed on the Faculty of Engineering graduate programs page.",
    "official_program_url": "https://ul.edu.lb/en/colleges-faculties-majors/343/Faculty-of-Engineering",
    "notes": "Faculty of Engineering graduate hub lists Robotics among the master's offerings. University-level graduate enrollment fee is centralized in university.json. UL source describes this cluster as research/professional or professional/research masters.",
    "delivery_mode": null,
    "source_ids": [
      "UL-SRC-005",
      "UL-SRC-006"
    ],
    "concentrations_or_tracks": null
  },
  {
    "id": "lu-fe-master-tcmis",
    "faculty": "Faculty of Engineering",
    "degree_type": "MASTER",
    "major_category": null,
    "major": "Master in TCMIS",
    "official_degree_name": "Master in TCMIS",
    "thesis_or_non_thesis": "THESIS_OR_NON_THESIS",
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "program_description": "Research/professional master listed on the Faculty of Engineering graduate programs page.",
    "official_program_url": "https://ul.edu.lb/en/colleges-faculties-majors/343/Faculty-of-Engineering",
    "notes": "Faculty of Engineering graduate hub lists TCMIS among the master's offerings. University-level graduate enrollment fee is centralized in university.json. UL source describes this cluster as research/professional or professional/research masters.",
    "delivery_mode": null,
    "source_ids": [
      "UL-SRC-005",
      "UL-SRC-006"
    ],
    "concentrations_or_tracks": null
  },
  {
    "id": "lu-fe-master-telecommunications",
    "faculty": "Faculty of Engineering",
    "degree_type": "MASTER",
    "major_category": null,
    "major": "Master in Telecommunications",
    "official_degree_name": "Master in Telecommunications",
    "thesis_or_non_thesis": "THESIS_OR_NON_THESIS",
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "program_description": "Research/professional master listed on the Faculty of Engineering graduate programs page.",
    "official_program_url": "https://ul.edu.lb/en/colleges-faculties-majors/343/Faculty-of-Engineering",
    "notes": "Faculty of Engineering graduate hub lists Telecommunications among the master's offerings. University-level graduate enrollment fee is centralized in university.json. UL source describes this cluster as research/professional or professional/research masters.",
    "delivery_mode": null,
    "source_ids": [
      "UL-SRC-005",
      "UL-SRC-006"
    ],
    "concentrations_or_tracks": null
  },
  {
    "id": "lu-fe-master-civil-engineering",
    "faculty": "Faculty of Engineering",
    "degree_type": "MASTER",
    "major_category": null,
    "major": "Master in Civil Engineering",
    "official_degree_name": "Master in Civil Engineering",
    "thesis_or_non_thesis": "THESIS_OR_NON_THESIS",
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "program_description": "Research/professional master listed on the Faculty of Engineering graduate programs page.",
    "official_program_url": "https://ul.edu.lb/en/colleges-faculties-majors/343/Faculty-of-Engineering",
    "notes": "Faculty of Engineering graduate hub lists Civil Engineering among the master's offerings. University-level graduate enrollment fee is centralized in university.json. UL source describes this cluster as research/professional or professional/research masters.",
    "delivery_mode": null,
    "source_ids": [
      "UL-SRC-005",
      "UL-SRC-006"
    ],
    "concentrations_or_tracks": null
  },
  {
    "id": "lu-fe-master-htte",
    "faculty": "Faculty of Engineering",
    "degree_type": "MASTER",
    "major_category": null,
    "major": "Master in HTTE",
    "official_degree_name": "Master in HTTE",
    "thesis_or_non_thesis": "THESIS_OR_NON_THESIS",
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "program_description": "Research/professional master listed on the Faculty of Engineering graduate programs page.",
    "official_program_url": "https://ul.edu.lb/en/colleges-faculties-majors/343/Faculty-of-Engineering",
    "notes": "Faculty of Engineering graduate hub lists HTTE among the master's offerings. University-level graduate enrollment fee is centralized in university.json. UL source describes this cluster as research/professional or professional/research masters.",
    "delivery_mode": null,
    "source_ids": [
      "UL-SRC-005",
      "UL-SRC-006"
    ],
    "concentrations_or_tracks": null
  },
  {
    "id": "lu-science-master-1-professional-programs",
    "faculty": "Faculty of Science",
    "degree_type": "MASTER",
    "major_category": null,
    "major": "Master 1 Professional Programs",
    "official_degree_name": "Master 1 Professional Programs",
    "thesis_or_non_thesis": null,
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "program_description": "Faculty of Science Master 1 professional program description and curriculum PDF.",
    "official_program_url": "https://ul.edu.lb/files/ann/20240531-ULFS-MSDescription001.pdf",
    "notes": "Official Faculty of Science M1 professional programs document. University-level graduate enrollment fee is centralized in university.json.",
    "delivery_mode": null,
    "source_ids": [
      "UL-SRC-008",
      "UL-SRC-011",
      "UL-SRC-012"
    ],
    "concentrations_or_tracks": null
  },
  {
    "id": "lu-science-master-2-biochemistry-biology",
    "faculty": "Faculty of Science",
    "degree_type": "MASTER",
    "major_category": null,
    "major": "Master 2 Biochemistry & Biology",
    "official_degree_name": "Master 2 Biochemistry & Biology",
    "thesis_or_non_thesis": null,
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "program_description": "Faculty of Science Master 2 Biochemistry & Biology description and curriculum PDF.",
    "official_program_url": "https://ul.edu.lb/files/ann/20240531-ULFS-MSDescription002Biochemistry%26Biology.pdf",
    "notes": "Official Faculty of Science M2 document for Biochemistry & Biology. University-level graduate enrollment fee is centralized in university.json.",
    "delivery_mode": null,
    "source_ids": [
      "UL-SRC-009",
      "UL-SRC-010",
      "UL-SRC-013"
    ],
    "concentrations_or_tracks": null
  },
  {
    "id": "lu-science-master-2-chemistry",
    "faculty": "Faculty of Science",
    "degree_type": "MASTER",
    "major_category": null,
    "major": "Master 2 Chemistry",
    "official_degree_name": "Master 2 Chemistry",
    "thesis_or_non_thesis": null,
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "program_description": "Faculty of Science Master 2 Chemistry description and curriculum PDF.",
    "official_program_url": "https://ul.edu.lb/files/ann/20240531-ULFS-MSDescription003Chemistry.pdf",
    "notes": "Official Faculty of Science M2 document for Chemistry. University-level graduate enrollment fee is centralized in university.json.",
    "delivery_mode": null,
    "source_ids": [
      "UL-SRC-009",
      "UL-SRC-010",
      "UL-SRC-014"
    ],
    "concentrations_or_tracks": null
  },
  {
    "id": "lu-science-master-2-informatics",
    "faculty": "Faculty of Science",
    "degree_type": "MASTER",
    "major_category": null,
    "major": "Master 2 Informatics",
    "official_degree_name": "Master 2 Informatics",
    "thesis_or_non_thesis": null,
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "program_description": "Faculty of Science Master 2 Informatics description and curriculum PDF.",
    "official_program_url": "https://ul.edu.lb/files/ann/20240531-ULFS-MSDescription004Informatics.pdf",
    "notes": "Official Faculty of Science M2 document for Informatics. University-level graduate enrollment fee is centralized in university.json.",
    "delivery_mode": null,
    "source_ids": [
      "UL-SRC-009",
      "UL-SRC-010",
      "UL-SRC-015"
    ],
    "concentrations_or_tracks": null
  },
  {
    "id": "lu-science-master-2-mathematics-statistics",
    "faculty": "Faculty of Science",
    "degree_type": "MASTER",
    "major_category": null,
    "major": "Master 2 Mathematics & Statistics",
    "official_degree_name": "Master 2 Mathematics & Statistics",
    "thesis_or_non_thesis": null,
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "program_description": "Faculty of Science Master 2 Mathematics & Statistics description and curriculum PDF.",
    "official_program_url": "https://ul.edu.lb/files/ann/20240531-ULFS-MSDescription005Mathematics%26Statistics.pdf",
    "notes": "Official Faculty of Science M2 document for Mathematics & Statistics. University-level graduate enrollment fee is centralized in university.json.",
    "delivery_mode": null,
    "source_ids": [
      "UL-SRC-009",
      "UL-SRC-010",
      "UL-SRC-016"
    ],
    "concentrations_or_tracks": null
  },
  {
    "id": "lu-science-master-2-physics-electronics",
    "faculty": "Faculty of Science",
    "degree_type": "MASTER",
    "major_category": null,
    "major": "Master 2 Physics & Electronics",
    "official_degree_name": "Master 2 Physics & Electronics",
    "thesis_or_non_thesis": null,
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "program_description": "Faculty of Science Master 2 Physics & Electronics description and curriculum PDF.",
    "official_program_url": "https://ul.edu.lb/files/ann/20240531-ULFS-MSDescription006Physics%26Electronics.pdf",
    "notes": "Official Faculty of Science M2 document for Physics & Electronics. University-level graduate enrollment fee is centralized in university.json.",
    "delivery_mode": null,
    "source_ids": [
      "UL-SRC-009",
      "UL-SRC-010",
      "UL-SRC-017"
    ],
    "concentrations_or_tracks": null
  },
  {
    "id": "lu-fph-master-programs",
    "faculty": "Faculty of Public Health",
    "degree_type": "MASTER",
    "major_category": null,
    "major": "Faculty of Public Health Master's Programs",
    "official_degree_name": "Faculty of Public Health Master's Programs",
    "thesis_or_non_thesis": "THESIS_OR_NON_THESIS",
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "program_description": "Faculty-level graduate master offering at the Faculty of Public Health.",
    "official_program_url": "https://ul.edu.lb/en/colleges-faculties-details/300/Faculty%20of%20Public%20Health",
    "notes": "Public Health master information and admissions are published through the faculty hub and PDF. University-level graduate enrollment fee is centralized in university.json. UL source describes this cluster as research/professional or professional/research masters.",
    "delivery_mode": null,
    "source_ids": [
      "UL-SRC-018",
      "UL-SRC-019",
      "UL-SRC-020"
    ],
    "concentrations_or_tracks": [
      "Research Master",
      "Professional Master"
    ]
  },
  {
    "id": "lu-fms-master-clinical-investigation",
    "faculty": "Faculty of Medical Sciences",
    "degree_type": "MASTER",
    "major_category": null,
    "major": "Master of Science in Clinical Investigation",
    "official_degree_name": "Master of Science in Clinical Investigation",
    "thesis_or_non_thesis": null,
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "program_description": "Master of Science in Clinical Investigation listed on the Faculty of Medical Sciences majors page.",
    "official_program_url": "https://ul.edu.lb/en/colleges-faculties-majors/332/Faculty-of-Medical-Sciences",
    "notes": "Clinical Investigation is the explicit master title surfaced for the Faculty of Medical Sciences. University-level graduate enrollment fee is centralized in university.json.",
    "delivery_mode": null,
    "source_ids": [
      "UL-SRC-021",
      "UL-SRC-022",
      "UL-SRC-025",
      "UL-SRC-026",
      "UL-SRC-027"
    ],
    "concentrations_or_tracks": null
  },
  {
    "id": "lu-nrc-master-neuroimaging",
    "faculty": "Neuroscience Research Center",
    "degree_type": "MASTER",
    "major_category": null,
    "major": "Master of Science in Neuroimaging",
    "official_degree_name": "Master of Science in Neuroimaging",
    "thesis_or_non_thesis": null,
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "program_description": "Official UL graduate program listed by the Neuroscience Research Center graduate materials.",
    "official_program_url": "https://ul.edu.lb/en/colleges-faculties-majors/335/Neuroscience-Research-Center",
    "notes": "Official UL sources surface Neuroimaging as a distinct graduate title within the Neuroscience Research Center cluster. University-level graduate enrollment fee is centralized in university.json.",
    "delivery_mode": null,
    "source_ids": [
      "UL-SRC-023",
      "UL-SRC-024",
      "UL-SRC-025",
      "UL-SRC-027"
    ],
    "concentrations_or_tracks": null
  },
  {
    "id": "lu-nrc-master-neuropsychology",
    "faculty": "Neuroscience Research Center",
    "degree_type": "MASTER",
    "major_category": null,
    "major": "Master of Science in Neuropsychology",
    "official_degree_name": "Master of Science in Neuropsychology",
    "thesis_or_non_thesis": null,
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "program_description": "Official UL graduate program listed by the Neuroscience Research Center graduate materials.",
    "official_program_url": "https://ul.edu.lb/en/colleges-faculties-majors/335/Neuroscience-Research-Center",
    "notes": "Official UL sources surface Neuropsychology as a distinct graduate title within the Neuroscience Research Center cluster. University-level graduate enrollment fee is centralized in university.json.",
    "delivery_mode": null,
    "source_ids": [
      "UL-SRC-023",
      "UL-SRC-024",
      "UL-SRC-025",
      "UL-SRC-027"
    ],
    "concentrations_or_tracks": null
  },
  {
    "id": "lu-nrc-master-neuroscience",
    "faculty": "Neuroscience Research Center",
    "degree_type": "MASTER",
    "major_category": null,
    "major": "Master of Science in Neuroscience",
    "official_degree_name": "Master of Science in Neuroscience",
    "thesis_or_non_thesis": null,
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "program_description": "Official UL graduate program listed by the Neuroscience Research Center graduate materials.",
    "official_program_url": "https://ul.edu.lb/en/colleges-faculties-majors/335/Neuroscience-Research-Center",
    "notes": "Official UL sources surface Neuroscience as a distinct graduate title within the Neuroscience Research Center cluster. University-level graduate enrollment fee is centralized in university.json.",
    "delivery_mode": null,
    "source_ids": [
      "UL-SRC-023",
      "UL-SRC-024",
      "UL-SRC-025",
      "UL-SRC-027"
    ],
    "concentrations_or_tracks": null
  },
  {
    "id": "lu-mrc-master-health-administration",
    "faculty": "Medical Research Center",
    "degree_type": "MASTER",
    "major_category": null,
    "major": "Master in Health Administration",
    "official_degree_name": "Master in Health Administration",
    "thesis_or_non_thesis": null,
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "program_description": "Health Administration master program surfaced through the MRC master's materials and registration notice.",
    "official_program_url": "https://ul.edu.lb/en/registration-open-new-students-masters-programs-nrc-and-mrc-academic-year-2025%E2%80%932026",
    "notes": "Medical Research Center master materials explicitly mention Health Administration. University-level graduate enrollment fee is centralized in university.json.",
    "delivery_mode": null,
    "source_ids": [
      "UL-SRC-024",
      "UL-SRC-025",
      "UL-SRC-026",
      "UL-SRC-027"
    ],
    "concentrations_or_tracks": null
  },
  {
    "id": "lu-law-master-programs",
    "faculty": "Faculty of Law and Political and Administrative Sciences",
    "degree_type": "MASTER",
    "major_category": null,
    "major": "Faculty of Law and Political and Administrative Sciences Master's Programs",
    "official_degree_name": "Faculty of Law and Political and Administrative Sciences Master's Programs",
    "thesis_or_non_thesis": "THESIS_OR_NON_THESIS",
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "program_description": "Faculty-level master's offering listing professional and research tracks in law, diplomacy, litigation, business law, and strategic studies.",
    "official_program_url": "https://ul.edu.lb/en/colleges-faculties-majors/286/Faculty-of-Law-and-Political-and-Administrative-Sciences",
    "notes": "UL publishes this area through the faculty hub and the master's program announcements, including a joint master's in strategic studies. University-level graduate enrollment fee is centralized in university.json. UL source describes this cluster as research/professional or professional/research masters.",
    "delivery_mode": null,
    "source_ids": [
      "UL-SRC-028",
      "UL-SRC-029",
      "UL-SRC-030",
      "UL-SRC-031"
    ],
    "concentrations_or_tracks": [
      "Legal Studies",
      "Diplomatic Studies",
      "Litigation Studies",
      "Business Law",
      "Strategic Studies"
    ]
  },
  {
    "id": "lu-information-master-corporate-communications",
    "faculty": "Faculty of Information",
    "degree_type": "MASTER",
    "major_category": null,
    "major": "Master in Corporate Communications",
    "official_degree_name": "Master in Corporate Communications",
    "thesis_or_non_thesis": "NON_THESIS",
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "program_description": "Professional master's program in Corporate Communications listed by the Faculty of Information.",
    "official_program_url": "https://ul.edu.lb/en/colleges-faculties-majors/265/Faculty-of-Information",
    "notes": "Faculty of Information graduate hub surfaces Corporate Communications as one of the master's offerings. University-level graduate enrollment fee is centralized in university.json. UL source describes this as a professional master.",
    "delivery_mode": null,
    "source_ids": [
      "UL-SRC-032",
      "UL-SRC-034",
      "UL-SRC-035"
    ],
    "concentrations_or_tracks": null
  },
  {
    "id": "lu-information-master-digital-media",
    "faculty": "Faculty of Information",
    "degree_type": "MASTER",
    "major_category": null,
    "major": "Master in Digital Media",
    "official_degree_name": "Master in Digital Media",
    "thesis_or_non_thesis": "NON_THESIS",
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "program_description": "Professional master's program in Digital Media listed by the Faculty of Information.",
    "official_program_url": "https://ul.edu.lb/en/colleges-faculties-majors/265/Faculty-of-Information",
    "notes": "Faculty of Information graduate hub surfaces Digital Media as one of the master's offerings. University-level graduate enrollment fee is centralized in university.json. UL source describes this as a professional master.",
    "delivery_mode": null,
    "source_ids": [
      "UL-SRC-032",
      "UL-SRC-034",
      "UL-SRC-035"
    ],
    "concentrations_or_tracks": null
  },
  {
    "id": "lu-information-master-economic-development-journalism",
    "faculty": "Faculty of Information",
    "degree_type": "MASTER",
    "major_category": null,
    "major": "Master in Economic & Development Journalism",
    "official_degree_name": "Master in Economic & Development Journalism",
    "thesis_or_non_thesis": "NON_THESIS",
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "program_description": "Professional master's program in Economic & Development Journalism listed by the Faculty of Information.",
    "official_program_url": "https://ul.edu.lb/en/colleges-faculties-majors/265/Faculty-of-Information",
    "notes": "Faculty of Information graduate hub surfaces Economic & Development Journalism as one of the master's offerings. University-level graduate enrollment fee is centralized in university.json. UL source describes this as a professional master.",
    "delivery_mode": null,
    "source_ids": [
      "UL-SRC-032",
      "UL-SRC-034",
      "UL-SRC-035"
    ],
    "concentrations_or_tracks": null
  },
  {
    "id": "lu-technology-master-geotechnics-environment",
    "faculty": "Faculty of Technology",
    "degree_type": "MASTER",
    "major_category": null,
    "major": "Master in Geotechnics and Environment",
    "official_degree_name": "Master in Geotechnics and Environment",
    "thesis_or_non_thesis": "THESIS_OR_NON_THESIS",
    "credits": 120,
    "duration_value": null,
    "duration_unit": null,
    "program_description": "Official UL graduate program listed by the Faculty of Technology graduate materials.",
    "official_program_url": "https://ul.edu.lb/en/colleges-faculties-majors/282/Faculty-of-Technology",
    "notes": "Split from the grouped Faculty of Technology inventory row. University-level graduate enrollment fee is centralized in university.json. UL source describes this cluster as research/professional or professional/research masters. UL curriculum PDF for geotechnics/environment reports 120 ECTS.",
    "delivery_mode": null,
    "source_ids": [
      "UL-SRC-036",
      "UL-SRC-039"
    ],
    "concentrations_or_tracks": null
  },
  {
    "id": "lu-technology-master-mechatronics-energy",
    "faculty": "Faculty of Technology",
    "degree_type": "MASTER",
    "major_category": null,
    "major": "Master in Mechatronics and Energy",
    "official_degree_name": "Master in Mechatronics and Energy",
    "thesis_or_non_thesis": "THESIS_OR_NON_THESIS",
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "program_description": "Official UL graduate program listed by the Faculty of Technology graduate materials.",
    "official_program_url": "https://ul.edu.lb/en/colleges-faculties-majors/282/Faculty-of-Technology",
    "notes": "Split from the grouped Faculty of Technology inventory row. University-level graduate enrollment fee is centralized in university.json. UL source describes this cluster as research/professional or professional/research masters.",
    "delivery_mode": null,
    "source_ids": [
      "UL-SRC-036"
    ],
    "concentrations_or_tracks": null
  },
  {
    "id": "lu-technology-master-communication-systems-engineering",
    "faculty": "Faculty of Technology",
    "degree_type": "MASTER",
    "major_category": null,
    "major": "Master in Communication Systems Engineering",
    "official_degree_name": "Master in Communication Systems Engineering",
    "thesis_or_non_thesis": "THESIS_OR_NON_THESIS",
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "program_description": "Official UL graduate program listed by the Faculty of Technology graduate materials.",
    "official_program_url": "https://ft.ul.edu.lb/Syllabus/CSESyllabus_EN.pdf",
    "notes": "The syllabus PDF is the most specific official UL source for this title. University-level graduate enrollment fee is centralized in university.json. UL source describes this cluster as research/professional or professional/research masters.",
    "delivery_mode": null,
    "source_ids": [
      "UL-SRC-036",
      "UL-SRC-038"
    ],
    "concentrations_or_tracks": null
  },
  {
    "id": "lu-technology-master-information-systems",
    "faculty": "Faculty of Technology",
    "degree_type": "MASTER",
    "major_category": null,
    "major": "Master in Information Systems",
    "official_degree_name": "Master in Information Systems",
    "thesis_or_non_thesis": "THESIS_OR_NON_THESIS",
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "program_description": "Official UL graduate program listed by the Faculty of Technology graduate materials.",
    "official_program_url": "https://ul.edu.lb/en/colleges-faculties-majors/282/Faculty-of-Technology",
    "notes": "Split from the grouped Faculty of Technology inventory row. University-level graduate enrollment fee is centralized in university.json. UL source describes this cluster as research/professional or professional/research masters.",
    "delivery_mode": null,
    "source_ids": [
      "UL-SRC-036"
    ],
    "concentrations_or_tracks": null
  },
  {
    "id": "lu-pharmacy-master-clinical-pharmacy",
    "faculty": "Faculty of Pharmacy",
    "degree_type": "MASTER",
    "major_category": null,
    "major": "Master in Clinical Pharmacy",
    "official_degree_name": "Master in Clinical Pharmacy",
    "thesis_or_non_thesis": "THESIS_OR_NON_THESIS",
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "program_description": "Official UL graduate program listed by the Faculty of Pharmacy graduate materials.",
    "official_program_url": "https://ul.edu.lb/en/colleges-faculties-majors/309/Facult%C3%A9%20de%20Pharmacie",
    "notes": "Split from the grouped Faculty of Pharmacy inventory row. University-level graduate enrollment fee is centralized in university.json. UL source describes this cluster as research/professional or professional/research masters.",
    "delivery_mode": null,
    "source_ids": [
      "UL-SRC-040"
    ],
    "concentrations_or_tracks": null
  },
  {
    "id": "lu-pharmacy-master-industrial-cosmetology-dermopharmacy",
    "faculty": "Faculty of Pharmacy",
    "degree_type": "MASTER",
    "major_category": null,
    "major": "Master in Industrial Cosmetology and Dermopharmacy",
    "official_degree_name": "Master in Industrial Cosmetology and Dermopharmacy",
    "thesis_or_non_thesis": "THESIS_OR_NON_THESIS",
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "program_description": "Official UL graduate program listed by the Faculty of Pharmacy graduate materials.",
    "official_program_url": "https://ul.edu.lb/en/colleges-faculties-majors/309/Facult%C3%A9%20de%20Pharmacie",
    "notes": "Split from the grouped Faculty of Pharmacy inventory row. University-level graduate enrollment fee is centralized in university.json. UL source describes this cluster as research/professional or professional/research masters.",
    "delivery_mode": null,
    "source_ids": [
      "UL-SRC-040"
    ],
    "concentrations_or_tracks": null
  },
  {
    "id": "lu-pharmacy-master-pharmaceutical-industry",
    "faculty": "Faculty of Pharmacy",
    "degree_type": "MASTER",
    "major_category": null,
    "major": "Master in Pharmaceutical Industry",
    "official_degree_name": "Master in Pharmaceutical Industry",
    "thesis_or_non_thesis": "THESIS_OR_NON_THESIS",
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "program_description": "Official UL graduate program listed by the Faculty of Pharmacy graduate materials.",
    "official_program_url": "https://ul.edu.lb/en/colleges-faculties-majors/309/Facult%C3%A9%20de%20Pharmacie",
    "notes": "Split from the grouped Faculty of Pharmacy inventory row. University-level graduate enrollment fee is centralized in university.json. UL source describes this cluster as research/professional or professional/research masters.",
    "delivery_mode": null,
    "source_ids": [
      "UL-SRC-040"
    ],
    "concentrations_or_tracks": null
  },
  {
    "id": "lu-pharmacy-master-pharmaceutical-mba",
    "faculty": "Faculty of Pharmacy",
    "degree_type": "MASTER",
    "major_category": null,
    "major": "Master in Pharmaceutical MBA",
    "official_degree_name": "Master in Pharmaceutical MBA",
    "thesis_or_non_thesis": "THESIS_OR_NON_THESIS",
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "program_description": "Official UL graduate program listed by the Faculty of Pharmacy graduate materials.",
    "official_program_url": "https://ul.edu.lb/en/colleges-faculties-majors/309/Facult%C3%A9%20de%20Pharmacie",
    "notes": "Split from the grouped Faculty of Pharmacy inventory row. University-level graduate enrollment fee is centralized in university.json. UL source describes this cluster as research/professional or professional/research masters.",
    "delivery_mode": null,
    "source_ids": [
      "UL-SRC-040"
    ],
    "concentrations_or_tracks": null
  },
  {
    "id": "lu-pharmacy-master-clinical-pharmacy-pharmacoepidemiology",
    "faculty": "Faculty of Pharmacy",
    "degree_type": "MASTER",
    "major_category": null,
    "major": "Master in Clinical Pharmacy and Pharmacoepidemiology",
    "official_degree_name": "Master in Clinical Pharmacy and Pharmacoepidemiology",
    "thesis_or_non_thesis": "THESIS_OR_NON_THESIS",
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "program_description": "Official UL graduate program listed by the Faculty of Pharmacy graduate materials.",
    "official_program_url": "https://ul.edu.lb/en/colleges-faculties-majors/309/Facult%C3%A9%20de%20Pharmacie",
    "notes": "Split from the grouped Faculty of Pharmacy inventory row. University-level graduate enrollment fee is centralized in university.json. UL source describes this cluster as research/professional or professional/research masters.",
    "delivery_mode": null,
    "source_ids": [
      "UL-SRC-040"
    ],
    "concentrations_or_tracks": null
  },
  {
    "id": "lu-pharmacy-master-pharmaceutical-biotechnology",
    "faculty": "Faculty of Pharmacy",
    "degree_type": "MASTER",
    "major_category": null,
    "major": "Master in Pharmaceutical Biotechnology",
    "official_degree_name": "Master in Pharmaceutical Biotechnology",
    "thesis_or_non_thesis": "THESIS_OR_NON_THESIS",
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "program_description": "Official UL graduate program listed by the Faculty of Pharmacy graduate materials.",
    "official_program_url": "https://ul.edu.lb/en/colleges-faculties-majors/309/Facult%C3%A9%20de%20Pharmacie",
    "notes": "Split from the grouped Faculty of Pharmacy inventory row. University-level graduate enrollment fee is centralized in university.json. UL source describes this cluster as research/professional or professional/research masters.",
    "delivery_mode": null,
    "source_ids": [
      "UL-SRC-040"
    ],
    "concentrations_or_tracks": null
  },
  {
    "id": "lu-pharmacy-master-pharmacology-toxicology",
    "faculty": "Faculty of Pharmacy",
    "degree_type": "MASTER",
    "major_category": null,
    "major": "Master in Pharmacology and Toxicology",
    "official_degree_name": "Master in Pharmacology and Toxicology",
    "thesis_or_non_thesis": "THESIS_OR_NON_THESIS",
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "program_description": "Official UL graduate program listed by the Faculty of Pharmacy graduate materials.",
    "official_program_url": "https://ul.edu.lb/en/colleges-faculties-majors/309/Facult%C3%A9%20de%20Pharmacie",
    "notes": "Split from the grouped Faculty of Pharmacy inventory row. University-level graduate enrollment fee is centralized in university.json. UL source describes this cluster as research/professional or professional/research masters.",
    "delivery_mode": null,
    "source_ids": [
      "UL-SRC-040"
    ],
    "concentrations_or_tracks": null
  },
  {
    "id": "lu-dental-master-orthodontics",
    "faculty": "Faculty of Dental Medicine",
    "degree_type": "MASTER",
    "major_category": null,
    "major": "Master in Orthodontics",
    "official_degree_name": "Master in Orthodontics",
    "thesis_or_non_thesis": null,
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "program_description": "Official UL graduate program listed by the Faculty of Dental Medicine graduate materials.",
    "official_program_url": "https://ul.edu.lb/en/colleges-faculties-majors/348/Faculty-of-Dental-Medicine",
    "notes": "Split from the grouped Faculty of Dental Medicine inventory row. University-level graduate enrollment fee is centralized in university.json.",
    "delivery_mode": null,
    "source_ids": [
      "UL-SRC-041"
    ],
    "concentrations_or_tracks": null
  },
  {
    "id": "lu-dental-master-forensic-dentistry-anthropology-human-identification",
    "faculty": "Faculty of Dental Medicine",
    "degree_type": "MASTER",
    "major_category": null,
    "major": "Master in Forensic Dentistry, Anthropology and Human Identification",
    "official_degree_name": "Master in Forensic Dentistry, Anthropology and Human Identification",
    "thesis_or_non_thesis": null,
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "program_description": "Official UL graduate program listed by the Faculty of Dental Medicine graduate materials.",
    "official_program_url": "https://ul.edu.lb/en/colleges-faculties-majors/348/Faculty-of-Dental-Medicine",
    "notes": "Split from the grouped Faculty of Dental Medicine inventory row. University-level graduate enrollment fee is centralized in university.json.",
    "delivery_mode": null,
    "source_ids": [
      "UL-SRC-041"
    ],
    "concentrations_or_tracks": null
  },
  {
    "id": "lu-dental-master-oral-surgery",
    "faculty": "Faculty of Dental Medicine",
    "degree_type": "MASTER",
    "major_category": null,
    "major": "Master in Oral Surgery",
    "official_degree_name": "Master in Oral Surgery",
    "thesis_or_non_thesis": null,
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "program_description": "Official UL graduate program listed by the Faculty of Dental Medicine graduate materials.",
    "official_program_url": "https://ul.edu.lb/en/colleges-faculties-majors/348/Faculty-of-Dental-Medicine",
    "notes": "Split from the grouped Faculty of Dental Medicine inventory row. University-level graduate enrollment fee is centralized in university.json.",
    "delivery_mode": null,
    "source_ids": [
      "UL-SRC-041"
    ],
    "concentrations_or_tracks": null
  },
  {
    "id": "lu-dental-master-restorative-esthetic-dentistry-endodontics",
    "faculty": "Faculty of Dental Medicine",
    "degree_type": "MASTER",
    "major_category": null,
    "major": "Master in Restorative Esthetic Dentistry and Endodontics",
    "official_degree_name": "Master in Restorative Esthetic Dentistry and Endodontics",
    "thesis_or_non_thesis": null,
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "program_description": "Official UL graduate program listed by the Faculty of Dental Medicine graduate materials.",
    "official_program_url": "https://ul.edu.lb/en/colleges-faculties-majors/348/Faculty-of-Dental-Medicine",
    "notes": "Split from the grouped Faculty of Dental Medicine inventory row. University-level graduate enrollment fee is centralized in university.json.",
    "delivery_mode": null,
    "source_ids": [
      "UL-SRC-041"
    ],
    "concentrations_or_tracks": null
  },
  {
    "id": "lu-feba-master-programs",
    "faculty": "Faculty of Economics & Business Administration",
    "degree_type": "MASTER",
    "major_category": null,
    "major": "Faculty of Economics & Business Administration Master's Programs",
    "official_degree_name": "Faculty of Economics & Business Administration Master's Programs",
    "thesis_or_non_thesis": null,
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "program_description": "Faculty-level master's offering for business administration students.",
    "official_program_url": "https://ul.edu.lb/en/colleges-faculties-majors/324/Faculty-of-Economics-%26-Business-Administration",
    "notes": "The faculty details page confirms master's-level students and the master announcement PDF was collected during discovery. University-level graduate enrollment fee is centralized in university.json.",
    "delivery_mode": null,
    "source_ids": [
      "UL-SRC-042",
      "UL-SRC-043",
      "UL-SRC-044"
    ],
    "concentrations_or_tracks": [
      "Business Administration"
    ]
  },
  {
    "id": "lu-letters-master-programs",
    "faculty": "Faculty of Letters and Human Sciences",
    "degree_type": "MASTER",
    "major_category": null,
    "major": "Faculty of Letters and Human Sciences Master's Programs",
    "official_degree_name": "Faculty of Letters and Human Sciences Master's Programs",
    "thesis_or_non_thesis": null,
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "program_description": "Faculty-level master's offering noted on the Faculty of Letters and Human Sciences details page.",
    "official_program_url": "https://ul.edu.lb/en/colleges-faculties-details/269/Faculty-of-Letters-and-Human-Sciences",
    "notes": "UL did not surface a separate master program page in this pass; the faculty details page is the authoritative graduate signal. University-level graduate enrollment fee is centralized in university.json.",
    "delivery_mode": null,
    "source_ids": [
      "UL-SRC-045"
    ],
    "concentrations_or_tracks": [
      "Languages and Translation",
      "Language Sciences and Communication"
    ]
  },
  {
    "id": "lu-iss-master-programs",
    "faculty": "Institute of Social Sciences",
    "degree_type": "MASTER",
    "major_category": null,
    "major": "Institute of Social Sciences Professional Master's Program",
    "official_degree_name": "Institute of Social Sciences Professional Master's Program",
    "thesis_or_non_thesis": null,
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "program_description": "Professional master's program at the Institute of Social Sciences.",
    "official_program_url": "https://ul.edu.lb/en/applications-open-enrollment-professional-master-iss-academic-year-2025%E2%80%932026",
    "notes": "The institute's official announcement confirms a professional master's intake for the academic year 2025-2026. University-level graduate enrollment fee is centralized in university.json.",
    "delivery_mode": null,
    "source_ids": [
      "UL-SRC-047"
    ],
    "concentrations_or_tracks": null
  },
  {
    "id": "lu-dslhss-phd-program",
    "faculty": "Doctoral School of Literature, Humanities & Social Sciences",
    "degree_type": "PHD",
    "major_category": null,
    "major": "Lebanese Doctoral Degree in Literature, Humanities and Social Sciences",
    "official_degree_name": "Lebanese Doctoral Degree in Literature, Humanities and Social Sciences",
    "thesis_or_non_thesis": "THESIS",
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "program_description": "Doctoral school offering the Lebanese doctoral degree in literature, humanities, and social sciences.",
    "official_program_url": "https://ul.edu.lb/en/colleges-faculties-details/259/Doctoral-School-of-Literature%2C-Humanities-%26-Social-Sciences",
    "notes": "The doctoral school pages and internal system describe the covered specializations and doctoral preparation route. University-level graduate enrollment fee is centralized in university.json.",
    "delivery_mode": null,
    "source_ids": [
      "UL-SRC-048",
      "UL-SRC-049",
      "UL-SRC-050",
      "UL-SRC-051"
    ],
    "concentrations_or_tracks": [
      "Literature",
      "Humanities",
      "Social Sciences",
      "Journalism and Information",
      "Pedagogy",
      "Arts and Art Sciences",
      "Translation",
      "Language and Communication Sciences"
    ]
  },
  {
    "id": "lu-dslpaes-phd-program",
    "faculty": "Doctoral School of Law, Political, Administrative & Economic Sciences",
    "degree_type": "PHD",
    "major_category": null,
    "major": "Lebanese Doctoral Degree in Law, Political, Administrative and Economic Sciences",
    "official_degree_name": "Lebanese Doctoral Degree in Law, Political, Administrative and Economic Sciences",
    "thesis_or_non_thesis": "THESIS",
    "credits": null,
    "duration_value": null,
    "duration_unit": null,
    "program_description": "Doctoral school offering the Lebanese doctoral degree in law, political, administrative, and economic sciences.",
    "official_program_url": "https://ul.edu.lb/en/colleges-faculties-details/261/Doctoral-School-of-Law%2C-Political%2C-Administrative-%26-Economic-Sciences",
    "notes": "The doctoral school pages and application announcements cover doctoral preparation in the legal, political, administrative, economic, business, economics, and tourism areas. University-level graduate enrollment fee is centralized in university.json.",
    "delivery_mode": null,
    "source_ids": [
      "UL-SRC-052",
      "UL-SRC-053",
      "UL-SRC-054",
      "UL-SRC-055"
    ],
    "concentrations_or_tracks": [
      "Law",
      "Political Sciences",
      "Administrative Sciences",
      "Economic Sciences",
      "Business Administration",
      "Economics",
      "Tourism"
    ]
  },
  {
    "id": "lu-dsst-phd-program",
    "faculty": "Doctoral School of Science & Technology",
    "degree_type": "PHD",
    "major_category": null,
    "major": "Lebanese Doctoral Degree in Science and Technology",
    "official_degree_name": "Lebanese Doctoral Degree in Science and Technology",
    "thesis_or_non_thesis": "THESIS",
    "credits": 180,
    "duration_value": null,
    "duration_unit": null,
    "program_description": "Doctoral school offering the Lebanese doctoral degree in science and technology.",
    "official_program_url": "https://ul.edu.lb/en/colleges-faculties-details/263/Doctoral-School-of-Science-%26-Technology",
    "notes": "The internal system page states the PhD is 180 ECTS with research/dissertation allocation. University-level graduate enrollment fee is centralized in university.json.",
    "delivery_mode": null,
    "source_ids": [
      "UL-SRC-056",
      "UL-SRC-057",
      "UL-SRC-058"
    ],
    "concentrations_or_tracks": [
      "Engineering",
      "Public Health",
      "Pharmacy",
      "Science",
      "Technology",
      "Medical Sciences",
      "Dental Medicine",
      "Fine Arts and Architecture",
      "Agronomy"
    ]
  }
]$UL_PROGRAMS$) AS x(
        id TEXT,
        faculty TEXT,
        degree_type TEXT,
        major_category TEXT,
        major TEXT,
        official_degree_name TEXT,
        thesis_or_non_thesis TEXT,
        credits INTEGER,
        duration_value NUMERIC(10,2),
        duration_unit TEXT,
        program_description TEXT,
        official_program_url TEXT,
        notes TEXT,
        delivery_mode TEXT,
        source_ids JSONB,
        concentrations_or_tracks JSONB
    );

    INSERT INTO graduate_program (university_id, faculty_id, department_id, degree_type_id, program_key, major_category, major, official_degree_name, thesis_or_non_thesis, credits, duration_value, duration_unit, primary_language_id, delivery_mode, program_description, official_program_url, source_id, notes)
    SELECT v_university_id, fac.id, NULL, dt.id, seed.id, seed.major_category, seed.major, seed.official_degree_name, seed.thesis_or_non_thesis, seed.credits, seed.duration_value, seed.duration_unit, NULL::BIGINT, seed.delivery_mode, seed.program_description, seed.official_program_url, s.id, seed.notes
    FROM ul_program_seed seed
    JOIN ul_faculty_seed fac_seed ON fac_seed.name = seed.faculty
    JOIN university_faculty fac ON fac.university_id = v_university_id AND fac.name = fac_seed.name
    JOIN degree_type dt ON dt.code = seed.degree_type
    JOIN ul_source_seed ss ON ss.source_id = seed.source_ids->>0
    JOIN source s ON s.university_id = v_university_id AND s.url = ss.url
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
    SELECT v_university_id, gp.id, s.id,
           CASE src.source_order
               WHEN 1 THEN 'PRIMARY'
               WHEN 2 THEN 'SECONDARY'
               WHEN 3 THEN 'ADMISSIONS'
               ELSE 'OTHER'
           END,
           src.source_order,
           src.source_id,
           NULL
    FROM ul_program_seed seed
    JOIN graduate_program gp ON gp.university_id = v_university_id AND gp.program_key = seed.id
    JOIN LATERAL jsonb_array_elements_text(seed.source_ids) WITH ORDINALITY AS src(source_id, source_order) ON TRUE
    JOIN ul_source_seed ss ON ss.source_id = src.source_id
    JOIN source s ON s.university_id = v_university_id AND s.url = ss.url
    ON CONFLICT (program_id, source_id, source_role) DO UPDATE SET
        source_order = EXCLUDED.source_order,
        evidence_text = EXCLUDED.evidence_text,
        notes = EXCLUDED.notes,
        updated_at = NOW();

    INSERT INTO graduate_fee_item (university_id, faculty_id, department_id, program_id, scope_level, record_key, academic_year, fee_name, billing_basis, currency, amount, category, notes, source_id)
    SELECT v_university_id, NULL, NULL, NULL, scope_level, record_key, academic_year, fee_name, billing_basis, currency, amount, category, notes, s.id
    FROM jsonb_to_recordset($UL_FEE_ITEMS$[
  {
    "record_key": "ul:fee_item:shared_graduate_enrollment_registration_fee",
    "scope_level": "UNIVERSITY",
    "academic_year": "2025-2026",
    "fee_name": "Graduate Enrollment / Registration Fee",
    "billing_basis": "FLAT_FEE",
    "currency": "LBP",
    "amount": 745000,
    "category": "Admissions",
    "notes": "The reviewed official pages present this as a shared graduate enrollment fee, not a per-credit tuition amount.",
    "source_id": "UL-SRC-059"
  },
  {
    "record_key": "ul:fee_item:cee_registration_fee",
    "scope_level": "UNIVERSITY",
    "academic_year": "2025-2026",
    "fee_name": "Competitive Entrance Exam Registration Fee",
    "billing_basis": "FLAT_FEE",
    "currency": "LBP",
    "amount": 35000,
    "category": "Admissions",
    "notes": "Published in the general enrollment document list for graduate applicants when applicable.",
    "source_id": "UL-SRC-061"
  }
]$UL_FEE_ITEMS$) AS x(
        record_key TEXT,
        scope_level TEXT,
        academic_year TEXT,
        fee_name TEXT,
        billing_basis TEXT,
        currency TEXT,
        amount NUMERIC(12,2),
        category TEXT,
        notes TEXT,
        source_id TEXT
    )
    JOIN ul_source_seed ss ON ss.source_id = x.source_id
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

    INSERT INTO graduate_admission_requirement (university_id, faculty_id, department_id, program_id, scope_level, record_key, requirement_type, requirement_text, comparison_operator, threshold_value, threshold_unit, is_required, notes, source_id)
    SELECT v_university_id, NULL, NULL, NULL, scope_level, record_key, requirement_type, requirement_text, comparison_operator, threshold_value, threshold_unit, is_required, notes, s.id
    FROM jsonb_to_recordset($UL_ADMISSION_REQUIREMENTS$[
  {
    "record_key": "ul:admission:university:general",
    "scope_level": "UNIVERSITY",
    "requirement_type": "GENERAL",
    "requirement_text": "Graduate applicants submit their enrollment file to the relevant faculty or institute administration, follow the deadlines set annually by that unit, and complete the enrollment process in person unless an official POA is used for non-Lebanese applicants who cannot attend. Some faculties also allow online admission.",
    "comparison_operator": null,
    "threshold_value": null,
    "threshold_unit": null,
    "is_required": true,
    "notes": "Central graduate admissions process published by UL.",
    "source_id": "UL-SRC-061"
  },
  {
    "record_key": "ul:admission:university:english",
    "scope_level": "UNIVERSITY",
    "requirement_type": "ENGLISH",
    "requirement_text": "No university-wide graduate language threshold was published in the reviewed UL pages. Language expectations appear to be faculty- or program-specific.",
    "comparison_operator": null,
    "threshold_value": null,
    "threshold_unit": null,
    "is_required": true,
    "notes": "The reviewed source set did not expose a central English/French proficiency policy for all graduate programs.",
    "source_id": "UL-SRC-061"
  }
]$UL_ADMISSION_REQUIREMENTS$) AS x(
        record_key TEXT,
        scope_level TEXT,
        requirement_type TEXT,
        requirement_text TEXT,
        comparison_operator TEXT,
        threshold_value NUMERIC(12,2),
        threshold_unit TEXT,
        is_required BOOLEAN,
        notes TEXT,
        source_id TEXT
    )
    JOIN ul_source_seed ss ON ss.source_id = x.source_id
    JOIN source s ON s.university_id = v_university_id AND s.url = ss.url
    ON CONFLICT (university_id, record_key) DO UPDATE SET
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

    INSERT INTO graduate_required_document (university_id, faculty_id, department_id, program_id, scope_level, record_key, document_type, document_name, is_optional, sort_order, notes, source_id)
    SELECT v_university_id, NULL, NULL, NULL, scope_level, record_key, document_type, document_name, is_optional, sort_order, notes, s.id
    FROM jsonb_to_recordset($UL_REQUIRED_DOCUMENTS$[
  {
    "record_key": "ul:document:01",
    "scope_level": "UNIVERSITY",
    "document_type": "BACCALAUREATE_OR_EQUIVALENCY",
    "document_name": "Lebanese Baccalaureate or the equivalent, certified by the Ministry of Education & Higher Education",
    "is_optional": false,
    "sort_order": 1,
    "notes": null,
    "source_id": "UL-SRC-061"
  },
  {
    "record_key": "ul:document:02",
    "scope_level": "UNIVERSITY",
    "document_type": "INTERNATIONAL_EQUIVALENCY",
    "document_name": "For non-Lebanese students, the high-school certificate that allows university admission in the home country and the Lebanese equivalency certificate",
    "is_optional": false,
    "sort_order": 2,
    "notes": null,
    "source_id": "UL-SRC-061"
  },
  {
    "record_key": "ul:document:03",
    "scope_level": "UNIVERSITY",
    "document_type": "CIVIL_STATUS_RECORD_OR_ID",
    "document_name": "Individual civil-status record not older than 3 months, or an ID copy for Lebanese students",
    "is_optional": false,
    "sort_order": 3,
    "notes": null,
    "source_id": "UL-SRC-061"
  },
  {
    "record_key": "ul:document:04",
    "scope_level": "UNIVERSITY",
    "document_type": "PASSPORT_COPY",
    "document_name": "Certified passport copy for foreign students",
    "is_optional": false,
    "sort_order": 4,
    "notes": null,
    "source_id": "UL-SRC-061"
  },
  {
    "record_key": "ul:document:05",
    "scope_level": "UNIVERSITY",
    "document_type": "ID_PHOTOS",
    "document_name": "Two ID photos",
    "is_optional": false,
    "sort_order": 5,
    "notes": null,
    "source_id": "UL-SRC-061"
  },
  {
    "record_key": "ul:document:06",
    "scope_level": "UNIVERSITY",
    "document_type": "SOCIAL_SECURITY_AFFIDAVIT_OR_CARD",
    "document_name": "Affidavit or Social Security card copy if the student benefits from Lebanese Social Security",
    "is_optional": false,
    "sort_order": 6,
    "notes": null,
    "source_id": "UL-SRC-061"
  },
  {
    "record_key": "ul:document:07",
    "scope_level": "UNIVERSITY",
    "document_type": "POSTAL_STAMP",
    "document_name": "Postal stamp of LBP 1,000",
    "is_optional": false,
    "sort_order": 7,
    "notes": null,
    "source_id": "UL-SRC-061"
  },
  {
    "record_key": "ul:document:08",
    "scope_level": "UNIVERSITY",
    "document_type": "TRANSFER_DOCUMENTS",
    "document_name": "Competitive Entrance Exam registration fee, if any (LBP 35,000)",
    "is_optional": false,
    "sort_order": 8,
    "notes": null,
    "source_id": "UL-SRC-061"
  },
  {
    "record_key": "ul:document:09",
    "scope_level": "UNIVERSITY",
    "document_type": "OTHER",
    "document_name": "Certified transfer documents when a faculty or institute accepts transfer students",
    "is_optional": false,
    "sort_order": 9,
    "notes": null,
    "source_id": "UL-SRC-061"
  }
]$UL_REQUIRED_DOCUMENTS$) AS x(
        record_key TEXT,
        scope_level TEXT,
        document_type TEXT,
        document_name TEXT,
        is_optional BOOLEAN,
        sort_order INTEGER,
        notes TEXT,
        source_id TEXT
    )
    JOIN ul_source_seed ss ON ss.source_id = x.source_id
    JOIN source s ON s.university_id = v_university_id AND s.url = ss.url
    ON CONFLICT (university_id, record_key) DO UPDATE SET
        scope_level = EXCLUDED.scope_level,
        document_type = EXCLUDED.document_type,
        document_name = EXCLUDED.document_name,
        is_optional = EXCLUDED.is_optional,
        sort_order = EXCLUDED.sort_order,
        notes = EXCLUDED.notes,
        source_id = EXCLUDED.source_id,
        updated_at = NOW();

    INSERT INTO graduate_admission_deadline (university_id, faculty_id, department_id, program_id, scope_level, record_key, academic_year, deadline_type, term, deadline_date, note, source_id)
    SELECT v_university_id, NULL, NULL, NULL, scope_level, record_key, academic_year, deadline_type, term, deadline_date, note, s.id
    FROM jsonb_to_recordset($UL_DEADLINES$[
  {
    "record_key": "ul:deadline:academic_calendar_2025_2026",
    "scope_level": "UNIVERSITY",
    "academic_year": "2025-2026",
    "deadline_type": "OTHER",
    "term": "Academic Calendar 2025-2026",
    "deadline_date": null,
    "note": "UL publishes an official 2025-2026 academic calendar PDF, and the registration page states that deadlines are set annually by each faculty or institute administration.",
    "source_id": "UL-SRC-061"
  }
]$UL_DEADLINES$) AS x(
        record_key TEXT,
        scope_level TEXT,
        academic_year TEXT,
        deadline_type TEXT,
        term TEXT,
        deadline_date DATE,
        note TEXT,
        source_id TEXT
    )
    JOIN ul_source_seed ss ON ss.source_id = x.source_id
    JOIN source s ON s.university_id = v_university_id AND s.url = ss.url
    ON CONFLICT (university_id, record_key) DO UPDATE SET
        scope_level = EXCLUDED.scope_level,
        academic_year = EXCLUDED.academic_year,
        deadline_type = EXCLUDED.deadline_type,
        term = EXCLUDED.term,
        deadline_date = EXCLUDED.deadline_date,
        note = EXCLUDED.note,
        source_id = EXCLUDED.source_id,
        updated_at = NOW();

    INSERT INTO graduate_financial_aid (university_id, faculty_id, department_id, program_id, scope_level, record_key, academic_year, name, description, amount, currency, notes, source_id)
    SELECT v_university_id, NULL, NULL, NULL, scope_level, record_key, academic_year, name, description, amount, currency, notes, s.id
    FROM jsonb_to_recordset($UL_FINANCIAL_AID$[
  {
    "record_key": "ul:financial_aid:doctoral_grant_support",
    "scope_level": "UNIVERSITY",
    "academic_year": "2025-2026",
    "name": "Doctoral Grant Support",
    "description": "The official doctoral-school pages state that UL grants support to PhD students in the Doctoral School of Law, Political, Administrative & Economic Sciences. No broader graduate aid table was published in the reviewed sources.",
    "amount": null,
    "currency": "USD",
    "notes": "No broader graduate aid table was published in the reviewed official UL sources.",
    "source_id": "UL-SRC-052"
  }
]$UL_FINANCIAL_AID$) AS x(
        record_key TEXT,
        scope_level TEXT,
        academic_year TEXT,
        name TEXT,
        description TEXT,
        amount NUMERIC(12,2),
        currency TEXT,
        notes TEXT,
        source_id TEXT
    )
    JOIN ul_source_seed ss ON ss.source_id = x.source_id
    JOIN source s ON s.university_id = v_university_id AND s.url = ss.url
    ON CONFLICT (university_id, record_key) DO UPDATE SET
        scope_level = EXCLUDED.scope_level,
        academic_year = EXCLUDED.academic_year,
        name = EXCLUDED.name,
        description = EXCLUDED.description,
        amount = EXCLUDED.amount,
        currency = EXCLUDED.currency,
        notes = EXCLUDED.notes,
        source_id = EXCLUDED.source_id,
        updated_at = NOW();

    INSERT INTO graduate_payment_plan (university_id, faculty_id, department_id, program_id, scope_level, record_key, academic_year, name, description, installments_count, down_payment_amount, down_payment_currency, interval_label, notes, source_id)
    SELECT v_university_id, NULL, NULL, NULL, scope_level, record_key, academic_year, name, description, installments_count, down_payment_amount, down_payment_currency, interval_label, notes, s.id
    FROM jsonb_to_recordset($UL_PAYMENT_PLANS$[
  {
    "record_key": "ul:payment_plan:one_installment_enrollment",
    "scope_level": "UNIVERSITY",
    "academic_year": "2025-2026",
    "name": "One-Installment Enrollment Fee",
    "description": "UL states that the enrollment fees are paid in one installment. No installment plan was published in the reviewed official sources.",
    "installments_count": 1,
    "down_payment_amount": null,
    "down_payment_currency": null,
    "interval_label": "One installment",
    "notes": "No installment plan was published; the reviewed official sources only confirm one-installment payment.",
    "source_id": "UL-SRC-061"
  }
]$UL_PAYMENT_PLANS$) AS x(
        record_key TEXT,
        scope_level TEXT,
        academic_year TEXT,
        name TEXT,
        description TEXT,
        installments_count INTEGER,
        down_payment_amount NUMERIC(12,2),
        down_payment_currency TEXT,
        interval_label TEXT,
        notes TEXT,
        source_id TEXT
    )
    JOIN ul_source_seed ss ON ss.source_id = x.source_id
    JOIN source s ON s.university_id = v_university_id AND s.url = ss.url
    ON CONFLICT (university_id, record_key) DO UPDATE SET
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

    INSERT INTO graduate_accreditation (university_id, faculty_id, department_id, program_id, scope_level, record_key, name, authority, status, valid_from, valid_until, notes, source_id)
    SELECT v_university_id, fac.id, NULL, gp.id, scope_level, record_key, name, authority, status, valid_from, valid_until, notes, s.id
    FROM jsonb_to_recordset($UL_ACCREDITATION$[
  {
    "record_key": "ul:accreditation:lu-letters-master-programs",
    "scope_level": "FACULTY",
    "name": "The faculty details page states accreditation for 37 majors at bachelor and master's levels.",
    "authority": null,
    "status": null,
    "valid_from": null,
    "valid_until": null,
    "notes": "UL did not surface a separate master program page in this pass; the faculty details page is the authoritative graduate signal. University-level graduate enrollment fee is centralized in university.json.",
    "source_id": "UL-SRC-045",
    "faculty": "Faculty of Letters and Human Sciences",
    "program_id": null
  },
  {
    "record_key": "ul:accreditation:lu-dslhss-phd-program",
    "scope_level": "PROGRAM",
    "name": "Lebanese Doctoral Degree and HDR.",
    "authority": null,
    "status": null,
    "valid_from": null,
    "valid_until": null,
    "notes": "The doctoral school pages and internal system describe the covered specializations and doctoral preparation route. University-level graduate enrollment fee is centralized in university.json.",
    "source_id": "UL-SRC-048",
    "faculty": "Doctoral School of Literature, Humanities & Social Sciences",
    "program_id": "lu-dslhss-phd-program"
  },
  {
    "record_key": "ul:accreditation:lu-dslpaes-phd-program",
    "scope_level": "PROGRAM",
    "name": "Lebanese PhD; doctoral school pages also mention grants for PhD students.",
    "authority": null,
    "status": null,
    "valid_from": null,
    "valid_until": null,
    "notes": "The doctoral school pages and application announcements cover doctoral preparation in the legal, political, administrative, economic, business, economics, and tourism areas. University-level graduate enrollment fee is centralized in university.json.",
    "source_id": "UL-SRC-052",
    "faculty": "Doctoral School of Law, Political, Administrative & Economic Sciences",
    "program_id": "lu-dslpaes-phd-program"
  },
  {
    "record_key": "ul:accreditation:lu-dsst-phd-program",
    "scope_level": "PROGRAM",
    "name": "Lebanese PhD and HDR.",
    "authority": null,
    "status": null,
    "valid_from": null,
    "valid_until": null,
    "notes": "The internal system page states the PhD is 180 ECTS with research/dissertation allocation. University-level graduate enrollment fee is centralized in university.json.",
    "source_id": "UL-SRC-056",
    "faculty": "Doctoral School of Science & Technology",
    "program_id": "lu-dsst-phd-program"
  }
]$UL_ACCREDITATION$) AS x(
        record_key TEXT,
        scope_level TEXT,
        name TEXT,
        authority TEXT,
        status TEXT,
        valid_from DATE,
        valid_until DATE,
        notes TEXT,
        source_id TEXT,
        faculty TEXT,
        program_id TEXT
    )
    LEFT JOIN university_faculty fac ON fac.university_id = v_university_id AND fac.name = x.faculty
    LEFT JOIN graduate_program gp ON gp.university_id = v_university_id AND gp.program_key = x.program_id
    JOIN ul_source_seed ss ON ss.source_id = x.source_id
    JOIN source s ON s.university_id = v_university_id AND s.url = ss.url
    ON CONFLICT (university_id, record_key) DO UPDATE SET
        scope_level = EXCLUDED.scope_level,
        name = EXCLUDED.name,
        authority = EXCLUDED.authority,
        status = EXCLUDED.status,
        valid_from = EXCLUDED.valid_from,
        valid_until = EXCLUDED.valid_until,
        notes = EXCLUDED.notes,
        source_id = EXCLUDED.source_id,
        updated_at = NOW();

    INSERT INTO graduate_program_track (university_id, faculty_id, department_id, program_id, track_type, track_name, track_order, is_primary, description, source_id, notes)
    SELECT v_university_id, fac.id, NULL, gp.id, track_type, track_name, track_order, is_primary, description, s.id, notes
    FROM jsonb_to_recordset($UL_TRACKS$[
  {
    "record_key": "ul:track:lu-fph-master-programs:1",
    "track_type": "CONCENTRATION",
    "track_name": "Research Master",
    "track_order": 1,
    "is_primary": true,
    "description": null,
    "source_id": "UL-SRC-018",
    "notes": "Public Health master information and admissions are published through the faculty hub and PDF. University-level graduate enrollment fee is centralized in university.json. UL source describes this cluster as research/professional or professional/research masters.",
    "faculty": "Faculty of Public Health",
    "program_id": "lu-fph-master-programs"
  },
  {
    "record_key": "ul:track:lu-fph-master-programs:2",
    "track_type": "CONCENTRATION",
    "track_name": "Professional Master",
    "track_order": 2,
    "is_primary": false,
    "description": null,
    "source_id": "UL-SRC-018",
    "notes": "Public Health master information and admissions are published through the faculty hub and PDF. University-level graduate enrollment fee is centralized in university.json. UL source describes this cluster as research/professional or professional/research masters.",
    "faculty": "Faculty of Public Health",
    "program_id": "lu-fph-master-programs"
  },
  {
    "record_key": "ul:track:lu-law-master-programs:1",
    "track_type": "CONCENTRATION",
    "track_name": "Legal Studies",
    "track_order": 1,
    "is_primary": true,
    "description": null,
    "source_id": "UL-SRC-028",
    "notes": "UL publishes this area through the faculty hub and the master's program announcements, including a joint master's in strategic studies. University-level graduate enrollment fee is centralized in university.json. UL source describes this cluster as research/professional or professional/research masters.",
    "faculty": "Faculty of Law and Political and Administrative Sciences",
    "program_id": "lu-law-master-programs"
  },
  {
    "record_key": "ul:track:lu-law-master-programs:2",
    "track_type": "CONCENTRATION",
    "track_name": "Diplomatic Studies",
    "track_order": 2,
    "is_primary": false,
    "description": null,
    "source_id": "UL-SRC-028",
    "notes": "UL publishes this area through the faculty hub and the master's program announcements, including a joint master's in strategic studies. University-level graduate enrollment fee is centralized in university.json. UL source describes this cluster as research/professional or professional/research masters.",
    "faculty": "Faculty of Law and Political and Administrative Sciences",
    "program_id": "lu-law-master-programs"
  },
  {
    "record_key": "ul:track:lu-law-master-programs:3",
    "track_type": "CONCENTRATION",
    "track_name": "Litigation Studies",
    "track_order": 3,
    "is_primary": false,
    "description": null,
    "source_id": "UL-SRC-028",
    "notes": "UL publishes this area through the faculty hub and the master's program announcements, including a joint master's in strategic studies. University-level graduate enrollment fee is centralized in university.json. UL source describes this cluster as research/professional or professional/research masters.",
    "faculty": "Faculty of Law and Political and Administrative Sciences",
    "program_id": "lu-law-master-programs"
  },
  {
    "record_key": "ul:track:lu-law-master-programs:4",
    "track_type": "CONCENTRATION",
    "track_name": "Business Law",
    "track_order": 4,
    "is_primary": false,
    "description": null,
    "source_id": "UL-SRC-028",
    "notes": "UL publishes this area through the faculty hub and the master's program announcements, including a joint master's in strategic studies. University-level graduate enrollment fee is centralized in university.json. UL source describes this cluster as research/professional or professional/research masters.",
    "faculty": "Faculty of Law and Political and Administrative Sciences",
    "program_id": "lu-law-master-programs"
  },
  {
    "record_key": "ul:track:lu-law-master-programs:5",
    "track_type": "CONCENTRATION",
    "track_name": "Strategic Studies",
    "track_order": 5,
    "is_primary": false,
    "description": null,
    "source_id": "UL-SRC-028",
    "notes": "UL publishes this area through the faculty hub and the master's program announcements, including a joint master's in strategic studies. University-level graduate enrollment fee is centralized in university.json. UL source describes this cluster as research/professional or professional/research masters.",
    "faculty": "Faculty of Law and Political and Administrative Sciences",
    "program_id": "lu-law-master-programs"
  },
  {
    "record_key": "ul:track:lu-feba-master-programs:1",
    "track_type": "CONCENTRATION",
    "track_name": "Business Administration",
    "track_order": 1,
    "is_primary": true,
    "description": null,
    "source_id": "UL-SRC-042",
    "notes": "The faculty details page confirms master's-level students and the master announcement PDF was collected during discovery. University-level graduate enrollment fee is centralized in university.json.",
    "faculty": "Faculty of Economics & Business Administration",
    "program_id": "lu-feba-master-programs"
  },
  {
    "record_key": "ul:track:lu-letters-master-programs:1",
    "track_type": "CONCENTRATION",
    "track_name": "Languages and Translation",
    "track_order": 1,
    "is_primary": true,
    "description": null,
    "source_id": "UL-SRC-045",
    "notes": "UL did not surface a separate master program page in this pass; the faculty details page is the authoritative graduate signal. University-level graduate enrollment fee is centralized in university.json.",
    "faculty": "Faculty of Letters and Human Sciences",
    "program_id": "lu-letters-master-programs"
  },
  {
    "record_key": "ul:track:lu-letters-master-programs:2",
    "track_type": "CONCENTRATION",
    "track_name": "Language Sciences and Communication",
    "track_order": 2,
    "is_primary": false,
    "description": null,
    "source_id": "UL-SRC-045",
    "notes": "UL did not surface a separate master program page in this pass; the faculty details page is the authoritative graduate signal. University-level graduate enrollment fee is centralized in university.json.",
    "faculty": "Faculty of Letters and Human Sciences",
    "program_id": "lu-letters-master-programs"
  },
  {
    "record_key": "ul:track:lu-dslhss-phd-program:1",
    "track_type": "CONCENTRATION",
    "track_name": "Literature",
    "track_order": 1,
    "is_primary": true,
    "description": null,
    "source_id": "UL-SRC-048",
    "notes": "The doctoral school pages and internal system describe the covered specializations and doctoral preparation route. University-level graduate enrollment fee is centralized in university.json.",
    "faculty": "Doctoral School of Literature, Humanities & Social Sciences",
    "program_id": "lu-dslhss-phd-program"
  },
  {
    "record_key": "ul:track:lu-dslhss-phd-program:2",
    "track_type": "CONCENTRATION",
    "track_name": "Humanities",
    "track_order": 2,
    "is_primary": false,
    "description": null,
    "source_id": "UL-SRC-048",
    "notes": "The doctoral school pages and internal system describe the covered specializations and doctoral preparation route. University-level graduate enrollment fee is centralized in university.json.",
    "faculty": "Doctoral School of Literature, Humanities & Social Sciences",
    "program_id": "lu-dslhss-phd-program"
  },
  {
    "record_key": "ul:track:lu-dslhss-phd-program:3",
    "track_type": "CONCENTRATION",
    "track_name": "Social Sciences",
    "track_order": 3,
    "is_primary": false,
    "description": null,
    "source_id": "UL-SRC-048",
    "notes": "The doctoral school pages and internal system describe the covered specializations and doctoral preparation route. University-level graduate enrollment fee is centralized in university.json.",
    "faculty": "Doctoral School of Literature, Humanities & Social Sciences",
    "program_id": "lu-dslhss-phd-program"
  },
  {
    "record_key": "ul:track:lu-dslhss-phd-program:4",
    "track_type": "CONCENTRATION",
    "track_name": "Journalism and Information",
    "track_order": 4,
    "is_primary": false,
    "description": null,
    "source_id": "UL-SRC-048",
    "notes": "The doctoral school pages and internal system describe the covered specializations and doctoral preparation route. University-level graduate enrollment fee is centralized in university.json.",
    "faculty": "Doctoral School of Literature, Humanities & Social Sciences",
    "program_id": "lu-dslhss-phd-program"
  },
  {
    "record_key": "ul:track:lu-dslhss-phd-program:5",
    "track_type": "CONCENTRATION",
    "track_name": "Pedagogy",
    "track_order": 5,
    "is_primary": false,
    "description": null,
    "source_id": "UL-SRC-048",
    "notes": "The doctoral school pages and internal system describe the covered specializations and doctoral preparation route. University-level graduate enrollment fee is centralized in university.json.",
    "faculty": "Doctoral School of Literature, Humanities & Social Sciences",
    "program_id": "lu-dslhss-phd-program"
  },
  {
    "record_key": "ul:track:lu-dslhss-phd-program:6",
    "track_type": "CONCENTRATION",
    "track_name": "Arts and Art Sciences",
    "track_order": 6,
    "is_primary": false,
    "description": null,
    "source_id": "UL-SRC-048",
    "notes": "The doctoral school pages and internal system describe the covered specializations and doctoral preparation route. University-level graduate enrollment fee is centralized in university.json.",
    "faculty": "Doctoral School of Literature, Humanities & Social Sciences",
    "program_id": "lu-dslhss-phd-program"
  },
  {
    "record_key": "ul:track:lu-dslhss-phd-program:7",
    "track_type": "CONCENTRATION",
    "track_name": "Translation",
    "track_order": 7,
    "is_primary": false,
    "description": null,
    "source_id": "UL-SRC-048",
    "notes": "The doctoral school pages and internal system describe the covered specializations and doctoral preparation route. University-level graduate enrollment fee is centralized in university.json.",
    "faculty": "Doctoral School of Literature, Humanities & Social Sciences",
    "program_id": "lu-dslhss-phd-program"
  },
  {
    "record_key": "ul:track:lu-dslhss-phd-program:8",
    "track_type": "CONCENTRATION",
    "track_name": "Language and Communication Sciences",
    "track_order": 8,
    "is_primary": false,
    "description": null,
    "source_id": "UL-SRC-048",
    "notes": "The doctoral school pages and internal system describe the covered specializations and doctoral preparation route. University-level graduate enrollment fee is centralized in university.json.",
    "faculty": "Doctoral School of Literature, Humanities & Social Sciences",
    "program_id": "lu-dslhss-phd-program"
  },
  {
    "record_key": "ul:track:lu-dslpaes-phd-program:1",
    "track_type": "CONCENTRATION",
    "track_name": "Law",
    "track_order": 1,
    "is_primary": true,
    "description": null,
    "source_id": "UL-SRC-052",
    "notes": "The doctoral school pages and application announcements cover doctoral preparation in the legal, political, administrative, economic, business, economics, and tourism areas. University-level graduate enrollment fee is centralized in university.json.",
    "faculty": "Doctoral School of Law, Political, Administrative & Economic Sciences",
    "program_id": "lu-dslpaes-phd-program"
  },
  {
    "record_key": "ul:track:lu-dslpaes-phd-program:2",
    "track_type": "CONCENTRATION",
    "track_name": "Political Sciences",
    "track_order": 2,
    "is_primary": false,
    "description": null,
    "source_id": "UL-SRC-052",
    "notes": "The doctoral school pages and application announcements cover doctoral preparation in the legal, political, administrative, economic, business, economics, and tourism areas. University-level graduate enrollment fee is centralized in university.json.",
    "faculty": "Doctoral School of Law, Political, Administrative & Economic Sciences",
    "program_id": "lu-dslpaes-phd-program"
  },
  {
    "record_key": "ul:track:lu-dslpaes-phd-program:3",
    "track_type": "CONCENTRATION",
    "track_name": "Administrative Sciences",
    "track_order": 3,
    "is_primary": false,
    "description": null,
    "source_id": "UL-SRC-052",
    "notes": "The doctoral school pages and application announcements cover doctoral preparation in the legal, political, administrative, economic, business, economics, and tourism areas. University-level graduate enrollment fee is centralized in university.json.",
    "faculty": "Doctoral School of Law, Political, Administrative & Economic Sciences",
    "program_id": "lu-dslpaes-phd-program"
  },
  {
    "record_key": "ul:track:lu-dslpaes-phd-program:4",
    "track_type": "CONCENTRATION",
    "track_name": "Economic Sciences",
    "track_order": 4,
    "is_primary": false,
    "description": null,
    "source_id": "UL-SRC-052",
    "notes": "The doctoral school pages and application announcements cover doctoral preparation in the legal, political, administrative, economic, business, economics, and tourism areas. University-level graduate enrollment fee is centralized in university.json.",
    "faculty": "Doctoral School of Law, Political, Administrative & Economic Sciences",
    "program_id": "lu-dslpaes-phd-program"
  },
  {
    "record_key": "ul:track:lu-dslpaes-phd-program:5",
    "track_type": "CONCENTRATION",
    "track_name": "Business Administration",
    "track_order": 5,
    "is_primary": false,
    "description": null,
    "source_id": "UL-SRC-052",
    "notes": "The doctoral school pages and application announcements cover doctoral preparation in the legal, political, administrative, economic, business, economics, and tourism areas. University-level graduate enrollment fee is centralized in university.json.",
    "faculty": "Doctoral School of Law, Political, Administrative & Economic Sciences",
    "program_id": "lu-dslpaes-phd-program"
  },
  {
    "record_key": "ul:track:lu-dslpaes-phd-program:6",
    "track_type": "CONCENTRATION",
    "track_name": "Economics",
    "track_order": 6,
    "is_primary": false,
    "description": null,
    "source_id": "UL-SRC-052",
    "notes": "The doctoral school pages and application announcements cover doctoral preparation in the legal, political, administrative, economic, business, economics, and tourism areas. University-level graduate enrollment fee is centralized in university.json.",
    "faculty": "Doctoral School of Law, Political, Administrative & Economic Sciences",
    "program_id": "lu-dslpaes-phd-program"
  },
  {
    "record_key": "ul:track:lu-dslpaes-phd-program:7",
    "track_type": "CONCENTRATION",
    "track_name": "Tourism",
    "track_order": 7,
    "is_primary": false,
    "description": null,
    "source_id": "UL-SRC-052",
    "notes": "The doctoral school pages and application announcements cover doctoral preparation in the legal, political, administrative, economic, business, economics, and tourism areas. University-level graduate enrollment fee is centralized in university.json.",
    "faculty": "Doctoral School of Law, Political, Administrative & Economic Sciences",
    "program_id": "lu-dslpaes-phd-program"
  },
  {
    "record_key": "ul:track:lu-dsst-phd-program:1",
    "track_type": "CONCENTRATION",
    "track_name": "Engineering",
    "track_order": 1,
    "is_primary": true,
    "description": null,
    "source_id": "UL-SRC-056",
    "notes": "The internal system page states the PhD is 180 ECTS with research/dissertation allocation. University-level graduate enrollment fee is centralized in university.json.",
    "faculty": "Doctoral School of Science & Technology",
    "program_id": "lu-dsst-phd-program"
  },
  {
    "record_key": "ul:track:lu-dsst-phd-program:2",
    "track_type": "CONCENTRATION",
    "track_name": "Public Health",
    "track_order": 2,
    "is_primary": false,
    "description": null,
    "source_id": "UL-SRC-056",
    "notes": "The internal system page states the PhD is 180 ECTS with research/dissertation allocation. University-level graduate enrollment fee is centralized in university.json.",
    "faculty": "Doctoral School of Science & Technology",
    "program_id": "lu-dsst-phd-program"
  },
  {
    "record_key": "ul:track:lu-dsst-phd-program:3",
    "track_type": "CONCENTRATION",
    "track_name": "Pharmacy",
    "track_order": 3,
    "is_primary": false,
    "description": null,
    "source_id": "UL-SRC-056",
    "notes": "The internal system page states the PhD is 180 ECTS with research/dissertation allocation. University-level graduate enrollment fee is centralized in university.json.",
    "faculty": "Doctoral School of Science & Technology",
    "program_id": "lu-dsst-phd-program"
  },
  {
    "record_key": "ul:track:lu-dsst-phd-program:4",
    "track_type": "CONCENTRATION",
    "track_name": "Science",
    "track_order": 4,
    "is_primary": false,
    "description": null,
    "source_id": "UL-SRC-056",
    "notes": "The internal system page states the PhD is 180 ECTS with research/dissertation allocation. University-level graduate enrollment fee is centralized in university.json.",
    "faculty": "Doctoral School of Science & Technology",
    "program_id": "lu-dsst-phd-program"
  },
  {
    "record_key": "ul:track:lu-dsst-phd-program:5",
    "track_type": "CONCENTRATION",
    "track_name": "Technology",
    "track_order": 5,
    "is_primary": false,
    "description": null,
    "source_id": "UL-SRC-056",
    "notes": "The internal system page states the PhD is 180 ECTS with research/dissertation allocation. University-level graduate enrollment fee is centralized in university.json.",
    "faculty": "Doctoral School of Science & Technology",
    "program_id": "lu-dsst-phd-program"
  },
  {
    "record_key": "ul:track:lu-dsst-phd-program:6",
    "track_type": "CONCENTRATION",
    "track_name": "Medical Sciences",
    "track_order": 6,
    "is_primary": false,
    "description": null,
    "source_id": "UL-SRC-056",
    "notes": "The internal system page states the PhD is 180 ECTS with research/dissertation allocation. University-level graduate enrollment fee is centralized in university.json.",
    "faculty": "Doctoral School of Science & Technology",
    "program_id": "lu-dsst-phd-program"
  },
  {
    "record_key": "ul:track:lu-dsst-phd-program:7",
    "track_type": "CONCENTRATION",
    "track_name": "Dental Medicine",
    "track_order": 7,
    "is_primary": false,
    "description": null,
    "source_id": "UL-SRC-056",
    "notes": "The internal system page states the PhD is 180 ECTS with research/dissertation allocation. University-level graduate enrollment fee is centralized in university.json.",
    "faculty": "Doctoral School of Science & Technology",
    "program_id": "lu-dsst-phd-program"
  },
  {
    "record_key": "ul:track:lu-dsst-phd-program:8",
    "track_type": "CONCENTRATION",
    "track_name": "Fine Arts and Architecture",
    "track_order": 8,
    "is_primary": false,
    "description": null,
    "source_id": "UL-SRC-056",
    "notes": "The internal system page states the PhD is 180 ECTS with research/dissertation allocation. University-level graduate enrollment fee is centralized in university.json.",
    "faculty": "Doctoral School of Science & Technology",
    "program_id": "lu-dsst-phd-program"
  },
  {
    "record_key": "ul:track:lu-dsst-phd-program:9",
    "track_type": "CONCENTRATION",
    "track_name": "Agronomy",
    "track_order": 9,
    "is_primary": false,
    "description": null,
    "source_id": "UL-SRC-056",
    "notes": "The internal system page states the PhD is 180 ECTS with research/dissertation allocation. University-level graduate enrollment fee is centralized in university.json.",
    "faculty": "Doctoral School of Science & Technology",
    "program_id": "lu-dsst-phd-program"
  }
]$UL_TRACKS$) AS x(
        record_key TEXT,
        track_type TEXT,
        track_name TEXT,
        track_order INTEGER,
        is_primary BOOLEAN,
        description TEXT,
        source_id TEXT,
        notes TEXT,
        faculty TEXT,
        program_id TEXT
    )
    JOIN university_faculty fac ON fac.university_id = v_university_id AND fac.name = x.faculty
    JOIN graduate_program gp ON gp.university_id = v_university_id AND gp.program_key = x.program_id
    JOIN ul_source_seed ss ON ss.source_id = x.source_id
    JOIN source s ON s.university_id = v_university_id AND s.url = ss.url
    ON CONFLICT (program_id, track_type, track_name) DO UPDATE SET
        track_order = EXCLUDED.track_order,
        is_primary = EXCLUDED.is_primary,
        description = EXCLUDED.description,
        source_id = EXCLUDED.source_id,
        notes = EXCLUDED.notes,
        updated_at = NOW();

END $$;
