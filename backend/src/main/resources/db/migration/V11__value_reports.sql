-- ============================================================
-- V11: Módulo "Relatório de Valor Gerado" (BI / ROI)
-- ============================================================

-- Fonte das métricas de automação: cada ação executada pelo n8n
-- (ou pelo próprio CRM) é registrada aqui via API token.
CREATE TABLE automation_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    type VARCHAR(40) NOT NULL,          -- MESSAGE_SENT, FOLLOWUP, LEAD_REENGAGED, LEAD_RECOVERED, TASK_AUTOMATED, OTHER
    channel VARCHAR(30),                -- WHATSAPP, INSTAGRAM, FACEBOOK, SITE, TELEGRAM, OTHER
    contact_id UUID REFERENCES contacts(id) ON DELETE SET NULL,
    success BOOLEAN NOT NULL DEFAULT TRUE,
    minutes_saved NUMERIC(10,2) NOT NULL DEFAULT 0,
    source VARCHAR(50) DEFAULT 'n8n',
    metadata JSONB DEFAULT '{}',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_automation_logs_tenant ON automation_logs(tenant_id);
CREATE INDEX idx_automation_logs_type ON automation_logs(type);
CREATE INDEX idx_automation_logs_channel ON automation_logs(channel);
CREATE INDEX idx_automation_logs_created ON automation_logs(created_at);

-- Config de ROI por tenant (1 linha por empresa)
CREATE TABLE report_settings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL UNIQUE REFERENCES tenants(id) ON DELETE CASCADE,
    monthly_investment NUMERIC(15,2) NOT NULL DEFAULT 0,
    currency VARCHAR(3) NOT NULL DEFAULT 'BRL',
    minutes_per_message NUMERIC(6,2) NOT NULL DEFAULT 2,
    minutes_per_followup NUMERIC(6,2) NOT NULL DEFAULT 5,
    minutes_per_task NUMERIC(6,2) NOT NULL DEFAULT 10,
    hourly_cost NUMERIC(10,2) NOT NULL DEFAULT 50,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ
);
CREATE INDEX idx_report_settings_tenant ON report_settings(tenant_id);

-- Snapshots de relatórios gerados (histórico para comparação)
CREATE TABLE value_reports (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    period_type VARCHAR(15) NOT NULL,   -- MONTHLY, QUARTERLY, ANNUAL
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    status VARCHAR(15) NOT NULL DEFAULT 'READY',  -- GENERATING, READY, FAILED
    metrics JSONB NOT NULL DEFAULT '{}',
    executive_summary TEXT,
    revenue NUMERIC(15,2) NOT NULL DEFAULT 0,
    investment NUMERIC(15,2) NOT NULL DEFAULT 0,
    roi_percent NUMERIC(10,2) NOT NULL DEFAULT 0,
    hours_saved NUMERIC(10,2) NOT NULL DEFAULT 0,
    generated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (tenant_id, period_type, period_start)
);
CREATE INDEX idx_value_reports_tenant ON value_reports(tenant_id);
CREATE INDEX idx_value_reports_period ON value_reports(period_type, period_start);
