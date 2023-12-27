package dev.steady.notification.fixture;

import dev.steady.notification.domain.Notification;
import dev.steady.notification.domain.NotificationResult;
import dev.steady.notification.domain.NotificationType;
import dev.steady.notification.dto.NotificationResponse;
import dev.steady.notification.dto.NotificationsResponse;
import dev.steady.user.domain.User;

import java.util.List;

public class NotificationFixture {

    public static Notification createFreshApplicationEvent(User receiver) {
        return Notification.builder()
                .name("테스트 스테디")
                .type(NotificationType.FRESH_APPLICATION)
                .result(NotificationResult.ARRIVED)
                .receiver(receiver)
                .build();
    }

    public static Notification createApplicationResultEvent(User receiver) {
        return Notification.builder()
                .name("테스트 스테디")
                .type(NotificationType.APPLICATION_RESULT)
                .result(NotificationResult.ACCEPTED)
                .receiver(receiver)
                .build();
    }

    public static NotificationsResponse createNotificationsResponse() {
        return new NotificationsResponse(List.of(
                new NotificationResponse(1L, "테스트 스테디", NotificationType.FRESH_APPLICATION,
                        NotificationResult.ARRIVED,false),
                new NotificationResponse(1L, "테스트 스테디", NotificationType.FRESH_APPLICATION,
                        NotificationResult.ARRIVED, true)
        ), 1L);
    }

}
