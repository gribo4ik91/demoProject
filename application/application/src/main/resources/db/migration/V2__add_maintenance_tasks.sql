CREATE TABLE IF NOT EXISTS maintenance_tasks (
    id UUID PRIMARY KEY,
    ecosystem_id UUID NOT NULL,
    title VARCHAR(120) NOT NULL,
    task_type VARCHAR(50) NOT NULL,
    due_date DATE,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_tasks_ecosystem
        FOREIGN KEY (ecosystem_id)
        REFERENCES ecosystems (id)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_tasks_ecosystem_id ON maintenance_tasks (ecosystem_id);
CREATE INDEX IF NOT EXISTS idx_tasks_status_due_date ON maintenance_tasks (status, due_date);
