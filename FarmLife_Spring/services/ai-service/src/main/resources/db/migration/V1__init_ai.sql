-- ============================================================================
-- FarmLife V6.1 · ai_db  (UPPERCASE enum values)
-- Requires: pgvector extension
-- ============================================================================
CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE EXTENSION IF NOT EXISTS vector;
CREATE SCHEMA IF NOT EXISTS ai;

-- ============ 1. RAG KNOWLEDGE CHUNKS ============
CREATE TABLE ai.knowledge_chunks (
    chunk_id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    doc_category       VARCHAR(40) NOT NULL
                       CHECK (doc_category IN
                       ('CROP_GUIDE','ANIMAL_CARE','CRAFTING_RECIPE',
                        'MARKET_RULES','WORLD_LORE','NPC_BIO','QUEST_GUIDE')),
    ref_code           VARCHAR(50),
    chunk_index        SMALLINT NOT NULL DEFAULT 0,
    title              VARCHAR(150) NOT NULL,
    content            TEXT NOT NULL,
    metadata           JSONB NOT NULL DEFAULT '{}',
    content_hash       CHAR(64) NOT NULL,
    token_count        INT NOT NULL DEFAULT 0,
    embedding          vector(768) NOT NULL,
    last_embedded_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (doc_category, ref_code, chunk_index)
);
CREATE INDEX idx_knowledge_hnsw ON ai.knowledge_chunks
    USING hnsw (embedding vector_cosine_ops)
    WITH (m = 16, ef_construction = 64);
CREATE INDEX idx_knowledge_cat  ON ai.knowledge_chunks(doc_category, ref_code);
CREATE INDEX idx_knowledge_hash ON ai.knowledge_chunks(content_hash);

