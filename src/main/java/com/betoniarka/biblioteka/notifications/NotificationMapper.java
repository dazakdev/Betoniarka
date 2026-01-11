package com.betoniarka.biblioteka.notifications;

import com.betoniarka.biblioteka.notifications.dto.NotificationResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface NotificationMapper {

    NotificationResponseDto toDto(Notification source);

}
