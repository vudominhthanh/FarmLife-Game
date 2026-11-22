package repository;

import contracts.InventorySlotDelta;
import entity.enums.MailboxSourceType;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class InventoryNativeRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public record LockedSlot(UUID slotId, short slotIndex, int lockQty, int quantityAfterLock, int lockedQuantityAfter) {}

    public record PatternAResult(InventorySlotDelta slotDelta, boolean overflowToMailbox) {}

    public PatternAResult addItemPatternA(
            UUID playerId,
            UUID inventoryId,
            int capacity,
            String itemCode,
            int addQty,
            short quality,
            String reason,
            UUID refId,
            MailboxSourceType overflowSourceType
    ) {
        jdbc.getJdbcOperations().execute(
                "SELECT pg_advisory_xact_lock(hashtext('" + inventoryId + "'))"
        );

        String patternAUpdateSql = """
                WITH target AS (
                    SELECT i.slot_id
                    FROM economy.inventory_items i
                    JOIN economy.items_catalog c ON c.item_code = i.item_code
                    WHERE i.inventory_id = :invId
                      AND i.item_code = :itemCode
                      AND i.quality = :quality
                      AND i.quantity + :addQty <= c.max_stack
                    ORDER BY i.slot_index ASC
                    LIMIT 1
                    FOR UPDATE
                )
                UPDATE economy.inventory_items i
                SET quantity = i.quantity + :addQty,
                    version  = i.version + 1
                FROM target
                WHERE i.slot_id = target.slot_id
                RETURNING i.slot_id, i.slot_index, i.quantity, i.locked_quantity
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("invId", inventoryId)
                .addValue("itemCode", itemCode)
                .addValue("quality", quality)
                .addValue("addQty", addQty);

        List<InventorySlotDelta> updated = jdbc.query(patternAUpdateSql, params, (rs, rowNum) ->
                new InventorySlotDelta(
                        UUID.fromString(rs.getString("slot_id")),
                        rs.getShort("slot_index"),
                        itemCode,
                        addQty,
                        rs.getInt("quantity"),
                        rs.getInt("locked_quantity"),
                        quality
                ));

        if (!updated.isEmpty()) {
            InventorySlotDelta delta = updated.get(0);
            insertItemTransaction(inventoryId, delta.slotId(), itemCode, addQty, reason, refId);
            return new PatternAResult(delta, false);
        }

        String freeSlotSql = """
                SELECT gs.idx
                FROM generate_series(0, :maxIdx) AS gs(idx)
                LEFT JOIN economy.inventory_items i
                  ON i.inventory_id = :invId AND i.slot_index = gs.idx
                WHERE i.slot_id IS NULL
                ORDER BY gs.idx ASC
                LIMIT 1
                """;

        List<Short> freeSlots = jdbc.query(
                freeSlotSql,
                new MapSqlParameterSource().addValue("maxIdx", capacity - 1).addValue("invId", inventoryId),
                (rs, rowNum) -> rs.getShort("idx")
        );

        if (!freeSlots.isEmpty()) {
            short freeIdx = freeSlots.get(0);
            UUID newSlotId = UUID.randomUUID();
            String insertSlotSql = """
                    INSERT INTO economy.inventory_items
                        (slot_id, inventory_id, slot_index, item_code, quantity, locked_quantity, quality, instance_data, version)
                    VALUES
                        (:slotId, :invId, :slotIndex, :itemCode, :qty, 0, :quality, '{}'::jsonb, 0)
                    """;
            jdbc.update(insertSlotSql, new MapSqlParameterSource()
                    .addValue("slotId", newSlotId)
                    .addValue("invId", inventoryId)
                    .addValue("slotIndex", freeIdx)
                    .addValue("itemCode", itemCode)
                    .addValue("qty", addQty)
                    .addValue("quality", quality));

            insertItemTransaction(inventoryId, newSlotId, itemCode, addQty, reason, refId);
            InventorySlotDelta delta = new InventorySlotDelta(
                    newSlotId, freeIdx, itemCode, addQty, addQty, 0, quality
            );
            return new PatternAResult(delta, false);
        }

        String mailboxSql = """
                INSERT INTO economy.mailbox_items
                    (mail_item_id, player_id, item_code, quantity, quality, source_type, ref_id)
                VALUES
                    (:mailId, :playerId, :itemCode, :qty, :quality, :sourceType, :refId)
                """;
        jdbc.update(mailboxSql, new MapSqlParameterSource()
                .addValue("mailId", UUID.randomUUID())
                .addValue("playerId", playerId)
                .addValue("itemCode", itemCode)
                .addValue("qty", addQty)
                .addValue("quality", quality)
                .addValue("sourceType", overflowSourceType.name())
                .addValue("refId", refId));

        return new PatternAResult(null, true);
    }

    public List<LockedSlot> lockItemsPatternB(
            UUID inventoryId,
            String itemCode,
            short quality,
            int needed
    ) {
        String patternBSql = """
                WITH available AS (
                    SELECT slot_id, slot_index, quantity - locked_quantity AS avail
                    FROM economy.inventory_items
                    WHERE inventory_id = :invId
                      AND item_code = :itemCode
                      AND quality = :quality
                      AND quantity - locked_quantity > 0
                    ORDER BY slot_index ASC
                    FOR UPDATE
                ),
                running AS (
                    SELECT slot_id, avail,
                           SUM(avail) OVER (
                               ORDER BY slot_index ASC
                               ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW
                           ) AS cum,
                           SUM(avail) OVER () AS total_available
                    FROM available
                ),
                to_lock AS (
                    SELECT slot_id,
                           LEAST(avail, GREATEST(0, :needed - (cum - avail))) AS lock_qty
                    FROM running
                    WHERE total_available >= :needed
                      AND cum - avail < :needed
                )
                UPDATE economy.inventory_items i
                SET locked_quantity = i.locked_quantity + t.lock_qty,
                    version         = i.version + 1
                FROM to_lock t
                WHERE i.slot_id = t.slot_id
                  AND t.lock_qty > 0
                RETURNING i.slot_id, i.slot_index, t.lock_qty, i.quantity, i.locked_quantity
                """;

        return jdbc.query(
                patternBSql,
                new MapSqlParameterSource()
                        .addValue("invId", inventoryId)
                        .addValue("itemCode", itemCode)
                        .addValue("quality", quality)
                        .addValue("needed", needed),
                (rs, rowNum) -> new LockedSlot(
                        UUID.fromString(rs.getString("slot_id")),
                        rs.getShort("slot_index"),
                        rs.getInt("lock_qty"),
                        rs.getInt("quantity"),
                        rs.getInt("locked_quantity")
                )
        );
    }

    public void createReservationRecord(
            UUID reservationId,
            UUID playerId,
            String reason,
            UUID refId,
            String status
    ) {
        String sql = """
                INSERT INTO economy.item_reservations
                    (reservation_id, player_id, reason, ref_id, status, resolved_at)
                VALUES
                    (:resvId, :playerId, :reason, :refId, :status, CASE WHEN :status = 'COMMITTED' THEN now() ELSE NULL END)
                """;
        jdbc.update(sql, new MapSqlParameterSource()
                .addValue("resvId", reservationId)
                .addValue("playerId", playerId)
                .addValue("reason", reason)
                .addValue("refId", refId)
                .addValue("status", status));
    }

    public void insertReservationLine(UUID reservationId, UUID slotId, String itemCode, int quantity) {
        String sql = """
                INSERT INTO economy.item_reservation_lines
                    (reservation_id, slot_id, item_code, quantity)
                VALUES
                    (:resvId, :slotId, :itemCode, :qty)
                """;
        jdbc.update(sql, new MapSqlParameterSource()
                .addValue("resvId", reservationId)
                .addValue("slotId", slotId)
                .addValue("itemCode", itemCode)
                .addValue("qty", quantity));
    }

    public InventorySlotDelta consumeLockedSlot(
            UUID inventoryId,
            UUID slotId,
            short slotIndex,
            String itemCode,
            short quality,
            int consumeQty,
            String reason,
            UUID refId
    ) {
        String updateSql = """
                UPDATE economy.inventory_items
                SET quantity        = quantity - :consumeQty,
                    locked_quantity = locked_quantity - :consumeQty,
                    version         = version + 1
                WHERE slot_id = :slotId
                  AND quantity > :consumeQty
                RETURNING quantity, locked_quantity
                """;

        List<int[]> remaining = jdbc.query(
                updateSql,
                new MapSqlParameterSource().addValue("slotId", slotId).addValue("consumeQty", consumeQty),
                (rs, rowNum) -> new int[]{rs.getInt("quantity"), rs.getInt("locked_quantity")}
        );

        int newQty;
        int newLocked;
        if (!remaining.isEmpty()) {
            newQty = remaining.get(0)[0];
            newLocked = remaining.get(0)[1];
        } else {
            jdbc.update(
                    "DELETE FROM economy.inventory_items WHERE slot_id = :slotId AND quantity = :consumeQty",
                    new MapSqlParameterSource().addValue("slotId", slotId).addValue("consumeQty", consumeQty)
            );
            newQty = 0;
            newLocked = 0;
        }

        insertItemTransaction(inventoryId, slotId, itemCode, -consumeQty, reason, refId);
        return new InventorySlotDelta(slotId, slotIndex, itemCode, -consumeQty, newQty, newLocked, quality);
    }

    public long countUnclaimedMailbox(UUID playerId) {
        Long count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM economy.mailbox_items WHERE player_id = :playerId AND claimed = FALSE",
                new MapSqlParameterSource().addValue("playerId", playerId),
                Long.class
        );
        return Optional.ofNullable(count).orElse(0L);
    }

    private void insertItemTransaction(UUID inventoryId, UUID slotId, String itemCode, int delta, String reason, UUID refId) {
        String sql = """
                INSERT INTO economy.item_transactions
                    (inventory_id, slot_id, item_code, delta, reason, ref_id)
                VALUES
                    (:invId, :slotId, :itemCode, :delta, :reason, :refId)
                """;
        jdbc.update(sql, new MapSqlParameterSource()
                .addValue("invId", inventoryId)
                .addValue("slotId", slotId)
                .addValue("itemCode", itemCode)
                .addValue("delta", delta)
                .addValue("reason", reason)
                .addValue("refId", refId));
    }
}
