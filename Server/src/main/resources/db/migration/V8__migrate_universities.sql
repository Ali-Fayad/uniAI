-- Migrate universities from renamed legacy table when available.
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_schema = 'public' AND table_name = 'universities_legacy'
    ) THEN
        INSERT INTO university (
            name,
            name_ar,
            acronym,
            latitude,
            longitude,
            campus_name,
            campus_type
        )
        SELECT
            ul.name,
            ul.name_ar,
            ul.acronym,
            ul.latitude,
            ul.longitude,
            ul.campus_name,
            ul.campus_type
        FROM universities_legacy ul
        ON CONFLICT (name, campus_name) DO UPDATE
        SET name_ar = EXCLUDED.name_ar,
            acronym = EXCLUDED.acronym,
            latitude = EXCLUDED.latitude,
            longitude = EXCLUDED.longitude,
            campus_type = EXCLUDED.campus_type;
    END IF;
END $$;
