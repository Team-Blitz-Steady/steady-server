package dev.steady.application.service;

import dev.steady.application.domain.Application;
import dev.steady.application.domain.ApplicationStatus;
import dev.steady.application.domain.SurveyResult;
import dev.steady.application.domain.repository.ApplicationRepository;
import dev.steady.application.domain.repository.SurveyResultRepository;
import dev.steady.application.dto.request.ApplicationStatusUpdateRequest;
import dev.steady.application.dto.response.ApplicationDetailResponse;
import dev.steady.application.dto.response.ApplicationSummaryResponse;
import dev.steady.application.dto.response.CreateApplicationResponse;
import dev.steady.application.dto.response.MyApplicationSummaryResponse;
import dev.steady.application.dto.response.SliceResponse;
import dev.steady.global.auth.UserInfo;
import dev.steady.global.exception.DuplicateException;
import dev.steady.global.exception.ForbiddenException;
import dev.steady.global.exception.NotFoundException;
import dev.steady.notification.domain.repository.NotificationRepository;
import dev.steady.steady.domain.repository.SteadyRepository;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static dev.steady.application.domain.ApplicationStatus.ACCEPTED;
import static dev.steady.application.domain.ApplicationStatus.REJECTED;
import static dev.steady.application.fixture.ApplicationFixture.createApplication;
import static dev.steady.application.fixture.ApplicationFixture.createSurveyResults;
import static dev.steady.application.fixture.SurveyResultFixture.createAnswers;
import static dev.steady.application.fixture.SurveyResultFixture.createSurveyResultRequests;
import static dev.steady.global.auth.AuthFixture.createUserInfo;
import static dev.steady.steady.fixture.SteadyFixturesV2.createSteady;
import static dev.steady.user.fixture.UserFixturesV2.generatePosition;
import static dev.steady.user.fixture.UserFixturesV2.generateStack;
import static dev.steady.user.fixture.UserFixturesV2.generateUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.groups.Tuple.tuple;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest
class ApplicationServiceTest {

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SteadyRepository steadyRepository;

    @Autowired
    private PositionRepository positionRepository;

    @Autowired
    private StackRepository stackRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private SurveyResultRepository surveyResultRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    private Position position;
    private User leader;
    private Stack stack;

    @BeforeEach
    void setUp() {
        this.position = positionRepository.save(generatePosition());
        this.leader = userRepository.save(generateUser(position));
        this.stack = stackRepository.save(generateStack());
    }

    @AfterEach
    void tearDown() {
        notificationRepository.deleteAll();
        surveyResultRepository.deleteAll();
        applicationRepository.deleteAll();
        steadyRepository.deleteAll();
        userRepository.deleteAll();
        positionRepository.deleteAll();
        stackRepository.deleteAll();
    }

    @DisplayName("스테디 신청을 받아 신청서를 생성한다.")
    @Test
    void createApplicationTest() {
        //given
        var steady = steadyRepository.save(createSteady(leader, List.of(stack)));
        var surveyResultRequests = createSurveyResultRequests();
        var otherUser = userRepository.save(generateUser(position));
        var userInfo = createUserInfo(otherUser.getId());

        //when
        CreateApplicationResponse response = applicationService.createApplication(steady.getId(),
                surveyResultRequests,
                userInfo);

        //then
        assertThat(response.applicationId()).isNotNull();
    }

    @DisplayName("신청서를 중복으로 제출하면 중복 예외가 발생한다")
    @Test
    void duplicateApplicationTest() {
        //given
        var steady = steadyRepository.save(createSteady(leader, List.of(stack)));
        var surveyResultRequests = createSurveyResultRequests();
        var otherUser = userRepository.save(generateUser(position));
        var userInfo = createUserInfo(otherUser.getId());

        //when
        applicationService.createApplication(steady.getId(),
                surveyResultRequests,
                userInfo);

        //then
        assertThatThrownBy(
                () -> applicationService.createApplication(steady.getId(),
                        surveyResultRequests,
                        userInfo)
        ).isInstanceOf(DuplicateException.class);
    }

