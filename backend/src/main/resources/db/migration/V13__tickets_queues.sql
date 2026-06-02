-- ============================================================
-- V13: Tickets + Filas (núcleo de atendimento, estilo Z-Pro)
-- ============================================================

-- Filas / Departamentos
CREATE TABLE queues (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    color VARCHAR(7) NOT NULL DEFAULT '#6366f1',
    order_index INTEGER NOT NULL DEFAULT 0,
    greeting_message TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_queues_tenant ON queues(tenant_id);

-- Motivos de encerramento
CREATE TABLE close_reasons (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    name VARCHAR(120) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_close_reasons_tenant ON close_reasons(tenant_id);

-- Sequência global de protocolo
CREATE SEQUENCE ticket_protocol_seq START 1000;

-- Tickets (unidade de atendimento)
CREATE TABLE tickets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    protocol BIGINT NOT NULL,
    subject VARCHAR(255),
    channel VARCHAR(20) NOT NULL DEFAULT 'WHATSAPP',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',   -- PENDING, OPEN, RESOLVED, CLOSED
    priority VARCHAR(10) NOT NULL DEFAULT 'MEDIUM',
    contact_id UUID REFERENCES contacts(id) ON DELETE SET NULL,
    conversation_id UUID REFERENCES whatsapp_conversations(id) ON DELETE SET NULL,
    queue_id UUID REFERENCES queues(id) ON DELETE SET NULL,
    assigned_to UUID REFERENCES users(id) ON DELETE SET NULL,
    close_reason_id UUID REFERENCES close_reasons(id) ON DELETE SET NULL,
    rating INTEGER,                 -- CSAT 1..5
    rating_comment TEXT,
    last_message_at TIMESTAMP,
    opened_at TIMESTAMP,
    closed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP
);
CREATE INDEX idx_tickets_tenant ON tickets(tenant_id);
CREATE INDEX idx_tickets_status ON tickets(status);
CREATE INDEX idx_tickets_queue ON tickets(queue_id);
CREATE INDEX idx_tickets_assigned ON tickets(assigned_to);
CREATE INDEX idx_tickets_contact ON tickets(contact_id);
CREATE INDEX idx_tickets_conversation ON tickets(conversation_id);

-- Notas internas do ticket
CREATE TABLE ticket_notes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    ticket_id UUID NOT NULL REFERENCES tickets(id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    created_by UUID REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_ticket_notes_ticket ON ticket_notes(ticket_id);

-- Tags do ticket (reaproveita a tabela tags)
CREATE TABLE ticket_tags (
    ticket_id UUID REFERENCES tickets(id) ON DELETE CASCADE,
    tag_id UUID REFERENCES tags(id) ON DELETE CASCADE,
    PRIMARY KEY (ticket_id, tag_id)
);

-- Seed: fila padrão + motivos para cada tenant existente
INSERT INTO queues (tenant_id, name, color, order_index)
SELECT id, 'Atendimento Geral', '#6366f1', 0 FROM tenants;

INSERT INTO close_reasons (tenant_id, name)
SELECT t.id, r.name FROM tenants t,
(VALUES ('Resolvido'), ('Sem resposta do cliente'), ('Não era cliente'), ('Duplicado')) AS r(name);
