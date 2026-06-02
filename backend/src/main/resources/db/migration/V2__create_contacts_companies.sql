-- ============================================================
-- V2: Companies e Contacts
-- ============================================================

CREATE TABLE companies (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    cnpj VARCHAR(18) UNIQUE,
    email VARCHAR(255),
    phone VARCHAR(20),
    website VARCHAR(255),
    industry VARCHAR(100),
    size VARCHAR(50),
    address TEXT,
    city VARCHAR(100),
    state VARCHAR(50),
    zip_code VARCHAR(10),
    country VARCHAR(50) DEFAULT 'Brasil',
    notes TEXT,
    owner_id UUID REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP
);

CREATE TABLE contacts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    phone VARCHAR(20),
    whatsapp VARCHAR(20),
    cpf VARCHAR(14) UNIQUE,
    birth_date DATE,
    position VARCHAR(100),
    department VARCHAR(100),
    status VARCHAR(30) NOT NULL DEFAULT 'LEAD',
    source VARCHAR(50),
    address TEXT,
    city VARCHAR(100),
    state VARCHAR(50),
    zip_code VARCHAR(10),
    country VARCHAR(50) DEFAULT 'Brasil',
    notes TEXT,
    avatar_url TEXT,
    company_id UUID REFERENCES companies(id) ON DELETE SET NULL,
    owner_id UUID REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP
);

-- Tags
CREATE TABLE tags (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL UNIQUE,
    color VARCHAR(7) NOT NULL DEFAULT '#6366f1',
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE contact_tags (
    contact_id UUID REFERENCES contacts(id) ON DELETE CASCADE,
    tag_id UUID REFERENCES tags(id) ON DELETE CASCADE,
    PRIMARY KEY (contact_id, tag_id)
);

CREATE TABLE company_tags (
    company_id UUID REFERENCES companies(id) ON DELETE CASCADE,
    tag_id UUID REFERENCES tags(id) ON DELETE CASCADE,
    PRIMARY KEY (company_id, tag_id)
);

CREATE INDEX idx_contacts_email ON contacts(email);
CREATE INDEX idx_contacts_phone ON contacts(phone);
CREATE INDEX idx_contacts_status ON contacts(status);
CREATE INDEX idx_contacts_company ON contacts(company_id);
CREATE INDEX idx_contacts_owner ON contacts(owner_id);
CREATE INDEX idx_companies_name ON companies(name);
CREATE INDEX idx_companies_owner ON companies(owner_id);
