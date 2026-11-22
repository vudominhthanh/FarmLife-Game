package service;

import dto.PlayerStatResponse;

import java.util.UUID;

public interface PlayerStatService {
    PlayerStatResponse getStats(UUID playerId);
    void recordHarvest(UUID playerId, long count);
    void recordTrade(UUID playerId, long trades, long coins);
    void recordAnimalRaised(UUID playerId, long count);
    void recordActive(UUID playerId);
}
