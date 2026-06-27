-- LAU graduate data seed migration.
-- Idempotent import for the canonical LAU graduate dataset.

DO $$
DECLARE
    v_university_id BIGINT;
BEGIN

    INSERT INTO university (name, name_ar, acronym, country, city, latitude, longitude, campus_name, campus_type)
    SELECT 'Lebanese American University', NULL, 'LAU', NULL, NULL, NULL, NULL, NULL, NULL
    WHERE NOT EXISTS (
        SELECT 1 FROM university WHERE name = 'Lebanese American University'
    );

    SELECT id INTO v_university_id
    FROM university
    WHERE name = 'Lebanese American University'
    ORDER BY id
    LIMIT 1;

    INSERT INTO degree_type (code, name)
    VALUES
        ('MASTER', 'Master'),
        ('PHD', 'Doctor of Philosophy')
    ON CONFLICT (code) DO UPDATE SET
        name = EXCLUDED.name,
        updated_at = NOW();

    INSERT INTO language (name, code, native_name)
    VALUES ('English', 'en', 'English')
    ON CONFLICT (code) DO UPDATE SET
        name = EXCLUDED.name,
        native_name = EXCLUDED.native_name;

    CREATE TEMP TABLE lau_source_seed (
        source_id TEXT PRIMARY KEY,
        page_title TEXT NOT NULL,
        url TEXT NOT NULL,
        date_accessed DATE,
        source_type TEXT NOT NULL
    ) ON COMMIT DROP;

    INSERT INTO lau_source_seed (source_id, page_title, url, date_accessed, source_type)
    SELECT source_id, page_title, url, date_accessed, source_type
    FROM jsonb_to_recordset($LAU$[{"source_id":"lau_home","page_title":"Lebanese American University","url":"https://www.lau.edu.lb/","date_accessed":"2026-06-27","source_type":"OTHER"},{"source_id":"lau_graduate_programs","page_title":"Graduate Programs | Study at LAU | LAU","url":"https://www.lau.edu.lb/study/graduate.php","date_accessed":"2026-06-27","source_type":"PROGRAM_LIST"},{"source_id":"lau_gsr_home","page_title":"Graduate Studies and Research | LAU","url":"https://gsr.lau.edu.lb/","date_accessed":"2026-06-26","source_type":"OTHER"},{"source_id":"lau_schools","page_title":"Schools | Study at LAU | LAU","url":"https://www.lau.edu.lb/study/schools.php","date_accessed":"2026-06-26","source_type":"SCHOOL_PAGE"},{"source_id":"lau_academic_calendar","page_title":"Academic Calendar | LAU","url":"https://www.lau.edu.lb/calendar/","date_accessed":"2026-06-27","source_type":"DEADLINE"},{"source_id":"lau_fees_2026_2027","page_title":"Fees for 2026-2027 | LAU","url":"https://www.lau.edu.lb/fees/2026-2027/","date_accessed":"2026-06-27","source_type":"TUITION_FEES"},{"source_id":"lau_graduate_applicants","page_title":"Graduate Applicants | Apply to LAU","url":"https://www.lau.edu.lb/apply/graduate.php","date_accessed":"2026-06-27","source_type":"ADMISSIONS"},{"source_id":"lau_request_info_grad_fall_2026","page_title":"Request Information - Graduate Admissions Fall 2026 | Study at LAU | LAU","url":"https://www.lau.edu.lb/study/request-information-graduate-admissions-fall-2026.php","date_accessed":"2026-06-27","source_type":"ADMISSIONS"},{"source_id":"lau_financial_aid_grad","page_title":"Scholarship and Financial Support for Graduate Students | Need-Based Financial Aid | Apply to LAU","url":"https://www.lau.edu.lb/apply/financial-aid/graduate.php","date_accessed":"2026-06-27","source_type":"FINANCIAL_AID"},{"source_id":"lau_graduate_research_scholarship","page_title":"Graduate Research Scholarship | LAU Research","url":"https://www.lau.edu.lb/research/grs/","date_accessed":"2026-06-27","source_type":"SCHOLARSHIP"},{"source_id":"lau_catalog_grad_rules","page_title":"Academic Rules and Procedures | Academic Catalog 2023–2024 | LAU","url":"https://catalog.lau.edu.lb/2023-2024/graduate/academic-rules-procedures.php","date_accessed":"2026-06-27","source_type":"CATALOG"},{"source_id":"lau_ugc","page_title":"University Graduate and Research Council | Office of the Provost | LAU","url":"https://www.lau.edu.lb/about/governance/provost/people/councils-committees/ugc.php","date_accessed":"2026-06-26","source_type":"OTHER"},{"source_id":"lau_school_sard","page_title":"The LAU School of Architecture and Design","url":"https://sard.lau.edu.lb/","date_accessed":"2026-06-26","source_type":"SCHOOL_PAGE"},{"source_id":"lau_school_soas","page_title":"The LAU School of Arts and Sciences","url":"https://soas.lau.edu.lb/","date_accessed":"2026-06-26","source_type":"SCHOOL_PAGE"},{"source_id":"lau_school_aksob","page_title":"The LAU Adnan Kassar School of Business","url":"https://sb.lau.edu.lb/","date_accessed":"2026-06-26","source_type":"SCHOOL_PAGE"},{"source_id":"lau_school_soe","page_title":"The LAU School of Engineering","url":"https://soe.lau.edu.lb/","date_accessed":"2026-06-26","source_type":"SCHOOL_PAGE"},{"source_id":"lau_school_medicine","page_title":"The LAU Gilbert and Rose-Marie Chagoury School of Medicine","url":"https://medicine.lau.edu.lb/","date_accessed":"2026-06-26","source_type":"SCHOOL_PAGE"},{"source_id":"lau_school_nursing","page_title":"The LAU Alice Ramez Chagoury School of Nursing","url":"https://nursing.lau.edu.lb/","date_accessed":"2026-06-26","source_type":"SCHOOL_PAGE"},{"source_id":"lau_school_pharmacy","page_title":"The LAU School of Pharmacy","url":"https://pharmacy.lau.edu.lb/","date_accessed":"2026-06-26","source_type":"SCHOOL_PAGE"},{"source_id":"lau_prog_emba","page_title":"Executive MBA and Executive Certificates  |  The LAU Adnan Kassar School of Business","url":"https://sb.lau.edu.lb/academics/programs/emba/","date_accessed":"2026-06-27","source_type":"PROGRAM_PAGE"},{"source_id":"lau_prog_ma_applied_economics","page_title":"Master of Arts (MA) in Applied Economics |  The LAU Adnan Kassar School of Business","url":"https://sb.lau.edu.lb/academics/programs/ma-applied-economics/","date_accessed":"2026-06-27","source_type":"PROGRAM_PAGE"},{"source_id":"lau_prog_mba","page_title":"Master of Business Administration (MBA) |  The LAU Adnan Kassar School of Business","url":"https://sb.lau.edu.lb/academics/programs/mba/","date_accessed":"2026-06-27","source_type":"PROGRAM_PAGE"},{"source_id":"lau_prog_llm","page_title":"Master of Laws |  The LAU Adnan Kassar School of Business","url":"https://sb.lau.edu.lb/academics/programs/llm/","date_accessed":"2026-06-27","source_type":"PROGRAM_PAGE"},{"source_id":"lau_prog_ms_data_analytics","page_title":"Master of Science in Data Analytics |  The LAU Adnan Kassar School of Business","url":"https://sb.lau.edu.lb/academics/programs/ms-data-analytics/master-of-science-in-data-analytics.php","date_accessed":"2026-06-27","source_type":"PROGRAM_PAGE"},{"source_id":"lau_prog_mshr","page_title":"Master of Science in Human Resources Management |  The LAU Adnan Kassar School of Business","url":"https://sb.lau.edu.lb/academics/programs/mshr/","date_accessed":"2026-06-27","source_type":"PROGRAM_PAGE"},{"source_id":"lau_prog_msl","page_title":"Master of Studies in Law (MSL) |  The LAU Adnan Kassar School of Business","url":"https://sb.lau.edu.lb/academics/programs/msl/","date_accessed":"2026-06-27","source_type":"PROGRAM_PAGE"},{"source_id":"lau_prog_online_mba_business_analytics","page_title":"Online MBA in Business Analytics - Business Analytics Master's Degree - LAU","url":"https://online.lau.edu.lb/programs/mba-masters-business-analytics/","date_accessed":"2026-06-27","source_type":"PROGRAM_PAGE"},{"source_id":"lau_prog_online_mba_global_business_admin","page_title":"Online MBA in Global Business Administration - LAU Online","url":"https://online.lau.edu.lb/programs/mba-masters-business-administration/","date_accessed":"2026-06-27","source_type":"PROGRAM_PAGE"},{"source_id":"lau_prog_online_mba_healthcare_mgmt","page_title":"Online MBA Master's in Healthcare Management - LAU Online","url":"https://online.lau.edu.lb/programs/mba-masters-healthcare-management/","date_accessed":"2026-06-27","source_type":"PROGRAM_PAGE"},{"source_id":"lau_prog_ma_islamic_art","page_title":"MA in Islamic Art |  The LAU School of Architecture & Design","url":"https://sard.lau.edu.lb/academics/degrees-minors/ma-islamic-art/","date_accessed":"2026-06-27","source_type":"PROGRAM_PAGE"},{"source_id":"lau_prog_ma_comparative_literature","page_title":"MA in Comparative Literature |  The LAU School of Arts and Sciences","url":"https://soas.lau.edu.lb/academics/programs/ma-comparative-literature.php","date_accessed":"2026-06-27","source_type":"PROGRAM_PAGE"},{"source_id":"lau_prog_ma_education","page_title":"MA in Education |  The LAU School of Arts and Sciences","url":"https://soas.lau.edu.lb/academics/programs/ma-education.php","date_accessed":"2026-06-27","source_type":"PROGRAM_PAGE"},{"source_id":"lau_prog_ma_interdisciplinary_gender_studies","page_title":"MA in Interdisciplinary Gender Studies  |  The LAU School of Arts and Sciences","url":"https://soas.lau.edu.lb/academics/programs/ma-interdisciplinary-gender-studies.php","date_accessed":"2026-06-27","source_type":"PROGRAM_PAGE"},{"source_id":"lau_prog_ma_international_affairs","page_title":"MA in International Affairs |  The LAU School of Arts and Sciences","url":"https://soas.lau.edu.lb/academics/programs/ma-international-affairs.php","date_accessed":"2026-06-27","source_type":"PROGRAM_PAGE"},{"source_id":"lau_prog_ma_migration_studies","page_title":"MA in Migration Studies |  The LAU School of Arts and Sciences","url":"https://soas.lau.edu.lb/academics/programs/ma-migrationstudies.php","date_accessed":"2026-06-27","source_type":"PROGRAM_PAGE"},{"source_id":"lau_prog_ma_multimedia_journalism","page_title":"MA in Multimedia Journalism |  The LAU School of Arts and Sciences","url":"https://soas.lau.edu.lb/academics/programs/ma-multimedia-journalism.php","date_accessed":"2026-06-27","source_type":"PROGRAM_PAGE"},{"source_id":"lau_prog_ms_ai_online","page_title":"MS in Applied Artificial Intelligence - Applied AI Degree - LAU","url":"https://online.lau.edu.lb/programs/ms-masters-applied-artificial-intelligence/","date_accessed":"2026-06-27","source_type":"PROGRAM_PAGE"},{"source_id":"lau_prog_ms_biological_sciences","page_title":"MS in Biological Sciences |  The LAU School of Arts and Sciences","url":"https://soas.lau.edu.lb/academics/programs/ms-biological-sciences.php","date_accessed":"2026-06-27","source_type":"PROGRAM_PAGE"},{"source_id":"lau_prog_ms_computer_science","page_title":"MS in Computer Science |  The LAU School of Arts and Sciences","url":"https://soas.lau.edu.lb/academics/programs/ms-computer-science.php","date_accessed":"2026-06-27","source_type":"PROGRAM_PAGE"},{"source_id":"lau_prog_online_ms_computer_science","page_title":"Online MS in Computer Science | Lebanese American University","url":"https://online.lau.edu.lb/programs/ms-masters-computer-science/","date_accessed":"2026-06-27","source_type":"PROGRAM_PAGE"},{"source_id":"lau_prog_online_ms_cybersecurity","page_title":"Online Masters in Cyber Security - Lebanese American University","url":"https://online.lau.edu.lb/programs/ms-cyber-security/","date_accessed":"2026-06-27","source_type":"PROGRAM_PAGE"},{"source_id":"lau_prog_ms_data_science","page_title":"MS in Data Science |  The LAU School of Arts and Sciences","url":"https://soas.lau.edu.lb/academics/programs/ms-data-science.php","date_accessed":"2026-06-27","source_type":"PROGRAM_PAGE"},{"source_id":"lau_prog_online_ms_data_science","page_title":"Online Masters in Data Science - Lebanese American University","url":"https://online.lau.edu.lb/programs/ms-data-science/","date_accessed":"2026-06-27","source_type":"PROGRAM_PAGE"},{"source_id":"lau_prog_ms_nutrition","page_title":"MS in Nutrition |  The LAU School of Arts and Sciences","url":"https://soas.lau.edu.lb/academics/programs/ms-nutrition.php","date_accessed":"2026-06-26","source_type":"PROGRAM_PAGE"},{"source_id":"lau_prog_ms_civil_environmental_engineering","page_title":"MS in Civil & Environmental Engineering |  The LAU School of Engineering","url":"https://soe.lau.edu.lb/departments/civil/degree-programs/ms-civil.php","date_accessed":"2026-06-27","source_type":"PROGRAM_PAGE"},{"source_id":"lau_prog_ms_computer_engineering","page_title":"MS in Computer Engineering |  The LAU School of Engineering","url":"https://soe.lau.edu.lb/departments/electrical-computer/degree-programs/ms-computer.php","date_accessed":"2026-06-27","source_type":"PROGRAM_PAGE"},{"source_id":"lau_prog_online_ms_engineering_management","page_title":"Online MS in Engineering Management - Lebanese American University","url":"https://online.lau.edu.lb/programs/ms-engineering-management/","date_accessed":"2026-06-27","source_type":"PROGRAM_PAGE"},{"source_id":"lau_prog_ms_industrial_engineering_management","page_title":"MS in Industrial Engineering & Engineering Management |  The LAU School of Engineering","url":"https://soe.lau.edu.lb/departments/industrial-mechanical/degree-programs/ms-industrial.php","date_accessed":"2026-06-27","source_type":"PROGRAM_PAGE"},{"source_id":"lau_prog_online_ms_international_construction_management","page_title":"Online M.S. International Construction Management Degree - LAU","url":"https://online.lau.edu.lb/programs/ms-masters-international-construction-management/","date_accessed":"2026-06-27","source_type":"PROGRAM_PAGE"},{"source_id":"lau_prog_ms_mechanical_engineering","page_title":"MS in Mechanical Engineering  |  The LAU School of Engineering","url":"https://soe.lau.edu.lb/departments/industrial-mechanical/degree-programs/ms-mechanical.php","date_accessed":"2026-06-27","source_type":"PROGRAM_PAGE"},{"source_id":"lau_prog_md","page_title":"MD Program | The Gilbert and Rose-Marie Chagoury School of Medicine | LAU","url":"https://medicine.lau.edu.lb/education/md/","date_accessed":"2026-06-26","source_type":"PROGRAM_PAGE"},{"source_id":"lau_prog_pharmd","page_title":"Doctor of Pharmacy (Pharm.D.) |  The LAU School of Pharmacy","url":"https://pharmacy.lau.edu.lb/education/graduate/pharmd.php","date_accessed":"2026-06-26","source_type":"PROGRAM_PAGE"},{"source_id":"lau_prog_ms_pharmaceutical_dev_mgmt","page_title":"M.S. Degree in Pharmaceutical Development and Management |  The LAU School of Pharmacy","url":"https://pharmacy.lau.edu.lb/education/graduate/ms-pharma.php","date_accessed":"2026-06-27","source_type":"PROGRAM_PAGE"},{"source_id":"lau_aksob_programs","page_title":"Programs of Study | The LAU Adnan Kassar School of Business","url":"https://sb.lau.edu.lb/academics/programs/","date_accessed":"2026-06-27","source_type":"PROGRAM_LIST"},{"source_id":"lau_online_home","page_title":"Lebanese American University | Flexible, 100% Online Programs","url":"https://online.lau.edu.lb/","date_accessed":"2026-06-27","source_type":"PROGRAM_LIST"},{"source_id":"lau_prog_ms_finance_accounting","page_title":"MS in Finance and Accounting |  The LAU Adnan Kassar School of Business","url":"https://sb.lau.edu.lb/academics/programs/ms-finance-and-accounting/","date_accessed":"2026-06-27","source_type":"PROGRAM_PAGE"}]$LAU$::jsonb) AS src(
        source_id TEXT,
        page_title TEXT,
        url TEXT,
        date_accessed DATE,
        source_type TEXT
    );

    INSERT INTO source (university_id, title, url, source_type, accessed_at)
    SELECT v_university_id, page_title, url, source_type, date_accessed
    FROM lau_source_seed
    ON CONFLICT (university_id, url) DO UPDATE SET
        title = EXCLUDED.title,
        source_type = EXCLUDED.source_type,
        accessed_at = EXCLUDED.accessed_at,
        updated_at = NOW();

    CREATE TEMP TABLE lau_program_seed (
        id TEXT PRIMARY KEY,
        faculty TEXT NOT NULL,
        department TEXT NOT NULL,
        major_category TEXT,
        major TEXT,
        degree_type TEXT NOT NULL,
        official_degree_name TEXT,
        thesis_or_non_thesis TEXT,
        concentrations_or_tracks JSONB,
        credits INTEGER,
        duration_value NUMERIC(10, 2),
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
        source_ids JSONB,
        tuition_academic_year TEXT,
        tuition_currency TEXT,
        tuition_billing_basis TEXT,
        tuition_amount NUMERIC(12, 2),
        tuition_category TEXT,
        tuition_notes TEXT,
        tuition_source_ids JSONB,
        notes TEXT
    ) ON COMMIT DROP;

    INSERT INTO lau_program_seed (
        id, faculty, department, major_category, major, degree_type, official_degree_name,
        thesis_or_non_thesis, concentrations_or_tracks, credits, duration_value, duration_unit,
        duration_raw_text, language, delivery_mode, program_description, admission_requirements,
        gre_requirement, gmat_requirement, english_requirement, interview_requirement,
        experience_requirement, accreditation, official_program_url, source_ids,
        tuition_academic_year, tuition_currency, tuition_billing_basis, tuition_amount,
        tuition_category, tuition_notes, tuition_source_ids, notes
    )
    SELECT
        id, faculty, department, major_category, major, degree_type, official_degree_name,
        thesis_or_non_thesis, concentrations_or_tracks, credits, duration_value, duration_unit,
        duration_raw_text, language, delivery_mode, program_description, admission_requirements,
        gre_requirement, gmat_requirement, english_requirement, interview_requirement,
        experience_requirement, accreditation, official_program_url, source_ids,
        tuition_academic_year, tuition_currency, tuition_billing_basis, tuition_amount,
        tuition_category, tuition_notes, tuition_source_ids, notes
    FROM jsonb_to_recordset($LAU$[{"id":"business-emba","faculty":"Adnan Kassar School of Business","department":"Adnan Kassar School of Business","major_category":"Business","major":"Business Administration","degree_type":"MASTER","official_degree_name":"Executive Master of Business Administration","thesis_or_non_thesis":null,"concentrations_or_tracks":[],"credits":36,"duration_value":null,"duration_unit":null,"duration_raw_text":"1.5-2 years","language":"ENGLISH","delivery_mode":"ON_CAMPUS","program_description":"Executive MBA for experienced professionals, delivered in a flexible weekend modular format.","admission_requirements":"Bachelor's degree from a recognized university; minimum six years of professional experience; English proficiency; interview required.","gre_requirement":null,"gmat_requirement":null,"english_requirement":null,"interview_requirement":"Successful completion of an interview","experience_requirement":"Minimum six years of professional experience","accreditation":null,"official_program_url":"https://sb.lau.edu.lb/academics/programs/emba/","source_ids":["lau_graduate_programs","lau_prog_emba"],"tuition_academic_year":"2026-2027","tuition_currency":"USD","tuition_billing_basis":"PER_CREDIT","tuition_amount":1043,"tuition_category":"Executive M.B.A.","tuition_notes":"Official LAU Fees for 2026-2027 schedule.","tuition_source_ids":["lau_fees_2026_2027"],"notes":"Executive MBA page also lists three embedded executive certificates. All applicants are admitted through the EMBA program, regardless of whether they intend to complete a single executive certificate or continue toward the full EMBA degree. Delivery mode normalized from official page wording \"on-campus (modular format)\"."},{"id":"business-ma-applied-economics","faculty":"Adnan Kassar School of Business","department":"Adnan Kassar School of Business","major_category":"Business","major":"Applied Economics","degree_type":"MASTER","official_degree_name":"MA in Applied Economics","thesis_or_non_thesis":"PROJECT","concentrations_or_tracks":[],"credits":30,"duration_value":2,"duration_unit":"YEARS","duration_raw_text":"two years (full-time)","language":null,"delivery_mode":"ON_CAMPUS","program_description":"Research-intensive MA in Applied Economics focused on advanced economic theory, empirical techniques, data analysis, and policy formulation.","admission_requirements":"LAU general graduate requirements; minimum GPA of 3.0; economics background preferred; applicants from business, mathematics, engineering, technology, or computer science may need 9-12 remedial credits.","gre_requirement":null,"gmat_requirement":null,"english_requirement":null,"interview_requirement":null,"experience_requirement":null,"accreditation":null,"official_program_url":"https://sb.lau.edu.lb/academics/programs/ma-applied-economics/","source_ids":["lau_graduate_programs","lau_aksob_programs","lau_prog_ma_applied_economics"],"tuition_academic_year":"2026-2027","tuition_currency":"USD","tuition_billing_basis":"PER_CREDIT","tuition_amount":1022,"tuition_category":"Applied Economics","tuition_notes":"Official LAU Fees for 2026-2027 schedule.","tuition_source_ids":["lau_fees_2026_2027"],"notes":"Start term: Fall or Spring."},{"id":"business-mba","faculty":"Adnan Kassar School of Business","department":"Adnan Kassar School of Business","major_category":"Business","major":"Business Administration","degree_type":"MASTER","official_degree_name":"MBA (Master of Business Administration)","thesis_or_non_thesis":null,"concentrations_or_tracks":[],"credits":39,"duration_value":null,"duration_unit":null,"duration_raw_text":null,"language":null,"delivery_mode":null,"program_description":"MBA program for students with business and non-business backgrounds, with core courses, electives, and a thesis option in the curriculum.","admission_requirements":"Bachelor's degree in business studies or equivalent from a recognized university; 3.0 GPA; letters of recommendation; interview if required.","gre_requirement":null,"gmat_requirement":null,"english_requirement":null,"interview_requirement":"Interview if required","experience_requirement":"Relevant professional experience is considered an asset, but is not a prerequisite.","accreditation":null,"official_program_url":"https://sb.lau.edu.lb/academics/programs/mba/","source_ids":["lau_graduate_programs","lau_aksob_programs","lau_prog_mba"],"tuition_academic_year":"2026-2027","tuition_currency":"USD","tuition_billing_basis":"PER_CREDIT","tuition_amount":1022,"tuition_category":"Business Administration","tuition_notes":"Official LAU Fees for 2026-2027 schedule.","tuition_source_ids":["lau_fees_2026_2027"],"notes":"Admissions take place in fall, spring, and summer semesters; part-time study is available."},{"id":"business-llm-business-law","faculty":"Adnan Kassar School of Business","department":"Adnan Kassar School of Business","major_category":"Business","major":"Business Law","degree_type":"MASTER","official_degree_name":"LLM in Business Law","thesis_or_non_thesis":null,"concentrations_or_tracks":[],"credits":30,"duration_value":null,"duration_unit":null,"duration_raw_text":null,"language":null,"delivery_mode":null,"program_description":"Graduate law program focused on business and transnational contract law with theoretical and practical training.","admission_requirements":null,"gre_requirement":null,"gmat_requirement":null,"english_requirement":null,"interview_requirement":null,"experience_requirement":null,"accreditation":null,"official_program_url":"https://sb.lau.edu.lb/academics/programs/llm/","source_ids":["lau_graduate_programs","lau_aksob_programs","lau_prog_llm"],"tuition_academic_year":"2026-2027","tuition_currency":"USD","tuition_billing_basis":"PER_CREDIT","tuition_amount":1022,"tuition_category":"Business Law","tuition_notes":"Official LAU Fees for 2026-2027 schedule.","tuition_source_ids":["lau_fees_2026_2027"],"notes":"Two academic years; 30 credits; Beirut campus."},{"id":"business-ms-finance-and-accounting","faculty":"Adnan Kassar School of Business","department":"Adnan Kassar School of Business","major_category":"Business","major":"Finance and Accounting","degree_type":"MASTER","official_degree_name":"MS in Finance and Accounting","thesis_or_non_thesis":null,"concentrations_or_tracks":[],"credits":30,"duration_value":null,"duration_unit":null,"duration_raw_text":null,"language":null,"delivery_mode":null,"program_description":null,"admission_requirements":null,"gre_requirement":null,"gmat_requirement":null,"english_requirement":null,"interview_requirement":null,"experience_requirement":null,"accreditation":null,"official_program_url":"https://sb.lau.edu.lb/academics/programs/ms-finance-and-accounting/","source_ids":["lau_graduate_programs","lau_aksob_programs","lau_prog_ms_finance_accounting"],"tuition_academic_year":"2026-2027","tuition_currency":"USD","tuition_billing_basis":"PER_CREDIT","tuition_amount":1022,"tuition_category":"Finance and Accounting","tuition_notes":"Official LAU Fees for 2026-2027 schedule.","tuition_source_ids":["lau_fees_2026_2027"],"notes":"Official graduate listing surfaces the program; dedicated page URL was not separately available in this pass."},{"id":"business-ms-data-analytics","faculty":"Adnan Kassar School of Business","department":"Adnan Kassar School of Business","major_category":"Business","major":"Data Analytics","degree_type":"MASTER","official_degree_name":"MS in Data Analytics","thesis_or_non_thesis":"THESIS_OR_PROJECT","concentrations_or_tracks":[],"credits":30,"duration_value":2,"duration_unit":"YEARS","duration_raw_text":"two years (full-time)","language":null,"delivery_mode":"ON_CAMPUS","program_description":"Interdisciplinary business analytics program at the intersection of information technology, operations management, applied mathematics, and statistics.","admission_requirements":"Bachelor's degree in a related field; minimum GPA of 3.2; statistics and programming coursework or readiness; related professional experience is valued but not required.","gre_requirement":null,"gmat_requirement":null,"english_requirement":null,"interview_requirement":null,"experience_requirement":"Relevant professional experience is valued but not required.","accreditation":null,"official_program_url":"https://sb.lau.edu.lb/academics/programs/ms-data-analytics/master-of-science-in-data-analytics.php","source_ids":["lau_graduate_programs","lau_aksob_programs","lau_prog_ms_data_analytics"],"tuition_academic_year":"2026-2027","tuition_currency":"USD","tuition_billing_basis":"PER_CREDIT","tuition_amount":1022,"tuition_category":"Data Analytics","tuition_notes":"Official LAU Fees for 2026-2027 schedule.","tuition_source_ids":["lau_fees_2026_2027"],"notes":"Start term: Fall or Spring."},{"id":"business-ms-human-resources-management","faculty":"Adnan Kassar School of Business","department":"Adnan Kassar School of Business","major_category":"Business","major":"Human Resources Management","degree_type":"MASTER","official_degree_name":"MS in Human Resources Management","thesis_or_non_thesis":null,"concentrations_or_tracks":[],"credits":30,"duration_value":null,"duration_unit":null,"duration_raw_text":null,"language":null,"delivery_mode":null,"program_description":"Graduate HRM program covering strategic human resources management, HR functions, and human capital evaluation.","admission_requirements":"Applicants are expected to have a bachelor's degree in business studies or equivalent, plus English proficiency as per LAU rules; letters of recommendation and an interview may be required.","gre_requirement":null,"gmat_requirement":null,"english_requirement":null,"interview_requirement":"Interview if required","experience_requirement":null,"accreditation":null,"official_program_url":"https://sb.lau.edu.lb/academics/programs/mshr/","source_ids":["lau_graduate_programs","lau_aksob_programs","lau_prog_mshr"],"tuition_academic_year":"2026-2027","tuition_currency":"USD","tuition_billing_basis":"PER_CREDIT","tuition_amount":1022,"tuition_category":"Human Resources Management","tuition_notes":"Official LAU Fees for 2026-2027 schedule.","tuition_source_ids":["lau_fees_2026_2027"],"notes":"Admissions are selective and competitive; applicants from non-business backgrounds complete 6 remedial credits."},{"id":"business-msl","faculty":"Adnan Kassar School of Business","department":"Adnan Kassar School of Business","major_category":"Business","major":"Law","degree_type":"MASTER","official_degree_name":"Master of Studies in Law","thesis_or_non_thesis":"THESIS_OR_PROJECT","concentrations_or_tracks":[],"credits":null,"duration_value":null,"duration_unit":null,"duration_raw_text":null,"language":null,"delivery_mode":null,"program_description":"Professional graduate law program for candidates without a law degree who want to understand legal principles and apply them in their field.","admission_requirements":null,"gre_requirement":null,"gmat_requirement":null,"english_requirement":null,"interview_requirement":null,"experience_requirement":null,"accreditation":null,"official_program_url":"https://sb.lau.edu.lb/academics/programs/msl/","source_ids":["lau_graduate_programs","lau_aksob_programs","lau_prog_msl"],"tuition_academic_year":"2026-2027","tuition_currency":"USD","tuition_billing_basis":"PER_CREDIT","tuition_amount":1022,"tuition_category":"Master of Studies in Law","tuition_notes":"Official LAU Fees for 2026-2027 schedule.","tuition_source_ids":["lau_fees_2026_2027"],"notes":"Graduate listing shows a 30–39 credit range, which is not stored as a single integer here. This is a 30- to 39-credit professional program completed in two academic years; thesis or project option is available."},{"id":"business-mba-business-analytics-online","faculty":"Adnan Kassar School of Business","department":"Adnan Kassar School of Business","major_category":"Business","major":"Business Analytics","degree_type":"MASTER","official_degree_name":"MBA in Business Analytics","thesis_or_non_thesis":null,"concentrations_or_tracks":[],"credits":33,"duration_value":2,"duration_unit":"YEARS","duration_raw_text":"2 years","language":"ENGLISH","delivery_mode":"ONLINE","program_description":"100% online MBA with a business analytics specialization, built around data-driven decision-making and applied analytics.","admission_requirements":"Bachelor's degree from a recognized university; minimum GPA of 2.5; CV or resume; personal statement; at least one letter of recommendation; work experience recommended; interview may be requested; English test required if English is not the first language of instruction.","gre_requirement":"Not required","gmat_requirement":"Not required","english_requirement":null,"interview_requirement":"Applicants may be asked for an interview","experience_requirement":"Work experience recommended.","accreditation":"AACSB; NECHE; New York State Education Department (Distance Education Format)","official_program_url":"https://online.lau.edu.lb/programs/mba-masters-business-analytics/","source_ids":["lau_graduate_programs","lau_online_home","lau_prog_online_mba_business_analytics"],"tuition_academic_year":"2026-2027","tuition_currency":"USD","tuition_billing_basis":"PER_CREDIT","tuition_amount":580,"tuition_category":"Online Master’s Programs","tuition_notes":"Official LAU Fees for 2026-2027 schedule.","tuition_source_ids":["lau_fees_2026_2027"],"notes":"Online offering. No prior analytics experience required; optional New York residency available; five intakes per year."},{"id":"business-mba-global-business-administration-online","faculty":"Adnan Kassar School of Business","department":"Adnan Kassar School of Business","major_category":"Business","major":"Global Business Administration","degree_type":"MASTER","official_degree_name":"MBA in Global Business Administration","thesis_or_non_thesis":null,"concentrations_or_tracks":[],"credits":33,"duration_value":2,"duration_unit":"YEARS","duration_raw_text":"2 years","language":"ENGLISH","delivery_mode":"ONLINE","program_description":"100% online MBA with a global business administration focus and customizable electives.","admission_requirements":"Bachelor's degree from a recognized university; minimum GPA of 2.5; CV or resume; personal statement; at least one letter of recommendation; work experience recommended; interview may be requested; English test required if English is not the first language of instruction.","gre_requirement":"Not required","gmat_requirement":"Not required","english_requirement":null,"interview_requirement":"Applicants may be asked for an interview","experience_requirement":"Work experience recommended.","accreditation":"AACSB; NECHE; New York State Education Department (Distance Education Format)","official_program_url":"https://online.lau.edu.lb/programs/mba-masters-business-administration/","source_ids":["lau_graduate_programs","lau_online_home","lau_prog_online_mba_global_business_admin"],"tuition_academic_year":"2026-2027","tuition_currency":"USD","tuition_billing_basis":"PER_CREDIT","tuition_amount":580,"tuition_category":"Online Master’s Programs","tuition_notes":"Official LAU Fees for 2026-2027 schedule.","tuition_source_ids":["lau_fees_2026_2027"],"notes":"Online offering. Optional New York residency available; five intakes per year; accelerated study option available."},{"id":"business-mba-healthcare-management-online","faculty":"Adnan Kassar School of Business","department":"Adnan Kassar School of Business","major_category":"Business","major":"Healthcare Management","degree_type":"MASTER","official_degree_name":"MBA in Healthcare Management","thesis_or_non_thesis":null,"concentrations_or_tracks":[],"credits":33,"duration_value":2,"duration_unit":"YEARS","duration_raw_text":"2 years","language":"ENGLISH","delivery_mode":"ONLINE","program_description":"100% online MBA tailored to healthcare management and leadership.","admission_requirements":"Bachelor's degree from a recognized university; minimum GPA of 2.5; CV or resume; personal statement; at least one letter of recommendation; work experience recommended; interview may be requested; English test required if English is not the first language of instruction.","gre_requirement":"Not required","gmat_requirement":"Not required","english_requirement":null,"interview_requirement":"Applicants may be asked for an interview","experience_requirement":"Work experience recommended.","accreditation":"AACSB; NECHE; New York State Education Department (Distance Education Format)","official_program_url":"https://online.lau.edu.lb/programs/mba-masters-healthcare-management/","source_ids":["lau_graduate_programs","lau_online_home","lau_prog_online_mba_healthcare_mgmt"],"tuition_academic_year":"2026-2027","tuition_currency":"USD","tuition_billing_basis":"PER_CREDIT","tuition_amount":580,"tuition_category":"Online Master’s Programs","tuition_notes":"Official LAU Fees for 2026-2027 schedule.","tuition_source_ids":["lau_fees_2026_2027"],"notes":"Online offering. Optional New York residency available; five intakes per year; accelerated study option available."},{"id":"sard-ma-islamic-art","faculty":"School of Architecture and Design","department":"School of Architecture and Design","major_category":"Architecture and Design","major":"Islamic Art","degree_type":"MASTER","official_degree_name":"MA in Islamic Art","thesis_or_non_thesis":null,"concentrations_or_tracks":[],"credits":30,"duration_value":3,"duration_unit":"SEMESTERS","duration_raw_text":"three semesters","language":null,"delivery_mode":null,"program_description":"Graduate program in Islamic art and architecture that supports research and doctoral preparation.","admission_requirements":null,"gre_requirement":null,"gmat_requirement":null,"english_requirement":null,"interview_requirement":null,"experience_requirement":null,"accreditation":null,"official_program_url":"https://sard.lau.edu.lb/academics/degrees-minors/ma-islamic-art/","source_ids":["lau_graduate_programs","lau_school_sard","lau_prog_ma_islamic_art"],"tuition_academic_year":"2026-2027","tuition_currency":"USD","tuition_billing_basis":"PER_CREDIT","tuition_amount":1004,"tuition_category":"Islamic Art","tuition_notes":"Official LAU Fees for 2026-2027 schedule.","tuition_source_ids":["lau_fees_2026_2027"],"notes":"30 credits; scholarship opportunities are available through the Mu’taz and Rada Sawwaf Scholarship Fund."},{"id":"soas-ma-comparative-literature","faculty":"School of Arts and Sciences","department":"School of Arts and Sciences","major_category":"Arts and Sciences","major":"Comparative Literature","degree_type":"MASTER","official_degree_name":"MA in Comparative Literature","thesis_or_non_thesis":"THESIS","concentrations_or_tracks":[],"credits":33,"duration_value":2,"duration_unit":"YEARS","duration_raw_text":"two years","language":null,"delivery_mode":null,"program_description":"Interdisciplinary comparative literature program studying texts and cultural productions across languages and cultures.","admission_requirements":null,"gre_requirement":null,"gmat_requirement":null,"english_requirement":null,"interview_requirement":null,"experience_requirement":null,"accreditation":null,"official_program_url":"https://soas.lau.edu.lb/academics/programs/ma-comparative-literature.php","source_ids":["lau_graduate_programs","lau_school_soas","lau_prog_ma_comparative_literature"],"tuition_academic_year":"2026-2027","tuition_currency":"USD","tuition_billing_basis":"PER_CREDIT","tuition_amount":903,"tuition_category":"Comparative Literature","tuition_notes":"Official LAU Fees for 2026-2027 schedule.","tuition_source_ids":["lau_fees_2026_2027"],"notes":"33 credits; three areas of advanced study; includes a six-credit thesis."},{"id":"soas-ma-education","faculty":"School of Arts and Sciences","department":"School of Arts and Sciences","major_category":"Arts and Sciences","major":"Education","degree_type":"MASTER","official_degree_name":"MA in Education","thesis_or_non_thesis":"THESIS_OR_PROJECT","concentrations_or_tracks":["Teaching English to Speakers of Other Languages (TESOL)","Science, Technology, Engineering, and Mathematics (STEM) Education","Educational Leadership and Policy Studies"],"credits":30,"duration_value":null,"duration_unit":null,"duration_raw_text":null,"language":null,"delivery_mode":null,"program_description":"Master of Arts in Education with specialized emphases in TESOL, STEM Education, and Educational Leadership and Policy Studies.","admission_requirements":null,"gre_requirement":null,"gmat_requirement":null,"english_requirement":null,"interview_requirement":null,"experience_requirement":null,"accreditation":null,"official_program_url":"https://soas.lau.edu.lb/academics/programs/ma-education.php","source_ids":["lau_graduate_programs","lau_school_soas","lau_prog_ma_education"],"tuition_academic_year":"2026-2027","tuition_currency":"USD","tuition_billing_basis":"PER_CREDIT","tuition_amount":903,"tuition_category":"Education","tuition_notes":"Official LAU Fees for 2026-2027 schedule.","tuition_source_ids":["lau_fees_2026_2027"],"notes":"30 credits; the capstone can be a thesis or project."},{"id":"soas-ma-interdisciplinary-gender-studies","faculty":"School of Arts and Sciences","department":"School of Arts and Sciences","major_category":"Arts and Sciences","major":"Interdisciplinary Gender Studies","degree_type":"MASTER","official_degree_name":"MA in Interdisciplinary Gender Studies","thesis_or_non_thesis":"THESIS_OR_PROJECT","concentrations_or_tracks":[],"credits":30,"duration_value":null,"duration_unit":null,"duration_raw_text":null,"language":null,"delivery_mode":null,"program_description":"Interdisciplinary graduate degree focused on gender, equality, human rights, and social justice.","admission_requirements":null,"gre_requirement":null,"gmat_requirement":null,"english_requirement":null,"interview_requirement":null,"experience_requirement":null,"accreditation":null,"official_program_url":"https://soas.lau.edu.lb/academics/programs/ma-interdisciplinary-gender-studies.php","source_ids":["lau_graduate_programs","lau_school_soas","lau_prog_ma_interdisciplinary_gender_studies"],"tuition_academic_year":"2026-2027","tuition_currency":"USD","tuition_billing_basis":"PER_CREDIT","tuition_amount":903,"tuition_category":"Interdisciplinary Gender Studies","tuition_notes":"Official LAU Fees for 2026-2027 schedule.","tuition_source_ids":["lau_fees_2026_2027"],"notes":"30 credits; students complete either a thesis or a professional placement with project."},{"id":"soas-ma-international-affairs","faculty":"School of Arts and Sciences","department":"School of Arts and Sciences","major_category":"Arts and Sciences","major":"International Affairs","degree_type":"MASTER","official_degree_name":"MA in International Affairs","thesis_or_non_thesis":"THESIS","concentrations_or_tracks":[],"credits":30,"duration_value":2,"duration_unit":"YEARS","duration_raw_text":"two years","language":null,"delivery_mode":null,"program_description":"Graduate program preparing professionals and leaders for national and international institutions with training in the theoretical, practical, legal, and ethical dimensions of international relations.","admission_requirements":null,"gre_requirement":null,"gmat_requirement":null,"english_requirement":null,"interview_requirement":null,"experience_requirement":null,"accreditation":null,"official_program_url":"https://soas.lau.edu.lb/academics/programs/ma-international-affairs.php","source_ids":["lau_graduate_programs","lau_school_soas","lau_prog_ma_international_affairs"],"tuition_academic_year":"2026-2027","tuition_currency":"USD","tuition_billing_basis":"PER_CREDIT","tuition_amount":903,"tuition_category":"International Affairs","tuition_notes":"Official LAU Fees for 2026-2027 schedule.","tuition_source_ids":["lau_fees_2026_2027"],"notes":"30 credits; Middle East area studies emphasis is available."},{"id":"soas-ma-migration-studies","faculty":"School of Arts and Sciences","department":"School of Arts and Sciences","major_category":"Arts and Sciences","major":"Migration Studies","degree_type":"MASTER","official_degree_name":"MA in Migration Studies","thesis_or_non_thesis":"THESIS","concentrations_or_tracks":[],"credits":30,"duration_value":2,"duration_unit":"YEARS","duration_raw_text":"two years","language":null,"delivery_mode":null,"program_description":"Interdisciplinary graduate degree in migration studies with a focus on the Middle East.","admission_requirements":null,"gre_requirement":null,"gmat_requirement":null,"english_requirement":null,"interview_requirement":null,"experience_requirement":null,"accreditation":null,"official_program_url":"https://soas.lau.edu.lb/academics/programs/ma-migrationstudies.php","source_ids":["lau_graduate_programs","lau_school_soas","lau_prog_ma_migration_studies"],"tuition_academic_year":"2026-2027","tuition_currency":"USD","tuition_billing_basis":"PER_CREDIT","tuition_amount":903,"tuition_category":"Migration Studies","tuition_notes":"Official LAU Fees for 2026-2027 schedule.","tuition_source_ids":["lau_fees_2026_2027"],"notes":"30 credits; includes a thesis and a practicum/internship-or-research option among electives."},{"id":"soas-ma-multimedia-journalism","faculty":"School of Arts and Sciences","department":"School of Arts and Sciences","major_category":"Arts and Sciences","major":"Multimedia Journalism","degree_type":"MASTER","official_degree_name":"MA in Multimedia Journalism","thesis_or_non_thesis":"THESIS_OR_PROJECT","concentrations_or_tracks":["Research","Professional"],"credits":32,"duration_value":null,"duration_unit":null,"duration_raw_text":null,"language":null,"delivery_mode":null,"program_description":"Graduate journalism program for experienced journalists and newcomers, with research and professional tracks and a strong multimedia and digital innovation focus.","admission_requirements":null,"gre_requirement":null,"gmat_requirement":null,"english_requirement":null,"interview_requirement":null,"experience_requirement":null,"accreditation":null,"official_program_url":"https://soas.lau.edu.lb/academics/programs/ma-multimedia-journalism.php","source_ids":["lau_graduate_programs","lau_school_soas","lau_prog_ma_multimedia_journalism"],"tuition_academic_year":"2026-2027","tuition_currency":"USD","tuition_billing_basis":"PER_CREDIT","tuition_amount":943,"tuition_category":"Multimedia Journalism","tuition_notes":"Official LAU Fees for 2026-2027 schedule.","tuition_source_ids":["lau_fees_2026_2027"],"notes":"32 credits; both tracks include a teaching apprenticeship."},{"id":"soas-ms-applied-artificial-intelligence-online","faculty":"School of Arts and Sciences","department":"School of Arts and Sciences","major_category":"Arts and Sciences","major":"Applied Artificial Intelligence","degree_type":"MASTER","official_degree_name":"MS in Applied Artificial Intelligence","thesis_or_non_thesis":null,"concentrations_or_tracks":[],"credits":30,"duration_value":2,"duration_unit":"YEARS","duration_raw_text":"2 years","language":"ENGLISH","delivery_mode":"ONLINE","program_description":"100% online applied artificial intelligence program with customizable electives across business, healthcare, and digital humanities.","admission_requirements":"Bachelor's degree from a recognized university with a minimum cumulative GPA of 2.5; official transcripts; CV or resume; personal statement; English test required if English is not the language of instruction.","gre_requirement":null,"gmat_requirement":null,"english_requirement":null,"interview_requirement":null,"experience_requirement":"No AI experience required; adequate professional experience may be considered if GPA is below 2.5.","accreditation":"NECHE; New York State Education Department (Distance Education Format)","official_program_url":"https://online.lau.edu.lb/programs/ms-masters-applied-artificial-intelligence/","source_ids":["lau_graduate_programs","lau_online_home","lau_prog_ms_ai_online"],"tuition_academic_year":"2026-2027","tuition_currency":"USD","tuition_billing_basis":"PER_CREDIT","tuition_amount":580,"tuition_category":"Online Master’s Programs","tuition_notes":"Official LAU Fees for 2026-2027 schedule.","tuition_source_ids":["lau_fees_2026_2027"],"notes":"Online offering. 30 credits; asynchronous online format; certificate option available; no application fee."},{"id":"soas-ms-biological-sciences","faculty":"School of Arts and Sciences","department":"School of Arts and Sciences","major_category":"Arts and Sciences","major":"Biological Sciences","degree_type":"MASTER","official_degree_name":"MS in Biological Sciences","thesis_or_non_thesis":"THESIS","concentrations_or_tracks":[],"credits":30,"duration_value":2,"duration_unit":"YEARS","duration_raw_text":"two years","language":null,"delivery_mode":null,"program_description":"Research-intensive MS in modern and applied biological sciences with four main research niche areas.","admission_requirements":null,"gre_requirement":null,"gmat_requirement":null,"english_requirement":null,"interview_requirement":null,"experience_requirement":null,"accreditation":null,"official_program_url":"https://soas.lau.edu.lb/academics/programs/ms-biological-sciences.php","source_ids":["lau_graduate_programs","lau_school_soas","lau_prog_ms_biological_sciences"],"tuition_academic_year":"2026-2027","tuition_currency":"USD","tuition_billing_basis":"PER_CREDIT","tuition_amount":943,"tuition_category":"Biological Sciences","tuition_notes":"Official LAU Fees for 2026-2027 schedule.","tuition_source_ids":["lau_fees_2026_2027"],"notes":"30 credits; graduate assistantships may cover up to 100% of tuition."},{"id":"soas-ms-computer-science","faculty":"School of Arts and Sciences","department":"School of Arts and Sciences","major_category":"Arts and Sciences","major":"Computer Science","degree_type":"MASTER","official_degree_name":"MS in Computer Science","thesis_or_non_thesis":"THESIS_OR_PROJECT","concentrations_or_tracks":["Algorithms and Theory","Systems","Hardware and Networks","Software Engineering"],"credits":30,"duration_value":2,"duration_unit":"YEARS","duration_raw_text":"two years","language":null,"delivery_mode":null,"program_description":"Graduate computer science program offering a broad foundation plus four concentration areas.","admission_requirements":null,"gre_requirement":null,"gmat_requirement":null,"english_requirement":null,"interview_requirement":null,"experience_requirement":null,"accreditation":null,"official_program_url":"https://soas.lau.edu.lb/academics/programs/ms-computer-science.php","source_ids":["lau_graduate_programs","lau_school_soas","lau_prog_ms_computer_science"],"tuition_academic_year":"2026-2027","tuition_currency":"USD","tuition_billing_basis":"PER_CREDIT","tuition_amount":943,"tuition_category":"Computer Science","tuition_notes":"Official LAU Fees for 2026-2027 schedule.","tuition_source_ids":["lau_fees_2026_2027"],"notes":"30 credits; courses are offered at night; the program includes a thesis or project option."},{"id":"soas-ms-computer-science-online","faculty":"School of Arts and Sciences","department":"School of Arts and Sciences","major_category":"Arts and Sciences","major":"Computer Science","degree_type":"MASTER","official_degree_name":"MS in Computer Science","thesis_or_non_thesis":null,"concentrations_or_tracks":[],"credits":30,"duration_value":2,"duration_unit":"YEARS","duration_raw_text":"2 years","language":"ENGLISH","delivery_mode":"ONLINE","program_description":"100% online computer science program with customizable electives and an application-oriented curriculum.","admission_requirements":"Bachelor's degree from a recognized university; minimum GPA of 2.75; official transcripts; CV or resume; personal statement; English test required if English is not the first language of instruction.","gre_requirement":null,"gmat_requirement":"Not required","english_requirement":null,"interview_requirement":null,"experience_requirement":null,"accreditation":"NECHE; New York State Education Department (Distance Education Format)","official_program_url":"https://online.lau.edu.lb/programs/ms-masters-computer-science/","source_ids":["lau_graduate_programs","lau_online_home","lau_prog_online_ms_computer_science"],"tuition_academic_year":"2026-2027","tuition_currency":"USD","tuition_billing_basis":"PER_CREDIT","tuition_amount":580,"tuition_category":"Online Master’s Programs","tuition_notes":"Official LAU Fees for 2026-2027 schedule.","tuition_source_ids":["lau_fees_2026_2027"],"notes":"Online offering; same academic field as the campus program but a distinct official LAU page and delivery mode. Asynchronous online format; no residency requirement; certificate options available; no application fee."},{"id":"soas-ms-cybersecurity-online","faculty":"School of Arts and Sciences","department":"School of Arts and Sciences","major_category":"Arts and Sciences","major":"Cybersecurity","degree_type":"MASTER","official_degree_name":"MS in Cybersecurity","thesis_or_non_thesis":null,"concentrations_or_tracks":[],"credits":30,"duration_value":2,"duration_unit":"YEARS","duration_raw_text":"2 years","language":"ENGLISH","delivery_mode":"ONLINE","program_description":"100% online cybersecurity program designed to protect organizations from evolving cyber threats.","admission_requirements":"Bachelor's degree from a recognized university; minimum GPA of 2.5; official transcripts; CV or resume; optional personal statement; optional letter of recommendation; English test required if English is not the language of instruction.","gre_requirement":null,"gmat_requirement":null,"english_requirement":null,"interview_requirement":null,"experience_requirement":null,"accreditation":"NECHE; New York State Education Department (Distance Education Format)","official_program_url":"https://online.lau.edu.lb/programs/ms-cyber-security/","source_ids":["lau_graduate_programs","lau_online_home","lau_prog_online_ms_cybersecurity"],"tuition_academic_year":"2026-2027","tuition_currency":"USD","tuition_billing_basis":"PER_CREDIT","tuition_amount":580,"tuition_category":"Online Master’s Programs","tuition_notes":"Official LAU Fees for 2026-2027 schedule.","tuition_source_ids":["lau_fees_2026_2027"],"notes":"Online offering. 30 credits; part-time only; accelerated study option available; no application fee."},{"id":"soas-ms-data-science","faculty":"School of Arts and Sciences","department":"School of Arts and Sciences","major_category":"Arts and Sciences","major":"Data Science","degree_type":"MASTER","official_degree_name":"MS in Data Science","thesis_or_non_thesis":"THESIS_OR_PROJECT","concentrations_or_tracks":[],"credits":30,"duration_value":2,"duration_unit":"YEARS","duration_raw_text":"two years","language":null,"delivery_mode":null,"program_description":"Comprehensive MS in Data Science covering algorithms, data engineering, visualization, machine learning, AI, statistical learning, and Bayesian statistics.","admission_requirements":null,"gre_requirement":null,"gmat_requirement":null,"english_requirement":null,"interview_requirement":null,"experience_requirement":null,"accreditation":null,"official_program_url":"https://soas.lau.edu.lb/academics/programs/ms-data-science.php","source_ids":["lau_graduate_programs","lau_school_soas","lau_prog_ms_data_science"],"tuition_academic_year":"2026-2027","tuition_currency":"USD","tuition_billing_basis":"PER_CREDIT","tuition_amount":943,"tuition_category":"Data Science","tuition_notes":"Official LAU Fees for 2026-2027 schedule.","tuition_source_ids":["lau_fees_2026_2027"],"notes":"30 credits; includes a capstone project or thesis option."},{"id":"soas-ms-data-science-online","faculty":"School of Arts and Sciences","department":"School of Arts and Sciences","major_category":"Arts and Sciences","major":"Data Science","degree_type":"MASTER","official_degree_name":"MS in Data Science","thesis_or_non_thesis":null,"concentrations_or_tracks":[],"credits":30,"duration_value":2,"duration_unit":"YEARS","duration_raw_text":"2 years","language":"ENGLISH","delivery_mode":"ONLINE","program_description":"100% online data science program with multidisciplinary coursework and a part-time format.","admission_requirements":"Bachelor's degree from a recognized university; minimum GPA of 2.5; official transcripts; industry experience recommended, but not required; optional personal statement; optional letter of recommendation; English test required if English is not the language of instruction.","gre_requirement":null,"gmat_requirement":null,"english_requirement":null,"interview_requirement":null,"experience_requirement":"Industry experience recommended, but not required.","accreditation":"NECHE; New York State Education Department (Distance Education Format)","official_program_url":"https://online.lau.edu.lb/programs/ms-data-science/","source_ids":["lau_graduate_programs","lau_online_home","lau_prog_online_ms_data_science"],"tuition_academic_year":"2026-2027","tuition_currency":"USD","tuition_billing_basis":"PER_CREDIT","tuition_amount":580,"tuition_category":"Online Master’s Programs","tuition_notes":"Official LAU Fees for 2026-2027 schedule.","tuition_source_ids":["lau_fees_2026_2027"],"notes":"Online offering; same academic field as the campus program but a distinct official LAU page and delivery mode. Part-time only; asynchronous format; no application fee."},{"id":"soas-ms-nutrition","faculty":"School of Arts and Sciences","department":"School of Arts and Sciences","major_category":"Arts and Sciences","major":"Nutrition","degree_type":"MASTER","official_degree_name":"MS in Nutrition","thesis_or_non_thesis":"THESIS_OR_PROJECT","concentrations_or_tracks":["Clinical and Behavioral Nutrition","Nutrition for Public Health","Food Innovation, Quality, and Sustainability"],"credits":30,"duration_value":2,"duration_unit":"YEARS","duration_raw_text":"two years","language":null,"delivery_mode":null,"program_description":"Graduate nutrition program offering thesis and project options with three specialized tracks.","admission_requirements":null,"gre_requirement":null,"gmat_requirement":null,"english_requirement":null,"interview_requirement":null,"experience_requirement":null,"accreditation":null,"official_program_url":"https://soas.lau.edu.lb/academics/programs/ms-nutrition.php","source_ids":["lau_graduate_programs","lau_school_soas","lau_prog_ms_nutrition"],"tuition_academic_year":"2026-2027","tuition_currency":"USD","tuition_billing_basis":"PER_CREDIT","tuition_amount":943,"tuition_category":"Nutrition","tuition_notes":"Official LAU Fees for 2026-2027 schedule.","tuition_source_ids":["lau_fees_2026_2027"],"notes":"30 credits; graduate assistantships are available; combined Registered Dietitian pathway is mentioned on the page."},{"id":"soe-ms-civil-environmental-engineering","faculty":"School of Engineering","department":"School of Engineering","major_category":"Engineering","major":"Civil and Environmental Engineering","degree_type":"MASTER","official_degree_name":"MS in Civil & Environmental Engineering","thesis_or_non_thesis":"THESIS","concentrations_or_tracks":["Construction Engineering and Management","Environmental and Water Resources Engineering","Geotechnical Engineering","Structural Engineering","Transportation Engineering"],"credits":30,"duration_value":null,"duration_unit":null,"duration_raw_text":null,"language":null,"delivery_mode":null,"program_description":"Research-oriented MS in Civil & Environmental Engineering with five emphasis areas.","admission_requirements":null,"gre_requirement":null,"gmat_requirement":null,"english_requirement":null,"interview_requirement":null,"experience_requirement":null,"accreditation":null,"official_program_url":"https://soe.lau.edu.lb/departments/civil/degree-programs/ms-civil.php","source_ids":["lau_graduate_programs","lau_school_soe","lau_prog_ms_civil_environmental_engineering"],"tuition_academic_year":"2026-2027","tuition_currency":"USD","tuition_billing_basis":"PER_CREDIT","tuition_amount":1032,"tuition_category":"Civil and Environmental Engineering","tuition_notes":"Official LAU Fees for 2026-2027 schedule.","tuition_source_ids":["lau_fees_2026_2027"],"notes":"30 credits; thesis-based; students complete a minimum of one academic year of full-time graduate study or equivalent part-time study."},{"id":"soe-ms-computer-engineering","faculty":"School of Engineering","department":"School of Engineering","major_category":"Engineering","major":"Computer Engineering","degree_type":"MASTER","official_degree_name":"MS in Computer Engineering","thesis_or_non_thesis":"THESIS","concentrations_or_tracks":["Communication and Signal Processing","Integrated Circuits, Electronics, and Control","Electric Power and Energy Systems","Computer Hardware","Computer Software and Networks","AI Systems Engineering"],"credits":30,"duration_value":null,"duration_unit":null,"duration_raw_text":null,"language":null,"delivery_mode":null,"program_description":"Research-oriented MS in Computer Engineering with concentration options across communication, hardware, software, AI, and power systems.","admission_requirements":null,"gre_requirement":null,"gmat_requirement":null,"english_requirement":null,"interview_requirement":null,"experience_requirement":null,"accreditation":null,"official_program_url":"https://soe.lau.edu.lb/departments/electrical-computer/degree-programs/ms-computer.php","source_ids":["lau_graduate_programs","lau_school_soe","lau_prog_ms_computer_engineering"],"tuition_academic_year":"2026-2027","tuition_currency":"USD","tuition_billing_basis":"PER_CREDIT","tuition_amount":1032,"tuition_category":"Computer Engineering","tuition_notes":"Official LAU Fees for 2026-2027 schedule.","tuition_source_ids":["lau_fees_2026_2027"],"notes":"30 credits; thesis required; the page emphasizes research competencies and Byblos campus delivery."},{"id":"soe-ms-engineering-management-online","faculty":"School of Engineering","department":"School of Engineering","major_category":"Engineering","major":"Engineering Management","degree_type":"MASTER","official_degree_name":"MS in Engineering Management","thesis_or_non_thesis":null,"concentrations_or_tracks":[],"credits":30,"duration_value":2,"duration_unit":"YEARS","duration_raw_text":"2 years","language":"ENGLISH","delivery_mode":"ONLINE","program_description":"100% online engineering management program built for leadership in engineering and technology-driven industries.","admission_requirements":null,"gre_requirement":null,"gmat_requirement":null,"english_requirement":null,"interview_requirement":null,"experience_requirement":null,"accreditation":"NECHE; New York State Education Department (Distance Education Format)","official_program_url":"https://online.lau.edu.lb/programs/ms-engineering-management/","source_ids":["lau_graduate_programs","lau_online_home","lau_prog_online_ms_engineering_management"],"tuition_academic_year":"2026-2027","tuition_currency":"USD","tuition_billing_basis":"PER_CREDIT","tuition_amount":580,"tuition_category":"Online Master’s Programs","tuition_notes":"Official LAU Fees for 2026-2027 schedule.","tuition_source_ids":["lau_fees_2026_2027"],"notes":"Online offering. 30 credits; optional New York residency available; five intakes per year; no application fee."},{"id":"soe-ms-industrial-engineering-management","faculty":"School of Engineering","department":"School of Engineering","major_category":"Engineering","major":"Industrial Engineering and Engineering Management","degree_type":"MASTER","official_degree_name":"MS in Industrial Engineering & Engineering Management","thesis_or_non_thesis":"THESIS_OR_PROJECT","concentrations_or_tracks":["Optimization","Production Systems and Manufacturing","Infrastructure and Construction Management","Industrial Management and Economics","Computational Modelling and Data Analytics"],"credits":30,"duration_value":null,"duration_unit":null,"duration_raw_text":null,"language":null,"delivery_mode":null,"program_description":"Interdisciplinary MS in Industrial Engineering and Engineering Management with five concentration areas.","admission_requirements":null,"gre_requirement":null,"gmat_requirement":null,"english_requirement":null,"interview_requirement":null,"experience_requirement":null,"accreditation":null,"official_program_url":"https://soe.lau.edu.lb/departments/industrial-mechanical/degree-programs/ms-industrial.php","source_ids":["lau_graduate_programs","lau_school_soe","lau_prog_ms_industrial_engineering_management"],"tuition_academic_year":"2026-2027","tuition_currency":"USD","tuition_billing_basis":"PER_CREDIT","tuition_amount":1032,"tuition_category":"Industrial Engineering and Engineering Management","tuition_notes":"Official LAU Fees for 2026-2027 schedule.","tuition_source_ids":["lau_fees_2026_2027"],"notes":"30 credit hours; thesis and project tracks are both available."},{"id":"soe-ms-international-construction-management-online","faculty":"School of Engineering","department":"School of Engineering","major_category":"Engineering","major":"International Construction Management","degree_type":"MASTER","official_degree_name":"MS in International Construction Management","thesis_or_non_thesis":null,"concentrations_or_tracks":[],"credits":30,"duration_value":2,"duration_unit":"YEARS","duration_raw_text":"2 years","language":"ENGLISH","delivery_mode":"ONLINE","program_description":"100% online international construction management program focused on global construction practices and technology.","admission_requirements":"Bachelor's degree from a recognized university; minimum GPA of 2.75; official transcripts; minimum 2 years of engineering/construction industry-related experience or 3 years of unrelated industry experience; CV or resume; personal statement; one letter of recommendation; English test required if English is not the first language of instruction.","gre_requirement":null,"gmat_requirement":null,"english_requirement":null,"interview_requirement":null,"experience_requirement":"Minimum 2 years of engineering/construction industry-related experience or 3 years of unrelated industry experience.","accreditation":"NECHE; New York State Education Department (Distance Education Format)","official_program_url":"https://online.lau.edu.lb/programs/ms-masters-international-construction-management/","source_ids":["lau_graduate_programs","lau_online_home","lau_prog_online_ms_international_construction_management"],"tuition_academic_year":"2026-2027","tuition_currency":"USD","tuition_billing_basis":"PER_CREDIT","tuition_amount":580,"tuition_category":"Online Master’s Programs","tuition_notes":"Official LAU Fees for 2026-2027 schedule.","tuition_source_ids":["lau_fees_2026_2027"],"notes":"Online offering. 30 credits; asynchronous format; full-time and part-time flexibility; no application fee."},{"id":"soe-ms-mechanical-engineering","faculty":"School of Engineering","department":"School of Engineering","major_category":"Engineering","major":"Mechanical Engineering","degree_type":"MASTER","official_degree_name":"MS in Mechanical Engineering","thesis_or_non_thesis":"THESIS","concentrations_or_tracks":[],"credits":30,"duration_value":null,"duration_unit":null,"duration_raw_text":null,"language":null,"delivery_mode":null,"program_description":"Graduate mechanical engineering program focused on contemporary mechanical engineering and research preparation.","admission_requirements":null,"gre_requirement":null,"gmat_requirement":null,"english_requirement":null,"interview_requirement":null,"experience_requirement":null,"accreditation":null,"official_program_url":"https://soe.lau.edu.lb/departments/industrial-mechanical/degree-programs/ms-mechanical.php","source_ids":["lau_graduate_programs","lau_school_soe","lau_prog_ms_mechanical_engineering"],"tuition_academic_year":"2026-2027","tuition_currency":"USD","tuition_billing_basis":"PER_CREDIT","tuition_amount":1032,"tuition_category":"Mechanical Engineering","tuition_notes":"Official LAU Fees for 2026-2027 schedule.","tuition_source_ids":["lau_fees_2026_2027"],"notes":"30 credit hours; thesis required; offered on the Byblos campus."},{"id":"pharmacy-ms-pharmaceutical-development-management","faculty":"School of Pharmacy","department":"School of Pharmacy","major_category":"Pharmacy","major":"Pharmaceutical Development and Management","degree_type":"MASTER","official_degree_name":"M.S. Degree in Pharmaceutical Development and Management","thesis_or_non_thesis":"THESIS_OR_NON_THESIS","concentrations_or_tracks":[],"credits":36,"duration_value":null,"duration_unit":null,"duration_raw_text":null,"language":null,"delivery_mode":null,"program_description":"Interdisciplinary MS in Pharmaceutical Development and Management with thesis and non-thesis professional tracks and industry partnership.","admission_requirements":"Applicants should hold a pharmacy degree (B.S. or Pharm.D.) or major in Chemistry or a biological science; minimum GPA of 2.75; English proficiency required; updated CV and personal statement required; non-pharmacy applicants may be conditionally accepted and complete remedial courses.","gre_requirement":null,"gmat_requirement":null,"english_requirement":null,"interview_requirement":null,"experience_requirement":null,"accreditation":"Recognized by the Lebanese Ministry of Education and Higher Education; New York State Education Department","official_program_url":"https://pharmacy.lau.edu.lb/education/graduate/ms-pharma.php","source_ids":["lau_graduate_programs","lau_school_pharmacy","lau_prog_ms_pharmaceutical_dev_mgmt"],"tuition_academic_year":"2026-2027","tuition_currency":"USD","tuition_billing_basis":"PER_CREDIT","tuition_amount":902,"tuition_category":"Pharmaceutical Development & Management","tuition_notes":"Official LAU Fees for 2026-2027 schedule.","tuition_source_ids":["lau_fees_2026_2027"],"notes":"36 credits; thesis track includes a research project, non-thesis track includes a 6-month internship and literature-based project."}]$LAU$::jsonb) AS seed(
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
        duration_value NUMERIC(10, 2),
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
        source_ids JSONB,
        tuition_academic_year TEXT,
        tuition_currency TEXT,
        tuition_billing_basis TEXT,
        tuition_amount NUMERIC(12, 2),
        tuition_category TEXT,
        tuition_notes TEXT,
        tuition_source_ids JSONB,
        notes TEXT
    );

    INSERT INTO university_faculty (university_id, name, short_name, faculty_type, official_url, notes)
    SELECT
        v_university_id,
        fac.name,
        fac.short_name,
        fac.faculty_type,
        fac.official_url,
        fac.notes
    FROM jsonb_to_recordset($LAU$[{"name":"Adnan Kassar School of Business","short_name":null,"faculty_type":"SCHOOL","official_url":"https://sb.lau.edu.lb/","notes":"Imported from LAU graduate program data."},{"name":"School of Architecture and Design","short_name":null,"faculty_type":"SCHOOL","official_url":"https://sard.lau.edu.lb/","notes":"Imported from LAU graduate program data."},{"name":"School of Arts and Sciences","short_name":null,"faculty_type":"SCHOOL","official_url":"https://soas.lau.edu.lb/","notes":"Imported from LAU graduate program data."},{"name":"School of Engineering","short_name":null,"faculty_type":"SCHOOL","official_url":"https://soe.lau.edu.lb/","notes":"Imported from LAU graduate program data."},{"name":"School of Pharmacy","short_name":null,"faculty_type":"SCHOOL","official_url":"https://pharmacy.lau.edu.lb/","notes":"Imported from LAU graduate program data."}]$LAU$::jsonb) AS fac(
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
    SELECT
        v_university_id,
        fac.id,
        dep.name,
        dep.short_name,
        dep.official_url,
        dep.notes
    FROM jsonb_to_recordset($LAU$[{"faculty_name":"Adnan Kassar School of Business","name":"Adnan Kassar School of Business","short_name":null,"official_url":null,"notes":null},{"faculty_name":"School of Architecture and Design","name":"School of Architecture and Design","short_name":null,"official_url":null,"notes":null},{"faculty_name":"School of Arts and Sciences","name":"School of Arts and Sciences","short_name":null,"official_url":null,"notes":null},{"faculty_name":"School of Engineering","name":"School of Engineering","short_name":null,"official_url":null,"notes":null},{"faculty_name":"School of Pharmacy","name":"School of Pharmacy","short_name":null,"official_url":null,"notes":null}]$LAU$::jsonb) AS dep(
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
            FROM jsonb_array_elements_text(COALESCE(seed.source_ids, '[]'::jsonb)) WITH ORDINALITY AS src(source_key, ord)
            JOIN lau_source_seed ss ON ss.source_id = src.source_key
            JOIN source s ON s.university_id = v_university_id AND s.url = ss.url
            ORDER BY ord
            LIMIT 1
        ),
        concat_ws(' ', NULLIF(seed.notes, ''), CASE WHEN seed.duration_raw_text IS NOT NULL THEN 'Original duration text: ' || seed.duration_raw_text END)
    FROM lau_program_seed seed
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
        NULL,
        'Imported from LAU program sources.'
    FROM lau_program_seed seed
    JOIN graduate_program gp
      ON gp.university_id = v_university_id
     AND gp.program_key = seed.id
    JOIN LATERAL jsonb_array_elements_text(COALESCE(seed.source_ids, '[]'::jsonb)) WITH ORDINALITY AS src(source_key, ord)
      ON TRUE
    JOIN lau_source_seed ss ON ss.source_id = src.source_key
    JOIN source s ON s.university_id = v_university_id AND s.url = ss.url
    ON CONFLICT (program_id, source_id, source_role) DO UPDATE SET
        source_order = EXCLUDED.source_order,
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
        1,
        NULL,
        'Tuition source linked for traceability.'
    FROM lau_program_seed seed
    JOIN graduate_program gp
      ON gp.university_id = v_university_id
     AND gp.program_key = seed.id
    JOIN LATERAL jsonb_array_elements_text(COALESCE(seed.tuition_source_ids, '[]'::jsonb)) WITH ORDINALITY AS src(source_key, ord)
      ON TRUE
    JOIN lau_source_seed ss ON ss.source_id = src.source_key
    JOIN source s ON s.university_id = v_university_id AND s.url = ss.url
    WHERE seed.tuition_academic_year IS NOT NULL
    ON CONFLICT (program_id, source_id, source_role) DO UPDATE SET
        source_order = EXCLUDED.source_order,
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
        seed.id || ':tuition:' || seed.tuition_academic_year,
        seed.tuition_academic_year,
        seed.tuition_currency,
        seed.tuition_billing_basis,
        seed.tuition_amount,
        seed.tuition_category,
        seed.tuition_notes,
        (
            SELECT s.id
            FROM jsonb_array_elements_text(COALESCE(seed.tuition_source_ids, '[]'::jsonb)) WITH ORDINALITY AS src(source_key, ord)
            JOIN lau_source_seed ss ON ss.source_id = src.source_key
            JOIN source s ON s.university_id = v_university_id AND s.url = ss.url
            ORDER BY ord
            LIMIT 1
        )
    FROM lau_program_seed seed
    JOIN graduate_program gp
      ON gp.university_id = v_university_id
     AND gp.program_key = seed.id
    WHERE seed.tuition_academic_year IS NOT NULL
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

    INSERT INTO graduate_admission_requirement (
        university_id, faculty_id, department_id, program_id, scope_level, record_key,
        requirement_type, requirement_text, comparison_operator, threshold_value, threshold_unit, is_required, notes, source_id
    )
    SELECT * FROM (
        SELECT
            v_university_id,
            gp.faculty_id,
            gp.department_id,
            gp.id,
            'PROGRAM'::TEXT,
            seed.id || ':admission_requirements',
            'GENERAL'::TEXT,
            seed.admission_requirements,
            NULL::TEXT,
            NULL::NUMERIC,
            NULL::TEXT,
            TRUE,
            NULL::TEXT,
            gp.source_id
        FROM lau_program_seed seed
        JOIN graduate_program gp ON gp.university_id = v_university_id AND gp.program_key = seed.id
        WHERE seed.admission_requirements IS NOT NULL
        UNION ALL
        SELECT
            v_university_id,
            gp.faculty_id,
            gp.department_id,
            gp.id,
            'PROGRAM',
            seed.id || ':gre_requirement',
            'GRE',
            seed.gre_requirement,
            NULL, NULL, NULL, TRUE, NULL, gp.source_id
        FROM lau_program_seed seed
        JOIN graduate_program gp ON gp.university_id = v_university_id AND gp.program_key = seed.id
        WHERE seed.gre_requirement IS NOT NULL
        UNION ALL
        SELECT
            v_university_id,
            gp.faculty_id,
            gp.department_id,
            gp.id,
            'PROGRAM',
            seed.id || ':gmat_requirement',
            'GMAT',
            seed.gmat_requirement,
            NULL, NULL, NULL, TRUE, NULL, gp.source_id
        FROM lau_program_seed seed
        JOIN graduate_program gp ON gp.university_id = v_university_id AND gp.program_key = seed.id
        WHERE seed.gmat_requirement IS NOT NULL
        UNION ALL
        SELECT
            v_university_id,
            gp.faculty_id,
            gp.department_id,
            gp.id,
            'PROGRAM',
            seed.id || ':english_requirement',
            'ENGLISH',
            seed.english_requirement,
            NULL, NULL, NULL, TRUE, NULL, gp.source_id
        FROM lau_program_seed seed
        JOIN graduate_program gp ON gp.university_id = v_university_id AND gp.program_key = seed.id
        WHERE seed.english_requirement IS NOT NULL
        UNION ALL
        SELECT
            v_university_id,
            gp.faculty_id,
            gp.department_id,
            gp.id,
            'PROGRAM',
            seed.id || ':experience_requirement',
            'EXPERIENCE',
            seed.experience_requirement,
            NULL, NULL, NULL, TRUE, NULL, gp.source_id
        FROM lau_program_seed seed
        JOIN graduate_program gp ON gp.university_id = v_university_id AND gp.program_key = seed.id
        WHERE seed.experience_requirement IS NOT NULL
        UNION ALL
        SELECT
            v_university_id,
            gp.faculty_id,
            gp.department_id,
            gp.id,
            'PROGRAM',
            seed.id || ':interview_requirement',
            'INTERVIEW',
            seed.interview_requirement,
            NULL, NULL, NULL, TRUE, NULL, gp.source_id
        FROM lau_program_seed seed
        JOIN graduate_program gp ON gp.university_id = v_university_id AND gp.program_key = seed.id
        WHERE seed.interview_requirement IS NOT NULL
    ) req
    ON CONFLICT (university_id, record_key) DO UPDATE SET
        faculty_id = EXCLUDED.faculty_id,
        department_id = EXCLUDED.department_id,
        program_id = EXCLUDED.program_id,
        scope_level = EXCLUDED.scope_level,
        requirement_type = EXCLUDED.requirement_type,
        requirement_text = EXCLUDED.requirement_text,
        is_required = EXCLUDED.is_required,
        source_id = EXCLUDED.source_id,
        updated_at = NOW();

    INSERT INTO graduate_program_track (
        university_id, program_id, track_type, track_name, track_order, is_primary, description, source_id, notes
    )
    SELECT
        v_university_id,
        gp.id,
        'TRACK',
        track_vals.track_name,
        track_vals.ord,
        track_vals.ord = 1,
        NULL,
        gp.source_id,
        'Imported from concentrations_or_tracks.'
    FROM lau_program_seed seed
    JOIN graduate_program gp
      ON gp.university_id = v_university_id
     AND gp.program_key = seed.id
    JOIN LATERAL jsonb_array_elements_text(COALESCE(seed.concentrations_or_tracks, '[]'::jsonb)) WITH ORDINALITY AS track_vals(track_name, ord)
      ON TRUE
    ON CONFLICT (program_id, track_type, track_name) DO UPDATE SET
        track_order = EXCLUDED.track_order,
        is_primary = EXCLUDED.is_primary,
        source_id = EXCLUDED.source_id,
        notes = EXCLUDED.notes,
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
        NULL,
        NULL,
        NULL,
        NULL,
        seed.accreditation,
        gp.source_id
    FROM lau_program_seed seed
    JOIN graduate_program gp ON gp.university_id = v_university_id AND gp.program_key = seed.id
    WHERE seed.accreditation IS NOT NULL
    ON CONFLICT (university_id, record_key) DO UPDATE SET
        faculty_id = EXCLUDED.faculty_id,
        department_id = EXCLUDED.department_id,
        program_id = EXCLUDED.program_id,
        scope_level = EXCLUDED.scope_level,
        name = EXCLUDED.name,
        notes = EXCLUDED.notes,
        source_id = EXCLUDED.source_id,
        updated_at = NOW();

END $$;
