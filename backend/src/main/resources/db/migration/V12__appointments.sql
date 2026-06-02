-- ============================================================
-- V12: Agendamentos (Kanban + Calendário)
-- ============================================================
CREATE TABLE appointments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    type VARCHAR(20) NOT NULL DEFAULT 'MEETING',     -- MEETING, CALL, VISIT, DEMO, FOLLOWUP, OTHER
    status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',  -- SCHEDULED, CONFIRMED, IN_PROGRESS, DONE, CANCELLED, NO_SHOW
    start_at TIMESTAMP NOT NULL,
    end_at TIMESTAMP,
    all_day BOOLEAN NOT NULL DEFAULT FALSE,
    location TEXT,
    meeting_url TEXT,
    color VARCHAR(7) DEFAULT '#6366f1',
    contact_id UUID REFERENCES contacts(id) ON DELETE SET NULL,
    deal_id UUID REFERENCES deals(id) ON DELETE SET NULL,
    assigned_to UUID REFERENCES users(id) ON DELETE SET NULL,
    created_by UUID REFERENCES users(id) ON DELETE SET NULL,
    reminder_minutes INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP
);

CREATE INDEX idx_appointments_tenant ON appointments(tenant_id);
CREATE INDEX idx_appointments_start ON appointments(start_at);
CREATE INDEX idx_appointments_status ON appointments(status);
CREATE INDEX idx_appointments_assigned ON appointments(assigned_to);
CREATE INDEX idx_appointments_contact ON appointments(contact_id);
