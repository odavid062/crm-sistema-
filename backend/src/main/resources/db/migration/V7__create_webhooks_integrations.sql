-- ============================================================
-- V7: Webhooks e Integrações (n8n)
-- ============================================================

CREATE TABLE webhook_configs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    url TEXT NOT NULL,
    secret VARCHAR(255),
    events TEXT[] NOT NULL DEFAULT '{}',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    headers JSONB DEFAULT '{}',
    created_by UUID REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP
);

CREATE TABLE webhook_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    webhook_id UUID REFERENCES webhook_configs(id) ON DELETE CASCADE,
    event VARCHAR(100) NOT NULL,
    payload JSONB,
    response_status INTEGER,
    response_body TEXT,
    error_message TEXT,
    delivered_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE integration_configs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    type VARCHAR(50) NOT NULL UNIQUE,
    config JSONB NOT NULL DEFAULT '{}',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_by UUID REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP
);

CREATE INDEX idx_webhook_logs_webhook ON webhook_logs(webhook_id);
CREATE INDEX idx_webhook_logs_event ON webhook_logs(event);
