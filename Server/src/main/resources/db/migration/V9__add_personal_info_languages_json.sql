-- Add languages section storage for personal info and guard existing JSON list columns.
ALTER TABLE personal_info
    ADD COLUMN IF NOT EXISTS education_json TEXT,
    ADD COLUMN IF NOT EXISTS skills_json TEXT,
    ADD COLUMN IF NOT EXISTS languages_json TEXT,
    ADD COLUMN IF NOT EXISTS experience_json TEXT;
