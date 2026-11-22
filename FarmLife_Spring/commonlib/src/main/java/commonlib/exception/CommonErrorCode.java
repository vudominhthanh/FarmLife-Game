package exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CommonErrorCode implements ErrorCode {

    // Common & System
    VALIDATION_FAILED("COMMON_VALIDATION_FAILED", "error.common.validation_failed", HttpStatus.BAD_REQUEST),
    BAD_REQUEST("COMMON_BAD_REQUEST", "error.common.bad_request", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED("COMMON_UNAUTHORIZED", "error.common.unauthorized", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("COMMON_FORBIDDEN", "error.common.forbidden", HttpStatus.FORBIDDEN),
    RESOURCE_NOT_FOUND("COMMON_NOT_FOUND", "error.common.not_found", HttpStatus.NOT_FOUND),
    CONFLICT("COMMON_CONFLICT", "error.common.conflict", HttpStatus.CONFLICT),
    MISSING_IDEMPOTENCY_KEY("COMMON_MISSING_IDEMPOTENCY_KEY", "error.common.missing_idempotency_key", HttpStatus.BAD_REQUEST),
    IDEMPOTENCY_CONFLICT("COMMON_IDEMPOTENCY_CONFLICT", "error.common.idempotency_conflict", HttpStatus.CONFLICT),
    INTERNAL_ERROR("COMMON_INTERNAL_ERROR", "error.common.internal_error", HttpStatus.INTERNAL_SERVER_ERROR),

    // Identity / Auth
    INVALID_CREDENTIALS("AUTH_INVALID_CREDENTIALS", "error.auth.invalid_credentials", HttpStatus.UNAUTHORIZED),
    ACCOUNT_LOCKED("AUTH_ACCOUNT_LOCKED", "error.auth.account_locked", HttpStatus.LOCKED),
    ACCOUNT_SUSPENDED("AUTH_ACCOUNT_SUSPENDED", "error.auth.account_suspended", HttpStatus.FORBIDDEN),
    USERNAME_ALREADY_EXISTS("AUTH_USERNAME_EXISTS", "error.auth.username_exists", HttpStatus.CONFLICT),
    EMAIL_ALREADY_EXISTS("AUTH_EMAIL_EXISTS", "error.auth.email_exists", HttpStatus.CONFLICT),
    PLAYER_NOT_FOUND("AUTH_PLAYER_NOT_FOUND", "error.auth.player_not_found", HttpStatus.NOT_FOUND),
    DEVICE_NOT_FOUND("AUTH_DEVICE_NOT_FOUND", "error.auth.device_not_found", HttpStatus.NOT_FOUND),
    DEVICE_LIMIT_EXCEEDED("AUTH_DEVICE_LIMIT", "error.auth.device_limit", HttpStatus.BAD_REQUEST),

    // World & Farming
    FARM_NOT_FOUND("WORLD_FARM_NOT_FOUND", "error.world.farm_not_found", HttpStatus.NOT_FOUND),
    PLOT_NOT_FOUND("WORLD_PLOT_NOT_FOUND", "error.world.plot_not_found", HttpStatus.NOT_FOUND),
    PLOT_OCCUPIED("WORLD_PLOT_OCCUPIED", "error.world.plot_occupied", HttpStatus.BAD_REQUEST),
    PLOT_LOCKED("WORLD_PLOT_LOCKED", "error.world.plot_locked", HttpStatus.BAD_REQUEST),
    CROP_OUT_OF_SEASON("WORLD_CROP_OUT_OF_SEASON", "error.world.crop_out_of_season", HttpStatus.BAD_REQUEST),
    CROP_NOT_READY("WORLD_CROP_NOT_READY", "error.world.crop_not_ready", HttpStatus.BAD_REQUEST),
    CROP_WITHERED("WORLD_CROP_WITHERED", "error.world.crop_withered", HttpStatus.BAD_REQUEST),
    OBJECT_OVERLAP("WORLD_OBJECT_OVERLAP", "error.world.object_overlap", HttpStatus.CONFLICT),
    ANIMAL_NOT_FOUND("WORLD_ANIMAL_NOT_FOUND", "error.world.animal_not_found", HttpStatus.NOT_FOUND),
    ANIMAL_NOT_HUNGRY("WORLD_ANIMAL_NOT_HUNGRY", "error.world.animal_not_hungry", HttpStatus.BAD_REQUEST),
    ANIMAL_NOT_READY_FOR_PRODUCT("WORLD_ANIMAL_NOT_READY", "error.world.animal_not_ready", HttpStatus.BAD_REQUEST),
    BREEDING_FAILED("WORLD_BREEDING_FAILED", "error.world.breeding_failed", HttpStatus.BAD_REQUEST),
    RECIPE_NOT_FOUND("WORLD_RECIPE_NOT_FOUND", "error.world.recipe_not_found", HttpStatus.NOT_FOUND),
    MACHINE_BUSY("WORLD_MACHINE_BUSY", "error.world.machine_busy", HttpStatus.CONFLICT),
    CLOCK_SKEW_REJECTED("WORLD_CLOCK_SKEW", "error.world.clock_skew", HttpStatus.BAD_REQUEST),
    PERMISSION_DENIED("WORLD_PERMISSION_DENIED", "error.world.permission_denied", HttpStatus.FORBIDDEN),

    // Economy & Market
    INSUFFICIENT_ITEMS("ECONOMY_INSUFFICIENT_ITEMS", "error.economy.insufficient_items", HttpStatus.BAD_REQUEST),
    INSUFFICIENT_FUNDS("ECONOMY_INSUFFICIENT_FUNDS", "error.economy.insufficient_funds", HttpStatus.BAD_REQUEST),
    INVENTORY_FULL("ECONOMY_INVENTORY_FULL", "error.economy.inventory_full", HttpStatus.BAD_REQUEST),
    ITEM_NOT_FOUND("ECONOMY_ITEM_NOT_FOUND", "error.economy.item_not_found", HttpStatus.NOT_FOUND),
    ITEM_NOT_TRADABLE("ECONOMY_ITEM_NOT_TRADABLE", "error.economy.item_not_tradable", HttpStatus.BAD_REQUEST),
    LISTING_NOT_FOUND("ECONOMY_LISTING_NOT_FOUND", "error.economy.listing_not_found", HttpStatus.NOT_FOUND),
    LISTING_EXPIRED("ECONOMY_LISTING_EXPIRED", "error.economy.listing_expired", HttpStatus.BAD_REQUEST),
    LISTING_SOLD_OUT("ECONOMY_LISTING_SOLD_OUT", "error.economy.listing_sold_out", HttpStatus.BAD_REQUEST),
    CANNOT_BUY_OWN_LISTING("ECONOMY_CANNOT_BUY_OWN", "error.economy.cannot_buy_own", HttpStatus.BAD_REQUEST),
    MAILBOX_ITEM_NOT_FOUND("ECONOMY_MAILBOX_NOT_FOUND", "error.economy.mailbox_not_found", HttpStatus.NOT_FOUND),
    RESERVATION_EXPIRED("ECONOMY_RESERVATION_EXPIRED", "error.economy.reservation_expired", HttpStatus.BAD_REQUEST),

    // Social & Quest
    QUEST_NOT_FOUND("SOCIAL_QUEST_NOT_FOUND", "error.social.quest_not_found", HttpStatus.NOT_FOUND),
    QUEST_NOT_COMPLETED("SOCIAL_QUEST_NOT_COMPLETED", "error.social.quest_not_completed", HttpStatus.BAD_REQUEST),
    QUEST_ALREADY_CLAIMED("SOCIAL_QUEST_ALREADY_CLAIMED", "error.social.quest_already_claimed", HttpStatus.BAD_REQUEST),
    FRIEND_REQUEST_EXISTS("SOCIAL_FRIEND_REQUEST_EXISTS", "error.social.friend_request_exists", HttpStatus.CONFLICT),
    FRIENDSHIP_NOT_FOUND("SOCIAL_FRIENDSHIP_NOT_FOUND", "error.social.friendship_not_found", HttpStatus.NOT_FOUND),
    CANNOT_FRIEND_SELF("SOCIAL_CANNOT_FRIEND_SELF", "error.social.cannot_friend_self", HttpStatus.BAD_REQUEST),
    GUILD_ALREADY_JOINED("SOCIAL_GUILD_ALREADY_JOINED", "error.social.guild_already_joined", HttpStatus.CONFLICT),
    GUILD_NAME_TAKEN("SOCIAL_GUILD_NAME_TAKEN", "error.social.guild_name_taken", HttpStatus.CONFLICT),
    GUILD_NOT_FOUND("SOCIAL_GUILD_NOT_FOUND", "error.social.guild_not_found", HttpStatus.NOT_FOUND),
    GUILD_FULL("SOCIAL_GUILD_FULL", "error.social.guild_full", HttpStatus.BAD_REQUEST),
    NOT_GUILD_LEADER("SOCIAL_NOT_GUILD_LEADER", "error.social.not_guild_leader", HttpStatus.FORBIDDEN),
    CHAT_RATE_LIMITED("SOCIAL_CHAT_RATE_LIMITED", "error.social.chat_rate_limited", HttpStatus.TOO_MANY_REQUESTS),
    GIFT_EXPIRED("SOCIAL_GIFT_EXPIRED", "error.social.gift_expired", HttpStatus.BAD_REQUEST),

    // Media
    UPLOAD_INTENT_NOT_FOUND("MEDIA_INTENT_NOT_FOUND", "error.media.intent_not_found", HttpStatus.NOT_FOUND),
    UPLOAD_INTENT_EXPIRED("MEDIA_INTENT_EXPIRED", "error.media.intent_expired", HttpStatus.BAD_REQUEST),
    UPLOAD_TOO_LARGE("MEDIA_UPLOAD_TOO_LARGE", "error.media.upload_too_large", HttpStatus.PAYLOAD_TOO_LARGE),
    UNSUPPORTED_MEDIA_TYPE("MEDIA_UNSUPPORTED_TYPE", "error.media.unsupported_type", HttpStatus.UNSUPPORTED_MEDIA_TYPE),
    UPLOAD_PENDING_LIMIT("MEDIA_PENDING_LIMIT", "error.media.pending_limit", HttpStatus.TOO_MANY_REQUESTS),

    // AI
    NPC_NOT_FOUND("AI_NPC_NOT_FOUND", "error.ai.npc_not_found", HttpStatus.NOT_FOUND),
    NPC_SLEEPING("AI_NPC_SLEEPING", "error.ai.npc_sleeping", HttpStatus.BAD_REQUEST),
    QUEST_GENERATION_FAILED("AI_QUEST_GEN_FAILED", "error.ai.quest_gen_failed", HttpStatus.INTERNAL_SERVER_ERROR),
    GUARDRAIL_BLOCKED("AI_GUARDRAIL_BLOCKED", "error.ai.guardrail_blocked", HttpStatus.BAD_REQUEST);

    private final String code;
    private final String messageKey;
    private final HttpStatus httpStatus;
}
