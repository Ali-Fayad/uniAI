-- Add JSON-backed list sections for the personal info profile page.
ALTER TABLE personal_info
    ADD COLUMN IF NOT EXISTS education_json TEXT,
    ADD COLUMN IF NOT EXISTS skills_json TEXT,
    ADD COLUMN IF NOT EXISTS experience_json TEXT;
