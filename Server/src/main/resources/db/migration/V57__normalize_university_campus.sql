-- Normalize institution identity and physical campuses using the approved DB_CAMPUS_003 matrix.
-- The mapping is embedded so this migration is self-contained and repeatable by Flyway.
CREATE TABLE campus (
    id BIGSERIAL PRIMARY KEY,
    university_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    campus_type VARCHAR(50),
    city VARCHAR(120) NOT NULL,
    locality VARCHAR(120),
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_campus_university FOREIGN KEY (university_id) REFERENCES university(id) ON DELETE CASCADE,
    CONSTRAINT uq_campus_university_name UNIQUE (university_id, name),
    CONSTRAINT ck_campus_coordinates CHECK (
        (latitude IS NULL AND longitude IS NULL)
        OR (latitude BETWEEN -90 AND 90 AND longitude BETWEEN -180 AND 180)
    )
);

CREATE INDEX idx_campus_university_id ON campus(university_id);
CREATE INDEX idx_campus_city ON campus(LOWER(BTRIM(city)));
CREATE INDEX idx_campus_locality ON campus(LOWER(BTRIM(locality)));
CREATE INDEX idx_campus_name ON campus(LOWER(BTRIM(name)));

CREATE TEMP TABLE university_normalization (
    old_university_id BIGINT PRIMARY KEY,
    canonical_university_id BIGINT NOT NULL,
    canonical_name VARCHAR(255) NOT NULL,
    canonical_name_ar VARCHAR(255),
    canonical_acronym VARCHAR(20) NOT NULL,
    country VARCHAR(120) NOT NULL,
    creates_campus BOOLEAN NOT NULL,
    campus_name VARCHAR(255),
    campus_type VARCHAR(50),
    city VARCHAR(120),
    locality VARCHAR(120),
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8)
) ON COMMIT DROP;

