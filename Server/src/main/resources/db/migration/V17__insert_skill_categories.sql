-- Seed skill category reference data.

INSERT INTO skill_category (id, name, created_at, updated_at)
VALUES
(1, 'Programming Languages', NOW(), NOW()),
(2, 'Frontend Development', NOW(), NOW()),
(3, 'Backend Development', NOW(), NOW()),
(4, 'Mobile Development', NOW(), NOW()),
(5, 'Database', NOW(), NOW()),
(6, 'DevOps & Cloud', NOW(), NOW()),
(7, 'Data & Analytics', NOW(), NOW()),
(8, 'AI & Machine Learning', NOW(), NOW()),
(9, 'Cybersecurity', NOW(), NOW()),
(10, 'Testing & QA', NOW(), NOW()),
(11, 'UI/UX & Design', NOW(), NOW()),
(12, 'Product & Project Management', NOW(), NOW()),
(13, 'Business & Marketing', NOW(), NOW()),
(14, 'Finance & Accounting', NOW(), NOW()),
(15, 'Communication & Soft Skills', NOW(), NOW()),
(16, 'Office & Productivity Tools', NOW(), NOW()),
(17, 'Engineering & CAD', NOW(), NOW()),
(18, 'Healthcare & Science', NOW(), NOW())
ON CONFLICT (name) DO UPDATE
SET updated_at = NOW();

SELECT setval(
    pg_get_serial_sequence('skill_category', 'id'),
    COALESCE((SELECT MAX(id) FROM skill_category), 1)
);
