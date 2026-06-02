-- ============================================================
-- V5: WhatsApp (UazAPI) - Conversas e Mensagens
-- ============================================================

CREATE TABLE whatsapp_conversations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    remote_jid VARCHAR(50) NOT NULL,
    contact_name VARCHAR(255),
    contact_id UUID REFERENCES contacts(id) ON DELETE SET NULL,
    assigned_to UUID REFERENCES users(id) ON DELETE SET NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    last_message_at TIMESTAMP,
    unread_count INTEGER DEFAULT 0,
    instance_name VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    UNIQUE(remote_jid, instance_name)
);

CREATE TABLE whatsapp_messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    conversation_id UUID NOT NULL REFERENCES whatsapp_conversations(id) ON DELETE CASCADE,
    message_id VARCHAR(255),
    content TEXT,
    media_url TEXT,
    media_type VARCHAR(50),
    media_caption TEXT,
    direction VARCHAR(10) NOT NULL,
    status VARCHAR(20) DEFAULT 'SENT',
    sender_id UUID REFERENCES users(id) ON DELETE SET NULL,
    timestamp TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE whatsapp_quick_replies (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    shortcut VARCHAR(50) NOT NULL UNIQUE,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    created_by UUID REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_conversations_jid ON whatsapp_conversations(remote_jid);
CREATE INDEX idx_conversations_contact ON whatsapp_conversations(contact_id);
CREATE INDEX idx_conversations_assigned ON whatsapp_conversations(assigned_to);
CREATE INDEX idx_conversations_status ON whatsapp_conversations(status);
CREATE INDEX idx_messages_conversation ON whatsapp_messages(conversation_id);
CREATE INDEX idx_messages_timestamp ON whatsapp_messages(timestamp);
