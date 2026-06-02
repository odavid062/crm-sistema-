-- ============================================================
-- V3: Pipelines e Deals (Funil de Vendas)
-- ============================================================

CREATE TABLE pipelines (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_by UUID REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP
);

CREATE TABLE pipeline_stages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pipeline_id UUID NOT NULL REFERENCES pipelines(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    color VARCHAR(7) DEFAULT '#6366f1',
    position INTEGER NOT NULL DEFAULT 0,
    win_probability DECIMAL(5,2) DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE deals (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(255) NOT NULL,
    value DECIMAL(15,2) DEFAULT 0,
    currency VARCHAR(3) DEFAULT 'BRL',
    stage_id UUID NOT NULL REFERENCES pipeline_stages(id) ON DELETE RESTRICT,
    pipeline_id UUID NOT NULL REFERENCES pipelines(id) ON DELETE RESTRICT,
    contact_id UUID REFERENCES contacts(id) ON DELETE SET NULL,
    company_id UUID REFERENCES companies(id) ON DELETE SET NULL,
    owner_id UUID REFERENCES users(id) ON DELETE SET NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    priority VARCHAR(10) DEFAULT 'MEDIUM',
    expected_close_date DATE,
    closed_at TIMESTAMP,
    lost_reason TEXT,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP
);

CREATE INDEX idx_deals_stage ON deals(stage_id);
CREATE INDEX idx_deals_pipeline ON deals(pipeline_id);
CREATE INDEX idx_deals_contact ON deals(contact_id);
CREATE INDEX idx_deals_owner ON deals(owner_id);
CREATE INDEX idx_deals_status ON deals(status);
CREATE INDEX idx_pipeline_stages_pipeline ON pipeline_stages(pipeline_id);
