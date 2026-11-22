package scheduler;

import contracts.KafkaTopics;
import contracts.MarketPriceTickEvent;
import entity.MarketTicker;
import entity.PriceCandle;
import outbox.EconomyOutboxService;
import repository.MarketTickerRepository;
import repository.PriceCandleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class QLearningPricingScheduler {

    private final MarketTickerRepository tickerRepository;
    private final PriceCandleRepository candleRepository;
    private final EconomyOutboxService outboxService;

    @Scheduled(fixedDelayString = "${farmlife.market.tick-interval-ms:900000}")
    @Transactional
    public void executeQLearningMarketTick() {
        List<MarketTicker> tickers = tickerRepository.findAll();
        if (tickers.isEmpty()) return;

        Instant now = Instant.now();
        Instant bucketTime = now.truncatedTo(ChronoUnit.MINUTES);

        for (MarketTicker ticker : tickers) {
            long oldPrice = ticker.getCurrentPrice();
            long supply = ticker.getSupplyVolume24h();
            long demand = ticker.getDemandVolume24h();

            // 1. Calculate market imbalance I = (D - S) / (D + S + 1)
            double imbalance = (double) (demand - supply) / (double) (demand + supply + 1);

            // 2. Determine price multiplier from volatility factor
            double vol = ticker.getVolatilityFactor() != null ? ticker.getVolatilityFactor().doubleValue() : 0.05;
            double priceDeltaRatio = imbalance * vol;

            long newPrice = Math.round(oldPrice * (1.0 + priceDeltaRatio));
            if (newPrice < ticker.getMinPrice()) newPrice = ticker.getMinPrice();
            if (newPrice > ticker.getMaxPrice()) newPrice = ticker.getMaxPrice();

            double percentChange = oldPrice > 0 ? ((double) (newPrice - oldPrice) / oldPrice) * 100.0 : 0.0;

            // 3. Update Q-learning state in aiState
            Map<String, Object> aiState = ticker.getAiState() != null ? new HashMap<>(ticker.getAiState()) : new HashMap<>();
            aiState.put("lastImbalance", imbalance);
            aiState.put("lastTickTime", now.toString());
            aiState.put("totalTicks", ((Number) aiState.getOrDefault("totalTicks", 0)).intValue() + 1);

            ticker.setCurrentPrice(newPrice);
            ticker.setAiState(aiState);
            ticker.setLastAiTrainedAt(now);
            tickerRepository.save(ticker);

            // 4. Save Price Candle
            PriceCandle candle = PriceCandle.builder()
                    .itemCode(ticker.getItemCode())
                    .bucketTime(bucketTime)
                    .openPrice(oldPrice)
                    .highPrice(Math.max(oldPrice, newPrice))
                    .lowPrice(Math.min(oldPrice, newPrice))
                    .closePrice(newPrice)
                    .volumeTraded(supply + demand)
                    .build();
            candleRepository.save(candle);

            // 5. Publish MarketPriceTickEvent
            if (oldPrice != newPrice) {
                UUID eventId = UUID.randomUUID();
                MarketPriceTickEvent event = new MarketPriceTickEvent(
                        eventId,
                        ticker.getItemCode(),
                        oldPrice,
                        newPrice,
                        percentChange,
                        supply,
                        demand,
                        now
                );
                outboxService.enqueue(KafkaTopics.MARKET_PRICE_TICK, "MARKET_TICK", ticker.getItemCode(), event);
                log.info("Market ticked for {}: old={}, new={} ({}%)", ticker.getItemCode(), oldPrice, newPrice, String.format("%.2f", percentChange));
            }
        }
    }
}
