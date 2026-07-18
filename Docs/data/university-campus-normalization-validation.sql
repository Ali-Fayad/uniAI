-- Read-only validation for Docs/data/university-campus-normalization.csv.
-- The import path is intentionally fixed for repeatable host/container use.
-- Copy the CSV to /tmp/university-campus-normalization.csv, then run this file.
-- Docker example from the repository root:
--   docker cp Docs/data/university-campus-normalization.csv \
--     uniai_postgres:/tmp/university-campus-normalization.csv
--   docker compose exec -T postgres psql -U uniai -d uniai -f - \
--     < Docs/data/university-campus-normalization-validation.sql
--
-- The script creates only a session-local temporary table and rolls back.
-- Every violation query should return zero rows.

\set ON_ERROR_STOP on

-- PostgreSQL does not allow CREATE TEMP TABLE inside a READ ONLY transaction.
-- This rollback-only transaction writes exclusively to the session-local table.
BEGIN;

CREATE TEMP TABLE normalization_mapping (
    old_university_id       BIGINT,
    old_name                TEXT,
    old_name_ar             TEXT,
    old_acronym             TEXT,
    old_country             TEXT,
    old_city                TEXT,
    old_campus_name         TEXT,
    old_campus_type         TEXT,
    old_latitude            NUMERIC,
    old_longitude           NUMERIC,
    canonical_university_id BIGINT,
    canonical_name          TEXT,
    canonical_name_ar       TEXT,
    canonical_acronym       TEXT,
    country                 TEXT,
    creates_campus          BOOLEAN,
    campus_name             TEXT,
    campus_type             TEXT,
    city                    TEXT,
    locality                TEXT,
    latitude                NUMERIC,
    longitude               NUMERIC,
    location_source         TEXT,
    location_source_type    TEXT,
    confidence              TEXT,
    review_status           TEXT,
    notes                   TEXT
) ON COMMIT DROP;

\copy normalization_mapping FROM '/tmp/university-campus-normalization.csv' WITH (FORMAT csv, HEADER true)

\echo 'Summary'
SELECT COUNT(*) AS mapping_rows,
       COUNT(DISTINCT old_university_id) AS distinct_old_ids,
       COUNT(DISTINCT canonical_university_id) AS canonical_institutions,
       COUNT(*) FILTER (WHERE creates_campus) AS proposed_campuses,
       COUNT(*) FILTER (WHERE review_status = 'APPROVED') AS approved_rows,
       COUNT(*) FILTER (WHERE review_status = 'NEEDS_REVIEW') AS needs_review_rows,
       COUNT(*) FILTER (WHERE review_status = 'BLOCKED') AS blocked_rows,
       COUNT(*) FILTER (WHERE review_status = 'DO_NOT_MIGRATE') AS do_not_migrate_rows
FROM normalization_mapping;

\echo 'Violation: unresolved review statuses'
SELECT old_university_id, review_status
FROM normalization_mapping
WHERE review_status IN ('NEEDS_REVIEW', 'BLOCKED')
ORDER BY old_university_id;

\echo 'Violation: DO_NOT_MIGRATE row proposes a campus'
SELECT old_university_id
FROM normalization_mapping
WHERE review_status = 'DO_NOT_MIGRATE'
  AND creates_campus
ORDER BY old_university_id;

\echo 'Violation: current university IDs missing from mapping or represented more than once'
SELECT u.id AS old_university_id, COUNT(m.old_university_id) AS mapping_count
FROM university u
LEFT JOIN normalization_mapping m ON m.old_university_id = u.id
GROUP BY u.id
HAVING COUNT(m.old_university_id) <> 1
ORDER BY u.id;

\echo 'Violation: mapping IDs not present in current university table'
SELECT m.old_university_id
FROM normalization_mapping m
LEFT JOIN university u ON u.id = m.old_university_id
WHERE u.id IS NULL
ORDER BY m.old_university_id;

