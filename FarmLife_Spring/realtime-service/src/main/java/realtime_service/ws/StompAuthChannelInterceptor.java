package realtime_service.ws;

import security.ApiHeaders;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private final SecretKey signingKey;

    public StompAuthChannelInterceptor(@Value("${farmlife.jwt.secret}") String jwtSecret) {
        this.signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null || accessor.getCommand() != StompCommand.CONNECT) {
            return message;
        }

        String playerId = accessor.getFirstNativeHeader(ApiHeaders.PLAYER_ID);
        if (playerId == null || playerId.isBlank()) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                playerId = extractPlayerIdFromJwt(authHeader.substring(7).trim());
            }
        }

        if (playerId == null || playerId.isBlank()) {
            throw new MessagingException("Unauthorized STOMP CONNECT: missing valid JWT or X-Player-Id");
        }

        accessor.setUser(new PlayerPrincipal(playerId));
        log.debug("Authenticated STOMP session {} for player {}", accessor.getSessionId(), playerId);
        return message;
    }

    private String extractPlayerIdFromJwt(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            String playerId = claims.get("playerId", String.class);
            return (playerId != null && !playerId.isBlank()) ? playerId : claims.getSubject();
        } catch (Exception ex) {
            log.warn("Invalid JWT on STOMP CONNECT: {}", ex.getMessage());
            return null;
        }
    }
}
