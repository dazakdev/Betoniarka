package com.betoniarka.biblioteka.notifications;

import com.betoniarka.biblioteka.appuser.AppUser;
import com.betoniarka.biblioteka.mail.EmailService;
import com.betoniarka.biblioteka.notifications.dto.NotificationResponseDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository repository;
    private final NotificationMapper mapper;
    private final EmailService emailService;

    public List<NotificationResponseDto> getByUserId(long id) {
        var notifications = repository.getNotificationsByAppUser_Id(id);
        return notifications.stream().map(mapper::toDto).toList();
    }

    public NotificationResponseDto create(String message, AppUser appUser) {
        var notification = new Notification(message);
        appUser.addNotification(notification);
        repository.save(notification);

        emailService.send(appUser.getEmail(), "New Notification", message);

        return mapper.toDto(notification);
    }

}
