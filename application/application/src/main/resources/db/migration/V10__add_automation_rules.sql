CREATE TABLE IF NOT EXISTS automation_rules (
    id UUID PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    scope_type VARCHAR(40) NOT NULL,
    ecosystem_type VARCHAR(50),
    trigger_type VARCHAR(40) NOT NULL,
    event_type VARCHAR(50),
    inactivity_days INT,
    delay_days INT,
    task_title VARCHAR(160) NOT NULL,
    task_type VARCHAR(50) NOT NULL,
    prevent_duplicates BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_automation_rules_trigger_event
    ON automation_rules (enabled, trigger_type, event_type);

CREATE INDEX IF NOT EXISTS idx_automation_rules_scope
    ON automation_rules (scope_type, ecosystem_type);

INSERT INTO automation_rules (
    id,
    name,
    enabled,
    scope_type,
    ecosystem_type,
    trigger_type,
    event_type,
    inactivity_days,
    delay_days,
    task_title,
    task_type,
    prevent_duplicates,
    created_at,
    updated_at
)
SELECT
    '6f6fcb24-bfd7-4ab6-9f9d-1f8a0c4f6401',
    'Post-watering moisture check',
    TRUE,
    'ALL_ECOSYSTEMS',
    NULL,
    'AFTER_EVENT',
    'WATERING',
    NULL,
    1,
    'Inspect moisture balance after watering',
    'INSPECTION',
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1
    FROM automation_rules
    WHERE name = 'Post-watering moisture check'
);

INSERT INTO automation_rules (
    id,
    name,
    enabled,
    scope_type,
    ecosystem_type,
    trigger_type,
    event_type,
    inactivity_days,
    delay_days,
    task_title,
    task_type,
    prevent_duplicates,
    created_at,
    updated_at
)
SELECT
    '57ce1035-f40a-4381-b5fe-8fca4413e4e7',
    'Post-feeding behavior observation',
    TRUE,
    'ALL_ECOSYSTEMS',
    NULL,
    'AFTER_EVENT',
    'FEEDING',
    NULL,
    1,
    'Log feeding response check',
    'INSPECTION',
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1
    FROM automation_rules
    WHERE name = 'Post-feeding behavior observation'
);