INSERT INTO university_normalization VALUES
(1,1,'American University of Beirut',NULL,'AUB','Lebanon',TRUE,'Main Campus','Main','Beirut','Ras Beirut',33.89996000,35.48228000),
(2,1,'American University of Beirut',NULL,'AUB','Lebanon',TRUE,'Medical Center','Medical','Beirut','Hamra',33.89484000,35.47920000),
(3,1,'American University of Beirut',NULL,'AUB','Lebanon',TRUE,'AREC','Research','Haush Sneid',NULL,33.82000000,35.90000000),
(4,1,'American University of Beirut',NULL,'AUB','Lebanon',TRUE,'Marine Research','Research','Batroun',NULL,34.25000000,35.66667000),
(5,5,'Lebanese American University',NULL,'LAU','Lebanon',TRUE,'Beirut Campus','Main','Beirut','Hamra',33.89304000,35.47788000),
(6,5,'Lebanese American University',NULL,'LAU','Lebanon',TRUE,'Byblos Campus','Main','Jbeil',NULL,34.11995000,35.64900000),
(7,75,'Université Saint-Joseph de Beyrouth',NULL,'USJ','Lebanon',TRUE,'Campus des Sciences Médicales','Medical','Beirut',NULL,33.89111000,35.50833000),
(8,75,'Université Saint-Joseph de Beyrouth',NULL,'USJ','Lebanon',TRUE,'Campus des Sciences Humaines','Humanities','Beirut',NULL,33.88333000,35.51667000),
(9,75,'Université Saint-Joseph de Beyrouth',NULL,'USJ','Lebanon',TRUE,'Campus de l''Innovation et du Sport','Innovation','Beirut',NULL,33.90000000,35.50000000),
(10,75,'Université Saint-Joseph de Beyrouth',NULL,'USJ','Lebanon',TRUE,'Campus des Sciences et Technologies','Sciences','Beirut','Mar Roukoz',33.88333000,35.53333000),
(11,11,'Lebanese University',NULL,'UL','Lebanon',TRUE,'Hadath Campus','Main','Hadath',NULL,33.82927000,35.52027000),
(12,11,'Lebanese University',NULL,'UL','Lebanon',TRUE,'Beirut Campus','Downtown','Beirut',NULL,33.87500000,35.50972000),
(13,11,'Lebanese University',NULL,'UL','Lebanon',TRUE,'Fanar Campus','Sciences','Fanar',NULL,33.87917000,35.54028000),
(14,11,'Lebanese University',NULL,'UL','Lebanon',TRUE,'Tripoli Campus','North','Tripoli',NULL,34.44028000,35.84306000),
(15,11,'Lebanese University',NULL,'UL','Lebanon',TRUE,'Saida Campus','South','Saida',NULL,33.56111000,35.37778000),
(16,11,'Lebanese University',NULL,'UL','Lebanon',TRUE,'Zahle Campus','Beqaa','Zahle',NULL,33.83333000,35.90000000),
(17,76,'Notre Dame University-Louaize',NULL,'NDU','Lebanon',TRUE,'Main Campus','Main','Zouk Mosbeh',NULL,33.95176000,35.61375000),
(18,76,'Notre Dame University-Louaize',NULL,'NDU','Lebanon',FALSE,NULL,NULL,NULL,NULL,NULL,NULL),
(19,76,'Notre Dame University-Louaize',NULL,'NDU','Lebanon',TRUE,'Shouf Campus','Branch','Deir El Qamar',NULL,NULL,NULL),
(20,20,'Holy Spirit University of Kaslik',NULL,'USEK','Lebanon',TRUE,'Kaslik Campus','Main','Kaslik',NULL,33.98286000,35.61873000),
(21,20,'Holy Spirit University of Kaslik',NULL,'USEK','Lebanon',TRUE,'Chekka Campus','Agriculture','Chekka',NULL,34.31667000,35.73333000),
(22,22,'University of Balamand',NULL,'UOB','Lebanon',TRUE,'Al-Kurah Campus','Main','Kelhat',NULL,34.36611000,35.78222000),
(23,22,'University of Balamand',NULL,'UOB','Lebanon',FALSE,NULL,NULL,NULL,NULL,NULL,NULL),
(24,24,'Beirut Arab University',NULL,'BAU','Lebanon',TRUE,'Beirut Campus','Main','Beirut','Tariq El Jdideh',33.87192000,35.49599000),
(25,24,'Beirut Arab University',NULL,'BAU','Lebanon',TRUE,'Debbieh Campus','Branch','Debbieh',NULL,33.50000000,35.41667000),
(26,24,'Beirut Arab University',NULL,'BAU','Lebanon',TRUE,'Tripoli Campus','Branch','Tripoli',NULL,34.43750000,35.83500000),
(27,27,'Lebanese International University',NULL,'LIU','Lebanon',TRUE,'Khiara Campus','Main','Khiara',NULL,33.84220000,35.83940000),
(28,27,'Lebanese International University',NULL,'LIU','Lebanon',TRUE,'Beirut Campus','Branch','Beirut',NULL,33.88194000,35.51389000),
(29,27,'Lebanese International University',NULL,'LIU','Lebanon',TRUE,'Saida Campus','Branch','Saida',NULL,33.56111000,35.37778000),
(30,27,'Lebanese International University',NULL,'LIU','Lebanon',TRUE,'Tripoli Campus','Branch','Tripoli',NULL,34.43889000,35.84444000),
(31,27,'Lebanese International University',NULL,'LIU','Lebanon',TRUE,'Jdeideh Campus','Branch','Jdeideh',NULL,33.89583000,35.55833000),
(32,27,'Lebanese International University',NULL,'LIU','Lebanon',TRUE,'Nabatieh Campus','Branch','Nabatieh',NULL,33.37917000,35.48889000),
(33,27,'Lebanese International University',NULL,'LIU','Lebanon',TRUE,'Rayak Campus','Branch','Rayak',NULL,33.85000000,35.85000000),
(34,77,'Antonine University',NULL,'UA','Lebanon',TRUE,'Hadat–Baabda Main Campus','Main','Hadat',NULL,33.86496000,35.98406000),
(35,77,'Antonine University',NULL,'UA','Lebanon',TRUE,'Nabi Ayla–Zahle Campus','Branch','Zahle','Nabi Ayla',33.83333000,35.90000000),
(36,77,'Antonine University',NULL,'UA','Lebanon',TRUE,'Mejdlaya–Zgharta Campus','Branch','Mejdlaya',NULL,34.26667000,35.80000000),
(37,37,'Haigazian University',NULL,'HU','Lebanon',TRUE,'Main Campus','Main','Beirut','Kantari',33.88500000,35.51250000),
(38,38,'American University of Science and Technology',NULL,'AUST','Lebanon',TRUE,'Beirut Campus','Main','Beirut','Achrafieh',33.88694000,35.51833000),
(39,38,'American University of Science and Technology',NULL,'AUST','Lebanon',TRUE,'Jdeideh Campus','Branch','Jdeideh',NULL,33.89611000,35.55833000),
(40,38,'American University of Science and Technology',NULL,'AUST','Lebanon',TRUE,'Baabda Campus','Branch','Baabda',NULL,33.83333000,35.53333000),
(41,38,'American University of Science and Technology',NULL,'AUST','Lebanon',TRUE,'Tripoli Campus','Branch','Tripoli',NULL,34.44028000,35.84306000),
(42,38,'American University of Science and Technology',NULL,'AUST','Lebanon',TRUE,'Saida Campus','Branch','Saida',NULL,33.56111000,35.37778000),
(43,38,'American University of Science and Technology',NULL,'AUST','Lebanon',TRUE,'Zahle Campus','Branch','Zahle',NULL,33.83333000,35.90000000),
(44,78,'Arts, Sciences and Technology University in Lebanon',NULL,'AUL','Lebanon',TRUE,'Beirut Campus','Main','Beirut','Hamra',33.88611000,35.47917000),
(45,78,'Arts, Sciences and Technology University in Lebanon',NULL,'AUL','Lebanon',TRUE,'Tripoli Campus','Branch','Tripoli',NULL,34.43333000,35.84028000),
(46,78,'Arts, Sciences and Technology University in Lebanon',NULL,'AUL','Lebanon',TRUE,'Jbeil Campus','Branch','Jbeil',NULL,34.11944000,35.65000000),
(47,47,'Al-Kafaàt University',NULL,'AKU','Lebanon',TRUE,'Daoura Campus','Main','Daoura',NULL,33.85833000,35.53056000),
(48,47,'Al-Kafaàt University',NULL,'AKU','Lebanon',TRUE,'Saida Campus','Branch','Saida',NULL,33.56250000,35.37500000),
(49,49,'Modern University for Business and Science',NULL,'MUBS','Lebanon',TRUE,'Beirut Campus','Main','Beirut','Tariq El Jdideh',33.86944000,35.49722000),
(50,49,'Modern University for Business and Science',NULL,'MUBS','Lebanon',TRUE,'Damour Campus','Branch','Damour',NULL,33.73333000,35.46667000),
(51,49,'Modern University for Business and Science',NULL,'MUBS','Lebanon',TRUE,'Jbeil Campus','Branch','Jbeil',NULL,34.12083000,35.64722000),
(52,79,'Arab Open University - Lebanon',NULL,'AOU','Lebanon',TRUE,'Beirut Branch','Main','Beirut',NULL,33.88611000,35.51389000),
(53,79,'Arab Open University - Lebanon',NULL,'AOU','Lebanon',TRUE,'Tripoli Branch','Branch','Tripoli',NULL,34.43889000,35.84444000),
(54,79,'Arab Open University - Lebanon',NULL,'AOU','Lebanon',TRUE,'Saida Branch','Branch','Saida',NULL,33.56250000,35.37639000),
(55,79,'Arab Open University - Lebanon',NULL,'AOU','Lebanon',TRUE,'Zahle Branch','Branch','Zahle',NULL,33.83472000,35.90139000),
(56,56,'Middle East University',NULL,'MEU','Lebanon',TRUE,'Main Campus','Main','Beirut',NULL,33.86667000,35.51667000),
(57,57,'Jinan University',NULL,'JU','Lebanon',TRUE,'Main Campus','Main','Tripoli',NULL,34.43333000,35.83333000),
(58,58,'Rafik Hariri University',NULL,'RHU','Lebanon',TRUE,'Main Campus','Main','Damour',NULL,33.73333000,35.46667000),
(59,59,'Phoenicia University',NULL,'PU','Lebanon',FALSE,NULL,NULL,NULL,NULL,NULL,NULL),
(60,60,'Al Maaref University',NULL,'MU','Lebanon',TRUE,'Main Campus','Main','Beirut',NULL,NULL,NULL),
(61,61,'Lebanese Canadian University',NULL,'LCU','Lebanon',TRUE,'Main Campus','Main','Ain Aar',NULL,33.90833000,35.60000000),
(62,62,'Lebanese German University',NULL,'LGU','Lebanon',TRUE,'Main Campus','Main','Sahel Alma',NULL,33.98333000,35.63333000),
(63,63,'Université La Sagesse',NULL,'ULS','Lebanon',TRUE,'Main Campus','Main','Furn El Chebbak',NULL,33.86667000,35.53333000),
(64,64,'University of Sciences & Arts in Lebanon',NULL,'USAL','Lebanon',TRUE,'Main Campus','Main','Beirut',NULL,33.88333000,35.51667000),
(65,65,'Makassed University of Beirut',NULL,'MUB','Lebanon',TRUE,'Main','Main','Beirut',NULL,33.87547523,35.50341840),
(66,66,'Beirut Islamic University',NULL,'BIU','Lebanon',TRUE,'Main Campus','Main','Beirut',NULL,NULL,NULL),
(67,82,'Cnam Lebanon / ISSAE-Cnam Liban',NULL,'CNAM','Lebanon',TRUE,'Beirut Center','Main','Beirut','Bir Hassan',33.86882541,35.49388111),
(68,68,'Lebanese National Conservatory',NULL,'LNC','Lebanon',TRUE,'Main','Main','Beirut',NULL,33.87705941,35.53992880),
(69,69,'American University of Culture & Education',NULL,'AUCE','Lebanon',TRUE,'Badaro Campus','Main','Beirut','Badaro',NULL,NULL),
(70,70,'American University of Technology',NULL,'AUOT','Lebanon',TRUE,'Main Campus','Main','Halat',NULL,NULL,NULL),
(71,71,'Global University',NULL,'GU','Lebanon',TRUE,'Main','Main','Beirut',NULL,33.89066254,35.49689534),
(72,80,'Tripoli University Institute / University of Tripoli',NULL,'TUI','Lebanon',TRUE,'Tripoli Campus',NULL,'Tripoli',NULL,34.42109743,35.85232583),
(73,73,'Académie Libanaise des Beaux-Arts',NULL,'ALBA','Lebanon',TRUE,'Main Campus','Main','Beirut',NULL,33.89217000,35.49312000),
(74,81,'École Supérieure des Affaires',NULL,'ESA','Lebanon',TRUE,'Main Campus','Business','Beirut','Clemenceau',33.89812000,35.49408000),
(75,75,'Université Saint-Joseph de Beyrouth',NULL,'USJ','Lebanon',FALSE,NULL,NULL,NULL,NULL,NULL,NULL),
(76,76,'Notre Dame University-Louaize',NULL,'NDU','Lebanon',FALSE,NULL,NULL,NULL,NULL,NULL,NULL),
(77,77,'Antonine University',NULL,'UA','Lebanon',FALSE,NULL,NULL,NULL,NULL,NULL,NULL),
(78,78,'Arts, Sciences and Technology University in Lebanon',NULL,'AUL','Lebanon',FALSE,NULL,NULL,NULL,NULL,NULL,NULL),
(79,79,'Arab Open University - Lebanon',NULL,'AOU','Lebanon',FALSE,NULL,NULL,NULL,NULL,NULL,NULL),
(80,80,'Tripoli University Institute / University of Tripoli',NULL,'TUI','Lebanon',FALSE,NULL,NULL,NULL,NULL,NULL,NULL),
(81,81,'École Supérieure des Affaires',NULL,'ESA','Lebanon',FALSE,NULL,NULL,NULL,NULL,NULL,NULL),
(82,82,'Cnam Lebanon / ISSAE-Cnam Liban',NULL,'CNAM','Lebanon',FALSE,NULL,NULL,NULL,NULL,NULL,NULL);

