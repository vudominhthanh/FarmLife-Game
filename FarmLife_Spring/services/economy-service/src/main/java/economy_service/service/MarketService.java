package service;

import dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface MarketService {
    Page<MarketListingResponse> getActiveListings(String itemCode, Pageable pageable);
    MarketListingResponse createListing(UUID sellerId, MarketListingRequest request);
    MarketOrderResponse buyListing(UUID buyerId, UUID listingId, BuyOrderRequest request);
    void cancelListing(UUID sellerId, UUID listingId);
    List<MarketTickerResponse> getTickers();
    List<PriceCandleResponse> getCandles(String itemCode, int limit);
}
