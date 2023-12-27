package dev.steady.steady.service;

import dev.steady.global.auth.UserInfo;
import dev.steady.steady.domain.Steady;
import dev.steady.steady.domain.repository.SteadyLikeRepository;
import dev.steady.steady.domain.repository.SteadyRepository;
import dev.steady.steady.dto.response.SteadyLikeResponse;
import dev.steady.user.domain.Position;
import dev.steady.user.domain.Stack;
import dev.steady.user.domain.User;
import dev.steady.user.domain.repository.PositionRepository;
import dev.steady.user.domain.repository.StackRepository;
import dev.steady.user.domain.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static dev.steady.steady.fixture.SteadyFixtures.createSteady;
import static dev.steady.user.fixture.UserFixtures.createFirstUser;
import static dev.steady.user.fixture.UserFixtures.createPosition;
import static dev.steady.user.fixture.UserFixtures.createStack;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest
class SteadyLikeServiceTest {


    @Autowired
    private SteadyLikeService steadyLikeService;
    @Autowired
    private SteadyLikeRepository steadyLikeRepository;
    @Autowired
    private SteadyRepository steadyRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PositionRepository positionRepository;
    @Autowired
    private StackRepository stackRepository;

    private Position position;
    private User user;

    @BeforeEach
    void setUp() {
        this.position = positionRepository.save(createPosition());
        this.user = userRepository.save(createFirstUser(position));
    }

    @AfterEach
    void tearDown() {
        steadyLikeRepository.deleteAll();
        steadyRepository.deleteAll();
        userRepository.deleteAll();
        positionRepository.deleteAll();
        stackRepository.deleteAll();
    }

    @DisplayName("스테디에 좋아요를 처음 누르면 좋아요가 상승한다.")
    @Test
    void updateSteadyLikeTest() {
        //given
        Stack stack = stackRepository.save(createStack());
        Steady steady = steadyRepository.save(createSteady(user, stack));

        //when
        SteadyLikeResponse response = steadyLikeService.updateSteadyLike(steady.getId(), new UserInfo(user.getId()));

        //then
        assertAll(
                () -> assertThat(response.isLike()).isTrue(),
                () -> assertThat(response.likeCount()).isOne()
        );
    }

    @DisplayName("스테디에 좋아요를 두번 누르면 좋아요가 해제된다.")
    @Test
    void updateSteadyLikeToggleTest() {
        //given
        Stack stack = stackRepository.save(createStack());
        Steady steady = steadyRepository.save(createSteady(user, stack));

        //when
        steadyLikeService.updateSteadyLike(steady.getId(), new UserInfo(user.getId()));
        SteadyLikeResponse response = steadyLikeService.updateSteadyLike(steady.getId(), new UserInfo(user.getId()));

        //then
        assertAll(
                () -> assertThat(response.isLike()).isFalse(),
                () -> assertThat(response.likeCount()).isZero()
        );
    }

}
