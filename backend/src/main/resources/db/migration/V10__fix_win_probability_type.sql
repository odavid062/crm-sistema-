-- ============================================================
-- V10: Ajusta tipo de win_probability para casar com o mapeamento
-- JPA (Java double -> double precision/float8). Estava como DECIMAL
-- (numeric), causando falha de schema-validation do Hibernate.
-- ============================================================
ALTER TABLE pipeline_stages
    ALTER COLUMN win_probability TYPE double precision
    USING win_probability::double precision;
