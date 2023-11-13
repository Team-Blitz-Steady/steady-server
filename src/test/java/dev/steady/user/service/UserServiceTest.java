package dev.steady.user.service;

import dev.steady.user.domain.Stack;
import dev.steady.user.domain.User;
import dev.steady.user.domain.repository.PositionRepository;
import dev.steady.user.domain.repository.StackRepository;
import dev.steady.user.domain.repository.UserRepository;
import dev.steady.user.domain.repository.UserStackRepository;
import dev.steady.user.dto.request.UserCreateRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.stream.IntStream;

import static dev.steady.user.fixture.UserFixtures.createPosition;
import static dev.steady.user.fixture.UserFixtures.createStack;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StackRepository stackRepository;

    @Autowired
    private PositionRepository positionRepository;

    @Autowired
    private UserStackRepository userStackRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @AfterEach
    void tearDown() {
        userStackRepository.deleteAll();
        userRepository.deleteAll();
        positionRepository.deleteAll();
        stackRepository.deleteAll();
    }

    @DisplayName("프로필 정보를 입력 받아 유저를 생성한다.")
    @Test
    void createUser() {
        // given
        var savedPosition = positionRepository.save(createPosition());
        var stacks = IntStream.range(0, 3)
                .mapToObj(i -> createStack())
                .toList();
        var savedStacks = stackRepository.saveAll(stacks);
        var stackIds = savedStacks.stream().map(Stack::getId).toList();

        // when
        var request = new UserCreateRequest(1L, "Nickname", savedPosition.getId(), stackIds);
        var userId = userService.createUser(request);

        User user = transactionTemplate.execute(status -> {
            var foundUser = userRepository.findById(userId).get();
            foundUser.getPosition().getName();
            return foundUser;
        });
        var userStacks = userStackRepository.findAllByUser(user);

        assertAll(
                () -> assertThat(user.getId()).isEqualTo(userId),
                () -> assertThat(user.getPosition().getName()).isEqualTo(savedPosition.getName()),
                () -> assertThat(userStacks).hasSameSizeAs(request.stackIds())
        );
    }

}
