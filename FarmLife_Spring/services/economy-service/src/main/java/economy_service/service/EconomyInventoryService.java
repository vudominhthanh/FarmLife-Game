package service;

import exception.BusinessException;
import exception.CommonErrorCode;
import contracts.KafkaTopics;
import contracts.CropHarvestedEvent;
import contracts.InventorySlotDelta;
import contracts.InventoryUpdatedEvent;
import contracts.ItemQuantity;
import contracts.ItemReserveRequestedEvent;
import contracts.PlayerCreatedEvent;
import contracts.ReservationResolvedEvent;
import dto.InventoryResponse;
import dto.ReserveOutcome;
import dto.WalletResponse;
import entity.Inventory;
import entity.InventoryItem;
import entity.ItemCatalog;
import entity.Wallet;
import entity.enums.MailboxSourceType;
import outbox.EconomyOutboxService;
import repository.InventoryItemRepository;
import repository.InventoryNativeRepository;
import repository.InventoryRepository;
import repository.ItemCatalogRepository;
import repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EconomyInventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final ItemCatalogRepository itemCatalogRepository;
    private final WalletRepository walletRepository;
    private final InventoryNativeRepository nativeRepository;
    private final EconomyOutboxService outboxService;

    @Value("${farmlife.economy.starter-coins:500}")
    private long starterCoins;

    @Value("${farmlife.economy.starter-gems:10}")
    private long starterGems;

    @Value("${farmlife.economy.starter-inventory-capacity:100}")
    private int starterInventoryCapacity;

    @Transactional
    public void provisionNewPlayerEconomy(PlayerCreatedEvent event) {
        UUID playerId = event.playerId();
        Inventory inventory = inventoryRepository.findByPlayerId(playerId).orElseGet(() ->
                inventoryRepository.save(Inventory.builder()
                        .inventoryId(UUID.randomUUID())
                        .playerId(playerId)
                        .capacity(starterInventoryCapacity)
                        .build()));

        if (!walletRepository.existsByPlayerId(playerId)) {
            walletRepository.save(Wallet.builder()
                    .walletId(UUID.randomUUID())
                    .playerId(playerId)
                    .coins(starterCoins)
                    .gems(starterGems)
                    .lockedCoins(0L)
                    .build());
        }

        nativeRepository.addItemPatternA(
                playerId, inventory.getInventoryId(), inventory.getCapacity(),
                "seed_wheat", 10, (short) 100, "STARTER_PACK", playerId, MailboxSourceType.SYSTEM_GIFT
        );
        nativeRepository.addItemPatternA(
                playerId, inventory.getInventoryId(), inventory.getCapacity(),
                "seed_pumpkin", 5, (short) 100, "STARTER_PACK", playerId, MailboxSourceType.SYSTEM_GIFT
        );
        log.info("Provisioned inventory & wallet for player {}", playerId);
    }

    @Transactional(readOnly = true)
    public InventoryResponse getMyInventory(UUID playerId) {
        Inventory inv = inventoryRepository.findByPlayerId(playerId)
                .orElseThrow(() -> new BusinessException(CommonErrorCode.RESOURCE_NOT_FOUND, "Inventory not found"));

        List<InventoryItem> items = inventoryItemRepository.findByInventoryIdOrderBySlotIndexAsc(inv.getInventoryId());
        Map<String, ItemCatalog> catalogMap = itemCatalogRepository.findAllById(
                items.stream().map(InventoryItem::getItemCode).distinct().toList()
        ).stream().collect(Collectors.toMap(ItemCatalog::getItemCode, Function.identity()));

        List<InventoryResponse.SlotView> slotViews = items.stream()
                .map(i -> {
                    ItemCatalog c = catalogMap.get(i.getItemCode());
                    return new InventoryResponse.SlotView(
                            i.getSlotId(),
                            i.getSlotIndex() != null ? i.getSlotIndex().shortValue() : (short) 0,
                            i.getItemCode(),
                            c != null ? c.getName() : i.getItemCode(),
                            c != null ? c.getCategory() : null,
                            c != null ? c.getRarity() : null,
                            c != null ? c.getIconAssetKey() : null,
                            i.getQuantity() != null ? i.getQuantity() : 0,
                            i.getLockedQuantity() != null ? i.getLockedQuantity() : 0,
                            c != null ? c.getMaxStack() : 999,
                            i.getQuality() != null ? i.getQuality().shortValue() : (short) 100
                    );
                })
                .toList();

        long mailboxCount = nativeRepository.countUnclaimedMailbox(playerId);
        return new InventoryResponse(inv.getInventoryId(), playerId, inv.getCapacity(), mailboxCount, slotViews);
    }

    public InventoryResponse getInventory(UUID playerId) {
        return getMyInventory(playerId);
    }

    @Transactional
    public void addItem(UUID playerId, String itemCode, int quantity, int quality, MailboxSourceType sourceType, UUID refId) {
        Inventory inv = inventoryRepository.findByPlayerId(playerId)
                .orElseThrow(() -> new BusinessException(CommonErrorCode.RESOURCE_NOT_FOUND, "Inventory not found"));

        InventoryNativeRepository.PatternAResult result = nativeRepository.addItemPatternA(
                playerId,
                inv.getInventoryId(),
                inv.getCapacity(),
                itemCode,
                quantity,
                (short) quality,
                sourceType != null ? sourceType.name() : "ADD_ITEM",
                refId,
                sourceType != null ? sourceType : MailboxSourceType.HARVEST_OVERFLOW
        );

        List<InventorySlotDelta> deltas = result.slotDelta() != null
                ? List.of(result.slotDelta())
                : List.of();

        outboxService.enqueue(
                KafkaTopics.INVENTORY_UPDATED,
                "INVENTORY",
                playerId.toString(),
                new InventoryUpdatedEvent(
                        playerId,
                        inv.getInventoryId(),
                        sourceType != null ? sourceType.name() : "ADD_ITEM",
                        refId,
                        deltas,
                        result.overflowToMailbox()
                )
        );
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ReserveOutcome reserveItems(UUID playerId, List<ItemQuantity> items, UUID reservationId, entity.enums.ReservationReason reason) {
        ItemReserveRequestedEvent event = new ItemReserveRequestedEvent(
                UUID.randomUUID(),
                reservationId,
                playerId,
                reason != null ? reason.name() : "RESERVE",
                reservationId,
                items,
                Instant.now()
        );
        return reserveAndCommitItems(event);
    }

    @Transactional(readOnly = true)
    public WalletResponse getMyWallet(UUID playerId) {
        Wallet wallet = walletRepository.findByPlayerId(playerId)
                .orElseThrow(() -> new BusinessException(CommonErrorCode.RESOURCE_NOT_FOUND, "Wallet not found"));

        return new WalletResponse(
                wallet.getWalletId(),
                wallet.getPlayerId(),
                wallet.getCoins(),
                wallet.getGems(),
                wallet.getLockedCoins(),
                wallet.getUpdatedAt()
        );
    }

    /**
     * Pattern B lock + commit inside REQUIRES_NEW.
     * On insufficient items, marks only this inner transaction rollback-only and returns ReserveOutcome.failed(...)
     * so the outer consumer transaction can safely enqueue ReservationResolvedEvent(success=false) and mark processed.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ReserveOutcome reserveAndCommitItems(ItemReserveRequestedEvent event) {
        Inventory inv = inventoryRepository.findByPlayerId(event.playerId()).orElse(null);
        if (inv == null) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ReserveOutcome.failed("INVENTORY_NOT_FOUND");
        }

        nativeRepository.createReservationRecord(
                event.reservationId(),
                event.playerId(),
                event.reason(),
                event.refId(),
                "COMMITTED"
        );

        List<InventorySlotDelta> deltas = new ArrayList<>();
        for (ItemQuantity reqItem : event.items()) {
            List<InventoryNativeRepository.LockedSlot> lockedSlots = nativeRepository.lockItemsPatternB(
                    inv.getInventoryId(),
                    reqItem.itemCode(),
                    reqItem.quality(),
                    reqItem.quantity()
            );
            if (lockedSlots.isEmpty()) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return ReserveOutcome.failed("INSUFFICIENT_ITEMS:" + reqItem.itemCode());
            }

            for (InventoryNativeRepository.LockedSlot locked : lockedSlots) {
                nativeRepository.insertReservationLine(
                        event.reservationId(),
                        locked.slotId(),
                        reqItem.itemCode(),
                        locked.lockQty()
                );
                InventorySlotDelta delta = nativeRepository.consumeLockedSlot(
                        inv.getInventoryId(),
                        locked.slotId(),
                        locked.slotIndex(),
                        reqItem.itemCode(),
                        reqItem.quality(),
                        locked.lockQty(),
                        event.reason(),
                        event.refId()
                );
                deltas.add(delta);
            }
        }

        outboxService.enqueue(
                KafkaTopics.RESERVATION_RESOLVED,
                "RESERVATION",
                event.playerId().toString(),
                new ReservationResolvedEvent(
                        event.reservationId(),
                        event.playerId(),
                        event.reason(),
                        event.refId(),
                        true,
                        null,
                        event.items()
                )
        );
        outboxService.enqueue(
                KafkaTopics.INVENTORY_UPDATED,
                "INVENTORY",
                event.playerId().toString(),
                new InventoryUpdatedEvent(
                        event.playerId(),
                        inv.getInventoryId(),
                        event.reason(),
                        event.refId(),
                        deltas,
                        false
                )
        );
        return ReserveOutcome.ok(deltas);
    }

    @Transactional
    public void handleCropHarvested(CropHarvestedEvent event) {
        Inventory inv = inventoryRepository.findByPlayerId(event.playerId())
                .orElseThrow(() -> new BusinessException(CommonErrorCode.RESOURCE_NOT_FOUND, "Inventory not found"));

        InventoryNativeRepository.PatternAResult result = nativeRepository.addItemPatternA(
                event.playerId(),
                inv.getInventoryId(),
                inv.getCapacity(),
                event.harvestItemCode(),
                event.quantity(),
                event.quality(),
                "CROP_HARVEST",
                event.plotId(),
                MailboxSourceType.HARVEST_OVERFLOW
        );

        List<InventorySlotDelta> deltas = result.slotDelta() != null
                ? List.of(result.slotDelta())
                : List.of();

        outboxService.enqueue(
                KafkaTopics.INVENTORY_UPDATED,
                "INVENTORY",
                event.playerId().toString(),
                new InventoryUpdatedEvent(
                        event.playerId(),
                        inv.getInventoryId(),
                        "CROP_HARVEST",
                        event.plotId(),
                        deltas,
                        result.overflowToMailbox()
                )
        );
    }
}
