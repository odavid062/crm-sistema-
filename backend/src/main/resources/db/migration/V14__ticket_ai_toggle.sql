-- ============================================================
-- V14: Toggle de IA por ticket (liga/desliga o agente n8n)
-- ============================================================
ALTER TABLE tickets ADD COLUMN ai_enabled BOOLEAN NOT NULL DEFAULT TRUE;
