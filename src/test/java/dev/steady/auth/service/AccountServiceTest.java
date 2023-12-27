package dev.steady.auth.service;

import dev.steady.auth.domain.repository.AccountRepository;
import dev.steady.user.domain.repository.PositionRepository;
import dev.steady.user.domain.repository.UserRepository;
import dev.steady.user.fixture.UserFixtures;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionTemplate;

import static dev.steady.auth.fixture.AccountFixture.createAccount;
import static dev.steady.user.fixture.UserFixtures.createFirstUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest
class AccountServiceTest {

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PositionRepository positionRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @AfterEach
    void tearDown() {
        accountRepository.deleteAll();
        userRepository.deleteAll();
        positionRepository.deleteAll();
    }

    @Test
    @DisplayName("Account가 존재한다면 User를 등록할 수 있다.")
    void registerUser() {
        // given
        var account = createAccount();
        var savedAccount = accountRepository.save(account);

        var position = UserFixtures.createPosition();
        var savedPosition = positionRepository.save(position);

        var user = createFirstUser(savedPosition);
        var savedUser = userRepository.save(user);

        // when
        accountService.registerUser(savedAccount.getId(), savedUser.getId());
        var foundAccount = transactionTemplate.execute(status -> {
                    return accountRepository.findById(savedAccount.getId()).get();
                }
        );

        // then
        assertAll(
                () -> assertThat(foundAccount.getUser()).isNotNull(),
                () -> assertThat(foundAccount.getUser().getId()).isEqualTo(savedUser.getId())
        );
    }

}