    @DisplayName("모집글 작성자가 신청서를 제출하면 예외가 발생한다")
    @Test
    void leaderSubmissionTest() {
        //given
        var steady = steadyRepository.save(createSteady(leader, List.of(stack)));
        var surveyResultRequests = createSurveyResultRequests();
        var userInfo = createUserInfo(leader.getId());

        //when
        //then
        assertThatThrownBy(
                () -> applicationService.createApplication(steady.getId(),
                        surveyResultRequests,
                        userInfo)
        ).isInstanceOf(ForbiddenException.class);
    }

    @DisplayName("스터디 리더는 신청서 목록 조회를 할 수 있다.")
    @Test
    void getApplicationsTest() {
        //given
        var steady = steadyRepository.save(createSteady(leader, List.of(stack)));
        var secondUser = userRepository.save(generateUser(position));
        var thirdUser = userRepository.save(generateUser(position));
        applicationRepository.saveAll(List.of(createApplication(secondUser, steady),
                createApplication(thirdUser, steady)));
        //when
        SliceResponse<ApplicationSummaryResponse> response = applicationService.getApplications(steady.getId(),
                createUserInfo(leader.getId()),
                PageRequest.of(0, 10));
        //then
        assertAll(
                () -> assertThat(response.numberOfElements()).isEqualTo(2),
                () -> assertThat(response.hasNext()).isFalse(),
                () -> assertThat(response.content()).hasSize(2)
                        .extracting(ApplicationSummaryResponse::nickname)
                        .containsExactly(secondUser.getNickname(), thirdUser.getNickname()));
    }

    @DisplayName("스터디 리더가 아니면 신청서 목록 조회를 할 수 없다.")
    @Test
    void getApplicationsFailTest() {
        //given
        var steady = steadyRepository.save(createSteady(leader, List.of(stack)));
        var secondUser = userRepository.save(generateUser(position));
        var thirdUser = userRepository.save(generateUser(position));
        applicationRepository.save(createApplication(secondUser, steady));
        var userInfo = createUserInfo(thirdUser.getId());
        var pageRequest = PageRequest.of(0, 10);
        //when
        //then
        assertThatThrownBy(() -> applicationService.getApplications(steady.getId(),
                userInfo,
                pageRequest)
        ).isInstanceOf(ForbiddenException.class);
    }

    @DisplayName("스테디 리더는 신청서를 상세조회할 수 있다.")
    @Test
    void getApplicationDetailTest() {
        //given
        var steady = steadyRepository.save(createSteady(leader, List.of(stack)));
        var secondUser = userRepository.save(generateUser(position));
        var application = applicationRepository.save(createApplication(secondUser, steady));
        surveyResultRepository.saveAll(createSurveyResults(application));
        var userInfo = createUserInfo(leader.getId());
        //when
        ApplicationDetailResponse response = applicationService.getApplicationDetail(application.getId(), userInfo);
        //then
        assertThat(response.surveys()).hasSize(3)
                .extracting("question", "answer")
                .containsExactly(
                        tuple("질문1", "답변1"),
                        tuple("질문2", "답변2"),
                        tuple("질문3", "답변3")
                );
    }

    @DisplayName("스테디 리더가 아니면 신청서를 상세조회할 수 없다.")
    @Test
    void getApplicationDetailFailTest() {
        //given
        var steady = steadyRepository.save(createSteady(leader, List.of(stack)));
        var secondUser = userRepository.save(generateUser(position));
        var thirdUser = userRepository.save(generateUser(position));
        var application = applicationRepository.save(createApplication(secondUser, steady));
        var userInfo = createUserInfo(thirdUser.getId());
        //when
        //then
        assertThatThrownBy(() -> applicationService.getApplicationDetail(application.getId(), userInfo))
                .isInstanceOf(ForbiddenException.class);
    }

    @DisplayName("스테디 리더는 신청서의 상태가 WAITING 일 때 상태를 변경할 수 있다.")
    @Test
    void createSurveyResultTest() {
        //given
        var steady = steadyRepository.save(createSteady(leader, List.of(stack)));
        var secondUser = userRepository.save(generateUser(position));
        var application = applicationRepository.save(createApplication(secondUser, steady));
        var userInfo = createUserInfo(leader.getId());
        var request = new ApplicationStatusUpdateRequest(ACCEPTED);
        //when
        applicationService.updateStatusOfApplication(application.getId(), request, userInfo);
        //then
        Application foundApplication = applicationRepository.getById(application.getId());
        ApplicationStatus status = foundApplication.getStatus();
        assertThat(status).isEqualTo(ACCEPTED);
    }

