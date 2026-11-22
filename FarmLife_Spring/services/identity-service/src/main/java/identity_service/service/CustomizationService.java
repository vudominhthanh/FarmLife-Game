package service;

import dto.CustomizationResponse;
import dto.UpdateCustomizationRequest;

import java.util.UUID;

public interface CustomizationService {
    CustomizationResponse getCustomization(UUID playerId);
    CustomizationResponse updateCustomization(UUID playerId, UpdateCustomizationRequest request);
    void updateCustomAvatarKey(UUID playerId, String assetKey);
}
