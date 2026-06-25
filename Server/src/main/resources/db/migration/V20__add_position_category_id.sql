-- Add normalized position category storage.

ALTER TABLE position
    ADD COLUMN IF NOT EXISTS category_id BIGINT;

CREATE INDEX IF NOT EXISTS idx_position_category_id ON position(category_id);

ALTER TABLE position
    ADD CONSTRAINT fk_position_category
        FOREIGN KEY (category_id) REFERENCES position_category(id);
