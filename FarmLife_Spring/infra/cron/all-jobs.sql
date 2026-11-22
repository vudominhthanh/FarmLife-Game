-- ============ [60 giây] Release reservation hết hạn ============
WITH expired_resv AS (
    UPDATE economy.item_reservations
    SET status = 'expired', resolved_at = now()
    WHERE status = 'pending' AND expires_at < now()
    RETURNING reservation_id
),
released_totals AS (
    SELECT rl.slot_id, SUM(rl.quantity) AS total_unlock_qty
    FROM economy.item_reservation_lines rl
    JOIN expired_resv er ON er.reservation_id = rl.reservation_id
    GROUP BY rl.slot_id
)
UPDATE economy.inventory_items i
SET locked_quantity = GREATEST(0, i.locked_quantity - rt.total_unlock_qty),
    version = i.version + 1
FROM released_totals rt
WHERE i.slot_id = rt.slot_id;

-- ============ [5 phút] Market listing expired → trả escrow → mailbox ============
WITH expired_listings AS (
    UPDATE economy.market_listings
    SET status = 'expired', updated_at = now(), version = version + 1
    WHERE status = 'active' AND expires_at < now()
    RETURNING listing_id, seller_id, item_code, quality, remaining_quantity
),
close_escrow AS (
    UPDATE economy.escrow_records e
    SET released_at = now()
    FROM expired_listings el
    WHERE e.ref_id = el.listing_id
      AND e.reason = 'market_listing'
      AND e.released_at IS NULL
)
INSERT INTO economy.mailbox_items
    (player_id, item_code, quantity, quality, source_type, ref_id)
SELECT seller_id, item_code, remaining_quantity, quality, 'market_expired', listing_id
FROM expired_listings
WHERE remaining_quantity > 0;

-- ============ [1 giờ] Cây khô (dùng COALESCE để bẫy NULL) ============
UPDATE world.active_plantings
SET is_withered = TRUE, status = 'withered', version = version + 1
WHERE is_withered = FALSE
  AND status IN ('growing','ready')
  AND COALESCE(last_watered_at, planted_at) < now() - INTERVAL '3 days';

-- ============ [6 giờ] Dọn dẹp ============
DELETE FROM economy.mailbox_items
WHERE claimed = FALSE AND expires_at < now();

UPDATE identity.outbox_events     SET status = 'DEAD_LETTER' WHERE status = 'FAILED' AND retry_count >= 5;
UPDATE world.outbox_events        SET status = 'DEAD_LETTER' WHERE status = 'FAILED' AND retry_count >= 5;
UPDATE economy.outbox_events      SET status = 'DEAD_LETTER' WHERE status = 'FAILED' AND retry_count >= 5;
UPDATE social_quest.outbox_events SET status = 'DEAD_LETTER' WHERE status = 'FAILED' AND retry_count >= 5;
UPDATE media.outbox_events        SET status = 'DEAD_LETTER' WHERE status = 'FAILED' AND retry_count >= 5;
UPDATE ai.outbox_events           SET status = 'DEAD_LETTER' WHERE status = 'FAILED' AND retry_count >= 5;

UPDATE media.assets
SET status = 'deleted', deleted_at = now()
WHERE status = 'deprecated' AND deprecated_at < now() - INTERVAL '30 days';

UPDATE media.upload_intents
SET status = 'expired'
WHERE status IN ('pending','uploaded') AND expires_at < now();

UPDATE media.assets
SET status = 'deprecated', deprecated_at = now()
WHERE asset_type = 'capture' AND status = 'active'
  AND created_at < now() - INTERVAL '7 days';

UPDATE ai.pregenerated_quests
SET status = 'expired'
WHERE status = 'ready' AND expires_at < now();

DELETE FROM identity.idempotency_keys     WHERE expires_at < now();
DELETE FROM world.idempotency_keys        WHERE expires_at < now();
DELETE FROM economy.idempotency_keys      WHERE expires_at < now();
DELETE FROM social_quest.idempotency_keys WHERE expires_at < now();
DELETE FROM media.idempotency_keys        WHERE expires_at < now();
DELETE FROM ai.idempotency_keys           WHERE expires_at < now();
