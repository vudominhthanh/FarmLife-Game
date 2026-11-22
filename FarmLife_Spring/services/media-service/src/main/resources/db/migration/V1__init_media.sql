-- ============================================================================
-- FarmLife V6.1 · media_db
-- 7 tables: asset metadata, upload intent, CDN purge, cross-cutting
-- ============================================================================
CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE SCHEMA IF NOT EXISTS media;

CREATE TABLE media.assets (
    asset_id       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    asset_key      VARCHAR(300) UNIQUE NOT NULL,
    bucket         VARCHAR(60)  NOT NULL
                   CHECK (bucket IN
                   ('farmlife-catalog','farmlife-user','farmlife-temp')),
    sha256         CHAR(64)     NOT NULL,
    content_type   VARCHAR(80)  NOT NULL,
    size_bytes     BIGINT       NOT NULL CHECK (size_bytes > 0),
    width          INT,
    height         INT,
    asset_type     VARCHAR(20)  NOT NULL
                   CHECK (asset_type IN ('catalog','user','capture')),
    visibility     VARCHAR(20)  NOT NULL DEFAULT 'public'
                   CHECK (visibility IN ('public','private')),
    owner_service  VARCHAR(30),
    owner_ref      VARCHAR(200),
    status         VARCHAR(20)  NOT NULL DEFAULT 'pending'
                   CHECK (status IN ('pending','active','deprecated','deleted')),
    uploaded_at    TIMESTAMPTZ,
    deprecated_at  TIMESTAMPTZ,
    deleted_at     TIMESTAMPTZ,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- Dedupe scope: cùng owner + cùng sha256 = 1 row
CREATE UNIQUE INDEX uq_assets_dedupe
    ON media.assets(owner_service, owner_ref, sha256)
    WHERE status = 'active';
CREATE INDEX idx_assets_owner     ON media.assets(owner_service, owner_ref)
    WHERE status = 'active';
CREATE INDEX idx_assets_deprecate ON media.assets(deprecated_at)
    WHERE status = 'deprecated';
CREATE INDEX idx_assets_capture   ON media.assets(created_at)
    WHERE asset_type = 'capture';

CREATE TABLE media.asset_variants (
    variant_id    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    asset_id      UUID NOT NULL REFERENCES media.assets(asset_id) ON DELETE CASCADE,
    variant_name  VARCHAR(30) NOT NULL,
    format        VARCHAR(10) NOT NULL
                  CHECK (format IN ('png','webp','avif','jpg')),
    asset_key     VARCHAR(300) UNIQUE NOT NULL,
    bucket        VARCHAR(60)  NOT NULL,
    width         INT,
    height        INT,
    size_bytes    BIGINT NOT NULL CHECK (size_bytes > 0),
    sha256        CHAR(64) NOT NULL,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (asset_id, variant_name, format)
);
CREATE INDEX idx_variants_asset ON media.asset_variants(asset_id);

CREATE TABLE media.upload_intents (
    intent_id        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    player_id        UUID NOT NULL,
    purpose          VARCHAR(30) NOT NULL
                     CHECK (purpose IN
                     ('avatar','guild_banner','farm_snapshot','support_screenshot')),
    expected_type    VARCHAR(80) NOT NULL,
    max_size_bytes   BIGINT NOT NULL CHECK (max_size_bytes > 0),
    temp_object_key  VARCHAR(300) NOT NULL,
    status           VARCHAR(20) NOT NULL DEFAULT 'pending'
                     CHECK (status IN
                     ('pending','uploaded','validating','validated','rejected','expired')),
    asset_id         UUID REFERENCES media.assets(asset_id),
    rejection_reason VARCHAR(120),
    expires_at       TIMESTAMPTZ NOT NULL DEFAULT now() + INTERVAL '15 minutes',
    uploaded_at      TIMESTAMPTZ,
    validated_at     TIMESTAMPTZ,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_intents_player  ON media.upload_intents(player_id, status);
CREATE INDEX idx_intents_pending ON media.upload_intents(expires_at)
    WHERE status IN ('pending','uploaded');

CREATE TABLE media.cdn_purge_queue (
    purge_id     BIGSERIAL PRIMARY KEY,
    asset_key    VARCHAR(300) NOT NULL,
    reason       VARCHAR(30) NOT NULL
                 CHECK (reason IN ('deprecated','deleted','variant_changed')),
    status       VARCHAR(20) NOT NULL DEFAULT 'pending'
                 CHECK (status IN ('pending','done','failed')),
    attempts     SMALLINT NOT NULL DEFAULT 0,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    processed_at TIMESTAMPTZ
);
CREATE INDEX idx_purge_pending ON media.cdn_purge_queue(created_at)
    WHERE status = 'pending';

CREATE TABLE media.idempotency_keys (
    key             UUID PRIMARY KEY,
    player_id       UUID NOT NULL,
    endpoint        VARCHAR(120) NOT NULL,
    request_hash    TEXT NOT NULL,
    response_status SMALLINT,
    response_body   JSONB,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    expires_at      TIMESTAMPTZ NOT NULL DEFAULT now() + INTERVAL '24 hours'
);
CREATE INDEX idx_idem_expires ON media.idempotency_keys(expires_at);

CREATE TABLE media.outbox_events (
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
CREATE INDEX idx_outbox_pending ON media.outbox_events(created_at)
    WHERE status = 'PENDING';

CREATE TABLE media.processed_events (
    event_id        UUID NOT NULL,
    consumer_group  VARCHAR(80) NOT NULL,
    processed_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (event_id, consumer_group)
);
