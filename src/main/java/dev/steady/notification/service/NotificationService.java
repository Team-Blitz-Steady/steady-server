package dev.steady.notification.service;

import dev.steady.global.auth.UserInfo;
import dev.steady.notification.domain.Notification;
import dev.steady.notification.domain.repository.NotificationRepository;
import dev.steady.notification.dto.NotificationsResponse;
import dev.steady.notification.event.NotificationEvent;
import dev.steady.user.domain.User;
import dev.steady.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void create(NotificationEvent event) {
        Notification notification = event.toEntity();
        notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public NotificationsResponse getNotifications(UserInfo userInfo) {
        User receiver = userRepository.getUserBy(userInfo.userId());
        List<Notification> notifications = notificationRepository.findByReceiverId(receiver.getId());
        return NotificationsResponse.from(notifications);
    }

    @Transactional
    public void readNotification(Long notificationId, UserInfo userInfo) {
        userRepository.getUserBy(userInfo.userId());
        Notification notification = notificationRepository.getById(notificationId);
        notification.read();
    }

    @Transactional
    public void readNotifications(UserInfo userInfo) {
        User receiver = userRepository.getUserBy(userInfo.userId());
        List<Notification> notifications = notificationRepository.findByReceiverId(receiver.getId());
        notifications.forEach(v -> v.read());
    }

    @Transactional
    public void deleteNotification(Long notificationId, UserInfo userInfo) {
        userRepository.getUserBy(userInfo.userId());
        notificationRepository.deleteById(notificationId);
    }

    @Transactional
    public void deleteAll(UserInfo userInfo) {
        User receiver = userRepository.getUserBy(userInfo.userId());
        notificationRepository.deleteByReceiverId(receiver.getId());
    }

}
