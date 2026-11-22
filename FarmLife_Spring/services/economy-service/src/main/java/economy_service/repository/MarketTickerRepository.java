package repository;

import entity.MarketTicker;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MarketTickerRepository extends JpaRepository<MarketTicker, String> {}
