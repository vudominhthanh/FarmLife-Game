package service;

import dto.PlayerProfileResponse;
import dto.UpdateProfileRequest;
import entity.PlayerProfile;
import exception.BusinessException;
import exception.CommonErrorCode;
import mapper.IdentityMapper;
import repository.PlayerProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlayerProfileServiceImpl implements PlayerProfileService {

    private final PlayerProfileRepository profileRepository;
    private final IdentityMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public PlayerProfileResponse getProfile(UUID playerId) {
        PlayerProfile profile = profileRepository.findById(playerId)
                .orElseThrow(() -> new BusinessException(CommonErrorCode.PLAYER_NOT_FOUND, "Player profile not found: " + playerId));
        return mapper.toProfileResponse(profile);
    }

    @Override
    @Transactional
    public PlayerProfileResponse updateProfile(UUID playerId, UpdateProfileRequest request) {
        PlayerProfile profile = profileRepository.findById(playerId)
                .orElseThrow(() -> new BusinessException(CommonErrorCode.PLAYER_NOT_FOUND, "Player profile not found: " + playerId));

        if (request.titleCode() != null) {
            profile.setTitleCode(request.titleCode());
        }
        if (request.bio() != null) {
            profile.setBio(request.bio());
        }
        profile.setUpdatedAt(Instant.now());

        PlayerProfile saved = profileRepository.save(profile);
        return mapper.toProfileResponse(saved);
    }

    @Override
    @Transactional
    public PlayerProfileResponse addXp(UUID playerId, long xpGained) {
        if (xpGained <= 0) {
            return getProfile(playerId);
        }
        PlayerProfile profile = profileRepository.findById(playerId)
                .orElseThrow(() -> new BusinessException(CommonErrorCode.PLAYER_NOT_FOUND, "Player profile not found: " + playerId));

        long newXp = profile.getXp() + xpGained;
        profile.setXp(newXp);

        // Simple RPG leveling formula: level = 1 + floor(sqrt(xp / 100))
        int calculatedLevel = Math.max(1, (int) Math.floor(Math.sqrt(newXp / 100.0)) + 1);
        if (calculatedLevel > profile.getLevel()) {
            log.info("Player {} leveled up from {} to {}", playerId, profile.getLevel(), calculatedLevel);
            profile.setLevel(calculatedLevel);
        }
        profile.setUpdatedAt(Instant.now());

        PlayerProfile saved = profileRepository.save(profile);
        return mapper.toProfileResponse(saved);
    }
}
