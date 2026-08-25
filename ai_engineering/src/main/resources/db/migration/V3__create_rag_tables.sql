-- ============================================================
-- V3 — RAG Documents and Document Chunks schema
-- ============================================================
-- Stores document metadata and chunked embeddings for RAG retrieval.
-- ============================================================

CREATE TABLE IF NOT EXISTS documents (
    id              UUID PRIMARY KEY,
    user_id         BIGINT NOT NULL REFERENCES "Users"(id) ON DELETE CASCADE,
    file_name       VARCHAR(255) NOT NULL,
    content_type    VARCHAR(100) NOT NULL,
    document_type   VARCHAR(30) NOT NULL,
    file_size       BIGINT NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    total_chunks    INT NOT NULL DEFAULT 0,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_documents_user_id ON documents(user_id);
CREATE INDEX IF NOT EXISTS idx_documents_created_at ON documents(created_at DESC);

CREATE TABLE IF NOT EXISTS document_chunks (
    id              UUID PRIMARY KEY,
    document_id     UUID NOT NULL REFERENCES documents(id) ON DELETE CASCADE,
    user_id         BIGINT NOT NULL REFERENCES "Users"(id) ON DELETE CASCADE,
    chunk_index     INT NOT NULL,
    content         TEXT NOT NULL,
    embedding       TEXT,
    source          VARCHAR(255),
    file_name       VARCHAR(255),
    page            INT,
    language        VARCHAR(50),
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_document_chunks_document_id ON document_chunks(document_id);
CREATE INDEX IF NOT EXISTS idx_document_chunks_user_id ON document_chunks(user_id);
