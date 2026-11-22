package api_gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class JwtAuthenticationGlobalFilter implements GlobalFilter, Ordered {

    private static final String HEADER_PLAYER_ID = "X-Player-Id";
    private static final String HEADER_ACCOUNT_ID = "X-Account-Id";
    private static final String HEADER_USERNAME = "X-Player-Username";
    private static final String HEADER_PLAYER_ROLES = "X-Player-Roles";

    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/v1/auth/register",
            "/api/v1/auth/login",
            "/api/v1/auth/refresh",
            "/actuator/health",
            "/bootstrap"
    );

    private final SecretKey secretKey;

    public JwtAuthenticationGlobalFilter(
            @Value("${farmlife.jwt.secret}") String secret
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        if (request.getMethod() == HttpMethod.OPTIONS || isPublicPath(path)) {
            return chain.filter(exchange);
        }

        String token = resolveToken(request);
        if (token == null || token.isBlank()) {
            return unauthorizedResponse(exchange.getResponse(), "Thiếu Bearer Token xác thực");
        }

        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String playerId = claims.getSubject();
            String accountId = claims.get("accountId", String.class);
            String username = claims.get("username", String.class);
            Object rolesObj = claims.get("roles");
            String roles = rolesObj != null ? rolesObj.toString() : null;

            ServerHttpRequest mutatedRequest = request.mutate()
                    .headers(httpHeaders -> {
                        httpHeaders.remove(HEADER_PLAYER_ID);
                        httpHeaders.remove(HEADER_ACCOUNT_ID);
                        httpHeaders.remove(HEADER_USERNAME);
                        httpHeaders.remove(HEADER_PLAYER_ROLES);
                        if (playerId != null) httpHeaders.add(HEADER_PLAYER_ID, playerId);
                        if (accountId != null) httpHeaders.add(HEADER_ACCOUNT_ID, accountId);
                        if (username != null) httpHeaders.add(HEADER_USERNAME, username);
                        if (roles != null) httpHeaders.add(HEADER_PLAYER_ROLES, roles);
                    })
                    .build();

            return chain.filter(exchange.mutate().request(mutatedRequest).build());
        } catch (JwtException | IllegalArgumentException ex) {
            return unauthorizedResponse(exchange.getResponse(), "Token không hợp lệ hoặc đã hết hạn");
        }
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    private String resolveToken(ServerHttpRequest request) {
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7).trim();
        }
        return request.getQueryParams().getFirst("token");
    }

    private Mono<Void> unauthorizedResponse(ServerHttpResponse response, String message) {
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = """
                {"errorCode":"UNAUTHORIZED","message":"%s"}
                """.formatted(message).trim();
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -100;
    }
}