CREATE TABLE IF NOT EXISTS ecosystems (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(255) NOT NULL,
    description TEXT,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS logs (
    id UUID PRIMARY KEY,
    ecosystem_id UUID NOT NULL,
    temperature_c DOUBLE PRECISION,
    humidity_percent INTEGER,
    event_type VARCHAR(255) NOT NULL,
    notes TEXT,
    recorded_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_logs_ecosystem
        FOREIGN KEY (ecosystem_id)
        REFERENCES ecosystems (id)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_logs_ecosystem_id ON logs (ecosystem_id);
CREATE INDEX IF NOT EXISTS idx_logs_recorded_at ON logs (recorded_at DESC);
