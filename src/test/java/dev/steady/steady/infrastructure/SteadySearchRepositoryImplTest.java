package dev.steady.steady.infrastructure;

import dev.steady.global.config.JpaConfig;
import dev.steady.global.config.QueryDslConfig;
import dev.steady.steady.domain.repository.ParticipantRepository;
import dev.steady.steady.domain.repository.SteadyPositionRepository;
import dev.steady.steady.domain.repository.SteadyQuestionRepository;
import dev.steady.steady.domain.repository.SteadyRepository;
import dev.steady.steady.dto.FilterConditionDto;
import dev.steady.steady.dto.request.SteadySearchRequest;
import dev.steady.steady.dto.response.MySteadyQueryResponse;
import dev.steady.user.domain.repository.PositionRepository;
import dev.steady.user.domain.repository.StackRepository;
import dev.steady.user.domain.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;

import java.util.List;

import static dev.steady.steady.domain.SteadyStatus.CLOSED;
import static dev.steady.steady.domain.SteadyStatus.FINISHED;
import static dev.steady.steady.domain.SteadyStatus.RECRUITING;
import static dev.steady.steady.fixture.SteadyFixtures.createSteady;
import static dev.steady.steady.fixture.SteadyFixtures.createSteadyPosition;
import static dev.steady.steady.fixture.SteadyFixtures.createSteadyQuestion;
import static dev.steady.steady.fixture.SteadyFixtures.createSteadyRequest;
import static dev.steady.user.fixture.UserFixtures.createFirstUser;
import static dev.steady.user.fixture.UserFixtures.createPosition;
import static dev.steady.user.fixture.UserFixtures.createStack;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@DataJpaTest
@Import({JpaConfig.class, QueryDslConfig.class})
class SteadySearchRepositoryImplTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private SteadySearchRepositoryImpl queryDslRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StackRepository stackRepository;

    @Autowired
    private SteadyRepository steadyRepository;

    @Autowired
    private PositionRepository positionRepository;

    @Autowired
    private SteadyPositionRepository steadyPositionRepository;

    @Autowired
    private SteadyQuestionRepository steadyQuestionRepository;

    @Autowired
    private ParticipantRepository participantRepository;

    @Test
    @DisplayName("검색 조건에 해당하는 스테디를 조회할 수 있다.")
    void findAllByConditionTest() {
        // given
        var position = positionRepository.save(createPosition());
        var user = userRepository.save(createFirstUser(position));
        var stack = stackRepository.save(createStack());
        var steadyRequest = createSteadyRequest(stack.getId(), position.getId());
        var steady = steadyRepository.save(steadyRequest.toEntity(user));
        var steadyPosition = createSteadyPosition(steady, position);
        steadyPositionRepository.save(steadyPosition);
        var steadyQuestion = createSteadyQuestion(steady, steadyRequest.questions());
        steadyQuestionRepository.saveAll(steadyQuestion);
        entityManager.flush();
        entityManager.clear();

        // when
        var request = new SteadySearchRequest(
                null,
                "DESC",
                null,
                null,
                "study",
                "online",
                "Java",
                "백엔드",
                "recruiting",
                "false",
                "스테디");

        var pageable = request.toPageable();
        var condition = FilterConditionDto.from(request);
        var response = queryDslRepository.findAllByFilterCondition(null, condition, pageable);
        var returnedSteady = response.getContent().get(0);

        // then
        int expectedSize = 1;
        assertAll(
                () -> assertThat(response.getTotalElements()).isEqualTo(expectedSize),
                () -> assertThat(returnedSteady.getId()).isEqualTo(steady.getId())
        );
    }

    @Test
    @DisplayName("검색 조건에 해당하지 않으면 스테디를 조회할 수 없다.")
    void findAllByConditionNotInTest() {
        // given
        var position = positionRepository.save(createPosition());
        var user = userRepository.save(createFirstUser(position));
        var stack = stackRepository.save(createStack());
        var steadyRequest = createSteadyRequest(stack.getId(), position.getId());
        var steady = steadyRepository.save(steadyRequest.toEntity(user));
        var steadyPosition = createSteadyPosition(steady, position);
        steadyPositionRepository.save(steadyPosition);
        var steadyQuestion = createSteadyQuestion(steady, steadyRequest.questions());
        steadyQuestionRepository.saveAll(steadyQuestion);
        entityManager.flush();
        entityManager.clear();

        // when
        var request = new SteadySearchRequest(
                null,
                null,
                null,
                null,
                "study",
                "both",
                "Java",
                "데브옵스",
                "finished",
                "false",
                "말도 안 되는 검색 조건!");

        var pageable = request.toPageable();
        var condition = FilterConditionDto.from(request);
        var response = queryDslRepository.findAllByFilterCondition(null, condition, pageable);

        // then
        assertThat(response.getTotalElements()).isZero();
    }

    @Test
    @DisplayName("기본 조건은 전체 조회 결과를 반환한다.")
    void findAllByBasicConditionTest() {
        // given
        var position = positionRepository.save(createPosition());
        var user = userRepository.save(createFirstUser(position));
        var stack = stackRepository.save(createStack());
        var steadyRequest = createSteadyRequest(stack.getId(), position.getId());
        var steady = steadyRepository.save(steadyRequest.toEntity(user));
        var steadyPosition = createSteadyPosition(steady, position);
        steadyPositionRepository.save(steadyPosition);
        var steadyQuestion = createSteadyQuestion(steady, steadyRequest.questions());
        steadyQuestionRepository.saveAll(steadyQuestion);
        entityManager.flush();
        entityManager.clear();

        // when
        var request = new SteadySearchRequest(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                "false",
                null);
        var pageable = request.toPageable();
        var condition = FilterConditionDto.from(request);
        var response = queryDslRepository.findAllByFilterCondition(null, condition, pageable);

        // then
        int expectedSize = 1;
        assertThat(response.getTotalElements()).isEqualTo(expectedSize);
    }

    @DisplayName("내가 참여한 전체 스테디를 조회한다.")
    @Test
    void findMyAllSteadies() {
        //given
        var position = positionRepository.save(createPosition());
        var user = userRepository.save(createFirstUser(position));
        var stack = stackRepository.save(createStack());
        var steady = createSteady(user, List.of(stack), RECRUITING);
        var secondSteady = createSteady(user, List.of(stack), CLOSED);
        var thirdSteady = createSteady(user, List.of(stack), FINISHED);
        var steadies = steadyRepository.saveAll(List.of(steady, secondSteady, thirdSteady));
        //when
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.Direction.DESC, "createdAt");
        Slice<MySteadyQueryResponse> mySteadies = queryDslRepository.findMySteadies(null, user, pageRequest);
        //then
        assertThat(mySteadies.hasNext()).isFalse();
        assertThat(mySteadies.getNumberOfElements()).isEqualTo(steadies.size());
    }

    @DisplayName("내가 참여했지만 종료된 스테디를 조회한다.")
    @Test
    void findMyFinishedSteadies() {
        //given
        var position = positionRepository.save(createPosition());
        var user = userRepository.save(createFirstUser(position));
        var stack = stackRepository.save(createStack());
        var steady = createSteady(user, List.of(stack), RECRUITING);
        var secondSteady = createSteady(user, List.of(stack), CLOSED);
        var thirdSteady = createSteady(user, List.of(stack), FINISHED);
        var steadies = steadyRepository.saveAll(List.of(steady, secondSteady, thirdSteady));
        //when
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.Direction.DESC, "createdAt");
        Slice<MySteadyQueryResponse> mySteadies = queryDslRepository.findMySteadies(FINISHED, user, pageRequest);
        //then
        assertThat(mySteadies.hasNext()).isFalse();
        assertThat(mySteadies.getNumberOfElements()).isOne();
    }

    @DisplayName("내가 참여해 진행중 스테디를 조회한다.")
    @Test
    void findMyNotFinishedSteadies() {
        //given
        var position = positionRepository.save(createPosition());
        var user = userRepository.save(createFirstUser(position));
        var stack = stackRepository.save(createStack());
        var steady = createSteady(user, List.of(stack), RECRUITING);
        var secondSteady = createSteady(user, List.of(stack), CLOSED);
        var thirdSteady = createSteady(user, List.of(stack), FINISHED);
        var steadies = steadyRepository.saveAll(List.of(steady, secondSteady, thirdSteady));
        //when
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.Direction.DESC, "createdAt");
        Slice<MySteadyQueryResponse> mySteadies = queryDslRepository.findMySteadies(RECRUITING, user, pageRequest);
        //then
        assertThat(mySteadies.hasNext()).isFalse();
        assertThat(mySteadies.getNumberOfElements()).isEqualTo(2);
    }

}