    @DisplayName("스테디 리더는 신청서의 상태가 WAITING가 아닐 때 상태를 변경할 수 없다.")
    @Test
    void createSurveyResultFailTest() {
        //given
        var steady = steadyRepository.save(createSteady(leader, List.of(stack)));
        var secondUser = userRepository.save(generateUser(position));
        var entity = createApplication(secondUser, steady);
        ReflectionTestUtils.setField(entity, "status", REJECTED);
        var application = applicationRepository.save(entity);
        var userInfo = createUserInfo(leader.getId());
        var request = new ApplicationStatusUpdateRequest(ACCEPTED);
        //when
        //then
        assertThatThrownBy(() -> {
            applicationService.updateStatusOfApplication(application.getId(), request, userInfo);
        }).isInstanceOf(ForbiddenException.class);
    }

    @DisplayName("신청자는 본인이 제출한 신청서의 답변을 수정할 수 있다.")
    @Test
    void updateApplicationAnswerTest() {
        //given
        var steady = steadyRepository.save(createSteady(leader, List.of(stack)));
        var secondUser = userRepository.save(generateUser(position));
        var application = createApplication(secondUser, steady);
        var savedApplication = applicationRepository.save(application);
        surveyResultRepository.saveAll(createSurveyResults(savedApplication));
        var applicationId = savedApplication.getId();
        var answers = createAnswers();
        var userInfo = new UserInfo(secondUser.getId());

        //when
        applicationService.updateApplicationAnswer(applicationId, answers, userInfo);

        //then
        List<SurveyResult> surveyResults = surveyResultRepository.findByApplicationOrderBySequenceAsc(savedApplication);
        assertThat(surveyResults).hasSameSizeAs(answers.answers())
                .extracting("answer")
                .containsExactly("답변100", "답변101", "답변102");
    }

    @DisplayName("신청자는 본인이 제출한 신청서의 답변을 수정할 수 있다.")
    @Test
    void updateApplicationAnswerTest2() {
        //given
        var steady = steadyRepository.save(createSteady(leader, List.of(stack)));
        var secondUser = userRepository.save(generateUser(position));
        var application = createApplication(secondUser, steady);
        var savedApplication = applicationRepository.save(application);
        surveyResultRepository.saveAll(createSurveyResults(savedApplication));
        var applicationId = savedApplication.getId();
        var answers = createAnswers();
        var userInfo = new UserInfo(leader.getId());

        //when
        //then
        assertThatThrownBy(() -> applicationService.updateApplicationAnswer(applicationId, answers, userInfo))
                .isInstanceOf(ForbiddenException.class);
    }

    @DisplayName("신청자는 본인이 신청한 신청서 리스트를 조회할 수 있다.")
    @Test
    void getMyApplications() {
        var steady = steadyRepository.save(createSteady(leader, List.of(stack)));
        var secondUser = userRepository.save(generateUser(position));
        var application = createApplication(secondUser, steady);
        var savedApplication = applicationRepository.save(application);
        var userInfo = new UserInfo(secondUser.getId());
        var pageRequest = PageRequest.of(0, 10);
        //when
        SliceResponse<MyApplicationSummaryResponse> response = applicationService.getMyApplications(userInfo, pageRequest);

        //then
        List<MyApplicationSummaryResponse> actual = response.content();
        assertThat(actual).extracting("applicationId", "steadyName")
                .contains(tuple(savedApplication.getId(), steady.getName()));
    }

    @DisplayName("본인의 신청서를 삭제할 수 있고 등록된 질답도 함께 삭제된다.")
    @Test
    void deleteApplication() {
        var steady = steadyRepository.save(createSteady(leader, List.of(stack)));
        var secondUser = userRepository.save(generateUser(position));
        var application = createApplication(secondUser, steady);
        var savedApplication = applicationRepository.save(application);
        var userInfo = new UserInfo(secondUser.getId());
        //when
        applicationService.deleteApplication(savedApplication.getId(), userInfo);

        //then
        assertThatThrownBy(() -> applicationRepository.getById(application.getId()))
                .isInstanceOf(NotFoundException.class);
    }

}
