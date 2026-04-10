CREATE TABLE IF NOT EXISTS cv_templates (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    thumbnail_url VARCHAR(500),
    component_name VARCHAR(100) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_cv_templates_component_name UNIQUE (component_name)
);

ALTER TABLE cvs
    ADD COLUMN IF NOT EXISTS template_id BIGINT;

ALTER TABLE cvs
    ADD COLUMN IF NOT EXISTS sections_order JSONB NOT NULL DEFAULT '["education","experience","skills","languages","projects","certificates"]'::jsonb;

ALTER TABLE cvs
    ADD CONSTRAINT fk_cvs_template
        FOREIGN KEY (template_id) REFERENCES cv_templates(id) ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS idx_cvs_template_id ON cvs(template_id);
CREATE INDEX IF NOT EXISTS idx_cvs_sections_order ON cvs USING GIN (sections_order);

INSERT INTO cv_templates (name, description, thumbnail_url, component_name, is_active)
VALUES
('Modern', 'Clean and minimal layout with generous whitespace.', '/templates/modern.png', 'ModernTemplate', TRUE),
('Classic', 'Traditional serif style suitable for formal roles.', '/templates/classic.png', 'ClassicTemplate', TRUE),
('Academic', 'Research-oriented structure with publication focus.', '/templates/academic.png', 'AcademicTemplate', TRUE),
('Creative', 'Bold visual hierarchy for design-forward profiles.', '/templates/creative.png', 'CreativeTemplate', TRUE),
('Executive', 'Corporate-first presentation for leadership roles.', '/templates/executive.png', 'ExecutiveTemplate', TRUE),
('Technical', 'Skill-focused grid and project-heavy presentation.', '/templates/technical.png', 'TechnicalTemplate', TRUE),
('Compact', 'Space-efficient one-page layout for concise resumes.', '/templates/compact.png', 'CompactTemplate', TRUE)
ON CONFLICT (component_name) DO UPDATE
SET name = EXCLUDED.name,
    description = EXCLUDED.description,
    thumbnail_url = EXCLUDED.thumbnail_url,
    is_active = EXCLUDED.is_active,
    updated_at = NOW();
