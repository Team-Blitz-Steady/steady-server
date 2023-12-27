package dev.steady.application.domain.repository;

import dev.steady.application.domain.Application;
import dev.steady.global.config.JpaConfig;
import dev.steady.global.config.QueryDslConfig;
import dev.steady.steady.domain.repository.SteadyRepository;
import dev.steady.user.domain.repository.PositionRepository;
import dev.steady.user.domain.repository.StackRepository;
import dev.steady.user.domain.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;

import java.util.List;

import static dev.steady.application.domain.ApplicationStatus.WAITING;
import static dev.steady.application.fixture.ApplicationFixture.createApplication;
import static dev.steady.steady.fixture.SteadyFixturesV2.createSteady;
import static dev.steady.user.fixture.UserFixturesV2.generatePosition;
import static dev.steady.user.fixture.UserFixturesV2.generateStack;
import static dev.steady.user.fixture.UserFixturesV2.generateUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@DataJpaTest
@Import({JpaConfig.class, QueryDslConfig.class})
class ApplicationRepositoryTest {

    @Autowired
    private SteadyRepository steadyRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private PositionRepository positionRepository;

    @Autowired
    private StackRepository stackRepository;

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void tearDown() {
        applicationRepository.deleteAll();
        steadyRepository.deleteAll();
        userRepository.deleteAll();
        positionRepository.deleteAll();
        stackRepository.deleteAll();
    }

    @DisplayName("스테디아이디를 통해 신청서를 전체 조회할 수 있다.")
    @Test
    void findAllBySteadyId() {
        //given
        var position = positionRepository.save(generatePosition());
        var leader = userRepository.save(generateUser(position));
        var savedStack = stackRepository.save(generateStack());
        var steady = steadyRepository.save(createSteady(leader, List.of(savedStack)));
        var secondUser = userRepository.save(generateUser(position));
        var thirdUser = userRepository.save(generateUser(position));
        applicationRepository.saveAll(List.of(createApplication(secondUser, steady),
                createApplication(thirdUser, steady)));
        //when
        Slice<Application> applications = applicationRepository.findAllBySteadyIdAndStatus(
                steady.getId(),
                WAITING,
                PageRequest.of(0, 10));
        //then
        assertAll(
                () -> assertThat(applications.getNumberOfElements()).isEqualTo(2),
                () -> assertThat(applications.hasNext()).isFalse());
    }

}
