-- ============================================================================
-- FarmLife V6.1 · economy_db
-- 17 tables: inventory, wallet, market, reservation, Q-Learning pricing
-- ============================================================================
CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE SCHEMA IF NOT EXISTS economy;

-- ============ CATALOG ============
CREATE TABLE economy.items_catalog (
    item_code       VARCHAR(50) PRIMARY KEY,
    name            VARCHAR(100) NOT NULL,
    category        VARCHAR(30) NOT NULL CHECK (category IN
                    ('seed','crop','tool','food','decor','material','animal','pet')),
    stackable       BOOLEAN NOT NULL DEFAULT TRUE,
    max_stack       INT NOT NULL DEFAULT 999 CHECK (max_stack > 0),
    base_value      BIGINT NOT NULL DEFAULT 0,
    rarity          VARCHAR(20) NOT NULL DEFAULT 'common'
                    CHECK (rarity IN
                    ('common','uncommon','rare','epic','legendary')),
    metadata        JSONB NOT NULL DEFAULT '{}',
    icon_asset_key  VARCHAR(300),
    is_tradable     BOOLEAN NOT NULL DEFAULT TRUE,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE
);
CREATE INDEX idx_items_icon ON economy.items_catalog(item_code)
    WHERE icon_asset_key IS NOT NULL;

