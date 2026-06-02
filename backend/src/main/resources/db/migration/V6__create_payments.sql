-- ============================================================
-- V6: Pagamentos (Asaas)
-- ============================================================

CREATE TABLE customers_asaas (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    contact_id UUID REFERENCES contacts(id) ON DELETE CASCADE,
    asaas_id VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE payments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    asaas_id VARCHAR(100) UNIQUE,
    contact_id UUID REFERENCES contacts(id) ON DELETE SET NULL,
    deal_id UUID REFERENCES deals(id) ON DELETE SET NULL,
    description VARCHAR(255) NOT NULL,
    value DECIMAL(15,2) NOT NULL,
    billing_type VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    due_date DATE NOT NULL,
    payment_date DATE,
    invoice_url TEXT,
    bank_slip_url TEXT,
    pix_code TEXT,
    pix_qr_code_image TEXT,
    nosso_numero VARCHAR(50),
    invoice_number VARCHAR(50),
    external_reference VARCHAR(255),
    created_by UUID REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP
);

CREATE TABLE payment_subscriptions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    asaas_id VARCHAR(100) UNIQUE,
    contact_id UUID REFERENCES contacts(id) ON DELETE SET NULL,
    description VARCHAR(255) NOT NULL,
    value DECIMAL(15,2) NOT NULL,
    billing_type VARCHAR(30) NOT NULL,
    cycle VARCHAR(20) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    next_due_date DATE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP
);

CREATE INDEX idx_payments_contact ON payments(contact_id);
CREATE INDEX idx_payments_deal ON payments(deal_id);
CREATE INDEX idx_payments_status ON payments(status);
CREATE INDEX idx_payments_due_date ON payments(due_date);
CREATE INDEX idx_payments_asaas ON payments(asaas_id);