DO $$
DECLARE
    expected_count BIGINT;
    mapped_count BIGINT;
BEGIN
    SELECT COUNT(*) INTO expected_count FROM university;
    SELECT COUNT(*) INTO mapped_count FROM university_normalization;
    IF expected_count <> mapped_count THEN
        RAISE EXCEPTION 'University normalization mapping covers %, but database contains % rows', mapped_count, expected_count;
    END IF;
    IF EXISTS (
        SELECT 1 FROM university_normalization m
        LEFT JOIN university u ON u.id = m.old_university_id
        WHERE u.id IS NULL
    ) THEN
        RAISE EXCEPTION 'University normalization mapping contains an ID absent from university';
    END IF;
    IF EXISTS (
        SELECT 1 FROM university_normalization
        WHERE creates_campus AND (NULLIF(BTRIM(campus_name), '') IS NULL OR NULLIF(BTRIM(city), '') IS NULL)
    ) THEN
        RAISE EXCEPTION 'Approved campus row is incomplete';
    END IF;
END $$;

INSERT INTO campus (university_id, name, campus_type, city, locality, latitude, longitude)
SELECT canonical_university_id, campus_name, campus_type, city, locality, latitude, longitude
FROM university_normalization
WHERE creates_campus
ON CONFLICT (university_id, name) DO UPDATE SET
    campus_type = EXCLUDED.campus_type,
    city = EXCLUDED.city,
    locality = EXCLUDED.locality,
    latitude = EXCLUDED.latitude,
    longitude = EXCLUDED.longitude,
    updated_at = NOW();

