INSERT INTO language (name, code, native_name)
VALUES
('English', 'en', 'English'),
('Arabic', 'ar', 'العربية'),
('French', 'fr', 'Français'),
('Spanish', 'es', 'Español'),
('German', 'de', 'Deutsch'),
('Italian', 'it', 'Italiano'),
('Turkish', 'tr', 'Türkçe'),
('Armenian', 'hy', 'Հայերեն'),
('Russian', 'ru', 'Русский'),
('Chinese', 'zh', '中文'),
('Japanese', 'ja', '日本語'),
('Portuguese', 'pt', 'Português'),
('Greek', 'el', 'Ελληνικά'),
('Swedish', 'sv', 'Svenska'),
('Dutch', 'nl', 'Nederlands')
ON CONFLICT (code) DO UPDATE
SET name = EXCLUDED.name,
    native_name = EXCLUDED.native_name;
