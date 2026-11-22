package mapper;

import dto.*;
import entity.*;
import org.springframework.stereotype.Component;

@Component
public class EconomyMapper {

    public MarketListingResponse toListingResponse(MarketListing l) {
        if (l == null) return null;
        return new MarketListingResponse(
                l.getListingId(),
                l.getSellerId(),
                l.getItemCode(),
                l.getInitialQuantity(),
                l.getRemainingQuantity(),
                l.getUnitPrice(),
                l.getStatus() != null ? l.getStatus().name() : null,
                l.getExpiresAt(),
                l.getCreatedAt()
        );
    }

    public MarketOrderResponse toOrderResponse(MarketOrder o) {
        if (o == null) return null;
        return new MarketOrderResponse(
                o.getOrderId(),
                o.getListingId(),
                o.getBuyerId(),
                o.getQuantity(),
                o.getUnitPrice(),
                o.getTotalPrice(),
                o.getTax(),
                o.getStatus() != null ? o.getStatus().name() : null,
                o.getCreatedAt()
        );
    }

    public MarketTickerResponse toTickerResponse(MarketTicker t) {
        if (t == null) return null;
        return new MarketTickerResponse(
                t.getItemCode(),
                t.getBasePrice(),
                t.getMinPrice(),
                t.getMaxPrice(),
                t.getCurrentPrice(),
                t.getSupplyVolume24h(),
                t.getDemandVolume24h(),
                t.getVolatilityFactor(),
                t.getUpdatedAt()
        );
    }

    public PriceCandleResponse toCandleResponse(PriceCandle c) {
        if (c == null) return null;
        return new PriceCandleResponse(
                c.getItemCode(),
                c.getBucketTime(),
                c.getOpenPrice(),
                c.getHighPrice(),
                c.getLowPrice(),
                c.getClosePrice(),
                c.getVolumeTraded()
        );
    }

    public MailboxItemResponse toMailboxResponse(MailboxItem m) {
        if (m == null) return null;
        return new MailboxItemResponse(
                m.getMailItemId(),
                m.getPlayerId(),
                m.getItemCode(),
                m.getQuantity(),
                m.getQuality(),
                m.getSourceType() != null ? m.getSourceType().name() : null,
                m.getExpiresAt(),
                m.getCreatedAt()
        );
    }

    public WalletBalanceResponse toWalletResponse(Wallet w) {
        if (w == null) return null;
        return new WalletBalanceResponse(
                w.getPlayerId(),
                w.getCoins(),
                w.getGems(),
                w.getVersion()
        );
    }
}
