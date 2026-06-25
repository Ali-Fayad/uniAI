-- Add normalized skill category storage while keeping the legacy text column for application compatibility.

ALTER TABLE skill
    ADD COLUMN IF NOT EXISTS category_id BIGINT;

UPDATE skill s
SET category_id = sc.id
FROM skill_category sc
WHERE s.category_id IS NULL
  AND s.category IS NOT NULL
  AND LOWER(TRIM(s.category)) = LOWER(sc.name);

CREATE INDEX IF NOT EXISTS idx_skill_category_id ON skill(category_id);

ALTER TABLE skill
    ADD CONSTRAINT fk_skill_category
        FOREIGN KEY (category_id) REFERENCES skill_category(id);
