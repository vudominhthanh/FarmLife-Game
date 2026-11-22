package mapper;

import dto.*;
import entity.*;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Component
public class IdentityMapper {

    public PlayerProfileResponse toProfileResponse(PlayerProfile profile) {
        if (profile == null) return null;
        String displayName = profile.getPlayer() != null ? profile.getPlayer().getDisplayName() : null;
        return new PlayerProfileResponse(
                profile.getPlayerId(),
                displayName,
                profile.getLevel(),
                profile.getXp(),
                profile.getTitleCode(),
                profile.getBio(),
                profile.getVersion(),
                profile.getUpdatedAt()
        );
    }

    public CustomizationResponse toCustomizationResponse(Customization c) {
        if (c == null) return null;
        return new CustomizationResponse(
                c.getPlayerId(),
                c.getAvatarCode(),
                c.getCustomAvatarAssetKey(),
                c.getFrameCode(),
                c.getFarmThemeCode(),
                c.getUiPalette(),
                c.getPetCompanionCode(),
                c.getVersion(),
                c.getUpdatedAt()
        );
    }

    public PlayerSettingsResponse toSettingsResponse(PlayerSetting s) {
        if (s == null) return null;
        return new PlayerSettingsResponse(
                s.getPlayerId(),
                s.getLanguage(),
                s.getMusicVolume(),
                s.getSfxVolume(),
                s.getNotifications(),
                s.getPrivacy(),
                s.getVersion(),
                s.getUpdatedAt()
        );
    }

    public PlaystyleResponse toPlaystyleResponse(Playstyle p) {
        if (p == null) return null;
        List<String> crops = p.getPreferredCropCodes() != null ? Arrays.asList(p.getPreferredCropCodes()) : Collections.emptyList();
        List<String> animals = p.getPreferredAnimalCodes() != null ? Arrays.asList(p.getPreferredAnimalCodes()) : Collections.emptyList();
        String tag = p.getPlayStyleTag() != null ? p.getPlayStyleTag().name().toLowerCase() : null;
        return new PlaystyleResponse(
                p.getPlayerId(),
                crops,
                animals,
                tag,
                p.getTags(),
                p.getAiProfiledAt(),
                p.getVersion(),
                p.getUpdatedAt()
        );
    }

    public DeviceResponse toDeviceResponse(Device d) {
        if (d == null) return null;
        String platformStr = d.getPlatform() != null ? d.getPlatform().name().toLowerCase() : null;
        UUID playerId = d.getPlayer() != null ? d.getPlayer().getPlayerId() : null;
        return new DeviceResponse(
                d.getDeviceId(),
                playerId,
                platformStr,
                d.getPushToken(),
                d.getFirstSeenAt(),
                d.getLastSeenAt()
        );
    }

    public PlayerStatResponse toStatResponse(PlayerStat s) {
        if (s == null) return null;
        return new PlayerStatResponse(
                s.getPlayerId(),
                s.getTotalHarvests(),
                s.getTotalAnimalsRaised(),
                s.getTotalCoinsEarned(),
                s.getTotalTrades(),
                s.getFriendCount(),
                s.getPlaytimeMinutes(),
                s.getLastActiveAt(),
                s.getUpdatedAt()
        );
    }
}
