package service;

import dto.WalletBalanceResponse;
import entity.Wallet;
import entity.WalletTransaction;
import entity.enums.CurrencyType;
import entity.enums.WalletTransactionType;
import exception.BusinessException;
import exception.CommonErrorCode;
import mapper.EconomyMapper;
import repository.WalletRepository;
import repository.WalletTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository transactionRepository;
    private final EconomyMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public WalletBalanceResponse getWallet(UUID playerId) {
        Wallet wallet = walletRepository.findByPlayerId(playerId)
                .orElseThrow(() -> new BusinessException(CommonErrorCode.RESOURCE_NOT_FOUND, "Wallet not found: " + playerId));
        return mapper.toWalletResponse(wallet);
    }

    @Override
    @Transactional
    public void addGold(UUID playerId, long amount, String reason) {
        Wallet wallet = walletRepository.findByPlayerId(playerId)
                .orElseThrow(() -> new BusinessException(CommonErrorCode.RESOURCE_NOT_FOUND, "Wallet not found: " + playerId));

        long newBalance = wallet.getCoins() + amount;
        wallet.setCoins(newBalance);
        walletRepository.save(wallet);

        WalletTransaction tx = WalletTransaction.builder()
                .walletId(wallet.getWalletId())
                .currency(CurrencyType.COIN)
                .amount(amount)
                .type(amount >= 0 ? WalletTransactionType.EARN : WalletTransactionType.SPEND)
                .balanceAfter(newBalance)
                .refType("MANUAL_UPDATE")
                .refId(UUID.randomUUID())
                .build();
        transactionRepository.save(tx);
        log.info("Updated wallet for player {}: delta={}, newBalance={}, reason={}", playerId, amount, newBalance, reason);
    }
}
