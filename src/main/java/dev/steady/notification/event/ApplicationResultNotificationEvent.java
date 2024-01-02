package dev.steady.notification.event;

import dev.steady.application.domain.Application;
import dev.steady.application.domain.ApplicationStatus;
import dev.steady.notification.domain.NotificationResult;
import dev.steady.notification.domain.NotificationType;
import dev.steady.steady.domain.Steady;

public class ApplicationResultNotificationEvent extends NotificationEvent {

    private final Steady steady;
    private final ApplicationStatus status;

    public ApplicationResultNotificationEvent(Application application) {
        super(application.getUser(), NotificationType.APPLICATION_RESULT);
        this.steady = application.getSteady();
        this.status = application.getStatus();
    }

    @Override
    public String getName() {
        return steady.getName();
    }

    @Override
    public NotificationResult getResult() {
        if (status == ApplicationStatus.ACCEPTED) {
            return NotificationResult.ACCEPTED;
        }
        return NotificationResult.REJECTED;
    }

}
