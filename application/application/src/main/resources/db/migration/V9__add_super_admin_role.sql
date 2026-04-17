UPDATE app_user
SET role = 'SUPER_ADMIN'
WHERE id = (
    SELECT id
    FROM app_user
    ORDER BY created_at ASC, username ASC
    LIMIT 1
);

UPDATE app_user
SET role = 'ADMIN'
WHERE role NOT IN ('SUPER_ADMIN', 'USER');