-- ============ INVENTORY ============
CREATE TABLE economy.inventories (
    inventory_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    player_id    UUID NOT NULL UNIQUE,
    capacity     INT  NOT NULL DEFAULT 100 CHECK (capacity > 0),
    version      BIGINT NOT NULL DEFAULT 0,
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE economy.inventory_items (
    slot_id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    inventory_id     UUID NOT NULL
                     REFERENCES economy.inventories(inventory_id) ON DELETE CASCADE,
    slot_index       SMALLINT NOT NULL,
    item_code        VARCHAR(50) NOT NULL
                     REFERENCES economy.items_catalog(item_code),
    quantity         INT NOT NULL CHECK (quantity > 0),
    locked_quantity  INT NOT NULL DEFAULT 0
                     CHECK (locked_quantity >= 0 AND locked_quantity <= quantity),
    quality          SMALLINT NOT NULL DEFAULT 100 CHECK (quality BETWEEN 0 AND 100),
    instance_data    JSONB NOT NULL DEFAULT '{}',
    version          BIGINT NOT NULL DEFAULT 0,
    acquired_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    expires_at       TIMESTAMPTZ,
    UNIQUE (inventory_id, slot_index)
);
CREATE INDEX idx_inv_lookup ON economy.inventory_items(inventory_id, item_code, quality);
CREATE INDEX idx_inv_locked ON economy.inventory_items(inventory_id)
    WHERE locked_quantity > 0;

CREATE TABLE economy.item_transactions (
    tx_id        BIGSERIAL PRIMARY KEY,
    inventory_id UUID NOT NULL,
    slot_id      UUID,
    item_code    VARCHAR(50) NOT NULL,
    delta        INT NOT NULL,
    reason       VARCHAR(50) NOT NULL,
    ref_id       UUID,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_item_tx ON economy.item_transactions(inventory_id, created_at DESC);

-- ============ RESERVATIONS (World → Economy item consumption) ============
CREATE TABLE economy.item_reservations (
    reservation_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    player_id      UUID NOT NULL,
    reason         VARCHAR(30) NOT NULL
                   CHECK (reason IN
                   ('planting','feeding','crafting','trade_offer','quest_cost')),
    ref_id         UUID NOT NULL,
    status         VARCHAR(20) NOT NULL DEFAULT 'pending'
                   CHECK (status IN ('pending','committed','released','expired')),
    expires_at     TIMESTAMPTZ NOT NULL DEFAULT now() + INTERVAL '5 minutes',
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    resolved_at    TIMESTAMPTZ
);
CREATE INDEX idx_resv_player  ON economy.item_reservations(player_id, status);
CREATE INDEX idx_resv_ref     ON economy.item_reservations(ref_id, status);
CREATE INDEX idx_resv_expires ON economy.item_reservations(expires_at)
    WHERE status = 'pending';

CREATE TABLE economy.item_reservation_lines (
    reservation_id UUID NOT NULL
                   REFERENCES economy.item_reservations(reservation_id) ON DELETE CASCADE,
    slot_id        UUID NOT NULL,
    item_code      VARCHAR(50) NOT NULL,
    quantity       INT NOT NULL CHECK (quantity > 0),
    PRIMARY KEY (reservation_id, slot_id)
);

-- ============ ESCROW (trade listing) ============
CREATE TABLE economy.escrow_records (
    escrow_id    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    player_id    UUID NOT NULL,
    item_code    VARCHAR(50) NOT NULL,
    quantity     INT NOT NULL CHECK (quantity > 0),
    slots        JSONB NOT NULL,
    reason       VARCHAR(30) NOT NULL
                 CHECK (reason IN ('market_listing','trade_offer','gift_pending')),
    ref_id       UUID NOT NULL,
    released_at  TIMESTAMPTZ,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_escrow_open ON economy.escrow_records(reason, ref_id)
    WHERE released_at IS NULL;

-- ============ WALLET ============
CREATE TABLE economy.wallets (
    wallet_id    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    player_id    UUID NOT NULL UNIQUE,
    coins        BIGINT NOT NULL DEFAULT 0 CHECK (coins >= 0),
    gems         BIGINT NOT NULL DEFAULT 0 CHECK (gems >= 0),
    locked_coins BIGINT NOT NULL DEFAULT 0
                 CHECK (locked_coins >= 0 AND locked_coins <= coins),
    version      BIGINT NOT NULL DEFAULT 0,
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE economy.transactions (
    tx_id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    wallet_id     UUID NOT NULL REFERENCES economy.wallets(wallet_id),
    currency      VARCHAR(10) NOT NULL CHECK (currency IN ('coin','gem')),
    amount        BIGINT NOT NULL,
    type          VARCHAR(30) NOT NULL CHECK (type IN
                  ('earn','spend','transfer_in','transfer_out','refund','tax','reward')),
    ref_type      VARCHAR(30),
    ref_id        UUID,
    balance_after BIGINT NOT NULL,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_tx_wallet ON economy.transactions(wallet_id, created_at DESC);

-- ============ MARKET ============
CREATE TABLE economy.market_listings (
    listing_id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    seller_id          UUID NOT NULL,
    item_code          VARCHAR(50) NOT NULL REFERENCES economy.items_catalog(item_code),
    quality            SMALLINT NOT NULL DEFAULT 100,
    initial_quantity   INT NOT NULL CHECK (initial_quantity > 0),
    remaining_quantity INT NOT NULL
                       CHECK (remaining_quantity >= 0 AND remaining_quantity <= initial_quantity),
    unit_price         BIGINT NOT NULL CHECK (unit_price > 0),
    status             VARCHAR(25) NOT NULL DEFAULT 'pending_escrow'
                       CHECK (status IN
                       ('pending_escrow','active','sold','cancelled','expired','escrow_failed')),
    version            BIGINT NOT NULL DEFAULT 0,
    expires_at         TIMESTAMPTZ NOT NULL,
    created_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at         TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_listings_active ON economy.market_listings(status, expires_at)
    WHERE status = 'active';
CREATE INDEX idx_listings_item   ON economy.market_listings(item_code, quality, status, unit_price);
CREATE INDEX idx_listings_seller ON economy.market_listings(seller_id, status);

CREATE TABLE economy.market_orders (
    order_id    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    listing_id  UUID NOT NULL REFERENCES economy.market_listings(listing_id),
    buyer_id    UUID NOT NULL,
    quantity    INT NOT NULL CHECK (quantity > 0),
    unit_price  BIGINT NOT NULL,
    total_price BIGINT NOT NULL,
    tax         BIGINT NOT NULL DEFAULT 0,
    status      VARCHAR(20) NOT NULL DEFAULT 'completed'
                CHECK (status IN ('pending','completed','refunded','failed')),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_orders_buyer ON economy.market_orders(buyer_id, created_at DESC);

-- ============ DYNAMIC PRICING + Q-LEARNING ============
CREATE TABLE economy.market_tickers (
    item_code           VARCHAR(50) PRIMARY KEY
                        REFERENCES economy.items_catalog(item_code),
    base_price          BIGINT NOT NULL,
    min_price           BIGINT NOT NULL,
    max_price           BIGINT NOT NULL,
    current_price       BIGINT NOT NULL,
    supply_volume_24h   BIGINT NOT NULL DEFAULT 0,
    demand_volume_24h   BIGINT NOT NULL DEFAULT 0,
    volatility_factor   NUMERIC(5,4) NOT NULL DEFAULT 0.0500,
    ai_state            JSONB NOT NULL DEFAULT '{}',
    last_ai_trained_at  TIMESTAMPTZ,
    version             BIGINT NOT NULL DEFAULT 0,
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_tickers_ai ON economy.market_tickers(last_ai_trained_at)
    WHERE last_ai_trained_at IS NOT NULL;

CREATE TABLE economy.price_candles (
    item_code     VARCHAR(50) NOT NULL
                  REFERENCES economy.market_tickers(item_code),
    bucket_time   TIMESTAMPTZ NOT NULL,
    open_price    BIGINT NOT NULL,
    high_price    BIGINT NOT NULL,
    low_price     BIGINT NOT NULL,
    close_price   BIGINT NOT NULL,
    volume_traded BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (item_code, bucket_time)
);

-- ============ MAILBOX (overflow) ============
CREATE TABLE economy.mailbox_items (
    mail_item_id  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    player_id     UUID NOT NULL,
    item_code     VARCHAR(50) NOT NULL REFERENCES economy.items_catalog(item_code),
    quantity      INT NOT NULL CHECK (quantity > 0),
    quality       SMALLINT NOT NULL DEFAULT 100,
    instance_data JSONB NOT NULL DEFAULT '{}',
    source_type   VARCHAR(30) NOT NULL
                  CHECK (source_type IN
                  ('harvest_overflow','craft_overflow','market_expired',
                   'quest_reward','system_gift')),
    ref_id        UUID,
    claimed       BOOLEAN NOT NULL DEFAULT FALSE,
    claimed_at    TIMESTAMPTZ,
    expires_at    TIMESTAMPTZ NOT NULL DEFAULT now() + INTERVAL '30 days',
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_mailbox_unclaimed ON economy.mailbox_items(player_id, created_at DESC)
    WHERE claimed = FALSE;

-- ============ CROSS-CUTTING ============
CREATE TABLE economy.idempotency_keys (
    key             UUID PRIMARY KEY,
    player_id       UUID NOT NULL,
    endpoint        VARCHAR(120) NOT NULL,
    request_hash    TEXT NOT NULL,
    response_status SMALLINT,
    response_body   JSONB,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    expires_at      TIMESTAMPTZ NOT NULL DEFAULT now() + INTERVAL '24 hours'
);
CREATE INDEX idx_idem_expires ON economy.idempotency_keys(expires_at);

CREATE TABLE economy.outbox_events (
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
CREATE INDEX idx_outbox_pending ON economy.outbox_events(created_at)
    WHERE status = 'PENDING';

CREATE TABLE economy.processed_events (
    event_id        UUID NOT NULL,
    consumer_group  VARCHAR(80) NOT NULL,
    processed_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (event_id, consumer_group)
);