\echo 'Violation: duplicate old IDs in CSV'
SELECT old_university_id, COUNT(*) AS occurrences
FROM normalization_mapping
GROUP BY old_university_id
HAVING COUNT(*) <> 1;

\echo 'Violation: old values differ from current database inventory'
SELECT m.old_university_id
FROM normalization_mapping m
JOIN university u ON u.id = m.old_university_id
WHERE BTRIM(COALESCE(m.old_name, '')) <> BTRIM(COALESCE(u.name, ''))
   OR BTRIM(COALESCE(m.old_name_ar, '')) <> BTRIM(COALESCE(u.name_ar, ''))
   OR BTRIM(COALESCE(m.old_acronym, '')) <> BTRIM(COALESCE(u.acronym, ''))
   OR BTRIM(COALESCE(m.old_country, '')) <> BTRIM(COALESCE(u.country, ''))
   OR BTRIM(COALESCE(m.old_city, '')) <> BTRIM(COALESCE(u.city, ''))
   OR BTRIM(COALESCE(m.old_campus_name, '')) <> BTRIM(COALESCE(u.campus_name, ''))
   OR BTRIM(COALESCE(m.old_campus_type, '')) <> BTRIM(COALESCE(u.campus_type, ''))
   OR m.old_latitude IS DISTINCT FROM u.latitude
   OR m.old_longitude IS DISTINCT FROM u.longitude
ORDER BY m.old_university_id;

\echo 'Violation: row does not map to a complete canonical institution'
SELECT old_university_id
FROM normalization_mapping
WHERE canonical_university_id IS NULL
   OR NULLIF(BTRIM(canonical_name), '') IS NULL
   OR NULLIF(BTRIM(canonical_acronym), '') IS NULL
   OR NULLIF(BTRIM(country), '') IS NULL
ORDER BY old_university_id;

\echo 'Violation: proposed canonical ID does not exist'
SELECT DISTINCT m.canonical_university_id
FROM normalization_mapping m
LEFT JOIN university u ON u.id = m.canonical_university_id
WHERE u.id IS NULL
ORDER BY m.canonical_university_id;

\echo 'Violation: one canonical ID has inconsistent institution identity'
SELECT canonical_university_id,
       COUNT(DISTINCT LOWER(BTRIM(canonical_name))) AS names,
       COUNT(DISTINCT LOWER(BTRIM(canonical_acronym))) AS acronyms,
       COUNT(DISTINCT LOWER(BTRIM(country))) AS countries
FROM normalization_mapping
GROUP BY canonical_university_id
HAVING COUNT(DISTINCT LOWER(BTRIM(canonical_name))) <> 1
    OR COUNT(DISTINCT LOWER(BTRIM(canonical_acronym))) <> 1
    OR COUNT(DISTINCT LOWER(BTRIM(country))) <> 1;

\echo 'Violation: canonical acronym maps to multiple canonical IDs'
SELECT LOWER(BTRIM(canonical_acronym)) AS canonical_acronym,
       COUNT(DISTINCT canonical_university_id) AS canonical_ids
FROM normalization_mapping
GROUP BY LOWER(BTRIM(canonical_acronym))
HAVING COUNT(DISTINCT canonical_university_id) <> 1;

\echo 'Violation: invalid enum-like values'
SELECT old_university_id, location_source_type, confidence, review_status
FROM normalization_mapping
WHERE location_source_type NOT IN ('DATABASE', 'REPOSITORY_RESEARCH', 'OFFICIAL_WEB', 'MANUAL_DECISION')
   OR confidence NOT IN ('HIGH', 'MEDIUM', 'LOW')
   OR review_status NOT IN ('APPROVED', 'NEEDS_REVIEW', 'BLOCKED', 'DO_NOT_MIGRATE')
ORDER BY old_university_id;

\echo 'Violation: approved campus is incomplete or lacks source evidence'
SELECT old_university_id
FROM normalization_mapping
WHERE review_status = 'APPROVED'
  AND creates_campus
  AND (NULLIF(BTRIM(campus_name), '') IS NULL
       OR NULLIF(BTRIM(city), '') IS NULL
       OR NULLIF(BTRIM(location_source), '') IS NULL)
