package dev.steady.steady.service;

import dev.steady.application.dto.response.SliceResponse;
import dev.steady.global.auth.UserInfo;
import dev.steady.global.exception.ForbiddenException;
import dev.steady.global.exception.InvalidStateException;
import dev.steady.global.exception.NotFoundException;
import dev.steady.steady.domain.Participant;
import dev.steady.steady.domain.Steady;
import dev.steady.steady.domain.SteadyPosition;
import dev.steady.steady.domain.SteadyQuestion;
import dev.steady.steady.domain.SteadyStack;
import dev.steady.steady.domain.SteadyStatus;
import dev.steady.steady.domain.repository.ParticipantRepository;
import dev.steady.steady.domain.repository.SteadyPositionRepository;
import dev.steady.steady.domain.repository.SteadyQuestionRepository;
import dev.steady.steady.domain.repository.SteadyRepository;
import dev.steady.steady.domain.repository.SteadyStackRepository;
import dev.steady.steady.dto.FilterConditionDto;
import dev.steady.steady.dto.request.SteadyCreateRequest;
import dev.steady.steady.dto.request.SteadyQuestionUpdateRequest;
import dev.steady.steady.dto.request.SteadySearchRequest;
import dev.steady.steady.dto.request.SteadyUpdateRequest;
import dev.steady.steady.dto.response.MySteadyResponse;
import dev.steady.steady.dto.response.PageResponse;
import dev.steady.steady.dto.response.ParticipantsResponse;
import dev.steady.steady.dto.response.SteadyDetailResponse;
import dev.steady.steady.dto.response.SteadyQueryResponse;
import dev.steady.steady.dto.response.SteadyQuestionsResponse;
import dev.steady.user.domain.User;
import dev.steady.user.domain.repository.PositionRepository;
import dev.steady.user.domain.repository.StackRepository;
import dev.steady.user.domain.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static dev.steady.global.auth.AuthFixture.createUserInfo;
import static dev.steady.steady.domain.SteadyStatus.CLOSED;
import static dev.steady.steady.domain.SteadyStatus.FINISHED;
import static dev.steady.steady.domain.SteadyStatus.RECRUITING;
import static dev.steady.steady.fixture.SteadyFixtures.createAnotherSteadyRequest;
import static dev.steady.steady.fixture.SteadyFixtures.createSteady;
import static dev.steady.steady.fixture.SteadyFixtures.createSteadyQuestion;
import static dev.steady.steady.fixture.SteadyFixtures.createSteadyRequest;
import static dev.steady.steady.fixture.SteadyFixtures.createSteadyUpdateRequest;
import static dev.steady.user.fixture.UserFixtures.createAnotherPosition;
import static dev.steady.user.fixture.UserFixtures.createAnotherStack;
import static dev.steady.user.fixture.UserFixtures.createFirstUser;
import static dev.steady.user.fixture.UserFixtures.createPosition;
import static dev.steady.user.fixture.UserFixtures.createSecondUser;
import static dev.steady.user.fixture.UserFixtures.createStack;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

