package service;

import dto.CustomizationResponse;
import dto.UpdateCustomizationRequest;
import entity.Customization;
import exception.BusinessException;
import exception.CommonErrorCode;
import mapper.IdentityMapper;
import repository.CustomizationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomizationServiceImpl implements CustomizationService {

    private final CustomizationRepository customizationRepository;
    private final IdentityMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public CustomizationResponse getCustomization(UUID playerId) {
        Customization customization = customizationRepository.findById(playerId)
                .orElseThrow(() -> new BusinessException(CommonErrorCode.RESOURCE_NOT_FOUND, "Customization not found: " + playerId));
        return mapper.toCustomizationResponse(customization);
    }

    @Override
    @Transactional
    public CustomizationResponse updateCustomization(UUID playerId, UpdateCustomizationRequest request) {
        Customization customization = customizationRepository.findById(playerId)
                .orElseThrow(() -> new BusinessException(CommonErrorCode.RESOURCE_NOT_FOUND, "Customization not found: " + playerId));

        if (request.avatarCode() != null && request.customAvatarAssetKey() != null) {
            throw new BusinessException(CommonErrorCode.BAD_REQUEST, "avatarCode and customAvatarAssetKey cannot both be set");
        }

        if (request.avatarCode() != null) {
            customization.setAvatarCode(request.avatarCode());
            customization.setCustomAvatarAssetKey(null);
        } else if (request.customAvatarAssetKey() != null) {
            customization.setCustomAvatarAssetKey(request.customAvatarAssetKey());
            customization.setAvatarCode(null);
        }

        if (request.frameCode() != null) {
            customization.setFrameCode(request.frameCode());
        }
        if (request.farmThemeCode() != null) {
            customization.setFarmThemeCode(request.farmThemeCode());
        }
        if (request.uiPalette() != null) {
            customization.setUiPalette(request.uiPalette());
        }
        if (request.petCompanionCode() != null) {
            customization.setPetCompanionCode(request.petCompanionCode());
        }
        customization.setUpdatedAt(Instant.now());

        Customization saved = customizationRepository.save(customization);
        return mapper.toCustomizationResponse(saved);
    }

    @Override
    @Transactional
    public void updateCustomAvatarKey(UUID playerId, String assetKey) {
        Customization customization = customizationRepository.findById(playerId)
                .orElseThrow(() -> new BusinessException(CommonErrorCode.RESOURCE_NOT_FOUND, "Customization not found: " + playerId));

        customization.setCustomAvatarAssetKey(assetKey);
        customization.setAvatarCode(null);
        customization.setUpdatedAt(Instant.now());
        customizationRepository.save(customization);
        log.info("Updated custom avatar asset key for player {} to {}", playerId, assetKey);
    }
}
