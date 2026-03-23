-- Convert personal_info to use user_id as the primary key and enforce user ownership.

ALTER TABLE personal_info
    ALTER COLUMN user_id SET NOT NULL;

ALTER TABLE personal_info
    DROP CONSTRAINT IF EXISTS uq_personal_info_user;

ALTER TABLE personal_info
    DROP CONSTRAINT IF EXISTS personal_info_pkey;

ALTER TABLE personal_info
    ADD CONSTRAINT personal_info_pkey PRIMARY KEY (user_id);

ALTER TABLE personal_info
    DROP COLUMN IF EXISTS id;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_schema = 'public' AND table_name = 'users'
    )
    AND NOT EXISTS (
        SELECT 1
        FROM information_schema.table_constraints
        WHERE table_schema = 'public'
          AND table_name = 'personal_info'
          AND constraint_name = 'fk_personal_info_user'
          AND constraint_type = 'FOREIGN KEY'
    ) THEN
        ALTER TABLE personal_info
            ADD CONSTRAINT fk_personal_info_user
            FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
    END IF;
END $$;
