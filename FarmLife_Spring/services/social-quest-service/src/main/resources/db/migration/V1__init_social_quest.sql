-- ============================================================================
-- FarmLife V6.1 · social_quest_db
-- 12 tables: quest, achievement, friend, guild, chat, gift
-- ============================================================================
CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE SCHEMA IF NOT EXISTS social_quest;

-- ============ QUESTS (V6.1: AI-generated tracking) ============
CREATE TABLE social_quest.quests (
    quest_code       VARCHAR(50) PRIMARY KEY,
    title            VARCHAR(120) NOT NULL,
    description      TEXT,
    type             VARCHAR(20) NOT NULL
                     CHECK (type IN
                     ('daily','weekly','story','event','personalized')),
    goal_type        VARCHAR(30) NOT NULL,
    goal_target      INT NOT NULL CHECK (goal_target > 0),
    reward_json      JSONB NOT NULL,
    starts_at        TIMESTAMPTZ,
    ends_at          TIMESTAMPTZ,
    is_personalized  BOOLEAN NOT NULL DEFAULT FALSE,
    source           VARCHAR(20) NOT NULL DEFAULT 'static'
                     CHECK (source IN ('static','ai_generated','event')),
    source_npc_code  VARCHAR(50),
    pregen_id        UUID,
    is_active        BOOLEAN NOT NULL DEFAULT TRUE
);
CREATE INDEX idx_quests_source ON social_quest.quests(source, is_active)
    WHERE is_active = TRUE;

CREATE TABLE social_quest.player_quests (
    player_quest_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    player_id       UUID NOT NULL,
    quest_code      VARCHAR(50) NOT NULL
                    REFERENCES social_quest.quests(quest_code),
    progress        INT NOT NULL DEFAULT 0 CHECK (progress >= 0),
    status          VARCHAR(20) NOT NULL DEFAULT 'active'
                    CHECK (status IN ('active','completed','claimed','expired')),
    assigned_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    completed_at    TIMESTAMPTZ,
    claimed_at      TIMESTAMPTZ,
    version         BIGINT NOT NULL DEFAULT 0,
    UNIQUE (player_id, quest_code, assigned_at)
);
CREATE INDEX idx_pq_active ON social_quest.player_quests(player_id, status)
    WHERE status = 'active';

-- ============ ACHIEVEMENTS ============
CREATE TABLE social_quest.achievements (
    achievement_code VARCHAR(50) PRIMARY KEY,
    title            VARCHAR(120) NOT NULL,
    criteria_json    JSONB NOT NULL,
    reward_json      JSONB NOT NULL,
    is_active        BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE social_quest.player_achievements (
    player_id        UUID NOT NULL,
    achievement_code VARCHAR(50) NOT NULL
                     REFERENCES social_quest.achievements(achievement_code),
    unlocked_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (player_id, achievement_code)
);

-- ============ FRIENDSHIPS (canonical order) ============
CREATE TABLE social_quest.friendships (
    player_a_id  UUID NOT NULL,
    player_b_id  UUID NOT NULL,
    status       VARCHAR(20) NOT NULL DEFAULT 'pending'
                 CHECK (status IN ('pending','accepted','blocked')),
    requested_by UUID NOT NULL,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (player_a_id, player_b_id),
    CHECK (player_a_id < player_b_id)
);
CREATE INDEX idx_friends_b ON social_quest.friendships(player_b_id, status);

-- ============ GUILDS ============
CREATE TABLE social_quest.guilds (
    guild_id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name              VARCHAR(80) UNIQUE NOT NULL,
    leader_id         UUID NOT NULL,
    level             INT NOT NULL DEFAULT 1,
    max_members       SMALLINT NOT NULL DEFAULT 50,
    description       TEXT,
    banner_asset_key  VARCHAR(300),
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE social_quest.guild_members (
    guild_id  UUID NOT NULL
              REFERENCES social_quest.guilds(guild_id) ON DELETE CASCADE,
    player_id UUID NOT NULL,
    role      VARCHAR(20) NOT NULL DEFAULT 'member'
              CHECK (role IN ('leader','officer','member')),
    joined_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (guild_id, player_id)
);
CREATE UNIQUE INDEX idx_guild_member_unique
    ON social_quest.guild_members(player_id);

-- ============ CHAT (partitioned by month) ============
CREATE TABLE social_quest.chat_messages (
    msg_id     BIGSERIAL,
    channel    VARCHAR(50) NOT NULL,
    sender_id  UUID NOT NULL,
    content    TEXT NOT NULL,
    sent_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (msg_id, sent_at)
) PARTITION BY RANGE (sent_at);
CREATE INDEX idx_chat_channel ON social_quest.chat_messages(channel, sent_at DESC);

-- Partition khởi tạo sẵn cho tháng 2026-11 và DEFAULT:
CREATE TABLE IF NOT EXISTS social_quest.chat_messages_2026_11
    PARTITION OF social_quest.chat_messages
    FOR VALUES FROM ('2026-11-01') TO ('2026-12-01');

CREATE TABLE IF NOT EXISTS social_quest.chat_messages_default
    PARTITION OF social_quest.chat_messages DEFAULT;

-- ============ GIFTS ============
CREATE TABLE social_quest.gifts (
    gift_id     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sender_id   UUID NOT NULL,
    receiver_id UUID NOT NULL,
    item_code   VARCHAR(50),
    quantity    INT NOT NULL DEFAULT 1 CHECK (quantity > 0),
    message     TEXT,
    claimed     BOOLEAN NOT NULL DEFAULT FALSE,
    claimed_at  TIMESTAMPTZ,
    expires_at  TIMESTAMPTZ,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_gifts_receiver ON social_quest.gifts(receiver_id, claimed)
    WHERE claimed = FALSE;

-- ============ CROSS-CUTTING ============
CREATE TABLE social_quest.idempotency_keys (
    key             UUID PRIMARY KEY,
    player_id       UUID NOT NULL,
    endpoint        VARCHAR(120) NOT NULL,
    request_hash    TEXT NOT NULL,
    response_status SMALLINT,
    response_body   JSONB,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    expires_at      TIMESTAMPTZ NOT NULL DEFAULT now() + INTERVAL '24 hours'
);
CREATE INDEX idx_idem_expires ON social_quest.idempotency_keys(expires_at);

CREATE TABLE social_quest.outbox_events (
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
CREATE INDEX idx_outbox_pending ON social_quest.outbox_events(created_at)
    WHERE status = 'PENDING';

CREATE TABLE social_quest.processed_events (
    event_id        UUID NOT NULL,
    consumer_group  VARCHAR(80) NOT NULL,
    processed_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (event_id, consumer_group)
);
