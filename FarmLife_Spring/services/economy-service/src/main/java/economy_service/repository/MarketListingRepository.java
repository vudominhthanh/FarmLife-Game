package repository;

import entity.MarketListing;
import entity.enums.ListingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MarketListingRepository extends JpaRepository<MarketListing, UUID> {
    Page<MarketListing> findByStatusOrderByCreatedAtDesc(ListingStatus status, Pageable pageable);
    Page<MarketListing> findByItemCodeAndStatusOrderByUnitPriceAsc(String itemCode, ListingStatus status, Pageable pageable);
    List<MarketListing> findAllBySellerId(UUID sellerId);
}
