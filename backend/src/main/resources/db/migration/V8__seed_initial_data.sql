-- ============================================================
-- V8: Dados iniciais
-- ============================================================

-- Admin padrão (senha: Admin@123)
INSERT INTO users (name, email, password, role)
VALUES (
    'Administrador',
    'admin@crm.com',
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
    'ADMIN'
) ON CONFLICT (email) DO NOTHING;

-- Tags padrão
INSERT INTO tags (name, color) VALUES
    ('VIP', '#f59e0b'),
    ('Prioritário', '#ef4444'),
    ('Novo Lead', '#10b981'),
    ('Em Negociação', '#6366f1'),
    ('Cliente Fiel', '#8b5cf6'),
    ('Sem Interesse', '#6b7280')
ON CONFLICT (name) DO NOTHING;

-- Pipeline padrão
INSERT INTO pipelines (name, description)
VALUES ('Vendas', 'Pipeline padrão de vendas')
ON CONFLICT DO NOTHING;

-- Etapas do pipeline
WITH p AS (SELECT id FROM pipelines WHERE name = 'Vendas' LIMIT 1)
INSERT INTO pipeline_stages (pipeline_id, name, color, position, win_probability)
SELECT p.id, etapa.name, etapa.color, etapa.pos, etapa.prob FROM p,
(VALUES
    ('Prospecção', '#6366f1', 0, 10),
    ('Qualificação', '#8b5cf6', 1, 25),
    ('Proposta', '#f59e0b', 2, 50),
    ('Negociação', '#f97316', 3, 75),
    ('Fechamento', '#10b981', 4, 90)
) AS etapa(name, color, pos, prob)
ON CONFLICT DO NOTHING;
