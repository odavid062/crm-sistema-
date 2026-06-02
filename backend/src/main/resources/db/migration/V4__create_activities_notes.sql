-- ============================================================
-- V4: Activities (Tarefas, Ligações, Reuniões) e Notes
-- ============================================================

CREATE TABLE activities (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(255) NOT NULL,
    type VARCHAR(20) NOT NULL,
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    priority VARCHAR(10) DEFAULT 'MEDIUM',
    due_date TIMESTAMP,
    completed_at TIMESTAMP,
    duration_minutes INTEGER,
    contact_id UUID REFERENCES contacts(id) ON DELETE CASCADE,
    company_id UUID REFERENCES companies(id) ON DELETE CASCADE,
    deal_id UUID REFERENCES deals(id) ON DELETE CASCADE,
    assigned_to UUID REFERENCES users(id) ON DELETE SET NULL,
    created_by UUID REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP
);

CREATE TABLE notes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    content TEXT NOT NULL,
    contact_id UUID REFERENCES contacts(id) ON DELETE CASCADE,
    company_id UUID REFERENCES companies(id) ON DELETE CASCADE,
    deal_id UUID REFERENCES deals(id) ON DELETE CASCADE,
    created_by UUID REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP
);

CREATE TABLE notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(30) NOT NULL,
    read_at TIMESTAMP,
    reference_type VARCHAR(50),
    reference_id UUID,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_activities_contact ON activities(contact_id);
CREATE INDEX idx_activities_deal ON activities(deal_id);
CREATE INDEX idx_activities_assigned ON activities(assigned_to);
CREATE INDEX idx_activities_status ON activities(status);
CREATE INDEX idx_activities_due_date ON activities(due_date);
CREATE INDEX idx_notes_contact ON notes(contact_id);
CREATE INDEX idx_notes_deal ON notes(deal_id);
CREATE INDEX idx_notifications_user ON notifications(user_id);
CREATE INDEX idx_notifications_read ON notifications(read_at);
