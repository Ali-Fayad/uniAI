-- Prepare schema transition from legacy CV/user verification tables.

-- Keep a backup of old universities data for V8 migration.
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_schema = 'public' AND table_name = 'universities'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_schema = 'public' AND table_name = 'universities_legacy'
    ) THEN
        ALTER TABLE universities RENAME TO universities_legacy;
    END IF;
END $$;

-- Drop old verification code table.
DROP TABLE IF EXISTS verify_codes;

-- Drop old personal_info definition (it will be recreated in V6).
DROP TABLE IF EXISTS personal_info CASCADE;

-- Drop old CV section tables (plural legacy naming).
DROP TABLE IF EXISTS experience_achievements;
DROP TABLE IF EXISTS project_technologies;
DROP TABLE IF EXISTS educations;
DROP TABLE IF EXISTS experiences;
DROP TABLE IF EXISTS skills;
DROP TABLE IF EXISTS projects;
DROP TABLE IF EXISTS languages;
DROP TABLE IF EXISTS certificates;
DROP TABLE IF EXISTS cvs;
