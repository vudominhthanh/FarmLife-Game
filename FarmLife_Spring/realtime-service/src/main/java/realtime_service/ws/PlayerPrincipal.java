package realtime_service.ws;

import java.security.Principal;

public record PlayerPrincipal(String playerId) implements Principal {
    @Override
    public String getName() {
        return playerId;
    }
}
