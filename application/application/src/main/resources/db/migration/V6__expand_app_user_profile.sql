ALTER TABLE app_user
ADD COLUMN IF NOT EXISTS display_name VARCHAR(60);

ALTER TABLE app_user
ADD COLUMN IF NOT EXISTS first_name VARCHAR(60);

ALTER TABLE app_user
ADD COLUMN IF NOT EXISTS last_name VARCHAR(60);

ALTER TABLE app_user
ADD COLUMN IF NOT EXISTS email VARCHAR(120);

UPDATE app_user
SET
    display_name = COALESCE(display_name, username),
    first_name = COALESCE(first_name, 'Demo'),
    last_name = COALESCE(last_name, 'User'),
    email = COALESCE(email, username || '@local.test');

ALTER TABLE app_user
ALTER COLUMN display_name SET NOT NULL;

ALTER TABLE app_user
ALTER COLUMN first_name SET NOT NULL;

ALTER TABLE app_user
ALTER COLUMN last_name SET NOT NULL;

ALTER TABLE app_user
ALTER COLUMN email SET NOT NULL;
