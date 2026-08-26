-- ============================================================
-- V4 — Agent Executions and Steps schema
-- ============================================================
-- Stores multi-step agent plans, execution iterations, and results.
-- ============================================================

CREATE TABLE IF NOT EXISTS agent_executions (
    id              UUID PRIMARY KEY,
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    goal            TEXT NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'RUNNING',
    iterations      INT NOT NULL DEFAULT 0,
    token_usage     INT NOT NULL DEFAULT 0,
    plan_json       TEXT,
    result          TEXT,
    error           TEXT,
    started_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at    TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_agent_executions_user_id ON agent_executions(user_id);
CREATE INDEX IF NOT EXISTS idx_agent_executions_started_at ON agent_executions(started_at DESC);

CREATE TABLE IF NOT EXISTS agent_steps (
    id              UUID PRIMARY KEY,
    execution_id    UUID NOT NULL REFERENCES agent_executions(id) ON DELETE CASCADE,
    step_index      INT NOT NULL,
    step_name       VARCHAR(255) NOT NULL,
    tool_name       VARCHAR(100),
    input_args      TEXT,
    output_result   TEXT,
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    duration_ms     BIGINT,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_agent_steps_execution_id ON agent_steps(execution_id);
