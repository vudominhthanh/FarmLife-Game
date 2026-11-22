package service;

import dto.WalletBalanceResponse;

import java.util.UUID;

public interface WalletService {
    WalletBalanceResponse getWallet(UUID playerId);
    void addGold(UUID playerId, long amount, String reason);
}
