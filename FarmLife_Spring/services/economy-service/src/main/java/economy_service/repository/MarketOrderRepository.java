package repository;

import entity.MarketOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MarketOrderRepository extends JpaRepository<MarketOrder, UUID> {
    List<MarketOrder> findAllByBuyerId(UUID buyerId);
    List<MarketOrder> findAllByListingId(UUID listingId);
}
