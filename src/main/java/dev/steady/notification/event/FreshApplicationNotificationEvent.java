package dev.steady.notification.event;

import dev.steady.notification.domain.NotificationResult;
import dev.steady.notification.domain.NotificationType;
import dev.steady.steady.domain.Steady;

public class FreshApplicationNotificationEvent extends NotificationEvent {

    private final Steady steady;

    public FreshApplicationNotificationEvent(Steady steady) {
        super(steady.getLeader(), NotificationType.FRESH_APPLICATION);
        this.steady = steady;
    }

    @Override
    public String getName() {
        return steady.getName();
    }

    @Override
    public NotificationResult getResult() {
        return NotificationResult.ARRIVED;
    }

}
