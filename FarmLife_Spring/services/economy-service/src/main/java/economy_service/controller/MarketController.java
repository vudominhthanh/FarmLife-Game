package controller;

import dto.*;
import service.MarketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/market")
@RequiredArgsConstructor
public class MarketController {

    private final MarketService marketService;

    @GetMapping("/listings")
    public ResponseEntity<Page<MarketListingResponse>> getListings(
            @RequestParam(required = false) String itemCode,
            Pageable pageable
    ) {
        return ResponseEntity.ok(marketService.getActiveListings(itemCode, pageable));
    }

    @PostMapping("/listings")
    public ResponseEntity<MarketListingResponse> createListing(
            @RequestHeader("X-Player-Id") UUID playerId,
            @Valid @RequestBody MarketListingRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(marketService.createListing(playerId, request));
    }

    @PostMapping("/listings/{listingId}/buy")
    public ResponseEntity<MarketOrderResponse> buyListing(
            @RequestHeader("X-Player-Id") UUID playerId,
            @PathVariable UUID listingId,
            @Valid @RequestBody BuyOrderRequest request
    ) {
        return ResponseEntity.ok(marketService.buyListing(playerId, listingId, request));
    }

    @DeleteMapping("/listings/{listingId}")
    public ResponseEntity<Void> cancelListing(
            @RequestHeader("X-Player-Id") UUID playerId,
            @PathVariable UUID listingId
    ) {
        marketService.cancelListing(playerId, listingId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/tickers")
    public ResponseEntity<List<MarketTickerResponse>> getTickers() {
        return ResponseEntity.ok(marketService.getTickers());
    }

    @GetMapping("/candles/{itemCode}")
    public ResponseEntity<List<PriceCandleResponse>> getCandles(
            @PathVariable String itemCode,
            @RequestParam(defaultValue = "30") int limit
    ) {
        return ResponseEntity.ok(marketService.getCandles(itemCode, limit));
    }
}
