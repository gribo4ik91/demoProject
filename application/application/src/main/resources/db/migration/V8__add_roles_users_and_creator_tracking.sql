ALTER TABLE app_user
ADD COLUMN IF NOT EXISTS role VARCHAR(20);

UPDATE app_user
SET role = COALESCE(role, 'USER');

UPDATE app_user
SET role = 'ADMIN'
WHERE id = (
    SELECT id
    FROM app_user
    ORDER BY created_at ASC, username ASC
    LIMIT 1
)
AND NOT EXISTS (
    SELECT 1
    FROM app_user
    WHERE role = 'ADMIN'
);

ALTER TABLE app_user
ALTER COLUMN role SET NOT NULL;

ALTER TABLE ecosystems
ADD COLUMN IF NOT EXISTS created_by_user_id UUID,
ADD COLUMN IF NOT EXISTS created_by_username VARCHAR(40),
ADD COLUMN IF NOT EXISTS created_by_display_name VARCHAR(60);

ALTER TABLE ecosystems
ADD CONSTRAINT fk_ecosystems_created_by_user
    FOREIGN KEY (created_by_user_id)
    REFERENCES app_user (id)
    ON DELETE SET NULL;

ALTER TABLE logs
ADD COLUMN IF NOT EXISTS created_by_user_id UUID,
ADD COLUMN IF NOT EXISTS created_by_username VARCHAR(40),
ADD COLUMN IF NOT EXISTS created_by_display_name VARCHAR(60);

ALTER TABLE logs
ADD CONSTRAINT fk_logs_created_by_user
    FOREIGN KEY (created_by_user_id)
    REFERENCES app_user (id)
    ON DELETE SET NULL;

ALTER TABLE maintenance_tasks
ADD COLUMN IF NOT EXISTS created_by_user_id UUID,
ADD COLUMN IF NOT EXISTS created_by_username VARCHAR(40),
ADD COLUMN IF NOT EXISTS created_by_display_name VARCHAR(60);

ALTER TABLE maintenance_tasks
ADD CONSTRAINT fk_maintenance_tasks_created_by_user
    FOREIGN KEY (created_by_user_id)
    REFERENCES app_user (id)
    ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS idx_ecosystems_created_by_user_id ON ecosystems (created_by_user_id);
CREATE INDEX IF NOT EXISTS idx_logs_created_by_user_id ON logs (created_by_user_id);
CREATE INDEX IF NOT EXISTS idx_maintenance_tasks_created_by_user_id ON maintenance_tasks (created_by_user_id);
