package dev.steady.notification.event;

import dev.steady.application.domain.repository.ApplicationRepository;
import dev.steady.application.domain.repository.SurveyResultRepository;
import dev.steady.application.dto.request.ApplicationStatusUpdateRequest;
import dev.steady.application.service.ApplicationService;
import dev.steady.notification.domain.Notification;
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

import java.util.List;

import static dev.steady.application.domain.ApplicationStatus.ACCEPTED;
import static dev.steady.application.fixture.ApplicationFixture.createApplication;
import static dev.steady.application.fixture.SurveyResultFixture.createSurveyResultRequests;
import static dev.steady.global.auth.AuthFixture.createUserInfo;
import static dev.steady.steady.fixture.SteadyFixturesV2.createSteady;
import static dev.steady.user.fixture.UserFixturesV2.generatePosition;
import static dev.steady.user.fixture.UserFixturesV2.generateStack;
import static dev.steady.user.fixture.UserFixturesV2.generateUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class NotificationEventListenerTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StackRepository stackRepository;

    @Autowired
    private SteadyRepository steadyRepository;

    @Autowired
    private PositionRepository positionRepository;

    @Autowired
    private SurveyResultRepository surveyResultRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private ApplicationService applicationService;

    private Position position;
    private Stack stack;
    private User leader;

    @BeforeEach
    void setUp() {
        this.position = positionRepository.save(generatePosition());
        this.stack = stackRepository.save(generateStack());
        this.leader = userRepository.save(generateUser(position));
    }

    @AfterEach
    void tearDown() {
        surveyResultRepository.deleteAll();
        applicationRepository.deleteAll();
        steadyRepository.deleteAll();
        notificationRepository.deleteAll();
        userRepository.deleteAll();
        stackRepository.deleteAll();
        positionRepository.deleteAll();
    }

    @Test
    @DisplayName("새로운 신청서가 등록되면 리더에게 새로운 신청 알림이 생성된다.")
    void createNotificationWhenCreateApplicationTest() throws InterruptedException {
        //given
        var steady = steadyRepository.save(createSteady(leader, List.of(stack)));
        var user = userRepository.save(generateUser(position));
        var surveyResultRequests = createSurveyResultRequests();
        var userInfo = createUserInfo(user.getId());

        //when
        applicationService.createApplication(steady.getId(), surveyResultRequests, userInfo);
        Thread.sleep(500);

        //then
        Notification notification = notificationRepository.findByReceiverId(leader.getId()).get(0);
        assertAll(
                () -> assertThat(notification).isNotNull(),
                () -> assertThat(notification.getReceiver().getId()).isEqualTo(leader.getId())
        );
    }

    @Test
    @DisplayName("신청서가 거절 혹은 수락되면 유저에게 새로운 신청서 결과 알림이 생성된다.")
    void createNotificationWhenApplicationGotResultTest() throws InterruptedException {
        //given
        var steady = steadyRepository.save(createSteady(leader, List.of(stack)));
        var user = userRepository.save(generateUser(position));
        var application = applicationRepository.save(createApplication(user, steady));
        var userInfo = createUserInfo(leader.getId());
        var request = new ApplicationStatusUpdateRequest(ACCEPTED);

        //when
        applicationService.updateStatusOfApplication(application.getId(), request, userInfo);
        Thread.sleep(500);

        //then
        Notification notification = notificationRepository.findByReceiverId(user.getId()).get(0);
        assertAll(
                () -> assertThat(notification).isNotNull(),
                () -> assertThat(notification.getReceiver().getId()).isEqualTo(user.getId())
        );
    }

}