ORDER BY old_university_id;

\echo 'Violation: institution-only legacy row creates a campus'
SELECT m.old_university_id
FROM normalization_mapping m
JOIN university u ON u.id = m.old_university_id
WHERE NULLIF(BTRIM(u.campus_name), '') IS NULL
  AND m.creates_campus
ORDER BY m.old_university_id;

\echo 'Violation: non-campus row contains target campus values'
SELECT old_university_id
FROM normalization_mapping
WHERE NOT creates_campus
  AND (NULLIF(BTRIM(campus_name), '') IS NOT NULL
       OR NULLIF(BTRIM(campus_type), '') IS NOT NULL
       OR NULLIF(BTRIM(city), '') IS NOT NULL
       OR NULLIF(BTRIM(locality), '') IS NOT NULL
       OR latitude IS NOT NULL
       OR longitude IS NOT NULL)
ORDER BY old_university_id;

\echo 'Violation: duplicate proposed campus business key'
SELECT canonical_university_id,
       LOWER(BTRIM(campus_name)) AS normalized_campus_name,
       COUNT(*) AS occurrences,
       ARRAY_AGG(old_university_id ORDER BY old_university_id) AS old_ids
FROM normalization_mapping
WHERE creates_campus
GROUP BY canonical_university_id, LOWER(BTRIM(campus_name))
HAVING COUNT(*) > 1;

\echo 'Violation: approved campus has invalid coordinates'
SELECT old_university_id, latitude, longitude
FROM normalization_mapping
WHERE review_status = 'APPROVED'
  AND creates_campus
  AND ((latitude IS NULL) <> (longitude IS NULL)
       OR latitude NOT BETWEEN -90 AND 90
       OR longitude NOT BETWEEN -180 AND 180)
ORDER BY old_university_id;

\echo 'Rows that are not migration-ready; migration must reject these'
SELECT old_university_id, review_status, notes
FROM normalization_mapping
WHERE review_status <> 'APPROVED'
ORDER BY old_university_id;

\echo 'Violation: BLOCKED or DO_NOT_MIGRATE row is classified as ready by status'
SELECT old_university_id, review_status
FROM normalization_mapping
WHERE review_status IN ('BLOCKED', 'DO_NOT_MIGRATE')
  AND review_status = 'APPROVED';

