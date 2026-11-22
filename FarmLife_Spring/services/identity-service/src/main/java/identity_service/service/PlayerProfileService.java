package service;

import dto.PlayerProfileResponse;
import dto.UpdateProfileRequest;

import java.util.UUID;

public interface PlayerProfileService {
    PlayerProfileResponse getProfile(UUID playerId);
    PlayerProfileResponse updateProfile(UUID playerId, UpdateProfileRequest request);
    PlayerProfileResponse addXp(UUID playerId, long xp);
}
