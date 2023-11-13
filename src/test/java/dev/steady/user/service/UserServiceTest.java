package dev.steady.user.service;

import dev.steady.user.domain.Position;
import dev.steady.user.domain.Stack;
import dev.steady.user.domain.User;
import dev.steady.user.domain.UserStack;
import dev.steady.user.domain.repository.PositionRepository;
import dev.steady.user.domain.repository.StackRepository;
import dev.steady.user.domain.repository.UserRepository;
import dev.steady.user.domain.repository.UserStackRepository;
import dev.steady.user.dto.request.UserCreateRequest;
import dev.steady.user.dto.response.UserDetailResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.stream.IntStream;

import static dev.steady.global.auth.AuthFixture.createUserInfo;
import static dev.steady.user.fixture.UserFixtures.createFirstUser;
import static dev.steady.user.fixture.UserFixtures.createPosition;
import static dev.steady.user.fixture.UserFixtures.createStack;
import static dev.steady.user.fixture.UserFixtures.createUserUpdateRequest;
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

    private Position position;

    @BeforeEach
    void setUp() {
        this.position = positionRepository.save(createPosition());
    }

    @AfterEach
    void tearDown() {
        userStackRepository.deleteAll();
        userRepository.deleteAll();
        positionRepository.deleteAll();
        stackRepository.deleteAll();
    }

    @Test
    @DisplayName("프로필 정보를 입력 받아 유저를 생성한다.")
    void createUser() {
        // given
        var stacks = IntStream.range(0, 3)
                .mapToObj(i -> createStack())
                .toList();
        var savedStacks = stackRepository.saveAll(stacks);
        var stackIds = savedStacks.stream().map(Stack::getId).toList();

        // when
        var request = new UserCreateRequest(1L, "Nickname", position.getId(), stackIds);
        var userId = userService.createUser(request);

        User user = transactionTemplate.execute(status -> {
            var foundUser = userRepository.findById(userId).get();
            foundUser.getPosition().getName();
            return foundUser;
        });
        var userStacks = userStackRepository.findAllByUser(user);

        assertAll(
                () -> assertThat(user.getId()).isEqualTo(userId),
                () -> assertThat(user.getPosition().getName()).isEqualTo(position.getName()),
                () -> assertThat(userStacks).hasSameSizeAs(request.stackIds())
        );
    }

    @Test
    @DisplayName("내 프로필을 조회할 수 있다.")
    void getMyUserDetail() {
        // given
        var savedUser = userRepository.save(createFirstUser(position));
        var userInfo = createUserInfo(savedUser.getId());

        // when
        UserDetailResponse response = userService.getMyUserDetail(userInfo);
        User user = transactionTemplate.execute(status -> {
            var foundUser = userRepository.findById(userInfo.userId()).get();
            foundUser.getPosition().getName();
            return foundUser;
        });
        List<UserStack> userStacks = userStackRepository.findAllByUser(user);

        // then
        assertAll(
                () -> assertThat(response.userId()).isEqualTo(user.getId()),
                () -> assertThat(response.nickname()).isEqualTo(user.getNickname()),
                () -> assertThat(response.profileImage()).isEqualTo(user.getProfileImage()),
                () -> assertThat(response.position().name()).isEqualTo(user.getPosition().getName()),
                () -> assertThat(response.stacks()).hasSameSizeAs(userStacks)
        );
    }


    @Test
    @DisplayName("인증된 사용자의 정보를 수정할 수 있다.")
    void updateUser() {
        // given
        var savedUser = userRepository.save(createFirstUser(position));
        var userInfo = createUserInfo(savedUser.getId());
        var newPosition = positionRepository.save(createPosition());
        var stacks = IntStream.range(0, 3)
                .mapToObj(i -> createStack())
                .toList();
        var savedStacks = stackRepository.saveAll(stacks);
        var stackIds = savedStacks.stream().map(Stack::getId).toList();

        // when
        var request = createUserUpdateRequest(newPosition.getId(), stackIds);
        userService.updateUser(request, userInfo);
        User user = transactionTemplate.execute(status -> {
            var foundUser = userRepository.findById(userInfo.userId()).get();
            foundUser.getPosition().getId();
            return foundUser;
        });
        List<UserStack> userStacks = userStackRepository.findAllByUser(user);

        // then
        assertAll(
                () -> assertThat(user.getNickname()).isEqualTo(request.nickname()),
                () -> assertThat(user.getProfileImage()).isEqualTo(request.profileImage()),
                () -> assertThat(user.getBio()).isEqualTo(request.bio()),
                () -> assertThat(user.getPosition().getId()).isEqualTo(newPosition.getId()),
                () -> assertThat(userStacks).hasSameSizeAs(request.stackIds())
        );
    }

}
