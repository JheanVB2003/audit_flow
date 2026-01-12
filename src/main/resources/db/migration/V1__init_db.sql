-- Extensões necessárias
CREATE EXTENSION IF NOT EXISTS vector;
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- =====================
-- USERS
-- =====================
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL
    );

-- =====================
-- RULES
-- =====================
CREATE TABLE IF NOT EXISTS rules (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    risk_level VARCHAR(50),
    active BOOLEAN DEFAULT TRUE,
    created_by BIGINT REFERENCES users(id)
    );

-- =====================
-- DOCUMENTS
-- =====================
CREATE TABLE IF NOT EXISTS documents (
    id BIGSERIAL PRIMARY KEY,
    filename VARCHAR(255) NOT NULL,
    upload_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(50),
    uploaded_by BIGINT REFERENCES users(id)
    );

-- =====================
-- DOCUMENT CHUNKS (IA)
-- =====================
CREATE TABLE IF NOT EXISTS document_chunks (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    document_id BIGINT REFERENCES documents(id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    embedding VECTOR(768)
    );

-- Índice para busca vetorial (cosine similarity)
CREATE INDEX IF NOT EXISTS idx_document_chunks_embedding
    ON document_chunks
    USING ivfflat (embedding vector_cosine_ops);

-- =====================
-- AUDIT RESULTS
-- =====================
CREATE TABLE IF NOT EXISTS audit_results (
    id BIGSERIAL PRIMARY KEY,
    document_id BIGINT REFERENCES documents(id),
    rule_id BIGINT REFERENCES rules(id),
    is_compliant BOOLEAN,
    ai_reasoning TEXT,
    relevant_text_snippet TEXT
    );
