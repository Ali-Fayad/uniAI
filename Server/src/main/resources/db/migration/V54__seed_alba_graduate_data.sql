-- ALBA graduate data seed migration.
-- Idempotent import for the canonical ALBA graduate dataset.

DO $$
DECLARE
    v_university_id BIGINT;
BEGIN
    INSERT INTO university (name, name_ar, acronym, country, city, latitude, longitude, campus_name, campus_type)
    SELECT 'Académie Libanaise des Beaux-Arts', NULL, 'ALBA', 'Lebanon', NULL, NULL, NULL, NULL, NULL
    WHERE NOT EXISTS (
        SELECT 1
        FROM university
        WHERE name = 'Académie Libanaise des Beaux-Arts'
    );

    SELECT id
    INTO v_university_id
    FROM university
    WHERE name = 'Académie Libanaise des Beaux-Arts'
    ORDER BY id
    LIMIT 1;

    INSERT INTO degree_type (code, name)
    VALUES
        ('MASTER', 'Master'),
        ('PHD', 'Doctor of Philosophy')
    ON CONFLICT (code) DO UPDATE SET
        name = EXCLUDED.name,
        updated_at = NOW();

    CREATE TEMP TABLE alba_source_seed (
        source_id TEXT PRIMARY KEY,
        title TEXT NOT NULL,
        url TEXT NOT NULL,
        source_type TEXT NOT NULL,
        accessed_at DATE NOT NULL,
        notes TEXT NOT NULL
    ) ON COMMIT DROP;

    INSERT INTO alba_source_seed (source_id, title, url, source_type, accessed_at, notes)
    VALUES
        ('ALBA-S001', 'ALBA official homepage', 'https://alba.edu.lb/', 'official_page', '2026-07-07', 'Official ALBA website landing page with navigation to schools, admissions, fees, financial aid, student life, and official PDFs.'),
        ('ALBA-S002', 'Admissions et Inscriptions', 'https://alba.edu.lb/sites/ALBA1/InsAdm/Pages/default.aspx', 'official_page', '2026-07-07', 'Official admissions page with sections for Licences, Masters, Scolarité et Frais, Demandes d''attestations, Aide financière, and Orientation.'),
        ('ALBA-S003', 'Demande Masters 2026-2027 Master PDF', 'https://alba.edu.lb/sites/ALBA1/Style%20Library/PDF/Demande%20Masters%202026-2027%20Master%20.pdf?csf=1&e=KO1r9S', 'official_pdf', '2026-07-07', 'Official ALBA Master application file for 2026-2027.'),
        ('ALBA-S004', 'École d’architecture', 'https://alba.edu.lb/sites/ALBA1/ECOLEDARCHITECTURE/Pages/default.aspx', 'official_page', '2026-07-07', 'Official school page for Architecture. Discovery did not find a clear separate official Master or PhD admission/program claim on this page.'),
        ('ALBA-S005', 'Architecture d’intérieur', 'https://alba.edu.lb/sites/ALBA1/ARCHITECTUREDINTERIEUR/Pages/default.aspx', 'official_page', '2026-07-07', 'Official ALBA section page explicitly listing Master en Architecture d''Intérieur and describing its mission/objectives.'),
        ('ALBA-S006', 'École des Arts Décoratifs - Section Design', 'https://alba.edu.lb/sites/ALBA1/ECOLEDESARTSDEC/Pages/default.aspx', 'official_page', '2026-07-07', 'Official section page listing Master en Design Global and describing it as a two-year program preparing professionals for a changing world.'),
        ('ALBA-S007', 'Master in Global Design at ALBA PDF', 'https://alba.edu.lb/sites/ALBA1/Style%20Library/PDF/Master%27s%20Text%20-%20Eng.pdf?csf=1&e=c3pqxU', 'official_pdf', '2026-07-07', 'Official ALBA PDF describing the Master in Global Design as a transformative two-year program.'),
        ('ALBA-S008', 'Graphisme et Publicité', 'https://alba.edu.lb/sites/ALBA1/GRAPHPUBLIC/Pages/GRAPHISME.aspx', 'official_page', '2026-07-07', 'Official page explicitly listing Master en Graphisme et Publicité and describing its mission to train creative directors and related communication professions.'),
        ('ALBA-S009', 'Illustration', 'https://alba.edu.lb/sites/ALBA1/GRAPHPUBLIC/Pages/ILLUSTRATION.aspx', 'official_page', '2026-07-07', 'Official page explicitly describing the Master en Illustration et Bande Dessinée and its mission.'),
        ('ALBA-S010', 'Animation 2D/3D', 'https://alba.edu.lb/sites/ALBA1/GRAPHPUBLIC/Pages/ANIMATION.aspx', 'official_page', '2026-07-07', 'Official page listing Master en Animation 2D/3D and describing the formation of professionals in animated image/cinema fields.'),
        ('ALBA-S011', 'École de Cinéma et de Réalisation Audiovisuelle', 'https://alba.edu.lb/sites/ALBA1/ECOLEDECINEMAETDEREALISATIONAUDIOVISUELLE/Pages/default.aspx', 'official_page', '2026-07-07', 'Official page listing Master en Réalisation Cinéma and Master en Production Audiovisuelle.'),
        ('ALBA-S012', 'Institut d’Urbanisme', 'https://alba.edu.lb/sites/ALBA1/URBANISME/Pages/default.aspx', 'official_page', '2026-07-07', 'Official institute page stating that Master and Licence level trainings are offered in design urbain and paysage; it explicitly lists Master en Design Urbain.'),
        ('ALBA-S013', 'École des Arts Visuels', 'https://alba.edu.lb/sites/ALBA1/ECOLEDESARTSVISUELS/Pages/default.aspx', 'official_page', '2026-07-07', 'Official school page for Visual Arts. Discovery did not find clear official Master or PhD program evidence here.'),
        ('ALBA-S014', 'École de Mode', 'https://alba.edu.lb/sites/ALBA1/APROPOSDELECOLE/Pages/default.aspx', 'official_page', '2026-07-07', 'Official school page for Fashion. It lists Licence en Design de Mode; no official Master or PhD evidence was found on this page.'),
        ('ALBA-S015', 'Livret de l''étudiant 2025-2026 PDF', 'https://alba.edu.lb/sites/ALBA1/Style%20Library/PDF/Livret-2025-2026.pdf?csf=1&e=ppCf77', 'official_pdf', '2026-07-07', 'Official student booklet linked from the ALBA site for student guidance and institutional/student-life reference.'),
        ('ALBA-S016', 'Brochure Générale 2023 PDF', 'https://alba.edu.lb/sites/ALBA1/Style%20Library/PDF/BrochureGenerale2023.pdf?csf=1&e=7NqvFs', 'official_pdf', '2026-07-07', 'Official general brochure linked from the ALBA homepage/student-life area.');

    INSERT INTO source (university_id, title, url, source_type, accessed_at, notes)
    SELECT v_university_id, title, url, source_type, accessed_at, notes
    FROM alba_source_seed
    ON CONFLICT (university_id, url) DO UPDATE SET
        title = EXCLUDED.title,
        source_type = EXCLUDED.source_type,
        accessed_at = EXCLUDED.accessed_at,
        notes = EXCLUDED.notes,
        updated_at = NOW();

    CREATE TEMP TABLE alba_source_map AS
    SELECT ss.source_id, s.id AS db_source_id
    FROM alba_source_seed ss
    JOIN source s
      ON s.university_id = v_university_id
     AND s.url = ss.url;

    CREATE TEMP TABLE alba_program_seed (
        program_key TEXT PRIMARY KEY,
        title TEXT NOT NULL,
        official_degree_name TEXT NOT NULL,
        major_category TEXT,
        major TEXT,
        official_program_url TEXT,
        primary_source_id TEXT NOT NULL,
        program_description TEXT NOT NULL,
        notes TEXT NOT NULL
    ) ON COMMIT DROP;

    INSERT INTO alba_program_seed (
        program_key,
        title,
        official_degree_name,
        major_category,
        major,
        official_program_url,
        primary_source_id,
        program_description,
        notes
    )
    VALUES
        (
            'alba-master-interior-architecture',
            'Master en Architecture d''Intérieur',
            'Master en Architecture d''Intérieur',
            'Architecture',
            'Interior Architecture',
            'https://alba.edu.lb/sites/ALBA1/ARCHITECTUREDINTERIEUR/Pages/default.aspx',
            'ALBA-S005',
            'Official ALBA page explicitly lists and describes the Master en Architecture d''Intérieur.',
            'Serialized from the official Architecture d''intérieur page. No additional program-level details were recovered in the discovery report.'
        ),
        (
            'alba-master-global-design',
            'Master en Design Global / Master in Global Design',
            'Master en Design Global',
            'Design',
            'Global Design',
            'https://alba.edu.lb/sites/ALBA1/ECOLEDESARTSDEC/Pages/default.aspx',
            'ALBA-S006',
            'Official ALBA Section Design page and PDF describe the Master in Global Design / Master en Design Global as a two-year program.',
            'The official page and PDF identify the program as a two-year Master in Global Design / Master en Design Global. No dedicated credit or thesis structure was recovered.'
        ),
        (
            'alba-master-graphic-advertising',
            'Master en Graphisme et Publicité',
            'Master en Graphisme et Publicité',
            'Graphic Design',
            'Graphic Design and Advertising',
            'https://alba.edu.lb/sites/ALBA1/GRAPHPUBLIC/Pages/GRAPHISME.aspx',
            'ALBA-S008',
            'Official ALBA page explicitly lists and describes the Master en Graphisme et Publicité.',
            'Serialized from the official Graphisme et Publicité page.'
        ),
        (
            'alba-master-illustration-comics',
            'Master en Illustration et Bande Dessinée',
            'Master en Illustration et Bande Dessinée',
            'Illustration',
            'Illustration and Comics',
            'https://alba.edu.lb/sites/ALBA1/GRAPHPUBLIC/Pages/ILLUSTRATION.aspx',
            'ALBA-S009',
            'Official ALBA page explicitly describes the Master en Illustration et Bande Dessinée.',
            'Serialized from the official Illustration page.'
        ),
        (
            'alba-master-animation-2d-3d',
            'Master en Animation 2D/3D',
            'Master en Animation 2D/3D',
            'Animation',
            '2D/3D Animation',
            'https://alba.edu.lb/sites/ALBA1/GRAPHPUBLIC/Pages/ANIMATION.aspx',
            'ALBA-S010',
            'Official ALBA page explicitly lists the Master en Animation 2D/3D.',
            'Serialized from the official Animation 2D/3D page.'
        ),
        (
            'alba-master-film-direction',
            'Master en Réalisation Cinéma',
            'Master en Réalisation Cinéma',
            'Cinema',
            'Film Direction',
            'https://alba.edu.lb/sites/ALBA1/ECOLEDECINEMAETDEREALISATIONAUDIOVISUELLE/Pages/default.aspx',
            'ALBA-S011',
            'Official ALBA cinema school page explicitly lists the Master en Réalisation Cinéma.',
            'Serialized from the official cinema and audiovisual school page.'
        ),
        (
            'alba-master-audiovisual-production',
            'Master en Production Audiovisuelle',
            'Master en Production Audiovisuelle',
            'Cinema',
            'Audiovisual Production',
            NULL,
            'ALBA-S011',
            'Official ALBA cinema school page explicitly lists the Master en Production Audiovisuelle and describes the program.',
            'Serialized from the official cinema and audiovisual school page. The shared school page already serves the cinema master above, so this row keeps official_program_url null to respect the university-level unique URL constraint.'
        ),
        (
            'alba-master-urban-design',
            'Master en Design Urbain',
            'Master en Design Urbain',
            'Urbanism',
            'Urban Design',
            'https://alba.edu.lb/sites/ALBA1/URBANISME/Pages/default.aspx',
            'ALBA-S012',
            'Official Institut d’Urbanisme page explicitly lists the Master en Design Urbain.',
            'The institute page also mentions master-level landscape training, but no separate official program title was recovered for a distinct graduate record.'
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
        NULL,
        NULL,
        dt.id,
        seed.program_key,
        seed.major_category,
        seed.major,
        seed.official_degree_name,
        NULL,
        NULL,
        NULL,
        NULL,
        NULL,
        NULL,
        seed.program_description,
        seed.official_program_url,
        sm.db_source_id,
        seed.notes
    FROM alba_program_seed seed
    JOIN degree_type dt
      ON dt.code = 'MASTER'
    JOIN alba_source_map sm
      ON sm.source_id = seed.primary_source_id
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

    CREATE TEMP TABLE alba_program_source_seed (
        program_key TEXT NOT NULL,
        source_id TEXT NOT NULL,
        source_role TEXT NOT NULL,
        source_order INTEGER NOT NULL,
        evidence_text TEXT NOT NULL,
        notes TEXT
    ) ON COMMIT DROP;

    INSERT INTO alba_program_source_seed (program_key, source_id, source_role, source_order, evidence_text, notes)
    VALUES
        ('alba-master-interior-architecture', 'ALBA-S005', 'PRIMARY', 1, 'Official Architecture d’intérieur page explicitly lists and describes the Master en Architecture d''Intérieur.', 'Primary program evidence.'),
        ('alba-master-global-design', 'ALBA-S006', 'PRIMARY', 1, 'Official Section Design page describes the Master in Global Design as a two-year program.', 'Primary program evidence.'),
        ('alba-master-global-design', 'ALBA-S007', 'PDF', 2, 'Official Master in Global Design PDF describing the program as a transformative two-year program.', NULL),
        ('alba-master-graphic-advertising', 'ALBA-S008', 'PRIMARY', 1, 'Official page explicitly lists and describes the Master en Graphisme et Publicité.', 'Primary program evidence.'),
        ('alba-master-illustration-comics', 'ALBA-S009', 'PRIMARY', 1, 'Official page explicitly describes the Master en Illustration et Bande Dessinée.', 'Primary program evidence.'),
        ('alba-master-animation-2d-3d', 'ALBA-S010', 'PRIMARY', 1, 'Official page explicitly lists the Master en Animation 2D/3D.', 'Primary program evidence.'),
        ('alba-master-film-direction', 'ALBA-S011', 'PRIMARY', 1, 'Official cinema school page explicitly lists the Master en Réalisation Cinéma.', 'Primary program evidence.'),
        ('alba-master-audiovisual-production', 'ALBA-S011', 'PRIMARY', 1, 'Official cinema school page explicitly lists the Master en Production Audiovisuelle and describes the program.', 'Primary program evidence.'),
        ('alba-master-urban-design', 'ALBA-S012', 'PRIMARY', 1, 'Official Institut d’Urbanisme page explicitly lists the Master en Design Urbain.', 'Primary program evidence.');

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
        sm.db_source_id,
        ps.source_role,
        ps.source_order,
        ps.evidence_text,
        ps.notes
    FROM alba_program_source_seed ps
    JOIN graduate_program gp
      ON gp.university_id = v_university_id
     AND gp.program_key = ps.program_key
    JOIN alba_source_map sm
      ON sm.source_id = ps.source_id
    ON CONFLICT (program_id, source_id, source_role) DO UPDATE SET
        source_order = EXCLUDED.source_order,
        evidence_text = EXCLUDED.evidence_text,
        notes = EXCLUDED.notes,
        updated_at = NOW();
END $$;
