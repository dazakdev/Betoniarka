package com.betoniarka.biblioteka.notifications;

import com.betoniarka.biblioteka.notifications.dto.NotificationResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("appusers/{id}/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService service;

    @GetMapping
    public List<NotificationResponseDto> getNotifications(@PathVariable Long id) {
        return service.getByUserId(id);
    }

}