-- Direct university references used to assess whether selected canonical IDs
-- minimize foreign-key movement. Empty tables remain in the union intentionally.
WITH dependent_refs AS (
    SELECT 'accreditation' AS table_name, university_id FROM accreditation
    UNION ALL SELECT 'admission_deadline', university_id FROM admission_deadline
    UNION ALL SELECT 'admission_requirement', university_id FROM admission_requirement
    UNION ALL SELECT 'educations', university_id FROM educations WHERE university_id IS NOT NULL
    UNION ALL SELECT 'financial_aid', university_id FROM financial_aid
    UNION ALL SELECT 'graduate_accreditation', university_id FROM graduate_accreditation
    UNION ALL SELECT 'graduate_admission_deadline', university_id FROM graduate_admission_deadline
    UNION ALL SELECT 'graduate_admission_requirement', university_id FROM graduate_admission_requirement
    UNION ALL SELECT 'graduate_fee_item', university_id FROM graduate_fee_item
    UNION ALL SELECT 'graduate_financial_aid', university_id FROM graduate_financial_aid
    UNION ALL SELECT 'graduate_payment_plan', university_id FROM graduate_payment_plan
    UNION ALL SELECT 'graduate_program', university_id FROM graduate_program
    UNION ALL SELECT 'graduate_program_alias', university_id FROM graduate_program_alias
    UNION ALL SELECT 'graduate_program_relationship', university_id FROM graduate_program_relationship
    UNION ALL SELECT 'graduate_program_source', university_id FROM graduate_program_source
    UNION ALL SELECT 'graduate_program_track', university_id FROM graduate_program_track
    UNION ALL SELECT 'graduate_required_document', university_id FROM graduate_required_document
    UNION ALL SELECT 'graduate_scholarship', university_id FROM graduate_scholarship
    UNION ALL SELECT 'graduate_tuition_rate', university_id FROM graduate_tuition_rate
    UNION ALL SELECT 'major', university_id FROM major
    UNION ALL SELECT 'major_category', university_id FROM major_category
    UNION ALL SELECT 'major_degree', university_id FROM major_degree
    UNION ALL SELECT 'payment_plan', university_id FROM payment_plan
    UNION ALL SELECT 'required_document', university_id FROM required_document
    UNION ALL SELECT 'scholarship', university_id FROM scholarship
    UNION ALL SELECT 'source', university_id FROM source
    UNION ALL SELECT 'tuition_fee', university_id FROM tuition_fee
    UNION ALL SELECT 'university_department', university_id FROM university_department
    UNION ALL SELECT 'university_faculty', university_id FROM university_faculty
), counts AS (
    SELECT m.canonical_university_id,
           m.old_university_id,
           COUNT(r.university_id) AS dependent_rows
    FROM normalization_mapping m
    LEFT JOIN dependent_refs r ON r.university_id = m.old_university_id
    GROUP BY m.canonical_university_id, m.old_university_id
), ranked AS (
    SELECT *, MAX(dependent_rows) OVER (PARTITION BY canonical_university_id) AS group_max
    FROM counts
), best AS (
    SELECT canonical_university_id,
           MAX(group_max) AS group_max,
           ARRAY_AGG(old_university_id ORDER BY old_university_id)
               FILTER (WHERE dependent_rows = group_max) AS best_owner_ids
    FROM ranked
    GROUP BY canonical_university_id
), selected AS (
    SELECT canonical_university_id, dependent_rows AS selected_owner_rows
    FROM ranked
    WHERE old_university_id = canonical_university_id
)
SELECT s.canonical_university_id,
       s.selected_owner_rows,
       b.group_max,
       b.best_owner_ids
FROM selected s
JOIN best b USING (canonical_university_id)
WHERE s.selected_owner_rows < b.group_max
ORDER BY s.canonical_university_id;

