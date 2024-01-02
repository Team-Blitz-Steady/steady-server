package dev.steady.notification.dto;

import dev.steady.notification.domain.Notification;
import dev.steady.notification.domain.NotificationResult;
import dev.steady.notification.domain.NotificationType;

public record NotificationResponse(
        Long id,
        String name,
        NotificationType type,
        NotificationResult result,
        boolean isRead
) {

    public static NotificationResponse from(Notification entity) {
        return new NotificationResponse(entity.getId(),
                entity.getName(),
                entity.getType(),
                entity.getResult(),
                entity.isRead());
    }

}
