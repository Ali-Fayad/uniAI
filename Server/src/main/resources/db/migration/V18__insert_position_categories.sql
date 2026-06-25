-- Seed position category reference data.

INSERT INTO position_category (id, name, created_at, updated_at)
VALUES
(1, 'Software Engineering', NOW(), NOW()),
(2, 'Frontend Development', NOW(), NOW()),
(3, 'Backend Development', NOW(), NOW()),
(4, 'Mobile Development', NOW(), NOW()),
(5, 'Data & AI', NOW(), NOW()),
(6, 'DevOps & Infrastructure', NOW(), NOW()),
(7, 'Cybersecurity', NOW(), NOW()),
(8, 'QA & Testing', NOW(), NOW()),
(9, 'Design & UX', NOW(), NOW()),
(10, 'Product & Project Management', NOW(), NOW()),
(11, 'Business & Operations', NOW(), NOW()),
(12, 'Marketing & Sales', NOW(), NOW()),
(13, 'Finance & Accounting', NOW(), NOW()),
(14, 'Human Resources', NOW(), NOW()),
(15, 'Education & Training', NOW(), NOW()),
(16, 'Engineering', NOW(), NOW()),
(17, 'Healthcare', NOW(), NOW()),
(18, 'Internship & Entry Level', NOW(), NOW())
ON CONFLICT (name) DO UPDATE
SET updated_at = NOW();

SELECT setval(
    pg_get_serial_sequence('position_category', 'id'),
    COALESCE((SELECT MAX(id) FROM position_category), 1)
);
