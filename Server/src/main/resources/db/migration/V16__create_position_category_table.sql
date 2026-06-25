-- Create the position category lookup table.

CREATE TABLE IF NOT EXISTS position_category (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_position_category_name UNIQUE (name)
);
