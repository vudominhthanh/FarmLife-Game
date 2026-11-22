-- ============================================================================
-- FarmLife V6.1 · identity_db
-- 14 tables: auth, session, profile, personalization, cross-cutting
-- ============================================================================
CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE EXTENSION IF NOT EXISTS citext;
CREATE SCHEMA IF NOT EXISTS identity;

-- ============ AUTH ============
CREATE TABLE identity.accounts (
    account_id       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username         CITEXT UNIQUE NOT NULL,
    email            CITEXT UNIQUE,
    phone            VARCHAR(20) UNIQUE,
    password_hash    TEXT NOT NULL,
    status           VARCHAR(20) NOT NULL DEFAULT 'active'
                     CHECK (status IN ('active','suspended','banned','deleted')),
    email_verified   BOOLEAN NOT NULL DEFAULT FALSE,
    failed_attempts  SMALLINT NOT NULL DEFAULT 0,
    locked_until     TIMESTAMPTZ,
    anonymized_at    TIMESTAMPTZ,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE identity.players (
    player_id      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id     UUID NOT NULL UNIQUE
                   REFERENCES identity.accounts(account_id) ON DELETE CASCADE,
    display_name   VARCHAR(50) NOT NULL,
    region         VARCHAR(10) NOT NULL DEFAULT 'sea',
    language       VARCHAR(10) NOT NULL DEFAULT 'vi',
    status         VARCHAR(20) NOT NULL DEFAULT 'active'
                   CHECK (status IN ('active','suspended','banned','deleted')),
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    last_login_at  TIMESTAMPTZ
);
CREATE INDEX idx_players_region ON identity.players(region) WHERE status = 'active';

-- ============ SESSIONS & DEVICES ============
CREATE TABLE identity.sessions (
    session_id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    player_id           UUID NOT NULL
                        REFERENCES identity.players(player_id) ON DELETE CASCADE,
    access_token_hash   TEXT NOT NULL,
    refresh_token_hash  TEXT NOT NULL,
    device_id           VARCHAR(100),
    ip_address          INET,
    user_agent          TEXT,
    expires_at          TIMESTAMPTZ NOT NULL,
    revoked_at          TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_sessions_active ON identity.sessions(player_id, expires_at)
    WHERE revoked_at IS NULL;
CREATE UNIQUE INDEX idx_sessions_token ON identity.sessions(access_token_hash)
    WHERE revoked_at IS NULL;

CREATE TABLE identity.devices (
    device_id      VARCHAR(100) PRIMARY KEY,
    player_id      UUID REFERENCES identity.players(player_id) ON DELETE CASCADE,
    platform       VARCHAR(20) NOT NULL CHECK (platform IN ('ios','android','web','pc')),
    push_token     TEXT,
    first_seen_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    last_seen_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ============ ROLES ============
CREATE TABLE identity.roles (
    role_id  SMALLSERIAL PRIMARY KEY,
    name     VARCHAR(30) UNIQUE NOT NULL
);

CREATE TABLE identity.account_roles (
    account_id  UUID REFERENCES identity.accounts(account_id) ON DELETE CASCADE,
    role_id     SMALLINT REFERENCES identity.roles(role_id) ON DELETE CASCADE,
    granted_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    granted_by  UUID,
    PRIMARY KEY (account_id, role_id)
);

-- ============ PROFILE ============
CREATE TABLE identity.player_profiles (
    player_id   UUID PRIMARY KEY
                REFERENCES identity.players(player_id) ON DELETE CASCADE,
    level       INT NOT NULL DEFAULT 1 CHECK (level >= 1),
    xp          BIGINT NOT NULL DEFAULT 0 CHECK (xp >= 0),
    title_code  VARCHAR(50),
    bio         TEXT,
    version     BIGINT NOT NULL DEFAULT 0,
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE identity.customizations (
    player_id                UUID PRIMARY KEY
                             REFERENCES identity.player_profiles(player_id) ON DELETE CASCADE,
    avatar_code              VARCHAR(50),
    custom_avatar_asset_key  VARCHAR(300),
    frame_code               VARCHAR(50),
    farm_theme_code          VARCHAR(50),
    ui_palette               VARCHAR(20),
    pet_companion_code       VARCHAR(50),
    version                  BIGINT NOT NULL DEFAULT 0,
    updated_at               TIMESTAMPTZ NOT NULL DEFAULT now(),
    CHECK (avatar_code IS NULL OR custom_avatar_asset_key IS NULL)
);

CREATE TABLE identity.player_settings (
    player_id      UUID PRIMARY KEY
                   REFERENCES identity.player_profiles(player_id) ON DELETE CASCADE,
    language       VARCHAR(10) NOT NULL DEFAULT 'vi',
    music_volume   SMALLINT NOT NULL DEFAULT 80 CHECK (music_volume BETWEEN 0 AND 100),
    sfx_volume     SMALLINT NOT NULL DEFAULT 100 CHECK (sfx_volume BETWEEN 0 AND 100),
    notifications  JSONB NOT NULL DEFAULT '{"quest":true,"social":true,"market":true}',
    privacy        JSONB NOT NULL DEFAULT '{"show_online":true,"allow_gift":true}',
    version        BIGINT NOT NULL DEFAULT 0,
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- V6.1: ai_profiled_at — khi nào AI phân tích playstyle lần cuối
CREATE TABLE identity.playstyles (
    player_id              UUID PRIMARY KEY
                           REFERENCES identity.player_profiles(player_id) ON DELETE CASCADE,
    preferred_crop_codes   TEXT[] DEFAULT '{}',
    preferred_animal_codes TEXT[] DEFAULT '{}',
    play_style_tag         VARCHAR(30)
                           CHECK (play_style_tag IN
                           ('casual','hardcore','trader','social','collector')),
    tags                   JSONB NOT NULL DEFAULT '{}',
    ai_profiled_at         TIMESTAMPTZ,
    version                BIGINT NOT NULL DEFAULT 0,
    updated_at             TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_playstyles_ai ON identity.playstyles(ai_profiled_at)
    WHERE ai_profiled_at IS NOT NULL;

CREATE TABLE identity.player_stats (
    player_id            UUID PRIMARY KEY
                         REFERENCES identity.player_profiles(player_id) ON DELETE CASCADE,
    total_harvests       BIGINT NOT NULL DEFAULT 0,
    total_animals_raised BIGINT NOT NULL DEFAULT 0,
    total_coins_earned   BIGINT NOT NULL DEFAULT 0,
    total_trades         BIGINT NOT NULL DEFAULT 0,
    friend_count         INT    NOT NULL DEFAULT 0,
    playtime_minutes     BIGINT NOT NULL DEFAULT 0,
    last_active_at       TIMESTAMPTZ,
    updated_at           TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_stats_active ON identity.player_stats(last_active_at DESC);

-- ============ CROSS-CUTTING ============
CREATE TABLE identity.idempotency_keys (
    key             UUID PRIMARY KEY,
    player_id       UUID NOT NULL,
    endpoint        VARCHAR(120) NOT NULL,
    request_hash    TEXT NOT NULL,
    response_status SMALLINT,
    response_body   JSONB,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    expires_at      TIMESTAMPTZ NOT NULL DEFAULT now() + INTERVAL '24 hours'
);
CREATE INDEX idx_idem_expires ON identity.idempotency_keys(expires_at);

CREATE TABLE identity.outbox_events (
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
CREATE INDEX idx_outbox_pending ON identity.outbox_events(created_at)
    WHERE status = 'PENDING';

CREATE TABLE identity.processed_events (
    event_id        UUID NOT NULL,
    consumer_group  VARCHAR(80) NOT NULL,
    processed_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (event_id, consumer_group)
);
