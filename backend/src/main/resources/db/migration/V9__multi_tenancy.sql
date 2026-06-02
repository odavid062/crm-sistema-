-- ============================================================
-- V9: Multi-tenancy (SaaS) - Planos, Tenants, API Tokens, Canais
-- Transforma o CRM em sistema multi-empresa com super admin.
-- ============================================================

-- ---------- Planos ----------
CREATE TABLE plans (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    description TEXT,
    price DECIMAL(15,2) NOT NULL DEFAULT 0,
    max_users INTEGER NOT NULL DEFAULT 5,
    max_channels INTEGER NOT NULL DEFAULT 1,
    max_queues INTEGER NOT NULL DEFAULT 3,
    max_contacts INTEGER NOT NULL DEFAULT 1000,
    features JSONB DEFAULT '{}',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP
);

-- ---------- Tenants (empresas clientes do SaaS) ----------
CREATE TABLE tenants (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(100) NOT NULL UNIQUE,
    document VARCHAR(18),
    email VARCHAR(255),
    phone VARCHAR(20),
    logo_url TEXT,
    plan_id UUID REFERENCES plans(id) ON DELETE SET NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'TRIAL',
    trial_ends_at DATE,
    due_date DATE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP
);

CREATE INDEX idx_tenants_slug ON tenants(slug);
CREATE INDEX idx_tenants_status ON tenants(status);

-- ---------- Plano e Tenant padrão (para migrar dados existentes) ----------
INSERT INTO plans (id, name, description, price, max_users, max_channels, max_queues, max_contacts)
VALUES ('00000000-0000-0000-0000-000000000001', 'Profissional', 'Plano padrão de migração', 199.90, 25, 5, 10, 50000);

INSERT INTO plans (name, description, price, max_users, max_channels, max_queues, max_contacts) VALUES
    ('Free', 'Plano gratuito para testes', 0, 2, 1, 1, 100),
    ('Starter', 'Para pequenas equipes', 99.90, 5, 2, 3, 5000),
    ('Enterprise', 'Recursos ilimitados', 499.90, 999, 99, 99, 999999);

INSERT INTO tenants (id, name, slug, status, plan_id)
VALUES ('00000000-0000-0000-0000-000000000001', 'Empresa Demo', 'demo', 'ACTIVE',
        '00000000-0000-0000-0000-000000000001');

-- ---------- Usuários: tenant_id + suporte a SUPER_ADMIN ----------
ALTER TABLE users ADD COLUMN tenant_id UUID REFERENCES tenants(id) ON DELETE CASCADE;
UPDATE users SET tenant_id = '00000000-0000-0000-0000-000000000001';
CREATE INDEX idx_users_tenant ON users(tenant_id);

-- Super admin global (sem tenant). Senha: Admin@123
INSERT INTO users (name, email, password, role, tenant_id)
VALUES (
    'Super Admin',
    'super@crm.com',
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
    'SUPER_ADMIN',
    NULL
) ON CONFLICT (email) DO NOTHING;

-- ---------- API Tokens (Bearer para integração externa) ----------
CREATE TABLE api_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    token_prefix VARCHAR(20) NOT NULL,
    scopes TEXT[] DEFAULT '{}',
    last_used_at TIMESTAMP,
    expires_at TIMESTAMP,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_by UUID REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_api_tokens_tenant ON api_tokens(tenant_id);
CREATE INDEX idx_api_tokens_hash ON api_tokens(token_hash);

-- ---------- Canais (WhatsApp, Instagram, etc.) ----------
CREATE TABLE channels (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DISCONNECTED',
    config JSONB DEFAULT '{}',
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP
);

CREATE INDEX idx_channels_tenant ON channels(tenant_id);
CREATE INDEX idx_channels_type ON channels(type);

-- ============================================================
-- Adiciona tenant_id a todas as tabelas do CRM + backfill
-- ============================================================
DO $$
DECLARE
    tbl TEXT;
    crm_tables TEXT[] := ARRAY[
        'companies','contacts','tags','pipelines','pipeline_stages','deals',
        'activities','notes','notifications','whatsapp_conversations',
        'whatsapp_messages','customers_asaas','payments','payment_subscriptions',
        'webhook_configs','webhook_logs','integration_configs'
    ];
BEGIN
    FOREACH tbl IN ARRAY crm_tables LOOP
        EXECUTE format('ALTER TABLE %I ADD COLUMN tenant_id UUID', tbl);
        EXECUTE format('UPDATE %I SET tenant_id = %L', tbl, '00000000-0000-0000-0000-000000000001');
        EXECUTE format('ALTER TABLE %I ALTER COLUMN tenant_id SET NOT NULL', tbl);
        EXECUTE format('ALTER TABLE %I ADD CONSTRAINT fk_%I_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE', tbl, tbl);
        EXECUTE format('CREATE INDEX idx_%I_tenant ON %I(tenant_id)', tbl, tbl);
    END LOOP;
END $$;

-- Tags passam a ser únicas por tenant (não mais globais)
ALTER TABLE tags DROP CONSTRAINT IF EXISTS tags_name_key;
ALTER TABLE tags ADD CONSTRAINT tags_tenant_name_unique UNIQUE (tenant_id, name);
