-- ============================================================================
-- FarmLife V6.1 · world_db
-- 15 tables: farm, plots, animals, crafting, co-op, replay
-- ============================================================================
CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE EXTENSION IF NOT EXISTS btree_gist;
CREATE SCHEMA IF NOT EXISTS world;

-- ============ CATALOGS ============
CREATE TABLE world.crops_catalog (
    crop_code          VARCHAR(50) PRIMARY KEY,
    seed_item_code     VARCHAR(50) NOT NULL,
    harvest_item_code  VARCHAR(50) NOT NULL,
    name               VARCHAR(80) NOT NULL,
    stage_durations    INT[] NOT NULL,
    regrow_sec         INT CHECK (regrow_sec IS NULL OR regrow_sec > 0),
    min_yield          INT NOT NULL DEFAULT 1 CHECK (min_yield > 0),
    max_yield          INT NOT NULL DEFAULT 1 CHECK (max_yield >= min_yield),
    allowed_seasons    VARCHAR(20)[] DEFAULT '{spring,summer,autumn}',
    sprite_asset_keys  TEXT[],
    is_active          BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE world.animal_types_catalog (
    animal_type_code     VARCHAR(50) PRIMARY KEY,
    name                 VARCHAR(80) NOT NULL,
    mature_sec           INT NOT NULL,
    product_item_code    VARCHAR(50),
    product_interval_sec INT,
    feed_item_code       VARCHAR(50),
    feed_amount_per_day  INT DEFAULT 1,
    housing_required     BOOLEAN NOT NULL DEFAULT TRUE,
    sprite_asset_keys    TEXT[],
    is_active            BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE world.recipes_catalog (
    recipe_code       VARCHAR(50) PRIMARY KEY,
    output_item_code  VARCHAR(50) NOT NULL,
    output_qty        INT NOT NULL DEFAULT 1 CHECK (output_qty > 0),
    craft_time_sec    INT NOT NULL,
    required_level    INT NOT NULL DEFAULT 1,
    ingredients       JSONB NOT NULL,
    machine_type      VARCHAR(50),
    icon_asset_key    VARCHAR(300),
    is_active         BOOLEAN NOT NULL DEFAULT TRUE
);

-- ============ FARMS & PLOTS ============
CREATE TABLE world.farms (
    farm_id      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    player_id    UUID NOT NULL UNIQUE,
    name         VARCHAR(80) NOT NULL DEFAULT 'My Farm',
    size_level   INT NOT NULL DEFAULT 1,
    grid_width   SMALLINT NOT NULL DEFAULT 20,
    grid_height  SMALLINT NOT NULL DEFAULT 20,
    weather      VARCHAR(20) NOT NULL DEFAULT 'sunny',
    season       VARCHAR(20) NOT NULL DEFAULT 'spring',
    version      BIGINT NOT NULL DEFAULT 0,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE world.plots (
    plot_id       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    farm_id       UUID NOT NULL REFERENCES world.farms(farm_id) ON DELETE CASCADE,
    coord_x       SMALLINT NOT NULL,
    coord_y       SMALLINT NOT NULL,
    soil_quality  SMALLINT NOT NULL DEFAULT 100 CHECK (soil_quality BETWEEN 0 AND 100),
    terrain_type  VARCHAR(20) NOT NULL DEFAULT 'soil',
    is_locked     BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE (farm_id, coord_x, coord_y)
);
CREATE INDEX idx_plots_farm ON world.plots(farm_id);

CREATE TABLE world.active_plantings (
    plot_id            UUID PRIMARY KEY REFERENCES world.plots(plot_id) ON DELETE CASCADE,
    crop_code          VARCHAR(50) NOT NULL REFERENCES world.crops_catalog(crop_code),
    current_stage      SMALLINT NOT NULL DEFAULT 0,
    planted_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
    next_stage_at      TIMESTAMPTZ NOT NULL,
    ready_at           TIMESTAMPTZ NOT NULL,
    is_watered         BOOLEAN NOT NULL DEFAULT FALSE,
    last_watered_at    TIMESTAMPTZ,
    fertilizer_code    VARCHAR(50),
    harvest_count      SMALLINT NOT NULL DEFAULT 0,
    is_withered        BOOLEAN NOT NULL DEFAULT FALSE,
    reservation_id     UUID,
    status             VARCHAR(20) NOT NULL DEFAULT 'pending_items'
                       CHECK (status IN
                       ('pending_items','growing','ready','withered')),
    version            BIGINT NOT NULL DEFAULT 0
);
CREATE INDEX idx_plantings_ready ON world.active_plantings(ready_at)
    WHERE is_withered = FALSE AND status IN ('growing','ready');

-- ============ MULTI-TILE PLACED OBJECTS ============
CREATE TABLE world.placed_objects (
    object_id       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    farm_id         UUID NOT NULL REFERENCES world.farms(farm_id) ON DELETE CASCADE,
    object_code     VARCHAR(50) NOT NULL,
    object_type     VARCHAR(30) NOT NULL
                    CHECK (object_type IN
                    ('building','machine','sprinkler','decoration','fence')),
    origin_x        INT NOT NULL,
    origin_y        INT NOT NULL,
    width           SMALLINT NOT NULL DEFAULT 1 CHECK (width > 0),
    height          SMALLINT NOT NULL DEFAULT 1 CHECK (height > 0),
    direction       SMALLINT NOT NULL DEFAULT 0 CHECK (direction BETWEEN 0 AND 3),
    max_capacity    INT,
    state_metadata  JSONB NOT NULL DEFAULT '{}',
    version         BIGINT NOT NULL DEFAULT 0,
    placed_at       TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_objects_farm ON world.placed_objects(farm_id, object_type);

ALTER TABLE world.placed_objects
ADD CONSTRAINT excl_placed_objects_no_overlap
EXCLUDE USING gist (
    farm_id WITH =,
    box(
        point(origin_x, origin_y),
        point(origin_x + width - 0.01, origin_y + height - 0.01)
    ) WITH &&
) WHERE (object_type IN ('building','machine','sprinkler'));

-- ============ ANIMALS ============
CREATE TABLE world.animals (
    animal_id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    player_id               UUID NOT NULL,
    housing_object_id       UUID NOT NULL
                            REFERENCES world.placed_objects(object_id) ON DELETE CASCADE,
    animal_type_code        VARCHAR(50) NOT NULL
                            REFERENCES world.animal_types_catalog(animal_type_code),
    name                    VARCHAR(50),
    age_sec                 BIGINT NOT NULL DEFAULT 0,
    hunger                  SMALLINT NOT NULL DEFAULT 100 CHECK (hunger BETWEEN 0 AND 100),
    happiness               SMALLINT NOT NULL DEFAULT 100 CHECK (happiness BETWEEN 0 AND 100),
    stage                   VARCHAR(20) NOT NULL DEFAULT 'baby'
                            CHECK (stage IN ('baby','young','adult','elder')),
    last_fed_at             TIMESTAMPTZ,
    last_product_at         TIMESTAMPTZ,
    feeding_reservation_id  UUID,
    version                 BIGINT NOT NULL DEFAULT 0,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_animals_player  ON world.animals(player_id);
CREATE INDEX idx_animals_housing ON world.animals(housing_object_id);

CREATE TABLE world.breeding (
    breed_id   BIGSERIAL PRIMARY KEY,
    parent_a   UUID REFERENCES world.animals(animal_id) ON DELETE SET NULL,
    parent_b   UUID REFERENCES world.animals(animal_id) ON DELETE SET NULL,
    child_id   UUID REFERENCES world.animals(animal_id) ON DELETE SET NULL,
    success    BOOLEAN NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ============ CRAFT QUEUE ============
CREATE TABLE world.craft_queue (
    queue_id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    player_id          UUID NOT NULL,
    machine_object_id  UUID NOT NULL
                       REFERENCES world.placed_objects(object_id) ON DELETE CASCADE,
    recipe_code        VARCHAR(50) NOT NULL
                       REFERENCES world.recipes_catalog(recipe_code),
    quantity           INT NOT NULL DEFAULT 1 CHECK (quantity > 0),
    status             VARCHAR(20) NOT NULL DEFAULT 'pending_items'
                       CHECK (status IN
                       ('pending_items','in_progress','done','claimed','cancelled','failed')),
    reservation_id     UUID,
    started_at         TIMESTAMPTZ,
    finishes_at        TIMESTAMPTZ,
    version            BIGINT NOT NULL DEFAULT 0,
    created_at         TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_craft_machine ON world.craft_queue(machine_object_id, status);
CREATE INDEX idx_craft_player  ON world.craft_queue(player_id, status);

-- ============ CO-OP & OFFLINE REPLAY ============
CREATE TABLE world.farm_permissions (
    farm_id     UUID NOT NULL REFERENCES world.farms(farm_id) ON DELETE CASCADE,
    player_id   UUID NOT NULL,
    role        VARCHAR(20) NOT NULL DEFAULT 'visitor'
                CHECK (role IN ('co_owner','helper','visitor')),
    can_water   BOOLEAN NOT NULL DEFAULT TRUE,
    can_harvest BOOLEAN NOT NULL DEFAULT FALSE,
    can_build   BOOLEAN NOT NULL DEFAULT FALSE,
    granted_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (farm_id, player_id)
);

CREATE TABLE world.action_replay_log (
    player_id        UUID NOT NULL,
    client_seq       BIGINT NOT NULL,
    action_type      VARCHAR(50) NOT NULL,
    payload          JSONB NOT NULL,
    idempotency_key  UUID NOT NULL,
    status           VARCHAR(20) NOT NULL DEFAULT 'applied'
                     CHECK (status IN ('applied','rejected','pending')),
    rejection_reason VARCHAR(120),
    applied_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (player_id, client_seq)
);

-- ============ CROSS-CUTTING ============
CREATE TABLE world.idempotency_keys (
    key             UUID PRIMARY KEY,
    player_id       UUID NOT NULL,
    endpoint        VARCHAR(120) NOT NULL,
    request_hash    TEXT NOT NULL,
    response_status SMALLINT,
    response_body   JSONB,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    expires_at      TIMESTAMPTZ NOT NULL DEFAULT now() + INTERVAL '24 hours'
);
CREATE INDEX idx_idem_expires ON world.idempotency_keys(expires_at);

CREATE TABLE world.outbox_events (
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
CREATE INDEX idx_outbox_pending ON world.outbox_events(created_at)
    WHERE status = 'PENDING';

CREATE TABLE world.processed_events (
    event_id        UUID NOT NULL,
    consumer_group  VARCHAR(80) NOT NULL,
    processed_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (event_id, consumer_group)
);
