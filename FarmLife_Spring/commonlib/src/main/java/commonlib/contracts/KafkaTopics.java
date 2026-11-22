package contracts;

public final class KafkaTopics {

    private KafkaTopics() {
    }

    // 1. Identity
    public static final String PLAYER_CREATED_V1 = "farmlife.identity.player-created.v1";
    public static final String PLAYER_CREATED = PLAYER_CREATED_V1;

    public static final String PLAYSTYLE_UPDATED_V1 = "farmlife.identity.playstyle-updated.v1";
    public static final String PLAYSTYLE_UPDATED = PLAYSTYLE_UPDATED_V1;

    // 2. World
    public static final String CROP_HARVESTED_V1 = "farmlife.world.crop-harvested.v1";
    public static final String CROP_HARVESTED = CROP_HARVESTED_V1;

    public static final String ITEM_RESERVE_REQUESTED_V1 = "farmlife.world.item-reserve-requested.v1";
    public static final String ITEM_RESERVE_REQUESTED = ITEM_RESERVE_REQUESTED_V1;

    // 3. Economy
    public static final String RESERVATION_RESOLVED_V1 = "farmlife.economy.reservation-resolved.v1";
    public static final String RESERVATION_RESOLVED = RESERVATION_RESOLVED_V1;

    public static final String INVENTORY_UPDATED_V1 = "farmlife.economy.inventory-updated.v1";
    public static final String INVENTORY_UPDATED = INVENTORY_UPDATED_V1;

    public static final String TRADE_COMPLETED_V1 = "farmlife.economy.trade-completed.v1";
    public static final String TRADE_COMPLETED = TRADE_COMPLETED_V1;

    public static final String MARKET_PRICE_TICK_V1 = "farmlife.economy.market-price-tick.v1";
    public static final String MARKET_PRICE_TICK = MARKET_PRICE_TICK_V1;

    // 4. Media
    public static final String ASSET_READY_V1 = "farmlife.media.asset-ready.v1";
    public static final String ASSET_READY = ASSET_READY_V1;

    // 5. AI
    public static final String QUEST_ASSIGNED_V1 = "farmlife.ai.quest-assigned.v1";
    public static final String QUEST_ASSIGNED = QUEST_ASSIGNED_V1;

    public static final String NPC_STATE_CHANGED_V1 = "farmlife.ai.npc-state-changed.v1";
    public static final String NPC_STATE_CHANGED = NPC_STATE_CHANGED_V1;

    public static final String MARKET_RUMOR_PUBLISHED_V1 = "farmlife.ai.market-rumor-published.v1";
    public static final String MARKET_RUMOR_PUBLISHED = MARKET_RUMOR_PUBLISHED_V1;
}
