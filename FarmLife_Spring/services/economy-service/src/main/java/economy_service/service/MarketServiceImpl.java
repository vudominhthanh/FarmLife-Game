package service;

import contracts.KafkaTopics;
import contracts.TradeCompletedEvent;
import dto.*;
import entity.*;
import entity.enums.*;
import exception.BusinessException;
import exception.CommonErrorCode;
import mapper.EconomyMapper;
import outbox.EconomyOutboxService;
import repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class MarketServiceImpl implements MarketService {

    private final MarketListingRepository listingRepository;
    private final MarketOrderRepository orderRepository;
    private final MarketTickerRepository tickerRepository;
    private final PriceCandleRepository candleRepository;
    private final WalletRepository walletRepository;
    private final EscrowRecordRepository escrowRepository;
    private final EconomyInventoryService inventoryService;
    private final EconomyOutboxService outboxService;
    private final EconomyMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public Page<MarketListingResponse> getActiveListings(String itemCode, Pageable pageable) {
        if (itemCode != null && !itemCode.isBlank()) {
            return listingRepository.findByItemCodeAndStatusOrderByUnitPriceAsc(itemCode, ListingStatus.ACTIVE, pageable)
                    .map(mapper::toListingResponse);
        }
        return listingRepository.findByStatusOrderByCreatedAtDesc(ListingStatus.ACTIVE, pageable)
                .map(mapper::toListingResponse);
    }

    @Override
    @Transactional
    public MarketListingResponse createListing(UUID sellerId, MarketListingRequest request) {
        // Reserve items from seller's inventory into escrow
        UUID reservationId = UUID.randomUUID();
        ReserveOutcome outcome = inventoryService.reserveItems(
                sellerId,
                List.of(new contracts.ItemQuantity(request.itemCode(), request.quantity())),
                reservationId,
                ReservationReason.TRADE_OFFER
        );

        if (!outcome.success()) {
            throw new BusinessException(CommonErrorCode.INSUFFICIENT_ITEMS, "Not enough items in inventory to list on market: " + outcome.failureReason());
        }

        Instant now = Instant.now();
        Instant expiresAt = now.plus(48, ChronoUnit.HOURS);

        MarketListing listing = MarketListing.builder()
                .sellerId(sellerId)
                .itemCode(request.itemCode())
                .quality(100)
                .initialQuantity(request.quantity())
                .remainingQuantity(request.quantity())
                .unitPrice(request.unitPrice())
                .status(ListingStatus.ACTIVE)
                .version(0L)
                .expiresAt(expiresAt)
                .createdAt(now)
                .updatedAt(now)
                .build();

        MarketListing saved = listingRepository.save(listing);

        // Record escrow
        EscrowRecord escrow = EscrowRecord.builder()
                .playerId(sellerId)
                .itemCode(request.itemCode())
                .quantity(request.quantity())
                .slots(Map.of("reservationId", reservationId.toString()))
                .reason(EscrowReason.MARKET_LISTING)
                .refId(saved.getListingId())
                .createdAt(now)
                .build();
        escrowRepository.save(escrow);

        log.info("Player {} created market listing {} for {} x {} at {} gold/ea",
                sellerId, saved.getListingId(), request.quantity(), request.itemCode(), request.unitPrice());
        return mapper.toListingResponse(saved);
    }

    @Override
    @Transactional
    public MarketOrderResponse buyListing(UUID buyerId, UUID listingId, BuyOrderRequest request) {
        MarketListing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new BusinessException(CommonErrorCode.RESOURCE_NOT_FOUND, "Listing not found: " + listingId));

        if (listing.getStatus() != ListingStatus.ACTIVE) {
            throw new BusinessException(CommonErrorCode.BAD_REQUEST, "Listing is not active: " + listing.getStatus());
        }
        if (listing.getSellerId().equals(buyerId)) {
            throw new BusinessException(CommonErrorCode.BAD_REQUEST, "Cannot purchase your own listing");
        }
        if (listing.getRemainingQuantity() < request.quantity()) {
            throw new BusinessException(CommonErrorCode.BAD_REQUEST, "Requested quantity (" + request.quantity() + ") exceeds remaining quantity (" + listing.getRemainingQuantity() + ")");
        }

        long totalPrice = (long) request.quantity() * listing.getUnitPrice();
        long tax = (long) Math.floor(totalPrice * 0.05); // 5% marketplace transaction tax
        long sellerEarnings = totalPrice - tax;

        // Buyer wallet check & debit
        Wallet buyerWallet = walletRepository.findByPlayerId(buyerId)
                .orElseThrow(() -> new BusinessException(CommonErrorCode.RESOURCE_NOT_FOUND, "Buyer wallet not found: " + buyerId));

        if (buyerWallet.getCoins() < totalPrice) {
            throw new BusinessException(CommonErrorCode.INSUFFICIENT_FUNDS, "Insufficient coins. Required: " + totalPrice + ", Available: " + buyerWallet.getCoins());
        }

        buyerWallet.setCoins(buyerWallet.getCoins() - totalPrice);
        walletRepository.save(buyerWallet);

        // Seller wallet credit
        Wallet sellerWallet = walletRepository.findByPlayerId(listing.getSellerId())
                .orElseThrow(() -> new BusinessException(CommonErrorCode.RESOURCE_NOT_FOUND, "Seller wallet not found: " + listing.getSellerId()));
        sellerWallet.setCoins(sellerWallet.getCoins() + sellerEarnings);
        walletRepository.save(sellerWallet);

        // Deliver item to buyer's inventory (or mailbox if full)
        inventoryService.addItem(buyerId, listing.getItemCode(), request.quantity(), listing.getQuality(), MailboxSourceType.HARVEST_OVERFLOW, listing.getListingId());

        // Update listing remaining quantity
        int remaining = listing.getRemainingQuantity() - request.quantity();
        listing.setRemainingQuantity(remaining);
        if (remaining == 0) {
            listing.setStatus(ListingStatus.SOLD);
        }
        listingRepository.save(listing);

        Instant now = Instant.now();
        MarketOrder order = MarketOrder.builder()
                .listingId(listingId)
                .buyerId(buyerId)
                .quantity(request.quantity())
                .unitPrice(listing.getUnitPrice())
                .totalPrice(totalPrice)
                .tax(tax)
                .status(OrderStatus.COMPLETED)
                .createdAt(now)
                .build();
        MarketOrder savedOrder = orderRepository.save(order);

        // Publish TradeCompletedEvent
        UUID eventId = UUID.randomUUID();
        TradeCompletedEvent tradeEvent = new TradeCompletedEvent(
                eventId,
                listingId,
                savedOrder.getOrderId(),
                listing.getSellerId(),
                buyerId,
                listing.getItemCode(),
                request.quantity(),
                listing.getUnitPrice(),
                totalPrice,
                tax,
                now
        );
        outboxService.enqueue(KafkaTopics.TRADE_COMPLETED, "TRADE", savedOrder.getOrderId().toString(), tradeEvent);

        log.info("Player {} bought {} x {} from listing {} for total {} coins (tax: {})",
                buyerId, request.quantity(), listing.getItemCode(), listingId, totalPrice, tax);
        return mapper.toOrderResponse(savedOrder);
    }

    @Override
    @Transactional
    public void cancelListing(UUID sellerId, UUID listingId) {
        MarketListing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new BusinessException(CommonErrorCode.RESOURCE_NOT_FOUND, "Listing not found: " + listingId));

        if (!listing.getSellerId().equals(sellerId)) {
            throw new BusinessException(CommonErrorCode.FORBIDDEN, "Not the owner of this listing");
        }
        if (listing.getStatus() != ListingStatus.ACTIVE) {
            throw new BusinessException(CommonErrorCode.BAD_REQUEST, "Listing cannot be cancelled because status is: " + listing.getStatus());
        }

        // Return remaining items to inventory or mailbox
        if (listing.getRemainingQuantity() > 0) {
            inventoryService.addItem(sellerId, listing.getItemCode(), listing.getRemainingQuantity(), listing.getQuality(), MailboxSourceType.MARKET_EXPIRED, listingId);
        }

        listing.setStatus(ListingStatus.CANCELLED);
        listingRepository.save(listing);
        log.info("Cancelled listing {} for seller {}", listingId, sellerId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MarketTickerResponse> getTickers() {
        return tickerRepository.findAll().stream()
                .map(mapper::toTickerResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PriceCandleResponse> getCandles(String itemCode, int limit) {
        return candleRepository.findByItemCodeOrderByBucketTimeDesc(itemCode, PageRequest.of(0, Math.min(limit, 100)))
                .stream()
                .map(mapper::toCandleResponse)
                .toList();
    }
}