\echo 'Potential dependent-row collisions after canonical remapping'
WITH remapped_keys AS (
    SELECT 'major_category' AS table_name, m.canonical_university_id,
           LOWER(BTRIM(x.name)) AS business_key, x.id
    FROM major_category x JOIN normalization_mapping m ON m.old_university_id = x.university_id
    UNION ALL
    SELECT 'major', m.canonical_university_id,
           COALESCE(x.major_category_id::TEXT, '<NULL>') || '|' || LOWER(BTRIM(x.name)), x.id
    FROM major x JOIN normalization_mapping m ON m.old_university_id = x.university_id
    UNION ALL
    SELECT 'major_degree', m.canonical_university_id, LOWER(BTRIM(x.name)), x.id
    FROM major_degree x JOIN normalization_mapping m ON m.old_university_id = x.university_id
    UNION ALL
    SELECT 'source', m.canonical_university_id, LOWER(BTRIM(x.url)), x.id
    FROM source x JOIN normalization_mapping m ON m.old_university_id = x.university_id
    UNION ALL
    SELECT 'scholarship', m.canonical_university_id, LOWER(BTRIM(x.name)), x.id
    FROM scholarship x JOIN normalization_mapping m ON m.old_university_id = x.university_id
    UNION ALL
    SELECT 'financial_aid', m.canonical_university_id, LOWER(BTRIM(x.name)), x.id
    FROM financial_aid x JOIN normalization_mapping m ON m.old_university_id = x.university_id
    UNION ALL
    SELECT 'payment_plan', m.canonical_university_id, LOWER(BTRIM(x.name)), x.id
    FROM payment_plan x JOIN normalization_mapping m ON m.old_university_id = x.university_id
    UNION ALL
    SELECT 'accreditation', m.canonical_university_id, LOWER(BTRIM(x.name)), x.id
    FROM accreditation x JOIN normalization_mapping m ON m.old_university_id = x.university_id
    UNION ALL
    SELECT 'university_faculty', m.canonical_university_id, LOWER(BTRIM(x.name)), x.id
    FROM university_faculty x JOIN normalization_mapping m ON m.old_university_id = x.university_id
    UNION ALL
    SELECT 'university_department', m.canonical_university_id,
           COALESCE(x.faculty_id::TEXT, '<NULL>') || '|' || LOWER(BTRIM(x.name)), x.id
    FROM university_department x JOIN normalization_mapping m ON m.old_university_id = x.university_id
    UNION ALL
    SELECT 'graduate_program', m.canonical_university_id, LOWER(BTRIM(x.program_key)), x.id
    FROM graduate_program x JOIN normalization_mapping m ON m.old_university_id = x.university_id
    UNION ALL
    SELECT 'graduate_program_alias', m.canonical_university_id,
           x.alias_type || '|' || LOWER(BTRIM(x.alias)), x.id
    FROM graduate_program_alias x JOIN normalization_mapping m ON m.old_university_id = x.university_id
    UNION ALL
    SELECT 'graduate_tuition_rate', m.canonical_university_id, LOWER(BTRIM(x.record_key)), x.id
    FROM graduate_tuition_rate x JOIN normalization_mapping m ON m.old_university_id = x.university_id
    UNION ALL
    SELECT 'graduate_fee_item', m.canonical_university_id, LOWER(BTRIM(x.record_key)), x.id
    FROM graduate_fee_item x JOIN normalization_mapping m ON m.old_university_id = x.university_id
    UNION ALL
    SELECT 'graduate_admission_requirement', m.canonical_university_id, LOWER(BTRIM(x.record_key)), x.id
    FROM graduate_admission_requirement x JOIN normalization_mapping m ON m.old_university_id = x.university_id
    UNION ALL
    SELECT 'graduate_required_document', m.canonical_university_id, LOWER(BTRIM(x.record_key)), x.id
    FROM graduate_required_document x JOIN normalization_mapping m ON m.old_university_id = x.university_id
    UNION ALL
    SELECT 'graduate_admission_deadline', m.canonical_university_id, LOWER(BTRIM(x.record_key)), x.id
    FROM graduate_admission_deadline x JOIN normalization_mapping m ON m.old_university_id = x.university_id
    UNION ALL
    SELECT 'graduate_scholarship', m.canonical_university_id, LOWER(BTRIM(x.record_key)), x.id
    FROM graduate_scholarship x JOIN normalization_mapping m ON m.old_university_id = x.university_id
    UNION ALL
    SELECT 'graduate_financial_aid', m.canonical_university_id, LOWER(BTRIM(x.record_key)), x.id
    FROM graduate_financial_aid x JOIN normalization_mapping m ON m.old_university_id = x.university_id
    UNION ALL
    SELECT 'graduate_payment_plan', m.canonical_university_id, LOWER(BTRIM(x.record_key)), x.id
    FROM graduate_payment_plan x JOIN normalization_mapping m ON m.old_university_id = x.university_id
    UNION ALL
    SELECT 'graduate_accreditation', m.canonical_university_id, LOWER(BTRIM(x.record_key)), x.id
    FROM graduate_accreditation x JOIN normalization_mapping m ON m.old_university_id = x.university_id
)
SELECT table_name, canonical_university_id, business_key,
       COUNT(*) AS colliding_rows,
       ARRAY_AGG(id ORDER BY id) AS row_ids
FROM remapped_keys
GROUP BY table_name, canonical_university_id, business_key
HAVING COUNT(*) > 1
ORDER BY table_name, canonical_university_id, business_key;

ROLLBACK;
