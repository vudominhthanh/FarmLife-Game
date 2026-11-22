package service;

import dto.PlayerStatResponse;
import entity.PlayerStat;
import exception.BusinessException;
import exception.CommonErrorCode;
import mapper.IdentityMapper;
import repository.PlayerStatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlayerStatServiceImpl implements PlayerStatService {

    private final PlayerStatRepository statRepository;
    private final IdentityMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public PlayerStatResponse getStats(UUID playerId) {
        PlayerStat stat = statRepository.findById(playerId)
                .orElseThrow(() -> new BusinessException(CommonErrorCode.RESOURCE_NOT_FOUND, "Player stats not found: " + playerId));
        return mapper.toStatResponse(stat);
    }

    @Override
    @Transactional
    public void recordHarvest(UUID playerId, long count) {
        statRepository.incrementHarvests(playerId, count, Instant.now());
        log.debug("Incremented {} harvests for player {}", count, playerId);
    }

    @Override
    @Transactional
    public void recordTrade(UUID playerId, long trades, long coins) {
        statRepository.incrementTradesAndCoins(playerId, trades, coins, Instant.now());
        log.debug("Recorded {} trades and {} coins for player {}", trades, coins, playerId);
    }

    @Override
    @Transactional
    public void recordAnimalRaised(UUID playerId, long count) {
        statRepository.incrementAnimalsRaised(playerId, count, Instant.now());
        log.debug("Incremented {} animals raised for player {}", count, playerId);
    }

    @Override
    @Transactional
    public void recordActive(UUID playerId) {
        statRepository.updateLastActive(playerId, Instant.now());
    }
}
