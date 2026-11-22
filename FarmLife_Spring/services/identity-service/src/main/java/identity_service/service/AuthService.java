package service;

import config.JwtService;
import dto.AuthResponse;
import dto.LoginRequest;
import dto.RegisterRequest;
import entity.*;
import entity.enums.*;
import exception.BusinessException;
import repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.net.InetAddress;
import java.time.OffsetDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    public static final String TOPIC_PLAYER_CREATED = "farmlife.identity.player-created.v1";
    private static final Set<String> VALID_PLATFORMS = Set.of("ios", "android", "web", "pc");

    private final AccountRepository accountRepository;
    private final PlayerRepository playerRepository;
    private final PlayerProfileRepository playerProfileRepository;
    private final CustomizationRepository customizationRepository;
    private final PlaystyleRepository playstyleRepository;
    private final PlayerSettingRepository playerSettingRepository;
    private final PlayerStatRepository playerStatRepository;
    private final DeviceRepository deviceRepository;
    private final SessionRepository sessionRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AccountLockoutService accountLockoutService;

    @Transactional
    public AuthResponse register(RegisterRequest request, String clientIp, String userAgent) {
        String normalizedUsername = request.username().trim();
        String normalizedEmail = (request.email() != null && !request.email().isBlank())
                ? request.email().trim() : null;

        if (accountRepository.existsByUsername(normalizedUsername)) {
            throw new BusinessException(IdentityErrorCode.USERNAME_TAKEN);
        }
        if (normalizedEmail != null && accountRepository.existsByEmail(normalizedEmail)) {
            throw new BusinessException(IdentityErrorCode.EMAIL_TAKEN);
        }

        // 1. Lưu identity.accounts
        Account account = Account.builder()
                .username(normalizedUsername)
                .email(normalizedEmail)
                .passwordHash(passwordEncoder.encode(request.password()))
                .status(AccountStatus.ACTIVE)
                .emailVerified(false)
                .build();
        account = accountRepository.save(account);

        // 2. Lưu identity.players
        String region = (request.region() != null && !request.region().isBlank())
                ? request.region().trim().toLowerCase() : "sea";
        String language = (request.language() != null && !request.language().isBlank())
                ? request.language().trim().toLowerCase() : "vi";

        Player player = Player.builder()
                .account(account)
                .displayName(request.displayName().trim())
                .region(region)
                .language(language)
                .status(PlayerStatus.ACTIVE)
                .lastLoginAt(OffsetDateTime.now().toInstant())
                .build();
        player = playerRepository.save(player);

        UUID playerId = player.getPlayerId();

        // 3. Lưu identity.player_profiles
        PlayerProfile profile = PlayerProfile.builder()
                .playerId(playerId)
                .level(1)
                .xp(0L)
                .titleCode("newbie_farmer")
                .bio("Nông dân mới tại FarmLife!")
                .build();
        playerProfileRepository.save(profile);

        // 4. Lưu identity.customizations
        Customization customization = Customization.builder()
                .playerId(playerId)
                .avatarCode("avatar_default_01")
                .frameCode("frame_wood_basic")
                .farmThemeCode("theme_spring_meadow")
                .uiPalette("cozy_warm")
                .build();
        customizationRepository.save(customization);

        // 5. Lưu identity.playstyles
        Playstyle playstyle = Playstyle.builder()
                .playerId(playerId)
                .playStyleTag(PlayerStyleTag.CASUAL)
                .build();
        playstyleRepository.save(playstyle);

        playerSettingRepository.save(PlayerSetting.builder()
                .playerId(playerId)
                .language(language)
                .build());
        playerStatRepository.save(PlayerStat.builder()
                .playerId(playerId)
                .build());

        Map<String, Object> eventPayload = new HashMap<>();
        eventPayload.put("playerId", playerId.toString());
        eventPayload.put("accountId", account.getAccountId().toString());
        eventPayload.put("username", account.getUsername());
        eventPayload.put("displayName", player.getDisplayName());
        eventPayload.put("region", player.getRegion());
        eventPayload.put("language", player.getLanguage());
        eventPayload.put("playStyleTag", playstyle.getPlayStyleTag());
        eventPayload.put("createdAt", player.getCreatedAt().toString());

        OutboxEvent outboxEvent = OutboxEvent.builder()
                .aggregateType("PLAYER")
                .aggregateId(playerId.toString())
                .eventType(TOPIC_PLAYER_CREATED)
                .payload(objectMapper.valueToTree(eventPayload))
                .status(OutboxStatus.PENDING)
                .build();
        outboxEventRepository.save(outboxEvent);

        log.info("Đã khởi tạo người chơi mới playerId={} (username={}) và ghi Outbox {}",
                playerId, account.getUsername(), TOPIC_PLAYER_CREATED);

        // 7. Tạo phiên đăng nhập (Session & Device) và trả JWT ngay sau khi đăng ký
        return createSessionAndBuildResponse(
                account, player, profile, customization, playstyle,
                request.deviceId(), request.platform(), clientIp, userAgent
        );
    }

    @Transactional
    public AuthResponse login(LoginRequest request, String clientIp, String userAgent) {
        String identifier = request.usernameOrEmail().trim();

        Account account = accountRepository.findByUsernameOrEmail(identifier, identifier)
                .orElseThrow(() -> new BusinessException(
                        IdentityErrorCode.INVALID_CREDENTIALS
                ));

        // Kiểm tra trạng thái tài khoản
        if (!"active".equalsIgnoreCase(String.valueOf(account.getStatus()))) {
            throw new BusinessException(
                    IdentityErrorCode.ACCOUNT_INACTIVE,
                    account.getStatus()
            );
        }

        // Kiểm tra khoá tạm thời do nhập sai nhiều lần
        if (account.getLockedUntil() != null && account.getLockedUntil().isAfter(OffsetDateTime.now().toInstant())) {
            throw new BusinessException(
                    IdentityErrorCode.ACCOUNT_LOCKED,
                    account.getLockedUntil().toString()
            );
        }

        // Xác thực mật khẩu BCrypt
        if (!passwordEncoder.matches(request.password(), account.getPasswordHash())) {
            accountLockoutService.recordFailedLogin(account);
            accountLockoutService.recordFailedLogin(account);
            throw new BusinessException(IdentityErrorCode.INVALID_CREDENTIALS);
        }

        // Reset bộ đếm lỗi khi đăng nhập đúng
        if (account.getFailedAttempts() != 0 || account.getLockedUntil() != null) {
            account.setFailedAttempts((short) 0);
            account.setLockedUntil(null);
            accountRepository.save(account);
        }

        Player player = playerRepository.findByAccountId(account.getAccountId())
                .orElseThrow(() -> new BusinessException(
                        IdentityErrorCode.PLAYER_NOT_FOUND
                ));

        player.setLastLoginAt(OffsetDateTime.now().toInstant());
        playerRepository.save(player);

        UUID playerId = player.getPlayerId();
        PlayerProfile profile = playerProfileRepository.findById(playerId)
                .orElseGet(() -> playerProfileRepository.save(PlayerProfile.builder().playerId(playerId).build()));
        Customization customization = customizationRepository.findById(playerId)
                .orElseGet(() -> customizationRepository.save(Customization.builder().playerId(playerId).build()));
        Playstyle playstyle = playstyleRepository.findById(playerId)
                .orElseGet(() -> playstyleRepository.save(Playstyle.builder().playerId(playerId).build()));

        return createSessionAndBuildResponse(
                account, player, profile, customization, playstyle,
                request.deviceId(), request.platform(), clientIp, userAgent
        );
    }

    private AuthResponse createSessionAndBuildResponse(
            Account account,
            Player player,
            PlayerProfile profile,
            Customization customization,
            Playstyle playstyle,
            String rawDeviceId,
            String rawPlatform,
            String clientIp,
            String userAgent
    ) {
        String platform = (rawPlatform != null && VALID_PLATFORMS.contains(rawPlatform.toLowerCase()))
                ? rawPlatform.toLowerCase() : "pc";
        String deviceId = (rawDeviceId != null && !rawDeviceId.isBlank()) ? rawDeviceId.trim() : null;

        if (deviceId != null) {

            Platform devicePlatform = Platform.valueOf(
                    platform.toUpperCase(Locale.ROOT)
            );

            Device device = deviceRepository.findById(deviceId)
                    .orElseGet(() -> Device.builder()
                            .deviceId(deviceId)
                            .player(player)
                            .platform(devicePlatform)
                            .build());
            device.setPlayer(player);
            device.setPlatform(devicePlatform);
            deviceRepository.save(device);
        }

        String accessToken = jwtService.generateAccessToken(account, player);
        String refreshToken = jwtService.generateRefreshToken(player);
        OffsetDateTime expiresAt = jwtService.calculateAccessTokenExpiry();

        InetAddress inetAddress = null;
        if (clientIp != null && !clientIp.isBlank()) {
            try {
                inetAddress = InetAddress.getByName(clientIp.split(",")[0].trim());
            } catch (Exception ignored) {
            }
        }

        Session session = Session.builder()
                .player(player)
                .accessTokenHash(jwtService.sha256Hex(accessToken))
                .refreshTokenHash(jwtService.sha256Hex(refreshToken))
                .deviceId(deviceId)
                .ipAddress(inetAddress)
                .userAgent(userAgent)
                .expiresAt(expiresAt.toInstant())
                .build();
        sessionRepository.save(session);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresAt(expiresAt)
                .player(AuthResponse.PlayerSummary.builder()
                        .playerId(player.getPlayerId())
                        .accountId(account.getAccountId())
                        .username(account.getUsername())
                        .displayName(player.getDisplayName())
                        .region(player.getRegion())
                        .language(player.getLanguage())
                        .level(profile.getLevel())
                        .xp(profile.getXp())
                        .avatarCode(customization.getAvatarCode())
                        .playStyleTag(playstyle.getPlayStyleTag())
                        .build())
                .build();
    }
}