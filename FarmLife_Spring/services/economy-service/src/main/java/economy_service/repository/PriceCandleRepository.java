package repository;

import entity.PriceCandle;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PriceCandleRepository extends JpaRepository<PriceCandle, PriceCandle.PriceCandleId> {
    List<PriceCandle> findByItemCodeOrderByBucketTimeDesc(String itemCode, Pageable pageable);
}