@Transactional
@SpringBootTest
class SteadyServiceTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private SteadyService steadyService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SteadyRepository steadyRepository;

    @Autowired
    private StackRepository stackRepository;

    @Autowired
    private PositionRepository positionRepository;

    @Autowired
    private ParticipantRepository participantRepository;

    @Autowired
    private SteadyStackRepository steadyStackRepository;

    @Autowired
    private SteadyQuestionRepository steadyQuestionRepository;

    @Autowired
    private SteadyPositionRepository steadyPositionRepository;

    @Test
    @DisplayName("스터디 생성 요청을 통해 스테디와 스테디 관련 정보를 생성할 수 있다.")
    void createSteadyTest() {
        // given
        var position = positionRepository.save(createPosition());
        var leader = userRepository.save(createFirstUser(position));
        var stack = stackRepository.save(createStack());
        var userInfo = createUserInfo(leader.getId());

        // when
        SteadyCreateRequest steadyRequest = createSteadyRequest(stack.getId(), position.getId());
        Long steadyId = steadyService.create(steadyRequest, userInfo);
        entityManager.flush();
        entityManager.clear();

        // then
        Steady steady = steadyRepository.findById(steadyId).get();
        List<Participant> participants = participantRepository.findBySteadyId(steadyId);
        List<SteadyStack> steadyStacks = steadyStackRepository.findBySteadyId(steadyId);
        List<SteadyQuestion> steadyQuestions = steadyQuestionRepository.findBySteadyId(steadyId);
        List<SteadyPosition> steadyPositions = steadyPositionRepository.findBySteadyId(steadyId);
        assertAll(
                () -> assertThat(steady.getId()).isEqualTo(steadyId),
                () -> assertThat(participants).hasSameSizeAs(steady.getParticipants().getAllParticipants()),
                () -> assertThat(steadyStacks).hasSameSizeAs(steady.getSteadyStacks()),
                () -> assertThat(steadyQuestions).hasSameSizeAs(steadyRequest.questions()),
                () -> assertThat(steadyPositions).hasSameSizeAs(steadyRequest.positions())
        );
    }

    @Test
    @DisplayName("스테디 검색 조회 요청을 통해 페이징 처리된 응답을 반환할 수 있다.")
    void getSteadiesSearchTest() {
        // given
        var position = positionRepository.save(createPosition());
        var leader = userRepository.save(createFirstUser(position));
        var stack = stackRepository.save(createStack());
        var userInfo = createUserInfo(leader.getId());

        var steadyRequest = createSteadyRequest(stack.getId(), position.getId());
        var anotherSteadyRequest = createAnotherSteadyRequest(stack.getId(), position.getId());
        steadyService.create(steadyRequest, userInfo);
        steadyService.create(anotherSteadyRequest, userInfo);
        entityManager.flush();
        entityManager.clear();

        // when
        SteadySearchRequest searchRequest = new SteadySearchRequest(null,
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
        FilterConditionDto condition = FilterConditionDto.from(searchRequest);
        Pageable pageable = searchRequest.toPageable();
        PageResponse<SteadyQueryResponse> response = steadyService.getSteadies(userInfo, condition, pageable);

        // then
        List<Steady> steadies = steadyRepository.findAll();
        List<SteadyQueryResponse> content = response.content();
        assertAll(
                () -> assertThat(content).hasSameSizeAs(steadies),
                () -> assertThat(content.get(0).promotedAt()).isAfter(content.get(1).promotedAt())
        );
    }

    @Test
    @DisplayName("마감임박순 조건을 통해 페이징 처리된 응답을 반환할 수 있다.")
    void getSteadiesSearchOrderByDeadlineTest() {
        // given
        var position = positionRepository.save(createPosition());
        var leader = userRepository.save(createFirstUser(position));
        var stack = stackRepository.save(createStack());
        var userInfo = createUserInfo(leader.getId());

        var steadyRequest = createSteadyRequest(stack.getId(), position.getId());
        var anotherSteadyRequest = createAnotherSteadyRequest(stack.getId(), position.getId());
        steadyService.create(steadyRequest, userInfo);
        steadyService.create(anotherSteadyRequest, userInfo);
        entityManager.flush();
        entityManager.clear();

        // when
        SteadySearchRequest searchRequest = new SteadySearchRequest(
                null,
                "asc",
                "deadline",
                null,
                null,
                null,
                null,
                null,
                null,
                "false",
                null);
        FilterConditionDto condition = FilterConditionDto.from(searchRequest);
        Pageable pageable = searchRequest.toPageable();
        PageResponse<SteadyQueryResponse> response = steadyService.getSteadies(userInfo, condition, pageable);

        // then
        List<Steady> steadies = steadyRepository.findAll();
        List<SteadyQueryResponse> content = response.content();
        assertAll(
                () -> assertThat(content).hasSameSizeAs(steadies),
                () -> assertThat(content.get(0).deadline()).isBefore(content.get(1).deadline())
        );
    }

    @Test
    @DisplayName("스테디 식별자를 통해 스테디 상세 조회를 할 수 있다.")
    void getDetailSteadyTest() {
        // given
        var position = positionRepository.save(createPosition());
        var leader = userRepository.save(createFirstUser(position));
        var stack = stackRepository.save(createStack());
        var userInfo = createUserInfo(leader.getId());

        var steadyRequest = createSteadyRequest(stack.getId(), position.getId());
        var steadyId = steadyService.create(steadyRequest, userInfo);
        entityManager.flush();
        entityManager.clear();

        // when
        SteadyDetailResponse response = steadyService.getDetailSteady(steadyId, userInfo);

        // then
        Steady steady = steadyRepository.getSteady(steadyId);
        List<SteadyPosition> positions = steadyPositionRepository.findBySteadyId(steadyId);
        assertAll(
                () -> assertThat(response.id()).isEqualTo(steady.getId()),
                () -> assertThat(response.positions()).hasSameSizeAs(positions),
                () -> assertThat(response.isLeader()).isTrue(),
                () -> assertThat(response.applicationId()).isNull(),
                () -> assertThat(response.isLiked()).isFalse()
        );
    }

    @Test
    @DisplayName("리더가 아닌 사용자도 스테디 식별자를 통해 스테디 상세 조회를 할 수 있다.")
    void getDetailSteadyNotLeaderTest() {
        // given
        var position = positionRepository.save(createPosition());
        var leader = userRepository.save(createFirstUser(position));
        var stack = stackRepository.save(createStack());
        var otherUser = userRepository.save(createSecondUser(position));
        var userInfo = createUserInfo(otherUser.getId());

        var steady = steadyRepository.save(createSteady(leader, stack));
        var steadyId = steady.getId();
        entityManager.flush();
        entityManager.clear();

        // when
        SteadyDetailResponse response = steadyService.getDetailSteady(steadyId, userInfo);

        // then
        Steady foundSteady = steadyRepository.getSteady(steadyId);
        List<SteadyPosition> positions = steadyPositionRepository.findBySteadyId(steadyId);
        assertAll(
                () -> assertThat(response.id()).isEqualTo(foundSteady.getId()),
                () -> assertThat(response.positions()).hasSameSizeAs(positions),
                () -> assertThat(response.isLeader()).isFalse(),
                () -> assertThat(response.applicationId()).isNull(),
                () -> assertThat(response.isLiked()).isFalse()
        );
    }

    @Test
    @DisplayName("리더가 아닌 사용자가 게시물을 조회하면 조회수가 상승한다.")
    void getDetailSteadyViewCountIncreaseTest() {
        // given
        var position = positionRepository.save(createPosition());
        var leader = userRepository.save(createFirstUser(position));
        var stack = stackRepository.save(createStack());
        var otherUser = userRepository.save(createSecondUser(position));
        var userInfo = createUserInfo(otherUser.getId());

        var steady = steadyRepository.save(createSteady(leader, stack));
        var steadyId = steady.getId();
        entityManager.flush();
        entityManager.clear();

        // when
        SteadyDetailResponse response = steadyService.getDetailSteady(steadyId, userInfo);

        // then
        assertThat(response.viewCount()).isOne();
    }

    @Test
    @DisplayName("게시물을 조회한지 3시간이 지나지 않았을때 재조회하면 조회수가 상승하지 않는다.")
    void getDetailSteadyViewCountNotIncreaseTest() {
        // given
        var position = positionRepository.save(createPosition());
        var leader = userRepository.save(createFirstUser(position));
        var stack = stackRepository.save(createStack());
        var otherUser = userRepository.save(createSecondUser(position));
        var userInfo = createUserInfo(otherUser.getId());

        var steady = steadyRepository.save(createSteady(leader, stack));
        var steadyId = steady.getId();
        entityManager.flush();
        entityManager.clear();

        // when
        steadyService.getDetailSteady(steadyId, userInfo);
        SteadyDetailResponse response = steadyService.getDetailSteady(steadyId, userInfo);

        // then
        assertThat(response.viewCount()).isOne();
    }

    @Test
    @DisplayName("로그인 하지 않은 사용자도 스테디 식별자를 통해 스테디 상세 조회를 할 수 있다.")
    void getDetailSteadyNotLoginUserTest() {
        // given
        var position = positionRepository.save(createPosition());
        var leader = userRepository.save(createFirstUser(position));
        var stack = stackRepository.save(createStack());
        var userInfo = new UserInfo(null);

        var steady = steadyRepository.save(createSteady(leader, stack));
        var steadyId = steady.getId();
        entityManager.flush();
        entityManager.clear();

        // when
        SteadyDetailResponse response = steadyService.getDetailSteady(steadyId, userInfo);

        // then
        Steady foundSteady = steadyRepository.findById(steadyId).get();
        List<SteadyPosition> positions = steadyPositionRepository.findBySteadyId(steadyId);
        assertAll(
                () -> assertThat(response.id()).isEqualTo(foundSteady.getId()),
                () -> assertThat(response.positions()).hasSameSizeAs(positions),
                () -> assertThat(response.isLeader()).isFalse(),
                () -> assertThat(response.applicationId()).isNull(),
                () -> assertThat(response.isLiked()).isFalse()
        );
    }

    @Test
    @DisplayName("스테디 식별자를 통해 스테디 질문을 조회할 수 있다.")
    void getSteadyQuestionsTest() {
        // given
        var position = positionRepository.save(createPosition());
        var leader = userRepository.save(createFirstUser(position));
        var stack = stackRepository.save(createStack());

        Steady steady = steadyRepository.save(createSteady(leader, stack));
        List<String> questions = List.of("질문1", "질문2");
        steadyQuestionRepository.saveAll(createSteadyQuestion(steady, questions));
        entityManager.flush();
        entityManager.clear();

        // when
        SteadyQuestionsResponse response = steadyService.getSteadyQuestions(steady.getId());

        // then
        Assertions.assertAll(
                () -> assertThat(response.steadyQuestions()).hasSameSizeAs(questions),
                () -> assertThat(response.steadyName()).isEqualTo(steady.getName())
        );
    }

    @Test
    @DisplayName("스테디 식별자를 통해 참여자 전체 조회를 할 수 있다.")
    void getSteadyParticipantsTest() {
        // given
        var position = positionRepository.save(createPosition());
        var leader = userRepository.save(createFirstUser(position));
        var stack = stackRepository.save(createStack());

        var steady = steadyRepository.save(createSteady(leader, stack));
        var steadyId = steady.getId();

        var anotherUser = userRepository.save(createSecondUser(position));
        steady.addParticipantByLeader(leader, anotherUser);
        entityManager.flush();
        entityManager.clear();

        // when & then
        int expectedSize = 2;
        ParticipantsResponse response = steadyService.getSteadyParticipants(steadyId);
        assertAll(
                () -> assertThat(response.participants()).hasSize(expectedSize)
                        .extracting("nickname")
                        .containsExactlyInAnyOrder(leader.getNickname(), anotherUser.getNickname())
        );
    }

    @Test
    @DisplayName("스테디 수정 요청을 통해 스테디 정보를 수정할 수 있다.")
    void steadyUpdateTest() {
        // given
        var position = positionRepository.save(createPosition());
        var leader = userRepository.save(createFirstUser(position));
        var stack = stackRepository.save(createStack());
        var userInfo = createUserInfo(leader.getId());

        var steadyRequest = createSteadyRequest(stack.getId(), position.getId());
        var steadyId = steadyService.create(steadyRequest, userInfo);

        var anotherStack = stackRepository.save(createAnotherStack());
        var anotherPosition = positionRepository.save(createAnotherPosition());
        entityManager.flush();
        entityManager.clear();

        // when
        SteadyUpdateRequest steadyUpdateRequest = createSteadyUpdateRequest(anotherStack.getId(), anotherPosition.getId());
        steadyService.updateSteady(steadyId, steadyUpdateRequest, userInfo);
        entityManager.flush();
        entityManager.clear();

        // then
        Steady updatedSteady = steadyRepository.findById(steadyId).get();
        List<SteadyStack> steadyStacks = steadyStackRepository.findBySteadyId(steadyId);
        List<SteadyPosition> steadyPositions = steadyPositionRepository.findBySteadyId(steadyId);
        assertAll(
                () -> assertThat(updatedSteady.getName()).isEqualTo(steadyUpdateRequest.name()),
                () -> assertThat(updatedSteady.getBio()).isEqualTo(steadyUpdateRequest.bio()),
                () -> assertThat(updatedSteady.getType()).isEqualTo(steadyUpdateRequest.type()),
                () -> assertThat(updatedSteady.getStatus()).isEqualTo(steadyUpdateRequest.status()),
                () -> assertThat(updatedSteady.getParticipantLimit()).isEqualTo(steadyUpdateRequest.participantLimit()),
                () -> assertThat(updatedSteady.getSteadyMode()).isEqualTo(steadyUpdateRequest.steadyMode()),
                () -> assertThat(String.valueOf(updatedSteady.getScheduledPeriod())).isEqualTo(steadyUpdateRequest.scheduledPeriod()),
                () -> assertThat(updatedSteady.getDeadline()).isEqualTo(steadyUpdateRequest.deadline()),
                () -> assertThat(updatedSteady.getTitle()).isEqualTo(steadyUpdateRequest.title()),
                () -> assertThat(updatedSteady.getContent()).isEqualTo(steadyUpdateRequest.content()),
                () -> assertThat(updatedSteady.getSteadyStacks()).hasSameSizeAs(steadyStacks)
                        .extracting("id").containsExactly(steadyStacks.get(0).getId()),
                () -> assertThat(steadyPositions).hasSameSizeAs(steadyUpdateRequest.positions())
                        .extracting("position.id").containsAll(steadyUpdateRequest.positions())
        );
    }

    @Test
    @DisplayName("리더가 아닌 유저가 수정 요청을 보내면 에러를 반환한다.")
    void updateSteadyByAnotherUserTest() {
        // given
        var position = positionRepository.save(createPosition());
        var leader = userRepository.save(createFirstUser(position));
        var stack = stackRepository.save(createStack());
        var userInfo = createUserInfo(leader.getId());

        var steadyRequest = createSteadyRequest(stack.getId(), position.getId());
        var steadyId = steadyService.create(steadyRequest, userInfo);

        var anotherPosition = positionRepository.save(createAnotherPosition());
        var anotherStack = stackRepository.save(createAnotherStack());
        entityManager.flush();
        entityManager.clear();

        // when & then
        User anotherUser = userRepository.save(createSecondUser(anotherPosition));
        UserInfo anotherUserInfo = createUserInfo(anotherUser.getId());
        SteadyUpdateRequest steadyUpdateRequest = createSteadyUpdateRequest(anotherStack.getId(), anotherPosition.getId());
        assertThatThrownBy(() -> steadyService.updateSteady(steadyId, steadyUpdateRequest, anotherUserInfo))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("스테디 질문 수정 요청을 통해 스테디 질문을 수정할 수 있다.")
    void updateSteadyQuestionsTest() {
        // given
        var position = positionRepository.save(createPosition());
        var leader = userRepository.save(createFirstUser(position));
        var stack = stackRepository.save(createStack());
        var userInfo = createUserInfo(leader.getId());

        var steadyRequest = createSteadyRequest(stack.getId(), position.getId());
        var steadyId = steadyService.create(steadyRequest, userInfo);

        // when
        List<SteadyQuestion> originalSteadyQuestions = steadyQuestionRepository.findBySteadyId(steadyId);
        SteadyQuestionUpdateRequest request = new SteadyQuestionUpdateRequest(List.of("변경된 질문1", "변경된 질문2"));
        steadyService.updateSteadyQuestions(steadyId, request, userInfo);
        entityManager.flush();
        entityManager.clear();

        // then
        List<SteadyQuestion> updatedSteadyQuestions = steadyQuestionRepository.findBySteadyId(steadyId);
        assertAll(
                () -> assertThat(originalSteadyQuestions).isNotEqualTo(updatedSteadyQuestions),
                () -> assertThat(originalSteadyQuestions.get(0).getContent()).isNotEqualTo(request.questions().get(0)),
                () -> assertThat(updatedSteadyQuestions.get(0).getContent()).isEqualTo(request.questions().get(0))
        );
    }

    @Test
    @DisplayName("스테디 리더가 참여자를 추방할 수 있다.")
    void expelParticipantTest() {
        // given
        var position = positionRepository.save(createPosition());
        var leader = userRepository.save(createFirstUser(position));
        var member = userRepository.save(createSecondUser(position));
        var stack = stackRepository.save(createStack());
        var userInfo = createUserInfo(leader.getId());

        var steadyRequest = createSteadyRequest(stack.getId(), position.getId());
        var steadyId = steadyService.create(steadyRequest, userInfo);
        var steady = steadyRepository.getSteady(steadyId);
        steady.addParticipantByLeader(leader, member);

        // when
        steadyService.expelParticipant(steady.getId(), member.getId(), userInfo);

        // then
        Steady foundSteady = steadyRepository.getSteady(steadyId);
        assertThat(foundSteady.getNumberOfParticipants()).isEqualTo(1);
    }

    @Test
    @DisplayName("스테디 리더가 끌어올리기 요청을 통해 스테디를 끌어올릴 수 있다.")
    void promoteSteadyTest() {
        // given
        var position = positionRepository.save(createPosition());
        var leader = userRepository.save(createFirstUser(position));
        var stack = stackRepository.save(createStack());
        var userInfo = createUserInfo(leader.getId());

        var steadyRequest = createSteadyRequest(stack.getId(), position.getId());
        var steadyId = steadyService.create(steadyRequest, userInfo);
        entityManager.flush();
        entityManager.clear();

        // when
        Steady steady = steadyRepository.findById(steadyId).get();
        steadyService.promoteSteady(steadyId, userInfo);
        entityManager.flush();
        entityManager.clear();

        // then
        Steady updatedSteady = steadyRepository.findById(steadyId).get();
        LocalDateTime createdAt = steady.getCreatedAt();
        LocalDateTime promotedAt = updatedSteady.getPromotion().getPromotedAt();
        assertThat(createdAt.isBefore(promotedAt)).isTrue();
    }

    @Test
    @DisplayName("리더가 아닌 유저가 끌어올리기 요청을 보내면 에러를 반환한다.")
    void promoteSteadyByAnotherUserTest() {
        // given
        var position = positionRepository.save(createPosition());
        var leader = userRepository.save(createFirstUser(position));
        var stack = stackRepository.save(createStack());
        var userInfo = createUserInfo(leader.getId());

        var steadyRequest = createSteadyRequest(stack.getId(), position.getId());
        var steadyId = steadyService.create(steadyRequest, userInfo);
        entityManager.flush();
        entityManager.clear();

        // when & then
        User anotherUser = userRepository.save(createSecondUser(position));
        var anotherUserInfo = createUserInfo(anotherUser.getId());
        assertThatThrownBy(() -> steadyService.promoteSteady(steadyId, anotherUserInfo))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("스테디 리더가 스테디를 종료 상태로 변경할 수 있다.")
    void finishSteadyTest() {
        // given
        var position = positionRepository.save(createPosition());
        var leader = userRepository.save(createFirstUser(position));
        var stack = stackRepository.save(createStack());
        var userInfo = createUserInfo(leader.getId());

        var steadyRequest = createSteadyRequest(stack.getId(), position.getId());
        var steadyId = steadyService.create(steadyRequest, userInfo);
        entityManager.flush();
        entityManager.clear();

        // when
        steadyService.finishSteady(steadyId, userInfo);
        entityManager.flush();
        entityManager.clear();

        // then
        Steady steady = steadyRepository.findById(steadyId).get();
        SteadyStatus status = steady.getStatus();
        assertThat(status).isEqualTo(SteadyStatus.FINISHED);
    }

    @Test
    @DisplayName("리더가 아닌 유저가 스테디 종료를 요청하면 에러를 반환한다.")
    void finishSteadyByAnotherUserTest() {
        // given
        var position = positionRepository.save(createPosition());
        var leader = userRepository.save(createFirstUser(position));
        var stack = stackRepository.save(createStack());
        var userInfo = createUserInfo(leader.getId());

        var steadyRequest = createSteadyRequest(stack.getId(), position.getId());
        var steadyId = steadyService.create(steadyRequest, userInfo);

        var anotherUser = userRepository.save(createSecondUser(position));
        var anotherUserInfo = createUserInfo(anotherUser.getId());
        entityManager.flush();
        entityManager.clear();

        // when & then
        assertThatThrownBy(() -> steadyService.finishSteady(steadyId, anotherUserInfo))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("스테디 참여자가 리더뿐이며 리더가 삭제 요청을 보내면 스테디를 삭제할 수 있다.")
    void deleteSteadyTest() {
        // given
        var position = positionRepository.save(createPosition());
        var leader = userRepository.save(createFirstUser(position));
        var stack = stackRepository.save(createStack());
        var userInfo = createUserInfo(leader.getId());

        var steadyRequest = createSteadyRequest(stack.getId(), position.getId());
        var steadyId = steadyService.create(steadyRequest, userInfo);
        entityManager.flush();
        entityManager.clear();

        // when
        steadyService.deleteSteady(steadyId, userInfo);
        entityManager.flush();
        entityManager.clear();

        // then
        int expectedSize = 0;
        List<Participant> participants = participantRepository.findBySteadyId(steadyId);
        List<SteadyStack> steadyStacks = steadyStackRepository.findBySteadyId(steadyId);
        List<SteadyPosition> steadyPositions = steadyPositionRepository.findBySteadyId(steadyId);
        List<SteadyQuestion> steadyQuestions = steadyQuestionRepository.findBySteadyId(steadyId);
        assertAll(
                () -> assertThatThrownBy(() -> steadyRepository.getSteady(steadyId))
                        .isInstanceOf(NotFoundException.class),
                () -> assertThat(participants).hasSize(expectedSize),
                () -> assertThat(steadyStacks).hasSize(expectedSize),
                () -> assertThat(steadyPositions).hasSize(expectedSize),
                () -> assertThat(steadyQuestions).hasSize(expectedSize)
        );
    }

    @Test
    @DisplayName("리더를 제외한 참여자가 존재하는 경우 스테디를 삭제할 수 없다.")
    void deleteSteadyWhenParticipantIsExistTest() {
        // given
        var position = positionRepository.save(createPosition());
        var leader = userRepository.save(createFirstUser(position));
        var stack = stackRepository.save(createStack());
        var userInfo = createUserInfo(leader.getId());

        var steadyRequest = createSteadyRequest(stack.getId(), position.getId());
        var steadyId = steadyService.create(steadyRequest, userInfo);

        var steady = steadyRepository.findById(steadyId).get();
        var anotherUser = userRepository.save(createSecondUser(position));

        // when
        steady.addParticipantByLeader(leader, anotherUser);
        entityManager.flush();
        entityManager.clear();

        // then
        assertThatThrownBy(() -> steadyService.deleteSteady(steadyId, userInfo))
                .isInstanceOf(InvalidStateException.class);
    }

    @Test
    @DisplayName("리더가 아닌 유저가 스테디 삭제 요청을 보내면 에러를 반환한다.")
    void deleteSteadyByAnotherUserTest() {
        // given
        var position = positionRepository.save(createPosition());
        var leader = userRepository.save(createFirstUser(position));
        var stack = stackRepository.save(createStack());
        var userInfo = createUserInfo(leader.getId());

        var steadyRequest = createSteadyRequest(stack.getId(), position.getId());
        var steadyId = steadyService.create(steadyRequest, userInfo);

        var anotherUser = userRepository.save(createSecondUser(position));
        var anotherUserInfo = createUserInfo(anotherUser.getId());
        entityManager.flush();
        entityManager.clear();

        // when & then
        assertThatThrownBy(() -> steadyService.deleteSteady(steadyId, anotherUserInfo))
                .isInstanceOf(ForbiddenException.class);
    }

    @DisplayName("필터링 조건에 따라 내 스테디를 조회한다.")
    @Test
    void findMySteadiesTest() {
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
        UserInfo userInfo = new UserInfo(user.getId());
        SliceResponse<MySteadyResponse> response = steadyService.findMySteadies(RECRUITING, userInfo, pageRequest);
        //then
        Assertions.assertAll(
                () -> assertThat(response.hasNext()).isFalse(),
                () -> assertThat(response.numberOfElements()).isEqualTo(2),
                () -> assertThat(response.content()).hasSize(2)
                        .extracting("steadyId")
                        .contains(steady.getId(), secondSteady.getId())
        );
    }

}
