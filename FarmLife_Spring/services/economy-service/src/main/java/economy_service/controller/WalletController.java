package controller;

import dto.WalletBalanceResponse;
import service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @GetMapping("/me")
    public ResponseEntity<WalletBalanceResponse> getMyWallet(@RequestHeader("X-Player-Id") UUID playerId) {
        return ResponseEntity.ok(walletService.getWallet(playerId));
    }
}
