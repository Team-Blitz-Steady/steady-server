package dev.steady.auth.service;

import dev.steady.auth.domain.Account;
import dev.steady.auth.domain.Platform;
import dev.steady.auth.domain.repository.AccountRepository;
import dev.steady.user.domain.User;
import dev.steady.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    @Transactional
    public Platform registerUser(Long accountId, Long userId) {
        Account account = accountRepository.getById(accountId);
        User user = userRepository.getUserBy(userId);
        account.registerUser(user);

        return account.getPlatform();
    }

}
