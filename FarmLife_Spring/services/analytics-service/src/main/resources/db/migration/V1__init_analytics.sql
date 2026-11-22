-- ============================================================================
-- FarmLife V6.1 · analytics_db
-- 5 tables: event log, KPI, A/B testing, dedupe
-- ============================================================================
CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE SCHEMA IF NOT EXISTS analytics;

CREATE TABLE analytics.events (
    event_id    UUID NOT NULL DEFAULT gen_random_uuid(),
    player_id   UUID,
    event_type  VARCHAR(50) NOT NULL,
    payload     JSONB NOT NULL DEFAULT '{}',
    session_id  UUID,
    occurred_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (event_id, occurred_at)
) PARTITION BY RANGE (occurred_at);
CREATE INDEX idx_events_type   ON analytics.events(event_type, occurred_at DESC);
CREATE INDEX idx_events_player ON analytics.events(player_id, occurred_at DESC)
    WHERE player_id IS NOT NULL;

-- Partition khởi tạo sẵn cho tháng 2026-11 và DEFAULT:
CREATE TABLE IF NOT EXISTS analytics.events_2026_11
    PARTITION OF analytics.events
    FOR VALUES FROM ('2026-11-01') TO ('2026-12-01');

CREATE TABLE IF NOT EXISTS analytics.events_default
    PARTITION OF analytics.events DEFAULT;

CREATE TABLE analytics.player_daily_metrics (
    player_id      UUID NOT NULL,
    day            DATE NOT NULL,
    sessions       INT NOT NULL DEFAULT 0,
    minutes_played INT NOT NULL DEFAULT 0,
    coins_earned   BIGINT NOT NULL DEFAULT 0,
    harvests       INT NOT NULL DEFAULT 0,
    trades         INT NOT NULL DEFAULT 0,
    PRIMARY KEY (player_id, day)
);
CREATE INDEX idx_metrics_day ON analytics.player_daily_metrics(day);

CREATE TABLE analytics.ab_experiments (
    experiment_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name          VARCHAR(80) UNIQUE NOT NULL,
    variants      JSONB NOT NULL,
    status        VARCHAR(20) NOT NULL DEFAULT 'draft'
                  CHECK (status IN ('draft','running','paused','ended')),
    started_at    TIMESTAMPTZ,
    ended_at      TIMESTAMPTZ
);

CREATE TABLE analytics.ab_assignments (
    experiment_id UUID NOT NULL
                  REFERENCES analytics.ab_experiments(experiment_id),
    player_id     UUID NOT NULL,
    variant       VARCHAR(30) NOT NULL,
    assigned_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (experiment_id, player_id)
);

CREATE TABLE analytics.processed_events (
    event_id        UUID NOT NULL,
    consumer_group  VARCHAR(80) NOT NULL,
    processed_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (event_id, consumer_group)
);
