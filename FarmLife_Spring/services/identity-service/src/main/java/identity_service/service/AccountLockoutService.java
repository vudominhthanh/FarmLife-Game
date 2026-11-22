package service;

import entity.Account;
import repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class AccountLockoutService {

    private final AccountRepository accountRepository;

    @Value("${farmlife.security.max-failed-attempts:5}")
    private short maxFailedAttempts;

    @Value("${farmlife.security.lock-duration-minutes:15}")
    private int lockDurationMinutes;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailedLogin(Account account) {
        short attempts = (short) (account.getFailedAttempts() + 1);
        account.setFailedAttempts(attempts);
        if (attempts >= maxFailedAttempts) {
            account.setLockedUntil(OffsetDateTime.now().plusMinutes(lockDurationMinutes).toInstant()); //
        }
        accountRepository.save(account);
    }
}