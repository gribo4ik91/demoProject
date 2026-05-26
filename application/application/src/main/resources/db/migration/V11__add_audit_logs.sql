CREATE TABLE IF NOT EXISTS audit_logs (
    id UUID PRIMARY KEY,
    entity_type VARCHAR(40) NOT NULL,
    entity_id UUID,
    entity_name VARCHAR(160),
    action VARCHAR(40) NOT NULL,
    field_name VARCHAR(80),
    old_value TEXT,
    new_value TEXT,
    created_by_username VARCHAR(40),
    created_by_display_name VARCHAR(60),
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_audit_logs_created_at ON audit_logs (created_at DESC);
CREATE INDEX IF NOT EXISTS idx_audit_logs_entity ON audit_logs (entity_type, entity_id);
