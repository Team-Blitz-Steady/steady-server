package dev.steady.notification.service;

import dev.steady.application.domain.ApplicationStatus;
import dev.steady.application.domain.repository.ApplicationRepository;
import dev.steady.application.domain.repository.SurveyResultRepository;
import dev.steady.application.dto.request.ApplicationStatusUpdateRequest;
import dev.steady.application.service.ApplicationService;
import dev.steady.global.exception.NotFoundException;
import dev.steady.notification.domain.Notification;
import dev.steady.notification.domain.repository.NotificationRepository;
import dev.steady.notification.dto.NotificationsResponse;
import dev.steady.notification.event.ApplicationResultNotificationEvent;
import dev.steady.notification.event.FreshApplicationNotificationEvent;
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
import static dev.steady.notification.fixture.NotificationFixture.createApplicationResultEvent;
import static dev.steady.notification.fixture.NotificationFixture.createFreshApplicationEvent;
import static dev.steady.steady.fixture.SteadyFixturesV2.createSteady;
import static dev.steady.user.fixture.UserFixturesV2.generatePosition;
import static dev.steady.user.fixture.UserFixturesV2.generateStack;
import static dev.steady.user.fixture.UserFixturesV2.generateUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest
class NotificationServiceTest {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private PositionRepository positionRepository;

    @Autowired
    private StackRepository stackRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private SurveyResultRepository surveyResultRepository;

    @Autowired
    private SteadyRepository steadyRepository;

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
        surveyResultRepository.deleteAll();
        applicationRepository.deleteAll();
        steadyRepository.deleteAll();
        notificationRepository.deleteAll();
        userRepository.deleteAll();
        positionRepository.deleteAll();
        stackRepository.deleteAll();
    }

    @Test
    @DisplayName("새로운 신청서에 대한 알림을 생성할 수 있다.")
    void createNewApplicationNotificationTest() {
        // given
        var steady = steadyRepository.save(createSteady(leader, List.of(stack)));
        var notificationEvent = new FreshApplicationNotificationEvent(steady);

        // when
        notificationService.create(notificationEvent);

        // then
        Notification notification = notificationRepository.findByReceiverId(leader.getId()).get(0);
        assertAll(
                () -> assertThat(notification.getName()).isEqualTo(steady.getName()),
                () -> assertThat(notification.getResult()).isEqualTo(notificationEvent.getResult())
        );
    }

    @Test
    @DisplayName("신청서 결과에 대한 알림을 생성할 수 있다.")
    void createAcceptedApplicationNotificationTest() {
        // given
        var steady = steadyRepository.save(createSteady(leader, List.of(stack)));
        var user = userRepository.save(generateUser(position));
        var application = createApplication(user, steady);
        application.updateStatus(ApplicationStatus.ACCEPTED, leader);
        var notificationEvent = new ApplicationResultNotificationEvent(application);

        // when
        notificationService.create(notificationEvent);

        // then
        Notification notification = notificationRepository.findByReceiverId(user.getId()).get(0);
        assertAll(
                () -> assertThat(notification.getName()).isEqualTo(steady.getName()),
                () -> assertThat(notification.getResult()).isEqualTo(notificationEvent.getResult())
        );
    }

    @Test
    @DisplayName("전체 알림을 가져올 수 있다.")
    void getNotificationsTest() {
        // given
        var userInfo = createUserInfo(leader.getId());
        steadyRepository.save(createSteady(leader, List.of(stack)));
        notificationRepository.save(createFreshApplicationEvent(leader));
        notificationRepository.save(createApplicationResultEvent(leader));

        // when
        NotificationsResponse notifications = notificationService.getNotifications(userInfo);

        // then
        int expectedSize = 2;
        assertThat(notifications.notifications()).hasSize(expectedSize);
    }

    @Test
    @DisplayName("알림을 읽음 상태로 변경할 수 있다.")
    void readNotificaitonTest() {
        // given
        var userInfo = createUserInfo(leader.getId());
        var notification = notificationRepository.save(createFreshApplicationEvent(leader));

        // when
        notificationService.readNotification(notification.getId(), userInfo);

        // then
        Notification readNotification = notificationRepository.getById(notification.getId());
        assertThat(readNotification.isRead()).isTrue();
    }

    @Test
    @DisplayName("모든 알림을 읽음 상태로 변경할 수 있다.")
    void readNotificaitonsTest() {
        // given
        var user = userRepository.save(generateUser(position));
        var userInfo = createUserInfo(user.getId());
        notificationRepository.save(createFreshApplicationEvent(user));
        notificationRepository.save(createFreshApplicationEvent(user));

        // when
        notificationService.readNotifications(userInfo);

        // then
        List<Notification> notifications = notificationRepository.findByReceiverId(user.getId());
        assertThat(notifications).extracting("isRead").containsOnly(true);
    }

    @Test
    @DisplayName("알림을 삭제할 수 있다.")
    void deleteNotificaitonTest() {
        // given
        var userInfo = createUserInfo(leader.getId());
        Notification notification = notificationRepository.save(createFreshApplicationEvent(leader));

        // when
        notificationService.deleteNotification(notification.getId(), userInfo);

        // then
        assertThatThrownBy(() -> notificationRepository.getById(notification.getId()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("모든 알림을 삭제할 수 있다.")
    void deleteNotificaitonsTest() {
        // given
        var userInfo = createUserInfo(leader.getId());
        notificationRepository.save(createFreshApplicationEvent(leader));
        notificationRepository.save(createFreshApplicationEvent(leader));

        // when
        notificationService.deleteAll(userInfo);

        // then
        assertThat(notificationRepository.findByReceiverId(leader.getId())).isEmpty();
    }

}