-- ============ 2. NPC AGENTS ============
CREATE TABLE ai.npc_agents (
    npc_code             VARCHAR(50) PRIMARY KEY,
    display_name         VARCHAR(80) NOT NULL,
    role                 VARCHAR(40) NOT NULL
                         CHECK (role IN
                         ('MERCHANT','QUEST_GIVER','GUIDE','ARTISAN','MAYOR')),
    persona_prompt       TEXT NOT NULL,
    favorite_items       TEXT[] DEFAULT '{}',
    disliked_items       TEXT[] DEFAULT '{}',
    llm_model            VARCHAR(50) NOT NULL DEFAULT 'qwen2.5:7b',
    fsm_state            VARCHAR(30) NOT NULL DEFAULT 'IDLE'
                         CHECK (fsm_state IN
                         ('IDLE','WALKING','WORKING','TRADING','FESTIVAL','SLEEPING')),
    current_map_code     VARCHAR(50) NOT NULL DEFAULT 'town_square',
    coord_x              SMALLINT NOT NULL DEFAULT 0,
    coord_y              SMALLINT NOT NULL DEFAULT 0,
    schedule_json        JSONB NOT NULL DEFAULT '{}',
    q_table_state        JSONB NOT NULL DEFAULT '{}',
    version              BIGINT NOT NULL DEFAULT 0,
    is_active            BOOLEAN NOT NULL DEFAULT TRUE,
    last_state_change_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at           TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_npc_active ON ai.npc_agents(is_active, fsm_state)
    WHERE is_active = TRUE;

-- ============ 3. NPC EPISODIC MEMORY ============
CREATE TABLE ai.npc_player_memories (
    memory_id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    npc_code           VARCHAR(50) NOT NULL
                       REFERENCES ai.npc_agents(npc_code) ON DELETE CASCADE,
    player_id          UUID NOT NULL,
    affection_score    INT NOT NULL DEFAULT 0
                       CHECK (affection_score BETWEEN -100 AND 1000),
    relationship       VARCHAR(30) NOT NULL DEFAULT 'STRANGER'
                       CHECK (relationship IN
                       ('STRANGER','ACQUAINTANCE','FRIEND','CLOSE_FRIEND','RIVAL')),
    memory_summary     TEXT NOT NULL DEFAULT 'Mới chuyển đến nông trại, chưa có tương tác đáng chú ý.',
    summary_updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    embedding          vector(768),
    version            BIGINT NOT NULL DEFAULT 0,
    last_interacted    TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (npc_code, player_id)
);
CREATE INDEX idx_npc_mem_player ON ai.npc_player_memories(player_id, npc_code);
CREATE INDEX idx_npc_mem_hnsw ON ai.npc_player_memories
    USING hnsw (embedding vector_cosine_ops)
    WITH (m = 16, ef_construction = 64)
    WHERE embedding IS NOT NULL;

-- ============ 4. QUEST GENERATION RULES ============
CREATE TABLE ai.quest_generation_rules (
    rule_id           SERIAL PRIMARY KEY,
    rule_name         VARCHAR(80) NOT NULL,
    priority          INT NOT NULL DEFAULT 100,
    min_player_lv     INT NOT NULL,
    max_player_lv     INT NOT NULL,
    play_style_tags   TEXT[] DEFAULT '{}',
    allowed_goals     TEXT[] NOT NULL,
    min_target_qty    INT NOT NULL CHECK (min_target_qty > 0),
    max_target_qty    INT NOT NULL CHECK (max_target_qty >= min_target_qty),
    max_coin_reward   BIGINT NOT NULL CHECK (max_coin_reward > 0),
    max_xp_reward     BIGINT NOT NULL CHECK (max_xp_reward > 0),
    allowed_items     TEXT[] NOT NULL,
    is_active         BOOLEAN NOT NULL DEFAULT TRUE,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_rules_lv ON ai.quest_generation_rules(min_player_lv, max_player_lv)
    WHERE is_active = TRUE;

-- ============ 5. PREGENERATED QUESTS ============
CREATE TABLE ai.pregenerated_quests (
    pregen_id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    player_id                UUID NOT NULL,
    npc_code                 VARCHAR(50) NOT NULL
                             REFERENCES ai.npc_agents(npc_code) ON DELETE CASCADE,
    rule_id                  INT NOT NULL
                             REFERENCES ai.quest_generation_rules(rule_id),
    source_event_id          UUID,
    quest_payload            JSONB NOT NULL,
    dialogue_text            TEXT NOT NULL,
    status                   VARCHAR(20) NOT NULL DEFAULT 'READY'
                             CHECK (status IN ('READY','ASSIGNED','EXPIRED')),
    assigned_player_quest_id UUID,
    expires_at               TIMESTAMPTZ NOT NULL DEFAULT now() + INTERVAL '24 hours',
    assigned_at              TIMESTAMPTZ,
    created_at               TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_pregen_lookup ON ai.pregenerated_quests(player_id, npc_code, status)
    WHERE status = 'READY';
CREATE INDEX idx_pregen_expires ON ai.pregenerated_quests(expires_at)
    WHERE status = 'READY';

-- ============ 6. LLM CALL LOGS (partitioned) ============
CREATE TABLE ai.llm_call_logs (
    call_id            UUID NOT NULL DEFAULT gen_random_uuid(),
    player_id          UUID,
    service_context    VARCHAR(30) NOT NULL
                       CHECK (service_context IN ('AI','ECONOMY','SOCIAL_QUEST')),
    purpose            VARCHAR(30) NOT NULL
                       CHECK (purpose IN
                       ('QUEST_GEN','NPC_DIALOGUE','RAG_GUIDE','EMBEDDING',
                        'MODERATION','INTENT_CLASSIFY')),
    model              VARCHAR(50) NOT NULL,
    prompt_tokens      INT NOT NULL DEFAULT 0,
    completion_tokens  INT NOT NULL DEFAULT 0,
    total_tokens       INT NOT NULL DEFAULT 0,
    latency_ms         INT NOT NULL,
    status             VARCHAR(20) NOT NULL
                       CHECK (status IN
                       ('SUCCESS','FAILED','TIMEOUT','RATE_LIMITED','GUARDRAIL_BLOCKED')),
    error_message      TEXT,
    cost_usd           NUMERIC(10,6) NOT NULL DEFAULT 0,
    occurred_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (call_id, occurred_at)
) PARTITION BY RANGE (occurred_at);
CREATE INDEX idx_llm_player  ON ai.llm_call_logs(player_id, occurred_at DESC)
    WHERE player_id IS NOT NULL;
CREATE INDEX idx_llm_purpose ON ai.llm_call_logs(purpose, occurred_at DESC);
CREATE INDEX idx_llm_status  ON ai.llm_call_logs(status) WHERE status <> 'SUCCESS';

-- Partition khởi tạo sẵn cho tháng 2026-11 và DEFAULT:
CREATE TABLE IF NOT EXISTS ai.llm_call_logs_2026_11
    PARTITION OF ai.llm_call_logs
    FOR VALUES FROM ('2026-11-01') TO ('2026-12-01');

CREATE TABLE IF NOT EXISTS ai.llm_call_logs_default
    PARTITION OF ai.llm_call_logs DEFAULT;

-- ============ 7, 8, 9. CROSS-CUTTING ============
CREATE TABLE ai.idempotency_keys (
    key             UUID PRIMARY KEY,
    player_id       UUID NOT NULL,
    endpoint        VARCHAR(120) NOT NULL,
    request_hash    TEXT NOT NULL,
    response_status SMALLINT,
    response_body   JSONB,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    expires_at      TIMESTAMPTZ NOT NULL DEFAULT now() + INTERVAL '24 hours'
);
CREATE INDEX idx_idem_expires ON ai.idempotency_keys(expires_at);

CREATE TABLE ai.outbox_events (
    event_id        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    aggregate_type  VARCHAR(50) NOT NULL,
    aggregate_id    VARCHAR(100) NOT NULL,
    event_type      VARCHAR(80) NOT NULL,
    payload         JSONB NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING'
                    CHECK (status IN ('PENDING','PUBLISHED','FAILED','DEAD_LETTER')),
    retry_count     SMALLINT NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    published_at    TIMESTAMPTZ
);
CREATE INDEX idx_outbox_pending ON ai.outbox_events(created_at)
    WHERE status = 'PENDING';

CREATE TABLE ai.processed_events (
    event_id        UUID NOT NULL,
    consumer_group  VARCHAR(80) NOT NULL,
    processed_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (event_id, consumer_group)
);