UPDATE university u
SET name = m.canonical_name,
    name_ar = m.canonical_name_ar,
    acronym = m.canonical_acronym,
    country = m.country
FROM university_normalization m
WHERE u.id = m.canonical_university_id;

DO $$
DECLARE
    ref RECORD;
BEGIN
    FOR ref IN
        SELECT ns.nspname AS schema_name,
               cls.relname AS table_name,
               att.attname AS column_name
        FROM pg_constraint con
        JOIN pg_class cls ON cls.oid = con.conrelid
        JOIN pg_namespace ns ON ns.oid = cls.relnamespace
        JOIN pg_class parent ON parent.oid = con.confrelid
        JOIN pg_namespace pns ON pns.oid = parent.relnamespace
        JOIN LATERAL unnest(con.conkey) WITH ORDINALITY AS key(attnum, ord) ON TRUE
        JOIN pg_attribute att ON att.attrelid = cls.oid AND att.attnum = key.attnum
        WHERE con.contype = 'f'
          AND pns.nspname = 'public'
          AND parent.relname = 'university'
          AND array_length(con.conkey, 1) = 1
    LOOP
        EXECUTE format(
            'UPDATE %I.%I target
                SET %I = mapping.canonical_university_id
              FROM university_normalization mapping
             WHERE target.%I = mapping.old_university_id
               AND mapping.old_university_id <> mapping.canonical_university_id',
            ref.schema_name, ref.table_name, ref.column_name, ref.column_name
        );
    END LOOP;
END $$;

DELETE FROM university u
USING university_normalization m
WHERE u.id = m.old_university_id
  AND m.old_university_id <> m.canonical_university_id;

ALTER TABLE university DROP CONSTRAINT IF EXISTS uq_university_name_campus;
ALTER TABLE university DROP COLUMN IF EXISTS city;
ALTER TABLE university DROP COLUMN IF EXISTS latitude;
ALTER TABLE university DROP COLUMN IF EXISTS longitude;
ALTER TABLE university DROP COLUMN IF EXISTS campus_name;
ALTER TABLE university DROP COLUMN IF EXISTS campus_type;

ALTER TABLE university
    ALTER COLUMN acronym SET NOT NULL,
    ALTER COLUMN country SET NOT NULL;

ALTER TABLE university ADD CONSTRAINT uq_university_acronym UNIQUE (acronym);
CREATE INDEX idx_university_name ON university(LOWER(BTRIM(name)));
CREATE INDEX idx_university_country ON university(LOWER(BTRIM(country)));

SELECT setval(
    pg_get_serial_sequence('university', 'id'),
    GREATEST((SELECT COALESCE(MAX(id), 1) FROM university), 1),
    TRUE
);

