-- CNAM Lebanon graduate data seed migration.
-- Idempotent import for the canonical CNAM graduate dataset.

DO $$
DECLARE
    v_university_id BIGINT;
BEGIN
    INSERT INTO university (name, name_ar, acronym, country, city, latitude, longitude, campus_name, campus_type)
    SELECT 'Cnam Lebanon / ISSAE-Cnam Liban', NULL, 'CNAM', 'Lebanon', NULL, NULL, NULL, NULL, NULL
    WHERE NOT EXISTS (
        SELECT 1
        FROM university
        WHERE name = 'Cnam Lebanon / ISSAE-Cnam Liban'
    );

    SELECT id
    INTO v_university_id
    FROM university
    WHERE name = 'Cnam Lebanon / ISSAE-Cnam Liban'
    ORDER BY id
    LIMIT 1;

    INSERT INTO degree_type (code, name)
    VALUES
        ('MASTER', 'Master'),
        ('PHD', 'Doctor of Philosophy')
    ON CONFLICT (code) DO UPDATE SET
        name = EXCLUDED.name,
        updated_at = NOW();

    CREATE TEMP TABLE cnam_source_seed (
        source_id TEXT PRIMARY KEY,
        title TEXT NOT NULL,
        url TEXT NOT NULL,
        source_type TEXT NOT NULL,
        accessed_at DATE NOT NULL,
        notes TEXT NOT NULL
    ) ON COMMIT DROP;

    INSERT INTO cnam_source_seed (source_id, title, url, source_type, accessed_at, notes)
    SELECT source_id, title, url, source_type, accessed_at, notes
    FROM jsonb_to_recordset($CNAM_SOURCES$[
      {
        "source_id": "cnam_home",
        "title": "Formations supérieures professionnelles à l'ISSAE-Cnam au Liban",
        "url": "https://www.cnam-liban.fr/",
        "source_type": "official_homepage",
        "accessed_at": "2026-07-07",
        "notes": "Official homepage for ISSAE-Cnam Liban, with navigation to the training offer, catalogues, departments, documents/procedures, and contact pages."
      },
      {
        "source_id": "cnam_catalogue_general",
        "title": "Catalogue général - 349 formations dans le centre Liban",
        "url": "https://www.cnam-liban.fr/offre-de-formation/catalogue-general/",
        "source_type": "official_catalogue",
        "accessed_at": "2026-07-07",
        "notes": "Official catalogue states that results include Cnam Liban formations and distance formations from other Cnam centres, and lists three Master entries with LIB codes. No specific Doctorat/PhD program entry was identified in the catalogue results inspected."
      },
      {
        "source_id": "cnam_master_finance",
        "title": "Master Finance Parcours Finance d'entreprise et ingénierie financière",
        "url": "https://www.cnam-liban.fr/offre-de-formation/catalogue-general/master-finance-parcours-finance-d-entreprise-et-ingenierie-financiere-1497513.kjsp?RF=libcatagene",
        "source_type": "official_program_page",
        "accessed_at": "2026-07-07",
        "notes": "Official Master page with code MR10701A-LIB, 120 credits, entry level Bac+3/Bac+4, exit level Bac+5, accreditation through 2029-2030, M1/M2 admission requirements, programme, and Liban centre offering years 2026/2027 and 2027/2028."
      },
      {
        "source_id": "cnam_master_data_science",
        "title": "Master mathématiques appliquées, statistique Parcours Science des données",
        "url": "https://www.cnam-liban.fr/offre-de-formation/catalogue-general/master-mathematiques-appliquees-statistique-parcours-science-des-donnees-1497515.kjsp?RF=libcatagene",
        "source_type": "official_program_page",
        "accessed_at": "2026-07-07",
        "notes": "Official Master page with code MR12303A-LIB, 120 credits, entry level Bac+3/Bac+4, exit level Bac+5, accreditation through 2029-2030, prerequisites, M2 dossier admission, programme, and Liban centre offering years 2026/2027 and 2027/2028."
      },
      {
        "source_id": "cnam_master_entrepreneurship_project_management",
        "title": "Master entrepreneuriat et management de projet Parcours Management de projet et d'affaires",
        "url": "https://www.cnam-liban.fr/offre-de-formation/catalogue-general/master-entrepreneuriat-et-management-de-projet-parcours-management-de-projet-et-d-affaires-1573881.kjsp?RF=libcatagene",
        "source_type": "official_program_page",
        "accessed_at": "2026-07-07",
        "notes": "Official Master page with code MR12001A-LIB, 120 credits, entry level Bac+3/Bac+4, exit level Bac+5, apprenticeship marked yes, accreditation through 2029-2030, prerequisites and selection-on-file admission text. The page did not show a populated Liban centre offering block in the captured lines, so it is included with a note for verification."
      },
      {
        "source_id": "cnam_diplomes_mode_emploi",
        "title": "Obtenir un diplôme, un titre ou un certificat au Cnam",
        "url": "https://www.cnam-liban.fr/offre-de-formation/diplomes-mode-d-emploi/",
        "source_type": "official_policy_page",
        "accessed_at": "2026-07-07",
        "notes": "Explains Cnam degree levels and national diploma categories including Master and Doctorat. This supports terminology only; it is not a specific PhD program offering."
      },
      {
        "source_id": "cnam_documents_procedures",
        "title": "Documents et procédures",
        "url": "https://www.cnam-liban.fr/documents-et-procedures/documents-et-procedures-784703.kjsp",
        "source_type": "official_admissions_documents_page",
        "accessed_at": "2026-07-07",
        "notes": "Lists required documents for new students and holders of higher-education diplomas, plus documents for diploma, dispensation, admission, and thesis/memoire procedures."
      },
      {
        "source_id": "cnam_financial_aid",
        "title": "Formulaire de demande d'aide financière pour les droits d'inscription",
        "url": "https://www.cnam-liban.fr/cnam-liban/formulaire-de-demande-d-aide-financiere-pour-les-droits-d-inscription-1455358.kjsp",
        "source_type": "official_financial_aid_page",
        "accessed_at": "2026-07-07",
        "notes": "Official page for requesting financial aid toward tuition/registration fees, with instructions to submit the PDF manually or electronically to the scolarité office."
      },
      {
        "source_id": "cnam_financial_aid_pdf",
        "title": "Formulaire aide financière 2023-2024 PDF",
        "url": "https://www.cnam-liban.fr/medias/fichier/formulaire-aide-financiere-2023-2024_1701352067187-pdf",
        "source_type": "official_pdf",
        "accessed_at": "2026-07-07",
        "notes": "Official financial-aid form asking for programme/diploma in progress, total tuition fees for the current year, amount already paid, and remaining amount due."
      },
      {
        "source_id": "cnam_eicnam_admission_request",
        "title": "Demande d'inscription à l'examen d'admission à l'EiCnam",
        "url": "https://www.cnam-liban.fr/cnam-liban/demande-d-inscription-a-l-examen-d-admission-a-l-eicnam-1353357.kjsp",
        "source_type": "official_admissions_page",
        "accessed_at": "2026-07-07",
        "notes": "Official admission-exam request page for EICnam, noting a 72 USD application/registration fee. This relates to EICnam engineering admission, not Master evidence."
      },
      {
        "source_id": "cnam_documents_download",
        "title": "Documents à télécharger",
        "url": "https://www.cnam-liban.fr/documents-et-procedures/documents-a-telecharger-785116.kjsp",
        "source_type": "official_documents_page",
        "accessed_at": "2026-07-07",
        "notes": "Official download hub mentioning habilitation documents, EICnam candidacy dossier, thesis/memoire support, and engineering diploma regulations. No Master/PhD-specific regulation PDF was identified here in the inspected results."
      }
    ]$CNAM_SOURCES$::jsonb) AS x(
        source_id TEXT,
        title TEXT,
        url TEXT,
        source_type TEXT,
        accessed_at DATE,
        notes TEXT
    );

    INSERT INTO source (university_id, title, url, source_type, accessed_at, notes)
    SELECT
        v_university_id,
        title,
        url,
        source_type,
        accessed_at,
        notes
    FROM cnam_source_seed
    ON CONFLICT (university_id, url) DO UPDATE SET
        title = EXCLUDED.title,
        source_type = EXCLUDED.source_type,
        accessed_at = EXCLUDED.accessed_at,
        notes = EXCLUDED.notes,
        updated_at = NOW();

    CREATE TEMP TABLE cnam_faculty_seed (
        name TEXT PRIMARY KEY,
        short_name TEXT,
        faculty_type TEXT NOT NULL,
        official_url TEXT,
        notes TEXT NOT NULL
    ) ON COMMIT DROP;

    INSERT INTO cnam_faculty_seed (name, short_name, faculty_type, official_url, notes)
    VALUES
        ('CNAM Lebanon', NULL, 'FACULTY', NULL, 'Imported from the official CNAM graduate inventory.');

    INSERT INTO university_faculty (university_id, name, short_name, faculty_type, official_url, notes)
    SELECT
        v_university_id,
        name,
        short_name,
        faculty_type,
        official_url,
        notes
    FROM cnam_faculty_seed
    ON CONFLICT (university_id, name) DO UPDATE SET
        short_name = EXCLUDED.short_name,
        faculty_type = EXCLUDED.faculty_type,
        official_url = EXCLUDED.official_url,
        notes = EXCLUDED.notes,
        updated_at = NOW();

    CREATE TEMP TABLE cnam_program_seed (
        program_key TEXT PRIMARY KEY,
        faculty_name TEXT NOT NULL,
        major_category TEXT NOT NULL,
        major TEXT NOT NULL,
        degree_type TEXT NOT NULL,
        official_degree_name TEXT NOT NULL,
        thesis_or_non_thesis TEXT,
        credits INTEGER,
        duration_value NUMERIC(10, 2),
        duration_unit TEXT,
        program_description TEXT NOT NULL,
        official_program_url TEXT NOT NULL,
        source_ids JSONB NOT NULL,
        primary_source_id TEXT NOT NULL,
        notes TEXT NOT NULL
    ) ON COMMIT DROP;

    INSERT INTO cnam_program_seed (
        program_key,
        faculty_name,
        major_category,
        major,
        degree_type,
        official_degree_name,
        thesis_or_non_thesis,
        credits,
        duration_value,
        duration_unit,
        program_description,
        official_program_url,
        source_ids,
        primary_source_id,
        notes
    )
    VALUES
        (
            'cnam-master-finance',
            'CNAM Lebanon',
            'Finance',
            'Finance d''entreprise et ingénierie financière',
            'MASTER',
            'Master Finance Parcours Finance d''entreprise et ingénierie financière',
            NULL,
            120,
            NULL,
            NULL,
            'Official Master Finance page with code MR10701A-LIB, 120 credits, Bac+5 exit level, M1/M2 access path, Liban centre offering years 2026/2027 and 2027/2028, and accreditation through 2029-2030.',
            'https://www.cnam-liban.fr/offre-de-formation/catalogue-general/master-finance-parcours-finance-d-entreprise-et-ingenierie-financiere-1497513.kjsp?RF=libcatagene',
            '["cnam_master_finance","cnam_catalogue_general","cnam_documents_procedures"]'::jsonb,
            'cnam_master_finance',
            'Imported from the official CNAM graduate inventory. No tuition row was seeded because no official graduate tuition table was published.'
        ),
        (
            'cnam-master-data-science',
            'CNAM Lebanon',
            'Applied Mathematics and Statistics',
            'Science des données',
            'MASTER',
            'Master mathématiques appliquées, statistique Parcours Science des données',
            NULL,
            120,
            NULL,
            NULL,
            'Official Master page with code MR12303A-LIB, 120 credits, Bac+5 exit level, dossier-based M2 admission, Liban centre offering years 2026/2027 and 2027/2028, and accreditation through 2029-2030.',
            'https://www.cnam-liban.fr/offre-de-formation/catalogue-general/master-mathematiques-appliquees-statistique-parcours-science-des-donnees-1497515.kjsp?RF=libcatagene',
            '["cnam_master_data_science","cnam_catalogue_general","cnam_documents_procedures"]'::jsonb,
            'cnam_master_data_science',
            'Imported from the official CNAM graduate inventory. No tuition row was seeded because no official graduate tuition table was published.'
        ),
        (
            'cnam-master-entrepreneurship-project-management',
            'CNAM Lebanon',
            'Entrepreneurship and Project Management',
            'Management de projet et d''affaires',
            'MASTER',
            'Master entrepreneuriat et management de projet Parcours Management de projet et d''affaires',
            NULL,
            120,
            NULL,
            NULL,
            'Official Master page with code MR12001A-LIB, 120 credits, Bac+5 exit level, apprenticeship marker, and accreditation through 2029-2030.',
            'https://www.cnam-liban.fr/offre-de-formation/catalogue-general/master-entrepreneuriat-et-management-de-projet-parcours-management-de-projet-et-d-affaires-1573881.kjsp?RF=libcatagene',
            '["cnam_master_entrepreneurship_project_management","cnam_catalogue_general","cnam_diplomes_mode_emploi","cnam_documents_procedures"]'::jsonb,
            'cnam_master_entrepreneurship_project_management',
            'Imported from the official CNAM graduate inventory. The official page confirms the programme code, 120-credit structure, apprenticeship marker, and selection-on-file admission text. The captured centre-offering block did not show Liban year rows, so this record is retained with the official programme page as evidence.'
        );

    INSERT INTO graduate_program (
        university_id,
        faculty_id,
        department_id,
        degree_type_id,
        program_key,
        major_category,
        major,
        official_degree_name,
        thesis_or_non_thesis,
        credits,
        duration_value,
        duration_unit,
        primary_language_id,
        delivery_mode,
        program_description,
        official_program_url,
        source_id,
        notes
    )
    SELECT
        v_university_id,
        f.id,
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
        NULL,
        NULL,
        seed.program_description,
        seed.official_program_url,
        s.id,
        seed.notes
    FROM cnam_program_seed seed
    JOIN university_faculty f
      ON f.university_id = v_university_id
     AND f.name = seed.faculty_name
    JOIN degree_type dt
      ON dt.code = seed.degree_type
    JOIN cnam_source_seed ss
      ON ss.source_id = seed.primary_source_id
    JOIN source s
      ON s.university_id = v_university_id
     AND s.url = ss.url
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
        university_id,
        program_id,
        source_id,
        source_role,
        source_order,
        evidence_text,
        notes
    )
    SELECT
        v_university_id,
        gp.id,
        s.id,
        CASE
            WHEN src.source_seed_id = seed.primary_source_id THEN 'PRIMARY'
            WHEN src.source_seed_id = 'cnam_catalogue_general' THEN 'CATALOG'
            WHEN src.source_seed_id = 'cnam_documents_procedures' THEN 'ADMISSIONS'
            WHEN src.source_seed_id = 'cnam_diplomes_mode_emploi' THEN 'OTHER'
            ELSE 'OTHER'
        END,
        src.ord,
        ss.title,
        'Imported from the official CNAM graduate dataset.'
    FROM cnam_program_seed seed
    JOIN graduate_program gp
      ON gp.university_id = v_university_id
     AND gp.program_key = seed.program_key
    JOIN LATERAL jsonb_array_elements_text(seed.source_ids) WITH ORDINALITY AS src(source_seed_id, ord)
      ON TRUE
    JOIN cnam_source_seed ss
      ON ss.source_id = src.source_seed_id
    JOIN source s
      ON s.university_id = v_university_id
     AND s.url = ss.url
    ON CONFLICT (program_id, source_id, source_role) DO UPDATE SET
        source_order = EXCLUDED.source_order,
        evidence_text = EXCLUDED.evidence_text,
        notes = EXCLUDED.notes,
        updated_at = NOW();

END $$;
