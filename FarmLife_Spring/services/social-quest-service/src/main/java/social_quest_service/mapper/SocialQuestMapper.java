package social_quest_service.mapper;

import social_quest_service.dto.*;
import social_quest_service.entity.*;

import java.util.UUID;

public class SocialQuestMapper {

    public static QuestResponse toQuestResponse(Quest quest) {
        if (quest == null) return null;
        return new QuestResponse(
                quest.getQuestCode(),
                quest.getTitle(),
                quest.getDescription(),
                quest.getType(),
                quest.getGoalType(),
                quest.getGoalTarget(),
                quest.getRewardJson(),
                quest.getStartsAt(),
                quest.getEndsAt(),
                quest.getIsPersonalized(),
                quest.getSource(),
                quest.getSourceNpcCode(),
                quest.getPregenId(),
                quest.getIsActive()
        );
    }

    public static PlayerQuestResponse toPlayerQuestResponse(PlayerQuest pq) {
        if (pq == null) return null;
        return new PlayerQuestResponse(
                pq.getPlayerQuestId(),
                pq.getPlayerId(),
                pq.getQuestCode(),
                pq.getProgress(),
                pq.getStatus(),
                pq.getAssignedAt(),
                pq.getCompletedAt(),
                pq.getClaimedAt(),
                pq.getVersion()
        );
    }

    public static AchievementResponse toAchievementResponse(Achievement a) {
        if (a == null) return null;
        return new AchievementResponse(
                a.getAchievementCode(),
                a.getTitle(),
                a.getCriteriaJson(),
                a.getRewardJson(),
                a.getIsActive()
        );
    }

    public static PlayerAchievementResponse toPlayerAchievementResponse(PlayerAchievement pa) {
        if (pa == null) return null;
        return new PlayerAchievementResponse(
                pa.getPlayerId(),
                pa.getAchievementCode(),
                pa.getUnlockedAt()
        );
    }

    public static FriendResponse toFriendResponse(Friendship f, UUID currentUserId) {
        if (f == null) return null;
        UUID friendId = f.getPlayerAId().equals(currentUserId) ? f.getPlayerBId() : f.getPlayerAId();
        return new FriendResponse(
                friendId,
                f.getStatus(),
                f.getRequestedBy(),
                f.getCreatedAt(),
                f.getUpdatedAt()
        );
    }

    public static GuildResponse toGuildResponse(Guild g, long memberCount) {
        if (g == null) return null;
        return new GuildResponse(
                g.getGuildId(),
                g.getName(),
                g.getLeaderId(),
                g.getLevel(),
                g.getMaxMembers(),
                g.getDescription(),
                g.getBannerAssetKey(),
                g.getCreatedAt(),
                memberCount
        );
    }

    public static GuildMemberResponse toGuildMemberResponse(GuildMember gm) {
        if (gm == null) return null;
        return new GuildMemberResponse(
                gm.getGuildId(),
                gm.getPlayerId(),
                gm.getRole(),
                gm.getJoinedAt()
        );
    }

    public static ChatMessageResponse toChatMessageResponse(ChatMessage cm) {
        if (cm == null) return null;
        return new ChatMessageResponse(
                cm.getMsgId(),
                cm.getSentAt(),
                cm.getChannel(),
                cm.getSenderId(),
                cm.getContent()
        );
    }

    public static GiftResponse toGiftResponse(Gift g) {
        if (g == null) return null;
        return new GiftResponse(
                g.getGiftId(),
                g.getSenderId(),
                g.getReceiverId(),
                g.getItemCode(),
                g.getQuantity(),
                g.getMessage(),
                g.getClaimed(),
                g.getClaimedAt(),
                g.getExpiresAt(),
                g.getCreatedAt()
        );
    }
}
