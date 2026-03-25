ALTER TABLE personal_info
    ADD COLUMN IF NOT EXISTS projects_json TEXT,
    ADD COLUMN IF NOT EXISTS certificates_json TEXT;
