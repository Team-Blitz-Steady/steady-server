package dev.steady.notification.event;

import dev.steady.notification.domain.Notification;
import dev.steady.notification.domain.NotificationResult;
import dev.steady.notification.domain.NotificationType;
import dev.steady.user.domain.User;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class NotificationEvent {

    private final User receiver;
    private final NotificationType type;

    public abstract String getName();

    public abstract NotificationResult getResult();

    public Notification toEntity() {
        return Notification.builder()
                .name(getName())
                .type(type)
                .result(getResult())
                .receiver(receiver)
                .build();
    }

}
